package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropTableStatement;

/**
 * Tests for {@link DropTableChange}
 */
public class DropTableChangeTest extends StandardChangeTest {
    private DropTableChange change;

    @Before
    public void setUp() throws Exception {
        change = new DropTableChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);
    }

     @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropTable", ChangeFactory.getInstance().getChangeMetaData(change).getName());
    }

     @Test
    public void generateStatement() throws Exception {
        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropTableStatement);
        assertEquals("SCHEMA_NAME", ((DropTableStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TAB_NAME", ((DropTableStatement) sqlStatements[0]).getTableName());
        assertTrue(((DropTableStatement) sqlStatements[0]).isCascadeConstraints());
    }

    @Test
    public void generateStatement_nullCascadeConstraints() throws Exception {
        change.setCascadeConstraints(null);
        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertFalse(((DropTableStatement) sqlStatements[0]).isCascadeConstraints());
    }

     @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Table TAB_NAME dropped", change.getConfirmationMessage());
    }
}