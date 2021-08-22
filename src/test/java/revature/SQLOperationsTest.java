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
    private final Configuration cfg;
    private static final TestClass test = new TestClass("test", (int)(Math.random()*100));;
    private SQLOperationHandler objHandler;
    {
        String strPassword = "project0";
        String strUsername = "postgres";
        String strDBUrl = "jdbc:postgresql://database-1.crgijayqoqqj.us-east-2.rds.amazonaws.com:5432/postgres?currentSchema=p1_admin";
        cfg = new Configuration(strDBUrl, strUsername, strPassword);
        cfg.addAnnotatedClass(TestClass.class);
    }

    @BeforeEach
    void setup() throws SQLException {
        objHandler = new SQLOperationHandler(cfg);
    }

    @Test
    @Order(1)
    void queryRowByID() {
        Assertions.assertEquals("select * from test_table where test_id=1 limit 1", DQLEngine.selectRowByID(TestClass.class,1));
    }

    @Test
    @Order(2)
    void getMetamodelByClassName() {
        //Assertions.assertEquals(Configuration.getMetamodelByClassName(TestClass.class.getName()))
    }

    @Test
    @Order(3)
    void insertRow() {
        Assertions.assertTrue(objHandler.persistThis(test));
    }

    @Test
    @Order(4)
    void update() throws SQLException, InvocationTargetException, IllegalAccessException {
        int iLastId;
        try (Connection objConnection = cfg.getConnection()) {
            Statement sStatement = objConnection.createStatement();
            ResultSet objRs = sStatement.executeQuery("select test_id from test_table order by test_id desc limit 1");
            objRs.next();
            iLastId = objRs.getInt("test_id");
        }
        test.setId(iLastId);
        test.setTestField2((int)(Math.random()*1000));
        Assertions.assertTrue(objHandler.update(test));
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
    void delete() throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        int iId = test.getId();
        Assertions.assertTrue(objHandler.delete(test));
        Assertions.assertFalse(objHandler.retrieveByID(iId,TestClass.class).isPresent());
    }
}
