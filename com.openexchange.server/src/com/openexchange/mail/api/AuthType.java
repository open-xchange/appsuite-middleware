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

package com.openexchange.mail.api;

import java.util.EnumSet;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;

/**
 * {@link AuthType} - The authentication type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public enum AuthType {

    /**
     * The login authentication type.
     */
    LOGIN("login"),
    /**
     * The XOAUTH2 authentication type; see <a href="https://developers.google.com/gmail/xoauth2_protocol">https://developers.google.com/gmail/xoauth2_protocol</a>
     */
    XOAUTH2("XOAUTH2"),
    /**
     * The OAUTHBEARER authentication type; see <a href="https://tools.ietf.org/html/rfc7628">https://tools.ietf.org/html/rfc7628</a>.
     */
    OAUTHBEARER("OAUTHBEARER"),
    ;

    private final String name;

    private AuthType(String name) {
        this.name = name;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    private static final Map<String, AuthType> MAP;
    static {
        ImmutableMap.Builder<String, AuthType> builder = ImmutableMap.builder();
        for (AuthType authType : AuthType.values()) {
            builder.put(Strings.asciiLowerCase(authType.name), authType);
        }
        // Legacy behavior
        builder.put("oauth", AuthType.XOAUTH2);
        MAP = builder.build();
    }

    /**
     * Parses specified string into an AuthType.
     *
     * @param authTypeStr The string to parse to an AuthType
     * @return An appropriate AuthType or <code>null</code> if string could not be parsed to an AuthType
     */
    public static final AuthType parse(final String authTypeStr) {
        return null == authTypeStr ? null : MAP.get(Strings.asciiLowerCase(authTypeStr));
    }

    private static final EnumSet<AuthType> OAUTH_TYPES = EnumSet.of(AuthType.XOAUTH2, AuthType.OAUTHBEARER);

    /**
     * Checks if given auth type is one of known OAuth-based types; either XOAUTH2 or OAUTHBEARER.
     *
     * @param authType The auth type to check
     * @return <code>true</code> auth type is one of known OAuth-based types; otherwise <code>false</code>
     */
    public static boolean isOAuthType(AuthType authType) {
        return null != authType && OAUTH_TYPES.contains(authType);
    }

}
