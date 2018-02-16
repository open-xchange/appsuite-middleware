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

package com.openexchange.groupware.tools.alias;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.java.Strings;

/**
 * {@link UserAliasUtility} - Utility class to handle alias specific checks. Checks are done <b>case insensitive</b>
 * as recommended by the RFC 5321.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5321#section-2.4">RFC 5321</a>
 */
public final class UserAliasUtility {

    /**
     * Check whether a given alias is an actual alias of a specific user.
     * <br>
     * The check is performed <b>case insensitive</b> as recommended by RFC 5321.
     * 
     * @param possibleAlias A {@link String} representing a possible alias of the user
     * @param aliases A {@link Collection} of aliases to check
     * @return <code>true</code> if the given alias belongs to the user, <code>false</code> otherwise.
     * @see <a href="https://tools.ietf.org/html/rfc5321#section-2.4">RFC 5321</a>
     */
    public static boolean isAlias(String possibleAlias, Collection<String> aliases) {
        if (Strings.isEmpty(possibleAlias)) {
            return false;
        }

        try {
            InternetAddress a = new InternetAddress(possibleAlias);
            for (String alias : aliases) {
                try {
                    InternetAddress aliasAddress = new InternetAddress(alias);
                    if (a.equals(aliasAddress)) {
                        // Address part is equalsIgnoreCase
                        return true;
                    }
                } catch (AddressException e) {
                    // Ignore
                }
            }
        } catch (AddressException e) {
            // Fall through
        }
        return false;
    }

    /**
     * Check whether a given alias is an actual alias of a specific user.
     * <br>
     * The check is performed <b>case insensitive</b> as recommended by RFC 5321.
     * 
     * @param possibleAlias A {@link String} representing a possible alias of the user
     * @param aliases A {@link Collection} of aliases to check
     * @return <code>true</code> if the given alias belongs to the user, <code>false</code> otherwise.
     * @see <a href="https://tools.ietf.org/html/rfc5321#section-2.4">RFC 5321</a>
     */
    public static boolean isAlias(String possibleAlias, String[] aliases) {
        if (null == aliases) {
            return false;
        }
        return isAlias(possibleAlias, Arrays.asList(aliases));
    }

    /**
     * Checks if a given alias can be matched to a user
     * <br>
     * Checks are performed <b>case insensitive</b> as recommended by RFC 5321.
     * 
     * @param possibleAlias A {@link String} representing a possible alias of a user
     * @param users The {@link Map} containing a {@link Collection} of aliases of a specific user identified by its user ID
     * @return The ID of the user matching the alias or <code>null</code>
     * @see <a href="https://tools.ietf.org/html/rfc5321#section-2.4">RFC 5321</a>
     */
    public static Integer getUser(String possibleAlias, Map<Integer, Collection<String>> users) {
        if (null != users) {
            for (Entry<Integer, Collection<String>> user : users.entrySet()) {
                if (isAlias(possibleAlias, user.getValue())) {
                    return user.getKey();
                }
            }
        }
        return null;
    }
}
