package com.revature.p1.orm.persistence;

import com.revature.p1.orm.util.*;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

public class SQLOperationHandler {
    //private final Configuration objConnFactory;
    private final ArrayList<Metamodel<?>> mKnownTables;


    public SQLOperationHandler() throws SQLException {
        mKnownTables = new ArrayList<>();
        updateExtantTables();
    }

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
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                            return false;
                        }
                    }).forEach(m -> mKnownTables.add(m));
        }
    }

    /** CRUD - Create
     *
     * @param obj
     * @return
     * @throws SQLException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public boolean persistThis(Object obj) {
        try (Connection objConnection = Configuration.getConnection()){
            Statement sStatement = objConnection.createStatement();
            if (!mKnownTables.contains(Configuration.getMetamodelByClassName(obj.getClass().getName()))) {
                sStatement.execute(DDLEngine.createByClass(obj.getClass()));
                mKnownTables.add(Configuration.getMetamodelByClassName(obj.getClass().getName()));
            }
            sStatement.execute(DMLEngine.insertRow(obj));
            objConnection.close();
            return true;
        } catch (SQLException e1) {
            System.out.println(e1.getMessage());
            return false;
        }
    }

    /** CRUD - Read
     *
     * @param iID
     * @param cTarget
     * @return
     * @throws SQLException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Optional<?> retrieveByID(int iID, Class<?> cTarget) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return retrieveByMetamodel(iID, Configuration.getMetamodelByClassName(cTarget.getName()));
    }
    public Optional<?> retrieveByID(int iID, String strTargetName) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return retrieveByMetamodel(iID, Configuration.getMetamodelByClassName(strTargetName));
    }
    private Optional<?> retrieveByMetamodel(int iID, Metamodel<?> mTarget) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!mKnownTables.contains(mTarget)) {
            return Optional.empty();
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

    public Optional<?> retrieveNewest(Metamodel<?> mTarget) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!mKnownTables.contains(mTarget)) {
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


    public boolean update(Object objTarget) throws SQLException, InvocationTargetException, IllegalAccessException {
        return update(objTarget, 1);
    }
    public boolean update(Object objTarget, int iRowCount) throws SQLException, InvocationTargetException, IllegalAccessException {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(objTarget.getClass().getName());
        if (mTarget == null) {
            return false;
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
                return false;
            }
            sStatement.execute("commit");
            return true;
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
                return true;
            }
        }
        return false;
    }
}
