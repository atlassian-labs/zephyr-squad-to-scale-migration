package com.atlassian.migration.app.zephyr.common;

import java.io.IOException;

public class ApiException extends IOException {

    public final int code;
    public final int codeMajor;
    public final String message;

    public ApiException(Exception e){
        super(e);
        this.code = -1;
        this.codeMajor = -1;
        this.message = e.getMessage();
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
        this.codeMajor = code/100;
        this.message = message;
    }


}
