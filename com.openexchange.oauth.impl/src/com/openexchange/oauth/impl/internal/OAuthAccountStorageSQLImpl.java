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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.oauth.impl.internal;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.java.Strings;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthAccountStorage;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.impl.services.Services;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.session.Session;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.Command;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link OAuthAccountStorageSQLImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthAccountStorageSQLImpl implements OAuthAccountStorage, SecretEncryptionStrategy<PWUpdate>, EncryptedItemDetectorService, SecretMigrator, EncryptedItemCleanUpService {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAccountStorageSQLImpl.class);

    private final DBProvider provider;
    private final IDGeneratorService idGenerator;
    private final OAuthServiceMetaDataRegistry registry;
    private final ContextService contextService;

    /**
     * Initialises a new {@link OAuthAccountStorageSQLImpl}.
     */
    public OAuthAccountStorageSQLImpl(DBProvider provider, IDGeneratorService idGenerator, OAuthServiceMetaDataRegistry registry, ContextService contextService) {
        super();
        this.provider = provider;
        this.idGenerator = idGenerator;
        this.registry = registry;
        this.contextService = contextService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthStorage#createAccount(com.openexchange.session.Session, com.openexchange.oauth.OAuthAccount)
     */
    @Override
    public int storeAccount(Session session, OAuthAccount account) throws OXException {
        int contextId = session.getContextId();
        int user = session.getUserId();

        // Crypt tokens
        ((DefaultOAuthToken) account).setToken(encrypt(account.getToken(), session));
        ((DefaultOAuthToken) account).setSecret(encrypt(account.getSecret(), session));
        ((DefaultOAuthAccount) account).setId(idGenerator.getId(OAuthConstants.TYPE_ACCOUNT, contextId));

        // Create INSERT command
        ArrayList<Object> values = new ArrayList<Object>(SQLStructure.OAUTH_COLUMN.values().length);
        INSERT insert = SQLStructure.insertAccount(account, contextId, user, values);
        // Execute INSERT command
        executeUpdate(contextId, insert, values);
        LOG.info("Created new {} account with ID {} for user {} in context {}", account.getMetaData().getDisplayName(), account.getId(), user, contextId);
        return account.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthStorage#getAccount(com.openexchange.session.Session, int)
     */
    @Override
    public OAuthAccount getAccount(Session session, int accountId) throws OXException {
        Connection connection = (Connection) session.getParameter("__file.storage.delete.connection");
        try {
            if (connection != null && Databases.isInTransaction(connection)) {
                // Given connection is already in transaction. Invoke & return immediately.
                return getAccount(session, accountId, connection);
            }
        } catch (SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }

        final Context context = getContext(session.getContextId());
        connection = getConnection(true, context);
        try {
            return getAccount(session, accountId, connection);
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            provider.releaseReadConnection(context, connection);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthStorage#deleteAccount(com.openexchange.session.Session, int)
     */
    @Override
    public void deleteAccount(Session session, int accountId) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        boolean rollback = false;
        PreparedStatement stmt = null;
        try {
            startTransaction(con);
            rollback = true;

            final DeleteListenerRegistry deleteListenerRegistry = DeleteListenerRegistry.getInstance();
            final Map<String, Object> properties = new HashMap<>(2);
            // Hint to not update the scopes since it's an oauth account deletion
            // This hint has to be passed via the delete listener
            properties.put(OAuthConstants.SESSION_PARAM_UPDATE_SCOPES, false);

            deleteListenerRegistry.triggerOnBeforeDeletion(accountId, properties, userId, contextId, con);
            stmt = con.prepareStatement("DELETE FROM oauthAccounts WHERE cid = ? AND user = ? and id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accountId);
            stmt.executeUpdate();
            deleteListenerRegistry.triggerOnAfterDeletion(accountId, properties, userId, contextId, con);

            con.commit(); // COMMIT
            rollback = false;
            LOG.info("Deleted OAuth account with id '{}' for user '{}' in context '{}'", accountId, userId, contextId);
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            Databases.closeSQLStuff(stmt);
            autocommit(con);
            provider.releaseReadConnection(context, con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthStorage#updateAccount(com.openexchange.session.Session, com.openexchange.oauth.OAuthAccount)
     */
    @Override
    public void updateAccount(Session session, OAuthAccount account) throws OXException {
        // Crypt tokens
        ((DefaultOAuthToken) account).setToken(encrypt(account.getToken(), session));
        ((DefaultOAuthToken) account).setSecret(encrypt(account.getSecret(), session));
        /*
         * Get connection
         */
        int contextId = session.getContextId();
        int userId = session.getUserId();
        Context ctx = getContext(contextId);
        Connection writeCon = getConnection(false, ctx);
        boolean rollback = false;
        try {
            /*
             * Create UPDATE command
             */
            final ArrayList<Object> values = new ArrayList<Object>(SQLStructure.OAUTH_COLUMN.values().length);
            final UPDATE update = SQLStructure.updateAccount(account, contextId, userId, values);
            Databases.startTransaction(writeCon);
            rollback = true;
            String identity = getUserIdentity(session, account.getMetaData().getId(), account.getId(), writeCon);
            if (Strings.isNotEmpty(identity) && Strings.isNotEmpty(account.getUserIdentity()) && !account.getUserIdentity().equals(identity)) {
                // The user selected the wrong account
                throw OAuthExceptionCodes.WRONG_OAUTH_ACCOUNT.create(account.getDisplayName());
            }
            /*
             * Execute UPDATE command
             */
            executeUpdate(update, values, writeCon);
            /*
             * Signal re-authorized event
             */
            Map<String, Object> properties = Collections.<String, Object> emptyMap();
            ReauthorizeListenerRegistry.getInstance().onAfterOAuthAccountReauthorized(account.getId(), properties, userId, contextId, writeCon);
            /*
             * Commit
             */
            writeCon.commit();
            /*
             * Re-authorise
             */
            OAuthAccessRegistryService registryService = Services.getService(OAuthAccessRegistryService.class);
            OAuthAccessRegistry oAuthAccessRegistry = registryService.get(account.getMetaData().getId());
            // No need to re-authorise if access not present
            OAuthAccess access = oAuthAccessRegistry.get(contextId, userId, account.getId());
            if (access != null) {
                // Initialise the access with the new access token
                access.initialize();
            }
            rollback = false;
        } catch (SQLException e) {
            LOG.error(e.toString());
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(writeCon);
            }
            Databases.autocommit(writeCon);
            if (writeCon != null) {
                provider.releaseWriteConnection(ctx, writeCon);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthAccountStorage#updateAccount(com.openexchange.session.Session, int, java.util.Map)
     */
    @Override
    public void updateAccount(Session session, int accountId, Map<String, Object> arguments) throws OXException {
        final List<Setter> list = setterFrom(arguments);
        if (list.isEmpty()) {
            return;
        }
        Connection connection = (Connection) session.getParameter("__file.storage.delete.connection");
        try {
            if (connection != null && Databases.isInTransaction(connection)) {
                // Given connection is already in transaction. Invoke & return immediately.
                updateAccount(session, accountId, list, connection);
                return;
            }
        } catch (SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        final Context context = getContext(session.getContextId());
        connection = getConnection(false, context);
        try {
            updateAccount(session, accountId, list, connection);
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            provider.releaseReadConnection(context, connection);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthAccountStorage#findByUserIdentity(com.openexchange.session.Session, java.lang.String, java.lang.String)
     */
    @Override
    public OAuthAccount findByUserIdentity(Session session, String userIdentity, String serviceId) throws OXException {
        final SecretEncryptionService<PWUpdate> encryptionService = Services.getService(SecretEncryptionFactoryService.class).createService(this);
        final OAuthScopeRegistry scopeRegistry = Services.getService(OAuthScopeRegistry.class);
        int contextId = session.getContextId();
        int userId = session.getUserId();
        final Context context = getContext(contextId);
        Connection connection = getConnection(true, context);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT id, displayName, accessToken, accessSecret, serviceId, scope, identity FROM oauthAccounts WHERE cid = ? AND user = ? AND serviceId = ? AND identity = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, serviceId);
            stmt.setString(4, userIdentity);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            int accountId = rs.getInt(1);
            DefaultOAuthAccount account = new DefaultOAuthAccount();
            account.setId(accountId);
            String displayName = rs.getString(2);
            account.setDisplayName(displayName);
            try {
                account.setToken(encryptionService.decrypt(session, rs.getString(3), new PWUpdate("accessToken", contextId, accountId)));
                account.setSecret(encryptionService.decrypt(session, rs.getString(4), new PWUpdate("accessSecret", contextId, accountId)));
            } catch (OXException e) {
                if (false == CryptoErrorMessage.BadPassword.equals(e)) {
                    throw e;
                }

                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e.getCause(), displayName, accountId);
            }

            account.setMetaData(registry.getService(rs.getString(5), userId, contextId));
            String scopes = rs.getString(6);
            Set<OAuthScope> enabledScopes = scopeRegistry.getAvailableScopes(account.getMetaData().getAPI(), OXScope.valuesOf(scopes));
            account.setEnabledScopes(enabledScopes);
            account.setUserIdentity(rs.getString(7));
            return account;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, connection);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthAccountStorage#hasUserIdentity(com.openexchange.session.Session, int, java.lang.String)
     */
    @Override
    public boolean hasUserIdentity(Session session, int accountId, String serviceId) throws OXException {
        return Strings.isNotEmpty(getUserIdentity(session, serviceId, accountId, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthStorage#getAccounts(com.openexchange.session.Session)
     */
    @Override
    public List<OAuthAccount> getAccounts(Session session) throws OXException {
        final SecretEncryptionService<PWUpdate> encryptionService = Services.getService(SecretEncryptionFactoryService.class).createService(this);
        final OAuthScopeRegistry scopeRegistry = Services.getService(OAuthScopeRegistry.class);
        int userId = session.getUserId();
        int contextId = session.getContextId();
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement("SELECT id, displayName, accessToken, accessSecret, serviceId, scope, identity FROM oauthAccounts WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>(8);
            do {
                try {
                    final DefaultOAuthAccount account = new DefaultOAuthAccount();
                    account.setMetaData(registry.getService(rs.getString(5), userId, contextId));
                    account.setId(rs.getInt(1));
                    account.setDisplayName(rs.getString(2));
                    try {
                        account.setToken(encryptionService.decrypt(session, rs.getString(3), new PWUpdate("accessToken", contextId, account.getId())));
                        account.setSecret(encryptionService.decrypt(session, rs.getString(4), new PWUpdate("accessSecret", contextId, account.getId())));
                    } catch (final OXException e) {
                        // Log for debug purposes and ignore...
                        LOG.debug("{}", e.getMessage(), e);
                    }
                    String scopes = rs.getString(6);
                    if (!Strings.isEmpty(scopes)) {
                        Set<OAuthScope> enabledScopes = scopeRegistry.getAvailableScopes(account.getMetaData().getAPI(), OXScope.valuesOf(scopes));
                        account.setEnabledScopes(enabledScopes);
                    }
                    account.setUserIdentity(rs.getString(7));
                    accounts.add(account);
                } catch (final OXException e) {
                    if (!OAuthExceptionCodes.UNKNOWN_OAUTH_SERVICE_META_DATA.equals(e)) {
                        throw e;
                    }
                    // Obviously associated service is not available. Log for debug purposes and ignore...
                    LOG.debug("{}", e.getMessage(), e);
                }
            } while (rs.next());
            return accounts;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthStorage#getAccounts(com.openexchange.session.Session, java.lang.String)
     */
    @Override
    public List<OAuthAccount> getAccounts(Session session, String serviceMetaData) throws OXException {
        final SecretEncryptionService<PWUpdate> encryptionService = Services.getService(SecretEncryptionFactoryService.class).createService(this);
        final OAuthScopeRegistry scopeRegistry = Services.getService(OAuthScopeRegistry.class);
        int userId = session.getUserId();
        int contextId = session.getContextId();
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, displayName, accessToken, accessSecret, scope, identity FROM oauthAccounts WHERE cid = ? AND user = ? AND serviceId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, serviceMetaData);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>(8);
            do {

                try {
                    final DefaultOAuthAccount account = new DefaultOAuthAccount();
                    account.setId(rs.getInt(1));
                    account.setDisplayName(rs.getString(2));
                    try {
                        account.setToken(encryptionService.decrypt(session, rs.getString(3), new PWUpdate("accessToken", contextId, account.getId())));
                        account.setSecret(encryptionService.decrypt(session, rs.getString(4), new PWUpdate("accessSecret", contextId, account.getId())));
                    } catch (final OXException x) {
                        // Log for debug purposes and ignore...
                        LOG.debug("{}", x.getMessage(), x);
                    }
                    account.setMetaData(registry.getService(serviceMetaData, userId, contextId));
                    String scopes = rs.getString(5);
                    Set<OAuthScope> enabledScopes = scopeRegistry.getAvailableScopes(account.getMetaData().getAPI(), OXScope.valuesOf(scopes));
                    account.setEnabledScopes(enabledScopes);
                    account.setUserIdentity(rs.getString(6));
                    accounts.add(account);
                } catch (final OXException e) {
                    if (!OAuthExceptionCodes.UNKNOWN_OAUTH_SERVICE_META_DATA.equals(e)) {
                        throw e;
                    }
                    // Obviously associated service is not available. Log for debug purposes and ignore...
                    LOG.debug("{}", e.getMessage(), e);
                }
            } while (rs.next());
            return accounts;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.secret.SecretEncryptionStrategy#update(java.lang.String, java.lang.Object)
     */
    @Override
    public void update(String recrypted, PWUpdate customizationNote) throws OXException {
        final StringBuilder b = new StringBuilder();
        b.append("UPDATE oauthAccounts SET ").append(customizationNote.field).append("= ? WHERE cid = ? AND id = ?");

        final Context context = getContext(customizationNote.cid);
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection(false, context);
            stmt = con.prepareStatement(b.toString());
            stmt.setString(1, recrypted);
            stmt.setInt(2, customizationNote.cid);
            stmt.setInt(3, customizationNote.id);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            provider.releaseWriteConnection(context, con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.secret.recovery.EncryptedItemDetectorService#hasEncryptedItems(com.openexchange.tools.session.ServerSession)
     */
    @Override
    public boolean hasEncryptedItems(ServerSession session) throws OXException {
        final int contextId = session.getContextId();
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM oauthAccounts WHERE cid = ? AND user = ? LIMIT 1");
            stmt.setInt(1, contextId);
            stmt.setInt(2, session.getUserId());
            return stmt.executeQuery().next();
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.secret.recovery.SecretMigrator#migrate(java.lang.String, java.lang.String, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void migrate(String oldSecret, String newSecret, ServerSession session) throws OXException {
        final CryptoService cryptoService = Services.getService(CryptoService.class);
        final int contextId = session.getContextId();
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, accessToken, accessSecret FROM oauthAccounts WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, session.getUserId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
            final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>(8);
            do {
                try {
                    // Try using the new secret. Maybe this account doesn't need the migration
                    final String accessToken = rs.getString(2);
                    if (!Strings.isEmpty(accessToken)) {
                        cryptoService.decrypt(accessToken, newSecret);
                    }
                    final String accessSecret = rs.getString(3);
                    if (!Strings.isEmpty(accessSecret)) {
                        cryptoService.decrypt(accessSecret, newSecret);
                    }
                } catch (final OXException e) {
                    // Needs migration
                    final DefaultOAuthAccount account = new DefaultOAuthAccount();
                    account.setId(rs.getInt(1));
                    account.setToken(cryptoService.decrypt(rs.getString(2), oldSecret));
                    account.setSecret(cryptoService.decrypt(rs.getString(3), oldSecret));
                    accounts.add(account);
                }
            } while (rs.next());
            closeSQLStuff(rs, stmt);
            if (accounts.isEmpty()) {
                return;
            }
            /*
             * Update
             */
            stmt = con.prepareStatement("UPDATE oauthAccounts SET accessToken = ?, accessSecret = ? WHERE cid = ? AND user = ? AND id = ?");
            stmt.setInt(3, contextId);
            stmt.setInt(4, session.getUserId());
            for (final OAuthAccount oAuthAccount : accounts) {
                stmt.setString(1, cryptoService.encrypt(oAuthAccount.getToken(), newSecret));
                stmt.setString(2, cryptoService.encrypt(oAuthAccount.getSecret(), newSecret));
                stmt.setInt(5, oAuthAccount.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseWriteConnection(context, con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.secret.recovery.EncryptedItemCleanUpService#cleanUpEncryptedItems(java.lang.String, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void cleanUpEncryptedItems(String secret, ServerSession session) throws OXException {
        final CryptoService cryptoService = Services.getService(CryptoService.class);
        final int contextId = session.getContextId();
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Boolean committed = null;
        try {
            stmt = con.prepareStatement("SELECT id, accessToken, accessSecret FROM oauthAccounts WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, session.getUserId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
            final List<Integer> accounts = new ArrayList<Integer>(8);
            do {
                try {
                    // Try using the secret.
                    final String accessToken = rs.getString(2);
                    if (!Strings.isEmpty(accessToken)) {
                        cryptoService.decrypt(accessToken, secret);
                    }
                    final String accessSecret = rs.getString(3);
                    if (!Strings.isEmpty(accessSecret)) {
                        cryptoService.decrypt(accessSecret, secret);
                    }
                } catch (final OXException e) {
                    // Clean-up
                    accounts.add(Integer.valueOf(rs.getInt(1)));
                }
            } while (rs.next());
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            if (accounts.isEmpty()) {
                return;
            }
            /*
             * Delete them
             */
            committed = Boolean.FALSE;
            startTransaction(con);
            // Statement
            stmt = con.prepareStatement("UPDATE oauthAccounts SET accessToken = ?, accessSecret = ? WHERE cid = ? AND user = ? AND id = ?");
            stmt.setString(1, "");
            stmt.setString(2, "");
            stmt.setInt(3, contextId);
            stmt.setInt(4, session.getUserId());
            for (final Integer accountId : accounts) {
                stmt.setInt(5, accountId.intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
            con.commit();
            committed = Boolean.TRUE;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (null != committed && !committed.booleanValue()) {
                rollback(con);
            }
            closeSQLStuff(rs, stmt);
            provider.releaseWriteConnection(context, con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.secret.recovery.EncryptedItemCleanUpService#removeUnrecoverableItems(java.lang.String, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void removeUnrecoverableItems(String secret, ServerSession session) throws OXException {
        final CryptoService cryptoService = Services.getService(CryptoService.class);
        final int contextId = session.getContextId();
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Boolean committed = null;
        try {
            stmt = con.prepareStatement("SELECT id, accessToken, accessSecret FROM oauthAccounts WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, session.getUserId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
            final List<Integer> accounts = new ArrayList<Integer>(8);
            do {
                try {
                    // Try using the secret.
                    final String accessToken = rs.getString(2);
                    if (!Strings.isEmpty(accessToken)) {
                        cryptoService.decrypt(accessToken, secret);
                    }
                    final String accessSecret = rs.getString(3);
                    if (!Strings.isEmpty(accessSecret)) {
                        cryptoService.decrypt(accessSecret, secret);
                    }
                } catch (final OXException e) {
                    // Clean-up
                    accounts.add(Integer.valueOf(rs.getInt(1)));
                }
            } while (rs.next());
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            if (accounts.isEmpty()) {
                return;
            }
            /*
             * Delete them
             */
            committed = Boolean.FALSE;
            startTransaction(con);
            // Statement
            stmt = con.prepareStatement("DELETE FROM oauthAccounts  WHERE cid = ? AND user = ? AND id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, session.getUserId());
            for (final Integer accountId : accounts) {
                stmt.setInt(3, accountId.intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
            con.commit();
            committed = Boolean.TRUE;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (null != committed && !committed.booleanValue()) {
                rollback(con);
            }
            closeSQLStuff(rs, stmt);
            provider.releaseWriteConnection(context, con);
        }
    }

    ////////////////////////////////////// HELPERS //////////////////////////////////////////

    /**
     * 
     * @param session
     * @param accountId
     * @param list
     * @param connection
     * @throws SQLException
     * @throws OXException
     */
    private void updateAccount(Session session, int accountId, List<Setter> list, Connection connection) throws SQLException, OXException {
        final StringBuilder stmtBuilder = new StringBuilder(128).append("UPDATE oauthAccounts SET ");
        final int size = list.size();
        list.get(0).appendTo(stmtBuilder);
        for (int i = 1; i < size; i++) {
            stmtBuilder.append(", ");
            list.get(i).appendTo(stmtBuilder);
        }
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stmtBuilder.append(" WHERE cid = ? AND user = ? and id = ?").toString());
            int pos = 1;
            for (final Setter setter : list) {
                pos = setter.set(pos, stmt);
            }
            int contextId = session.getContextId();
            int userId = session.getUserId();
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos, accountId);
            final int rows = stmt.executeUpdate();
            if (rows <= 0) {
                throw OAuthExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, userId, contextId);
            }
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * 
     * @param session
     * @param accountId
     * @param connection
     * @return
     * @throws SQLException
     * @throws OXException
     */
    private OAuthAccount getAccount(Session session, int accountId, Connection connection) throws SQLException, OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT displayName, accessToken, accessSecret, serviceId, scope, identity FROM oauthAccounts WHERE cid = ? AND user = ? and id = ?");
            int contextId = session.getContextId();
            int userId = session.getUserId();
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, accountId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OAuthExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(accountId), Integer.valueOf(userId), Integer.valueOf(contextId));
            }

            DefaultOAuthAccount account = new DefaultOAuthAccount();
            account.setId(accountId);
            String displayName = rs.getString(1);
            account.setDisplayName(displayName);
            try {
                SecretEncryptionService<PWUpdate> encryptionService = Services.getService(SecretEncryptionFactoryService.class).createService(this);
                account.setToken(encryptionService.decrypt(session, rs.getString(2), new PWUpdate("accessToken", contextId, accountId)));
                account.setSecret(encryptionService.decrypt(session, rs.getString(3), new PWUpdate("accessSecret", contextId, accountId)));
            } catch (OXException e) {
                if (false == CryptoErrorMessage.BadPassword.equals(e)) {
                    throw e;
                }

                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e.getCause(), displayName, accountId);
            }

            account.setMetaData(registry.getService(rs.getString(4), userId, contextId));
            String scopes = rs.getString(5);
            OAuthScopeRegistry scopeRegistry = Services.getService(OAuthScopeRegistry.class);
            Set<OAuthScope> enabledScopes = scopeRegistry.getAvailableScopes(account.getMetaData().getAPI(), OXScope.valuesOf(scopes));
            account.setEnabledScopes(enabledScopes);
            account.setUserIdentity(rs.getString(6));
            return account;
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    /**
     * 
     * @param session
     * @param serviceId
     * @param accountId
     * @param connection
     * @return
     * @throws OXException
     */
    private String getUserIdentity(Session session, String serviceId, int accountId, Connection connection) throws OXException {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        final Context context = getContext(contextId);
        boolean releaseConnection = false;
        if (connection == null) {
            connection = getConnection(true, context);
            releaseConnection = true;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT identity FROM oauthAccounts WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, serviceId);
            stmt.setInt(4, accountId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return rs.getString(1);
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            if (releaseConnection) {
                provider.releaseReadConnection(context, connection);
            }
        }
    }

    /**
     * Retrieves the {@link Context} with the specified identifier from the storage
     * 
     * @param contextId The {@link Context} identifier
     * @return The {@link Context}
     * @throws OXException if the {@link Context} cannot be retrieved
     */
    private Context getContext(final int contextId) throws OXException {
        try {
            return contextService.getContext(contextId);
        } catch (final OXException e) {
            throw e;
        }
    }

    /**
     * Retrieves a {@link ConnectException} from the {@link DBProvider}
     * 
     * @param readOnly <code>true</code> to retrieve a read-only {@link Connection}
     * @param context The {@link Context} for which to retrieve the {@link Connection}
     * @return The {@link Connection}
     * @throws OXException if the {@link Connection} cannot be retrieved
     */
    private Connection getConnection(final boolean readOnly, final Context context) throws OXException {
        return readOnly ? provider.getReadConnection(context) : provider.getWriteConnection(context);
    }

    /**
     * 
     * @param contextId
     * @param command
     * @param values
     * @throws OXException
     */
    private void executeUpdate(int contextId, Command command, List<Object> values) throws OXException {
        Context ctx = getContext(contextId);
        Connection writeCon = getConnection(false, ctx);
        try {
            executeUpdate(command, values, writeCon);
        } finally {
            if (writeCon != null) {
                provider.releaseWriteConnection(ctx, writeCon);
            }
        }
    }

    /**
     * 
     * @param command
     * @param values
     * @param writeCon
     * @throws OXException
     */
    private void executeUpdate(Command command, List<Object> values, Connection writeCon) throws OXException {
        try {
            new StatementBuilder().executeStatement(writeCon, command, values);
        } catch (SQLException e) {
            LOG.error(e.toString());
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * 
     * @param toEncrypt
     * @param session
     * @return
     * @throws OXException
     */
    private String encrypt(final String toEncrypt, final Session session) throws OXException {
        if (Strings.isEmpty(toEncrypt)) {
            return toEncrypt;
        }
        final SecretEncryptionService<PWUpdate> service = Services.getService(SecretEncryptionFactoryService.class).createService(this);
        return service.encrypt(session, toEncrypt);
    }

    /**
     * {@link Setter}
     */
    private static interface Setter {

        void appendTo(StringBuilder stmtBuilder);

        int set(int pos, PreparedStatement stmt) throws SQLException;
    }

    /**
     * 
     * @param arguments
     * @return
     * @throws OXException
     */
    @SuppressWarnings("unchecked")
    private List<Setter> setterFrom(final Map<String, Object> arguments) throws OXException {
        final List<Setter> ret = new ArrayList<Setter>(4);
        /*
         * Check for display name
         */
        final String displayName = (String) arguments.get(OAuthConstants.ARGUMENT_DISPLAY_NAME);
        if (null != displayName) {
            ret.add(new Setter() {

                @Override
                public int set(final int pos, final PreparedStatement stmt) throws SQLException {
                    stmt.setString(pos, displayName);
                    return pos + 1;
                }

                @Override
                public void appendTo(final StringBuilder stmtBuilder) {
                    stmtBuilder.append("displayName = ?");
                }
            });
        }
        /*
         * Check for request token
         */
        final OAuthToken token = (OAuthToken) arguments.get(OAuthConstants.ARGUMENT_REQUEST_TOKEN);
        if (null != token) {
            /*
             * Crypt tokens
             */
            final Session session = (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION);
            if (null == session) {
                throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_SESSION);
            }
            final String sToken = encrypt(token.getToken(), session);
            final String secret = encrypt(token.getSecret(), session);
            ret.add(new Setter() {

                @Override
                public int set(final int pos, final PreparedStatement stmt) throws SQLException {
                    stmt.setString(pos, sToken);
                    stmt.setString(pos + 1, secret);
                    return pos + 2;
                }

                @Override
                public void appendTo(final StringBuilder stmtBuilder) {
                    stmtBuilder.append("accessToken = ?, accessSecret = ?");
                }
            });
        }

        /*
         * Scopes
         */
        final Set<OAuthScope> scopes = (Set<OAuthScope>) arguments.get(OAuthConstants.ARGUMENT_SCOPES);
        if (null != scopes) {
            ret.add(new Setter() {

                @Override
                public void appendTo(StringBuilder stmtBuilder) {
                    stmtBuilder.append("scope = ?");
                }

                @Override
                public int set(int pos, PreparedStatement stmt) throws SQLException {
                    String scope = Strings.concat(" ", scopes.toArray());
                    stmt.setString(pos, scope);
                    return pos + 1;
                }
            });
        }
        /*
         * Check for display name
         */
        final String identity = (String) arguments.get(OAuthConstants.ARGUMENT_IDENTITY);
        if (null != identity) {
            ret.add(new Setter() {

                @Override
                public int set(final int pos, final PreparedStatement stmt) throws SQLException {
                    stmt.setString(pos, identity);
                    return pos + 1;
                }

                @Override
                public void appendTo(final StringBuilder stmtBuilder) {
                    stmtBuilder.append("identity = ?");
                }
            });
        }
        /*
         * Other arguments?
         */
        return ret;
    }
}
