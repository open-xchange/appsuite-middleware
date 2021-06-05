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

package com.openexchange.config;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * {@link WildcardFilter} - Expects comma-separated wildcard strings.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WildcardFilter implements Filter {

    private static final Pattern SPLIT = Pattern.compile(" *, *");

    private static final Filter DUMMY = new Filter() {

        @Override
        public boolean accepts(final String value) {
            return true;
        }
    };

    private static final class PatternBasedFilter implements Filter {

        private final Set<Pattern> patterns;

        PatternBasedFilter(final Set<Pattern> patterns) {
            super();
            this.patterns = patterns;
        }

        @Override
        public boolean accepts(final String value) {
            for (final Pattern pattern : patterns) {
                if (pattern.matcher(value).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    private final Filter delegate;

    /**
     * Initializes a new {@link WildcardFilter}.
     */
    public WildcardFilter(final String csvWildcards) {
        super();
        if (com.openexchange.java.Strings.isEmpty(csvWildcards)) {
            delegate = DUMMY;
        } else {
            final String[] wildcards = SPLIT.split(csvWildcards);
            final Set<Pattern> patterns = new HashSet<Pattern>(wildcards.length);
            for (final String s : wildcards) {
                if (s.indexOf('*') < 0 && s.indexOf('?') < 0) {
                    patterns.add(Pattern.compile(wildcardToRegex("*@"+s.trim()), Pattern.CASE_INSENSITIVE));
                } else {
                    patterns.add(Pattern.compile(wildcardToRegex(s.trim()), Pattern.CASE_INSENSITIVE));
                }
            }
            delegate = new PatternBasedFilter(patterns);
        }
    }

    @Override
    public boolean accepts(final String value) {
        return delegate.accepts(value);
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
