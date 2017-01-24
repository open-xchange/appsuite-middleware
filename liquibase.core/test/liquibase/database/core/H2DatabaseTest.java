package liquibase.database.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Assert;
import org.junit.Test;
import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;

public class H2DatabaseTest extends AbstractJdbcDatabaseTest {

    public H2DatabaseTest() throws Exception {
        super(new H2Database());
    }

    @Override
    protected String getProductNameString() {
        return "H2";
    }


     @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

     @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

     @Test
     public void testGetDefaultDriver() {
        Database database = getDatabase();

        assertEquals("org.h2.Driver", database.getDefaultDriver("jdbc:h2:mem:liquibase"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

     @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

     @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("schemaName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }    
}
