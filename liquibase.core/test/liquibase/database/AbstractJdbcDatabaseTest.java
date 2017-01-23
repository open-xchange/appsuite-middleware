package liquibase.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import liquibase.structure.core.Table;

/**
 * Base test class for database-specific tests
 */
public abstract class AbstractJdbcDatabaseTest {

    protected AbstractJdbcDatabase database;

    protected AbstractJdbcDatabaseTest(AbstractJdbcDatabase database) throws Exception {
        this.database = database;
    }

    public AbstractJdbcDatabase getDatabase() {
        return database;
    }

    protected abstract String getProductNameString();

    public abstract void supportsInitiallyDeferrableColumns();

    public abstract void getCurrentDateTimeFunction();

    @Test
    public void defaultsWorkWithoutAConnection() {
        database.getDatabaseProductName();
        database.getDefaultCatalogName();
        database.getDefaultSchemaName();
        database.getDefaultPort();
    }
    @Test
    public void isCorrectDatabaseImplementation() throws Exception {
        assertTrue(getDatabase().isCorrectDatabaseImplementation(getMockConnection()));
    }

    protected DatabaseConnection getMockConnection() throws Exception {
        DatabaseConnection conn = Mockito.mock(DatabaseConnection.class);
        Mockito.when(conn.getDatabaseProductName()).thenReturn(getProductNameString());
        conn.setAutoCommit(false);

        return conn;
    }

    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        if (database.supportsCatalogInObjectName(Table.class)) {
            assertEquals("catalogName.schemaName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
        } else {
            assertEquals("schemaName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
        }
    }

//    @Test
//    public void getColumnType_javaTypes() throws SQLException {
//        Database database = getDatabase();
//        DatabaseConnection connection = database.getConnection();
//        if (connection != null) {
//            ((JdbcConnection) connection).getUnderlyingConnection().rollback();
//            assertEquals(database.getDateType().getDataTypeName().toUpperCase(), database.getDataType("java.sql.Types.DATE", false).toUpperCase());
//            assertEquals(database.getBooleanType().getDataTypeName().toUpperCase(), database.getDataType("java.sql.Types.BOOLEAN", false).toUpperCase());
//            assertEquals("VARCHAR(255)", database.getDataType("java.sql.Types.VARCHAR(255)", false).toUpperCase().replaceAll("VARCHAR2", "VARCHAR"));
//        }
//    }
}
