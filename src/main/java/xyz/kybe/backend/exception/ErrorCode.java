package xyz.kybe.backend.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error codes")
public enum ErrorCode {
	@Schema(description = "The provided request parameters are invalid", example = "BAD_REQUEST")
	BAD_REQUEST
}
