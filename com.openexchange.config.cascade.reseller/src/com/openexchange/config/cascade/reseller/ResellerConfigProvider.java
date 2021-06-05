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

package com.openexchange.config.cascade.reseller;

import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.ResellerService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ResellerConfigProvider}
 *
 * @author <a href="mailto:jhouklis@gmail.com">Ioannis Chouklis</a>
 */
public class ResellerConfigProvider implements ConfigProviderService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ResellerConfigProvider}.
     */
    public ResellerConfigProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public BasicProperty get(String propertyName, int contextId, int userId) throws OXException {
        if (contextId == NO_CONTEXT) {
            return NO_PROPERTY;
        }
        ResellerService resellerService = services.getOptionalService(ResellerService.class);
        if (resellerService == null || false == resellerService.isEnabled()) {
            return NO_PROPERTY;
        }
        return new ResellerBasicPropertyImpl(propertyName, contextId, resellerService);
    }

    @Override
    public Collection<String> getAllPropertyNames(int contextId, int userId) throws OXException {
        if (contextId == NO_CONTEXT) {
            return ImmutableList.of();
        }
        ResellerService resellerService = services.getOptionalService(ResellerService.class);
        if (resellerService == null || false == resellerService.isEnabled()) {
            return ImmutableList.of();
        }
        return resellerService.getAllConfigPropertiesByContext(contextId).keySet();
    }

    @Override
    public String getScope() {
        return ConfigViewScope.RESELLER.getScopeName();
    }
}
