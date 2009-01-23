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

package com.openexchange.server.impl;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * Interface for accessing Configuration settings.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ServerUserSetting {

    private static final Attribute<Boolean> CONTACT_COLLECT_ENABLED = new Attribute<Boolean>() {

        public Boolean getAttribute(final ResultSet rs) throws SQLException {
            return Boolean.valueOf(rs.getBoolean(getColumnName()));
        }

        public String getColumnName() {
            return "contact_collect_enabled";
        }

        public void setAttribute(final PreparedStatement pstmt, final Boolean value) throws SQLException {
            pstmt.setBoolean(1, value.booleanValue());

        }

    };

    private static final Attribute<Integer> CONTACT_COLLECT_FOLDER = new Attribute<Integer>() {

        public Integer getAttribute(final ResultSet rs) throws SQLException {
            return Integer.valueOf(rs.getInt(getColumnName()));
        }

        public String getColumnName() {
            return "contact_collect_folder";
        }

        public void setAttribute(final PreparedStatement pstmt, final Integer value) throws SQLException {
            pstmt.setInt(1, value.intValue());

        }

    };

    /**
     * Enables/Disables the collection of Contacts triggered by mails.
     * 
     * @param cid context id
     * @param user user id
     */
    public static void setContactColletion(final int cid, final int user, final boolean enabled) {
        defaultInstance.setIContactColletion(cid, user, enabled);
    }

    /**
     * Gets the flag for Contact collection.
     * 
     * @param cid context id
     * @param user user id
     * @return true if mails should be collected, false otherwise
     */
    public static boolean isContactCollectionEnabled(final int cid, final int user) {
        return defaultInstance.isIContactCollectionEnabled(cid, user);
    }

    /**
     * Sets the folder used to store collected Contacts.
     * 
     * @param cid context id
     * @param user user id
     * @param folder folder id
     */
    public static void setContactCollectionFolder(final int cid, final int user, final int folder) {
        defaultInstance.setIContactCollectionFolder(cid, user, folder);
    }

    /**
     * Returns the folder used to store collected Contacts.
     * 
     * @param cid context id
     * @param user user id
     * @return folder id or <code>0</code> if none found
     */
    public static int getContactCollectionFolder(final int cid, final int user) {
        return defaultInstance.getIContactCollectionFolder(cid, user);
    }

    private static final Log LOG = LogFactory.getLog(ServerUserSetting.class);

    private static final ServerUserSetting defaultInstance = new ServerUserSetting();

    /**
     * Gets the default instance.
     * 
     * @return The default instance.
     */
    public static ServerUserSetting getDefaultInstance() {
        return defaultInstance;
    }

    /**
     * Gets the instance using specified connection.
     * 
     * @param connection The connection to use.
     * @return The instance using specified connection.
     */
    public static ServerUserSetting getInstance(final Connection connection) {
        return new ServerUserSetting(connection);
    }

    /*-
     * ################### Member fields & methods ###################
     */

    private final Connection connection;

    /**
     * Initializes a new {@link ServerUserSetting}.
     */
    private ServerUserSetting() {
        this(null);
    }

    /**
     * Initializes a new {@link ServerUserSetting}.
     * 
     * @param connection The connection to use.
     */
    private ServerUserSetting(final Connection connection) {
        super();
        this.connection = connection;
    }

    /**
     * Enables/Disables the collection of Contacts triggered by mails.
     * 
     * @param cid context id
     * @param user user id
     */
    public void setIContactColletion(final int cid, final int user, final boolean enabled) {
        try {
            if (getAttributeWithoutException(cid, user, CONTACT_COLLECT_FOLDER, connection) == null) {
                final int defaultFolder = new OXFolderAccess(ContextStorage.getStorageContext(cid)).getDefaultFolder(
                    user,
                    FolderObject.CONTACT).getObjectID();
                setAttributeWithoutException(cid, user, CONTACT_COLLECT_FOLDER, Integer.valueOf(defaultFolder), connection);
            }
            setAttributeWithoutException(cid, user, CONTACT_COLLECT_ENABLED, Boolean.valueOf(enabled), connection);
        } catch (final ContextException e) {
            LOG.debug("Error during Context creation.", e);
        } catch (final OXException e) {
            LOG.debug("Error during folder creation.", e);
        }
    }

    /**
     * Gets the flag for Contact collection.
     * 
     * @param cid context id
     * @param user user id
     * @return true if mails should be collected, false otherwise
     */
    public boolean isIContactCollectionEnabled(final int cid, final int user) {
        boolean retval = false;
        final Boolean temp = getAttributeWithoutException(cid, user, CONTACT_COLLECT_ENABLED, connection);
        if (temp != null) {
            retval = temp.booleanValue();
        }
        return retval;
    }

    /**
     * Sets the folder used to store collected Contacts.
     * 
     * @param cid context id
     * @param user user id
     * @param folder folder id
     */
    public void setIContactCollectionFolder(final int cid, final int user, final int folder) {
        if (getAttributeWithoutException(cid, user, CONTACT_COLLECT_ENABLED, connection) == null) {
            setAttributeWithoutException(cid, user, CONTACT_COLLECT_ENABLED, Boolean.FALSE, connection);
        }
        setAttributeWithoutException(cid, user, CONTACT_COLLECT_FOLDER, Integer.valueOf(folder), connection);
    }

    /**
     * Returns the folder used to store collected Contacts.
     * 
     * @param cid context id
     * @param user user id
     * @return folder id or <code>0</code> if none found
     */
    public int getIContactCollectionFolder(final int cid, final int user) {
        final Integer retval = getAttributeWithoutException(cid, user, CONTACT_COLLECT_FOLDER, connection);
        if (retval == null) {
            return 0;
        }
        return retval.intValue();
    }

    /*-
     * ################### Static helpers ###################
     */

    private static <T> T getAttributeWithoutException(final int cid, final int user, final Attribute<T> attribute, final Connection connection) {
        try {
            return getAttribute(cid, user, attribute, connection);
        } catch (final DBPoolingException e) {
            LOG.debug("Can not retrieve Connection", e);
        } catch (final SQLException e) {
            LOG.debug("SQL Exception occurred", new ContactException(Category.CODE_ERROR, -1, "SQL Exception occurred", e));
        }
        return null;
    }

    private static <T> void setAttributeWithoutException(final int cid, final int user, final Attribute<T> attribute, final T value, final Connection connection) {
        try {
            if (hasEntry(cid, user, connection)) {
                updateAttribute(cid, user, attribute, value, connection);
            } else {
                setAttribute(cid, user, attribute, value, connection);
            }
        } catch (final DBPoolingException e) {
            LOG.debug("Can not retrieve Connection", e);
        } catch (final SQLException e) {
            LOG.debug("SQL Exception occurred", new ContactException(Category.CODE_ERROR, -1, "SQL Exception occurred", e));
        }
    }

    private static <T> T getAttribute(final int cid, final int user, final Attribute<T> attribute, final Connection connection) throws DBPoolingException, SQLException {
        T retval = null;
        final Connection con;
        final boolean closeCon;
        if (connection == null) {
            con = Database.get(cid, false);
            closeCon = true;
        } else {
            con = connection;
            closeCon = false;
        }

        final String select = "SELECT " + attribute.getColumnName() + " FROM user_setting_server WHERE cid = ? AND user = ?";

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(select);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            if (rs.next()) {
                retval = attribute.getAttribute(rs);
            }
        } finally {
            closeSQLStuff(rs, stmt);
            if (closeCon) {
                Database.back(cid, false, con);
            }
        }

        return retval;
    }

    private static <T> void updateAttribute(final int cid, final int user, final Attribute<T> attribute, final T value, final Connection connection) throws DBPoolingException, SQLException {
        final Connection con;
        final boolean closeCon;
        if (connection == null) {
            con = Database.get(cid, true);
            closeCon = true;
        } else {
            con = connection;
            closeCon = false;
        }

        final String update = "UPDATE user_setting_server SET " + attribute.getColumnName() + " = ? WHERE cid = ? AND user = ?";

        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(update);
            attribute.setAttribute(stmt, value);
            stmt.setInt(2, cid);
            stmt.setInt(3, user);
            stmt.execute();
        } finally {
            stmt.close();
            if (closeCon) {
                Database.back(cid, true, con);
            }
        }
    }

    private static <T> void setAttribute(final int cid, final int user, final Attribute<T> attribute, final T value, final Connection connection) throws DBPoolingException, SQLException {
        final Connection con;
        final boolean closeCon;
        if (connection == null) {
            con = Database.get(cid, true);
            closeCon = true;
        } else {
            con = connection;
            closeCon = false;
        }

        final String insert = "INSERT INTO user_setting_server (" + attribute.getColumnName() + ", cid, user) VALUES (?, ?, ?)";

        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(insert);
            attribute.setAttribute(stmt, value);
            stmt.setInt(2, cid);
            stmt.setInt(3, user);
            stmt.execute();
        } finally {
            stmt.close();
            if (closeCon) {
                Database.back(cid, true, con);
            }
        }
    }

    private static boolean hasEntry(final int cid, final int user, final Connection connection) throws DBPoolingException, SQLException {
        boolean retval = false;
        final Connection con;
        final boolean closeCon;
        if (connection == null) {
            con = Database.get(cid, false);
            closeCon = true;
        } else {
            con = connection;
            closeCon = false;
        }

        final String select = "SELECT * FROM user_setting_server WHERE cid = ? AND user = ?";

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(select);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            retval = rs.next();
        } finally {
            closeSQLStuff(rs, stmt);
            if (closeCon) {
                Database.back(cid, false, con);
            }
        }

        return retval;
    }

    private interface Attribute<T> {

        void setAttribute(PreparedStatement pstmt, T value) throws SQLException;

        T getAttribute(ResultSet rs) throws SQLException;

        String getColumnName();
    }

}
