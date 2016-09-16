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

package com.openexchange.pns.transport.websocket.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.pns.Hits;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.KnownTransport;
import com.openexchange.pns.Message;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushMessageGenerator;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.UserAndContext;
import com.openexchange.pns.transport.websocket.WebSocketClient;
import com.openexchange.pns.transport.websocket.WebSocketToClientResolver;
import com.openexchange.websockets.WebSocketService;

/**
 * {@link WebSocketPushNotificationTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketPushNotificationTransport implements PushNotificationTransport, PushSubscriptionProvider {

    /** The topic for all */
    private static final String ALL = KnownTopic.ALL.getName();

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketPushNotificationTransport.class);

    /** The identifier of the Web Socket transport */
    static final String ID = KnownTransport.WEB_SOCKET.getTransportId();

    private static final String TOKEN_PREFIX = "ws::";

    private static final String ATTR_WS_CLIENT = "ws:client";

    private static volatile Long delayDuration;

    private static long delayDuration(ServiceLookup services) {
        Long tmp = delayDuration;
        if (null == tmp) {
            synchronized (WebSocketPushNotificationTransport.class) {
                tmp = delayDuration;
                if (null == tmp) {
                    int defaultValue = 10000; // 10 seconds
                    ConfigurationService service = services.getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(service.getIntProperty("com.openexchange.pns.transport.websocket.delayDuration", defaultValue));
                    delayDuration = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Long timerFrequency;

    private static long timerFrequency(ServiceLookup services) {
        Long tmp = timerFrequency;
        if (null == tmp) {
            synchronized (WebSocketPushNotificationTransport.class) {
                tmp = timerFrequency;
                if (null == tmp) {
                    int defaultValue = 2000; // 2 seconds
                    ConfigurationService service = services.getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(service.getIntProperty("com.openexchange.pns.transport.websocket.timerFrequency", defaultValue));
                    timerFrequency = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    /**
     * Cleans statically initialized values.
     */
    public static void cleanseInits() {
        delayDuration = null;
        timerFrequency = null;
    }

    // ---------------------------------------------------------------------------------------------------------------

    private final ConfigViewFactory configViewFactory;
    private final WebSocketToClientResolverRegistry resolvers;
    private final WebSocketService webSocketService;
    private final PushMessageGeneratorRegistry generatorRegistry;

    /**
     * Initializes a new {@link WebSocketPushNotificationTransport}.
     */
    public WebSocketPushNotificationTransport(WebSocketToClientResolverRegistry resolvers, ServiceLookup services) {
        super();
        this.resolvers = resolvers;
        this.webSocketService = services.getService(WebSocketService.class);
        this.generatorRegistry = services.getService(PushMessageGeneratorRegistry.class);
        this.configViewFactory = services.getService(ConfigViewFactory.class);
    }

    /**
     * Stops this Web Socket transport.
     */
    public void stop() {
        // Nothing to do (so far)
    }

    // ---------------------------------------------------------------------------------------------------------

    /**
     * Creates the artificial Web Socket subscription token for specified arguments; e.g.
     * <pre>"ws::17-1337::open-xchange-appsuite"</pre>
     *
     * @param client The client identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The appropriate token to use
     */
    static String createTokenFor(String client, int userId, int contextId) {
        return new StringBuilder(24).append(TOKEN_PREFIX).append(userId).append('-').append(contextId).append("::").append(client).toString();
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public boolean hasInterestedSubscriptions(int userId, int contextId, String topic) throws OXException {
        // Remember checked clients
        Map<WebSocketClient, Boolean> checkedOnes = new LinkedHashMap<>();

        // Check resolvers
        for (WebSocketToClientResolver resolver : resolvers) {
            Map<String, WebSocketClient> clients = resolver.getSupportedClients();
            for (WebSocketClient client : clients.values()) {
                Boolean exists = checkedOnes.get(client);
                if (null == exists) {
                    String pathFilter = client.getPathFilter();
                    try {
                        exists = Boolean.valueOf(webSocketService.exists(pathFilter, userId, contextId));
                    } catch (OXException e) {
                        LOG.error("Failed to check for any open filter-satisfying Web Socket using filter \"{}\" for user {} in context {}. Assuming there is any...", null == pathFilter ? "<none>" : pathFilter, I(userId), I(contextId), e);
                        exists = Boolean.TRUE;
                    }
                    checkedOnes.put(client, exists);
                }
                if (exists.booleanValue()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasInterestedSubscriptions(String client, int userId, int contextId, String topic) throws OXException {
        // Check resolvers
        for (WebSocketToClientResolver resolver : resolvers) {
            Map<String, WebSocketClient> clients = resolver.getSupportedClients();
            WebSocketClient wsClient = clients.get(client);
            if (null != wsClient) {
                String pathFilter = wsClient.getPathFilter();
                Boolean exists;
                try {
                    exists = Boolean.valueOf(webSocketService.exists(pathFilter, userId, contextId));
                } catch (OXException e) {
                    LOG.error("Failed to check for any open filter-satisfying Web Socket using filter \"{}\" for user {} in context {}. Assuming there is any...", null == pathFilter ? "<none>" : pathFilter, I(userId), I(contextId), e);
                    exists = Boolean.TRUE;
                }
                if (exists.booleanValue()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Hits getInterestedSubscriptions(final int userId, final int contextId, String topic) throws OXException {
        // Remember checked clients
        Map<WebSocketClient, Boolean> checkedOnes = new LinkedHashMap<>();
        Set<WebSocketClient> hasOpenWebSocket = new LinkedHashSet<>();
        boolean anyOpen = false;

        // Check resolvers
        for (WebSocketToClientResolver resolver : resolvers) {
            Map<String, WebSocketClient> clients = resolver.getSupportedClients();
            for (WebSocketClient client : clients.values()) {
                Boolean exists = checkedOnes.get(client);
                if (null == exists) {
                    String pathFilter = client.getPathFilter();
                    try {
                        exists = Boolean.valueOf(webSocketService.exists(pathFilter, userId, contextId));
                    } catch (OXException e) {
                        LOG.error("Failed to check for any open filter-satisfying Web Socket using filter \"{}\" for user {} in context {}. Assuming there is any...", null == pathFilter ? "<none>" : pathFilter, I(userId), I(contextId), e);
                        exists = Boolean.TRUE;
                    }
                    checkedOnes.put(client, exists);
                }
                if (exists.booleanValue()) {
                    hasOpenWebSocket.add(client);
                    anyOpen = true;
                }
            }
        }

        if (false == anyOpen) {
            return Hits.EMPTY_HITS;
        }

        // Advertise subscription for each client that has an open Web Socket
        return new WebSocketHits(hasOpenWebSocket, userId, contextId);
    }

    @Override
    public Hits getInterestedSubscriptions(String client, int userId, int contextId, String topic) throws OXException {
        // Check resolvers
        for (WebSocketToClientResolver resolver : resolvers) {
            Map<String, WebSocketClient> clients = resolver.getSupportedClients();
            WebSocketClient wsClient = clients.get(client);
            if (null != wsClient) {
                String pathFilter = wsClient.getPathFilter();
                Boolean exists;
                try {
                    exists = Boolean.valueOf(webSocketService.exists(pathFilter, userId, contextId));
                } catch (OXException e) {
                    LOG.error("Failed to check for any open filter-satisfying Web Socket using filter \"{}\" for user {} in context {}. Assuming there is any...", null == pathFilter ? "<none>" : pathFilter, I(userId), I(contextId), e);
                    exists = Boolean.TRUE;
                }
                if (exists.booleanValue()) {
                    return new WebSocketHits(Collections.singleton(wsClient), userId, contextId);
                }
            }
        }

        return Hits.EMPTY_HITS;
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public boolean servesClient(String client) throws OXException {
        return resolvers.getAllSupportedClients().containsKey(client);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isEnabled(String topic, String client, int userId, int contextId) throws OXException {
        ConfigView view = configViewFactory.getView(userId, contextId);

        String basePropertyName = "com.openexchange.pns.transport.websocket.enabled";

        ComposedConfigProperty<Boolean> property;
        property = null == topic || null == client ? null : view.property(basePropertyName + "." + client + "." + topic, boolean.class);
        if (null != property && property.isDefined()) {
            return property.get().booleanValue();
        }

        property = null == client ? null : view.property(basePropertyName + "." + client, boolean.class);
        if (null != property && property.isDefined()) {
            return property.get().booleanValue();
        }

        property = view.property(basePropertyName, boolean.class);
        if (null != property && property.isDefined()) {
            return property.get().booleanValue();
        }

        return false;
    }

    @Override
    public void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        if (null != notification && null != matches && !matches.isEmpty()) {
            // Determine associated client and path filter
            Map<String, ClientAndPathFilter> clientToFilter = getResolveResultsFor(matches);
            if (null == clientToFilter) {
                // No appropriate client/path-filter associated with specified matches
                return;
            }

            // Sort incoming matches by user-context pairs
            for (Map.Entry<UserAndContext, List<PushMatch>> userAssociatedMatches : sortByUser(matches).entrySet()) {
                UserAndContext uac = userAssociatedMatches.getKey();
                for (String client : extractClientsFrom(userAssociatedMatches.getValue())) {
                    ClientAndPathFilter clientAndPathFilter = clientToFilter.get(client);
                    if (null != clientAndPathFilter) {
                        transport(notification, clientAndPathFilter, uac);
                    }
                }
            }
        }
    }

    private void transport(PushNotification notification, ClientAndPathFilter clientAndPathFilter, UserAndContext uac) throws OXException {
        String client = clientAndPathFilter.getClient();
        PushMessageGenerator generator = generatorRegistry.getGenerator(client);
        if (null == generator) {
            throw PushExceptionCodes.NO_SUCH_GENERATOR.create(client);
        }

        Message<?> message = generator.generateMessageFor(ID, notification);

        // Get & send message's textual representation
        String textMessage = message.getMessage().toString();
        int userId = uac.getUserId();
        int contextId = uac.getContextId();
        webSocketService.sendMessage(textMessage, clientAndPathFilter.getPathFilter(), userId, contextId);
    }

    private Map<String, ClientAndPathFilter> getResolveResultsFor(Collection<PushMatch> matches) throws OXException {
        // Only care about clients for Web Sockets
        Set<String> clients = extractClientsFrom(matches);

        Map<String, ClientAndPathFilter> applicable = null;
        for (String client : clients) {
            for (WebSocketToClientResolver resolver : resolvers) {
                String pathFilter = resolver.getPathFilterFor(client);
                if (pathFilter == null) {
                    // Client unknown
                    LOG.warn("Client \"{}\" is unknown, hence cannot resolve it to an appropriate path filter expression.");
                } else {
                    if (null == applicable) {
                        applicable = new HashMap<>(clients.size());
                    }
                    applicable.put(client, new ClientAndPathFilter(client, pathFilter));
                }
            }
        }
        return applicable;
    }

    private Set<String> extractClientsFrom(Collection<PushMatch> userMatches) {
        if (1 == userMatches.size()) {
            return Collections.singleton(userMatches.iterator().next().getClient());
        }

        Set<String> clients = new HashSet<>();
        for (PushMatch userMatch : userMatches) {
            clients.add(userMatch.getClient());
        }
        return clients;
    }

    private Map<UserAndContext, List<PushMatch>> sortByUser(Collection<PushMatch> matches) {
        if (1 == matches.size()) {
            PushMatch match = matches.iterator().next();
            return Collections.singletonMap(UserAndContext.newInstance(match.getUserId(), match.getContextId()), Collections.singletonList(match));
        }

        Map<UserAndContext, List<PushMatch>> byUser = new LinkedHashMap<>();

        // Only one match is needed as there is no difference per match
        for (PushMatch match : matches) {
            UserAndContext uac = UserAndContext.newInstance(match.getUserId(), match.getContextId());
            List<PushMatch> matchList = byUser.get(uac);
            if (null == matchList) {
                matchList = new LinkedList<>();
                byUser.put(uac, matchList);
            }
            matchList.add(match);
        }

        return byUser;
    }

}
