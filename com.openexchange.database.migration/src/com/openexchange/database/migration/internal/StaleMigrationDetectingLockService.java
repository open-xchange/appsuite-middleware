/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.database.migration.internal;

import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.migration.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceImpl;
import liquibase.logging.LogFactory;

/**
 * {@link StaleMigrationDetectingLockService} is a {@link LockService} implementation which is able to identify stale migrations and release locks accordingly.
 *
 * It works by creating a life thread which updates the timestamp as long the migration is in progress.
 * Other middleware nodes can then check whether this timestamp hasn't been updated for a certain amount of time and release the lock if not.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v8.0.0
 */
public class StaleMigrationDetectingLockService extends LockServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(StaleMigrationDetectingLockService.class);

    private static final String UPDATE_SQL = "UPDATE DATABASECHANGELOGLOCK SET LOCKGRANTED=? WHERE ID=1";
    private static final String CHECK_SQL = "SELECT LOCKGRANTED FROM DATABASECHANGELOGLOCK WHERE ID=1";

    private long waitForLockTimeoutTimeNanos = TimeUnit.SECONDS.toNanos(10);

    private ScheduledTimerTask timerTask;
    private Database db;

    /**
     * Initializes a new {@link StaleMigrationDetectingLockService}.
     *
     */
    public StaleMigrationDetectingLockService() {
        super();
    }

    @Override
    public synchronized void waitForLock() throws LockException {

        boolean locked = false;
        while (!locked) {
            locked = acquireLock();
            if (!locked) {
                try {
                    if (checkSchemaLockIsStale()) {
                        // liquibase migration is stale
                        LOG.info("Liquibase migration is stale. Releasing lock...");
                        forceReleaseLock();
                        locked = acquireLock();
                        if (locked) {
                            break;
                        }
                    }
                } catch (LockException | DatabaseException | OXException e) {
                    LOG.warn("Unable to peform stale check", e);
                }

                LogFactory.getLogger().info("Waiting for changelog lock....");
                LockSupport.parkNanos(waitForLockTimeoutTimeNanos);
            }
        }

        LOG.trace("Liquibase lock acquired. Starting life thread...");
        startLifeThread();
    }

    /**
     * Starts a life thread which schedules a repeating task which updates the timestamp of the liquibase lock
     *
     * @throws LockException in case the thread couldn't be started
     */
    private void startLifeThread() throws LockException {
        try {
            long refreshIntervalMillis = getRefreshIntervalMillis();
            if (refreshIntervalMillis > 0) {
                TimerService timerService = Services.getService(TimerService.class);
                Runnable task = () -> {
                    try {
                        if (tryRefreshSchemaLock()) {
                            LOG.info("Refreshed lock for schema {}", db.getDefaultSchemaName());
                        }
                    } catch (Exception e) {
                        LOG.warn("Failed to refresh lock for schema {}", db.getDefaultSchemaName(), e);
                    }
                };
                timerTask = timerService.scheduleWithFixedDelay(task, refreshIntervalMillis, refreshIntervalMillis, TimeUnit.MILLISECONDS);
            }
        } catch (OXException e) {
            LOG.error("Unable to start life thread for liquibase migration", e);
            throw new LockException("Unable to start life thread for liquibase migration: " + e.getMessage());
        }
    }

    @Override
    public synchronized void releaseLock() throws LockException {
        try {
            stopLifeThread();
            super.releaseLock();
        } catch (LockException e) {
            // restart life thread in case releasing the lock fails
            try {
                startLifeThread();
            } catch (LockException e2) {
                LOG.error("Unable to restart life thread after error", e2);
            }
            throw e;
        } catch (OXException e) {
            LOG.error("Unable to stop life thread for liquibase", e);
            throw new LockException("Unable to stop life thread for liquibase: " + e.getMessage());
        }
    }

    /**
     * Stops the life thread task
     *
     * @throws OXException
     */
    private void stopLifeThread() throws OXException {
        if (timerTask != null) {
            timerTask.cancel();
            Services.getService(TimerService.class).purge();
            timerTask = null;
        }
    }

    @Override
    public synchronized void setDatabase(Database database) {
        super.setDatabase(database);
        this.db = database;
    }

    /**
     * Refreshes the timestamp of the liquibase lock
     *
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    private boolean tryRefreshSchemaLock() {
        Connection con = ((LifeThreadConnectionAwareMysqlDatabase) db).getLifeThreadConnection();
        try (PreparedStatement stmt = con.prepareStatement(UPDATE_SQL)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            int rows = stmt.executeUpdate();
            return rows != 1 ? false : true;
        } catch (SQLException e) {
            LOG.warn("Unable to refresh liquibase lock", e);
            return false;
        }
    }

    /**
     * Checks if a liquibase migration is stale
     *
     * @return <code>true</code> if a liquibase migration is stale, <code>false</code> otherwise
     * @throws OXException
     */
    private boolean checkSchemaLockIsStale() throws OXException {
        Connection con = ((LifeThreadConnectionAwareMysqlDatabase) db).getLifeThreadConnection();
        try (PreparedStatement stmt = con.prepareStatement(CHECK_SQL)) {
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next() == false) {
                LOG.warn("Unable to check whether schema lock is stale or not.");
                return false;
            }
            Timestamp timestamp = resultSet.getTimestamp("LOCKGRANTED");
            return getMaxIdleMillis() < (System.currentTimeMillis() - timestamp.getTime());
        } catch (SQLException e) {
            LOG.warn("Unable to refresh liquibase lock", e);
            return false;
        }
    }

    /**
     * Gets the fresh interval for the life thread in milliseconds
     *
     * @return The fresh interval in milliseconds
     * @throws OXException
     */
    private long getRefreshIntervalMillis() throws OXException {
        return Services.getService(LeanConfigurationService.class).getLongProperty(LiquibaseProperties.REFRESH_INTERVAL_MILLIS);
    }

    /**
     * Gets the maximum time in milliseconds a liquibase migration is considered still running
     *
     * @return The maximum time idle
     * @throws OXException
     */
    private long getMaxIdleMillis() throws OXException {
        return Services.getService(LeanConfigurationService.class).getLongProperty(LiquibaseProperties.MAX_IDLE_MILLIS);
    }

    @Override
    public synchronized void setChangeLogLockRecheckTime(long changeLogLocRecheckTimeMillis) {
        waitForLockTimeoutTimeNanos = TimeUnit.MILLISECONDS.toNanos(changeLogLocRecheckTimeMillis);
    }

}
