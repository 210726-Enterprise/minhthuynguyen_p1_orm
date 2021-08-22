package com.revature.p1.orm.util;

import com.revature.p1.orm.annotations.JoinColumn;
import com.revature.p1.orm.exception.GetterSetterMissingException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class ForeignKeyField<T> implements SQLField {

    private final Field fThis;
    private Method mtGetter, mtSetter;

    public ForeignKeyField(Field fThis) {
        if (fThis.getAnnotation(JoinColumn.class) == null) {
            throw new IllegalStateException("Cannot create ForeignKeyField object! Provided field, " + getName() + "is not annotated with @JoinColumn");
        }
        this.fThis = fThis;
    }

    public String getName() {
        return fThis.getName();
    }

    public Class<?> getType() {
        return fThis.getType();
    }

    public String getColumnName() {
        return fThis.getAnnotation(JoinColumn.class).columnName().toLowerCase();
    }

    public Field getField() {
        return fThis;
    }

    private void findGetter() {
        mtGetter = Arrays.stream(fThis.getDeclaringClass().getDeclaredMethods())
                .filter(mt -> mt.getName().toLowerCase()
                        .contains("get" + fThis.getName().toLowerCase()))
                .findFirst()
                .orElseThrow(GetterSetterMissingException::new);
    }

    public Optional<T> getValue(Object objInvoker) throws InvocationTargetException, IllegalAccessException {
        if (mtGetter == null) {
            findGetter();
        }
        return Optional.of((T)mtGetter.invoke(objInvoker));
    }

    private void findSetter() {
        mtSetter = Arrays.stream(fThis.getDeclaringClass().getDeclaredMethods())
                .filter(mt -> mt.getName().toLowerCase()
                        .contains("set" + fThis.getName().toLowerCase()))
                .findFirst()
                .orElseThrow(GetterSetterMissingException::new);
    }

    public boolean setValue(Object objInvoker, T objValue) throws InvocationTargetException, IllegalAccessException {
        if (mtSetter == null) {
            findSetter();
        }
        mtSetter.invoke(objInvoker,objValue);
        return true;
    }
}
