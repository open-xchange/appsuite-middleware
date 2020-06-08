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

package com.openexchange.push.dovecot.stateful;

import static com.openexchange.java.Autoboxing.I;
import java.util.Optional;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.dovecot.AbstractDovecotPushListener;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.SessionInfo;
import com.openexchange.push.dovecot.registration.RegistrationContext;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.registration.RegistrationResult;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link DovecotPushListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class DovecotPushListener extends AbstractDovecotPushListener implements Runnable {

    /** The logger */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DovecotPushListener.class);

    /** The timeout threshold; cluster lock timeout minus one minute */
    private static final long TIMEOUT_THRESHOLD_MILLIS = DovecotPushClusterLock.TIMEOUT_MILLIS - 60000L;

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final DovecotPushManagerService pushManager;

    private boolean initialized;
    private ScheduledTimerTask refreshLockTask;
    private ScheduledTimerTask retryTask;

    /**
     * Initializes a new {@link DovecotPushListener}.
     *
     * @param registrationContext The registration context
     * @param permanent <code>true</code> if associated with a permanent listener; otherwise <code>false</code>
     * @param pushManager The Dovecot push manager instance
     * @param services The OSGi service look-up
     */
    public DovecotPushListener(RegistrationContext registrationContext, boolean permanent, DovecotPushManagerService pushManager, ServiceLookup services) {
        super(registrationContext, permanent, services);
        this.pushManager = pushManager;
    }

    @Override
    public void run() {
        try {
            if (!isUserValid()) {
                unregister(false, Optional.empty());
                return;
            }

            pushManager.refreshLock(new SessionInfo(registrationContext, permanent));
        } catch (Exception e) {
            LOGGER.warn("Failed to refresh lock for user {} in context {}", I(registrationContext.getUserId()), I(registrationContext.getContextId()), e);
        }
    }

    @Override
    public synchronized String initateRegistration() throws OXException {
        if (initialized) {
            // Already initialized
            return null;
        }

        if (false == registrationContext.hasWebMailAndIsActive()) {
            StringBuilder sb = new StringBuilder("Denied start of a ").append(permanent ? "permanent" : "session-bound").append(" push listener for user ").append(registrationContext.getUserId());
            sb.append(" in context ").append(registrationContext.getContextId()).append(": Missing \"webmail\" permission or user is disabled.");
            return sb.toString();
        }

        TimerService timerService = services.getOptionalService(TimerService.class);
        if (null == timerService) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }

        boolean scheduleRetry = false;
        String logInfo = null;
        try {
            RegistrationPerformer performer = REGISTRATION_PERFORMER_REFERENCE.get();
            RegistrationResult registrationResult = performer.initateRegistration(registrationContext);
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
                retryTask = timerService.schedule(new RetryRunnable(logInfo, LOGGER), delay);
            }
        }
    }

    @Override
    public synchronized Runnable unregister(boolean tryToReconnect, Optional<Session> optionalOldSession) throws OXException {
        // Avoid subsequent initialization attempt
        initialized = true;

        // Check if DoveAdm is used
        if (registrationContext.isDoveAdmBased()) {
            if (tryToReconnect) {
                if (permanent) {
                    // Not associated with a certain session. Keep as-is
                    return null;
                }

                // Check if there is still a valid push-capable session available
                Session session = pushManager.lookUpSessionFor(registrationContext.getUserId(), registrationContext.getContextId(), optionalOldSession.orElse(null));
                if (null != session) {
                    // Keep as-is
                    return null;
                }
            }

            // Cancel timer tasks
            {
                ScheduledTimerTask retryTask_tmp = this.retryTask;
                if (null != retryTask_tmp) {
                    this.retryTask = null;
                    retryTask_tmp.cancel();
                }

                ScheduledTimerTask refreshLockTask_tmp = this.refreshLockTask;
                if (null != refreshLockTask_tmp) {
                    this.refreshLockTask = null;
                    refreshLockTask_tmp.cancel();
                }
            }

            // Dispose...
            Runnable cleanUpTask = createCleanUpTask();
            doUnregistration();
            initialized = false;
            return cleanUpTask;
        }

        // Session-based...
        // Cancel timer tasks
        {
            ScheduledTimerTask retryTask_tmp = this.retryTask;
            if (null != retryTask_tmp) {
                this.retryTask = null;
                retryTask_tmp.cancel();
            }

            ScheduledTimerTask refreshLockTask_tmp = this.refreshLockTask;
            if (null != refreshLockTask_tmp) {
                this.refreshLockTask = null;
                refreshLockTask_tmp.cancel();
            }
        }

        Runnable cleanUpTask;
        DovecotPushListener anotherListener = tryToReconnect ? pushManager.injectAnotherListenerFor(registrationContext.getSession()) : null;
        if (null == anotherListener) {
            // No other listener available
            // Give up lock and return
            cleanUpTask = createCleanUpTask();
        } else {
            try {
                // No need to re-execute registration
                cleanUpTask = null;
            } catch (Exception e) {
                LOGGER.warn("Failed to start new listener for user {} in context {}.", Integer.valueOf(registrationContext.getUserId()), Integer.valueOf(registrationContext.getContextId()), e);
                // Give up lock and return
                cleanUpTask = createCleanUpTask();
            }
        }

        if (null != cleanUpTask) {
            doUnregistration();
            // Mark as uninitialized
            initialized = false;
        }

        return cleanUpTask;
    }

    private Runnable createCleanUpTask() {
        final RegistrationContext registrationContext_tmp = this.registrationContext;
        final boolean permanent_tmp = this.permanent;
        return new Runnable() {
            @Override
            public void run() {
                try {
                    pushManager.releaseLock(new SessionInfo(registrationContext_tmp, permanent_tmp));
                } catch (Exception e) {
                    LOGGER.warn("Failed to release lock for user {} in context {}.", Integer.valueOf(registrationContext_tmp.getUserId()), Integer.valueOf(registrationContext_tmp.getContextId()), e);
                }
            }
        };
    }

    private void doUnregistration() throws OXException {
        RegistrationPerformer registrationPerformer = REGISTRATION_PERFORMER_REFERENCE.get();
        registrationPerformer.unregister(registrationContext);
    }

}
