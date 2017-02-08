package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;

public class DropForeignKeyConstraintChangeTest extends StandardChangeTest {
     @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropForeignKeyConstraint", ChangeFactory.getInstance().getChangeMetaData(new DropForeignKeyConstraintChange()).getName());
    }

     @Test
    public void generateStatement() throws Exception {

//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
//                change.setBaseTableSchemaName("SCHEMA_NAME");
//                change.setBaseTableName("TABLE_NAME");
//                change.setConstraintName("FK_NAME");
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                assertEquals(1, sqlStatements.length);
//                assertTrue(sqlStatements[0] instanceof DropForeignKeyConstraintStatement);
//
//                assertEquals("SCHEMA_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getBaseTableSchemaName());
//                assertEquals("TABLE_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getBaseTableName());
//                assertEquals("FK_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getConstraintName());
//            }
//        });
    }

     @Test
    public void getConfirmationMessage() throws Exception {
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setConstraintName("FK_NAME");

        assertEquals("Foreign key FK_NAME dropped", change.getConfirmationMessage());
    }
}
