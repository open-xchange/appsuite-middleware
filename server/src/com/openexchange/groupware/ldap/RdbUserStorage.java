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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException.Code;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.Collections.SmartIntArray;

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
    //private static final String SELECT_ALIAS = "SELECT value FROM "
    //    + "user_attribute WHERE cid=? AND " + IDENTIFIER + "=? AND name=?";

    /**
     * SQL statement for selecting attributes for a user.
     */
    private static final String SELECT_ATTRS = "SELECT name, value FROM "
        + "user_attribute WHERE cid=? AND " + IDENTIFIER + "=?";

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
        } catch (final DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
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
                throw new LdapException(EnumComponent.USER, Code.USER_NOT_FOUND,
                    uid, Integer.valueOf(context.getContextId()));
            }
            if (result.next()) {
                throw new LdapException(EnumComponent.USER, Code.USER_CONFLICT,
                    uid, Integer.valueOf(context.getContextId()));
            }
        } catch (final SQLException e) {
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e,
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
        } catch (final DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            return getUser(context, con, userId);
        } catch (final UserException e) {
            throw new LdapException(e);
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    private User getUser(final Context ctx, final Connection con,
        final int userId) throws UserException {
        UserImpl retval = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_USER);
            stmt.setLong(1, ctx.getContextId());
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
                throw new UserException(UserException.Code.USER_NOT_FOUND,
                    Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()));
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        loadLoginInfo(ctx, con, retval);
        loadContact(ctx, con, retval);
        loadGroups(ctx, con, retval);
        loadAliases(ctx, con, retval);
        return retval;
    }
    
    /**
     * Reads the login information for a user.
     * @param context context.
     * @param con readable database connection.
     * @param user User object.
     * @throws UserException if some problem occurs.
     */
    private void loadLoginInfo(final Context context, Connection con,
        final UserImpl user) throws UserException {
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
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * Reads the contact information for a user.
     * @param context context.
     * @param con readable database connection.
     * @param user User object.
     * @throws UserException if reading contact fails.
     */
    private void loadContact(final Context context, final Connection con,
        final UserImpl user) throws UserException {
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
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * Reads the group identifier the user is member of.
     * @param context context.
     * @param con readable database connection.
     * @param user User object.
     * @throws UserException if loading groups failed.
     */
    private void loadGroups(final Context context, final Connection con,
        final UserImpl user) throws UserException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final String sql = "SELECT id FROM groups_member WHERE cid=? AND "
                + "member=?";
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, context.getContextId());
            stmt.setInt(2, user.getId());
            result = stmt.executeQuery();
            final List<Integer> tmp = new ArrayList<Integer>();
            while (result.next()) {
                tmp.add(Integer.valueOf(result.getInt(1)));
            }
            final int[] groups = new int[tmp.size()];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = tmp.get(i).intValue();
            }
            user.setGroups(groups);
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }
    
    private static final String STR_ALIAS = "alias";
    
    /**
     * Reads the attributes/aliases of a user.
     * @param context The context
     * @param con readable database connection.
     * @param user User object.
     * @throws UserException if loading the aliases fails.
     */
    private void loadAliases(final Context context, final Connection con,
        final UserImpl user) throws UserException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_ATTRS);
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, user.getId());
            result = stmt.executeQuery();
            final Map<String, Set<String>> attrs = new HashMap<String, Set<String>>();
            /*
             * Gather attributes
             */
            while (result.next()) {
                final String name = result.getString(1);
                Set<String> set = attrs.get(name);
                if (null == set) {
                    set = new HashSet<String>();
                    attrs.put(name, set);
                }
                final String value = result.getString(2);
                set.add(value);
            }
            /*
             * Check for aliases
             */
            {
                final Set<String> aliases = attrs.get(STR_ALIAS);
                if (aliases == null) {
                    user.setAliases(new String[0]);
                } else {
                    user.setAliases(aliases.toArray(new String[aliases.size()]));
                }
            }
            /*
             * Apply attributes
             */
            final Iterator<Map.Entry<String, Set<String>>> iter = attrs.entrySet().iterator();
            final int size = attrs.size();
            for (int i = 0; i < size; i++) {
                final Map.Entry<String, Set<String>> e = iter.next();
                e.setValue(Collections.unmodifiableSet(e.getValue()));
            }
            user.setAttributes(Collections.unmodifiableMap(attrs));
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
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
        } catch (final Exception e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
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
        } catch (final SQLException e) {
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e,
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
        } catch (final DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
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
            } catch (final SQLException e) {
                throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e,
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
                    throw new LdapException(EnumComponent.USER,
                        Code.NO_USER_BY_MAIL, email);
                }
                return getUser(context, con, userId);
            } catch (final SQLException e) {
                throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e,
                    e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
            }
        } catch (final UserException e) {
            throw new LdapException(e);
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
        } catch (final Exception e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
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
        } catch (final SQLException e) {
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e,
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
    public int[] listAllUser(Context context) throws UserException {
        final String sql = "SELECT " + IDENTIFIER + " FROM user WHERE user.cid=?";
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
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
        } catch (final SQLException e) {
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
    public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws UserException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw new UserException(UserException.Code.NO_CONNECTION, e);
        }
        final int[] users;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_IMAPLOGIN);
            final int cid = context.getContextId();
            stmt.setInt(1, cid);
            stmt.setString(2, imapLogin);
            result = stmt.executeQuery();
            final SmartIntArray sia = new SmartIntArray(4);
            if (result.next()) {
                do {
                    sia.append(result.getInt(1));
                } while (result.next());
            } else {
                throw new UserException(UserException.Code.USER_NOT_FOUND,
                        imapLogin, Integer.valueOf(cid));
            }
            users = sia.toArray();
        } catch (final SQLException e) {
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
    public void invalidateUser(final Context ctx, final int userId) {
        // Nothing to do.
    }

    @Override
    protected void startInternal() throws UserException {
    }

    @Override
    protected void stopInternal() throws UserException {
    }
}
