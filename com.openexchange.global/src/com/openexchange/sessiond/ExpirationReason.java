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

package com.openexchange.sessiond;

import com.openexchange.java.Strings;

/**
 * {@link ExpirationReason} - An enumeration of known reasons for advertising session expiration to a client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public enum ExpirationReason {

    /**
     * There is no such session for passed session identifier.
     */
    NO_SUCH_SESSION("nosession"),
    /**
     * Session timed out.
     */
    TIMED_OUT("timeout"),
    /**
     * The IP check failed.
     */
    IP_CHECK_FAILED("ipcheck"),
    /**
     * Session-associated context does not or no more exist.
     */
    CONTEXT_NONEXISTENT("usernonexistent"),
    /**
     * Session-associated user has been disabled.
     */
    USER_DISABLED("userdisabled"),
    /**
     * Session-associated user does not or no more exist.
     */
    USER_NONEXISTENT("usernonexistent"),
    /**
     * Client did not pass an expected secret cookie conform to <code>"open-xchange-secret-" + &lt;calculated-hash&gt;</code>
     */
    NO_EXPECTED_SECRET_COOKIE("nosecretcookie"),
    /**
     * Client passed a secret string in its secret cookie that does not match the one held by session.
     */
    SECRET_MISMATCH("secretmismatch"),
    /**
     * Session contains an invalid refresh token or a permanent error occurred while trying to refresh tokens for session.
     */
    OAUTH_TOKEN_REFRESH_FAILED("oauthtokenrefresh"),
    ;

    private final String identifier;

    private ExpirationReason(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets this reason's identifier.
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Determines the appropriate expiration reason for given identifier.
     *
     * @param expirationReason The identifier to look-up
     * @return The expiration reason or <code>null</code>
     */
    public static ExpirationReason expirationReasonFor(String expirationReason) {
        if (Strings.isEmpty(expirationReason)) {
            return null;
        }

        String lc = Strings.asciiLowerCase(expirationReason);
        for (ExpirationReason er : ExpirationReason.values()) {
            if (er.getIdentifier().equals(lc)) {
                return er;
            }
        }
        return null;
    }

}
