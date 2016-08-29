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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.BufferingQueue;
import com.openexchange.session.UserAndContext;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketExceptionCodes;
import com.openexchange.websockets.WebSockets;
import com.openexchange.websockets.grizzly.remote.portable.PortableMessageDistributor;

/**
 * {@link HzRemoteWebSocketDistributor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class HzRemoteWebSocketDistributor implements RemoteWebSocketDistributor {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HzRemoteWebSocketDistributor.class);

    private static int delayDuration(ConfigurationService configService) {
        int defaultValue = 5000;
        if (null == configService) {
            return defaultValue;
        }
        return configService.getIntProperty("com.openexchange.websockets.grizzly.remote.delayDuration", defaultValue);
    }

    private static int maxDelayDuration(ConfigurationService configService) {
        int defaultValue = 10000;
        if (null == configService) {
            return defaultValue;
        }
        return configService.getIntProperty("com.openexchange.websockets.grizzly.remote.maxDelayDuration", defaultValue);
    }

    private static int timerFrequency(ConfigurationService configService) {
        int defaultValue = 3000;
        if (null == configService) {
            return defaultValue;
        }
        return configService.getIntProperty("com.openexchange.websockets.grizzly.remote.timerFrequency", defaultValue);
    }

    // -------------------------------------------------------------------------------------------------------------

    private final BufferingQueue<Distribution> publishQueue;
    private final ScheduledTimerTask timerTask;

    private volatile HazelcastInstance hzInstance;
    private volatile String mapName;
    private volatile String memberUuid;

    /**
     * Initializes a new {@link HzRemoteWebSocketDistributor}.
     */
    public HzRemoteWebSocketDistributor(TimerService timerService, ConfigurationService configService) {
        super();
        publishQueue = new BufferingQueue<Distribution>(delayDuration(configService), maxDelayDuration(configService)) {

            @Override
            protected BufferingQueue.BufferedElement<Distribution> transfer(Distribution toOffer, BufferingQueue.BufferedElement<Distribution> existing) {
                toOffer.mergeWith(existing.getElement());
                return new BufferingQueue.BufferedElement<Distribution>(toOffer, existing);
            }
        };

        // Timer task
        final org.slf4j.Logger log = LOG;
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

    /**
     * Shuts-down this remote distributor.
     */
    public void shutDown() {
        unsetHazelcastResources();
        timerTask.cancel();
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
        this.memberUuid = hzInstance.getCluster().getLocalMember().getUuid();
    }

    /**
     * Unsets the previously set Hazelcast resources (if any).
     */
    public void unsetHazelcastResources() {
        this.hzInstance = null;
        this.mapName = null;
        this.memberUuid = null;
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
    private MultiMap<String, String> map(String mapName, HazelcastInstance hzInstance) throws OXException {
        try {
            return hzInstance.getMultiMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            // Obviously Hazelcast is absent
            LOG.warn("HazelcastInstance not active", e);
            return null;
        } catch (HazelcastException e) {
            throw WebSocketExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw WebSocketExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private String generateKey(int userId, int contextId, String memberUuid) {
        return new StringBuilder(48).append(userId).append('@').append(contextId).append(':').append(memberUuid).toString();
    }

    private String generateValue(String connectionId, String path) {
        return new StringBuilder(48).append(connectionId).append(':').append(null == path ? "" : path).toString();
    }

    @Override
    public boolean existsAnyRemote(String pathFilter, int userId, int contextId) {
        try {
            // Get the Hazelcast map reference
            MultiMap<String, String> hzMap = map(mapName, hzInstance);

            // Grab known connection identifiers
            Collection<String> infos = hzMap.get(generateKey(userId, contextId, memberUuid));
            if (null == infos || infos.isEmpty()) {
                return false;
            }

            if (null == pathFilter) {
                // Well, either one is enough then...
                return true;
            }

            // Check if filter matches for any
            for (String info : infos) {
                if (WebSockets.matches(pathFilter, WebSocketInfo.parsePathFrom(info))) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            LOG.warn("Failed to remotely check any open Web Socket using path filter {} for user {} in context {}", null == pathFilter ? "<none>" : pathFilter, I(userId), I(contextId), e);
        }
        return false;
    }

    @Override
    public void sendRemote(String message, String pathFilter, int userId, int contextId, boolean async) {
        publishQueue.offerOrReplaceAndReset(new Distribution(message, pathFilter, userId, contextId, async));
    }

    /**
     * Triggers all due notifications.
     */
    public void triggerDistribution() {
        List<Distribution> distributions = new LinkedList<Distribution>();
        if (0 < publishQueue.drainTo(distributions)) {
            HazelcastInstance hzInstance = this.hzInstance;
            if (null == hzInstance) {
                LOG.warn("Missing Hazelcast instance. Failed to remotely distribute notifications");
                return;
            }

            String mapName = this.mapName;
            if (null == mapName) {
                LOG.warn("Missing Hazelcast map name. Failed to remotely distribute notifications");
                return;
            }

            try {
                // Get the Hazelcast map reference
                MultiMap<String, String> hzMap = map(mapName, hzInstance);

                // Get local member
                Cluster cluster = hzInstance.getCluster();
                Member localMember = cluster.getLocalMember();

                // Determine other cluster members holding an open Web Socket connection for current user
                Set<Member> otherMembers = getOtherMembers(cluster.getMembers(), localMember);

                for (Map.Entry<UserAndContext, Set<Distribution.DistributionPayload>> userMessages : sortyByUser(distributions).entrySet()) {
                    UserAndContext uac = userMessages.getKey();
                    doSendRemote(userMessages.getValue(), uac.getUserId(), uac.getContextId(), hzMap, otherMembers, hzInstance);
                }
            } catch (Exception e) {
                LOG.warn("Failed to remotely distribute notifications", e);
            }
        }
    }

    /**
     * Sends specified messages to all remotely known end-points for given user.
     *
     * @param payloads The notifications to distribute
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param hzMap The Hazelcast map to use
     * @param otherMembers The other cluster members (excluding this one)
     * @param hzInstance The Haszelcast instance to use
     */
    protected void doSendRemote(Set<Distribution.DistributionPayload> payloads, int userId, int contextId, MultiMap<String, String> hzMap, Set<Member> otherMembers, HazelcastInstance hzInstance) {
        // Determine other cluster members holding an open Web Socket connection for current user
        Set<Member> effectiveOtherMembers = new LinkedHashSet<>(otherMembers);
        for (Iterator<Member> it = effectiveOtherMembers.iterator(); it.hasNext(); ) {
            Member member = it.next();
            Collection<String> infos = hzMap.get(generateKey(userId, contextId, member.getUuid()));
            if (null == infos || infos.isEmpty()) {
                it.remove();
            }
        }

        if (!effectiveOtherMembers.isEmpty()) {
            IExecutorService executor = hzInstance.getExecutorService("default");

            for (final Distribution.DistributionPayload distributionPayload : payloads) {
                boolean async = distributionPayload.async;
                Map<Member, Future<Void>> futureMap = executor.submitToMembers(new PortableMessageDistributor(distributionPayload.message, distributionPayload.pathFilter, userId, contextId, async), effectiveOtherMembers);
                if (false == async) {
                    // Wait for completion of each submitted task
                    for (Iterator<Entry<Member, Future<Void>>> it = futureMap.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<Member, Future<Void>> futureMapEntry = it.next();
                        Future<Void> future = futureMapEntry.getValue();
                        // Check Future's return value
                        int retryCount = 3;
                        while (retryCount-- > 0) {
                            try {
                                future.get();
                                retryCount = 0;
                                LOG.debug("Submitted message \"{}\" to remote Web Socket(s) connected to member \"{}\" using path filter \"{}\" to user {} in context {}", new Object() { @Override public String toString(){ return StringUtils.abbreviate(distributionPayload.message, 12); }}, futureMapEntry.getKey(), distributionPayload.pathFilter, userId, contextId);
                            } catch (InterruptedException e) {
                                // Interrupted - Keep interrupted state
                                LOG.debug("Interrupted while waiting for {} to complete", PortableMessageDistributor.class.getSimpleName(), e);
                                Thread.currentThread().interrupt();
                            } catch (CancellationException e) {
                                // Canceled
                                LOG.debug("Canceled while waiting for {} to complete", PortableMessageDistributor.class.getSimpleName(), e);
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
                                    cancelFutureSafe(future);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sorts specified distributions by user association.
     *
     * @param distributions The distributions to sort
     * @return The user-wise sorted distributions
     */
    private Map<UserAndContext, Set<Distribution.DistributionPayload>> sortyByUser(List<Distribution> distributions) {
        Map<UserAndContext, Set<Distribution.DistributionPayload>> map = new LinkedHashMap<>();
        for (Distribution distribution : distributions) {
            UserAndContext uac = UserAndContext.newInstance(distribution.getUserId(), distribution.getContextId());
            Set<Distribution.DistributionPayload> userMessages = map.get(uac);
            if (null == userMessages) {
                userMessages = new LinkedHashSet<>();
                map.put(uac, userMessages);
            }
            userMessages.addAll(distribution.getPayloads());
        }
        return map;
    }

    /**
     * Gets the other cluster members
     *
     * @param uuids The known UUIDs
     * @param allMembers All known members
     * @param localMember The local member
     * @return Other cluster members
     */
    private Set<Member> getOtherMembers(Set<Member> allMembers, Member localMember) {
        Set<Member> otherMembers = new LinkedHashSet<Member>(allMembers);
        if (!otherMembers.remove(localMember)) {
            LOG.warn("Couldn't remove local member from cluster members.");
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

    private void addToHzMultiMap(int userId, int contextId, String connectionId, String path) {
        try {
            HazelcastInstance hzInstance = this.hzInstance;
            if (null == hzInstance) {
                LOG.warn("Missing Hazelcast instance. Failed to add Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
                return;
            }

            String mapName = this.mapName;
            if (null == mapName) {
                LOG.warn("Missing Hazelcast map name. Failed to add Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
                return;
            }

            map(mapName, hzInstance).put(generateKey(userId, contextId, memberUuid), generateValue(connectionId, path));
        } catch (Exception e) {
            LOG.warn("Failed to add Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId), e);
        }
    }

    private void removeFromHzMultiMap(int userId, int contextId, String connectionId, String path) {
        try {
            HazelcastInstance hzInstance = this.hzInstance;
            if (null == hzInstance) {
                LOG.warn("Missing Hazelcast instance. Failed to remove Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
                return;
            }

            String mapName = this.mapName;
            if (null == mapName) {
                LOG.warn("Missing Hazelcast map name. Failed to remove Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId));
                return;
            }

            map(mapName, hzInstance).remove(generateKey(userId, contextId, memberUuid), generateValue(connectionId, path));
        } catch (Exception e) {
            LOG.warn("Failed to remove Web Socket with path {} for user {} in context {}", path, I(userId), I(contextId), e);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Call-back for a connected Web Socket.
     *
     * @param socket The Web Socket
     */
    @Override
    public void onWebSocketConnect(WebSocket socket) {
        addToHzMultiMap(socket.getUserId(), socket.getContextId(), socket.getConnectionId().getId(), socket.getPath());
    }

    /**
     * Call-back for a closed Web Socket.
     *
     * @param socket The Web Socket
     */
    @Override
    public void onWebSocketClose(WebSocket socket) {
        removeFromHzMultiMap(socket.getUserId(), socket.getContextId(), socket.getConnectionId().getId(), socket.getPath());
    }

}
