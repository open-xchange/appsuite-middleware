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
import com.openexchange.database.IncorrectStringSQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.Collections;
import com.openexchange.tools.sql.DBUtils;

/**
 * Implementation of the participant storage interface.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbParticipantStorage extends ParticipantStorage {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbParticipantStorage.class);

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
        final StorageType type) throws OXException {
        final Map<Integer, HashSet<InternalParticipant>> tmp =
            new HashMap<Integer, HashSet<InternalParticipant>>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getIN(SQL.SELECT_PARTS.get(type),
                tasks.length));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            for (final int taskId : tasks) {
                stmt.setInt(pos++, taskId);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                pos = 1;
                final int taskId = result.getInt(pos++);
                final UserParticipant participant = new UserParticipant(
                    result.getInt(pos++));
                Integer groupId = Integer.valueOf(result.getInt(pos++));
                if (result.wasNull()) {
                    groupId = null;
                }
                final InternalParticipant taskParticipant =
                    new InternalParticipant(participant, groupId);
                taskParticipant.setConfirm(result.getInt(pos++));
                taskParticipant.setConfirmMessage(result.getString(pos++));
                // Only for removed participants the folder is stored in the participant table. For all active participants the folder
                // is stored in the task_folder table.
                if (StorageType.REMOVED == type) {
                    final int folderId = result.getInt(pos++);
                    if (0 == folderId) {
                        taskParticipant.setFolderId(UserParticipant.NO_PFID);
                    } else {
                        taskParticipant.setFolderId(folderId);
                    }
                }
                HashSet<InternalParticipant> participants = tmp.get(Integer
                    .valueOf(taskId));
                if (null == participants) {
                    participants = new HashSet<InternalParticipant>();
                    tmp.put(Integer.valueOf(taskId), participants);
                }
                participants.add(taskParticipant);
            }
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        final Map<Integer, Set<InternalParticipant>> retval =
            new HashMap<Integer, Set<InternalParticipant>>();
        retval.putAll(tmp);
        return retval;
    }

    @Override
    void updateInternal(Context ctx, Connection con, int taskId, Set<InternalParticipant> participants, StorageType type) throws OXException {
        PreparedStatement stmt = null;
        try {
            // UPDATE table SET group_id=?, accepted=?, description=? WHERE cid=? AND task=? AND user=?
            stmt = con.prepareStatement(SQL.UPDATE_PARTS.get(type));
            for (final InternalParticipant participant : participants) {
                int pos = 1;
                if (null == participant.getGroupId()) {
                    stmt.setNull(pos++, Types.INTEGER);
                } else {
                    stmt.setInt(pos++, participant.getGroupId().intValue());
                }
                stmt.setInt(pos++, participant.getConfirm());
                stmt.setString(pos++, participant.getConfirmMessage());
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, taskId);
                stmt.setInt(pos++, participant.getIdentifier());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final DataTruncation e) {
            throw parseTruncated(con, e, type, participants);
        } catch (IncorrectStringSQLException e) {
            throw Tools.parseIncorrectString(e);
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
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
        final boolean check) throws OXException {
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
            for (final int user : users) {
                stmt.setInt(counter++, user);
            }
            deleted = stmt.executeUpdate();
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
        if (check && users.length != deleted) {
            final OXException tske = TaskExceptionCode
                .PARTICIPANT_DELETE_WRONG.create(Integer.valueOf(users.length),
                Integer.valueOf(deleted));
            LOG.error("", tske);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int[] findTasksWithGroup(final Context ctx, final Connection con,
        final int groupId, final StorageType type) throws OXException {
        final List<Integer> tasks = new ArrayList<Integer>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL.FIND_GROUP.get(type));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, groupId);
            result = stmt.executeQuery();
            while (result.next()) {
                tasks.add(Integer.valueOf(result.getInt(1)));
            }
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
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
        final int userId, final StorageType type) throws OXException {
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
                tasks.add(Integer.valueOf(result.getInt(1)));
            }
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return Collections.toArray(tasks);
    }

    @Override
    protected Map<Integer, Set<ExternalParticipant>> selectExternal(Context ctx, Connection con, int[] tasks, StorageType type) throws OXException {
        if (StorageType.REMOVED == type || StorageType.DELETED == type) {
            return java.util.Collections.emptyMap();
        }
        final Map<Integer, Set<ExternalParticipant>> retval = new HashMap<Integer, Set<ExternalParticipant>>();
        try (PreparedStatement stmt = con.prepareStatement(getIN(SQL.SELECT_EXTERNAL.get(type), tasks.length))) {
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            for (final int taskId : tasks) {
                stmt.setInt(pos++, taskId);
            }
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    pos = 1;
                    final int taskId = result.getInt(pos++);
                    final ExternalUserParticipant external =
                        new ExternalUserParticipant(result.getString(pos++));
                    external.setDisplayName(result.getString(pos++));
                    final ExternalParticipant participant =
                        new ExternalParticipant(external);
                    Set<ExternalParticipant> participants = retval.get(Integer
                        .valueOf(taskId));
                    if (null == participants) {
                        participants = new HashSet<ExternalParticipant>();
                        retval.put(Integer.valueOf(taskId), participants);
                    }
                    participants.add(participant);
                }
            }
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        }
        return retval;
    }

    @Override
    void deleteExternal(Context ctx, Connection con, int taskId, String[] addresses, StorageType type, boolean check) throws OXException {
        if (0 == addresses.length || StorageType.REMOVED == type || StorageType.DELETED == type) {
            return;
        }
        int deleted = 0;
        try (PreparedStatement stmt = con.prepareStatement(getIN(SQL.DELETE_EXTERNAL.get(type), addresses.length))) {
            int counter = 1;
            stmt.setInt(counter++, ctx.getContextId());
            stmt.setInt(counter++, taskId);
            for (final String address : addresses) {
                stmt.setString(counter++, address);
            }
            deleted = stmt.executeUpdate();
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        }
        if (check && addresses.length != deleted) {
            OXException exc = TaskExceptionCode.PARTICIPANT_DELETE_WRONG.create(I(addresses.length), I(deleted));
            LOG.error(exc.getMessage(), exc);
        }
    }

    @Override
    public void insertExternals(Context ctx, Connection con, int taskId, Set<ExternalParticipant> participants, StorageType type) throws OXException {
        if (0 == participants.size() || StorageType.REMOVED == type || StorageType.DELETED == type) {
            return;
        }
        try (PreparedStatement stmt = con.prepareStatement(SQL.INSERT_EXTERNAL.get(type))) {
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            for (final ExternalParticipant participant : participants) {
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
            throw parseTruncatedE(con, e, type, participants);
        } catch (IncorrectStringSQLException e) {
            throw Tools.parseIncorrectString(e);
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        }
    }

    private static OXException parseTruncatedE(final Connection con,
        final DataTruncation dt, final StorageType type,
        final Set<ExternalParticipant> participants) {
        final String[] fields = DBUtils.parseTruncatedFields(dt);
        final OXException.Truncated[] truncateds = new OXException.Truncated[fields.length];
        final StringBuilder sFields = new StringBuilder();
        int tmp = 0;
        for (final ExternalParticipant participant : participants) {
            tmp = Math.max(tmp, participant.getMail().length());
            if (null != participant.getDisplayName()) {
                tmp = Math.max(tmp, participant.getDisplayName().length());
            }
        }
        final int maxLength = tmp;
        for (int i = 0; i < fields.length; i++) {
            sFields.append(fields[i]);
            sFields.append(", ");
            tmp = 0;
            try {
                tmp = DBUtils.getColumnSize(con, SQL.EPARTS_TABLES.get(type),
                    fields[i]);
                if (-1 == tmp) {
                    tmp = 0;
                }
            } catch (final SQLException e) {
                LOG.error("", e);
                tmp = 0;
            }
            final int maxSize = tmp;
            truncateds[i] = new OXException.Truncated() {
                @Override
                public int getId() {
                    return -1; // No ID defined here
                }
                @Override
                public int getLength() {
                    return maxLength;
                }
                @Override
                public int getMaxSize() {
                    return maxSize;
                }
            };
        }
        sFields.setLength(sFields.length() - 1);
        final OXException tske;
        if (truncateds.length > 0) {
            final OXException.Truncated truncated = truncateds[0];
            tske = TaskExceptionCode.TRUNCATED.create(dt, sFields.toString(),
                Integer.valueOf(truncated.getMaxSize()),
                Integer.valueOf(truncated.getLength()));
        } else {
            tske = TaskExceptionCode.TRUNCATED.create(dt, sFields.toString(),
                Integer.valueOf(0),
                Integer.valueOf(0));
        }
        for (final OXException.Truncated truncated : truncateds) {
            tske.addProblematic(truncated);
        }
        return tske;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertInternals(final Context ctx, final Connection con,
        final int taskId, final Set<InternalParticipant> participants,
        final StorageType type) throws OXException {
        if (0 == participants.size()) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.INSERT_PARTS.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            for (final InternalParticipant participant : participants) {
                pos = 3;
                stmt.setInt(pos++, participant.getIdentifier());
                if (null == participant.getGroupId()) {
                    stmt.setNull(pos++, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(pos++, participant.getGroupId().intValue());
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
        } catch (final DataTruncation e) {
            throw parseTruncated(con, e, type, participants);
        } catch (IncorrectStringSQLException e) {
            throw Tools.parseIncorrectString(e);
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private static OXException parseTruncated(final Connection con,
        final DataTruncation dt, final StorageType type,
        final Set<InternalParticipant> participants) {
        final String field = "description";
        int tmp = 0;
        for (final InternalParticipant participant : participants) {
            tmp = Math.max(tmp, participant.getConfirmMessage().length());
        }
        final int maxLength = tmp;
        try {
            tmp = DBUtils.getColumnSize(con, SQL.EPARTS_TABLES.get(type), field);
            if (-1 == tmp) {
                tmp = 0;
            }
        } catch (final SQLException e) {
            LOG.error("", e);
            tmp = 0;
        }
        final int maxSize = tmp;
        final OXException tske = TaskExceptionCode.TRUNCATED.create(dt,
            field, Integer.valueOf(maxSize), Integer.valueOf(maxLength));
        tske.addProblematic(new OXException.Truncated() {
            @Override
            public int getId() {
                return -1; // No ID defined here
            }
            @Override
            public int getLength() {
                return maxLength;
            }
            @Override
            public int getMaxSize() {
                return maxSize;
            }
        });
        return tske;
    }
}
