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



package com.openexchange.tools.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * OXServletContext
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class ServletContextWrapper implements ServletContext {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ServletContextWrapper.class);

	public static final int OX_SERVLET_MAJOR = 0;

	public static final int OX_SERVLET_MINOR = 8;

	public static final int OX_SERVLET_PATCH = 2;

	protected Hashtable<String, Object> attributes = new Hashtable<String, Object>();

	private ServletConfigWrapper servletconfigwrapper;

	public ServletContextWrapper(ServletConfigWrapper servletconfigwrapper) {
		this.servletconfigwrapper = servletconfigwrapper;
	}

	public void setAttribute(final String name, final Object value) {
		attributes.put(name, value);
	}

	public void removeAttribute(final String name) {
		attributes.remove(name);
	}

	public void log(final String string) {
		LOG.info(string);
	}

	public String getMimeType(final String string) {
		return null;
	}

	public InputStream getResourceAsStream(final String string) {
		return null;
	}

	public ServletContext getContext(final String string) {
		return null;
	}

	public Object getAttribute(final String name) {
		if (attributes.containsKey(name)) {
			return attributes.get(name);
		} else {
			return null;
		}
	}

	public Set getResourcePaths(final String string) {
		return null;
	}

	public String getInitParameter(final String name) {
		return servletconfigwrapper.getInitParameter(name);
	}

	public String getRealPath(final String string) {
		return string;
	}

	public Servlet getServlet(final String string) throws ServletException {
		return null;
	}

	public URL getResource(final String string) throws MalformedURLException {
		return null;
	}

	public RequestDispatcher getRequestDispatcher(final String string) {
		return null;
	}

	public RequestDispatcher getNamedDispatcher(final String string) {
		return null;
	}

	public void log(final Exception exception, final String string) {
        LOG.info(string, exception);
	}

	public void log(final String string, final Throwable throwable) {
        LOG.info(string, throwable);
	}

	public int getMajorVersion() {
		return OX_SERVLET_MAJOR;
	}

	public Enumeration getServlets() {
		return null;
	}

	public Enumeration getServletNames() {
		return null;
	}

	public Enumeration getAttributeNames() {
		return null;
	}

	public int getMinorVersion() {
		return OX_SERVLET_MINOR;
	}

	public String getServerInfo() {
		return "THE SERVER INFO";
	}

	public Enumeration getInitParameterNames() {
		return servletconfigwrapper.getInitParameterNames();
	}

	public String getServletContextName() {
		return servletconfigwrapper.getServletName();
	}

}
