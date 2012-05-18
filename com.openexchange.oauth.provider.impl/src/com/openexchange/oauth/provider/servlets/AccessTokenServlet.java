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

package com.openexchange.oauth.provider.servlets;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.internal.DatabaseOAuthProviderService;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceLookup;

/**
 * Access Token request handler
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AccessTokenServlet extends HttpServlet {

    private static final long serialVersionUID = -1473491912970781256L;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        processRequest(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        processRequest(request, response);
    }

    public void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            /*
             * Parse OAuth message
             */
            final OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
            /*
             * Get provider service
             */
            final OAuthProviderService providerService = getProviderService();
            final OAuthAccessor accessor = providerService.getAccessor(requestMessage);
            /*
             * Validate message
             */
            providerService.getValidator().validateMessage(requestMessage, accessor);
            /*
             * Make sure token is authorized
             */
            if (!Boolean.TRUE.equals(accessor.getProperty(OAuthProviderService.PROP_AUTHORIZED))) {
                final OAuthProblemException problem = new OAuthProblemException("permission_denied");
                throw problem;
            }
            /*
             * Generate access token and secret
             */
            final int userId = accessor.<Integer> getProperty(OAuthProviderService.PROP_USER).intValue();
            final int contextId = accessor.<Integer> getProperty(OAuthProviderService.PROP_CONTEXT).intValue();
            providerService.generateAccessToken(accessor, userId, contextId);
            /*
             * Write back
             */
            response.setContentType("text/plain");
            final OutputStream out = response.getOutputStream();
            OAuth.formEncode(OAuth.newList("oauth_token", accessor.accessToken, "oauth_token_secret", accessor.tokenSecret), out);
            out.close();
        } catch (final Exception e) {
            DatabaseOAuthProviderService.handleException(e, request, response, true);
        }
    }

    private OAuthProviderService getProviderService() {
        return OAuthProviderServiceLookup.getService(OAuthProviderService.class);
    }

}
