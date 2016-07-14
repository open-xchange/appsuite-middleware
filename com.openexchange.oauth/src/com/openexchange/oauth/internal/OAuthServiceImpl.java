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

package com.openexchange.oauth.internal;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.DropBoxApi;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.FoursquareApi;
import org.scribe.builder.api.Google2Api;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.builder.api.XingApi;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.html.HtmlService;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthEventConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.oauth.services.Services;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.Command;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;

/**
 * An {@link OAuthService} Implementation using the RDB for storage and Scribe OAuth library for the OAuth interaction.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OAuthServiceImpl implements OAuthService, SecretEncryptionStrategy<PWUpdate>, EncryptedItemDetectorService, SecretMigrator, EncryptedItemCleanUpService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OAuthServiceImpl.class);

    private final OAuthServiceMetaDataRegistry registry;

    private final DBProvider provider;

    private final IDGeneratorService idGenerator;

    private final ContextService contexts;

    private final CallbackRegistryImpl callbackRegistry;

    /**
     * Initializes a new {@link OAuthServiceImpl}.
     *
     * @param provider
     * @param simIDGenerator
     */
    public OAuthServiceImpl(final DBProvider provider, final IDGeneratorService idGenerator, final OAuthServiceMetaDataRegistry registry, final ContextService contexts, CallbackRegistryImpl cbRegistry) {
        super();
        this.registry = registry;
        this.provider = provider;
        this.idGenerator = idGenerator;
        this.contexts = contexts;
        this.callbackRegistry = cbRegistry;
    }

    @Override
    public OAuthServiceMetaDataRegistry getMetaDataRegistry() {
        return registry;
    }

    @Override
    public List<OAuthAccount> getAccounts(final Session session, final int user, final int contextId) throws OXException {
        final SecretEncryptionService<PWUpdate> encryptionService = Services.getService(SecretEncryptionFactoryService.class).createService(this);
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, displayName, accessToken, accessSecret, serviceId FROM oauthAccounts WHERE cid = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>(8);
            do {
                try {
                    final DefaultOAuthAccount account = new DefaultOAuthAccount();
                    account.setMetaData(registry.getService(rs.getString(5), user, contextId));
                    account.setId(rs.getInt(1));
                    account.setDisplayName(rs.getString(2));
                    try {
                        account.setToken(encryptionService.decrypt(session, rs.getString(3), new PWUpdate("accessToken", contextId, account.getId())));
                        account.setSecret(encryptionService.decrypt(session, rs.getString(4), new PWUpdate("accessSecret", contextId, account.getId())));
                    } catch (final OXException e) {
                        // IGNORE
                    }
                    accounts.add(account);
                } catch (final OXException e) {
                    if (!OAuthExceptionCodes.UNKNOWN_OAUTH_SERVICE_META_DATA.equals(e)) {
                        throw e;
                    }
                    // Obviously associated service is not available. Ignore...
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

    @Override
    public List<OAuthAccount> getAccounts(final String serviceMetaData, final Session session, final int user, final int contextId) throws OXException {
        final SecretEncryptionService<PWUpdate> encryptionService = Services.getService(SecretEncryptionFactoryService.class).createService(this);
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, displayName, accessToken, accessSecret FROM oauthAccounts WHERE cid = ? AND user = ? AND serviceId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            stmt.setString(3, serviceMetaData);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>(8);
            do {
                final DefaultOAuthAccount account = new DefaultOAuthAccount();
                account.setId(rs.getInt(1));
                account.setDisplayName(rs.getString(2));
                try {
                    account.setToken(encryptionService.decrypt(session, rs.getString(3), new PWUpdate("accessToken", contextId, account.getId())));
                    account.setSecret(encryptionService.decrypt(session, rs.getString(4), new PWUpdate("accessSecret", contextId, account.getId())));
                } catch (final OXException x) {
                    // IGNORE
                }
                account.setMetaData(registry.getService(serviceMetaData, user, contextId));
                accounts.add(account);
            } while (rs.next());
            return accounts;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    @Override
    public OAuthInteraction initOAuth(final String serviceMetaData, final String callbackUrl, final String currentHost, final Session session) throws OXException {
        try {
            final int contextId = session.getContextId();
            final int userId = session.getUserId();

            /*
             * Get associated OAuth meta data implementation
             */
            final OAuthServiceMetaData metaData = registry.getService(serviceMetaData, userId, contextId);

            // ------------------------------------------------------------------------------------------ //

            /*
             * Check for individual OAuthInteraction
             */
            final OAuthInteraction interaction = metaData.initOAuth(callbackUrl, session);
            if (interaction != null) {
                return interaction;
            }

            // ------------------------------------------------------------------------------------------ //

            String cbUrl = callbackUrl;
            /*
             * Apply possible modifications to call-back URL
             */
            {
                final String modifiedUrl = metaData.modifyCallbackURL(cbUrl, currentHost, session);
                if (modifiedUrl != null) {
                    cbUrl = modifiedUrl;
                }
            }
            /*
             * Check for available deferrer service
             */
            DeferringURLService ds = Services.getService(DeferringURLService.class);
            {
                boolean deferred = false;
                if (isDeferrerAvailable(ds, userId, contextId)) {
                    String deferredURL = ds.getDeferredURL(cbUrl, userId, contextId);
                    if (deferredURL != null) {
                        cbUrl = deferredURL;
                        deferred = true;
                    }
                }
                if (false == deferred && metaData.registerTokenBasedDeferrer()) {
                    // Not yet deferred, but wants to
                }
            }
            /*
             * Get token & authorization URL
             */
            boolean tokenRegistered = false;
            Token scribeToken;
            StringBuilder authorizationURL;
            if (metaData.registerTokenBasedDeferrer() && metaData.needsRequestToken()) {
                try {
                    URI uri = new URI(cbUrl);

                    String path;
                    {
                        DispatcherPrefixService prefixService = Services.getService(DispatcherPrefixService.class);
                        StringBuilder pathBuilder = new StringBuilder(prefixService.getPrefix()).append("defer");
                        if (pathBuilder.charAt(0) != '/') {
                            pathBuilder.insert(0, '/');
                        }
                        path = pathBuilder.toString();
                    }

                    String prevCbUrl = cbUrl;
                    cbUrl = new StringBuilder(uri.getScheme()).append("://").append(uri.getHost()).append(path).toString();

                    org.scribe.oauth.OAuthService service = getScribeService(metaData, cbUrl, session);
                    scribeToken = service.getRequestToken();
                    authorizationURL = new StringBuilder(service.getAuthorizationUrl(scribeToken));

                    callbackRegistry.add(scribeToken.getToken(), prevCbUrl);
                    tokenRegistered = true;
                } catch (URISyntaxException e) {
                    org.scribe.oauth.OAuthService service = getScribeService(metaData, cbUrl, session);
                    scribeToken = service.getRequestToken();
                    authorizationURL = new StringBuilder(service.getAuthorizationUrl(scribeToken));
                }
            } else {
                org.scribe.oauth.OAuthService service = getScribeService(metaData, cbUrl, session);
                scribeToken = metaData.needsRequestToken() ? service.getRequestToken() : null;
                authorizationURL = new StringBuilder(service.getAuthorizationUrl(scribeToken));
            }
            /*
             * Add optional scope
             */
            {
                final String scope = metaData.getScope();
                if (scope != null && authorizationURL.indexOf("&scope=") < 0) {
                    authorizationURL.append("&scope=").append(urlEncode(scope));
                }
            }
            /*
             * Process authorization URL
             */
            final String authURL = metaData.processAuthorizationURLCallbackAware(metaData.processAuthorizationURL(authorizationURL.toString()), cbUrl);
            /*
             * Register deferrer
             */
            if (!tokenRegistered && metaData.registerTokenBasedDeferrer()) {
                // Register by token
                if (null != scribeToken) {
                    registerTokenForDeferredAccess(scribeToken.getToken(), cbUrl, ds, userId, contextId);
                } else {
                    String registerToken = metaData.getRegisterToken(authURL);
                    if (null != registerToken) {
                        registerTokenForDeferredAccess(registerToken, cbUrl, ds, userId, contextId);
                    }
                }
            }
            /*
             * Return interaction
             */
            OAuthToken requestToken = scribeToken == null ? OAuthToken.EMPTY_TOKEN : new ScribeOAuthToken(scribeToken);
            OAuthInteractionType interactionType = cbUrl == null ? OAuthInteractionType.OUT_OF_BAND : OAuthInteractionType.CALLBACK;
            return new OAuthInteractionImpl(requestToken, authURL, interactionType);
        } catch (final org.scribe.exceptions.OAuthException e) {
            throw handleScribeOAuthException(e);
        } catch (final Exception e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void registerTokenForDeferredAccess(final String token, String cbUrl, final DeferringURLService ds, final int userId, final int contextId) {
        // Is only applicable if call-back URL is deferred; e.g. /ajax/defer?redirect=http:%2F%2Fmy.host.com%2Fpath...
        if (isDeferrerAvailable(ds, userId, contextId)) {
            if (ds.seemsDeferred(cbUrl, userId, contextId)) {
                callbackRegistry.add(token, cbUrl);
            } else {
                LOG.warn("Call-back URL cannot be registered as it is not deferred: {}", Strings.abbreviate(cbUrl, 32));
            }
        } else {
            // No chance to check
            callbackRegistry.add(token, cbUrl);
        }
    }

    private boolean isDeferrerAvailable(final DeferringURLService ds, final int userId, final int contextId) {
        return null != ds && ds.isDeferrerURLAvailable(userId, contextId);
    }

    private static String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }

    @Override
    public OAuthAccount createAccount(final String serviceMetaData, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        try {
            /*
             * Create appropriate OAuth account instance
             */
            final DefaultOAuthAccount account = new DefaultOAuthAccount();
            /*
             * Determine associated service's meta data
             */
            final OAuthServiceMetaData service = registry.getService(serviceMetaData, user, contextId);
            account.setMetaData(service);
            /*
             * Set display name & identifier
             */
            final String displayName = (String) arguments.get(OAuthConstants.ARGUMENT_DISPLAY_NAME);
            if (Strings.isEmpty(displayName)) {
                throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_DISPLAY_NAME);
            }
            account.setDisplayName(displayName);
            account.setId(idGenerator.getId(OAuthConstants.TYPE_ACCOUNT, contextId));
            /*
             * Token & secret
             */
            account.setToken((String) arguments.get(OAuthConstants.ARGUMENT_TOKEN));
            account.setSecret((String) arguments.get(OAuthConstants.ARGUMENT_SECRET));
            /*
             * Crypt tokens
             */
            final Session session = (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION);
            if (null == session) {
                throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_SESSION);
            }
            account.setToken(encrypt(account.getToken(), session));
            account.setSecret(encrypt(account.getSecret(), session));
            /*
             * Create INSERT command
             */
            final ArrayList<Object> values = new ArrayList<Object>(SQLStructure.OAUTH_COLUMN.values().length);
            final INSERT insert = SQLStructure.insertAccount(account, contextId, user, values);
            /*
             * Execute INSERT command
             */
            executeUpdate(contextId, insert, values);
            LOG.info("Created new {} account with ID {} for user {} in context {}", serviceMetaData, account.getId(), user, contextId);
            /*
             * Return newly created account
             */
            return account;
        } catch (final OXException x) {
            throw x;
        }
    }

    @Override
    public OAuthAccount createAccount(final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        try {
            /*
             * Create appropriate OAuth account instance
             */
            final DefaultOAuthAccount account = new DefaultOAuthAccount();
            /*
             * Determine associated service's meta data
             */
            final OAuthServiceMetaData service = registry.getService(serviceMetaData, user, contextId);
            account.setMetaData(service);
            /*
             * Set display name & identifier
             */
            final String displayName = (String) arguments.get(OAuthConstants.ARGUMENT_DISPLAY_NAME);
            if (Strings.isEmpty(displayName)) {
                throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_DISPLAY_NAME);
            }
            account.setDisplayName(displayName);
            account.setId(idGenerator.getId(OAuthConstants.TYPE_ACCOUNT, contextId));
            /*
             * Obtain & apply the access token
             */
            obtainToken(type, arguments, account);
            /*
             * Encrypt token & secret
             */
            final Session session = (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION);
            if (null == session) {
                throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_SESSION);
            }
            if (Strings.isEmpty(account.getToken())) {
                throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_TOKEN);
            }
            account.setToken(encrypt(account.getToken(), session));
            if (null == account.getSecret()) {
                throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_SECRET);
            }
            account.setSecret(encrypt(account.getSecret(), session));
            /*
             * Create INSERT command
             */
            final ArrayList<Object> values = new ArrayList<Object>(SQLStructure.OAUTH_COLUMN.values().length);
            final INSERT insert = SQLStructure.insertAccount(account, contextId, user, values);
            /*
             * Execute INSERT command
             */
            executeUpdate(contextId, insert, values);
            LOG.info("Created new {} account with ID {} for user {} in context {}", serviceMetaData, account.getId(), user, contextId);
            /*
             * Return newly created account
             */
            return account;
        } catch (final OXException x) {
            throw x;
        }
    }

    private void executeUpdate(final int contextId, final Command command, final List<Object> values) throws OXException {
        final Context ctx = getContext(contextId);
        final Connection writeCon = getConnection(false, ctx);
        try {
            new StatementBuilder().executeStatement(writeCon, command, values);
        } catch (final SQLException e) {
            LOG.error(e.toString());
            throw OAuthExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            if (writeCon != null) {
                provider.releaseWriteConnection(ctx, writeCon);
            }
        }
    }

    @Override
    public void deleteAccount(final int accountId, final int user, final int contextId) throws OXException {
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        startTransaction(con);
        boolean committed = false;
        try {
            deleteAccount(accountId, user, contextId, con);
            con.commit(); // COMMIT
            committed = true;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (!committed) {
                rollback(con);
            }
            autocommit(con);
            provider.releaseReadConnection(context, con);
        }
    }

    private void deleteAccount(final int accountId, final int user, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            final DeleteListenerRegistry deleteListenerRegistry = DeleteListenerRegistry.getInstance();
            final Map<String, Object> properties = Collections.<String, Object> emptyMap();
            deleteListenerRegistry.triggerOnBeforeDeletion(accountId, properties, user, contextId, con);
            stmt = con.prepareStatement("DELETE FROM oauthAccounts WHERE cid = ? AND user = ? and id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            stmt.setInt(3, accountId);
            stmt.executeUpdate();
            deleteListenerRegistry.triggerOnAfterDeletion(accountId, properties, user, contextId, con);
            /*
             * Post folder event
             */
            postOAuthDeleteEvent(accountId, user, contextId);
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void postOAuthDeleteEvent(final int accountId, final int userId, final int contextId) {
        final Session session = getUserSession(userId, contextId);
        if (null == session) {
            /*
             * No session available
             */
            return;
        }
        final EventAdmin eventAdmin = Services.getService(EventAdmin.class);
        if (null == eventAdmin) {
            /*
             * Missing event admin service
             */
            return;
        }
        final Dictionary<String, Object> props = new Hashtable<String, Object>(4);
        props.put(OAuthEventConstants.PROPERTY_SESSION, session);
        props.put(OAuthEventConstants.PROPERTY_CONTEXT, Integer.valueOf(contextId));
        props.put(OAuthEventConstants.PROPERTY_USER, Integer.valueOf(userId));
        props.put(OAuthEventConstants.PROPERTY_ID, Integer.valueOf(accountId));
        final Event event = new Event(OAuthEventConstants.TOPIC_DELETE, props);
        /*
         * Finally deliver it
         */
        eventAdmin.sendEvent(event);
    }

    private static Session getUserSession(final int userId, final int contextId) {
     // Firstly let's see if the currently active session matches the one we need here and prefer that one.
        final SessionHolder sessionHolder = Services.getService(SessionHolder.class);
        if (sessionHolder != null) {
            final Session session = sessionHolder.getSessionObject();
            if (session != null && session.getUserId() == userId && session.getContextId() == contextId) {
                return session;
            }
        }
        final SessiondService service = Services.getService(SessiondService.class);
        if (null == service) {
            return null;
        }
        return service.getAnyActiveSessionForUser(userId, contextId);
    }

    @Override
    public void updateAccount(final int accountId, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        final List<Setter> list = setterFrom(arguments);
        if (list.isEmpty()) {
            /*
             * Nothing to update
             */
            return;
        }
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        PreparedStatement stmt = null;
        try {
            final StringBuilder stmtBuilder = new StringBuilder(128).append("UPDATE oauthAccounts SET ");
            final int size = list.size();
            list.get(0).appendTo(stmtBuilder);
            for (int i = 1; i < size; i++) {
                stmtBuilder.append(", ");
                list.get(i).appendTo(stmtBuilder);
            }
            stmt = con.prepareStatement(stmtBuilder.append(" WHERE cid = ? AND user = ? and id = ?").toString());
            int pos = 1;
            for (final Setter setter : list) {
                pos = setter.set(pos, stmt);
            }
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, user);
            stmt.setInt(pos, accountId);
            final int rows = stmt.executeUpdate();
            if (rows <= 0) {
                throw OAuthExceptionCodes.ACCOUNT_NOT_FOUND.create(
                    Integer.valueOf(accountId),
                    Integer.valueOf(user),
                    Integer.valueOf(contextId));
            }
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    @Override
    public OAuthAccount getAccount(final int accountId, final Session session, final int user, final int contextId) throws OXException {
        final SecretEncryptionService<PWUpdate> encryptionService = Services.getService(SecretEncryptionFactoryService.class).createService(this);
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT displayName, accessToken, accessSecret, serviceId FROM oauthAccounts WHERE cid = ? AND user = ? and id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            stmt.setInt(3, accountId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OAuthExceptionCodes.ACCOUNT_NOT_FOUND.create(
                    Integer.valueOf(accountId),
                    Integer.valueOf(user),
                    Integer.valueOf(contextId));
            }
            final DefaultOAuthAccount account = new DefaultOAuthAccount();
            account.setId(accountId);
            account.setDisplayName(rs.getString(1));
            account.setToken(encryptionService.decrypt(session, rs.getString(2), new PWUpdate("accessToken", contextId, accountId)));
            account.setSecret(encryptionService.decrypt(session, rs.getString(3), new PWUpdate("accessSecret", contextId, accountId)));
            account.setMetaData(registry.getService(rs.getString(4), user, contextId));
            return account;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    @Override
    public OAuthAccount getDefaultAccount(final API api, final Session session) throws OXException {
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        final List<OAuthServiceMetaData> allServices = registry.getAllServices(userId, contextId);
        for (final OAuthServiceMetaData metaData : allServices) {
            if (metaData.getAPI() == api) {
                final List<OAuthAccount> accounts = getAccounts(metaData.getId(), session, userId, contextId);
                OAuthAccount likely = null;
                for (final OAuthAccount acc : accounts) {
                    if (likely == null || acc.getId() < likely.getId()) {
                        likely = acc;
                    }
                }
                if (likely != null) {
                    return likely;
                }
            }
        }
        throw OAuthExceptionCodes.ACCOUNT_NOT_FOUND.create("default:" + api.toString(), Integer.valueOf(userId), Integer.valueOf(contextId));
    }

    @Override
    public OAuthAccount updateAccount(final int accountId, final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        try {
            /*
             * Create appropriate OAuth account instance
             */
            final DefaultOAuthAccount account = new DefaultOAuthAccount();
            /*
             * Determine associated service's meta data
             */
            final OAuthServiceMetaData service = registry.getService(serviceMetaData, user, contextId);
            account.setMetaData(service);
            /*
             * Set display name & identifier
             */
            final String displayName = (String) arguments.get(OAuthConstants.ARGUMENT_DISPLAY_NAME);
            account.setDisplayName(null == displayName ? null : displayName);
            account.setId(accountId);
            /*
             * Obtain & apply the access token
             */
            obtainToken(type, arguments, account);
            /*
             * Crypt tokens
             */
            final Session session = (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION);
            if (null == session) {
                throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_SESSION);
            }
            account.setToken(encrypt(account.getToken(), session));
            account.setSecret(encrypt(account.getSecret(), session));
            /*
             * Create UPDATE command
             */
            final ArrayList<Object> values = new ArrayList<Object>(SQLStructure.OAUTH_COLUMN.values().length);
            final UPDATE update = SQLStructure.updateAccount(account, contextId, user, values);
            /*
             * Execute UPDATE command
             */
            executeUpdate(contextId, update, values);
            /*
             * Return the account
             */
            return account;
        } catch (final OXException x) {
            throw x;
        }
    }

    // OAuth

    protected void obtainToken(final OAuthInteractionType type, final Map<String, Object> arguments, final DefaultOAuthAccount account) throws OXException {
        switch (type) {
        case OUT_OF_BAND:
            obtainTokenByOutOfBand(arguments, account);
            break;
        case CALLBACK:
            obtainTokenByCallback(arguments, account);
            break;
        default:
            break;
        }
    }

    protected void obtainTokenByOutOfBand(final Map<String, Object> arguments, final DefaultOAuthAccount account) throws OXException {
        try {
            final OAuthServiceMetaData metaData = account.getMetaData();
            final OAuthToken oAuthToken = metaData.getOAuthToken(arguments);
            if (null == oAuthToken) {
                final String pin = (String) arguments.get(OAuthConstants.ARGUMENT_PIN);
                if (null == pin) {
                    throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_PIN);
                }
                final OAuthToken requestToken = (OAuthToken) arguments.get(OAuthConstants.ARGUMENT_REQUEST_TOKEN);
                if (null == requestToken) {
                    throw OAuthExceptionCodes.MISSING_ARGUMENT.create(OAuthConstants.ARGUMENT_REQUEST_TOKEN);
                }

                Session session = (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION);
                /*
                 * With the request token and the verifier (which is a number) we need now to get the access token
                 */
                final Verifier verifier = new Verifier(pin);
                final org.scribe.oauth.OAuthService service = getScribeService(account.getMetaData(), null, session);
                final Token accessToken = service.getAccessToken(new Token(requestToken.getToken(), requestToken.getSecret()), verifier);
                /*
                 * Apply to account
                 */
                account.setToken(accessToken.getToken());
                account.setSecret(accessToken.getSecret());
            } else {
                account.setToken(oAuthToken.getToken());
                account.setSecret(oAuthToken.getSecret());
            }
        } catch (final org.scribe.exceptions.OAuthException e) {
            throw handleScribeOAuthException(e);
        } catch (final OXException e) {
            throw e;
        } catch (final Exception e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    protected void obtainTokenByCallback(final Map<String, Object> arguments, final DefaultOAuthAccount account) throws OXException {
        obtainTokenByOutOfBand(arguments, account);
    }

    // Helper Methods

    private static org.scribe.oauth.OAuthService getScribeService(final OAuthServiceMetaData metaData, final String callbackUrl, Session session) throws OXException {
        final Class<? extends Api> apiClass;
        if (metaData instanceof com.openexchange.oauth.ScribeAware) {
            apiClass = ((com.openexchange.oauth.ScribeAware) metaData).getScribeService();
        } else {
            final String serviceId = Strings.asciiLowerCase(metaData.getId());
            if (serviceId.indexOf("twitter") >= 0) {
                apiClass = TwitterApi.class;
            } else if (serviceId.indexOf("linkedin") >= 0) {
                apiClass = LinkedInApi.class;
            } else if (serviceId.indexOf("google") >= 0) {
                apiClass = Google2Api.class;
            } else if (serviceId.indexOf("yahoo") >= 0) {
                apiClass = YahooApi.class;
            } else if (serviceId.indexOf("foursquare") >= 0) {
                apiClass = FoursquareApi.class;
            } else if (serviceId.indexOf("tumblr") >= 0) {
                apiClass = TumblrApi.class;
            } else if (serviceId.indexOf("flickr") >= 0) {
                apiClass = FlickrApi.class;
            } else if (serviceId.indexOf("dropbox") >= 0) {
                apiClass = DropBoxApi.class;
            } else if (serviceId.indexOf("xing") >= 0) {
                apiClass = XingApi.class;
            } else if (serviceId.indexOf("vkontakte") >= 0) {
                apiClass = VkontakteApi.class;
            } else {
                throw OAuthExceptionCodes.UNSUPPORTED_SERVICE.create(serviceId);
            }
        }
        final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(apiClass);
        serviceBuilder.apiKey(metaData.getAPIKey(session)).apiSecret(metaData.getAPISecret(session));
        if (null != callbackUrl) {
            serviceBuilder.callback(callbackUrl);
        }
        final String scope = metaData.getScope();
        if (null != scope) {
            serviceBuilder.scope(scope);
        }
        return serviceBuilder.build();
    }

    private Connection getConnection(final boolean readOnly, final Context context) throws OXException {
        return readOnly ? provider.getReadConnection(context) : provider.getWriteConnection(context);
    }

    private Context getContext(final int contextId) throws OXException {
        try {
            return contexts.getContext(contextId);
        } catch (final OXException e) {
            throw e;
        }
    }

    private static interface Setter {

        void appendTo(StringBuilder stmtBuilder);

        int set(int pos, PreparedStatement stmt) throws SQLException;
    }

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
         * Other arguments?
         */
        return ret;
    }

    private String encrypt(final String toEncrypt, final Session session) throws OXException {
        if (Strings.isEmpty(toEncrypt)) {
            return toEncrypt;
        }
        final SecretEncryptionService<PWUpdate> service = Services.getService(SecretEncryptionFactoryService.class).createService(this);
        return service.encrypt(session, toEncrypt);
    }

    @Override
    public void update(final String recrypted, final PWUpdate customizationNote) throws OXException {
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

    @Override
    public boolean hasEncryptedItems(final ServerSession session) throws OXException {
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

    @Override
    public void migrate(final String oldSecret, final String newSecret, final ServerSession session) throws OXException {
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

    private static void startTransaction(final Connection con) throws OXException {
        try {
            con.setAutoCommit(false); // BEGIN
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static OXException handleScribeOAuthException(org.scribe.exceptions.OAuthException e) {
        final String message = e.getMessage();
        if (null != message) {
            final String lcMsg = com.openexchange.java.Strings.toLowerCase(message);
            String str = "can't extract token and secret from this:";
            int pos = lcMsg.indexOf(str);
            if (pos > 0) {
                final String msg = toText(message.substring(pos + str.length()));
                return OAuthExceptionCodes.DENIED_BY_PROVIDER.create(e, msg);
            }
            str = "can't extract a token from an empty string";
            pos = lcMsg.indexOf(str);
            if (pos > 0) {
                final String msg = toText(message.substring(pos));
                return OAuthExceptionCodes.DENIED_BY_PROVIDER.create(e, msg);
            }
            str = "can't extract a token from this:";
            pos = lcMsg.indexOf(str);
            if (pos > 0) {
                final String msg = toText(message.substring(pos + str.length()));
                return OAuthExceptionCodes.DENIED_BY_PROVIDER.create(e, msg);
            }
        }
        if (e instanceof org.scribe.exceptions.OAuthConnectionException) {
            return OAuthExceptionCodes.CONNECT_ERROR.create(e, e.getMessage());
        }
        return OAuthExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
    }

    private static String toText(final String msg) {
        final HtmlService htmlService = Services.getService(HtmlService.class);
        if (null != htmlService) {
            if (com.openexchange.java.HTMLDetector.containsHTMLTags(Charsets.toAsciiBytes(msg), 0, msg.length())) {
                return htmlService.html2text(msg, false);
            }
        }
        return msg;
    }
}
