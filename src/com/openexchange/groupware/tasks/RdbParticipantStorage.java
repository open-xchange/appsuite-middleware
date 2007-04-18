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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.tools.Collections;

/**
 * Implementation of the participant storage interface.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbParticipantStorage extends ParticipantStorage {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory
        .getLog(RdbParticipantStorage.class);

    /**
     * Default constructor.
     */
    public RdbParticipantStorage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Set<TaskInternalParticipant> selectInternal(final Context ctx,
        final Connection con, final int taskId, final StorageType type)
        throws TaskException {
        final Set<TaskInternalParticipant> participants =
            new HashSet<TaskInternalParticipant>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(
                SQL.SELECT_PARTS.get(type));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, taskId);
            result = stmt.executeQuery();
            while (result.next()) {
                final UserParticipant participant = new UserParticipant();
                int pos = 1;
                participant.setIdentifier(result.getInt(pos++));
                Integer groupId = result.getInt(pos++);
                if (result.wasNull()) {
                    groupId = null;
                }
                final TaskInternalParticipant taskParticipant =
                    new TaskInternalParticipant(participant, groupId);
                taskParticipant.setConfirm(result.getInt(pos++));
                taskParticipant.setConfirmMessage(result.getString(pos++));
                if (StorageType.REMOVED == type) {
                    final int folderId = result.getInt(pos++);
                    if (0 == folderId) {
                        taskParticipant.setFolderId(UserParticipant.NO_PFID);
                    } else {
                        taskParticipant.setFolderId(folderId);
                    }
                }
                participants.add(taskParticipant);
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return participants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void updateInternal(final Context ctx, final Connection con,
        final int taskId, final Set<TaskInternalParticipant> participants,
        final StorageType type) throws TaskException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.UPDATE_PARTS.get(type));
            for (TaskInternalParticipant participant : participants) {
                int pos = 1;
                if (null == participant.getGroupId()) {
                    stmt.setNull(pos++, Types.INTEGER);
                } else {
                    stmt.setInt(pos++, participant.getGroupId());
                }
                stmt.setInt(pos++, participant.getConfirm());
                stmt.setString(pos++, participant.getConfirmMessage());
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, taskId);
                stmt.setInt(pos++, participant.getIdentifier());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deleteInternal(final Context ctx, final Connection con,
        final int taskId, final int[] users, final StorageType type,
        final boolean check) throws TaskException {
        if (users.length == 0) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append(SQL.DELETE_PARTS.get(type));
        for (int i = 0; i < users.length; i++) {
            sql.append("?,");
        }
        sql.setCharAt(sql.length() - 1, ')');
        PreparedStatement stmt = null;
        final int deleted;
        try {
            stmt = con.prepareStatement(sql.toString());
            int counter = 1;
            stmt.setInt(counter++, ctx.getContextId());
            stmt.setInt(counter++, taskId);
            for (int user : users) {
                stmt.setInt(counter++, user);
            }
            deleted = stmt.executeUpdate();
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
        if (check && users.length != deleted) {
            final TaskException tske = new TaskException(Code
                .PARTICIPANT_DELETE_WRONG, users.length, deleted);
            LOG.error(tske.getMessage(), tske);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int[] findTasksWithGroup(final Context ctx, final Connection con,
        final int groupId, final StorageType type) throws TaskException {
        final List<Integer> tasks = new ArrayList<Integer>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL.FIND_GROUP.get(type));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, groupId);
            result = stmt.executeQuery();
            while (result.next()) {
                tasks.add(result.getInt(1));
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return Collections.toArray(tasks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int[] findTasksWithParticipant(final Context ctx, final Connection con,
        final int userId, final StorageType type) throws TaskException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> tasks = new ArrayList<Integer>();
        try {
            stmt = con.prepareStatement(SQL.FIND_PARTICIPANT.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            result = stmt.executeQuery();
            while (result.next()) {
                tasks.add(result.getInt(1));
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return Collections.toArray(tasks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Set<TaskExternalParticipant> selectExternal(final Context ctx,
        final Connection con, final int taskId, final StorageType type)
        throws TaskException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final Set<TaskExternalParticipant> participants =
            new HashSet<TaskExternalParticipant>();
        try {
            stmt = con.prepareStatement(SQL.SELECT_EXTERNAL.get(type));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, taskId);
            result = stmt.executeQuery();
            while (result.next()) {
                final ExternalUserParticipant external =
                    new ExternalUserParticipant();
                int pos = 1;
                external.setEmailAddress(result.getString(pos++));
                external.setDisplayName(result.getString(pos++));
                final TaskExternalParticipant participant =
                    new TaskExternalParticipant(external);
                participants.add(participant);
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return participants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deleteExternal(final Context ctx, final Connection con,
        final int taskId, final String[] addresses, final StorageType type,
        final boolean check) throws TaskException {
        if (0 == addresses.length) {
            return;
        }
        PreparedStatement stmt = null;
        int deleted = 0;
        try {
            stmt = con.prepareStatement(SQL.getIN(SQL.DELETE_EXTERNAL.get(type),
                addresses.length));
            int counter = 1;
            stmt.setInt(counter++, ctx.getContextId());
            stmt.setInt(counter++, taskId);
            for (String address : addresses) {
                stmt.setString(counter++, address);
            }
            deleted = stmt.executeUpdate();
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
        if (check && addresses.length != deleted) {
            final TaskException e = new TaskException(Code
                .PARTICIPANT_DELETE_WRONG, addresses.length, deleted);
            LOG.error(e.getMessage(), e);
        }
    }
}
