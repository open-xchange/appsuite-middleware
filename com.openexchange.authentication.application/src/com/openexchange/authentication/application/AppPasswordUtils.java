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

package com.openexchange.authentication.application;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link AppPasswordUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class AppPasswordUtils {

    /**
     * Gets a value indicating whether a specific session originates in the login with an application specific password.
     *
     * @param session The session to check
     * @return <code>true</code> if the session is <i>restricted</i>, <code>false</code>, otherwise
     */
    public static boolean isRestricted(Session session) {
        return null != session && session.containsParameter(Session.PARAM_RESTRICTED);
    }

    /**
     * Gets the scopes from a <i>restricted</i> session.
     * 
     * @param session The session to get the scopes from
     * @return The scopes, or <code>null</code> if there are none defined
     */
    public static Set<String> getRestrictedScopes(Session session) {
        String value = (String) session.getParameter(Session.PARAM_RESTRICTED);
        return null != value ? Strings.splitByComma(value, new HashSet<String>()) : null;
    }

    /**
     * Gets a value indicating whether a session originates in the login with an application specific password, and is equipped with certain restricted scopes.
     *
     * @param session The session to check
     * @param scopes The scopes to check against
     * @return <code>true</code> if the session is <i>restricted</i> and equipped with the given scopes, <code>false</code>, otherwise
     */
    public static boolean hasRestrictedScopes(Session session, String... scopes) {
        Set<String> restrictedScopes = getRestrictedScopes(session);
        if (null != restrictedScopes) {
            if (null != scopes) {
                for (String scope : scopes) {
                    if (false == restrictedScopes.contains(scope)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a value indicating whether the supplied session is either <i>unrestricted</i> (in terms of not originating in a login with an
     * application specific password), or if it is at least equipped with certain restricted scopes.
     *
     * @param session The session to check
     * @param scopes The scopes to check if the session is <i>restricted</i>
     * @return <code>true</code> if the session is <i>unrestricted</i> or equipped with the given scopes, <code>false</code>, otherwise
     */
    public static boolean isNotRestrictedOrHasScopes(Session session, String... scopes) {
        if (null == scopes || 0 == scopes.length) {
            return true;
        }
        Set<String> restrictedScopes = getRestrictedScopes(session);
        if (null != restrictedScopes) {
            for (String scope : scopes) {
                if (false == restrictedScopes.contains(scope)) {
                    return false;
                }
            }
        }
        return true;
    }

}
