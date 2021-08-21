package com.vinyla.server.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DefaultRes<T> {
    private int status;
    private boolean success;
    private String responseMessage;
    private T data;

    public DefaultRes(final int statusCode, final boolean success, final String responseMessage) {
        this.status = statusCode;
        this.success = success;
        this.responseMessage = responseMessage;
        this.data = null;
    }

    public static<T> DefaultRes<T> res(final int statusCode, final boolean success, final String responseMessage) {
        return res(statusCode, success, responseMessage, null);
    }

    public static<T> DefaultRes<T> res(final int statusCode, final boolean success, final String responseMessage, final T t) {
        return DefaultRes.<T>builder()
                .data(t)
                .status(statusCode)
                .success(success)
                .responseMessage(responseMessage)
                .build();
    }
}
