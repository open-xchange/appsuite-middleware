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

package com.openexchange.push.dovecot;

import static com.openexchange.java.Autoboxing.I;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushListener;
import com.openexchange.push.dovecot.registration.RegistrationContext;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.UserExceptionCode;
import com.openexchange.user.UserService;

/**
 * {@link AbstractDovecotPushListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public abstract class AbstractDovecotPushListener implements PushListener {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractDovecotPushListener.class);
    }

    /** The currently registered registration performer */
    protected static final AtomicReference<RegistrationPerformer> REGISTRATION_PERFORMER_REFERENCE = new AtomicReference<RegistrationPerformer>();

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

    protected final boolean permanent;
    protected final RegistrationContext registrationContext;
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractDovecotPushListener}.
     *
     * @param registrationContext The registration context
     * @param permanent <code>true</code> if associated with a permanent listener; otherwise <code>false</code>
     * @param services The OSGi service look-up
     */
    protected AbstractDovecotPushListener(RegistrationContext registrationContext, boolean permanent, ServiceLookup services) {
        super();
        this.permanent = permanent;
        this.registrationContext = registrationContext;
        this.services = services;
    }

    /**
     * Checks of listener-associated user is valid.
     *
     * @param checkContext Whether to check context, too
     * @return <code>true</code> if user is valid; otherwise <code>false</code>
     */
    protected boolean isUserValid() {
        try {
            UserService userService = services.getService(UserService.class);
            return userService.getUser(registrationContext.getUserId(), registrationContext.getContextId()).isMailEnabled();
        } catch (OXException e) {
            if (!UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                LoggerHolder.LOG.error("Failed loading user {} in context {}", I(registrationContext.getUserId()), I(registrationContext.getContextId()), e);
            }
            return false;
        }
    }

    /**
     * Gets the registration context
     *
     * @return The registration context
     */
    public RegistrationContext getRegistrationContext() {
        return registrationContext;
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
    public final void notifyNewMail() {
        // Do nothing as we notify on incoming push event
    }

    /**
     * Initializes registration for this listener.
     *
     * @param initiateLockRefresher Whether to initiate refreshing acquired lock
     * @return A reason string in case registration failed; otherwise <code>null</code> on success
     * @throws OXException If registration failed hard
     */
    public abstract String initateRegistration(boolean initiateLockRefresher) throws OXException;

    /**
     * Unregisters this listeners.
     *
     * @param tryToReconnect Whether to try to reconnect this listener; otherwise <code>false</code>
     * @param optionalOldSession The optional old session
     * @return An optional clean-up task or <code>null</code>
     * @throws OXException If unregistration fails
     */
    public abstract Runnable unregister(boolean tryToReconnect, Optional<Session> optionalOldSession) throws OXException;

    // -------------------------------------------------------------------------------------------------------------------------------

    /** A task that re-invokes <code>initateRegistration()</code> method */
    public class RetryRunnable implements Runnable {

        private final boolean initiateLockRefresher;
        private final String logInfo;
        private final Logger logger;

        /**
         * Initializes a new {@link RetryRunnable}.
         *
         * @param initiateLockRefresher Whether to initiate refreshing acquired lock
         * @param logInfo The log information
         * @param logger The logger to use
         */
        public RetryRunnable(boolean initiateLockRefresher, String logInfo, org.slf4j.Logger logger) {
            super();
            this.initiateLockRefresher = initiateLockRefresher;
            this.logInfo = logInfo;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                initateRegistration(initiateLockRefresher);
            } catch (Exception e) {
                if (null == logInfo) {
                    logger.error("Failed to initiate Dovecot Push registration for user {} in context {}", Integer.valueOf(registrationContext.getUserId()), Integer.valueOf(registrationContext.getContextId()), e);
                } else {
                    logger.error("Failed to initiate Dovecot Push registration for {} (user={}, context={})", logInfo, Integer.valueOf(registrationContext.getUserId()), Integer.valueOf(registrationContext.getContextId()), e);
                }
            }
        }
    } // End of class RetryRunnable

}
