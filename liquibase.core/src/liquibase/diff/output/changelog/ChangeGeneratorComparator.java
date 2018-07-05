package liquibase.diff.output.changelog;

import java.util.Comparator;
import liquibase.database.Database;
import liquibase.structure.DatabaseObject;

public class ChangeGeneratorComparator implements Comparator<ChangeGenerator> {

    private Class<? extends DatabaseObject> objectType;
    private Database database;

    public ChangeGeneratorComparator(Class<? extends DatabaseObject> objectType, Database database) {
        this.objectType = objectType;
        this.database = database;
    }

    @Override
    public int compare(ChangeGenerator o1, ChangeGenerator o2) {
        int result = -1 * new Integer(o1.getPriority(objectType, database)).compareTo(o2.getPriority(objectType, database));
        if (result == 0) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
        return result;
    }
}
