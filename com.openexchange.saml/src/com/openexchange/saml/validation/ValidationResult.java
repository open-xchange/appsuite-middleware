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
import com.openexchange.saml.state.AuthnRequestInfo;


/**
 * The result object of a response validation via {@link ValidationStrategy}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ValidationResult {

    private final Assertion bearerAssertion;

    private final AuthnRequestInfo requestInfo;

    /**
     * Initializes a new {@link ValidationResult}.
     * @param bearerAssertion
     * @param requestInfo
     */
    public ValidationResult(Assertion bearerAssertion, AuthnRequestInfo requestInfo) {
        super();
        this.bearerAssertion = bearerAssertion;
        this.requestInfo = requestInfo;
    }

    /**
     * Initializes a new {@link ValidationResult}.
     * @param bearerAssertion
     * @param requestInfo
     */
    public ValidationResult(Assertion bearerAssertion) {
        super();
        this.bearerAssertion = bearerAssertion;
        this.requestInfo = null;
    }

    /**
     * Gets the bearer assertion determined during validation of the authentication response.
     *
     * @return The bearer assertion
     */
    public Assertion getBearerAssertion() {
        return bearerAssertion;
    }

    /**
     * Gets the {@link AuthnRequestInfo} of the authentication request according to the validated response.
     *
     * @return The authentication info or <code>null</code> if no InResponseTo were contained.
     */
    public AuthnRequestInfo getRequestInfo() {
        return requestInfo;
    }


}
