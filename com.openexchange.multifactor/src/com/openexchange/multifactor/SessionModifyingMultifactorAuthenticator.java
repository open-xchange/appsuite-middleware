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

package com.openexchange.multifactor;

import java.util.Date;
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
        session.setParameter(Session.MULTIFACTOR_AUTHENTICATED, true);
        session.setParameter(Session.MULTIFACTOR_LAST_VERIFIED, new Date().getTime());
        sessiondService.storeSession(session.getSessionID());
    }

}
