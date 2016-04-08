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

package com.openexchange.subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link AbstractSubscribeService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractSubscribeService implements SubscribeService {

    public static final AtomicReference<SubscriptionStorage> STORAGE = new AtomicReference<SubscriptionStorage>();

    public static final AtomicReference<SecretEncryptionFactoryService> ENCRYPTION_FACTORY = new AtomicReference<SecretEncryptionFactoryService>();

    public static final AtomicReference<CryptoService> CRYPTO_SERVICE = new AtomicReference<CryptoService>();

    public static final AtomicReference<FolderService> FOLDERS = new AtomicReference<FolderService>();

    public static final AtomicReference<UserPermissionService> USER_PERMISSIONS = new AtomicReference<UserPermissionService>();

    /**
     * Enabled by default - override as needed.
     */
    @Override
    public boolean isCreateModifyEnabled() {
        return true;
    }

    @Override
    public Collection<Subscription> loadSubscriptions(final Context ctx, final String folderId, final String secret) throws OXException {
        final List<Subscription> allSubscriptions = STORAGE.get().getSubscriptions(ctx, folderId);
        return prepareSubscriptions(allSubscriptions, secret, ctx, -1);
    }

    @Override
    public Collection<Subscription> loadSubscriptions(final Context context, final int userId, final String secret) throws OXException {
        final List<Subscription> allSubscriptions = STORAGE.get().getSubscriptionsOfUser(context, userId);
        return prepareSubscriptions(allSubscriptions, secret, context, userId);
    }

    private Collection<Subscription> prepareSubscriptions(final List<Subscription> allSubscriptions, final String secret, final Context context, final int userId) throws OXException {
        final List<Subscription> subscriptions = new ArrayList<Subscription>();
        final Map<String, Boolean> canSee = new HashMap<String, Boolean>();

        for (final Subscription subscription : allSubscriptions) {
            if (subscription.getSource() != null && getSubscriptionSource() != null && subscription.getSource().getId().equals(
                getSubscriptionSource().getId())) {

                if(userId == -1) {
                    subscriptions.add(subscription);
                } else if (canSee.containsKey(subscription.getFolderId()) && canSee.get(subscription.getFolderId())) {
                    subscriptions.add(subscription);
                } else {
                    final EffectivePermission folderPermission = FOLDERS.get().getFolderPermission(Integer.parseInt(subscription.getFolderId()), userId, context.getContextId());
                    final boolean visible = folderPermission.isFolderVisible() ;
                    canSee.put(subscription.getFolderId(), visible);
                    if(visible) {
                        subscriptions.add(subscription);
                    }

                }

            }
        }
        for (final Subscription subscription : subscriptions) {
            subscription.setSecret(secret);
            modifyOutgoing(subscription);
        }

        return subscriptions;
    }


    @Override
    public Subscription loadSubscription(final Context ctx, final int subscriptionId, final String secret) throws OXException {
        final Subscription subscription = STORAGE.get().getSubscription(ctx, subscriptionId);
        if (null == subscription) {
            return null;
        }
        subscription.setSecret(secret);
        modifyOutgoing(subscription);
        return subscription;
    }

    @Override
    public void subscribe(final Subscription subscription) throws OXException {
        checkCreate(subscription);
        modifyIncoming(subscription);
        if (checkForDuplicate()) {
            doCheckForDuplicate(subscription);
        }
        STORAGE.get().rememberSubscription(subscription);
        modifyOutgoing(subscription);
    }

    @Override
    public void unsubscribe(final Subscription subscription) throws OXException {
        final Subscription loadedSubscription = loadSubscription(subscription.getContext(), subscription.getId(), null);
        if (null == loadedSubscription) {
            throw SubscriptionErrorMessage.SubscriptionNotFound.create();
        }
        if (loadedSubscription.getSession() == null) {
            loadedSubscription.setSession(subscription.getSession());
        }
        checkDelete(loadedSubscription);
        STORAGE.get().forgetSubscription(subscription);
    }

    @Override
    public void update(final Subscription subscription) throws OXException {
        final Subscription loadedSubscription = loadSubscription(subscription.getContext(), subscription.getId(), null);
        if (null == loadedSubscription) {
            throw SubscriptionErrorMessage.SubscriptionNotFound.create();
        }
        checkUpdate(loadedSubscription);
        modifyIncoming(subscription);
        STORAGE.get().updateSubscription(subscription);
        modifyOutgoing(subscription);
    }

    public void modifyIncoming(final Subscription subscription) throws OXException {
        Object accountIDObject = subscription.getConfiguration().get("account");
        if (JSONObject.NULL == accountIDObject) {
            throw SubscriptionErrorMessage.NO_OAUTH_ACCOUNT_GIVEN.create();
        }
    }

    public void modifyOutgoing(final Subscription subscription) throws OXException {
        // Empty body
    }

    @Override
    public boolean knows(final Context ctx, final int subscriptionId) throws OXException {
        final Subscription subscription = STORAGE.get().getSubscription(ctx, subscriptionId);
        if (subscription == null) {
            return false;
        }
        if (subscription.getSource().getId().equals(getSubscriptionSource().getId())) {
            return true;
        }
        return false;
    }

    @Override
    public void touch(Context ctx, int subscriptionId) throws OXException {
        STORAGE.get().touch(ctx, subscriptionId, System.currentTimeMillis());
    }

    public static void encrypt(final Session session, final Map<String, Object> map, final String... keys) throws OXException {
        final SecretEncryptionFactoryService encryptionFactoryService = ENCRYPTION_FACTORY.get();
        if (encryptionFactoryService == null) {
            return;
        }
        if (session == null) {
            return;
        }
        final SecretEncryptionService<EncryptedField> encryptionService = encryptionFactoryService.createService(STORAGE.get());
        for (final String key : keys) {
            if (map.containsKey(key)) {
                final String toEncrypt = (String) map.get(key);
                final String encrypted = encryptionService.encrypt(session, toEncrypt);
                map.put(key, encrypted);
            }
        }
    }

    public static void decrypt(final Subscription subscription, final Session session, final Map<String, Object> map, final String... keys) throws OXException {
        final SecretEncryptionFactoryService encryptionFactoryService = ENCRYPTION_FACTORY.get();
        if (encryptionFactoryService == null) {
            return;
        }
        if (session == null) {
            return;
        }

        SecretEncryptionService<EncryptedField> encryptionService = encryptionFactoryService.createService(STORAGE.get());
        for (String key : keys) {
            if (map.containsKey(key)) {
                EncryptedField encryptedField = new EncryptedField(subscription, key);

                String toDecrypt = (String) map.get(key);
                String decrypted;
                try {
                    decrypted = encryptionService.decrypt(session, toDecrypt, encryptedField);
                } catch (Exception e) {
                    // Fail silently
                    decrypted = null;
                }
                map.put(key, decrypted);
            }
        }
    }

    @Override
    public boolean hasAccounts(final Context ctx, final User user) throws OXException {
        final Set<String> passwordFields = getSubscriptionSource().getPasswordFields();
        if (passwordFields.isEmpty()) {
            return false;
        }
        return STORAGE.get().hasSubscriptions(ctx, user);
    }

    @Override
    public void migrateSecret(final Session session, final String oldSecret, final String newSecret) throws OXException {
        final SubscriptionSource subscriptionSource = getSubscriptionSource();
        final Set<String> passwordFields = subscriptionSource.getPasswordFields();
        if (passwordFields.isEmpty()) {
            return;
        }
        final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        final List<Subscription> allSubscriptions = STORAGE.get().getSubscriptionsOfUser(serverSession.getContext(), session.getUserId());
        final String id = subscriptionSource.getId();
        final CryptoService cryptoService = CRYPTO_SERVICE.get();
        final Map<String, Object> update = new HashMap<String, Object>();
        for (final Subscription subscription : allSubscriptions) {
            if (id.equals(getSubscriptionSourceId(subscription))) {
                final Map<String, Object> configuration = subscription.getConfiguration();
                update.clear();
                boolean save = false;
                for (final String passwordField : passwordFields) {
                    final String password = (String) configuration.get(passwordField);
                    if (!com.openexchange.java.Strings.isEmpty(password)) {
                        try {
                            // If we can already decrypt with the new secret, we're done with this entry
                            cryptoService.decrypt(password, newSecret);
                        } catch (final OXException x) {
                            // This one needs migration
                            final String transcriptedPassword = cryptoService.encrypt(cryptoService.decrypt(password, oldSecret), newSecret);
                            update.put(passwordField, transcriptedPassword);
                            save = true;
                        }
                    }
                }
                if (save) {
                    subscription.setConfiguration(update);
                    STORAGE.get().updateSubscription(subscription);
                }
            }
        }
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        final SubscriptionSource subscriptionSource = getSubscriptionSource();
        final Set<String> passwordFields = subscriptionSource.getPasswordFields();
        if (passwordFields.isEmpty()) {
            return;
        }
        final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        final List<Subscription> allSubscriptions = STORAGE.get().getSubscriptionsOfUser(serverSession.getContext(), session.getUserId());
        final String id = subscriptionSource.getId();
        final CryptoService cryptoService = CRYPTO_SERVICE.get();
        final Map<String, Object> update = new HashMap<String, Object>();
        for (final Subscription subscription : allSubscriptions) {
            if (id.equals(getSubscriptionSourceId(subscription))) {
                final Map<String, Object> configuration = subscription.getConfiguration();
                update.clear();
                boolean save = false;
                for (final String passwordField : passwordFields) {
                    final String password = (String) configuration.get(passwordField);
                    if (!com.openexchange.java.Strings.isEmpty(password)) {
                        try {
                            // If we can already decrypt with the new secret, we're done with this entry
                            cryptoService.decrypt(password, secret);
                        } catch (final OXException x) {
                            // This one needs clean-up
                            update.put(passwordField, "");
                            save = true;
                        }
                    }
                }
                if (save) {
                    subscription.setConfiguration(update);
                    STORAGE.get().updateSubscription(subscription);
                }
            }
        }
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        final SubscriptionSource subscriptionSource = getSubscriptionSource();
        final Set<String> passwordFields = subscriptionSource.getPasswordFields();
        if (passwordFields.isEmpty()) {
            return;
        }
        final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        final List<Subscription> allSubscriptions = STORAGE.get().getSubscriptionsOfUser(serverSession.getContext(), session.getUserId());
        final String id = subscriptionSource.getId();
        final CryptoService cryptoService = CRYPTO_SERVICE.get();

        List<Subscription> subscriptionsToDelete = new ArrayList<Subscription>(allSubscriptions.size());

        for (final Subscription subscription : allSubscriptions) {
            if (id.equals(getSubscriptionSourceId(subscription))) {
                final Map<String, Object> configuration = subscription.getConfiguration();
                for (final String passwordField : passwordFields) {
                    final String password = (String) configuration.get(passwordField);
                    if (!com.openexchange.java.Strings.isEmpty(password)) {
                        try {
                            // If we can already decrypt with the new secret, we're done with this entry
                            cryptoService.decrypt(password, secret);
                        } catch (final OXException x) {
                            // This one needs clean-up
                            if (!subscriptionsToDelete.contains(subscription)) {
                                subscriptionsToDelete.add(subscription);
                            }
                        }
                    }
                }
            }
        }

        for (Subscription subscription : subscriptionsToDelete) {
            if (null == subscription.getSession()) {
                subscription.setSession(serverSession);
            }
            unsubscribe(subscription);
        }
    }

    /**
     * Override if possible to allow pipelined processing of the subscription's content.
     */
    @Override
    public SearchIterator<?> loadContent(Subscription subscription) throws OXException {
        return new SearchIteratorDelegator(getContent(subscription));
    }

    private static String getSubscriptionSourceId(final Subscription subscription) {
        if (null == subscription) {
            return null;
        }
        final SubscriptionSource source = subscription.getSource();
        return null == source ? null : source.getId();
    }

    protected void removeWhereConfigMatches(final Context context, final Map<String, Object> query) throws OXException {
        STORAGE.get().deleteAllSubscriptionsWhereConfigMatches(query, getSubscriptionSource().getId(), context);
    }

    // Check for duplicate

    /**
     * Signals if a check for a duplicate subscription is supposed to be performed
     *
     * @return <code>true</code> to check for a duplicate subscription; otherwise <code>false</code>
     */
    public boolean checkForDuplicate() {
        return false;
    }

    /**
     * Performs the check for a duplicate subscription
     *
     * @param subscription The subscription to check for
     * @throws OXException If there is a duplicate subscription
     */
    public void doCheckForDuplicate(Subscription subscription) throws OXException {
        Map<String, Object> configuration = subscription.getConfiguration();
        if (null == configuration) {
            // Cannot check for possible duplicate
            return;
        }

        List<Subscription> subscriptions = STORAGE.get().getSubscriptionsOfUser(subscription.getContext(), subscription.getUserId(), subscription.getSource().getId());
        for (Subscription existingSubscription : subscriptions) {
            Map<String, Object> existingConfiguration = existingSubscription.getConfiguration();
            if (null != existingConfiguration && isEqualConfiguration(configuration, existingConfiguration)) {
                throw SubscriptionErrorMessage.DUPLICATE_SUBSCRIPTION.create(subscription.getSource().getId(), subscription.getUserId(), subscription.getContext().getContextId());
            }
        }
    }

    private boolean isEqualConfiguration(Map<String, Object> config1, Map<String, Object> config2) {
        if (config2.size() != config1.size()) {
            return false;
        }

        try {
            for (Entry<String, Object> e : config1.entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                if (value == null) {
                    if (!(config2.get(key) == null && config2.containsKey(key))) {
                        return false;
                    }
                } else {
                    if (!value.equals(config2.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    // Permission Checks

    public void checkCreate(final Subscription subscription) throws OXException {
        if (canWrite(subscription)) {
            return;
        }
        throw SubscriptionErrorMessage.PERMISSION_DENIED.create();
    }
    public void checkUpdate(final Subscription subscription) throws OXException {
        if (subscription.getSession().getUserId() == subscription.getUserId() || isFolderAdmin(subscription)) {
            return;
        }
        throw SubscriptionErrorMessage.PERMISSION_DENIED.create();
    }

    public void checkDelete(final Subscription subscription) throws OXException {
        if (subscription.getSession().getUserId() == subscription.getUserId() || isFolderAdmin(subscription)) {
            return;
        }
        throw SubscriptionErrorMessage.PERMISSION_DENIED.create();
    }

    private boolean canWrite(final Subscription subscription) throws OXException {
        final OCLPermission permission = loadFolderPermission(subscription);
        return permission.isFolderAdmin() || permission.getFolderPermission() >= OCLPermission.ADMIN_PERMISSION ||  ( permission.getFolderPermission() >= OCLPermission.CREATE_OBJECTS_IN_FOLDER && permission.getWritePermission() >= OCLPermission.WRITE_ALL_OBJECTS);
    }

    private boolean isFolderAdmin(final Subscription subscription) throws OXException {
        final OCLPermission permission = loadFolderPermission(subscription);
        return  permission.isFolderAdmin() || permission.getFolderPermission() >= OCLPermission.ADMIN_PERMISSION;
    }

    private OCLPermission loadFolderPermission(final Subscription subscription) throws OXException {
        final int folderId = Integer.parseInt(subscription.getFolderId());
        final int userId = subscription.getSession().getUserId();
        final Context ctx = subscription.getContext();
        final UserPermissionBits userPerm = USER_PERMISSIONS.get().getUserPermissionBits(userId, ctx);

        return new OXFolderAccess(ctx).getFolderPermission(folderId, userId, userPerm);
    }
}
