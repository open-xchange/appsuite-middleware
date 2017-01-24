package liquibase.database.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Assert;
import org.junit.Test;
import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;

/**
 * Tests for {@link MSSQLDatabase}
 */
public class MSSQLDatabaseTest extends AbstractJdbcDatabaseTest {

    public MSSQLDatabaseTest() throws Exception {
        super(new MSSQLDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "Microsoft SQL Server";
    }


     @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

     @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void getDefaultDriver() {
        Database database = new MSSQLDatabase();

        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", database.getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase"));

        assertNull(database.getDefaultDriver("jdbc:oracle:thin://localhost;databaseName=liquibase"));
    }

     @Test
    public void escapeTableName_noSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[tableName]", database.escapeTableName(null, null, "tableName"));
    }

     @Test
    public void escapeTableName_withSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[schemaName].[tableName]", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Test
    public void changeDefaultSchema() throws DatabaseException {
        Database database = new MSSQLDatabase();
        assertNull(database.getDefaultSchemaName());

        database.setDefaultSchemaName("myschema");
        assertEquals("myschema", database.getDefaultSchemaName());
    }
}
