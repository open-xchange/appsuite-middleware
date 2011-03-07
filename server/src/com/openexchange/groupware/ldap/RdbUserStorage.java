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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DBPoolingException;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException.Code;
import com.openexchange.passwordchange.PasswordMechanism;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.Collections.SmartIntArray;
import com.openexchange.tools.StringCollection;

/**
 * This class implements the user storage using a relational database instead
 * of a directory service.
 */
public class RdbUserStorage extends UserStorage {

    private static final Log LOG = LogFactory.getLog(RdbUserStorage.class);

    private static final String SELECT_ALL_USER = "SELECT id,userPassword,mailEnabled,imapServer,imapLogin,smtpServer,mailDomain," +
        "shadowLastChange,mail,timeZone,preferredLanguage,passwordMech,contactId FROM user WHERE user.cid=?";

    private static final String SELECT_USER = SELECT_ALL_USER + " AND id IN (";

    private static final String SELECT_ATTRS = "SELECT id,name,value FROM user_attribute WHERE cid=? AND id IN (";

    private static final String SELECT_CONTACT = "SELECT intfield01,field03,field02,field01 FROM prg_contacts WHERE cid=? AND intfield01 IN (";

    private static final String SELECT_ID = "SELECT id FROM login2user WHERE cid=? AND uid=?";

    private static final String SELECT_LOGIN = "SELECT id,uid FROM login2user where cid=? AND id IN (";

    private static final String SELECT_IMAPLOGIN = "SELECT id FROM user WHERE cid=? AND imapLogin=?";
    
    private static final String SQL_UPDATE_PASSWORD = "UPDATE user SET userPassword = ?, shadowLastChange = ? WHERE cid = ? AND id = ?";


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
            stmt.setInt(1, context.getContextId());
            stmt.setString(2, uid);
            result = stmt.executeQuery();
            if (result.next()) {
                userId = result.getInt(1);
            } else {
                throw new LdapException(EnumComponent.USER, Code.USER_NOT_FOUND, uid, I(context.getContextId()));
            }
        } catch (final SQLException e) {
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
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
        } catch (final DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            return getUser(context, con, new int[] { userId })[0];
        } catch (final UserException e) {
            throw new LdapException(e);
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public User getUser(Context ctx, int userId, Connection con) throws UserException {
        return getUser(ctx, con, new int[] { userId })[0];
    }

    private static final int LIMIT = 1000;

    private User[] getUser(final Context ctx, final Connection con, final int[] userIds) throws UserException {
        final int length = userIds.length;
        if (0 == length) {
            return new User[0];
        }
        final Map<Integer, UserImpl> users = new HashMap<Integer, UserImpl>(length);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final TIntIntHashMap userMap;
            if (length <= LIMIT) {
                final StringBuilder sb = new StringBuilder(512);
                sb.append("SELECT u.id,u.userPassword,u.mailEnabled,u.imapServer,u.imapLogin,u.smtpServer,u.mailDomain,u.shadowLastChange,u.mail,u.timeZone,u.preferredLanguage,u.passwordMech,u.contactId FROM user AS u");
                if (1 == length) {
                    sb.append(" WHERE u.id = ? AND u.cid = ?");
                } else {
                    sb.append(" INNER JOIN (");
                    sb.append("SELECT ? AS id");
                    for (int i = 1; i < length; i++) {
                        sb.append(" UNION ALL SELECT ?");
                    }
                    sb.append(") AS x ON u.id = x.id WHERE u.cid = ?");
                }
                stmt = con.prepareStatement(sb.toString());
                int pos = 1;
                userMap = new TIntIntHashMap(length, 1);
                for (int index = 0; index < length; index++) {
                    final int userId = userIds[index];
                    stmt.setInt(pos++, userId);
                    userMap.put(userId, index);
                }
                stmt.setInt(pos++, ctx.getContextId());
            } else {
                stmt = con.prepareStatement("SELECT u.id,u.userPassword,u.mailEnabled,u.imapServer,u.imapLogin,u.smtpServer,u.mailDomain,u.shadowLastChange,u.mail,u.timeZone,u.preferredLanguage,u.passwordMech,u.contactId FROM user AS u WHERE u.cid = ?");
                userMap = new TIntIntHashMap(length, 1);
                for (int index = 0; index < length; index++) {
                    userMap.put(userIds[index], index);
                }
                stmt.setInt(1, ctx.getContextId());
            }
            result = stmt.executeQuery();
            int pos;
            while (result.next()) {
                pos = 1;
                final int userId = result.getInt(pos++);
                if (userMap.containsKey(userId)) {
                    final UserImpl user = new UserImpl();
                    user.setId(userId);
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
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.LOAD_FAILED, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (final int userId : userIds) {
            if (!users.containsKey(I(userId))) {
                throw new UserException(UserException.Code.USER_NOT_FOUND, I(userId), I(ctx.getContextId()));
            }
        }
        loadLoginInfo(ctx, con, users);
        loadContact(ctx, con, users);
        loadGroups(ctx, con, users);
        loadAttributes(ctx, con, users);
        User[] retval = new User[users.size()];
        for (int i = 0; i < length; i++) {
            retval[i] = users.get(I(userIds[i]));
        }
        return retval;
    }

    @Override
    public User[] getUser(final Context ctx) throws UserException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (final DBPoolingException e) {
            throw new UserException(e);
        }
        try {
            return getUser(ctx, con, listAllUser(ctx, con));
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    @Override
    public User[] getUser(final Context ctx, final int[] userIds) throws UserException {
        if (0 == userIds.length) {
            return new User[0];
        }
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (final DBPoolingException e) {
            throw new UserException(e);
        }
        try {
            return getUser(ctx, con, userIds);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    private void loadLoginInfo(final Context context, final Connection con, final Map<Integer, UserImpl> users) throws UserException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final String sql = getIN(SELECT_LOGIN, users.size());
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            for (final Integer userId : users.keySet()) {
                stmt.setInt(pos++, userId.intValue());
            }
            result = stmt.executeQuery();
            while (result.next()) {
                users.get(I(result.getInt(1))).setLoginInfo(result.getString(2));
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void loadContact(final Context ctx, final Connection con, final Map<Integer, UserImpl> users) throws UserException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getIN(SELECT_CONTACT, users.size()));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            final Map<Integer, UserImpl> userByContactId = new HashMap<Integer, UserImpl>(users.size(), 1);
            for (final UserImpl user : users.values()) {
                stmt.setInt(pos++, user.getContactId());
                userByContactId.put(I(user.getContactId()), user);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                pos = 1;
                final UserImpl user = userByContactId.get(I(result.getInt(pos++)));
                user.setGivenName(result.getString(pos++));
                user.setSurname(result.getString(pos++));
                user.setDisplayName(result.getString(pos++));
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void loadGroups(final Context context, final Connection con, final Map<Integer, UserImpl> users) throws UserException {
        final Map<Integer, List<Integer>> tmp = new HashMap<Integer, List<Integer>>(users.size(), 1);
        for (final User user : users.values()) {
            final List<Integer> userGroups = new ArrayList<Integer>();
            userGroups.add(I(0));
            tmp.put(I(user.getId()), userGroups);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final String sql = getIN("SELECT member,id FROM groups_member WHERE cid=? AND member IN (", users.size());
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            for (final User user : users.values()) {
                stmt.setInt(pos++, user.getId());
            }
            result = stmt.executeQuery();
            while (result.next()) {
                tmp.get(I(result.getInt(1))).add(I(result.getInt(2)));
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (final UserImpl user : users.values()) {
            user.setGroups(I2i(tmp.get(I(user.getId()))));
        }
    }
    
    private static final String STR_ALIAS = "alias";
    
    private void loadAttributes(final Context context, final Connection con, final Map<Integer, UserImpl> users) throws UserException {
        final Map<Integer, Map<String, Set<String>>> usersAttrs = new HashMap<Integer, Map<String, Set<String>>>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getIN(SELECT_ATTRS, users.size()));
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            for (final User user : users.values()) {
                stmt.setInt(pos++, user.getId());
                usersAttrs.put(I(user.getId()), new HashMap<String, Set<String>>());
            }
            result = stmt.executeQuery();
            /*
             * Gather attributes
             */
            while (result.next()) {
                final Map<String, Set<String>> attrs = usersAttrs.get(I(result.getInt(1)));
                final String name = result.getString(2);
                Set<String> set = attrs.get(name);
                if (null == set) {
                    set = new HashSet<String>();
                    attrs.put(name, set);
                }
                set.add(result.getString(3));
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (final UserImpl user : users.values()) {
            final Map<String, Set<String>> attrs = usersAttrs.get(I(user.getId()));
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
            for (final Map.Entry<String, Set<String>> entry : attrs.entrySet()) {
                entry.setValue(Collections.unmodifiableSet(entry.getValue()));
            }
            user.setAttributes(Collections.unmodifiableMap(attrs));
        }
    }

    @Override
    public void updateUser(final User user, final Context context) throws LdapException {
        int contextId = context.getContextId();
        int userId = user.getId();
        String timeZone = user.getTimeZone();
        String preferredLanguage = user.getPreferredLanguage();
        String password = user.getUserPassword();
        String mech = user.getPasswordMech();
        int shadowLastChanged = user.getShadowLastChange();
        
        final Connection con;
        try {
            con = DBPool.pickupWriteable(context);
            con.setAutoCommit(false);
        } catch (DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        } catch (SQLException e) {
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
        }
        try {
            // Update time zone and language
            if (null != timeZone && null != preferredLanguage) {
                PreparedStatement stmt = null;
                try {
                    String sql = "UPDATE user SET " + TIMEZONE + "=?," + LANGUAGE + "=? WHERE cid=? AND id=?";
                    stmt = con.prepareStatement(sql);
                    int pos = 1;
                    stmt.setString(pos++, timeZone);
                    stmt.setString(pos++, preferredLanguage);
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                    stmt.execute();
                    /*
                     * Drop possible cached locale-sensitive folder data
                     */
                    CacheFolderStorage.dropUserEntries(userId, contextId);
                } finally {
                    closeSQLStuff(stmt);
                }
            }
            if (null != user.getAttributes()) {
                updateAttributes(context, user, con);
            }
            if (null != password && null != mech) {
                String encodedPassword = null;
                PreparedStatement stmt = null;
                try {
                    encodedPassword = PasswordMechanism.getEncodedPassword(mech, password);
                    stmt = con.prepareStatement(SQL_UPDATE_PASSWORD);
                    int pos = 1;
                    stmt.setString(pos++, encodedPassword);
                    stmt.setInt(pos++, shadowLastChanged);
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                    stmt.execute();
                } catch (UnsupportedEncodingException e) {
                    throw new SQLException(e.toString());
                } catch (NoSuchAlgorithmException e) {
                    throw new SQLException(e.toString());
                } finally {
                    closeSQLStuff(stmt);
                }
            }
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
        } catch (Exception e) {
            rollback(con);
            throw new LdapException(EnumComponent.USER, Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            autocommit(con);
            DBPool.closeWriterSilent(context, con);
        }
    }

    @Override
    public void setUserAttribute(final String name, final String value, final int userId, final Context context) throws LdapException {
        final String attrName = new StringBuilder("attr_").append(name).toString();
        setAttribute(attrName, value, userId, context);
    }
    
    @Override
    public void setAttribute(final String name, final String value, final int userId, final Context context) throws LdapException {
        if (null == name) {
            throw new LdapException(EnumComponent.USER, Code.UNEXPECTED_ERROR, "Attribute name is null.");
        }
        final Connection con;
        try {
            con = DBPool.pickupWriteable(context);
            con.setAutoCommit(false);
        } catch (final SQLException e) {
            throw new LdapException(EnumComponent.USER, Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            setAttribute(name, value, context.getContextId(), userId, con);
            
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
        } catch (final Exception e) {
            rollback(con);
            throw new LdapException(EnumComponent.USER, Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            autocommit(con);
            DBPool.closeWriterSilent(context, con);
        }
    }

    private void setAttribute(final String name, final String value, final int contextId, final int userId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND " + IDENTIFIER + "=? and name=?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos, name);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
            stmt = null;
        }
        if (null != value) {
            try {
                stmt = con.prepareStatement("INSERT INTO user_attribute (cid," + IDENTIFIER + ",name,value) VALUES (?,?,?,?)");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, userId);
                stmt.setString(pos++, name);
                stmt.setString(pos, value);
                stmt.executeUpdate();
            } finally {
                closeSQLStuff(stmt);
            }
        }
    }

    @Override
    public String getUserAttribute(final String name, final int userId, final Context context) throws LdapException {
        if (null == name) {
            throw new LdapException(EnumComponent.USER, Code.UNEXPECTED_ERROR, "Attribute name is null.");
        }
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            final String attrName = new StringBuilder("attr_").append(name).toString();
            return getAttribute(attrName, context.getContextId(), userId, con);
        } catch (final SQLException e) {
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
        } catch (final Exception e) {
            throw new LdapException(EnumComponent.USER, Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeWriterSilent(context, con);
        }
    }

    private String getAttribute(final String name, final int contextId, final int userId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid=? AND " + IDENTIFIER + "=? and name=?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos, name);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private void updateAttributes(Context ctx, User user, Connection con) throws SQLException {
        // Update attributes
        int contextId = ctx.getContextId();
        int userId = user.getId();
        Map<String, Set<String>> attributes = user.getAttributes();
        // Clear all attributes
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND " + IDENTIFIER + "=?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
        // Insert new ones
        if (!attributes.isEmpty()) {
            // Batch update them
            stmt = null;
            try {
                stmt = con.prepareStatement("INSERT INTO user_attribute (cid," + IDENTIFIER + ",name,value) VALUES (?,?,?,?)");
                for (final Entry<String, Set<String>> entry : attributes.entrySet()) {
                    for (final String value : entry.getValue()) {
                        int pos = 1;
                        stmt.setInt(pos++, contextId);
                        stmt.setInt(pos++, userId);
                        stmt.setString(pos++, entry.getKey());
                        stmt.setString(pos++, value);
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            } finally {
                closeSQLStuff(stmt);
            }
        } else {
            UserException e = new UserException(UserException.Code.ERASED_ATTRIBUTES, I(contextId), I(userId));
            LOG.warn(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User searchUser(final String email, final Context context) throws LdapException {
        String sql = "SELECT id FROM user WHERE cid=? AND mail LIKE ?";
        Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            final String pattern = StringCollection.prepareForSearch(email, false, true);
            PreparedStatement stmt = null;
            ResultSet result = null;
            int userId = -1;
            try {
                stmt = con.prepareStatement(sql);
                stmt.setInt(1, context.getContextId());
                stmt.setString(2, pattern);
                result = stmt.executeQuery();
                if (result.next()) {
                    userId = result.getInt(1);
                }
            } catch (final SQLException e) {
                throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
            }
            try {
                if (userId == -1) {
                    sql = "SELECT id FROM user_attribute WHERE cid=? AND name=? AND value LIKE ?";
                    stmt = con.prepareStatement(sql);
                    int pos = 1;
                    stmt.setInt(pos++, context.getContextId());
                    stmt.setString(pos++, ALIAS);
                    stmt.setString(pos++, pattern);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        userId = result.getInt(1);
                    }
                }
                if (userId == -1) {
                    throw new LdapException(EnumComponent.USER, Code.NO_USER_BY_MAIL, email);
                }
                return getUser(context, con, new int[] { userId })[0];
            } catch (final SQLException e) {
                throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
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
                tmp.add(I(result.getInt(1)));
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
    public int[] listAllUser(final Context context) throws UserException {
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

    private int[] listAllUser(final Context ctx, final Connection con) throws UserException {
        final int[] users;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user WHERE user.cid=?");
            stmt.setInt(1, ctx.getContextId());
            result = stmt.executeQuery();
            final TIntArrayList tmp = new TIntArrayList();
            while (result.next()) {
                tmp.add(result.getInt(1));
            }
            users = tmp.toNativeArray();
        } catch (final SQLException e) {
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
                        imapLogin, I(cid));
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
    protected void startInternal() {
        // Nothing to set up.
    }

    @Override
    protected void stopInternal() {
        // Nothing to tear down.
    }
}
