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

package com.openexchange.tools.servlet.http.manager;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletConfig;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.tools.servlet.ServletConfigLoader;
import com.openexchange.tools.servlet.http.HttpErrorServlet;
import com.openexchange.tools.servlet.http.ServletQueue;

/**
 * {@link AbstractHttpServletManager} - Abstract {@link IHttpServletManager}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractHttpServletManager implements IHttpServletManager {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AbstractHttpServletManager.class);

    protected final ConcurrentMap<String, ServletQueue> servletPool;

    protected final Map<String, Constructor<?>> servletConstructorMap;

    /**
     * Creates a new {@link AbstractHttpServletManager}.
     * <p>
     * Initializes static servlet instances and puts them into servlet pool.
     * 
     * @param servletConstructorMap The servlet constructor map from which to initialize static servlet instances
     */
    protected AbstractHttpServletManager(final Map<String, Constructor<?>> servletConstructorMap) {
        super();
        this.servletConstructorMap = servletConstructorMap;
        servletPool = new ConcurrentHashMap<String, ServletQueue>();
        createServlets();
    }

    private static final Object[] INIT_ARGS = new Object[] {};

    /**
     * Creates the static servlets identified by servlet constructor map.
     */
    private final void createServlets() {
        final ServletConfigLoader configLoader = ServletConfigLoader.getDefaultInstance();
        if (null == configLoader) {
            LOG.error("Aborting servlets' initialization: HTTP service has not been initialized since default servlet configuration loader is null.");
            return;
        }
        for (final Iterator<Map.Entry<String, Constructor<?>>> iter = servletConstructorMap.entrySet().iterator(); iter.hasNext();) {
            final Map.Entry<String, Constructor<?>> entry = iter.next();
            String path;
            try {
                path = new URI(entry.getKey().charAt(0) == '/' ? entry.getKey().substring(1) : entry.getKey()).normalize().toString();
            } catch (final URISyntaxException e) {
                LOG.error("Invalid servlet path skipped: " + entry.getKey());
                continue;
            }
            ServletQueue servletQueue = null;
            /*
             * Create a pool of servlet instances
             */
            final Constructor<?> servletConstructor = entry.getValue();
            if (servletConstructor == null) {
                servletQueue = new ServletQueue(1, null);
                servletQueue.enqueue(new HttpErrorServlet("No Servlet Constructor found for " + path));
            } else {
                try {
                    HttpServlet servletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
                    if (servletInstance instanceof SingleThreadModel) {
                        final int servletPoolSize = AJPv13Config.getServletPoolSize();
                        servletQueue = new ServletQueue(servletPoolSize, servletConstructor);
                        if (servletPoolSize > 0) {
                            final ServletConfig conf = configLoader.getConfig(servletInstance.getClass().getCanonicalName(), path);
                            servletInstance.init(conf);
                            servletQueue.enqueue(servletInstance);
                            /*
                             * Enqueue more than one instance if it implements SingleThreadModel
                             */
                            for (int i = 1; i < servletPoolSize; i++) {
                                servletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
                                servletInstance.init(conf);
                                servletQueue.enqueue(servletInstance);
                            }
                        }
                    } else {
                        servletQueue = new ServletQueue(1, servletConstructor);
                        servletInstance.init(configLoader.getConfig(servletInstance.getClass().getCanonicalName(), path));
                        servletQueue.enqueue(servletInstance);
                    }
                } catch (final Throwable t) {
                    LOG.error(t.getMessage(), t);
                }
            }
            servletPool.put(path, servletQueue);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("All Servlet Instances created & initialized");
        }
    }

    /*-
     * ############################ Static helper methods ############################
     */

    /**
     * Checks if given <code>currentPath</code> implies given <code>path</code>
     * 
     * @param currentPath The path which might imply
     * @param path The path which might be implied
     * @return <code>true</code> if given <code>currentPath</code> implies given <code>path</code>; otherwise <code>false</code>
     */
    protected static final boolean implies(final String currentPath, final String path) {
        final int len = currentPath.length();
        if (currentPath.charAt(len - 1) == '*') {
            /*
             * A wildcard path
             */
            final String _currentPath = len == 1 ? "" : currentPath.substring(0, len - 1);
            /*
             * Make sure ap.path is longer or equal length so a/b/ does imply a/b
             */
            return (path.length() >= _currentPath.length()) && path.startsWith(_currentPath);
        }
        return currentPath.equals(path);
    }

    /**
     * Gets the servlet instance bound to given path
     * 
     * @param servletQueue The servlet queue
     * @param path The servlet path
     * @return The servlet instance dequeued from pool or newly created
     */
    protected static final HttpServlet getServletInternal(final ServletQueue servletQueue, final String path) {
        if (servletQueue.isEmpty()) {
            /*
             * Empty queue: create & return a new servlet instance
             */
            final HttpServlet servletInst = servletQueue.createServletInstance(path);
            if (servletInst == null) {
                return new HttpErrorServlet(
                    new StringBuilder(64).append("Servlet ").append(path).append(" could NOT be created").toString());
            }
            return servletInst;
        }
        final HttpServlet servletInstance = servletQueue.get();
        if (servletInstance instanceof SingleThreadModel) {
            /*
             * If servlet class implements SingleThreadModel the same instance MUST NOT be used concurrently by multiple threads. So remove
             * from queue.
             */
            servletQueue.dequeue();
        }
        return servletInstance;
    }
}
