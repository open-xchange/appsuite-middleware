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

package com.openexchange.configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;

/**
 * {@link ClientWhitelist} - A client whitelist.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ClientWhitelist {

    private final ConcurrentMap<Pattern, Pattern> map;

    private final ConcurrentMap<String, Boolean> allowedCache;

    /**
     * Initializes a new {@link ClientWhitelist}.
     */
    public ClientWhitelist() {
        super();
        map = new ConcurrentHashMap<Pattern, Pattern>(4, 0.9f, 1);
        allowedCache = new ConcurrentHashMap<String, Boolean>(4, 0.9f, 1);
    }

    /**
     * Adds specified comma-separated wildcard patterns.
     *
     * @param wildcardPatterns The wildcard patterns to add
     * @return This client whitelist with patterns applied
     */
    public ClientWhitelist add(final String wildcardPatterns) {
        if (null == wildcardPatterns) {
            return this;
        }
        allowedCache.clear();
        final String[] wps = wildcardPatterns.split(" *, *", 0);
        for (final String wildcardPattern : wps) {
            if (Strings.isNotEmpty(wildcardPattern)) {
                add(Pattern.compile(Strings.wildcardToRegex(removeQuotes(wildcardPattern.trim())), Pattern.CASE_INSENSITIVE));
            }
        }
        return this;
    }

    /**
     * Adds specified pattern if no such pattern is already contained.
     *
     * @param pattern The pattern to add
     * @return <code>true</code> for successful insertion; otherwise <code>false</code>
     */
    public boolean add(final Pattern pattern) {
        allowedCache.clear();
        return (null == map.putIfAbsent(pattern, pattern));
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
     * Checks if this white-list contains specified pattern.
     *
     * @param pattern The pattern
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(final Pattern pattern) {
        return map.containsKey(pattern);
    }

    /**
     * Removes specified pattern.
     *
     * @param pattern The pattern
     * @return <code>true</code> if specified pattern was removed; otherwise <code>false</code>
     */
    public boolean remove(final Pattern pattern) {
        allowedCache.clear();
        return (null != map.remove(pattern));
    }

    /**
     * Clears this white-list.
     */
    public void clear() {
        map.clear();
        allowedCache.clear();
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
     * Gets currently contained patterns.
     *
     * @return Currently contained patterns.
     */
    public Set<Pattern> getPatterns() {
        return new HashSet<Pattern>(map.keySet());
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
        final Boolean cached = allowedCache.get(clientId);
        if (null != cached) {
            return cached.booleanValue();
        }
        for (final Pattern pattern : map.keySet()) {
            if (pattern.matcher(clientId).matches()) {
                allowedCache.put(clientId, Boolean.TRUE);
                return true;
            }
        }
        allowedCache.put(clientId, Boolean.FALSE);
        return false;
    }

    /*-
     * ------------------------------------- HELPERS -----------------------------------------------
     */

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
