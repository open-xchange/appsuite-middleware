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
