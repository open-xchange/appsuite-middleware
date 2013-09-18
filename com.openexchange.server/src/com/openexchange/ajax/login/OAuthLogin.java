/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.login;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;
import com.openexchange.ajax.Login;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link OAuthLogin}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class OAuthLogin extends AbstractLoginRequestHandler {

    private LoginConfiguration conf;

    /**
     * Initializes a new {@link OAuthLogin}.
     * 
     * @param login
     */
    public OAuthLogin(LoginConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        // Look-up necessary credentials
        try {
            doOAuthLogin(req, resp);
        } catch (final OXException e) {
            Login.logAndSendException(resp, e);
        }
    }

    private void doOAuthLogin(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, OXException {
        loginOperation(req, resp, new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req2) throws OXException {
                try {
                    final OAuthProviderService providerService = ServerServiceRegistry.getInstance().getService(OAuthProviderService.class);
                    final OAuthMessage requestMessage = OAuthServlet.getMessage(req2, null);
                    final OAuthAccessor accessor = providerService.getAccessor(requestMessage);
                    providerService.getValidator().validateMessage(requestMessage, accessor);
                    final String login = accessor.<String> getProperty(OAuthProviderConstants.PROP_LOGIN);
                    final String password = accessor.<String> getProperty(OAuthProviderConstants.PROP_PASSWORD);
                    final LoginRequest request = LoginTools.parseLogin(
                        req2,
                        login,
                        password,
                        false,
                        conf.getDefaultClient(),
                        conf.isCookieForceHTTPS(),
                        false);
                    return LoginPerformer.getInstance().doLogin(request);
                } catch (final OAuthProblemException e) {
                    try {
                        handleException(e, req2, resp, false);
                        return null;
                    } catch (final IOException ioe) {
                        throw LoginExceptionCodes.UNKNOWN.create(ioe, ioe.getMessage());
                    } catch (final ServletException se) {
                        throw LoginExceptionCodes.UNKNOWN.create(se, se.getMessage());
                    }
                } catch (final IOException e) {
                    throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                } catch (final OAuthException e) {
                    throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                } catch (final URISyntaxException e) {
                    throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                }
            }

            private void handleException(final Exception e, final HttpServletRequest request, final HttpServletResponse response, final boolean sendBody) throws IOException, ServletException {
                final com.openexchange.java.StringAllocator realm = new com.openexchange.java.StringAllocator(32).append((request.isSecure()) ? "https://" : "http://");
                realm.append(request.getLocalName());
                OAuthServlet.handleException(response, e, realm.toString(), sendBody);
            }
        }, conf);
    }
}
