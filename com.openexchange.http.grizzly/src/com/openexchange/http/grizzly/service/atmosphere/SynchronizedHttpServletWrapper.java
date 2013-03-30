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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.service.atmosphere;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

/**
 * {@link SynchronizedHttpServletWrapper}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class SynchronizedHttpServletWrapper extends HttpServlet {

    private final HttpServlet delegate;

    /**
     * Initializes a new {@link SynchronizedHttpServletWrapper}.
     * 
     * @param delegate
     */
    public SynchronizedHttpServletWrapper(HttpServlet delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
    public void destroy() {
        delegate.destroy();
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @param name
     * @return
     * @see javax.servlet.GenericServlet#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String name) {
        return delegate.getInitParameter(name);
    }

    /**
     * @return
     * @see javax.servlet.GenericServlet#getInitParameterNames()
     */
    public Enumeration getInitParameterNames() {
        return delegate.getInitParameterNames();
    }

    /**
     * @return
     * @see javax.servlet.GenericServlet#getServletConfig()
     */
    public ServletConfig getServletConfig() {
        return delegate.getServletConfig();
    }

    /**
     * @return
     * @see javax.servlet.GenericServlet#getServletContext()
     */
    public ServletContext getServletContext() {
        return delegate.getServletContext();
    }

    /**
     * @return
     * @see javax.servlet.GenericServlet#getServletInfo()
     */
    public String getServletInfo() {
        return delegate.getServletInfo();
    }

    /**
     * @return
     * @see javax.servlet.GenericServlet#getServletName()
     */
    public String getServletName() {
        return delegate.getServletName();
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @throws ServletException
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        delegate.init();
    }

    /**
     * @param config
     * @throws ServletException
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
        delegate.init(config);
    }

    /**
     * @param message
     * @param t
     * @see javax.servlet.GenericServlet#log(java.lang.String, java.lang.Throwable)
     */
    public void log(String message, Throwable t) {
        delegate.log(message, t);
    }

    /**
     * @param msg
     * @see javax.servlet.GenericServlet#log(java.lang.String)
     */
    public void log(String msg) {
        delegate.log(msg);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws ServletException
     * @throws IOException
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public synchronized void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
        delegate.service(arg0, arg1);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }
    

}
