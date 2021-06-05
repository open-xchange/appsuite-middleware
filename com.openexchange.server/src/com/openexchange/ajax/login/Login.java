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

package com.openexchange.ajax.login;

import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;

/**
 * {@link Login}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Login extends AbstractLoginRequestHandler {

    /**
     *
     */
    final LoginConfiguration conf;

    /**
     * Initializes a new {@link Login}.
     *
     * @param login
     */
    public Login(LoginConfiguration conf, Set<LoginRampUpService> rampUp) {
        super(rampUp);
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        // Look-up necessary credentials
        try {
            doLogin(req, resp, requestContext);
        } catch (OXException e) {
            LoginServlet.logAndSendException(resp, e);
            requestContext.getMetricProvider().recordException(e);
        }
    }

    private void doLogin(final HttpServletRequest req, final HttpServletResponse resp, LoginRequestContext requestContext) throws IOException, OXException {
        loginOperation(req, resp, new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req2) throws OXException {
                final LoginRequest request = LoginTools.parseLogin(
                    req2,
                    LoginFields.NAME_PARAM,
                    false,
                    conf.getDefaultClient(),
                    conf.isCookieForceHTTPS(),
                    conf.isDisableTrimLogin(),
                    false);
                return LoginPerformer.getInstance().doLogin(request);
            }
        }, conf, requestContext);
    }
}
