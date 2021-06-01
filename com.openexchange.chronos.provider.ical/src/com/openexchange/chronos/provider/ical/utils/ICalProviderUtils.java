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

package com.openexchange.chronos.provider.ical.utils;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionCodes;
import com.openexchange.chronos.provider.ical.properties.ICalCalendarProviderProperties;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link ICalProviderUtils}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalProviderUtils {

    public static void verifyURI(String feedUrl) throws OXException {
        if (Strings.isEmpty(feedUrl)) {
            throw ICalProviderExceptionCodes.MISSING_FEED_URI.create();
        }
        try {
            URI uri = new URI(feedUrl);
            check(uri);
            boolean denied = ICalCalendarProviderProperties.isDenied(uri);

            if (denied || !isValid(uri)) {
                throw ICalProviderExceptionCodes.FEED_URI_NOT_ALLOWED.create(feedUrl);
            }
        } catch (URISyntaxException e) {
            throw ICalProviderExceptionCodes.BAD_FEED_URI.create(e, feedUrl);
        }
    }

    private static void check(URI uri) throws OXException {
        if (Strings.isEmpty(uri.getScheme()) || Strings.containsSurrogatePairs(uri.toString())) {
            throw com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionCodes.BAD_FEED_URI.create(uri.toString());
        }
    }

    private static boolean isValid(URI uri) {
        try {
            InetAddress inetAddress = InetAddress.getByName(uri.getHost());
            if (inetAddress.isAnyLocalAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()) {
                org.slf4j.LoggerFactory.getLogger(ICalProviderUtils.class).debug("Given feed URL \"{}\" with destination IP {} appears not to be valid.", uri.toString(), inetAddress.getHostAddress());
                return false;
            }
        } catch (UnknownHostException e) {
            org.slf4j.LoggerFactory.getLogger(ICalProviderUtils.class).debug("Given feed URL \"{}\" appears not to be valid.", uri.toString(), e);
            return false;
        }
        return true;
    }
}
