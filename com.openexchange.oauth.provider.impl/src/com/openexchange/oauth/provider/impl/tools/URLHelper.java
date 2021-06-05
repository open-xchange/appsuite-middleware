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

package com.openexchange.oauth.provider.impl.tools;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.oauth.provider.impl.osgi.Services;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link URLHelper}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class URLHelper {

    private URLHelper() {
        super();
    }

    /**
     * Takes a {@link HttpServletRequest} and constructs the value for a 'Location' header
     * while forcing 'https://' as protocol.
     *
     * @param request The servlet request
     * @return The absolute location
     */
    public static String getSecureLocation(HttpServletRequest request) {
        String hostname = getHostname(request);
        StringBuilder requestURL = new StringBuilder("https://").append(hostname).append(request.getServletPath());
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            requestURL.append(pathInfo);
        }

        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append('?');
            requestURL.append(queryString);
        }
        return requestURL.toString();
    }

    /**
     * Takes a {@link HttpServletRequest} and constructs the value for a 'Location' header
     * based on its given scheme, host and path.
     *
     * @param request The servlet request
     * @return The absolute location
     */
    public static String getRequestLocation(HttpServletRequest request) {
        String hostname = getHostname(request);
        StringBuilder requestURL = new StringBuilder(request.getScheme()).append("://").append(hostname).append(request.getServletPath());
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            requestURL.append(pathInfo);
        }

        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append('?');
            requestURL.append(queryString);
        }
        return requestURL.toString();
    }

    public static String getBaseLocation(HttpServletRequest request) throws OXException {
        String scheme = Tools.getProtocol(request);
        String hostname = getHostname(request);
        String prefix = Services.requireService(DispatcherPrefixService.class).getPrefix();
        return scheme + hostname + prefix;
    }

    public static String getRedirectLocation(String redirectURI, String... additionalParams) throws OXException {
        Map<String, String> parameterMap;
        if (additionalParams != null && additionalParams.length > 0) {
            if (additionalParams.length % 2 != 0) {
                throw new IllegalArgumentException("The number of additional arguments must be even!");
            }

            parameterMap = new LinkedHashMap<>();
            for (int i = 0; i < additionalParams.length; i++) {
                String name = additionalParams[i++];
                String value = additionalParams[i];
                parameterMap.put(name, value);
            }
        } else {
            parameterMap = Collections.emptyMap();
        }

        return getRedirectLocation(redirectURI, parameterMap);
    }

    public static String getRedirectLocation(String redirectURI, Map<String, String> additionalParams) throws OXException {
        try {
            URIBuilder builder = new URIBuilder(redirectURI);
            for (Entry<String, String> param : additionalParams.entrySet()) {
                builder.setParameter(param.getKey(), param.getValue());
            }

            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, "Could not construct redirect location for URI '" + redirectURI + "' and params '" + additionalParams + "'");
        }
    }

    public static String getErrorRedirectLocation(String redirectURI, String error, String errorDescription, String... additionalParams) throws OXException {
        List<String> params = new ArrayList<>(8);
        params.add(OAuthProviderConstants.PARAM_ERROR);
        params.add(error);
        params.add(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION);
        params.add(errorDescription);

        if (additionalParams != null) {
            for (String str : additionalParams) {
                params.add(str);
            }
        }

        return getRedirectLocation(redirectURI, params.toArray(new String[params.size()]));
    }

    public static String getHostname(HttpServletRequest request) {
        String hostname = null;
        HostnameService hostnameService = Services.getServiceLookup().getService(HostnameService.class);
        if (hostnameService != null) {
            hostname = hostnameService.getHostname(-1, -1);
        }

        if (hostname == null) {
            hostname = request.getServerName();
        }

        return hostname;
    }

}
