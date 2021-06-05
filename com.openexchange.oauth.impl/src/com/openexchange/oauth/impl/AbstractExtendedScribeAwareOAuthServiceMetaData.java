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

package com.openexchange.oauth.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.API;
import com.openexchange.oauth.DefaultAPI;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.HostInfo;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractExtendedScribeAwareOAuthServiceMetaData}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractExtendedScribeAwareOAuthServiceMetaData extends AbstractScribeAwareOAuthServiceMetaData {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractExtendedScribeAwareOAuthServiceMetaData.class);

    /**
     * Initializes a new {@link AbstractExtendedScribeAwareOAuthServiceMetaData}.
     *
     * @param services The {@link ServiceLookup}
     * @param api The {@link DefaultAPI}
     * @param scopes The {@link OAuthScope}s
     */
    protected AbstractExtendedScribeAwareOAuthServiceMetaData(ServiceLookup services, API api, OAuthScope... scopes) {
        this(services, api, false, true, scopes);
    }

    /**
     * Initializes a new {@link AbstractExtendedScribeAwareOAuthServiceMetaData}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param api The {@link DefaultAPI}
     * @param needsRequestToken Whether it needs a request token
     * @param registerTokenBasedDeferrer Whether to register the token based deferrer
     */
    protected AbstractExtendedScribeAwareOAuthServiceMetaData(ServiceLookup services, API api, boolean needsRequestToken, boolean registerTokenBasedDeferrer, OAuthScope... scopes) {
        super(services, api, scopes);
        this.needsRequestToken = needsRequestToken;
        this.registerTokenBasedDeferrer = registerTokenBasedDeferrer;
    }

    @Override
    public String getRegisterToken(String authUrl) {
        int pos = authUrl.indexOf("&state=");
        if (pos <= 0) {
            return null;
        }

        int nextPos = authUrl.indexOf('&', pos + 1);
        return nextPos < 0 ? authUrl.substring(pos + 7) : authUrl.substring(pos + 7, nextPos);
    }

    @Override
    public String processAuthorizationURL(String authUrl, final Session session) throws OXException {
        int pos = authUrl.indexOf("&redirect_uri=");
        if (pos <= 0) {
            return authUrl;
        }

        // Trim redirect URI to have an exact match to deferrer servlet path,
        // which should be the one defined as "Redirect URL" in the relevant app account
        StringBuilder authUrlBuilder;
        {
            int nextPos = authUrl.indexOf('&', pos + 1);
            if (nextPos < 0) {
                String redirectUri = trimRedirectUri(authUrl.substring(pos + 14), session);
                authUrlBuilder = new StringBuilder(authUrl.substring(0, pos)).append("&redirect_uri=").append(redirectUri);
            } else {
                // There are more URL parameters
                String redirectUri = trimRedirectUri(authUrl.substring(pos + 14, nextPos), session);
                authUrlBuilder = new StringBuilder(authUrl.substring(0, pos)).append("&redirect_uri=").append(redirectUri).append(authUrl.substring(nextPos));
            }
        }

        // Append state parameter used for later look-up in "CallbackRegistry" class
        return authUrlBuilder.append("&state=").append("__ox").append(UUIDs.getUnformattedString(UUID.randomUUID())).toString();
    }

    @Override
    public void processArguments(Map<String, Object> arguments, Map<String, String> parameter, Map<String, Object> state) throws OXException {
        String pCode = org.scribe.model.OAuthConstants.CODE;
        String code = parameter.get(pCode);
        if (Strings.isEmpty(code)) {
            throw OAuthExceptionCodes.MISSING_ARGUMENT.create(pCode);
        }
        arguments.put(pCode, code);

        String pAuthUrl = OAuthConstants.ARGUMENT_AUTH_URL;
        String authUrl = (String) state.get(pAuthUrl);
        if (Strings.isEmpty(authUrl)) {
            throw OAuthExceptionCodes.MISSING_ARGUMENT.create(pAuthUrl);
        }
        arguments.put(pAuthUrl, authUrl);
    }

    @Override
    public OAuthToken getOAuthToken(Map<String, Object> arguments, Set<OAuthScope> scopes) throws OXException {
        Session session = (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION);
        final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(getScribeService());
        serviceBuilder.apiKey(getAPIKey(session)).apiSecret(getAPISecret(session));

        final String callbackUrl = (String) arguments.get(OAuthConstants.ARGUMENT_CALLBACK);
        if (null != callbackUrl) {
            serviceBuilder.callback(callbackUrl);
        } else {
            try {
                String authUrl = (String) arguments.get(OAuthConstants.ARGUMENT_AUTH_URL);
                String pRedirectUri = "&redirect_uri=";
                int pos = authUrl.indexOf(pRedirectUri);
                int nextPos = authUrl.indexOf('&', pos + 1);
                String callback = nextPos < 0 ? authUrl.substring(pos + pRedirectUri.length()) : authUrl.substring(pos + pRedirectUri.length(), nextPos);
                callback = URLDecoder.decode(callback, "UTF-8");
                serviceBuilder.callback(callback);
            } catch (UnsupportedEncodingException e) {
                throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }

        // Add requested scopes
        String mappings = OAuthUtil.providerScopesToString(scopes);
        if (Strings.isNotEmpty(mappings)) {
            serviceBuilder.scope(mappings);
        }

        OAuthService scribeOAuthService = serviceBuilder.build();

        Verifier verifier = new Verifier((String) arguments.get(org.scribe.model.OAuthConstants.CODE));
        Token accessToken = scribeOAuthService.getAccessToken(null, verifier);

        return new DefaultOAuthToken(accessToken.getToken(), accessToken.getSecret(), accessToken.getExpiry() == null ? 0L : accessToken.getExpiry().getTime());
    }

    @Override
    public String modifyCallbackURL(final String callbackUrl, final HostInfo currentHost, final Session session) {
        if (null == callbackUrl) {
            return super.modifyCallbackURL(callbackUrl, currentHost, session);
        }

        final DeferringURLService deferrer = services.getService(DeferringURLService.class);
        if (null != deferrer && deferrer.isDeferrerURLAvailable(session.getUserId(), session.getContextId())) {
            final String retval = deferrer.getDeferredURL(callbackUrl, session.getUserId(), session.getContextId());
            LOGGER.debug("Initializing {} OAuth account for user {} in context {} with call-back URL: {}", getDisplayName(), I(session.getUserId()), I(session.getContextId()), retval);
            return retval;
        }

        final String retval = deferredURLUsing(callbackUrl, new StringBuilder(extractProtocol(callbackUrl)).append("://").append(currentHost.getHost()).toString());
        LOGGER.debug("Initializing {} OAuth account for user {} in context {} with call-back URL: {}", getDisplayName(), I(session.getUserId()), I(session.getContextId()), retval);
        return retval;
    }

    /**
     * Trims the specified redirect URI (strips the protocol)
     *
     * @param redirectUri The redirect URI to trim
     * @return The trimmed redirect URI
     * @throws OXException If redirect URI cannot be returned
     */
    private String trimRedirectUri(String redirectUri, Session session) throws OXException {
        String actual = optFromConfigViewFactory(session, null, OAuthPropertyID.redirectUrl);
        if (null == actual) {
            return redirectUri;
        }

        boolean startsWith = stripProtocol(redirectUri).startsWith(stripProtocol(actual));
        return startsWith ? actual : redirectUri;
    }

    /**
     * Strips the protocol from the specified URL
     *
     * @param encodedUrl The encoded URL
     * @return the encoded URL without the protocol
     */
    private String stripProtocol(String encodedUrl) {
        if (encodedUrl.startsWith("https")) {
            return encodedUrl.substring(5);
        } else if (encodedUrl.startsWith("http")) {
            return encodedUrl.substring(4);
        }
        return encodedUrl;
    }

    /**
     * Extracts the protocol from the specified URL
     *
     * @param url The URL
     * @return The extracted protocol (either <code>"http"</code> or <code>"https"</code>)
     */
    private String extractProtocol(final String url) {
        return Strings.toLowerCase(url).startsWith("https") ? "https" : "http";
    }

    /**
     * Defers the specified URL
     *
     * @param url The URL to defer
     * @param domain The domain
     * @return The deferred URL
     */
    private String deferredURLUsing(final String url, final String domain) {
        if (url == null) {
            return null;
        }
        if (Strings.isEmpty(domain)) {
            return url;
        }
        String deferrerURL = domain.trim();
        final DispatcherPrefixService prefixService = services.getService(DispatcherPrefixService.class);
        String path = new StringBuilder(prefixService.getPrefix()).append("defer").toString();
        if (!path.startsWith("/")) {
            path = new StringBuilder(path.length() + 1).append('/').append(path).toString();
        }
        if (seemsAlreadyDeferred(url, deferrerURL, path)) {
            // Already deferred
            return url;
        }
        // Return deferred URL
        return new StringBuilder(deferrerURL).append(path).append("?redirect=").append(AJAXUtility.encodeUrl(url, false, false)).toString();
    }

    /**
     * Checks whether the specified URL is already deferred
     *
     * @param url The URL to check
     * @param deferrerURL The deferred URL
     * @param path The path
     * @return true if the URL is already deferred; false otherwise
     */
    private boolean seemsAlreadyDeferred(final String url, final String deferrerURL, final String path) {
        final String str = "://";
        final int pos1 = url.indexOf(str);
        final int pos2 = deferrerURL.indexOf(str);
        if (pos1 > 0 && pos2 > 0) {
            final String deferrerPrefix = new StringBuilder(deferrerURL.substring(pos2)).append(path).toString();
            return url.substring(pos1).startsWith(deferrerPrefix);
        }
        final String deferrerPrefix = new StringBuilder(deferrerURL).append(path).toString();
        return url.startsWith(deferrerPrefix);
    }

}
