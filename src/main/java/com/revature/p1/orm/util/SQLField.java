package com.revature.p1.orm.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * Implemented by classes that wrap fields to be persisted in SQL.
 * Allows QueryProcessingUtils methods to be blanket-applied to any implementing class.
 * (Allows those methods to accept an entire SQLField object as an argument, rather than requiring code that uses
 * those methods to extract relevant data from them to pass as arguments.)
 */
public interface SQLField<T> {
    String getName();
    Class<?> getType();
    String getColumnName();
    Field getField();
    Optional<T> getValue(Object objInvoker) throws InvocationTargetException, IllegalAccessException;
}
