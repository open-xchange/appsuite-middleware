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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.servlet.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Charsets;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.tools.encoding.URLCoder;

/**
 * {@link RedirectLocationBuilder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RedirectLocationBuilder {

    private static final Pattern P_UIWEBPATH = Pattern.compile("[uiwebpath]", Pattern.LITERAL);

    private final List<Entry<String, String>> parameters;

    /**
     * Initializes a new {@link RedirectLocationBuilder}
     */
    public RedirectLocationBuilder() {
        super();
        parameters = new ArrayList<Entry<String,String>>();
    }

    /**
     * Appends the login type suitable for the supplied authentication mode.
     *
     * @param authentication The authentication mode
     * @return The builder
     */
    public RedirectLocationBuilder loginType(AuthenticationMode authentication) {
        switch (authentication) {
            case ANONYMOUS_PASSWORD:
                return parameter("login_type", "anonymous");
            case GUEST_PASSWORD:
                return parameter("login_type", "guest");
            default:
                throw new UnsupportedOperationException("No login type for " + authentication);
        }
    }

    /**
     * Appends the (base) token of the accessed share.
     *
     * @param token The share token to append
     * @return The builder
     */
    public RedirectLocationBuilder share(String token) {
        return parameter("share", token);
    }

    /**
     * Appends the path to a specific share target.
     *
     * @param target The share target to append, or <code>null</code> if not specified
     * @return The builder
     */
    public RedirectLocationBuilder target(ShareTarget target) {
        return null != target ? parameter("target", target.getPath()) : this;
    }

    /**
     * Appends a message to pass to the client along with the redirect location.
     *
     * @param type The message type
     * @param message The message
     * @param status The message status
     * @return The builder
     */
    public RedirectLocationBuilder message(MessageType type, String message, String status) {
        parameter("message_type", type.toString());
        parameter("message", message);
        parameter("status", status);
        return this;
    }

    /**
     * Builds and returns the relative redirect location, ready to use in the <code>Location</code> header of a HTTP response.
     *
     * @return The built redirect URL
     */
    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getLoginLink()).append('#');
        if (0 < parameters.size()) {
            stringBuilder.append(parameters.get(0).getKey()).append('=').append(URLCoder.encode(parameters.get(0).getValue(), Charsets.UTF_8));
            for (int i = 1; i < parameters.size(); i++) {
                stringBuilder.append('&').append(parameters.get(i).getKey()).append('=').append(URLCoder.encode(parameters.get(i).getValue(), Charsets.UTF_8));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Adds an additional parameter to the builder instance.
     *
     * @param name The parameter name
     * @param value The parameter value
     * @return The builder
     */
    private RedirectLocationBuilder parameter(String name, String value) {
        parameters.add(new AbstractMap.SimpleEntry<String, String>(name, value));
        return this;
    }

    /**
     * Gets the relative path to the login page as defined by the <code>com.openexchange.share.loginLink</code> and
     * <code>com.openexchange.UIWebPath</code> configuration properties.
     *
     * @return The relative login link, e.g. <code>/appsuite/ui</code>
     */
    private static String getLoginLink() {
        ConfigurationService configService = ShareServiceLookup.getService(ConfigurationService.class);
        String loginLink = configService.getProperty("com.openexchange.share.loginLink", "/[uiwebpath]/ui");
        String uiWebPath = configService.getProperty("com.openexchange.UIWebPath", "/appsuite");
        return P_UIWEBPATH.matcher(loginLink).replaceAll(Matcher.quoteReplacement(trimSlashes(uiWebPath)));
    }

    /**
     * Trims trailing and leading slashes from the supplied path.
     *
     * @param path The path
     * @return The trimmed path
     */
    private static String trimSlashes(String path) {
        String pazz = path;
        if (null != pazz && 0 < pazz.length()) {
            if ('/' == pazz.charAt(0)) {
                pazz = pazz.substring(1);
            }
            if (0 < pazz.length() && '/' == pazz.charAt(pazz.length() - 1)) {
                pazz = pazz.substring(0, pazz.length() - 1);
            }
        }
        return pazz;
    }

}
