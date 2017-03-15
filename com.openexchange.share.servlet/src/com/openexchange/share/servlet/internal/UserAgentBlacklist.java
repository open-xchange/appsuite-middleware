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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.share.servlet.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;

/**
 * {@link UserAgentBlacklist}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class UserAgentBlacklist {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserAgentBlacklist.class);

    /**
     * The default User-Agent black-list based on <a href="https://perishablepress.com/list-all-user-agents-top-search-engines/">this article</a>.
     */
    public static UserAgentBlacklist DEFAULT_BLACKLIST = new UserAgentBlacklist(
        "*aolbuild*, *baidu*, *bingbot*, *bingpreview*, *msnbot*, *duckduckgo*, *adsbot-google*, *googlebot*, *mediapartners-google*, *teoma*, *slurp*, *yandex*", true);

    // ----------------------------------------------------------------------------------------------------------------

    private final ImmutableMap<Matcher, Matcher> map;
    private final ConcurrentMap<String, Boolean> deniedCache;

    /**
     * Initializes a new {@link UserAgentBlacklist}.
     */
    public UserAgentBlacklist(String wildcardPatterns, boolean ignoreCase) {
        super();

        String[] wps = wildcardPatterns.split(" *, *", 0);
        List<Matcher> blacklistMatchers = new ArrayList<>(wps.length);
        for (String wildcardPattern : wps) {
            if (!Strings.isEmpty(wildcardPattern)) {
                String expr = wildcardPattern.trim();
                try {
                    String unquoted = removeQuotes(expr);
                    if (Strings.isEmpty(unquoted)) {
                        LOG.warn("Ignoring empty pattern expression: {}", expr);
                    } else {
                        String contains = isContainsMatcher(unquoted);
                        if (null != contains) {
                            blacklistMatchers.add(new ContainsMatcher(ignoreCase ? Strings.asciiLowerCase(contains) : contains, ignoreCase));
                        } else {
                            Pattern pattern = Pattern.compile(Strings.wildcardToRegex(unquoted), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
                            blacklistMatchers.add(new PatternMatcher(pattern));
                        }
                    }
                } catch (PatternSyntaxException e) {
                    LOG.warn("Ignoring invalid pattern expression: {}", expr, e);
                }
            }
        }

        ImmutableMap.Builder<Matcher, Matcher> map = ImmutableMap.builder();
        for (Matcher blacklistMatcher : blacklistMatchers) {
            map.put(blacklistMatcher, blacklistMatcher);
        }
        this.map = map.build();

        deniedCache = new ConcurrentHashMap<String, Boolean>(1024, 0.9f, 1);
    }

    private static String isContainsMatcher(String expr) {
        int len = expr.length();
        if (len <= 2) {
            return null;
        }

        if (expr.charAt(0) != '*' || expr.charAt(len - 1) != '*') {
            return null;
        }

        for (int i = len - 1; i-- > 1;) {
            char ch = expr.charAt(i);
            if (false == Strings.isAsciiLetter(ch)) {
                return null;
            }
        }

        return expr.substring(1, len - 1);
    }

    /**
     * Checks if this black-list is empty.
     *
     * @return <code>true</code> if this black-list is empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Checks if specified User-Agent identifier is matched by one of contained black-list patterns.
     *
     * @param userAgent The User-Agent identifier
     * @return <code>true</code> if specified User-Agent identifier is black-listed; otherwise <code>false</code>
     */
    public boolean isBlacklisted(String userAgent) {
        if (null == userAgent) {
            return false;
        }

        Boolean cached = deniedCache.get(userAgent);
        if (null != cached) {
            return cached.booleanValue();
        }

        for (Matcher matcher : map.keySet()) {
            if (matcher.matches(userAgent)) {
                deniedCache.put(userAgent, Boolean.TRUE);
                return true;
            }
        }
        deniedCache.put(userAgent, Boolean.FALSE);
        return false;
    }

    /*-
     * ------------------------------------- HELPERS -----------------------------------------------
     */

    private static interface Matcher {

        boolean matches(String userAgent);
    }

    private static final class PatternMatcher implements Matcher {

        private final Pattern pattern;

        PatternMatcher(Pattern pattern) {
            super();
            this.pattern = pattern;
        }

        @Override
        public boolean matches(String userAgent) {
            return pattern.matcher(userAgent).matches();
        }
    }

    private static final class ContainsMatcher implements Matcher {

        private final String contained;
        private final boolean ignoreCase;

        ContainsMatcher(String contained, boolean ignoreCase) {
            super();
            this.contained = contained;
            this.ignoreCase = ignoreCase;
        }

        @Override
        public boolean matches(String userAgent) {
            return (ignoreCase ? Strings.asciiLowerCase(userAgent) : userAgent).indexOf(contained) >= 0;
        }
    }

    /**
     * Removes possible surrounding quotes.
     *
     * @param quoted The possibly quoted string
     * @return The unquoted string
     */
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

}
