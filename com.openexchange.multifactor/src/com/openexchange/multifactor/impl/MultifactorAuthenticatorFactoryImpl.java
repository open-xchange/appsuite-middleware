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

package com.openexchange.multifactor.impl;

import com.openexchange.multifactor.MultifactorAuthenticator;
import com.openexchange.multifactor.MultifactorAuthenticatorFactory;
import com.openexchange.multifactor.MultifactorLockoutService;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.SessionModifyingMultifactorAuthenticator;
import com.openexchange.multifactor.listener.MultifactorListenerChain;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MultifactorAuthenticatorFactoryImpl}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorAuthenticatorFactoryImpl implements MultifactorAuthenticatorFactory {

    private final MultifactorLockoutService lockoutService;
    private final SessiondService sessiondService;
    private final MultifactorListenerChain listenerChain;
    private final MultifactorProviderRegistry providerRegistry;

    /**
     * Initializes a new {@link MultifactorAuthenticatorFactoryImpl}.
     *
     * @param lockoutService The {@link MultifactorLockoutService} to use
     * @param sessiondService The {@link SessiondService} to use
     * @param listenerChain The {@link MultifactorListenerChain} to use
     * @param providerRegistry The {@link MultifactorProviderRegistry} to use
     */
    public MultifactorAuthenticatorFactoryImpl (MultifactorLockoutService lockoutService, SessiondService sessiondService, MultifactorListenerChain listenerChain, MultifactorProviderRegistry providerRegistry) {
        this.lockoutService = lockoutService;
        this.sessiondService = sessiondService;
        this.listenerChain = listenerChain;
        this.providerRegistry = providerRegistry;
    }

    @Override
    public MultifactorAuthenticator createAuthenticator(MultifactorProvider provider) {
        return new MultifactorAuthenticator(provider, lockoutService, listenerChain, providerRegistry);
    }

    @Override
    public MultifactorAuthenticator createSessionModifyingAuthenticator(MultifactorProvider provider, ServerSession session) {
        return new SessionModifyingMultifactorAuthenticator(provider, session, sessiondService, lockoutService, listenerChain, providerRegistry);
    }
}
