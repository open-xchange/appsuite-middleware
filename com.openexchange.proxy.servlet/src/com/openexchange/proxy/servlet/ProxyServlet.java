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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.proxy.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.proxy.ProxyRegistration;
import com.openexchange.proxy.Response;
import com.openexchange.proxy.Restriction;

/**
 * {@link ProxyServlet}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ProxyServlet extends SessionServlet {

    private static final long serialVersionUID = -2988897861113557499L;

    /**
     * The HTTP time out.
     */
    private static final int TIMEOUT = 3000;

    /**
     * The HTTPS identifier constant.
     */
    private static final String HTTPS = "https";

    /**
     * The HTTP protocol constant.
     */
    private static final Protocol PROTOCOL_HTTP = Protocol.getProtocol("http");

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String sessionId = req.getParameter(AJAXServlet.PARAMETER_SESSION);
        if (null == sessionId) {
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Missing parameter \"session\"");
            return;
        }
        final String uuidStr = req.getParameter(AJAXServlet.PARAMETER_UID);
        if (null == uuidStr) {
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Missing parameter \"uid\"");
            return;
        }

        final ProxyRegistration registration = ProxyRegistryImpl.getInstance().getRegistration(sessionId, UUIDs.fromUnformattedString(uuidStr));
        if (null == registration) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        /*
         * Compose GET method from URL
         */
        final URL url = registration.getURL();
        final HttpClient client = new HttpClient();
        client.getParams().setSoTimeout(TIMEOUT);
        client.getParams().setIntParameter("http.connection.timeout", TIMEOUT);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        /*
         * Create host configuration or URI
         */
        final HostConfiguration hostConfiguration;
        {
            final String host = url.getHost();
            if (HTTPS.equalsIgnoreCase(url.getProtocol())) {
                int port = url.getPort();
                if (port == -1) {
                    port = 443;
                }
                /*
                 * Own HTTPS host configuration and relative URI
                 */
                final Protocol httpsProtocol = new Protocol(HTTPS, ((ProtocolSocketFactory) new TrustAllAdapter()), port);
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(host, port, httpsProtocol);
            } else {
                int port = url.getPort();
                if (port == -1) {
                    port = 80;
                }
                /*
                 * HTTP host configuration and relative URI
                 */
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(host, port, PROTOCOL_HTTP);
            }
        }
        final HttpMethodBase httpMethod = new GetMethod();
        String uri = url.getPath();
        /*
         * Create a URI and allow for null/empty URI values
         */
        if (uri == null || uri.equals("")) {
            uri = "/";
        }
        final HttpMethodParams params = httpMethod.getParams();
        httpMethod.setURI(new URI(uri, true, params.getUriCharset()));
        params.setSoTimeout(TIMEOUT);
        httpMethod.setQueryString(url.getQuery());
        /*
         * Fire request
         */
        final int responseCode = client.executeMethod(hostConfiguration, httpMethod);
        /*
         * Check response code
         */
        if (200 != responseCode) {
            /*
             * GET request failed
             */
            httpMethod.releaseConnection();
            resp.sendError(responseCode, httpMethod.getStatusLine().toString());
            return;
        }
        try {
            final Response response = new ResponseImpl(httpMethod);
            for (final Restriction restriction : registration.getRestrictions()) {
                if (!restriction.allow(response)) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Restriction failed: " + restriction.getDescription());
                    return;
                }
            }
            /*
             * Add response header
             */
            header2Response(httpMethod, resp);
            /*
             * Binary content
             */
            final InputStream responseStream = httpMethod.getResponseBodyAsStream();
            try {
                final OutputStream outputStream = resp.getOutputStream();
                final byte[] buf = new byte[8192];
                int read = -1;
                while ((read = responseStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, read);
                }
            } finally {
                try {
                    responseStream.close();
                } catch (final Exception e) {
                    org.apache.commons.logging.LogFactory.getLog(ProxyServlet.class).error(e.getMessage(), e);
                }
            }
            /*
             * Set status
             */
            resp.setStatus(httpMethod.getStatusCode());
        } finally {
            httpMethod.releaseConnection();
        }
    }

    private void header2Response(final HttpMethodBase method, final HttpServletResponse resp) {

        for (final Header header : method.getResponseHeaders()) {

            final String name = header.getName();
            final String value = header.getValue();

            if ("Content-Type".equals(name)) {
                if ("text/html".equals(value)) {
                    resp.setContentType("text/html; charset=" + resp.getCharacterEncoding());
                } else {
                    resp.setContentType(value);
                }
            } else if ("Content-Length".equals(name)) {
                // set content length
                final long length = method.getResponseContentLength();
                if (length > 0) {
                    resp.setContentLength((int) length);
                }
            }
        }
    }

}
