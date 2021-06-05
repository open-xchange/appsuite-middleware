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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationCallback;
import com.openexchange.database.migration.DBMigrationConnectionProvider;
import com.openexchange.database.migration.DBMigrationState;
import liquibase.resource.ResourceAccessor;


/**
 * {@link ScheduledExecution}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ScheduledExecution implements DBMigrationState {

    private final Lock lock = new ReentrantLock();
    private final Condition wasExecuted = lock.newCondition();
    private final Object rollbackTarget;
    private final DBMigration migration;
    private final DBMigrationCallback callback;

    private ExecutionException exception = null;
    private boolean done = false;

    /**
     * Initializes a new {@link ScheduledExecution}.
     *
     * @param callback A migration callback to get notified on completion, or <code>null</code> if not set
     * @param migration The database migration
     */
    public ScheduledExecution(DBMigration migration, DBMigrationCallback callback) {
        this(migration, callback, null);
    }

    /**
     * Initializes a new {@link ScheduledExecution}.
     *
     * @param migration The database migration
     * @param callback A migration callback to get notified on completion, or <code>null</code> if not set
     * @param rollbackTarget The rollback target
     */
    public ScheduledExecution(DBMigration migration, DBMigrationCallback callback, Object rollbackTarget) {
        super();
        this.migration = migration;
        this.callback = callback;
        this.rollbackTarget = rollbackTarget;
    }

    /**
     * Gets the migration callback.
     *
     * @return The migration callback, or <code>null</code> if not set
     */
    DBMigrationCallback getCallback() {
        return callback;
    }

    /**
     * Gets the database connection provider.
     *
     * @return The database connection provider
     */
    DBMigrationConnectionProvider getConnectionProvider() {
        return migration.getConnectionProvider();
    }

    String getFileLocation() {
        return migration.getFileLocation();
    }

    ResourceAccessor getResourceAccessor() {
        return migration.getAccessor();
    }

    boolean isRollback() {
        return rollbackTarget != null;
    }

    Object getRollbackTarget() {
        return rollbackTarget;
    }

    void setDone(Throwable t) {
        lock.lock();
        try {
            done = true;
            if (t != null) {
                exception = new ExecutionException(t);
            }

            wasExecuted.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isDone() {
        lock.lock();
        try {
            return done;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void awaitCompletion() throws ExecutionException, InterruptedException {
        while (!done) {
            lock.lock();
            try {
                wasExecuted.await();
                if (exception != null) {
                    throw exception;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public DBMigration getMigration() {
        return migration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFileLocation() == null) ? 0 : getFileLocation().hashCode());
        result = prime * result + ((getResourceAccessor() == null) ? 0 : getResourceAccessor().hashCode());
        result = prime * result + ((rollbackTarget == null) ? 0 : rollbackTarget.hashCode());
        result = prime * result + ((wasExecuted == null) ? 0 : wasExecuted.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ScheduledExecution other = (ScheduledExecution) obj;
        if (getFileLocation() == null) {
            if (other.getFileLocation() != null) {
                return false;
            }
        } else if (!getFileLocation().equals(other.getFileLocation())) {
            return false;
        }
        if (getResourceAccessor() == null) {
            if (other.getResourceAccessor() != null) {
                return false;
            }
        } else if (!getResourceAccessor().equals(other.getResourceAccessor())) {
            return false;
        }
        if (rollbackTarget == null) {
            if (other.rollbackTarget != null) {
                return false;
            }
        } else if (!rollbackTarget.equals(other.rollbackTarget)) {
            return false;
        }
        if (wasExecuted == null) {
            if (other.wasExecuted != null) {
                return false;
            }
        } else if (!wasExecuted.equals(other.wasExecuted)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScheduledExecution [fileLocation=" + getFileLocation() + ", resourceAccessor=" + getResourceAccessor() + ", rollbackTarget=" + rollbackTarget + ", done=" + done + "]";
    }

}
