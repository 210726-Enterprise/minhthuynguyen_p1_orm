package com.revature.p1.orm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Configuration {
	private static Connection objConnection = null;
	private static final String strDbUrl = "jdbc:postgresql://database-1.crgijayqoqqj.us-east-2.rds.amazonaws.com:5432/postgres?currentSchema=p1_admin";
	private static final String strUsername = "postgres";
	private static final String strPassword = "project0";
	private static List<Metamodel<Class<?>>> metamodelList;

	private Configuration() {
		super();
	}

	public static synchronized Connection getConnection() throws SQLException {
		try {
			if (objConnection == null || objConnection.isClosed()) {
				Class.forName("org.postgresql.Driver");
				objConnection = DriverManager.getConnection(strDbUrl, strUsername, strPassword);
			}
			return objConnection;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void addAnnotatedClass(Class annotatedClass) {
		if(metamodelList == null) {
			metamodelList = new LinkedList<>();
		}
		// generate a method in metamodel that transforms a class into an appropriate data model to be
		// transposed into a relation db object
		metamodelList.add(Metamodel.of(annotatedClass));
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
