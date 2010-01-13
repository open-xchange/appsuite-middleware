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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.preferences;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * Interface for accessing configuration settings.
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
            if (null == value) {
                pstmt.setNull(1, Types.BOOLEAN);
            } else {
                pstmt.setBoolean(1, value.booleanValue());
            }
        }

    };

    private static final Attribute<Integer> CONTACT_COLLECT_FOLDER = new Attribute<Integer>() {

        public Integer getAttribute(final ResultSet rs) throws SQLException {
            int retval = rs.getInt(getColumnName());
            return rs.wasNull() ? null : Integer.valueOf(retval);
        }

        public String getColumnName() {
            return "contact_collect_folder";
        }

        public void setAttribute(final PreparedStatement pstmt, final Integer value) throws SQLException {
            if (null == value) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, value.intValue());
            }
        }

    };

    private static final Attribute<Boolean> CONTACT_COLLECT_ON_MAIL_ACCESS = new Attribute<Boolean>() {

        public Boolean getAttribute(final ResultSet rs) throws SQLException {
            return Boolean.valueOf(rs.getBoolean(getColumnName()));
        }

        public String getColumnName() {
            return "contactCollectOnMailAccess";
        }

        public void setAttribute(final PreparedStatement pstmt, final Boolean value) throws SQLException {
            if (value == null) {
                pstmt.setBoolean(1, true);
            } else {
                pstmt.setBoolean(1, value.booleanValue());
            }
        }

    };

    private static final Attribute<Boolean> CONTACT_COLLECT_ON_MAIL_TRANSPORT = new Attribute<Boolean>() {

        public Boolean getAttribute(final ResultSet rs) throws SQLException {
            return Boolean.valueOf(rs.getBoolean(getColumnName()));
        }

        public String getColumnName() {
            return "contactCollectOnMailTransport";
        }

        public void setAttribute(final PreparedStatement pstmt, final Boolean value) throws SQLException {
            if (value == null) {
                pstmt.setBoolean(1, true);
            } else {
                pstmt.setBoolean(1, value.booleanValue());
            }
        }

    };

    private static final Attribute<Integer> DEFAULT_STATUS_PRIVATE = new Attribute<Integer>() {

        public Integer getAttribute(final ResultSet rs) throws SQLException {
            return Integer.valueOf(rs.getInt(getColumnName()));
        }

        public String getColumnName() {
            return "defaultStatusPrivate";
        }

        public void setAttribute(final PreparedStatement pstmt, final Integer value) throws SQLException {
            if (value == null) {
                pstmt.setInt(1, 0);
            } else {
                pstmt.setInt(1, value.intValue());
            }
        }

    };

    private static final Attribute<Integer> DEFAULT_STATUS_PUBLIC = new Attribute<Integer>() {

        public Integer getAttribute(final ResultSet rs) throws SQLException {
            return Integer.valueOf(rs.getInt(getColumnName()));
        }

        public String getColumnName() {
            return "defaultStatusPublic";
        }

        public void setAttribute(final PreparedStatement pstmt, final Integer value) throws SQLException {
            if (value == null) {
                pstmt.setInt(1, 0);
            } else {
                pstmt.setInt(1, value.intValue());
            }
        }

    };

    private static final ServerUserSetting defaultInstance = new ServerUserSetting();

    /**
     * Enables/Disables the collection of contacts triggered by mails.
     * 
     * @param cid context id
     * @param user user id
     */
    public static void setContactColletion(Context ctx, Session session, final int userId, final boolean enabled) throws SettingException {
        defaultInstance.setIContactColletion(ctx, session, userId, enabled);
    }

    /**
     * Gets the flag for contact collection.
     * 
     * @param cid context id
     * @param user user id
     * @return <code>true</code> if mails should be collected, <code>false</code> otherwise
     */
    public static Boolean isContactCollectionEnabled(final int cid, final int user) throws SettingException {
        return defaultInstance.isIContactCollectionEnabled(cid, user);
    }

    /**
     * Sets the folder used to store collected contacts.
     * 
     * @param cid context id
     * @param user user id
     * @param folder folder id
     */
    public static void setContactCollectionFolder(final int cid, final int user, final int folder) throws SettingException {
        defaultInstance.setIContactCollectionFolder(cid, user, Integer.valueOf(folder));
    }

    /**
     * Returns the folder used to store collected contacts.
     * 
     * @param cid context id
     * @param user user id
     * @return folder id or <code>null</code> if none found
     */
    public static Integer getContactCollectionFolder(final int cid, final int user) throws SettingException {
        return defaultInstance.getIContactCollectionFolder(cid, user);
    }

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
     * Enables/Disables the collection of contacts triggered by mails.
     * 
     * @param cid context id
     * @param user user id
     */
    public void setIContactColletion(Context ctx, Session session, final int userId, final boolean enabled) throws SettingException {
        setAttributeInternal(ctx.getContextId(), userId, CONTACT_COLLECT_ENABLED, Boolean.valueOf(enabled), connection);
        if (enabled && getAttributeInternal(ctx.getContextId(), userId, CONTACT_COLLECT_FOLDER, connection) == null) {
            ContactCollectorService contactCollectService = ServerServiceRegistry.getInstance().getService(ContactCollectorService.class);
            User user;
            try {
                user = UserStorage.getInstance().getUser(userId, ctx);
                String folderName = new StringHelper(user.getLocale()).getString(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);
                contactCollectService.createCollectFolder(session, ctx, folderName, connection);
            } catch (LdapException e) {
                throw new SettingException(SettingException.Code.SUBSYSTEM, e);
            } catch (AbstractOXException e) {
                throw new SettingException(SettingException.Code.SUBSYSTEM, e);
            } catch (SQLException e) {
                throw new SettingException(SettingException.Code.SQL_ERROR, e);
            }
        }
    }

    /**
     * Gets the flag for contact collection.
     * 
     * @param cid context id
     * @param user user id
     * @return The value or <code>false</code> if no entry is found.
     */
    public Boolean isIContactCollectionEnabled(final int cid, final int user) throws SettingException {
        final Boolean attribute = getAttributeInternal(cid, user, CONTACT_COLLECT_ENABLED, connection);
        return null == attribute ? Boolean.FALSE : attribute;
    }

    /**
     * Sets the folder used to store collected contacts.
     * 
     * @param cid context id
     * @param user user id
     * @param folder folder id
     */
    public void setIContactCollectionFolder(final int cid, final int user, final Integer folder) throws SettingException {
        setAttributeInternal(cid, user, CONTACT_COLLECT_FOLDER, folder, connection);
    }

    /**
     * Returns the folder used to store collected contacts.
     * 
     * @param cid The context id
     * @param user The user id
     * @return folder id or <code>null</code> if no entry found.
     */
    public Integer getIContactCollectionFolder(final int cid, final int user) throws SettingException {
        return getAttributeInternal(cid, user, CONTACT_COLLECT_FOLDER, connection);
    }

    /**
     * Sets the flag for contact collection on incoming mails.
     * 
     * @param cid The context id
     * @param user The user id
     * @param value The flag to set
     * @throws SettingException If a setting error occurs
     */
    public void setContactCollectOnMailAccess(final int cid, final int user, final boolean value) throws SettingException {
        setAttributeInternal(cid, user, CONTACT_COLLECT_ON_MAIL_ACCESS, Boolean.valueOf(value), connection);
    }

    /**
     * Gets the flag for contact collection on incoming mails. If <code>null</code> default if <code>false</code>.
     * 
     * @param cid The context id
     * @param user The user id
     * @return The flag for contact collection on incoming mails or <code>false</code>
     * @throws SettingException If a setting error occurs
     */
    public Boolean isContactCollectOnMailAccess(final int cid, final int user) throws SettingException {
        final Boolean attribute = getAttributeInternal(cid, user, CONTACT_COLLECT_ON_MAIL_ACCESS, connection);
        return null == attribute ? Boolean.FALSE : attribute;
    }

    /**
     * Sets the flag for contact collection on outgoing mails.
     * 
     * @param cid The context id
     * @param user The user id
     * @param value The flag to set
     * @throws SettingException If a setting error occurs
     */
    public void setContactCollectOnMailTransport(final int cid, final int user, final boolean value) throws SettingException {
        setAttributeInternal(cid, user, CONTACT_COLLECT_ON_MAIL_TRANSPORT, Boolean.valueOf(value), connection);
    }

    /**
     * Gets the flag for contact collection on outgoing mails. If <code>null</code> default if <code>false</code>.
     * 
     * @param cid The context id
     * @param user The user id
     * @return The flag for contact collection on outgoing mails or <code>false</code>
     * @throws SettingException If a setting error occurs
     */
    public Boolean isContactCollectOnMailTransport(final int cid, final int user) throws SettingException {
        final Boolean attribute = getAttributeInternal(cid, user, CONTACT_COLLECT_ON_MAIL_TRANSPORT, connection);
        return null == attribute ? Boolean.FALSE : attribute;
    }

    /**
     * Returns the default confirmation status for private folders. If no value is set this parameter defaults to 0.
     * 
     * @param cid
     * @param user
     * @return
     * @throws SettingException If a setting error occurs
     */
    public Integer getDefaultStatusPrivate(final int cid, final int user) throws SettingException {
        Integer value = getAttributeInternal(cid, user, DEFAULT_STATUS_PRIVATE, connection);
        if (value == null) {
            value = Integer.valueOf(0);
        }
        return value;
    }

    /**
     * Sets the default confirmation status for private folders. <code>null</code> will default to 0.
     * 
     * @param cid
     * @param user
     * @param status
     * @throws SettingException
     */
    public void setDefaultStatusPrivate(final int cid, final int user, final Integer status) throws SettingException {
        setAttributeInternal(cid, user, DEFAULT_STATUS_PRIVATE, status, connection);
    }

    /**
     * Returns the default confirmation status for public folders. If no value is set this parameter defaults to 0.
     * 
     * @param cid
     * @param user
     * @return
     * @throws SettingException
     */
    public Integer getDefaultStatusPublic(final int cid, final int user) throws SettingException {
        Integer value = getAttributeInternal(cid, user, DEFAULT_STATUS_PUBLIC, connection);
        if (value == null) {
            value = Integer.valueOf(0);
        }
        return value;
    }

    /**
     * Sets the default confirmation status for public folders. <code>null</code> will default to 0.
     * 
     * @param cid
     * @param user
     * @param status
     * @throws SettingException
     */
    public void setDefaultStatusPublic(final int cid, final int user, final Integer status) throws SettingException {
        setAttributeInternal(cid, user, DEFAULT_STATUS_PUBLIC, status, connection);
    }

    private static <T> T getAttributeInternal(final int cid, final int user, final Attribute<T> attribute, final Connection connection) throws SettingException {
        final Connection con;
        final boolean closeCon;
        if (connection == null) {
            try {
                con = Database.get(cid, false);
            } catch (final DBPoolingException e) {
                throw new SettingException(e);
            }
            closeCon = true;
        } else {
            con = connection;
            closeCon = false;
        }
        try {
            return getAttribute(cid, user, attribute, con);
        } finally {
            if (closeCon) {
                Database.back(cid, false, con);
            }
        }
    }

    private static <T> void setAttributeInternal(final int cid, final int user, final Attribute<T> attribute, final T value, final Connection connection) throws SettingException {
        final Connection con;
        final boolean closeCon;
        if (connection == null) {
            try {
                con = Database.get(cid, true);
            } catch (final DBPoolingException e) {
                throw new SettingException(e);
            }
            closeCon = true;
        } else {
            con = connection;
            closeCon = false;
        }
        try {
            if (hasEntry(cid, user, con)) {
                updateAttribute(cid, user, attribute, value, con);
            } else {
                insertAttribute(cid, user, attribute, value, con);
            }
        } finally {
            if (closeCon) {
                Database.back(cid, true, con);
            }
        }
    }

    private static <T> T getAttribute(final int cid, final int user, final Attribute<T> attribute, final Connection connection) throws SettingException {
        T retval = null;
        final Connection con;
        final boolean closeCon;
        if (connection == null) {
            // Use a writable connection to ensure most up-to-date value is read
            try {
                con = Database.get(cid, true);
            } catch (final DBPoolingException e) {
                throw new SettingException(e);
            }
            closeCon = true;
        } else {
            con = connection;
            closeCon = false;
        }
        final String select = "SELECT " + attribute.getColumnName() + " FROM user_setting_server WHERE cid=? AND user=?";
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
        } catch (final SQLException e) {
            throw new SettingException(SettingException.Code.SQL_ERROR, e);
        } finally {
            closeSQLStuff(rs, stmt);
            if (closeCon) {
                Database.back(cid, true, con);
            }
        }
        return retval;
    }

    private static <T> void updateAttribute(final int cid, final int user, final Attribute<T> attribute, final T value, final Connection con) throws SettingException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_setting_server SET " + attribute.getColumnName() + "=? WHERE cid=? AND user=?");
            attribute.setAttribute(stmt, value);
            stmt.setInt(2, cid);
            stmt.setInt(3, user);
            stmt.execute();
        } catch (final SQLException e) {
            throw new SettingException(SettingException.Code.SQL_ERROR, e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static <T> void insertAttribute(final int cid, final int user, final Attribute<T> attribute, final T value, final Connection con) throws SettingException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO user_setting_server (" + attribute.getColumnName() + ",cid,user) VALUES (?,?,?)");
            attribute.setAttribute(stmt, value);
            stmt.setInt(2, cid);
            stmt.setInt(3, user);
            stmt.execute();
        } catch (final SQLException e) {
            throw new SettingException(SettingException.Code.SQL_ERROR, e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    public static void deleteEntry(final int cid, final int user, final Connection con) throws SettingException {
        PreparedStatement stmt = null;
        final ResultSet rs = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_setting_server WHERE cid=? AND user=?");
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new SettingException(SettingException.Code.SQL_ERROR, e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static boolean hasEntry(final int cid, final int user, final Connection con) throws SettingException {
        boolean retval = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT user FROM user_setting_server WHERE cid=? AND user=?");
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            retval = rs.next();
        } catch (final SQLException e) {
            throw new SettingException(SettingException.Code.SQL_ERROR, e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
        return retval;
    }

    private static interface Attribute<T> {

        void setAttribute(PreparedStatement pstmt, T value) throws SQLException;

        T getAttribute(ResultSet rs) throws SQLException;

        String getColumnName();
    }

}
