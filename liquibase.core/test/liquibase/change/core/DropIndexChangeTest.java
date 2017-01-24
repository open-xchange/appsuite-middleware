package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;

/**
 * Tests for {@link liquibase.change.core.DropIndexChange}
 */
public class DropIndexChangeTest extends StandardChangeTest {

     @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropIndex", ChangeFactory.getInstance().getChangeMetaData(new DropIndexChange()).getName());
    }

     @Test
    public void generateStatement() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setSchemaName("SCHEMA_NAME");

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropIndexStatement);
        assertEquals("SCHEMA_NAME", ((DropIndexStatement) sqlStatements[0]).getTableSchemaName());
        assertEquals("TABLE_NAME", ((DropIndexStatement) sqlStatements[0]).getTableName());
        assertEquals("IDX_NAME", ((DropIndexStatement) sqlStatements[0]).getIndexName());
    }

     @Test
    public void getConfirmationMessage() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        assertEquals("Index IDX_NAME dropped from table TABLE_NAME", refactoring.getConfirmationMessage());
    }
}