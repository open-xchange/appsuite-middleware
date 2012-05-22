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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.filters;

import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.MimeHeaders;

/**
 * {@link BackendRouteFilter} to append the backend route to the JSESSIONID cookie.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class BackendRouteFilter extends BaseFilter {

    private static final Log LOG = LogFactory.getLog(BackendRouteFilter.class);

    private String backendRoute;

    public BackendRouteFilter(String backendRoute) {
        this.backendRoute = backendRoute;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        HttpContent content = ctx.getMessage();
        return super.handleRead(ctx);
    }

    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
        Object message = ctx.getMessage();
        HttpContent content = (HttpContent) message;
        HttpHeader httpHeader = content.getHttpHeader();
        MimeHeaders mimeHeaders = httpHeader.getHeaders();
        Iterator<String> valuesIterator = mimeHeaders.values(Header.SetCookie).iterator();

        boolean notFound = true;
        while (valuesIterator.hasNext() && notFound) {
            String cookieValue = valuesIterator.next();
            if (cookieValue.startsWith("JSESSIONID")) {
                notFound=false;
                mimeHeaders.addValue(Header.SetCookie).setString(addRoute(cookieValue));
                valuesIterator.remove();
            }
        }
        return super.handleWrite(ctx);
    }

    /**
     * Add roue if this JSESSIONID cookie doesn't contain a backend route like .OX-0
     * 
     * @param jSessionId the current JSESSIONID header
     * @return the JSESSIONID header with backend route added
     */
    private String addRoute(final String jSessionId) {
        /*
         * Each cookie begins with a NAME=VALUE pair, followed by zero or more
         * semi-colon-separated attribute-value pairs. We just want to change
         * the VALUE so we split if the a-v pairs.
         */
        String[] crumbs = jSessionId.split(";");
        /*
         * The JSESSIONID may already have a route.
         * JSESSIONID=5852760717757920088[.OX]
         * TODO: add Handling of old AJP depending on value of route
         */
        String[] tokens = crumbs[0].split("\\.");
        if (tokens.length < 2) { //no route available
            StringBuilder sb = new StringBuilder(crumbs[0]);
            sb.append('.');
            sb.append(backendRoute);
            // Add back the a-v-pairs
            for (int i = 1; i < crumbs.length; i++) {
                sb.append("; ");
                sb.append(crumbs[i]);
                return sb.toString();
            }
            return sb.toString();
        } else {
            return jSessionId;
        }
    }

}
