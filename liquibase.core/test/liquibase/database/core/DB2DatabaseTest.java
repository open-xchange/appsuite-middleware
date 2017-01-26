package liquibase.database.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import liquibase.database.Database;

public class DB2DatabaseTest {

     @Test
     public void testGetDefaultDriver() {
        Database database = new DB2Database();

        assertEquals("com.ibm.db2.jcc.DB2Driver", database.getDefaultDriver("jdbc:db2://localhost:50000/liquibas"));

        assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
    }


}
