package com.api.picpay.transaction;

public class InvalidTransactionException extends RuntimeException{
    public InvalidTransactionException(String message){
        super(message);
    }
}
