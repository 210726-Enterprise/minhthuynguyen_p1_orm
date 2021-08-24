package com.revature.p1.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Field to be used as the primary key column for a SQL table.
 * Currently only sequential integer primary keys are accepted. ONLY ANNOTATE INT/INTEGER FIELDS WITH THIS.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
	String columnName();
}
