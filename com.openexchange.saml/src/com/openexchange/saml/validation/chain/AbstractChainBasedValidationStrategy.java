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

package com.openexchange.saml.validation.chain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.xml.encryption.ChainingEncryptedKeyResolver;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.encryption.SimpleKeyInfoReferenceEncryptedKeyResolver;
import org.opensaml.xml.encryption.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoProvider;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.provider.DEREncodedKeyValueProvider;
import org.opensaml.xml.security.keyinfo.provider.DSAKeyValueProvider;
import org.opensaml.xml.security.keyinfo.provider.InlineX509DataProvider;
import org.opensaml.xml.security.keyinfo.provider.KeyInfoReferenceProvider;
import org.opensaml.xml.security.keyinfo.provider.RSAKeyValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.validation.DefaultValidationResult;
import com.openexchange.saml.validation.ValidationResult;
import com.openexchange.saml.validation.ValidationResult.ErrorReason;
import com.openexchange.saml.validation.ValidationStrategy;


/**
 * {@link AbstractChainBasedValidationStrategy}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public abstract class AbstractChainBasedValidationStrategy implements ValidationStrategy {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractChainBasedValidationStrategy.class);

    protected final SAMLConfig config;

    protected final CredentialProvider credentialProvider;

    /**
     * Initializes a new {@link AbstractChainBasedValidationStrategy}.
     * @param config
     * @param credentialProvider
     */
    protected AbstractChainBasedValidationStrategy(SAMLConfig config, CredentialProvider credentialProvider) {
        super();
        this.config = config;
        this.credentialProvider = credentialProvider;
    }


    @Override
    public ValidationResult validate(Response response, Binding binding) {
        ValidatorChain validatiorChain = getValidatorChain(response, binding);
        ValidationError error = validatiorChain.validateResponse(response);
        if (error != null) {
            return new DefaultValidationResult(error.getReason(), error.getMessage());
        }

        List<Assertion> allAssertions;
        try {
            allAssertions = decryptAndCollectAssertions(response);
            if (allAssertions.size() == 0) {
                return new DefaultValidationResult(ErrorReason.MISSING_ELEMENT, "Response contains neither an 'Assertion' nor an 'EncryptedAssertion' element");
            }
        } catch (DecryptionException e) {
            LOG.debug("", e);
            return new DefaultValidationResult(ErrorReason.DECRYPTION_FAILED, e.getMessage());
        }


        error = validatiorChain.validateAssertions(response, allAssertions);
        if (error != null) {
            return new DefaultValidationResult(error.getReason(), error.getMessage());
        }


        List<Assertion> bearerAssertions = new LinkedList<Assertion>();
        for (Assertion assertion : allAssertions) {
            if (isValidBearerAssertion(assertion)) {
                LOG.debug("Assertion '{}' is a valid bearer assertion", assertion.getID());
                bearerAssertions.add(assertion);
            }
        }

        if (bearerAssertions.isEmpty()) {
            return new DefaultValidationResult(ErrorReason.NO_VALID_ASSERTION_CONTAINED, "No contained assertion can be considered a valid bearer assertion");
        }

        Assertion bearerAssertion = null;
        for (Assertion assertion : bearerAssertions) {
            if (conditionsMet(assertion) && hasValidAuthnStatement(assertion)) {
                bearerAssertion = assertion;
                break;
            }
        }

        if (bearerAssertion == null) {
            return new DefaultValidationResult(ErrorReason.NO_VALID_ASSERTION_CONTAINED, "No contained bearer assertion meets the required conditions");
        }

        /*
         * TODO:
         * If the identity provider supports the Single Logout profile, defined in Section 4.4, any authentication
         * statements MUST include a SessionIndex attribute to enable per-session logout requests by the
         * service provider.
         * [profiles 06 - 4.1.4.2p20]
         */

        /*
         * TODO:
         * If an <AuthnStatement> used to establish a security context for the principal contains a
         * SessionNotOnOrAfter attribute, the security context SHOULD be discarded once this time is
         * reached, unless the service provider reestablishes the principal's identity by repeating the use of this
         * profile. Note that if multiple <AuthnStatement> elements are present, the SessionNotOnOrAfter
         * value closest to the present time SHOULD be honored.
         */

        /*
         * TODO:
         * The service provider MUST ensure that bearer assertions are not replayed, by maintaining the set of used
         * ID values for the length of time for which the assertion would be considered valid based on the
         * NotOnOrAfter attribute in the <SubjectConfirmationData>.
         * [profiles 06 - 4.1.4.2p21]
         */

        return new DefaultValidationResult(bearerAssertion);
    }

    protected abstract ValidatorChain getValidatorChain(Response response, Binding binding);

    private boolean conditionsMet(Assertion assertion) {
        /*
         * Each bearer assertion(s) containing a bearer subject confirmation MUST contain an
         * <AudienceRestriction> including the service provider's unique identifier as an <Audience>.
         * Other conditions (and other <Audience> elements) MAY be included as requested by the service
         * provider or at the discretion of the identity provider. (Of course, all such conditions MUST be
         * understood by and accepted by the service provider in order for the assertion to be considered valid.)
         * [profiles 06 - 4.1.4.2p20]
         */
        Conditions conditions = assertion.getConditions();
        if (conditions == null) {
            return false;
        }

        DateTime now = new DateTime();
        DateTime notBefore = conditions.getNotBefore();
        if (notBefore != null && now.isBefore(notBefore)) {
            return false;
        }

        DateTime notOnOrAfter = conditions.getNotOnOrAfter();
        if (notOnOrAfter != null && !now.isBefore(notOnOrAfter)) {
            return false;
        }


        List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
        if (audienceRestrictions.isEmpty()) {
            return false;
        }

        boolean audienceValid = false;
        outer: for (AudienceRestriction audienceRestriction : audienceRestrictions) {
            List<Audience> audiences = audienceRestriction.getAudiences();
            for (Audience audience : audiences) {
                if (config.getEntityID().equals(audience.getAudienceURI())) {
                    audienceValid = true;
                    break outer;
                }
            }
        }

        if (!audienceValid) {
            return false;
        }

        /*
         * TODO:
         * OneTimeUse
         * ProxyRestriction
         * Both not relevant for us? Provide an extension point (maybe even for custom conditions)?
         * Fail on unknown conditions?
         */

        return true;
    }

    private boolean hasValidAuthnStatement(Assertion assertion) {
        /*
         * The set of one or more bearer assertions MUST contain at least one <AuthnStatement> that
         * reflects the authentication of the principal to the identity provider. Multiple <AuthnStatement>
         * elements MAY be included, but the semantics of multiple statements is not defined by this profile.
         * [profiles 06 - 4.1.4.2p20]
         */
        return !assertion.getAuthnStatements().isEmpty();
    }

    private List<Assertion> decryptAndCollectAssertions(Response response) throws DecryptionException {
        List<Assertion> assertions = new LinkedList<Assertion>();
        List<EncryptedAssertion> encryptedAssertions = response.getEncryptedAssertions();
        if (encryptedAssertions.size() > 0) {
            Decrypter decrypter = getDecrypter();
            for (EncryptedAssertion encryptedAssertion : encryptedAssertions) {
                assertions.add(decrypter.decrypt(encryptedAssertion));
            }
        }

        for (Assertion assertion : response.getAssertions()) {
            assertions.add(assertion);
        }

        return assertions;
    }


    private Decrypter getDecrypter() {
        /*
         * Currently this decrypter is only able to decrypt assertions
         * that come along with their symmetric encrytion keys which are
         * in turn encrypted with the public key of 'encryptionCredential'
         */
        List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
        keyInfoProviders.add(new InlineX509DataProvider());
        keyInfoProviders.add(new KeyInfoReferenceProvider());
        keyInfoProviders.add(new DEREncodedKeyValueProvider());
        keyInfoProviders.add(new RSAKeyValueProvider());
        keyInfoProviders.add(new DSAKeyValueProvider());

        ChainingEncryptedKeyResolver encryptedKeyResolver = new ChainingEncryptedKeyResolver();
        encryptedKeyResolver.getResolverChain().add(new InlineEncryptedKeyResolver());
        encryptedKeyResolver.getResolverChain().add(new EncryptedElementTypeEncryptedKeyResolver());
        encryptedKeyResolver.getResolverChain().add(new SimpleRetrievalMethodEncryptedKeyResolver());
        encryptedKeyResolver.getResolverChain().add(new SimpleKeyInfoReferenceEncryptedKeyResolver());

        // FIXME:
        StaticKeyInfoCredentialResolver skicr = new StaticKeyInfoCredentialResolver(credentialProvider.getDecryptionCredential());
        Decrypter decrypter = new Decrypter(null, skicr, new InlineEncryptedKeyResolver());
        decrypter.setRootInNewDocument(true);
        return decrypter;
    }

    private boolean isValidBearerAssertion(Assertion assertion) {
        /*
         * Any assertion issued for consumption using this profile MUST contain a <Subject> element with at
         * least one <SubjectConfirmation> element containing a Method of
         * urn:oasis:names:tc:SAML:2.0:cm:bearer. Such an assertion is termed a bearer assertion.
         * Bearer assertions MAY contain additional <SubjectConfirmation> elements.
         * [profiles 06 - 4.1.4.2p20]
         */
        Subject subject = assertion.getSubject();
        if (subject == null) {
            return false;
        }

        List<SubjectConfirmation> bearerConfirmations = new LinkedList<SubjectConfirmation>();
        for (SubjectConfirmation confirmation : subject.getSubjectConfirmations()) {
            if ("urn:oasis:names:tc:SAML:2.0:cm:bearer".equals(confirmation.getMethod()) && confirmation.getSubjectConfirmationData() != null) {
                bearerConfirmations.add(confirmation);
            }
        }

        if (bearerConfirmations.isEmpty()) {
            return false;
        }

        /*
         * At lease one bearer <SubjectConfirmation> element MUST contain a
         * <SubjectConfirmationData> element that itself MUST contain a Recipient attribute containing
         * the service provider's assertion consumer service URL and a NotOnOrAfter attribute that limits the
         * window during which the assertion can be [E52]confirmed by the relying party. It MAY also contain an
         * Address attribute limiting the client address from which the assertion can be delivered. It MUST NOT
         * contain a NotBefore attribute. If the containing message is in response to an <AuthnRequest>,
         * then the InResponseTo attribute MUST match the request's ID.
         * [profiles 06 - 4.1.4.2p20]
         */
        List<SubjectConfirmation> validConfirmations = new LinkedList<SubjectConfirmation>();
        for (SubjectConfirmation confirmation : bearerConfirmations) {
            SubjectConfirmationData confirmationData = confirmation.getSubjectConfirmationData();
            String recipient = confirmationData.getRecipient();
            if (recipient == null) {
                continue;
            }

            if (!config.getAssertionConsumerServiceURL().equals(recipient)) {
                continue;
            }

            DateTime notOnOrAfter = confirmationData.getNotOnOrAfter();
            if (notOnOrAfter == null) {
                continue;
            }

            if (!new DateTime().isBefore(notOnOrAfter)) {
                continue;
            }

            String inResponseTo = confirmationData.getInResponseTo();
            if (inResponseTo != null) {
                // TODO validate
            }

            validConfirmations.add(confirmation);
        }


        if (validConfirmations.isEmpty()) {
            return false;
        }

        return true;
    }

}
