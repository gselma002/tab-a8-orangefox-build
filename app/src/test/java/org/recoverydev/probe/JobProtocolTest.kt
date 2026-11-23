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

    @Test fun rejectsExpiryArithmeticOverflow() {
        val extreme = request(expiry = Long.MAX_VALUE)
        assertFalse(JobRequestValidator.validate(extreme, Long.MIN_VALUE).isValid)
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

    @Test fun projectOperationsRequireWorkspaceScope() {
        val statusWithoutWorkspace = request(operation = JobOperation.REPOSITORY_STATUS)
        assertFalse(JobRequestValidator.validate(statusWithoutWorkspace, now).isValid)
        assertEquals(JobRisk.READ_ONLY, JobOperation.REPOSITORY_STATUS.risk)

        val checkoutWithoutWorkspace = request(operation = JobOperation.PINNED_SOURCE_CHECKOUT)
        assertFalse(JobRequestValidator.validate(checkoutWithoutWorkspace, now).isValid)

        val scoped = statusWithoutWorkspace.copy(
            parameters = mapOf("workspace_id" to "workspace-01")
        )
        assertTrue(JobRequestValidator.validate(scoped, now).isValid)
    }

    @Test fun rejectsMalformedWorkspaceAndArtifactIdentifiers() {
        val badWorkspace = request(
            operation = JobOperation.REPOSITORY_STATUS,
            parameters = mapOf("workspace_id" to "../outside")
        )
        assertFalse(JobRequestValidator.validate(badWorkspace, now).isValid)

        val missingArtifact = request(operation = JobOperation.APK_INSPECTION)
        assertFalse(JobRequestValidator.validate(missingArtifact, now).isValid)

        val badArtifact = request(
            operation = JobOperation.APK_INSPECTION,
            parameters = mapOf("artifact_id" to "../../private")
        )
        assertFalse(JobRequestValidator.validate(badArtifact, now).isValid)
    }

    @Test fun sourceOperationsRequireCanonicalRepositoryAndImmutableCommit() {
        val valid = request(
            operation = JobOperation.ANDROID_APP_BUILD,
            parameters = mapOf("workspace_id" to "workspace-01"),
            repositoryUrl = "https://github.com/example/project",
            commitSha = "a".repeat(40)
        )
        assertTrue(JobRequestValidator.validate(valid, now).isValid)

        val branchRef = valid.copy(commitSha = "main")
        assertFalse(JobRequestValidator.validate(branchRef, now).isValid)

        val credentialUrl = valid.copy(
            repositoryUrl = "https://user:pass@github.com/example/project"
        )
        assertFalse(JobRequestValidator.validate(credentialUrl, now).isValid)

        val otherHost = valid.copy(repositoryUrl = "https://example.com/example/project")
        assertFalse(JobRequestValidator.validate(otherHost, now).isValid)

        val encodedPath = valid.copy(
            repositoryUrl = "https://github.com/example/%2e%2e"
        )
        assertFalse(JobRequestValidator.validate(encodedPath, now).isValid)

        val malformedAuthority = valid.copy(
            repositoryUrl = "https://github.com:invalid/example/project"
        )
        assertFalse(JobRequestValidator.validate(malformedAuthority, now).isValid)

        val extraPath = valid.copy(
            repositoryUrl = "https://github.com/example/project/extra"
        )
        assertFalse(JobRequestValidator.validate(extraPath, now).isValid)
    }

    @Test fun stateMachineRejectsInvalidAndTerminalTransitions() {
        assertTrue(JobStateMachine.canTransition(JobState.RECEIVED, JobState.VALIDATING))
        assertFalse(JobStateMachine.canTransition(JobState.RECEIVED, JobState.RUNNING))
        assertFalse(JobStateMachine.canTransition(JobState.VALIDATING, JobState.QUEUED))
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
