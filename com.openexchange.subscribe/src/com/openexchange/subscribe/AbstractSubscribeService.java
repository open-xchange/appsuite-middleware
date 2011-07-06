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

package com.openexchange.subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.crypto.CryptoService;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.impl.EffectivePermission;

/**
 * {@link AbstractSubscribeService}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractSubscribeService implements SubscribeService {

    public static SubscriptionStorage STORAGE = null;

    public static CryptoService CRYPTO;

    public static FolderService FOLDERS;

    public Collection<Subscription> loadSubscriptions(Context ctx, String folderId, String secret) throws AbstractOXException {
        List<Subscription> allSubscriptions = STORAGE.getSubscriptions(ctx, folderId);
        return prepareSubscriptions(allSubscriptions, secret, ctx, -1);
    }

    public Collection<Subscription> loadSubscriptions(Context context, int userId, String secret) throws AbstractOXException {
        List<Subscription> allSubscriptions = STORAGE.getSubscriptionsOfUser(context, userId);
        return prepareSubscriptions(allSubscriptions, secret, context, userId);
    }

    private Collection<Subscription> prepareSubscriptions(List<Subscription> allSubscriptions, String secret, Context context, int userId) throws AbstractOXException {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        Map<String, Boolean> canSee = new HashMap<String, Boolean>();
        
        for (Subscription subscription : allSubscriptions) {
            if (subscription.getSource() != null && getSubscriptionSource() != null && subscription.getSource().getId().equals(
                getSubscriptionSource().getId())) {
                
                if(userId == -1) {
                    subscriptions.add(subscription);
                } else if (canSee.containsKey(subscription.getFolderId()) && canSee.get(subscription.getFolderId())) {
                    subscriptions.add(subscription);
                } else {
                    EffectivePermission folderPermission = FOLDERS.getFolderPermission(Integer.parseInt(subscription.getFolderId()), userId, context.getContextId());
                    boolean visible = folderPermission.isFolderVisible() ;
                    canSee.put(subscription.getFolderId(), visible);
                    if(visible) {
                        subscriptions.add(subscription);
                    }
                    
                }
                
            }
        }
        for (Subscription subscription : subscriptions) {
            subscription.setSecret(secret);
            modifyOutgoing(subscription);
        }

        return subscriptions;
    }


    public Subscription loadSubscription(Context ctx, int subscriptionId, String secret) throws AbstractOXException {
        Subscription subscription = STORAGE.getSubscription(ctx, subscriptionId);
        subscription.setSecret(secret);
        modifyOutgoing(subscription);
        return subscription;
    }

    public void subscribe(Subscription subscription) throws AbstractOXException {
        modifyIncoming(subscription);
        STORAGE.rememberSubscription(subscription);
        modifyOutgoing(subscription);
    }

    public void unsubscribe(Subscription subscription) throws AbstractOXException {
        STORAGE.forgetSubscription(subscription);
    }

    public void update(Subscription subscription) throws AbstractOXException {
        modifyIncoming(subscription);
        STORAGE.updateSubscription(subscription);
        modifyOutgoing(subscription);
    }

    public void modifyIncoming(Subscription subscription) throws SubscriptionException {

    }

    public void modifyOutgoing(Subscription subscription) throws SubscriptionException {

    }

    public boolean knows(Context ctx, int subscriptionId) throws AbstractOXException {
        Subscription subscription = STORAGE.getSubscription(ctx, subscriptionId);
        if (subscription == null) {
            return false;
        }
        if (subscription.getSource().getId().equals(getSubscriptionSource().getId())) {
            return true;
        }
        return false;
    }

    public static void encrypt(String secret, Map<String, Object> map, String... keys) throws SubscriptionException {
        if (CRYPTO == null) {
            return;
        }
        if (secret == null) {
            return;
        }
        for (String key : keys) {
            if (map.containsKey(key)) {
                String toEncrypt = (String) map.get(key);
                String encrypted;
                try {
                    encrypted = CRYPTO.encrypt(toEncrypt, secret);
                } catch (OXException e) {
                    throw new SubscriptionException(e);
                }
                map.put(key, encrypted);
            }
        }
    }

    public static void decrypt(String secret, Map<String, Object> map, String... keys) throws SubscriptionException {
        if (CRYPTO == null) {
            return;
        }
        if (secret == null) {
            return;
        }
        for (String key : keys) {
            if (map.containsKey(key)) {
                String toDecrypt = (String) map.get(key);
                String decrypted;
                try {
                    decrypted = CRYPTO.decrypt(toDecrypt, secret);
                } catch (OXException e) {
                    // Fail silently
                    decrypted = null;
                }
                map.put(key, decrypted);
            }
        }
    }

    public String checkSecretCanDecryptPasswords(Context context, User user, String secret) throws SubscriptionException {
        Set<String> passwordFields = getSubscriptionSource().getPasswordFields();
        if (passwordFields.isEmpty()) {
            return null;
        }
        List<Subscription> allSubscriptions = STORAGE.getSubscriptionsOfUser(context, user.getId());
        
        for (Subscription subscription : allSubscriptions) {
            try {
                Map<String, Object> configuration = subscription.getConfiguration();
                for (String passwordField : passwordFields) {
                    String password = (String) configuration.get(passwordField);
                    if (password != null) {
                        CRYPTO.decrypt(password, secret);
                    }
                }
            } catch (OXException x) {
                return "Could not decode subscription passwords for subscription: "+subscription.getId()+" : "+subscription.getDisplayName();
            }
        }
        return null;
    }

    public void migrateSecret(Context context, User user, String oldSecret, String newSecret) throws SubscriptionException {
        Set<String> passwordFields = getSubscriptionSource().getPasswordFields();
        if (passwordFields.isEmpty()) {
            return;
        }
        List<Subscription> allSubscriptions = STORAGE.getSubscriptionsOfUser(context, user.getId());
        for (Subscription subscription : allSubscriptions) {
            if (subscription.getSource().getId().equals(getSubscriptionSource().getId())) {
                Map<String, Object> configuration = subscription.getConfiguration();
                Map<String, Object> update = new HashMap<String, Object>();
                boolean save = false;
                for (String passwordField : passwordFields) {
                    String password = (String) configuration.get(passwordField);
                    if (password != null) {
                        try {
                            try {
                                // If we can already decrypt with the new secret, we're done with this entry
                                CRYPTO.decrypt(password, newSecret);
                            } catch (OXException x) {
                                // Alright, this one needs migration
                                String transcriptedPassword = CRYPTO.encrypt(CRYPTO.decrypt(password, oldSecret), newSecret);
                                update.put(passwordField, transcriptedPassword);
                                save = true;
                            }
                        } catch (OXException e) {
                            throw new SubscriptionException(e);
                        }
                    }
                }
                if(save) {
                    subscription.setConfiguration(update);
                    STORAGE.updateSubscription(subscription);
                }
            }
        }
    }
    
    
    protected void removeWhereConfigMatches(Context context, Map<String, Object> query) throws SubscriptionException {
        STORAGE.deleteAllSubscriptionsWhereConfigMatches(query, getSubscriptionSource().getId(), context);
    }
}
