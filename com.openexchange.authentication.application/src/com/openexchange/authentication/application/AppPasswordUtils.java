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

package com.openexchange.authentication.application;

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
     * Gets a value indicating whether a session originates in the login with an application specific password, and is equipped with certain restricted scopes.
     *
     * @param session The session to check
     * @return <code>true</code> if the session is <i>restricted</i> and equipped with the given scopes, <code>false</code>, otherwise
     */
    public static boolean hasRestrictedScopes(Session session, String... scopes) {
        String[] restrictedScopes = (String[]) session.getParameter(Session.PARAM_RESTRICTED);
        if (null != restrictedScopes) {
            if (null != scopes) {
                for (String scope : scopes) {
                    if (false == com.openexchange.tools.arrays.Arrays.contains(restrictedScopes, scope)) {
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
        String[] restrictedScopes = (String[]) session.getParameter(Session.PARAM_RESTRICTED);
        if (null != restrictedScopes) {
            for (String scope : scopes) {
                if (false == com.openexchange.tools.arrays.Arrays.contains(restrictedScopes, scope)) {
                    return false;
                }
            }
        }
        return true;
    }

}
