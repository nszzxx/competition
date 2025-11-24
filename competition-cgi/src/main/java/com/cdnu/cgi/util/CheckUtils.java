package com.cdnu.cgi.util;

import org.apache.commons.lang3.exception.UncheckedException;

public class CheckUtils {
    public CheckUtils() {
    }

    public static RuntimeException unchecked(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException)t;
        } else if (t instanceof Error) {
            throw (Error)t;
        } else {
            throw new UncheckedException(t);
        }
    }

    public static RuntimeException unchecked(Throwable t, String message) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException)t;
        } else if (t instanceof Error) {
            throw (Error)t;
        } else {
            throw new UncheckedException(t);
        }
    }
}
