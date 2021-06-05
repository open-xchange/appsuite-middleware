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

package com.openexchange.soap.cxf.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.cxf.interceptor.LoggingMessage;

/**
 * {@link LoggingUtility} - Utility class for CXF logging.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SuppressWarnings("deprecation")
public final class LoggingUtility {

    /**
     * Initializes a new {@link LoggingUtility}.
     */
    private LoggingUtility() {
        super();
    }

    /** The regular expression to discover possible password elements */
    private static final Pattern PATTERN_PASSWORD = Pattern.compile("(<.*?password.*?>)[^<>]+(</.*?password.*?>)");

    /**
     * Sanitizes possible user-sensitive data from given message.
     *
     * @param message The message to sanitize
     * @return The sanitized message
     */
    public static String sanitizeMessage(final String message) {
        if (null != message) {
            // Replace possible passwords in message
            final StringBuffer sb = new StringBuffer(message.length());
            final Matcher m = PATTERN_PASSWORD.matcher(message);
            while (m.find()) {
                m.appendReplacement(sb, "$1XXXX$2");
            }
            m.appendTail(sb);
            return sb.toString();
        }

        return message;
    }

    /**
     * Sanitizes possible user-sensitive data from given logging message's payload.
     *
     * @param loggingMessage The logging message to sanitize
     * @return The sanitized logging message
     */
    public static LoggingMessage sanitizeLoggingMessage(final LoggingMessage loggingMessage) {
        if (null != loggingMessage) {
            // Replace possible passwords in payload
            final StringBuilder payload = loggingMessage.getPayload();
            if (null != payload && payload.length() > 0) {
                final String sanitizedPayload = sanitizeMessage(payload.toString());
                payload.setLength(0);
                payload.append(sanitizedPayload);
            }
        }

        return loggingMessage;
    }

}
