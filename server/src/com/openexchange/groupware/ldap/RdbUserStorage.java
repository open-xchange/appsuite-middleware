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

package com.openexchange.groupware.ldap;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException.Code;

/**
 * This class implements the user storage using a relational database instead
 * of a directory service.
 */
public class RdbUserStorage extends UserStorage {

    /**
     * SQL statement for loading a user.
     */
    private static final String SELECT_USER = "SELECT " + USERPASSWORD + ','
        + MAILENABLED + ',' + IMAPSERVER + ",imapLogin," + SMTPSERVER + ','
        + MAILDOMAIN + ',' + SHADOWLASTCHANGE + ',' + MAIL + ',' +  TIMEZONE
        + ',' + LANGUAGE + ",passwordMech,contactId FROM user WHERE user.cid=? "
        + "AND " + IDENTIFIER + "=?";

    /**
     * SQL statement for selecting aliases for a user.
     */
    private static final String SELECT_ALIAS = "SELECT value FROM "
        + "user_attribute WHERE cid=? AND " + IDENTIFIER + "=? AND name=?";

    /**
     * SQL statement for loading the contact data of a user.
     */
    private static final String SELECT_CONTACT = "SELECT " + GIVENNAME + ','
        + SURENAME + ',' + DISPLAYNAME + " FROM prg_contacts WHERE cid=? "
        + "AND intfield01=?";

    /**
     * SQL statement for resolving the identifier of a user.
     */
    private static final String SELECT_ID = "SELECT " + IDENTIFIER
        + " FROM login2user WHERE cid=? AND " + UID + "=?";

    /**
     * SQL statement for reading the login info for a user.
     */
    private static final String SELECT_LOGIN = "SELECT " + UID
        + " FROM login2user where cid=? AND " + IDENTIFIER + "=?";

    /**
     * SQL statement for resolving an imap login to a user.
     */
    private static final String SELECT_IMAPLOGIN = "SELECT " + IDENTIFIER
        + " FROM user WHERE cid=? AND imapLogin=?";

    /**
     * Default constructor.
     */
    public RdbUserStorage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public int getUserId(final String uid, final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (DBPoolingException e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        int userId = -1;
        try {
            stmt = con.prepareStatement(SELECT_ID);
            stmt.setLong(1, context.getContextId());
            stmt.setString(2, uid);
            result = stmt.executeQuery();
            if (result.next()) {
                userId = result.getInt(1);
            } else {
                throw new LdapException(Component.USER, Code.USER_NOT_FOUND,
                    uid, Integer.valueOf(context.getContextId()));
            }
            if (result.next()) {
                throw new LdapException(Component.USER, Code.USER_CONFLICT,
                    uid, Integer.valueOf(context.getContextId()));
            }
        } catch (SQLException e) {
            throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public User getUser(final int userId, final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (DBPoolingException e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        UserImpl retval = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_USER);
            stmt.setLong(1, context.getContextId());
            stmt.setInt(2, userId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = new UserImpl();
                int pos = 1;
                retval.setId(userId);
                retval.setUserPassword(result.getString(pos++));
                retval.setMailEnabled(result.getBoolean(pos++));
                retval.setImapServer(result.getString(pos++));
                retval.setImapLogin(result.getString(pos++));
                retval.setSmtpServer(result.getString(pos++));
                retval.setMailDomain(result.getString(pos++));
                retval.setShadowLastChange(result.getInt(pos++));
                if (result.wasNull()) {
                    retval.setShadowLastChange(-1);
                }
                retval.setMail(result.getString(pos++));
                retval.setTimeZone(result.getString(pos++));
                retval.setPreferredLanguage(result.getString(pos++));
                retval.setPasswordMech(result.getString(pos++));
                retval.setContactId(result.getInt(pos++));
            } else {
                throw new LdapException(Component.USER,
                    Code.USER_NOT_FOUND, Integer.valueOf(userId), Integer.valueOf(context.getContextId()));
            }
        } catch (SQLException e) {
            throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        loadLoginInfo(retval, context);
        loadContact(retval, context);
        loadGroups(retval, context);
        loadAliases(retval, context);
        return retval;
    }

    /**
     * Reads the login information for a user.
     * @param user User object.
     * @throws LdapException if reading fails.
     */
    private void loadLoginInfo(final UserImpl user, final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (DBPoolingException e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_LOGIN);
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, user.getId());
            result = stmt.executeQuery();
            if (result.next()) {
                user.setLoginInfo(result.getString(1));
            }
        } catch (SQLException e) {
            throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * Reads the contact information for a user.
     * @param user User object.
     * @throws LdapException if reading fails.
     */
    private void loadContact(final UserImpl user, final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (DBPoolingException e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_CONTACT);
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, user.getContactId());
            result = stmt.executeQuery();
            if (result.next()) {
                int pos = 1;
                user.setGivenName(result.getString(pos++));
                user.setSurname(result.getString(pos++));
                user.setDisplayName(result.getString(pos++));
            }
        } catch (SQLException e) {
            throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * Reads the group identifier the user is member of.
     * @param user User object.
     * @throws LdapException if reading fails.
     */
    private void loadGroups(final UserImpl user, final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (DBPoolingException e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final String sql = "SELECT " + GroupStorage.IDENTIFIER
                + " FROM groups_member WHERE cid=? AND "
                + GroupStorage.MEMBER + "=?";
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, context.getContextId());
            stmt.setInt(2, user.getId());
            result = stmt.executeQuery();
            final List<Integer> tmp = new ArrayList<Integer>();
            while (result.next()) {
                tmp.add(Integer.valueOf(result.getInt(1)));
            }
            int[] groups = new int[tmp.size()];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = tmp.get(i).intValue();
            }
            user.setGroups(groups);
        } catch (SQLException e) {
            throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * Reads the aliases of a user.
     * @param user User object.
     * @throws LdapException if reading fails.
     */
    private void loadAliases(final UserImpl user, final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (DBPoolingException e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_ALIAS);
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, user.getId());
            stmt.setString(pos++, "alias");
            result = stmt.executeQuery();
            final List<String> aliases = new ArrayList<String>();
            while (result.next()) {
                aliases.add(result.getString(1));
            }
            user.setAliases(aliases.toArray(new String[aliases.size()]));
        } catch (SQLException e) {
            throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(final User user, final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickupWriteable(context);
        } catch (Exception e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        try {
            final String sql = "UPDATE user SET " + TIMEZONE + "=?," + LANGUAGE
                + "=? WHERE cid=? AND id=?";
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setString(pos++, user.getTimeZone());
            stmt.setString(pos++, user.getPreferredLanguage());
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, user.getId());
            stmt.execute();
        } catch (SQLException e) {
            throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            DBPool.closeWriterSilent(context, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User searchUser(final String email, final Context context) throws LdapException {
        String sql = "SELECT id FROM user WHERE cid=? AND mail=?";
        Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (DBPoolingException e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        try {
            PreparedStatement stmt = null;
            ResultSet result = null;
            int userId = -1;
            try {
                stmt = con.prepareStatement(sql);
                stmt.setInt(1, context.getContextId());
                stmt.setString(2, email);
                result = stmt.executeQuery();
                if (result.next()) {
                    userId = result.getInt(1);
                }
            } catch (SQLException e) {
                throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                    e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
            }
            try {
                if (userId == -1) {
                    sql = "SELECT id FROM user_attribute WHERE cid=? "
                        + "AND name=?  AND value=?";
                    stmt = con.prepareStatement(sql);
                    int pos = 1;
                    stmt.setInt(pos++, context.getContextId());
                    stmt.setString(pos++, ALIAS);
                    stmt.setString(pos++, email);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        userId = result.getInt(1);
                    }
                }
                if (userId == -1) {
                    throw new LdapException(Component.USER,
                        Code.NO_USER_BY_MAIL, email);
                }
                return getUser(userId, context);
            } catch (SQLException e) {
                throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                    e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
            }
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public int[] listModifiedUser(final Date modifiedSince, final Context context)
        throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.USER, Code.NO_CONNECTION, e);
        }
        final String sql = "SELECT id FROM user LEFT JOIN prg_contacts ON "
            + "(user.cid=prg_contacts.cid AND "
            + "user.contactId=prg_contacts.intfield01) "
            + "WHERE cid=? AND " + MODIFYTIMESTAMP + ">=?";
        int[] users;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, context.getContextId());
            stmt.setTimestamp(2, new Timestamp(modifiedSince.getTime()));
            result = stmt.executeQuery();
            final List<Integer> tmp = new ArrayList<Integer>();
            while (result.next()) {
                tmp.add(Integer.valueOf(result.getInt(1)));
            }
            users = new int[tmp.size()];
            for (int i = 0; i < users.length; i++) {
                users[i] = tmp.get(i).intValue();
            }
        } catch (SQLException e) {
            throw new LdapException(Component.USER, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] listAllUser(final Context context) throws UserException {
        final String sql = "SELECT " + IDENTIFIER + " FROM user "
            + "WHERE user.cid=?";
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new UserException(UserException.Code.NO_CONNECTION, e);
        }
        final int[] users;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, context.getContextId());
            result = stmt.executeQuery();
            final List<Integer> tmp = new ArrayList<Integer>();
            while (result.next()) {
                tmp.add(Integer.valueOf(result.getInt(1)));
            }
            users = new int[tmp.size()];
            for (int i = 0; i < users.length; i++) {
                users[i] = tmp.get(i).intValue();
            }
        } catch (SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e
                .getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int resolveIMAPLogin(final String imapLogin, final Context context) throws UserException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new UserException(UserException.Code.NO_CONNECTION, e);
        }
        final int user;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_IMAPLOGIN);
            final int cid = context.getContextId();
            stmt.setInt(1, cid);
            stmt.setString(2, imapLogin);
            result = stmt.executeQuery();
            if (result.next()) {
                user = result.getInt(1);
            } else {
                throw new UserException(UserException.Code.USER_NOT_FOUND,
                    imapLogin, Integer.valueOf(cid));
            }
            if (result.next()) {
                throw new UserException(UserException.Code.USER_CONFLICT,
                    imapLogin, Integer.valueOf(cid));
            }
        } catch (SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e
                .getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return user;
    }
}
