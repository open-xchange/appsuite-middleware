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

package com.openexchange.multifactor;

import static com.openexchange.java.Autoboxing.L;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.listener.MultifactorListenerChain;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SessionModifyingMultifactorAuthenticator}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class SessionModifyingMultifactorAuthenticator extends MultifactorAuthenticator {

    private final ServerSession   session;
    private final SessiondService sessiondService;

    /**
     * Initializes a new {@link SessionModifyingMultifactorAuthenticator}.
     *
     * @param multifactorProvider instance to user for performing authenticaiton
     * @param session The server Session
     * @param sessiondService SessiondStorage to save/update session authentication status
     * @param lockoutService Lockout service to handle bad attempts
     * @param multifactorListenerChain  Chain of listeners for multifactor events
     */
    public SessionModifyingMultifactorAuthenticator(MultifactorProvider multifactorProvider,
        ServerSession session,
        SessiondService sessiondService,
        MultifactorLockoutService lockoutService,
        MultifactorListenerChain listenerChain,
        MultifactorProviderRegistry providerRegistry) {
        super(multifactorProvider, lockoutService, listenerChain, providerRegistry);

        this.session = Objects.requireNonNull(session, "Session must not be null");
        this.sessiondService = Objects.requireNonNull(sessiondService, "SessiondService should not be null");
    }

    @Override
    protected void authenticationPerformed() throws OXException {
        session.setParameter(Session.MULTIFACTOR_AUTHENTICATED, Boolean.TRUE);
        session.setParameter(Session.MULTIFACTOR_LAST_VERIFIED, L(System.currentTimeMillis()));
        sessiondService.storeSession(session.getSessionID());
    }

}
