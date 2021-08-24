package com.revature.p1.orm.persistence;

import com.revature.p1.orm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

/**
 * Instantiated DAO class that performs SQL operations. Each instance keeps its own ArrayList of known tables.
 *
 * TODO: Turn all functionality here into static methods and fields OR consider whether this would work better as a singleton class.
 */
public class SQLOperationHandler {
    private static final Logger lLog4j = LoggerFactory.getLogger(SQLOperationHandler.class);

    private final ArrayList<Metamodel<?>> mKnownTables;
    public SQLOperationHandler() throws SQLException {
        mKnownTables = new ArrayList<>();
        updateExtantTables();
    }

    /**
     * Updates list of metamodels for whom a table with matching table name is found in the database.
     * Called in the default constructor, and whenever an operation is passed a metamodel that's not on the list to double check.
     * @throws SQLException
     */
    private void updateExtantTables() throws SQLException {
        try (Connection objConnection = Configuration.getConnection()) {
            PreparedStatement sPrep = objConnection.prepareStatement(DQLEngine.prepareIfTableExists());
            Configuration.getMetamodels().stream()
                    .filter(m -> !mKnownTables.contains(m))
                    .filter(m -> {
                        try {
                            sPrep.setString(1,m.getTableName());
                            ResultSet objRs = sPrep.executeQuery();
                            return(objRs.next());
                        } catch (SQLException e) {
                            e.printStackTrace();
                            lLog4j.error(e.getMessage());
                            lLog4j.error(e.getSQLState());
                            return false;
                        }
                    }).forEach(m -> mKnownTables.add(m));
        }
    }

    /** CRUD - Create
     * Currently, a single method handles both table creation (C) and row insertion (U).
     */
    /**
     * Catch-all method that checks whether the class of the passed model object already has a table in the database. If it doesn't, creates a new table for that class by inferring columns from the class' annotated fields. Afterwards insert a new row to the table based on the contents of the input object's annotated fields.
     *
     * @param objInput Object to be persisted.
     * @return An optional based on insertion results.
     *          - If insertion was successful and the model class contains a primary key, returns optional of an object representing the highest ID row in the table. (Helps confirm data was inserted correctly.)
     *          - If insertion was successful but the model class has no primary key, return optional of the input object.
     *          - If insertion was unsuccessful but no exceptions/errors were encountered, returns an empty optional.
     */
    public Optional<?> persistThis(Object objInput) throws SQLException, InvocationTargetException,InstantiationException,IllegalAccessException {
        try (Connection objConnection = Configuration.getConnection()) {
            Metamodel<Class<?>> mTarget = Configuration.getMetamodelByClassName(objInput.getClass().getName());
            Statement sStatement = objConnection.createStatement();
            if (!mKnownTables.contains(mTarget)) {
                sStatement.execute(DDLEngine.createByClass(objInput.getClass()));
                mKnownTables.add(mTarget);
            }
            if (sStatement.executeUpdate(DMLEngine.insertRow(objInput)) > 0) {
                if (mTarget.getPrimaryKey()!=null) {
                    return retrieveNewest(mTarget);
                }
                lLog4j.trace("Persisted new object of class " + mTarget.getBaseClass().getName());
                return Optional.of(objInput);
            }
            return Optional.empty();
        }
    }

    /** CRUD - Read
     */
    /**
     * Retrieves an object representing data in the requested row. Requires the primary key of the desired row.
     * @param iID int primary key of desired row.
     * @param cTarget Accepts either a reference to the model class or metamodel associated with the desired table, or a String of the model class' fully qualified name.
     * @return Optional of a model object that contains the data retrieved. Returns an empty optional if no table with the table name specified in the object's metamodel is found, or if the SQL query returned zero rows.
     */
    public Optional<?> retrieveByID(int iID, Class<?> cTarget) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return retrieveByMetamodel(iID, Configuration.getMetamodelByClassName(cTarget.getName()));
    }
    public Optional<?> retrieveByID(int iID, String strTargetName) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return retrieveByMetamodel(iID, Configuration.getMetamodelByClassName(strTargetName));
    }
    private Optional<?> retrieveByMetamodel(int iID, Metamodel<?> mTarget) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!mKnownTables.contains(mTarget)) {
            updateExtantTables();
            if (!mKnownTables.contains(mTarget)) {
                return Optional.empty();
            }
        }
        try (Connection objConnection = Configuration.getConnection()) {
            Statement sStatement = objConnection.createStatement();
            ResultSet objRs = sStatement.executeQuery(DQLEngine.selectRowByID(mTarget.getBaseClass(),iID));
            Optional<?> objResult = Optional.of(mTarget.getBaseClass().newInstance());
            if (objRs.next()) {
                if (mTarget.getPrimaryKey()!=null) {
                    mTarget.getPrimaryKey().setValue(objResult.get(),objRs.getInt(mTarget.getPrimaryKey().getColumnName()));
                }
                for (int i=1;i<=objRs.getMetaData().getColumnCount();i++) {
                    if (mTarget.getTableFieldNames().contains(objRs.getMetaData().getColumnName(i))) {
                        int iTemp = i;
                        Optional<ColumnField> fSetTarget = mTarget.getColumns().stream()
                                .filter(f -> {
                                    try {
                                        return f.getColumnName().equals(objRs.getMetaData().getColumnName(iTemp));
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        lLog4j.error(e.getMessage());
                                        lLog4j.error(e.getSQLState());
                                        return false;
                                    }
                                })
                                .findFirst();
                        if (fSetTarget.isPresent()){
                            fSetTarget.get().setValue(objResult.get(),objRs.getObject(i));
                        }
                    }
                }
                objConnection.close();
                lLog4j.trace("SELECT query performed for class " + mTarget.getBaseClass().getName());
                return objResult;
            } else {
                objConnection.close();
                return Optional.empty();
            }
        }
    }

    /**
     * Retrieves the most recently inserted extant row of a desired table, but only if it has a primary key column.
     * The query is based on highest primary key ID, and thus this does not take into account deleted rows or rows with manually assigned IDs (manual ID assignment is currently outside the scope of this ORM).
     * @param mTarget Metamodel associated with target table.
     * @return Optional of an object representing the row retrieved. Returns empty optional instead if no rows are present in the table.
     */
    public Optional<?> retrieveNewest(Metamodel<?> mTarget) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!mKnownTables.contains(mTarget) || mTarget.getPrimaryKey() == null) {
            return Optional.empty();
        }
        try (Connection objConnection = Configuration.getConnection()) {
            Statement sStatement = objConnection.createStatement();
            ResultSet objRs = sStatement.executeQuery(DQLEngine.selectLastRowById(mTarget.getBaseClass()));
            Optional<?> objResult = Optional.of(mTarget.getBaseClass().newInstance());
            if (objRs.next()) {
                if (mTarget.getPrimaryKey()!=null) {
                    mTarget.getPrimaryKey().setValue(objResult.get(),objRs.getInt(mTarget.getPrimaryKey().getColumnName()));
                }
                for (int i=1;i<=objRs.getMetaData().getColumnCount();i++) {
                    if (mTarget.getTableFieldNames().contains(objRs.getMetaData().getColumnName(i))) {
                        int iTemp = i;
                        Optional<ColumnField> fSetTarget = mTarget.getColumns().stream()
                                .filter(f -> {
                                    try {
                                        return f.getColumnName().equals(objRs.getMetaData().getColumnName(iTemp));
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        lLog4j.error(e.getMessage());
                                        lLog4j.error(e.getSQLState());
                                        return false;
                                    }
                                })
                                .findFirst();
                        if (fSetTarget.isPresent()){
                            fSetTarget.get().setValue(objResult.get(),objRs.getObject(i));
                        }
                    }
                }
                objConnection.close();
                return objResult;
            } else {
                objConnection.close();
                return Optional.empty();
            }
        }
    }

    /** CRUD - Update
     */
    /**
     * Updates a database row with primary key matching that of the object passed. Code logic includes some redundancy for future implementation of the ability to update one or more rows based on matching column values, but for now this only works one row at a time and requires a primary key for finding the desired row.
     * @param objTarget
     * @return Optional of the input object if update successful; empty optional otherwise.
     */
    public Optional<?> update(Object objTarget) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return update(objTarget, 1);
    }
    public Optional<?> update(Object objTarget, int iRowCount) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(objTarget.getClass().getName());
        if (mTarget == null) {
            return Optional.empty();
        }
        if (!mKnownTables.contains(mTarget)) {
            updateExtantTables();
            if (!mKnownTables.contains(mTarget)) {
                return persistThis(objTarget);
            }
        }
        try (Connection objConnection = Configuration.getConnection()) {
            Statement sStatement = objConnection.createStatement();
            sStatement.execute("begin");
            String strSql = DMLEngine.updateRowById(objTarget);
            int iUpdated = sStatement.executeUpdate(strSql);
            if (iUpdated  < iRowCount) {
                sStatement.execute("rollback");
                return Optional.empty();
            }
            sStatement.execute("commit");
            lLog4j.trace("Successfully updated a row for class " + mTarget.getBaseClass().getName());
            return Optional.of(objTarget);
        }
    }

    public boolean delete(Object objTarget) throws SQLException, InvocationTargetException, IllegalAccessException {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(objTarget.getClass().getName());
        if (!mKnownTables.contains(mTarget)) {
            updateExtantTables();
            if (!mKnownTables.contains(mTarget)) {
                throw new NoSuchElementException("Table named " + mTarget.getTableName() + " not found in database! It might have been dropped already.");
            }
        }
        try (Connection objConnection = Configuration.getConnection()) {
            Statement sStatement = objConnection.createStatement();
            if (sStatement.executeUpdate(DMLEngine.deleteRow(objTarget)) > 0) {
                lLog4j.trace("Successfully deleted a row for class " + mTarget.getBaseClass().getName());
                return true;
            }
        }
        return false;
    }
}
