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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.osgi.util.ServiceCallWrapper;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;

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
     * @throws OXException If determining the host name fails
     */
    public static String getSecureLocation(HttpServletRequest request) throws OXException {
        String hostname;
        try {
            hostname = ServiceCallWrapper.tryServiceCall(URLHelper.class, HostnameService.class, new ServiceUser<HostnameService, String>() {
                @Override
                public String call(HostnameService service) throws Exception {
                    return service.getHostname(-1, -1);
                }
            }, request.getServerName());
        } catch (ServiceException e) {
            throw e.toOXException();
        }

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

    public static String getRedirectLocation(String redirectURI, String... additionalParams) {
        StringBuilder builder = new StringBuilder(redirectURI);
        if (additionalParams != null && additionalParams.length > 0) {
            if (additionalParams.length % 2 != 0) {
                throw new IllegalArgumentException("The number of additional arguments must be even!");
            }

            char concat = '?';
            if (redirectURI.indexOf('?') >= 0) {
                concat = '&';
            } else if (redirectURI.endsWith("/")) {
                builder.setLength(builder.length() - 1);
            }

            for (int i = 0; i < additionalParams.length; i++) {
                String name = additionalParams[i++];
                String value = additionalParams[i];
                builder.append(concat);
                builder.append(name).append('=').append(encodeUrl(value));
                if (i > 0) {
                    concat = '&';
                }
            }
        }

        return builder.toString();
    }

    public static String getErrorRedirectLocation(String redirectURI, String error, String errorDescription, String... additionalParams) {
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

    public static String encodeUrl(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 must not fail
            throw new RuntimeException(e);
        }
    }

}
