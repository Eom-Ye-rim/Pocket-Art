package com.pocekt.art.user.exception;

import com.pocekt.art.common.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-401","해당 유저를 찾을 수 없습나다.");

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
