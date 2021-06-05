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

package com.openexchange.saml.spi;

import javax.servlet.http.HttpServletRequest;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.StateManagement;
import com.openexchange.session.Session;


/**
 * A convenience class that allows implementors of {@link WebSSOCustomizer} to only
 * implement the methods that are really needed. All methods of the interface are pre-
 * implemented with default implementations, i.e. no customization takes place.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see WebSSOCustomizer
 */
public abstract class AbstractWebSSOCustomizer implements WebSSOCustomizer {

    @Override
    public AuthnRequest customizeAuthnRequest(AuthnRequest authnRequest, RequestContext requestContext) throws OXException {
        return authnRequest;
    }

    @Override
    public String decodeAuthnResponse(HttpServletRequest httpRequest) throws OXException {
        return null;
    }

    @Override
    public LogoutRequest customizeLogoutRequest(LogoutRequest logoutRequest, Session session, RequestContext requestContext) {
        return logoutRequest;
    }

    @Override
    public LogoutResponse customizeLogoutResponse(LogoutResponse logoutResponse, RequestContext requestContext) throws OXException {
        return logoutResponse;
    }

    @Override
    public String decodeLogoutRequest(HttpServletRequest httpRequest, Binding binding) throws OXException {
        return null;
    }

    @Override
    public String decodeLogoutResponse(HttpServletRequest httpRequest, Binding binding) throws OXException {
        return null;
    }

    @Override
    public SPSSODescriptor customizeSPSSODescriptor(SPSSODescriptor descriptor) throws OXException {
        return descriptor;
    }

    @Override
    public AuthnRequestInfo getRequestInfo(HttpServletRequest httpRequest, Response response, StateManagement stateManagement) throws OXException {
        return null;
    }
}
