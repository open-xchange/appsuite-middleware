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

package com.openexchange.oidc.impl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.SessionSsoProvider;


/**
 * {@link OIDCSessionSsoProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class OIDCSessionSsoProvider implements SessionSsoProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCSessionSsoProvider.class);

    /**
     * Initializes a new {@link OIDCSessionSsoProvider}.
     */
    public OIDCSessionSsoProvider() {
        super();
    }

    @Override
    public boolean isSsoSession(Session session) throws OXException {
        return session != null && null != session.getParameter(OIDCTools.IDTOKEN);
    }

    @Override
    public boolean skipAutoLoginAttempt(HttpServletRequest request, HttpServletResponse response) throws OXException {
        LoginConfiguration loginConfiguration = LoginServlet.getLoginConfiguration();
        if (loginConfiguration == null) {
            LOG.warn("Cannot verify autologin request due to missing login configuration");
            return false;
        }

        Cookie sessionCookie = OIDCTools.loadSessionCookie(request, loginConfiguration);
        if (sessionCookie == null) {
            return false;
        }

        Session session = OIDCTools.getSessionFromSessionCookie(sessionCookie, request);
        return isSsoSession(session);
    }

}
