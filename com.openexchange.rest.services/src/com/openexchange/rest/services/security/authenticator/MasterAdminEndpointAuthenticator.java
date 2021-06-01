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

package com.openexchange.rest.services.security.authenticator;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.Credentials;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MasterAdminEndpointAuthenticator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MasterAdminEndpointAuthenticator extends AbstractEndpointAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(MasterAdminEndpointAuthenticator.class);

    /**
     * Initialises a new {@link MasterAdminEndpointAuthenticator}.
     */
    public MasterAdminEndpointAuthenticator() {
        super();
    }

    @Override
    public boolean authenticate(String login, String password, Method invokedMethod) {
        try {
            Authenticator authenticator = ServerServiceRegistry.getInstance().getService(Authenticator.class);
            if (null == authenticator) {
                throw ServiceExceptionCode.absentService(Authenticator.class);
            }
            authenticator.doAuthentication(new Credentials(login, password));
            return true;
        } catch (Exception e) {
            LOG.error("", e);
        }
        return false;
    }
}
