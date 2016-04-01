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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.mail.api.MailConfig.PasswordSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUserClient;
import com.openexchange.push.PushUserInfo;
import com.openexchange.push.PushUtility;
import com.openexchange.push.credstorage.CredentialStorage;
import com.openexchange.push.credstorage.CredentialStorageProvider;
import com.openexchange.push.credstorage.Credentials;
import com.openexchange.push.credstorage.DefaultCredentials;
import com.openexchange.push.impl.PushDbUtils.DeleteResult;
import com.openexchange.push.impl.balancing.reschedulerpolicy.PermanentListenerRescheduler;
import com.openexchange.push.impl.osgi.Services;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

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
    private final AtomicReference<PermanentListenerRescheduler> reschedulerRef;

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
        reschedulerRef = new AtomicReference<PermanentListenerRescheduler>();
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
     * Gets the rescheduler instance
     *
     * @return The rescheduler instance or <code>null</code>
     */
    public PermanentListenerRescheduler getRescheduler() {
        return reschedulerRef.get();
    }

    /**
     * Sets the rescheduler instance
     *
     * @param rescheduler The rescheduler instance
     */
    public void setRescheduler(PermanentListenerRescheduler rescheduler) {
        reschedulerRef.set(rescheduler);
    }

    /**
     * Checks if permanent push is allowed as per configuration
     *
     * @return <code>true</code> if allowed; otherwise <code>false</code> if disabled
     */
    public boolean isPermanentPushAllowed() {
        boolean defaultValue = true;
        ConfigurationService service = Services.optService(ConfigurationService.class);
        return null == service ? defaultValue : service.getBoolProperty("com.openexchange.push.allowPermanentPush", defaultValue);
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
     * Lists currently running push users.
     *
     * @return The push users
     */
    public List<PushUserInfo> listPushUsers() {
        Set<PushUserInfo> pushUsers = listPushUsers0();
        List<PushUserInfo> list = new ArrayList<PushUserInfo>(pushUsers);
        Collections.sort(list);
        return list;
    }

    /**
     * Lists currently running permanent push users.
     *
     * @return The push users
     */
    private Set<PushUserInfo> listPushUsers0() {
        Set<PushUserInfo> pushUsers = new HashSet<PushUserInfo>(256);

        for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
            PushManagerService pushManager = pushManagersIterator.next();
            if (pushManager instanceof PushManagerExtendedService) {
                try {
                    pushUsers.addAll(((PushManagerExtendedService) pushManager).getAvailablePushUsers());
                } catch (Exception e) {
                    LOG.error("Failed to determine available push users from push manager \"{}\".", pushManager, e);
                }
            }
        }

        return pushUsers;
    }

    /**
     * Lists registered push users.
     *
     * @return The push users
     * @throws OXException If registered push users cannot be returned
     */
    public List<PushUserClient> listRegisteredPushUsers() throws OXException {
        return PushDbUtils.getPushClientRegistrations();
    }


    // --------------------------------- The central start & stop routines for permanent listeners --------------------------------------

    /**
     * Starts a permanent listener for given push users.
     *
     * @param pushUsers The push users
     * @param extendedService The associated extended push manager
     * @param allowPermanentPush Whether permanent push is allowed at all
     * @return The actually started ones
     */
    private List<PushUser> startPermanentListenersFor(Collection<PushUser> pushUsers, PushManagerExtendedService extendedService, boolean allowPermanentPush) {
        // Always called when holding synchronized lock
        List<PushUser> startedOnes = new LinkedList<PushUser>();
        if (allowPermanentPush && extendedService.supportsPermanentListeners()) {
            TIntSet blockedContexts = new TIntHashSet(pushUsers.size());
            for (PushUser pushUser : pushUsers) {
                int contextId = pushUser.getContextId();
                int userId = pushUser.getUserId();

                // Start permanent listener for current push user
                int retry = 2;
                while (retry-- > 0) {
                    try {
                        if (blockedContexts.contains(contextId) || schemaBeingLockedOrNeedsUpdate(contextId)) {
                            blockedContexts.add(contextId);
                            retry = 0;
                            LOG.info("Database schema is locked or needs update. Denied start-up of permanent push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(userId), Integer.valueOf(contextId), extendedService);

                            DatabaseService dbService = services.getOptionalService(DatabaseService.class);
                            if (null != dbService) {
                                try {
                                    for (int contextInSameSchema : dbService.getContextsInSameSchema(contextId)) {
                                        blockedContexts.add(contextInSameSchema);
                                    }
                                } catch (Exception e) {
                                    // Ignore
                                }
                            }
                        } else {
                            PushListener pl = extendedService.startPermanentListener(pushUser);
                            retry = 0;
                            if (null != pl) {
                                LOG.debug("Started permanent push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(userId), Integer.valueOf(contextId), extendedService);
                                startedOnes.add(pushUser);
                            }
                        }
                    } catch (OXException e) {
                        if (PushExceptionCodes.AUTHENTICATION_ERROR.equals(e) || PushExceptionCodes.MISSING_PASSWORD.equals(e)) {
                            handleInvalidCredentials(pushUser, true, e);
                        } else {
                            retry = 0;
                            LOG.error("Error while starting permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), extendedService, e);
                        }
                    } catch (RuntimeException e) {
                        retry = 0;
                        LOG.error("Runtime error while starting permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), extendedService, e);
                    }
                }
            }
        }
        Collections.sort(startedOnes);
        return startedOnes;
    }

    private void handleInvalidCredentials(PushUser pushUser, boolean tryRestore, OXException e) {
        CredentialStorage credentialStorage;
        try {
            credentialStorage = optCredentialStorage();
        } catch (Exception x) {
            LOG.debug("Failed to acquire credentials storage for push user {} in context {}.", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), e);
            return;
        }
        if (null != credentialStorage) {
            try {
                Session session = tryRestore ? new SessionLookUpUtility(this, services).lookUpSessionFor(pushUser, false, true) : null;
                if (null == session) {
                    credentialStorage.deleteCredentials(pushUser.getUserId(), pushUser.getContextId());
                } else {
                    credentialStorage.storeCredentials(new DefaultCredentials(session));
                }
            } catch (Exception x) {
                LOG.warn("Failed to {} credentials for push user {} in context {}.", tryRestore ? "restore" : "delete", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), e);
            }
        }
    }

    private boolean schemaBeingLockedOrNeedsUpdate(int contextId) throws OXException {
        Updater updater;
        try {
            updater = Updater.getInstance();
            UpdateStatus status = updater.getStatus(contextId);
            if (status.blockingUpdatesRunning()) {
                LOG.info("Another database update process is already running");
                return true;
            }

            // We only reach this point, if no other thread is already locking us
            if (!status.needsBlockingUpdates()) {
                return false;
            }

            // We reach this point, we must return true
            return true;
        } catch (OXException e) {
            if (e.getCode() == 102) {
                // NOTE: this situation should not happen!
                // it can only happen, when a schema has not been initialized correctly!
                LOG.debug("FATAL: this error must not happen",e);
            }
            LOG.error("Error in checking/updating schema",e);
            throw e;
        }
    }

    /**
     * Stops a permanent listener for given push user.
     *
     * @param pushUser The push user to stop
     * @param extendedService The associated extended push manager
     * @param tryToReconnect Whether a reconnect attempt is supposed to be performed
     * @return <code>true</code> if permanent listener has been successfully stopped; otherwise <code>false</code>
     * @throws OXException If stop attempt fails
     */
    private boolean stopPermanentListenerFor(PushUser pushUser, PushManagerExtendedService extendedService, boolean tryToReconnect) throws OXException {
        // Always called when holding synchronized lock
        try {
            return extendedService.stopPermanentListener(pushUser, tryToReconnect);
        } catch (OXException e) {
            if (PushExceptionCodes.AUTHENTICATION_ERROR.equals(e)) {
                handleInvalidCredentials(pushUser, false, e);
            }
            throw e;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Starts the permanent listeners for given push users.
     *
     * @param pushUsers The push users
     * @param parkNanos The number of nanoseconds to wait prior to starting listeners
     * @return The actually started ones
     */
    public List<PushUser> applyInitialListeners(List<PushUser> pushUsers, long parkNanos) {
        synchronized (this) {
            Collection<PushUser> toStop;
            {
                Set<PushUser> current = new HashSet<PushUser>(initialPushUsers);
                current.removeAll(pushUsers);
                toStop = current;
            }

            Collection<PushUser> toStart = new LinkedList<PushUser>();
            for (PushUser pushUser : pushUsers) {
                if (initialPushUsers.add(pushUser)) {
                    toStart.add(pushUser);
                }
            }

            initialPushUsers.removeAll(toStop);

            boolean nothingToStop = toStop.isEmpty();
            if (nothingToStop && toStart.isEmpty()) {
                // Nothing to do
                return Collections.emptyList();
            }

            // Determine currently available push managers
            List<PushManagerService> managers = new LinkedList<PushManagerService>(map.values());

            // Stop permanent candidates (release acquired resources, etc.)
            if (false == nothingToStop) {
                for (PushUser pushUser : toStop) {
                    for (PushManagerService pushManager : managers) {
                        if (pushManager instanceof PushManagerExtendedService) {
                            try {
                                boolean stopped = stopPermanentListenerFor(pushUser, (PushManagerExtendedService) pushManager, false);
                                if (stopped) {
                                    LOG.debug("Stopped permanent push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), pushManager);
                                }
                            } catch (OXException e) {
                                if (PushExceptionCodes.AUTHENTICATION_ERROR.equals(e)) {
                                    handleInvalidCredentials(pushUser, true, e);
                                }
                                LOG.error("Error while stopping permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), pushManager, e);
                            } catch (RuntimeException e) {
                                LOG.error("Runtime error while stopping permanent push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(pushUser.getUserId()), Integer.valueOf(pushUser.getContextId()), pushManager, e);
                            }
                        }
                    }
                }
            }

            // Park a while
            if (parkNanos > 0L) {
                LockSupport.parkNanos(parkNanos);
            }

            // Start permanent candidates
            List<PushUser> startedOnes = new LinkedList<PushUser>();
            boolean allowPermanentPush = isPermanentPushAllowed();
            for (PushManagerService pushManager : managers) {
                if (pushManager instanceof PushManagerExtendedService) {
                    List<PushUser> started = startPermanentListenersFor(toStart, (PushManagerExtendedService) pushManager, allowPermanentPush);
                    startedOnes.addAll(started);
                }
            }
            Collections.sort(startedOnes);
            return startedOnes;
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

        synchronized (this) {
            int contextId = session.getContextId();
            int userId = session.getUserId();

            boolean inserted = PushDbUtils.insertPushRegistration(userId, contextId, clientId);

            if (inserted) {
                // Not registered
                CredentialStorage credentialStorage = optCredentialStorage();
                if (null != credentialStorage) {
                    try {
                        credentialStorage.storeCredentials(new DefaultCredentials(session));
                        LOG.info("Successfully stored/updated credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId));
                    } catch (Exception e) {
                        LOG.error("Failed to store credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId), e);
                    }
                }

                // Start for push user
                boolean allowPermanentPush = isPermanentPushAllowed();
                Collection<PushUser> toStart = Collections.singletonList(new PushUser(userId, contextId));
                for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
                    PushManagerService pushManager = pushManagersIterator.next();
                    if (pushManager instanceof PushManagerExtendedService) {
                        PushManagerExtendedService extendedService = (PushManagerExtendedService) pushManager;
                        PermanentListenerRescheduler rescheduler = reschedulerRef.get();
                        if (null == rescheduler) {
                            startPermanentListenersFor(toStart, extendedService, allowPermanentPush);
                        } else {
                            if (extendedService.supportsPermanentListeners()) {
                                try {
                                    rescheduler.planReschedule(true);
                                } catch (OXException e) {
                                    LOG.error("Failed to plan rescheduling", e);
                                }
                            }
                        }

                    }
                }
            } else {
                // Already registered a permanent listener for the client
                CredentialStorage credentialStorage = optCredentialStorage();
                if (null != credentialStorage) {
                    try {
                        if (null == credentialStorage.getCredentials(userId, contextId)) {
                            // No credentials stored, yet
                            credentialStorage.storeCredentials(new DefaultCredentials(session));
                        }
                    } catch (OXException e) {
                        LOG.error("Failed to check credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId), e);
                    }
                }
            }

            return inserted;
        }
    }

    @Override
    public boolean unregisterPermanentListenerFor(Session session, String clientId) throws OXException {
        return unregisterPermanentListenerFor(session.getUserId(), session.getContextId(), clientId);
    }

    /**
     * Unregisters the permanent listener for specified push user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param clientId The client identifier
     * @return <code>true</code> on successful unregistration; otherwise <code>false</code>
     * @throws OXException If unregistration fails
     */
    public boolean unregisterPermanentListenerFor(int userId, int contextId, String clientId) throws OXException {
        if (!PushUtility.allowedClient(clientId)) {
            /*
             * No permanent push listener for the client.
             */
            return false;
        }

        synchronized (this) {
            DeleteResult deleteResult = PushDbUtils.deletePushRegistration(userId, contextId, clientId);
            if (DeleteResult.DELETED_COMPLETELY == deleteResult) {
                CredentialStorage credentialStorage = optCredentialStorage();
                if (null != credentialStorage) {
                    try {
                        credentialStorage.deleteCredentials(userId, contextId);
                        LOG.info("Successfully deleted credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId));
                    } catch (Exception e) {
                        LOG.error("Failed to delete credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId), e);
                    }
                }

                PushUser pushUser = new PushUser(userId, contextId);
                for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
                    PushManagerService pushManager = pushManagersIterator.next();
                    if (pushManager instanceof PushManagerExtendedService) {
                        try {
                            // Stop listener for session
                            boolean stopped = stopPermanentListenerFor(pushUser, (PushManagerExtendedService) pushManager, true);
                            if (stopped) {
                                LOG.debug("Stopped push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager);
                            }
                        } catch (OXException e) {
                            LOG.error("Error while stopping push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                        } catch (RuntimeException e) {
                            LOG.error("Runtime error while stopping push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                        }
                    }
                }
            }
            return (DeleteResult.NOT_DELETED != deleteResult);
        }
    }

    /**
     * Unregisters all permanent listeners for specified push user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> on successful unregistration; otherwise <code>false</code>
     * @throws OXException If unregistration fails
     */
    public boolean unregisterAllPermanentListenersFor(int userId, int contextId) throws OXException {
        synchronized (this) {
            DeleteResult deleteResult = PushDbUtils.deleteAllPushRegistrations(userId, contextId);
            if (DeleteResult.DELETED_COMPLETELY == deleteResult) {
                CredentialStorage credentialStorage = optCredentialStorage();
                if (null != credentialStorage) {
                    try {
                        credentialStorage.deleteCredentials(userId, contextId);
                        LOG.info("Successfully deleted credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId));
                    } catch (Exception e) {
                        LOG.error("Failed to delete credentials for push user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId), e);
                    }
                }

            }

            PushUser pushUser = new PushUser(userId, contextId);
            for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
                PushManagerService pushManager = pushManagersIterator.next();
                if (pushManager instanceof PushManagerExtendedService) {
                    try {
                        // Stop listener for specified push user
                        boolean stopped = stopPermanentListenerFor(pushUser, (PushManagerExtendedService) pushManager, true);
                        if (stopped) {
                            LOG.debug("Stopped push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager);
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
    }

    /**
     * Stops the permanent listener for specified push users
     *
     * @param pushUsers The push users
     */
    public void stopPermanentListenerFor(Collection<PushUser> pushUsers) {
        synchronized (this) {
            for (PushUser pushUser : pushUsers) {
                int userId = pushUser.getUserId();
                int contextId = pushUser.getContextId();
                for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
                    PushManagerService pushManager = pushManagersIterator.next();
                    if (pushManager instanceof PushManagerExtendedService) {
                        try {
                            // Stop listener for specified push user
                            boolean stopped = stopPermanentListenerFor(pushUser, (PushManagerExtendedService) pushManager, true);
                            if (stopped) {
                                LOG.debug("Stopped push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager);
                            }
                        } catch (OXException e) {
                            LOG.error("Error while stopping push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                        } catch (RuntimeException e) {
                            LOG.error("Runtime error while stopping push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Stops all permanent listeners.
     */
    public void stopAllPermanentListener() {
        synchronized (this) {
            for (Iterator<PushManagerService> pushManagersIterator = map.values().iterator(); pushManagersIterator.hasNext();) {
                PushManagerService pushManager = pushManagersIterator.next();
                if (pushManager instanceof PushManagerExtendedService) {
                    PushManagerExtendedService extendedService = (PushManagerExtendedService) pushManager;

                    // Determine current push manager's listeners
                    List<PushUserInfo> availablePushUsers;
                    try {
                        availablePushUsers = extendedService.getAvailablePushUsers();
                    } catch (OXException e) {
                        LOG.error("Error while determining available push users by push manager \"{}\".", pushManager, e);
                        availablePushUsers = Collections.emptyList();
                    }

                    // Stop the permanent ones
                    for (PushUserInfo pushUserInfo : availablePushUsers) {
                        if (pushUserInfo.isPermanent()) {
                            int userId = pushUserInfo.getUserId();
                            int contextId = pushUserInfo.getContextId();
                            try {
                                // Stop listener for session
                                boolean stopped = stopPermanentListenerFor(pushUserInfo.getPushUser(), extendedService, true);
                                if (stopped) {
                                    LOG.debug("Stopped push listener for user {} in context {} by push manager \"{}\"", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager);
                                }
                            } catch (OXException e) {
                                LOG.error("Error while stopping push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                            } catch (RuntimeException e) {
                                LOG.error("Runtime error while stopping push listener for user {} in context {} by push manager \"{}\".", Integer.valueOf(userId), Integer.valueOf(contextId), pushManager, e);
                            }
                        }
                    }
                }
            }
        }
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
                startPermanentListenersFor(initialPushUsers, (PushManagerExtendedService) pushManager, isPermanentPushAllowed());
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
