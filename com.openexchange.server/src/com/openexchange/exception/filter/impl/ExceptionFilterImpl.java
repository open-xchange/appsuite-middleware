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

package com.openexchange.exception.filter.impl;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.exception.filter.ExceptionFilter;
import com.openexchange.java.Strings;

/**
 * {@link ExceptionFilterImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ExceptionFilterImpl implements ExceptionFilter, Reloadable {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ExceptionFilterImpl.class);

    /** The property defining the blacklisted exceptions */
    private final static String BLACK_LISTED = "com.openexchange.ajax.response.exceptions.blacklisted";

    private Set<String> prefixes;
    private Set<String> codes;

    /**
     * Initializes a new {@link ExceptionFilterImpl}.
     * 
     */
    public ExceptionFilterImpl() {
        super();
        prefixes = new ConcurrentSkipListSet<>();
        codes = new ConcurrentSkipListSet<>();
    }

    @Override
    public boolean isStackTraceAllowed(OXException exception) {
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

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        // Clean up
        prefixes.clear();
        codes.clear();

        // Read data
        String unparsed = configService.getProperty(BLACK_LISTED);
        if (Strings.isEmpty(unparsed)) {
            LOGGER.debug("There are no exceptions configured to be blacklisted. Using default instead.");
            prefixes.add("SES");
            return;
        }

        // Parse
        String[] entries = Strings.splitByComma(unparsed);
        for (String entry : entries) {
            if (entry.matches("[A-Z]{3}-[0-9]*")) {
                // Complete error code like SES-200
                codes.add(entry);
            } else if (entry.matches("[A-Z]{3}(-|-\\*|\\*)?")) {
                // Prefix like SES, SES* or SES-*
                prefixes.add(entry.replaceAll("-|-\\*|\\*", ""));
            } else {
                // Not supported
                LOGGER.debug("{} does not match any typical prefix or exception code", entry);
            }
        }
    }

    @Override
    public Interests getInterests() {
        return new Interests() {

            @Override
            public String[] getPropertiesOfInterest() {
                return new String[] { BLACK_LISTED };
            }

            @Override
            public String[] getConfigFileNames() {
                return null;
            }
        };
    }
}
