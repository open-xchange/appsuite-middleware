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
