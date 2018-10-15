package com.sunsan.framework.model;



public class ApiException extends Exception {
    private ErrCode errCode;

    public ApiException() {
        super();
    }

    public ApiException(ErrCode errCode) {
        super(errCode.getMessage());
        this.errCode = errCode;
    }

    public ApiException(String message) {
        super(message);
        this.errCode = ErrCode.unknown;
    }

    public ApiException(ErrCode errCode, String message) {
        super(message);
        this.errCode = errCode;
    }


    public int getCode() {
        return errCode.getCode();
    }

    public ErrCode getErrCode() {
        return errCode;
    }
}
