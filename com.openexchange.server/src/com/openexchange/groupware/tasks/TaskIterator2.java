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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.groupware.tasks;

import static com.openexchange.java.Autoboxing.I;
import gnu.trove.map.TIntObjectMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.Collections;
import com.openexchange.tools.exceptions.ExceptionUtils;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class implements the new iterator for tasks that fixes problems with
 * connection timeouts if a lot of tasks are read and that improved performance
 * while reading from database.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TaskIterator2 implements TaskIterator, Runnable {

    private final Context ctx;

    private final int userId;

    private final String sql;

    private final StatementSetter setter;

    private final int folderId;

    private final int[] taskAttributes;

    private final int[] additionalAttributes;

    private final StorageType type;

    private final Connection con;

    private final PreRead<Task> preread = new PreRead<Task>();

    private final Queue<Task> ready = new LinkedList<Task>();

    private OXException exc;

    private final Future<Object> runner;

    private final ParticipantStorage partStor = ParticipantStorage.getInstance();

    private final List<OXException> warnings;

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
        this(ctx, userId, sql, setter, folderId, attributes, type, null);
    }

    TaskIterator2(final Context ctx, final int userId, final String sql,
        final StatementSetter setter, final int folderId, final int[] attributes,
        final StorageType type, final Connection con) {
        super();
        this.warnings =  new ArrayList<OXException>(2);
        this.ctx = ctx;
        this.userId = userId;
        this.sql = sql;
        this.setter = setter;
        this.folderId = folderId;
        final List<Integer> tmp1 = new ArrayList<Integer>(attributes.length);
        final List<Integer> tmp2 = new ArrayList<Integer>(attributes.length);
        for (final int column : attributes) {
            if (null == Mapping.getMapping(column) && FolderChildObject.FOLDER_ID != column) {
                tmp2.add(I(column));
            } else {
                tmp1.add(I(column));
            }
        }
        this.taskAttributes = Collections.toArray(tmp1);
        modifyAdditionalAttributes(tmp2);
        this.additionalAttributes = Collections.toArray(tmp2);
        this.type = type;
        this.con = con;
        final ThreadPoolService threadPool = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
        runner = threadPool.submit(ThreadPools.trackableTask(this));
    }

    private void modifyAdditionalAttributes(final List<Integer> additional) {
        // If participants are requested we also add users automatically. Users
        // are calculated from participants.
        if (additional.contains(I(CalendarObject.USERS))) {
            if (!additional.contains(I(CalendarObject.PARTICIPANTS))) {
                additional.add(I(CalendarObject.PARTICIPANTS));
            }
            // Not removed users will give SearchIteratorException in code
            // below.
            additional.remove(I(CalendarObject.USERS));
        }
    }

    @Override
    public void close() {
        try {
            runner.get();
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(TaskIterator2.class);
            logger.error("Failed to close search iterator", e.getCause());
        }
    }

    @Override
    public boolean hasNext() {
        return !ready.isEmpty() || preread.hasNext() || null != exc;
    }

    public boolean hasSize() {
        return false;
    }

    @Override
    public Task next() throws OXException {
        if (ready.isEmpty() && !preread.hasNext()) {
            throw exc;
        }
        if (ready.isEmpty()) {
            try {
                final List<Task> tasks = new ArrayList<Task>();
                tasks.addAll(preread.take(additionalAttributes.length > 0));
                for (final int attribute : additionalAttributes) {
                    switch (attribute) {
                    case CommonObject.LAST_MODIFIED_OF_NEWEST_ATTACHMENT:
                        addLastModifiedOfNewestAttachment(tasks);
                        break;
                    case CalendarObject.PARTICIPANTS:
                        try {
                            readParticipants(tasks);
                        } catch (final OXException e) {
                            throw e;
                        }
                        break;
                    case CalendarObject.ALARM:
                        try {
                            if (con == null) {
                                Reminder.loadReminder(ctx, userId, tasks);
                            } else {
                                Reminder.loadReminder(ctx, userId, tasks, con);
                            }
                        } catch (final OXException e) {
                            throw e;
                        }
                        break;
                    default:
                        throw
                            TaskExceptionCode.UNKNOWN_ATTRIBUTE.create(Integer
                            .valueOf(attribute));
                    }
                }
                ready.addAll(tasks);
            } catch (final InterruptedException e) {
                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                Thread.currentThread().interrupt();
                throw TaskExceptionCode.THREAD_ISSUE.create(e);
            }
        }
        return ready.poll();
    }

    @Override
    public void addWarning(final OXException warning) {
        warnings.add(warning);
    }

    @Override
    public OXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Reads the participants for the tasks.
     * @param tasks Tasks that should be filled with participants.
     * @throws OXException if reading the participants fails.
     */
    private void readParticipants(final List<Task> tasks) throws OXException {
        final int[] ids = new int[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            ids[i] = tasks.get(i).getObjectID();
        }
        Map<Integer, Set<TaskParticipant>> parts = null;
        if (this.con == null) {
            parts = partStor.selectParticipants(ctx, ids, type);
        } else {
            parts = partStor.selectParticipants(ctx, ids, type, con);
        }
        for (final Task task : tasks) {
            final Set<TaskParticipant> participants = parts.get(I(task.getObjectID()));
            if (null != participants) {
                task.setParticipants(TaskLogic.createParticipants(participants));
                task.setUsers(TaskLogic.createUserParticipants(participants));
            }
        }
    }

    private void addLastModifiedOfNewestAttachment(final List<Task> tasks) throws OXException {
        final int[] ids = new int[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            ids[i] = tasks.get(i).getObjectID();
        }
        final AttachmentBase attachmentBase = Attachments.getInstance();
        final TIntObjectMap<Date> dates = attachmentBase.getNewestCreationDates(ctx, Types.TASK, ids);
        for (final Task task : tasks) {
            final Date newestCreationDate = dates.get(task.getObjectID());
            if (null != newestCreationDate) {
                task.setLastModifiedOfNewestAttachment(newestCreationDate);
            }
        }
    }

    @Override
    public int size() {
        return -1;
    }

    @Override
    public void run() {
        final Connection myCon;
        if (null == con) {
            try {
                myCon = DBPool.pickup(ctx);
            } catch (final OXException e) {
                preread.finished();
                exc = TaskExceptionCode.NO_CONNECTION.create(e);
                return;
            }
        } else {
            myCon = this.con;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = myCon.prepareStatement(sql);
            setter.perform(stmt);
            result = stmt.executeQuery();
            while (result.next()) {
                final Task task = new Task();
                int pos = 1;
                for (final int taskField : taskAttributes) {
                    final Mapper<?> mapper = Mapping.getMapping(taskField);
                    if (FolderChildObject.FOLDER_ID == taskField) {
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
        } catch (final SQLException e) {
            exc = TaskExceptionCode.SQL_ERROR.create(e);
        } catch (final Throwable t) {
            ExceptionUtils.handleThrowable(t);
            exc = TaskExceptionCode.THREAD_ISSUE.create(t);
        } finally {
            preread.finished();
            DBUtils.closeSQLStuff(result, stmt);
            if (null == con) {
                DBPool.closeReaderSilent(ctx, myCon);
            }
        }
    }

    interface StatementSetter {
        void perform(final PreparedStatement stmt) throws SQLException;
    }
}
