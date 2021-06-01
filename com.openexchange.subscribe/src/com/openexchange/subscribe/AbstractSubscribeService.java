/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.subscribe;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link AbstractSubscribeService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractSubscribeService implements SubscribeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSubscribeService.class);

    private static final String PASSWORD = "password";

    // Services
    public static final AtomicReference<SubscriptionStorage> STORAGE = new AtomicReference<>();
    public static final AtomicReference<SecretEncryptionFactoryService> ENCRYPTION_FACTORY = new AtomicReference<>();
    public static final AtomicReference<CryptoService> CRYPTO_SERVICE = new AtomicReference<>();
    public static final AtomicReference<FolderService> FOLDERS = new AtomicReference<>();
    public static final AtomicReference<UserPermissionService> USER_PERMISSIONS = new AtomicReference<>();

    private final SubscriptionErrorMarker marker;

    /**
     * Initializes a new {@link AbstractSubscribeService}.
     *
     * @param folderService The {@link com.openexchange.folderstorage.FolderService} to use
     */
    public AbstractSubscribeService(com.openexchange.folderstorage.FolderService folderService) {
        super();
        marker = new SubscriptionErrorMarker(this, folderService);
    }

    /**
     * Enabled by default - override as needed.
     */
    @Override
    public boolean isCreateModifyEnabled() {
        return true;
    }

    @Override
    public Collection<Subscription> loadSubscriptions(Context ctx, String folderId, String secret) throws OXException {
        List<Subscription> allSubscriptions = STORAGE.get().getSubscriptions(ctx, folderId);
        return prepareSubscriptions(allSubscriptions, secret, ctx, -1);
    }

    @Override
    public Collection<Subscription> loadSubscriptions(Context context, int userId, String secret) throws OXException {
        List<Subscription> allSubscriptions = STORAGE.get().getSubscriptionsOfUser(context, userId);
        return prepareSubscriptions(allSubscriptions, secret, context, userId);
    }

    private Collection<Subscription> prepareSubscriptions(List<Subscription> allSubscriptions, String secret, Context context, int userId) throws OXException {
        List<Subscription> subscriptions = new ArrayList<>();
        Map<String, Boolean> canSee = new HashMap<>();

        for (Subscription subscription : allSubscriptions) {
            if (subscription.getSource() != null && getSubscriptionSource() != null && subscription.getSource().getId().equals(getSubscriptionSource().getId())) {

                if (userId == -1) {
                    subscriptions.add(subscription);
                } else if (canSee.containsKey(subscription.getFolderId()) && canSee.get(subscription.getFolderId()).booleanValue()) {
                    subscriptions.add(subscription);
                } else {
                    EffectivePermission folderPermission = FOLDERS.get().getFolderPermission(Integer.parseInt(subscription.getFolderId()), userId, context.getContextId());
                    boolean visible = folderPermission.isFolderVisible();
                    canSee.put(subscription.getFolderId(), B(visible));
                    if (visible) {
                        subscriptions.add(subscription);
                    }

                }

            }
        }
        for (Subscription subscription : subscriptions) {
            subscription.setSecret(secret);
            decrypt(subscription, subscription.getSession(), subscription.getConfiguration(), PASSWORD);
            modifyOutgoing(subscription);
        }

        return subscriptions;
    }

    @Override
    public Subscription loadSubscription(Context ctx, int subscriptionId, String secret) throws OXException {
        Subscription subscription = STORAGE.get().getSubscription(ctx, subscriptionId);
        if (null == subscription) {
            return null;
        }
        subscription.setSecret(secret);
        decrypt(subscription, subscription.getSession(), subscription.getConfiguration(), PASSWORD);
        modifyOutgoing(subscription);
        return subscription;
    }

    @Override
    public void subscribe(Subscription subscription) throws OXException {
        checkCreate(subscription);
        modifyIncoming(subscription);
        if (checkForDuplicate()) {
            doCheckForDuplicate(subscription);
        }
        encrypt(subscription.getSession(), subscription.getConfiguration(), PASSWORD);
        STORAGE.get().rememberSubscription(subscription);
        modifyOutgoing(subscription);
    }

    @Override
    public void unsubscribe(Subscription subscription) throws OXException {
        Subscription loadedSubscription = loadSubscription(subscription.getContext(), subscription.getId(), null);
        if (null == loadedSubscription) {
            throw SubscriptionErrorMessage.SubscriptionNotFound.create();
        }
        if (loadedSubscription.getSession() == null) {
            // FIXME Implementation of getSession() will create a session. Thus this code will never be reached
            loadedSubscription.setSession(subscription.getSession());
        }
        checkDelete(loadedSubscription);
        STORAGE.get().forgetSubscription(subscription);
    }

    @Override
    public void update(Subscription subscription) throws OXException {
        Subscription loadedSubscription = loadSubscription(subscription.getContext(), subscription.getId(), null);
        if (null == loadedSubscription) {
            throw SubscriptionErrorMessage.SubscriptionNotFound.create();
        }
        doUpdate(subscription, loadedSubscription);
    }

    public void modifyIncoming(Subscription subscription) throws OXException {
        Object accountIDObject = subscription.getConfiguration().get("account");
        if (JSONObject.NULL == accountIDObject) {
            throw SubscriptionErrorMessage.NO_OAUTH_ACCOUNT_GIVEN.create();
        }
    }

    @SuppressWarnings("unused")
    public void modifyOutgoing(Subscription subscription) throws OXException {
        // Empty body
    }

    @Override
    public boolean knows(Context ctx, int subscriptionId) throws OXException {
        Subscription subscription = STORAGE.get().getSubscription(ctx, subscriptionId);
        if (subscription == null || subscription.getSource() == null) {
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

    @Override
    public boolean hasAccounts(Context ctx, User user) throws OXException {
        Set<String> passwordFields = getSubscriptionSource().getPasswordFields();
        if (passwordFields.isEmpty()) {
            return false;
        }
        return STORAGE.get().hasSubscriptions(ctx, user);
    }

    @Override
    public void migrateSecret(Session session, String oldSecret, String newSecret) throws OXException {
        SubscriptionSource subscriptionSource = getSubscriptionSource();
        Set<String> passwordFields = subscriptionSource.getPasswordFields();
        if (passwordFields.isEmpty()) {
            return;
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        List<Subscription> allSubscriptions = STORAGE.get().getSubscriptionsOfUser(serverSession.getContext(), session.getUserId());
        String id = subscriptionSource.getId();
        CryptoService cryptoService = CRYPTO_SERVICE.get();
        Map<String, Object> update = new HashMap<>();
        for (Subscription subscription : allSubscriptions) {
            if (id.equals(getSubscriptionSourceId(subscription))) {
                Map<String, Object> configuration = subscription.getConfiguration();
                update.clear();
                boolean save = false;
                for (String passwordField : passwordFields) {
                    String password = (String) configuration.get(passwordField);
                    if (!com.openexchange.java.Strings.isEmpty(password)) {
                        try {
                            // If we can already decrypt with the new secret, we're done with this entry
                            cryptoService.decrypt(password, newSecret);
                        } catch (OXException x) {
                            // This one needs migration
                            String transcriptedPassword = cryptoService.encrypt(cryptoService.decrypt(password, oldSecret), newSecret);
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
        SubscriptionSource subscriptionSource = getSubscriptionSource();
        Set<String> passwordFields = subscriptionSource.getPasswordFields();
        if (passwordFields.isEmpty()) {
            return;
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        List<Subscription> allSubscriptions = STORAGE.get().getSubscriptionsOfUser(serverSession.getContext(), session.getUserId());
        String id = subscriptionSource.getId();
        CryptoService cryptoService = CRYPTO_SERVICE.get();
        Map<String, Object> update = new HashMap<>();
        for (Subscription subscription : allSubscriptions) {
            if (id.equals(getSubscriptionSourceId(subscription))) {
                Map<String, Object> configuration = subscription.getConfiguration();
                update.clear();
                boolean save = false;
                for (String passwordField : passwordFields) {
                    String password = (String) configuration.get(passwordField);
                    if (!com.openexchange.java.Strings.isEmpty(password)) {
                        try {
                            // If we can already decrypt with the new secret, we're done with this entry
                            cryptoService.decrypt(password, secret);
                        } catch (OXException x) {
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
        SubscriptionSource subscriptionSource = getSubscriptionSource();
        Set<String> passwordFields = subscriptionSource.getPasswordFields();
        if (passwordFields.isEmpty()) {
            return;
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        List<Subscription> allSubscriptions = STORAGE.get().getSubscriptionsOfUser(serverSession.getContext(), session.getUserId());
        String id = subscriptionSource.getId();
        CryptoService cryptoService = CRYPTO_SERVICE.get();

        List<Subscription> subscriptionsToDelete = new ArrayList<>(allSubscriptions.size());

        for (Subscription subscription : allSubscriptions) {
            if (id.equals(getSubscriptionSourceId(subscription))) {
                Map<String, Object> configuration = subscription.getConfiguration();
                for (String passwordField : passwordFields) {
                    String password = (String) configuration.get(passwordField);
                    if (!com.openexchange.java.Strings.isEmpty(password)) {
                        try {
                            // If we can already decrypt with the new secret, we're done with this entry
                            cryptoService.decrypt(password, secret);
                        } catch (OXException e) {
                            // This one needs clean-up
                            LOGGER.trace("Unable to decrypt password. Removing subscription.", e);
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
                // FIXME Implementation of getSession() will create a session. Thus this code will never be reached
                subscription.setSession(serverSession);
            }
            unsubscribe(subscription);
        }
    }

    /**
     * Override if possible to allow pipelined processing of the subscription's content.
     *
     * @param subscription The subscription for which to load the content
     * @return The {@link SearchIterator} with the content, or an empty iterator if no content was loaded
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public SearchIterator<?> loadContent(Subscription subscription) throws OXException {
        try {
            Collection collection = getContent(subscription);
            marker.unmark(subscription);
            return null == collection ? SearchIterators.emptyIterator() : new SearchIteratorDelegator(collection);
        } catch (OXException e) {
            marker.mark(subscription);
            throw e;
        }
    }

    ///////////////////////////////////////// HELPERS /////////////////////////////////////////////

    void doUpdate(Subscription subscription, Subscription loadedSubscription) throws OXException {
        checkUpdate(loadedSubscription);
        modifyIncoming(subscription);
        encrypt(subscription.getSession(), subscription.getConfiguration(), PASSWORD);
        STORAGE.get().updateSubscription(subscription);
        modifyOutgoing(subscription);
    }

    private String getSubscriptionSourceId(Subscription subscription) {
        if (null == subscription) {
            return null;
        }
        SubscriptionSource source = subscription.getSource();
        return null == source ? null : source.getId();
    }

    protected void removeWhereConfigMatches(Context context, Map<String, Object> query) throws OXException {
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
                throw SubscriptionErrorMessage.DUPLICATE_SUBSCRIPTION.create(subscription.getSource().getId(), I(subscription.getUserId()), I(subscription.getContext().getContextId()));
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
        } catch (ClassCastException e) {
            LOGGER.trace("", e);
            return false;
        } catch (NullPointerException e) {
            LOGGER.trace("", e);
            return false;
        }

        return true;
    }

    // Permission Checks

    public void checkCreate(Subscription subscription) throws OXException {
        if (canWrite(subscription)) {
            return;
        }
        throw SubscriptionErrorMessage.PERMISSION_DENIED.create();
    }

    public void checkUpdate(Subscription subscription) throws OXException {
        Session session = subscription.getSession();
        if (null != session && session.getUserId() == subscription.getUserId() || isFolderAdmin(subscription)) {
            return;
        }
        throw SubscriptionErrorMessage.PERMISSION_DENIED.create();
    }

    public void checkDelete(Subscription subscription) throws OXException {
        Session session = subscription.getSession();
        if (null != session && session.getUserId() == subscription.getUserId() || isFolderAdmin(subscription)) {
            return;
        }
        throw SubscriptionErrorMessage.PERMISSION_DENIED.create();
    }

    private boolean canWrite(Subscription subscription) throws OXException {
        OCLPermission permission = loadFolderPermission(subscription);
        return permission.isFolderAdmin() || permission.getFolderPermission() >= OCLPermission.ADMIN_PERMISSION || (permission.getFolderPermission() >= OCLPermission.CREATE_OBJECTS_IN_FOLDER && permission.getWritePermission() >= OCLPermission.WRITE_ALL_OBJECTS);
    }

    private boolean isFolderAdmin(Subscription subscription) throws OXException {
        OCLPermission permission = loadFolderPermission(subscription);
        return permission.isFolderAdmin() || permission.getFolderPermission() >= OCLPermission.ADMIN_PERMISSION;
    }

    private OCLPermission loadFolderPermission(Subscription subscription) throws OXException {
        int folderId = Integer.parseInt(subscription.getFolderId());
        int userId = subscription.getSession().getUserId();
        Context ctx = subscription.getContext();
        UserPermissionBits userPerm = USER_PERMISSIONS.get().getUserPermissionBits(userId, ctx);

        return new OXFolderAccess(ctx).getFolderPermission(folderId, userId, userPerm);
    }

    ///////////////////////////////// ENCRYPTION //////////////////////////////

    /**
     * Encrypts the values of the specified keys
     *
     * @param session The session
     * @param map The map
     * @param keys The keys for which their values shall be encrypted
     * @throws OXException if encryption fails
     */
    void encrypt(Session session, Map<String, Object> map, String... keys) throws OXException {
        SecretEncryptionFactoryService encryptionFactoryService = ENCRYPTION_FACTORY.get();
        if (encryptionFactoryService == null) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create();
        }
        if (session == null) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create("Session is missing");
        }
        SecretEncryptionService<EncryptedField> encryptionService = encryptionFactoryService.createService(STORAGE.get());
        for (String key : keys) {
            if (false == map.containsKey(key)) {
                continue;
            }
            String toEncrypt = (String) map.get(key);
            String encrypted = encryptionService.encrypt(session, toEncrypt);
            map.put(key, encrypted);
        }
    }

    /**
     * Decrypts the values of the specified keys for the specified subscription
     *
     * @param subscription The subscription
     * @param session The session
     * @param map The map
     * @param keys The keys for which their values shall be encrypted
     * @throws OXException if service is missing
     */
    private void decrypt(Subscription subscription, Session session, Map<String, Object> map, String... keys) throws OXException {
        SecretEncryptionFactoryService encryptionFactoryService = ENCRYPTION_FACTORY.get();
        if (encryptionFactoryService == null) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create();
        }
        if (session == null) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create("Session is missing");
        }

        SecretEncryptionService<EncryptedField> encryptionService = encryptionFactoryService.createService(STORAGE.get());
        for (String key : keys) {
            if (false == map.containsKey(key)) {
                continue;
            }
            EncryptedField encryptedField = new EncryptedField(subscription, key);

            /*
             * Decrypt value or remove it on failure
             */
            String toDecrypt = (String) map.get(key);
            try {
                map.put(key, encryptionService.decrypt(session, toDecrypt, encryptedField));
            } catch (Exception e) {
                LOGGER.info("Unable to decrypt field \"{}\" in subscription.", key, e);
                map.remove(key);
            }
        }
    }
}
