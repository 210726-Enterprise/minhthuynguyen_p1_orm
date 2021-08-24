package com.revature.p1.orm.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**Enum with static utility method for translating some common Java data types into the name of their PostGreSQL equivalent.
 * Currently only used by DDLEngine to write column definitions for CREATE TABLE statements.
 */
public enum SQLTypeMapping {
    INT(new String[]{"int", "java.lang.Integer"},"integer"),
    DOUBLE(new String[]{"double","java.lang.Double"},"float8"),
    FLOAT(new String[]{"float","java.lang.Float"},"float8"),
    STRING(new String[]{"java.lang.String"},"varchar(30)"),
    CHAR(new String[]{"char","java.lang.Char"},"char(1)");

    private static final Logger lLog4j = LoggerFactory.getLogger(SQLTypeMapping.class);

    private final String strSqlType;
    private final List<String> strJavaType;
    SQLTypeMapping(String[] strJavaType, String strSqlType) {
        this.strJavaType = new ArrayList<>();
        Arrays.stream(strJavaType).forEach(str -> this.strJavaType.add(str));
        this.strSqlType = strSqlType;
    }

    public static String toSQLType(Type tJavaType) {
        for (SQLTypeMapping ePair : SQLTypeMapping.values()) {
            if (ePair.strJavaType.contains(tJavaType.getTypeName())) {
                lLog4j.trace("Performed Java->SQL type conversion: " + tJavaType.getTypeName() + " -> " + ePair.strSqlType);
                return ePair.strSqlType;
            }
        }
        return null;
    }
}
