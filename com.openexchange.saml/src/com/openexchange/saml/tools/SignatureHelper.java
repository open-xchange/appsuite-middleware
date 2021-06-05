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

package com.openexchange.saml.tools;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.SigningUtil;
import org.opensaml.xmlsec.algorithm.AlgorithmSupport;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import com.openexchange.saml.validation.ValidationError;
import com.openexchange.saml.validation.ValidationFailedReason;

/**
 * {@link SignatureHelper}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SignatureHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureHelper.class);

    /**
     * Validates the signature of a {@link SignableXMLObject} with the passed {@link Credential}.
     *
     * @param object The signed object
     * @param credentials The credential list
     * @return A {@link ValidationError} if signature validation fails or <code>null</code> if the signature is valid
     */
    public static ValidationError validateSignature(SignableXMLObject object, List<Credential> credentials) {
        try {
            Signature signature = object.getSignature();
            validateWithCredentials(credentials, signature);
        } catch (SignatureException e) {
            return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, e.getMessage(), e);
        }

        return null;
    }

    private static void validateWithCredentials(List<Credential> credentials, Signature signature) throws SignatureException {
        boolean isValid = false;

        Iterator<Credential> iterator = credentials.iterator();
        while (iterator.hasNext()) {
            Credential credential = iterator.next();
            try {
                SignatureValidator.validate(signature, credential);
                isValid = true;
            } catch (SignatureException e) {
                if (iterator.hasNext() == false && isValid == false) {
                    throw e;
                }
            }
        }
    }

    /**
     * Validates the signature of a HTTP-Redirect request.
     *
     * @param httpRequest The HTTP request
     * @param credentials The credential list
     * @return A {@link ValidationError} if signature validation fails or <code>null</code> if the signature is valid
     */
    public static ValidationError validateURISignature(HttpServletRequest httpRequest, List<Credential> credentials) {
        String sigAlgUri = httpRequest.getParameter("SigAlg");
        if (sigAlgUri == null) {
            return new ValidationError(ValidationFailedReason.INVALID_REQUEST, "Parameter 'SigAlg' is not set");
        }

        String signature = httpRequest.getParameter("Signature");
        if (signature == null) {
            return new ValidationError(ValidationFailedReason.INVALID_REQUEST, "Parameter 'Signature' is not set");
        }

        String queryString = httpRequest.getQueryString();
        StringBuilder signedContent = new StringBuilder();
        if (httpRequest.getParameter("SAMLRequest") != null) {
            signedContent.append(HTTPTransportUtils.getRawQueryStringParameter(queryString, "SAMLRequest"));
        } else {
            signedContent.append(HTTPTransportUtils.getRawQueryStringParameter(queryString, "SAMLResponse"));
        }
        String rawRelayState = HTTPTransportUtils.getRawQueryStringParameter(queryString, "RelayState");
        if (rawRelayState != null) {
            signedContent.append('&').append(rawRelayState);
        }
        signedContent.append('&').append(HTTPTransportUtils.getRawQueryStringParameter(queryString, "SigAlg"));

        return getValidationError(credentials, sigAlgUri, signature, signedContent);
    }

    private static ValidationError getValidationError(List<Credential> credentials, String sigAlgUri, String signature, StringBuilder signedContent) {
        try {
            boolean isInvalid = true;
            final boolean isHMAC = AlgorithmSupport.isHMAC(sigAlgUri);
            final String jcaAlgorithmID = AlgorithmSupport.getAlgorithmID(sigAlgUri);
            for (final Credential credential : credentials) {
                if (SigningUtil.verify(credential, jcaAlgorithmID, isHMAC, Base64.decodeBase64(signature), signedContent.toString().getBytes("UTF-8"))) {
                    isInvalid = false;
                } else {
                    String fingerprint = Strings.isEmpty(credential.getEntityId()) ? "" : credential.getEntityId();
                    LOG.debug("Invalid certificate found in keystore with fingerprint {}", fingerprint);
                }
            }
            if (isInvalid) {
                return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, "The signatures do not match the signed content");
            }
        } catch (UnsupportedEncodingException | org.opensaml.security.SecurityException e) {
            return new ValidationError(ValidationFailedReason.INVALID_SIGNATURE, e.getMessage(), e);
        }

        return null;
    }

}
