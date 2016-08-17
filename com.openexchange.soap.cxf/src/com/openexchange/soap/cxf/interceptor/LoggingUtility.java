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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.soap.cxf.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.cxf.interceptor.LoggingMessage;

/**
 * {@link LoggingUtility} - Utility class for CXF logging.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
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
