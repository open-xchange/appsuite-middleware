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

package com.openexchange.oauth;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Strings.isEmpty;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLHandshakeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.exceptions.OAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.RequestContext;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.session.Session;

/**
 * {@link OAuthUtil} - Utility class for OAuth.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class OAuthUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthUtil.class);

    /**
     * Checks that specified scopes are both - available and enabled.
     *
     * @param oauthAccount The OAuth account providing enabled scopes
     * @param session The session providing user data
     * @param scopes The scopes to check
     * @throws OXException If one of specified scopes is either not available or not enabled
     */
    public static void checkScopesAvailableAndEnabled(OAuthAccount oauthAccount, Session session, OXScope... scopes) throws OXException {
        checkScopesAvailableAndEnabled(oauthAccount, session.getUserId(), session.getContextId(), scopes);
    }

    /**
     * Checks that specified scopes are both - available and enabled.
     *
     * @param oauthAccount The OAuth account providing enabled scopes
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param scopes The scopes to check
     * @throws OXException If one of specified scopes is either not available or not enabled
     */
    public static void checkScopesAvailableAndEnabled(OAuthAccount oauthAccount, int userId, int contextId, OXScope... scopes) throws OXException {
        if (null == oauthAccount) {
            return;
        }
        if (null == scopes || scopes.length <= 0) {
            return;
        }

        OAuthServiceMetaData oAuthServiceMetaData = oauthAccount.getMetaData();

        // Check the scopes permitted through "com.openexchange.oauth.modules.enabled + <service>" property
        {
            Set<OAuthScope> availableScopes = oAuthServiceMetaData.getAvailableScopes(userId, contextId);
            for (OXScope oxScope : scopes) {
                boolean found = false;
                for (Iterator<OAuthScope> it = availableScopes.iterator(); false == found && it.hasNext();) {
                    if (oxScope == it.next().getOXScope()) {
                        found = true;
                    }
                }
                if (false == found) {
                    throw OAuthExceptionCodes.NO_SUCH_SCOPE_AVAILABLE.create(oxScope.getDisplayName());
                }
            }
        }

        // Check the scopes enabled for the account
        {
            Set<OAuthScope> enabledScopes = oauthAccount.getEnabledScopes();
            for (OXScope oxScope : scopes) {
                boolean supported = false;
                for (Iterator<OAuthScope> it = enabledScopes.iterator(); !supported && it.hasNext();) {
                    if (oxScope == it.next().getOXScope()) {
                        supported = true;
                    }
                }
                if (false == supported) {
                    throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(oAuthServiceMetaData.getDisplayName(), oxScope.getDisplayName());
                }
            }
        }
    }

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
        Set<String> scopeIdentifiers = new LinkedHashSet<>();
        for (OAuthScope scope : scopes) {
            String[] split = Strings.splitByWhitespaces(scope.getProviderScopes());
            for (String scopeIdentifier : split) {
                scopeIdentifiers.add(scopeIdentifier);
            }
        }
        return setToString(scopeIdentifiers);
    }

    /**
     * Parses the specified {@link Set} with {@link OAuthScope}s and returns
     * the ({@link OAuthScope#getOXScope()}) as a whitespace separated string
     *
     * @param scopes The {@link OAuthScope}s
     * @return a space separated string with all {@link OAuthScope}s in the specified {@link Set}
     */
    public static final String oxScopesToString(Set<OAuthScope> scopes) {
        Set<String> scopeNames = new LinkedHashSet<>();
        for (OAuthScope scope : scopes) {
            scopeNames.add(scope.getOXScope().name());
        }
        return setToString(scopeNames);
    }

    /**
     * Creates a whitespace separated list of strings out of the specified {@link Set}
     *
     * @param scopes The {@link Set} with the strings
     * @return a whitespace separated list of strings
     */
    private static final String setToString(Set<String> scopes) {
        if (scopes.isEmpty()) {
            return "";
        }

        Iterator<String> iter = scopes.iterator();
        StringBuilder builder = new StringBuilder();
        builder.append(iter.next());
        while (iter.hasNext()) {
            builder.append(' ').append(iter.next());
        }
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
        builder.append("&serviceId=").append(account.getAPI().getDisplayName());
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
                // Log and ignore
                LOGGER.debug("", e);
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
        } catch (UnsupportedEncodingException e) {
            LOGGER.debug("", e);
            return s;
        }
    }

    /**
     * Returns the OAuth account identifier from associated account's configuration
     *
     * @param configuration The configuration
     * @return The account identifier
     * @throws IllegalArgumentException If the configuration is <code>null</code>, or if the account identifier is not present, or is present but cannot be parsed as an integer
     */
    public static int getAccountId(Map<String, Object> configuration) {
        if (null == configuration) {
            throw new IllegalArgumentException("The configuration cannot be 'null'");
        }

        Object accountId = configuration.get("account");
        if (null == accountId) {
            throw new IllegalArgumentException("The account identifier is missing from the configuration");
        }

        if (accountId instanceof Integer) {
            return ((Integer) accountId).intValue();
        }

        try {
            return Integer.parseInt(accountId.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The account identifier '" + accountId.toString() + "' cannot be parsed as an integer.", e);
        }
    }

    /**
     * Handles the specified {@link OAuthException} for the specified {@link OAuthAccount} and the
     * specified {@link Session} and returns an appropriate {@link OXException}.
     *
     * @param e The exception to handle
     * @param oauthAccount the {@link OAuthAccount}
     * @param session The groupware session
     * @return The appropriate OXException
     */
    public static OXException handleScribeOAuthException(OAuthException e, OAuthAccount oauthAccount, Session session) {
        if (ExceptionUtils.isEitherOf(e, SSLHandshakeException.class)) {
            List<Object> displayArgs = new ArrayList<>(2);
            displayArgs.add(SSLExceptionCode.extractArgument(e, "fingerprint"));
            displayArgs.add(oauthAccount.getAPI().getURL());
            return SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(e, displayArgs.toArray(new Object[] {}));
        }

        String exMessage = e.getMessage();
        String errorMsg = parseKeyFrom(exMessage, "error");
        if (Strings.isEmpty(errorMsg)) {
            return OAuthExceptionCodes.OAUTH_ERROR.create(e, exMessage);
        }
        if (exMessage.contains("invalid_grant") || exMessage.contains("deleted_client")) {
            if (null != oauthAccount) {
                return OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(e, oauthAccount.getDisplayName(), I(oauthAccount.getId()), I(session.getUserId()), I(session.getContextId()));
            }
            return OAuthExceptionCodes.INVALID_ACCOUNT.create(e, new Object[0]);
        }

        String errorDescription = parseKeyFrom(exMessage, "error_description");
        if (Strings.isEmpty(errorDescription)) {
            return OAuthExceptionCodes.OAUTH_ERROR.create(e, exMessage);
        }
        if (errorDescription.contains("Missing required parameter: refresh_token")) {
             return OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(oauthAccount.getDisplayName(), I(oauthAccount.getId()));
        }
        return OAuthExceptionCodes.OAUTH_ERROR.create(e, exMessage);
    }

    /**
     * Parses the specified key from from the specified message
     *
     * @param message The message from which to parse the error code
     * @return The error code, or <code>null</code> if none can be parsed
     */
    private static String parseKeyFrom(String message, String key) {
        if (Strings.isEmpty(message)) {
            return null;
        }

        String marker = "Can't extract a token from this: '";
        int pos = message.indexOf(marker);
        if (pos < 0) {
            return null;
        }

        try {
            JSONObject jo = new JSONObject(message.substring(pos + marker.length(), message.length() - 1));
            return jo.optString(key, null);
        } catch (JSONException e) {
            // Apparent no JSON response
            LOGGER.debug("", e);
            return null;
        }
    }
}
