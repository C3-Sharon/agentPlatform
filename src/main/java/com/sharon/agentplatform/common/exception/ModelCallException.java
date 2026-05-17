package com.sharon.agentplatform.common.exception;

public class ModelCallException extends BusinessException {

    public ModelCallException(String message) {
        super(message);
    }

    public ModelCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
