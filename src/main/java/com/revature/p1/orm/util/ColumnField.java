package com.revature.p1.orm.util;

import com.revature.p1.orm.annotations.Column;

import java.lang.reflect.Field;

public class ColumnField implements SQLField {

    private Field field;

    public ColumnField(Field field) {
        if (field.getAnnotation(Column.class) == null) {
            throw new IllegalStateException("Cannot create ColumnField object! Provided field, " + getName() + "is not annotated with @Column");
        }
        this.field = field;
    }
    @Override
    public String getName() {
        return field.getName();
    }

    public Class<?> getType() {
        return field.getType();
    }

    public String getColumnName() {
        return field.getAnnotation(Column.class).columnName().toLowerCase();
    }

    public Field getField() {
        return field;
    }
}
