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

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import com.openexchange.sessiond.SessiondService;


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

        if (false == isDovecotPushEnabledFor(userId, contextId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Denied starting Dovecot listener for user {} in context {} with session {} ({}) since disabled via configuration", I(userId), I(contextId), session.getSessionID(), session.getClient(), new Throwable("Dovecot start listener trace"));
            } else {
                LOGGER.info("Denied starting Dovecot listener for user {} in context {} with session {} ({}) since disabled via configuration", I(userId), I(contextId), session.getSessionID(), session.getClient());
            }
            return null;
        }

        RegistrationContext registrationContext = getRegistrationContext(session);
        StatelessDovecotPushListener listener = new StatelessDovecotPushListener(registrationContext, false, services);
        String reason = listener.initateRegistration(true);
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
        listener.unregister(false, Optional.empty());
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

        if (false == isDovecotPushEnabledFor(userId, contextId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Denied starting permanent Dovecot listener for user {} in context {} since disabled via configuration", I(userId), I(contextId), new Throwable("Dovecot start permanent listener trace"));
            } else {
                LOGGER.info("Denied starting permanent Dovecot listener for user {} in context {} since disabled via configuration", I(userId), I(contextId));
            }
            return null;
        }

        RegistrationContext registrationContext = getRegistrationContext(pushUser);
        StatelessDovecotPushListener listener = new StatelessDovecotPushListener(registrationContext, false, services);
        String reason = listener.initateRegistration(true);
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
            listener.unregister(false, Optional.empty());
            return true;
        }

        if (hasPermanentPush(userId, contextId)) {
            // User has permanent push enabled. Leave...
            return false;
        }

        Session oldSession = null;
        if (pushUser.getIdOfIssuingSession().isPresent()) {
            oldSession = services.getServiceSafe(SessiondService.class).peekSession(pushUser.getIdOfIssuingSession().get(), false);
        }

        Session anotherSession = lookUpSessionFor(userId, contextId, oldSession);
        if (anotherSession != null) {
            // User has another push-capable session in cluster
            return false;
        }

        listener.unregister(false, Optional.empty());
        return true;
    }

    @Override
    public void unregisterForDeletedUser(PushUser pushUser) throws OXException {
        RegistrationContext registrationContext = getRegistrationContext(pushUser);
        StatelessDovecotPushListener listener = new StatelessDovecotPushListener(registrationContext, false, services);
        listener.unregister(false, Optional.empty());
    }

    @Override
    public List<PushUserInfo> getAvailablePushUsers() {
        return Collections.emptyList();
    }

}
