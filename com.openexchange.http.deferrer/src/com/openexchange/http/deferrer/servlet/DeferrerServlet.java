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

package com.openexchange.http.deferrer.servlet;

import static com.openexchange.ajax.AJAXServlet.encodeUrl;
import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.http.deferrer.CustomRedirectURLDetermination;
import com.openexchange.http.deferrer.impl.DefaultDeferringURLService;
import com.openexchange.java.Strings;

/**
 * {@link DeferrerServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DeferrerServlet extends HttpServlet {

    private static final long serialVersionUID = 1358634554782437089L;

    /**
     * The listing for custom handlers.
     */
    public static final List<CustomRedirectURLDetermination> CUSTOM_HANDLERS = new CopyOnWriteArrayList<CustomRedirectURLDetermination>();

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // Create a new HttpSession if it's missing
        req.getSession(true);

        // Get the URL to defer to
        // Redirect
        final String redirectURL = determineRedirectURL(req);
        if (redirectURL == null) {
            return;
        }
        char concat = '?';
        if (redirectURL.indexOf('?') >= 0) {
            concat = '&';
        }

        final Map<String, String> params = parseQueryStringFromUrl(redirectURL);
        final StringBuilder builder = new StringBuilder(AJAXUtility.encodeUrl(redirectURL, true, false));
        for (final Enumeration<?> parameterNames = req.getParameterNames(); parameterNames.hasMoreElements();) {
            final String name = (String) parameterNames.nextElement();
            if ("redirect".equals(name) || params.containsKey(name)) {
                continue;
            }
            final String parameter = req.getParameter(name);
            builder.append(concat);
            concat = '&';
            builder.append(name).append('=').append(AJAXUtility.encodeUrl(parameter, true, true));
        }
        resp.sendRedirect(builder.toString());
    }

    private String determineRedirectURL(final HttpServletRequest req) {
        for (final CustomRedirectURLDetermination determination : CUSTOM_HANDLERS) {
            final String url = determination.getURL(req);
            if (url != null) {
                return prepareCustomRedirectURL(url);
            }
        }
        return req.getParameter("redirect");
    }

    /**
     * Checks if custom redirect URL starts with deferrer path.
     * <p>
     * E.g. /ajax/defer?redirect=http:%2F%2Fmy.host.com%2Fpath...
     * <p>
     * This avoids duplicate redirect as redirect URL would again redirect to <code>DeferrerServlet</code>.
     *
     * @param url The redirect URL to check
     * @return The checked redirect URL
     */
    private String prepareCustomRedirectURL(final String url) {
        try {
            final URL jUrl = new URL(url);
            final String path = jUrl.getPath();
            if (null != path && Strings.toLowerCase(path).startsWith(getDeferrerPath())) {
                final String query = jUrl.getQuery();
                if (null != query) {
                    final Map<String, String> params = parseQueryString(query);
                    if (1 == params.size() && params.containsKey("redirect")) {
                        final String redirect = params.get("redirect");
                        return isEmpty(redirect) ? url : redirect;
                    }
                }
            }
        } catch (final Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(DeferrerServlet.class);
            logger.debug("", e);
        }
        return url;
    }

    private String getDeferrerPath() {
        return new StringBuilder(DefaultDeferringURLService.PREFIX.get().getPrefix()).append("defer").toString();
    }

    /**
     * Parses a query string from given URL.
     *
     * @param url The URL string to be parsed
     * @return The parsed parameters
     */
    private Map<String, String> parseQueryStringFromUrl(final String url) {
        try {
            final URL jUrl = new URL(url);
            final String query = jUrl.getQuery();
            if (null != query) {
                return parseQueryString(query);
            }
        } catch (final Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(DeferrerServlet.class);
            logger.debug("", e);
        }
        return Collections.emptyMap();
    }

    private static final java.util.regex.Pattern PATTERN_SPLIT = java.util.regex.Pattern.compile("&");

    /**
     * Parses given query string.
     *
     * @param queryStr The query string to be parsed
     * @return The parsed parameters
     */
    private Map<String, String> parseQueryString(final String queryStr) {
        final String[] paramsNVPs = PATTERN_SPLIT.split(queryStr, 0);
        final String defaultCharEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
        final Map<String, String> map = new LinkedHashMap<String, String>(4);
        for (String paramsNVP : paramsNVPs) {
            paramsNVP = paramsNVP.trim();
            if (paramsNVP.length() > 0) {
                // Look-up character '='
                final int pos = paramsNVP.indexOf('=');
                if (pos >= 0) {
                    map.put(paramsNVP.substring(0, pos), Utility.decodeUrl(paramsNVP.substring(pos + 1), defaultCharEnc));
                } else {
                    map.put(paramsNVP, "");
                }
            }
        }
        return map;
    }

}
