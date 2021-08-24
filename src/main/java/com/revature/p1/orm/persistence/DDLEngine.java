package com.revature.p1.orm.persistence;

import com.revature.p1.orm.util.ColumnField;
import com.revature.p1.orm.util.Configuration;
import com.revature.p1.orm.util.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates SQL statements for DDL operations. (CREATE TABLE)
 */
public class DDLEngine {
    private static final Logger lLog4j = LoggerFactory.getLogger(DDLEngine.class);

    /**
     * Returns a SQL statement that creates a table with columns based on the annotated field of the class provided.
     * If the class has an annotated primary key field, forcefully defines it as a serial primary key in the database.
     * 'if not exists' clause is hardcoded into the SQL statement built.
     * @param strName String containing fully qualified class name, or reference to the
     *                class itself.
     * @return String for SQL statement.
     */
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
        lLog4j.trace("SQL generated: " + strSql);
        return strSql.toString();
    }
    public static String createByClass(Class<?> cTarget) {
        return createByClassName(cTarget.getName());
    }
}

