package com.revature.p1.orm.util;

import com.revature.p1.orm.annotations.Column;
import com.revature.p1.orm.annotations.Entity;
import com.revature.p1.orm.annotations.Id;
import com.revature.p1.orm.persistence.SQLTypeMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Metamodel of an annotated class that contains definitions for how objects of that class should be persisted in a SQL database.
 * @param <T> Type of class provided.
 */
public class Metamodel<T> {
    private static final Logger lLog4j = LoggerFactory.getLogger(Metamodel.class);

    private final Class<T> cModelClass;
    private final IdField fPrimaryKey;
    private boolean bPKInitialized = false;
    private final List<ColumnField> fColumns;

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

    /**
     * Gets a reference to the IdField representing this Metamodel's primary key column.
     * On first call, searches the model class for an annotated IdField.
     * @return IdField
     */
    public IdField getPrimaryKey() {
        if (fPrimaryKey == null) {
            if (bPKInitialized) {
                return null;
            }
            for (Field f : cModelClass.getDeclaredFields()) {
                if (f.getAnnotation(Id.class) != null) {
                    bPKInitialized = true;
                    return new IdField(f);
                }
            }
            bPKInitialized = true;
            return null;
        }
        return fPrimaryKey;
    }

    /**
     * Gets a List containing all ColumnFields in this Metamodel.
     * On first call, builds the List by searching through the model class for annotated ColumnFields.
     * @return List of ColumnFields.
     */
    public List<ColumnField> getColumns() {
        if (fColumns == null) {
            Field[] fFound = cModelClass.getDeclaredFields();
            List<ColumnField> fFoundColumns = new ArrayList<>();
            for (Field field : fFound) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    fFoundColumns.add(new ColumnField(field));
                }
            }
            return fFoundColumns;
        }
        return fColumns;
    }

    /**
     * Gets a List of all SQLFields corresponding to columns of any kind in this Metamodel.
     * Builds the list from known SQLFields on first call.
     * @return List of SQLFields.
     */
    public List<SQLField> getTableFields() {
        List<SQLField> fAllFields = new ArrayList<>();
        if (getPrimaryKey() != null) {
            fAllFields.add(getPrimaryKey());
        }
        if (!getColumns().isEmpty()) {
            for (ColumnField f : getColumns()) {
                fAllFields.add(f);
            }
        }
        return fAllFields;
    }

    /**
     * Returns a List containing the names of all SQLFields in the previous method's List.
     * @return
     */
    public List<String> getTableFieldNames() {
        List<String> strFieldNames = new ArrayList<>();
        for (SQLField f : getTableFields()) {
            strFieldNames.add(f.getColumnName());
        }
        return strFieldNames;
    }
}
