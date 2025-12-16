package com.nortal.library.api.controller;

import com.nortal.library.api.dto.ResultResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResultResponse> handleValidation() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ResultResponse(false, "INVALID_REQUEST"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResultResponse> handleGeneric() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ResultResponse(false, "ERROR"));
  }
}
