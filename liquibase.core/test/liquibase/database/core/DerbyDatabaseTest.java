package liquibase.database.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import liquibase.database.Database;

public class DerbyDatabaseTest {

     @Test
     public void testGetDefaultDriver() {
        Database database = new DerbyDatabase();

        assertEquals("org.apache.derby.jdbc.EmbeddedDriver", database.getDefaultDriver("java:derby:liquibase;create=true"));

        assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
    }

     @Test
     public void testGetDateLiteral() {
        assertEquals("TIMESTAMP('2008-01-25 13:57:41')", new DerbyDatabase().getDateLiteral("2008-01-25 13:57:41"));
        assertEquals("TIMESTAMP('2008-01-25 13:57:41.300000')", new DerbyDatabase().getDateLiteral("2008-01-25 13:57:41.3"));
        assertEquals("TIMESTAMP('2008-01-25 13:57:41.340000')", new DerbyDatabase().getDateLiteral("2008-01-25 13:57:41.34"));
        assertEquals("TIMESTAMP('2008-01-25 13:57:41.347000')", new DerbyDatabase().getDateLiteral("2008-01-25 13:57:41.347"));
    }

}
