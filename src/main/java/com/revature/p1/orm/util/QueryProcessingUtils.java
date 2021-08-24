package com.revature.p1.orm.util;

/**
 * Contains static methods that helps with building non-prepared SQL statements.
 * Never initialized.
 */
public class QueryProcessingUtils {
    /**
     * Formats int, double and float as a plain number, and char and wraps Strings and chars in single quotes (as well as their respective wrapper classes.)
     * For now, all other types are rejected.
     * @return String contain value of input object, properly re-formatted for SQL statements.
     */
    public static String toSQLData(Object obj) {
        switch (obj.getClass().getTypeName()) {
            case "java.lang.Integer":
            case "java.lang.Double":
            case "java.lang.Float":
                return String.valueOf(obj);
            case "java.lang.String":
            case "java.lang.Character":
                return("'" + obj + "'");
            default:
                throw new IllegalArgumentException("Conversion of type " + obj.getClass().getTypeName() + " to SQL data is undefined!");
        }
    }
    public static String toSQLData(int n) {
        return String.valueOf(n);
    }
    public static String toSQLData(double n) {
        return String.valueOf(n);
    }
    public static String toSQLData(float n) {
        return String.valueOf(n);
    }
    public static String toSQLData(char c) {
        return("'" + c + "'");
    }
}
