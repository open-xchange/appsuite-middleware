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

package com.openexchange.external.account.impl.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.database.DatabaseService;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.external.account.ExternalAccountRMIService;
import com.openexchange.external.account.ExternalAccountService;
import com.openexchange.external.account.impl.ExternalAccountProviderRegistry;
import com.openexchange.external.account.impl.ExternalAccountRMIServiceImpl;
import com.openexchange.external.account.impl.ExternalAccountServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ExternalAccountActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class ExternalAccountActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ExternalAccountActivator}.
     */
    public ExternalAccountActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // Initialize registry
        ExternalAccountProviderRegistry externalAccountProviderRegistry = new ExternalAccountProviderRegistry(context);
        track(ExternalAccountProvider.class, externalAccountProviderRegistry);
        openTrackers();

        // Register external account service
        ExternalAccountServiceImpl externalAccountService = new ExternalAccountServiceImpl(externalAccountProviderRegistry, this);
        registerService(ExternalAccountService.class, externalAccountService);

        // Register RMI interface for external account service
        {
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put("RMI_NAME", ExternalAccountRMIService.RMI_NAME);
            registerService(Remote.class, new ExternalAccountRMIServiceImpl(externalAccountService), serviceProperties);
        }
    }

}
