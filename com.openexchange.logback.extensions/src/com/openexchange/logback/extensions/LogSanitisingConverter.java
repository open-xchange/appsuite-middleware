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

package com.openexchange.logback.extensions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.helpers.MessageFormatter;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * {@link LogSanitisingConverter}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LogSanitisingConverter extends ClassicConverter {

    /**
     * Initialises a new {@link LogSanitisingConverter}.
     */
    public LogSanitisingConverter() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.pattern.Converter#convert(java.lang.Object)
     */
    @Override
    public String convert(ILoggingEvent event) {
        // If there are no arguments attached to the logging event, try to sanitise the already formatted message
        if (event.getArgumentArray() == null || event.getArgumentArray().length == 0) {
            return sanitise(event.getFormattedMessage());
        }

        // Go through the arguments list and sanitise each argument
        String[] sanitisedArguments = new String[event.getArgumentArray().length];
        int index = 0;
        for (Object o : event.getArgumentArray()) {
            String sanitisedString = null;
            if (o != null) {
                String string = !(o instanceof String) ? o.toString() : (String) o;
                sanitisedString = sanitise(string);
            }
            sanitisedArguments[index++] = sanitisedString;
        }

        // Re-compile the formatted message
        String message = event.getMessage();
        return MessageFormatter.arrayFormat(message, sanitisedArguments).getMessage();
    }

    /**
     * Detects all escape control characters and ANSI color codes
     */
    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("(?:\\u001B)\\[[\\d;]*[^\\d;]|([\\x00-\\x1F])");

    /**
     * Sanitises the specified string from any ANSI escape sequences
     * 
     * @param string The string to sanitise
     * @return The sanitised string
     */
    private String sanitise(String string) {
        Matcher matcher = ANSI_ESCAPE_PATTERN.matcher(string);
        int lastMatch = 0;
        StringBuilder s = new StringBuilder();
        while (matcher.find()) {
            String substring = string.substring(lastMatch, matcher.start());
            s.append(substring);
            lastMatch = matcher.end();
        }
        if (s.length() != 0) {
            string = s.toString();
        }
        return string;
    }
}
