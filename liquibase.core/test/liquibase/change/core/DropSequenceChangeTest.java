package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;

/**
 * Tests for {@link liquibase.change.core.DropSequenceChange}
 */
public class DropSequenceChangeTest extends StandardChangeTest {

    private DropSequenceChange change;

    @Before
    public void setUp() throws Exception {
        change = new DropSequenceChange();
        change.setSequenceName("SEQ_NAME");
    }

     @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropSequence", ChangeFactory.getInstance().getChangeMetaData(new DropSequenceChange()).getName());
    }

     @Test
    public void generateStatement() throws Exception {

//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                if (!database.supportsSequences()) {
//                    return;
//                }
//
//                DropSequenceChange change = new DropSequenceChange();
//                change.setSchemaName("SCHEMA_NAME");
//                change.setSequenceName("SEQ_NAME");
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                assertEquals(1, sqlStatements.length);
//                assertTrue(sqlStatements[0] instanceof DropSequenceStatement);
//
//                assertEquals("SCHEMA_NAME", ((DropSequenceStatement) sqlStatements[0]).getSchemaName());
//                assertEquals("SEQ_NAME", ((DropSequenceStatement) sqlStatements[0]).getSequenceName());
//
//            }
//        });
    }

     @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Sequence SEQ_NAME dropped", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return !database.supportsSequences();
    }

}