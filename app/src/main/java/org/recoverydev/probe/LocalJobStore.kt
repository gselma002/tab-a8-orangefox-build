package org.recoverydev.probe

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * App-private, durable job metadata store.
 *
 * It stores bounded metadata only: no command text, raw stdout/stderr, credentials,
 * source contents, or artifact bytes. It is not a broker or an execution service.
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
                $COLUMN_REQUEST_ID TEXT PRIMARY KEY NOT NULL,
                $COLUMN_OPERATION TEXT NOT NULL,
                $COLUMN_STATE TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_EXPIRES_AT INTEGER NOT NULL,
                $COLUMN_WORKSPACE_ID TEXT,
                $COLUMN_SUMMARY TEXT,
                CHECK(length($COLUMN_REQUEST_ID) BETWEEN 8 AND 64),
                CHECK(length($COLUMN_OPERATION) BETWEEN 1 AND 40),
                CHECK(length($COLUMN_STATE) BETWEEN 1 AND 24),
                CHECK($COLUMN_CREATED_AT >= 0),
                CHECK($COLUMN_UPDATED_AT >= $COLUMN_CREATED_AT),
                CHECK($COLUMN_EXPIRES_AT > 0),
                CHECK($COLUMN_SUMMARY IS NULL OR length($COLUMN_SUMMARY) <= 512)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX jobs_created_at_idx ON $TABLE_JOBS ($COLUMN_CREATED_AT DESC)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // No destructive automatic migration. Add explicit, versioned migrations here.
        throw IllegalStateException("Unsupported job database migration: $oldVersion -> $newVersion")
    }

    /**
     * Validates and persists a request as RECEIVED in one database transaction.
     * The primary key makes duplicate request IDs fail closed across app restarts.
     */
    fun create(request: JobRequest, nowEpochSeconds: Long): Record {
        val validation = JobRequestValidator.validate(request, nowEpochSeconds)
        require(validation.isValid) {
            "Invalid job request: " + validation.errors.joinToString("; ")
        }
        require(nowEpochSeconds >= 0) { "Invalid creation timestamp" }

        val workspaceId = request.parameters["workspace_id"]
        val values = ContentValues().apply {
            put(COLUMN_REQUEST_ID, request.requestId)
            put(COLUMN_OPERATION, request.operation.name)
            put(COLUMN_STATE, JobState.RECEIVED.name)
            put(COLUMN_CREATED_AT, nowEpochSeconds)
            put(COLUMN_UPDATED_AT, nowEpochSeconds)
            put(COLUMN_EXPIRES_AT, request.expiresAtEpochSeconds)
            if (workspaceId == null) putNull(COLUMN_WORKSPACE_ID)
            else put(COLUMN_WORKSPACE_ID, workspaceId)
            putNull(COLUMN_SUMMARY)
        }

        val db = writableDatabase
        db.beginTransaction()
        try {
            try {
                db.insertOrThrow(TABLE_JOBS, null, values)
            } catch (_: SQLiteConstraintException) {
                throw DuplicateRequestException()
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return get(request.requestId)
            ?: throw IllegalStateException("Persisted job could not be read back")
    }

    /**
     * Compare-and-set transition prevents concurrent callers from overwriting
     * a newer state. Callers must separately enforce visible user approval.
     */
    fun transition(
        requestId: String,
        expectedState: JobState,
        nextState: JobState,
        nowEpochSeconds: Long,
        summary: String? = null
    ): Boolean {
        require(requestId.matches(Regex("^[A-Za-z0-9_-]{8,64}$"))) {
            "Invalid request ID"
        }
        require(nowEpochSeconds >= 0) { "Invalid update timestamp" }
        require(JobStateMachine.canTransition(expectedState, nextState)) {
            "Invalid job state transition: $expectedState -> $nextState"
        }
        val safeSummary = summary?.let {
            DiagnosticRedactor.redact(it, maxChars = MAX_SUMMARY_CHARS)
        }

        val values = ContentValues().apply {
            put(COLUMN_STATE, nextState.name)
            put(COLUMN_UPDATED_AT, nowEpochSeconds)
            if (safeSummary == null) putNull(COLUMN_SUMMARY)
            else put(COLUMN_SUMMARY, safeSummary)
        }
        return writableDatabase.update(
            TABLE_JOBS,
            values,
            "$COLUMN_REQUEST_ID = ? AND $COLUMN_STATE = ? AND $COLUMN_CREATED_AT <= ?",
            arrayOf(requestId, expectedState.name, nowEpochSeconds.toString())
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

    fun recent(limit: Int = 25): List<Record> {
        require(limit in 1..100) { "Limit must be between 1 and 100" }
        readableDatabase.query(
            TABLE_JOBS,
            RECORD_COLUMNS,
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC",
            limit.toString()
        ).use { cursor ->
            return buildList {
                while (cursor.moveToNext()) add(cursor.toRecord())
            }
        }
    }

    private fun android.database.Cursor.toRecord(): Record {
        val operationName = getString(getColumnIndexOrThrow(COLUMN_OPERATION))
        val stateName = getString(getColumnIndexOrThrow(COLUMN_STATE))
        return Record(
            requestId = getString(getColumnIndexOrThrow(COLUMN_REQUEST_ID)),
            operation = JobOperation.valueOf(operationName),
            state = JobState.valueOf(stateName),
            createdAtEpochSeconds = getLong(getColumnIndexOrThrow(COLUMN_CREATED_AT)),
            updatedAtEpochSeconds = getLong(getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
            expiresAtEpochSeconds = getLong(getColumnIndexOrThrow(COLUMN_EXPIRES_AT)),
            workspaceId = getString(getColumnIndexOrThrow(COLUMN_WORKSPACE_ID)),
            summary = getString(getColumnIndexOrThrow(COLUMN_SUMMARY))
        )
    }

    companion object {
        private const val DATABASE_NAME = "local_jobs.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_JOBS = "jobs"
        private const val MAX_SUMMARY_CHARS = 512

        private const val COLUMN_REQUEST_ID = "request_id"
        private const val COLUMN_OPERATION = "operation"
        private const val COLUMN_STATE = "state"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
        private const val COLUMN_EXPIRES_AT = "expires_at"
        private const val COLUMN_WORKSPACE_ID = "workspace_id"
        private const val COLUMN_SUMMARY = "summary"

        private val RECORD_COLUMNS = arrayOf(
            COLUMN_REQUEST_ID,
            COLUMN_OPERATION,
            COLUMN_STATE,
            COLUMN_CREATED_AT,
            COLUMN_UPDATED_AT,
            COLUMN_EXPIRES_AT,
            COLUMN_WORKSPACE_ID,
            COLUMN_SUMMARY
        )
    }
}
