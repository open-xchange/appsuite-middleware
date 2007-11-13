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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * OXServletConfig
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class ServletConfigWrapper implements ServletConfig {

	protected String servletName;

	protected Map<String, String> initParameter = new HashMap<String, String>();

	ServletContextWrapper servletcontextwrapper;

	public ServletConfigWrapper() {
		servletcontextwrapper = new ServletContextWrapper(this);
	}

	public void setServletName(final String servlet_name) {
		this.servletName = servlet_name;
	}

	public void setInitParameter(final Map<String, String> init_parameter) {
		this.initParameter = init_parameter;
	}

	public ServletContextWrapper getServletcontextwrapper() {
		return servletcontextwrapper;
	}

	public void setServletContextWrapper(final ServletContextWrapper servletcontextwrapper) {
		this.servletcontextwrapper = servletcontextwrapper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
	 */
	public String getInitParameter(final String name) {
		return initParameter.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletConfig#getServletName()
	 */
	public String getServletName() {
		return servletName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletConfig#getServletContext()
	 */
	public ServletContext getServletContext() {
		return servletcontextwrapper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 */
	public Enumeration<String> getInitParameterNames() {
		return new IteratorEnumeration<String>(initParameter.keySet().iterator());
	}

	/**
	 * IteratorEnumeration
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 */
	private static class IteratorEnumeration<E> implements Enumeration<E> {

		private final Iterator<E> iter;

		public IteratorEnumeration(final Iterator<E> iter) {
			this.iter = iter;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Enumeration#hasMoreElements()
		 */
		public boolean hasMoreElements() {
			return iter.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Enumeration#nextElement()
		 */
		public E nextElement() {
			return iter.next();
		}

	}
}
