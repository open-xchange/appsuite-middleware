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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.websockets.grizzly.remote.portable.PortableMessageDistributor;

/**
 * {@link RemoteWebSocketDistributor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class RemoteWebSocketDistributor {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoteWebSocketDistributor.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link RemoteWebSocketDistributor}.
     */
    public RemoteWebSocketDistributor(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Sends specified text message to all remotely known end-points for given user.
     *
     * @param message The text message to distribute
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param async Whether to send asynchronously or blocking
     * @throws OXException If remote distribution fails
     */
    public void sendRemote(String message, int userId, int contextId, boolean async) throws OXException {
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            LOG.warn("Missing Hazelcast instance. Failed to remotely distribute message to user {} in context {}", userId, contextId);
            return;
        }

        Cluster cluster = hzInstance.getCluster();

        // Get local member
        Member localMember = cluster.getLocalMember();

        // Determine other cluster members
        Set<Member> otherMembers = getOtherMembers(cluster.getMembers(), localMember);

        if (!otherMembers.isEmpty()) {
            IExecutorService executor = hzInstance.getExecutorService("default");
            Map<Member, Future<Void>> futureMap = executor.submitToMembers(new PortableMessageDistributor(message, userId, contextId, async), otherMembers);

            if (false == async) {
                // Wait for completion of each submitted task
                for (Iterator<Entry<Member, Future<Void>>> it = futureMap.entrySet().iterator(); it.hasNext();) {
                    Future<Void> future = it.next().getValue();
                    // Check Future's return value
                    int retryCount = 3;
                    while (retryCount-- > 0) {
                        try {
                            future.get();
                            retryCount = 0;
                        } catch (InterruptedException e) {
                            // Interrupted - Keep interrupted state
                            Thread.currentThread().interrupt();
                        } catch (CancellationException e) {
                            // Canceled
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

    /**
     * Gets the other cluster members
     *
     * @param allMembers All known members
     * @param localMember The local member
     * @return Other cluster members
     */
    static Set<Member> getOtherMembers(Set<Member> allMembers, Member localMember) {
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
    static <V> void cancelFutureSafe(Future<V> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

    private static final class GetFuture<V> implements Future<V> {

        private final V result;

        GetFuture(V result) {
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return result;
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return result;
        }
    }

}
