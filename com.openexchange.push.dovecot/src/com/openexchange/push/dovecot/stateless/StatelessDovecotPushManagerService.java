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

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUserInfo;
import com.openexchange.push.dovecot.AbstractDovecotPushManagerService;
import com.openexchange.push.dovecot.DovecotPushConfiguration;
import com.openexchange.push.dovecot.registration.RegistrationContext;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link StatelessDovecotPushManagerService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class StatelessDovecotPushManagerService extends AbstractDovecotPushManagerService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StatelessDovecotPushManagerService.class);

    /**
     * Initializes the push manager instance.
     *
     * @param config The push configuration
     * @param services The service look-up
     * @return The new instance
     * @throws OXException If initialization fails
     */
    public static StatelessDovecotPushManagerService newInstance(DovecotPushConfiguration config, ServiceLookup services) {
        StatelessDovecotPushManagerService newi = new StatelessDovecotPushManagerService(config, services);
        return newi;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link StatelessDovecotPushManagerService}.
     */
    private StatelessDovecotPushManagerService(DovecotPushConfiguration config, ServiceLookup services) {
        super(config, services);
    }

    @Override
    public PushListener startListener(Session session) throws OXException {
        if (null == session) {
            return null;
        }

        int contextId = session.getContextId();
        int userId = session.getUserId();
        RegistrationContext registrationContext = getRegistrationContext(session);
        StatelessDovecotPushListener listener = new StatelessDovecotPushListener(registrationContext, false, services);
        String reason = listener.initateRegistration();
        if (null == reason) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Started Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient(), new Throwable("Dovecot start listener trace"));
            } else {
                LOGGER.info("Started Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient());
            }
            return listener;
        }

        // Registration failed
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Could not register Dovecot listener for user {} in context {} with session {} ({}). Reason: {}", I(userId), I(contextId), session.getSessionID(), session.getClient(), reason, new Throwable("Dovecot start listener trace"));
        } else {
            LOGGER.info("Could not register Dovecot listener for user {} in context {} with session {} ({}). Reason: {}", I(userId), I(contextId), session.getSessionID(), session.getClient(), reason);
        }

        // No listener registered for given session
        return null;
    }

    @Override
    public boolean stopListener(Session session) throws OXException {
        if (null == session) {
            return false;
        }

        if (hasPermanentPush(session.getUserId(), session.getContextId())) {
            // User has permanent push enabled. Leave...
            return false;
        }

        Session anotherSession = lookUpSessionFor(session.getUserId(), session.getContextId(), session);
        if (anotherSession != null) {
            // User has another push-capable session in cluster
            return false;
        }

        RegistrationContext registrationContext = getRegistrationContext(session);
        StatelessDovecotPushListener listener = new StatelessDovecotPushListener(registrationContext, false, services);
        listener.unregister(false);
        return true;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    @Override
    public PushListener startPermanentListener(PushUser pushUser) throws OXException {
        if (null == pushUser) {
            return null;
        }

        int contextId = pushUser.getContextId();
        int userId = pushUser.getUserId();
        RegistrationContext registrationContext = getRegistrationContext(pushUser);
        StatelessDovecotPushListener listener = new StatelessDovecotPushListener(registrationContext, false, services);
        String reason = listener.initateRegistration();
        if (null == reason) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Started permanent Dovecot listener for user {} in context {}", I(userId), I(contextId), new Throwable("Dovecot start permanent listener trace"));
            } else {
                LOGGER.info("Started permanent Dovecot listener for user {} in context {}", I(userId), I(contextId));
            }
            return listener;
        }

        // Registration failed
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Could not register permanent Dovecot listener for user {} in context {}. Reason: {}", I(userId), I(contextId), reason, new Throwable("Dovecot start permanent listener trace"));
        } else {
            LOGGER.info("Could not register permanent Dovecot listener for user {} in context {}. Reason: {}", I(userId), I(contextId), reason);
        }

        // No listener registered
        return null;
    }

    @Override
    public boolean stopPermanentListener(PushUser pushUser, boolean tryToReconnect) throws OXException {
        if (null == pushUser) {
            return false;
        }

        int contextId = pushUser.getContextId();
        int userId = pushUser.getUserId();
        RegistrationContext registrationContext = getRegistrationContext(pushUser);
        StatelessDovecotPushListener listener = new StatelessDovecotPushListener(registrationContext, false, services);

        if (!tryToReconnect) {
            listener.unregister(false);
            return true;
        }

        if (hasPermanentPush(userId, contextId)) {
            // User has permanent push enabled. Leave...
            return false;
        }

        Session anotherSession = lookUpSessionFor(userId, contextId, null);
        if (anotherSession != null) {
            // User has another push-capable session in cluster
            return false;
        }

        listener.unregister(false);
        return true;
    }

    @Override
    public void unregisterForDeletedUser(PushUser pushUser) throws OXException {
        RegistrationContext registrationContext = getRegistrationContext(pushUser);
        StatelessDovecotPushListener listener = new StatelessDovecotPushListener(registrationContext, false, services);
        listener.unregister(false);
    }

    @Override
    public List<PushUserInfo> getAvailablePushUsers() {
        return Collections.emptyList();
    }

}
