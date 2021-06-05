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

package com.openexchange.share.servlet.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import com.openexchange.i18n.Translator;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.tools.encoding.URLCoder;

/**
 * Holds a relative redirect location to the login page.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LoginLocation {

    /**
     * The default allowed attributes consisting of:
     * <ul>
     * <li><code>"login_name"</code></li>
     * <li><code>"login_type"</code></li>
     * <li><code>"share"</code></li>
     * <li><code>"target"</code></li>
     * </ul>
     */
    public static final Collection<String> DEFAULT_ALLOWED_ATTRIBUTES = Collections.unmodifiableCollection(Arrays.asList("login_name", "login_type", "share", "target", "confirm"));

    /**
     * Builds the redirect location using specified token
     *
     * @param token The token
     * @param location The associated login location
     * @param allowedAttributes Specifies those attributes kept in given <code>LoginLocation</code> instance that are allowed to be passed to client
     * @param translator The translator to use to translate locale-sensitive strings, or <code>null</code> to fall back to the untranslated version
     * @return The redirect location
     */
    public static String buildRedirectWith(String token, LoginLocation location, Collection<String> allowedAttributes, Translator translator) {
        StringBuilder sb = new StringBuilder(96);
        sb.append(ShareRedirectUtils.getLoginLink()).append("#!");
        sb.append("&token=").append(token);

        // Add attributes
        if (null != allowedAttributes) {
            Map<String, String> attributes = location.asMap(translator);
            for (String allowedAttribute : allowedAttributes) {
                if (Strings.isNotEmpty(allowedAttribute)) {
                    String value = attributes.get(allowedAttribute);
                    if (Strings.isNotEmpty(value)) {
                        sb.append('&').append(allowedAttribute).append('=').append(value);
                    }
                }
            }
        }

        return sb.toString();
    }


    // ----------------------------------------------------------------------------------------------------------------------------

    private final Map<String, String> parameters;

    private Function<Translator, String> translatingMessage;

    /**
     * Initializes a new {@link LoginLocation}
     */
    public LoginLocation() {
        super();
        parameters = new LinkedHashMap<String, String>(8);
    }

    /**
     * Appends the login type suitable for the supplied authentication mode.
     *
     * @param authentication The authentication mode
     * @return The builder
     */
    public LoginLocation loginType(AuthenticationMode authentication) {
        switch (authentication) {
            case GUEST:
                return loginType(LoginType.GUEST);
            case GUEST_PASSWORD:
                return loginType(LoginType.GUEST_PASSWORD);
            case ANONYMOUS_PASSWORD:
                return loginType(LoginType.ANONYMOUS_PASSWORD);
            default:
                throw new UnsupportedOperationException("No login type for " + authentication);
        }
    }

    /**
     * Appends the login type suitable for the supplied identifier.
     *
     * @param loginType The login type to set
     * @return The builder
     */
    public LoginLocation loginType(LoginType loginType) {
        return parameter("login_type", loginType.getId());
    }

    /**
     * Appends the (base) token of the accessed share.
     *
     * @param token The share token to append
     * @return The builder
     */
    public LoginLocation share(String token) {
        return parameter("share", token);
    }

    /**
     * Appends the path to a specific share target.
     *
     * @param targetPath The share target path to append, or <code>null</code> if not specified
     * @return The builder
     */
    public LoginLocation target(ShareTargetPath targetPath) {
        return null != targetPath ? parameter("target", targetPath.get()) : this;
    }

    /**
     * Appends the status parameter to pass to the client.
     *
     * @param status The message status
     * @return The builder
     */
    public LoginLocation status(String status) {
        parameter("status", status);
        return this;
    }

    /**
     * Appends a message to pass to the client along with the redirect location.
     *
     * @param type The message type
     * @param translatingMessage The function that yields the translated message using the supplied translator instance
     * @return The builder
     */
    public LoginLocation message(MessageType type, Function<Translator, String> translatingMessage) {
        parameter("message_type", type.toString());
        this.translatingMessage = translatingMessage;
        return this;
    }

    /**
     * Sets the login name, i.e. the guest users email address.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return This login location
     */
    public LoginLocation loginName(int userId, int contextId) {
        return parameter("login_name", new StringBuilder(16).append(userId).append('@').append(contextId).toString());
    }

    /**
     * Adds an additional parameter to the builder instance.
     *
     * @param name The parameter name
     * @param value The parameter value
     * @return The builder
     */
    public LoginLocation parameter(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Gets the map view for this login location
     *
     * @param translator The translator to use to translate locale-sensitive strings, or <code>null</code> to fall back to the untranslated version
     * @return The parameters map
     */
    public Map<String, String> asMap(Translator translator) {
        if (null == translatingMessage) {
            return Collections.unmodifiableMap(parameters);
        }
        Map<String, String> map = new HashMap<String, String>(parameters);
        map.put("message", translatingMessage.apply(null == translator ? Translator.EMPTY : translator));
        return map;
    }

    /**
     * Builds and returns the relative redirect location, ready to use in the <code>Location</code> header of a HTTP response.
     *
     * @param translator The translator to use to translate locale-sensitive strings, or <code>null</code> to fall back to the untranslated version
     * @return The built redirect URL
     */
    public String toString(Translator translator) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(ShareRedirectUtils.getLoginLink()).append("#!");
        for (Entry<String, String> entry : asMap(translator).entrySet()) {
            sb.append('&').append(entry.getKey()).append('=').append(URLCoder.encode(entry.getValue(), Charsets.UTF_8));
        }
        return sb.toString();
    }

    /**
     * Builds and returns the relative redirect location, ready to use in the <code>Location</code> header of a HTTP response.
     *
     * @return The built redirect URL
     */
    @Override
    public String toString() {
        return toString(Translator.EMPTY);
    }

}
