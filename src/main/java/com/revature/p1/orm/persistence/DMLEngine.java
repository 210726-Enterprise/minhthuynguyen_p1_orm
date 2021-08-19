package com.revature.p1.orm.persistence;

import com.revature.p1.orm.util.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DMLEngine {
    public static String insertRow(Object objTarget) throws InvocationTargetException, IllegalAccessException {
        Metamodel<?>mTarget = Configuration.getMetamodelByClassName(objTarget.getClass().getName());
        StringBuilder strSQL = new StringBuilder("insert into " + mTarget.getTableName() + " values (");
        if (mTarget.getPrimaryKey() != null) {
            strSQL.append("default,");
        }
        for (Object obj : mTarget.getColumns()) {
            ColumnField f = (ColumnField) obj;
            System.out.println("Now getting getters for column " + f.getColumnName());
            Method mt = QueryProcessingUtils.getGetter(objTarget.getClass(), f);
            try {
                strSQL.append(QueryProcessingUtils.toSQLData(mt.invoke(objTarget)) + ",");
            } catch (IllegalArgumentException e1) {
                throw new IllegalArgumentException(e1.getMessage() + " Tried to persist column " + f.getColumnName() + " on table " + mTarget.getTableName() + " from getter " + mt.getName() + " with incompatible return type " + mt.getReturnType() + "!");
            }
        }
        strSQL.deleteCharAt(strSQL.length()-1);
        strSQL.append(")");
        return strSQL.toString();
    }

    public static String deleteRow(Class<?> cTarget, int iID) {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        //StringBuilder strSQL = new StringBuilder("delete from " + mTarget.getTableName() + " where " + mTarget.getPrimaryKey().getColumnName() + "=" + iID);
        return("delete from " + mTarget.getTableName() + " where " + mTarget.getPrimaryKey().getColumnName() + "=" + iID);
    }
    public static String deleteRow(Object objTarget) throws InvocationTargetException, IllegalAccessException {
        Class<?> cTarget = objTarget.getClass();
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        if (mTarget.getPrimaryKey()!=null) {
            Method mtPKGetter = QueryProcessingUtils.getGetter(cTarget, mTarget.getPrimaryKey());
            if (mtPKGetter != null) {
                return deleteRow(cTarget,(int)mtPKGetter.invoke(objTarget));
            }
        }
        StringBuilder strSQL = new StringBuilder("delete from " + mTarget.getTableName() + " where ");
        boolean bAnyConditionAppended = false;
        for (ColumnField f : mTarget.getColumns()) {
            Method mtPKGetter = QueryProcessingUtils.getGetter(cTarget, f);
            if (mtPKGetter != null) {
                strSQL.append(f.getColumnName() + "=" + QueryProcessingUtils.toSQLData(mtPKGetter.invoke(objTarget)) + " and ");
                bAnyConditionAppended = true;
            }
        }
        if (bAnyConditionAppended) {
            strSQL.delete(strSQL.length()-5,strSQL.length());
            return strSQL.toString();
        }
        return null;
    }
}
