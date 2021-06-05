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

package com.openexchange.oauth;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.Token;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

/**
 * {@link AccessTokenSecretExtractor20}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class AccessTokenSecretExtractor20 implements AccessTokenExtractor {

    /** The <code>"access_token": &lt;token&gt;</code> pattern */
    private static final Pattern PATTERN_ACCESS_TOKEN = Pattern.compile("\"access_token\" *: *\"([^&\"]+)\"");

    /** The <code>"refresh_token": &lt;token&gt;</code> pattern */
    private static final Pattern PATTERN_REFRESH_TOKEN = Pattern.compile("\"refresh_token\" *: *\"([^&\"]+)\"");

    /** The <code>"expires_in": &lt;number&gt;</code> pattern */
    private static final Pattern PATTERN_EXPIRES = Pattern.compile("\"expires_in\" *: *([0-9]+)");

    /**
     * Initializes a new {@link AccessTokenSecretExtractor20}.
     */
    public AccessTokenSecretExtractor20() {
        super();
    }

    @Override
    public Token extract(String response) {
        Preconditions.checkEmptyString(response, "Response body is incorrect. Can't extract a token from an empty string");

        Matcher matcher = PATTERN_ACCESS_TOKEN.matcher(response);
        if (false == matcher.find()) {
            throw new OAuthException("Response body is incorrect. Can't extract a token from this: '" + response + "'", null);
        }
        String token = OAuthEncoder.decode(matcher.group(1));
        String refreshToken = "";
        Matcher refreshMatcher = PATTERN_REFRESH_TOKEN.matcher(response);
        if (refreshMatcher.find()) {
            refreshToken = OAuthEncoder.decode(refreshMatcher.group(1));
        }
        Date expiry = null;
        Matcher expiryMatcher = PATTERN_EXPIRES.matcher(response);
        if (expiryMatcher.find()) {
            int lifeTime = Integer.parseInt(OAuthEncoder.decode(expiryMatcher.group(1)));
            expiry = new Date(System.currentTimeMillis() + lifeTime * 1000);
        }
        return new Token(token, refreshToken, expiry, response);
    }

}
