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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.push.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * {@link PushClientWhitelist}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushClientWhitelist {

    private static final PushClientWhitelist instance = new PushClientWhitelist();

    /**
     * Gets the instance.
     * 
     * @return The instance
     */
    public static PushClientWhitelist getInstance() {
        return instance;
    }

    private final ConcurrentMap<Pattern, Pattern> map;

    /**
     * Initializes a new {@link PushClientWhitelist}.
     */
    private PushClientWhitelist() {
        super();
        map = new ConcurrentHashMap<Pattern, Pattern>(4);
    }

    public Pattern addIfAbsent(final Pattern pattern) {
        return map.putIfAbsent(pattern, pattern);
    }

    public int size() {
        return map.size();
    }

    public boolean contains(final Pattern pattern) {
        return map.containsKey(pattern);
    }

    public Pattern add(final Pattern pattern) {
        return map.put(pattern, pattern);
    }

    public Pattern remove(final Pattern pattern) {
        return map.remove(pattern);
    }

    public void clear() {
        map.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<Pattern> getPatterns() {
        return new HashSet<Pattern>(map.keySet());
    }

    public boolean isAllowed(final String clientId) {
        if (null == clientId) {
            return false;
        }
        for (final Pattern pattern : map.keySet()) {
            if (pattern.matcher(clientId).matches()) {
                return true;
            }
        }
        return false;
    }

}
