package liquibase.serializer;

import static org.junit.Assert.assertEquals;
import liquibase.sql.visitor.PrependSqlVisitor;
import org.junit.Test;


public class ReflectionSerializerTest {

    @Test
    public void getValue() {
        PrependSqlVisitor visitor = new PrependSqlVisitor();
        visitor.setValue("ValHere");
        visitor.setApplyToRollback(true);

        assertEquals("ValHere", ReflectionSerializer.getInstance().getValue(visitor, "value"));
        assertEquals(true, ReflectionSerializer.getInstance().getValue(visitor, "applyToRollback"));
    }
}
