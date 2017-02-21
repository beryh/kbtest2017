package com.yh9589.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by beryh on 2017-02-17.
 */
@Getter
@AllArgsConstructor
public class BookmarkException extends RuntimeException {
    public enum Action {
        PUT, DELETE
    }

    private Action action;
}
