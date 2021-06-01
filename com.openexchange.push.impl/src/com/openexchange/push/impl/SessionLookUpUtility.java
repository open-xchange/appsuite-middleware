/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.push.impl;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.hazelcast.Hazelcasts;
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
            final ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
            if (null != hzInstance && null != obfuscatorService) {
                // Determine other cluster members
                Set<Member> otherMembers = Hazelcasts.getRemoteMembers(hzInstance);

                if (!otherMembers.isEmpty()) {
                    Hazelcasts.Filter<PortableSession, PortableSession> filter = new Hazelcasts.Filter<PortableSession, PortableSession>() {

                        @Override
                        public PortableSession accept(PortableSession portableSession) {
                            if (null != portableSession) {
                                portableSession.setPassword(obfuscatorService.unobfuscate(portableSession.getPassword()));
                                return portableSession;
                            }
                            return null;
                        }
                    };
                    try {
                        session = Hazelcasts.executeByMembersAndFilter(new PortableSessionRemoteLookUp(userId, contextId), otherMembers, hzInstance.getExecutorService("default"), filter);
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof RuntimeException) {
                            throw ((RuntimeException) cause);
                        }
                        if (cause instanceof Error) {
                            throw (Error) cause;
                        }
                        throw new IllegalStateException("Not unchecked", cause);
                    }
                }
            }
        }

        return session;
    }

}
