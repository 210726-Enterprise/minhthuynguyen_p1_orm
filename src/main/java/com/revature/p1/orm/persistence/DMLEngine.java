package com.revature.p1.orm.persistence;

import com.revature.p1.orm.exception.OperationRequiresPrimaryKeyException;
import com.revature.p1.orm.util.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

public class DMLEngine {
    public static String insertRow(Object objTarget) throws InvocationTargetException, IllegalAccessException {
        Metamodel<?>mTarget = Configuration.getMetamodelByClassName(objTarget.getClass().getName());
        StringBuilder strSql = new StringBuilder("insert into " + mTarget.getTableName() + " values (");
        if (mTarget.getPrimaryKey() != null) {
            strSql.append("default,");
        }
        for (Object obj : mTarget.getColumns()) {
            ColumnField f = (ColumnField) obj;
            Method mt = QueryProcessingUtils.getGetter(objTarget.getClass(), f);
            try {
                strSql.append(QueryProcessingUtils.toSQLData(mt.invoke(objTarget)) + ",");
            } catch (IllegalArgumentException e1) {
                throw new IllegalArgumentException(e1.getMessage() + " Tried to persist column " + f.getColumnName() + " on table " + mTarget.getTableName() + " from getter " + mt.getName() + " with incompatible return type " + mt.getReturnType() + "!");
            }
        }
        strSql.deleteCharAt(strSql.length()-1);
        strSql.append(")");
        return strSql.toString();
    }

    public static String deleteRow(Class<?> cTarget, int iID) {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        //StringBuilder strSql = new StringBuilder("delete from " + mTarget.getTableName() + " where " + mTarget.getPrimaryKey().getColumnName() + "=" + iID);
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
        StringBuilder strSql = new StringBuilder("delete from " + mTarget.getTableName() + " where ");
        boolean bAnyConditionAppended = false;
        for (ColumnField f : mTarget.getColumns()) {
            Method mtPKGetter = QueryProcessingUtils.getGetter(cTarget, f);
            if (mtPKGetter != null) {
                strSql.append(f.getColumnName() + "=" + QueryProcessingUtils.toSQLData(mtPKGetter.invoke(objTarget)) + " and ");
                bAnyConditionAppended = true;
            }
        }
        if (bAnyConditionAppended) {
            strSql.delete(strSql.length()-5,strSql.length());
            return strSql.toString();
        }
        return null;
    }
    
    public static String updateRowById(Object objTarget) {
        Class<?> cTarget = objTarget.getClass();
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        if (mTarget.getPrimaryKey()==null) {
            throw new OperationRequiresPrimaryKeyException(cTarget.getName());
        }
        StringBuilder strSql = new StringBuilder("update table " + mTarget.getTableName() + " set ");
        if (mTarget.getColumns().isEmpty()) {
            throw new NoSuchElementException();
        }
        return null;
    }
}
