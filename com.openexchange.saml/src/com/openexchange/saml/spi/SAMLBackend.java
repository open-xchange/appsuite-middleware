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

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.Response;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.authentication.Authenticated;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
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
import com.openexchange.user.User;

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
     * Gets the possible available access token from given assertion.
     *
     * @param assertion The assertion to get the access token from
     * @return The access token or <code>null</code>
     * @throws OXException If determining the OAuth access token fails
     */
    String getAccessToken(Assertion assertion) throws OXException;

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
     * @throws If parsing the relay state fails
     */
    AuthnRequestInfo parseRelayState(Response response, String relayState) throws OXException;

    /**
     * Method to retrieve the SAMLConfig
     * When requesting config parameters, this method should always be used instead of saving the config parameters
     * @return The SAMLConfig to be used
     */
    SAMLConfig getConfig();

    /**
     * Returns the samlPath part of the servlet path, can be left empty for default path <br>prefix/saml/</br>
     * If set, the servlet path for this SAMLBackend will be changed to <br>prefix/saml/samlPath/</br>
     * Allowed values are [a-zA-Z] or <code>null</code>
     * @return samlPath or <code>null</code>
     */
    String getPath();

    /**
     * Returns a static redirect for login if present or <code>null</code>
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @return a static redirect for login situations or <code>null</code>
     */
    String getStaticLoginRedirectLocation(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
