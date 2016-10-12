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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import static com.openexchange.tools.sql.DBUtils.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.exception.OXExceptions;
import com.openexchange.filestore.FileStorages;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteRegistry;
import com.openexchange.groupware.i18n.Users;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.passwordmechs.IPasswordMech;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.internal.mapping.UserField;
import com.openexchange.user.internal.mapping.UserMapper;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * This class implements the user storage using a relational database instead
 * of a directory service.
 */
public class RdbUserStorage extends UserStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbUserStorage.class);

    private static final String SELECT_ATTRS = "SELECT id,uuid,name,value FROM user_attribute WHERE cid=? AND id IN (";

    private static final String SELECT_CONTACT = "SELECT intfield01,field03,field02,field01 FROM prg_contacts WHERE cid=? AND intfield01 IN (";

    private static final String SELECT_ID = "SELECT id FROM login2user WHERE cid=? AND uid=?";

    private static final String SELECT_LOGIN = "SELECT id,uid FROM login2user where cid=? AND id IN (";

    private static final String SELECT_IMAPLOGIN = "SELECT id FROM user WHERE cid=? AND imapLogin=?";

    private static final String INSERT_USER = "INSERT INTO user (cid, id, imapServer, imapLogin, mail, mailDomain, mailEnabled, preferredLanguage, shadowLastChange, smtpServer, timeZone, userPassword, contactId, passwordMech, uidNumber, gidNumber, homeDirectory, loginShell, guestCreatedBy, filestore_id, filestore_owner, filestore_name, filestore_login, filestore_passwd, quota_max) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_ATTRIBUTES = "INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?, ?, ?, ?, ?)";

    private static final String INSERT_LOGIN_INFO = "INSERT INTO login2user (cid, id, uid) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE_PASSWORD_AND_MECH = "UPDATE user SET userPassword = ?, passwordMech = ? WHERE cid = ? AND id = ?";

    /**
     * Default constructor.
     */
    public RdbUserStorage() {
        super();
    }

    @Override
    public boolean isGuest(int userId, Context context) throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final OXException e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("USR");
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM user WHERE cid=? AND id=? AND guestCreatedBy > 0");
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, userId);
            result = stmt.executeQuery();
            return result.next();
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
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
        } catch (SQLException e) {
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
            int userId = IDGenerator.getId(context, com.openexchange.groupware.Types.PRINCIPAL, con);

            // Insert user...
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
            stmt.setInt(i++, user.getCreatedBy());

            // File storage & quota information
            int filestoreId = user.getFilestoreId();
            if (filestoreId > 0) {
                stmt.setInt(i++, filestoreId); // filestore_id
                stmt.setInt(i++, user.getFileStorageOwner() <= 0 ? 0 : user.getFileStorageOwner()); // filestore_owner
                setStringOrNull(i++, stmt, FileStorages.getNameForUser(userId, context.getContextId())); // filestore_name; e.g. "1337_ctx_17_user_store"
                {
                    String[] auth = user.getFileStorageAuth();
                    if (null != auth && auth.length == 2) {
                        setStringOrNull(i++, stmt, auth[0]); // filestore_login
                        setStringOrNull(i++, stmt, auth[1]); // filestore_password
                    } else {
                        setStringOrNull(i++, stmt, null); // filestore_login
                        setStringOrNull(i++, stmt, null); // filestore_password
                    }
                }
                stmt.setLong(i++, user.getFileStorageQuota() < 0 ? -1 : user.getFileStorageQuota()); // quota_max
            } else {
                stmt.setInt(i++, 0); // filestore_id
                stmt.setInt(i++, 0); // filestore_owner
                setStringOrNull(i++, stmt, null); // filestore_name
                setStringOrNull(i++, stmt, null); // filestore_login
                setStringOrNull(i++, stmt, null); // filestore_password
                stmt.setLong(i++, 0); // quota_max
            }

            stmt.executeUpdate();

            if (false == user.isGuest()) {
                writeLoginInfo(con, user, context, userId);
            }
            writeUserAttributes(con, user.getAttributes(), context, userId);
            writeUserAliases(con, user.getAliases(), context, userId);
            return userId;
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Deletes a user from the database.
     *
     * @param context The context
     * @param userId The identifier of the user to delete
     */
    @Override
    public void deleteUser(Context context, int userId) throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickupWriteable(context);
            deleteUser(con, context, userId);
        } finally {
            DBPool.closeWriterSilent(context, con);
        }
    }

    @Override
    public void deleteUser(Connection con, Context context, int userId) throws OXException {
        try {
            /*
             * fetch required data of deleted user
             */
            int contactId;
            int uidNumber;
            int gidNumber;
            int guestCreatedBy;
            String mail;
            ResultSet result = null;
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement("SELECT mail,contactId,uidNumber,gidNumber,guestCreatedBy FROM user WHERE cid=? AND id=?;");
                stmt.setInt(1, context.getContextId());
                stmt.setInt(2, userId);
                result = stmt.executeQuery();
                if (false == result.next()) {
                    throw UserExceptionCode.USER_NOT_FOUND.create(I(userId), I(context.getContextId()));
                }
                mail = result.getString(1);
                contactId = result.getInt(2);
                uidNumber = result.getInt(3);
                gidNumber = result.getInt(4);
                guestCreatedBy = result.getInt(5);
            } finally {
                closeSQLStuff(result, stmt);
            }
            /*
             * prpeare & fire delete event
             */
            DeleteEvent deleteEvent;
            if (0 < guestCreatedBy) {
                int subType = Strings.isEmpty(mail) ? DeleteEvent.SUBTYPE_ANONYMOUS_GUEST : DeleteEvent.SUBTYPE_INVITED_GUEST;
                deleteEvent = new DeleteEvent(this, userId, DeleteEvent.TYPE_USER, subType, context, null);
            } else {
                deleteEvent = new DeleteEvent(this, userId, DeleteEvent.TYPE_USER, context);
            }
            DeleteRegistry.getInstance().fireDeleteEvent(deleteEvent, con, con);
            /*
             * insert tombstone record into del_user table
             */
            try {
                stmt = con.prepareStatement("INSERT INTO del_user (cid,id,contactId,uidNumber,gidNumber,guestCreatedBy) VALUES (?,?,?,?,?,?);");
                stmt.setInt(1, context.getContextId());
                stmt.setInt(2, userId);
                stmt.setInt(3, contactId);
                stmt.setInt(4, uidNumber);
                stmt.setInt(5, gidNumber);
                stmt.setInt(6, guestCreatedBy);
                stmt.executeUpdate();
            } finally {
                closeSQLStuff(stmt);
            }
            /*
             * remove login info if needed
             */
            if (0 < guestCreatedBy) {
                try {
                    stmt = con.prepareStatement("DELETE FROM login2user WHERE cid=? AND id=?;");
                    stmt.setInt(1, context.getContextId());
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                } finally {
                    closeSQLStuff(stmt);
                }
            }
            /*
             * remove all user attributes
             */
            try {
                stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND id=?;");
                stmt.setInt(1, context.getContextId());
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            } finally {
                closeSQLStuff(stmt);
            }
            /*
             * delete user from user table
             */
            try {
                stmt = con.prepareStatement("DELETE FROM user WHERE cid=? AND id=?;");
                stmt.setInt(1, context.getContextId());
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            } finally {
                closeSQLStuff(stmt);
            }
            /*
             * reassign guest user created-by identifier to someone else for guest users created by the deleted user
             */
            if (0 >= guestCreatedBy) {
                List<Integer> guestUserIDs = new ArrayList<Integer>();
                try {
                    stmt = con.prepareStatement("SELECT id FROM user WHERE cid=? AND guestCreatedBy=?;");
                    stmt.setInt(1, context.getContextId());
                    stmt.setInt(2, userId);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        guestUserIDs.add(I(result.getInt(1)));
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
                for (Integer guestUserID : guestUserIDs) {
                    int newGuestCreatedBy;
                    try {
                        stmt = con.prepareStatement("SELECT created_by FROM share WHERE cid=? AND guest=? AND created_by<>? ORDER BY created ASC LIMIT 1;");
                        stmt.setInt(1, context.getContextId());
                        stmt.setInt(2, guestUserID.intValue());
                        stmt.setInt(3, userId);
                        result = stmt.executeQuery();
                        newGuestCreatedBy = result.next() ? result.getInt(1) : context.getMailadmin();
                    } finally {
                        closeSQLStuff(result, stmt);
                    }
                    try {
                        stmt = con.prepareStatement("UPDATE user SET guestCreatedBy=? WHERE cid=? AND id=?;");
                        stmt.setInt(1, newGuestCreatedBy);
                        stmt.setInt(2, context.getContextId());
                        stmt.setInt(3, guestUserID.intValue());
                        stmt.executeUpdate();
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
            }
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static void writeLoginInfo(Connection con, User user, Context context, int userId) throws SQLException {
        ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        boolean autoLowerCase = null == service ? false : service.getBoolProperty("AUTO_TO_LOWERCASE_UID", false);

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_LOGIN_INFO);
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, userId);
            stmt.setString(3, autoLowerCase ? user.getLoginInfo().toLowerCase() : user.getLoginInfo());

            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void writeUserAttributes(Connection con, Map<String, Set<String>> attributes, Context context, int userId) throws SQLException {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_ATTRIBUTES);
            for (final Entry<String, Set<String>> entry : attributes.entrySet()) {
                final Set<String> valueSet = entry.getValue();
                for (final String value : valueSet) {
                    stmt.setInt(1, context.getContextId());
                    stmt.setInt(2, userId);
                    stmt.setString(3, entry.getKey());
                    stmt.setString(4, value);
                    stmt.setBytes(5, UUIDs.toByteArray(UUID.randomUUID()));

                    stmt.addBatch();
                }
            }

            stmt.executeBatch();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void writeUserAliases(Connection con, String[] aliases, Context context, int userId) throws OXException {
        UserAliasStorage userAlias = ServerServiceRegistry.getInstance().getService(UserAliasStorage.class, true);
        if (aliases != null && aliases.length > 0) {
            for (String tmp_mail : aliases) {
                if (tmp_mail.length() > 0) {
                    userAlias.createAlias(con, context.getContextId(), userId, tmp_mail);
                }
            }
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
    public User loadIfAbsent(int userId, Context ctx, Connection con) throws OXException {
        return getUser(ctx, con, new int[] { userId })[0];
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
        final TIntObjectMap<UserImpl> regularUsers = new TIntObjectHashMap<UserImpl>(length);
        try {
            for (int i = 0; i < userIds.length; i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    final int[] currentUserIds = Arrays.extract(userIds, i, IN_LIMIT);
                    stmt = con.prepareStatement(getIN("SELECT id,userPassword,mailEnabled,imapServer,imapLogin,smtpServer,mailDomain,shadowLastChange,mail,timeZone,preferredLanguage,passwordMech,contactId,guestCreatedBy,filestore_id,filestore_owner,filestore_name,filestore_login,filestore_passwd,quota_max FROM user WHERE user.cid=? AND id IN (", currentUserIds.length));
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
                        // 'guestCreatedBy'
                        user.setCreatedBy(result.getInt(pos++));
                        // File storage stuff
                        user.setFilestoreId(result.getInt(pos++));
                        user.setFileStorageOwner(result.getInt(pos++));
                        user.setFilestoreName(result.getString(pos++));
                        {
                            String login = result.getString(pos++);
                            String passwd = result.getString(pos++);
                            user.setFilestoreAuth(new String[] { login, passwd });
                        }
                        {
                            long quotaMax = result.getLong(pos++);
                            if (result.wasNull()) {
                                quotaMax = -1L;
                            }
                            user.setFileStorageQuota(quotaMax);
                        }

                        users.put(user.getId(), user);
                        if (false == user.isGuest()) {
                            regularUsers.put(user.getId(), user);
                        } else if (Strings.isEmpty(user.getMail()) && Strings.isEmpty(user.getDisplayName())) {
                            String guest = StringHelper.valueOf(user.getLocale()).getString(Users.GUEST);
                            user.setDisplayName(guest);
                            user.setLoginInfo(guest);
                        }
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.LOAD_FAILED.create(e, e.getMessage());
        }
        for (final int userId : userIds) {
            if (!users.containsKey(userId)) {
                throw UserExceptionCode.USER_NOT_FOUND.create(I(userId), I(ctx.getContextId()));
            }
        }
        loadLoginInfo(ctx, con, regularUsers);
        loadContact(ctx, con, users);
        loadAttributes(ctx.getContextId(), con, users, false);
        loadGroups(ctx, con, users);
        final User[] retval = new User[users.size()];
        for (int i = 0; i < length; i++) {
            retval[i] = users.get(userIds[i]);
        }
        return retval;
    }

    @Override
    public User[] getUser(final Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            return getUser(ctx, con, listAllUser(ctx.getContextId(), con, includeGuests, excludeUsers));
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    @Override
    public User[] getUser(Connection con, Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException {
        return getUser(ctx, con, listAllUser(ctx.getContextId(), con, includeGuests, excludeUsers));
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

    @Override
    public User[] getUser(final Context ctx, final int[] userIds, Connection con) throws OXException {
        if (0 == userIds.length) {
            return new User[0];
        }
        return getUser(ctx, con, userIds);
    }

    @Override
    public User[] getGuestsCreatedBy(Connection con, Context context, int userId) throws OXException {
        int[] userIds;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user WHERE cid=? AND guestCreatedBy=?");
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, userId);
            result = stmt.executeQuery();
            TIntList tmp = new TIntArrayList();
            while (result.next()) {
                tmp.add(result.getInt(1));
            }
            userIds = tmp.toArray();
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return getUser(context, con, userIds);
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
            if (user.isGuest()) {
                userGroups.add(GroupStorage.GUEST_GROUP_IDENTIFIER);
            } else {
                userGroups.add(GroupStorage.GROUP_ZERO_IDENTIFIER);
            }
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
        final TIntObjectMap<Map<String, UserAttribute>> usersAttrs = new TIntObjectHashMap<Map<String, UserAttribute>>();
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

        UserAliasStorage userAlias = ServerServiceRegistry.getInstance().getService(UserAliasStorage.class);
        // Proceed iterating users
        for (final UserImpl user : users.valueCollection()) {
            final Map<String, UserAttribute> attrs = usersAttrs.get(user.getId());
            {
                Set<String> aliases = userAlias.getAliases(contextId, user.getId());
                final List<String> tmp = new ArrayList<String>(aliases.size());
                if (aliases != null && false == aliases.isEmpty()) {
                    for (final String alias : aliases) {
                        try {
                            tmp.add(new QuotedInternetAddress(alias, false).toUnicodeString());
                        } catch (Exception e) {
                            tmp.add(alias);
                        }
                    }
                    user.setAliases(tmp.toArray(new String[tmp.size()]));
                } else {
                    user.setAliases(new String[0]);
                }
            }
            // Apply attributes
            user.setAttributesInternal(attrs);
        }
    }

    private static final UserMapper MAPPER = new UserMapper();

    @Override
    protected void updateUserInternal(Connection con, final User user, final Context context) throws OXException {
        try {
            if (con == null) {
                final DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
                do {
                    try {
                        con = DBPool.pickupWriteable(context);
                    } catch (final OXException e) {
                        throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("USR");
                    }
                    condition.resetTransactionRollbackException();
                    boolean rollback = false;
                    try {
                        startTransaction(con);
                        rollback = true;
                        updateUserInDB(con, user, context);
                        con.commit();
                        rollback = false;
                    } catch (final SQLException e) {
                        if (!condition.isFailedTransactionRollback(e)) {
                            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
                        }
                    } finally {
                        if (rollback) {
                            rollback(con);
                        }
                        autocommit(con);
                        DBPool.closeWriterSilent(context, con);
                    }
                } while (condition.checkRetry());
            } else {
                boolean autoCommit = con.getAutoCommit();
                if (autoCommit) {
                    try {
                        startTransaction(con);
                        updateUserInDB(con, user, context);
                        con.commit();
                    } catch (OXException e) {
                        rollback(con);
                        throw e;
                    } catch (SQLException e) {
                        rollback(con);
                        throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
                    } finally {
                        autocommit(con);
                    }
                } else {
                    updateUserInDB(con, user, context);
                }
            }
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        }
    }

    private void updateUserInDB(final Connection con, final User user, final Context context) throws SQLException, OXException {
        updateUserFields(con, user, context);
        if (null != user.getAttributes()) {
            updateAttributes(context, user, con);
        }
    }

    private void updateUserFields(final Connection con, final User user, final Context context) throws SQLException, OXException {
        // Update attribute defined through UserMapper
        UserField[] fields = MAPPER.getAssignedFields(user);
        if (fields.length > 0) {
            PreparedStatement stmt = null;
            try {
                final String sql = "UPDATE user SET " + MAPPER.getAssignments(fields) + " WHERE cid=? AND id=?";
                stmt = con.prepareStatement(sql);
                MAPPER.setParameters(stmt, user, fields);
                int pos = 1 + fields.length;
                stmt.setInt(pos++, context.getContextId());
                stmt.setInt(pos++, user.getId());
                stmt.execute();
            } finally {
                closeSQLStuff(stmt);
            }
        }
    }

    private void updatePasswordInternal(Context context, int userId, IPasswordMech mech, String password) throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickupWriteable(context);
            updatePasswordInternal(con, context, userId, mech, password);
        } finally {
            DBPool.closeWriterSilent(context, con);
        }
    }

    @Override
    protected void updatePasswordInternal(Connection connection, Context context, int userId, IPasswordMech mech, String password) throws OXException {
        if (connection == null) {
            updatePasswordInternal(context, userId, mech, password);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL_UPDATE_PASSWORD_AND_MECH);
            int pos = 1;
            stmt.setString(pos++, password);
            stmt.setString(pos++, mech != null ? mech.getIdentifier() : "");
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, userId);
            stmt.execute();
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Stores a public user attribute. This attribute is prepended with "attr_". This prefix is used to separate public user attributes from
     * internal user attributes. Public user attributes prefixed with "attr_" can be read and written by every client through the HTTP/JSON
     * API.
     *
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
     * Stores an internal user attribute. Internal user attributes must not be exposed to clients through the HTTP/JSON API.
     * <p>
     * This method might throw a {@link UserExceptionCode#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @throws OXException if writing the attribute fails.
     * @see UserExceptionCode#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY
     */
    @Override
    public void setAttribute(String name, String value, int userId, Context context) throws OXException {
        setAttributeAndReturnUser(name, value, userId, context, false);
    }

    @Override
    public void setAttribute(Connection con, String name, String value, int userId, Context context) throws OXException {
        if (value == null) {
            deleteAttribute(name, userId, context, con);
        } else {
            insertOrUpdateAttribute(name, value, userId, context, con);
        }
    }

    @Override
    public void setAttribute(Connection con, String name, String value, int userId, Context context, boolean invalidate) throws OXException {
        setAttribute(con, name, value, userId, context);
    }

    /**
     * Stores an internal user attribute. Internal user attributes must not be exposed to clients through the HTTP/JSON API.
     * <p>
     * This method might throw a {@link UserExceptionCode#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @param returnUser Whether to return updated user instance or not
     * @throws OXException if writing the attribute fails.
     * @see UserExceptionCode#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY
     */
    public User setAttributeAndReturnUser(String name, String value, int userId, Context context, boolean returnUser) throws OXException {
        if (null == name) {
            throw LdapExceptionCode.UNEXPECTED_ERROR.create("Attribute name is null.").setPrefix("USR");
        }
        User retval = null;
        Connection con = DBPool.pickupWriteable(context);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;
            if (value == null) {
                deleteAttribute(name, userId, context, con);
            } else {
                insertOrUpdateAttribute(name, value, userId, context, con);
            }
            if (returnUser) {
                retval = getUser(context, con, new int[] { userId })[0];
            }
            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (null != con) {
                if (rollback) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            DBPool.closeWriterSilent(context, con);
        }
        return retval;
    }

    private static void deleteAttribute(String name, int userId, Context context, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ? AND name = ?");
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, userId);
            stmt.setString(3, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static void insertOrUpdateAttribute(String name, String value, int userId, Context context, Connection con) throws OXException {
        int contextId = context.getContextId();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT uuid FROM user_attribute WHERE cid=? AND id=? AND name=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, name);
            rs = stmt.executeQuery();
            List<UUID> toUpdate = new LinkedList<UUID>();
            while (rs.next()) {
                toUpdate.add(UUIDs.toUUID(rs.getBytes(1)));
            }
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
            rs = null;
            if (toUpdate.isEmpty()) {
                stmt = con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value,uuid) VALUES (?,?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, name);
                stmt.setString(4, value);
                stmt.setBytes(5, UUIDs.toByteArray(UUID.randomUUID()));
                stmt.executeUpdate();
            } else {
                stmt = con.prepareStatement("UPDATE user_attribute SET value=?,uuid=? WHERE cid=? AND id=? AND name=? AND uuid=?");
                for (UUID uuid : toUpdate) {
                    stmt.setString(1, value);
                    stmt.setBytes(2, UUIDs.toByteArray(UUID.randomUUID()));
                    stmt.setInt(3, contextId);
                    stmt.setInt(4, userId);
                    stmt.setString(5, name);
                    stmt.setBytes(6, UUIDs.toByteArray(uuid));
                    stmt.addBatch();
                }
                int[] updateCounts = stmt.executeBatch();
                for (int updateCount : updateCounts) {
                    // Concurrent modification of at least one attribute. We lost the race...
                    if (updateCount == 1) {
                        LOG.error("Concurrent modification of attribute '{}' for user {} in context {}. New value '{}' could not be set.", name, I(userId), I(contextId), value);
                        throw UserExceptionCode.CONCURRENT_ATTRIBUTES_UPDATE.create(I(contextId), I(userId));
                    }
                }
            }
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw OXExceptions.general(OXExceptionStrings.MESSAGE, e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
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
            DBPool.closeReaderSilent(context, con);
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

    private static boolean updateAttributes(int contextId, int userId, Connection con, Map<String, UserAttribute> oldAttributes, Map<String, UserAttribute> attributes) throws SQLException, OXException {
        boolean retval = false;
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
                retval = true;
            } finally {
                closeSQLStuff(stmt);
            }
        }

        // Check if table 'user_attribute' has a primary key
        final boolean hasPrimaryKey = hasPrimaryKey("user_attribute", con);

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
                        if (hasPrimaryKey && null != uuid) {
                            stmt2.setBytes(2, UUIDs.toByteArray(uuid));
                            stmt2.addBatch();
                        } else {
                            stmt.setString(3, attribute.getName());
                            stmt.setString(4, value.getValue());
                            stmt.addBatch();
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
                retval = true;
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
                        UUID uuid = value.getUuid();
                        if (hasPrimaryKey && null != uuid) {
                            stmt2.setString(1, value.getNewValue());
                            stmt2.setBytes(3, UUIDs.toByteArray(uuid));
                            stmt2.addBatch();
                            size2++;
                        } else {
                            stmt.setString(1, value.getNewValue());
                            stmt.setString(4, attribute.getName());
                            stmt.setString(5, value.getValue());
                            stmt.addBatch();
                            size1++;
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
                        LOG.error("Old: {}, New: {}, Added: {}, Removed: {}, Changed: {}.", oldAttributes, attributes, added, removed, changed, e);
                        LOG.error("Expected lines: {} Updated lines: {}", size1, lines1);
                        final TIntObjectMap<UserImpl> map = createSingleUserMap(userId);
                        loadAttributes(contextId, con, map, false);
                        for (int i : map.keys()) {
                            LOG.error("User {}: {}", i, map.get(i).getAttributes());
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
                        LOG.error("Old: {}, New: {}, Added: {}, Removed: {}, Changed: {}.", oldAttributes, attributes, added, removed, changed, e);
                        LOG.error("Expected lines: {} Updated lines: {}", size2, lines2);
                        final TIntObjectMap<UserImpl> map = createSingleUserMap(userId);
                        loadAttributes(contextId, con, map, false);
                        for (int i : map.keys()) {
                            LOG.error("User {}: {}", i, map.get(i).getAttributes());
                        }
                        throw e;
                    }
                }
                retval = true;
            } finally {
                closeSQLStuff(stmt);
                closeSQLStuff(stmt2);
            }
        }
        // Signal if any modification has been performed
        return retval;
    }

    private static boolean hasPrimaryKey(final String table, final Connection con) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        // Get primary keys
        final ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, table);
        try {
            return primaryKeys.next();
        } finally {
            closeSQLStuff(primaryKeys);
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
        for (final String key : oldAttributes.keySet()) {
            added.remove(key);
        }
        // Find removed keys
        removed.putAll(oldAttributes);
        for (final String key : newAttributes.keySet()) {
            removed.remove(key);
        }
        // Now the keys that are contained in old and new attributes.
        for (final Entry<String, UserAttribute> entry : newAttributes.entrySet()) {
            String key = entry.getKey();
            if (oldAttributes.containsKey(key)) {
                compareValues(key, oldAttributes.get(key), entry.getValue(), added, removed, changed);
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
    public User searchUser(final String email, final Context context, boolean considerAliases, boolean includeGuests, boolean excludeUsers) throws OXException {
        /*
         *  Use utf8_bin to match umlauts. But that also makes it case sensitive, so use LOWER to be case insesitive.
         */
        StringBuilder stringBuilder = new StringBuilder("SELECT id FROM user WHERE cid=? AND LOWER(mail) LIKE LOWER(?) COLLATE utf8_bin");
        if (excludeUsers) {
            /*
             * exclude all regular users
             */
            stringBuilder.append(" AND guestCreatedBy>0");
        }
        if (false == includeGuests) {
            /*
             * exclude all guest users
             */
            stringBuilder.append(" AND guestCreatedBy=0");
        }
        String sql = stringBuilder.toString();
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
                if (userId < 0 && considerAliases) {
                    UserAliasStorage alias = ServerServiceRegistry.getInstance().getService(UserAliasStorage.class);
                    int retUserId = alias.getUserId(context.getContextId(), pattern);
                    if (retUserId > 0) {
                        userId = retUserId;
                    }
                }
                if (userId < 0) {
                    //FIXME: javadoc claims to return null if not found...
                    throw LdapExceptionCode.NO_USER_BY_MAIL.create(email).setPrefix("USR");
                }
                return getUser(context, con, new int[] { userId })[0];
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
    public int[] listModifiedUser(final Date modifiedSince, final Context context) throws OXException {
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
    public int[] listAllUser(Connection con, final Context context, boolean includeGuests, boolean excludeUsers) throws OXException {
        boolean closeCon = false;
        if (con == null) {
            try {
                closeCon = true;
                con = DBPool.pickup(context);
            } catch (final Exception e) {
                throw UserExceptionCode.NO_CONNECTION.create(e);
            }
        }
        try {
            return listAllUser(context.getContextId(), con, includeGuests, excludeUsers);
        } finally {
            if (closeCon) {
                DBPool.closeReaderSilent(context, con);
            }
        }
    }

    @Override
    public int[] listAllUser(Connection con, int contextID, boolean includeGuests, boolean excludeUsers) throws OXException {
        boolean closeCon = false;
        if (con == null) {
            try {
                closeCon = true;
                con = ServerServiceRegistry.getServize(DatabaseService.class, true).getReadOnly(contextID);
            } catch (final Exception e) {
                throw UserExceptionCode.NO_CONNECTION.create(e);
            }
        }
        try {
            return listAllUser(contextID, con, includeGuests, excludeUsers);
        } finally {
            if (closeCon) {
                ServerServiceRegistry.getServize(DatabaseService.class, true).backReadOnly(contextID, con);
            }
        }
    }

    private static int[] listAllUser(int contextID, Connection con, boolean includeGuests, boolean excludeUsers) throws OXException {
        StringBuilder stringBuilder = new StringBuilder("SELECT id FROM user WHERE cid=?");
        if (excludeUsers) {
            /*
             * exclude all regular users
             */
            stringBuilder.append(" AND guestCreatedBy>0");
        }
        if (false == includeGuests) {
            /*
             * exclude all guest users
             */
            stringBuilder.append(" AND guestCreatedBy=0");
        }
        String sql = stringBuilder.toString();
        final int[] users;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, contextID);
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
            final TIntList sia = new TIntArrayList(4);
            if (result.next()) {
                do {
                    sia.add(result.getInt(1));
                } while (result.next());
            } else {
                throw UserExceptionCode.USER_NOT_FOUND.create(imapLogin, I(cid));
            }
            users = sia.toArray();
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
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
