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

package com.openexchange.proxy.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.proxy.ProxyRegistration;
import com.openexchange.proxy.Response;
import com.openexchange.proxy.Restriction;
import com.openexchange.proxy.servlet.osgi.Services;
import com.openexchange.rest.client.httpclient.HttpClientService;

/**
 * {@link ProxyServlet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ProxyServlet extends SessionServlet {

    private static final long serialVersionUID = -2988897861113557499L;
    
    /**
     * Initializes a new {@link ProxyServlet}.
     */
    public ProxyServlet() {
        super();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String sessionId = req.getParameter(AJAXServlet.PARAMETER_SESSION);
        if (null == sessionId) {
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Missing parameter \"" + AJAXServlet.PARAMETER_SESSION + "\"");
            return;
        }
        final String uuidStr = req.getParameter(AJAXServlet.PARAMETER_UID);
        if (null == uuidStr) {
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Missing parameter \"" + AJAXServlet.PARAMETER_UID + "\"");
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
        /*
         * Create host configuration or URI
         */
        final CloseableHttpClient client;
        try {
            client = getHttpClient();
        } catch (OXException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        HttpRequestBase httpMethod;
        try {
            httpMethod = new HttpGet(url.toURI());
        } catch (URISyntaxException e) {
            // should never happen
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        /*
         * Fire request
         */
        HttpResponse httpResp = client.execute(httpMethod);
        /*
         * Check response code
         */
        if (HttpStatus.SC_OK != httpResp.getStatusLine().getStatusCode()) {
            /*
             * GET request failed
             */
            final String txt = httpResp.getStatusLine().getReasonPhrase();
            resp.sendError(httpResp.getStatusLine().getStatusCode(), txt);
            return;
        }
        final Response response = new ResponseImpl(httpResp);
        for (final Restriction restriction : registration.getRestrictions()) {
            if (!restriction.allow(response)) {
                final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyServlet.class);
                log.info("Status code 403 (FORBIDDEN): Restriction failed: {}", restriction.getDescription());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Restriction failed: " + restriction.getDescription());
                return;
            }
        }
        /*
         * Set status
         */
        resp.setStatus(httpResp.getStatusLine().getStatusCode());
        /*
         * Add response header
         */
        header2Response(httpResp, resp);
        /*
         * Binary content
         */
        final InputStream responseStream = httpResp.getEntity().getContent();
        try {
            final ServletOutputStream outputStream = resp.getOutputStream();
            final byte[] buf = new byte[8192];
            int read = -1;
            while ((read = responseStream.read(buf)) > 0) {
                outputStream.write(buf, 0, read);
            }
            outputStream.flush();
        } finally {
            Streams.close(responseStream);
        }
    }

    private static void header2Response(final HttpResponse httpResp, final HttpServletResponse resp) {
        /*
         * By now only considers Content-Type and Content-Length header
         */
        final Header ctHeader = httpResp.getFirstHeader("Content-Type");
        if (null != ctHeader) {
            /*-
             *
            final String value = ctHeader.getValue();
            if ("text/html".equals(value)) {
                resp.setContentType("text/html; charset=" + resp.getCharacterEncoding());
            } else {
                resp.setContentType(value);
            }
            */
            resp.setContentType(ctHeader.getValue());
        }

        final long length = httpResp.getEntity().getContentLength();
        if (length > 0) {
            resp.setContentLength((int) length);
        }

        /*-
         * Enable this to consider all header
         *
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
                final long len = method.getResponseContentLength();
                if (len > 0) {
                    resp.setContentLength((int) len);
                }
            }
        }
        */
    }
    
    private CloseableHttpClient getHttpClient() throws OXException {
        return Services.requireService(HttpClientService.class).getHttpClient("proxy").getCloseableHttpClient();
    }

}
