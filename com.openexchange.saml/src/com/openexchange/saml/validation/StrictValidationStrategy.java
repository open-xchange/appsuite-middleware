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

package com.openexchange.saml.validation;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Response;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.validation.chain.AbstractChainBasedValidationStrategy;
import com.openexchange.saml.validation.chain.AssertionValidators.AssertionIssuerValidator;
import com.openexchange.saml.validation.chain.AssertionValidators.AssertionSignatureValidator;
import com.openexchange.saml.validation.chain.ResponseValidators.ResponseDestinationValidator;
import com.openexchange.saml.validation.chain.ResponseValidators.ResponseIssuerValidator;
import com.openexchange.saml.validation.chain.ResponseValidators.ResponseSignatureValidator;
import com.openexchange.saml.validation.chain.ResponseValidators.ResponseStatusCodeValidator;
import com.openexchange.saml.validation.chain.SubjectConfirmationDataValidators.InResponseToValidator;
import com.openexchange.saml.validation.chain.SubjectConfirmationDataValidators.NotOnOrAfterValidator;
import com.openexchange.saml.validation.chain.SubjectConfirmationDataValidators.RecipientValidator;
import com.openexchange.saml.validation.chain.ValidatorChain;


/**
 * {@link StrictValidationStrategy}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class StrictValidationStrategy extends AbstractChainBasedValidationStrategy {

    /**
     * Initializes a new {@link StrictValidationStrategy}.
     * @param config
     * @param credentialProvider
     */
    public StrictValidationStrategy(SAMLConfig config, CredentialProvider credentialProvider) {
        super(config, credentialProvider);
    }

    @Override
    protected ValidatorChain getValidatorChain(Response response, Binding binding) {
        ValidatorChain chain = new ValidatorChain();

        /*
         * If response is signed, we need to verify the signature
         */
        chain.add(new ResponseSignatureValidator(credentialProvider.getValidationCredential()));

        /*
         * If the message is signed, the Destination XML attribute in the root SAML element of the protocol
         * message MUST contain the URL to which the sender has instructed the user agent to deliver the
         * message. The recipient MUST then verify that the value matches the location at which the message has
         * been received.
         * [bindings 05 - 3.5.5.2p24]
         */
        boolean allowNullDestination = !(binding == Binding.HTTP_POST && response.isSigned());
        chain.add(new ResponseDestinationValidator(config.getAssertionConsumerServiceURL(), allowNullDestination));

        /*
         * The status code of the response must be 'urn:oasis:names:tc:SAML:2.0:status:Success'
         */
        chain.add(new ResponseStatusCodeValidator());

        /*
         * If the <Response> message is signed or
         * if an enclosed assertion is encrypted, then the <Issuer> element MUST be present. Otherwise it
         * MAY be omitted. If present it MUST contain the unique identifier of the issuing identity provider; the
         * Format attribute MUST be omitted or have a value of urn:oasis:names:tc:SAML:2.0:nameid-format:entity.
         * [profiles 06 - 4.1.4.2p19]
         */
        boolean allowNullIssuer = !(response.isSigned() || response.getEncryptedAssertions().size() > 0);
        chain.add(new ResponseIssuerValidator(config.getIdentityProviderEntityID(), allowNullIssuer));

        /*
         * The <Assertion> element(s) in the <Response> MUST be signed, if the HTTP POST binding is used,
         * and MAY be signed if the HTTP-Artifact binding is used.
         * [profiles 06 - 4.1.3.5p18]
         */
        boolean enforceSignature = (binding == Binding.HTTP_POST);
        chain.add(new AssertionSignatureValidator(credentialProvider.getValidationCredential(), enforceSignature));

        /*
         * Check the assertions issuers
         */
        chain.add(new AssertionIssuerValidator(config.getIdentityProviderEntityID()));

        /*
         * At lease one bearer <SubjectConfirmation> element MUST contain a
         * <SubjectConfirmationData> element that itself MUST contain a Recipient attribute containing
         * the service provider's assertion consumer service URL and a NotOnOrAfter attribute that limits the
         * window during which the assertion can be confirmed by the relying party. It MAY also contain an
         * Address attribute limiting the client address from which the assertion can be delivered. It MUST NOT
         * contain a NotBefore attribute. If the containing message is in response to an <AuthnRequest>,
         * then the InResponseTo attribute MUST match the request's ID.
         * [profiles 06 - 4.1.4.2p20]
         */
        chain.add(new RecipientValidator(config.getAssertionConsumerServiceURL()));
        chain.add(new NotOnOrAfterValidator(new DateTime()));
        chain.add(new InResponseToValidator(null)); // FIXME

        return chain;
    }

}
