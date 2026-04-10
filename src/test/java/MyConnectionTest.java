import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;

import java.sql.Connection;

class MyConnectionTest {

    @Test
    void connectionTest(){
        Connection cnx = MyConnection.getInstance().getCnx();
        Assertions.assertNotNull(cnx);
    }

    @Test
    void singletonTest(){
        MyConnection myc = MyConnection.getInstance();
        MyConnection myc2 = MyConnection.getInstance();
        Assertions.assertSame(myc, myc2, "Singleton not working properly");
    }
}