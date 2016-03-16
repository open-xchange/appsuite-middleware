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

package com.openexchange.http.testservlet;

import static com.openexchange.http.testservlet.SaneScriptTags.saneScriptTags;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.java.Streams;

/**
 * {@link TestServlet} - Default TestServlet for basic server tests.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class TestServlet extends HttpServlet {

    private static final long serialVersionUID = -4037317824217605551L;

    /**
     * Default constructor.
     */
    public TestServlet() {
        super();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // create a new HttpSession if it's missing
        req.getSession(true);
        super.service(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        // Uncomment to test long-running task
        /*-
         *
        try {
           System.out.println("Going asleep...");
           Thread.sleep(100000);
           System.out.println("... and now continues processing.");
        } catch (final InterruptedException e) {
           e.printStackTrace();
        }
         *
         */

        final StringBuilder page = new StringBuilder();
        page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        page.append("<html>\n");
        page.append("<head><title>TestServlet's doGet Page</title></head>\n");
        page.append("<body>\n");
        page.append("<h1>TestServlet's doGet Page</h1><hr/>\n");
        page.append("<p>This is a <i>tiny</i> paragraph with some text inside!</p>\n");
        page.append("</p><p><b>Headers</b><br>");
        final Enumeration<?> paramEnum = req.getHeaderNames();
        while (paramEnum.hasMoreElements()) {
            final String headerName = (String) paramEnum.nextElement();
            page.append(headerName);
            page.append(": ");
            final Enumeration<?> valueEnum = req.getHeaders(headerName);
            while (valueEnum.hasMoreElements()) {
                page.append(saneScriptTags(valueEnum.nextElement().toString()));
                page.append(valueEnum.hasMoreElements() ? ", " : "");
            }
            page.append("<br>");
        }
        page.append("</p><p>The content: ").append(saneScriptTags(this.getBody(req)));
        page.append("</p></body>\n</html>");

        /*boolean found = false;
        {
            Cookie[] cookies = req.getCookies();
            if (null != cookies) {
                for (int i = cookies.length; !found && i-- > 0;) {
                    Cookie cookie = cookies[i];
                    if ("check-me".equals(cookie.getName())) {
                        found = true;
                        System.out.println("Cookie found!");
                    }
                }
            }
        }

        if (!found) {
            Cookie cookie = new Cookie("check-me", "foobariscool");
            cookie.setPath("/");
            cookie.setMaxAge(10);
            resp.addCookie(cookie);
            System.out.println("Cookie added!");
        }*/

        resp.setContentType("text/html; charset=UTF-8");
        final byte[] output = page.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        resp.setContentLength(output.length);

        final boolean split = Boolean.parseBoolean(req.getParameter("split"));
        if (split) {
            // Split data
            final int n = output.length / 2;
            final byte[] firstChunk = new byte[n];
            final byte[] secondChunk = new byte[output.length - n];
            System.arraycopy(output, 0, firstChunk, 0, n);
            System.arraycopy(output, n, secondChunk, 0, secondChunk.length);
            resp.getOutputStream().write(firstChunk);
            try {
                Thread.sleep(40000);// 40 sec
            } catch (final InterruptedException e) {
                // Restore interrupted status
                Thread.currentThread().interrupt();
            }
            resp.getOutputStream().write(secondChunk);
        } else {
            // Write whole data at once
            resp.getOutputStream().write(output);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        final StringBuilder page = new StringBuilder();
        page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        page.append("<html>\n");
        page.append("<head><title>TestServlet's doGet Page</title></head>\n");
        page.append("<body>\n");
        page.append("<h1>TestServlet's doGet Page</h1><hr/>\n");
        page.append("<p>This is a tiny paragraph with some text inside!</p>\n");
        final Enumeration<?> paramEnum = req.getHeaderNames();
        while (paramEnum.hasMoreElements()) {
            final String headerName = (String) paramEnum.nextElement();
            page.append(headerName);
            page.append(": ");
            final Enumeration<?> valueEnum = req.getHeaders(headerName);
            while (valueEnum.hasMoreElements()) {
                page.append(saneScriptTags(valueEnum.nextElement().toString()));
                page.append(valueEnum.hasMoreElements() ? ", " : "");
            }
            page.append("<br>");
        }
        page.append("</p><p>The content: ").append(saneScriptTags(this.getBody(req)));
        page.append("</p></body>\n</html>");
        resp.setContentType("text/html; charset=UTF-8");
        final byte[] output = page.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        resp.setContentLength(output.length);
        resp.getOutputStream().write(output);
    }

    /**
     * Returns the complete body as a string. Be carefull when getting big request bodies.
     *
     * @param req http servlet request.
     * @return a string with the complete body.
     * @throws IOException if an error occurs while reading the body.
     */
    public String getBody(final HttpServletRequest req) throws IOException {
        InputStreamReader isr = null;
        try {
            int count = 0;
            final char[] c = new char[8192];
            String charset = null == req.getCharacterEncoding() ? ServerConfig.getProperty(Property.DefaultEncoding) : req.getCharacterEncoding();
            if (null == charset) {
                charset = "UTF-8";
            }
            isr = new InputStreamReader(req.getInputStream(), charset);
            if ((count = isr.read(c)) > 0) {
                final StringBuilder sb = new StringBuilder(16384);
                do {
                    sb.append(c, 0, count);
                } while ((count = isr.read(c)) > 0);
                return sb.toString();
            }
            return "";
        } catch (final UnsupportedEncodingException e) {
            /*
             * Should never occur
             */
            return "";
        } finally {
            Streams.close(isr);
        }
    }
}
