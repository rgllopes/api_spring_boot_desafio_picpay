package com.api.picpay.authorization;

public class UnauthorizedTransactionException extends RuntimeException{
    public UnauthorizedTransactionException(String message){
        super(message);
    }
}
