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

import static com.openexchange.database.migration.internal.LiquibaseHelper.LIQUIBASE_NO_DEFINED_CONTEXT;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
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

            Exception exception = null;
            if (scheduledExecution != null) {
            	Liquibase liquibase = null;
                try {
                	String fileLocation = scheduledExecution.getFileLocation();
					liquibase = LiquibaseHelper.prepareLiquibase(databaseService, fileLocation, scheduledExecution.getResourceAccessor());
                    if (scheduledExecution.isRollback()) {
                        Object target = scheduledExecution.getRollbackTarget();
                        if (target instanceof Integer) {
                            int numberOfChangeSetsToRollback = (Integer) target;
                            LOG.info("Rollback {} numbers of changesets of changelog {}", numberOfChangeSetsToRollback, fileLocation);
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
                } catch (LiquibaseException e) {
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
                    try {
						LiquibaseHelper.cleanUpLiquibase(databaseService, liquibase);
					} catch (OXException e) {
						LOG.error("", e);
					}
                }
            }
        }
    }
    
    public ScheduledExecution scheduleMigration(String fileLocation, ResourceAccessor resourceAccessor) {
    	ScheduledExecution scheduledExecution = new ScheduledExecution(fileLocation, resourceAccessor);
    	schedule(scheduledExecution);
    	return scheduledExecution;
    }
    
    public ScheduledExecution scheduleRollback(String fileLocation, ResourceAccessor resourceAccessor, Object rollbackTarget) {
    	ScheduledExecution scheduledExecution = new ScheduledExecution(fileLocation, resourceAccessor, rollbackTarget);
    	schedule(scheduledExecution);
    	return scheduledExecution;
    }

    private void schedule(ScheduledExecution scheduledExecution) {
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
