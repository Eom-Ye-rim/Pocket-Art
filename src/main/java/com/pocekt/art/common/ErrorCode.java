package com.pocekt.art.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


public interface ErrorCode {
  HttpStatus getHttpStatus();

  String getMessage();
  String getCode();
}
