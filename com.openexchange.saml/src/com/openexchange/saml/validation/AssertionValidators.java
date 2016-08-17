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

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.security.credential.Credential;
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

    private static final Logger LOG = LoggerFactory.getLogger(AssertionValidators.class);

    /**
     * Verifies the signature of the given assertion against the given validation credential.
     * Signatures can be inherited from the according response object, so make sure that a
     * possibly response signature has been verified before.
     */
    public static final class AssertionSignatureValidator implements AssertionValidator {

        private final Credential validationCredential;

        private final boolean enforceSignature;

        /**
         * @param validationCredential The credential containing a public key to verify the signature
         * @param enforceSignature Whether either the assertion or its response must be signed
         */
        public AssertionSignatureValidator(Credential validationCredential, boolean enforceSignature) {
            super();
            this.validationCredential = validationCredential;
            this.enforceSignature = enforceSignature;
        }

        @Override
        public ValidationError validate(Response response, Assertion assertion) {
            String assertionID = assertion.getID();
            if (assertion.isSigned()) {
                ValidationError error = SignatureHelper.validateSignature(assertion, validationCredential);
                if (error == null) {
                    LOG.debug("Assertion '{}' contains a valid signature", assertionID);
                } else if (error.getThrowable() != null) {
                    LOG.debug("", error.getThrowable());
                }
                return error;
            } else {
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
            } else {
                String issuerFormat = issuer.getFormat();
                if (issuerFormat != null && !NameIDType.ENTITY.equals(issuerFormat)) {
                    return new ValidationError(ValidationFailedReason.INVALID_ELEMENT, "'Issuer' of assertion '" + assertionID + "' has unexpected format: " + issuerFormat);
                }

                String issuerValue = issuer.getValue();
                if (!issuerValue.equals(expected)) {
                    return new ValidationError(ValidationFailedReason.INVALID_ELEMENT, "'Issuer' of assertion '" + assertionID + "' has unexpected value: " + issuerValue);
                }

                LOG.debug("Assertion '{}' contains a valid 'Issuer' element: {}", assertionID, issuerValue);
            }

            return null;
        }
    }

}
