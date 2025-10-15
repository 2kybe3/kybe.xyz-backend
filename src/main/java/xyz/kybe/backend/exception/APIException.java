package xyz.kybe.backend.exception;

import lombok.Getter;

@Getter
public class APIException extends RuntimeException {
	private final ErrorCode code;

	public APIException(ErrorCode code, String message) {
		super(message);
		this.code = code;
	}
}
