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

package com.openexchange.oidc.tools;

import org.mockito.Mockito;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.impl.OIDCPasswordGrantAuthentication;
import com.openexchange.oidc.osgi.OIDCBackendRegistry;
import com.openexchange.serverconfig.ServerConfigService;


/**
 * {@link MockablePasswordGrantAuthentication}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class MockablePasswordGrantAuthentication extends OIDCPasswordGrantAuthentication {

    public MockablePasswordGrantAuthentication(OIDCBackendRegistry backends) {
        this(backends, Mockito.mock(ServerConfigService.class));
    }

    public MockablePasswordGrantAuthentication(OIDCBackendRegistry backends, ServerConfigService serverConfigService) {
        super(backends, serverConfigService);
    }

    @Override
    public TokenRequest buildTokenRequest(OIDCBackend backend, String username, String password) throws OXException {
        return super.buildTokenRequest(backend, username, password);
    }

    @Override
    public TokenResponse sendTokenRequest(OIDCBackend backend, TokenRequest request) throws OXException {
        return super.sendTokenRequest(backend, request);
    }

    @Override
    protected OIDCBackend getBackend(LoginInfo loginInfo) {
        // Return default backend
        for (OIDCBackend backend : backends.getAllRegisteredBackends()) {
            if ("".equals(backend.getPath())) {
                return backend;
            }
        }

        return null;
    }

}
