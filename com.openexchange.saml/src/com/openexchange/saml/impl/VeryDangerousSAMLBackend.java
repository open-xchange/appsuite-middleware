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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.saml.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.security.credential.Credential;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.AbstractSAMLBackend;
import com.openexchange.saml.spi.AuthenticationInfo;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.LogoutInfo;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.LogoutRequestInfo;
import com.openexchange.saml.state.StateManagement;
import com.openexchange.saml.validation.AuthnResponseValidationResult;
import com.openexchange.saml.validation.ValidationException;
import com.openexchange.saml.validation.ValidationStrategy;
import com.openexchange.user.UserService;


/**
 * A backend for development and debugging purposes that simply accepts every response and assertion.
 * Users are resolved by NameID, which is expected in email format and to match {@code <user>@<context>}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class VeryDangerousSAMLBackend extends AbstractSAMLBackend {

    private final UserService userService;

    private final ContextService contextService;

    /**
     * Initializes a new {@link VeryDangerousSAMLBackend}.
     * @param userService
     * @param contextService
     */
    public VeryDangerousSAMLBackend(UserService userService, ContextService contextService) {
        super();
        this.userService = userService;
        this.contextService = contextService;
    }

    @Override
    protected CredentialProvider doGetCredentialProvider() {
        return new CredentialProvider() {

            @Override
            public boolean hasValidationCredentials() {
                return false;
            }

            @Override
            public boolean hasValidationCredential() {
                return false;
            }

            @Override
            public boolean hasSigningCredential() {
                return false;
            }

            @Override
            public boolean hasDecryptionCredential() {
                return false;
            }

            @Override
            public List<Credential> getValidationCredentials() {
                return Collections.emptyList();
            }

            @Override
            public Credential getValidationCredential() {
                return null;
            }

            @Override
            public Credential getSigningCredential() {
                return null;
            }

            @Override
            public Credential getDecryptionCredential() {
                return null;
            }
        };
    }

    @Override
    protected ValidationStrategy doGetValidationStrategy(SAMLConfig config, StateManagement stateManagement) {
        return new ValidationStrategy() {

            @Override
            public void validateLogoutResponse(LogoutResponse response, HttpServletRequest httpRequest, LogoutRequestInfo requestInfo, Binding binding) throws ValidationException {

            }

            @Override
            public void validateLogoutRequest(LogoutRequest logoutRequest, HttpServletRequest httpRequest, Binding binding) throws ValidationException {

            }

            @Override
            public AuthnResponseValidationResult validateAuthnResponse(Response response, AuthnRequestInfo requestInfo, Binding binding) throws ValidationException {
                Optional<Assertion> assertion = response.getAssertions().stream().findFirst();
                return new AuthnResponseValidationResult(assertion.get());
            }
        };
    }

    @Override
    protected AuthenticationInfo doResolveAuthnResponse(Response response, Assertion assertion) throws OXException {
        String[] split = assertion.getSubject().getNameID().getValue().split("@");
        String userInfo = split[0];
        String contextInfo = split[1];

        if (userInfo == null || contextInfo == null) {
            throw new OXException();
        }

        int contextId = contextService.getContextId(contextInfo);
        if (contextId < 0) {
            throw new OXException();
        }

        int userId = userService.getUserId(userInfo, contextService.getContext(contextId));
        AuthenticationInfo authInfo = new AuthenticationInfo(contextId, userId);
        return authInfo;
    }

    @Override
    protected LogoutInfo doResolveLogoutRequest(LogoutRequest request) throws OXException {
        LogoutInfo logoutInfo = new LogoutInfo();
        return logoutInfo;
    }

    @Override
    protected void doFinishLogout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect("https://www.google.com");
    }

}
