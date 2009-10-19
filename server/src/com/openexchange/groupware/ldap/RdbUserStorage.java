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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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

    private static final String SELECT_ALL_USER = "SELECT id,userPassword,mailEnabled,imapLogin,imapLogin,smtpServer,mailDomain," +
        "shadowLastChange,mail,timeZone,preferredLanguage,passwordMech,contactId FROM user WHERE user.cid=?";

    private static final String SELECT_USER = SELECT_ALL_USER + " AND id IN (";

    private static final String SELECT_ATTRS = "SELECT id,name,value FROM user_attribute WHERE cid=? AND id IN (";

    private static final String SELECT_CONTACT = "SELECT intfield01,field03,field02,field01 FROM prg_contacts WHERE cid=? AND intfield01 IN (";

    private static final String SELECT_ID = "SELECT id FROM login2user WHERE cid=? AND uid=?";

    private static final String SELECT_LOGIN = "SELECT id,uid FROM login2user where cid=? AND id IN (";

    private static final String SELECT_IMAPLOGIN = "SELECT id FROM user WHERE cid=? AND imapLogin=?";

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

    @Override
    public User getUser(final int userId, final Context context) throws LdapException {
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            return getUser(context, con, new int[] { userId })[0];
        } catch (UserException e) {
            throw new LdapException(e);
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    private User[] getUser(Context ctx, Connection con, int[] userIds) throws UserException {
        if (0 == userIds.length) {
            return new User[0];
        }
        Map<Integer, UserImpl> users = new HashMap<Integer, UserImpl>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getIN(SELECT_USER, userIds.length));
            int pos = 1;
            stmt.setLong(pos++, ctx.getContextId());
            for (int userId : userIds) {
                stmt.setInt(pos++, userId);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                UserImpl user = new UserImpl();
                pos = 1;
                user.setId(result.getInt(pos++));
                user.setUserPassword(result.getString(pos++));
                user.setMailEnabled(result.getBoolean(pos++));
                user.setImapServer(result.getString(pos++));
                user.setImapLogin(result.getString(pos++));
                user.setSmtpServer(result.getString(pos++));
                user.setMailDomain(result.getString(pos++));
                user.setShadowLastChange(result.getInt(pos++));
                if (result.wasNull()) {
                    user.setShadowLastChange(-1);
                }
                user.setMail(result.getString(pos++));
                user.setTimeZone(result.getString(pos++));
                user.setPreferredLanguage(result.getString(pos++));
                user.setPasswordMech(result.getString(pos++));
                user.setContactId(result.getInt(pos++));
                users.put(I(user.getId()), user);
            }
        } catch (SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (int userId : userIds) {
            if (!users.containsKey(I(userId))) {
                throw new UserException(UserException.Code.USER_NOT_FOUND, I(userId), I(ctx.getContextId()));
            }
        }
        loadLoginInfo(ctx, con, users);
        loadContact(ctx, con, users);
        loadGroups(ctx, con, users);
        loadAttributes(ctx, con, users);
        return users.values().toArray(new UserImpl[users.size()]);
    }

    @Override
    public User[] getUser(Context ctx) throws UserException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new UserException(e);
        }
        try {
            return getUser(ctx, con, listAllUser(ctx, con));
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    @Override
    public User[] getUser(Context ctx, int[] userIds) throws UserException {
        if (0 == userIds.length) {
            return new User[0];
        }
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new UserException(e);
        }
        try {
            return getUser(ctx, con, userIds);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    private void loadLoginInfo(Context context, Connection con, Map<Integer, UserImpl> users) throws UserException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            String sql = getIN(SELECT_LOGIN, users.size());
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            for (Integer userId : users.keySet()) {
                stmt.setInt(pos++, userId.intValue());
            }
            result = stmt.executeQuery();
            while (result.next()) {
                users.get(I(result.getInt(1))).setLoginInfo(result.getString(2));
            }
        } catch (SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void loadContact(Context ctx, Connection con, Map<Integer, UserImpl> users) throws UserException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getIN(SELECT_CONTACT, users.size()));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            Map<Integer, UserImpl> userByContactId = new HashMap<Integer, UserImpl>(users.size(), 1);
            for (UserImpl user : users.values()) {
                stmt.setInt(pos++, user.getContactId());
                userByContactId.put(I(user.getContactId()), user);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                pos = 1;
                UserImpl user = userByContactId.get(I(result.getInt(pos++)));
                user.setGivenName(result.getString(pos++));
                user.setSurname(result.getString(pos++));
                user.setDisplayName(result.getString(pos++));
            }
        } catch (SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void loadGroups(Context context, Connection con, Map<Integer, UserImpl> users) throws UserException {
        Map<Integer, List<Integer>> tmp = new HashMap<Integer, List<Integer>>(users.size(), 1);
        for (User user : users.values()) {
            List<Integer> userGroups = new ArrayList<Integer>();
            userGroups.add(I(0));
            tmp.put(I(user.getId()), userGroups);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            String sql = getIN("SELECT member,id FROM groups_member WHERE cid=? AND member IN (", users.size());
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setLong(pos++, context.getContextId());
            for (User user : users.values()) {
                stmt.setInt(pos++, user.getId());
            }
            result = stmt.executeQuery();
            while (result.next()) {
                tmp.get(I(result.getInt(1))).add(I(result.getInt(2)));
            }
        } catch (SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (UserImpl user : users.values()) {
            user.setGroups(I2i(tmp.get(I(user.getId()))));
        }
    }
    
    private static final String STR_ALIAS = "alias";
    
    private void loadAttributes(Context context, Connection con, Map<Integer, UserImpl> users) throws UserException {
        Map<Integer, Map<String, Set<String>>> usersAttrs = new HashMap<Integer, Map<String, Set<String>>>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getIN(SELECT_ATTRS, users.size()));
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            for (User user : users.values()) {
                stmt.setInt(pos++, user.getId());
                usersAttrs.put(I(user.getId()), new HashMap<String, Set<String>>());
            }
            result = stmt.executeQuery();
            /*
             * Gather attributes
             */
            while (result.next()) {
                Map<String, Set<String>> attrs = usersAttrs.get(I(result.getInt(1)));
                final String name = result.getString(2);
                Set<String> set = attrs.get(name);
                if (null == set) {
                    set = new HashSet<String>();
                    attrs.put(name, set);
                }
                set.add(result.getString(3));
            }
        } catch (SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (UserImpl user : users.values()) {
            Map<String, Set<String>> attrs = usersAttrs.get(I(user.getId()));
            /*
             * Check for aliases
             */
            {
                Set<String> aliases = attrs.get(STR_ALIAS);
                if (aliases == null) {
                    user.setAliases(new String[0]);
                } else {
                    user.setAliases(aliases.toArray(new String[aliases.size()]));
                }
            }
            /*
             * Apply attributes
             */
            for (Map.Entry<String, Set<String>> entry : attrs.entrySet()) {
                entry.setValue(Collections.unmodifiableSet(entry.getValue()));
            }
            user.setAttributes(Collections.unmodifiableMap(attrs));
        }
    }

    @Override
    public void updateUser(final User user, final Context context) throws LdapException {
        final Connection con;
        try {
            con = DBPool.pickupWriteable(context);
        } catch (final Exception e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            final int contextId = context.getContextId();
            final int id = user.getId();
            /*
             * Update time zone and language
             */
            PreparedStatement stmt = null;
            try {
                final String sql = "UPDATE user SET " + TIMEZONE + "=?," + LANGUAGE
                    + "=? WHERE cid=? AND id=?";
                stmt = con.prepareStatement(sql);
                int pos = 1;
                stmt.setString(pos++, user.getTimeZone());
                stmt.setString(pos++, user.getPreferredLanguage());
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, id);
                stmt.execute();
            } catch (final SQLException e) {
                throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e,
                    e.getMessage());
            } finally {
                closeSQLStuff(null, stmt);
            }
            /*
             * Check if attributes are set
             */
            final Map<String, Set<String>> attributes = user.getAttributes();
            if (null != attributes) {
                /*
                 * Update attributes
                 */
                try {
                    /*
                     * Clear all attributes
                     */
                    stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND " + IDENTIFIER + "=?");
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, id);
                    stmt.executeUpdate();
                    closeSQLStuff(null, stmt);
                    /*
                     * Insert new ones
                     */
                    if (!attributes.isEmpty()) {
                        /*
                         * Batch update them
                         */
                        stmt = con.prepareStatement("INSERT INTO user_attribute (cid, " + IDENTIFIER + ", name, value) VALUES (?, ?, ?, ?)");
                        for (final Entry<String, Set<String>> entry : attributes.entrySet()) {
                            final String name = entry.getKey();
                            for (final String value : entry.getValue()) {
                                pos = 1;
                                stmt.setInt(pos++, contextId);
                                stmt.setInt(pos++, id);
                                stmt.setString(pos++, name);
                                stmt.setString(pos++, value);
                                stmt.addBatch();
                            }
                        }
                        stmt.executeBatch();
                    }
                } catch (final SQLException e) {
                    throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e,
                        e.getMessage());
                } finally {
                    closeSQLStuff(null, stmt);
                }
            }
        } finally {
            DBPool.closeWriterSilent(context, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User searchUser(String email, Context context) throws LdapException {
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
                    sql = "SELECT id FROM user_attribute WHERE cid=? AND name=? AND value=?";
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
                return getUser(context, con, new int[] { userId })[0];
            } catch (SQLException e) {
                throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
            }
        } catch (UserException e) {
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
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw new UserException(UserException.Code.NO_CONNECTION, e);
        }
        try {
            return listAllUser(context, con);
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    private int[] listAllUser(Context ctx, Connection con) throws UserException {
        final int[] users;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user WHERE user.cid=?");
            stmt.setInt(1, ctx.getContextId());
            result = stmt.executeQuery();
            List<Integer> tmp = new ArrayList<Integer>();
            while (result.next()) {
                tmp.add(I(result.getInt(1)));
            }
            users = new int[tmp.size()];
            for (int i = 0; i < users.length; i++) {
                users[i] = tmp.get(i).intValue();
            }
        } catch (SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
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
