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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.java.Strings;

/**
 * {@link DiagnosticServlet} - Default Servlet for JVM diagnostics.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DiagnosticServlet extends HttpServlet {

    private final List<String> defaultCipherSuites;

    /**
     * Default constructor.
     */
    public DiagnosticServlet() {
        super();

        // SSL cipher suites
        String[] defaultCipherSuites = ((javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault()).getDefaultCipherSuites();
        final List<String> l = new CopyOnWriteArrayList<String>();
        for (String cipherSuites : defaultCipherSuites) {
            l.add(cipherSuites);
        }
        this.defaultCipherSuites = l;
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // Create a new HttpSession if it's missing
        req.getSession(true);
        super.service(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String parameter = req.getParameter("param");
        if (Strings.isEmpty(parameter)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();
            final StringBuilder page = new StringBuilder(1024);
            page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
            page.append("<html>\n");
            page.append("<head><title>Diagnostic</title></head>\n");
            page.append("<body>\n");
            page.append("<h1>Diagnostic</h1><hr/>\n");
            page.append("<p>Missing \"param\" URL parameter. Possible value(s):</p>\n<ul>\n");
            page.append("<li>ciphersuites</li>\n");
            page.append("<li>version</li>\n");
            page.append("</ul>\n");
            page.append("</body>\n</html>");
            writer.write(page.toString());
            writer.flush();
            return;
        }

        if ("ciphersuites".equalsIgnoreCase(parameter)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();
            final StringBuilder page = new StringBuilder(1024);
            page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
            page.append("<html>\n");
            page.append("<head><title>Diagnostic</title></head>\n");
            page.append("<body>\n");
            page.append("<h1>Cipher Suites</h1><hr/>\n");
            page.append("<ul>\n");

            for (String suite : defaultCipherSuites) {
                page.append("<li>").append(suite).append("</li>\n");
            }

            page.append("</ul>\n");
            page.append("</body>\n</html>");
            writer.write(page.toString());
            writer.flush();
            return;
        }

        if ("version".equalsIgnoreCase(parameter)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();
            final StringBuilder page = new StringBuilder(1024);
            page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
            page.append("<html>\n");
            page.append("<head><title>Version</title></head>\n");
            page.append("<body>\n");
            page.append("<h1>Version</h1><hr/>\n");
            page.append("<p>\n");
            page.append(com.openexchange.version.Version.getInstance().getVersionString()).append("\n");
            page.append("</p>\n");
            page.append("</body>\n</html>");
            writer.write(page.toString());
            writer.flush();
            return;
        }

        // Unknown parameter value
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        final StringBuilder page = new StringBuilder(1024);
        page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        page.append("<html>\n");
        page.append("<head><title>Diagnostic</title></head>\n");
        page.append("<body>\n");
        page.append("<h1>Diagnostic</h1><hr/>\n");
        page.append("<p>Unknown parameter value</p>\n");
        page.append("</body>\n</html>");
        writer.write(page.toString());
        writer.flush();
    }

}
