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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.leaseConnectionForContext;
import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.leaseReadConnectionForConfigDB;
import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.leaseWriteContextConnectionWithoutTimeout;
import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.releaseConfigDBConnection;
import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.releaseWriteContextConnection;
import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.releaseWriteContextConnectionAfterReading;
import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.releaseWriteContextConnectionWithoutTimeout;
import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.osgi.framework.ServiceException;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.alias.AliasUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.contact.ContactUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.custom.CustomUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.guipref.GuiPreferenceUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailaccount.UserMailAccountAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailaccount.primary.PrimaryMailAccountAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailsetting.UserSettingMailAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.spamfilter.SpamFilterUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.UserAttribute;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.UserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.username.UserNameUserAttributeChangers;
import com.openexchange.admin.storage.sqlStorage.OXUserSQLStorage;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.NameBuilder;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFinishedListenerRegistry;
import com.openexchange.groupware.delete.DeleteRegistry;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.groupware.userconfiguration.RdbUserPermissionBitsStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.oxfolder.OXFolderDefaultMode;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;

/**
 * @author cutmasta
 * @author d7
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OXUserMySQLStorage extends OXUserSQLStorage implements OXMySQLDefaultValues {

    private static class MethodAndName {

        final Method method;
        final String name;

        /**
         * @param method
         * @param name
         */
        public MethodAndName(final Method method, final String name) {
            super();
            this.method = method;
            this.name = name;
        }

    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXUserMySQLStorage.class);

    // DEFAULTS FOR USER CREATE; SHOULD BE MOVED TO PROPERTIES FILE
    private static final String DEFAULT_TIMEZONE_CREATE = "Europe/Berlin";
    private static final String DEFAULT_SMTP_SERVER_CREATE = "smtp://localhost:25";
    private static final String DEFAULT_IMAP_SERVER_CREATE = "imap://localhost:143";

    private final AdminCache cache;
    private final PropertyHandler prop;
    private final List<AttributeChangers> attributeChangers;

    /**
     * Initializes a new {@link OXUserMySQLStorage}.
     */
    public OXUserMySQLStorage() {
        super();
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
        attributeChangers = initialiseAttributeChangers();
    }

    /**
     * Initialises the {@link AttributeChangers}
     *
     * @return An unmodifiable {@link List} with the initialised {@link AttributeChangers}
     */
    private List<AttributeChangers> initialiseAttributeChangers() {
        List<AttributeChangers> ac = new ArrayList<>(12);
        ac.add(new UserAttributeChangers(cache));
        ac.add(new UserSettingMailAttributeChangers());
        ac.add(new UserMailAccountAttributeChangers());
        ac.add(new CustomUserAttributeChangers());
        ac.add(new ContactUserAttributeChangers());
        ac.add(new SpamFilterUserAttributeChangers());
        ac.add(new GuiPreferenceUserAttributeChangers());
        ac.add(new UserNameUserAttributeChangers(cache));
        ac.add(new AliasUserAttributeChangers());
        ac.add(new PrimaryMailAccountAttributeChangers());
        return Collections.unmodifiableList(ac);
    }

    @Override
    public boolean doesContextExist(final Context ctx) throws StorageException {
        final Integer id = ctx.getId();
        if (null == id) {
            throw new StorageException("Missing context identifier");
        }
        Connection con = leaseReadConnectionForConfigDB(cache);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM context WHERE cid = ? LIMIT 1");
            stmt.setInt(1, id.intValue());
            return stmt.executeQuery().next();
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
            releaseConfigDBConnection(con, cache);
        }
    }

    @Override
    public Set<String> getCapabilities(Context ctx, User user) throws StorageException {
        final int contextId = ctx.getId().intValue();
        // SQL resources
        Connection con = leaseConnectionForContext(contextId, cache);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cap FROM capability_user WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user.getId().intValue());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.<String> emptySet();
            }
            final Set<String> caps = new HashSet<>(16);
            do {
                caps.add(rs.getString(1));
            } while (rs.next());
            return caps;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    @Override
    public void changeMailAddressPersonal(Context ctx, User user, String personal) throws StorageException {
        int contextId = ctx.getId().intValue();
        int userId = user.getId().intValue();

        // SQL resources
        Connection con = leaseConnectionForContext(contextId, cache);
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean autocommit = false;
        try {
            con.setAutoCommit(false); // BEGIN
            autocommit = true;
            rollback = true;

            stmt = con.prepareStatement("UPDATE user_mail_account SET personal=? WHERE cid=? AND user=? AND id=?");
            if (Strings.isEmpty(personal)) {
                stmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, personal);
            }
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            stmt.setInt(4, MailAccount.DEFAULT_ID);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            con.commit(); // COMMIT
            rollback = false;

            // Invalidate cache
            try {
                MailAccountStorageService mass = AdminServiceRegistry.getInstance().getService(MailAccountStorageService.class);
                if (null != mass) {
                    mass.invalidateMailAccount(0, userId, contextId);
                } else {
                    CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != cacheService) {
                        Cache cache = cacheService.getCache("MailAccount");
                        cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), Integer.toString(0), Integer.toString(userId)));
                        cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), Integer.toString(userId)));
                        cache.invalidateGroup(ctx.getId().toString());
                    }
                }
            } catch (final OXException e) {
                LOG.error("", e);
            }

        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (rollback) {
                rollback(con);
            }
            if (autocommit) {
                autocommit(con);
            }
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    @Override
    public void changeCapabilities(final Context ctx, final User user, final Set<String> capsToAdd, final Set<String> capsToRemove, final Set<String> capsToDrop, final Credentials auth) throws StorageException {
        final int contextId = ctx.getId().intValue();
        // SQL resources
        Connection con = leaseConnectionForContext(contextId, cache);
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean autocommit = false;
        try {
            con.setAutoCommit(false); // BEGIN
            autocommit = true;
            rollback = true;
            // First drop
            if (null != capsToDrop && !capsToDrop.isEmpty()) {
                for (final String cap : capsToDrop) {
                    if (null == stmt) {
                        stmt = con.prepareStatement("DELETE FROM capability_user WHERE cid=? AND user=? AND cap=?");
                        stmt.setInt(1, contextId);
                        stmt.setInt(2, user.getId().intValue());
                    }
                    stmt.setString(3, cap);
                    stmt.addBatch();
                    if (cap.startsWith("-")) {
                        stmt.setString(3, cap.substring(1));
                        stmt.addBatch();
                    } else {
                        stmt.setString(3, "-" + cap);
                        stmt.addBatch();
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Determine what is already present
            final Set<String> existing;
            {
                stmt = con.prepareStatement("SELECT cap FROM capability_user WHERE cid=? AND user=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, user.getId().intValue());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    existing = new HashSet<>(16);
                    do {
                        existing.add(rs.getString(1));
                    } while (rs.next());
                } else {
                    existing = Collections.<String> emptySet();
                }
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
                rs = null;
            }
            final Set<String> capsToInsert = new HashSet<>(capsToAdd);
            // Delete existing ones
            if (null != capsToRemove && !capsToRemove.isEmpty()) {
                for (final String cap : capsToRemove) {
                    if (existing.contains(cap)) {
                        if (null == stmt) {
                            stmt = con.prepareStatement("DELETE FROM capability_user WHERE cid=? AND user=? AND cap=?");
                            stmt.setInt(1, contextId);
                            stmt.setInt(2, user.getId().intValue());
                        }
                        stmt.setString(3, cap);
                        stmt.addBatch();
                        existing.remove(cap);
                    }
                    final String plusCap = "+" + cap;
                    if (existing.contains(plusCap)) {
                        if (null == stmt) {
                            stmt = con.prepareStatement("DELETE FROM capability_user WHERE cid=? AND user=? AND cap=?");
                            stmt.setInt(1, contextId);
                            stmt.setInt(2, user.getId().intValue());
                        }
                        stmt.setString(3, plusCap);
                        stmt.addBatch();
                        existing.remove(plusCap);
                    }
                    final String minusCap = "-" + cap;
                    if (!existing.contains(minusCap)) {
                        capsToInsert.add(minusCap);
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Insert new ones
            if (!capsToInsert.isEmpty()) {
                if (capsToAdd != null) {
                    for (final String capToAdd : capsToAdd) {
                        final String minusCap = "-" + capToAdd;
                        if (existing.contains(minusCap)) {
                            if (null == stmt) {
                                stmt = con.prepareStatement("DELETE FROM capability_user WHERE cid=? AND user=? AND cap=?");
                                stmt.setInt(1, contextId);
                                stmt.setInt(2, user.getId().intValue());
                            }
                            stmt.setString(3, minusCap);
                            stmt.addBatch();
                        }
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                stmt = con.prepareStatement("INSERT INTO capability_user (cid, user, cap) VALUES (?, ?, ?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, user.getId().intValue());
                for (final String cap : capsToInsert) {
                    if (cap.startsWith("-")) {
                        // A capability to remove
                        stmt.setString(3, cap);
                        stmt.addBatch();
                    } else {
                        if (!existing.contains(cap) && !existing.contains("+" + cap)) {
                            // A capability to add
                            stmt.setString(3, cap);
                            stmt.addBatch();
                        }
                    }
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
            con.commit(); // COMMIT
            rollback = false;

            // Invalidate cache
            {
                CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                if (null != cacheService) {
                    try {
                        final Cache jcs = cacheService.getCache("UserConfiguration");
                        CacheKey key = jcs.newCacheKey(ctx.getId(), user.getId().toString(), String.valueOf(false));
                        jcs.remove(key);
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                    try {
                        final Cache jcs = cacheService.getCache("CapabilitiesUser");
                        jcs.removeFromGroup(user.getId(), ctx.getId().toString());
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                    try {
                        final Cache jcs = cacheService.getCache("Capabilities");
                        jcs.removeFromGroup(user.getId(), ctx.getId().toString());
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                }
            }

        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
            if (rollback) {
                rollback(con);
            }
            if (autocommit) {
                autocommit(con);
            }
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    @Override
    public void enableUser(int userId, Context ctx) throws StorageException {
        toggleUserStatus(userId, ctx, true);
    }

    @Override
    public void disableUser(int userId, Context ctx) throws StorageException {
        toggleUserStatus(userId, ctx, false);
    }

    @Override
    public void setUserEnabled(int userId, int contextId, boolean value, Connection con) throws StorageException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user SET mailEnabled = ? WHERE cid = ? AND id = ? AND mailEnabled != ?");
            stmt.setBoolean(1, value);
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            stmt.setBoolean(4, value);
            boolean changed = stmt.executeUpdate() > 0;

            if (changed) {

                // Invalidate associated sessions in case user has been disabled
                if (false == value) {
                    SessiondService sessiondService = AdminServiceRegistry.getInstance().getService(SessiondService.class);
                    if (null != sessiondService) {
                        try {
                            sessiondService.removeUserSessions(userId, ContextStorage.getInstance().getContext(contextId));
                        } catch (Exception e) {
                            LOG.error("Failed to invalidate sessions for user {} in context {}", I(userId), I(contextId), e);
                        }
                    }
                }

                // JCS
                {
                    CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != cacheService) {
                        try {
                            CacheKey key = cacheService.newCacheKey(contextId, userId);
                            Cache cache = cacheService.getCache("User");
                            cache.remove(key);
                        } catch (OXException e) {
                            LOG.error("", e);
                        }
                    }
                }
                // End of JCS
            }
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void change(final Context ctx, final User usrdata) throws StorageException {
        int contextId = ctx.getId().intValue();
        int userId = usrdata.getId().intValue();

        Connection con = leaseConnectionForContext(contextId, cache);

        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            lock(contextId, con);

            // Change attributes
            Set<String> changedAttributes = new HashSet<>();
            Collection<Runnable> pendingInvocations = new LinkedList<>();
            for (AttributeChangers attributeChangers : attributeChangers) {
                changedAttributes.addAll(attributeChangers.change(usrdata, userId, contextId, con, pendingInvocations));
            }

            // Hint for the cache when updating display name
            boolean displayNameUpdate = changedAttributes.contains("Display_name");
            Set<Integer> quotaAffectedUserIDs = changedAttributes.contains(UserAttribute.QUOTA.getName()) ? getQuotaAffectedUserIds(contextId, userId, con) : Collections.emptySet();

            if (usrdata.isConvertDriveUserFolders()) {
                convertDriveUserFolders(ctx, usrdata, con);
            }

            if (usrdata.getDisplay_name() != null) {
                // update folder name via ox api if 'display name' was changed
                final int[] changedfields = new int[] { Contact.DISPLAY_NAME };

                OXFolderAdminHelper.propagateUserModification(userId, changedfields, System.currentTimeMillis(), con, con, contextId);
            }

            storeFolderTree(ctx, con, usrdata, userId);
            // update last modified column
            changeLastModified(userId, ctx, con);

            // Fire up
            con.commit();
            rollback = false;

            // Update JCS Caches
            updateJCSCaches(ctx, usrdata, con, quotaAffectedUserIDs, displayNameUpdate);
            for (Runnable pendingInvocation : pendingInvocations) {
                executeSafe(pendingInvocation);
            }

            LOG.info("User {} in context {} changed! Changed attributes: {}", Integer.valueOf(userId), Integer.valueOf(contextId), toString(changedAttributes));
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        } catch (final ServiceException e) {
            LOG.error("Required service is missing.", e);
            throw new StorageException(e);
        } catch (final IllegalArgumentException | SecurityException | OXException e) {
            LOG.error("Error", e);
            throw new StorageException(e);
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } finally {
            if (rollback) {
                rollback(con);
            }
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    private void executeSafe(Runnable pendingInvocation) {
        try {
            pendingInvocation.run();
        } catch (Exception e) {
            LOG.error("Failed to execute pending invocation after user change: {}", pendingInvocation.getClass().getName(), e);
        }
    }

    /**
     * If the quota was changed for a user owned filestore then get all affected user ids
     *
     * @param contextId The context identifier
     * @param userId The user identifier (the filestore owner)
     * @param connection The {@link Connection}
     * @return A {@link Set} with all affected user ids, or an empty {@link Set} if no user was affected.
     * @throws StorageException if a storage error is occurred
     */
    private Set<Integer> getQuotaAffectedUserIds(int contextId, int userId, Connection connection) throws StorageException {
        ResultSet result = null;
        PreparedStatement stmt = null;
        try {
            Set<Integer> affectedUserIDs = new HashSet<>();
            stmt = connection.prepareStatement("SELECT id FROM user WHERE cid=? AND filestore_owner=?;");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            result = stmt.executeQuery();
            while (result.next()) {
                affectedUserIDs.add(Integer.valueOf(result.getInt(1)));
            }
            return affectedUserIDs;
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
    }

    /**
     * Updates the JCS caches for:
     * <ul>
     * <li>Aliases</li>
     * <li>User</li>
     * <li>UserPermissionBits</li>
     * <li>UserConfiguration</li>
     * <li>UserSettingMail</li>
     * <li>Capabilities</li>
     * <li>MailAccount</li>
     * <li>QuotaFileStorages</li>
     * <li>OXFolderCache</li>
     * <li>GlobalFolderCache</li>
     * </ul>
     *
     * @param ctx The {@link Context}
     * @param usrdata The {@link User} data
     * @param connection the {@link Connection}
     * @param quotaAffectedUserIDs The optional affected user ids after a quota change
     * @param displayNameUpdate whether the display name was updated
     * @throws OXException if an error is occurred
     */
    private void updateJCSCaches(final Context ctx, final User usrdata, Connection connection, Set<Integer> quotaAffectedUserIDs, boolean displayNameUpdate) throws OXException {
        int contextId = ctx.getId().intValue();
        int userId = usrdata.getId().intValue();

        // Invalidate alias cache
        UserAliasStorage aliasStorage = AdminServiceRegistry.getInstance().getService(UserAliasStorage.class);
        aliasStorage.invalidateAliases(ctx.getId(), userId);

        CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return;
        }

        try {
            invalidateUserCaches(userId, contextId, cacheService);
            Cache cache = cacheService.getCache("User");
            if (null != quotaAffectedUserIDs && false == quotaAffectedUserIDs.isEmpty()) {
                List<Serializable> keys = new ArrayList<>(quotaAffectedUserIDs.size());
                for (Integer userID : quotaAffectedUserIDs) {
                    keys.add(cacheService.newCacheKey(contextId, userID.intValue()));
                }
                cache.remove(keys);
            }

            MailAccountStorageService mass = AdminServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null != mass) {
                mass.invalidateMailAccount(0, userId, contextId);
            } else {
                cache = cacheService.getCache("MailAccount");
                cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), Integer.toString(0), Integer.toString(userId)));
                cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), Integer.toString(userId)));
                cache.invalidateGroup(ctx.getId().toString());
            }

            if (displayNameUpdate) {
                final int fuid = getDefaultInfoStoreFolder(usrdata, ctx, connection);
                if (fuid > 0) {
                    cache = cacheService.getCache("OXFolderCache");
                    CacheKey key = cacheService.newCacheKey(contextId, fuid);
                    cache.remove(key);
                    cache = cacheService.getCache("GlobalFolderCache");
                    key = cacheService.newCacheKey(1, "0", Integer.toString(fuid));
                    cache.removeFromGroup(key, Integer.toString(contextId));
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Converts the specified {@link Set} to string
     *
     * @param set The {@link Set} to convert
     * @return The string representation
     */
    private String toString(Set<String> set) {
        if (set.isEmpty()) {
            return "No attributes changed";
        }
        StringBuilder builder = new StringBuilder();
        for (String s : set) {
            builder.append(s).append(", ");
        }
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    private int getDefaultInfoStoreFolder(final User user, final Context ctx, final Connection con) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid=? AND created_from=? AND module=? AND default_flag > 0");
            int pos = 1;
            stmt.setInt(pos++, ctx.getId().intValue());
            stmt.setInt(pos++, user.getId().intValue());
            stmt.setInt(pos, FolderObject.INFOSTORE);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (final Exception e) {
            return -1;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public int create(final Context ctx, final User usrdata, final UserModuleAccess moduleAccess, final Connection con, final int userId, final int contactId, final int uid_number) throws StorageException {
        int contextId = ctx.getId().intValue();

        // Find file storage for user if a valid quota is specified
        Long maxQuota = usrdata.getMaxQuota();
        Integer filestoreId = null;
        if (maxQuota != null) {
            long quota_max_temp = maxQuota.longValue();
            if (quota_max_temp != -1) {
                // A valid quota is specified - ensure an appropriate file storage is set
                Integer fsId = usrdata.getFilestoreId();
                if (fsId == null || fsId.intValue() <= 0) {
                    // Auto-select next suitable file storage
                    OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
                    int fileStorageToPrefer = oxutil.getFilestoreIdFromContext(contextId);
                    Filestore filestoreForUser = oxutil.findFilestoreForUser(fileStorageToPrefer);
                    filestoreId = filestoreForUser.getId();
                    usrdata.setFilestoreId(filestoreId);
                } else {
                    if (!OXToolStorageInterface.getInstance().existsStore(i(fsId))) {
                        throw new StorageException("Filestore with identifier " + fsId + " does not exist.");
                    }
                }

                // Load it to ensure validity
                OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
                try {
                    URI uri = FileStorages.getFullyQualifyingUriForContext(ctx.getId().intValue(), oxu.getFilestoreURI(i(usrdata.getFilestoreId())));
                    FileStorages.getFileStorageService().getFileStorage(uri);
                } catch (OXException e) {
                    throw new StorageException(e.getMessage(), e);
                }
            }
        }

        PreparedStatement ps = null;
        final String LOGINSHELL = "/bin/bash";

        try {
            ps = con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid=?");
            ps.setInt(1, contextId);
            final ResultSet rs = ps.executeQuery();
            int admin_id = 0;
            boolean mustMapAdmin = false;
            if (rs.next()) {
                admin_id = rs.getInt("user");
            } else {
                admin_id = userId;
                mustMapAdmin = true;
            }
            rs.close();

            final String passwd = cache.encryptPassword(usrdata);

            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement("INSERT INTO user (cid,id,userPassword,passwordMech,shadowLastChange,mail,timeZone,preferredLanguage,mailEnabled,imapserver,smtpserver,contactId,homeDirectory,uidNumber,gidNumber,loginShell,imapLogin,filestore_id,filestore_owner,filestore_name,quota_max) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, passwd);
                stmt.setString(4, usrdata.getPasswordMech());

                if (usrdata.getPassword_expired() == null) {
                    usrdata.setPassword_expired(Boolean.FALSE);
                }
                stmt.setInt(5, getintfrombool(usrdata.getPassword_expired().booleanValue()));

                stmt.setString(6, usrdata.getPrimaryEmail());

                final String timezone = usrdata.getTimezone();
                if (null != timezone) {
                    stmt.setString(7, timezone);
                } else {
                    String fallbackTimeZone = cache.getProperties().getUserProp(AdminProperties.User.DEFAULT_TIMEZONE, DEFAULT_TIMEZONE_CREATE);
                    stmt.setString(7, fallbackTimeZone);
                }

                // language cannot be null, that's checked in checkCreateUserData()
                final String lang = usrdata.getLanguage();
                stmt.setString(8, lang);

                // mailenabled
                if (usrdata.getMailenabled() == null) {
                    usrdata.setMailenabled(Boolean.TRUE);
                }
                stmt.setBoolean(9, usrdata.getMailenabled().booleanValue());

                // imap and smtp server
                String imapServer = usrdata.getImapServerString();
                if (null == imapServer) {
                    imapServer = DEFAULT_IMAP_SERVER_CREATE;
                }
                stmt.setString(10, URIParser.parse(imapServer, URIDefaults.IMAP).toString());

                String smtpServer = usrdata.getSmtpServerString();
                if (null == smtpServer) {
                    smtpServer = DEFAULT_SMTP_SERVER_CREATE;
                }
                stmt.setString(11, URIParser.parse(smtpServer, URIDefaults.SMTP).toString());

                stmt.setInt(12, contactId);

                String homedir = "/home"; //prop.getUserProp(AdminProperties.User.HOME_DIR_ROOT, "/home");
                homedir += "/" + usrdata.getName();
                stmt.setString(13, homedir);

                if (Integer.parseInt(prop.getUserProp(AdminProperties.User.UID_NUMBER_START, "-1")) > 0) {
                    stmt.setInt(14, uid_number);
                } else {
                    stmt.setInt(14, NOBODY);
                }

                final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

                // Get user's default group ID anf check its existence
                final int def_group_id;
                {
                    final Group defaultGroup = usrdata.getDefault_group();
                    if (defaultGroup == null) {
                        // Set to context's default group
                        def_group_id = tool.getDefaultGroupForContext(ctx, con);
                    } else {
                        def_group_id = defaultGroup.getId().intValue();
                        if (!tool.existsGroup(ctx, con, def_group_id)) {
                            throw new StorageException("No such group with ID " + def_group_id + " in context " + ctx.getId());
                        }
                    }
                }
                // now check if gidnumber feature is enabled
                // if yes, update user table to correct gidnumber of users
                // default group
                if (Integer.parseInt(prop.getGroupProp(AdminProperties.Group.GID_NUMBER_START, "-1")) > 0) {
                    final int gid_number = tool.getGidNumberOfGroup(ctx, def_group_id, con);
                    if (-1 == gid_number) {
                        // Specified group does not exist
                        stmt.setInt(15, NOGROUP);
                    } else {
                        stmt.setInt(15, gid_number);
                    }
                } else {
                    stmt.setInt(15, NOGROUP);
                }

                stmt.setString(16, LOGINSHELL);

                if (usrdata.getImapLogin() != null) {
                    stmt.setString(17, usrdata.getImapLogin());
                } else {
                    stmt.setNull(17, java.sql.Types.VARCHAR);
                }

                boolean fileStorageSet = false;
                {
                    Integer fsId = usrdata.getFilestoreId();
                    if (fsId != null && -1 != fsId.intValue()) {
                        stmt.setInt(18, fsId.intValue());

                        Integer fsOwner = usrdata.getFilestoreOwner();
                        if (fsOwner != null && -1 != fsOwner.intValue()) {
                            stmt.setInt(19, fsOwner.intValue());
                        } else {
                            stmt.setInt(19, 0);
                        }

                        String filestore_name = usrdata.getFilestore_name();
                        if (null != filestore_name) {
                            stmt.setString(20, filestore_name);
                        } else {
                            stmt.setString(20, FileStorages.getNameForUser(userId, contextId));
                        }

                        fileStorageSet = true;
                    } else {
                        // No file storage information
                        stmt.setInt(18, 0);
                        stmt.setInt(19, 0);
                        stmt.setNull(20, java.sql.Types.VARCHAR);
                    }
                }

                {
                    if (null != maxQuota) {
                        long quota_max_temp = maxQuota.longValue();
                        if (quota_max_temp != -1) {
                            quota_max_temp = quota_max_temp << 20;
                        }
                        stmt.setLong(21, quota_max_temp);
                    } else {
                        stmt.setLong(21, -1);
                    }
                }

                stmt.executeUpdate();
                stmt.close();

                if (fileStorageSet) {
                    stmt = con.prepareStatement("INSERT INTO filestore_usage (cid, user, used) VALUES (?, ?, ?)");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.setLong(3, 0L);
                    stmt.executeUpdate();
                    stmt.close();
                }

                // fill up statement for prg_contacts update

                final Class<? extends User> c = usrdata.getClass();
                final Method[] theMethods = c.getMethods();

                List<MethodAndName> methodlist = getGetters(theMethods);

                StringBuilder contactInsert = new StringBuilder("INSERT INTO prg_contacts (cid,userid,creating_date,created_from,changing_date,changed_from,fid,intfield01,field90,uid,");
                StringBuilder placeHolders = new StringBuilder();
                List<Method> methodlist2 = new LinkedList<>();
                for (MethodAndName methodandname : methodlist) {
                    // First we have to check which return value we have. We have to distinguish four types.
                    final Method method = methodandname.method;
                    final String methodName = methodandname.name;
                    final Class<?> returnType = method.getReturnType();
                    if (String.class.equals(returnType)) {
                        final String result = (String) method.invoke(usrdata, (Object[]) null);
                        if (null != result) {
                            contactInsert.append(Mapper.method2field.get(methodName));
                            contactInsert.append(",");
                            placeHolders.append("?,");
                            methodlist2.add(method);
                        }
                    } else if (Integer.class.equals(returnType)) {
                        final int result = i((Integer) method.invoke(usrdata, (Object[]) null));
                        if (-1 != result) {
                            contactInsert.append(Mapper.method2field.get(methodName));
                            contactInsert.append(",");
                            placeHolders.append("?,");
                            methodlist2.add(method);
                        }
                    } else if (Boolean.class.equals(returnType)) {
                        contactInsert.append(Mapper.method2field.get(methodName));
                        contactInsert.append(",");
                        placeHolders.append("?,");
                        methodlist2.add(method);
                    } else if (Date.class.equals(returnType)) {
                        final Date result = (Date) method.invoke(usrdata, (Object[]) null);
                        if (null != result) {
                            contactInsert.append(Mapper.method2field.get(methodName));
                            contactInsert.append(",");
                            placeHolders.append("?,");
                            methodlist2.add(method);
                        }
                    } else if (Long.class.equals(returnType)) {
                        final long result = ((Long) method.invoke(usrdata, (Object[]) null)).longValue();
                        if (-1 != result) {
                            contactInsert.append(Mapper.method2field.get(methodName));
                            contactInsert.append(",");
                            placeHolders.append("?,");
                            methodlist2.add(method);
                        }
                    }
                }
                placeHolders.deleteCharAt(placeHolders.length() - 1);
                contactInsert.deleteCharAt(contactInsert.length() - 1);
                contactInsert.append(") VALUES (?,?,?,?,?,?,?,?,?,?,");
                contactInsert.append(placeHolders);
                contactInsert.append(")");
                stmt = con.prepareStatement(contactInsert.toString());
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, userId);
                long now = System.currentTimeMillis();
                stmt.setLong(pos++, now);
                stmt.setInt(pos++, userId);
                stmt.setLong(pos++, now);
                stmt.setLong(pos++, userId);
                stmt.setLong(pos++, FolderObject.SYSTEM_LDAP_FOLDER_ID);
                stmt.setInt(pos++, contactId);
                stmt.setString(pos++, usrdata.getDisplay_name());
                stmt.setString(pos++, UUID.randomUUID().toString());

                for (final Method method : methodlist2) {
                    final Class<?> returntype = method.getReturnType();
                    if (String.class.equals(returntype)) {
                        final String result = (String) method.invoke(usrdata, (Object[]) null);
                        if (null != result) {
                            stmt.setString(pos++, result);
                        } else {
                            stmt.setNull(pos++, java.sql.Types.VARCHAR);
                        }
                    } else if (Integer.class.equals(returntype)) {
                        final int result = i((Integer) method.invoke(usrdata, (Object[]) null));
                        if (-1 != result) {
                            stmt.setInt(pos++, result);
                        } else {
                            stmt.setNull(pos++, java.sql.Types.INTEGER);
                        }
                    } else if (Boolean.class.equals(returntype)) {
                        final boolean result = b((Boolean) method.invoke(usrdata, (Object[]) null));
                        stmt.setBoolean(pos++, result);
                    } else if (Date.class.equals(returntype)) {
                        final Date result = (Date) method.invoke(usrdata, (Object[]) null);
                        if (null != result) {
                            stmt.setTimestamp(pos++, new Timestamp(result.getTime()));
                        } else {
                            stmt.setNull(pos++, java.sql.Types.VARCHAR);
                        }
                    } else if (Long.class.equals(returntype)) {
                        final long result = ((Integer) method.invoke(usrdata, (Object[]) null)).longValue();
                        if (-1 != result) {
                            stmt.setLong(pos++, result);
                        } else {
                            stmt.setNull(pos++, java.sql.Types.BIGINT);
                        }
                    }
                }
                stmt.executeUpdate();
                stmt.close();

                // get mailfolder
                String std_mail_folder_sent;
                if (null != usrdata.getMail_folder_sent_name()) {
                    std_mail_folder_sent = usrdata.getMail_folder_sent_name();
                } else {
                    std_mail_folder_sent = prop.getUserProp("SENT_MAILFOLDER_" + lang.toUpperCase(), "Sent");
                }

                String std_mail_folder_trash;
                if (null != usrdata.getMail_folder_trash_name()) {
                    std_mail_folder_trash = usrdata.getMail_folder_trash_name();
                } else {
                    std_mail_folder_trash = prop.getUserProp("TRASH_MAILFOLDER_" + lang.toUpperCase(), "Trash");
                }

                String std_mail_folder_drafts;
                if (null != usrdata.getMail_folder_drafts_name()) {
                    std_mail_folder_drafts = usrdata.getMail_folder_drafts_name();
                } else {
                    std_mail_folder_drafts = prop.getUserProp("DRAFTS_MAILFOLDER_" + lang.toUpperCase(), "Drafts");
                }

                String std_mail_folder_spam;
                if (null != usrdata.getMail_folder_spam_name()) {
                    std_mail_folder_spam = usrdata.getMail_folder_spam_name();
                } else {
                    std_mail_folder_spam = prop.getUserProp("SPAM_MAILFOLDER_" + lang.toUpperCase(), "Spam");
                }

                String std_mail_folder_confirmed_spam;
                if (null != usrdata.getMail_folder_confirmed_spam_name()) {
                    std_mail_folder_confirmed_spam = usrdata.getMail_folder_confirmed_spam_name();
                } else {
                    std_mail_folder_confirmed_spam = prop.getUserProp("CONFIRMED_SPAM_MAILFOLDER_" + lang.toUpperCase(), "confirmed-spam");
                }

                String std_mail_folder_confirmed_ham;
                if (null != usrdata.getMail_folder_confirmed_ham_name()) {
                    std_mail_folder_confirmed_ham = usrdata.getMail_folder_confirmed_ham_name();
                } else {
                    std_mail_folder_confirmed_ham = prop.getUserProp("CONFIRMED_HAM_MAILFOLDER_" + lang.toUpperCase(), "confirmed-ham");
                }

                // Insert aliases
                if (usrdata.getAliases() != null && usrdata.getAliases().size() > 0) {
                    UserAliasStorage userAlias = AdminServiceRegistry.getInstance().getService(UserAliasStorage.class, true);
                    for (Iterator<String> itr = usrdata.getAliases().iterator(); itr.hasNext();) {
                        String tmp_mail = itr.next().toString().trim();
                        if (tmp_mail.length() > 0) {
                            userAlias.createAlias(con, ctx.getId().intValue(), userId, tmp_mail);
                        }
                    }
                }

                // Fill in dynamic attributes
                insertDynamicAttributes(con, contextId, userId, usrdata.getUserAttributes());

                // add user to login2user table with the internal id
                boolean autoLowerCase = cache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false);
                stmt = con.prepareStatement("INSERT INTO login2user (cid,id,uid) VALUES (?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, autoLowerCase ? usrdata.getName().toLowerCase() : usrdata.getName());
                stmt.executeUpdate();
                stmt.close();

                stmt = con.prepareStatement("INSERT INTO groups_member (cid,id,member) VALUES (?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, def_group_id);
                stmt.setInt(3, userId);
                stmt.executeUpdate();
                stmt.close();

                if (mustMapAdmin) {
                    stmt = con.prepareStatement("INSERT INTO user_setting_admin (cid,user) VALUES (?,?)");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, admin_id);
                    stmt.executeUpdate();
                    stmt.close();
                }

                // add the module access rights to the db
                final int[] groupsForUser = getGroupsForUser(ctx, userId, con);

                myChangeInsertModuleAccess(ctx, userId, moduleAccess, true, con, groupsForUser);

                // add users standard mail settings
                final StringBuffer sb = new StringBuffer("INSERT INTO user_setting_mail (cid,user,std_trash,std_sent,std_drafts,std_spam,msg_format,send_addr,bits,confirmed_spam,confirmed_ham,");
                final boolean uploadFileSizeLimitset = usrdata.getUploadFileSizeLimit() != null;
                final boolean uploadFileSizeLimitPerFileset = usrdata.getUploadFileSizeLimitPerFile() != null;
                if (uploadFileSizeLimitset) {
                    sb.append("upload_quota,");
                }
                if (uploadFileSizeLimitPerFileset) {
                    sb.append("upload_quota_per_file,");
                }
                // Remove comma
                sb.deleteCharAt(sb.length() - 1);
                sb.append(") VALUES (?,?,?,?,?,?,?,?,?,?,?,");
                if (uploadFileSizeLimitset) {
                    sb.append("?,");
                }
                if (uploadFileSizeLimitPerFileset) {
                    sb.append("?,");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append(')');
                stmt = con.prepareStatement(sb.toString());
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, userId);
                stmt.setString(pos++, std_mail_folder_trash);
                stmt.setString(pos++, std_mail_folder_sent);
                stmt.setString(pos++, std_mail_folder_drafts);
                stmt.setString(pos++, std_mail_folder_spam);
                stmt.setInt(pos++, UserSettingMail.MSG_FORMAT_BOTH);
                stmt.setString(pos++, usrdata.getDefaultSenderAddress());
                // set the flag for "receiving notifications" in the ox, was bug
                // #5336
                // TODO: choeger: Extend API to allow setting of these flags
                int flags = UserSettingMail.INT_NOTIFY_TASKS | UserSettingMail.INT_NOTIFY_APPOINTMENTS;

                if (usrdata.getGui_spam_filter_enabled() != null && usrdata.getGui_spam_filter_enabled().booleanValue()) {
                    flags |= UserSettingMail.INT_SPAM_ENABLED;
                }

                flags = checkHTMLMailAllowed(flags, usrdata, userId, contextId);

                /*-
                 * Apply other default values
                 *
                 * ( taken from '{GIT_HOME}/frontend6/open-xchange-gui/js/config/config.js' )
                 */
                flags |= UserSettingMail.INT_DISPLAY_HTML_INLINE_CONTENT;
                flags |= UserSettingMail.INT_SHOW_GRAPHIC_EMOTICONS;
                flags |= UserSettingMail.INT_USE_COLOR_QUOTE;
                flags |= UserSettingMail.INT_NOTIFY_APPOINTMENTS_CONFIRM_OWNER;
                flags |= UserSettingMail.INT_NOTIFY_TASKS_CONFIRM_OWNER;
                flags |= UserSettingMail.INT_NOTIFY_TASKS_CONFIRM_PARTICIPANT;

                stmt.setInt(pos++, flags);
                stmt.setString(pos++, std_mail_folder_confirmed_spam);
                stmt.setString(pos++, std_mail_folder_confirmed_ham);
                if (uploadFileSizeLimitset) {
                    stmt.setInt(pos++, usrdata.getUploadFileSizeLimit().intValue());
                }
                if (uploadFileSizeLimitPerFileset) {
                    stmt.setInt(pos++, usrdata.getUploadFileSizeLimitPerFile().intValue());
                }
                stmt.executeUpdate();
                stmt.close();

                // only when user is NOT the admin user, then invoke the ox api
                // directly, else
                // a context is currently in creation and we would get an error
                // by the ox api
                if (userId != admin_id) {
                    final OXFolderAdminHelper oxa = new OXFolderAdminHelper();
                    OXFolderDefaultMode folderCreationMode = getFolderCreationMode(usrdata);
                    oxa.addUserToOXFolders(userId, usrdata.getDisplay_name(), lang, contextId, con, folderCreationMode);
                }
            } finally {
                Databases.closeSQLStuff(stmt);
            }
            // Write primary mail account.
            createPrimaryMailAccount(ctx, con, usrdata, userId);
            // Write GUI configuration to database.
            storeUISettings(ctx, con, usrdata, userId);
            // Set wanted folder tree.
            storeFolderTree(ctx, con, usrdata, userId);
            // Remember filestore-to-user association
            if (null != filestoreId) {
                Filestore2UserUtil.addFilestore2UserEntry(contextId, userId, filestoreId.intValue(), ClientAdminThreadExtended.cache);
            }
            return userId;
        } catch (final ServiceException e) {
            LOG.error("Required service not found.", e);
            throw new StorageException(e.toString());
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException e) {
            throw new StorageException(e.toString(), e);
        } catch (final OXException e) {
            throw new StorageException(e.toString(), e);
        } catch (final NoSuchAlgorithmException e) {
            // Here we throw without rollback, because at the point this
            // exception is thrown
            // no database activity has happened
            throw new StorageException(e);
        } catch (final UnsupportedEncodingException e) {
            // Here we throw without rollback, because at the point this
            // exception is thrown
            // no database activity has happened
            throw new StorageException(e);
        } catch (final IllegalArgumentException e) {
            LOG.error("IllegalArgument Error", e);
            throw new StorageException(e);
        } catch (final IllegalAccessException e) {
            LOG.error("IllegalAccess Error", e);
            throw new StorageException(e);
        } catch (final InvocationTargetException e) {
            LOG.error("InvocationTarget Error", e);
            throw new StorageException(e);
        } catch (final URISyntaxException e) {
            LOG.error("InvocationTarget Error", e);
            throw new StorageException(e);
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } finally {
            Databases.closeSQLStuff(ps);
        }
    }

    /**
     * Check if HTML content is allowed to be displayed by default.
     * Follow the scheme:
     * 1) passed value for {@link User#isRemoteHtmlLoadingAllowed()} (introduced with MW-988)
     * 2) passed value for MAIL_ALLOW_HTML_CONTENT_BY_DEFAULT
     * 3) value for new parameter from config cascade
     * 4) false as fallback
     * 
     * @param flags The flag bitmap
     * @param usrdata The user data
     * @param userId The ID of the user to be created
     * @param contextId The ID of the context, the user gets created in
     * @return The flag bitmap
     */
    private int checkHTMLMailAllowed(int flags, User usrdata, int userId, int contextId) {
        Boolean isAllowed = null;

        if ((null != usrdata.isRemoteHtmlLoadingAllowed() && usrdata.isRemoteHtmlLoadingAllowed().booleanValue()) || Boolean.parseBoolean(prop.getUserProp("MAIL_ALLOW_HTML_CONTENT_BY_DEFAULT", "true").trim())) {
            isAllowed = Boolean.TRUE;
        } else {
            isAllowed = getConfigViewValue(userId, contextId, "com.openexchange.mail.isHTMLMailAllowed", null);
        }
        if (null != isAllowed && isAllowed.booleanValue()) {
            flags |= UserSettingMail.INT_ALLOW_HTML_IMAGES;
        }
        return flags;
    }

    /**
     * Get a {@link Boolean} for the specified property name
     * 
     * @param userId The ID of the user to be created
     * @param contextId The ID of the context, the user gets created in
     * @param propertyName The name of the property to get
     * @param defaultValue The default value for the boolean to use
     * @return A {@link Boolean} or the default value
     */
    private Boolean getConfigViewValue(int userId, int contextId, String propertyName, Boolean defaultValue) {
        ConfigViewFactory viewFactory = AdminServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (viewFactory != null) {
            try {
                ConfigView view = viewFactory.getView(userId, contextId);
                return view.get(propertyName, Boolean.class);
            } catch (OXException e) {
                if (ContextExceptionCodes.NOT_FOUND.equals(e)) {
                    // Context is about being created...
                } else {
                    LOG.warn("Unable to load {}.", propertyName);
                }
            }
        }
        return defaultValue;
    }

    private OXFolderDefaultMode getFolderCreationMode(final User usrdata) {
        OXFolderDefaultMode folderCreationMode = OXFolderDefaultMode.DEFAULT;
        if (usrdata.isDriveFolderModeSet()) {
            folderCreationMode = OXFolderDefaultMode.fromString(usrdata.getDriveFolderMode());
        }
        return folderCreationMode;
    }

    private void insertDynamicAttributes(final Connection write_ox_con, final int cid, final int userId, final Map<String, Map<String, String>> dynamicValues) throws SQLException {
        PreparedStatement stmt = null;

        try {
            stmt = write_ox_con.prepareStatement("INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?, ?, ?, ?, ?)");
            stmt.setInt(1, cid);
            stmt.setInt(2, userId);
            for (Map.Entry<String, Map<String, String>> namespaced : dynamicValues.entrySet()) {
                String namespace = namespaced.getKey();
                StringBuilder nameBuilder = null;
                for (Map.Entry<String, String> pair : namespaced.getValue().entrySet()) {
                    if (null == nameBuilder) {
                        nameBuilder = new StringBuilder(48);
                    } else {
                        nameBuilder.setLength(0);
                    }
                    String name = nameBuilder.append(namespace).append('/').append(pair.getKey()).toString();
                    String value = pair.getValue();
                    stmt.setString(3, name);
                    stmt.setString(4, value);
                    UUID uuid = UUID.randomUUID();
                    byte[] uuidBinary = UUIDs.toByteArray(uuid);
                    stmt.setBytes(5, uuidBinary);
                    stmt.executeUpdate();
                }
            }

        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void createPrimaryMailAccount(final Context ctx, final Connection con, final User user, final int userId) throws StorageException, OXException {
        // Loading a context is not possible if here the primary mail account for the admin is created.
        final com.openexchange.groupware.contexts.Context context = new ContextImpl(ctx.getId().intValue());
        final MailAccountStorageService mass = AdminServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
        final MailAccountDescription account = new MailAccountDescription();
        account.setDefaultFlag(true);
        account.setName((!user.isPrimaryAccountNameSet()) || Strings.isEmpty(user.getPrimaryAccountName()) ? MailFolder.DEFAULT_FOLDER_NAME : user.getPrimaryAccountName());
        String imapServer = user.getImapServerString();
        if (null == imapServer) {
            imapServer = DEFAULT_IMAP_SERVER_CREATE;
        }
        try {
            account.setMailServer(URIParser.parse(imapServer, URIDefaults.IMAP));
        } catch (final URISyntaxException e) {
            LOG.error("Problem storing the primary mail account.", e);
            throw new StorageException(e.toString());
        }
        account.setLogin(null == user.getImapLogin() ? "" : user.getImapLogin());
        account.setPrimaryAddress(user.getPrimaryEmail());
        {
            Boolean check = getConfigViewValue(userId, ctx.getId().intValue(), "com.openexchange.mail.useStaticDefaultFolders", Boolean.FALSE);

            if (check != null && check) {
                String lang = user.getLanguage().toUpperCase();
                // Drafts
                String defaultName = prop.getUserProp("DRAFTS_MAILFOLDER_" + lang, "Drafts");
                account.setDrafts(null == user.getMail_folder_drafts_name() ? defaultName : user.getMail_folder_drafts_name());
                // Sent
                defaultName = prop.getUserProp("SENT_MAILFOLDER_" + lang, "Sent");
                account.setSent(null == user.getMail_folder_sent_name() ? defaultName : user.getMail_folder_sent_name());
                // Spam/Junk
                defaultName = prop.getUserProp("SPAM_MAILFOLDER_" + lang, "Spam");
                account.setSpam(null == user.getMail_folder_spam_name() ? defaultName : user.getMail_folder_spam_name());
                // Trash
                defaultName = prop.getUserProp("TRASH_MAILFOLDER_" + lang, "Trash");
                account.setTrash(null == user.getMail_folder_trash_name() ? defaultName : user.getMail_folder_trash_name());
                // Confirmed-ham
                defaultName = prop.getUserProp("CONFIRMED_HAM_MAILFOLDER_" + lang, "confirmed-ham");
                account.setConfirmedHam(null == user.getMail_folder_confirmed_ham_name() ? defaultName : user.getMail_folder_confirmed_ham_name());
                // Confirmed-spam
                defaultName = prop.getUserProp("CONFIRMED_SPAM_MAILFOLDER_" + lang, "confirmed-spam");
                account.setConfirmedSpam(null == user.getMail_folder_confirmed_spam_name() ? defaultName : user.getMail_folder_confirmed_spam_name());
            } else {
                // Drafts
                account.setDrafts(user.getMail_folder_drafts_name());
                // Sent
                account.setSent(user.getMail_folder_sent_name());
                // Spam/Junk
                account.setSpam(user.getMail_folder_spam_name());
                // Trash
                account.setTrash(user.getMail_folder_trash_name());
                // Confirmed-ham
                account.setConfirmedHam(user.getMail_folder_confirmed_ham_name());
                // Confirmed-spam
                account.setConfirmedSpam(user.getMail_folder_confirmed_spam_name());
            }
        }
        {
            String archiveFullname = user.getMail_folder_archive_full_name();
            if (null != archiveFullname) {
                account.setArchiveFullname(archiveFullname);
            }
        }
        account.setSpamHandler(SpamHandler.SPAM_HANDLER_FALLBACK);
        String smtpServer = user.getSmtpServerString();
        if (null == smtpServer) {
            smtpServer = DEFAULT_SMTP_SERVER_CREATE;
        }
        try {
            account.setTransportServer(URIParser.parse(smtpServer, URIDefaults.SMTP));
        } catch (final URISyntaxException e) {
            LOG.error("Problem storing the primary mail account.", e);
            throw new StorageException(e.toString());
        }
        try {
            mass.insertMailAccount(account, userId, context, null, con);
            lockTable("updateTask", con);
        } catch (final OXException e) {
            throw new StorageException("Problem storing the primary mail account.", e);
        } catch (SQLException e) {
            throw new StorageException("Problem storing the primary mail account.", e);
        }
    }

    private void lockTable(String table, Connection con) throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute("SELECT 1 FROM " + table + " FOR UPDATE");
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void storeUISettings(final Context ctx, final Connection con, final User user, final int userId) {
        final SettingStorage settStor = SettingStorage.getInstance(ctx.getId().intValue(), userId);
        final Map<String, String> guiPreferences = user.getGuiPreferences();
        if (guiPreferences != null) {
            final Iterator<Entry<String, String>> iter = guiPreferences.entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<String, String> entry = iter.next();
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (null != key && null != value) {
                    try {
                        final Setting setting = ConfigTree.getInstance().getSettingByPath(key);
                        setting.setSingleValue(value);
                        settStor.save(con, setting);
                    } catch (final OXException e) {
                        LOG.error("Problem while storing GUI preferences.", e);
                    }
                }
            }
        }
    }

    private void storeFolderTree(final Context ctx, final Connection con, final User user, final int userId) throws OXException {
        if (!user.isFolderTreeSet()) {
            return;
        }
        final Integer folderTree = user.getFolderTree();
        if (null == folderTree) {
            return;
        }
        ServerUserSetting.getInstance(con).setFolderTree(i(ctx.getId()), userId, folderTree);
    }

    private int nextId(final int contextId, final int type, final Connection con) throws SQLException {
        boolean rollback = false;
        try {
            // BEGIN
            con.setAutoCommit(false);
            rollback = true;
            // Acquire next available identifier
            final int id = IDGenerator.getId(contextId, type, con);
            // COMMIT
            con.commit();
            rollback = false;
            return id;
        } finally {
            if (rollback) {
                rollback(con);
            }
        }
    }

    private void lock(final int contextId, final Connection con) throws SQLException {
        if (null == con) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            if (con.getAutoCommit()) {
                throw new SQLException("Connection is not in transaction state.");
            }
            stmt = con.prepareStatement("SELECT COUNT(*) FROM user WHERE cid=? FOR UPDATE");
            stmt.setInt(1, contextId);
            stmt.executeQuery();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public int create(final Context ctx, final User usrdata, final UserModuleAccess moduleAccess) throws StorageException {
        int context_id = ctx.getId().intValue();
        Connection write_ox_con = leaseConnectionForContext(context_id, cache);
        boolean rollback = false;
        try {
            final int internal_user_id = nextId(context_id, com.openexchange.groupware.Types.PRINCIPAL, write_ox_con);
            final int contact_id = nextId(context_id, com.openexchange.groupware.Types.CONTACT, write_ox_con);
            final int uid_number = (Integer.parseInt(prop.getUserProp(AdminProperties.User.UID_NUMBER_START, "-1")) > 0) ? nextId(context_id, com.openexchange.groupware.Types.UID_NUMBER, write_ox_con) : -1;

            write_ox_con.setAutoCommit(false);
            rollback = true;

            lock(context_id, write_ox_con);

            final int userId = create(ctx, usrdata, moduleAccess, write_ox_con, internal_user_id, contact_id, uid_number);
            write_ox_con.commit();
            rollback = false;
            LOG.info("User {} created!", Integer.toString(userId));
            return userId;
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException sql) {
            LOG.error("SQL Error", sql);
            throw new StorageException(sql.toString());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } finally {
            if (rollback) {
                Databases.rollback(write_ox_con);
            }
            autocommit(write_ox_con);
            releaseWriteContextConnection(write_ox_con, ctx, cache);
        }
    }

    @Override
    public int[] getAll(Context ctx) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection read_ox_con = leaseConnectionForContext(contextId, cache);
        try {
            return getAll(ctx, read_ox_con);
        } finally {
            releaseWriteContextConnectionAfterReading(read_ox_con, contextId, cache);
        }
    }

    @Override
    public int[] getAll(Context ctx, Connection read_ox_con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = read_ox_con.prepareStatement("SELECT con.userid FROM prg_contacts con JOIN login2user lu ON con.userid = lu.id WHERE con.cid = ? AND con.cid = lu.cid AND (lu.uid LIKE '%' OR con.field01 LIKE '%')");
            stmt.setInt(1, ctx.getId().intValue());
            rs = stmt.executeQuery();

            List<Integer> list = new LinkedList<>();
            while (rs.next()) {
                list.add(Integer.valueOf(rs.getInt(1)));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            Iterator<Integer> iter = list.iterator();
            int size = list.size();
            int[] retval = new int[size];
            for (int i = 0; i < size; i++) {
                retval[i] = iter.next().intValue();
            }
            return retval;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public User[] listUsersWithOwnFilestore(final Context context, final Integer filestore_id) throws StorageException {
        int contextId = context.getId().intValue();

        Connection read_ox_con = leaseConnectionForContext(contextId, cache);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT us.id FROM user us LEFT JOIN login2user lu ON us.id = lu.id AND us.cid = lu.cid ").append("LEFT JOIN prg_contacts con ON us.id = con.userid AND us.cid = con.cid WHERE us.cid = ? ");

            if (filestore_id != null) {
                sb.append("AND us.filestore_id = ?");
            } else {
                sb.append("AND us.filestore_id != 0");
            }

            String query = sb.toString();
            stmt = read_ox_con.prepareStatement(query);
            stmt.setInt(1, contextId);
            if (filestore_id != null) {
                stmt.setInt(2, filestore_id.intValue());
            }
            rs = stmt.executeQuery();
            List<User> retval = new LinkedList<>();
            while (rs.next()) {
                retval.add(new User(rs.getInt(1)));
            }
            return retval.toArray(new User[retval.size()]);
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseWriteContextConnectionAfterReading(read_ox_con, contextId, cache);
        }
    }

    @Override
    public User[] list(final Context ctx, final String search_pattern) throws StorageException {
        return list(ctx, search_pattern, false, false);
    }

    @Override
    public User[] list(final Context ctx, final String search_pattern, final boolean includeGuests, final boolean excludeUsers) throws StorageException {
        return listInternal(ctx, search_pattern, false, includeGuests, excludeUsers);
    }

    @Override
    public User[] listCaseInsensitive(final Context ctx, final String search_pattern) throws StorageException {
        return listCaseInsensitive(ctx, search_pattern, false, false);
    }

    @Override
    public User[] listCaseInsensitive(final Context ctx, final String search_pattern, final boolean includeGuests, final boolean excludeUsers) throws StorageException {
        return listInternal(ctx, search_pattern, true, includeGuests, excludeUsers);
    }

    private User[] listInternal(final Context ctx, final String search_pattern, final boolean ignoreCase, final boolean includeGuests, final boolean excludeUsers) throws StorageException {
        int contextId = ctx.getId().intValue();
        String new_search_pattern = null;
        boolean pattern = false;
        if (null != search_pattern) {
            new_search_pattern = search_pattern.replace('*', '%');
            pattern = !"%".equals(new_search_pattern);
        }

        Connection read_ox_con = leaseConnectionForContext(contextId, cache);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = buildQuery(new_search_pattern, ignoreCase, includeGuests, excludeUsers);
            stmt = read_ox_con.prepareStatement(sql);
            stmt.setInt(1, contextId);
            if (pattern) {
                stmt.setString(2, new_search_pattern);
                stmt.setString(3, new_search_pattern);
            }
            rs = stmt.executeQuery();
            List<User> retval = new LinkedList<>();
            while (rs.next()) {
                retval.add(new User(rs.getInt(1)));
            }
            return retval.toArray(new User[retval.size()]);
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseWriteContextConnectionAfterReading(read_ox_con, contextId, cache);
        }
    }

    private String buildQuery(String search_pattern, boolean ignoreCase, boolean includeGuests, boolean excludeUsers) {
        StringBuilder sb = new StringBuilder();
        if (!includeGuests) {
            sb.append("SELECT con.userid FROM prg_contacts con JOIN login2user lu ON con.userid = lu.id AND con.cid = lu.cid ").append("JOIN user us ON con.userid = us.id AND us.cid = con.cid ").append("WHERE con.cid = ?");
            if (excludeUsers) {
                sb.append(" AND us.guestCreatedBy > 0");
            }
            if (null != search_pattern && !"%".equals(search_pattern)) {
                if (ignoreCase) {
                    sb.append(" AND (lower(lu.uid) LIKE lower(?) OR lower(con.field01) LIKE lower(?))");
                } else {
                    sb.append(" AND (lu.uid LIKE ? OR con.field01 LIKE ?)");
                }
            }
        } else {
            sb.append("SELECT us.id FROM user us LEFT JOIN login2user lu ON us.id = lu.id AND us.cid = lu.cid ").append("LEFT JOIN prg_contacts con ON us.id = con.userid AND us.cid = con.cid WHERE us.cid = ?");
            if (excludeUsers) {
                sb.append(" AND us.guestCreatedBy > 0");
            }
            if (null != search_pattern && !"%".equals(search_pattern)) {
                if (ignoreCase) {
                    sb.append(" AND (lower(lu.uid) LIKE lower(?) ").append("OR lower(con.field01) LIKE lower(?))");
                } else {
                    sb.append(" AND (lu.uid LIKE ? ").append("OR con.field01 LIKE ?)");
                }
            }
        }
        return sb.toString();
    }

    /**
     * read all gui related settings from the configtree
     *
     * @param ctx
     * @param user
     * @param con
     * @return Map containing the GUI configuration, null if no settings found
     * @throws OXException
     * @throws SettingException
     */
    private Map<String, String> readGUISettings(final Context ctx, final User user, final Connection con) throws OXException {
        Map<String, String> ret = null;
        final int id = user.getId().intValue();
        final SettingStorage settStor = SettingStorage.getInstance(ctx.getId().intValue(), id);

        for (final String p : new String[] { "/gui", "/fastgui" }) {
            final Setting s = ConfigTree.getInstance().getSettingByPath(p);
            if (s != null) {
                settStor.readValues(con, s);
                if (ret == null) {
                    ret = new HashMap<>();
                }
                final String value = (String) s.getSingleValue();
                if (value != null) {
                    ret.put(p, value);
                }
            }
        }

        final Setting[] modules = ConfigTree.getInstance().getSettingByPath("modules").getElements();
        for (Setting module : modules) {
            final Setting guiSetting = module.getElement("gui");
            if (guiSetting != null) {
                final String path = module.getPath() + "/gui";
                settStor.readValues(con, guiSetting);
                if (ret == null) {
                    ret = new HashMap<>();
                }
                final String value = (String) guiSetting.getSingleValue();
                if (value != null) {
                    ret.put(path, value);
                }
            }
        }
        return ret;
    }

    @Override
    public User[] getData(Context ctx, User[] users) throws StorageException {
        final int contextId = i(ctx.getId());
        final Class<User> c = User.class;
        final Method[] theMethods = c.getMethods();
        final List<Method> list = new LinkedList<>();
        final HashSet<String> notallowed = new HashSet<>(9);

        // Define all those fields which are contained in the user table
        notallowed.add("setMailFolderDrafts");
        notallowed.add("setMailFolderSent");
        notallowed.add("setMailFolderSpam");
        notallowed.add("setMailFolderTrash");
        notallowed.add("setMailFolderConfirmedSpam");
        notallowed.add("setMailFolderConfirmedHam");

        // TODO: load data for guests too
        final StringBuilder query = new StringBuilder("SELECT ");

        for (final Method method : theMethods) {
            final String methodname = method.getName();

            if (methodname.startsWith("set")) {
                if (!notallowed.contains(methodname)) {
                    final String fieldname = Mapper.method2field.get(methodname.substring(3));
                    if (null != fieldname) {
                        list.add(method);
                        query.append(fieldname);
                        query.append(", ");
                    }
                }
            }
        }
        // query.deleteCharAt(query.length() - 1);
        query.delete(query.length() - 2, query.length() - 1);

        query.append(" FROM user JOIN login2user USING (cid,id) JOIN prg_contacts ON (user.cid=prg_contacts.cid AND user.id=prg_contacts.userid) ");
        query.append("WHERE user.cid = ? AND user.id = ?");

        Connection read_ox_con = leaseConnectionForContext(contextId, cache);
        PreparedStatement stmtuid = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmtid = null;
        PreparedStatement stmtuserattributes = null;
        PreparedStatement stmtusm = null;
        PreparedStatement stmtacc = null;
        List<User> userlist = new LinkedList<>();

        try {
            final OXToolStorageInterface oxtool = OXToolStorageInterface.getInstance();
            final int adminForContext = oxtool.getAdminForContext(ctx, read_ox_con);

            boolean autoLowerCase = cache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false);

            stmt2 = read_ox_con.prepareStatement(query.toString());
            stmt2.setInt(1, contextId);
            stmtuserattributes = read_ox_con.prepareStatement("SELECT name, value FROM user_attribute WHERE cid=? and id=?");
            stmtuserattributes.setInt(1, contextId);
            stmtusm = read_ox_con.prepareStatement("SELECT std_trash,std_sent,std_drafts,std_spam,confirmed_spam,confirmed_ham,bits,send_addr,upload_quota,upload_quota_per_file FROM user_setting_mail WHERE cid = ? and user = ?");
            stmtusm.setInt(1, contextId);
            stmtacc = read_ox_con.prepareStatement("SELECT trash,sent,drafts,spam,confirmed_spam,confirmed_ham,archive_fullname, name FROM user_mail_account WHERE cid = ? and id = " + MailAccount.DEFAULT_ID + " and user = ?");
            stmtacc.setInt(1, contextId);
            ResultSet rs = null;
            UserAliasStorage aliasStorage = AdminServiceRegistry.getInstance().getService(UserAliasStorage.class, true);
            for (final User user : users) {
                int user_id = user.getId().intValue();
                final User newuser = (User) user.clone();
                String username = user.getName();
                if (autoLowerCase && null != username) {
                    username = username.toLowerCase();
                }

                final Map<String, String> guiPrefs = readGUISettings(ctx, newuser, read_ox_con);

                if (guiPrefs != null) {
                    final String un = username;
                    LOG.debug("{}", new Object() {

                        @Override
                        public String toString() {
                            StringBuilder stringBuilder = new StringBuilder("User: ");
                            stringBuilder.append(un);
                            Iterator<Entry<String, String>> i = guiPrefs.entrySet().iterator();
                            while (i.hasNext()) {
                                Entry<String, String> entry = i.next();
                                String key = entry.getKey();
                                String value = entry.getValue();
                                stringBuilder.append('\t').append(key).append('=').append(value).append('\n');
                            }
                            return stringBuilder.toString();
                        }
                    });
                    newuser.setGuiPreferences(guiPrefs);
                }

                if (-1 != user_id) {
                    // TODO: Why do we make this clause?
                    if (null == username) {
                        stmtuid = read_ox_con.prepareStatement("SELECT uid FROM login2user WHERE cid = ? AND id = ?");
                        stmtuid.setInt(1, contextId);
                        stmtuid.setInt(2, user_id);
                        rs = stmtuid.executeQuery();
                        if (rs.next()) {
                            username = rs.getString("uid");
                            user.setName(username);
                        }
                        rs.close();
                        stmtuid.close();
                        stmtuid = null;
                    }
                    stmt2.setInt(2, user_id);
                } else if (null != user.getName()) {
                    String name = autoLowerCase ? user.getName().toLowerCase() : user.getName();
                    stmtid = read_ox_con.prepareStatement("SELECT id FROM login2user WHERE cid = ? AND uid = ?");
                    stmtid.setInt(1, contextId);
                    stmtid.setString(2, name);
                    rs = stmtid.executeQuery();
                    if (rs.next()) {
                        user_id = rs.getInt("id");
                    }
                    rs.close();
                    stmtid.close();
                    stmtid = null;

                    stmt2.setInt(2, user_id);
                } else {
                    throw new StorageException("Neither user name nor user id given");
                }
                newuser.setName(username);

                rs = stmt2.executeQuery();
                if (rs.next()) {
                    for (final Method method : list) {
                        final String methodnamewithoutset = method.getName().substring(3);
                        final String fieldname = Mapper.method2field.get(methodnamewithoutset);
                        final String paramtype = method.getParameterTypes()[0].getName();
                        if (paramtype.equalsIgnoreCase("java.lang.String")) {
                            final String fieldvalue = rs.getString(fieldname);
                            method.invoke(newuser, fieldvalue);
                        } else if (paramtype.equalsIgnoreCase("java.lang.Integer")) {
                            method.invoke(newuser, Integer.valueOf(rs.getInt(fieldname)));
                        } else if (paramtype.equalsIgnoreCase("java.lang.Boolean")) {
                            if (methodnamewithoutset.equals(Mapper.PASSWORD_EXPIRED)) {
                                method.invoke(newuser, Boolean.valueOf(getboolfromint(rs.getInt(fieldname))));
                            } else {
                                method.invoke(newuser, Boolean.valueOf(rs.getBoolean(fieldname)));
                            }

                        } else if (paramtype.equalsIgnoreCase("java.lang.Long")) {
                            long longValue = rs.getLong(fieldname);
                            if ("MaxQuota".equals(methodnamewithoutset)) {
                                if (rs.wasNull()) {
                                    longValue = -1;
                                } else if (longValue != -1) {
                                    longValue = longValue >> 20;
                                }
                            }
                            method.invoke(newuser, Long.valueOf(longValue));
                        } else if (paramtype.equalsIgnoreCase("java.util.Date")) {
                            final Date fieldvalue = rs.getTimestamp(fieldname);
                            method.invoke(newuser, fieldvalue);
                        } else if (paramtype.equalsIgnoreCase("java.util.Locale")) {
                            final String locale = rs.getString(fieldname);
                            final Locale loc = new Locale(locale.substring(0, 2), locale.substring(3, 5));
                            method.invoke(newuser, loc);
                        } else if (paramtype.equalsIgnoreCase("java.util.TimeZone")) {
                            final String fieldvalue = rs.getString(fieldname);
                            method.invoke(newuser, TimeZone.getTimeZone(fieldvalue));
                        }
                    }
                }
                rs.close();

                //
                Set<String> aliases = aliasStorage.getAliases(contextId, user_id);
                if (aliases != null && false == aliases.isEmpty()) {
                    for (String alias : aliases) {
                        newuser.addAlias(alias);
                    }
                }

                stmtuserattributes.setInt(2, user_id);
                rs = stmtuserattributes.executeQuery();
                while (rs.next()) {
                    final String name = rs.getString("name");
                    final String value = rs.getString("value");
                    if (isDynamicAttribute(name)) {
                        final String[] namespaced = parseDynamicAttribute(name);
                        newuser.setUserAttribute(namespaced[0], namespaced[1], value);
                    }
                }
                rs.close();

                stmtacc.setInt(2, user_id);
                rs = stmtacc.executeQuery();
                if (rs.next()) {
                    newuser.setMail_folder_drafts_name(rs.getString("drafts"));
                    newuser.setMail_folder_sent_name(rs.getString("sent"));
                    newuser.setMail_folder_spam_name(rs.getString("spam"));
                    newuser.setMail_folder_trash_name(rs.getString("trash"));
                    newuser.setMail_folder_archive_full_name(rs.getString("archive_fullname"));
                    newuser.setMail_folder_confirmed_ham_name(rs.getString("confirmed_ham"));
                    newuser.setMail_folder_confirmed_spam_name(rs.getString("confirmed_spam"));
                    newuser.setPrimaryAccountName(rs.getString("name"));
                }
                rs.close();

                stmtusm.setInt(2, user_id);
                rs = stmtusm.executeQuery();
                if (rs.next()) {
                    int bits = rs.getInt("bits");
                    if ((bits & UserSettingMail.INT_SPAM_ENABLED) == UserSettingMail.INT_SPAM_ENABLED) {
                        newuser.setGui_spam_filter_enabled(Boolean.TRUE);
                    } else {
                        newuser.setGui_spam_filter_enabled(Boolean.FALSE);
                    }
                    newuser.setDefaultSenderAddress(rs.getString("send_addr"));
                    newuser.setUploadFileSizeLimit(Integer.valueOf(rs.getInt("upload_quota")));
                    newuser.setUploadFileSizeLimitPerFile(Integer.valueOf(rs.getInt("upload_quota_per_file")));
                }
                rs.close();

                {
                    PreparedStatement ps = null;
                    ResultSet result = null;
                    try {
                        ps = read_ox_con.prepareStatement("SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ? AND filestore_usage.user = ?");
                        ps.setInt(1, contextId);
                        ps.setInt(2, user_id);
                        result = ps.executeQuery();
                        if (result.next()) {
                            long usedQuota = result.getLong(1);
                            usedQuota = usedQuota >> 20;
                            newuser.setUsedQuota(Long.valueOf(usedQuota));
                        }
                    } finally {
                        Databases.closeSQLStuff(result, ps);
                    }
                }

                newuser.setContextadmin(newuser.getId().intValue() == adminForContext);
                userlist.add(newuser);
            }

            return userlist.toArray(new User[userlist.size()]);
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (IllegalArgumentException e) {
            LOG.error("Error", e);
            throw new StorageException(e);
        } catch (IllegalAccessException e) {
            LOG.error("Error", e);
            throw new StorageException(e);
        } catch (InvocationTargetException e) {
            LOG.error("Error", e);
            throw new StorageException(e);
        } catch (CloneNotSupportedException e) {
            LOG.error("Error", e);
            throw new StorageException(e);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (OXException e) {
            LOG.error("GUI setting Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(stmtuid, stmt2, stmtid, stmtuserattributes, stmtusm, stmtacc);
            releaseWriteContextConnectionAfterReading(read_ox_con, contextId, cache);
        }
    }

    /**
     * Parses a dynamic attribute from the user_attribute table
     * Returns a String[] with retval[0] being the namespace and retval[1] being the name
     *
     * @throws StorageException
     */
    private String[] parseDynamicAttribute(final String name) throws StorageException {
        final int pos = name.indexOf('/');
        if (pos == -1) {
            throw new StorageException("Could not parse dynamic attribute name: " + name);
        }
        final String[] parsed = new String[2];
        parsed[0] = name.substring(0, pos);
        parsed[1] = name.substring(pos + 1);
        return parsed;
    }

    private boolean isDynamicAttribute(final String name) {
        return name.indexOf('/') >= 0;
    }

    @Override
    public void delete(final Context ctx, final User[] users, Integer destUser, final Connection write_ox_con) throws StorageException {
        PreparedStatement stmt = null;
        try {
            // delete all users
            int contextId = ctx.getId().intValue();
            for (User user : users) {
                int userId = user.getId().intValue();

                {
                    DeleteEvent delev = DeleteEvent.createDeleteEventForUserDeletion(this, userId, 0, ContextStorage.getInstance().getContext(contextId), destUser);
                    DeleteRegistry.getInstance().fireDeleteEvent(delev, write_ox_con, write_ox_con);
                }

                com.openexchange.groupware.ldap.User gwUser = null;
                try {
                    com.openexchange.groupware.contexts.Context gwCtx = AdminServiceRegistry.getInstance().getService(ContextService.class).getContext(contextId);
                    gwUser = AdminServiceRegistry.getInstance().getService(UserService.class).getUser(userId, gwCtx);
                } catch (Exception e) {
                    LOG.error("Failed to load groupware user.", e);
                }

                if (null != gwUser) {
                    int filestoreId = gwUser.getFilestoreId();
                    if (filestoreId > 0) {
                        int owner = gwUser.getFileStorageOwner();
                        if (owner <= 0 || owner == userId) {
                            // Delete file storage
                            QuotaFileStorageService qfsService = FileStorages.getQuotaFileStorageService();
                            QuotaFileStorage quotaFileStorage = qfsService.getQuotaFileStorage(userId, contextId, Info.administrative());

                            try {
                                quotaFileStorage.remove();
                            } catch (Exception e) {
                                // Try hard-delete
                                URI storageURI = quotaFileStorage.getUri();
                                if ("file".equalsIgnoreCase(storageURI.getScheme())) {
                                    FileUtils.deleteDirectory(new File(storageURI));
                                } else {
                                    throw new StorageException("Can't hard-delete non-local file store at \"" + storageURI + "\"");
                                }
                            }
                        }
                    }
                }

                LOG.debug("Delete user {}({}) from login2user...", user.getId(), ctx.getId());
                stmt = write_ox_con.prepareStatement("DELETE FROM login2user WHERE cid = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();
                LOG.debug("Delete user {}({}) from groups member...", user.getId(), ctx.getId());
                stmt = write_ox_con.prepareStatement("DELETE FROM groups_member WHERE cid = ? AND member = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();
                LOG.debug("Delete user {}({}) from user attribute ...", user.getId(), ctx.getId());
                stmt = write_ox_con.prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();
                LOG.debug("Delete user {}({}) from user mail setting...", user.getId(), ctx.getId());
                stmt = write_ox_con.prepareStatement("DELETE FROM user_setting_mail WHERE cid = ? AND user = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();

                // delete from user_setting_admin if user is mailadmin
                final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
                if (userId == tools.getAdminForContext(ctx, write_ox_con)) {
                    stmt = write_ox_con.prepareStatement("DELETE FROM user_setting_admin WHERE cid = ? AND user = ?");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                    stmt.close();
                }

                stmt = write_ox_con.prepareStatement("DELETE FROM user_setting WHERE cid = ? AND user_id = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();

                stmt = write_ox_con.prepareStatement("DELETE FROM filestore_usage WHERE cid = ? AND user = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();

                // when table ready, enable this
                createRecoveryData(ctx, userId, write_ox_con);
                LOG.debug("Delete user {}({}) from user ...", user.getId(), ctx.getId());
                stmt = write_ox_con.prepareStatement("DELETE FROM user WHERE cid = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();

                LOG.debug("Delete user {}({}) from contacts ...", user.getId(), ctx.getId());
                int contactID = getContactIdByUserId(contextId, userId, write_ox_con);
                stmt = write_ox_con.prepareStatement("DELETE FROM prg_contacts_image WHERE cid = ? AND intfield01 = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, contactID);
                stmt.executeUpdate();
                stmt.close();
                stmt = write_ox_con.prepareStatement("DELETE FROM prg_contacts WHERE cid = ? AND userid = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                stmt.close();

                // JCS
                {
                    CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != cacheService) {
                        try {
                            CacheKey key = cacheService.newCacheKey(contextId, user.getId().intValue());
                            Cache cache = cacheService.getCache("User");
                            cache.remove(key);
                            cache = cacheService.getCache("UserPermissionBits");
                            cache.remove(key);
                            cache = cacheService.getCache("UserConfiguration");
                            cache.remove(key);
                            cache = cacheService.getCache("UserSettingMail");
                            cache.remove(key);
                            cache = cacheService.getCache("Capabilities");
                            cache.removeFromGroup(user.getId(), ctx.getId().toString());
                            cache = cacheService.getCache("QuotaFileStorages");
                            cache.invalidateGroup(Integer.toString(contextId));
                        } catch (final OXException e) {
                            LOG.error("", e);
                        }
                    }
                }
                // End of JCS

                //Delete aliases
                UserAliasStorage aliasStorage = AdminServiceRegistry.getInstance().getService(UserAliasStorage.class);
                aliasStorage.deleteAliases(write_ox_con, contextId, userId);

                LOG.info("Deleted user {}({}) ...", user.getId(), ctx.getId());

            }
        } catch (SQLException sqle) {
            LOG.error("SQL Error", sqle);
            throw new StorageException(sqle.toString());
        } catch (OXException e) {
            final SQLException sqle = DBUtils.extractSqlException(e);
            if (null != sqle) {
                LOG.error("SQL Error", sqle);
                throw new StorageException(sqle.toString());
            }
            LOG.error("Delete contact yielded groupware API error");
            throw new StorageException(e.toString());
        } catch (final IOException e) {
            LOG.error("", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private int getContactIdByUserId(final int ctxId, final int userId, final Connection con) throws StorageException {
        int retval = -1;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT intfield01 FROM prg_contacts WHERE cid=? AND userid=?");
            stmt.setInt(1, ctxId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                retval = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
        return retval;
    }

    @Override
    public void delete(final Context ctx, final User[] users, Integer destUser) throws StorageException {
        try {
            DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
            do {
                int contextId = ctx.getId().intValue();
                Connection con = leaseWriteContextConnectionWithoutTimeout(contextId, cache);
                condition.resetTransactionRollbackException();
                boolean rollback = false;
                try {
                    startTransaction(con);
                    rollback = true;

                    lock(contextId, con);

                    delete(ctx, users, destUser, con);
                    for (final User user : users) {
                        LOG.info("User {} deleted!", user.getId());
                    }

                    con.commit();
                    rollback = false;

                    for (final User user : users) {
                        try {
                            DeleteEvent delev = DeleteEvent.createDeleteEventForUserDeletion(this, user.getId().intValue(), 0, ContextStorage.getInstance().getContext(contextId), destUser);
                            DeleteFinishedListenerRegistry.getInstance().fireDeleteEvent(delev);
                        } catch (Exception e) {
                            LOG.warn("Failed to trigger delete finished listeners", e);
                        }
                        LOG.info("User {} deleted!", user.getId());
                    }
                } catch (final StorageException st) {
                    final SQLException sqle = DBUtils.extractSqlException(st);
                    if (!condition.isFailedTransactionRollback(sqle)) {
                        LOG.error("Storage Error", st);
                        throw st;
                    }
                } catch (final SQLException sql) {
                    if (!condition.isFailedTransactionRollback(sql)) {
                        LOG.error("SQL Error", sql);
                        throw new StorageException(sql.toString(), sql);
                    }
                } catch (final RuntimeException e) {
                    LOG.error("", e);
                    throw e;
                } finally {
                    if (rollback) {
                        rollback(con);
                    }
                    autocommit(con);
                    releaseWriteContextConnectionWithoutTimeout(con, contextId, cache);
                }
            } while (retry(condition, users, ctx));
        } catch (final SQLException sql) {
            throw new StorageException(sql.toString(), sql);
        }
    }

    private boolean retry(DBUtils.TransactionRollbackCondition condition, final User[] users, Context ctx) throws SQLException {
        SQLException sqle = condition.getTransactionRollbackException();
        boolean retry = condition.checkRetry();
        if (retry) {
            LOG.info("Retrying to delete users {} from context {} as suggested by: {}", Arrays.toString(users), ctx.getId(), sqle.getMessage());
        }
        return retry;
    }

    @Override
    public void delete(final Context ctx, final User user, Integer destUID) throws StorageException {
        delete(ctx, new User[] { user }, destUID);
    }

    @Override
    public void changeModuleAccess(final Context ctx, final int[] userIds, final UserModuleAccess moduleAccess) throws StorageException {
        int contextId = ctx.getId().intValue();

        Connection con = leaseConnectionForContext(contextId, cache);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            lock(contextId, con);

            // Loop through the int[] and change the module access rights for each user
            for (final int userId : userIds) {
                // first get all groups the user is in
                final int[] groupsForUser = getGroupsForUser(ctx, userId, con);
                // update last modified column
                changeLastModified(userId, ctx, con);
                myChangeInsertModuleAccess(ctx, userId, moduleAccess, false, con, groupsForUser);
            }
            con.commit();
            rollback = false;

            CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null == cacheService) {
                return;
            }
            try {
                for (int userId : userIds) {
                    invalidateUserCaches(userId, contextId, cacheService);
                }
            } catch (final OXException e) {
                LOG.error("", e);
            }
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } finally {
            if (rollback) {
                rollback(con);
            }
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    @Override
    public void changeModuleAccess(final Context ctx, final int userId, final UserModuleAccess moduleAccess) throws StorageException {
        changeModuleAccess(ctx, new int[] { userId }, moduleAccess);
    }

    @Override
    public UserModuleAccess getModuleAccess(final Context ctx, final int user_id) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection read_ox_con = leaseConnectionForContext(contextId, cache);
        try {
            int[] all_groups_of_user = getGroupsForUser(ctx, user_id, read_ox_con);

            UserPermissionBits user = RdbUserPermissionBitsStorage.adminLoadUserPermissionBits(user_id, all_groups_of_user, contextId, read_ox_con);

            UserModuleAccess acc = new UserModuleAccess();

            acc.setCalendar(user.hasPermission(UserConfiguration.CALENDAR));
            acc.setContacts(user.hasPermission(UserConfiguration.CONTACTS));
            acc.setEditPublicFolders(user.hasPermission(UserConfiguration.EDIT_PUBLIC_FOLDERS));
            acc.setReadCreateSharedFolders(user.hasPermission(UserConfiguration.READ_CREATE_SHARED_FOLDERS));
            acc.setIcal(user.hasPermission(UserConfiguration.ICAL));
            acc.setInfostore(user.hasPermission(UserConfiguration.INFOSTORE));
            acc.setSyncml(user.hasPermission(UserConfiguration.MOBILITY));
            acc.setTasks(user.hasPermission(UserConfiguration.TASKS));
            acc.setVcard(user.hasPermission(UserConfiguration.VCARD));
            acc.setWebdav(user.hasPermission(UserConfiguration.WEBDAV));
            acc.setWebdavXml(user.hasPermission(UserConfiguration.WEBDAV_XML));
            acc.setWebmail(user.hasPermission(UserConfiguration.WEBMAIL));
            acc.setDelegateTask(user.hasPermission(UserConfiguration.DELEGATE_TASKS));
            acc.setEditGroup(user.hasPermission(UserConfiguration.EDIT_GROUP));
            acc.setEditResource(user.hasPermission(UserConfiguration.EDIT_RESOURCE));
            acc.setEditPassword(user.hasPermission(UserConfiguration.EDIT_PASSWORD));
            acc.setCollectEmailAddresses(user.hasPermission(UserConfiguration.COLLECT_EMAIL_ADDRESSES));
            acc.setMultipleMailAccounts(user.hasPermission(UserConfiguration.MULTIPLE_MAIL_ACCOUNTS));
            acc.setPublication(user.hasPermission(UserConfiguration.PUBLICATION));
            acc.setSubscription(user.hasPermission(UserConfiguration.SUBSCRIPTION));
            acc.setActiveSync(user.hasPermission(UserConfiguration.ACTIVE_SYNC));
            acc.setUSM(user.hasPermission(UserConfiguration.USM));
            acc.setOLOX20(user.hasPermission(UserConfiguration.OLOX20));
            acc.setDeniedPortal(user.hasPermission(UserConfiguration.DENIED_PORTAL));
            final OXFolderAdminHelper adminHelper = new OXFolderAdminHelper();
            acc.setGlobalAddressBookDisabled(adminHelper.isGlobalAddressBookDisabled(contextId, user_id, read_ox_con));
            acc.setPublicFolderEditable(adminHelper.isPublicFolderEditable(contextId, user_id, read_ox_con));
            return acc;
        } catch (final SQLException sqle) {
            LOG.error("SQL Error ", sqle);
            throw new StorageException(sqle.toString());
        } catch (final OXException e) {
            LOG.error("OX Error ", e);
            throw new StorageException(e.toString(), e);
        } finally {
            releaseWriteContextConnectionAfterReading(read_ox_con, contextId, cache);
        }

    }

    @Override
    public void changeLastModified(final int user_id, final Context ctx, final Connection write_ox_con) throws StorageException {
        PreparedStatement prep_edit_user = null;
        try {
            prep_edit_user = write_ox_con.prepareStatement("UPDATE prg_contacts SET changing_date=? WHERE cid=? AND userid=?;");
            prep_edit_user.setLong(1, System.currentTimeMillis());
            prep_edit_user.setInt(2, ctx.getId().intValue());
            prep_edit_user.setInt(3, user_id);
            prep_edit_user.executeUpdate();

        } catch (final SQLException sqle) {
            LOG.error("SQL Error ", sqle);
            throw new StorageException(sqle.toString());
        } finally {
            try {
                if (prep_edit_user != null) {
                    prep_edit_user.close();
                }
            } catch (final SQLException ex) {
                LOG.error("Error closing statement!", ex);
            }
        }
    }

    @Override
    public void createRecoveryData(final Context ctx, final int user_id, final Connection write_ox_con) throws StorageException {
        // move user to del_user table if table is ready
        PreparedStatement del_st = null;
        ResultSet rs = null;
        try {
            del_st = write_ox_con.prepareStatement("SELECT contactId,uidNumber,gidNumber FROM user WHERE id = ? AND cid = ?");
            del_st.setInt(1, user_id);
            del_st.setInt(2, ctx.getId().intValue());
            rs = del_st.executeQuery();

            int contactid = -1;
            int uidnumber = -1;
            int gidnumber = -1;

            if (rs.next()) {
                contactid = rs.getInt("contactId");
                uidnumber = rs.getInt("uidNumber");
                gidnumber = rs.getInt("gidNumber");
            }
            del_st.close();
            rs.close();

            del_st = write_ox_con.prepareStatement("INSERT into del_user (id,cid,contactId,uidNumber,gidNumber) VALUES (?,?,?,?,?)");
            del_st.setInt(1, user_id);
            del_st.setInt(2, ctx.getId().intValue());
            if (contactid != -1) {
                del_st.setInt(3, contactid);
            } else {
                del_st.setNull(3, Types.INTEGER);
            }
            if (uidnumber != -1) {
                del_st.setInt(4, uidnumber);
            } else {
                del_st.setNull(4, Types.INTEGER);
            }
            if (gidnumber != -1) {
                del_st.setInt(5, gidnumber);
            } else {
                del_st.setNull(5, Types.INTEGER);
            }
            del_st.executeUpdate();
        } catch (final DataTruncation dt) {
            LOG.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException sqle) {
            LOG.error("SQL Error ", sqle);
            throw new StorageException(sqle.toString());
        } finally {
            try {
                if (del_st != null) {
                    del_st.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing prepared statement!", e);
            }
        }
    }

    @Override
    public void deleteAllRecoveryData(final Context ctx, final Connection con) throws StorageException {
        // delete from del_user table
        PreparedStatement del_st = null;
        try {
            del_st = con.prepareStatement("DELETE from del_user WHERE cid = ?");
            del_st.setInt(1, ctx.getId().intValue());
            del_st.executeUpdate();
        } catch (final SQLException sqle) {
            LOG.error("SQL Error ", sqle);
            throw new StorageException(sqle.toString());
        } finally {
            try {
                if (del_st != null) {
                    del_st.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing prepared statement!", e);
            }
        }
    }

    @Override
    public void deleteRecoveryData(final Context ctx, final int user_id, final Connection con) throws StorageException {
        // delete from del_user table
        PreparedStatement del_st = null;
        try {
            del_st = con.prepareStatement("DELETE from del_user WHERE id = ? AND cid = ?");
            del_st.setInt(1, user_id);
            del_st.setInt(2, ctx.getId().intValue());
            del_st.executeUpdate();
        } catch (final SQLException sqle) {
            LOG.error("SQL Error ", sqle);
            throw new StorageException(sqle.toString());
        } finally {
            try {
                if (del_st != null) {
                    del_st.close();
                }
            } catch (final SQLException e) {
                LOG.error("Error closing prepared statement!", e);
            }
        }
    }

    public static int getintfrombool(final boolean bool) {
        return bool ? 0 : -1;
    }

    public static boolean getboolfromint(final int number) {
        return (0 == number);
    }

    private int[] getGroupsForUser(final Context ctx, final int user_id, final Connection read_ox_con) throws SQLException {

        PreparedStatement prep = null;
        try {

            prep = read_ox_con.prepareStatement("SELECT id FROM groups_member WHERE cid = ? AND member = ?");
            prep.setInt(1, ctx.getId().intValue());
            prep.setInt(2, user_id);

            final ResultSet rs = prep.executeQuery();

            final List<Integer> tmp = new LinkedList<>();

            // add colubrids ALL_GROUPS_AND_USERS group to the group
            tmp.add(Integer.valueOf(0));
            while (rs.next()) {
                tmp.add(Integer.valueOf(rs.getInt(1)));
            }
            rs.close();

            int[] ret = new int[tmp.size()];
            for (int a = 0; a < tmp.size(); a++) {
                ret[a] = tmp.get(a).intValue();
            }
            return ret;
        } finally {
            Databases.closeSQLStuff(prep);
        }
    }

    private void myChangeInsertModuleAccess(final Context ctx, final int userId, final UserModuleAccess access, final boolean insert, final Connection writeCon, final int[] groups) throws StorageException {
        checkForIllegalCombination(access);
        try {
            final UserPermissionBits user = RdbUserPermissionBitsStorage.adminLoadUserPermissionBits(userId, groups, ctx.getId().intValue(), writeCon);
            user.setCalendar(access.getCalendar());
            user.setContact(access.getContacts());
            user.setFullPublicFolderAccess(access.getEditPublicFolders());
            user.setFullSharedFolderAccess(access.getReadCreateSharedFolders());
            user.setICal(access.getIcal());
            user.setInfostore(access.getInfostore());
            user.setSyncML(access.getSyncml());
            user.setTask(access.getTasks());
            user.setVCard(access.getVcard());
            user.setWebDAV(access.getWebdav());
            user.setWebDAVXML(access.getWebdavXml());
            user.setWebMail(access.getWebmail());
            user.setDelegateTasks(access.getDelegateTask());
            user.setEditGroup(access.getEditGroup());
            user.setEditResource(access.getEditResource());
            user.setEditPassword(access.getEditPassword());
            user.setCollectEmailAddresses(access.isCollectEmailAddresses());
            user.setMultipleMailAccounts(access.isMultipleMailAccounts());
            user.setSubscription(access.isSubscription());
            user.setPublication(access.isPublication());
            user.setActiveSync(access.isActiveSync());
            user.setUSM(access.isUSM());
            user.setOLOX20(access.isOLOX20());
            user.setDeniedPortal(access.isDeniedPortal());
            // Apply access.isGlobalAddressBook() to OXFolderAdminHelper.setGlobalAddressBookEnabled()
            final OXFolderAdminHelper adminHelper = new OXFolderAdminHelper();
            adminHelper.setGlobalAddressBookDisabled(ctx.getId().intValue(), userId, access.isGlobalAddressBookDisabled(), writeCon);
            adminHelper.setPublicFolderEditable(access.isPublicFolderEditable(), ctx.getId().intValue(), userId, writeCon);

            RdbUserPermissionBitsStorage.saveUserPermissionBits(user, insert, writeCon);
            if (!insert) {
                final com.openexchange.groupware.contexts.Context gwCtx = ContextStorage.getInstance().getContext(ctx.getId().intValue());
                UserConfigurationStorage.getInstance().invalidateCache(user.getUserId(), gwCtx);
            }
        } catch (final SQLException e) {
            throw new StorageException(e.toString(), e);
        } catch (final OXException e) {
            throw new StorageException(e.toString(), e);
        }
    }

    private void checkForIllegalCombination(final UserModuleAccess access) throws StorageException {
        if (access.isGlobalAddressBookDisabled()) {
            // At least Outlook does not work if global address book is not available. All other groupware functionality gets useless.
            if (access.getEditPublicFolders()) {
                throw new StorageException("Global address book can not be disabled for non-PIM users.");
            }
            if (access.getReadCreateSharedFolders()) {
                throw new StorageException("Global address book can not be disabled for non-PIM users.");
            }
            if (access.getWebdavXml()) {
                throw new StorageException("Global address book can not be disabled for non-PIM users.");
            }
            if (access.getDelegateTask()) {
                throw new StorageException("Global address book can not be disabled for non-PIM users.");
            }
        }
    }

    private List<MethodAndName> getGetters(final Method[] theMethods) {
        final List<MethodAndName> retlist = new LinkedList<>();

        // Define the returntypes we search for
        final HashSet<String> returntypes = new HashSet<>(4);
        returntypes.add("java.lang.String");
        returntypes.add("java.lang.Integer");
        returntypes.add("java.lang.Long");
        returntypes.add("java.lang.Boolean");
        returntypes.add("java.util.Date");

        // First we get all the getters of the user data class
        for (final Method method : theMethods) {
            final String methodname = method.getName();

            if (methodname.startsWith("get")) {
                final String methodnamewithoutprefix = methodname.substring(3);
                if (!Mapper.notallowed.contains(methodnamewithoutprefix)) {
                    if (null != Mapper.method2field.get(methodnamewithoutprefix)) {
                        final String returntype = method.getReturnType().getName();
                        if (returntypes.contains(returntype)) {
                            retlist.add(new MethodAndName(method, methodnamewithoutprefix));
                        }
                    }
                }
            } else if (methodname.startsWith("is")) {
                final String methodnamewithoutprefix = methodname.substring(2);
                if (!Mapper.notallowed.contains(methodnamewithoutprefix)) {
                    if (null != Mapper.method2field.get(methodnamewithoutprefix)) {
                        final String returntype = method.getReturnType().getName();
                        if (returntypes.contains(returntype)) {
                            retlist.add(new MethodAndName(method, methodnamewithoutprefix));
                        }
                    }
                }
            }
        }
        return retlist;
    }

    @Override
    public User[] listUsersByAliasDomain(Context context, String aliasDomain) throws StorageException {
        int contextId = context.getId().intValue();

        Connection read_ox_con = leaseConnectionForContext(contextId, cache);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT us.id FROM user us LEFT JOIN user_alias la ON us.id = la.user AND us.cid = la.cid ");
            sb.append("WHERE us.cid = ? and la.alias LIKE ?");

            String query = sb.toString();
            stmt = read_ox_con.prepareStatement(query);
            stmt.setInt(1, context.getId());
            stmt.setString(2, "%" + aliasDomain);

            rs = stmt.executeQuery();
            List<User> retval = new LinkedList<>();
            while (rs.next()) {
                retval.add(new User(rs.getInt(1)));
            }
            return retval.toArray(new User[retval.size()]);
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseWriteContextConnectionAfterReading(read_ox_con, contextId, cache);
        }
    }

    private void convertDriveUserFolders(Context ctx, User user, Connection con) throws StorageException {
        int contextId = ctx.getId().intValue();
        int userId = user.getId().intValue();
        PreparedStatement stmt = null;
        try {
            FolderCacheManager cache = FolderCacheManager.getInstance();
            List<Pair<Integer, String>> folderIds = prepareFolders(contextId, userId, con);
            long now = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder("UPDATE oxfolder_tree SET default_flag = 0, type = 2, fname = ?, changing_date = ? WHERE cid = ? AND fuid = ?");
            stmt = con.prepareStatement(sb.toString());
            for (Pair<Integer, String> pair : folderIds) {
                stmt.setString(1, pair.getSecond());
                stmt.setLong(2, now);
                stmt.setInt(3, contextId);
                stmt.setInt(4, pair.getFirst());
                stmt.addBatch();
                cache.removeFolderObject(pair.getFirst().intValue(), ContextStorage.getStorageContext(contextId));
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (OXException e) {
            LOG.error("Cache Error", e);
            throw new StorageException(e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private List<Pair<Integer, String>> prepareFolders(int contextId, int userId, Connection con) throws SQLException {
        List<Pair<Integer, String>> result = new ArrayList<>(6);
        String language = getLanguage(contextId, userId, con);
        Locale locale = LocaleTools.getLocale(language);
        StringHelper helper = StringHelper.valueOf(locale);
        boolean needTranslation = true;
        if (Locale.ENGLISH.equals(locale) || Locale.US.equals(locale)) {
            needTranslation = false;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid, fname, parent FROM oxfolder_tree WHERE cid = ? AND created_from = ? AND module = 8 AND default_flag = 1 AND type IN (20, 21, 22, 23, 24) FOR UPDATE");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int folderId = rs.getInt(1);
                String name = rs.getString(2);
                String translatedName = name;
                if (needTranslation) {
                    int parent = rs.getInt(3);
                    translatedName = helper.getString(name);
                    NameBuilder nb = null;
                    while (existsFolder(contextId, parent, folderId, translatedName, con)) {
                        if (null == nb) {
                            nb = new NameBuilder(translatedName);
                        }
                        translatedName = nb.advance().toString();
                    }
                }
                result.add(new Pair<Integer, String>(folderId, translatedName));
            }
            return result;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private boolean existsFolder(int contextId, int parent, int folderId, String name, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ? AND fuid <> ? AND fname = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, parent);
            stmt.setInt(3, folderId);
            stmt.setString(4, name);
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private String getLanguage(int contextId, int userId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT preferredLanguage FROM user WHERE cid = ? AND id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            String language;
            if (rs.next()) {
                language = rs.getString(1);
                if (Strings.isNotEmpty(language)) {
                    return language;
                }
            }
            return Locale.getDefault().getLanguage();
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Toggles the user status between active/inactive
     *
     * @param userId the user identifier
     * @param ctx The {@link Context}
     * @param flag The value of the flag
     * @throws StorageException if a storage error is occurred
     */
    private void toggleUserStatus(int userId, Context ctx, boolean flag) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection con = leaseConnectionForContext(contextId, cache);
        try {
            setUserEnabled(userId, contextId, flag, con);
        } finally {
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    private static final String[] USER_CACHES = { "User", "UserPermissionBits", "UserConfiguration", "UserSettingMail" };

    /**
     * Invalidates the following user caches:
     * <ul>
     * <li>User</li>
     * <li>UserPermissionBits</li>
     * <li>UserConfiguration</li>
     * <li>UserSettingMail</li>
     * <li>Capabilities</li>
     * <li>QuotaFileStorages</li>
     * </ul>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param cacheService the {@link CacheService}
     * @throws OXException if an error is occurred
     */
    private void invalidateUserCaches(int userId, int contextId, CacheService cacheService) throws OXException {
        for (String cacheName : USER_CACHES) {
            Cache cache = cacheService.getCache(cacheName);
            CacheKey key = cacheService.newCacheKey(contextId, userId);
            cache.remove(key);
        }
        Cache cache = cacheService.getCache("Capabilities");
        cache.removeFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
        cache = cacheService.getCache("QuotaFileStorages");
        cache.invalidateGroup(Integer.toString(contextId));
    }
}
