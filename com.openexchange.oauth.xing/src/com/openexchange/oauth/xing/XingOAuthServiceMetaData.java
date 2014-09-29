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

package com.openexchange.oauth.xing;

import java.util.HashMap;
import java.util.Map;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.XingApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link XingOAuthServiceMetaData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class XingOAuthServiceMetaData extends AbstractOAuthServiceMetaData implements com.openexchange.oauth.ScribeAware, Reloadable {

    private static final Logger LOGGER = LoggerFactory.getLogger(XingOAuthServiceMetaData.class);

    private final static String[] PROPERTIES = new String[] {"com.openexchange.oauth.xing.apiKey",
        "com.openexchange.oauth.xing.apiSecret", "com.openexchange.oauth.xing.consumerKey", "com.openexchange.oauth.xing.consumerSecret"};

    // -------------------------------------------------------------------------------------------------- //

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XingOAuthServiceMetaData}.
     *
     * @param services The service look-up
     * @throws IllegalStateException If either API key or secret is missing
     */
    public XingOAuthServiceMetaData(final ServiceLookup services) {
        super();
        this.services = services;
        id = "com.openexchange.oauth.xing";
        displayName = "XING";
        setAPIKeyName("com.openexchange.oauth.xing.apiKey");
        setAPISecretName("com.openexchange.oauth.xing.apiSecret");
        setConsumerKeyName("com.openexchange.oauth.xing.consumerKey");
        setConsumerSecretName("com.openexchange.oauth.xing.consumerSecret");

        final ConfigurationService configService = services.getService(ConfigurationService.class);
        if (null == configService) {
            throw new IllegalStateException("Missing configuration service");
        }
        final String apiKey = configService.getProperty("com.openexchange.oauth.xing.apiKey");
        if (Strings.isEmpty(apiKey)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.apiKey");
        }
        this.apiKey = apiKey;

        final String apiSecret = configService.getProperty("com.openexchange.oauth.xing.apiSecret");
        if (Strings.isEmpty(apiSecret)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.apiSecret");
        }
        this.apiSecret = apiSecret;

        final String consumerKey = configService.getProperty("com.openexchange.oauth.xing.consumerKey");
        if (Strings.isEmpty(consumerKey)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.consumerKey");
        }
        this.consumerKey = consumerKey;

        final String consumerSecret = configService.getProperty("com.openexchange.oauth.xing.consumerSecret");
        if (Strings.isEmpty(consumerSecret)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.consumerSecret");
        }
        this.consumerSecret = consumerSecret;
    }

    @Override
    public API getAPI() {
        return API.XING;
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return XingApi.class;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        final String apiKey = configService.getProperty("com.openexchange.oauth.xing.apiKey");
        if (Strings.isEmpty(apiKey)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.apiKey");
        }
        this.apiKey = apiKey;

        final String apiSecret = configService.getProperty("com.openexchange.oauth.xing.apiSecret");
        if (Strings.isEmpty(apiSecret)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.apiSecret");
        }
        this.apiSecret = apiSecret;

        final String consumerKey = configService.getProperty("com.openexchange.oauth.xing.consumerKey");
        if (Strings.isEmpty(consumerKey)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.consumerKey");
        }
        this.consumerKey = consumerKey;

        final String consumerSecret = configService.getProperty("com.openexchange.oauth.xing.consumerSecret");
        if (Strings.isEmpty(consumerSecret)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.consumerSecret");
        }
        this.consumerKey = consumerSecret;
    }

    @Override
    public Map<String, String[]> getConfigFileNames() {
        Map<String, String[]> map = new HashMap<String, String[]>(1);
        map.put("xingoauth.properties", PROPERTIES);
        return map;
    }

    @Override
    public boolean registerTokenBasedDeferrer() {
        return true;
    }

    @Override
    public String modifyCallbackURL(final String callbackUrl, final String currentHost, final Session session) {
        if (null == callbackUrl) {
            return super.modifyCallbackURL(callbackUrl, currentHost, session);
        }

        final DeferringURLService deferrer = services.getService(DeferringURLService.class);
        if (null != deferrer && deferrer.isDeferrerURLAvailable(session.getUserId(), session.getContextId())) {
            final String retval = deferrer.getDeferredURL(callbackUrl, session.getUserId(), session.getContextId());
            LOGGER.debug("Initializing XING OAuth account for user {} in context {} with call-back URL: {}", session.getUserId(), session.getContextId(), retval);
            return retval;
        }

        final String retval = deferredURLUsing(callbackUrl, new StringBuilder(extractProtocol(callbackUrl)).append("://").append(currentHost).append('/').toString());
        LOGGER.debug("Initializing XING OAuth account for user {} in context {} with call-back URL: {}", session.getUserId(), session.getContextId(), retval);
        return retval;
    }

    private String extractProtocol(final String url) {
        return Strings.toLowerCase(url).startsWith("https") ? "https" : "http";
    }

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

    private static boolean seemsAlreadyDeferred(final String url, final String deferrerURL, final String path) {
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
