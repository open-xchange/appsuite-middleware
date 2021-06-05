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

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import com.openexchange.java.Strings;

/**
 * {@link OAuthTokens}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class OAuthTokens {

    private final String accessToken;
    private final Date expiryDate;
    private final String refreshToken;

    /**
     * Initializes a new {@link OAuthTokens}.
     *
     * @param accessToken The access token; must not be {@code null}
     * @param expiryDate The expiry date; must not be {@code null}
     * @param refreshToken The refresh token; might be {@code null}
     */
    public OAuthTokens(String accessToken, Date expiryDate, String refreshToken) {
        super();
        if (accessToken == null) {
            throw new IllegalArgumentException("accessToken must not be null!");
        }
        this.accessToken = accessToken;
        this.expiryDate = expiryDate;
        this.refreshToken = refreshToken;
    }

    /**
     * Gets whether the access token is expired.
     *
     * @return {@code true} if so
     */
    public boolean isAccessExpired() {
        return !accessExpiresBeforeOrOn(new Date());
    }

    /**
     * Gets whether the access token expires within the given time frame.
     *
     * @param timeFrame The time frame
     * @param unit Unit of the time frame value
     * @return {@code true} if access token expiration happens within the time frame
     */
    public boolean accessExpiresWithin(long timeFrame, TimeUnit unit) {
        return expiryDate == null ? false : new Date(System.currentTimeMillis() + unit.toMillis(timeFrame)).after(expiryDate);
    }

    /**
     * Gets whether the access token expires before or on the given date.
     *
     * @param date The date to compare against
     * @return {@code true} if so
     */
    public boolean accessExpiresBeforeOrOn(Date date) {
        if (expiryDate == null) {
            return false;
        }
        return !date.after(expiryDate);
    }

    /**
     * Gets the accessToken
     *
     * @return The accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Gets whether an expiry date is contained.
     *
     * @return The expiry date
     */
    public boolean hasExpiryDate() {
        return expiryDate != null;
    }

    /**
     * Gets the expiryDate
     *
     * @return The expiryDate or {@code null}
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Gets whether a refresh token is contained
     *
     * @return {@code true} if a refresh token is available
     */
    public boolean hasRefreshToken() {
        return refreshToken != null;
    }

    /**
     * Gets the refreshToken
     *
     * @return The refreshToken or {@code null}
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Gets the milliseconds in which the access token will expire, based on
     * {@link System#currentTimeMillis()}.
     *
     * @return The expires-in ms. Can be {@code 0} or negative in case token is already expired.
     */
    public long getExpiresInMillis() {
        return expiryDate.getTime() - System.currentTimeMillis();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((expiryDate == null) ? 0 : expiryDate.hashCode());
        result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OAuthTokens other = (OAuthTokens) obj;
        if (accessToken == null) {
            if (other.accessToken != null) {
                return false;
            }
        } else if (!accessToken.equals(other.accessToken)) {
            return false;
        }
        if (expiryDate == null) {
            if (other.expiryDate != null) {
                return false;
            }
        } else if (!expiryDate.equals(other.expiryDate)) {
            return false;
        }
        if (refreshToken == null) {
            if (other.refreshToken != null) {
                return false;
            }
        } else if (!refreshToken.equals(other.refreshToken)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[access_token=").append(Integer.toHexString(accessToken.hashCode()));
        if (expiryDate != null) {
            sb.append(", expires_in=").append(expiryDate.getTime() - System.currentTimeMillis()).append(" seconds");
        }
        if (refreshToken != null) {
            sb.append(", refresh_token=").append(Integer.toHexString(refreshToken.hashCode()));
        }
        sb.append(']');
        return sb.toString();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the debug information for refresh token.
     *
     * @param oauthTokens The OAuth tokens
     * @return The debug information for refresh token
     */
    public static Object getDebugInfoForRefreshToken(OAuthTokens oauthTokens) {
        return oauthTokens == null ? null : getDebugInfoForToken(oauthTokens.getRefreshToken());
    }

    /**
     * Gets the debug information for given token.
     *
     * @param token The token
     * @return The debug information
     */
    public static Object getDebugInfoForToken(String token) {
        return Strings.isEmpty(token) ? "<not-available>" : new TokenDebugInfo(token);
    }

    private static final class TokenDebugInfo {

        private final String token;

        /**
         * Initializes a new {@link TokenDebugInfo}.
         *
         * @param token The token
         */
        TokenDebugInfo(String token) {
            super();
            this.token = token;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(32);
            sb.append(StringUtils.abbreviate(token, "...", token.length() - 10, 13));
            sb.append(" (").append(Integer.toHexString(token.hashCode())).append(')');
            return sb.toString();
        }
    }

}
