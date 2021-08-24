package com.revature.p1.orm.util;

import com.revature.p1.orm.annotations.Id;
import com.revature.p1.orm.exception.GetterSetterMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Class representing an annotated Field in a model class that corresponds to a that class' primary key column.
 * Java data type of the value of the Field in question.
 */
public class IdField implements SQLField {
    private static final Logger lLog4j = LoggerFactory.getLogger(IdField.class);

    private final Field fThis;
    private static Method mtGetter, mtSetter;

    public IdField(Field fThis) {
        if (fThis.getAnnotation(Id.class) == null) {
            throw new IllegalStateException("Cannot create IdField object! Provided field, " + getName() + " is not annotated with @Id");
        }
        this.fThis = fThis;
        lLog4j.trace("New IdField initialized.");
    }

    public String getName() {
        return fThis.getName();
    }

    public Class<?> getType() {
        return fThis.getType();
    }

    public String getColumnName() {
        return fThis.getAnnotation(Id.class).columnName().toLowerCase();
    }

    public Field getField() {
        return fThis;
    }

    /**
     * Looks for a method named like a getter for the attached Field in its parent class, then if found it to this.mtGetter.
     */
    private void findGetter() {
        mtGetter = Arrays.stream(fThis.getDeclaringClass().getDeclaredMethods())
                .filter(mt -> mt.getName().toLowerCase()
                        .contains("get" + fThis.getName().toLowerCase()))
                .findFirst()
                .orElseThrow(GetterSetterMissingException::new);
    }

    /**
     * Invokes the getter for the attached Field on an instance of its parent class. The first time this is called, also calls findGetter().
     * @param objInvoker Object to invoke the getter.
     * @return Optional containing the getter's return, if any.
     */
    public Optional<Integer> getValue(Object objInvoker) throws InvocationTargetException, IllegalAccessException {
        if (mtGetter == null) {
            findGetter();
        }
        try {
            return Optional.of((int) mtGetter.invoke(objInvoker));
        } catch (ClassCastException e) {
            lLog4j.error("ClassCastException occurred in SQLField.getValue() for Field " + getField().getName() + " in class " + getField().getDeclaringClass().getName() + "!");
            throw e;
        }
    }

    /**
     * Looks for a method named like a setter for the attached Field in its parent class, then if found it to this.mtSetter.
     */
    private void findSetter() {
        mtSetter = Arrays.stream(fThis.getDeclaringClass().getDeclaredMethods())
                .filter(mt -> mt.getName().toLowerCase()
                        .contains("set" + fThis.getName().toLowerCase()))
                .findFirst()
                .orElseThrow(GetterSetterMissingException::new);
    }

    /**
     * Invokes the setter for the attached Field on an instance of its parent class. The first time this is called, also calls findSetter().
     * @param objInvoker Object to invoke setter.
     * @param objValue Value to set the Field to.
     * @return true
     * @throws IllegalArgumentException Thrown when an attempt to set a non-zero field to something else, since primary keys shouldn't be changed after assignment.
     */
    public boolean setValue(Object objInvoker, Object objValue) throws InvocationTargetException, IllegalAccessException {
        if (mtSetter == null) {
            findSetter();
        }
        if (!getValue(objInvoker).isPresent() || ((getValue(objInvoker).get())==0)) {
            mtSetter.invoke(objInvoker, objValue);
            return true;
        }
        lLog4j.error("Primary key field " + getName() + "'s setter method was called even though it already has a nonzero value!");
        throw new IllegalArgumentException();
    }
}
