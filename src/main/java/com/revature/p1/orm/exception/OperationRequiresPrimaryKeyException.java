package com.revature.p1.orm.exception;

public class OperationRequiresPrimaryKeyException extends RuntimeException {
    public OperationRequiresPrimaryKeyException(String strTargetClassName) {
        super("A 'by ID' database operation was attempted on an object of class " + strTargetClassName + " which lacks an annotated primary key field!");
    }
}