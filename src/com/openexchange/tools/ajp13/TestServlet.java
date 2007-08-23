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

package com.openexchange.tools.ajp13;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {

	/**
	 * For serialization.
	 */
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
	protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
        final StringBuilder page = new StringBuilder();
        page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n");
        page.append("<html>\n");
        page.append("<head><title>TestServlet's doGet Page</title></head>\n");
        page.append("<body>\n");
        page.append("<h1><blink>TestServlet's doGet Page</blink></h1><hr/>\n");
        page.append("<p>This is a tiny paragraph with some text inside!</p>\n");
        page.append("<p>Again a tiny paragraph with some text inside!</p>\n");
        page.append("<ol><li>First list entry</li>");
        page.append("<li>Sublist<ul><li>Foo</li><li>Bar</li></ul></li>");
        page.append("<li>Third list entry</li></ol>\n");
        page.append("<p><b>Parameters</b><br>");
        Enumeration paramEnum = req.getParameterNames();
		while (paramEnum.hasMoreElements()) {
			final String parameterName = (String) paramEnum.nextElement();
            page.append(parameterName);
            page.append(": ");
            page.append(req.getParameter(parameterName));
            page.append("<br>");
		}
        page.append("</p><p><b>Headers</b><br>");
        paramEnum = req.getHeaderNames();
		while (paramEnum.hasMoreElements()) {
			final String headerName = (String) paramEnum.nextElement();
            page.append(headerName);
            page.append(": ");
            final Enumeration valueEnum = req.getHeaders(headerName);
			while (valueEnum.hasMoreElements()) {
                page.append(valueEnum.nextElement());
                page.append(valueEnum.hasMoreElements() ? ", " : "");
			}
            page.append("<br>");
		}
        page.append("</p></body>\n</html>");
		resp.setContentType("text/html; charset=UTF-8");
        final byte[] output = page.toString().getBytes("UTF-8");
		resp.setContentLength(output.length);
		resp.getOutputStream().write(output);
	}
}
