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

package com.openexchange.push.dovecot.stateless;

import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.dovecot.AbstractDovecotPushListener;
import com.openexchange.push.dovecot.registration.RegistrationContext;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.registration.RegistrationResult;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
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
    public synchronized String initateRegistration() throws OXException {
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
                timerService.schedule(new RetryRunnable(logInfo, LOGGER), delay);
            }
        }
    }

    @Override
    public synchronized Runnable unregister(boolean tryToReconnect) throws OXException {
        RegistrationPerformer registrationPerformer = REGISTRATION_PERFORMER_REFERENCE.get();
        registrationPerformer.unregister(registrationContext);
        return null;
    }

}
