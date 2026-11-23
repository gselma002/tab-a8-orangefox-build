package org.recoverydev.probe

import org.junit.Assert.*
import org.junit.Test

class JobProtocolTest {
    private val now = 1_800_000_000L

    private fun request(
        operation: JobOperation = JobOperation.DEVICE_INVENTORY,
        parameters: Map<String, String> = emptyMap(),
        repositoryUrl: String? = null,
        commitSha: String? = null,
        expiry: Long = now + 300
    ) = JobRequest(
        protocolVersion = 1,
        requestId = "request_123",
        expiresAtEpochSeconds = expiry,
        operation = operation,
        parameters = parameters,
        requestedOutputFields = setOf("job_id", "state"),
        repositoryUrl = repositoryUrl,
        commitSha = commitSha
    )

    @Test fun acceptsReadOnlyRequestWithBoundedExpiry() {
        assertTrue(JobRequestValidator.validate(request(), now).isValid)
    }

    @Test fun rejectsExpiredAndExcessivelyLongRequests() {
        assertFalse(JobRequestValidator.validate(request(expiry = now), now).isValid)
        assertFalse(JobRequestValidator.validate(request(expiry = now + 901), now).isValid)
    }

    @Test fun rejectsUnknownParametersAndOutputFields() {
        assertFalse(
            JobRequestValidator.validate(
                request(parameters = mapOf("shell" to "id")),
                now
            ).isValid
        )
        val invalidOutput = request().copy(requestedOutputFields = setOf("all_files"))
        assertFalse(JobRequestValidator.validate(invalidOutput, now).isValid)
    }

    @Test fun sourceOperationsRequireCanonicalRepositoryAndImmutableCommit() {
        val valid = request(
            operation = JobOperation.ANDROID_APP_BUILD,
            repositoryUrl = "https://github.com/example/project",
            commitSha = "a".repeat(40)
        )
        assertTrue(JobRequestValidator.validate(valid, now).isValid)

        val branchRef = valid.copy(commitSha = "main")
        assertFalse(JobRequestValidator.validate(branchRef, now).isValid)

        val credentialUrl = valid.copy(repositoryUrl = "https://user:pass@github.com/example/project")
        assertFalse(JobRequestValidator.validate(credentialUrl, now).isValid)

        val otherHost = valid.copy(repositoryUrl = "https://example.com/example/project")
        assertFalse(JobRequestValidator.validate(otherHost, now).isValid)
    }

    @Test fun stateMachineRejectsInvalidAndTerminalTransitions() {
        assertTrue(JobStateMachine.canTransition(JobState.RECEIVED, JobState.VALIDATING))
        assertFalse(JobStateMachine.canTransition(JobState.RECEIVED, JobState.RUNNING))
        assertFalse(JobStateMachine.canTransition(JobState.SUCCEEDED, JobState.RUNNING))
        assertEquals(
            JobState.QUEUED,
            JobStateMachine.transition(JobState.AWAITING_APPROVAL, JobState.QUEUED)
        )
        assertThrows(IllegalArgumentException::class.java) {
            JobStateMachine.transition(JobState.SUCCEEDED, JobState.RUNNING)
        }
    }

    @Test fun redactorMasksCommonSecretsAndEmailAddresses() {
        val result = DiagnosticRedactor.redact(
            "Bearer abc.def token=secret-value user@example.com"
        )
        assertFalse(result.contains("abc.def"))
        assertFalse(result.contains("secret-value"))
        assertFalse(result.contains("user@example.com"))
        assertTrue(result.contains("[REDACTED]"))
        assertTrue(result.contains("[EMAIL REDACTED]"))
    }

    @Test fun redactorEnforcesOutputBounds() {
        assertEquals("1234", DiagnosticRedactor.redact("123456", maxChars = 4))
        assertThrows(IllegalArgumentException::class.java) {
            DiagnosticRedactor.redact("x", maxChars = 0)
        }
    }
}
