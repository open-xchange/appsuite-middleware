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

package com.openexchange.tools.servlet.http;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ConcurrentModificationException;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.tools.NonBlockingRWLock;
import com.openexchange.tools.servlet.ServletConfigLoader;

/**
 * {@link HttpServletManager} - The HTTP servlet manager
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpServletManager {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(HttpServletManager.class);

    private static final AtomicBoolean initialized = new AtomicBoolean();

    private static final ConcurrentMap<String, ServletQueue> SERVLET_POOL = new ConcurrentHashMap<String, ServletQueue>();

    private static Map<String, Constructor<?>> servletConstructorMap;

    private static final NonBlockingRWLock RW_LOCK = new NonBlockingRWLock(true);

    /**
     * Initializes a new {@link HttpServletManager}
     */
    private HttpServletManager() {
        super();
    }

    /**
     * Determines the instance of {@link HttpServlet} that corresponds to given path; e.g. <code>/servlet/path</code>
     * 
     * @param path The servlet path to lookup
     * @param pathStorage A container to keep the actual servlet path contained in servlet mapping for later servlet release
     * @return The instance of {@link HttpServlet} or <code>null</code> if no instance is bound to specified path
     */
    public static HttpServlet getServlet(final String path, final StringBuilder pathStorage) {
        int state;
        HttpServlet retval;
        String path2Append;
        do {
            state = RW_LOCK.acquireRead();
            final ServletQueue servletQueue = SERVLET_POOL.get(path);
            if (servletQueue != null) {
                path2Append = path;
                retval = getServletInternal(servletQueue, path);
            } else {
                retval = null;
                path2Append = null;
                /*
                 * Try through resolving
                 */
                try {
                    final int size = SERVLET_POOL.size();
                    final Iterator<Map.Entry<String, ServletQueue>> iter = SERVLET_POOL.entrySet().iterator();
                    boolean b = true;
                    for (int i = 0; i < size && b; i++) {
                        final Map.Entry<String, ServletQueue> e = iter.next();
                        final String currentPath = e.getKey();
                        if (implies(currentPath, path)) {
                            path2Append = currentPath;
                            retval = getServletInternal(e.getValue(), currentPath);
                            b = false;
                        }
                    }
                } catch (final ConcurrentModificationException e) {
                    LOG.warn("Resolving servlet path failed. Trying again...", e);
                } catch (final NoSuchElementException e) {
                    LOG.warn("Resolving servlet path failed. Trying again...", e);
                }
            }
        } while (!RW_LOCK.releaseRead(state));
        if (path2Append != null) {
            pathStorage.append(path2Append);
        }
        return retval;
    }

    /**
     * Checks if given <code>currentPath</code> implies given <code>path</code>
     * 
     * @param currentPath The path which might imply
     * @param path The path which might be implied
     * @return <code>true</code> if given <code>currentPath</code> implies given <code>path</code>; otherwise <code>false</code>
     */
    private static final boolean implies(final String currentPath, final String path) {
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
    private static final HttpServlet getServletInternal(final ServletQueue servletQueue, final String path) {
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

    private final static Class<?>[] CLASS_ARR = new Class[] {};

    /**
     * Puts a servlet bound to given ID into this servlet manager's pool
     * 
     * @param path The servlet's path
     * @param servletObj The servlet instance
     */
    public static final void putServlet(final String path, final HttpServlet servletObj) {
        if (SERVLET_POOL.containsKey(path) && !(servletObj instanceof SingleThreadModel)) {
            return;
        }
        RW_LOCK.acquireWrite();
        try {
            if (SERVLET_POOL.containsKey(path)) {
                /*
                 * Since heading condition failed the servlet must be an instance of SingleThreadModel
                 */
                SERVLET_POOL.get(path).enqueue(servletObj);
            } else {
                final ServletQueue servlets;
                try {
                    servlets = new ServletQueue(1, servletObj.getClass().getConstructor(CLASS_ARR));
                } catch (final SecurityException e) {
                    LOG.error("Default constructor could not be found for servlet class: " + servletObj.getClass().getName(), e);
                    return;
                } catch (final NoSuchMethodException e) {
                    LOG.error("Default constructor could not be found for servlet class: " + servletObj.getClass().getName(), e);
                    return;
                }
                final ServletConfig conf = ServletConfigLoader.getDefaultInstance().getConfig(
                    servletObj.getClass().getCanonicalName(),
                    path);
                try {
                    servletObj.init(conf);
                } catch (final ServletException e) {
                    LOG.error("Servlet could not be put into pool", e);
                    return;
                }
                servlets.enqueue(servletObj);
                SERVLET_POOL.put(path, servlets);
            }
        } finally {
            RW_LOCK.releaseWrite();
        }
    }

    /**
     * Registers a servlet if not already contained
     * 
     * @param id The servlet's ID or alias (e.g. <code>my/servlet</code>). Servlet's path without leading '/' character
     * @param servlet The servlet instance
     * @param initParams The servlet's init parameters
     * @throws ServletException If servlet's initialization fails or another servlet has already been registered with the same alias
     */
    public static final void registerServlet(final String id, final HttpServlet servlet, final Dictionary<String, String> initParams) throws ServletException {
        RW_LOCK.acquireWrite();
        try {
            final String path = new URI(id.charAt(0) == '/' ? id.substring(1) : id).normalize().toString();
            if (SERVLET_POOL.containsKey(path)) {
                throw new ServletException(new StringBuilder(256).append("A servlet with alias \"").append(id).append(
                    "\" has already been registered before.").toString());
            }
            final ServletConfigLoader configLoader = ServletConfigLoader.getDefaultInstance();
            if (null == configLoader) {
                throw new ServletException(
                    "Aborting servlet registration: HTTP service has not been initialized since default servlet configuration loader is null.");
            }
            if ((null != initParams) && !initParams.isEmpty()) {
                configLoader.setConfig(servlet.getClass().getCanonicalName(), initParams);
            }
            /*
             * Try to determine default constructor for later instantiations
             */
            final ServletQueue servletQueue;
            try {
                servletQueue = new ServletQueue(1, servlet.getClass().getConstructor(CLASS_ARR));
            } catch (final SecurityException e) {
                final ServletException se = new ServletException(
                    "Default constructor could not be found for servlet class: " + servlet.getClass().getName(),
                    e);
                se.initCause(e);
                throw se;
            } catch (final NoSuchMethodException e) {
                final ServletException se = new ServletException(
                    "Default constructor could not be found for servlet class: " + servlet.getClass().getName(),
                    e);
                se.initCause(e);
                throw se;
            }
            final ServletConfig conf = configLoader.getConfig(servlet.getClass().getCanonicalName(), path);
            servlet.init(conf);
            servletQueue.enqueue(servlet);
            /*
             * Put into servlet pool for being accessible
             */
            if (SERVLET_POOL.putIfAbsent(path, servletQueue) != null) {
                throw new ServletException(new StringBuilder(256).append("A servlet with alias \"").append(id).append(
                    "\" has already been registered before.").toString());
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuilder(64).append("New servlet \"").append(servlet.getClass().getCanonicalName()).append(
                    "\" successfully registered to \"").append(path).append('"'));
            }
        } catch (final URISyntaxException e) {
            final ServletException se = new ServletException("Servlet path is not a valid URI", e);
            se.initCause(e);
            throw se;
        } finally {
            RW_LOCK.releaseWrite();
        }
    }

    /**
     * Unregisters the servlet bound to given ID from mapping.
     * 
     * @param id The servlet ID or alias
     */
    public static final void unregisterServlet(final String id) {
        RW_LOCK.acquireWrite();
        try {
            final String path = new URI(id.charAt(0) == '/' ? id.substring(1) : id).normalize().toString();
            final ServletConfigLoader configLoader = ServletConfigLoader.getDefaultInstance();
            if (null == configLoader) {
                LOG.error("Aborting servlet un-registration: HTTP service has not been initialized since default servlet configuration loader is null.");
                return;
            }
            configLoader.removeConfig(SERVLET_POOL.get(path).dequeue().getClass().getCanonicalName());
            SERVLET_POOL.remove(path);
        } catch (final URISyntaxException e) {
            final ServletException se = new ServletException("Servlet path is not a valid URI", e);
            se.initCause(e);
            LOG.error("Unregistering servlet failed. Servlet path is not a valid URI: " + id, se);
        } finally {
            RW_LOCK.releaseWrite();
        }
    }

    /**
     * Destroys the servlet that is bound to given ID.
     * 
     * @param id The servlet ID
     * @param servletObj The servlet instance
     */
    public static final void destroyServlet(final String id, final HttpServlet servletObj) {
        RW_LOCK.acquireWrite();
        try {
            if (servletObj instanceof SingleThreadModel) {
                /*
                 * Single-thread are used per instance, so there is no reference used by HttpServletManager, cause any reference is
                 * completely removed on invocations of getServlet()
                 */
                return;
            }
            SERVLET_POOL.remove(id);
        } finally {
            RW_LOCK.releaseWrite();
        }
    }

    private static final void clearServletPool() {
        RW_LOCK.acquireWrite();
        try {
            SERVLET_POOL.clear();
        } finally {
            RW_LOCK.releaseWrite();
        }
    }

    /**
     * Initializes HTTP servlet manager with specified initial servlet constructor map
     * 
     * @param servletConstructorMap The servlet constructor map
     */
    final static void initHttpServletManager(final Map<String, Constructor<?>> servletConstructorMap) {
        if (!initialized.get()) {
            synchronized (initialized) {
                if (!initialized.get()) {
                    HttpServletManager.servletConstructorMap = servletConstructorMap;
                    createServlets();
                    initialized.set(true);
                }
            }
        }
    }

    /**
     * Releases the HTTP servlet manager
     */
    final static void releaseHttpServletManager() {
        if (initialized.get()) {
            synchronized (initialized) {
                if (initialized.get()) {
                    clearServletPool();
                    HttpServletManager.servletConstructorMap.clear();
                    HttpServletManager.servletConstructorMap = null;
                    initialized.set(false);
                }
            }
        }
    }

    private static final Object[] INIT_ARGS = new Object[] {};

    private static final void createServlets() {
        RW_LOCK.acquireWrite();
        try {
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
                SERVLET_POOL.put(path, servletQueue);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("All Servlet Instances created & initialized");
            }
        } finally {
            RW_LOCK.releaseWrite();
        }
    }
}
