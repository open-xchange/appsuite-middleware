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

package com.openexchange.oauth.provider.impl.tools;

import static com.openexchange.java.Autoboxing.I;
import java.util.UUID;
import java.util.regex.Pattern;
import com.openexchange.java.util.UUIDs;

/**
 * A token that consists of a context ID, a user ID and a random part.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class UserizedToken {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-f0-9]{48}", Pattern.CASE_INSENSITIVE);

    private static int getContextObfuscator(String baseToken) {
        return Integer.parseInt(baseToken.substring(0, 7), 16);
    }

    private static int getUserObfuscator(String baseToken) {
        return Integer.parseInt(baseToken.substring(7, 14), 16);
    }

    /**
     * Initializes a new {@link UserizedToken}, based on the supplied share token.
     * See {@link UserizedToken#isValid(String)} for validating tokens before parsing
     * them.
     *
     * @param token The token
     * @throws IllegalArgumentException If token is invalid
     */
    public static UserizedToken parse(String token) {
        if (!isValid(token)) {
            throw new IllegalArgumentException("Invalid token string: " + token);
        }

        String baseToken = token.substring(16, 48);
        int contextId = Integer.parseInt(token.substring(0, 8), 16) ^ getContextObfuscator(baseToken);
        int userId = Integer.parseInt(token.substring(8, 16), 16) ^ getUserObfuscator(baseToken);
        return new UserizedToken(userId, contextId, baseToken);
    }

    /**
     * Initializes a new {@link UserizedToken} with a randomly
     * generated base token.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static UserizedToken generate(int userId, int contextId) {
        return new UserizedToken(userId, contextId, UUIDs.getUnformattedString(UUID.randomUUID()));
    }

    /**
     * Checks if the given token string is a valid {@link UserizedToken} in terms of its syntax.
     * The existence of an according grant or the validity of the contained user information is
     * not checked.
     *
     * @param token the token
     * @return <code>true</code> if the token is valid
     */
    public static boolean isValid(String token) {
        if (null == token || 48 != token.length() || false == TOKEN_PATTERN.matcher(token).matches()) {
            return false;
        }

        return true;
    }

    // ---------------------------------------------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final String baseToken;

    /**
     * Initializes a new {@link UserizedToken}. Don't generate new base tokens on your own,
     * always use {@link UserizedToken#generate(int, int)}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param baseToken The base token
     */
    public UserizedToken(int userId, int contextId, String baseToken) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.baseToken = baseToken;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the token with context and user IDs encoded.
     *
     * @return The token
     */
    public String getToken() {
        return String.format("%1$08x%2$08x%3$s", I(contextId ^ getContextObfuscator(baseToken)), I(userId ^ getUserObfuscator(baseToken)), baseToken);
    }

    /**
     * Gets the base token, i.e. the random part of the token without encoded information.
     *
     * @return The token
     */
    public String getBaseToken() {
        return baseToken;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseToken == null) ? 0 : baseToken.hashCode());
        result = prime * result + contextId;
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserizedToken)) {
            return false;
        }
        UserizedToken other = (UserizedToken) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (baseToken == null) {
            if (other.baseToken != null) {
                return false;
            }
        } else if (!baseToken.equals(other.baseToken)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserizedToken [contextID=" + contextId + ", userID=" + userId + ", baseToken=" + baseToken + "]";
    }

}
