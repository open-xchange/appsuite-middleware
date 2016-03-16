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

package com.openexchange.oauth.httpclient.impl.scribe;

import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.BoxApi;
import org.scribe.builder.api.DropBoxApi;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.Google2Api;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.MsLiveConnectApi;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.builder.api.XingApi;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.httpclient.OAuthHTTPRequest;
import com.openexchange.oauth.httpclient.OAuthHTTPRequestBuilder;

public abstract class ScribeGenericHTTPRequestBuilder<T extends HTTPGenericRequestBuilder<T>> {

    protected final Map<String, String> parameters;
    protected final Map<String, String> headers;
    protected final OAuthHTTPRequestBuilder coreBuilder;
    private boolean isVerbatimUrl;
    private String verbatimUrl;
    private String baseUrl;
    protected final OAuthService scribeOAuthService;
    protected final Class<? extends org.scribe.builder.api.Api> provider;

    /**
     * Initializes a new {@link ScribeGenericHTTPRequestBuilder}.
     *
     * @param coreBuilder The parental request builder
     * @throws IllegalStateException If associated API cannot be mapped to a Scribe provider
     */
    protected ScribeGenericHTTPRequestBuilder(final OAuthHTTPRequestBuilder coreBuilder) {
        super();
        parameters = new TreeMap<String, String>();
        headers = new TreeMap<String, String>();
        this.coreBuilder = coreBuilder;
        provider = getProvider(coreBuilder.getApi());
        scribeOAuthService =
            new ServiceBuilder().provider(getProvider(coreBuilder.getApi())).apiKey(coreBuilder.getApiKey()).apiSecret(
                coreBuilder.getSecret()).build();
    }

    /**
     * Gets the associated verb.
     *
     * @return The verb
     */
    protected abstract Verb getVerb();

    /**
     * Gets the Scribe provider for given API.
     *
     * @param api The API
     * @return The associated Scribe provider
     * @throws IllegalStateException If given API cannot be mapped to a Scribe provider
     */
    protected static Class<? extends Api> getProvider(final API api) {
        switch (api) {
        case LINKEDIN:
            return LinkedInApi.class;
        case TWITTER:
            return TwitterApi.class;
        case YAHOO:
            return YahooApi.class;
        case TUMBLR:
            return TumblrApi.class;
        case FLICKR:
            return FlickrApi.class;
        case DROPBOX:
            return DropBoxApi.class;
        case XING:
            return XingApi.class;
        case GOOGLE:
            return Google2Api.class;
        case BOX_COM:
            return BoxApi.class;
        case MS_LIVE_CONNECT:
            return MsLiveConnectApi.class;
        case VKONTAKTE:
            return VkontakteApi.class;
            // Add new API enums above

        case OTHER: // fall-through
        default:
        }
        throw new IllegalStateException("Unsupported API type: " + api);
    }

    @SuppressWarnings("unchecked")
    public T url(final String url) {
        isVerbatimUrl = false;
        this.baseUrl = url;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T verbatimURL(final String url) {
        isVerbatimUrl = true;
        this.verbatimUrl = url;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T parameter(final String parameter, final String value) {
        parameters.put(parameter, value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T parameters(final Map<String, String> parameters) {
        this.parameters.putAll(parameters);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T header(final String header, final String value) {
        headers.put(header, value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T headers(final Map<String, String> cookies) {
        headers.putAll(cookies);
        return (T) this;
    }

    /**
     * Builds the HTTP request.
     *
     * @return The HTTP request
     * @throws OXException If operation fails
     */
    public HTTPRequest build() throws OXException {
        final OAuthRequest request;
        {
            String finalUrl;
            try {
                finalUrl = isVerbatimUrl ? verbatimUrl : URIUtil.encodeQuery(baseUrl);
            } catch (final URIException e) {
                finalUrl = baseUrl;
            }
            request = new OAuthRequest(getVerb(), finalUrl);
        }
        setParameters(parameters, request);
        setHeaders(headers, request);
        modify(request);
        scribeOAuthService.signRequest(getToken(), request);
        return new OAuthHTTPRequest(request, parameters);
    }

    protected void setHeaders(final Map<String, String> headers, final OAuthRequest request) {
        for (final Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey() != null && header.getValue() != null) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }
    }

    protected void setParameters(final Map<String, String> params, final OAuthRequest request) {
        for (final Map.Entry<String, String> param : params.entrySet()) {
            final String key = param.getKey();
            if (key != null) {
                final String value = param.getValue();
                if (value != null) {
                    request.addQuerystringParameter(key, value);
                }
            }
        }
    }

    protected void modify(final OAuthRequest request) {
        // Nope
    }

    /**
     * Gets the Scribe token for associated OAuth account.
     *
     * @return The Scribe token
     * @throws OXException If operation fails due to an invalid account
     */
    private Token getToken() throws OXException {
        final OAuthAccount account = coreBuilder.getAccount();
        try {
            return new Token(account.getToken(), account.getSecret());
        } catch (final RuntimeException e) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ScribeGenericHTTPRequestBuilder.class);
            logger.warn("Associated OAuth \"{} ({})\" account misses token information.", account.getDisplayName(), account.getId());
            throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e, account.getDisplayName(), account.getId());
        }
    }

}
