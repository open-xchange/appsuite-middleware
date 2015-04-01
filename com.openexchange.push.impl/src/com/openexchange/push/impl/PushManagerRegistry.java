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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailConfig.PasswordSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUtility;
import com.openexchange.push.credstorage.CredentialStorage;
import com.openexchange.push.credstorage.CredentialStorageProvider;
import com.openexchange.push.credstorage.Credentials;
import com.openexchange.push.credstorage.DefaultCredentials;
import com.openexchange.push.impl.PushDbUtils.DeleteResult;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link PushManagerRegistry} - The push manager registry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushManagerRegistry implements PushListenerService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PushManagerRegistry.class);

    /** The <code>PushManagerRegistry</code> instance */
    private static volatile PushManagerRegistry instance;

    /**
     * Initializes push manager registry.
     */
    public static synchronized void init(ServiceLookup services) {
        if (null == instance) {
            instance = new PushManagerRegistry(services);
        }
    }

    /**
     * Shuts down push manager registry.
     */
    public static synchronized void shutdown() {
        instance = null;
    }

    /**
     * Gets the push manager registry.
     *
     * @return The push manager registry
     */
    public static PushManagerRegistry getInstance() {
        return instance;
    }

    /*-
     * --------------------------------------------------------- Member section ----------------------------------------------------------
     */

    private final ConcurrentMap<Class<? extends PushManagerService>, PushManagerService> map;
    private final ServiceLookup services;
    private final Set<PushUser> initialPushUsers;

    /**
     * Initializes a new {@link PushManagerRegistry}.
     *
     * @param services
     */
    private PushManagerRegistry(ServiceLookup services) {
        super();
        this.services = services;
        initialPushUsers = new HashSet<PushUser>(256); // Always wrapped by surrounding synchronized block
        map = new ConcurrentHashMap<Class<? extends PushManagerService>, PushManagerService>();
    }

    private CredentialStorage optCredentialStorage() throws OXException {
        CredentialStorageProvider storageProvider = services.getOptionalService(CredentialStorageProvider.class);
        return null == storageProvider ? null : storageProvider.getCredentialStorage();
    }

    private Credentials optCredentials(int userId, int contextId) throws OXException {
        CredentialStorage storage = optCredentialStorage();
        return null == storage ? null : storage.getCredentials(userId, contextId);
    }

    /**
     * Checks if this registry contains at least one <i>{@link PushManagerExtendedService}</i> instance at the time of invocation.
     *
     * @return <code>true</code> if this registry contains at least one <code>PushManagerExtendedService</code> instance; otherwise <code>false</code>
     */
    public boolean hasExtendedService() {
        for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
            PushManagerService pushManager = pushManagersIterator.next();
            if (pushManager instanceof PushManagerExtendedService) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the permanent listeners for given push users.
     *
     * @param pushUsers The push users
     */
    public void applyInitialListeners(List<PushUser> pushUsers) {
        if (null == pushUsers || pushUsers.isEmpty()) {
            return;
        }

        Collection<PushUser> toStart;
        Collection<PushUser> toStop;
        synchronized (this) {
            {
                Set<PushUser> current = new HashSet<PushUser>(initialPushUsers);
                current.removeAll(pushUsers);
                toStop = current;
            }

            toStart = new LinkedList<PushUser>();
            for (PushUser pushUser : pushUsers) {
                if (initialPushUsers.add(pushUser)) {
                    toStart.add(pushUser);
                }
            }

            initialPushUsers.removeAll(toStop);
        }

        // Start permanent candidates
        for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
            PushManagerService pushManager = pushManagersIterator.next();
            if (pushManager instanceof PushManagerExtendedService) {
                startPermanentListenersFor(toStart, (PushManagerExtendedService) pushManager);
            }
        }

        // Stop permanent candidates
        for (PushUser pushUser : toStop) {
            boolean rescheduled = false;
            for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
                PushManagerService pushManager = pushManagersIterator.next();
                if (pushManager instanceof PushManagerExtendedService) {
                    try {
                        boolean stopped = ((PushManagerExtendedService) pushManager).stopPermanentListener(pushUser, false);
                        if (stopped) {
                            rescheduled = true;
                            LOG.debug("Rescheduling permanent push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), pushManager);
                        }
                    } catch (OXException e) {
                        LOG.error("Error while starting permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), pushManager, e);
                    } catch (RuntimeException e) {
                        LOG.error("Runtime error while starting permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), pushManager, e);
                    }
                }
            }

            if (rescheduled) {
                LOG.info("Rescheduled permanent push listener for user {} in context {} on another cluster node.", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()));
            }
        }
    }

    private void startPermanentListenersFor(Collection<PushUser> pushUsers, PushManagerExtendedService extendedService) {
        for (PushUser pushUser : pushUsers) {
            try {
                PushListener pl = extendedService.startPermanentListener(pushUser);
                if (null != pl) {
                    LOG.debug("Started permanent push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), extendedService);
                }
            } catch (OXException e) {
                LOG.error("Error while starting permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), extendedService, e);
            } catch (RuntimeException e) {
                LOG.error("Runtime error while starting permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), extendedService, e);
            }
        }
    }

    @Override
    public boolean registerPermanentListenerFor(Session session, String clientId) throws OXException {
        if (!PushUtility.allowedClient(clientId)) {
            /*
             * No permanent push listener for the client.
             */
            return false;
        }

        int contextId = session.getContextId();
        int userId = session.getUserId();

        boolean inserted = PushDbUtils.insertPushRegistration(userId, contextId, clientId);

        if (inserted) {
            CredentialStorage credentialStorage = optCredentialStorage();
            if (null != credentialStorage) {
                try {
                    credentialStorage.storeCredentials(new DefaultCredentials(session));
                } catch (Exception e) {
                    LOG.error("Failed to store credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId), e);
                }
            }

            PushUser pushUser = new PushUser(userId, contextId);
            for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
                PushManagerService pushManager = pushManagersIterator.next();
                try {
                    if (pushManager instanceof PushManagerExtendedService) {
                        PushListener pl = ((PushManagerExtendedService) pushManager).startPermanentListener(pushUser);
                        if (null != pl) {
                            LOG.debug("Started permanent push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager);
                        }
                    }
                } catch (OXException e) {
                    LOG.error("Error while starting permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                } catch (RuntimeException e) {
                    LOG.error("Runtime error while starting permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                }
            }
        }

        return inserted;
    }

    @Override
    public boolean unregisterPermanentListenerFor(Session session, String clientId) throws OXException {
        if (!PushUtility.allowedClient(clientId)) {
            /*
             * No permanent push listener for the client.
             */
            return false;
        }

        int contextId = session.getContextId();
        int userId = session.getUserId();

        DeleteResult deleteResult = PushDbUtils.deletePushRegistration(userId, contextId, clientId);
        if (DeleteResult.DELETED_COMPLETELY == deleteResult) {
            CredentialStorage credentialStorage = optCredentialStorage();
            if (null != credentialStorage) {
                try {
                    credentialStorage.deleteCredentials(userId, contextId);
                } catch (Exception e) {
                    LOG.error("Failed to delete credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId), e);
                }
            }

            PushUser pushUser = new PushUser(userId, contextId);
            for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
                PushManagerService pushManager = pushManagersIterator.next();
                try {
                    if (pushManager instanceof PushManagerExtendedService) {
                        // Stop listener for session
                        boolean stopped = ((PushManagerExtendedService) pushManager).stopPermanentListener(pushUser, true);
                        if (stopped) {
                            LOG.debug("Stopped push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager);
                        }
                    }
                } catch (OXException e) {
                    LOG.error("Error while stopping push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                } catch (RuntimeException e) {
                    LOG.error("Runtime error while stopping push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                }
            }
        }

        return (DeleteResult.NOT_DELETED != deleteResult);
    }

    @Override
    public List<PushUser> getUsersWithPermanentListeners() throws OXException {
        return PushDbUtils.getPushRegistrations();
    }

    @Override
    public boolean hasRegistration(PushUser pushUser) throws OXException {
        return PushDbUtils.hasPushRegistration(pushUser);
    }

    @Override
    public Session generateSessionFor(PushUser pushUser) throws OXException {
        // Check push user
        if (false == PushDbUtils.hasPushRegistration(pushUser)) {
            throw PushExceptionCodes.NO_PUSH_REGISTRATION.create(Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()));
        }

        int contextId = pushUser.getContextId();
        int userId = pushUser.getUserId();

        // Generate session instance
        GeneratedSession session = new GeneratedSession(userId, contextId);

        // Get credentials
        {
            Credentials credentials = optCredentials(userId, contextId);
            if (null != credentials) {
                session.setPassword(credentials.getPassword());
                session.setLoginName(credentials.getLogin());
            }
        }

        // Password
        {
            PasswordSource passwordSource = MailProperties.getInstance().getPasswordSource();
            switch (passwordSource) {
                case GLOBAL: {
                    // Just for convenience
                    String masterPassword = MailProperties.getInstance().getMasterPassword();
                    if (null == masterPassword) {
                        throw PushExceptionCodes.MISSING_MASTER_PASSWORD.create();
                    }
                    session.setPassword(masterPassword);
                    break;
                }
                case SESSION:
                    // Fall-through
                default: {
                    if (null == session.getPassword()) {
                        throw PushExceptionCodes.MISSING_PASSWORD.create();
                    }
                    break;
                }
            }
        }

        // Login
        {
            String proxyDelimiter = MailProperties.getInstance().getAuthProxyDelimiter();
            if (null != proxyDelimiter && null == session.getLoginName()) {
                // Login cannot be determined
                throw PushExceptionCodes.MISSING_LOGIN_STRING.create();
            }
        }

        return session;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    @Override
    public PushListener startListenerFor(Session session) {
        /*
         * Check session's client identifier
         */
        if (!PushUtility.allowedClient(session.getClient())) {
            /*
             * No push listener for the client associated with current session.
             */
            return null;
        }
        /*
         * Iterate push managers
         */
        for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
            try {
                PushManagerService pushManager = pushManagersIterator.next();
                // Initialize a new push listener for session
                PushListener pl = pushManager.startListener(session);
                if (null != pl) {
                    LOG.debug("Started push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), pushManager);
                    return pl;
                }
            } catch (OXException e) {
                LOG.error("Error while starting push listener.", e);
            } catch (RuntimeException e) {
                LOG.error("Runtime error while starting push listener.", e);
            }
        }
        return null;
    }

    @Override
    public boolean stopListenerFor(Session session) {
        if (!PushUtility.allowedClient(session.getClient())) {
            /*
             * No push listener for the client associated with current session.
             */
            return false;
        }
        /*
         * Iterate push managers
         */
        for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
            try {
                PushManagerService pushManager = pushManagersIterator.next();
                // Stop listener for session
                boolean stopped = pushManager.stopListener(session);
                if (stopped) {
                    LOG.debug("Stopped push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), pushManager);
                    return true;
                }
            } catch (OXException e) {
                LOG.error("Error while stopping push listener.", e);
            } catch (RuntimeException e) {
                LOG.error("Runtime error while stopping push listener.", e);
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    /**
     * Adds specified push manager service.
     *
     * @param pushManager The push manager service to add
     * @return <code>true</code> if push manager service could be successfully added; otherwise <code>false</code>
     */
    public boolean addPushManager(PushManagerService pushManager) {
        boolean added = (null == map.putIfAbsent(pushManager.getClass(), pushManager));

        if (added && (pushManager instanceof PushManagerExtendedService)) {
            synchronized (this) {
                startPermanentListenersFor(initialPushUsers, (PushManagerExtendedService) pushManager);
            }
        }

        return added;
    }

    /**
     * Removes specified push manager service.
     *
     * @param pushManager The push manager service to remove
     */
    public void removePushManager(PushManagerService pushManager) {
        map.remove(pushManager.getClass());
    }

    /**
     * Gets a read-only {@link Iterator iterator} over the push managers in this registry.
     * <p>
     * Invoking {@link Iterator#remove() remove} will throw an {@link UnsupportedOperationException}.
     *
     * @return A read-only {@link Iterator iterator} over the push managers in this registry.
     */
    public Iterator<PushManagerService> getPushManagers() {
        return unmodifiableIterator(map.values().iterator());
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    /**
     * Strips the <tt>remove()</tt> functionality from an existing iterator.
     * <p>
     * Wraps the supplied iterator into a new one that will always throw an <tt>UnsupportedOperationException</tt> if its <tt>remove()</tt>
     * method is called.
     *
     * @param iterator The iterator to turn into an unmodifiable iterator.
     * @return An iterator with no remove functionality.
     */
    private static <T> Iterator<T> unmodifiableIterator(final Iterator<T> iterator) {
        if (iterator == null) {
            @SuppressWarnings("unchecked") final Iterator<T> empty = EMPTY_ITER;
            return empty;
        }

        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private static final Iterator EMPTY_ITER = new Iterator() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

}
