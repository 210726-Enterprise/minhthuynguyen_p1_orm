package com.revature.p1.orm.persistence;

import com.revature.p1.orm.exception.GetterSetterMissingException;
import com.revature.p1.orm.exception.OperationRequiresPrimaryKeyException;
import com.revature.p1.orm.util.*;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Optional;

public class DMLEngine {
    public static String insertRow(Object objTarget) {
        Metamodel<?>mTarget = Configuration.getMetamodelByClassName(objTarget.getClass().getName());
        StringBuilder strSql = new StringBuilder("insert into " + mTarget.getTableName() + " values (");
        if (mTarget.getPrimaryKey() != null) {
            strSql.append("default,");
        }
        mTarget.getColumns().stream()
                .forEachOrdered(f -> {
                    try {
                        if (f.getValue(objTarget).isPresent()) {
                            strSql.append(
                                    QueryProcessingUtils
                                            .toSQLData(
                                                    f.getValue(objTarget).get()) + ","
                            );
                        } else {
                            throw new NoSuchElementException();
                        }
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(e.getMessage() + "\nTried to persist column " + f.getColumnName() + " on table " + mTarget.getTableName() + " from field " + f.getName() + " which possesses a getter with incompatible return type!");
                    } catch (GetterSetterMissingException e) {
                        throw new GetterSetterMissingException(f.getColumnName(), mTarget.getTableName(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        strSql.deleteCharAt(strSql.length()-1);
        strSql.append(")");
        return strSql.toString();
    }

    public static String deleteRow(Class<?> cTarget, int iID) {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        return("delete from " + mTarget.getTableName() + " where " + mTarget.getPrimaryKey().getColumnName() + "=" + iID);
    }
    public static String deleteRow(Object objTarget) throws InvocationTargetException, IllegalAccessException {
        Class<?> cTarget = objTarget.getClass();
        Metamodel<Class<?>> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        if (mTarget == null) {
            return null;
        }
        if (mTarget.getPrimaryKey()!=null) {
            Optional<Integer> iId = mTarget.getPrimaryKey().getValue(objTarget);
            if (iId.isPresent()) {
                return deleteRow(cTarget, iId.get());
            }
        }
        StringBuilder strSql = new StringBuilder("delete from " + mTarget.getTableName() + " where ");
        if (mTarget.getColumns().stream()
                .noneMatch(f -> {
                    try {
                        return f.getValue(objTarget).isPresent();
                    } catch (GetterSetterMissingException e) {
                        throw new GetterSetterMissingException(f.getColumnName(), mTarget.getTableName(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })) {
            return null;
        }
        mTarget.getColumns().forEach(f -> {
                    try {
                        if (f.getValue(objTarget).isPresent()) {
                            strSql.append(f.getColumnName()
                                    + "="
                                    + QueryProcessingUtils.toSQLData(
                                    f.getValue(objTarget).get()
                            ));
                        }
                    } catch (GetterSetterMissingException e) {
                        throw new GetterSetterMissingException(f.getColumnName(), mTarget.getTableName(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        strSql.delete(strSql.length()-5,strSql.length());
        return strSql.toString();
    }

    public static String updateRowById(Object objTarget) throws InvocationTargetException, IllegalAccessException {
        Class<?> cTarget = objTarget.getClass();
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        try {
            if (mTarget.getPrimaryKey() == null || !mTarget.getPrimaryKey().getValue(objTarget).isPresent()) {
                throw new OperationRequiresPrimaryKeyException(cTarget.getName());
            }
        } catch (GetterSetterMissingException e) {
            throw new GetterSetterMissingException(mTarget.getPrimaryKey().getColumnName(), mTarget.getTableName(), true);
        }
        StringBuilder strSql = new StringBuilder("update " + mTarget.getTableName() + " set ");
        if (mTarget.getColumns().stream()
                .noneMatch(f -> {
                    try {
                        return f.getValue(objTarget).isPresent();
                    } catch (GetterSetterMissingException e) {
                        throw new GetterSetterMissingException(f.getColumnName(), mTarget.getTableName(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })) {
            throw new NoSuchElementException();
        }
        mTarget.getColumns().forEach(f -> {
            try {
                if (f.getValue(objTarget).isPresent()) {
                    strSql.append(f.getColumnName()
                            + "="
                            + QueryProcessingUtils.toSQLData(
                            f.getValue(objTarget).get())
                            + ","
                    );
                }
            } catch (GetterSetterMissingException e) {
                throw new GetterSetterMissingException(f.getColumnName(), mTarget.getTableName(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        strSql.deleteCharAt(strSql.length()-1);
        strSql.append(" where " + mTarget.getPrimaryKey().getColumnName()
                + "="
                + mTarget.getPrimaryKey().getValue(objTarget).get());
        return strSql.toString();
    }
}
