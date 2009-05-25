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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link InMemorySubscriptionStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InMemorySubscriptionStorage implements SubscriptionStorage {
    private Map<Integer, Map<Integer, Subscription>> subscriptions = new HashMap<Integer, Map<Integer, Subscription>>();
    private Map<Integer, Integer> keys = new HashMap<Integer, Integer>();
    
    /* (non-Javadoc)
     * @see com.openexchange.subscribe.SubscriptionStorage#rememberSubscription(int, com.openexchange.subscribe.Subscription)
     */
    public void rememberSubscription(Subscription subscription) {
        int cid = subscription.getContext().getContextId();
        if(-1 == subscription.getId()) {
            subscription.setId(nextId(cid));
        }
        Subscription current = subscriptions(cid).get(subscription.getId());
        if(current == null) {
            subscriptions(cid).put(subscription.getId(), subscription);
        } else {
            if (subscription.getFolderId() != -1) {
                current.setFolderId(subscription.getFolderId());
            }
            
            if(subscription.getConfiguration() != null && (subscription.getSource() == null || subscription.getSource().equals(current.getSource()))) {
                current.getConfiguration().putAll(subscription.getConfiguration());
                for (Entry<String, Object> entry : new HashSet<Entry<String, Object>>(current.getConfiguration().entrySet())) {
                    if(entry.getValue() == null) {
                        current.getConfiguration().remove(entry.getKey());
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.subscribe.SubscriptionStorage#forgetSubscription(int, com.openexchange.subscribe.Subscription)
     */
    public void forgetSubscription(Subscription subscription) {
        subscriptions(subscription.getContext().getContextId()).remove(subscription.getId());
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.subscribe.SubscriptionStorage#getSubscriptions(int, int)
     */
    public List<Subscription> getSubscriptions(Context ctx, int folderId) {
        List<Subscription> found = new LinkedList<Subscription>();
        for(Subscription subscription : subscriptions(ctx.getContextId()).values()) {
            if(subscription.getFolderId() == folderId) {
                found.add(subscription);
            }
        }
        return found;
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.subscribe.SubscriptionStorage#getSubscription(int, int)
     */
    public Subscription getSubscription(Context ctx, int id) {
        return subscriptions(ctx.getContextId()).get(id);
    }
    
    private int nextId(int cid) {
        if(keys.containsKey(cid)) {
            int next = keys.get(cid);
            keys.put(cid, next+1);
            return next;
        } else {
            keys.put(cid,2);
            return 1;
        }
    }
    
    private Map<Integer, Subscription> subscriptions(int cid) {
        if(!subscriptions.containsKey(cid)) {
            subscriptions.put(cid, new HashMap<Integer, Subscription>());
        }
        return subscriptions.get(cid);
    }

    public void updateSubscription(Subscription subscription) throws SubscriptionException {
        // TODO Auto-generated method stub
        
    }
    
}
