package com.revature.p1.orm.exception;

public class MissingAnnotatedMethodException extends RuntimeException {
    public MissingAnnotatedMethodException(String strErrorMessage) {
        super(strErrorMessage);
    }
    public MissingAnnotatedMethodException(String strAnnotatedMethod, String strNotFoundIn) {
        throw new MissingAnnotatedMethodException("ORM operation failed due to lack of an annotated " + strAnnotatedMethod + " method in class " + strNotFoundIn + "!");
    }

    public MissingAnnotatedMethodException() {
        super("ORM operation failed due to missing a needed annotated method!");
    }
}
