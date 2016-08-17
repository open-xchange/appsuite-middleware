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

package com.openexchange.push.impl;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.push.PushUser;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSession;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionRemoteLookUp;

/**
 * {@link SessionLookUpUtility} - Utility class to look-up a session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SessionLookUpUtility {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionLookUpUtility.class);

    private final ServiceLookup services;
    private final PushManagerRegistry registry;

    /**
     * Initializes a new {@link SessionLookUpUtility}.
     */
    public SessionLookUpUtility(PushManagerRegistry registry, ServiceLookup services) {
        super();
        this.registry = registry;
        this.services = services;
    }

    /**
     * Attempts to look-up a session for specified user
     *
     * @param pushUser The user to look-up for
     * @param considerGenerated Whether to consider a generated session
     * @param considerRemoteLookUp Whether to consider to perform a remote look-up
     * @return The looked-up session or <code>null</code>
     */
    public Session lookUpSessionFor(PushUser pushUser, boolean considerGenerated, boolean considerRemoteLookUp) {
        int contextId = pushUser.getContextId();
        int userId = pushUser.getUserId();

        SessiondService sessiondService = services.getService(SessiondService.class);
        Session session = sessiondService.findFirstMatchingSessionForUser(userId, contextId, new SessionMatcher() {

            @Override
            public Set<Flag> flags() {
                return SessionMatcher.NO_FLAGS;
            }

            @Override
            public boolean accepts(Session session) {
                return true;
            }}
        );

        if (null == session && considerGenerated) {
            try {
                session = registry.generateSessionFor(pushUser);
            } catch (Exception e) {
                // Failed...
            }
        }

        if (null == session && considerRemoteLookUp) {
            HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
            ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
            if (null != hzInstance && null != obfuscatorService) {
                Cluster cluster = hzInstance.getCluster();

                // Get local member
                Member localMember = cluster.getLocalMember();

                // Determine other cluster members
                Set<Member> otherMembers = getOtherMembers(cluster.getMembers(), localMember);

                if (!otherMembers.isEmpty()) {
                    IExecutorService executor = hzInstance.getExecutorService("default");
                    Map<Member, Future<PortableSession>> futureMap = executor.submitToMembers(new PortableSessionRemoteLookUp(userId, contextId), otherMembers);
                    for (Iterator<Entry<Member, Future<PortableSession>>> it = futureMap.entrySet().iterator(); null == session && it.hasNext();) {
                        Future<PortableSession> future = it.next().getValue();
                        // Check Future's return value
                        int retryCount = 3;
                        while (retryCount-- > 0) {
                            try {
                                PortableSession portableSession = future.get();
                                retryCount = 0;
                                if (null != portableSession) {
                                    portableSession.setPassword(obfuscatorService.unobfuscate(portableSession.getPassword()));
                                    session = portableSession;
                                }
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

        return session;
    }

    /**
     * Gets the other cluster members
     *
     * @param allMembers All known members
     * @param localMember The local member
     * @return Other cluster members
     */
    private static Set<Member> getOtherMembers(Set<Member> allMembers, Member localMember) {
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
    private static <V> void cancelFutureSafe(Future<V> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

}
