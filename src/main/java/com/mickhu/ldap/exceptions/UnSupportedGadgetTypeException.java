package com.mickhu.ldap.exceptions;

public class UnSupportedGadgetTypeException extends RuntimeException {
    public UnSupportedGadgetTypeException(){ super();}
    public UnSupportedGadgetTypeException(String message){
        super(message);
    }
}
