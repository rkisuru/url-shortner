package com.rkisuru.url_shortner.exception;

public class DuplicateAliasException extends RuntimeException{
    public DuplicateAliasException(String alias) {
        super("The alias '" + alias + "' is already taken");
    }
}
