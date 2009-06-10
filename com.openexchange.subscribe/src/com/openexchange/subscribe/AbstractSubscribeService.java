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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.List;
import java.util.Map;
import com.openexchange.crypto.CryptoException;
import com.openexchange.crypto.CryptoService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link AbstractSubscribeService}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractSubscribeService implements SubscribeService {

    public static SubscriptionStorage STORAGE = null;

    public static CryptoService CRYPTO;

    public Collection<Subscription> loadSubscriptions(Context ctx, String folderId, String secret) throws AbstractOXException {
        List<Subscription> allSubscriptions = STORAGE.getSubscriptions(ctx, folderId);
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        for (Subscription subscription : allSubscriptions) {
            if(subscription.getSource() != null && getSubscriptionSource() != null && subscription.getSource().getId().equals(getSubscriptionSource().getId())) {
                subscriptions.add(subscription);
            }
        }
        for (Subscription subscription : subscriptions) {
            subscription.getConfiguration().put("com.openexchange.crypto.secret", secret);
            modifyOutgoing(subscription);
            subscription.getConfiguration().remove("com.openexchange.crypto.secret");
        }
        return subscriptions;
    }

    public Subscription loadSubscription(Context ctx, int subscriptionId, String secret) throws AbstractOXException {
        Subscription subscription = STORAGE.getSubscription(ctx, subscriptionId);
        subscription.getConfiguration().put("com.openexchange.crypto.secret", secret);
        modifyOutgoing(subscription);
        subscription.getConfiguration().remove("com.openexchange.crypto.secret");
        return subscription;
    }

    public void subscribe(Subscription subscription) throws AbstractOXException {
        String secret = (String) subscription.getConfiguration().get("com.openexchange.crypto.secret");
        modifyIncoming(subscription);
        subscription.getConfiguration().remove("com.openexchange.crypto.secret");
        STORAGE.rememberSubscription(subscription);
        subscription.getConfiguration().put("com.openexchange.crypto.secret", secret);
        modifyOutgoing(subscription);
        subscription.getConfiguration().remove("com.openexchange.crypto.secret");
    }

    public void unsubscribe(Subscription subscription) throws AbstractOXException {
        STORAGE.forgetSubscription(subscription);
    }

    public void update(Subscription subscription) throws AbstractOXException {
        String secret = (String) subscription.getConfiguration().get("com.openexchange.crypto.secret");
        modifyIncoming(subscription);
        subscription.getConfiguration().remove("com.openexchange.crypto.secret");
        STORAGE.updateSubscription(subscription);
        subscription.getConfiguration().put("com.openexchange.crypto.secret", secret);
        modifyOutgoing(subscription);
        subscription.getConfiguration().remove("com.openexchange.crypto.secret");
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

    public static void encrypt(Map<String, Object> map, String... keys) throws SubscriptionException {
        if (CRYPTO == null) {
            return;
        }
        String secret = (String) map.get("com.openexchange.crypto.secret");
        if(secret == null) {
            return;
        }
        for (String key : keys) {
            if (map.containsKey(key)) {
                String toEncrypt = (String) map.get(key);
                String encrypted;
                try {
                    encrypted = CRYPTO.encrypt(toEncrypt, secret);
                } catch (CryptoException e) {
                    throw new SubscriptionException(e);
                }
                map.put(key, encrypted);
            }
        }
        map.remove("com.openexchange.crypto.secret");
    }

    public static void decrypt(Map<String, Object> map, String... keys) throws SubscriptionException {
        if (CRYPTO == null) {
            return;
        }
        String secret = (String) map.get("com.openexchange.crypto.secret");
        if(secret == null) {
            return;
        }
        for (String key : keys) {
            if (map.containsKey(key)) {
                String toDecrypt = (String) map.get(key);
                String decrypted;
                try {
                    decrypted = CRYPTO.decrypt(toDecrypt, secret);
                } catch (CryptoException e) {
                    throw new SubscriptionException(e);
                }
                map.put(key, decrypted);
            }
        }
        map.remove("com.openexchange.crypto.secret");
    }

}
