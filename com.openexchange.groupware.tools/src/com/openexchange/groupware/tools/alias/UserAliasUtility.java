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
