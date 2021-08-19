package com.revature.p1.orm.util;

import com.revature.p1.orm.annotations.Column;
import com.revature.p1.orm.annotations.Entity;
import com.revature.p1.orm.annotations.Id;
import com.revature.p1.orm.annotations.JoinColumn;
import com.revature.p1.orm.exception.GetterSetterMissingException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Metamodel<T> {

    private final Class<T> cModelClass;
    private final IdField fPrimaryKey;
    private final List<ColumnField> fColumns;
    private final List<ForeignKeyField> fForeignKeys;
    private final HashMap<String, Method> fmtColumnsGetters;
    private final HashMap<String, Method> fmtColumnsSetters;

    public static <T> Metamodel<T> of(Class<T> cClass) {
        if (cClass.getAnnotation(Entity.class) == null) {
            throw new IllegalStateException("Cannot create Metamodel object! Provided class, " + cClass.getName() + "is not annotated with @Entity");
        }
        return new Metamodel<>(cClass);
    }

    public Metamodel(Class<T> cModelClass) {
        this.cModelClass = cModelClass;
        this.fPrimaryKey = getPrimaryKey();
        this.fColumns = getColumns();
        this.fForeignKeys = getForeignKeys();
        this.fmtColumnsGetters = new HashMap<>();
        this.fmtColumnsSetters = new HashMap<>();
    }

    public HashMap<String, Method> getColumnsGetters() {
        if (fmtColumnsGetters.isEmpty()) {
            Method[] mtFoundMethods = cModelClass.getDeclaredMethods();
            for (SQLField f : getTableFields()) {
                boolean bFoundMethod = false;
                for (Method mt : mtFoundMethods) {
                    if (mt.getName().toLowerCase().matches("(.*)get" + f.getField().getName().toLowerCase() + "(.*)")) {
                        fmtColumnsGetters.put(f.getColumnName(), mt);
                        bFoundMethod = true;
                        break;
                    }
                }
                if (!bFoundMethod) {
                    throw new GetterSetterMissingException(f.getColumnName(), getTableName(), true);
                }
            }
        }
        return fmtColumnsGetters;
    }

    public HashMap<String, Method> getColumnsSetters() {
        if (fmtColumnsSetters.isEmpty()) {
            Method[] mtFoundMethods = cModelClass.getDeclaredMethods();
            for (SQLField f : getTableFields()) {
                boolean bFoundMethod = false;
                for (Method mt : mtFoundMethods) {
                    if (mt.getName().toLowerCase().matches("(.*)set" + f.getField().getName().toLowerCase() + "(.*)")) {
                        fmtColumnsSetters.put(f.getColumnName(), mt);
                        bFoundMethod = true;
                        break;
                    }
                }
                if (!bFoundMethod) {
                    throw new GetterSetterMissingException(f.getColumnName(), getTableName(), false);
                }
            }
        }
        return fmtColumnsSetters;
    }


    public String getClassName() {
        return cModelClass.getName();
    }

    public Class<?> getBaseClass() {
        return cModelClass;
    }

    public String getTableName() {
        return cModelClass.getAnnotation(Entity.class).tableName().toLowerCase();
    }

    public String getSimpleClassName() {
        return cModelClass.getSimpleName();
    }

    public IdField getPrimaryKey() {
        Field[] fields = cModelClass.getDeclaredFields();
        for (Field field : fields) {
            Id primaryKey = field.getAnnotation(Id.class);
            if (primaryKey != null) {
                return new IdField(field);
            }
        }
        //throw new RuntimeException("Did not find a field annotated with @Id in: " + cModelClass.getName());
        return null;
    }

    public List<ColumnField> getColumns() {
        Field[] fields = cModelClass.getDeclaredFields();
        List<ColumnField> fFoundColumns = new ArrayList<>();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                fFoundColumns.add( new ColumnField(field));
            }
        }
        return fFoundColumns;
    }

    public List<SQLField> getTableFields() {
        List<SQLField> fAllFields = new ArrayList<>();
        if (getPrimaryKey() != null) {
            fAllFields.add(getPrimaryKey());
        }
        if (!getForeignKeys().isEmpty()) {
            for (ForeignKeyField f : getForeignKeys()) {
                fAllFields.add(f);
            }
        }
        if (!getColumns().isEmpty()) {
            for (ColumnField f : getColumns()) {
                fAllFields.add(f);
            }
        }
        return fAllFields;
    }
    public List<String> getTableFieldNames() {
        List<String> strFieldNames = new ArrayList<>();
        for (SQLField f : getTableFields()) {
            strFieldNames.add(f.getColumnName());
        }
        return strFieldNames;
    }

    public List<ForeignKeyField> getForeignKeys() {

        List<ForeignKeyField> foreignKeyFields = new ArrayList<>();
        Field[] fields = cModelClass.getDeclaredFields();
        for (Field field : fields) {
            JoinColumn column = field.getAnnotation(JoinColumn.class);
            if (column != null) {
                foreignKeyFields.add(new ForeignKeyField(field));
            }
        }
        return foreignKeyFields;
    }
}
