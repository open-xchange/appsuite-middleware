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

package com.openexchange.oauth.provider.impl;

import java.util.Arrays;
import java.util.List;
import com.openexchange.oauth.provider.impl.introspection.OAuthIntrospectionAuthorizationService;
import com.openexchange.oauth.provider.impl.jwt.OAuthJwtAuthorizationService;

/**
 * {@link OAuthProviderMode} - defines available modes for OAuth provider
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public enum OAuthProviderMode {

    /**
     * The OAuth provider also acts as a authorization server.
     * This mode enables {@link DefaultAuthorizationService}.
     */
    AUTH_SEVER("auth_server"),

    /**
     * The OAuth provider expects JWT and is able to parse and validate it.
     * This mode enables {@link OAuthJwtAuthorizationService}
     */
    EXPECT_JWT("expect_jwt"),

    /**
     * The OAuthProvider uses token introspection to verify a received token.
     * This mode enables {@link OAuthIntrospectionAuthorizationService}
     */
    TOKEN_INTROSPECTION("token_introspection");

    private final String mode;

    /**
     * Initializes a new {@link OAuthProviderMode}.
     */
    private OAuthProviderMode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns the mode string associated with the {@link OAuthProviderMode}.
     *
     * @return the mode string
     */
    public String getProviderModeString() {
        return mode;
    }

    /**
     * Return the corresponding {@link OAuthProviderMode} or {@link OAuthProviderMode#AUTH_SEVER} in case the given mode string is unknown.
     * 
     * @param input String representation of an {@link OAuthProviderMode}.
     * @return The corresponding {@link OAuthProviderMode}
     */
    public static OAuthProviderMode getProviderMode(String input) {
        List<OAuthProviderMode> modes = Arrays.asList(OAuthProviderMode.values());
        return modes.stream().filter(mode -> mode.getProviderModeString().equals(input)).findAny().orElse(AUTH_SEVER);
    }
}
