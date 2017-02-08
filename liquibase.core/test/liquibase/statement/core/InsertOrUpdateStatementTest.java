package liquibase.statement.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class InsertOrUpdateStatementTest extends InsertStatementTest {

    @Test
    public void setPrimaryKey(){
        String primaryKey = "PRIMARYKEY";
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("CATALOG", "SCHEMA","TABLE", primaryKey);
        assertEquals(primaryKey,statement.getPrimaryKey());
    }

}
