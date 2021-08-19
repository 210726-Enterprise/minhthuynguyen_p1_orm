package com.revature.p1.orm.util;

import com.revature.p1.orm.annotations.Id;

import java.lang.reflect.Field;

public class IdField implements SQLField {

    private Field field;

    public IdField(Field field) {
        if (field.getAnnotation(Id.class) == null) {
            throw new IllegalStateException("Cannot create IdField object! Provided field, " + getName() + "is not annotated with @Id");
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
        return field.getAnnotation(Id.class).columnName().toLowerCase();
    }

    public Field getField() {
        return field;
    }
}
