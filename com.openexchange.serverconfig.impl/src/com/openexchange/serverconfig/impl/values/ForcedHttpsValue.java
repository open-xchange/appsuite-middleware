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
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.session.Session;

/**
 * {@link ForcedHttpsValue} - Ensured that value of property <code>"com.openexchange.forceHTTPS"</code> is contained in server configuration
 * passed to App Suite UI.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ForcedHttpsValue implements ComputedServerConfigValueService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ForcedHttpsValue}.
     */
    public ForcedHttpsValue(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void addValue(Map<String, Object> serverConfig, String hostName, int userId, int contextId, Session optSession) {
        if (!serverConfig.containsKey("forceHTTPS")) {
            final ConfigurationService service = services.getService(ConfigurationService.class);
            final boolean forceHttps = service.getBoolProperty("com.openexchange.forceHTTPS", false);
            serverConfig.put("forceHTTPS", Boolean.valueOf(forceHttps));
        }
    }

}
