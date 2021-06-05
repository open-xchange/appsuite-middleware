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

package com.openexchange.rest.client.httpclient.internal;

import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.cookie.DefaultCookieSpec;
import org.apache.http.protocol.HttpContext;

/**
 * {@link LenientCookieSpecProvider}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Moved with 7.10.4
 * @since v7.6.1
 */
public class LenientCookieSpecProvider implements CookieSpecProvider {

    private final CookieSpec cookieSpec;

    public LenientCookieSpecProvider() {
        super();
        this.cookieSpec = new LenientCookieSpec();
    }

    @Override
    public CookieSpec create(HttpContext context) {
        return cookieSpec;
    }

    private static class LenientCookieSpec extends DefaultCookieSpec {

        // @formatter:off
        // See org.apache.http.impl.cookie.BrowserCompatSpec.DEFAULT_DATE_PATTERNS
        private static final String[] DEFAULT_DATE_PATTERNS = new String[] {
            DateUtils.PATTERN_RFC1123,
            DateUtils.PATTERN_RFC1036,
            DateUtils.PATTERN_ASCTIME,
            "EEE, dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MMM-yyyy HH-mm-ss z",
            "EEE, dd MMM yy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH:mm:ss z",
            "EEE dd MMM yyyy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH-mm-ss z",
            "EEE dd-MMM-yy HH:mm:ss z",
            "EEE dd MMM yy HH:mm:ss z",
            "EEE,dd-MMM-yy HH:mm:ss z",
            "EEE,dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MM-yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss Z",
        };
        // @formatter:on

        /** Initializes a new {@link LenientCookieSpec}. */
        LenientCookieSpec() {
            super(DEFAULT_DATE_PATTERNS, false);
        }
    }
}
