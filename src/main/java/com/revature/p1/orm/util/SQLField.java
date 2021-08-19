package com.revature.p1.orm.util;

import java.lang.reflect.Field;

/**
 * Implemented by classes that wrap fields to be persisted in SQL.
 * Allows QueryProcessingUtils methods to be blanket-applied to any implementing class.
 * (Allows those methods to accept an entire SQLField object as an argument, rather than requiring code that uses
 * those methods to extract relevant data from them to pass as arguments.)
 */
public interface SQLField {
    String getName();
    Class<?> getType();
    String getColumnName();
    Field getField();
}
