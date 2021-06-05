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

package com.openexchange.session.oauth;

import com.openexchange.exception.OXException;

/**
 * {@link TokenRefreshResponse}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class TokenRefreshResponse {

    /** The constant for the special token refresh response for missing refresh token */
    public static TokenRefreshResponse MISSING_REFRESH_TOKEN = new TokenRefreshResponse(new Error(ErrorType.PERMANENT, "missing_token", "Session contained no refresh token"));

    /** The enumeration of known error types */
    public static enum ErrorType {
        /**
         * Access token could not be refreshed because the sessions refresh token
         * is not valid anymore. This will lead to session termination!
         */
        INVALID_REFRESH_TOKEN,
        /**
         * Any temporary error like an unavailable HTTP service or a network timeout
         */
        TEMPORARY,
        /**
         * Any permanent error that cannot be recovered from. Be careful when returning
         * this, affected sessions might be terminated!
         */
        PERMANENT;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------
    
    /** The error for a token refresh response */
    public static class Error {

        private final ErrorType type;
        private final String code;
        private final String description;
        private final OXException exception;

        public Error(ErrorType type, String code, String description) {
            this(type, code, description, null);
        }

        public Error(ErrorType type, String code, String description, OXException exception) {
            super();
            if (type == null) {
                throw new IllegalArgumentException("type must be set!");
            }
            if (code == null) {
                throw new IllegalArgumentException("code must be set!");
            }
            this.type = type;
            this.code = code;
            this.description = description;
            this.exception = exception;
        }

        public ErrorType getType() {
            return type;
        }

        public String getCode() {
            return code;
        }

        public boolean hasDescription() {
            return description != null;
        }

        public String getDescription() {
            return description;
        }

        public boolean hasException() {
            return exception != null;
        }

        public OXException getException() {
            return exception;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(getCode());
            if (hasDescription()) {
                sb.append(" - ").append(getDescription());
            }
            if (hasException()) {
                sb.append(" [").append(getException().getMessage()).append("]");
            }
            return sb.toString();
        }
    }
    
    // -------------------------------------------------------------------------------------------------------------------------------------

    private final OAuthTokens tokens;
    private final Error error;

    /**
     * Initializes a new {@link TokenRefreshResponse}.
     * 
     * @param tokens The OAuth tokens
     */
    public TokenRefreshResponse(OAuthTokens tokens) {
        this(tokens, null);
    }

    /**
     * Initializes a new {@link TokenRefreshResponse}.
     * 
     * @param error The error
     */
    public TokenRefreshResponse(Error error) {
        this(null, error);
    }

    private TokenRefreshResponse(OAuthTokens tokens, Error error) {
        if (tokens == null && error == null ) {
            throw new IllegalArgumentException("Either tokens or error must be set!");
        } else if (tokens != null && error != null) {
            throw new IllegalArgumentException("Either tokens or error must be set but not both!");
        }
        this.tokens = tokens;
        this.error = error;
    }

    /**
     * Checks if this token refresh response was successful.
     * 
     * @return <code>true</code> if successful; otherwise <code>false</code>
     */
    public boolean isSuccess() {
        return tokens != null;
    }

    /**
     * Gets the refreshed tokens.
     * 
     * @return The refreshed tokens or <code>null</code>
     * @see #isSuccess()
     */
    public OAuthTokens getTokens() {
        return tokens;
    }

    /**
     * Gets the error.
     * 
     * @return The error or <code>null</code>
     * @see #isSuccess()
     */
    public Error getError() {
        return error;
    }

}
