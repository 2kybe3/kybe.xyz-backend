package xyz.kybe.backend.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(defaultValue = "API Error Response")
public record ErrorResponse(
	@Schema(description = "Error code", example = "BAD_REQUEST")
	ErrorCode code,

	@Schema(description = "Human-readable error message", example = "Invalid request parameters")
	String message
) {
}
