package com.revature.p1.orm.util;

import com.revature.p1.orm.annotations.Column;
import com.revature.p1.orm.annotations.Entity;
import com.revature.p1.orm.annotations.Id;
import com.revature.p1.orm.annotations.JoinColumn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Metamodel<T> {

    private final Class<T> cModelClass;
    private final IdField<Integer> fPrimaryKey;
    private boolean bPKInitialized = false;
    private final List<ColumnField> fColumns;
    private final List<ForeignKeyField> fForeignKeys;

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

    public IdField<Integer> getPrimaryKey() {
        if (fPrimaryKey == null) {
            if (bPKInitialized) {
                return null;
            }
            for (Field f : cModelClass.getDeclaredFields()) {
                if (f.getAnnotation(Id.class) != null) {
                    bPKInitialized = true;
                    return new IdField<>(f);
                }
            }
            bPKInitialized = true;
            return null;
        }
        return fPrimaryKey;
    }

    public List<ColumnField> getColumns() {
        if (fColumns == null) {
            Field[] fields = cModelClass.getDeclaredFields();
            List<ColumnField> fFoundColumns = new ArrayList<>();
            for (Field field : fields) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    fFoundColumns.add(new ColumnField(field));
                }
            }
            return fFoundColumns;
        }
        return fColumns;
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
