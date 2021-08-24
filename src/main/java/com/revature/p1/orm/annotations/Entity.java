package com.revature.p1.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Class for Metamodel construction, and denotes that it should be persisted in its own SQL table.
 */
@Target(ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface Entity {
	String tableName();
}