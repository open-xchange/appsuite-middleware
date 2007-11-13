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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;
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

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ServletContextWrapper.class);

	public static final int OX_SERVLET_MAJOR = 0;

	public static final int OX_SERVLET_MINOR = 8;

	public static final int OX_SERVLET_PATCH = 2;

	/**
	 * Return a context-relative path, beginning with a "/", that represents the
	 * canonical version of the specified path after ".." and "." elements are
	 * resolved out. If the specified path attempts to go outside the boundaries
	 * of the current context (i.e. too many ".." path elements are present),
	 * return <code>null</code> instead.
	 * 
	 * @param path
	 *            Path to be normalized
	 */
	private static String normalize(final String path) {
		if (path == null) {
			return null;
		}
		String normalized = path;
		/*
		 * Normalize the slashes and add leading slash if necessary
		 */
		if (normalized.indexOf('\\') >= 0) {
			normalized = normalized.replace('\\', '/');
		}
		/*
		 * Resolve occurrences of "/../" in the normalized path
		 */
		while (true) {
			final int index = normalized.indexOf("/../");
			if (index < 0) {
				break;
			}
			if (index == 0) {
				/*
				 * Trying to go outside our context
				 */
				return (null);
			}
			final int index2 = normalized.lastIndexOf('/', index - 1);
			normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
		}
		/*
		 * Return the normalized path
		 */
		return (normalized);

	}

	/**
	 * The context attributes for this context.
	 */
	protected Map<String, Object> attributes = new HashMap<String, Object>();

	private String resourceDir;

	private final ServletConfigWrapper servletconfigwrapper;

	public ServletContextWrapper(final ServletConfigWrapper servletconfigwrapper) {
		this.servletconfigwrapper = servletconfigwrapper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
	 */
	public Object getAttribute(final String name) {
		return attributes.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 */
	public Enumeration<?> getAttributeNames() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getContext(java.lang.String)
	 */
	public ServletContext getContext(final String uri) {
		if ((uri == null) || (uri.charAt(0) != '/')) {
			return null;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	public String getInitParameter(final String name) {
		return servletconfigwrapper.getInitParameter(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 */
	public Enumeration<String> getInitParameterNames() {
		return servletconfigwrapper.getInitParameterNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getMajorVersion()
	 */
	public int getMajorVersion() {
		return OX_SERVLET_MAJOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
	 */
	public String getMimeType(final String fileName) {
		return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getMinorVersion()
	 */
	public int getMinorVersion() {
		return OX_SERVLET_MINOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
	 */
	public RequestDispatcher getNamedDispatcher(final String string) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
	 */
	public String getRealPath(final String string) {
		return string;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(final String string) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getResource(java.lang.String)
	 */
	public URL getResource(final String string) throws MalformedURLException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(final String path) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
	 */
	public Set<?> getResourcePaths(final String string) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServerInfo()
	 */
	public String getServerInfo() {
		return "THE SERVER INFO";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServlet(java.lang.String)
	 */
	public Servlet getServlet(final String string) throws ServletException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServletContextName()
	 */
	public String getServletContextName() {
		return servletconfigwrapper.getServletName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServletNames()
	 */
	public Enumeration<?> getServletNames() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#getServlets()
	 */
	public Enumeration<?> getServlets() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#log(java.lang.Exception,
	 *      java.lang.String)
	 */
	public void log(final Exception exception, final String string) {
		if (LOG.isInfoEnabled()) {
			LOG.info(string, exception);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#log(java.lang.String)
	 */
	public void log(final String string) {
		if (LOG.isInfoEnabled()) {
			LOG.info(string);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#log(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void log(final String string, final Throwable throwable) {
		if (LOG.isInfoEnabled()) {
			LOG.info(string, throwable);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(final String name) {
		attributes.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	public void setAttribute(final String name, final Object value) {
		attributes.put(name, value);
	}

}
