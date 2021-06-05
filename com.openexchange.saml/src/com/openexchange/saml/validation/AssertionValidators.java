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

package com.openexchange.saml.validation;

import java.util.List;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.saml.tools.SignatureHelper;


/**
 * Container for common assertion validators.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class AssertionValidators {

    /** The logger constant */
    static final Logger LOG = LoggerFactory.getLogger(AssertionValidators.class);

    /**
     * Verifies the signature of the given assertion against the given validation credential.
     * Signatures can be inherited from the according response object, so make sure that a
     * possibly response signature has been verified before.
     */
    public static final class AssertionSignatureValidator implements AssertionValidator {

        private final List<Credential> validationCredentials;

        private final boolean enforceSignature;

        /**
         * @param validationCredentials The credential list, containing public keys to verify the signature
         * @param enforceSignature Whether either the assertion or its response must be signed
         */
        public AssertionSignatureValidator(List<Credential> validationCredentials, boolean enforceSignature) {
            super();
            this.validationCredentials = validationCredentials;
            this.enforceSignature = enforceSignature;
        }

        @Override
        public ValidationError validate(Response response, Assertion assertion) {
            String assertionID = assertion.getID();
            if (assertion.isSigned()) {
                ValidationError error = SignatureHelper.validateSignature(assertion, validationCredentials);
                if (error == null) {
                    LOG.debug("Assertion '{}' contains a valid signature", assertionID);
                } else if (error.getThrowable() != null) {
                    LOG.debug("", error.getThrowable());
                }
                return error;
            }
            /*
             * A SAML assertion may be embedded within another SAML element, such as an enclosing <Assertion>
             * or a request or response, which may be signed. When a SAML assertion does not contain a
             * <ds:Signature> element, but is contained in an enclosing SAML element that contains a
             * <ds:Signature> element, and the signature applies to the <Assertion> element and all its children,
             * then the assertion can be considered to inherit the signature from the enclosing element. The resulting
             * interpretation should be equivalent to the case where the assertion itself was signed with the same key
             * and signature options.
             * [core 06 - 5.3p70/71]
             */
            if (!response.isSigned() && enforceSignature) {
                return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, "Assertion '" + assertionID + "' is not signed");
            }

            return null;
        }
    }

    /**
     * Validates the issuer of an assertion
     */
    public static final class AssertionIssuerValidator implements AssertionValidator {

        private final String expected;

        /**
         * @param expected The expected issuer value
         */
        public AssertionIssuerValidator(String expected) {
            super();
            this.expected = expected;
        }

        @Override
        public ValidationError validate(Response response, Assertion assertion) {
            String assertionID = assertion.getID();
            Issuer issuer = assertion.getIssuer();
            if (issuer == null) {
                return new ValidationError(ValidationFailedReason.MISSING_ELEMENT, "'Issuer' is missing in assertion '" + assertionID + "'");
            }
            String issuerFormat = issuer.getFormat();
            if (issuerFormat != null && !NameIDType.ENTITY.equals(issuerFormat)) {
                return new ValidationError(ValidationFailedReason.INVALID_ELEMENT, "'Issuer' of assertion '" + assertionID + "' has unexpected format: " + issuerFormat);
            }

            String issuerValue = issuer.getValue();
            if (!issuerValue.equals(expected)) {
                return new ValidationError(ValidationFailedReason.INVALID_ELEMENT, "'Issuer' of assertion '" + assertionID + "' has unexpected value: " + issuerValue);
            }

            LOG.debug("Assertion '{}' contains a valid 'Issuer' element: {}", assertionID, issuerValue);

            return null;
        }
    }

}
