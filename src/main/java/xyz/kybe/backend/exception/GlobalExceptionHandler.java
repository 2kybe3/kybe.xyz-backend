package xyz.kybe.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(APIException.class)
	public ResponseEntity<ErrorResponse> handleAPIException(APIException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ex.getCode(), ex.getMessage());
		return ResponseEntity.status(400).body(errorResponse);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST, ex.getMessage());
		return ResponseEntity.status(400).body(errorResponse);
	}
}
