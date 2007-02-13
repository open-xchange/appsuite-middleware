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
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet implements SingleThreadModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4037317824217605551L;

	public TestServlet() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		String htmlPage = "<html>\n<head><title>TestServlet's doGet Page</title></head>\n" +
				"<body>\n<h1><blink>TestServlet's doGet Page</blink></h1><hr/>\n" +
				"<p>This is a tiny paragraph with stupid text inside!</p>\n" +
				"<p>Again a tiny paragraph with stupid text inside!</p>\n" +
				"<ol><li>Erster Eintrag</li><li>Unterliste<ul><li>Foo</li><li>Bar</li></ul></li><li>Dritter Eintrag</li></ol>\n" +
				"</body>\n" +
				"</html>";
		htmlPage += "<p><b>Parameters</b><br>";
		for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
			String pn = (String)e.nextElement();
			htmlPage += pn + ": " + req.getParameter(pn) + "<br>";
		}
		htmlPage += "</p>";
		htmlPage += "<p><b>Headers</b><br>";
		for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
			String hn = (String)e.nextElement();
			htmlPage += hn + ": ";
			for (Enumeration e2 = req.getHeaders(hn); e2.hasMoreElements();) {
				htmlPage += e2.nextElement() + (e2.hasMoreElements() ? ", " : "");
			}
			htmlPage += "<br>";
		}
		htmlPage += "</p>";
		resp.setContentType("text/html");
		resp.setContentLength(htmlPage.getBytes().length);
		resp.getOutputStream().write(htmlPage.getBytes());
		resp.getOutputStream().flush();
	}

}
