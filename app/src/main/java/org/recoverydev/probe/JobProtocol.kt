package org.recoverydev.probe

/**
 * Pure, deterministic protocol primitives for the local automation prototype.
 *
 * This is not a command runner, transport, or remote-control implementation.
 * Callers must still enforce on-device approval and execute only registered handlers.
 */
enum class JobOperation(val risk: JobRisk) {
    DEVICE_INVENTORY(JobRisk.READ_ONLY),
    TOOL_INVENTORY(JobRisk.READ_ONLY),
    REPOSITORY_STATUS(JobRisk.LOCAL_CHANGE),
    PINNED_SOURCE_CHECKOUT(JobRisk.LOCAL_CHANGE),
    UNIT_TESTS(JobRisk.LOCAL_CHANGE),
    ANDROID_APP_BUILD(JobRisk.LOCAL_CHANGE),
    APK_INSPECTION(JobRisk.READ_ONLY)
}

enum class JobRisk { READ_ONLY, LOCAL_CHANGE }

enum class JobState {
    RECEIVED,
    VALIDATING,
    AWAITING_APPROVAL,
    QUEUED,
    RUNNING,
    CANCELLING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    EXPIRED,
    REJECTED,
    INTERRUPTED;

    val isTerminal: Boolean
        get() = this in setOf(SUCCEEDED, FAILED, CANCELLED, EXPIRED, REJECTED, INTERRUPTED)
}

data class JobRequest(
    val protocolVersion: Int,
    val requestId: String,
    val expiresAtEpochSeconds: Long,
    val operation: JobOperation,
    val parameters: Map<String, String> = emptyMap(),
    val requestedOutputFields: Set<String> = emptySet(),
    val repositoryUrl: String? = null,
    val commitSha: String? = null
)

data class ValidationResult(val errors: List<String>) {
    val isValid: Boolean get() = errors.isEmpty()
}

object JobRequestValidator {
    private val requestIdPattern = Regex("^[A-Za-z0-9_-]{8,64}$")
    private val commitPattern = Regex("^[a-fA-F0-9]{40}$")
    private val allowedRepositoryHost = "github.com"
    private val allowedOutputFields = setOf(
        "job_id", "state", "exit_code", "started_at", "finished_at",
        "handler_version", "summary", "output_hashes"
    )
    private val allowedParameters = setOf("workspace_id", "artifact_id")

    fun validate(request: JobRequest, nowEpochSeconds: Long): ValidationResult {
        val errors = mutableListOf<String>()

        if (request.protocolVersion != 1) errors += "Unsupported protocol version"
        if (!requestIdPattern.matches(request.requestId)) errors += "Invalid request ID"
        if (request.expiresAtEpochSeconds <= nowEpochSeconds) errors += "Request has expired"
        if (request.expiresAtEpochSeconds > nowEpochSeconds + 15 * 60) {
            errors += "Request expiry exceeds 15-minute limit"
        }
        if (request.parameters.size > 8) errors += "Too many parameters"
        request.parameters.forEach { (key, value) ->
            if (key !in allowedParameters) errors += "Unsupported parameter: $key"
            if (value.isBlank() || value.length > 128) errors += "Invalid value for parameter: $key"
        }
        if (request.operation.risk == JobRisk.LOCAL_CHANGE &&
            request.parameters["workspace_id"].isNullOrBlank()
        ) {
            errors += "A pre-registered workspace ID is required for local-change operations"
        }
        if (request.operation == JobOperation.APK_INSPECTION &&
            request.parameters["artifact_id"].isNullOrBlank()
        ) {
            errors += "A pre-registered artifact ID is required for APK inspection"
        }
        if (request.requestedOutputFields.size > 8 ||
            !allowedOutputFields.containsAll(request.requestedOutputFields)
        ) {
            errors += "Unsupported or excessive output fields"
        }

        val sourceOperation = request.operation in setOf(
            JobOperation.PINNED_SOURCE_CHECKOUT,
            JobOperation.UNIT_TESTS,
            JobOperation.ANDROID_APP_BUILD
        )
        if (sourceOperation) {
            val uri = runCatching { java.net.URI(request.repositoryUrl.orEmpty()) }.getOrNull()
            if (uri == null || uri.scheme != "https" || uri.host != allowedRepositoryHost ||
                uri.port !in setOf(-1, 443) || uri.userInfo != null ||
                uri.query != null || uri.fragment != null ||
                uri.path.count { it == '/' } != 2
            ) {
                errors += "A canonical HTTPS GitHub repository URL is required"
            }
            if (!commitPattern.matches(request.commitSha.orEmpty())) {
                errors += "An immutable 40-character commit SHA is required"
            }
        } else if (request.repositoryUrl != null || request.commitSha != null) {
            errors += "Repository inputs are not accepted for this operation"
        }

        return ValidationResult(errors.distinct())
    }
}

/** Strict state-transition table; unknown transitions fail closed. */
object JobStateMachine {
    private val transitions = mapOf(
        JobState.RECEIVED to setOf(JobState.VALIDATING, JobState.REJECTED, JobState.EXPIRED),
        JobState.VALIDATING to setOf(
            JobState.AWAITING_APPROVAL, JobState.REJECTED, JobState.EXPIRED
        ),
        JobState.AWAITING_APPROVAL to setOf(
            JobState.QUEUED, JobState.REJECTED, JobState.EXPIRED
        ),
        JobState.QUEUED to setOf(JobState.RUNNING, JobState.CANCELLED, JobState.EXPIRED),
        JobState.RUNNING to setOf(
            JobState.CANCELLING, JobState.SUCCEEDED, JobState.FAILED, JobState.INTERRUPTED
        ),
        JobState.CANCELLING to setOf(JobState.CANCELLED, JobState.FAILED, JobState.INTERRUPTED)
    )

    fun canTransition(from: JobState, to: JobState): Boolean =
        to in transitions[from].orEmpty()

    fun transition(from: JobState, to: JobState): JobState {
        require(canTransition(from, to)) { "Invalid job state transition: $from -> $to" }
        return to
    }
}

/**
 * Conservative redaction for diagnostic text. This is defense in depth, not
 * a guarantee that arbitrary logs are safe to export.
 */
object DiagnosticRedactor {
    private val bearerPattern = Regex("(?i)\\bBearer\\s+[A-Za-z0-9._~+/-]+=*")
    private val tokenAssignmentPattern =
        Regex("(?i)(token|password|passwd|secret|api[_-]?key)\\s*[:=]\\s*[^\\s,;]+")
    private val emailPattern = Regex("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", RegexOption.IGNORE_CASE)

    fun redact(input: String, maxChars: Int = 8_192): String {
        require(maxChars in 1..65_536) { "Invalid output limit" }
        return input
            .take(maxChars)
            .replace(bearerPattern, "Bearer [REDACTED]")
            .replace(tokenAssignmentPattern) { match ->
                "${match.groupValues[1]}=[REDACTED]"
            }
            .replace(emailPattern, "[EMAIL REDACTED]")
    }
}
