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

package com.openexchange.pns.subscription.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.exception.OXException;
import com.openexchange.pns.Hits;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.PushSubscription.Nature;


/**
 * {@link CompositePushSubscriptionRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CompositePushSubscriptionRegistry implements PushSubscriptionRegistry {

    private final List<PushSubscriptionRegistry> registries;
    private final PushSubscriptionRegistry persistentRegistry;
    private final PushSubscriptionRegistry volatileRegistry;

    /**
     * Initializes a new {@link CompositePushSubscriptionRegistry}.
     */
    public CompositePushSubscriptionRegistry(PushSubscriptionRegistry persistentRegistry, PushSubscriptionRegistry volatileRegistry) {
        super();
        this.persistentRegistry = persistentRegistry;
        this.volatileRegistry = volatileRegistry;
        this.registries = new CopyOnWriteArrayList<PushSubscriptionRegistry>(Arrays.<PushSubscriptionRegistry> asList(persistentRegistry, volatileRegistry));
    }

    @Override
    public Hits getInterestedSubscriptions(int userId, int contextId, String topic) throws OXException {
        Map<ClientAndTransport, List<PushMatch>> map = null;

        for (PushSubscriptionRegistry registry : registries) {
            Hits currentHits = registry.getInterestedSubscriptions(userId, contextId, topic);
            if (false == currentHits.isEmpty()) {
                Map<ClientAndTransport, List<PushMatch>> currentMap = ((MapBackedHits) currentHits).getMap();

                // Yet initialized?
                if (null == map) {
                    map = new LinkedHashMap<>();
                }

                // Add hits
                for (Map.Entry<ClientAndTransport, List<PushMatch>> entry : currentMap.entrySet()) {
                    List<PushMatch> list = map.get(entry.getKey());
                    if (null == list) {
                        list = new LinkedList<>();
                        map.put(entry.getKey(), list);
                    }
                    list.addAll(entry.getValue());
                }
            }
        }

        return new MapBackedHits(null == map ? Collections.<ClientAndTransport, List<PushMatch>> emptyMap() : map);
    }

    @Override
    public void registerSubscription(PushSubscription subscription) throws OXException {
        if (Nature.PERSISTENT == subscription.getNature()) {
            persistentRegistry.registerSubscription(subscription);
        } else {
            volatileRegistry.registerSubscription(subscription);
        }
    }

    @Override
    public boolean unregisterSubscription(PushSubscription subscription) throws OXException {
        // Is nature given?
        Nature nature = subscription.getNature();

        if (Nature.VOLATILE == nature) {
            return volatileRegistry.unregisterSubscription(subscription);
        } else if (Nature.PERSISTENT == nature) {
            return persistentRegistry.unregisterSubscription(subscription);
        } else {
            // Don't know better
            boolean removed = persistentRegistry.unregisterSubscription(subscription);
            removed |= volatileRegistry.unregisterSubscription(subscription);
            return removed;
        }
    }

    @Override
    public int unregisterSubscription(String token, String transportId) throws OXException {
        int numRemoved = persistentRegistry.unregisterSubscription(token, transportId);
        numRemoved += volatileRegistry.unregisterSubscription(token, transportId);
        return numRemoved;
    }

    @Override
    public boolean updateToken(PushSubscription subscription, String newToken) throws OXException {
        boolean updated = persistentRegistry.updateToken(subscription, newToken);
        updated |= volatileRegistry.updateToken(subscription, newToken);
        return updated;
    }

}
