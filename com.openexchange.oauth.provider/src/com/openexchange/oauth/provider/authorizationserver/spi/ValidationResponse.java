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
         * The received token is rejected, i.e. token validation failed.
         */
        INVALID,
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
