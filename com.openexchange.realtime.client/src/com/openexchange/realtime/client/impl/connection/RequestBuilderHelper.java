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

package com.openexchange.realtime.client.impl.connection;

import org.atmosphere.wasync.impl.AtmosphereClient;
import org.atmosphere.wasync.impl.AtmosphereRequest.AtmosphereRequestBuilder;
import com.ning.http.client.FluentStringsMap;
import com.ning.http.client.RequestBuilder;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTSession;
import com.openexchange.realtime.client.impl.config.ConfigurationProvider;

/**
 * {@link RequestBuilderHelper} Utility to create preconfigured {@link RequestBuilders} for realtime request.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RequestBuilderHelper {

    /**
     * Create a new RequestBuilder for realtime "send" requests. Host, Path, Method, Content-Type- and Cookie-header are already set based
     * on values found in {@link Constants} or overwritten via {@link ConfigurationProvider}.
     *
     * @return the preconfigured RequestBuilder that can be enhanced with the request body
     */
    public static RequestBuilder newSendRequest(final RTConnectionProperties connectionProperties, final RTSession state) {
        RequestBuilder builder = new RequestBuilder("PUT");
        builder.setUrl(buildUrl(ConfigurationProvider.getInstance().getSendPath(), connectionProperties));
        builder.setHeader("Content-Type", "text/javascript; charset=UTF-8");
        builder.setHeader("User-Agent", Constants.USER_AGENT_NAME);
        builder.setHeader("Cookie", cookieHeaderFromSession(state));
        builder.setQueryParameters(getSendParameters(connectionProperties.getResource(), state));
        return builder;
    }

    /**
     * Create a new RequestBuilder for realtime "query" requests. Host, Path, Method, Content-Type- and Cookie-header are already set based
     * on values found in {@link Constants} or overwritten via {@link ConfigurationProvider}.
     *
     * @return the preconfigured RequestBuilder that can be enhanced with the request body
     */
    public static RequestBuilder newQueryRequest(final RTConnectionProperties connectionProperties, final RTSession state) {
        RequestBuilder builder = new RequestBuilder("PUT");
        builder.setUrl(buildUrl(ConfigurationProvider.getInstance().getQueryPath(), connectionProperties));
        builder.setHeader("Content-Type", "text/javascript; charset=UTF-8");
        builder.setHeader("User-Agent", Constants.USER_AGENT_NAME);
        builder.setHeader("Cookie", cookieHeaderFromSession(state));
        builder.setQueryParameters(getQueryParameters(connectionProperties.getResource(), state));
        return builder;
    }

    public static String cookieHeaderFromSession(final RTSession session) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.JSESSIONID_NAME).append("=").append(session.getjSessionID());
        sb.append(";");
        sb.append(session.getSecretSessionKey()).append("=").append(session.getSecretSessionValue());
        return sb.toString();
    }

    private static FluentStringsMap getQueryParameters(final String resource, final RTSession state) {
        FluentStringsMap params = getDefaultParameters(resource, state);
        params.add("action", "query");
        return params;
    }

    private static FluentStringsMap getSendParameters(final String resource, final RTSession state) {
        FluentStringsMap params = getDefaultParameters(resource, state);
        params.add("action", "send");
        return params;
    }

    private static FluentStringsMap getDefaultParameters(final String resource, final RTSession state) {
        FluentStringsMap parameterMap = new FluentStringsMap();
        parameterMap.add("resource", resource);
        parameterMap.add("session", state.getSessionID());
        return parameterMap;
    }

    private static String buildUrl(String queryPath, RTConnectionProperties connectionProperties) {
        StringBuilder sb = new StringBuilder();

        boolean isSecure = connectionProperties.getSecure();
        String host = connectionProperties.getHost();
        int port = connectionProperties.getPort();
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        if (isSecure) {
            sb.append("https");
        } else {
            sb.append("http");
        }
        sb.append("://").append(host);
        if (port != -1) {
            sb.append(":").append(port);
        }
        sb.append(queryPath);

        return sb.toString();
    }

    /**
     * Creates a new {@link RequestBuilder}
     *
     * @param atmosphereClient - {@link AtmosphereClient} for that a {@link RequestBuilder} should be created for.
     * @param connectionProperties - {@link RTConnectionProperties} with the connection information to route the request.
     * @param userState - {@link RTSession} with the user information to use the existing connection
     * @return {@link RequestBuilder} to build an atmosphere request
     */
    public static org.atmosphere.wasync.RequestBuilder<AtmosphereRequestBuilder> newAtmosphereRequestBuilder(AtmosphereClient atmosphereClient, RTConnectionProperties connectionProperties, RTSession userState) {
        final org.atmosphere.wasync.RequestBuilder<AtmosphereRequestBuilder> requestBuilder = atmosphereClient.newRequestBuilder();
        requestBuilder.method(org.atmosphere.wasync.Request.METHOD.GET);
        requestBuilder.uri(buildUrl(ConfigurationProvider.getInstance().getAtmospherePath(), connectionProperties));
        requestBuilder.transport(org.atmosphere.wasync.Request.TRANSPORT.LONG_POLLING);
        requestBuilder.header(
            "Cookie",
            "JSESSIONID=" + userState.getjSessionID() + "; " + userState.getSecretSessionKey() + "=" + userState.getSecretSessionValue() + "; " + "open-xchange-public-session=" + userState.getSessionID());
        requestBuilder.header("X-Atmosphere-Transport", "long-polling");
        requestBuilder.header("User-Agent", Constants.USER_AGENT_NAME);
        // IMPORTANT: send the session included in the query string
        requestBuilder.queryString("session", userState.getSessionID());
        // IMPORTANT: set 'resource' to the string to allow a client have more than one atmosphere resource assigned
        requestBuilder.queryString("resource", connectionProperties.getResource());

        return requestBuilder;
    }

}
