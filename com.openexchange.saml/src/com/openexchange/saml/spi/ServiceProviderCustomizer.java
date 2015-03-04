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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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
import org.opensaml.saml2.metadata.SPSSODescriptor;
import com.openexchange.exception.OXException;
import com.openexchange.saml.OpenSAML;
import com.openexchange.saml.SAMLConfig;



/**
 * In order to fulfill the requirements of a certain identity provider, it might be necessary
 * to customize the generated XML objects of the service provider side. This interface provides
 * a set of customization functions for this purpose.
 *
 * A concrete implementation must be registered as OSGi service under the {@link ServiceProviderCustomizer}
 * interface. At most one instance is allowed to exist at runtime.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface ServiceProviderCustomizer {

    /**
     * A parameter class that encapsulates context data of
     * the servlet request that caused the initialization
     * of an authentication flow.
     */
    public static final class RequestContext {

        public SAMLConfig config;

        public OpenSAML openSAML;

        public HttpServletRequest httpRequest;

        public HttpServletResponse httpResponse;

    }

    /**
     * Customizes the authentication request. The request is prepared using the
     * configuration in <code>saml.properties</code>. Additionally certain assumptions
     * have been made:
     *
     * <ul>
     *   <li><code>ForceAuthn</code> is <code>false</code></li>
     *   <li><code>IsPassive</code> is <code>false</code></li>
     *   <li><code>Destination</code> is set to the configured IDP URL</li>
     *   <li><code>ID</code> is a random {@link UUID} string without the dashes</li>
     *   <li><code>IssueInstant</code> is set to now</li>
     *   <li><code>urn:oasis:names:tc:SAML:2.0:nameid-format:entity</code> is assumed as <code>Issuer</code> format</li>
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
     *     ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"
     *     ProviderName="appsuite">
     *
     *     <saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" Format="urn:oasis:names:tc:SAML:2.0:nameid-format:entity">https://appsuite.ox-hosting.com</saml2:Issuer>
     * </saml2p:AuthnRequest>
     * </pre>
     *
     * @param authnRequest The prepared authentication request
     * @param requestContext The request context
     * @return The customized instance. This allows a full replacement of the request. Always return a valid request,
     * not <code>null</code>!
     * @throws OXException If thrown the further processing will be aborted
     */
    AuthnRequest customizeAuthnRequest(AuthnRequest authnRequest, RequestContext requestContext) throws OXException;

    /**
     * Customizes the SPSSODescriptor of the metadata XML. This method is called right after the descriptor
     * has been constructed based on general assumptions and the properties in <code>saml.properties</code>.
     *
     * @param descriptor The prepared descriptor
     * @return The customized instance. This allows a full replacement of the descriptor. Always return a valid descriptor,
     * not <code>null</code>!
     * @throws OXException If thrown the further processing will be aborted
     */
    SPSSODescriptor customizeSPSSODescriptor(SPSSODescriptor descriptor) throws OXException;

}
