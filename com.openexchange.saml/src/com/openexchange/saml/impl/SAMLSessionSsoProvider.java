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

package com.openexchange.saml.impl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLSessionParameters;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.session.Session;
import com.openexchange.session.SessionSsoProvider;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link SAMLSessionSsoProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class SAMLSessionSsoProvider implements SessionSsoProvider {

    private final LoginConfigurationLookup loginConfigLookup;

    private final SessiondService sessiondService;

    /**
     * Initializes a new {@link SAMLSessionSsoProvider}.
     * @param sessiondService
     */
    public SAMLSessionSsoProvider(LoginConfigurationLookup loginConfigLookup, SessiondService sessiondService) {
        super();
        this.loginConfigLookup = loginConfigLookup;
        this.sessiondService = sessiondService;
    }

    @Override
    public boolean isSsoSession(Session session) throws OXException {
        return session != null && "true".equals(session.getParameter(SAMLSessionParameters.AUTHENTICATED));
    }

    @Override
    public boolean skipAutoLoginAttempt(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        LoginConfiguration loginConfiguration = loginConfigLookup.getLoginConfiguration();
        Cookie sessionCookie = SAMLLoginTools.getSessionCookie(httpRequest, loginConfiguration);
        Session session = SAMLLoginTools.getSessionForSessionCookie(sessionCookie, sessiondService);
        if (session == null) {
            return false;
        }

        if (isSsoSession(session)) {
            String hash = HashCalculator.getInstance().getHash(httpRequest, LoginTools.parseUserAgent(httpRequest), LoginTools.parseClient(httpRequest, false, loginConfiguration.getDefaultClient()));
            return SAMLLoginTools.isValidSession(httpRequest, session, hash);
        }
        return false;
    }

}
