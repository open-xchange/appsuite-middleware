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
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import com.openexchange.exception.OXException;
import com.openexchange.saml.OpenSAML;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.session.Session;

/**
 * In order to fulfill the requirements of a certain identity provider, it might be necessary
 * to customize the generation and decoding of SAML XML objects. This interface provides a set
 * of customization functions for this purpose. It is  recommended to inherit from {@link AbstractWebSSOCustomizer}
 * instead implementing this interface directly.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see AbstractWebSSOCustomizer
 */
public interface WebSSOCustomizer {

    /**
     * A parameter class that encapsulates context data of the HTTP request that caused the initialization
     * of a SAML flow.
     */
    public static final class RequestContext {

        /**
         * The SAML configuration used while processing the according request
         */
        public SAMLConfig config;

        /**
         * {@link OpenSAML} to provide the full feature set of the underlying library
         */
        public OpenSAML openSAML;

        /**
         * The servlet request
         */
        public HttpServletRequest httpRequest;

        /**
         * The servlet response
         */
        public HttpServletResponse httpResponse;

    }

    /**
     * Customizes the authentication request. The request is prepared using the
     * configuration in <code>saml.properties</code>. Additionally certain assumptions
     * have been made:
     *
     * <ul>
     *   <li><code>Issuer</code> is set to the configured entity ID</li>
     *   <li><code>ProviderName</code> is set to the configured value</li>
     *   <li><code>ProtocolBinding</code> is set to <code>urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST</code></li>
     *   <li><code>AssertionConsumerServiceURL</code> is set to the configured value</li>
     *   <li><code>Destination</code> is set to the configured IdP authentication URL</li>
     *   <li><code>IsPassive</code> is <code>false</code></li>
     *   <li><code>ForceAuthn</code> is <code>false</code></li>
     *   <li><code>ID</code> is a random {@link UUID} string without the dashes</li>
     *   <li><code>IssueInstant</code> is set to now</li>
     *   <li>Several optional elements and attributes are not set (e.g. <code>Subject</code> or <code>NameIDPolicy</code>)</li>
     * </ul>
     *
     * Before the customize method is called, the corresponding request looks like this:
     * <pre>
     * <saml2p:AuthnRequest xmlns:saml2p="urn:oasis:names:tc:SAML:2.0:protocol"
     *     Version="2.0"
     *     ID="18a56c7a00be44fc97881ffd7b24d9dd"
     *     IssueInstant="2015-02-23T16:29:59.716Z"
     *     Destination="https://sso.ox-hosting.com/idp"
     *     ForceAuthn="false"
     *     IsPassive="false"
     *     AssertionConsumerServiceURL="https://appsuite.ox-hosting.com/appsuite/api/acs"
     *     ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
     *     ProviderName="OX App Suite">
     *
     *     <saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">appsuite.com</saml2:Issuer>
     * </saml2p:AuthnRequest>
     * </pre>
     *
     * @param authnRequest The prepared authentication request
     * @param requestContext The request context
     * @return The customized instance. This allows a full replacement of the request. Always return a valid instance,
     * not <code>null</code>!
     * @throws OXException If thrown the further processing will be aborted
     */
    AuthnRequest customizeAuthnRequest(AuthnRequest authnRequest, RequestContext requestContext) throws OXException;

    /**
     * Allows to customize the decoding of the authentication response. The result of this method is expected to be the full XML
     * representation of the &lt;Response&gt; element. The implementation of this method is optional, simply return <code>null</code>
     * if the response shall be decoded by the core component in the normal (spec-conform) way.
     *
     * @param httpRequest The servlet request of the HTTP POST that (normally) contains the base64 encoded response XML as a form field
     * @return The decoded XML or <code>null</code> if the response shall be decoded in the normal way by the core implementation.
     * @throws OXException If thrown the further processing will be aborted
     */
    String decodeAuthnResponse(HttpServletRequest httpRequest) throws OXException;

    /**
     * This method is only called if single logout is enabled by configuration. It allows customization of logout requests that are sent
     * to the IdP during SP-initiated single logout flows. The request was prepared based on the users session and the properties in
     * <code>saml.properties</code>.
     *
     * @param logoutRequest The logout request
     * @param session The session that triggered the logout
     * @param requestContext The request context
     * @return The customized instance. This allows a full replacement of the request. Always return a valid instance,
     * not <code>null</code>!
     * @throws OXException If thrown the further processing will be aborted
     */
    LogoutRequest customizeLogoutRequest(LogoutRequest logoutRequest, Session session, RequestContext requestContext) throws OXException;

    /**
     * This method is only called if single logout is enabled by configuration. It allows to customize the decoding of the logout responses
     * that are sent by the IdP in response to SP-initiated single logout requests. The result of this method is expected to be the full XML
     * representation of the &lt;LogoutRequest&gt; element. The implementation of this method is optional, simply return <code>null</code>
     * if the response shall be decoded by the core component in the normal (spec-conform) way.
     *
     * @param httpRequest The servlet request containing the logout request
     * @param binding The binding via which the request was received
     * @return The decoded XML or <code>null</code> if the request shall be decoded in the normal way by the core implementation.
     * @throws OXException If thrown the further processing will be aborted
     */
    String decodeLogoutResponse(HttpServletRequest httpRequest, Binding binding) throws OXException;

    /**
     * This method is only called if single logout is enabled by configuration. It allows customization of logout responses that are to be
     * sent as replies to IdP-initiated single logout requests. The given response was prepared based on the processed logout request and
     * the properties in <code>saml.properties</code>.
     *
     * @param logoutResponse The prepared logout response
     * @param requestContext The request context
     * @return The customized instance. This allows a full replacement of the response. Always return a valid instance,
     * not <code>null</code>!
     * @throws OXException If thrown the further processing will be aborted
     */
    LogoutResponse customizeLogoutResponse(LogoutResponse logoutResponse, RequestContext requestContext) throws OXException;

    /**
     * This method is only called if single logout is enabled by configuration. It allows to customize the decoding of the logout requests
     * that are received on IdP-initiated logout requests. The result of this method is expected to be the full XML representation of the
     * &lt;LogoutRequest&gt; element. The implementation of this method is optional, simply return <code>null</code> if the response shall
     * be decoded by the core component in the normal (spec-conform) way.
     *
     * @param httpRequest The servlet request containing the logout request
     * @param binding The binding via which the request was received
     * @return The decoded XML or <code>null</code> if the request shall be decoded in the normal way by the core implementation.
     * @throws OXException If thrown the further processing will be aborted
     */
    String decodeLogoutRequest(HttpServletRequest httpRequest, Binding binding) throws OXException;

    /**
     * This method is only called if providing metadata is enabled by configuration. It allows customization of the SPSSODescriptor of the
     * metadata XML. This method is then called right after the descriptor has been constructed based on general assumptions and the properties
     * in <code>saml.properties</code>.
     *
     * @param descriptor The prepared descriptor
     * @return The customized instance. This allows a full replacement of the descriptor. Always return a valid descriptor,
     * not <code>null</code>!
     * @throws OXException If thrown the further processing will be aborted
     */
    SPSSODescriptor customizeSPSSODescriptor(SPSSODescriptor descriptor) throws OXException;

}
