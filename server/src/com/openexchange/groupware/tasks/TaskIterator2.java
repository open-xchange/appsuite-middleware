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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.Collections;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class implements the new iterator for tasks that fixes problems with
 * connection timeouts if a lot of tasks are read and that improved performance
 * while reading from database.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TaskIterator2 implements TaskIterator, Runnable {

    /**
     * Context.
     */
    private final Context ctx;

    /**
     * unique identifier of the user for reminders.
     */
    private final int userId;

    private final String sql;

    private final StatementSetter setter;

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
    private final StorageType type;

    /**
     * Pre read tasks.
     */
    private final PreRead<Task> preread = new PreRead<Task>();

    /**
     * Finished read tasks.
     */
    private final Queue<Task> ready = new LinkedList<Task>();

    private TaskException exc;

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
     * Warnings
     */
    private final List<AbstractOXException> warnings;

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
    TaskIterator2(final Context ctx, final int userId, final String sql,
        final StatementSetter setter, final int folderId, final int[] attributes,
        final StorageType type) {
        super();
        this.warnings =  new ArrayList<AbstractOXException>(2);
        this.ctx = ctx;
        this.userId = userId;
        this.sql = sql;
        this.setter = setter;
        this.folderId = folderId;
        final List<Integer> tmp1 = new ArrayList<Integer>(attributes.length);
        final List<Integer> tmp2 = new ArrayList<Integer>(attributes.length);
        for (final int column : attributes) {
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
        } catch (final InterruptedException e) {
            throw new SearchIteratorException(new TaskException(TaskException
                .Code.THREAD_ISSUE, e));
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return !ready.isEmpty() || preread.hasNext() || null != exc;
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
        if (ready.isEmpty() && !preread.hasNext()) {
            throw new SearchIteratorException(exc);
        }
        if (ready.isEmpty()) {
            try {
                final List<Task> tasks = new ArrayList<Task>();
                tasks.addAll(preread.take(additionalAttributes.length > 0));
                for (final int attribute : additionalAttributes) {
                    switch (attribute) {
                    case Task.PARTICIPANTS:
                        try {
                            readParticipants(tasks);
                        } catch (final TaskException e) {
                            throw new SearchIteratorException(e);
                        }
                        break;
                    case Task.ALARM:
                        try {
                            Reminder.loadReminder(ctx, userId, tasks);
                        } catch (final TaskException e) {
                            throw new SearchIteratorException(e);
                        }
                        break;
                    default:
                        throw new SearchIteratorException(new TaskException(
                            TaskException.Code.UNKNOWN_ATTRIBUTE, Integer
                            .valueOf(attribute)));
                    }
                }
                ready.addAll(tasks);
            } catch (final InterruptedException e) {
                throw new SearchIteratorException(new TaskException(
                    TaskException.Code.THREAD_ISSUE, e));
            }
        }
        return ready.poll();
    }

    /**
     * {@inheritDoc}
     */
    public void addWarning(final AbstractOXException warning) {
        warnings.add(warning);
    }

    /**
     * {@inheritDoc}
     */
    public AbstractOXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new AbstractOXException[warnings.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Reads the participants for the tasks.
     * @param tasks Tasks that should be filled with participants.
     * @throws TaskException if reading the participants fails.
     */
    private void readParticipants(final List<Task> tasks) throws TaskException {
        final int[] ids = new int[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            ids[i] = tasks.get(i).getObjectID();
        }
        final Map<Integer, Set<TaskParticipant>> parts = partStor
            .selectParticipants(ctx, ids, type);
        for (final Task task : tasks) {
            final Set<TaskParticipant> participants = parts.get(Integer
                .valueOf(task.getObjectID()));
            if (null != participants) {
                task.setParticipants(TaskLogic.createParticipants(participants));
                task.setUsers(TaskLogic.createUserParticipants(participants));
            }
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
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = DBPool.pickup(ctx);
            stmt = con.prepareStatement(sql);
            setter.perform(stmt);
            result = stmt.executeQuery();
            while (result.next()) {
                final Task task = new Task();
                int pos = 1;
                for (final int taskField : taskAttributes) {
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
        } catch (final SQLException e) {
            exc = new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } catch (final DBPoolingException e) {
            exc = new TaskException(Code.NO_CONNECTION, e);
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    interface StatementSetter {
        void perform(final PreparedStatement stmt) throws SQLException;
    }
}
