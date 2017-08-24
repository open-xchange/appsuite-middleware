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

package com.openexchange.mail.filter.json.v2.config;

import java.util.Map;
import java.util.Set;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.BasicGroup;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.Field;

/**
 * {@link Blacklist} is a collection of blacklisted mail filter elements for a single user
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class Blacklist {

    private final Map<String, Set<String>> blackmap;

    /**
     * Initializes a new {@link Blacklist}.
     */
    public Blacklist(Map<String, Set<String>> blackmap) {
        super();
        this.blackmap = blackmap;
    }

    /**
     * Checks if the the value is blacklisted for the given {@link BasicGroup}
     *
     * @param basic The {@link BasicGroup} to check
     * @param value The value
     * @return true if the blacklist of the {@link BasicGroup} contains the given value, otherwise false.
     */
    public boolean isBlacklisted(BasicGroup basic, String value) {
        if (blackmap.containsKey(basic.name())) {
            return blackmap.get(basic.name()).contains(value);
        }
        return false;
    }

    /**
     * Checks if the the value is blacklisted for the given {@link BasicGroup}, element and {@link Field}
     *
     * @param basic The {@link BasicGroup}
     * @param element The element of the {@link BasicGroup}
     * @param field The {@link Field} of the element
     * @param value The value
     * @return true if the blacklist of the specific {@link Field} contains the given value, otherwise false.
     */
    public boolean isBlacklisted(BasicGroup basic, String element, Field field, String value) {
        String key = key(basic, element, field);
        if (blackmap.containsKey(key)) {
            return blackmap.get(key).contains(value);
        }
        return false;
    }

    private static final String DOT = ".";

    /**
     * Creates a key which is used by this Blacklist
     *
     * @param basic The {@link BasicGroup}
     * @param element The element
     * @param field The {@link Field}
     * @return The key for this triplet
     */
    public static String key(BasicGroup basic, String element, Field field) {
        return basic.name() + DOT + element + DOT + field.name();
    }

    /**
     * Returns a single blacklist
     *
     * @param basic The {@link BasicGroup}
     * @param element The element name of the {@link BasicGroup}
     * @param field The {@link Field}
     * @return A set of blacklisted values or null
     */
    public Set<String> get(BasicGroup basic, String element, Field field) {
        if (element == null) {
            return blackmap.get(basic.name());
        }
        return blackmap.get(key(basic, element, field));
    }

}
