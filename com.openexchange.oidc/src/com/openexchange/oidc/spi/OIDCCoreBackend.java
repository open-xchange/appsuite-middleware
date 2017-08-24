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

package com.openexchange.oidc.spi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.openexchange.exception.OXException;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.Services;

/**
 * The implementation of the core OpenID backend.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCCoreBackend extends AbstractOIDCBackend{

    private static final String LOAD_EMAIL_ADDRESS = "load email address";

    @Override
    public AuthenticationInfo resolveAuthenticationResponse(HttpServletRequest request, OIDCTokenResponse tokenResponse) throws OXException {
        BearerAccessToken bearerAccessToken = tokenResponse.getTokens().getBearerAccessToken();
        String emailAddress = this.loadEmailAddressFromIDP(bearerAccessToken);
        MailResolver mailMapping = Services.getService(MailResolver.class);
        ResolvedMail resolvedMail = mailMapping.resolve(emailAddress);
        int contextId = resolvedMail.getContextID();
        int userId = resolvedMail.getUserID();
        AuthenticationInfo resultInfo = new AuthenticationInfo(contextId, userId);
        resultInfo.getProperties().put(AUTH_RESPONSE, tokenResponse.toJSONObject().toJSONString());
        return resultInfo;
    }

    private String loadEmailAddressFromIDP(BearerAccessToken bearerAccessToken) throws OXException {
        UserInfoRequest userInfoReq = null;
        userInfoReq = new UserInfoRequest(getURIFromPath(this.getBackendConfig().getUserInfoEndpoint()), bearerAccessToken);

        HTTPResponse userInfoHTTPResp = null;
        try {
          userInfoHTTPResp = userInfoReq.toHTTPRequest().send();
        } catch (SerializeException | IOException e) {
            throw OIDCExceptionCode.UNABLE_TO_SEND_REQUEST.create(e, LOAD_EMAIL_ADDRESS);
        }

        UserInfoResponse userInfoResponse = null;
        try {
          userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
        } catch (ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_RESPONSE_FROM_IDP.create(e, LOAD_EMAIL_ADDRESS);
        }

        if (userInfoResponse instanceof UserInfoErrorResponse) {
          ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
          throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create(error.getCode() + " " + error.getDescription());
        }

        UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;
        if (successResponse == null) {
            throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create();
        }
        return successResponse.getUserInfo().getEmailAddress();
    }

    private URI getURIFromPath(String path) throws OXException{
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, path);
        }
    }

    @Override
    public void finishLogout(HttpServletRequest request, HttpServletResponse response) {
    }
}
