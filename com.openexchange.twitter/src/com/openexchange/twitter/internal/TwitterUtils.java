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

package com.openexchange.twitter.internal;

import com.openexchange.exception.OXException;
import com.openexchange.twitter.TwitterExceptionCodes;


/**
 * {@link TwitterUtils} - A utility class for Twitter.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterUtils {

    /**
     * Initializes a new {@link TwitterUtils}.
     */
    private TwitterUtils() {
        super();
    }

    /** Bad or expired access token. Need to re-authenticate user. */
    public static final int _401_UNAUTHORIZED = 401;

    /**
     * Converts given <code>twitter4j.TwitterException</code> instance to an appropriate <code>OXException</code> instance.
     * <p>
     * See <a href="https://dev.twitter.com/docs/error-codes-responses">https://dev.twitter.com/docs/error-codes-responses</a>.
     *
     * @param e The Twitter error
     * @return An appropriate {@code OXException} instance
     */
    public static OXException handleTwitterException(final twitter4j.TwitterException e) {
        if (null == e) {
            return null;
        }
        // According to https://dev.twitter.com/docs/error-codes-responses
        final int statusCode = e.getStatusCode();
        if (400 == statusCode) {
            return TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        if (_401_UNAUTHORIZED == statusCode) {
            return TwitterExceptionCodes.REAUTHORIZE_ERROR.create(e, e.getMessage());
        }
        if (403 == statusCode) {
            return TwitterExceptionCodes.DENIED_ERROR.create(e, e.getMessage());
        }
        if (406 == statusCode) {
            return TwitterExceptionCodes.INVALID_QUERY.create(e, e.getMessage());
        }
        return TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
    }

}
