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

package com.openexchange.push;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.java.Strings;

/**
 * {@link PushClientWhitelist}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushClientWhitelist {

    /** Check if a certain client identifier matches */
    public static interface ClientMatcher {

        /**
         * Checks if specified client identifier does match according to this matcher's consideration.
         *
         * @return  <tt>true</tt> if client identifier matches; otherwise <code>false</code>
         */
        boolean matches(String clientId);
    }

    /** A matcher which checks if client identifier ignore-case starts with a certain prefix */
    public static class IgnoreCasePrefixClientMatcher implements ClientMatcher {

        private final String prefix;

        /**
         * Initializes a new {@link IgnoreCasePrefixClientMatcher}.
         *
         * @param prefix The prefix
         */
        public IgnoreCasePrefixClientMatcher(String prefix) {
            super();
            this.prefix = Strings.asciiLowerCase(prefix);
        }

        @Override
        public boolean matches(String clientId) {
            return Strings.asciiLowerCase(clientId).startsWith(prefix);
        }
    }

    /** A matcher which uses a regular expression to check if a client identifier matches */
    public static class PatternClientMatcher implements ClientMatcher {

        private final Pattern pattern;

        /**
         * Initializes a new {@link PatternClientMatcher}.
         *
         * @param pattern
         */
        public PatternClientMatcher(Pattern pattern) {
            super();
            this.pattern = pattern;
        }

        @Override
        public boolean matches(String clientId) {
            return pattern.matcher(clientId).matches();
        }
    }

    /** A matcher which checks if client identifier ignore-case equals a certain identifier */
    public static class IgnoreCaseExactClientMatcher implements ClientMatcher {

        private final String clientId;

        /**
         * Initializes a new {@link IgnoreCaseExactClientMatcher}.
         *
         * @param clientId The client identifier
         */
        public IgnoreCaseExactClientMatcher(String clientId) {
            super();
            this.clientId = Strings.asciiLowerCase(clientId);
        }

        @Override
        public boolean matches(String clientId) {
            return this.clientId.equals(Strings.asciiLowerCase(clientId));
        }
    }

    private static final PushClientWhitelist instance = new PushClientWhitelist();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static PushClientWhitelist getInstance() {
        return instance;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<ClientMatcher, ClientMatcher> map;
    private final Cache<String, Boolean> checks;

    /**
     * Initializes a new {@link PushClientWhitelist}.
     */
    private PushClientWhitelist() {
        super();
        map = new ConcurrentHashMap<ClientMatcher, ClientMatcher>(4, 0.9f, 1);
        checks = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofHours(2)).initialCapacity(16).maximumSize(1024).build();
    }

    /**
     * Adds specified matcher if no such matcher is already contained.
     *
     * @param matcher The matcher to add
     * @return <code>true</code> for successful insertion; otherwise <code>false</code>
     */
    public boolean add(final ClientMatcher matcher) {
        boolean added = (null == map.putIfAbsent(matcher, matcher));
        if (added) {
            checks.invalidateAll();
        }
        return added;
    }

    /**
     * Gets this white-list's size.
     *
     * @return The size
     */
    public int size() {
        return map.size();
    }

    /**
     * Checks if this white-list contains specified matcher.
     *
     * @param matcher The matcher
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(final ClientMatcher matcher) {
        return map.containsKey(matcher);
    }

    /**
     * Removes specified matcher.
     *
     * @param matcher The matcher
     * @return <code>true</code> if specified pattern was removed; otherwise <code>false</code>
     */
    public boolean remove(final ClientMatcher matcher) {
        boolean removed = null != map.remove(matcher);
        if (removed) {
            checks.invalidateAll();
        }
        return removed;
    }

    /**
     * Clears this white-list.
     */
    public void clear() {
        map.clear();
        checks.invalidateAll();
    }

    /**
     * Checks if this white-list is empty.
     *
     * @return <code>true</code> if this white-list is empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Checks if specified client identifier is matched by one of contained patterns.
     *
     * @param clientId The client identifier
     * @return <code>true</code> if specified client identifier is matched by one of contained patterns; otherwise <code>false</code>
     */
    public boolean isAllowed(final String clientId) {
        if (null == clientId) {
            return false;
        }

        Boolean matches = checks.getIfPresent(clientId);
        if (null != matches) {
            return matches.booleanValue();
        }

        matches = Boolean.valueOf(doCheckAllowed(clientId));
        checks.put(clientId, matches);
        return matches.booleanValue();
    }

    private boolean doCheckAllowed(final String clientId) {
        for (ClientMatcher matcher : map.keySet()) {
            if (matcher.matches(clientId)) {
                return true;
            }
        }
        return false;
    }

}
