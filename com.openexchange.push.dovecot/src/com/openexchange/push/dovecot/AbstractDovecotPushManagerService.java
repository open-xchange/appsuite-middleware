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

package com.openexchange.push.dovecot;

import static com.openexchange.java.Autoboxing.I;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.Hazelcasts;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUtility;
import com.openexchange.push.dovecot.registration.RegistrationContext;
import com.openexchange.push.dovecot.registration.RegistrationContext.DoveAdmClientProvider;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableMultipleActiveSessionRemoteLookUp;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSession;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionCollection;


/**
 * {@link AbstractDovecotPushManagerService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public abstract class AbstractDovecotPushManagerService implements PushManagerExtendedService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractDovecotPushManagerService.class);

    private final String name;

    /** The bundle configuration */
    protected final DovecotPushConfiguration config;

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractDovecotPushManagerService}.
     *
     * @param config The dovecot push configuration
     * @param services The service look-up
     */
    protected AbstractDovecotPushManagerService(DovecotPushConfiguration config, ServiceLookup services) {
        super();
        name = "Dovecot Push Manager";
        this.config = config;
        this.services = services;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean supportsPermanentListeners() {
        return true;
    }

    @Override
    public boolean listenersRequireResources() {
        return false;
    }

    /**
     * Checks if Dovecot Push is enabled for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    protected boolean isDovecotPushEnabledFor(int userId, int contextId) throws OXException {
        ConfigViewFactory factory = services.getOptionalService(ConfigViewFactory.class);
        if (factory == null) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(userId, contextId);
        return ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.push.dovecot.enabled", true, view);
    }

    /**
     * Checks if given user has a permanent push registration.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if given user has a permanent push registration; otherwise <code>false</code>
     */
    protected boolean hasPermanentPush(int userId, int contextId) {
        try {
            PushListenerService pushListenerService = services.getService(PushListenerService.class);
            return pushListenerService.hasRegistration(new PushUser(userId, contextId));
        } catch (Exception e) {
            LOGGER.warn("Failed to check for push registration for user {} in context {}", I(userId), I(contextId), e);
            return false;
        }
    }

    /**
     * Generates a session for specified push user.
     *
     * @param pushUser The push user
     * @return The generated session
     * @throws OXException If session cannot be generated
     */
    protected Session generateSessionFor(PushUser pushUser) throws OXException {
        PushListenerService pushListenerService = services.getService(PushListenerService.class);
        return pushListenerService.generateSessionFor(pushUser);
    }

    /**
     * Looks-up a push-capable session for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param optOldSession The optional old session (which expired)
     * @return A push-capable session or <code>null</code>
     */
    public Session lookUpSessionFor(int userId, int contextId, Session optOldSession) {
        // Look-up sessions
        SessiondService sessiondService = services.getService(SessiondService.class);
        if (null != sessiondService) {
            final String oldSessionId = null == optOldSession ? null : optOldSession.getSessionID();

            // Query local ones
            SessionMatcher matcher = new SessionMatcher() {

                @Override
                public Set<Flag> flags() {
                    return SessionMatcher.ONLY_SHORT_TERM;
                }

                @Override
                public boolean accepts(Session session) {
                    return (oldSessionId == null || !oldSessionId.equals(session.getSessionID())) && PushUtility.allowedClient(session.getClient(), session, true);
                }
            };
            Session anotherActiveSession = sessiondService.findFirstMatchingSessionForUser(userId, contextId, matcher);
            if (anotherActiveSession != null) {
                return anotherActiveSession;
            }

            // Look-up remote sessions, too, if possible
            Session session = lookUpRemoteSessionFor(userId, contextId, optOldSession);
            if (session != null) {
                Session ses = sessiondService.getSession(session.getSessionID());
                if (ses != null) {
                    return ses;
                }
            }
        }

        return null;
    }

    private Session lookUpRemoteSessionFor(int userId, int contextId, Session optOldSession) {
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        final ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
        if (null == hzInstance || null == obfuscatorService) {
            return null;
        }

        // Determine other cluster members
        Set<Member> otherMembers = Hazelcasts.getRemoteMembers(hzInstance);
        if (otherMembers.isEmpty()) {
            return null;
        }

        final String oldSessionId = null == optOldSession ? null : optOldSession.getSessionID();
        Hazelcasts.Filter<PortableSessionCollection, PortableSession> filter = new Hazelcasts.Filter<PortableSessionCollection, PortableSession>() {

            @Override
            public PortableSession accept(PortableSessionCollection portableSessionCollection) {
                PortableSession[] portableSessions = portableSessionCollection.getSessions();
                if (null != portableSessions) {
                    for (PortableSession portableSession : portableSessions) {
                        if ((null == oldSessionId || false == oldSessionId.equals(portableSession.getSessionID())) && PushUtility.allowedClient(portableSession.getClient(), portableSession, true)) {
                            portableSession.setPassword(obfuscatorService.unobfuscate(portableSession.getPassword()));
                            return portableSession;
                        }
                    }
                }
                return null;
            }
        };
        try {
            return Hazelcasts.executeByMembersAndFilter(new PortableMultipleActiveSessionRemoteLookUp(userId, contextId), otherMembers, hzInstance.getExecutorService("default"), filter);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            LOGGER.error("lookUpRemoteSessionFor failed with an ExecutionException", cause);
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    public abstract void unregisterForDeletedUser(PushUser pushUser) throws OXException;

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** Implements a <code>DoveAdmClientProvider</code> based on a service look-up */
    public static class ServiceLookupDoveAdmClientProvider implements DoveAdmClientProvider {

        private final ServiceLookup services;

        /**
         * Initializes a new instance based on given service look-up.
         */
        public ServiceLookupDoveAdmClientProvider(ServiceLookup services) {
            super();
            this.services = services;
        }

        @Override
        public DoveAdmClient getDoveAdmClient() throws OXException {
            DoveAdmClient doveAdmClient = services.getOptionalService(DoveAdmClient.class);
            if (null == doveAdmClient) {
                throw ServiceExceptionCode.absentService(DoveAdmClient.class);
            }
            return doveAdmClient;
        }
    }

    protected RegistrationContext getRegistrationContext(Session session) {
        DoveAdmClient doveAdmClient = config.preferDoveadmForMetadata() ? services.getOptionalService(DoveAdmClient.class): null;
        RegistrationContext registrationContext = null == doveAdmClient ? RegistrationContext.createSessionContext(session) : RegistrationContext.createDoveAdmClientContext(session.getUserId(), session.getContextId(), new ServiceLookupDoveAdmClientProvider(services));
        return registrationContext;
    }

    protected RegistrationContext getRegistrationContext(PushUser pushUser) throws OXException {
        DoveAdmClient doveAdmClient = services.getOptionalService(DoveAdmClient.class);
        if (config.preferDoveadmForMetadata() && doveAdmClient != null) {
            return RegistrationContext.createDoveAdmClientContext(pushUser.getUserId(), pushUser.getContextId(), new ServiceLookupDoveAdmClientProvider(services));
        }

        // TODO: Ensuring OAuth tokens should be implemented as part of com.openexchange.push.PushListenerService.generateSessionFor(PushUser)
        try {
            Session session = null;
            {
                String sessionId = LogProperties.get(LogProperties.Name.SESSION_SESSION_ID);
                if (Strings.isNotEmpty(sessionId)) {
                    session = services.getServiceSafe(SessiondService.class).getSession(sessionId);
                    if (session != null && (session.getContextId() != pushUser.getContextId() || session.getUserId() != pushUser.getUserId())) {
                        session = null;
                    }
                }
            }

            if (session == null) {
                session = generateSessionFor(pushUser);
            }

            if (AuthType.isOAuthType(MailConfig.getConfiguredAuthType(true, session)) && session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN) == null) {
                LOGGER.debug("Falling back to DoveAdm push registration context due to missing OAuth token in session");
                return RegistrationContext.createDoveAdmClientContext(pushUser.getUserId(), pushUser.getContextId(), new ServiceLookupDoveAdmClientProvider(services));
            }

            return RegistrationContext.createSessionContext(session);
        } catch (OXException e) {
            if (doveAdmClient != null && (PushExceptionCodes.MISSING_PASSWORD.equals(e) || PushExceptionCodes.MISSING_MASTER_PASSWORD.equals(e))) {
                LOGGER.debug("Falling back to DoveAdm push registration context due to missing password in session");
                return RegistrationContext.createDoveAdmClientContext(pushUser.getUserId(), pushUser.getContextId(), new ServiceLookupDoveAdmClientProvider(services));
            }

            throw e;
        }

    }

}
