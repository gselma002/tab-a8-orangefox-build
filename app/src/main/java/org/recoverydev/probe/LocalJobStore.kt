package org.recoverydev.probe

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * App-private, durable job metadata store.
 *
 * Stores bounded metadata only: no command text, raw stdout/stderr, credentials,
 * source contents, or artifact bytes. This is a storage primitive, not an
 * authorization boundary, broker, or execution service.
 */
class LocalJobStore(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    data class Record(
        val requestId: String,
        val operation: JobOperation,
        val state: JobState,
        val createdAtEpochSeconds: Long,
        val updatedAtEpochSeconds: Long,
        val expiresAtEpochSeconds: Long,
        val workspaceId: String?,
        val artifactId: String?,
        val summary: String?
    )

    class DuplicateRequestException : IllegalStateException(
        "A job with this request ID already exists"
    )

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_JOBS (
                $COLUMN_REQUEST_ID TEXT PRIMARY KEY NOT NULL
                    CHECK(length($COLUMN_REQUEST_ID) BETWEEN 8 AND 64),
                $COLUMN_OPERATION TEXT NOT NULL CHECK($COLUMN_OPERATION IN (
                    'DEVICE_INVENTORY', 'TOOL_INVENTORY', 'REPOSITORY_STATUS',
                    'PINNED_SOURCE_CHECKOUT', 'UNIT_TESTS', 'ANDROID_APP_BUILD',
                    'APK_INSPECTION'
                )),
                $COLUMN_STATE TEXT NOT NULL CHECK($COLUMN_STATE IN (
                    'RECEIVED', 'VALIDATING', 'AWAITING_APPROVAL', 'QUEUED',
                    'RUNNING', 'CANCELLING', 'SUCCEEDED', 'FAILED', 'CANCELLED',
                    'EXPIRED', 'REJECTED', 'INTERRUPTED'
                )),
                $COLUMN_CREATED_AT INTEGER NOT NULL CHECK($COLUMN_CREATED_AT >= 0),
                $COLUMN_UPDATED_AT INTEGER NOT NULL
                    CHECK($COLUMN_UPDATED_AT >= $COLUMN_CREATED_AT),
                $COLUMN_EXPIRES_AT INTEGER NOT NULL CHECK($COLUMN_EXPIRES_AT > 0),
                $COLUMN_WORKSPACE_ID TEXT
                    CHECK($COLUMN_WORKSPACE_ID IS NULL OR length($COLUMN_WORKSPACE_ID) BETWEEN 1 AND 64),
                $COLUMN_ARTIFACT_ID TEXT
                    CHECK($COLUMN_ARTIFACT_ID IS NULL OR length($COLUMN_ARTIFACT_ID) BETWEEN 1 AND 64),
                $COLUMN_SUMMARY TEXT
                    CHECK($COLUMN_SUMMARY IS NULL OR length($COLUMN_SUMMARY) <= $MAX_SUMMARY_CHARS)
            )
            """.trimIndent()
        )
        createIndexes(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Preserve existing metadata. Never drop or recreate the jobs table automatically.
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL(
                """
                ALTER TABLE $TABLE_JOBS
                ADD COLUMN $COLUMN_ARTIFACT_ID TEXT
                CHECK($COLUMN_ARTIFACT_ID IS NULL OR length($COLUMN_ARTIFACT_ID) BETWEEN 1 AND 64)
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS jobs_state_expiry_idx " +
                    "ON $TABLE_JOBS ($COLUMN_STATE, $COLUMN_EXPIRES_AT)"
            )
            return
        }
        throw IllegalStateException(
            "Unsupported job database migration: $oldVersion -> $newVersion"
        )
    }

    /** Validates and persists a request as RECEIVED in one transaction. */
    fun create(request: JobRequest, nowEpochSeconds: Long): Record {
        require(nowEpochSeconds >= 0) { "Invalid creation timestamp" }
        val validation = JobRequestValidator.validate(request, nowEpochSeconds)
        require(validation.isValid) {
            "Invalid job request: " + validation.errors.joinToString("; ")
        }

        val values = ContentValues().apply {
            put(COLUMN_REQUEST_ID, request.requestId)
            put(COLUMN_OPERATION, request.operation.name)
            put(COLUMN_STATE, JobState.RECEIVED.name)
            put(COLUMN_CREATED_AT, nowEpochSeconds)
            put(COLUMN_UPDATED_AT, nowEpochSeconds)
            put(COLUMN_EXPIRES_AT, request.expiresAtEpochSeconds)
            putNullable(COLUMN_WORKSPACE_ID, request.parameters["workspace_id"])
            putNullable(COLUMN_ARTIFACT_ID, request.parameters["artifact_id"])
            putNull(COLUMN_SUMMARY)
        }

        val db = writableDatabase
        db.beginTransaction()
        try {
            try {
                db.insertOrThrow(TABLE_JOBS, null, values)
            } catch (error: SQLiteConstraintException) {
                val duplicate = db.query(
                    TABLE_JOBS,
                    arrayOf(COLUMN_REQUEST_ID),
                    "$COLUMN_REQUEST_ID = ?",
                    arrayOf(request.requestId),
                    null,
                    null,
                    null,
                    "1"
                ).use { it.moveToFirst() }
                if (duplicate) throw DuplicateRequestException()
                throw error
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return get(request.requestId)
            ?: throw IllegalStateException("Persisted job could not be read back")
    }

    /**
     * Performs a compare-and-set transition.
     *
     * The timestamp predicate prevents updated_at from moving backwards.
     * userApproved is only a defensive API guard; the broker/UI must enforce
     * real, visible on-device approval before calling this method.
     */
    fun transition(
        requestId: String,
        expectedState: JobState,
        nextState: JobState,
        nowEpochSeconds: Long,
        summary: String? = null,
        userApproved: Boolean = false
    ): Boolean {
        require(requestId.matches(REQUEST_ID_PATTERN)) { "Invalid request ID" }
        require(nowEpochSeconds >= 0) { "Invalid update timestamp" }
        require(JobStateMachine.canTransition(expectedState, nextState)) {
            "Invalid job state transition: $expectedState -> $nextState"
        }
        if (expectedState == JobState.AWAITING_APPROVAL && nextState == JobState.QUEUED) {
            require(userApproved) {
                "Explicit on-device approval is required to queue this job"
            }
        }

        val expiryPredicate = if (nextState == JobState.EXPIRED) {
            "$COLUMN_EXPIRES_AT <= ?"
        } else {
            "$COLUMN_EXPIRES_AT > ?"
        }
        val safeSummary = summary?.let {
            DiagnosticRedactor.redact(it, maxChars = MAX_SUMMARY_CHARS)
        }

        val values = ContentValues().apply {
            put(COLUMN_STATE, nextState.name)
            put(COLUMN_UPDATED_AT, nowEpochSeconds)
            putNullable(COLUMN_SUMMARY, safeSummary)
        }

        val where = listOf(
            "$COLUMN_REQUEST_ID = ?",
            "$COLUMN_STATE = ?",
            "$COLUMN_CREATED_AT <= ?",
            "$COLUMN_UPDATED_AT <= ?",
            expiryPredicate
        ).joinToString(" AND ")

        return writableDatabase.update(
            TABLE_JOBS,
            values,
            where,
            arrayOf(
                requestId,
                expectedState.name,
                nowEpochSeconds.toString(),
                nowEpochSeconds.toString(),
                nowEpochSeconds.toString()
            )
        ) == 1
    }

    fun get(requestId: String): Record? {
        readableDatabase.query(
            TABLE_JOBS,
            RECORD_COLUMNS,
            "$COLUMN_REQUEST_ID = ?",
            arrayOf(requestId),
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toRecord() else null
        }
    }

    fun recent(limit: Int = DEFAULT_RECENT_LIMIT): List<Record> {
        require(limit in 1..MAX_RECENT_LIMIT) {
            "Limit must be between 1 and $MAX_RECENT_LIMIT"
        }
        readableDatabase.query(
            TABLE_JOBS,
            RECORD_COLUMNS,
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC, $COLUMN_REQUEST_ID ASC",
            limit.toString()
        ).use { cursor ->
            return buildList {
                while (cursor.moveToNext()) add(cursor.toRecord())
            }
        }
    }

    private fun android.database.Cursor.toRecord(): Record = Record(
        requestId = getString(getColumnIndexOrThrow(COLUMN_REQUEST_ID)),
        operation = JobOperation.valueOf(getString(getColumnIndexOrThrow(COLUMN_OPERATION))),
        state = JobState.valueOf(getString(getColumnIndexOrThrow(COLUMN_STATE))),
        createdAtEpochSeconds = getLong(getColumnIndexOrThrow(COLUMN_CREATED_AT)),
        updatedAtEpochSeconds = getLong(getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
        expiresAtEpochSeconds = getLong(getColumnIndexOrThrow(COLUMN_EXPIRES_AT)),
        workspaceId = getString(getColumnIndexOrThrow(COLUMN_WORKSPACE_ID)),
        artifactId = getString(getColumnIndexOrThrow(COLUMN_ARTIFACT_ID)),
        summary = getString(getColumnIndexOrThrow(COLUMN_SUMMARY))
    )

    private fun ContentValues.putNullable(key: String, value: String?) {
        if (value == null) putNull(key) else put(key, value)
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE INDEX jobs_created_at_idx ON $TABLE_JOBS ($COLUMN_CREATED_AT DESC)"
        )
        db.execSQL(
            "CREATE INDEX jobs_state_expiry_idx ON $TABLE_JOBS ($COLUMN_STATE, $COLUMN_EXPIRES_AT)"
        )
    }

    companion object {
        private const val DATABASE_NAME = "local_jobs.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_JOBS = "jobs"
        private const val MAX_SUMMARY_CHARS = 512
        private const val DEFAULT_RECENT_LIMIT = 25
        private const val MAX_RECENT_LIMIT = 100
        private val REQUEST_ID_PATTERN = Regex("^[A-Za-z0-9_-]{8,64}$")

        private const val COLUMN_REQUEST_ID = "request_id"
        private const val COLUMN_OPERATION = "operation"
        private const val COLUMN_STATE = "state"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
        private const val COLUMN_EXPIRES_AT = "expires_at"
        private const val COLUMN_WORKSPACE_ID = "workspace_id"
        private const val COLUMN_ARTIFACT_ID = "artifact_id"
        private const val COLUMN_SUMMARY = "summary"

        private val RECORD_COLUMNS = arrayOf(
            COLUMN_REQUEST_ID,
            COLUMN_OPERATION,
            COLUMN_STATE,
            COLUMN_CREATED_AT,
            COLUMN_UPDATED_AT,
            COLUMN_EXPIRES_AT,
            COLUMN_WORKSPACE_ID,
            COLUMN_ARTIFACT_ID,
            COLUMN_SUMMARY
        )
    }
}
