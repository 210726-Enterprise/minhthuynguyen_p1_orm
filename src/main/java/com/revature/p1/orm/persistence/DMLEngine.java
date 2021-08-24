package com.revature.p1.orm.persistence;

import com.revature.p1.orm.exception.GetterSetterMissingException;
import com.revature.p1.orm.exception.OperationRequiresPrimaryKeyException;
import com.revature.p1.orm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Generates SQL statements for DML operations. (UPDATE, INSERT)
 */
public class DMLEngine {
    private static final Logger lLog4j = LoggerFactory.getLogger(DMLEngine.class);

    /**
     * Parses the values of a model object's fields and inserts them as a new row to its corresponding table.
     * Ignores the object's value for the primary key field and instead instructs the database to use the next serial value.
     * @param objTarget Model object whose fields contain data to be inserted.
     * @return String for SQL statement.
     */
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
                            strSql.append("null,");
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
        lLog4j.trace("SQL generated: " + strSql);
        return strSql.toString();
    }

    /**
     * Attempts to delete row(s), either based on primary key or targeting rows that match every non-null field of the provided model object.
     * When not deleting by ID, this statement does not provide any constraints on number of rows deleted. Wrap this in a transaction in that case!
     * @param cTarget Either a class reference to delete by ID, or a model object whose fields are used to identify the target row(s). If an object with a primary key field set to a positive int value is passed, attempts to delete by ID and remaining fields are ignored.
     * @param iID int primary key. Needed if and only if the first param was a class reference.
     * @return String for SQL statement.
     */
    public static String deleteRow(Class<?> cTarget, int iID) {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        String strSql = "delete from " + mTarget.getTableName() + " where " + mTarget.getPrimaryKey().getColumnName() + "=" + iID;
        lLog4j.trace("SQL generated: " + strSql);
        return(strSql);
    }
    public static String deleteRow(Object objTarget) throws InvocationTargetException, IllegalAccessException {
        Class<?> cTarget = objTarget.getClass();
        Metamodel<Class<?>> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        if (mTarget == null) {
            return null;
        }
        if (mTarget.getPrimaryKey()!=null) {
            Optional<Integer> iId = mTarget.getPrimaryKey().getValue(objTarget);
            if (iId.isPresent() && iId.get() > 0) {
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
                        lLog4j.warn(e.getMessage());
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
                            ) + " and ");
                        }
                    } catch (GetterSetterMissingException e) {
                        throw new GetterSetterMissingException(f.getColumnName(), mTarget.getTableName(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        lLog4j.error(e.getMessage());
                    }
                });
        strSql.delete(strSql.length()-5,strSql.length());
        lLog4j.trace("SQL generated: " + strSql);
        return strSql.toString();
    }

    /**
     * Updates a row to match the column fields of the input model object. Only applicable to tables with primary keys, and with objects with a primary key field set to a positive int.
     * @param objTarget Object containing the primary key and desired field values.
     * @return String for SQL statement.
     */
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
                        lLog4j.error(e.getMessage());
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
                lLog4j.error(e.getMessage());
            }
        });
        strSql.deleteCharAt(strSql.length()-1);
        strSql.append(" where " + mTarget.getPrimaryKey().getColumnName()
                + "="
                + mTarget.getPrimaryKey().getValue(objTarget).get());
        lLog4j.trace("SQL generated: " + strSql);
        return strSql.toString();
    }
}
