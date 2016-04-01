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

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.SessionInfo;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.registration.RegistrationResult;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link DovecotPushListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class DovecotPushListener implements PushListener, Runnable {

    /** The logger */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DovecotPushListener.class);

    /** The timeout threshold; cluster lock timeout minus one minute */
    private static final long TIMEOUT_THRESHOLD_MILLIS = DovecotPushClusterLock.TIMEOUT_MILLIS - 60000L;

    private static final AtomicReference<RegistrationPerformer> REGISTRATION_PERFORMER_REFERENCE = new AtomicReference<RegistrationPerformer>();

    /**
     * Sets the registration performer to use.
     *
     * @param performer The performer
     * @return The new unused performer after this call or <code>null</code> if there was none
     */
    public static RegistrationPerformer setIfHigherRanked(RegistrationPerformer performer) {
        RegistrationPerformer current;
        do {
            current = REGISTRATION_PERFORMER_REFERENCE.get();
            if (null != current && current.getRanking() >= performer.getRanking()) {
                return performer;
            }
        } while (!REGISTRATION_PERFORMER_REFERENCE.compareAndSet(current, performer));
        return current;
    }

    /**
     * Replaces currently active registration performer with specified replacement
     *
     * @param toReplace The performer to replace
     * @param replacement The performer to use
     * @return <code>true</code> if replaced; otherwise <code>false</code>
     */
    public static boolean replaceIfActive(RegistrationPerformer toReplace, RegistrationPerformer replacement) {
        RegistrationPerformer current;
        do {
            current = REGISTRATION_PERFORMER_REFERENCE.get();
            if (null != current && !current.equals(toReplace)) {
                return false;
            }
        } while (!REGISTRATION_PERFORMER_REFERENCE.compareAndSet(current, replacement));
        return true;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final String authPassword;
    private final String authLogin;
    private final URI uri;
    private final boolean permanent;
    private final Session session;
    private final ServiceLookup services;
    private final DovecotPushManagerService pushManager;

    private boolean initialized;
    private ScheduledTimerTask refreshLockTask;
    private ScheduledTimerTask retryTask;

    /**
     * Initializes a new {@link DovecotPushListener}.
     *
     * @param uri The URL end-point
     * @param authLogin The option login
     * @param authPassword The optional password
     * @param session The session
     * @param permanent <code>true</code> if associated with a permanent listener; otherwise <code>false</code>
     * @param pushManager The Dovecot push manager instance
     * @param services The OSGi service look-up
     */
    public DovecotPushListener(URI uri, final String authLogin, final String authPassword, Session session, boolean permanent, DovecotPushManagerService pushManager, ServiceLookup services) {
        super();
        this.uri = uri;
        this.authLogin = authLogin;
        this.authPassword = authPassword;
        this.pushManager = pushManager;
        this.permanent = permanent;
        this.session = session;
        this.services = services;
    }

    private boolean isUserValid() {
        try {
            ContextService contextService = services.getService(ContextService.class);
            Context context = contextService.loadContext(session.getContextId());
            if (!context.isEnabled()) {
                return false;
            }

            UserService userService = services.getService(UserService.class);
            User user = userService.getUser(session.getUserId(), context);
            return user.isMailEnabled();
        } catch (OXException e) {
            return false;
        }
    }

    @Override
    public void run() {
        try {
            if (!isUserValid()) {
                unregister(false);
                return;
            }

            pushManager.refreshLock(new SessionInfo(session, permanent));
        } catch (Exception e) {
            LOGGER.warn("Failed to refresh lock for user {} in context {}", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the permanent flag
     *
     * @return The permanent flag
     */
    public boolean isPermanent() {
        return permanent;
    }

    @Override
    public void notifyNewMail() throws OXException {
        // Do nothing as we notify on incoming push event
    }

    /**
     * Initializes registration for this listener.
     *
     * @return A reason string in case registration failed; otherwise <code>null</code> on success
     * @throws OXException If registration failed hard
     */
    public synchronized String initateRegistration() throws OXException {
        if (initialized) {
            // Already initialized
            return null;
        }

        TimerService timerService = services.getOptionalService(TimerService.class);
        if (null == timerService) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }

        boolean scheduleRetry = false;
        String logInfo = null;
        try {
            RegistrationPerformer performer = REGISTRATION_PERFORMER_REFERENCE.get();
            RegistrationResult registrationResult = performer.initateRegistration(session);
            if (registrationResult.isDenied()) {
                return registrationResult.getReason();
            }
            if (registrationResult.isFailed()) {
                scheduleRetry = registrationResult.scheduleRetry();
                logInfo = registrationResult.getLogInfo();
                throw registrationResult.getException();
            }

            // Otherwise success
            // Mark as initialized
            initialized = true;

            // Schedule to refresh cluster lock
            long delay = TIMEOUT_THRESHOLD_MILLIS;
            refreshLockTask = timerService.scheduleAtFixedRate(this, delay, delay);
            return null;
        } catch (RuntimeException rte) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
        } finally {
            if (scheduleRetry) {
                long delay = 5000L;
                retryTask = timerService.schedule(new RetryRunnable(logInfo), delay);
            }
        }
    }

    /**
     * Unregisters this listeners.
     *
     * @throws OXException If unregistration fails
     */
    public synchronized boolean unregister(boolean tryToReconnect) throws OXException {
        // Avoid subsequent initialization attempt
        initialized = true;

        // Cancel timer tasks
        {
            ScheduledTimerTask retryTask = this.retryTask;
            if (null != retryTask) {
                this.retryTask = null;
                retryTask.cancel();
            }

            ScheduledTimerTask refreshLockTask = this.refreshLockTask;
            if (null != refreshLockTask) {
                this.refreshLockTask = null;
                refreshLockTask.cancel();
            }
        }

        boolean reconnected = false;
        DovecotPushListener anotherListener = tryToReconnect ? pushManager.injectAnotherListenerFor(session) : null;
        if (null == anotherListener) {
            // No other listener available
            // Give up lock and return
            try {
                pushManager.releaseLock(new SessionInfo(session, permanent));
            } catch (Exception e) {
                LOGGER.warn("Failed to release lock for user {} in context {}.", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
            }
        } else {
            try {
                // No need to re-execute registration
                reconnected = true;
            } catch (Exception e) {
                LOGGER.warn("Failed to start new listener for user {} in context {}.", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                // Give up lock and return
                try {
                    pushManager.releaseLock(new SessionInfo(session, permanent));
                } catch (Exception x) {
                    LOGGER.warn("Failed to release DB lock for user {} in context {}.", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), x);
                }
            }
        }

        if (false == reconnected) {
            doUnregistration();
            // Mark as uninitialized
            initialized = false;
        }

        return reconnected;
    }

    private void doUnregistration() throws OXException {
        RegistrationPerformer registrationPerformer = REGISTRATION_PERFORMER_REFERENCE.get();
        registrationPerformer.unregister(session);
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private class RetryRunnable implements Runnable {

        private final String logInfo;

        RetryRunnable(String logInfo) {
            this.logInfo = logInfo;
        }

        @Override
        public void run() {
            try {
                initateRegistration();
            } catch (Exception e) {
                if (null == logInfo) {
                    LOGGER.error("Failed to initiate Dovecot Push registration for user {} in context {}", session.getUserId(), session.getContextId(), e);
                } else {
                    LOGGER.error("Failed to initiate Dovecot Push registration for {} (user={}, context={})", logInfo, session.getUserId(), session.getContextId(), e);
                }
            }
        }
    }

}
