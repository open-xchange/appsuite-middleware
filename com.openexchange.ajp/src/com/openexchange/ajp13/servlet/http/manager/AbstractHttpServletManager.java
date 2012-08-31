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

package com.openexchange.ajp13.servlet.http.manager;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.servlet.ServletConfigLoader;
import com.openexchange.ajp13.servlet.http.FiFoServletQueue;
import com.openexchange.ajp13.servlet.http.HttpErrorServlet;
import com.openexchange.ajp13.servlet.http.ServletQueue;
import com.openexchange.ajp13.servlet.http.SingletonServletQueue;

/**
 * {@link AbstractHttpServletManager} - Abstract {@link IHttpServletManager}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractHttpServletManager implements IHttpServletManager {

    /**
     * The logger.
     */
    protected static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractHttpServletManager.class));

    /**
     * The empty class array.
     */
    protected final static Class<?>[] CLASS_ARR = new Class[] {};

    protected final ConcurrentMap<String, ServletQueue> servletPool;

    protected final ConcurrentMap<String, ConcurrentStack<ServletQueue>> parkedServletPool;

    /**
     * Creates a new {@link AbstractHttpServletManager}.
     * <p>
     * Initializes static servlet instances and puts them into servlet pool.
     *
     * @param servletConstructorMap The servlet constructor map from which to initialize static servlet instances
     */
    protected AbstractHttpServletManager(final Map<String, Constructor<?>> servletConstructorMap) {
        super();
        servletPool = new NonBlockingHashMap<String, ServletQueue>();
        parkedServletPool = new NonBlockingHashMap<String, ConcurrentStack<ServletQueue>>(4);
        createServlets(servletConstructorMap);
    }

    private static final Object[] INIT_ARGS = new Object[] {};

    /**
     * Creates the static servlets identified by servlet constructor map.
     */
    private final void createServlets(final Map<String, Constructor<?>> servletConstructorMap) {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractHttpServletManager.class));
        final ServletConfigLoader configLoader = ServletConfigLoader.getDefaultInstance();
        if (null == configLoader) {
            log.error("Aborting servlets' initialization: HTTP service has not been initialized since default servlet configuration loader is null.");
            return;
        }
        for (final Iterator<Map.Entry<String, Constructor<?>>> iter = servletConstructorMap.entrySet().iterator(); iter.hasNext();) {
            final Map.Entry<String, Constructor<?>> entry = iter.next();
            String path;
            try {
                final String id = entry.getKey();
                path =
                    new URI(entry.getKey().charAt(0) == '/' ? id : new StringBuilder(id.length() + 1).append('/').append(id).toString()).normalize().toString();
            } catch (final URISyntaxException e) {
                log.error("Invalid servlet path skipped: " + entry.getKey());
                continue;
            }
            ServletQueue servletQueue = null;
            /*
             * Create a pool of servlet instances
             */
            final Constructor<?> servletConstructor = entry.getValue();
            if (servletConstructor == null) {
                servletQueue = new SingletonServletQueue(new HttpErrorServlet("No servlet constructor found for " + path), path);
                servletQueue.enqueue(new HttpErrorServlet("No servlet constructor found for " + path));
            } else {
                try {
                    final HttpServlet servletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
                    if (servletInstance instanceof SingleThreadModel) {
                        /*
                         * Multiple instances
                         */
                        final int servletPoolSize = AJPv13Config.getServletPoolSize();
                        servletQueue = new FiFoServletQueue(servletPoolSize, servletConstructor, false, path);
                        if (servletPoolSize > 0) {
                            final ServletConfig conf = configLoader.getConfig(servletInstance.getClass().getCanonicalName(), path);
                            servletInstance.init(conf);
                            servletQueue.enqueue(servletInstance);
                            /*
                             * Enqueue more than one instance if it implements SingleThreadModel
                             */
                            for (int i = 1; i < servletPoolSize; i++) {
                                final HttpServlet anotherServletInstance = (HttpServlet) servletConstructor.newInstance(INIT_ARGS);
                                anotherServletInstance.init(conf);
                                servletQueue.enqueue(anotherServletInstance);
                            }
                        }
                    } else {
                        /*
                         * A singleton
                         */
                        servletQueue = new SingletonServletQueue(servletInstance, path);
                        servletInstance.init(configLoader.getConfig(servletInstance.getClass().getCanonicalName(), path));
                        servletQueue.enqueue(servletInstance);
                    }
                } catch (final Throwable t) {
                    log.error(t.getMessage(), t);
                }
            }
            servletPool.put(path, servletQueue);
        }
        if (log.isInfoEnabled()) {
            log.info("All Servlet Instances created & initialized");
        }
    }

    protected final void registerServlet0(final String id, final HttpServlet servlet, final Dictionary<String, String> initParams) throws ServletException {
        try {
            final String path = new URI(prependSlash(id)).normalize().toString();
            if (servletPool.containsKey(path) && ((null == initParams) || initParams.isEmpty() || !Boolean.parseBoolean(initParams.get(HTTP_REGISTER_FORCE)))) {
                throw new ServletException(new StringBuilder(256).append("A servlet with alias \"").append(path).append(
                    "\" has already been registered before.").toString());
            }
            final ServletConfigLoader configLoader = ServletConfigLoader.getDefaultInstance();
            if (null == configLoader) {
                throw new ServletException(
                    "Aborting servlet registration: HTTP service has not been initialized since default servlet configuration loader is null.");
            }
            final boolean forceRegistration;
            if ((null != initParams) && !initParams.isEmpty()) {
                configLoader.setConfig(servlet.getClass().getCanonicalName(), initParams);
                forceRegistration = Boolean.parseBoolean(initParams.get(HTTP_REGISTER_FORCE));
            } else {
                forceRegistration = false;
            }
            /*
             * Initialize servlet with servlet config
             */
            final ServletConfig conf = configLoader.getConfig(servlet.getClass().getCanonicalName(), path);
            servlet.init(conf);
            /*
             * Create servlet queue dependent on singleton or not
             */
            final ServletQueue servletQueue;
            if (servlet instanceof SingleThreadModel) {
                Constructor<? extends HttpServlet> servletConstructor;
                try {
                    servletConstructor = servlet.getClass().getConstructor(CLASS_ARR);
                } catch (final SecurityException e) {
                    LOG.warn(
                        "Default constructor could not be found for javax.servlet.SingleThreadModel servlet class: " + servlet.getClass().getName(),
                        e);
                    servletConstructor = null;
                } catch (final NoSuchMethodException e) {
                    LOG.warn(
                        "Default constructor could not be found for javax.servlet.SingleThreadModel servlet class: " + servlet.getClass().getName(),
                        e);
                    servletConstructor = null;
                }
                final int servletPoolSize = AJPv13Config.getServletPoolSize();
                servletQueue = new FiFoServletQueue(servletPoolSize <= 0 ? 1 : servletPoolSize, servletConstructor, false, path);
                servletQueue.enqueue(servlet);
            } else {
                /*
                 * Singleton
                 */
                servletQueue = new SingletonServletQueue(servlet, path);
            }
            /*
             * Put into servlet pool for being accessible
             */
            if (forceRegistration) {
                final ServletQueue previous = servletPool.put(path, servletQueue);
                if (null != previous) {
                    /*-
                     *
                    final String canonicalName = previous.get().getClass().getCanonicalName();
                    configLoader.removeConfig(canonicalName);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(new StringBuilder(64).append("Previous servlet \"").append(canonicalName).append("\" unregistered from \"").append(
                            path).append('"'));
                    }
                     *
                     */
                    /*
                     * Park queue
                     */
                    ConcurrentStack<ServletQueue> parkedQueues = parkedServletPool.get(path);
                    if (null == parkedQueues) {
                        final ConcurrentStack<ServletQueue> newParkedQueues = new ConcurrentStack<ServletQueue>();
                        parkedQueues = parkedServletPool.putIfAbsent(path, newParkedQueues);
                        if (null == parkedQueues) {
                            parkedQueues = newParkedQueues;
                        }
                    }
                    parkedQueues.push(previous);
                }
            } else {
                if (servletPool.putIfAbsent(path, servletQueue) != null) {
                    throw new ServletException(new StringBuilder(256).append("A servlet with alias \"").append(path).append(
                        "\" has already been registered before.").toString());
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuilder(64).append("New servlet \"").append(servlet.getClass().getCanonicalName()).append(
                    "\" successfully registered to \"").append(path).append('"'));
            }
        } catch (final URISyntaxException e) {
            final ServletException se = new ServletException("Servlet path is not a valid URI", e);
            se.initCause(e);
            throw se;
        }
    }

    protected final void unregisterServlet0(final String id) {
        try {
            final String path = new URI(prependSlash(id)).normalize().toString();
            final ServletConfigLoader configLoader = ServletConfigLoader.getDefaultInstance();
            if (null == configLoader) {
                LOG.error("Aborting servlet un-registration: HTTP service has not been initialized since default servlet configuration loader is null.");
                return;
            }
            final ServletQueue servletQueue = servletPool.remove(path);
            if (null == servletQueue) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Servlet un-registration failed. No servlet is bound to path: " + path);
                }
                return;
            }
            configLoader.removeConfig(servletQueue.get().getClass().getCanonicalName());
            /*
             * Look-up parked queues
             */
            final ConcurrentStack<ServletQueue> parkedQueues = parkedServletPool.get(path);
            if (null != parkedQueues) {
                /*
                 * Restore previous queue
                 */
                final ServletQueue queue = parkedQueues.pop();
                if (null != queue) {
                    servletPool.put(path, queue);
                }
            }
        } catch (final URISyntaxException e) {
            final ServletException se = new ServletException("Servlet path is not a valid URI", e);
            se.initCause(e);
            LOG.error("Unregistering servlet failed. Servlet path is not a valid URI: " + id, se);
        }
    }

    /*-
     * ############################ Static helper methods ############################
     */

    /**
     * Ensures specified path starts with "/" character.
     *
     * @param path The path
     * @return The path starting with "/" character
     */
    protected static String prependSlash(final String path) {
        return path.charAt(0) == '/' ? path : new StringBuilder(path.length() + 1).append('/').append(path).toString();
    }

    /**
     * Checks if given <code>implier</code> implies given <code>path</code>.
     * <p>
     * Parameter <code>forceWhitespaceNotation</code> controls if a whitespace character needs to be present in order to let
     * <code>implier</code> imply <code>path</code>.<br>
     * If <code>true</code>:<br>
     * <code>"path/sub*"</code> implies <code>"path/sub/anysub"</code><br>
     * If <code>false</code> the whitespace character is not necessary.
     *
     * @param implier The path which might imply
     * @param path The path which might be implied
     * @param forceWhitespaceNotation <code>true</code> to enforce whitespace notation for implication; otherwise <code>false</code>
     * @return <code>true</code> if given <code>implier</code> implies given <code>path</code>; otherwise <code>false</code>
     */
    protected static final boolean implies(final String implier, final String path, final boolean forceWhitespaceNotation) {
        final int len = implier.length();
        if (!forceWhitespaceNotation) {
            /*
             * A wildcard path
             */
            final String sImplier;
            if ('*' == implier.charAt(len - 1)) {
                sImplier = len == 1 ? "" : implier.substring(0, len - 1);
            } else {
                sImplier = implier;
            }
            /*
             * Make sure ap.path is longer or equal length so a/b/ does imply a/b
             */
            return (path.length() >= sImplier.length()) && path.startsWith(sImplier);
        }
        if ('*' == implier.charAt(len - 1)) {
            /*
             * A wildcard path
             */
            final String _currentPath = len == 1 ? "" : implier.substring(0, len - 1);
            /*
             * Make sure ap.path is longer or equal length so a/b/ does imply a/b
             */
            return (path.length() >= _currentPath.length()) && path.startsWith(_currentPath);
        }
        return implier.equals(path);
    }

    /**
     * Gets the servlet instance bound to given path.
     *
     * @param servletQueue The servlet queue
     * @param path The servlet path
     * @return The servlet instance dequeued from pool or newly created
     */
    protected static final HttpServlet getServletFromQueue(final ServletQueue servletQueue, final String path) {
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
        /*
         * If servlet class implements SingleThreadModel (non-singleton) the same instance MUST NOT be used concurrently by multiple
         * threads. So remove from queue.
         */
        return servletQueue.isSingleton() ? servletQueue.get() : servletQueue.dequeue();
    }

}
