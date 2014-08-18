package com.openexchange.database.migration;

import java.util.List;
import liquibase.change.custom.CustomSqlChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.resource.ResourceAccessor;

/**
 * {@link DBMigrationExecutorService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public interface DBMigrationExecutorService {

    /**
     * @param databaseChangeLog
     */
    public void execute(DatabaseChangeLog databaseChangeLog);

    /**
     * @param databaseChangeLog
     */
    public void execute(String fileName);

    /**
     * Execute database migration based on the given filename. If {@link CustomSqlChange} or {@link CustomTaskChange} are desired to be used
     * add additional {@link ResourceAccessor} via parameter 'additionalAccessors' so that this classes can be found. Provide
     * <code>null</code> in case you are using xml files no additional accessor is required.
     *
     * @param fileName
     * @param additionalAccessors
     */
    public void execute(String fileName, List<ResourceAccessor> additionalAccessors);
}
