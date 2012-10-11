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

package com.openexchange.ajp13.servlet.http;

import java.lang.reflect.Constructor;
import javax.servlet.http.HttpServlet;
import com.openexchange.ajp13.servlet.FIFOQueue;
import com.openexchange.ajp13.servlet.ServletConfigLoader;

/**
 * {@link FiFoServletQueue} - The servlet queue backed by a {@link FIFOQueue fi-fo queue} and capable to create new servlet instances on
 * demand.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FiFoServletQueue extends FIFOQueue<HttpServlet> implements ServletQueue {

    private static final Object[] INIT_ARGS = new Object[] {};

    private final Constructor<?> servletConstructor;

    private final boolean singleton;

    private final String servletPath;

    /**
     * Initializes a new concurrent {@link FiFoServletQueue}
     *
     * @param maxsize The max. size
     * @param servletConstructor The servlet constructor to create new servlet instances on demand
     * @param singleton <code>true</code> to mark passed servlet instance as a singleton; otherwise <code>false</code>
     * @param servletPath The servlet path
     */
    public FiFoServletQueue(final int maxsize, final Constructor<?> servletConstructor, final boolean singleton, final String servletPath) {
        this(maxsize, true, servletConstructor, singleton, servletPath);
    }

    /**
     * Initializes a new {@link FiFoServletQueue}
     *
     * @param maxsize The max. size
     * @param isSynchronized <code>true</code> for a concurrent queue; otherwise <code>false</code>
     * @param servletConstructor The servlet constructor to create new servlet instances on demand
     * @param singleton <code>true</code> to mark passed servlet instance as a singleton; otherwise <code>false</code>
     * @param servletPath The servlet path
     */
    public FiFoServletQueue(final int maxsize, final boolean isSynchronized, final Constructor<?> servletConstructor, final boolean singleton, final String servletPath) {
        super(maxsize, isSynchronized);
        this.servletConstructor = servletConstructor;
        this.singleton = singleton;
        this.servletPath = servletPath;
    }

    @Override
    public boolean isSingleton() {
        return singleton;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public HttpServlet createServletInstance(final String servletKey) {
        if (servletConstructor == null) {
            return null;
        }
        try {
            final HttpServlet servletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
            servletInstance.init(ServletConfigLoader.getDefaultInstance().getConfig(
                servletInstance.getClass().getCanonicalName(),
                servletKey));
            return servletInstance;
        } catch (final Throwable t) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FiFoServletQueue.class)).error(t.getMessage(), t);
        }
        return null;
    }

}
