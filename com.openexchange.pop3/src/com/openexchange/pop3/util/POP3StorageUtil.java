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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.pop3.util;

import static com.openexchange.pop3.util.UIDUtil.uid2long;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.storage.POP3StoragePropertyNames;

/**
 * {@link POP3StorageUtil} - Utility class for POP3 storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class POP3StorageUtil {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(POP3StorageUtil.class));

    /**
     * Initializes a new {@link POP3StorageUtil}.
     */
    private POP3StorageUtil() {
        super();
    }

    private static final String SQL_SELECT_STORAGE_NAME = "SELECT value FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?";

    /**
     * Gets the POP3 storage provider name of specified user for given account.
     *
     * @param accountId The POP3 account ID
     * @param user The user ID
     * @param cid The context ID
     * @return The POP3 storage provider name of specified user for given account
     * @throws OXException If POP3 storage provider name cannot be returned
     */
    public static String getPOP3StorageProviderName(final int accountId, final int user, final int cid) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_STORAGE_NAME);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos, POP3StoragePropertyNames.PROPERTY_STORAGE);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    private static final String SQL_DELETE_STORAGE_NAME = "DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?";

    private static final String SQL_INSERT_STORAGE_NAME = "INSERT INTO user_mail_account_properties (cid, user, id, name, value) VALUES (?, ?, ?, ?, ?)";

    /**
     * Sets the POP3 storage provider name of specified user for given account.
     *
     * @param accountId The POP3 account ID
     * @param user The user ID
     * @param cid The context ID
     * @param name The provider name
     * @throws OXException If POP3 storage provider name cannot be set
     */
    public static void setPOP3StorageProviderName(final int accountId, final int user, final int cid, final String name) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE_STORAGE_NAME);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, POP3StoragePropertyNames.PROPERTY_STORAGE);
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            stmt = con.prepareStatement(SQL_INSERT_STORAGE_NAME);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, POP3StoragePropertyNames.PROPERTY_STORAGE);
            stmt.setString(pos++, name);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            Database.back(cid, true, con);
        }
    }

    /**
     * Gets the number of new messages since last sync.
     *
     * @param uidls The UIDLs reflecting current POP3 INBOX content
     * @param user The user ID
     * @param cid The context ID
     * @return The number of new messages since last sync
     * @throws OXException If determining new messages fails
     */
    public static int getNewMessageCount(final String[] uidls, final int user, final int cid) throws OXException {
        final List<String> databaseUIDLs = getUIDLs(user, cid);
        final List<String> actualUIDLs = Arrays.asList(uidls);

        // Determine & insert new UIDLs
        final Set<String> newUIDLs = new HashSet<String>(actualUIDLs);
        newUIDLs.removeAll(databaseUIDLs);
        return newUIDLs.size();
    }

    /**
     * Gets the number of deleted messages since last sync.
     *
     * @param uidls The UIDLs reflecting current POP3 INBOX content
     * @param user The user ID
     * @param cid The context ID
     * @return The number of deleted messages since last sync
     * @throws OXException If determining deleted messages fails
     */
    public static int getDeletedMessageCount(final String[] uidls, final int user, final int cid) throws OXException {
        final List<String> databaseUIDLs = getUIDLs(user, cid);
        final List<String> actualUIDLs = Arrays.asList(uidls);

        // Determine & delete removed UIDLs
        final Set<String> removedUIDLs = new HashSet<String>(databaseUIDLs);
        removedUIDLs.removeAll(actualUIDLs);
        return removedUIDLs.size();
    }

    /**
     * Synchronizes database with specified UIDLs.
     *
     * @param uidls The UIDLs reflecting current POP3 INBOX content
     * @param user The user ID
     * @param cid The context ID
     * @return The number of new messages since last sync
     * @throws OXException If synchronizing messages fails
     */
    public static int syncDBEntries(final String[] uidls, final int user, final int cid) throws OXException {
        final List<String> databaseUIDLs = getUIDLs(user, cid);
        final List<String> actualUIDLs = Arrays.asList(uidls);

        // Determine & delete removed UIDLs
        final Set<String> removedUIDLs = new HashSet<String>(databaseUIDLs);
        removedUIDLs.removeAll(actualUIDLs);
        deleteMessagesFromTables(removedUIDLs, user, cid);

        // Determine & insert new UIDLs
        final Set<String> newUIDLs = new HashSet<String>(actualUIDLs);
        newUIDLs.removeAll(databaseUIDLs);
        insertMessagesIntoTables(newUIDLs, user, cid);
        return newUIDLs.size();
    }

    private static final String SQL_SELECT_UIDLS = "SELECT uidl FROM user_pop3_data WHERE cid = ? AND user = ?";

    /**
     * Gets the UIDLs of the messages currently kept in database.
     *
     * @param user The user ID
     * @param cid The context ID
     * @return The UIDLs of the messages currently kept in database
     * @throws OXException If UIDLs cannot be retrieved from database
     */
    public static List<String> getUIDLs(final int user, final int cid) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        final List<String> uidls = new ArrayList<String>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_UIDLS);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            rs = stmt.executeQuery();
            while (rs.next()) {
                uidls.add(rs.getString(1));
            }
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
        return uidls;
    }

    private static final String SQL_DELETE_MSGS = "DELETE user_pop3_data, user_pop3_user_flag FROM user_pop3_data, user_pop3_user_flag WHERE user_pop3_data.uid = user_pop3_user_flag.uid AND user_pop3_data.cid = ? AND user_pop3_data.user = ? AND user_pop3_data.uidl = ?";

    /**
     * Deletes the messages from database whose UIDL is contained in specified collection.
     *
     * @param uidls The collection of UIDLs
     * @param user The user ID
     * @param cid The context ID
     * @throws OXException If messages cannot be deleted
     */
    public static void deleteMessagesFromTables(final Collection<String> uidls, final int user, final int cid) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE_MSGS);
            for (final String uidl : uidls) {
                stmt.setInt(1, cid);
                stmt.setInt(2, user);
                stmt.setString(3, uidl);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private static final String SQL_INSERT_DATA = "INSERT INTO user_pop3_data (cid, user, uid, uidl, flags, color_flag, received_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

    /**
     * Inserts the messages to database whose UIDL is contained in specified collection.
     *
     * @param uidls The collection of UIDLs
     * @param user The user ID
     * @param cid The context ID
     * @throws OXException If messages cannot be inserted
     */
    public static void insertMessagesIntoTables(final Collection<String> uidls, final int user, final int cid) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_INSERT_DATA);
            final long creatingTime = System.currentTimeMillis();
            for (final String uidl : uidls) {
                stmt.setLong(1, cid);
                stmt.setLong(2, user);
                stmt.setLong(3, uid2long(uidl));
                stmt.setString(4, uidl);
                stmt.setInt(5, 0);
                stmt.setInt(6, 0);
                stmt.setLong(7, creatingTime);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    /**
     * Gets the number of unread messages kept in database.
     *
     * @param user The user ID
     * @param cid The context ID
     * @return The number of unread messages kept in database
     * @throws OXException
     */
    public static int getUnreadMessagesCount(final int user, final int cid) throws OXException {
        return getUnreadMessages(user, cid).size();
    }

    private static final String SQL_SELECT_UNREAD = "SELECT uidl FROM user_pop3_data WHERE cid = ? AND user = ? AND (flags & ?) = ?";

    /**
     * Gets the UIDLs of unread messages kept in database.
     *
     * @param user The user ID
     * @param cid The context ID
     * @return The UIDLs of unread messages kept in database
     * @throws OXException If unread messages cannot be retrieved
     */
    public static Set<String> getUnreadMessages(final int user, final int cid) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        final Set<String> uidls = new HashSet<String>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_UNREAD);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, MailMessage.FLAG_SEEN);
            stmt.setInt(pos++, 0);
            rs = stmt.executeQuery();
            while (rs.next()) {
                uidls.add(rs.getString(1));
            }
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
        return uidls;
    }

    private static final String SQL_SELECT_SYS_FLAGS = "SELECT flags FROM user_pop3_data WHERE uidl = ? AND user = ? AND cid = ?";

    /**
     * Gets the system flags of the message identified by specified UIDL.
     *
     * @param uidl The UIDL
     * @param user The user ID
     * @param cid The context ID
     * @return The system flags of the message identified by specified UIDL
     * @throws OXException If message's flags cannot be retrieved
     */
    public static int getSystemFlags(final String uidl, final int user, final int cid) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_SYS_FLAGS);
            stmt.setString(1, uidl);
            stmt.setLong(2, user);
            stmt.setLong(3, cid);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return 0;
            }
            return rs.getInt(1);
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    private static final String SQL_SELECT_COL_FLAGS = "SELECT color_flag FROM user_pop3_data WHERE uidl = ? AND user = ? AND cid = ?";

    /**
     * Gets the color flag of the message identified by specified UIDL.
     *
     * @param uidl The UIDL
     * @param user The user ID
     * @param cid The context ID
     * @return The color flag of the message identified by specified UIDL
     * @throws OXException If message's color flag cannot be retrieved
     */
    public static int getColorFlag(final String uidl, final int user, final int cid) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_COL_FLAGS);
            stmt.setString(1, uidl);
            stmt.setLong(2, user);
            stmt.setLong(3, cid);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return 0;
            }
            return rs.getInt(1);
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    private static final String SQL_SELECT_USER_FLAGS = "SELECT user_flag FROM user_pop3_user_flag WHERE uid = ? AND user = ? AND cid = ?";

    /**
     * Gets the user flags of the message identified by specified UIDL.
     *
     * @param uidl The UIDL
     * @param user The user ID
     * @param cid The context ID
     * @return The user flags of the message identified by specified UIDL
     * @throws OXException If message's flags cannot be retrieved
     */
    public static String[] getUserFlags(final String uidl, final int user, final int cid) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_USER_FLAGS);
            stmt.setLong(1, UIDUtil.uid2long(uidl));
            stmt.setLong(2, user);
            stmt.setLong(3, cid);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new String[0];
            }
            final List<String> tmp = new ArrayList<String>();
            do {
                tmp.add(rs.getString(1));
            } while (rs.next());
            return tmp.toArray(new String[tmp.size()]);
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    private static final String LOGIN_DELAY = "LOGIN-DELAY";

    /**
     * Parses the minimum allowed seconds between logins indicated by "LOGIN-DELAY" capability.
     *
     * @param capabilities The capabilities possibly containing "LOGIN-DELAY" capability
     * @return The minimum allowed seconds between logins or <code>-1</code> on absence
     */
    public static int parseLoginDelaySeconds(final String capabilities) {
        if (null == capabilities) {
            return -1;
        }
        int pos = capabilities.indexOf(LOGIN_DELAY);
        if (-1 == pos) {
            // No LOGIN-DELAY capability found
            return -1;
        }
        // Parse seconds; something like LOGIN-DELAY 60
        final StringBuilder seconds = new StringBuilder(16);
        pos += LOGIN_DELAY.length();
        char c = capabilities.charAt(pos++);
        final int len = capabilities.length();
        while ('\r' != c && '\n' != c) {
            if (Character.isDigit(c)) {
                seconds.append(c);
            }
            if (pos >= len) {
                c = '\n';
            } else {
                c = capabilities.charAt(pos++);
            }
        }
        try {
            return Integer.parseInt(seconds.toString());
        } catch (final NumberFormatException e) {
            LOG.warn("LOGIN-DELAY seconds cannot be parsed to an integer: " + capabilities, e);
            return -1;
        }
    }

}
