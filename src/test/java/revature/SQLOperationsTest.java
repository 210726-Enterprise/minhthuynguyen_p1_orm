package revature;

import com.revature.p1.orm.persistence.DQLEngine;
import com.revature.p1.orm.persistence.SQLOperationHandler;
import com.revature.p1.orm.util.Configuration;
import org.junit.jupiter.api.*;
import revature.model.TestClass;
import revature.model.User;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SQLOperationsTest {
    private final String strDBUrl = "jdbc:postgresql://database-1.crgijayqoqqj.us-east-2.rds.amazonaws.com:5432/postgres?currentSchema=p1_admin",
            strUsername = "postgres",
            strPassword = "project0";
    private Configuration cfg;


    @BeforeEach
    void setUp(){
        cfg = new Configuration(strDBUrl, strUsername, strPassword);
        cfg.addAnnotatedClass(User.class)
                .addAnnotatedClass(TestClass.class);
    }

    @Test
    @Order(1)
    void queryRowByID() {
        Assertions.assertEquals("select * from test_table where test_id=1 limit 1", DQLEngine.queryRowByID(TestClass.class,1));
    }

    @Test
    @Order(2)
    void getMetamodelByClassName() {
        //Assertions.assertEquals(Configuration.getMetamodelByClassName(TestClass.class.getName()))
    }

    @Test
    @Order(10)
    void retrieveByID() throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        SQLOperationHandler objTest = new SQLOperationHandler(cfg);
        Assertions.assertEquals(1,((TestClass)objTest.retrieveByID(1,TestClass.class).get()).getId());
    }
}
