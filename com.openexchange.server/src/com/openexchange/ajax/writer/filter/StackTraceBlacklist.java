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

package com.openexchange.ajax.writer.filter;

import static com.openexchange.java.Strings.unquote;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link StackTraceBlacklist}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class StackTraceBlacklist {

    /** The logger */
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StackTraceBlacklist.class);

    // Blacklisted exceptions
    private final Set<String> prefixes;
    private final Set<String> codes;

    /**
     * Initializes a new {@link StackTraceBlacklist}.
     *
     * @param blacklist The comma separated list of exception codes and prefix which stack-traces should not be invoked into a response
     */
    public StackTraceBlacklist(String blacklist) {
        super();
        // Prepare sets
        ImmutableSet.Builder<String> prefixes = ImmutableSet.builder();
        ImmutableSet.Builder<String> codes = ImmutableSet.builder();


        if (null == blacklist) {
            LOGGER.debug("There are no exceptions configured to be blacklisted. Using default instead.");
            prefixes.add("SES");
        } else {
            String toParse = unquote(blacklist);

            // Read data
            if (Strings.isEmpty(toParse)) {
                LOGGER.debug("There are no exceptions configured to be blacklisted. Using default instead.");
                prefixes.add("SES");
            } else {
                // Prepare pattern
                Pattern patternCode = Pattern.compile("[A-Z]+-[0-9]+");
                Pattern patternPrefix = Pattern.compile("([A-Z]+)(-|-\\*|\\*)?");

                // Parse
                Matcher m;
                for (String entry : Strings.splitByComma(toParse)) {
                    if ((m = patternCode.matcher(entry)).matches()) {
                        // Complete error code like 'SES-200'
                        codes.add(entry);
                    } else if ((m = patternPrefix.matcher(entry)).matches()) {
                        // Prefix like 'SES', 'SES*' or 'SES-*'
                        prefixes.add(m.group(1));
                    } else {
                        // Not supported
                        LOGGER.warn("\"{}\" does neither match any known format for an exception prefix nor exception code. Therefore it won't be part of the blacklist.", entry);
                    }
                }
            }
        }

        this.prefixes = prefixes.build();
        this.codes = codes.build();
    }

    /**
     * Check if given {@link OXException} is allowed to be added in a response
     *
     * @param exception The {@link OXException} to check
     * @return <code>true</code> If the exception can be added in a response
     *         <code>false</code> If the exception is not allowed to be a part of the response
     */
    public boolean isIncludeAllowed(OXException exception) {
        // Check
        if (prefixes.contains(exception.getPrefix())) {
            // Prefix is blacklisted
            return false;
        }
        if (codes.contains(exception.getErrorCode())) {
            // Error is blacklisted
            return false;
        }
        return true;
    }

}
