package com.revature.p1.orm.persistence;

import com.revature.p1.orm.util.ColumnField;
import com.revature.p1.orm.util.Configuration;
import com.revature.p1.orm.util.DAOSetterUtil;
import com.revature.p1.orm.util.Metamodel;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

public class SQLOperationHandler {
    private final Configuration objConnFactory;
    private final ArrayList<Metamodel<?>> mKnownTables;


    public SQLOperationHandler(Configuration objConnFactory) throws SQLException {
        mKnownTables = new ArrayList<>();
        this.objConnFactory = objConnFactory;
        updateExtantTables();
    }

    private void updateExtantTables() throws SQLException {
        try (Connection objConnection = objConnFactory.getConnection()) {
            PreparedStatement sPrep = objConnection.prepareStatement(DQLEngine.prepareIfTableExists());
            Configuration.getMetamodels().stream()
                    .filter(m -> !mKnownTables.contains(m))
                    .filter(m -> {
                        try {
                            sPrep.setString(1,m.getTableName());
                            ResultSet objRS = sPrep.executeQuery();
                            return(objRS.next());
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
    public boolean persistThis(Object obj) throws InvocationTargetException, IllegalAccessException {
        try (Connection objConnection = objConnFactory.getConnection()){
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
        try (Connection objConnection = objConnFactory.getConnection()) {
            Statement sStatement = objConnection.createStatement();
            ResultSet objRS = sStatement.executeQuery(DQLEngine.queryRowByID(mTarget.getBaseClass(),iID));
            Optional<?> objResult = Optional.of(mTarget.getBaseClass().newInstance());
            if (objRS.next()) {
                if (mTarget.getPrimaryKey()!=null) {
                    DAOSetterUtil.setField(objResult.get(), mTarget.getPrimaryKey(), objRS.getObject(mTarget.getPrimaryKey().getColumnName()));
                }
                for (int i=1;i<=objRS.getMetaData().getColumnCount();i++) {
                    if (mTarget.getTableFieldNames().contains(objRS.getMetaData().getColumnName(i))) {
                        int iTemp = i;
                        Optional<ColumnField> fSetTarget = mTarget.getColumns().stream()
                                .filter(f -> {
                                    try {
                                        return f.getColumnName().equals(objRS.getMetaData().getColumnName(iTemp));
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                        return false;
                                    }
                                })
                                .findFirst();
                        if (fSetTarget.isPresent()){
                            DAOSetterUtil.setField(objResult.get(), fSetTarget.get(), objRS.getObject(i));
                            continue;
                        }
                    }
                }
                objConnection.close();
                return objResult;
            } else {
                objConnection.close();
                throw new RuntimeException("Query returned no entries!");
            }
        }
    }

    public boolean updateIfExists(Object obj) throws SQLException, InvocationTargetException, IllegalAccessException {
        Metamodel<?>mObj = Configuration.getMetamodelByClassName(obj.getClass().getName());
        if (!mKnownTables.contains(mObj)) {
            updateExtantTables();
            if (!mKnownTables.contains(mObj)) {
                return persistThis(obj);
            }
        }
        try (Connection objConnection = objConnFactory.getConnection()) {
            Statement sStatement = objConnection.createStatement();

        }
        return false;
    }
}
