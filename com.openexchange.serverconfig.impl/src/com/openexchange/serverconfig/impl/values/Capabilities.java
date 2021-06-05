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

package com.openexchange.serverconfig.impl.values;

import java.util.Map;
import java.util.Set;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.session.Session;

/**
 * {@link Capabilities}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Capabilities implements ComputedServerConfigValueService {

    private final ServiceLookup services;

    public Capabilities(ServiceLookup services) {
        super();
        this.services = services;
    }


    @Override
    public void addValue(Map<String, Object> serverConfig, String hostName, int userId, int contextId, Session optSession) throws OXException {
        CapabilityService capabilityService = services.getService(CapabilityService.class);
        Set<Capability> capabilities;
        if (null == optSession) {
            capabilities = capabilityService.getCapabilities(userId, contextId, true, true).asSet();
        } else {
            capabilities = capabilityService.getCapabilities(optSession, true).asSet();
        }

        serverConfig.put("capabilities", capabilities);
    }

}
