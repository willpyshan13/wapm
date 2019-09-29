package com.will.library;

import android.util.Log;


public class LoggerHandler {

    protected void log(String tag, String msg) {

    }

    public static LoggerHandler DEFAULT_IMPL = new LoggerHandler() {

        @Override
        public void log(String tag, String msg) {
            Log.i(tag, msg);
        }

    };

    public static LoggerHandler CUSTOM_IMPL = DEFAULT_IMPL;

    public static void installLogImpl(LoggerHandler loggerHandler) {
        CUSTOM_IMPL = loggerHandler;
    }

}
