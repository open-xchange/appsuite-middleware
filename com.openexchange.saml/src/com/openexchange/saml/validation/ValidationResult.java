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

import org.opensaml.saml2.core.Assertion;


/**
 * Encapsulates the result of a response validation. If the validation was successful, the
 * detected bearer assertion can be obtained. If the validation failed, details about what
 * led to the failure are provided.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface ValidationResult {

    public enum ErrorReason {
        /**
         * The response is digitally signed but its signature cannot be verified.
         */
        INVALID_RESPONSE_SIGNATURE("The response is digitally signed but its signature cannot be verified."),
        /**
         * The assertion is digitally signed but its signature cannot be verified.
         */
        INVALID_ASSERTION_SIGNATURE("The assertion is digitally signed but its signature cannot be verified."),
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
        NO_VALID_ASSERTION_CONTAINED("The response contained one or more <Assertion> elements, but none is valid.");

        private final String message;

        private ErrorReason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Checks whether the according response is considered valid.
     *
     * @return <code>true</code> if the response is valid, otherwise <code>false</code>
     */
    boolean success();

    /**
     * Gets the reason why the response is considered invalid.
     *
     * @return The reason or <code>null</code> if the response is valid
     */
    ErrorReason getErrorReason();

    /**
     * Gets a detailed error message that further describes the error reason.
     *
     * @return The detail message or <code>null</code> if the response is valid
     */
    String getErrorDetail();

    /**
     * Gets the bearer assertion which caused the according response to be considered valid.
     *
     * @return The assertion. If the response is invalid, <code>null</code> is returned.
     */
    Assertion getBearerAssertion();

}
