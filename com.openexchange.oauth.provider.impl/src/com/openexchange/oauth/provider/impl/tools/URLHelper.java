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

    public static String getBaseLocation(HttpServletRequest request) throws OXException {
        String hostname = getHostname(request);
        String prefix = Services.requireService(DispatcherPrefixService.class).getPrefix();
        return "https://" + hostname + prefix;

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
