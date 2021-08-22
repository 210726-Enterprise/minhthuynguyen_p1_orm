package com.revature.p1.orm.persistence;

import com.revature.p1.orm.util.ColumnField;
import com.revature.p1.orm.util.Configuration;
import com.revature.p1.orm.util.Metamodel;

public class DDLEngine {
    public static String createByClassName(String strName) {
        Metamodel<?>mModel = Configuration.getMetamodelByClassName(strName);
        if (mModel == null) {
            return null;
        }
        StringBuilder strSql = new StringBuilder("create table if not exists " + mModel.getTableName() + " (");
        if ((mModel.getPrimaryKey() == null) && (mModel.getColumns().isEmpty())) {
            throw new IllegalStateException("Annotated class " + strName + " has no annotated fields!");
        }
        if (mModel.getPrimaryKey() != null) {
            strSql.append(mModel.getPrimaryKey().getColumnName() + " serial primary key,");
        }
        for (Object f : mModel.getColumns()) {
            strSql.append(((ColumnField) f).getColumnName() + " " + (SQLTypeMapping.toSQLType(((ColumnField) f).getType())) + ",");
        }
        strSql.deleteCharAt(strSql.length() - 1);
        strSql.append(");");
        System.out.println(strSql.toString());
        return strSql.toString();
    }
    public static String createByClass(Class<?> cTarget) {
        return createByClassName(cTarget.getName());
    }
}

