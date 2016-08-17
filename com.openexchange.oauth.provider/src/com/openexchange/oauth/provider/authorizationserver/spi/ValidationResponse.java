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

package com.openexchange.oauth.provider.authorizationserver.spi;

import java.util.List;

/**
 * Encapsulates the response of an access token validation. Validation is only considered
 * successful if the token status {@link TokenStatus#VALID} is returned. All other statuses
 * will result in according error responses.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.1
 * @see DefaultValidationResponse
 */
public interface ValidationResponse {

    /**
     * Status of the checked access token.
     */
    public static enum TokenStatus {
        /**
         * The token is unknown, i.e. has not been issued by the IDM in the near past.
         */
        UNKNOWN,
        /**
         * The token is invalid in terms of syntax.
         */
        MALFORMED,
        /**
         * The token was valid in the past but is already expired.
         */
        EXPIRED,
        /**
         * The token is valid.
         */
        VALID
    }

    /**
     * Gets the validation status of the token.
     *
     * @return The status
     */
    TokenStatus getTokenStatus();

    /**
     * Gets the according OX users context ID.
     *
     * @return The context ID
     */
    int getContextId();

    /**
     * Gets the according OX users ID.
     *
     * @return The user ID
     */
    int getUserId();

    /**
     * Gets the granted scope as a list of scope tokens.
     *
     * @return The scope; never <code>null</code> or empty
     */
    List<String> getScope();

    /**
     * Gets the name of the client application this grant was created for.
     * The name becomes part of the according session and is therefore tracked
     * by the last login recorder.
     *
     * @return The client name; never <code>null</code>
     */
    String getClientName();

}
