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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.BufferingQueue;
import com.openexchange.pns.DefaultPushSubscription;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.pns.PushSubscription.Nature;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketListener;

/**
 * {@link WebSocketPushNotificationTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketPushNotificationTransport implements PushNotificationTransport, WebSocketListener {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketPushNotificationTransport.class);

    private static final String ID = "websocket";

    private static volatile Long delayDuration;

    private static long delayDuration(ServiceLookup services) {
        Long tmp = delayDuration;
        if (null == tmp) {
            synchronized (WebSocketPushNotificationTransport.class) {
                tmp = delayDuration;
                if (null == tmp) {
                    int defaultValue = 5000;
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
                    int defaultValue = 2000;
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

    private static final class Unsubscription {

        final int userId;
        final int contextId;
        final String client;
        private final int hash;

        Unsubscription(int userId, int contextId, String client) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            this.client = client;

            int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            result = prime * result + ((client == null) ? 0 : client.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Unsubscription)) {
                return false;
            }
            Unsubscription other = (Unsubscription) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            if (client == null) {
                if (other.client != null) {
                    return false;
                }
            } else if (!client.equals(other.client)) {
                return false;
            }
            return true;
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private final PushSubscriptionRegistry subscriptionRegistry;
    private final PushMessageGeneratorRegistry generatorRegistry;
    private final ConcurrentMap<WebSocket, String> sockets;
    private final BufferingQueue<Unsubscription> scheduledUnsubscriptions;
    private ScheduledTimerTask scheduledTimerTask; // Accessed synchronized
    private boolean stopped; // Accessed synchronized

    /**
     * Initializes a new {@link WebSocketPushNotificationTransport}.
     */
    public WebSocketPushNotificationTransport(PushSubscriptionRegistry subscriptionRegistry, PushMessageGeneratorRegistry generatorRegistry, ServiceLookup services) {
        super();
        this.generatorRegistry = generatorRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
        this.services = services;

        sockets = new ConcurrentHashMap<>(32, 0.9F, 1);
        scheduledUnsubscriptions = new BufferingQueue<>(delayDuration(services));
    }

    /**
     * Stops this Web Socket transport.
     */
    public void stop() {
        synchronized (this) {
            scheduledUnsubscriptions.clear();
            cancelTimerTask();
            stopped = true;
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public void onMessage(WebSocket socket, String text) {
        // No client-initiated push message...
    }

    @Override
    public void onWebSocketConnect(WebSocket socket) {
        String path = socket.getPath();
        if (null != path && path.startsWith("/websockets/push")) {
            String client = "open-xchange-appsuite";
            if (null == sockets.putIfAbsent(socket, client)) {
                // Subscribe...
                boolean removed = scheduledUnsubscriptions.remove(new Unsubscription(socket.getUserId(), socket.getContextId(), "open-xchange-appsuite"));
                if (removed) {
                    // Already subscribed...
                } else {
                    DefaultPushSubscription subscription = new DefaultPushSubscription.Builder()
                        .client(client)
                        .contextId(socket.getContextId())
                        .nature(Nature.VOLATILE)
                        .token("")
                        .topics(Collections.singletonList("*"))
                        .transportId(ID)
                        .userId(socket.getUserId())
                        .build();

                    try {
                        subscriptionRegistry.registerSubscription(subscription);
                    } catch (OXException e) {
                        LOG.error("Failed to add push subscription for Web Socket on path {} for client {} from user {} in context {}", path, client, socket.getUserId(), socket.getContextId(), e);
                    }
                }
            }
        }
    }

    @Override
    public void onWebSocketClose(WebSocket socket) {
        String path = socket.getPath();
        if (null != path && path.startsWith("/websockets/push")) {
            if (null != sockets.remove(socket)) {
                // Check time task is alive
                ScheduledTimerTask scheduledTimerTask = this.scheduledTimerTask;
                if (null == scheduledTimerTask) {
                    // Initialize timer task
                    TimerService timerService = services.getService(TimerService.class);
                    Runnable timerTask = new Runnable() {

                        @Override
                        public void run() {
                            checkUnsubscription();
                        }
                    };
                    long delay = timerFrequency(services); // 2sec delay
                    this.scheduledTimerTask = timerService.scheduleWithFixedDelay(timerTask, delay, delay);
                    LOG.info("Initialized new timer task for unsubscribing Web Socket push subscriptions");
                }

                String client = "open-xchange-appsuite";
                scheduledUnsubscriptions.offerOrReplace(new Unsubscription(socket.getUserId(), socket.getContextId(), client));
                LOG.info("Scheduled unsubscribing Web Socket push subscription for client {} from user {} in context", client, socket.getUserId(), socket.getContextId());
            }
        }
    }

    /**
     * Checks for an available unsubscriptions.
     */
    protected void checkUnsubscription() {
        synchronized (this) {
            // Stopped
            if (stopped) {
                return;
            }

            Unsubscription unsubscription = scheduledUnsubscriptions.poll();
            if (null != unsubscription) {
                DefaultPushSubscription subscription = new DefaultPushSubscription.Builder()
                    .client(unsubscription.client)
                    .contextId(unsubscription.contextId)
                    .nature(Nature.VOLATILE)
                    .token("")
                    .topics(Collections.singletonList("*"))
                    .transportId(ID)
                    .userId(unsubscription.userId)
                    .build();

                try {
                    subscriptionRegistry.unregisterSubscription(subscription);
                    LOG.info("Unsubscribed Web Socket push for client {} from user {} in context", unsubscription.client, unsubscription.userId, unsubscription.contextId);
                } catch (OXException e) {
                    LOG.info("Failed to unsubscribe Web Socket push for client {} from user {} in context", unsubscription.client, unsubscription.userId, unsubscription.contextId);
                }
            }

            if (scheduledUnsubscriptions.isEmpty()) {
                // No more planned rescheduling operations
                cancelTimerTask();
            }
        }
    }

    /**
     * Cancels the currently running timer task (if any).
     */
    protected void cancelTimerTask() {
        ScheduledTimerTask scheduledTimerTask = this.scheduledTimerTask;
        if (null != scheduledTimerTask) {
            scheduledTimerTask.cancel();
            this.scheduledTimerTask = null;
            LOG.info("Canceled timer task for unsubscribing Web Sockets push subscriptions");
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public boolean servesClient(String client) throws OXException {
        try {
            for (String c : sockets.values()) {
                if (client.equals(c)) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        if (null != notification && null != matches) {

        }
    }

}
