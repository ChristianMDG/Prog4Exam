package com.exam.app.submission.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler {
  @ExceptionHandler(InvalidFileTypeException.class)
  public ResponseEntity<Map<String, String>> handleInvalidFileType(InvalidFileTypeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(EmptyFileException.class)
  public ResponseEntity<Map<String, String>> handleEmptyFile(EmptyFileException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(SubmissionNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleSubmissionNotFound(
      SubmissionNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolation(
      jakarta.validation.ConstraintViolationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email invalide"));
  }
}
