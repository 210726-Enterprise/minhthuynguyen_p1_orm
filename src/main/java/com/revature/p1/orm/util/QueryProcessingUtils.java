package com.revature.p1.orm.util;

import java.lang.reflect.Method;

public class QueryProcessingUtils {
    public static Method getGetter(Class<?> cTarget, SQLField fColumn) {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        if (mTarget == null) {
            return(null);
        }
        if (fColumn instanceof IdField) {
            return mTarget.getPKGetter();
        }
        return mTarget.getColumnsGetters().get(fColumn.getColumnName());
    }


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



    public static String toSQLName(String strJavaName) {
        StringBuilder strName = new StringBuilder();
        boolean bConsecutiveCaps = false;
        int n = 0;
        for (int i=0;i<strJavaName.length();i++) {
            if (i>n) {
                if (!Character.isLowerCase(strJavaName.charAt(i))) {
                    strName.append(strJavaName.substring(n,i).toLowerCase());
                    n = i;
                    if (!((strJavaName.charAt(i)=='_') || (Character.isUpperCase(strJavaName.charAt(i)) && (Character.isUpperCase(strJavaName.charAt(i-1)))) || (Character.isDigit(strJavaName.charAt(i)) && (Character.isDigit(strJavaName.charAt(i-1)))))) {
                        strName.append('_');
                    }
                }
            }
        }
        strName.append(strJavaName.substring(n));
        return strName.toString();
    }
}
