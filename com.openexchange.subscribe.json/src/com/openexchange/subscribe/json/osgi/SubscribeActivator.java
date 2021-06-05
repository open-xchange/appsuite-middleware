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

package com.openexchange.subscribe.json.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.json.actions.SubscriptionActionFactory;
import com.openexchange.subscribe.json.actions.SubscriptionSourcesActionFactory;

/**
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public class SubscribeActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, SubscriptionExecutionService.class, PreferencesItemService.class, SubscriptionSourceDiscoveryService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        this.addService(SecretService.class, new WhiteboardSecretService(context));

        registerModule(new SubscriptionSourcesActionFactory(this), "subscriptionSources");
        registerModule(new SubscriptionActionFactory(this), "subscriptions");
    }

    @Override
    protected void stopBundle() throws Exception {
        services = null;
        super.stopBundle();
    }
}
