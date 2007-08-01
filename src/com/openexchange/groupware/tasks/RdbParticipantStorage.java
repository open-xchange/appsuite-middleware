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
import static com.openexchange.tools.sql.DBUtils.getIN;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    protected Map<Integer, Set<InternalParticipant>> selectInternal(
        final Context ctx, final Connection con, final int[] tasks,
        final StorageType type) throws TaskException {
        final Map<Integer, HashSet<InternalParticipant>> tmp =
            new HashMap<Integer, HashSet<InternalParticipant>>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getIN(SQL.SELECT_PARTS.get(type),
                tasks.length));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            for (int taskId : tasks) {
                stmt.setInt(pos++, taskId);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                final UserParticipant participant = new UserParticipant();
                pos = 1;
                final int taskId = result.getInt(pos++);
                participant.setIdentifier(result.getInt(pos++));
                Integer groupId = result.getInt(pos++);
                if (result.wasNull()) {
                    groupId = null;
                }
                final InternalParticipant taskParticipant =
                    new InternalParticipant(participant, groupId);
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
                HashSet<InternalParticipant> participants = tmp.get(taskId);
                if (null == participants) {
                    participants = new HashSet<InternalParticipant>();
                    tmp.put(taskId, participants);
                }
                participants.add(taskParticipant);
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        final Map<Integer, Set<InternalParticipant>> retval =
            new HashMap<Integer, Set<InternalParticipant>>();
        retval.putAll(tmp);
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void updateInternal(final Context ctx, final Connection con,
        final int taskId, final Set<InternalParticipant> participants,
        final StorageType type) throws TaskException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.UPDATE_PARTS.get(type));
            for (InternalParticipant participant : participants) {
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
        PreparedStatement stmt = null;
        int deleted = 0;
        try {
            stmt = con.prepareStatement(SQL.DELETE_PARTS.get(type));
            int counter = 1;
            stmt.setInt(counter++, ctx.getContextId());
            stmt.setInt(counter++, taskId);
            for (int user : users) {
                stmt.setInt(counter, user);
                stmt.addBatch();
            }
            final int[] rows = stmt.executeBatch();
            for (int row : rows) {
                deleted += row;
            }
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
    protected Map<Integer, Set<ExternalParticipant>> selectExternal(
        final Context ctx, final Connection con, final int[] tasks,
        final StorageType type) throws TaskException {
        final Map<Integer, Set<ExternalParticipant>> retval =
            new HashMap<Integer, Set<ExternalParticipant>>();
        if (StorageType.REMOVED == type) {
            return retval;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getIN(SQL.SELECT_EXTERNAL.get(type),
                tasks.length));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            for (int taskId : tasks) {
                stmt.setInt(pos++, taskId);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                pos = 1;
                final int taskId = result.getInt(pos++);
                final ExternalUserParticipant external =
                    new ExternalUserParticipant();
                external.setEmailAddress(result.getString(pos++));
                external.setDisplayName(result.getString(pos++));
                final ExternalParticipant participant =
                    new ExternalParticipant(external);
                Set<ExternalParticipant> participants = retval.get(taskId);
                if (null == participants) {
                    participants = new HashSet<ExternalParticipant>();
                    retval.put(taskId, participants);
                }
                participants.add(participant);
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval;
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
            stmt = con.prepareStatement(getIN(SQL.DELETE_EXTERNAL.get(type),
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
            final TaskException exc = new TaskException(Code
                .PARTICIPANT_DELETE_WRONG, addresses.length, deleted);
            LOG.error(exc.getMessage(), exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void insertExternals(final Context ctx, final Connection con,
        final int taskId, final Set<ExternalParticipant> participants,
        final StorageType type) throws TaskException {
        if (0 == participants.size() || StorageType.REMOVED == type) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.INSERT_EXTERNAL.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            for (ExternalParticipant participant : participants) {
                pos = 3;
                stmt.setString(pos++, participant.getMail());
                final String displayName = participant.getDisplayName();
                if (null == displayName) {
                    stmt.setNull(pos++, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos++, displayName);
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (DataTruncation e) {
            final int truncated = -1; // No ID defined here
            final TaskException tske = new TaskException(Code.TRUNCATED, e,
                "mail");
            tske.addTruncatedId(truncated);
            throw tske;
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
    void insertInternals(final Context ctx, final Connection con,
        final int taskId, final Set<InternalParticipant> participants,
        final StorageType type) throws TaskException {
        if (0 == participants.size()) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.INSERT_PARTS.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            for (InternalParticipant participant : participants) {
                pos = 3;
                stmt.setInt(pos++, participant.getIdentifier());
                if (null == participant.getGroupId()) {
                    stmt.setNull(pos++, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(pos++, participant.getGroupId());
                }
                stmt.setInt(pos++, participant.getConfirm());
                if (null == participant.getConfirmMessage()) {
                    stmt.setNull(pos++, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos++, participant.getConfirmMessage());
                }
                if (StorageType.REMOVED == type) {
                    final int folderId = participant.getFolderId();
                    if (UserParticipant.NO_PFID == folderId) {
                        stmt.setInt(pos++, 0);
                    } else {
                        stmt.setInt(pos++, participant.getFolderId());
                    }
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (DataTruncation e) {
            final int truncated = -1; // No ID defined here
            final TaskException tske = new TaskException(Code.TRUNCATED, e,
                "description");
            tske.addTruncatedId(truncated);
            throw tske;
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }
}
