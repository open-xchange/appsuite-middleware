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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link ReverseProxy}
 *
 * @author <a href="mailto:matthias.biggeleben@open-xchange.com">Matthias Biggeleben</a>
 */
public class ReverseProxy extends HttpServlet {

    private static final long serialVersionUID = -4173073773095806657L;

    private static final Log LOG = LogFactory.getLog(ReverseProxy.class);

	@Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    String methodS = req.getMethod();
	    String host = getHost(req);
	    String path = getPath(req);
	    LOG.info(host + ':' + path);
        HttpClient httpClient = new HttpClient();
        cookies2Request(req, httpClient.getState(), host);
        HttpMethodBase method = determineMethod(methodS);
        headers2Request(req, method);
        HostConfiguration hostConfiguration = httpClient.getHostConfiguration();
        hostConfiguration.setHost(new URI("http://" + host + path, false));
        httpClient.executeMethod(method);
        cookies2Response(method, resp);
        header2Response(method, resp);
        InputStream responseStream = method.getResponseBodyAsStream();
        OutputStream outputStream = resp.getOutputStream();
        byte[] buf = new byte[1024];
        int length = -1;
        while ((length = responseStream.read(buf)) != -1) {
            outputStream.write(buf, 0, length);
        }
	}

    private void cookies2Request(HttpServletRequest req, HttpState state, String host) {
        for (javax.servlet.http.Cookie cookie : req.getCookies()) {
            state.addCookie(new Cookie(host, cookie.getName(), cookie.getValue()));
        }
    }

    private void headers2Request(HttpServletRequest req, HttpMethodBase method) {
        Enumeration<?> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            if ("bla".equals(headerName)) {
                method.addRequestHeader(new Header("blub", "blubber"));
            } else
            if ("Referer".equals(headerName)) {
                method.addRequestHeader(new Header("Referer", req.getHeader(headerName)));
            }
        }
    }

    private void cookies2Response(HttpMethodBase method, HttpServletResponse resp) {
        for (Header header : method.getResponseHeaders("Set-Cookie")) {
            resp.setHeader(header.getName(), header.getValue());
        }
    }

    private void header2Response(HttpMethodBase method, HttpServletResponse resp) {
        for (Header header : method.getResponseHeaders()) {
//            LOG.info("Response header: " + header.getName() + ':' + header.getValue());
            if ("Content-Type".equals(header.getName())) {
                resp.setContentType(header.getValue());
            }
        }
        long length = method.getResponseContentLength();
        if (length > 0) {
            resp.setContentLength((int) length);
        }
    }

    private HttpMethodBase determineMethod(String methodS) {
	    if ("GET".equals(methodS)) {
	        return new GetMethod();
	    }
	    return null;
    }

    private String getHost(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        int end = pathInfo.indexOf('/', 1);
        return end > 0 ? pathInfo.substring(1, end) : pathInfo.substring(1, pathInfo.length());
    }

    private String getPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        int start = pathInfo.indexOf('/', 1);
        return start > 0 ? pathInfo.substring(start) : "";
    }
}
