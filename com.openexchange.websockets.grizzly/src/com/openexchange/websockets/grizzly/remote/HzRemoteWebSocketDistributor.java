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

package com.openexchange.websockets.grizzly.remote;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.hazelcast.nio.Address;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.UnsynchronizedBufferingQueue;
import com.openexchange.session.UserAndContext;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketExceptionCodes;
import com.openexchange.websockets.WebSocketInfo;
import com.openexchange.websockets.WebSockets;
import com.openexchange.websockets.grizzly.GrizzlyWebSocketApplication;
import com.openexchange.websockets.grizzly.remote.portable.PortableMessageDistributor;

/**
 * {@link HzRemoteWebSocketDistributor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class HzRemoteWebSocketDistributor implements RemoteWebSocketDistributor {

    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HzRemoteWebSocketDistributor.class);
    private static final Logger WS_LOGGER = org.slf4j.LoggerFactory.getLogger("WEBSOCKET");

    private static int delayDuration(ConfigurationService configService) {
        int defaultValue = 1000;
        if (null == configService) {
            return defaultValue;
        }
        return configService.getIntProperty("com.openexchange.websockets.grizzly.remote.delayDuration", defaultValue);
    }

    private static int maxDelayDuration(ConfigurationService configService) {
        int defaultValue = 3000;
        if (null == configService) {
            return defaultValue;
        }
        return configService.getIntProperty("com.openexchange.websockets.grizzly.remote.maxDelayDuration", defaultValue);
    }

    private static int timerFrequency(ConfigurationService configService) {
        int defaultValue = 500;
        if (null == configService) {
            return defaultValue;
        }
        return configService.getIntProperty("com.openexchange.websockets.grizzly.remote.timerFrequency", defaultValue);
    }

    // -------------------------------------------------------------------------------------------------------------

    private final Lock lock;
    private final TimerService timerService;
    private final ConfigurationService configService;
    final SetMultimap<String, String> myValues;
    private final UnsynchronizedBufferingQueue<RemoteMessage> remoteMsgs;
    private ScheduledTimerTask timerTask;

    volatile HazelcastInstance hzInstance;
    volatile String mapName;
    volatile String entryListenerRegistrationId;

    private final ConcurrentMap<UserAndContext, ScheduledTimerTask> cleanerTasks;

    /**
     * Initializes a new {@link HzRemoteWebSocketDistributor}.
     */
    public HzRemoteWebSocketDistributor(TimerService timerService, ConfigurationService configService) {
        super();
        this.timerService = timerService;
        this.configService = configService;
        myValues = SetMultimapBuilder.hashKeys().hashSetValues(4).build();
        lock = new ReentrantLock();
        remoteMsgs = new UnsynchronizedBufferingQueue<RemoteMessage>(delayDuration(configService), maxDelayDuration(configService)) {

            @Override
            protected UnsynchronizedBufferingQueue.BufferedElement<RemoteMessage> transfer(RemoteMessage toOffer, UnsynchronizedBufferingQueue.BufferedElement<RemoteMessage> existing) {
                toOffer.mergeWith(existing.getElement());
                return new UnsynchronizedBufferingQueue.BufferedElement<RemoteMessage>(toOffer, existing);
            }
        };
        cleanerTasks = new ConcurrentHashMap<>(512, 0.9F, 16);
    }

    /**
     * Shuts-down this remote distributor.
     */
    public void shutDown() {
        unsetHazelcastResources();
        lock.lock();
        try {
            cancelTimerTask();
        } finally {
            lock.unlock();
        }

        for (Map.Entry<UserAndContext, ScheduledTimerTask> entry : cleanerTasks.entrySet()) {
            entry.getValue().cancel();
        }
        cleanerTasks.clear();
        timerService.purge();
    }

    @Override
    public long getNumberOfBufferedMessages() {
        lock.lock();
        try {
            return remoteMsgs.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<WebSocketInfo> listClusterWebSocketInfo() {
        try {
            HazelcastInstance hzInstance = this.hzInstance;
            if (null == hzInstance) {
                LOGGER.warn("Missing Hazelcast instance. Failed to check for cluster Web Sockets' information");
                return null;
            }

            String mapName = this.mapName;
            if (null == mapName) {
                LOGGER.warn("Missing Hazelcast map name. Failed to check for cluster Web Sockets' information");
                return null;
            }

            // Get the Hazelcast map reference
            MultiMap<String, String> hzMap = map(mapName, hzInstance);

            if (null == hzMap) {
                LOGGER.warn("Missing Hazelcast map (Hazelcast inactive?). Failed to check for cluster Web Sockets' information");
                return null;
            }

            // Iterate its entry set
            Set<Entry<String, String>> entrySet = hzMap.entrySet();
            List<WebSocketInfo> infos = new ArrayList<>(entrySet.size());
            for (Map.Entry<String, String> mapEntry : entrySet) {
                MapKey key = parseKey(mapEntry.getKey());
                MapValue value = parseValue(mapEntry.getValue());

                WebSocketInfo info = WebSocketInfo.builder()
                    .connectionId(ConnectionId.newInstance(value.getConnectionId()))
                    .contextId(key.getContextId())
                    .address(key.getAddress())
                    .path(value.getPath())
                    .userId(key.getUserId())
                    .build();
                infos.add(info);
            }

            return infos;
        } catch (Exception e) {
            LOGGER.warn("Failed to check for cluster Web Sockets' information", e);
            return null;
        }
    }

    /**
     * Sets the Hazelcast resources to use.
     *
     * @param hzInstance The Hazelcast instance
     * @param mapName The name of the associated Hazelcast map
     */
    public void setHazelcastResources(HazelcastInstance hzInstance, String mapName) {
        this.hzInstance = hzInstance;
        this.mapName = mapName;

        GrizzlyWebSocketApplication app = GrizzlyWebSocketApplication.getGrizzlyWebSocketApplication();
        if (null == app) {
            LOGGER.warn("Entry listener cloud not be applied", new Throwable("Missing Grizzly Web Application"));
            return;
        }

        try {
            // Get the Hazelcast map reference & add listener
            MultiMap<String, String> hzMap = map(mapName, hzInstance);
            if (null == hzMap) {
                LOGGER.warn("Missing Hazelcast map (Hazelcast inactive?). Failed to add entry listener");
                return;
            }

            WebSocketClosingEntryListener entryListener = new WebSocketClosingEntryListener(app);
            String entryListenerRegistrationId = hzMap.addEntryListener(entryListener, true);
            this.entryListenerRegistrationId = entryListenerRegistrationId;
            LOGGER.info("Successfully added entry listener with registration ID \"{}\"", entryListenerRegistrationId);
        } catch (Exception e) {
            LOGGER.error("Entry listener could not be applied", e);
        }
    }

    /**
     * Unsets the previously set Hazelcast resources (if any).
     */
    public void unsetHazelcastResources() {
        HazelcastInstance hzInstance = this.hzInstance;
        if (null == hzInstance) {
            return;
        }

        String mapName = this.mapName;
        if (null == mapName) {
            return;
        }

        // Get the Hazelcast map reference
        MultiMap<String, String> hzMap;
        try {
            hzMap = map(mapName, hzInstance);
            if (null == hzMap) {
                return;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to acquire Hazelcast map", e);
            return;
        }

        synchronized (myValues) {
            if (!myValues.isEmpty()) {
                try {
                    for (Entry<String, String> entry : myValues.entries()) {
                        hzMap.remove(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to remove keys from Hazelcast map.", e);
                }
                myValues.clear();
            }
        }

        String entryListenerRegistrationId = this.entryListenerRegistrationId;
        if (null != entryListenerRegistrationId) {
            this.entryListenerRegistrationId = null;
            try {
                hzMap.removeEntryListener(entryListenerRegistrationId);
                LOGGER.info("Successfully removed entry listener with registration ID \"{}\"", entryListenerRegistrationId);
            } catch (Exception e) {
                LOGGER.error("Entry listener could not be removed", e);
            }
        }

        this.hzInstance = null;
        this.mapName = null;
    }

    /**
     * Gets the reference form Hazelcast Multi-Map with the associations:<br>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in;">
     *   &lt;userId&gt; + <code>"@"</code> + &lt;contextId&gt; + <code>":"</code> + &lt;memberUuid&gt; --&gt; &lt;connectionId-path-pair&gt;*
     * </div>
     * Examples:
     * <pre>
     *   "17@1337:45e4d611-7005-47c8-8a7d-190a59c38faf" --&gt; ["cbb322...:/socket.io", "f8fa78...:/websockets"]
     *    "9@1337:45e4d611-7005-47c8-8a7d-190a59c38faf" --&gt; ["e2b3bb...:/socket.io"]
     * </pre>
     *
     * @param mapName The name of the Hazelcast multi-map
     * @param hzInstance The Hazlectas instance to use
     * @return The Hazelcast multi-map
     * @throws OXException If Hazelcast multi-map cannot be returned
     */
    MultiMap<String, String> map(String mapName, HazelcastInstance hzInstance) throws OXException {
        try {
            return hzInstance.getMultiMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            // Obviously Hazelcast is absent
            LOGGER.warn("HazelcastInstance not active", e);
            return null;
        } catch (HazelcastException e) {
            throw WebSocketExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw WebSocketExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    String generateKey(int userId, int contextId, String host, int port) {
        return new StringBuilder(48).append(userId).append('@').append(contextId).append('_').append(host).append(':').append(port).toString();
    }

    private MapKey parseKey(String key) {
        try {
            return MapKey.parseFrom(key);
        } catch (Exception e) {
            LOGGER.warn("Invalid key: {}", key);
            return null;
        }
    }

    String generateValue(String connectionId, String path) {
        if (null == path) {
            return new StringBuilder(34).append(connectionId).append(':').toString();
        }

        // With path info
        return new StringBuilder(48).append(connectionId).append(':').append(path).toString();
    }

    private MapValue parseValue(String socketInfo) {
        try {
            return MapValue.parseFrom(socketInfo);
        } catch (Exception e) {
            LOGGER.warn("Invalid socket info: {}", socketInfo);
            return null;
        }
    }

    @Override
    public boolean existsAnyRemote(String pathFilter, int userId, int contextId) {
        try {
            HazelcastInstance hzInstance = this.hzInstance;
            if (null == hzInstance) {
                LOGGER.warn("Missing Hazelcast instance. Failed to check for open remote Web Sockets for user {} in context {}", I(userId), I(contextId));
                return false;
            }

            String mapName = this.mapName;
            if (null == mapName) {
                LOGGER.warn("Missing Hazelcast map name. Failed to check for open remote Web Sockets for user {} in context {}", I(userId), I(contextId));
                return false;
            }

            // Get the Hazelcast map reference
            MultiMap<String, String> hzMap = map(mapName, hzInstance);

            if (null == hzMap) {
                LOGGER.warn("Missing Hazelcast map (Hazelcast inactive?). Failed to check for open remote Web Sockets for user {} in context {}", I(userId), I(contextId));
                return false;
            }

            // Get local member
            Cluster cluster = hzInstance.getCluster();
            Member localMember = cluster.getLocalMember();

            // Determine other cluster members
            Set<Member> otherMembers = getOtherMembers(cluster.getMembers(), localMember);

            // Iterate other members & check for a matching open Web Socket
            for (Iterator<Member> it = otherMembers.iterator(); it.hasNext();) {
                Member member = it.next();
                Address address = member.getAddress();
                Collection<String> infos = hzMap.get(generateKey(userId, contextId, address.getHost(), address.getPort()));
                if (null != infos && !infos.isEmpty()) {
                    if (null == pathFilter) {
                        // Well, either one is enough then...
                        return true;
                    }

                    // Check if filter matches any
                    for (String info : infos) {
                        if (WebSockets.matches(pathFilter, MapValue.parsePathFrom(info))) {
                            return true;
                        }
                    }
                }
            }

            // Apparently no Web Socket open
            WS_LOGGER.debug("Found no connected Web Sockets on remote cluster members using path filter {} for user {} in context {}", null == pathFilter ? "<none>" : pathFilter, I(userId), I(contextId));
            return false;
        } catch (Exception e) {
            LOGGER.warn("Failed to remotely check any open Web Socket using path filter {} for user {} in context {}", null == pathFilter ? "<none>" : pathFilter, I(userId), I(contextId), e);
        }
        return false;
    }

    /**
     * Cancels the timer task (if present).
     * <p>
     * May only be accessed when holding lock.
     */
    protected void cancelTimerTask() {
        if (null != timerTask) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    @Override
    public void sendRemote(String message, String pathFilter, int userId, int contextId, boolean async) {
        lock.lock();
        try {
            remoteMsgs.offerOrReplaceAndReset(new RemoteMessage(message, pathFilter, userId, contextId, async));

            if (null == timerTask) {
                // Timer task
                final org.slf4j.Logger log = LOGGER;
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            triggerDistribution();
                        } catch (final Exception e) {
                            log.warn("Failed to trigger publishing notifications.", e);
                        }
                    }
                };
                final int delay = timerFrequency(configService);
                timerTask = timerService.scheduleWithFixedDelay(r, delay, delay);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Triggers all due notifications.
     */
    public void triggerDistribution() {
        Collection<RemoteMessage> remoteMessages;
        lock.lock();
        try {
            remoteMessages = remoteMsgs.drain();

            if (remoteMsgs.isEmpty()) {
                cancelTimerTask();
            }
        } finally {
            lock.unlock();
        }

        if (false == remoteMessages.isEmpty()) {
            HazelcastInstance hzInstance = this.hzInstance;
            if (null == hzInstance) {
                LOGGER.warn("Missing Hazelcast instance. Failed to remotely distribute notifications");
                return;
            }

            String mapName = this.mapName;
            if (null == mapName) {
                LOGGER.warn("Missing Hazelcast map name. Failed to remotely distribute notifications");
                return;
            }

            try {
                // Get the Hazelcast map reference
                MultiMap<String, String> hzMap = map(mapName, hzInstance);
                if (null == hzMap) {
                    LOGGER.warn("Missing Hazelcast map (Hazelcast inactive?). Failed to remotely distribute notifications");
                    return;
                }

                // Get local member
                Cluster cluster = hzInstance.getCluster();
                Member localMember = cluster.getLocalMember();

                // Determine other cluster members
                Set<Member> otherMembers = getOtherMembers(cluster.getMembers(), localMember);

                for (Map.Entry<DistributionKey, Set<String>> userMessages : sortyByUser(remoteMessages).entrySet()) {
                    DistributionKey key = userMessages.getKey();
                    doSendRemote(new ArrayList<String>(userMessages.getValue()), key.userId, key.contextId, key.async, key.pathFilter, hzMap, otherMembers, hzInstance);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to remotely distribute notifications", e);
            }
        }
    }

    /**
     * Sends specified messages to all remotely known end-points for given user.
     *
     * @param payloads The notifications to distribute
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param async The async flag
     * @param pathFilter The optional path filter expression
     * @param hzMap The Hazelcast map to use
     * @param otherMembers The other cluster members (excluding this one)
     * @param hzInstance The Haszelcast instance to use
     */
    private void doSendRemote(List<String> payloads, int userId, int contextId, boolean async, String pathFilter, MultiMap<String, String> hzMap, Set<Member> otherMembers, HazelcastInstance hzInstance) {
        // Determine other cluster members holding an open Web Socket connection for current user
        Set<Member> effectiveOtherMembers = new LinkedHashSet<>(otherMembers);
        for (Iterator<Member> it = effectiveOtherMembers.iterator(); it.hasNext(); ) {
            Member member = it.next();
            Address address = member.getAddress();
            Collection<String> infos = hzMap.get(generateKey(userId, contextId, address.getHost(), address.getPort()));
            if (null == infos || infos.isEmpty()) {
                it.remove();
            } else {
                WS_LOGGER.debug("Cluster member \"{}\" signals to hold connected Web Socket(s) for user {} in context {}", member, I(userId), I(contextId));
            }
        }

        if (effectiveOtherMembers.isEmpty()) {
            WS_LOGGER.debug("No remote cluster member holds any connected Web Socket(s) for user {} in context {}", I(userId), I(contextId));
        } else {
            IExecutorService executor = hzInstance.getExecutorService("default");

            for (List<String> partition : Lists.partition(payloads, 5)) {
                Map<Member, Future<Void>> futureMap = executor.submitToMembers(new PortableMessageDistributor(partition, pathFilter, userId, contextId, async), effectiveOtherMembers);
                if (async) {
                    WS_LOGGER.debug("Submitted {} message(s) to remote Web Socket(s) connected to member(s) \"{}\" using path filter \"{}\" to user {} in context {}", I(partition.size()), effectiveOtherMembers, pathFilter, I(userId), I(contextId));
                } else {
                    // Wait for completion of each submitted task
                    int numOfPayloads = partition.size();
                    for (Map.Entry<Member, Future<Void>> element : futureMap.entrySet()) {
                        handleSubmittedFuture(element.getValue(), numOfPayloads, pathFilter, element.getKey(), userId, contextId);
                    }
                }
            }
        }
    }

    private void handleSubmittedFuture(Future<Void> future, int numOfPayloads, String pathFilter, Member member, int userId, int contextId) throws Error {
        // Check Future's return value
        int retryCount = 3;
        while (retryCount-- > 0) {
            try {
                future.get();
                retryCount = 0;
                WS_LOGGER.debug("Transmitted {} message(s) to remote Web Socket(s) connected to member \"{}\" using path filter \"{}\" to user {} in context {}", I(numOfPayloads), member, pathFilter, I(userId), I(contextId));
            } catch (InterruptedException e) {
                // Interrupted - Keep interrupted state
                LOGGER.debug("Interrupted while waiting for {} to complete on member \"{}\" using path filter \"{}\" for user {} in context {}", PortableMessageDistributor.class.getSimpleName(), member, pathFilter, I(userId), I(contextId), e);
                Thread.currentThread().interrupt();
            } catch (CancellationException e) {
                // Canceled
                LOGGER.debug("Canceled while waiting for {} to complete on member \"{}\" using path filter \"{}\" for user {} in context {}", PortableMessageDistributor.class.getSimpleName(), member, pathFilter, I(userId), I(contextId), e);
                retryCount = 0;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();

                // Check for Hazelcast timeout
                if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                    if (cause instanceof RuntimeException) {
                        throw ((RuntimeException) cause);
                    }
                    if (cause instanceof Error) {
                        throw (Error) cause;
                    }
                    throw new IllegalStateException("Not unchecked", cause);
                }

                // Timeout while awaiting remote result
                if (retryCount <= 0) {
                    // No further retry
                    LOGGER.warn("Repeatedly failed transmitting message(s) to remote Web Socket(s) connected to member \"{}\" using path filter \"{}\" to user {} in context {}", member, pathFilter, I(userId), I(contextId));
                    cancelFutureSafe(future);
                }
            }
        } // End of while loop
    }

    /**
     * Sorts specified distributions by user association.
     *
     * @param remoteMessages The distributions to sort
     * @return The user-wise sorted distributions
     */
    private Map<DistributionKey, Set<String>> sortyByUser(Collection<RemoteMessage> remoteMessages) {
        Map<DistributionKey, Set<String>> map = new LinkedHashMap<>();
        for (RemoteMessage remoteMessage : remoteMessages) {
            DistributionKey key = new DistributionKey(remoteMessage.getUserId(), remoteMessage.getContextId(), remoteMessage.isAsync(), remoteMessage.getPathFilter());
            Set<String> userMessages = map.get(key);
            if (null == userMessages) {
                userMessages = new LinkedHashSet<>();
                map.put(key, userMessages);
            }
            userMessages.addAll(remoteMessage.getPayloads());
        }
        return map;
    }

    /**
     * Gets the other cluster members
     *
     * @param allMembers All known members
     * @param localMember The local member
     * @return Other cluster members
     */
    private Set<Member> getOtherMembers(Set<Member> allMembers, Member localMember) {
        Set<Member> otherMembers = new LinkedHashSet<Member>(allMembers);
        if (!otherMembers.remove(localMember)) {
            LOGGER.warn("Couldn't remove local member from cluster members.");
        }
        return otherMembers;
    }

    /**
     * Cancels given {@link Future} safely
     *
     * @param future The {@code Future} to cancel
     */
    private <V> void cancelFutureSafe(Future<V> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------

    private void addMultipleToHzMultiMap(Collection<WebSocket> sockets) {
        if (null == sockets || sockets.isEmpty()) {
            return;
        }

        HazelcastInstance hzInstance = this.hzInstance;
        if (null == hzInstance) {
            LOGGER.warn("Missing Hazelcast instance. Failed to add Web Sockets");
            return;
        }

        String mapName = this.mapName;
        if (null == mapName) {
            LOGGER.warn("Missing Hazelcast map name. Failed to add Web Sockets");
            return;
        }

        MultiMap<String, String> map;
        try {
            map = map(mapName, hzInstance);
        } catch (Exception e) {
            LOGGER.warn("Failed to aquire Hazelcast MultiMap", e);
            return;
        }

        if (null == map) {
            LOGGER.warn("Missing Hazelcast map (Hazelcast inactive?). Failed to add Web Sockets");
            return;
        }

        for (WebSocket socket : sockets) {
            int userId = socket.getUserId();
            int contextId = socket.getContextId();
            String path = socket.getPath();

            try {
                Address address = hzInstance.getCluster().getLocalMember().getAddress();
                String key = generateKey(userId, contextId, address.getHost(), address.getPort());
                String value = generateValue(socket.getConnectionId().getId(), path);
                map.put(key, value);
                synchronized (myValues) {
                    myValues.put(key, value);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to add Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId), e);
            }
        }
    }

    private void addToHzMultiMap(WebSocket socket) {
        if (null == socket) {
            return;
        }

        int userId = socket.getUserId();
        int contextId = socket.getContextId();
        String path = socket.getPath();

        HazelcastInstance hzInstance = this.hzInstance;
        if (null == hzInstance) {
            LOGGER.warn("Missing Hazelcast instance. Failed to add Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
            return;
        }

        String mapName = this.mapName;
        if (null == mapName) {
            LOGGER.warn("Missing Hazelcast map name. Failed to add Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
            return;
        }

        try {
            MultiMap<String, String> map = map(mapName, hzInstance);

            if (null == map) {
                LOGGER.warn("Missing Hazelcast map (Hazelcast inactive?). Failed to add Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
                return;
            }

            Address address = hzInstance.getCluster().getLocalMember().getAddress();
            String key = generateKey(userId, contextId, address.getHost(), address.getPort());
            String value = generateValue(socket.getConnectionId().getId(), path);
            map.put(key, value);
            synchronized (myValues) {
                myValues.put(key, value);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to add Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId), e);
        }
    }

    private void removeFromHzMultiMap(WebSocket socket) {
        if (null == socket) {
            return;
        }

        int userId = socket.getUserId();
        int contextId = socket.getContextId();
        String path = socket.getPath();

        HazelcastInstance hzInstance = this.hzInstance;
        if (null == hzInstance) {
            LOGGER.warn("Missing Hazelcast instance. Failed to remove Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
            return;
        }

        String mapName = this.mapName;
        if (null == mapName) {
            LOGGER.warn("Missing Hazelcast map name. Failed to remove Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
            return;
        }

        try {
            Address address = hzInstance.getCluster().getLocalMember().getAddress();
            String key = generateKey(userId, contextId, address.getHost(), address.getPort());
            String value = generateValue(socket.getConnectionId().getId(), path);
            synchronized (myValues) {
                myValues.remove(key, value);
            }
            MultiMap<String, String> hzMap = map(mapName, hzInstance);

            if (null == hzMap) {
                LOGGER.warn("Missing Hazelcast map (Hazelcast inactive?). Failed to remove Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
                return;
            }

            hzMap.remove(key, value);
        } catch (Exception e) {
            LOGGER.warn("Failed to remove Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId), e);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void addWebSocket(WebSocket socket) {
        addToHzMultiMap(socket);
        startCleanerTaskFor(socket.getUserId(), socket.getContextId());
    }

    @Override
    public void addWebSocket(Collection<WebSocket> sockets) {
        addMultipleToHzMultiMap(sockets);
    }

    @Override
    public void removeWebSocket(WebSocket socket) {
        removeFromHzMultiMap(socket);
    }

    @Override
    public void startCleanerTaskFor(int userId, int contextId) {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        ScheduledTimerTask cleanerTask = cleanerTasks.get(key);
        if (null == cleanerTask) {
            Runnable r = new CleanerRunnable(userId, contextId);
            ScheduledTimerTask newTask = timerService.scheduleAtFixedRate(r, 5, 5, TimeUnit.MINUTES);
            cleanerTask = cleanerTasks.putIfAbsent(key, newTask);
            if (null != cleanerTask) {
                // Another thread scheduled a timer task in the meantime
                newTask.cancel();
                timerService.purge();
            } else {
                LOGGER.debug("Started cleaner task for user {} in context {}", I(userId), I(contextId));
            }
        }
    }

    @Override
    public void stopCleanerTaskFor(int userId, int contextId) {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        ScheduledTimerTask cleanerTask = cleanerTasks.remove(key);
        if (null != cleanerTask) {
            cleanerTask.cancel();
            timerService.purge();
            LOGGER.debug("Stopped cleaner task for user {} in context {} as last active session was dropped", I(userId), I(contextId));
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------

    private final class CleanerRunnable implements Runnable {

        private final int contextId;
        private final int userId;

        CleanerRunnable(int userId, int contextId) {
            this.contextId = contextId;
            this.userId = userId;
        }

        @Override
        public void run() {
            try {
                HazelcastInstance hzInstance = HzRemoteWebSocketDistributor.this.hzInstance;
                if (null == hzInstance) {
                    LOGGER.warn("Missing Hazelcast instance. Failed to perform cleaner task for user {} in context {}", I(userId), I(contextId));
                    return;
                }

                String mapName = HzRemoteWebSocketDistributor.this.mapName;
                if (null == mapName) {
                    LOGGER.warn("Missing Hazelcast map name. Failed to perform cleaner task for user {} in context {}", I(userId), I(contextId));
                    return;
                }

                GrizzlyWebSocketApplication application = GrizzlyWebSocketApplication.getGrizzlyWebSocketApplication();
                if (null == application) {
                    LOGGER.warn("Missing Web Application instance. Failed to perform cleaner task for user {} in context {}", I(userId), I(contextId));
                    return;
                }

                MultiMap<String, String> map = map(mapName, hzInstance);
                if (null == map) {
                    LOGGER.warn("Missing Hazelcast map (Hazelcast inactive?). Failed to perform cleaner task for user {} in context {}", I(userId), I(contextId));
                    return;
                }

                LOGGER.debug("Running cleaner task for user {} in context {}...", I(userId), I(contextId));


                Address address = hzInstance.getCluster().getLocalMember().getAddress();
                String key = generateKey(userId, contextId, address.getHost(), address.getPort());
                Collection<String> collection = map.get(key);
                if (null == collection || collection.isEmpty()) {
                    LOGGER.debug("Detected no orphaned entries in Hazelcast map during cleaner task run for user {} in context {}", I(userId), I(contextId));
                    return;
                }

                Map<ConnectionId, MapValue> connectionIds = new HashMap<>();
                for (String value : collection) {
                    try {
                        MapValue v = MapValue.parseFrom(value);
                        connectionIds.put(ConnectionId.newInstance(v.getConnectionId()), v);
                    } catch (Exception e) {
                        // Ignore invalid value
                    }
                }

                application.retainNonExisting(connectionIds.keySet(), userId, contextId);

                if (connectionIds.isEmpty()) {
                    LOGGER.debug("Detected no orphaned entries in Hazelcast map during cleaner task run for user {} in context {}", I(userId), I(contextId));
                    return;
                }

                for (MapValue v : connectionIds.values()) {
                    String value = generateValue(v.getConnectionId(), v.getPath());
                    map.remove(key, value);
                    synchronized (myValues) {
                        myValues.remove(key, value);
                    }
                }

                LOGGER.info("Removed {} orphaned entries from Hazelcast map during cleaner task run for user {} in context {}", I(connectionIds.size()), I(userId), I(contextId));
            } catch (Exception e) {
                LOGGER.warn("Failed to perform cleaner task for user {} in context {}", I(userId), I(contextId), e);
            }
        }
    }

    private static final class DistributionKey {

        final int userId;
        final int contextId;
        final boolean async;
        final String pathFilter;
        private final int hash;

        /**
         * Initializes a new {@link RemoteMessage}.
         */
        DistributionKey(int userId, int contextId, boolean async, String pathFilter) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            this.async = async;
            this.pathFilter = pathFilter;
            int prime = 31;
            int result = 1;
            result = prime * result + (async ? 1231 : 1237);
            result = prime * result + contextId;
            result = prime * result + userId;
            result = prime * result + ((pathFilter == null) ? 0 : pathFilter.hashCode());
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
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DistributionKey other = (DistributionKey) obj;
            if (async != other.async) {
                return false;
            }
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            if (pathFilter == null) {
                if (other.pathFilter != null) {
                    return false;
                }
            } else if (!pathFilter.equals(other.pathFilter)) {
                return false;
            }
            return true;
        }
    }

}
