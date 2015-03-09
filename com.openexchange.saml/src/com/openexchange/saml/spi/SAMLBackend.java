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

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.validation.StrictValidationStrategy;
import com.openexchange.saml.validation.ValidationStrategy;
import com.openexchange.saml.validation.chain.AbstractChainBasedValidationStrategy;
import com.openexchange.saml.validation.chain.AssertionValidator;
import com.openexchange.saml.validation.chain.ResponseValidator;
import com.openexchange.saml.validation.chain.ValidatorChain;


/**
 * A {@link SAMLBackend} must be implemented and registered as OSGi service to enable
 * SAML-based SSO. It provides the necessary deployment-specific objects that are needed
 * to create SP requests and process IDP responses.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
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
    SAMLWebSSOCustomizer getWebSSOCustomizer();

    /**
     * Gets the validation strategy that will be used to validate authentication responses.
     * Most likely you want return an instance of {@link StrictValidationStrategy} here.
     *
     * Unfortunately it might be that the actual IDPs responses are not conform to the SAML spec,
     * as it is quite complex and hard to implement. In such cases you need to implement your own
     * validation strategy. To avoid that malicious responses are accepted as valid you should not
     * start with implementing your own validation mechanisms from scratch, but inherit from
     * {@link AbstractChainBasedValidationStrategy}. You can then simply leave single validators
     * out to work around the validation problems.
     *
     * @param config The SAML configuration
     * @return The validation strategy
     * @see StrictValidationStrategy
     * @see AbstractChainBasedValidationStrategy
     * @see ValidatorChain
     * @see ResponseValidator
     * @see AssertionValidator
     */
    ValidationStrategy getValidationStrategy(SAMLConfig config);

    /**
     * Resolves a principal based on the provided response and bearer assertion.
     *
     * @param response The SAML response
     * @param assertion A valid bearer assertion whose subject shall be mapped to a principal
     * @return The principal, which must denote an existing user
     * @throws OXException If the principal cannot be resolved
     */
    Principal resolvePrincipal(Response response, Assertion assertion) throws OXException;

}
