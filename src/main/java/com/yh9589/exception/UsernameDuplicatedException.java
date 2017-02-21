package com.yh9589.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by beryh on 2017-02-15.
 */
@ResponseStatus(value= HttpStatus.CONFLICT, reason="Username is duplicated")
public class UsernameDuplicatedException extends RuntimeException {
}
