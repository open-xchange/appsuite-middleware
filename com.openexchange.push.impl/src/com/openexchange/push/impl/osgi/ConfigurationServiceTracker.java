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

package com.openexchange.push.impl.osgi;

import java.util.regex.Pattern;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.push.PushClientWhitelist;

/**
 * {@link ConfigurationServiceTracker} - The service tracker for {@code ConfigurationService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConfigurationServiceTracker implements ServiceTrackerCustomizer<ConfigurationService,ConfigurationService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link ConfigurationServiceTracker}.
     *
     * @param context The bundle context
     */
    public ConfigurationServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public ConfigurationService addingService(final ServiceReference<ConfigurationService> reference) {
        final ConfigurationService service = context.getService(reference);
        final String property = service.getProperty("com.openexchange.push.allowedClients");
        final PushClientWhitelist clientWhitelist = PushClientWhitelist.getInstance();
        clientWhitelist.clear();
        if (null == property) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationServiceTracker.class);
            log.info("Cleared push client white-list from.");
        } else {
            final String[] wildcardPatterns = property.split(" *, *", 0);
            for (final String wildcardPattern : wildcardPatterns) {
                if (!isEmpty(wildcardPattern)) {
                    clientWhitelist.add(Pattern.compile(wildcardToRegex(removeQuotes(wildcardPattern.trim())), Pattern.CASE_INSENSITIVE));
                }
            }
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationServiceTracker.class);
            log.info("Built push client white-list from: {}", property);
        }
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
        // NOP
    }

    @Override
    public void removedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
        // no-op
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static String removeQuotes(final String quoted) {
        if (quoted.length() < 2 || quoted.charAt(0) != '"') {
            return quoted;
        }
        String retval = quoted.substring(1);
        final int end = retval.length() - 1;
        if (retval.charAt(end) == '"') {
            retval = retval.substring(0, end);
        }
        return retval;
    }

    /**
     * Converts specified wildcard string to a regular expression
     *
     * @param wildcard The wildcard string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    private static String wildcardToRegex(final String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\') {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

}
