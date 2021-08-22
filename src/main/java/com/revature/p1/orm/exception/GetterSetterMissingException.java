package com.revature.p1.orm.exception;


import com.revature.p1.orm.util.SQLField;

public class GetterSetterMissingException extends RuntimeException {
    public GetterSetterMissingException() {
        super();
    }
    public GetterSetterMissingException(String strErrorMessage) {
        super(strErrorMessage);
    }
    public GetterSetterMissingException(String strColumnName, String strTableName, boolean bTrueIfGetter) {
        throw new GetterSetterMissingException("Cannot find a public " + (bTrueIfGetter?"getter":"setter") + " method for field used for column " + strColumnName + " on table " + strTableName + "!\nIf one does exist, please make sure it is named \"" + (bTrueIfGetter?"get":"set") + "<field name>\" after the field in that class that corresponds to the column in question.");
    }
}
