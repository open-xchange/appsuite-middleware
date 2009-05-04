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

package com.openexchange.pop3.storage.mailaccount.util;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.database.Database;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.pop3.POP3Exception;
import com.openexchange.pop3.storage.mailaccount.PropertyNames;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;

/**
 * {@link Utility} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    /**
     * Prepends specified path to given virtual fullname.<br>
     * <code>
     * &quot;<b>Trash</b>&quot;&nbsp;=&gt;&nbsp;&quot;INBOX/path/to/pop3account/<b>Trash</b>&quot;
     * </code>
     * 
     * @param path The path to prepend
     * @param separator The separator character
     * @param virtualFullname The virtual fullname
     * @return The real fullname
     */
    public static String prependPath2Fullname(final String path, final char separator, final String virtualFullname) {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(virtualFullname)) {
            return path;
        }
        return new StringBuilder(path.length() + virtualFullname.length() + 1).append(path).append(separator).append(virtualFullname).toString();
    }

    /**
     * Strips possibly prepended path from specified real fullname.<br>
     * <code>
     * &quot;INBOX/path/to/pop3account/<b>Trash</b>&quot;&nbsp;=&gt;&nbsp;&quot;<b>Trash</b>&quot;
     * </code>
     * 
     * @param path The path to strip
     * @param realFullname The real fullname
     * @return The virtual fullname
     */
    public static String stripPathFromFullname(final String path, final String realFullname) {
        if (null == realFullname) {
            return realFullname;
        } else if (path.equals(realFullname)) {
            return MailFolder.DEFAULT_FOLDER_ID;
        } else if (!realFullname.startsWith(path)) {
            return realFullname;
        }
        return realFullname.substring(path.length() + 1);
    }

    /**
     * Gets the (real) UIDs corresponding to specified POP3 UIDLs.
     * 
     * @param uidls The POP3 UIDLs
     * @param accountId The account ID
     * @param session The session (caching the map)
     * @return The (real) UIDs corresponding to specified POP3 UIDLs
     * @throws POP3Exception If mapping fails
     */
    @SuppressWarnings("unchecked")
    public static String[] getRealIDs(final String[] uidls, final String fullname, final int accountId, final Session session) throws POP3Exception {
        Map<String, String> m = (Map<String, String>) session.getParameter(PropertyNames.PROP_UIDL_MAP);
        if (null == m) {
            // Initialize map
            initIDs(accountId, session);
            m = (Map<String, String>) session.getParameter(PropertyNames.PROP_UIDL_MAP);
        }
        final String[] real = new String[uidls.length];
        for (int i = 0; i < real.length; i++) {
            real[i] = m.get(uidls[i]);
        }
        return real;
    }

    /**
     * Gets the (real) UID corresponding to specified POP3 UIDL.
     * 
     * @param uidls The POP3 UIDL
     * @param accountId The account ID
     * @param session The session (caching the map)
     * @return The (real) UID corresponding to specified POP3 UIDL
     * @throws POP3Exception If mapping fails
     */
    @SuppressWarnings("unchecked")
    public static String getRealID(final String uidl, final int accountId, final Session session) throws POP3Exception {
        Map<String, String> m = (Map<String, String>) session.getParameter(PropertyNames.PROP_UIDL_MAP);
        if (null == m) {
            // Initialize map
            initIDs(accountId, session);
            m = (Map<String, String>) session.getParameter(PropertyNames.PROP_UIDL_MAP);
        }
        return m.get(uidl);
    }

    /**
     * Gets the POP3 UIDLs corresponding to specified (real) UIDs.
     * 
     * @param realIDs The (real) UIDs
     * @param accountId The account ID
     * @param session The session (caching the map)
     * @return The POP3 UIDLs corresponding to specified (real) UIDs.
     * @throws POP3Exception If mapping fails
     */
    @SuppressWarnings("unchecked")
    public static String[] getUIDLs(final String[] realIDs, final int accountId, final Session session) throws POP3Exception {
        Map<String, String> m = (Map<String, String>) session.getParameter(PropertyNames.PROP_ID_MAP);
        if (null == m) {
            // Initialize map
            initIDs(accountId, session);
            m = (Map<String, String>) session.getParameter(PropertyNames.PROP_ID_MAP);
        }
        final String[] uidls = new String[realIDs.length];
        for (int i = 0; i < uidls.length; i++) {
            uidls[i] = m.get(realIDs[i]);
        }
        return uidls;
    }

    /**
     * Gets the POP3 UIDL corresponding to specified (real) UID.
     * 
     * @param realIDs The (real) UID
     * @param accountId The account ID
     * @param session The session (caching the map)
     * @return The POP3 UIDLs corresponding to specified (real) UID.
     * @throws POP3Exception If mapping fails
     */
    @SuppressWarnings("unchecked")
    public static String getUIDL(final String realID, final int accountId, final Session session) throws POP3Exception {
        Map<String, String> m = (Map<String, String>) session.getParameter(PropertyNames.PROP_ID_MAP);
        if (null == m) {
            // Initialize map
            initIDs(accountId, session);
            m = (Map<String, String>) session.getParameter(PropertyNames.PROP_ID_MAP);
        }
        return m.get(realID);
    }

    private static final String SQL_INSERT_UIDLS = "INSERT INTO pop3_storage_ids (cid, user, id, uidl, fullname, uid) VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * Adds specified mappings.
     * 
     * @param uidls The new POP3 UIDLs
     * @param realIDs The new (real) IDs
     * @param fullname The fullname
     * @param accountId The account ID
     * @param session The session (caching the map)
     * @throws POP3Exception If adding mappings fails
     */
    public static void addMappings(final String[] uidls, final String[] realIDs, final String fullname, final int accountId, final Session session) throws POP3Exception {
        final int cid = session.getContextId();
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_INSERT_UIDLS);
            for (int i = 0; i < realIDs.length; i++) {
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, session.getUserId());
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, uidls[i]);
                stmt.setString(pos++, fullname);
                stmt.setString(pos++, realIDs[i]);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
        // Reload cached maps
        initIDs(fullname, accountId, session);
    }

    private static final String SQL_SELECT_UIDLS = "SELECT uidl, uid FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND fullname = ?";

    private static void initIDs(final String fullname, final int accountId, final Session session) throws POP3Exception {
        final int cid = session.getContextId();
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
        }
        final Map<String, String> m1 = new HashMap<String, String>();
        final Map<String, String> m2 = new HashMap<String, String>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_UIDLS);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, session.getUserId());
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, fullname);
            rs = stmt.executeQuery();
            while (rs.next()) {
                final String uidl = rs.getString(1);
                final String uid = rs.getString(2);
                m1.put(uidl, uid);
                m2.put(uid, uidl);
            }
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
        final d
        session.setParameter("pop3.uidlmap", m1);
        session.setParameter("pop3.idmap", m2);
    }

}
