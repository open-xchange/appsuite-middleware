package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.TagDatabaseStatement;

public class TagDatabaseChangeTest extends StandardChangeTest {

    private TagDatabaseChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new TagDatabaseChange();
    }

     @Test
    public void getRefactoringName() throws Exception {
        assertEquals("tagDatabase", ChangeFactory.getInstance().getChangeMetaData(refactoring).getName());
    }

     @Test
    public void generateStatement() throws Exception {
        TagDatabaseChange refactoring = new TagDatabaseChange();
        refactoring.setTag("TAG_NAME");

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof TagDatabaseStatement);
        assertEquals("TAG_NAME", ((TagDatabaseStatement) sqlStatements[0]).getTag());
    }

     @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setTag("TAG_NAME");

        assertEquals("Tag 'TAG_NAME' applied to database", refactoring.getConfirmationMessage());
    }   
}