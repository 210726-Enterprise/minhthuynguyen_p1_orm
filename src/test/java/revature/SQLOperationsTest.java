package revature;

import com.revature.p1.orm.persistence.DQLEngine;
import com.revature.p1.orm.persistence.SQLOperationHandler;
import com.revature.p1.orm.util.Configuration;
import org.junit.jupiter.api.*;
import revature.model.TestClass;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SQLOperationsTest {
    private static final TestClass test = new TestClass("test", (int)(Math.random()*100));;
    private SQLOperationHandler objHandler;
    {
        Configuration.addAnnotatedClass(TestClass.class);
    }

    @BeforeEach
    void setup() throws SQLException {
        objHandler = new SQLOperationHandler();
    }

    @Test
    @Order(1)
    void queryRowByID() {
        Assertions.assertEquals("select * from test_table where test_id=1 limit 1", DQLEngine.selectRowByID(TestClass.class,1));
    }

    @Test
    @Order(3)
    void insertRow() throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Assertions.assertTrue(objHandler.persistThis(test).isPresent());
        Assertions.assertTrue(objHandler.persistThis(new TestClass(null,3)).isPresent());
    }

    @Test
    @Order(4)
    void update() throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        int iLastId;
        try (Connection objConnection = Configuration.getConnection()) {
            Statement sStatement = objConnection.createStatement();
            ResultSet objRs = sStatement.executeQuery("select test_id from test_table order by test_id desc limit 1");
            objRs.next();
            iLastId = objRs.getInt("test_id");
        }
        test.setId(iLastId);
        test.setTestField2((int)(Math.random()*1000));
        Assertions.assertTrue(objHandler.update(test).isPresent());
    }

    @Test
    @Order(5)
    void retrieveByID() throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        TestClass objTest = (TestClass)objHandler.retrieveByID(test.getId(), TestClass.class).get();
        Assertions.assertEquals(test.getId(),objTest.getId());
        Assertions.assertEquals(test.getTestField1(),objTest.getTestField1());
        Assertions.assertEquals(test.getTestField2(),objTest.getTestField2());
    }

    @Test
    @Order(6)
    void retrieveNewest() throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        TestClass objNewest = (TestClass) objHandler.retrieveNewest(Configuration.getMetamodelByClassName(TestClass.class.getName())).get();
        Assertions.assertEquals(test.getId(),objNewest.getId());
        Assertions.assertEquals(test.getTestField1(),objNewest.getTestField1());
        Assertions.assertEquals(test.getTestField2(),objNewest.getTestField2());

    }

    @Test
    @Order(7)
    void delete() throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        int iId = test.getId();
        Assertions.assertTrue(objHandler.delete(test));
        Assertions.assertFalse(objHandler.retrieveByID(iId,TestClass.class).isPresent());
    }

    @Test
    @Order(8)
    void insertThenDeleteWithoutId() throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        test.setTestField2((int)(Math.random()*100000));
        Assertions.assertTrue(objHandler.persistThis(test).isPresent());
        int iId = ((TestClass)objHandler.retrieveNewest(Configuration.getMetamodelByClassName(TestClass.class.getName())).get()).getId();
        test.setId(0);
        Assertions.assertTrue(objHandler.delete(test));
        Assertions.assertFalse(objHandler.retrieveByID(iId,TestClass.class).isPresent());
    }
}
