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

package com.openexchange.multifactor.json.osgi;

import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.login.multifactor.MultifactorLoginService;
import com.openexchange.multifactor.MultifactorAuthenticatorFactory;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.json.MultifactorDeviceActionFactory;
import com.openexchange.multifactor.json.MultifactorProviderActionFactory;
import com.openexchange.multifactor.json.converter.MultifactorChallengeResultConverter;
import com.openexchange.multifactor.json.converter.MultifactorDevicesResultConverter;
import com.openexchange.multifactor.json.converter.MultifactorProvidersResultConverter;
import com.openexchange.multifactor.json.login.MultifactorLoginRampupService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;

/**
 * {@link MultifactorJsonActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorJsonActivator extends AJAXModuleActivator {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MultifactorJsonActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MultifactorProviderRegistry.class, MultifactorAuthenticatorFactory.class,
            ThreadPoolService.class, Dispatcher.class, UserService.class, MultifactorLoginService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());
        registerModule(new MultifactorProviderActionFactory(this), MultifactorProviderActionFactory.MODULE);
        registerModule(new MultifactorDeviceActionFactory(this), MultifactorDeviceActionFactory.MODULE);

        //register result converter
        registerService(ResultConverter.class, new MultifactorChallengeResultConverter());
        registerService(ResultConverter.class, new MultifactorDevicesResultConverter());
        registerService(ResultConverter.class, new MultifactorProvidersResultConverter());

        registerService(LoginRampUpService.class, new MultifactorLoginRampupService(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
