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

package com.openexchange.calendar.storage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.contexts.Context;

/**
 * Stores external participants in a relational database.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class RdbParticipantStorage extends ParticipantStorage {

    public RdbParticipantStorage() {
        super();
    }

    @Override
    public void insertParticipants(final Context ctx, final Connection con, final int appointmentId, final ExternalUserParticipant[] participants) throws OXException {
        if (null == participants || 0 == participants.length) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.INSERT_EXTERNAL);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, appointmentId);
            final Set<String> addresses = new HashSet<String>(participants.length);
            for (final ExternalUserParticipant participant : participants) {
                pos = 3;
                final String emailAddress = participant.getEmailAddress();
                if (addresses.add(emailAddress)) {
                    stmt.setString(pos++, emailAddress);
                    final String displayName = participant.getDisplayName();
                    if (null == displayName) {
                        stmt.setNull(pos++, Types.VARCHAR);
                    } else {
                        stmt.setString(pos++, displayName);
                    }
                    stmt.setInt(pos++, participant.getConfirm());
                    final String message = participant.getMessage();
                    if (null == message) {
                        stmt.setNull(pos++, Types.VARCHAR);
                    } else {
                        stmt.setString(pos++, message);
                    }
                    stmt.addBatch();
                } else {
                    /*
                     * Duplicate address
                     */
                    final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RdbParticipantStorage.class);
                    // final OXException e = OXCalendarExceptionCodes.DUPLICATE_EXTERNAL_PARTICIPANT.create(emailAddress);
                    logger.warn("An external participant with the E-Mail address {} is already included. Please remove participant duplicate and retry.", emailAddress);
                }
            }
            stmt.executeBatch();
            // TODO data truncation should be catched if some too long messages should be stored.
//        } catch (final DataTruncation e) {
//            throw parseTruncatedE(con, e, type, participants);
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public Map<Integer, ExternalUserParticipant[]> selectExternal(final Context ctx, final Connection con, final int[] appointmentIds) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final Map<Integer, List<ExternalUserParticipant>> retval = new HashMap<Integer, List<ExternalUserParticipant>>(appointmentIds.length, 1);
        for (final int appointmentId : appointmentIds) {
            retval.put(I(appointmentId), new ArrayList<ExternalUserParticipant>());
        }
        try {
            stmt = con.prepareStatement(getIN(SQL.SELECT_EXTERNAL, appointmentIds.length));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            for (final int appointmentId : appointmentIds) {
                stmt.setInt(pos++, appointmentId);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                pos = 1;
                final int appointmentId = rs.getInt(pos++);
                final ExternalUserParticipant participant = new ExternalUserParticipant(rs.getString(pos++));
                participant.setDisplayName(rs.getString(pos++));
                participant.setConfirm(rs.getInt(pos++));
                participant.setMessage(rs.getString(pos++));
                final List<ExternalUserParticipant> participants = retval.get(I(appointmentId));
                participants.add(participant);
            }
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
        final Map<Integer, ExternalUserParticipant[]> retval2 = new HashMap<Integer, ExternalUserParticipant[]>();
        for (final Entry<Integer, List<ExternalUserParticipant>> entry : retval.entrySet()) {
            final List<ExternalUserParticipant> participants = entry.getValue();
            retval2.put(entry.getKey(), participants.toArray(new ExternalUserParticipant[participants.size()]));
        }
        return Collections.unmodifiableMap(retval2);
    }

    @Override
    public void deleteParticipants(final Context ctx, final Connection con, final int appointmentId, final ExternalUserParticipant[] participants) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.DELETE_EXTERNAL);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, appointmentId);
            for (final ExternalUserParticipant participant : participants) {
                stmt.setString(pos, participant.getEmailAddress());
                stmt.addBatch();
            }
            final int[] rowss = stmt.executeBatch();
            if (rowss.length != participants.length) {
                throw OXCalendarExceptionCodes.WRONG_ROW_COUNT.create(I(participants.length), I(rowss.length));
            }
            for (final int rows : rowss) {
                if (1 != rows) {
                    throw OXCalendarExceptionCodes.WRONG_ROW_COUNT.create(I(1), I(rows));
                }
            }
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(stmt);
        }
    }
}
