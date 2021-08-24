package com.revature.p1.orm.persistence;

import com.revature.p1.orm.util.Configuration;
import com.revature.p1.orm.util.Metamodel;
import com.revature.p1.orm.util.QueryProcessingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates statements for DQL operations. (SELECT)
 */
public class DQLEngine {
    private static final Logger lLog4j = LoggerFactory.getLogger(DQLEngine.class);

    /**
     * Queries for all columns of a row matching the provided primary key ID from the table associated with the specified model class.
     * @param cTargetClass Model class of the table to be queried.
     * @param iID Int primary key ID to be retrieved.
     * @return String for SQL statement.
     */
    public static String selectRowByID(Class<?> cTargetClass, int iID) {
        Metamodel<?>mTarget = Configuration.getMetamodelByClassName(cTargetClass.getName());
        if (mTarget.getPrimaryKey() == null) {
            throw new IllegalStateException(cTargetClass.getName() + ": class contains no annotated ID field, cannot perform getRowByID!");
        }
        StringBuilder strSql = new StringBuilder("select * from " + mTarget.getTableName());
        strSql.append(" where " + mTarget.getPrimaryKey().getColumnName() + "=" + iID + " limit 1");
        lLog4j.trace("SQL generated: " + strSql);
        return strSql.toString();
    }
    /**
     * Returns the SQL string for a PreparedStatement that checks if a table with a given name exists in the database.
     * @return String for SQL prepared statement.
     */
    public static String prepareIfTableExists() {
        return "select exists (select from information_schema.tables where table_name = ?)";
    }

    /**
     * Selects the row with the highest primary key ID in the table corresponding to the given model class. Intended for the retrieveNewest() method.
     * @param cTargetClass Model class associated with desired table.
     * @return String for SQL statement.
     */
    public static String selectLastRowById(Class<?> cTargetClass) {
        Metamodel<?>mTarget = Configuration.getMetamodelByClassName(cTargetClass.getName());
        if (mTarget.getPrimaryKey() == null) {
            throw new IllegalStateException(cTargetClass.getName() + ": class contains no annotated ID field, cannot perform getRowByID!");
        }
        String strSql = "select * from " + mTarget.getTableName() + " order by " + mTarget.getPrimaryKey().getColumnName() + " desc limit 1";
        lLog4j.trace("SQL generated: " + strSql);
        return(strSql);
    }
}
