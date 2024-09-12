package com.atlassian.migration.app.zephyr.common;

import java.io.IOException;

public class ZephyrApiException extends IOException {

    public final int code;
    public final int codeMajor;
    public final String message;

    public ZephyrApiException(ApiException e) {
        super(e);
        this.code = e.code;
        this.codeMajor = e.codeMajor;
        this.message = e.message;
    }

}