package com.pocekt.art.board.exception;

import com.pocekt.art.common.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum BoardErrorCode implements ErrorCode {
  BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD-401","이미 사용중인 닉네임입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;


  @Override
  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }
}