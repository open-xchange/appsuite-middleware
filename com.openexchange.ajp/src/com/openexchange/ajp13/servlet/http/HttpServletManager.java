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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.servlet.http;

import java.lang.reflect.Constructor;
import java.util.Dictionary;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import com.openexchange.ajp13.servlet.http.manager.ConcurrentHttpServletManager;
import com.openexchange.ajp13.servlet.http.manager.DummyHttpServletManager;
import com.openexchange.ajp13.servlet.http.manager.IHttpServletManager;
import com.openexchange.ajp13.servlet.http.manager.NonBlockingHttpServletManager;

/**
 * {@link HttpServletManager} - The HTTP servlet manager
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpServletManager {

    private static volatile IHttpServletManager instance = DummyHttpServletManager.INSTANCE;

    /**
     * Initializes a new {@link HttpServletManager}
     */
    private HttpServletManager() {
        super();
    }

    /**
     * Determines the instance of {@link HttpServlet} that corresponds to given path; e.g. <code>/servlet/path</code>.
     *
     * @param path The servlet path to lookup
     * @param pathStorage A container to keep the actual servlet path contained in servlet mapping for later servlet release
     * @return The instance of {@link HttpServlet} or <code>null</code> if no instance is bound to specified path
     */
    public static HttpServlet getServlet(final String path, final StringBuilder pathStorage) {
        return instance.getServlet(path, pathStorage);
    }

    /**
     * Puts a servlet bound to given ID into this servlet manager's pool.
     *
     * @param path The servlet's path
     * @param servletObj The servlet instance
     */
    public static final void putServlet(final String path, final HttpServlet servletObj) {
        instance.putServlet(path, servletObj);
    }

    /**
     * Registers a servlet if not already contained.
     *
     * @param id The servlet's ID or alias (e.g. <code>/my/servlet</code>)
     * @param servlet The servlet instance
     * @param initParams The servlet's init parameters
     * @throws ServletException If servlet's initialization fails or another servlet has already been registered with the same alias
     */
    public static final void registerServlet(final String id, final HttpServlet servlet, final Dictionary<String, String> initParams) throws ServletException {
        instance.registerServlet(id, servlet, initParams);
    }

    /**
     * Unregisters the servlet bound to given ID from mapping.
     *
     * @param id The servlet ID or alias
     */
    public static final void unregisterServlet(final String id) {
        instance.unregisterServlet(id);
    }

    /**
     * Destroys the servlet that is bound to given ID.
     *
     * @param id The servlet ID
     * @param servletObj The servlet instance
     */
    public static final void destroyServlet(final String id, final HttpServlet servletObj) {
        instance.destroyServlet(id, servletObj);
    }

    /**
     * Initializes HTTP servlet manager with specified initial servlet constructor map.
     *
     * @param servletConstructorMap The servlet constructor map
     * @param nonBlocking <code>true</code> to use a non-blocking {@link IHttpServletManager servlet manager}; otherwise <code>false</code>
     *            to use a concurrent {@link IHttpServletManager servlet manager}
     */
    final static void initHttpServletManager(final Map<String, Constructor<?>> servletConstructorMap, final boolean nonBlocking) {
        synchronized (HttpServletManager.class) {
            if (DummyHttpServletManager.INSTANCE.equals(instance)) {
                if (nonBlocking) {
                    instance = new NonBlockingHttpServletManager(servletConstructorMap);
                } else {
                    instance = new ConcurrentHttpServletManager(servletConstructorMap);
                }
            }
        }
    }

    /**
     * Shuts down the HTTP servlet manager.
     */
    final static void shutdownHttpServletManager() {
        synchronized (HttpServletManager.class) {
            if (!DummyHttpServletManager.INSTANCE.equals(instance)) {
                instance = DummyHttpServletManager.INSTANCE;
            }
        }
    }

}
