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

package com.openexchange.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link ReverseProxy}
 * 
 * @author <a href="mailto:matthias.biggeleben@open-xchange.com">Matthias Biggeleben</a>
 * @author <a href="mailto:markus.klein@open-xchange.com">Markus Klein</a>
 */
public class ReverseProxy extends HttpServlet {

    private static final long serialVersionUID = -4173073773095806657L;

    private static final Log LOG = LogFactory.getLog(ReverseProxy.class);

    private Hashtable<String,ReverseProxyConfig> proxies = new Hashtable<String, ReverseProxyConfig>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // add dummy config
        proxies.put("devtank.de", new ReverseProxyConfig("devtank.de", "http", "shuffle.devtank.de", ""));
        proxies.put("login.1und1.de", new ReverseProxyConfig("login.1und1.de", "https", "login.1und1.de", ""));
        // proxies.put("login.1und1.de", new ReverseProxyConfig("login.1und1.de", "https", "shuffle.devtank.de", ""));
        proxies.put("mein.1und1.de", new ReverseProxyConfig("mein.1und1.de", "https", "mein.1und1.de", ""));

        // get proxy id
        String proxyId = getProxyId(req);
 
        // valid proxy?
        if (proxies.containsKey(proxyId)) {
           
            // get proxy config
            ReverseProxyConfig proxy = proxies.get(proxyId);
            
            String prefix = "/proxy/" + proxyId;
            String path = req.getRequestURI().substring(prefix.length());
            String methodS = req.getMethod();
            String query = req.getQueryString();
            
            //LOG.info("path=" + path + " query=" + query);
            
            // http client to process subrequest
            HttpClient httpClient = new HttpClient();
            HttpMethodBase method = determineMethod(methodS);
            
            // === REQUEST ===
            
            // add request cookies
            prefix = ("proxy-" + proxyId + "-").replaceAll("[^\\w\\-]", "-");
            HttpState state = httpClient.getState();
            
            javax.servlet.http.Cookie[] kekse = req.getCookies(); // might return null instead of empty array
            if (kekse != null) {
                for (javax.servlet.http.Cookie cookie : kekse) {
                    //LOG.info("Keks: " + cookie.getName() + " " + cookie.getValue() + " Compare with " + prefix);
                    String cookieName = cookie.getName();
                    if (cookieName.startsWith(prefix)) {
                        cookieName = cookieName.substring(prefix.length());
                        //LOG.info("Set-Cookie " + cookieName + "=" + cookie.getValue() + "; Host=" + proxy.host);
                        Cookie keks = new Cookie(proxy.host, cookieName, cookie.getValue());
                        keks.setPath("/");
                        httpClient.getState().addCookie(keks);
                    }
                }
            }

            // set URI
            method.setURI(proxy.getURI(path, query));

            // add headers
            Enumeration<?> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                // get header name (lower case!)
                String name = (String) headerNames.nextElement();
                String value = req.getHeader(name);
                if ("referer".equals(name)) {
                    // bypass "Referer"
                    String refPrefix = "https://www.disco2000.ox/proxy/" + proxy.id;;
                    String remainder = value.substring(refPrefix.length());
                    value = proxy.getPrefix() + remainder;
                    method.addRequestHeader(new Header("Referer",  value));
                } else if ("user-agent".equals(name)) {
                    // bypass "User-Agent"
                    method.addRequestHeader(new Header("User-Agent", req.getHeader(name)));
                } else if ("content-type".equals(name)) {
                    // bypass "Content-Type"
                    method.addRequestHeader(new Header("Content-Type", req.getHeader(name)));
                }
            }
            
            // POST?
            if (methodS.equals("POST")) {
                PostMethod post = (PostMethod) method;
                // add body (contains url-encoded parameter)
                post.setRequestBody(req.getInputStream());
            }

            // do not follow redirects
            method.setFollowRedirects(false);
            
            // send request
            httpClient.executeMethod(method);

            // === RESPONSE ====
            
            // process response cookies
            state = httpClient.getState();
            for (Cookie cookie : state.getCookies()) {
                //LOG.info("Response KEKS: " + cookie.getName() + " " + cookie.getValue());
                // add to response
                String cookieName = prefix + cookie.getName();
                javax.servlet.http.Cookie keks = new javax.servlet.http.Cookie(cookieName, cookie.getValue());
                keks.setDomain("www.disco2000.ox");
                keks.setPath("/proxy/" + proxyId);
                resp.addCookie(keks);
            }
            
            // add response header
            header2Response(method, resp);
            
            // process response body (html only)
            Header ct = method.getResponseHeader("Content-Type");
            if (ct.getValue().startsWith("text/html")) {
                String body = method.getResponseBodyAsString();
                // replace content inside body
                body = this.processBody(body, proxy);
                // write as response
                PrintWriter out = resp.getWriter();
                out.write(body);
            } else {
                // binary 
                InputStream responseStream = method.getResponseBodyAsStream();
                OutputStream outputStream = resp.getOutputStream();
                byte[] buf = new byte[1024];
                int length = -1;
                while ((length = responseStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, length);
                }
            }
            // close connection
            method.releaseConnection();
            
            // set status
            resp.setStatus(method.getStatusCode());

        } else {

            // invalid proxy
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Unkown proxy");
        }
    }

    private void header2Response(HttpMethodBase method, HttpServletResponse resp) {
        
        for (Header header : method.getResponseHeaders()) {
            
            String name = header.getName();
            String value = header.getValue();
            
            if ("Content-Type".equals(name)) {
                // set content type
                resp.setContentType(value);
                
            } else if ("Location".equals(name)) {
                // handle redirects
                Enumeration<?> proxies = this.proxies.elements();
                while (proxies.hasMoreElements()) {
                    ReverseProxyConfig proxy = (ReverseProxyConfig) proxies.nextElement();
                    String prefix = proxy.getPrefix();
                    LOG.info("Test: " + value + " Against: " + prefix);
                    // matches header value?
                    if (value.startsWith("/")) {
                        // relative URL
                        value = "https://www.disco2000.ox/proxy/" + proxy.id + value;
                        resp.addHeader(name, value);
                        break;
                    }
                    else if (value.startsWith(prefix)) { // add trailing slash to prevent id collisions
                        // absolute URL
                        String path = value.substring(prefix.length());
                        // remove port
                        path = path.replaceFirst("\\:\\d+", "");
                        value = "https://www.disco2000.ox/proxy/" + proxy.id + path;
                        LOG.info("FOUND! Redirect: " + value);
                        resp.addHeader(name, value);
                        break;
                    }
                    LOG.info("Not matching!");
                }
                
            } else if ("Content-Length".equals(name)) {
                // set content length
                long length = method.getResponseContentLength();
                if (length > 0) {
                    resp.setContentLength((int) length);
                }
            }
            
        }
    }

    private HttpMethodBase determineMethod(String methodS) {
        if ("GET".equals(methodS)) {
            return new GetMethod();
        } else if ("POST".equals(methodS)) {
            return new PostMethod();
        }
        return null;
    }

    private String getProxyId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        int end = pathInfo.indexOf('/', 1);
        return end > 0 ? pathInfo.substring(1, end) : pathInfo.substring(1);
    }
    
    private String processBody(String body, ReverseProxyConfig currentProxy) {
        // loop through proxies
        Enumeration<?> proxies = this.proxies.elements();
        while (proxies.hasMoreElements()) {
            ReverseProxyConfig proxy = (ReverseProxyConfig) proxies.nextElement();
            String prefix = proxy.getPrefix();
            // replace
            String regex = java.util.regex.Pattern.quote(prefix);
            body = body.replaceAll(regex, "https://www.disco2000.ox/proxy/" + proxy.id);
        }
        // make absolute pathes
        body = body.replaceAll("\"/(.*?)\"", "\"/proxy/" + currentProxy.id + "/$1\""); //=\\\"\\/(.*?)\\\"", "https://www.disco2000.ox/proxy/" + currentProxy.id + "/$1");
        //body = body.replaceAll("href=\\\"\\/(.*?)\\\"", "https://www.disco2000.ox/proxy/" + currentProxy.id + "/$1");
        // done
        return body;
    }
}
