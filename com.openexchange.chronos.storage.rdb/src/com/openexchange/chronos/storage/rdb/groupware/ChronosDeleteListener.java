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

package com.openexchange.chronos.storage.rdb.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * {@link ChronosDeleteListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public final class ChronosDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link ChronosDeleteListener}.
     */
    public ChronosDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            if (DeleteEvent.SUBTYPE_ANONYMOUS_GUEST != event.getSubType() && DeleteEvent.SUBTYPE_INVITED_GUEST != event.getSubType()) {
                /*
                 * remove all calendar data of deleted user
                 */
                purgeUserData(writeCon, event.getContext().getContextId(), event.getId());
            }
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            /*
             * remove all calendar data of deleted context
             */
            purgeContextData(writeCon, event.getContext().getContextId());
        }
    }

    /**
     * Purges the user data
     * 
     * @param writeCon The writeable {@link Connection}
     * @param cid The context identifier
     * @param user The user identifier
     * @throws OXException
     */
    private void purgeUserData(Connection writeCon, int cid, int user) throws OXException {
        try {
            /*
             * delete calendar accounts of user
             */
            deleteAccounts(writeCon, cid, user);
            /*
             * delete all availability data of the user
             */
            deleteAvailabilities(writeCon, cid, user);
            deleteFreeSlots(writeCon, cid, user);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Purges the context data
     * 
     * @param writeCon The writeable {@link Connection}
     * @param cid The context identifier
     * @throws OXException
     */
    private void purgeContextData(Connection writeCon, int cid) throws OXException {
        try {
            /*
             * delete all calendar accounts in context
             */
            deleteAccounts(writeCon, cid);
            /*
             * delete any 'tombstone' records
             */
            //...
            /*
             * delete all availability data in the context
             */
            deleteAvailabilities(writeCon, cid);
            deleteFreeSlots(writeCon, cid);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    private static int deleteAccounts(Connection connection, int cid) throws SQLException {
        return deleteForContext(connection, "calendar_account", cid);
    }

    private static int deleteAccounts(Connection connection, int cid, int user) throws SQLException {
        return deleteForUser(connection, "calendar_account", cid, user);
    }

    private static int deleteAvailabilities(Connection connection, int cid) throws SQLException {
        return deleteForContext(connection, "calendar_availability", cid);
    }

    private static int deleteAvailabilities(Connection connection, int cid, int user) throws SQLException {
        return deleteForUser(connection, "calendar_availability", cid, user);
    }

    private static int deleteFreeSlots(Connection connection, int cid) throws SQLException {
        return deleteForContext(connection, "calendar_free_slot", cid);
    }

    private static int deleteFreeSlots(Connection connection, int cid, int user) throws SQLException {
        return deleteForUser(connection, "calendar_free_slot", cid, user);
    }

    private static int deleteForContext(Connection connection, String tableName, int cid) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + tableName + " WHERE cid=?;")) {
            stmt.setInt(1, cid);
            return stmt.executeUpdate();
        }
    }

    private static int deleteForUser(Connection connection, String tableName, int cid, int user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + tableName + " WHERE cid=? AND user=?;")) {
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            return stmt.executeUpdate();
        }
    }
}
