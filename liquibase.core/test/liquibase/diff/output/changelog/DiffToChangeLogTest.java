package liquibase.diff.output.changelog;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import java.util.List;
import org.junit.Test;
import liquibase.database.core.MySQLDatabase;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.structure.DatabaseObject;

public class DiffToChangeLogTest {

    @Test
    public void getOrderedOutputTypes_isConsistant() throws Exception {
        MySQLDatabase database = new MySQLDatabase();
        DiffToChangeLog obj = new DiffToChangeLog(new DiffResult(new EmptyDatabaseSnapshot(database), new EmptyDatabaseSnapshot(database), new CompareControl()), null);

        for (Class<? extends ChangeGenerator> type : new Class[] {UnexpectedObjectChangeGenerator.class, MissingObjectChangeGenerator.class, ChangedObjectChangeGenerator.class}) {
            List<Class<? extends DatabaseObject>> orderedOutputTypes = obj.getOrderedOutputTypes(type);
            for (int i=0; i<50; i++) {
                assertThat("Error checking "+type.getName(), orderedOutputTypes, contains(obj.getOrderedOutputTypes(type).toArray()));
            }
        }
    }
}
