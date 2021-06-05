/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.http.testservlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        } catch (InterruptedException e) {
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
        page.append("</body>\n</html>");

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
            } catch (InterruptedException e) {
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
        page.append("<h1>TestServlet's doPut Page</h1><hr/>\n");
        page.append("<p>This is a tiny paragraph with some text inside!</p>\n");
        page.append("</body>\n</html>");
        resp.setContentType("text/html; charset=UTF-8");
        final byte[] output = page.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        resp.setContentLength(output.length);
        resp.getOutputStream().write(output);
    }

}
