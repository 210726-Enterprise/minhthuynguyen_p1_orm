package com.revature.p1.orm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Configuration {
	
	private final String strDbUrl;
	private final String strUsername;
	private final String strPassword;
	private static List<Metamodel<Class<?>>> metamodelList;

	public Configuration(String strDbUrl, String strUsername, String strPassword) {
		this.strDbUrl = strDbUrl;
		this.strUsername = strUsername;
		this.strPassword = strPassword;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(strDbUrl, strUsername, strPassword);
	}

	public Configuration addAnnotatedClass(Class annotatedClass) {
		if(metamodelList == null) {
			metamodelList = new LinkedList<>();
		}
		// generate a method in metamodel that transforms a class into an appropriate data model to be
		// transposed into a relation db object
		metamodelList.add(Metamodel.of(annotatedClass));

		return this; // we're returning the
	}
	
	public static List<Metamodel<Class<?>>> getMetamodels() {
		return (metamodelList == null) ? Collections.emptyList() : metamodelList;
	}
	public static Metamodel<Class<?>> getMetamodelByClassName(String strName) {
		for (Metamodel<Class<?>> m : metamodelList) {
			if (m.getClassName().equals(strName)) {
				return m;
			}
		}
		return null;
	}



}
