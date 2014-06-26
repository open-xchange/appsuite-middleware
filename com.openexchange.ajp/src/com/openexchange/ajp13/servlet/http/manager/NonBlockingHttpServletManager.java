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

package com.openexchange.ajp13.servlet.http.manager;

import java.lang.reflect.Constructor;
import java.util.ConcurrentModificationException;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import com.openexchange.ajp13.servlet.ServletConfigLoader;
import com.openexchange.ajp13.servlet.http.FiFoServletQueue;
import com.openexchange.ajp13.servlet.http.ServletQueue;
import com.openexchange.tools.NonBlockingRWLock;

/**
 * {@link NonBlockingHttpServletManager} - A HTTP servlet manager using a {@link NonBlockingRWLock non-blocking read-write lock}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NonBlockingHttpServletManager extends AbstractHttpServletManager {

    private final NonBlockingRWLock readWriteLock;

    /**
     * Initializes a new {@link NonBlockingHttpServletManager}.
     *
     * @param servletConstructorMap The servlet constructor map from which to initialize static servlet instances
     */
    public NonBlockingHttpServletManager(final Map<String, Constructor<?>> servletConstructorMap) {
        super(servletConstructorMap);
        readWriteLock = new NonBlockingRWLock(true);
    }

    @Override
    public void destroyServlet(final String id, final HttpServlet servletObj) {
        readWriteLock.acquireWrite();
        try {
            if (servletObj instanceof SingleThreadModel) {
                /*
                 * Single-thread are used per instance, so there is no reference used by HttpServletManager, cause any reference is
                 * completely removed on invocations of getServlet()
                 */
                return;
            }
            servletPool.remove(id);
        } finally {
            readWriteLock.releaseWrite();
        }
    }

    @Override
    public HttpServlet getServlet(final String path, final StringBuilder pathStorage) {
        int state;
        HttpServlet retval = null;
        String path2Append;
        do {
            state = readWriteLock.acquireRead();
            final ServletQueue servletQueue = servletPool.get(path);
            if (servletQueue != null) {
                if (null != retval) {
                    // Previously obtained
                    releaseServletInternal(servletQueue, retval);
                }
                path2Append = path;
                retval = getServletFromQueue(servletQueue, path);
            } else {
                /*
                 * Try through resolving
                 */
                path2Append = null;
                try {
                    ServletQueue queue = null;
                    String longestImplier = null;
                    for (final Iterator<Map.Entry<String, ServletQueue>> iter = servletPool.entrySet().iterator(); iter.hasNext();) {
                        final Map.Entry<String, ServletQueue> e = iter.next();
                        final String currentPath = e.getKey();
                        if (implies(currentPath, path, false)) {
                            if (null == longestImplier) {
                                longestImplier = currentPath;
                                queue = e.getValue();
                            } else if (currentPath.length() > longestImplier.length()) {
                                longestImplier = currentPath;
                                queue = e.getValue();
                            }
                        }
                    }
                    if (null != longestImplier) {
                        if (null != retval) {
                            // Previously obtained
                            releaseServletInternal(queue, retval);
                        }
                        path2Append = longestImplier;
                        retval = getServletFromQueue(queue, longestImplier);
                    }
                } catch (final ConcurrentModificationException e) {
                    LOG.warn("Resolving servlet path failed. Trying again...", e);
                } catch (final NoSuchElementException e) {
                    LOG.warn("Resolving servlet path failed. Trying again...", e);
                }
            }
        } while (!readWriteLock.releaseRead(state));
        if (path2Append != null) {
            pathStorage.append(path2Append);
        }
        return retval;
    }

    @Override
    public void putServlet(final String path, final HttpServlet servlet) {
        ServletQueue servletQueue = servletPool.get(path);
        if (null != servletQueue && servletQueue.isSingleton()) {
            return;
        }
        readWriteLock.acquireWrite();
        try {
            // servletQueue = servletPool.get(path);
            if (null != servletQueue) {
                /*
                 * Since heading condition failed the servlet must be an instance of SingleThreadModel
                 */
                servletQueue.enqueue(servlet);
            } else {
                try {
                    servletQueue =
                        new FiFoServletQueue(1, servlet.getClass().getConstructor(CLASS_ARR), !(servlet instanceof SingleThreadModel), path);
                } catch (final SecurityException e) {
                    LOG.error("Default constructor could not be found for servlet class: {}", servlet.getClass().getName(), e);
                    return;
                } catch (final NoSuchMethodException e) {
                    LOG.error("Default constructor could not be found for servlet class: {}", servlet.getClass().getName(), e);
                    return;
                }
                final ServletConfig conf = ServletConfigLoader.getDefaultInstance().getConfig(servlet.getClass().getCanonicalName(), path);
                try {
                    servlet.init(conf);
                } catch (final ServletException e) {
                    LOG.error("Servlet could not be put into pool", e);
                    return;
                }
                servletQueue.enqueue(servlet);
                servletPool.put(path, servletQueue);
            }
        } finally {
            readWriteLock.releaseWrite();
        }

    }

    @Override
    public void registerServlet(final String id, final HttpServlet servlet, final Dictionary<String, String> initParams) throws ServletException {
        readWriteLock.acquireWrite();
        try {
            registerServlet0(id, servlet, initParams);
        } finally {
            readWriteLock.releaseWrite();
        }
    }

    @Override
    public void unregisterServlet(final String id) {
        readWriteLock.acquireWrite();
        try {
            unregisterServlet0(id);
        } finally {
            readWriteLock.releaseWrite();
        }
    }

    /*-
     * ######################### HELPER METHODS #########################
     */

    private static final void releaseServletInternal(final ServletQueue servletQueue, final HttpServlet servletInstance) {
        if (!servletQueue.isSingleton()) {
            servletQueue.enqueue(servletInstance);
        }
    }

}
