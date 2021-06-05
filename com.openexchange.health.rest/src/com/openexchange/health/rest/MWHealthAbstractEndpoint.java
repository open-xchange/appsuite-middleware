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

package com.openexchange.health.rest;

import java.lang.reflect.Method;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.health.MWHealthCheckProperty;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.rest.services.EndpointAuthenticator;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MWHealthAbstractEndpoint}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v8.0.0
 */
public abstract class MWHealthAbstractEndpoint implements EndpointAuthenticator {

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link MWHealthAbstractEndpoint}.
     *
     * @param services The {@link ServiceLookup}
     */
    protected MWHealthAbstractEndpoint(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public String getRealmName() {
        return "OX HEALTH";
    }

    @Override
    public boolean permitAll(Method invokedMethod) {
        Pair<String, String> credentials;
        try {
            credentials = getCredentials();
            return (null == credentials || (Strings.isEmpty(credentials.getFirst()) && Strings.isEmpty(credentials.getSecond())));
        } catch (@SuppressWarnings("unused") OXException e) {
            return false;
        }
    }

    @Override
    public boolean authenticate(String login, String password, Method invokedMethod) {
        Pair<String, String> credentials;
        try {
            credentials = getCredentials();
            return (null != credentials && credentials.getFirst().equals(login) && credentials.getSecond().equals(password));
        } catch (@SuppressWarnings("unused") OXException e) {
            return false;
        }
    }

    private Pair<String, String> getCredentials() throws OXException {
        LeanConfigurationService configurationService = services.getServiceSafe(LeanConfigurationService.class);
        String username = configurationService.getProperty(MWHealthCheckProperty.username);
        String password = configurationService.getProperty(MWHealthCheckProperty.password);
        return new Pair<String, String>(username, password);
    }

}
