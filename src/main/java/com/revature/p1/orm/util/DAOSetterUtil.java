package com.revature.p1.orm.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DAOSetterUtil {
    public static void setField(Object objTarget, SQLField fColumn, Object objValue) throws InvocationTargetException, IllegalAccessException {
        Method mtSetter = getSetter(objTarget.getClass(),fColumn);
        System.out.println("Now finding setter for " + fColumn.getColumnName());
        mtSetter.invoke(objTarget,objValue);
    }

    public static Method getSetter(Class<?> cTarget, SQLField fColumn) {
        Metamodel<?> mTarget = Configuration.getMetamodelByClassName(cTarget.getName());
        if (mTarget == null) {
            return(null);
        }
        if (fColumn instanceof IdField) {
            return mTarget.getPKSetter();
        }
        return mTarget.getColumnsSetters().get(fColumn.getColumnName());
    }

    /**public static Method getSetter(Class<?> cTarget, SQLField fColumn) {
        for (Method mt : cTarget.getDeclaredMethods()) {
            if ((mt.getAnnotation(Setter.class) != null) && (mt.getAnnotation(Setter.class).columnName().equals(fColumn.getColumnName()))) {
                return mt;
            } else {
                if (!(mt.getAnnotation(Setter.class) == null)) {
                    System.out.println("Found annotated Setter class for column " + mt.getAnnotation(Setter.class).columnName() + " but not desired column " + fColumn.getColumnName());
                }
            }
        }
        System.out.println("Found no annotated Setter for column " + fColumn.getColumnName() + " in class " + cTarget.getName());
        return null;
    }*/
}
