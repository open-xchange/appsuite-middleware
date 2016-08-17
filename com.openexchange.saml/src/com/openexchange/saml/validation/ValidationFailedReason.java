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