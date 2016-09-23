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

import javax.servlet.http.HttpServletRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.xml.security.credential.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.saml.tools.SignatureHelper;


/**
 * A container for common response validators.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ResponseValidators {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseValidator.class);

    /**
     * Verifies the signature of the given response object against the given validation credential.
     */
    public static final class ResponseSignatureValidator implements ResponseValidator {

        private final Credential validationCredential;

        private final boolean enforceSignature;

        /**
         * @param validationCredential The credential containing a public key to verify the signature
         * @param enforceSignature Whether validation should fail if response is not signed
         */
        public ResponseSignatureValidator(Credential validationCredential, boolean enforceSignature) {
            super();
            this.validationCredential = validationCredential;
            this.enforceSignature = enforceSignature;
        }

        @Override
        public ValidationError validate(StatusResponseType response) {
            /*
             * All SAML protocol request and response messages MAY be signed using XML Signature.
             * [core 06 - 5.1p70]
             */
            if (response.isSigned()) {
                ValidationError error = SignatureHelper.validateSignature(response, validationCredential);
                if (error == null) {
                    LOG.debug("Response is signed and the signature is valid");
                } else if (error.getThrowable() != null) {
                    LOG.debug("", error.getThrowable());
                }

                return error;
            } else if (enforceSignature) {
                return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, "Response '" + response.getID() + "' is not signed");
            }

            return null;
        }
    }

    /**
     * Verifies the signature of a HTTP request URI against the given validation credential.
     */
    public static final class ResponseURISignatureValidator implements ResponseValidator {

        private final Credential validationCredential;

        private final HttpServletRequest httpRequest;

        private final boolean enforceSignature;

        /**
         * @param validationCredential The credential containing a public key to verify the signature
         * @param httpRequest The servlet request containing the signed request URI
         * @param enforceSignature Whether validation should fail if response is not signed
         */
        public ResponseURISignatureValidator(Credential validationCredential, HttpServletRequest httpRequest, boolean enforceSignature) {
            super();
            this.validationCredential = validationCredential;
            this.httpRequest = httpRequest;
            this.enforceSignature = enforceSignature;
        }

        @Override
        public ValidationError validate(StatusResponseType response) {
            /*
             * All SAML protocol request and response messages MAY be signed using XML Signature.
             * [core 06 - 5.1p70]
             *
             *
             * Any signature on the SAML protocol message, including the <ds:Signature> XML element itself,
             * MUST be removed. [...]
             *
             * If the original SAML protocol message was signed using an XML digital signature, a new signature
             * covering the encoded data as specified above MUST be attached[...]
             * [bindings 05 - 3.4.4.1p17]
             */
            String signature = httpRequest.getParameter("Signature");
            if (signature != null) {
                ValidationError error = SignatureHelper.validateURISignature(httpRequest, validationCredential);
                if (error == null) {
                    LOG.debug("Response is signed via request URI and the signature is valid");
                } else if (error.getThrowable() != null) {
                    LOG.debug("", error.getThrowable());
                }

                return error;
            } else if (enforceSignature) {
                return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, "Response '" + response.getID() + "' is not signed via request URI");
            }

            return null;
        }
    }

    /**
     * Validates the destination attribute of the given response
     */
    public static final class ResponseDestinationValidator implements ResponseValidator {

        private final String expected;

        private final boolean allowNull;

        /**
         * @param expected The expected destination value
         * @param allowNull <code>true</code> if <code>null</code> is allowed for the actual destination value
         */
        public ResponseDestinationValidator(String expected, boolean allowNull) {
            super();
            this.expected = expected;
            this.allowNull = allowNull;
        }

        @Override
        public ValidationError validate(StatusResponseType response) {
            String actual = response.getDestination();
            if (actual == null) {
                if (allowNull) {
                    return null;
                }

                return new ValidationError(ValidationFailedReason.MISSING_ATTRIBUTE, "'Destination' is not set in response");
            }

            /*
             * URI reference indicating the address to which this response has been sent. This is useful to prevent
             * malicious forwarding of responses to unintended recipients, a protection that is required by some
             * protocol bindings. If it is present, the actual recipient MUST check that the URI reference identifies the
             * location at which the message was received. If it does not, the response MUST be discarded. Some
             * protocol bindings may require the use of this attribute (see [SAMLBind]).
             * [core 06 - 3.2.2p38]
             */
            if (actual.equals(expected)) {
                LOG.debug("Response contains a valid 'Destination' attribute");
            } else {
                return new ValidationError(ValidationFailedReason.INVALID_ATTRIBUTE, "'Destination' attribute of response contains an unexpected value: " + actual);
            }

            return null;
        }

    }

    /**
     * Validates the status code of the given response
     */
    public static final class ResponseStatusCodeValidator implements ResponseValidator {

        public ResponseStatusCodeValidator() {
            super();
        }

        @Override
        public ValidationError validate(StatusResponseType response) {
            Status status = response.getStatus();
            if (status == null) {
                return new ValidationError(ValidationFailedReason.MISSING_ELEMENT, "'Status' is missing in response");
            }

            String statusCodeValue = status.getStatusCode().getValue();
            if (StatusCode.SUCCESS_URI.equals(statusCodeValue)) {
                LOG.debug("Response status is 'Success'");
            } else {
                String statusMessage = "none";
                StatusMessage message = status.getStatusMessage();
                if (message != null) {
                    statusMessage = message.getMessage();
                }

                return new ValidationError(ValidationFailedReason.RESPONSE_NOT_SUCCESSFUL, "Status code: " + statusCodeValue + ", message: " + statusMessage);
            }

            return null;
        }

    }

    /**
     * Validates the issuer of the given response
     */
    public static final class ResponseIssuerValidator implements ResponseValidator {

        private final String expected;

        private final boolean allowNull;

        /**
         * @param expected The expected issuer value
         * @param allowNull <code>true</code> if the actual issuer may be <code>null</code>
         */
        public ResponseIssuerValidator(String expected, boolean allowNull) {
            super();
            this.expected = expected;
            this.allowNull = allowNull;
        }

        @Override
        public ValidationError validate(StatusResponseType response) {
            Issuer issuer = response.getIssuer();
            if (issuer == null) {
                if (allowNull) {
                    return null;
                }
                return new ValidationError(ValidationFailedReason.MISSING_ELEMENT, "'Issuer' is missing in response");
            }

            String issuerFormat = issuer.getFormat();
            if (issuerFormat != null && !NameIDType.ENTITY.equals(issuerFormat)) {
                return new ValidationError(ValidationFailedReason.INVALID_ELEMENT, "'Issuer' has unexpected format: " + issuerFormat);
            }

            String issuerValue = issuer.getValue();
            if (!issuerValue.equals(expected)) {
                return new ValidationError(ValidationFailedReason.INVALID_ELEMENT, "'Issuer' has unexpected value: " + issuerValue);
            }

            LOG.debug("Response contains a valid 'Issuer' element: {}", issuerValue);
            return null;
        }
    }

    /**
     * Validates the issuer of the given response
     */
    public static final class InResponseToValidator implements ResponseValidator {

        private final String expected;

        private final boolean allowNull;

        /**
         * @param expected The expected issuer value
         * @param allowNull <code>true</code> if InResponseTo may be <code>null</code> within the response
         */
        public InResponseToValidator(String expected, boolean allowNull) {
            super();
            this.expected = expected;
            this.allowNull = allowNull;
        }

        @Override
        public ValidationError validate(StatusResponseType response) {
            String inResponseTo = response.getInResponseTo();
            if (inResponseTo == null) {
                if (allowNull) {
                    return null;
                }
                return new ValidationError(ValidationFailedReason.MISSING_ATTRIBUTE, "'InResponseTo' is missing in response");
            } else {
                if (inResponseTo.equals(expected)) {
                    LOG.debug("Response has valid 'InResponseTo' attribute: {}", response.getID(), inResponseTo);
                } else {
                    return new ValidationError(ValidationFailedReason.INVALID_ATTRIBUTE, "'InResponseTo' attribute of response contains unexpected value: " + inResponseTo);
                }
            }

            return null;
        }
    }

}
