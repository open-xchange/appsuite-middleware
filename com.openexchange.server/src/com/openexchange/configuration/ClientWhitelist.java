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
            if (!Strings.isEmpty(wildcardPattern)) {
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
