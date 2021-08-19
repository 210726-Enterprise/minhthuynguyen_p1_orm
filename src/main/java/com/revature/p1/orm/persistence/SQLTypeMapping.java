package com.revature.p1.orm.persistence;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SQLTypeMapping {
    INT(new String[]{"int", "java.lang.Integer"},"integer"),
    DOUBLE(new String[]{"double","java.lang.Double"},"float8"),
    FLOAT(new String[]{"float","java.lang.Float"},"float8"),
    STRING(new String[]{"java.lang.String"},"varchar(30)"),
    CHAR(new String[]{"char","java.lang.Char"},"char(1)");

    private final String strSQLType;
    private final List<String> strJavaType;
    SQLTypeMapping(String[] strJavaType, String strSQLType) {
        this.strJavaType = new ArrayList<>();
        Arrays.stream(strJavaType).forEach(str -> this.strJavaType.add(str));
        this.strSQLType = strSQLType;
    }

    public String getSQLType(String strType) {
        return strSQLType;
    }
    public List<String> getJavaTypes() {
        return strJavaType;
    }

    public static String toSQLType(Type tJavaType) {
        for (SQLTypeMapping ePair : SQLTypeMapping.values()) {
            if (ePair.strJavaType.contains(tJavaType.getTypeName())) {
                return ePair.strSQLType;
            }
        }
        return null;
    }
}
