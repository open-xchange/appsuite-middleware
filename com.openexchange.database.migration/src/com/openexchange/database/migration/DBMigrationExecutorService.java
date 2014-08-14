package com.openexchange.database.migration;

import liquibase.changelog.DatabaseChangeLog;

/**
 * {@link DBMigrationExecutorService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public interface DBMigrationExecutorService {

    /**
     * Provide DatabaseChangeLog files to execute
     *
     * @param databaseChangeLog
     */
    public void register(DatabaseChangeLog databaseChangeLog);

    /**
     * @param databaseChangeLog
     */
    public void execute(DatabaseChangeLog databaseChangeLog);

    /**
     * @param databaseChangeLog
     */
    public void execute(String fileName);
}
