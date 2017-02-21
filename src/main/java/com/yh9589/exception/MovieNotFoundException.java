package com.yh9589.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by beryh on 2017-02-20.
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Cannot find movie")
public class MovieNotFoundException extends RuntimeException {
}
