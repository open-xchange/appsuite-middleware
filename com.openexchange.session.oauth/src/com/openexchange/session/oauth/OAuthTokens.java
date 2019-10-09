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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.session.oauth;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
        if (expiryDate == null) {
            return false;
        }
        return new Date(System.currentTimeMillis() + unit.toMillis(timeFrame)).after(expiryDate);
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
        return "OAuthTokens [accessToken=******, expiryDate=" + expiryDate + ", refreshToken=******]";
    }

}