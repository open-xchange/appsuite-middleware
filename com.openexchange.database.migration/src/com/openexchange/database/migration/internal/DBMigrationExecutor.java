/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.database.migration.internal;

import static com.openexchange.database.migration.internal.LiquibaseHelper.LIQUIBASE_NO_DEFINED_CONTEXT;
import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationCallback;
import com.openexchange.database.migration.DBMigrationConnectionProvider;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.exception.OXException;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.exception.ValidationFailedException;

/**
 * {@link DBMigrationExecutor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class DBMigrationExecutor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DBMigrationExecutor.class);

    private final Queue<ScheduledExecution> scheduledExecutions;
    private final Lock lock = new ReentrantLock();
    private volatile Thread thread;

    /**
     * Initializes a new {@link DBMigrationExecutor}.
     */
    public DBMigrationExecutor() {
        super();
        scheduledExecutions = new LinkedList<ScheduledExecution>();
    }

    /**
     * Gets a value indicating whether migrations are currently executed or not.
     *
     * @return <code>true</code> if the executor is currently active, <code>false</code>, otherwise
     */
    public boolean isActive() {
        return null != thread;
    }

    private boolean checkIfUpdateIsNeeded(ScheduledExecution scheduledExecution, Connection connection) {
        try {
            if (needsUpdate(scheduledExecution, connection)) {
                return true;
            }

            LOG.info("No unrun liquibase changesets detected, skipping migration {}.", scheduledExecution.getMigration());
            scheduledExecution.setDone(null);
            notify(scheduledExecution.getCallback(), Collections.<ChangeSet>emptyList(), Collections.<ChangeSet>emptyList());
            return false;
        } catch (ValidationFailedException validationFailedException) {
            LOG.error("MD5Sum validation failed. No more ChangeSets from file {} will be executed!", scheduledExecution.getMigration().getFileLocation(), validationFailedException);
            return false;
        }
    }

    @Override
    public void run() {
        for (ScheduledExecution scheduledExecution; (scheduledExecution = nextExecution()) != null;) {
            DBMigrationConnectionProvider connectionProvider = scheduledExecution.getConnectionProvider();
            Connection connection = null;
            Connection lifeThreadConnection = null;
            try {
                // Acquire connection
                connection = connectionProvider.get();
                lifeThreadConnection = connectionProvider.get();

                // Check if update is needed
                if (checkIfUpdateIsNeeded(scheduledExecution, connection)) {
                    // Execute update
                    Exception exception = null;
                    Liquibase liquibase = null;
                    DBMigrationListener migrationListener = new DBMigrationListener();
                    String fileLocation = scheduledExecution.getFileLocation();
                    try {
                        liquibase = LiquibaseHelper.prepareLiquibase(connection, lifeThreadConnection, scheduledExecution.getMigration());
                        liquibase.setChangeExecListener(migrationListener);
                        DBMigrationMonitor.getInstance().addFile(fileLocation);
                        if (scheduledExecution.isRollback()) {
                            Object target = scheduledExecution.getRollbackTarget();
                            if (target instanceof Integer) {
                                int numberOfChangeSetsToRollback = ((Integer) target).intValue();
                                LOG.info("Rollback {} numbers of changesets of changelog {}", Integer.valueOf(numberOfChangeSetsToRollback), fileLocation);
                                liquibase.rollback(numberOfChangeSetsToRollback, LIQUIBASE_NO_DEFINED_CONTEXT);
                            } else if (target instanceof String) {
                                String changeSetTag = (String) target;
                                LOG.info("Rollback to changeset {} of changelog {}", changeSetTag, fileLocation);
                                liquibase.rollback(changeSetTag, LIQUIBASE_NO_DEFINED_CONTEXT);
                            } else {
                                throw DBMigrationExceptionCodes.WRONG_TYPE_OF_DATA_ROLLBACK_ERROR.create();
                            }
                        } else {
                            LOG.info("Running migrations of changelog {}", fileLocation);
                            liquibase.update(LIQUIBASE_NO_DEFINED_CONTEXT);
                        }
                    } catch (liquibase.exception.ValidationFailedException e) {
                        exception = e;
                        LOG.error("MD5Sum validation failed. No more ChangeSets will be executed!", e);
                    } catch (liquibase.exception.LiquibaseException e) {
                        exception = e;
                        LOG.error("", e);
                    } catch (OXException e) {
                        exception = e;
                        LOG.error("", e);
                    } catch (Exception e) {
                        exception = e;
                        LOG.error("", e);
                    } finally {
                        scheduledExecution.setDone(exception);
                        if (null != liquibase) {
                            try {
                                LiquibaseHelper.cleanUpLiquibase(liquibase);
                            } catch (Exception e) {
                                LOG.error("", e);
                            }
                        }
                        if (null != connection) {
                            connectionProvider.back(connection);
                            connection = null;
                        }
                        if (null != lifeThreadConnection) {
                            connectionProvider.back(lifeThreadConnection);
                            lifeThreadConnection = null;
                        }
                        DBMigrationMonitor.getInstance().removeFile(fileLocation);
                    }

                    // Notify...
                    notify(scheduledExecution.getCallback(), migrationListener.getExecuted(), migrationListener.getRolledBack());
                }
            } catch (OXException e) {
                LOG.warn("Error acquiring connection.", e);
            } finally {
                if (null != connection) {
                    connectionProvider.backAfterReading(connection);
                }
                if (null != lifeThreadConnection) {
                    connectionProvider.backAfterReading(lifeThreadConnection);
                }
            }
        }
    }

    /**
     * Schedules a database migration.
     *
     * @param migration The database migration
     * @param callback A migration callback to get notified on completion, or <code>null</code> if not set
     * @return The scheduled migration
     */
    public ScheduledExecution scheduleMigration(DBMigration migration, DBMigrationCallback callback) {
        return schedule(new ScheduledExecution(migration, callback));
    }

    /**
     * Schedules a database migration rollback.
     *
     * @param migration The database migration
     * @param callback A migration callback to get notified on completion, or <code>null</code> if not set
     * @param rollbackTarget The rollback target
     */
    public ScheduledExecution scheduleRollback(DBMigration migration, DBMigrationCallback callback, Object rollbackTarget) {
        return schedule(new ScheduledExecution(migration, callback, rollbackTarget));
    }

    private ScheduledExecution schedule(ScheduledExecution scheduledExecution) {
        lock.lock();
        try {
            scheduledExecutions.add(scheduledExecution);
            if (this.thread == null) {
                Thread thread = new Thread(this);
                this.thread = thread;
                thread.start();
            }
        } finally {
            lock.unlock();
        }
        return scheduledExecution;
    }

    private ScheduledExecution nextExecution() {
        lock.lock();
        try {
            ScheduledExecution scheduledExecution = scheduledExecutions.poll();
            if (scheduledExecution == null) {
                this.thread = null;
            }
            return scheduledExecution;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Performs an unsynchronized check to determine if there are any unrun changesets in the scheduled execution or not.
     *
     * @param scheduledExecution The scheduled execution
     * @param connection The connection to use
     * @return <code>true</code> if unrun changesets are signaled by liquibase or if the unrun changesets can't be evaluated, <code>false</code>, otherwise
     * @throws ValidationFailedException
     */
    private static boolean needsUpdate(ScheduledExecution scheduledExecution, Connection connection) throws ValidationFailedException {
        Liquibase liquibase = null;
        boolean releaseLocks = true;
        try {
            liquibase = LiquibaseHelper.prepareLiquibase(connection, null, scheduledExecution.getMigration());
            if (false == scheduledExecution.isRollback()) {
                List<ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets(LIQUIBASE_NO_DEFINED_CONTEXT);
                releaseLocks = false;
                return null != unrunChangeSets && 0 < unrunChangeSets.size();
            }
        } catch (liquibase.exception.ValidationFailedException e) {
            throw e;
        } catch (Exception e) {
            LOG.warn("Error determining if liquibase update is required, assuming yes.", e);
        } finally {
            try {
                LiquibaseHelper.cleanUpLiquibase(liquibase, releaseLocks);
            } catch (Exception e) {
                LOG.warn("", e);
            }
        }
        return true;
    }

    /**
     * Notifies a migration callback about a finished execution.
     *
     * @param callback The migration callback, or <code>null</code> if not set
     * @param executed A list of changesets that have been executed during the migration, or an empty list if there are none
     * @param rolledBack A list of changesets that have been rolled back during the migration, or an empty list if there are none
     */
    private static void notify(DBMigrationCallback callback, List<ChangeSet> executed, List<ChangeSet> rolledBack) {
        if (null != callback) {
            callback.onMigrationFinished(executed, rolledBack);
        }
    }

}
