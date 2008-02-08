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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.Collections;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class implements the new iterator for tasks that fixes problems with
 * connection timeouts if a lot of tasks are read and that improved performance
 * while reading from database.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TaskIterator implements SearchIterator<Task>, Runnable {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TaskIterator.class);

    /**
     * Context.
     */
    private final Context ctx;

    /**
     * unique identifier of the user for reminders.
     */
    private final int userId;

    /**
     * This ResultSet contains the data for this iterator.
     */
    private final ResultSet result;

    /**
     * Folder through that the objects are requested.
     */
    private final int folderId;

    /**
     * Wanted fields of the task.
     */
    private final int[] taskAttributes;

    /**
     * Additional fields that cannot be read from the {@link ResultSet}.
     */
    private final int[] additionalAttributes;

    /**
     * ACTIVE or DELETED.
     */
    private StorageType type;

    /**
     * Pre read tasks.
     */
    private final PreRead<Task> preread = new PreRead<Task>();

    /**
     * Finished read tasks.
     */
    private final Queue<Task> ready = new LinkedList<Task>();

    /**
     * Field for the thread reading the {@link ResultSet}.
     */
    private final Thread runner;

    /**
     * For reading participants.
     */
    private final ParticipantStorage partStor = ParticipantStorage
        .getInstance();

    /**
     * Default constructor.
     * @param ctx Context.
     * @param userId unique identifier of the user if we have to load reminders.
     * @param result This iterator iterates over this ResultSet.
     * @param folderId Unique identifier of the folder through that the tasks
     * are requested.
     * @param attributes Array of fields that the returned tasks should contain.
     * @param type ACTIVE or DELETED.
     */
    TaskIterator(final Context ctx, final int userId, final ResultSet result,
        final int folderId, final int[] attributes, final StorageType type) {
        super();
        this.ctx = ctx;
        this.userId = userId;
        this.result = result;
        this.folderId = folderId;
        final List<Integer> tmp1 = new ArrayList<Integer>(attributes.length);
        final List<Integer> tmp2 = new ArrayList<Integer>(attributes.length);
        for (int column : attributes) {
            if (null == Mapping.getMapping(column)
                && Task.FOLDER_ID != column) {
                tmp2.add(Integer.valueOf(column));
            } else {
                tmp1.add(Integer.valueOf(column));
            }
        }
        this.taskAttributes = Collections.toArray(tmp1);
        modifyAdditionalAttributes(tmp2);
        this.additionalAttributes = Collections.toArray(tmp2);
        this.type = type;
        runner = new Thread(this);
        runner.start();
    }

    private void modifyAdditionalAttributes(final List<Integer> additional) {
        // If participants are requested we also add users automatically. Users
        // are calculated from participants.
        if (additional.contains(Integer.valueOf(Task.USERS))) {
            if (!additional.contains(Integer.valueOf(Task.PARTICIPANTS))) {
                additional.add(Integer.valueOf(Task.PARTICIPANTS));
            }
            // Not removed users will give SearchIteratorException in code
            // below.
            additional.remove(Integer.valueOf(Task.USERS));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws SearchIteratorException {
        try {
            runner.join();
        } catch (InterruptedException e) {
            throw new SearchIteratorException(new TaskException(TaskException
                .Code.THREAD_ISSUE, e));
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return !ready.isEmpty() || preread.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSize() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Task next() throws SearchIteratorException, OXException {
        if (ready.isEmpty()) {
            try {
                final Map<Integer, Task> tasks = new HashMap<Integer, Task>();
                for (Task task : preread.take(
                    additionalAttributes.length > 0)) {
                    tasks.put(Integer.valueOf(task.getObjectID()), task);
                }
                for (int attribute : additionalAttributes) {
                    switch (attribute) {
                    case Task.PARTICIPANTS:
                        try {
                            readParticipants(tasks);
                        } catch (TaskException e) {
                            throw new SearchIteratorException(e);
                        }
                        break;
                    case Task.ALARM:
                        try {
                            Reminder.loadReminder(ctx, userId, tasks.values());
                        } catch (TaskException e) {
                            throw new SearchIteratorException(e);
                        }
                        break;
                    default:
                        throw new SearchIteratorException(new TaskException(
                            TaskException.Code.UNKNOWN_ATTRIBUTE, Integer
                            .valueOf(attribute)));
                    }
                }
                ready.addAll(tasks.values());
            } catch (InterruptedException e) {
                throw new SearchIteratorException(new TaskException(
                    TaskException.Code.THREAD_ISSUE, e));
            }
        }
        return ready.poll();
    }

    /**
     * Reads the participants for the tasks.
     * @param tasks Tasks that should be filled with participants.
     * @throws TaskException if reading the participants fails.
     */
    private void readParticipants(final Map<Integer, Task> tasks)
        throws TaskException {
        final Map<Integer, Set<TaskParticipant>> parts = partStor
            .selectParticipants(ctx, Collections.toArray(tasks.keySet()), type);
        for (Entry<Integer, Set<TaskParticipant>> entry : parts.entrySet()) {
            final Task task = tasks.get(entry.getKey());
            final Set<TaskParticipant> participants = entry.getValue();
            task.setParticipants(TaskLogic.createParticipants(participants));
            task.setUsers(TaskLogic.createUserParticipants(participants));
        }
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        try {
            while (result.next()) {
                Task task = new Task();
                int pos = 1;
                for (int taskField : taskAttributes) {
                    final Mapper<?> mapper = Mapping.getMapping(taskField);
                    if (Task.FOLDER_ID == taskField) {
                        if (-1 == folderId) {
                            task.setParentFolderID(result.getInt(pos++));
                        } else {
                            task.setParentFolderID(folderId);
                        }
                    } else if (null != mapper) {
                        mapper.fromDB(result, pos++, task);
                    }
                }
                preread.offer(task);
            }
            preread.finished();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        Connection con = null;
        Statement stmt = null;
        try {
            stmt = result.getStatement();
            con = stmt.getConnection();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        DBUtils.closeSQLStuff(result, stmt);
        if (null != con) {
            DBPool.closeReaderSilent(ctx, con);
            con = null;
        }
    }

    /**
     * Implements the queue of preread tasks.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    private static class PreRead<T> {

        /**
         * Logger.
         */
        private static final Log LOG = LogFactory.getLog(PreRead.class);

        /**
         * What is the minimum count of tasks for additional sub requests.
         */
        private static final int MINIMUM_PREREAD = 10;

        /**
         * Contains the tasks read by the thread.
         */
        private final Queue<T> elements = new LinkedList<T>();

        /**
         * Lock for the condition.
         */
        private final Lock lock = new ReentrantLock();

        /**
         * Condition for waiting for enough elements.
         */
        private final Condition waitForMinimum = lock.newCondition();

        /**
         * For blocking client so prereader is able to set state properly.
         */
        private final Condition waitForPreReader = lock.newCondition();

        /**
         * Did the pre reader finish?
         */
        private boolean preReaderFinished = false;

        /**
         * @param preread
         */
        protected PreRead() {
            super();
        }

        public void finished() {
            lock.lock();
            try {
                preReaderFinished = true;
                waitForPreReader.signal();
                waitForMinimum.signal();
                LOG.debug("Finished.");
            } finally {
                lock.unlock();
            }
        }

        public void offer(final T element) {
            lock.lock();
            try {
                elements.offer(element);
                waitForPreReader.signal();
                if (elements.size() >= MINIMUM_PREREAD) {
                    waitForMinimum.signal();
                }
                LOG.debug("Offered. " + elements.size());
            } finally {
                lock.unlock();
            }
        }

        public List<T> take(final boolean minimum) throws InterruptedException {
            final List<T> retval;
            lock.lock();
            try {
                LOG.debug("Taking. " + minimum);
                if (minimum && elements.size() < MINIMUM_PREREAD
                    && !preReaderFinished) {
                    LOG.debug("Waiting for enough.");
                    waitForMinimum.await();
                }
                if (elements.isEmpty()) {
                    throw new NoSuchElementException();
                }
                retval = new ArrayList<T>(elements.size());
                retval.addAll(elements);
                elements.clear();
                LOG.debug("Taken.");
            } finally {
                lock.unlock();
            }
            return retval;
        }

        public boolean hasNext() {
            lock.lock();
            try {
                while (!preReaderFinished && elements.isEmpty()) {
                    LOG.debug("Waiting for state.");
                    try {
                        waitForPreReader.await();
                    } catch (InterruptedException e) {
                        // Nothing to do. Continue with normal work.
                        LOG.debug(e.getMessage(), e);
                    }
                }
                return !elements.isEmpty();
            } finally {
                lock.unlock();
            }
        }
    }
}
