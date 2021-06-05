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

package com.openexchange.oauth.provider.exceptions;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.Category;


/**
 * {@link OAuthInvalidTokenException}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthInvalidTokenException extends OAuthRequestException {

    private static final long serialVersionUID = 518106848861523133L;

    public enum Reason {
        TOKEN_MALFORMED,
        TOKEN_EXPIRED,
        TOKEN_UNKNOWN,
        TOKEN_MISSING,
        TOKEN_INVALID,
        INVALID_AUTH_SCHEME
    }

    public static Map<Reason, String> DESCRIPTIONS = new HashMap<>();
    static {
        DESCRIPTIONS.put(Reason.TOKEN_EXPIRED, "The passed access token is expired.");
        DESCRIPTIONS.put(Reason.TOKEN_MALFORMED, "The passed access token is malformed.");
        DESCRIPTIONS.put(Reason.TOKEN_INVALID, "The passed access token was rejected");
        DESCRIPTIONS.put(Reason.TOKEN_UNKNOWN, "The passed access token is unknown to the server.");
        DESCRIPTIONS.put(Reason.INVALID_AUTH_SCHEME, "Invalid auth scheme, must always be 'Bearer'.");
    }

    private final Reason reason;

    public OAuthInvalidTokenException(Reason reason) {
        super();
        this.reason = reason;
    }

    @Override
    public int getCode() {
        return 1;
    }

    @Override
    public Category getCategory() {
        return Category.CATEGORY_PERMISSION_DENIED;
    }

    @Override
    public String getError() {
        return "invalid_token";
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public String getErrorDescription() {
        return DESCRIPTIONS.get(reason);
    }

}
