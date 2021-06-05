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

/**
 * Denotes the reason why a response validation failed.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public enum ValidationFailedReason {
    /**
     * The SAML request is invalid.
     */
    INVALID_REQUEST("The SAML request is invalid."),
    /**
     * The object is digitally signed but its signature cannot be verified.
     */
    INVALID_SIGNATURE("The response is digitally signed but its signature cannot be verified."),
    /**
     * A required XML element is missing.
     */
    MISSING_ELEMENT("A required XML element is missing."),
    /**
     * A required XML attribute is missing.
     */
    MISSING_ATTRIBUTE("A required XML attribute is missing."),
    /**
     * A provided XML element contains an invalid value.
     */
    INVALID_ELEMENT("A provided XML element contains an invalid value."),
    /**
     * A provided XML attribute contains an invalid value.
     */
    INVALID_ATTRIBUTE("A provided XML attribute contains an invalid value."),
    /**
     * An encrypted XML element cannot be decrypted.
     */
    DECRYPTION_FAILED("An encrypted XML element cannot be decrypted."),
    /**
     * The response contains a status code different from 'urn:oasis:names:tc:SAML:2.0:status:Success'.
     */
    RESPONSE_NOT_SUCCESSFUL("The response contains a status code different from 'urn:oasis:names:tc:SAML:2.0:status:Success'."),
    /**
     * The response contained one or more &lt;Assertion&gt; elements, but none is valid.
     */
    NO_VALID_ASSERTION_CONTAINED("The response contained one or more <Assertion> elements, but none is valid."),
    /**
     * An internal error occurred.
     */
    INTERNAL_ERROR("An internal error occurred."),
    /**
     * The response is a replay!
     */
    RESPONSE_REPLAY("The response is a replay!");

    private final String message;

    private ValidationFailedReason(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}