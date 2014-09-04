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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.sql.Connection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import liquibase.Liquibase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;


/**
 * {@link DBMigrationExecutor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class DBMigrationExecutor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DBMigrationExecutor.class);

    private final DatabaseService databaseService;

    private final Queue<ScheduledExecution> scheduledExecutions;

    private final Lock lock = new ReentrantLock();

    private Thread thread;

    public DBMigrationExecutor(DatabaseService databaseService) {
        super();
        this.databaseService = databaseService;
        scheduledExecutions = new LinkedList<ScheduledExecution>();
    }

    @Override
    public void run() {
        boolean running = true;
        ScheduledExecution scheduledExecution = null;
        while (running) {
            lock.lock();
            try {
                scheduledExecution = scheduledExecutions.poll();
                if (scheduledExecution == null) {
                    thread = null;
                    running = false;
                }
            } finally {
                lock.unlock();
            }

            if (scheduledExecution != null) {
                Liquibase liquibase = null;
                Connection connection = null;
                try {
                    connection = databaseService.getWritable(); // TODO: without timeout
                    MySQLDatabase database = new MySQLDatabase();
                    database.setConnection(new JdbcConnection(connection));
                    liquibase = new Liquibase(
                        scheduledExecution.getFileLocation(),
                        scheduledExecution.getResourceAccessor(),
                        database);

                    liquibase.update("");
                } catch (LiquibaseException e) {
                    // TODO
                    LOG.error("", e);
                } catch (OXException e) {
                    LOG.error("", e);
                } catch (Exception e) {
                    LOG.error("", e);
                } finally {
                    scheduledExecution.setExecuted();

                    if (liquibase != null) {
                        try {
                            liquibase.forceReleaseLocks();
                        } catch (LiquibaseException e) {
                            // TODO Auto-generated catch block
                            LOG.error("", e);
                        }
                    }

                    if (connection != null) {
                        Databases.autocommit(connection);
                        databaseService.backWritable(connection);
                    }
                }
            }
        }
    }

    public void schedule(ScheduledExecution scheduledExecution) {
        lock.lock();
        try {
            scheduledExecutions.add(scheduledExecution);
            if (thread == null) {
                thread = new Thread(this);
                thread.start();
            }
        } finally {
            lock.unlock();
        }
    }

}
