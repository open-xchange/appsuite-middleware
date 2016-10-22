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

package com.openexchange.saml.spi;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.Response;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.authentication.Authenticated;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLExceptionCode;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.StateManagement;
import com.openexchange.saml.validation.AssertionValidator;
import com.openexchange.saml.validation.ResponseValidator;
import com.openexchange.saml.validation.StrictValidationStrategy;
import com.openexchange.saml.validation.ValidationStrategy;
import com.openexchange.session.reservation.EnhancedAuthenticated;

/**
 * A {@link SAMLBackend} must be implemented and registered as OSGi service to enable
 * SAML-based SSO. It provides the necessary deployment-specific objects and operations
 * that are needed to create SP requests and process IdP responses.
 *
 * It's considered best practice to not implement this interface directly but to inherit
 * from {@link AbstractSAMLBackend}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see AbstractSAMLBackend
 */
public interface SAMLBackend {

    /**
     * Gets the credential provider used to sign/verify/decrypt XML objects.
     *
     * @return The credential provider
     * @see KeySpecCredentialProvider
     * @see KeyStoreCredentialProvider
     */
    CredentialProvider getCredentialProvider();

    /**
     * Gets an optional customizer that allows to modify authentication requests and
     * the service providers metadata (&lt;SPSSODescriptor&gt;) before they are marshalled
     * and returned to the requesting party.
     *
     * @return The customizer or <code>null</code> if customization is not necessary.
     */
    WebSSOCustomizer getWebSSOCustomizer();

    /**
     * Gets the exception handler that deals with exceptions thrown by e.g. {@link WebSSOCustomizer}.
     * You can simply return an instance of {@link DefaultExceptionHandler} here, but probably you
     * want to customize a few things (like HTML error pages). Its considered best practice to inherit
     * from {@link DefaultExceptionHandler} and return your custom version.
     *
     * @return The exception handler
     */
    ExceptionHandler getExceptionHandler();

    /**
     * Gets the validation strategy that will be used to validate SAML responses from the IdP.
     * You should start with returning {@link StrictValidationStrategy} here. If the actual responses
     * are considered invalid, but they must be processed anyway (e.g. because the IdP does not behave
     * compliant to the standard) you need to implement your own strategy. You'll probably want to
     * inherit from {@link StrictValidationStrategy} then and adjust only the validation steps that fail.
     * Implementing the interface from scratch on your own should be last resort.
     *
     * @param config The SAML configuration
     * @param stateManagement The SAML state management
     * @return The validation strategy
     * @see StrictValidationStrategy
     * @see ResponseValidator
     * @see AssertionValidator
     */
    ValidationStrategy getValidationStrategy(SAMLConfig config, StateManagement stateManagement);

    /**
     * Resolves an authentication response based on the bearer assertion that was determined and validated
     * by the validation strategy.
     *
     * @param response The SAML response
     * @param assertion The bearer assertion whose subject shall be mapped to a principal
     * @return The authentication information
     * @throws OXException If the principal cannot be resolved
     * @see SAMLExceptionCode#UNKNOWN_USER
     */
    AuthenticationInfo resolveAuthnResponse(Response response, Assertion assertion) throws OXException;

    /**
     * Resolves an authentication response based on the bearer assertion that was determined and validated
     * by the validation strategy.
     *
     * @param response The SAML response
     * @param bearerAssertion The bearer assertion whose subject shall be mapped to a principal
     * @param requestInfo The AuthnRequestInfo
     * @return The authentication information
     * @throws OXException If the principal cannot be resolved
     * @see SAMLExceptionCode#UNKNOWN_USER
     */
    AuthenticationInfo resolveAuthnResponse(Response response, Assertion bearerAssertion, AuthnRequestInfo requestInfo) throws OXException;

    /**
     * Resolves a logout request and determines which sessions are to be terminated. This method is only
     * called if single logout is activated. Otherwise you can simply return <code>null</code>.
     *
     * @param request The logout request
     * @return The logout info
     * @throws OXException If the sessions to terminate cannot be determined
     */
    LogoutInfo resolveLogoutRequest(LogoutRequest request) throws OXException;

    /**
     * If the single logout profile is enabled, this method is called at the end of a SP-initiated logout,
     * i.e. it is called after the IdP sent his LogoutResponse and the logout was performed on our side.
     * The SAML backend is then responsible to finish the request, most likely by returning a special HTML
     * page or redirecting the user agent to a certain website.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @throws IOException If writing to to servlet output stream fails
     */
    void finishLogout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException;

    /**
     * Prepares the login request that is passed to {@link LoginPerformer} to create the session.
     *
     * @param httpRequest The servlet request
     * @param loginConfiguration The login configuration
     * @param user The user
     * @param context The context
     * @return The login request
     * @throws OXException
     */
    LoginRequest prepareLoginRequest(HttpServletRequest httpRequest, LoginConfiguration loginConfiguration, User user, Context context) throws OXException;

    /**
     * Allows to enhance the {@link Authenticated} that is used to create the session based on an authentication
     * response. Use {@link EnhancedAuthenticated} to wrap the given {@link Authenticated} instance and add your
     * customizations.
     *
     * @param authenticated The authenticated prepared by the 'samlLogin' login action
     * @param properties The properties that were returned as part of {@link AuthenticationInfo} from {@link #resolveAuthnResponse(Response, Assertion)}
     * @return The enhanced {@link Authenticated}. If you don't need to adjust anything, simply return <code>null</code> here
     * @see EnhancedAuthenticated
     */
    Authenticated enhanceAuthenticated(Authenticated authenticated, Map<String, String> properties);

    /**
     * Allows the backend to parse AuthnRequestInfo based on the response and the relayState when using unsolicited responses.
     * @param response The SAML response
     * @param relayState The relayState set by the IDP
     * @return The AuthnRequestInfo
     */
    AuthnRequestInfo parseRelayState(Response response, String relayState);

}
