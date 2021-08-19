package com.revature.p1.orm.util;

import com.revature.p1.orm.annotations.JoinColumn;

import java.lang.reflect.Field;

public class ForeignKeyField implements SQLField {

    private Field field;

    public ForeignKeyField(Field field) {
        if (field.getAnnotation(JoinColumn.class) == null) {
            throw new IllegalStateException("Cannot create ForeignKeyField object! Provided field, " + getName() + "is not annotated with @JoinColumn");
        }
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }

    public Class<?> getType() {
        return field.getType();
    }

    public String getColumnName() {
        return field.getAnnotation(JoinColumn.class).columnName().toLowerCase();
    }

    public Field getField() {
        return field;
    }
}
