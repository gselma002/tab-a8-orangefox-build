package org.recoverydev.probe

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Runs against the Android framework's SQLiteOpenHelper/SQLite implementation.
 * These tests must be executed on an Android device; JVM unit tests do not cover them.
 */
@RunWith(AndroidJUnit4::class)
class LocalJobStoreInstrumentedTest {
    private lateinit var context: Context
    private var store: LocalJobStore? = null

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DATABASE_NAME)
        store = LocalJobStore(context)
    }

    @After
    fun tearDown() {
        store?.close()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun createPersistsWorkspaceAndArtifactAcrossReopen() {
        val created = requireStore().create(
            request(
                operation = JobOperation.APK_INSPECTION,
                parameters = mapOf("artifact_id" to "artifact_01")
            ),
            NOW
        )

        assertEquals(JobState.RECEIVED, created.state)
        assertEquals("artifact_01", created.artifactId)

        requireStore().close()
        store = LocalJobStore(context)

        val reopened = requireStore().get("request_123")
        assertNotNull(reopened)
        assertEquals("artifact_01", reopened?.artifactId)
        assertEquals(JobState.RECEIVED, reopened?.state)
    }

    @Test
    fun duplicateRequestIdIsRejected() {
        val job = request()
        requireStore().create(job, NOW)

        assertThrows(LocalJobStore.DuplicateRequestException::class.java) {
            requireStore().create(job, NOW)
        }
    }

    @Test
    fun compareAndSetAllowsOnlyOneConcurrentTransition() {
        requireStore().create(request(), NOW)

        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val attempts = (1..2).map {
                executor.submit<Boolean> {
                    ready.countDown()
                    check(start.await(5, TimeUnit.SECONDS))
                    requireStore().transition(
                        requestId = "request_123",
                        expectedState = JobState.RECEIVED,
                        nextState = JobState.VALIDATING,
                        nowEpochSeconds = NOW + 1
                    )
                }
            }

            assertTrue("Workers did not become ready", ready.await(5, TimeUnit.SECONDS))
            start.countDown()
            val results = attempts.map { it.get(10, TimeUnit.SECONDS) }
            assertEquals(1, results.count { it })
            assertEquals(1, results.count { !it })
            assertEquals(JobState.VALIDATING, requireStore().get("request_123")?.state)
        } finally {
            start.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun approvalGuardRejectsFalseAndAllowsTrue() {
        requireStore().create(request(), NOW)
        assertTrue(
            requireStore().transition(
                "request_123", JobState.RECEIVED, JobState.VALIDATING, NOW + 1
            )
        )
        assertTrue(
            requireStore().transition(
                "request_123", JobState.VALIDATING, JobState.AWAITING_APPROVAL, NOW + 2
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            requireStore().transition(
                "request_123",
                JobState.AWAITING_APPROVAL,
                JobState.QUEUED,
                NOW + 3,
                userApproved = false
            )
        }
        assertTrue(
            requireStore().transition(
                "request_123",
                JobState.AWAITING_APPROVAL,
                JobState.QUEUED,
                NOW + 3,
                userApproved = true
            )
        )
    }

    @Test
    fun expiredJobCanOnlyEnterExpiredAfterItsDeadline() {
        requireStore().create(request(expiry = NOW + 60), NOW)

        assertFalse(
            requireStore().transition(
                "request_123", JobState.RECEIVED, JobState.EXPIRED, NOW + 59
            )
        )
        assertTrue(
            requireStore().transition(
                "request_123", JobState.RECEIVED, JobState.EXPIRED, NOW + 60
            )
        )
        assertEquals(JobState.EXPIRED, requireStore().get("request_123")?.state)
    }

    @Test
    fun migrationFromRealVersionOneSchemaPreservesExistingJob() {
        requireStore().close()
        store = null
        context.deleteDatabase(DATABASE_NAME)

        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        databaseFile.parentFile?.mkdirs()
        val v1 = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        try {
            v1.execSQL(
                """
                CREATE TABLE jobs (
                    request_id TEXT PRIMARY KEY NOT NULL,
                    operation TEXT NOT NULL,
                    state TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    expires_at INTEGER NOT NULL,
                    workspace_id TEXT,
                    summary TEXT,
                    CHECK(length(request_id) BETWEEN 8 AND 64),
                    CHECK(length(operation) BETWEEN 1 AND 40),
                    CHECK(length(state) BETWEEN 1 AND 24),
                    CHECK(created_at >= 0),
                    CHECK(updated_at >= created_at),
                    CHECK(expires_at > 0),
                    CHECK(summary IS NULL OR length(summary) <= 512)
                )
                """.trimIndent()
            )
            v1.execSQL("CREATE INDEX jobs_created_at_idx ON jobs (created_at DESC)")
            val row = ContentValues().apply {
                put("request_id", "legacy_123")
                put("operation", JobOperation.DEVICE_INVENTORY.name)
                put("state", JobState.RECEIVED.name)
                put("created_at", NOW)
                put("updated_at", NOW)
                put("expires_at", NOW + 300)
                putNull("workspace_id")
                put("summary", "legacy record")
            }
            assertTrue(v1.insertOrThrow("jobs", null, row) > 0)
            v1.version = 1
        } finally {
            v1.close()
        }

        store = LocalJobStore(context)
        val migrated = requireStore().get("legacy_123")
        assertNotNull(migrated)
        assertEquals("legacy record", migrated?.summary)
        assertNull(migrated?.artifactId)
        assertEquals(JobState.RECEIVED, migrated?.state)

        val newArtifactJob = request(
            requestId = "artifact_123",
            operation = JobOperation.APK_INSPECTION,
            parameters = mapOf("artifact_id" to "artifact_02")
        )
        assertEquals("artifact_02", requireStore().create(newArtifactJob, NOW).artifactId)
    }

    @Test
    fun persistedSummaryIsRedactedAndBounded() {
        requireStore().create(request(), NOW)
        val raw = "token=very-secret user@example.com " + "x".repeat(900)

        assertTrue(
            requireStore().transition(
                "request_123",
                JobState.RECEIVED,
                JobState.VALIDATING,
                NOW + 1,
                summary = raw
            )
        )

        val saved = requireStore().get("request_123")?.summary
        assertNotNull(saved)
        assertTrue(saved!!.length <= 512)
        assertFalse(saved.contains("very-secret"))
        assertFalse(saved.contains("user@example.com"))
    }

    private fun requireStore(): LocalJobStore = checkNotNull(store)

    private fun request(
        requestId: String = "request_123",
        operation: JobOperation = JobOperation.DEVICE_INVENTORY,
        parameters: Map<String, String> = emptyMap(),
        expiry: Long = NOW + 300
    ) = JobRequest(
        protocolVersion = 1,
        requestId = requestId,
        expiresAtEpochSeconds = expiry,
        operation = operation,
        parameters = parameters
    )

    companion object {
        private const val DATABASE_NAME = "local_jobs.db"
        private const val NOW = 1_800_000_000L
    }
}
