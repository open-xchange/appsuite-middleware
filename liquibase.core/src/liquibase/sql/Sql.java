package liquibase.sql;

import java.util.Collection;
import liquibase.structure.DatabaseObject;

public interface Sql {
    public String toSql();

    String getEndDelimiter();

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();

}
