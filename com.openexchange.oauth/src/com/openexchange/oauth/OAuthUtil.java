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

package com.openexchange.oauth;

import static com.openexchange.java.Strings.isEmpty;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.framework.request.RequestContext;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.oauth.scope.OAuthScope;

/**
 * {@link OAuthUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class OAuthUtil {

    /**
     * Parses the specified {@link Set} with {@link OAuthScope}s and returns
     * the OAuth provider-specific mappings ({@link OAuthScope#getProviderScopes()})
     * as a space separated string. Duplicate OAuth provider-specific scopes will only be
     * present once in the returned string.
     *
     * @param scopes The {@link OAuthScope}s
     * @return a space separated string with all {@link OAuthScope}s in the specified {@link Set}
     */
    public static final String providerScopesToString(Set<OAuthScope> scopes) {
        Set<String> strings = new HashSet<>();
        for (OAuthScope scope : scopes) {
            String[] split = Strings.splitByWhitespaces(scope.getProviderScopes());
            for (String s : split) {
                strings.add(s);
            }
        }
        return setToString(strings);
    }

    /**
     * Parses the specified {@link Set} with {@link OAuthScope}s and returns
     * the ({@link OAuthScope#getOXScope()}) as a whitespace separated string
     *
     * @param scopes The {@link OAuthScope}s
     * @return a space separated string with all {@link OAuthScope}s in the specified {@link Set}
     */
    public static final String oxScopesToString(Set<OAuthScope> scopes) {
        Set<String> strings = new HashSet<>();
        for (OAuthScope scope : scopes) {
            strings.add(scope.getOXScope().name());
        }
        return setToString(strings);
    }

    /**
     * Creates a whitespace separated list of strings out of the specified {@link Set}
     *
     * @param strings The {@link Set} with the strings
     * @return a whitespace separated list of strings
     */
    private static final String setToString(Set<String> strings) {
        if (strings.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string).append(" ");
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    /**
     * Builds the 'init' call-back URL for the given {@link OAuthAccount}
     *
     * @param account The {@link OAuthAccount}
     *
     * @return the 'init' call-back URL for the given {@link OAuthAccount}
     */
    public static final String buildCallbackURL(OAuthAccount account) {
        RequestContext requestContext = RequestContextHolder.get();
        if (null == requestContext) {
            return null;
        }

        HostData hostData = requestContext.getHostData();
        boolean isSecure = hostData.isSecure();

        StringBuilder builder = new StringBuilder();
        builder.append(isSecure ? "https://" : "http://");
        builder.append(determineHost(hostData));
        builder.append(hostData.getDispatcherPrefix());
        builder.append("oauth/accounts?action=init");
        builder.append("&serviceId=").append(account.getAPI().getFullName());
        builder.append("&id=").append(account.getId());
        builder.append('&').append(OAuthConstants.ARGUMENT_DISPLAY_NAME).append('=').append(urlEncode(account.getDisplayName()));
        builder.append("&scopes=").append(urlEncode(OAuthUtil.oxScopesToString(account.getEnabledScopes())));

        return builder.toString();
    }

    /**
     * Tries to determine the hostname by first looking in to {@link HostData},
     * then through Java and if still not available, falls back to 'localhost' as last resort.
     *
     * @param hostData The {@link HostData}
     * @return The hostname
     */
    private static final String determineHost(HostData hostData) {
        // Try from the host data
        String hostname = hostData.getHost();

        // Get hostname from java
        if (isEmpty(hostname)) {
            try {
                hostname = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                // ignore
            }
        }
        // Fall back to localhost as last resort
        if (isEmpty(hostname)) {
            hostname = "localhost";
        }

        return hostname;
    }

    /**
     * URL encodes the specified string using "ISO-8859-1"
     *
     * @param s The string to encode
     * @return The encoded string
     */
    private static String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }
}
