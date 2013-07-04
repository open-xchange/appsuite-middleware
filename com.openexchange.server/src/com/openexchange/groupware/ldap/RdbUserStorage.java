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

package com.openexchange.groupware.ldap;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.IN_LIMIT;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.passwordchange.PasswordMechanism;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.Collections.SmartIntArray;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.internal.mapping.UserField;
import com.openexchange.user.internal.mapping.UserMapper;

/**
 * This class implements the user storage using a relational database instead
 * of a directory service.
 */
public class RdbUserStorage extends UserStorage {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(RdbUserStorage.class));

    private static final String SELECT_ALL_USER = "SELECT id,userPassword,mailEnabled,imapServer,imapLogin,smtpServer,mailDomain," +
        "shadowLastChange,mail,timeZone,preferredLanguage,passwordMech,contactId FROM user WHERE user.cid=?";

    private static final String SELECT_USER = SELECT_ALL_USER + " AND id IN (";

    private static final String SELECT_ATTRS = "SELECT id,uuid,name,value FROM user_attribute WHERE cid=? AND id IN (";

    private static final String SELECT_CONTACT = "SELECT intfield01,field03,field02,field01 FROM prg_contacts WHERE cid=? AND intfield01 IN (";

    private static final String SELECT_ID = "SELECT id FROM login2user WHERE cid=? AND uid=?";

    private static final String SELECT_LOGIN = "SELECT id,uid FROM login2user where cid=? AND id IN (";

    private static final String SELECT_IMAPLOGIN = "SELECT id FROM user WHERE cid=? AND imapLogin=?";

    private static final String SQL_UPDATE_PASSWORD = "UPDATE user SET userPassword = ?, shadowLastChange = ? WHERE cid = ? AND id = ?";

    private static final String INSERT_USER = "INSERT INTO user (cid, id, imapServer, imapLogin, mail, mailDomain, mailEnabled, " +
        "preferredLanguage, shadowLastChange, smtpServer, timeZone, userPassword, contactId, passwordMech, uidNumber, gidNumber, " +
        "homeDirectory, loginShell) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_ATTRIBUTES = "INSERT INTO user_attribute (cid, id, name, value) VALUES (?, ?, ?, ?)";

    private static final String INSERT_LOGIN_INFO = "INSERT INTO login2user (cid, id, uid) VALUES (?, ?, ?)";


    /**
     * Default constructor.
     */
    public RdbUserStorage() {
        super();
    }

    @Override
    public int getUserId(final String uid, final Context context) throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final OXException e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("USR");
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
                throw LdapExceptionCode.USER_NOT_FOUND.create(uid, I(context.getContextId())).setPrefix("USR");
            }
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return userId;
    }

    @Override
    public int createUser(final Connection con, final Context context, final User user) throws OXException {
        PreparedStatement stmt = null;
        try {
            final int userId = IDGenerator.getId(context, com.openexchange.groupware.Types.PRINCIPAL, con);
            stmt = con.prepareStatement(INSERT_USER);
            int i = 1;
            stmt.setInt(i++, context.getContextId());
            stmt.setInt(i++, userId);
            setStringOrNull(i++, stmt, user.getImapServer());
            setStringOrNull(i++, stmt, user.getImapLogin());
            setStringOrNull(i++, stmt, user.getMail());
            setStringOrNull(i++, stmt, user.getMailDomain());

            /*
             * Setting mailEnabled to true as it is not part of the user object.
             * Referring to com.openexchange.admin.rmi.dataobjects.User this value is not used anyway.
             * This may(!) cause a loss of data during a user move/copy.
             */
            stmt.setInt(i++, 1);

            setStringOrNull(i++, stmt, user.getPreferredLanguage());
            stmt.setInt(i++, user.getShadowLastChange());
            setStringOrNull(i++, stmt, user.getSmtpServer());
            setStringOrNull(i++, stmt, user.getTimeZone());
            setStringOrNull(i++, stmt, user.getUserPassword());
            stmt.setInt(i++, user.getContactId());
            setStringOrNull(i++, stmt, user.getPasswordMech());

            /*
             * Now we fill uidNumber, gidNumber, homeDirectory and loginShell.
             * As this seems not to be used anymore we set this manually.
             * This may(!) cause a loss of data during a user move/copy.
             */
            stmt.setInt(i++, 0);
            stmt.setInt(i++, 0);
            setStringOrNull(i++, stmt, "/home/" + user.getGivenName());
            setStringOrNull(i++, stmt, "/bin/bash");
            stmt.executeUpdate();

            writeLoginInfo(con, user, context, userId);
            writeUserAttributes(con, user, context, userId);
            return userId;
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void writeLoginInfo(Connection con, User user, Context context, int userId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_LOGIN_INFO);
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, userId);
            stmt.setString(3, user.getLoginInfo());

            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void writeUserAttributes(Connection con, User user, Context context, int userId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_ATTRIBUTES);
            final Map<String, Set<String>> attributes = user.getAttributes();
            for (final String key : attributes.keySet()) {
                final Set<String> valueSet = attributes.get(key);
                for (final String value : valueSet) {
                    stmt.setInt(1, context.getContextId());
                    stmt.setInt(2, userId);
                    stmt.setString(3, key);
                    stmt.setString(4, value);

                    stmt.addBatch();
                }
            }

            stmt.executeBatch();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public int createUser(final Context context, final User user) throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
            return createUser(con, context, user);
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    private static void setStringOrNull(int parameter, PreparedStatement stmt, String value) throws SQLException {
        if (value == null) {
            stmt.setNull(parameter, java.sql.Types.VARCHAR);
        } else {
            stmt.setString(parameter, value);
        }
    }

    @Override
    public User getUser(int userId, Context context) throws OXException {
        final Connection con = DBPool.pickup(context);
        try {
            return getUser(context, con, new int[] { userId })[0];
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public User getUser(final Context ctx, final int userId, final Connection con) throws OXException {
        return getUser(ctx, con, new int[] { userId })[0];
    }

    private static User[] getUser(Context ctx, Connection con, int[] userIds) throws OXException {
        final int length = userIds.length;
        if (0 == length) {
            return new User[0];
        }
        final TIntObjectMap<UserImpl> users = new TIntObjectHashMap<UserImpl>(length);
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
                        users.put(user.getId(), user);
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.LOAD_FAILED.create(e, e.getMessage());
        }
        for (final int userId : userIds) {
            if (!users.containsKey(I(userId))) {
                throw UserExceptionCode.USER_NOT_FOUND.create(I(userId), I(ctx.getContextId()));
            }
        }
        loadLoginInfo(ctx, con, users);
        loadContact(ctx, con, users);
        loadGroups(ctx, con, users);
        loadAttributes(ctx.getContextId(), con, users, false);
        final User[] retval = new User[users.size()];
        for (int i = 0; i < length; i++) {
            retval[i] = users.get(I(userIds[i]));
        }
        return retval;
    }

    @Override
    public User[] getUser(final Context ctx) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            return getUser(ctx, con, listAllUser(ctx, con));
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    @Override
    public User[] getUser(final Context ctx, final int[] userIds) throws OXException {
        if (0 == userIds.length) {
            return new User[0];
        }
        final Connection con = DBPool.pickup(ctx);
        try {
            return getUser(ctx, con, userIds);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    private static void loadLoginInfo(Context context, Connection con, TIntObjectMap<UserImpl> users) throws OXException {
        try {
            final TIntIterator iter = users.keySet().iterator();
            for (int i = 0; i < users.size(); i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    final int length = Arrays.determineRealSize(users.size(), i, IN_LIMIT);
                    stmt = con.prepareStatement(getIN(SELECT_LOGIN, length));
                    int pos = 1;
                    stmt.setInt(pos++, context.getContextId());
                    for (int j = 0; j < length; j++) {
                        stmt.setInt(pos++, iter.next());
                    }
                    result = stmt.executeQuery();
                    while (result.next()) {
                        users.get(result.getInt(1)).setLoginInfo(result.getString(2));
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static void loadContact(Context ctx, Connection con, TIntObjectMap<UserImpl> users) throws OXException {
        try {
            final Iterator<UserImpl> iter = users.valueCollection().iterator();
            for (int i = 0; i < users.size(); i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    final int length = Arrays.determineRealSize(users.size(), i, IN_LIMIT);
                    stmt = con.prepareStatement(getIN(SELECT_CONTACT, length));
                    int pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
                    final TIntObjectMap<UserImpl> userByContactId = new TIntObjectHashMap<UserImpl>(length, 1);
                    for (int j = 0; j < length; j++) {
                        final UserImpl user = iter.next();
                        stmt.setInt(pos++, user.getContactId());
                        userByContactId.put(user.getContactId(), user);
                    }
                    result = stmt.executeQuery();
                    while (result.next()) {
                        pos = 1;
                        final UserImpl user = userByContactId.get(result.getInt(pos++));
                        user.setGivenName(result.getString(pos++));
                        user.setSurname(result.getString(pos++));
                        user.setDisplayName(result.getString(pos++));
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static void loadGroups(Context context, Connection con, TIntObjectMap<UserImpl> users) throws OXException {
        final TIntObjectMap<TIntList> tmp = new TIntObjectHashMap<TIntList>(users.size(), 1);
        for (final User user : users.valueCollection()) {
            final TIntList userGroups = new TIntArrayList();
            userGroups.add(0);
            tmp.put(user.getId(), userGroups);
        }
        try {
            final TIntIterator iter = users.keySet().iterator();
            for (int i = 0; i < users.size(); i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    final int length = Arrays.determineRealSize(users.size(), i, IN_LIMIT);
                    final String sql = getIN("SELECT member,id FROM groups_member WHERE cid=? AND member IN (", length);
                    stmt = con.prepareStatement(sql);
                    int pos = 1;
                    stmt.setInt(pos++, context.getContextId());
                    for (int j = 0; j < length; j++) {
                        stmt.setInt(pos++, iter.next());
                    }
                    result = stmt.executeQuery();
                    while (result.next()) {
                        tmp.get(result.getInt(1)).add(result.getInt(2));
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        for (final UserImpl user : users.valueCollection()) {
            user.setGroups(tmp.get(user.getId()).toArray());
        }
    }

    private static void loadAttributes(int contextId, Connection con, TIntObjectMap<UserImpl> users, boolean lockRows) throws OXException {
        if (lockRows && users.size() != 1) {
            throw UserExceptionCode.LOCKING_NOT_ALLOWED.create(I(users.size()));
        }
        final TIntObjectMap<Map<String, UserAttribute>> usersAttrs = new TIntObjectHashMap<Map<String,UserAttribute>>();
        try {
            final TIntIterator iter = users.keySet().iterator();
            for (int i = 0; i < users.size(); i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    final int length = Arrays.determineRealSize(users.size(), i, IN_LIMIT);
                    String sql = getIN(SELECT_ATTRS, length);
                    if (lockRows) {
                        sql += " FOR UPDATE";
                    }
                    stmt = con.prepareStatement(sql);
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    for (int j = 0; j < length; j++) {
                        final int userId = iter.next();
                        stmt.setInt(pos++, userId);
                        usersAttrs.put(userId, new HashMap<String, UserAttribute>());
                    }
                    result = stmt.executeQuery();
                    // Gather attributes
                    while (result.next()) {
                        Map<String, UserAttribute> attrs = usersAttrs.get(result.getInt(1));
                        String name = result.getString(3);
                        UserAttribute attribute = attrs.get(name);
                        if (null == attribute) {
                            attribute = new UserAttribute(name);
                            attrs.put(name, attribute);
                        }
                        final UUID uuid;
                        byte[] bytes = result.getBytes(2);
                        if (result.wasNull()) {
                            uuid = null;
                        } else {
                            uuid = UUIDs.toUUID(bytes);
                        }
                        attribute.addValue(new AttributeValue(result.getString(4), uuid));
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        // Proceed iterating users
        for (final UserImpl user : users.valueCollection()) {
            final Map<String, UserAttribute> attrs = usersAttrs.get(user.getId());
            // Check for aliases
            {
                UserAttribute aliases = attrs.get("alias");
                if (aliases == null) {
                    user.setAliases(new String[0]);
                } else {
                    final List<String> tmp = new ArrayList<String>(aliases.size());
                    for (final String alias : aliases.getStringValues()) {
                        try {
                            tmp.add(new QuotedInternetAddress(alias, false).toUnicodeString());
                        } catch (Exception e) {
                            tmp.add(alias);
                        }
                    }
                    user.setAliases(tmp.toArray(new String[tmp.size()]));
                }
            }
            // Apply attributes
            user.setAttributesInternal(attrs);
        }
    }

    private static final UserMapper MAPPER = new UserMapper();

    @Override
    protected void updateUserInternal(final User user, final Context context) throws OXException {
        final int contextId = context.getContextId();
        final int userId = user.getId();
        final String password = user.getUserPassword();
        final String mech = user.getPasswordMech();
        final int shadowLastChanged = user.getShadowLastChange();

        try {
            final DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
            do {
                final Connection con;
                try {
                    con = DBPool.pickupWriteable(context);
                } catch (final OXException e) {
                    throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("USR");
                }
                condition.resetTransactionRollbackException();
                try {
                    startTransaction(con);
                    // Update attribute defined through UserMapper
                    UserField[] fields = MAPPER.getAssignedFields(user);
                    if (fields.length > 0) {
                        PreparedStatement stmt = null;
                        try {
                            final String sql = "UPDATE user SET " + MAPPER.getAssignments(fields) + " WHERE cid=? AND id=?";
                            stmt = con.prepareStatement(sql);
                            MAPPER.setParameters(stmt, user, fields);
                            int pos = 1 + fields.length;
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
                        } catch (final UnsupportedEncodingException e) {
                            throw new SQLException(e.toString());
                        } catch (final NoSuchAlgorithmException e) {
                            throw new SQLException(e.toString());
                        } finally {
                            closeSQLStuff(stmt);
                        }
                    }
                    con.commit();
                } catch (final SQLException e) {
                    rollback(con);
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
                    }
                } catch (final OXException e) {
                    rollback(con);
                    throw e;
                } finally {
                    autocommit(con);
                    DBPool.closeWriterSilent(context, con);
                }
            } while (condition.checkRetry());
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        }
    }

    /**
     * Stores a public user attribute. This attribute is prepended with "attr_". This prefix is used to separate public user attributes from
     * internal user attributes. Public user attributes prefixed with "attr_" can be read and written by every client through the HTTP/JSON
     * API.
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @throws OXException if writing the attribute fails.
     */
    @Override
    public void setUserAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        if (null == name) {
            throw LdapExceptionCode.UNEXPECTED_ERROR.create("Attribute name is null.").setPrefix("USR");
        }
        final String attrName = new StringBuilder("attr_").append(name).toString();
        setAttribute(attrName, value, userId, context);
    }

    /**
     * Stores a internal user attribute. Internal user attributes must not be exposed to clients through the HTTP/JSON API.
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @throws OXException if writing the attribute fails.
     */
    @Override
    public void setAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        if (null == name) {
            throw LdapExceptionCode.UNEXPECTED_ERROR.create("Attribute name is null.").setPrefix("USR");
        }
        final Connection con;
        try {
            con = DBPool.pickupWriteable(context);
        } catch (final OXException e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("USR");
        }
        try {
            con.setAutoCommit(false);
            setAttribute(context.getContextId(), con, userId, name, value);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } catch (final Exception e) {
            rollback(con);
            throw LdapExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } finally {
            autocommit(con);
            DBPool.closeWriterSilent(context, con);
        }
    }

    private static void setAttribute(int contextId, Connection con, int userId, String name, String value) throws SQLException, OXException {
        TIntObjectMap<UserImpl> userMap = createSingleUserMap(userId);
        loadAttributes(contextId, con, userMap, true);
        Map<String, UserAttribute> oldAttributes = userMap.get(userId).getAttributesInternal();
        Map<String, UserAttribute> attributes = new HashMap<String, UserAttribute>(oldAttributes);
        if (null == value) {
            attributes.remove(name);
        } else {
            UserAttribute newAttribute = new UserAttribute(name);
            newAttribute.addValue(value);
            attributes.put(name, newAttribute);
        }
        updateAttributes(contextId, userId, con, oldAttributes, attributes);
    }

    @Override
    public String getUserAttribute(final String name, final int userId, final Context context) throws OXException {
        if (null == name) {
            throw LdapExceptionCode.UNEXPECTED_ERROR.create("Attribute name is null.").setPrefix("USR");
        }
        final Connection con = DBPool.pickup(context);
        try {
            final String attrName = new StringBuilder("attr_").append(name).toString();
            return getAttribute(context.getContextId(), con, userId, attrName);
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } catch (final Exception e) {
            throw LdapExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } finally {
            DBPool.closeWriterSilent(context, con);
        }
    }

    private static String getAttribute(int contextId, Connection con, int userId, String name) throws SQLException {
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

    private static void updateAttributes(Context ctx, User user, Connection con) throws SQLException, OXException {
        final int contextId = ctx.getContextId();
        final int userId = user.getId();
        final TIntObjectMap<UserImpl> loadMap = createSingleUserMap(userId);
        loadAttributes(ctx.getContextId(), con, loadMap, true);
        final Map<String, UserAttribute> oldAttributes = loadMap.get(userId).getAttributesInternal();
        final Map<String, UserAttribute> attributes = UserImpl.toInternal(user.getAttributes());
        updateAttributes(contextId, userId, con, oldAttributes, attributes);
    }

    private static void updateAttributes(int contextId, int userId, Connection con, Map<String, UserAttribute> oldAttributes, Map<String, UserAttribute> attributes) throws SQLException, OXException {
        final Map<String, UserAttribute> added = new HashMap<String, UserAttribute>();
        final Map<String, UserAttribute> removed = new HashMap<String, UserAttribute>();
        final Map<String, UserAttribute> changed = new HashMap<String, UserAttribute>();
        calculateDifferences(oldAttributes, attributes, added, removed, changed);
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        // Add new attributes
        if (!added.isEmpty()) {
            try {
                stmt = con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value,uuid) VALUES (?,?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                int size = 0;
                for (Entry<String, UserAttribute> entry : added.entrySet()) {
                    UserAttribute userAttribute = entry.getValue();
                    for (AttributeValue value : userAttribute.getValues()) {
                        stmt.setString(3, userAttribute.getName());
                        stmt.setString(4, value.getValue());
                        final UUID uuid = value.getUuid();
                        stmt.setBytes(5, UUIDs.toByteArray(null == uuid ? UUID.randomUUID() : uuid));
                        stmt.addBatch();
                        size++;
                    }
                }
                int lines = 0;
                for (final int mLine : stmt.executeBatch()) {
                    lines += mLine;
                }
                if (size != lines) {
                    final OXException e = UserExceptionCode.UPDATE_ATTRIBUTES_FAILED.create(I(contextId), I(userId));
                    LOG.error(String.format("Old: %1$s, New: %2$s, Added: %3$s, Removed: %4$s, Changed: %5$s.", oldAttributes, attributes, added, removed, changed), e);
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
                stmt2 = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND uuid=?");
                stmt2.setInt(1, contextId);
                int size = 0;
                for (UserAttribute attribute : removed.values()) {
                    for (AttributeValue value : attribute.getValues()) {
                        final UUID uuid = value.getUuid();
                        if (null == uuid) {
                            stmt.setString(3, attribute.getName());
                            stmt.setString(4, value.getValue());
                            stmt.addBatch();
                        } else {
                            stmt2.setBytes(2, UUIDs.toByteArray(uuid));
                            stmt2.addBatch();
                        }
                        size++;
                    }
                }
                int lines = 0;
                for (final int mLine : stmt.executeBatch()) {
                    lines += mLine;
                }
                for (final int mLine : stmt2.executeBatch()) {
                    lines += mLine;
                }
                if (size != lines) {
                    final OXException e = UserExceptionCode.UPDATE_ATTRIBUTES_FAILED.create(I(contextId), I(userId));
                    LOG.error(String.format("Old: %1$s, New: %2$s, Added: %3$s, Removed: %4$s, Changed: %5$s.", oldAttributes, attributes, added, removed, changed), e);
                    throw e;
                }
            } finally {
                closeSQLStuff(stmt);
                closeSQLStuff(stmt2);
            }
        }
        // Update attributes
        if (!changed.isEmpty()) {
            try {
                stmt = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND id=? AND name=? AND value=?");
                stmt.setInt(2, contextId);
                stmt.setInt(3, userId);
                stmt2 = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND uuid=?");
                stmt2.setInt(2, contextId);
                int size1 = 0;
                int size2 = 0;
                for (UserAttribute attribute : changed.values()) {
                    for (AttributeValue value : attribute.getValues()) {
                        stmt2.setString(1, value.getNewValue());
                        UUID uuid = value.getUuid();
                        if (null == uuid) {
                            stmt.setString(4, attribute.getName());
                            stmt.setString(5, value.getValue());
                            stmt.addBatch();
                            size1++;
                        } else {
                            stmt2.setBytes(3, UUIDs.toByteArray(uuid));
                            stmt2.addBatch();
                            size2++;
                        }
                    }
                }
                int lines1 = 0;
                int[] mLines1 = stmt.executeBatch();
                for (final int mLine : mLines1) {
                    lines1 += mLine;
                }
                int lines2 = 0;
                int[] mLines2 = stmt2.executeBatch();
                for (final int mLine : mLines2) {
                    lines2 += mLine;
                }
                if (size1 != lines1) {
                    // Ignoring the failed update of a clients login time stamp. This only happens if a concurrent login with the same client took place.
                    boolean onlyLoginsFailed = true;
                    int j = 0;
                    for (Entry<String, UserAttribute> entry : changed.entrySet()) {
                        if (!entry.getKey().startsWith("client:") && mLines1[j] != 1) {
                            onlyLoginsFailed = false;
                            break;
                        }
                        j++;
                    }
                    if (!onlyLoginsFailed) {
                        final OXException e = UserExceptionCode.UPDATE_ATTRIBUTES_FAILED.create(I(contextId), I(userId));
                        LOG.error(String.format("Old: %1$s, New: %2$s, Added: %3$s, Removed: %4$s, Changed: %5$s.", oldAttributes, attributes, added, removed, changed), e);
                        LOG.error("Expected lines: " + size1 + " Updated lines: " + lines1);
                        final TIntObjectMap<UserImpl> map = createSingleUserMap(userId);
                        loadAttributes(contextId, con, map, false);
                        for (int i : map.keys()) {
                            LOG.error("User " + i + ": " + map.get(i).getAttributes().toString());
                        }
                        throw e;
                    }
                }
                if (size2 != lines2) {
                    // Ignoring the failed update of a clients login time stamp. This only happens if a concurrent login with the same client took place.
                    boolean onlyLoginsFailed = true;
                    int j = 0;
                    for (Entry<String, UserAttribute> entry : changed.entrySet()) {
                        if (!entry.getKey().startsWith("client:") && mLines2[j] != 1) {
                            onlyLoginsFailed = false;
                            break;
                        }
                        j++;
                    }
                    if (!onlyLoginsFailed) {
                        final OXException e = UserExceptionCode.UPDATE_ATTRIBUTES_FAILED.create(I(contextId), I(userId));
                        LOG.error(String.format("Old: %1$s, New: %2$s, Added: %3$s, Removed: %4$s, Changed: %5$s.", oldAttributes, attributes, added, removed, changed), e);
                        LOG.error("Expected lines: " + size2 + " Updated lines: " + lines2);
                        final TIntObjectMap<UserImpl> map = createSingleUserMap(userId);
                        loadAttributes(contextId, con, map, false);
                        for (int i : map.keys()) {
                            LOG.error("User " + i + ": " + map.get(i).getAttributes().toString());
                        }
                        throw e;
                    }
                }
            } finally {
                closeSQLStuff(stmt);
                closeSQLStuff(stmt2);
            }
        }
    }

    private static TIntObjectMap<UserImpl> createSingleUserMap(int userId) {
        final UserImpl load = new UserImpl();
        load.setId(userId);
        TIntObjectMap<UserImpl> loadMap = new TIntObjectHashMap<UserImpl>(1);
        loadMap.put(userId, load);
        return loadMap;
    }

    static void calculateDifferences(Map<String, UserAttribute> oldAttributes, Map<String, UserAttribute> newAttributes, Map<String, UserAttribute> added, Map<String, UserAttribute> removed, Map<String, UserAttribute> changed) {
        // Find added keys
        added.putAll(newAttributes);
        for (final String key : oldAttributes.keySet()) { added.remove(key); }
        // Find removed keys
        removed.putAll(oldAttributes);
        for (final String key : newAttributes.keySet()) { removed.remove(key); }
        // Now the keys that are contained in old and new attributes.
        for (final String key : newAttributes.keySet()) {
            if (oldAttributes.containsKey(key)) {
                compareValues(key, oldAttributes.get(key), newAttributes.get(key), added, removed, changed);
            }
        }
    }

    private static void compareValues(String name, UserAttribute oldSet, UserAttribute newSet, Map<String, UserAttribute> added, Map<String, UserAttribute> removed, Map<String, UserAttribute> changed) {
        // Comparison must be made based only on attribute value because newSet mostly does not contain any UUIDs and hashCode() and
        // equals() methods would not match the AttributeValue.
        final Set<String> addedValues = new HashSet<String>(newSet.getStringValues());
        final Set<String> removedValues = new HashSet<String>(oldSet.getStringValues());
        // Find added values for a key.
        addedValues.removeAll(oldSet.getStringValues());
        // Find removed values for a key.
        removedValues.removeAll(newSet.getStringValues());
        // Try to replace as much attribute values as possible instead of deleting old one and inserting new ones causing more deadlocks on
        // the database.
        final Iterator<String> addedIter = addedValues.iterator();
        final Iterator<String> removedIter = removedValues.iterator();
        while (addedIter.hasNext() && removedIter.hasNext()) {
            UserAttribute attribute = changed.get(name);
            if (null == attribute) {
                attribute = new UserAttribute(name);
                changed.put(name, attribute);
            }
            AttributeValue value = oldSet.getValue(removedIter.next());
            // Null can not be returned for value because removedIter is based on attributes given through oldSet.
            attribute.addValue(new AttributeValue(value, addedIter.next()));
        }
        while (addedIter.hasNext()) {
            add(added, name, new AttributeValue(addedIter.next()));
        }
        while (removedIter.hasNext()) {
            add(removed, name, oldSet.getValue(removedIter.next()));
            // Null can not be returned for value because removedIter is based on attributes given through oldSet.
        }
    }

    private static void add(Map<String, UserAttribute> attributes, String name, AttributeValue value) {
        UserAttribute values = attributes.get(name);
        if (null == values) {
            values = new UserAttribute(name);
            attributes.put(name, values);
        }
        values.addValue(value);
    }

    @Override
    public User[] searchUserByName(final String name, final Context context, final int searchType) throws OXException {
        if (0 == searchType) {
            return new User[0];
        }
        final Connection con = DBPool.pickup(context);
        try {
            final String pattern = StringCollection.prepareForSearch(name, false, true);
            final int contextId = context.getContextId();
            final TIntSet userIds = new TIntHashSet();
            PreparedStatement stmt = null;
            ResultSet result = null;
            final boolean searchLoginName = (searchType & SEARCH_LOGIN_NAME) > 0;
            final boolean searchDisplayName = (searchType & SEARCH_DISPLAY_NAME) > 0;
            if (searchDisplayName && searchLoginName) {
                try {
                    stmt = con.prepareStatement("SELECT con.userid FROM prg_contacts con JOIN login2user lu ON con.userid = lu.id AND con.cid = lu.cid WHERE con.cid = ? AND (lu.uid LIKE ? OR con.field01 LIKE ?)");
                    stmt.setInt(1, contextId);
                    stmt.setString(2, pattern);
                    stmt.setString(3, pattern);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        userIds.add(result.getInt(1));
                    }
                } catch (final SQLException e) {
                    throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
                } finally {
                    closeSQLStuff(result, stmt);
                }
            } else {
                if (searchLoginName) {
                    try {
                        stmt = con.prepareStatement("SELECT id FROM login2user WHERE cid=? AND uid LIKE ?");
                        stmt.setInt(1, contextId);
                        stmt.setString(2, pattern);
                        result = stmt.executeQuery();
                        while (result.next()) {
                            userIds.add(result.getInt(1));
                        }
                    } catch (final SQLException e) {
                        throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
                    } finally {
                        closeSQLStuff(result, stmt);
                    }
                }
                if (searchDisplayName) {
                    try {
                        stmt = con.prepareStatement("SELECT userid FROM prg_contacts WHERE cid=? AND fid=? AND userid IS NOT NULL AND field01 LIKE ?");
                        stmt.setInt(1, contextId);
                        stmt.setInt(2, FolderObject.SYSTEM_LDAP_FOLDER_ID);
                        stmt.setString(3, pattern);
                        result = stmt.executeQuery();
                        while (result.next()) {
                            userIds.add(result.getInt(1));
                        }
                    } catch (final SQLException e) {
                        throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
                    } finally {
                        closeSQLStuff(result, stmt);
                    }
                }
            }
            return getUser(context, userIds.toArray());
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public User searchUser(final String email, final Context context) throws OXException {
        return searchUser(email, context, true);
    }

    @Override
    public User searchUser(final String email, final Context context, boolean considerAliases) throws OXException {
        String sql = "SELECT id FROM user WHERE cid=? AND mail LIKE ?";
        final Connection con = DBPool.pickup(context);
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
                throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
            } finally {
                closeSQLStuff(result, stmt);
            }
            try {
                if (userId == -1 && considerAliases) {
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
                    throw LdapExceptionCode.NO_USER_BY_MAIL.create(email).setPrefix("USR");
                }
                return getUser(context, con, new int[] { userId })[0];
            } catch (final SQLException e) {
                throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
            } finally {
                closeSQLStuff(result, stmt);
            }
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public User[] searchUserByMailLogin(final String login, final Context context) throws OXException {
        String sql = "SELECT id FROM user WHERE cid=? AND imapLogin LIKE ?";
        final Connection con = DBPool.pickup(context);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final String pattern = StringCollection.prepareForSearch(login, false, true);
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, context.getContextId());
            stmt.setString(2, pattern);
            result = stmt.executeQuery();
            final TIntSet userIds = new TIntHashSet();
            while (result.next()) {
                userIds.add(result.getInt(1));
            }
            return getUser(context, userIds.toArray());
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public int[] listModifiedUser(final Date modifiedSince, final Context context)
        throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("USR");
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
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return users;
    }

    @Override
    public int[] listAllUser(final Context context) throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw UserExceptionCode.NO_CONNECTION.create(e);
        }
        try {
            return listAllUser(context, con);
        } finally {
            DBPool.closeReaderSilent(context, con);
        }
    }

    private static int[] listAllUser(Context ctx, Connection con) throws OXException {
        final int[] users;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user WHERE user.cid=?");
            stmt.setInt(1, ctx.getContextId());
            result = stmt.executeQuery();
            final TIntList tmp = new TIntArrayList();
            while (result.next()) {
                tmp.add(result.getInt(1));
            }
            users = tmp.toArray();
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return users;
    }

    @Override
    public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw UserExceptionCode.NO_CONNECTION.create(e);
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
                throw UserExceptionCode.USER_NOT_FOUND.create(imapLogin, I(cid));
            }
            users = sia.toArray();
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e
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
