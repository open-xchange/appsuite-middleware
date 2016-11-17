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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.oauth.provider.impl.servlets;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.service.MailService;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.Grant;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link TokenIntrospection} - End-point for validating a token.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class TokenIntrospection extends OAuthTokenValidationEndpoint {

    private static final long serialVersionUID = 1337205004658187201L;

    private static final Logger LOG = LoggerFactory.getLogger(TokenIntrospection.class);

    private static final TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

    // ------------------------------------------------------------------------------------------------------------------

    public TokenIntrospection(OAuthAuthorizationService oauthAuthService, ClientManagement clientManagement, GrantManagement grantManagement, ServiceLookup services) {
        super(oauthAuthService, clientManagement, grantManagement, services);
    }

    private void respondWithError(HttpServletResponse resp) throws Exception {
        respondWithError("invalid_token", resp);
    }

    private void respondWithError(String errorToken, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter writer = resp.getWriter();
        writer.write(new StringBuilder("{\"error\":\"").append(errorToken).append("\"}").toString());
        writer.flush();
    }

    @Override
    protected Set<String> requiredQueryParameters() {
        return Collections.singleton(OAuthProviderConstants.PARAM_CLIENT_ID);
    }

    @Override
    protected void handleExpiredToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        respondWithError(resp);
    }

    @Override
    protected void handleMalformedToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        respondWithError(resp);
    }

    @Override
    protected void handleUnknownToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        respondWithError(resp);
    }

    @Override
    protected void handleValidToken(String accessToken, Map<String, String> parameters, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        Grant grant = grantManagement.getGrantByAccessToken(accessToken);
        if (null == grant) {
            respondWithError(resp);
            return;
        }

        {
            String givenClientId = parameters.get(OAuthProviderConstants.PARAM_CLIENT_ID);
            if (false == givenClientId.equals(grant.getClientId())) {
                respondWithError("invalid_client_id", resp);
                return;
            }
        }

        UserService userService = services.getService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }

        MailService mailService = services.getService(MailService.class);
        if (null == mailService) {
            throw ServiceExceptionCode.absentService(MailService.class);
        }

        User user = userService.getUser(grant.getUserId(), grant.getContextId());
        String mailLogin = mailService.getMailLoginFor(grant.getUserId(), grant.getContextId(), 0);

        JSONObject jResponse = new JSONObject(4);
        jResponse.put("mail_login", mailLogin);
        jResponse.put("enabled", user.isMailEnabled());
        jResponse.put("login_info", user.getLoginInfo());

        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = resp.getWriter();
        writer.write(jResponse.toString());
        writer.flush();
    }

}
