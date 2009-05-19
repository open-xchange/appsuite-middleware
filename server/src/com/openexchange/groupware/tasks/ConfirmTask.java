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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.event.EventException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ConfirmTask {

    private static final TaskStorage storage = TaskStorage.getInstance();

    private static final ParticipantStorage partStor = ParticipantStorage.getInstance();

    private static final int[] CHANGED_ATTRIBUTES = new int[] {
        Task.LAST_MODIFIED, Task.MODIFIED_BY
    };

    private final Context ctx;

    private final int taskId;

    private final int userId;

    private final int confirm;

    private final String message;

    private Task origTask;

    private Task changedTask;

    private InternalParticipant origParticipant;

    private InternalParticipant changedParticipant;

    /**
     * Default constructor.
     */
    ConfirmTask(final Context ctx, final int taskId, final int userId,
        final int confirm, final String message) {
        super();
        this.ctx = ctx;
        this.taskId = taskId;
        this.userId = userId;
        this.confirm = confirm;
        this.message = message;
    }

    // ===================== API methods =======================================
    
    /**
     * This method loads all necessary data and prepares the objects for updating
     * the database.
     */
    void prepare() throws TaskException {
        // Check if task exists.
        getOrigTask();
        // Load participant and set confirmation
        changedParticipant = getOrigParticipant();
        changedParticipant.setConfirm(confirm);
        changedParticipant.setConfirmMessage(message);
        // Prepare changed task attributes.
        changedTask = new Task();
        changedTask.setObjectID(taskId);
        changedTask.setModifiedBy(userId);
        changedTask.setLastModified(new Date());
    }

    /**
     * This method does all the changes in the database in a transaction.
     */
    void doConfirmation() throws TaskException {
        final Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (final DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            partStor.updateInternal(ctx, con, taskId, changedParticipant,
                StorageType.ACTIVE);
            UpdateData.updateTask(ctx, con, changedTask, getOrigTask()
                .getLastModified(), CHANGED_ATTRIBUTES, null, null, null, null);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            autocommit(con);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    void sentEvent(final Session session) throws TaskException {
		try {
			final EventClient eventClient = new EventClient(session);
			final int confirm = changedParticipant.getConfirm();
			if (CalendarObject.ACCEPT == confirm) {
				eventClient.accept(changedTask);
			} else if (CalendarObject.DECLINE == confirm) {
				eventClient.declined(changedTask);
			} else if (CalendarObject.TENTATIVE == confirm) {
				eventClient.tentative(changedTask);
			}
		} catch (final EventException e) {
			throw new TaskException(Code.EVENT, e);
		} catch (final OXException e) {
			throw new TaskException(e);
		} catch (final ContextException e) {
			throw new TaskException(e);
		}
    }

    /**
     * Gives the new last modified attribute of the changed task. This can be
     * only requested after {@link #prepare()} has been called.
     * @return the new last modified of the changed task.
     */
    Date getLastModified() {
        return changedTask.getLastModified();
    }

    // =========================== internal helper methods =====================

    /**
     * @return the original task.
     * @throws TaskException if loading of the original tasks fails.
     */
    private Task getOrigTask() throws TaskException {
        if (null == origTask) {
            origTask = storage.selectTask(ctx, taskId, StorageType.ACTIVE);
        }
        return origTask;
    }

    /**
     * @return the original participant.
     */
    private InternalParticipant getOrigParticipant() throws TaskException {
        if (null == origParticipant) {
            origParticipant = partStor.selectInternal(ctx, taskId, userId,
                StorageType.ACTIVE);
        }
        return origParticipant;
    }
}
