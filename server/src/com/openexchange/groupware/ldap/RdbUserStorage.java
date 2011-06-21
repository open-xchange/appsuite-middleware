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
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.sql.DBUtils.IN_LIMIT;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.TIntArrayList;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException.Code;
import com.openexchange.passwordchange.PasswordMechanism;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.Collections.SmartIntArray;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.arrays.Arrays;

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

    private User[] getUser(final Context ctx, final Connection con, final int[] userIds) throws UserException {
        final int length = userIds.length;
        if (0 == length) {
            return new User[0];
        }
        final Map<Integer, UserImpl> users = new HashMap<Integer, UserImpl>(length);
        try {
            for (int i = 0; i < userIds.length; i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    final int[] currentUserIds = Arrays.extract(userIds, i, IN_LIMIT);
                    stmt = con.prepareStatement(getIN(SELECT_USER, currentUserIds.length));
                    int pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
                    for (final int userId : currentUserIds) {
                        stmt.setInt(pos++, userId);
                    }
                    result = stmt.executeQuery();
                    while (result.next()) {
                        final UserImpl user = new UserImpl();
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
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.LOAD_FAILED, e, e.getMessage());
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
        try {
            Iterator<Integer> iter = users.keySet().iterator();
            for (int i = 0; i < users.size(); i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    int length = Arrays.determineRealSize(users.size(), i, IN_LIMIT);
                    stmt = con.prepareStatement(getIN(SELECT_LOGIN, length));
                    int pos = 1;
                    stmt.setInt(pos++, context.getContextId());
                    for (int j = 0; j < length; j++) {
                        stmt.setInt(pos++, i(iter.next()));
                    }
                    result = stmt.executeQuery();
                    while (result.next()) {
                        users.get(I(result.getInt(1))).setLoginInfo(result.getString(2));
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        }
    }

    private void loadContact(final Context ctx, final Connection con, final Map<Integer, UserImpl> users) throws UserException {
        try {
            Iterator<UserImpl> iter = users.values().iterator();
            for (int i = 0; i < users.size(); i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    int length = Arrays.determineRealSize(users.size(), i, IN_LIMIT);
                    stmt = con.prepareStatement(getIN(SELECT_CONTACT, length));
                    int pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
                    final Map<Integer, UserImpl> userByContactId = new HashMap<Integer, UserImpl>(length, 1);
                    for (int j = 0; j < length; j++) {
                        UserImpl user = iter.next();
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
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        }
    }

    private void loadGroups(final Context context, final Connection con, final Map<Integer, UserImpl> users) throws UserException {
        final Map<Integer, List<Integer>> tmp = new HashMap<Integer, List<Integer>>(users.size(), 1);
        for (final User user : users.values()) {
            final List<Integer> userGroups = new ArrayList<Integer>();
            userGroups.add(I(0));
            tmp.put(I(user.getId()), userGroups);
        }
        try {
            Iterator<Integer> iter = users.keySet().iterator();
            for (int i = 0; i < users.size(); i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    int length = Arrays.determineRealSize(users.size(), i, IN_LIMIT);
                    final String sql = getIN("SELECT member,id FROM groups_member WHERE cid=? AND member IN (", length);
                    stmt = con.prepareStatement(sql);
                    int pos = 1;
                    stmt.setInt(pos++, context.getContextId());
                    for (int j = 0; j < length; j++) {
                        stmt.setInt(pos++, i(iter.next()));
                    }
                    result = stmt.executeQuery();
                    while (result.next()) {
                        tmp.get(I(result.getInt(1))).add(I(result.getInt(2)));
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        }
        for (final UserImpl user : users.values()) {
            user.setGroups(I2i(tmp.get(I(user.getId()))));
        }
    }

    private void loadAttributes(final Context context, final Connection con, final Map<Integer, UserImpl> users) throws UserException {
        final Map<Integer, Map<String, Set<String>>> usersAttrs = new HashMap<Integer, Map<String, Set<String>>>();
        try {
            Iterator<Integer> iter = users.keySet().iterator();
            for (int i = 0; i < users.size(); i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    int length = Arrays.determineRealSize(users.size(), i, IN_LIMIT);
                    stmt = con.prepareStatement(getIN(SELECT_ATTRS, length));
                    int pos = 1;
                    stmt.setInt(pos++, context.getContextId());
                    for (int j = 0; j < length; j++) {
                        int userId = i(iter.next());
                        stmt.setInt(pos++, userId);
                        usersAttrs.put(I(userId), new HashMap<String, Set<String>>());
                    }
                    result = stmt.executeQuery();
                    // Gather attributes
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
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw new UserException(UserException.Code.SQL_ERROR, e, e.getMessage());
        }
        for (final UserImpl user : users.values()) {
            final Map<String, Set<String>> attrs = usersAttrs.get(I(user.getId()));
            // Check for aliases
            {
                final Set<String> aliases = attrs.get("alias");
                if (aliases == null) {
                    user.setAliases(new String[0]);
                } else {
                    user.setAliases(aliases.toArray(new String[aliases.size()]));
                }
            }
            // Apply attributes
            for (final Map.Entry<String, Set<String>> entry : attrs.entrySet()) {
                entry.setValue(Collections.unmodifiableSet(entry.getValue()));
            }
            user.setAttributes(Collections.unmodifiableMap(attrs));
        }
    }

    @Override
    public void updateUserInternal(final User user, final Context context) throws LdapException {
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
        } catch (DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            // Update time zone and language
            if (null != timeZone && null != preferredLanguage) {
                PreparedStatement stmt = null;
                try {
                    String sql = "UPDATE user SET timeZone=?,preferredLanguage=? WHERE cid=? AND id=?";
                    stmt = con.prepareStatement(sql);
                    int pos = 1;
                    stmt.setString(pos++, timeZone);
                    stmt.setString(pos++, preferredLanguage);
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                    stmt.execute();
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
        } catch (UserException e) {
            rollback(con);
            throw new LdapException(e);
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
        } catch (final DBPoolingException e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            setAttribute(context.getContextId(), con, userId, name, value);
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

    private void setAttribute(final int contextId, final Connection con, final int userId, final String name, final String value) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND id=? AND name=?");
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
                stmt = con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value) VALUES (?,?,?,?)");
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
            return getAttribute(context.getContextId(), con, userId, attrName);
        } catch (final SQLException e) {
            throw new LdapException(EnumComponent.USER, Code.SQL_ERROR, e, e.getMessage());
        } catch (final Exception e) {
            throw new LdapException(EnumComponent.USER, Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeWriterSilent(context, con);
        }
    }

    private String getAttribute(final int contextId, final Connection con, final int userId, final String name) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid=? AND id=? AND name=?");
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

    private void updateAttributes(Context ctx, User user, Connection con) throws SQLException, UserException {
        int contextId = ctx.getContextId();
        int userId = user.getId();
        UserImpl load = new UserImpl();
        load.setId(userId);
        Map<Integer, UserImpl> loadMap = new HashMap<Integer, UserImpl>(1);
        loadMap.put(I(userId), load);
        loadAttributes(ctx, con, loadMap);
        Map<String, Set<String>> oldAttributes = load.getAttributes();
        Map<String, Set<String>> attributes = user.getAttributes();
        Map<String, Set<String>> added = new HashMap<String, Set<String>>();
        Map<String, Set<String>> removed = new HashMap<String, Set<String>>();
        Map<String, Set<String[]>> changed = new HashMap<String, Set<String[]>>();
        calculateDifferences(oldAttributes, attributes, added, removed, changed);
        PreparedStatement stmt = null;
        // Add new attributes
        if (!added.isEmpty()) {
            try {
                stmt = con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value) VALUES (?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                int size = 0;
                for (Map.Entry<String, Set<String>> entry : added.entrySet()) {
                    for (final String value : entry.getValue()) {
                        stmt.setString(3, entry.getKey());
                        stmt.setString(4, value);
                        stmt.addBatch();
                        size++;
                    }
                }
                int[] mLines = stmt.executeBatch();
                int lines = 0;
                for (int mLine : mLines) {
                    lines += mLine;
                }
                if (size != lines) {
                    UserException e = new UserException(UserException.Code.UPDATE_ATTRIBUTES_FAILED, I(contextId), I(userId));
                    LOG.error(String.format("Old: %3$s, New: %4$s, Added: %5$s, Removed: %6$s, Changed: %7$s.", oldAttributes, attributes, added, removed, toString(changed)), e);
                    throw e;
                }
            } finally {
                closeSQLStuff(stmt);
            }
        }
        // Remove attributes
        if (!removed.isEmpty()) {
            try {
                stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND id=? AND name=? AND value=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                int size = 0;
                for (Map.Entry<String, Set<String>> entry : removed.entrySet()) {
                    for (final String value : entry.getValue()) {
                        stmt.setString(3, entry.getKey());
                        stmt.setString(4, value);
                        stmt.addBatch();
                        size++;
                    }
                }
                int[] mLines = stmt.executeBatch();
                int lines = 0;
                for (int mLine : mLines) {
                    lines += mLine;
                }
                if (size != lines) {
                    UserException e = new UserException(UserException.Code.UPDATE_ATTRIBUTES_FAILED, I(contextId), I(userId));
                    LOG.error(String.format("Old: %3$s, New: %4$s, Added: %5$s, Removed: %6$s, Changed: %7$s.", oldAttributes, attributes, added, removed, toString(changed)), e);
                    throw e;
                }
            } finally {
                closeSQLStuff(stmt);
            }
        }
        // Update attributes
        if (!changed.isEmpty()) {
            try {
                stmt = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND id=? AND name=? AND value=?");
                stmt.setInt(2, contextId);
                stmt.setInt(3, userId);
                int size = 0;
                for (Map.Entry<String, Set<String[]>> entry : changed.entrySet()) {
                    for (String[] value : entry.getValue()) {
                        stmt.setString(4, entry.getKey());
                        stmt.setString(5, value[0]);
                        stmt.setString(1, value[1]);
                        stmt.addBatch();
                        size++;
                    }
                }
                int[] mLines = stmt.executeBatch();
                int lines = 0;
                for (int mLine : mLines) {
                    lines += mLine;
                }
                if (size != lines) {
                    UserException e = new UserException(UserException.Code.UPDATE_ATTRIBUTES_FAILED, I(contextId), I(userId));
                    LOG.error(String.format("Old: %3$s, New: %4$s, Added: %5$s, Removed: %6$s, Changed: %7$s.", oldAttributes, attributes, added, removed, toString(changed)), e);
                    throw e;
                }
            } finally {
                closeSQLStuff(stmt);
            }
        }
    }

    private String toString(Map<String, Set<String[]>> changed) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, Set<String[]>> entry : changed.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=[");
            for (String[] value : entry.getValue()) {
                sb.append(value[0]);
                sb.append("=>");
                sb.append(value[1]);
                sb.append(',');
            }
            sb.setCharAt(sb.length() - 1, ']');
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, '}');
        return sb.toString();
    }

    static void calculateDifferences(Map<String, Set<String>> oldAttributes, Map<String, Set<String>> newAttributes, Map<String, Set<String>> added, Map<String, Set<String>> removed, Map<String, Set<String[]>> changed) {
        // Find added keys
        added.putAll(newAttributes);
        for (String key : oldAttributes.keySet()) { added.remove(key); }
        // Find removed keys
        removed.putAll(oldAttributes);
        for (String key : newAttributes.keySet()) { removed.remove(key); }
        // Now the keys that are contained in old and new attributes.
        for (String key : newAttributes.keySet()) {
            if (oldAttributes.containsKey(key)) {
                compareValues(key, oldAttributes.get(key), newAttributes.get(key), added, removed, changed);
            }
        }
    }

    private static void compareValues(String name, Set<String> oldSet, Set<String> newSet, Map<String, Set<String>> added, Map<String, Set<String>> removed, Map<String, Set<String[]>> changed) {
        Set<String> addedValues = new HashSet<String>();
        Set<String> removedValues = new HashSet<String>();
        // Find added values for a key.
        addedValues.addAll(newSet);
        addedValues.removeAll(oldSet);
        // Find removed values for a key.
        removedValues.addAll(oldSet);
        removedValues.removeAll(newSet);
        Iterator<String> addedIter = addedValues.iterator();
        Iterator<String> removedIter = removedValues.iterator();
        while (addedIter.hasNext() && removedIter.hasNext()) {
            Set<String[]> values = changed.get(name);
            if (null == values) {
                values = new HashSet<String[]>();
                changed.put(name, values);
            }
            values.add(new String[] { removedIter.next(), addedIter.next() });
        }
        while (addedIter.hasNext()) {
            add(added, name, addedIter.next());
        }
        while (removedIter.hasNext()) {
            add(removed, name, removedIter.next());
        }
    }

    private static void add(Map<String, Set<String>> attributes, String name, String value) {
        Set<String> values = attributes.get(name);
        if (null == values) {
            values = new HashSet<String>();
            attributes.put(name, values);
        }
        values.add(value);
    }

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
                    stmt.setString(pos++, "alias");
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

    @Override
    public int[] listModifiedUser(final Date modifiedSince, final Context context)
        throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw new LdapException(EnumComponent.USER, Code.NO_CONNECTION, e);
        }
        final String sql = "SELECT id FROM user LEFT JOIN prg_contacts ON (user.cid=prg_contacts.cid AND user.contactId=prg_contacts.intfield01) WHERE cid=? AND changing_date>=?";
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
