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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.transport.websocket.osgi;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.pns.transport.websocket.WebSocketToClientResolver;
import com.openexchange.pns.transport.websocket.internal.WebSocketToClientResolverRegistry;

/**
 * {@link WebSocketToClientResolverTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketToClientResolverTracker extends RankingAwareNearRegistryServiceTracker<WebSocketToClientResolver> implements WebSocketToClientResolverRegistry {

    private final ConcurrentMap<String, Boolean> supportedClients;

    /**
     * Initializes a new {@link WebSocketToClientResolverTracker}.
     */
    public WebSocketToClientResolverTracker(BundleContext context) {
        super(context, WebSocketToClientResolver.class, 0);
        supportedClients = new ConcurrentHashMap<>(16, 0.9F, 1);
    }

    @Override
    protected boolean onServiceAppeared(WebSocketToClientResolver resolver) {
        List<String> toRemove = new LinkedList<>();
        boolean invalid = true;
        try {
            Set<String> clients = resolver.getSupportedClients();
            for (String clientToAdd : clients) {
                if (null != supportedClients.putIfAbsent(clientToAdd, Boolean.TRUE)) {
                    // There is already such a client...
                    return false;
                }
            }
            invalid = false;
            return true;
        } finally {
            if (invalid) {
                for (String clientToRemove : toRemove) {
                    supportedClients.remove(clientToRemove);
                }
            }
        }
    }

    @Override
    protected void onServiceRemoved(WebSocketToClientResolver resolver) {
        Set<String> clients = resolver.getSupportedClients();
        for (String clientToRemove : clients) {
            supportedClients.remove(clientToRemove);
        }
    }

    @Override
    public Set<String> getAllSupportedClients() {
        return Collections.unmodifiableSet(supportedClients.keySet());
    }

    @Override
    public boolean containsClient(String client) {
        return null != client && supportedClients.containsKey(client);
    }

}
