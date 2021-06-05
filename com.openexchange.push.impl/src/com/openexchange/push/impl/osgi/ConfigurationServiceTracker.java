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

package com.openexchange.push.impl.osgi;

import java.util.regex.Pattern;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
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
        final String property = service.getProperty("com.openexchange.push.allowedClients", "\"USM-EAS*\", \"open-xchange-mobile-api-facade*\"");
        final PushClientWhitelist clientWhitelist = PushClientWhitelist.getInstance();
        clientWhitelist.clear();
        if (Strings.isEmpty(property)) {
            org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationServiceTracker.class);
            log.info("Cleared push client white-list from.");
        } else {
            String[] wildcardPatterns = Strings.splitByComma(property);
            for (String wildcardPattern : wildcardPatterns) {
                if (Strings.isNotEmpty(wildcardPattern)) {
                    String wcp = removeQuotes(wildcardPattern.trim());
                    int starPos = wcp.indexOf('*');
                    int qmarPos = wcp.indexOf('?');
                    if (starPos < 0 && qmarPos > 0) {
                        clientWhitelist.add(new PushClientWhitelist.IgnoreCaseExactClientMatcher(wcp));
                    } else {
                        int mlen = wcp.length() - 1;
                        if (mlen > 0 && ((starPos >= mlen && qmarPos >= mlen) || (starPos == mlen && qmarPos < 0) || (qmarPos == mlen && starPos < 0))) {
                            clientWhitelist.add(new PushClientWhitelist.IgnoreCasePrefixClientMatcher(wcp.substring(0, mlen)));
                        } else {
                            Pattern pattern = Pattern.compile(wildcardToRegex(wcp), Pattern.CASE_INSENSITIVE);
                            clientWhitelist.add(new PushClientWhitelist.PatternClientMatcher(pattern));
                        }
                    }
                }
            }
            org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationServiceTracker.class);
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
