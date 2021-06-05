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

package com.openexchange.push.dovecot.stateless;

import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.dovecot.AbstractDovecotPushListener;
import com.openexchange.push.dovecot.registration.RegistrationContext;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.registration.RegistrationResult;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;

/**
 * {@link StatelessDovecotPushListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class StatelessDovecotPushListener extends AbstractDovecotPushListener {

    /** The logger */
    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StatelessDovecotPushListener.class);

    /**
     * Initializes a new {@link StatelessDovecotPushListener}.
     *
     * @param registrationContext The registration context
     * @param permanent <code>true</code> if associated with a permanent listener; otherwise <code>false</code>
     * @param services The OSGi service look-up
     */
    public StatelessDovecotPushListener(RegistrationContext registrationContext, boolean permanent, ServiceLookup services) {
        super(registrationContext, permanent, services);
    }

    @Override
    public synchronized String initateRegistration(boolean initiateLockRefresher) throws OXException {
        if (false == registrationContext.hasWebMailAndIsActive()) {
            StringBuilder sb = new StringBuilder("Denied start of a ").append(permanent ? "permanent" : "session-bound").append(" push listener for user ").append(registrationContext.getUserId());
            sb.append(" in context ").append(registrationContext.getContextId()).append(": Missing \"webmail\" permission or user is disabled.");
            return sb.toString();
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
                if (!isUserValid()) {
                    scheduleRetry = false;
                    logInfo = registrationResult.getLogInfo();
                    throw registrationResult.getException();
                }
                scheduleRetry = registrationResult.scheduleRetry();
                logInfo = registrationResult.getLogInfo();
                throw registrationResult.getException();
            }

            return null;
        } catch (RuntimeException rte) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
        } finally {
            if (scheduleRetry) {
                TimerService timerService = services.getOptionalService(TimerService.class);
                if (null == timerService) {
                    throw ServiceExceptionCode.absentService(TimerService.class);
                }
                long delay = 5000L;
                timerService.schedule(new RetryRunnable(initiateLockRefresher, logInfo, LOGGER), delay);
            }
        }
    }

    @Override
    public synchronized Runnable unregister(boolean tryToReconnect, Optional<Session> optionalOldSession) throws OXException {
        RegistrationPerformer registrationPerformer = REGISTRATION_PERFORMER_REFERENCE.get();
        registrationPerformer.unregister(registrationContext);
        return null;
    }

}
