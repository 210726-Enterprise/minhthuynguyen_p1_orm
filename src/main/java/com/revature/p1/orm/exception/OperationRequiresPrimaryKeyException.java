package com.revature.p1.orm.exception;

/**
 * Exception thrown when either a database operation that identifies target
 * row by primary key fails because a) it is being attempted on a table with no
 * primary key, or b) it is being attempted with an input model object that has
 * an primary key ID field equal to 0 or less (all PK fields are int currently,
 * so a PK ID of 0 means the ID is likely not initialized yet).
 */
public class OperationRequiresPrimaryKeyException extends RuntimeException {
    public OperationRequiresPrimaryKeyException(String strTargetClassName) {
        super("A 'by ID' database operation was attempted on an object of class " + strTargetClassName + " which lacks an annotated primary key field!");
    }
}