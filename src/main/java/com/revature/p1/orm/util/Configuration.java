package com.revature.p1.orm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Singleton class that handles startup operations, e.g. connecting to the database and building metamodels.
 */
public class Configuration {
	private static final Logger lLog4j = LoggerFactory.getLogger(Configuration.class);

	private static Connection objConnection = null;
	private static final String strDbUrl = "jdbc:postgresql://database-1.crgijayqoqqj.us-east-2.rds.amazonaws.com:5432/postgres?currentSchema=p1_admin";
	private static final String strUsername = "postgres";
	private static final String strPassword = "project0";
	private static List<Metamodel<Class<?>>> metamodelList;

	private Configuration() {
	}

	/**
	 * If a Connection is not already initialized and open, requests for one from DriverManager and stores its reference for later.
	 * @return Connection
	 */
	public static synchronized Connection getConnection() {
		try {
			if (objConnection == null || objConnection.isClosed()) {
				Class.forName("org.postgresql.Driver");
				objConnection = DriverManager.getConnection(strDbUrl, strUsername, strPassword);
			}
			return objConnection;
		} catch (Exception e) {
			e.printStackTrace();
			lLog4j.warn("Configuration class failed to establish a new Connection.");
			return null;
		}
	}

	/**
	 * Creates a Metamodel out of provided annotated class and adds it to an internal list for later reference.
	 * @param cAnnotatedClass
	 */
	public static void addAnnotatedClass(Class cAnnotatedClass) {
		if(metamodelList == null) {
			metamodelList = new LinkedList<>();
		}
		metamodelList.add(Metamodel.of(cAnnotatedClass));
		lLog4j.trace("Added new Metamodel for class named " + cAnnotatedClass.getName());
	}

	/**
	 * @return List of all Metamodels built by this Configuration instance.
	 */
	public static List<Metamodel<Class<?>>> getMetamodels() {
		return (metamodelList == null) ? Collections.emptyList() : metamodelList;
	}

	/**
	 * Retrieves a built Metamodel from internal list based on fully qualified name of the class associated with it.
	 * @param strName Fully qualified class name, obtainable via the Class class' .getName() method.
	 * @return Metamodel for the indicated class.
	 */
	public static Metamodel<Class<?>> getMetamodelByClassName(String strName) {
		for (Metamodel<Class<?>> m : metamodelList) {
			if (m.getClassName().equals(strName)) {
				return m;
			}
		}
		return null;
	}



}
