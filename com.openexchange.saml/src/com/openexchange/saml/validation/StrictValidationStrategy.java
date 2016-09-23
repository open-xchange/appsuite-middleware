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

package com.openexchange.saml.validation;

import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.xml.encryption.DecryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.LogoutRequestInfo;
import com.openexchange.saml.state.StateManagement;
import com.openexchange.saml.tools.CryptoHelper;
import com.openexchange.saml.tools.SignatureHelper;
import com.openexchange.saml.validation.AssertionValidators.AssertionIssuerValidator;
import com.openexchange.saml.validation.AssertionValidators.AssertionSignatureValidator;
import com.openexchange.saml.validation.ResponseValidators.ResponseDestinationValidator;
import com.openexchange.saml.validation.ResponseValidators.ResponseIssuerValidator;
import com.openexchange.saml.validation.ResponseValidators.ResponseSignatureValidator;
import com.openexchange.saml.validation.ResponseValidators.ResponseStatusCodeValidator;
import com.openexchange.saml.validation.ResponseValidators.ResponseURISignatureValidator;


/**
 * A validation strategy that tries to strictly obey the SAML 2.0 spec. It is meant as reference implementation for validating
 * SAML messages from the IDP. Several validation steps are commented with excerpts from the SAML 2.0 specification. Those
 * excerpts are always annotated with their origin. E.g. [core 06 - 1.1p7] means "Cited from core specification, working draft 06,
 * section 1.1 on page 7". The "errata composite" documents from https://wiki.oasis-open.org/security/FrontPage have been used as
 * implementation reference.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class StrictValidationStrategy implements ValidationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(StrictValidationStrategy.class);

    protected final SAMLConfig config;

    protected final CredentialProvider credentialProvider;

    protected final StateManagement stateManagement;


    /**
     * Initializes a new {@link StrictValidationStrategy}.
     * @param config The SAML configuration
     * @param credentialProvider The credential provider as in {@link SAMLBackend#getCredentialProvider()}
     * @param stateManagement The state management
     */
    public StrictValidationStrategy(SAMLConfig config, CredentialProvider credentialProvider, StateManagement stateManagement) {
        super();
        this.config = config;
        this.credentialProvider = credentialProvider;
        this.stateManagement = stateManagement;
    }

    /**
     * <p>
     *   The response validation is divided into three subsequent steps while the result of each predecessor method
     *   is passed as an argument to the successor method. Every method can abort further validation by returning an error that
     *   describes why the validation failed. The first method basically validates the response object and its contained assertions.
     *   Encrypted assertions are decrypted here. The second one looks for the first basically valid bearer assertion. In the last step
     *   that bearer assertion is checked further in terms of fulfilled conditions etc.
     * </p>
     * <p>
     *   The JavaDoc of every method denotes the validation steps that are necessary to obey the SAML 2.0 specification. Nevertheless,
     *   the IDP of a concrete deployment may not obey the spec in such a strict way and validation will fail even if the received assertions
     *   must be accepted. In these cases you need to implement your own validation strategy. Best practice is to base your implementation
     *   on this one and only remove/relax the single validation steps to let the validation pass.
     * </p>
     */
    @Override
    public AuthnResponseValidationResult validateAuthnResponse(Response response, AuthnRequestInfo requestInfo, Binding binding) throws ValidationException {
        List<Assertion> responseValidationResult = validateResponse(binding, response, requestInfo);
        Assertion bearerAssertion = determineAssertion(binding, response, responseValidationResult);
        validateBearerAssertion(binding, response, requestInfo, bearerAssertion);
        return new AuthnResponseValidationResult(bearerAssertion);
    }

    @Override
    public void validateLogoutRequest(LogoutRequest logoutRequest, HttpServletRequest httpRequest, Binding binding) throws ValidationException {
        ValidationError error = verifyLogoutRequestSignature(logoutRequest, httpRequest, binding);
        if (error != null) {
            throw error.toValidationException();
        }

        String destination = logoutRequest.getDestination();
        if (destination != null) {
            if (!config.getSingleLogoutServiceURL().equals(destination)) {
                throw new ValidationException(ValidationFailedReason.INVALID_ATTRIBUTE, "Attribute 'Destination' of LogoutRequest '" + logoutRequest.getID() + "' contains an unexpected value: " + destination);
            }
        }

        /*
         * The <Issuer> element MUST be present and MUST contain the unique identifier of the requesting entity;
         * the Format attribute MUST be omitted or have a value of urn:oasis:names:tc:SAML:2.0:nameid-format:entity.
         * [profiles 06 - 4.4.4.1p39]
         */
        Issuer issuer = logoutRequest.getIssuer();
        if (issuer == null) {
            throw new ValidationException(ValidationFailedReason.MISSING_ELEMENT, "'Issuer' is missing in LogoutRequest '" + logoutRequest.getID() + "'");
        }

        if (!config.getIdentityProviderEntityID().equals(issuer.getValue())) {
            throw new ValidationException(ValidationFailedReason.INVALID_ELEMENT, "'Issuer' of LogoutRequest '" + logoutRequest.getID() + "' contains an unexpected value: " + issuer.getValue());
        }

        DateTime notOnOrAfter = logoutRequest.getNotOnOrAfter();
        if (notOnOrAfter != null) {
            if (notOnOrAfter.isBeforeNow() || notOnOrAfter.isEqualNow()) {
                throw new ValidationException(ValidationFailedReason.INVALID_ATTRIBUTE, "Attribute 'NotOnOrAfter' contains an unexpected value: " + notOnOrAfter + " for LogoutRequest '" + logoutRequest.getID() + "'");
            }
        }

        if (logoutRequest.getBaseID() == null && logoutRequest.getNameID() == null && logoutRequest.getEncryptedID() == null) {
            throw new ValidationException(ValidationFailedReason.MISSING_ELEMENT, "Neither 'BaseID' nor 'NameID' nor 'EncryptedID' is contained in LogoutRequest '" + logoutRequest.getID() + "'");
        }
    }

    @Override
    public void validateLogoutResponse(LogoutResponse response, HttpServletRequest httpRequest, LogoutRequestInfo requestInfo, Binding binding) throws ValidationException {
        List<ResponseValidator> responseValidators = getLogoutResponseValidators(binding, response, httpRequest, requestInfo);
        ValidationError error = validateResponse(response, responseValidators);
        if (error != null) {
            throw error.toValidationException();
        }
    }

    private ValidationError verifyLogoutRequestSignature(LogoutRequest logoutRequest, HttpServletRequest httpRequest, Binding binding) {
        /*
         * The <LogoutResponse> message MUST be signed if the HTTP POST or Redirect binding is used.
         * [profiles 06 - 4.4.3.4p39]
         *
         * Nevertheless, we can only check the signature if we have according credentials...
         */
        if ((binding == Binding.HTTP_POST || binding == Binding.HTTP_REDIRECT) && credentialProvider.hasValidationCredential()) {
            if (binding == Binding.HTTP_REDIRECT) {
                // Signature is part of the URL
                return SignatureHelper.validateURISignature(httpRequest, credentialProvider.getValidationCredential());
            } else {
                if (!logoutRequest.isSigned()) {
                    return new ValidationError(ValidationFailedReason.INVALID_REQUEST, "LogoutResponse was not signed");
                }

                return SignatureHelper.validateSignature(logoutRequest, credentialProvider.getValidationCredential());
            }
        } else if (logoutRequest.isSigned() && credentialProvider.hasValidationCredential()) {
            return SignatureHelper.validateSignature(logoutRequest, credentialProvider.getValidationCredential());
        }

        return null;
    }

    /**
     * <p>
     *   Checks if the given response is basically valid and returns all contained assertions after they have possibly been decrypted.
     *   If the response was invalid or at least one assertion could not be decrypted, a validation exception is thrown.
     * </p>
     *
     * <p>Validation steps:</p>
     * <ul>
     *  <li>If the response element is signed, its signature is valid.</li>
     *  <li>If the response element contains a InResponseTo attribute, its value must match the ID of the according authentication request.</li>
     *  <li>The response element contains a destination attribute containing the ACS URL.</li>
     *  <li>If the response element contains an issuer element, its value must be equal to the IDPs entity ID.</li>
     *  <li>The response element contains a status element with a status code equal to <code>urn:oasis:names:tc:SAML:2.0:status:Success</code>.</li>
     *  <li>The response element contains one or more assertion elements which may or may not be encrypted.</li>
     *  <li>
     *    Every contained assertion must be basically valid in terms of:
     *    <ul>
     *      <li>
     *        If the assertion is signed, its signature must be valid. Whether a signature is required depends on the binding.
     *        If the response was signed, unsigned assertions inherit the response signature.
     *      </li>
     *      <li>The assertion element contains an issuer element with a value equal to the IDPs entity ID.</li>
     *    </ul>
     *  </li>
     * </ul>
     *
     * @param binding The binding via which the response was received
     * @param response The response
     * @param requestInfo The request info
     * @return All assertions contained in the response
     * @throws ValidationException If validation fails
     */
    protected List<Assertion> validateResponse(Binding binding, Response response, AuthnRequestInfo requestInfo) throws ValidationException {
        List<ResponseValidator> responseValidators = getAuthnResponseValidators(binding, response, requestInfo);
        ValidationError error = validateResponse(response, responseValidators);
        if (error != null) {
            throw error.toValidationException();
        }

        try {
            List<Assertion> allAssertions = decryptAndCollectAssertions(response);
            if (allAssertions.size() == 0) {
                throw new ValidationException(ValidationFailedReason.MISSING_ELEMENT, "Response contains neither an 'Assertion' nor an 'EncryptedAssertion' element");
            }

            List<AssertionValidator> assertionValidators = getAssertionValidators(binding, response);
            error = validateAssertions(response, allAssertions, assertionValidators);
            if (error != null) {
                throw error.toValidationException();
            }

            return allAssertions;
        } catch (DecryptionException e) {
            LOG.debug("", e);
            throw new ValidationException(ValidationFailedReason.DECRYPTION_FAILED, e.getMessage());
        }

    }

    /**
     * <p>
     *   Takes the assertions that are returned from {@link #validateAuthnResponse(Response, Binding)}, and determines the bearer assertion that
     *   asserts a certain users authentication. If none of the passed assertions is a valid bearer assertion, a validation exception is
     *   thrown.
     * </p>
     *
     * <p>Steps to determine an assertion:</p>
     * <ul>
     *   <li>
     *     The assertion contains a subject element with at least one subject confirmation whose method is equal to
     *     <code>urn:oasis:names:tc:SAML:2.0:cm:bearer</code>. The subject confirmation contains a subject confirmation data element with
     *     a recipient attribute equal to the ACS URL.
     *   </li>
     *   <li>
     *     The assertion contains an audience restriction element, which in turn contains an audience element whose value is equal to the
     *     SPs entity ID.
     *   </li>
     *   <li>The assertion contains at least one authentication statement.</li>
     * </ul>
     *
     * @param binding The binding via which the response was received
     * @param response The response
     * @param assertions The assertions, formerly returned from {@link #validateResponse(Binding, Response)}
     * @return The determined bearer assertion
     * @throws ValidationException If no bearer assertion could be determined
     */
    protected Assertion determineAssertion(Binding binding, Response response, List<Assertion> assertions) throws ValidationException {
        /*
         * We'll select the first contained assertion that
         *  - is a bearer assertion
         *  - has an audience restriction that contains us as the target audience
         *  - has an authentication statement
         */
        for (Assertion assertion : assertions) {
            /*
             * Any assertion issued for consumption using this profile MUST contain a <Subject> element with at
             * least one <SubjectConfirmation> element containing a Method of
             * urn:oasis:names:tc:SAML:2.0:cm:bearer. Such an assertion is termed a bearer assertion.
             * Bearer assertions MAY contain additional <SubjectConfirmation> elements.
             * [profiles 06 - 4.1.4.2p20]
             */
            Subject subject = assertion.getSubject();
            if (subject == null) {
                continue;
            }

            /*
             * At lease one bearer <SubjectConfirmation> element MUST contain a
             * <SubjectConfirmationData> element that itself MUST contain a Recipient attribute containing
             * the service provider's assertion consumer service URL [...].
             * [profiles 06 - 4.1.4.2p20]
             */
            boolean isBearerAssertion = findBearerConfirmation(subject) != null;
            if (isBearerAssertion) {
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
                    continue;
                }

                List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
                if (audienceRestrictions.isEmpty()) {
                    continue;
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

                if (audienceValid) {
                    /*
                     * The set of one or more bearer assertions MUST contain at least one <AuthnStatement> that
                     * reflects the authentication of the principal to the identity provider. Multiple <AuthnStatement>
                     * elements MAY be included, but the semantics of multiple statements is not defined by this profile.
                     * [profiles 06 - 4.1.4.2p20]
                     */
                    if (assertion.getAuthnStatements().size() > 0) {
                        LOG.debug("Assertion '{}' is a valid bearer assertion", assertion.getID());
                        return assertion;
                    }
                }
            }
        }

        throw new ValidationException(ValidationFailedReason.NO_VALID_ASSERTION_CONTAINED, "No contained bearer assertion meets the required conditions");
    }

    /**
     * <p>
     *   Takes the determined bearer assertion and performs some final validation steps. If any validation step fails, a validation exception is thrown.
     * </p>
     *
     * <p>Validation steps:</p>
     * <ul>
     *   <li>The subject confirmation must contain an InResponseTo attribute. The value must match the ID of the according authentication request.</li>
     *   <li>The subject confirmation must contain an NotOnOrAfter attribute, which must be fulfilled.</li>
     *   <li>If the assertion contains more conditions than the audience restriction (e.g. NotBefore, NotOnOrAfter), all conditions must be understood and met.</li>
     *   <li>The ID of the response must not have been approved before, i.e. a replay check has to be performed.</li>
     * </ul>
     *
     * @param binding The binding via which the response was received
     * @param response The response
     * @param assertion The bearer assertion
     * @return The {@link AuthnRequestInfo} according to the response based on possibly set InResponseTo attributes or <code>null</code> if InResponseTo is not set
     * @throws ValidationException If any validation step fails
     */
    protected void validateBearerAssertion(Binding binding, Response response, AuthnRequestInfo requestInfo, Assertion bearerAssertion) throws ValidationException {
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
        DateTime now = new DateTime();
        SubjectConfirmationData confirmationData = findBearerConfirmation(bearerAssertion.getSubject()).getSubjectConfirmationData();
        DateTime scNotOnOrAfter = confirmationData.getNotOnOrAfter();
        if (scNotOnOrAfter == null) {
            throw new ValidationException(ValidationFailedReason.MISSING_ATTRIBUTE, "SubjectConfirmationData contains no 'NotOnOrAfter' attribute");
        }

        if (now.isEqual(scNotOnOrAfter) || now.isAfter(scNotOnOrAfter)) {
            throw new ValidationException(ValidationFailedReason.INVALID_ATTRIBUTE, "Assertion is not valid anymore due to 'NotOnOrAfter' attribute in SubjectConfirmationData: " + scNotOnOrAfter);
        }

        String inResponseTo = confirmationData.getInResponseTo();
        if (config.isAllowUnsolicitedResponses()){
            if (inResponseTo == null) {
                LOG.debug("SubjectConfirmationData contains no 'InResponseTo' attribute, but unsolicitedResponses are allowed");
            } else if (!inResponseTo.equals(requestInfo.getRequestId())) {
                throw new ValidationException(ValidationFailedReason.INVALID_ATTRIBUTE, "SubjectConfirmationData contains invalid 'InResponseTo' attribute: " + inResponseTo);
            }
        } else {
            if (inResponseTo == null) {
                throw new ValidationException(ValidationFailedReason.MISSING_ATTRIBUTE, "SubjectConfirmationData contains no 'InResponseTo' attribute");
            }
            if (!inResponseTo.equals(requestInfo.getRequestId())) {
                throw new ValidationException(ValidationFailedReason.INVALID_ATTRIBUTE, "SubjectConfirmationData contains invalid 'InResponseTo' attribute: " + inResponseTo);
            }
        }

        /*
         * Check conditions
         */
        Conditions conditions = bearerAssertion.getConditions();
        DateTime notBefore = conditions.getNotBefore();
        if (notBefore != null && now.isBefore(notBefore)) {
            throw new ValidationException(ValidationFailedReason.INVALID_ATTRIBUTE, "Assertion is not valid anymore due to 'NotBefore' attribute in Conditions: " + notBefore);
        }

        DateTime notOnOrAfter = conditions.getNotOnOrAfter();
        if (notOnOrAfter != null && !now.isBefore(notOnOrAfter)) {
            throw new ValidationException(ValidationFailedReason.INVALID_ATTRIBUTE, "Assertion is not valid anymore due to 'NotOnOrAfter' attribute in Conditions: " + notOnOrAfter);
        }

        /*
         * The service provider MUST ensure that bearer assertions are not replayed, by maintaining the set of used
         * ID values for the length of time for which the assertion would be considered valid based on the
         * NotOnOrAfter attribute in the <SubjectConfirmationData>.
         * [profiles 06 - 4.1.4.2p21]
         */
        checkForReplayAttack(response);
    }

    /**
     * Override this method to change the basic validation steps for the authentication response objects.
     *
     * @param binding The binding via which the response was received
     * @param response The response
     * @param requestInfo The request info
     * @return The list of {@link ResponseValidator}s used to the response
     */
    protected List<ResponseValidator> getAuthnResponseValidators(Binding binding, Response response, AuthnRequestInfo requestInfo) {
        List<ResponseValidator> responseValidators = new LinkedList<ResponseValidator>();

        /*
         * If response is signed, we need to verify the signature
         */
        responseValidators.add(new ResponseSignatureValidator(credentialProvider.getValidationCredential(), false));

        /*
         * If the message is signed, the Destination XML attribute in the root SAML element of the protocol
         * message MUST contain the URL to which the sender has instructed the user agent to deliver the
         * message. The recipient MUST then verify that the value matches the location at which the message has
         * been received.
         * [bindings 05 - 3.5.5.2p24]
         */
        boolean allowNullDestination = !(binding == Binding.HTTP_POST && response.isSigned());
        responseValidators.add(new ResponseDestinationValidator(config.getAssertionConsumerServiceURL(), allowNullDestination));

        /*
         * The status code of the response must be 'urn:oasis:names:tc:SAML:2.0:status:Success'
         */
        responseValidators.add(new ResponseStatusCodeValidator());

        /*
         * If the <Response> message is signed or
         * if an enclosed assertion is encrypted, then the <Issuer> element MUST be present. Otherwise it
         * MAY be omitted. If present it MUST contain the unique identifier of the issuing identity provider; the
         * Format attribute MUST be omitted or have a value of urn:oasis:names:tc:SAML:2.0:nameid-format:entity.
         * [profiles 06 - 4.1.4.2p19]
         */
        boolean allowNullIssuer = !(response.isSigned() || response.getEncryptedAssertions().size() > 0);
        responseValidators.add(new ResponseIssuerValidator(config.getIdentityProviderEntityID(), allowNullIssuer));

        /*
         * A reference to the identifier of the request to which the response corresponds, if any. If the response
         * is not generated in response to a request, or if the ID attribute value of a request cannot be
         * determined (for example, the request is malformed), then this attribute MUST NOT be present.
         * Otherwise, it MUST be present and its value MUST match the value of the corresponding request's ID
         * attribute.
         * [core 06 - 3.2.2p38]
         */
        responseValidators.add(new ResponseValidators.InResponseToValidator(requestInfo.getRequestId(), config.isAllowUnsolicitedResponses()));

        return responseValidators;
    }

    /**
     * Override this method to change the basic validation steps for the logout response objects.
     *
     * @param binding The binding via which the response was received
     * @param response The response
     * @param httpRequest The servlet request
     * @param requestInfo The request info
     * @return The list of {@link ResponseValidator}s used to the response
     */
    protected List<ResponseValidator> getLogoutResponseValidators(Binding binding, StatusResponseType response, HttpServletRequest httpRequest, LogoutRequestInfo requestInfo) {
        List<ResponseValidator> responseValidators = new LinkedList<ResponseValidator>();

        /*
         * The responder MUST authenticate itself to the requester and ensure message integrity, either by signing
         * the message or using a binding-specific mechanism.
         * [profiles 06 - 4.4.4.1p39]
         *
         * If the message is signed, the Destination XML attribute in the root SAML element of the protocol
         * message MUST contain the URL to which the sender has instructed the user agent to deliver the
         * message. The recipient MUST then verify that the value matches the location at which the message has
         * been received.
         * [bindings 05 - 3.4.5.2p19/3.5.5.2p24]
         */
        if (binding == Binding.HTTP_REDIRECT) {
            responseValidators.add(new ResponseURISignatureValidator(credentialProvider.getValidationCredential(), httpRequest, true));
            responseValidators.add(new ResponseDestinationValidator(config.getSingleLogoutServiceURL(), httpRequest.getParameter("Signature") == null));
        } else {
            responseValidators.add(new ResponseSignatureValidator(credentialProvider.getValidationCredential(), true));
            responseValidators.add(new ResponseDestinationValidator(config.getSingleLogoutServiceURL(), !response.isSigned()));
        }

        /*
         * The status code of the response must be 'urn:oasis:names:tc:SAML:2.0:status:Success'
         */
        responseValidators.add(new ResponseStatusCodeValidator());

        /*
         * The <Issuer> element MUST be present and MUST contain the unique identifier of the responding entity;
         * the Format attribute MUST be omitted or have a value of urn:oasis:names:tc:SAML:2.0:nameid-format:entity.
         * [profiles 06 - 4.4.4.1p39]
         */
        responseValidators.add(new ResponseIssuerValidator(config.getIdentityProviderEntityID(), false));

        /*
         * A reference to the identifier of the request to which the response corresponds, if any. If the response
         * is not generated in response to a request, or if the ID attribute value of a request cannot be
         * determined (for example, the request is malformed), then this attribute MUST NOT be present.
         * Otherwise, it MUST be present and its value MUST match the value of the corresponding request's ID
         * attribute.
         * [core 06 - 3.2.2p38]
         *
         * We don't support unsolicited responses, so InResponseTo must be set
         */
        responseValidators.add(new ResponseValidators.InResponseToValidator(requestInfo.getRequestId(), false));

        return responseValidators;
    }

    /**
     * Override this method to change the basic validation steps for the assertion objects.
     *
     * @param binding The binding via which the response was received
     * @param response The response
     * @return The list of {@link AssertionValidator}s used to validate every single assertion
     */
    protected List<AssertionValidator> getAssertionValidators(Binding binding, Response response) {
        List<AssertionValidator> assertionValidators = new LinkedList<AssertionValidator>();

        /*
         * The <Assertion> element(s) in the <Response> MUST be signed, if the HTTP POST binding is used,
         * and MAY be signed if the HTTP-Artifact binding is used.
         * [profiles 06 - 4.1.3.5p18]
         */
        boolean enforceSignature = (binding == Binding.HTTP_POST);
        assertionValidators.add(new AssertionSignatureValidator(credentialProvider.getValidationCredential(), enforceSignature));

        /*
         * Check the assertions issuers
         */
        assertionValidators.add(new AssertionIssuerValidator(config.getIdentityProviderEntityID()));
        return assertionValidators;
    }

    /**
     * Checks if the given response has been replayed and throws a validation exception if so.
     *
     * @param response The response
     * @throws ValidationException
     */
    protected void checkForReplayAttack(Response response) throws ValidationException {
        try {
            if (stateManagement.hasAuthnResponseID(response.getID())) {
                throw new ValidationException(ValidationFailedReason.RESPONSE_REPLAY, response.getID());
            }
        } catch (OXException e) {
            LOG.error("", e);
            throw new ValidationException(ValidationFailedReason.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    protected SubjectConfirmation findBearerConfirmation(Subject subject) {
        for (SubjectConfirmation confirmation : subject.getSubjectConfirmations()) {
            SubjectConfirmationData subjectConfirmationData = confirmation.getSubjectConfirmationData();
            if ("urn:oasis:names:tc:SAML:2.0:cm:bearer".equals(confirmation.getMethod()) && subjectConfirmationData != null) {
                if (config.getAssertionConsumerServiceURL().equals(subjectConfirmationData.getRecipient())) {
                    return confirmation;
                }
            }
        }

        return null;
    }

    private static ValidationError validateResponse(StatusResponseType response, List<ResponseValidator> responseValidators) {
        for (ResponseValidator responseValidator : responseValidators) {
            ValidationError error = responseValidator.validate(response);
            if (error != null) {
                return error;
            }
        }

        return null;
    }

    private static ValidationError validateAssertions(Response response, List<Assertion> assertions,List<AssertionValidator> assertionValidators) {
        for (Assertion assertion : assertions) {
            for (AssertionValidator assertionValidator : assertionValidators) {
                ValidationError error = assertionValidator.validate(response, assertion);
                if (error != null) {
                    return error;
                }
            }
        }

        return null;
    }

    private List<Assertion> decryptAndCollectAssertions(Response response) throws DecryptionException {
        List<Assertion> assertions = new LinkedList<Assertion>();
        List<EncryptedAssertion> encryptedAssertions = response.getEncryptedAssertions();
        if (encryptedAssertions.size() > 0) {
            Decrypter decrypter = CryptoHelper.getDecrypter(credentialProvider);
            for (EncryptedAssertion encryptedAssertion : encryptedAssertions) {
                assertions.add(decrypter.decrypt(encryptedAssertion));
            }
        }

        for (Assertion assertion : response.getAssertions()) {
            assertions.add(assertion);
        }

        return assertions;
    }

}
