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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.diagnostics.DiagnosticService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DiagnosticServlet} - Default Servlet for JVM diagnostics.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DiagnosticServlet extends HttpServlet {

    private enum ServletParameter {
        charsets("Character Sets"),
        ciphersuites("Cipher Suites"),
        protocols("SSL Protocols"),
        version("Version");

        private final String description;
        private static final List<String> parameters;
        static {
            List<String> p = new ArrayList<>(ServletParameter.values().length);
            for (ServletParameter servletParameter : ServletParameter.values()) {
                p.add(servletParameter.name());
            }
            parameters = Collections.unmodifiableList(p);
        }

        /**
         * Initialises a new {@link DiagnosticServlet.ServletParameter}.
         */
        private ServletParameter(String description) {
            this.description = description;
        }

        /**
         * Gets the description
         *
         * @return The description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns an unmodifiable {@link List} of all available {@link ServletParameter}s
         * 
         * @return an unmodifiable {@link List} of all available {@link ServletParameter}s
         */
        public static List<String> getParameters() {
            return parameters;
        }
    }

    private static final long serialVersionUID = 9099870094224041974L;
    private ServiceLookup services;

    /**
     * Default constructor.
     */
    public DiagnosticServlet(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String parameter = req.getParameter("param");
        final StringBuilder page = new StringBuilder(1024);
        if (Strings.isEmpty(parameter)) {
            writeStatusAndContentType(resp, HttpServletResponse.SC_BAD_REQUEST);
            writeHeader(resp, page, "Error");
            page.append("<p>Missing \"param\" URL parameter. Possible value(s):</p>\n");
            writeList(ServletParameter.getParameters(), page);
            writeFooter(page);
            flush(resp, page);
            return;
        }

        ServletParameter servletParameter;
        try {
            servletParameter = ServletParameter.valueOf(parameter);
        } catch (Exception e) {
            // Unknown parameter value
            writeStatusAndContentType(resp, HttpServletResponse.SC_BAD_REQUEST);
            writeHeader(resp, page, "Error");
            page.append("<p>Unknown parameter value: ");
            page.append(parameter);
            page.append("</p>\n");
            writeFooter(page);
            flush(resp, page);
            return;
        }

        writeHeader(resp, page, servletParameter.getDescription());
        writeStatusAndContentType(resp, HttpServletResponse.SC_OK);
        DiagnosticService diagnosticService = services.getService(DiagnosticService.class);
        switch (servletParameter) {
            case charsets:
                writeList(diagnosticService.getCharsets(true), page);
                break;
            case ciphersuites:
                writeList(diagnosticService.getCipherSuites(), page);
                break;
            case protocols:
                writeList(diagnosticService.getProtocols(), page);
                break;
            case version:
                page.append(diagnosticService.getVersion()).append("\n");
                break;
            default:
                // Unknown parameter value
                writeStatusAndContentType(resp, HttpServletResponse.SC_BAD_REQUEST);
                writeHeader(resp, page, "Error");
                page.append("<p>Unknown parameter value: ");
                page.append(servletParameter.name());
                page.append("</p>\n");
                break;
        }
        writeFooter(page);
        flush(resp, page);
    }

    /**
     * Writes the specified {@link List} to the specified page as <code>&lt;ul&gt;&lt;li&gt;...&lt;/li&gt;&lt;/ul&gt</code> list
     * 
     * @param list The {@link List} to write
     * @param page The {@link StringBuilder} holding the page content
     */
    private void writeList(List<String> list, StringBuilder page) {
        page.append("<ul>\n");
        for (String element : list) {
            page.append("<li>").append(element).append("</li>");
        }
        page.append("</ul>\n");
    }

    /**
     * Writes the header of the page
     * 
     * @param resp The {@link HttpServletResponse}
     * @param page The {@link StringBuilder} holding the page content
     * @param servletParameter The {@link ServletParameter}
     */
    private void writeHeader(HttpServletResponse resp, StringBuilder page, String header) {
        page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        page.append("<html>\n");
        page.append("<head><title>Diagnostic</title></head>\n");
        page.append("<body>\n");
        page.append("<h1>");
        page.append(header);
        page.append("</h1><hr/>\n");
    }

    /**
     * Writes the footer of the page
     * 
     * @param page The {@link StringBuilder} holding the page content
     * 
     */
    private void writeFooter(StringBuilder page) {
        page.append("</body>\n</html>");
    }

    /**
     * Flushes the content to the stream
     * 
     * @param resp The {@link HttpServletResponse}
     * @param page The {@link StringBuilder} holding the page content
     * @throws IOException if an I/O error occurs while flushing the page
     */
    private void flush(HttpServletResponse resp, StringBuilder page) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.write(page.toString());
        writer.flush();
    }

    /**
     * Writes the status and content of a bad request
     * 
     * @param resp The {@link HttpServletResponse}
     * @param int the status code to return
     */
    private void writeStatusAndContentType(HttpServletResponse resp, int statusCode) {
        resp.setStatus(statusCode);
        resp.setContentType("text/html");
    }
}
