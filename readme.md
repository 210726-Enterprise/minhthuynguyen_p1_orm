# Installation
1) Install this package to your Maven local repository.
2) Declare the following dependency in your module's pom.xml:
```        
<dependency>
   <groupId>com.revature.p1.orm</groupId>
    <artifactId>Project1ORM</artifactId>
   <version>1.0</version>
</dependency>
   ```
# Requirements for model classes to be persisted by this ORM + startup instructions
1) Annotate class with @Entity and provide a tableName.
2) Provide a no-args constructor. If there is a primary key field, DO NOT GIVE INITIALIZE A VALUE FOR IT IN THE CONSTRUCTOR.
3) Annotate fields to be persisted with @Column, and provide columnName for each.
4) (Optional) Annotate up to one int/Integer field to be used as the primary key column with @Id and provide a columnName. Primary keys are hardcoded to use a serial incrementing int, and this ORM provides no client-side control over the values of the primary key column. Do NOT also annotate it with @Column.
5) Provide public getters and setters for every field to be persisted. These methods must be named "get\<field name\>" or "set\<field name\>", respectively (case-insensitive).
6) On module startup, call the com.revature.p1.orm.util.Configuration.addAnnotatedClass(Class clazz) method for each model class to be persisted, where clazz is a reference to the class.
7) In your service layer, initialize a new instance of the com.revature.p1.orm.persistence.SQLOperationHandler class. Maintain a reference to it; it will be used to call API methods.

# Currently available API Methods
- `com.revature.p1.orm.persistence.SQLOperationHandler.persistThis(Object obj)` Checks whether the class of the passed model object already has a table in the database. If it doesn't, creates a new table for that class by inferring columns from the class' annotated fields. Afterwards, inserts a new row to the table based on the contents of the input object's annotated fields.
    - Params: An object belonging to an annotated class with its fields set to the values of the row to be inserted. Primary key field is ignored.
    - Returns: An Optional. It contains an Object of the same class as the input containing the inserted row's persisted values if insert was successful (may not match input exactly if floating points were used), or is empty if insert was unsuccessful (under this ORM's defined behaviour, this should never be returned; method should throw exceptions instead).
- `com.revature.p1.orm.persistence.SQLOperationHandler.retrieveById(int iId, ?)` Retrieves an object representing data in the row with the primary key ID provided. Only possible on tables with a primary key column.
    - Params: 
        - 1) Integer value of primary key ID.
        - 2) Wildcard param that can be either: a Class reference to the model class for the desired table, or a String containing the fully qualified name of that class.
    - Returns: An Optional. Contains an Object of the appropriate model class with the values of the queried row if query was successful. Empty if query returned no rows, or if the desired table does not exist in database.
- `com.revature.p1.orm.persistence.SQLOperationHandler.update(Object obj)` Updates a database row with primary key matching that of the object passed, so that its columns match the values of the object's non-null non-primary key column fields. Currently requires a primary key ID and only updates 1 row at a time.
    - Params: An object belonging to an annotated class with its fields set to the desired values. Fields with null values are not updated.
    - Returns: An Optional. Contains the input object if update is successful, or is empty if unsuccessful. Throws a NoSuchElementException if all column fields of input object except for the ID field are null.
- `com.revature.p1.orm.persistence.SQLOperationHandler.delete(Object obj)` Attempts to delete exactly one row where either the primary key matches that of the input object, or, if no positive integer primary key is provided, where all of the input object's non-null column fields match the value of corresponding columns of the row.
    - Params: An object belonging to an annotated class. Its fields are used to identify the row to be deleted. If a positive primary key is provided, all other fields are ignored.
    - Returns: True if the deletion was successful and affected exactly one row, false if no rows were deleted (likely due to inability to find one that matches input). Throws a BatchUpdateException if multiple rows match input.