package liquibase.snapshot;

import java.util.Comparator;
import liquibase.database.Database;
import liquibase.structure.DatabaseObject;

class SnapshotGeneratorComparator implements Comparator<SnapshotGenerator> {

    private Class<? extends DatabaseObject> objectType;
    private Database database;

    public SnapshotGeneratorComparator(Class<? extends DatabaseObject> objectType, Database database) {
        this.objectType = objectType;
        this.database = database;
    }

    @Override
    public int compare(SnapshotGenerator o1, SnapshotGenerator o2) {
        int result = -1 * new Integer(o1.getPriority(objectType, database)).compareTo(o2.getPriority(objectType, database));
        if (result == 0) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
        return result;
    }
}
