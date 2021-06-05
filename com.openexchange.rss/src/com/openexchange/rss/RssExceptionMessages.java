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

package com.openexchange.rss;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link RssExceptionMessages} - Exception messages for RSS module that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RssExceptionMessages implements LocalizableStrings {

    // Invalid RSS URL -- No or not well-formed XML content provided by URL: %1$s
    public static final String INVALID_RSS_MSG = "Invalid RSS URL or not well-formed XML content provided by URL: %1$s";

    // HTTP error %1$s while loading RSS feed from URL: %2$s.
    public static final String RSS_HTTP_ERROR_MSG = "HTTP error %1$s while loading RSS feed from URL: %2$s.";

    // Timeout while reading the RSS feed from URL: %1$s
    public static final String TIMEOUT_ERROR_MSG = "Timeout while reading the RSS feed from URL: %1$s";

    // Failed to read RSS feed from URL: %1$s
    public static final String GENERIC_ERROR_WITH_ARG1_MSG = "Failed to read RSS feed from URL: %1$s";

    // Failed to read RSS feed from URL: %2$s
    public static final String GENERIC_ERROR_WITH_ARG2_MSG = "Failed to read RSS feed from URL: %2$s";
    
    // The RSS feed is exceeding the maximum allowed size of '%1$s'
    public static final String RSS_SIZE_EXCEEDED = "The RSS feed is exceeding the maximum allowed size of '%1$s'";

    // Cannot connect to RSS with URL: %1$s. Please change URL and try again.
    public static final String RSS_CONNECTION_ERROR_MSG = "Cannot connect to RSS with URL: %1$s. Please change URL and try again.";

    /**
     * Initializes a new {@link RssExceptionMessages}.
     */
    private RssExceptionMessages() {
        super();
    }

}
