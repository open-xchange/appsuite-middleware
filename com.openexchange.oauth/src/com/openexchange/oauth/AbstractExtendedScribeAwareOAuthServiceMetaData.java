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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
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
     * Initialises a new {@link AbstractExtendedScribeAwareOAuthServiceMetaData}.
     * 
     * @param services The {@link ServiceLookup} instance
     * @param id The service identifier
     * @param displayName The display name
     */
    public AbstractExtendedScribeAwareOAuthServiceMetaData(ServiceLookup services, String id, String displayName) {
        this(services, id, displayName, false, true);
    }

    /**
     * Initialises a new {@link AbstractExtendedScribeAwareOAuthServiceMetaData}.
     * 
     * @param services The {@link ServiceLookup} instance
     * @param id The service identifier
     * @param displayName The display name
     * @param needsRequestToken Whether it needs a request token
     * @param registerTokenBasedDeferrer Whether to register the token based deferrer
     */
    public AbstractExtendedScribeAwareOAuthServiceMetaData(ServiceLookup services, String id, String displayName, boolean needsRequestToken, boolean registerTokenBasedDeferrer) {
        super(services, id, displayName);
        this.needsRequestToken = needsRequestToken;
        this.registerTokenBasedDeferrer = registerTokenBasedDeferrer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.AbstractOAuthServiceMetaData#getRegisterToken(java.lang.String)
     */
    @Override
    public String getRegisterToken(String authUrl) {
        int pos = authUrl.indexOf("&state=");
        if (pos <= 0) {
            return null;
        }

        int nextPos = authUrl.indexOf('&', pos + 1);
        return nextPos < 0 ? authUrl.substring(pos + 7) : authUrl.substring(pos + 7, nextPos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.AbstractOAuthServiceMetaData#processAuthorizationURL(java.lang.String)
     */
    @Override
    public String processAuthorizationURL(String authUrl) {
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
                String redirectUri = trimRedirectUri(authUrl.substring(pos + 14));
                authUrlBuilder = new StringBuilder(authUrl.substring(0, pos)).append("&redirect_uri=").append(redirectUri);
            } else {
                // There are more URL parameters
                String redirectUri = trimRedirectUri(authUrl.substring(pos + 14, nextPos));
                authUrlBuilder = new StringBuilder(authUrl.substring(0, pos)).append("&redirect_uri=").append(redirectUri).append(authUrl.substring(nextPos));
            }
        }

        // Append state parameter used for later look-up in "CallbackRegistry" class
        return authUrlBuilder.append("&state=").append("__ox").append(UUIDs.getUnformattedString(UUID.randomUUID())).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.AbstractOAuthServiceMetaData#processArguments(java.util.Map, java.util.Map, java.util.Map)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.AbstractOAuthServiceMetaData#getOAuthToken(java.util.Map)
     */
    @Override
    public OAuthToken getOAuthToken(Map<String, Object> arguments) throws OXException {
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

        final String scope = getScope();
        if (null != scope) {
            serviceBuilder.scope(scope);
        }

        OAuthService scribeOAuthService = serviceBuilder.build();

        Verifier verifier = new Verifier((String) arguments.get(org.scribe.model.OAuthConstants.CODE));
        Token accessToken = scribeOAuthService.getAccessToken(null, verifier);

        return new DefaultOAuthToken(accessToken.getToken(), accessToken.getSecret());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.AbstractOAuthServiceMetaData#modifyCallbackURL(java.lang.String, java.lang.String, com.openexchange.session.Session)
     */
    @Override
    public String modifyCallbackURL(final String callbackUrl, final String currentHost, final Session session) {
        if (null == callbackUrl) {
            return super.modifyCallbackURL(callbackUrl, currentHost, session);
        }

        final DeferringURLService deferrer = services.getService(DeferringURLService.class);
        if (null != deferrer && deferrer.isDeferrerURLAvailable(session.getUserId(), session.getContextId())) {
            final String retval = deferrer.getDeferredURL(callbackUrl, session.getUserId(), session.getContextId());
            LOGGER.debug("Initializing {} OAuth account for user {} in context {} with call-back URL: {}", getDisplayName(), session.getUserId(), session.getContextId(), retval);
            return retval;
        }

        final String retval = deferredURLUsing(callbackUrl, new StringBuilder(extractProtocol(callbackUrl)).append("://").append(currentHost).toString());
        LOGGER.debug("Initializing {} OAuth account for user {} in context {} with call-back URL: {}", getDisplayName(), session.getUserId(), session.getContextId(), retval);
        return retval;
    }

    /**
     * Trims the specified redirect URI (strips the protocol)
     * 
     * @param redirectUri The redirect URI to trim
     * @return The trimmed redirect URI
     */
    private String trimRedirectUri(String redirectUri) {
        OAuthConfigurationProperty oAuthProperty = getOAuthProperty(OAuthPropertyID.redirectUrl);
        if (oAuthProperty == null) {
            return redirectUri;
        }
        String actual = oAuthProperty.getValue();
        if (!stripProtocol(redirectUri).startsWith(stripProtocol(actual))) {
            return redirectUri;
        }
        return actual;
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
     * @return The extracted protocol (either http or https)
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
