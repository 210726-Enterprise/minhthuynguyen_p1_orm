package com.revature.p1.orm.persistence;

import com.revature.p1.orm.util.Configuration;
import com.revature.p1.orm.util.Metamodel;
import com.revature.p1.orm.util.QueryProcessingUtils;

public class DQLEngine {
    public static String selectRowByID(Class<?> cTargetClass, int iID) {
        Metamodel<?>mTarget = Configuration.getMetamodelByClassName(cTargetClass.getName());
        if (mTarget.getPrimaryKey() == null) {
            throw new IllegalStateException(cTargetClass.getName() + ": class contains no annotated ID field, cannot perform getRowByID!");
        }
        StringBuilder strSql = new StringBuilder("select * from " + mTarget.getTableName());
        strSql.append(" where " + QueryProcessingUtils.toSQLName(mTarget.getPrimaryKey().getColumnName()) + "=" + iID + " limit 1");
        return strSql.toString();
    }
    public static String prepareIfTableExists() {
        return "select exists (select from information_schema.tables where table_name = ?)";
    }

    public static String selectLastRowById(Class<?> cTargetClass) {
        Metamodel<?>mTarget = Configuration.getMetamodelByClassName(cTargetClass.getName());
        if (mTarget.getPrimaryKey() == null) {
            throw new IllegalStateException(cTargetClass.getName() + ": class contains no annotated ID field, cannot perform getRowByID!");
        }
        return("select * from " + mTarget.getTableName() + " order by " + mTarget.getPrimaryKey().getColumnName() + " desc limit 1");
    }
}
