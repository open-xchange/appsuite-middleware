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
import java.util.ConcurrentModificationException;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.ajp13.AJPv13Utility;
import com.openexchange.ajp13.servlet.ServletConfigLoader;
import com.openexchange.ajp13.servlet.http.FiFoServletQueue;
import com.openexchange.ajp13.servlet.http.HttpErrorServlet;
import com.openexchange.ajp13.servlet.http.ServletQueue;
import com.openexchange.ajp13.servlet.http.SingletonServletQueue;

/**
 * {@link ConcurrentHttpServletManager} - A HTTP servlet manager using a {@link ReadWriteLock concurrent read-write lock}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConcurrentHttpServletManager extends AbstractHttpServletManager {

    // private final ReadWriteLock readWriteLock;

    private final Lock readLock;

    private final Lock writeLock;

    private final ConcurrentMap<String, ServletQueue> implierCache;

    /**
     * Initializes a new {@link ConcurrentHttpServletManager}.
     *
     * @param servletConstructorMap The servlet constructor map from which to initialize static servlet instances
     */
    public ConcurrentHttpServletManager(final Map<String, Constructor<?>> servletConstructorMap) {
        super(servletConstructorMap);
        final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
        implierCache = new NonBlockingHashMap<String, ServletQueue>();
    }

    @Override
    public void destroyServlet(final String id, final HttpServlet servletObj) {
        writeLock.lock();
        try {
            if (servletObj instanceof SingleThreadModel) {
                /*
                 * Single-thread are used per instance, so there is no reference used by HttpServletManager, cause any reference is
                 * completely removed on invocations of getServlet()
                 */
                return;
            }
            servletPool.remove(id);
            implierCache.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public HttpServlet getServlet(final String path, final StringBuilder pathStorage) {
        readLock.lock();
        try {
            final ServletQueue servletQueue = servletPool.get(path);
            if (servletQueue != null) {
                /*
                 * Direct hit
                 */
                if (path != null) {
                    pathStorage.append(path);
                }
                return getServletFromQueue(servletQueue, path);
            }
            /*
             * Try through resolving
             */
            try {
                /*
                 * Check implier cache
                 */
                ServletQueue cachedQueue = implierCache.get(path);
                if (null != cachedQueue) {
                    final String implier = cachedQueue.getServletPath();
                    pathStorage.append(implier);
                    return getServletFromQueue(cachedQueue, implier);
                }
                /*
                 * Upgrade lock: unlock read first to acquire write lock
                 */
                readLock.unlock();
                writeLock.lock();
                try {
                    /*
                     * Re-Check implier cache
                     */
                    cachedQueue = implierCache.get(path);
                    if (null != cachedQueue) {
                        final String implier = cachedQueue.getServletPath();
                        pathStorage.append(implier);
                        return getServletFromQueue(cachedQueue, implier);
                    }
                    /*
                     * Not available in implier cache
                     */
                    class Implier {

                        ServletQueue queue;

                        String implier;

                        Implier(final String implier, final ServletQueue queue) {
                            this.implier = implier;
                            this.queue = queue;
                        }

                        void setIfLonger(final String implier, final ServletQueue queue) {
                            if (implier.length() > this.implier.length()) {
                                this.implier = implier;
                                this.queue = queue;
                            }
                        }
                    }
                    Implier implier = null;
                    for (final Iterator<Map.Entry<String, ServletQueue>> iter = servletPool.entrySet().iterator(); iter.hasNext();) {
                        final Map.Entry<String, ServletQueue> e = iter.next();
                        final String currentPath = e.getKey();
                        if (implies(currentPath, path, false)) {
                            if (null == implier) {
                                implier = new Implier(currentPath, e.getValue());
                            } else {
                                implier.setIfLonger(currentPath, e.getValue());
                            }
                        }
                    }
                    if (null == implier) {
                        final ServletQueue errServletQueue =
                            new SingletonServletQueue(new HttpErrorServlet("No servlet bound to path/alias: " + AJPv13Utility.urlEncode(path)), path);
                        implierCache.put(path, errServletQueue);
                    } else {
                        pathStorage.append(implier.implier);
                        if (implier.queue.isSingleton()) {
                            implierCache.put(path, implier.queue);
                        }
                        return getServletFromQueue(implier.queue, implier.implier);
                    }
                } finally {
                    /*
                     * Downgrade lock: reacquire read without giving up write lock and...
                     */
                    readLock.lock();
                    /*
                     * ... unlock write.
                     */
                    writeLock.unlock();
                }
            } catch (final ConcurrentModificationException e) {
                LOG.warn("Resolving servlet path failed. Trying again...", e);
            } catch (final NoSuchElementException e) {
                LOG.warn("Resolving servlet path failed. Trying again...", e);
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void putServlet(final String path, final HttpServlet servlet) {
        if (implierCache.containsKey(path)) {
            /*
             * Implier cache contains only singletons
             */
            return;
        }
        ServletQueue servletQueue = servletPool.get(path);
        if (null != servletQueue && servletQueue.isSingleton()) {
            return;
        }
        writeLock.lock();
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
                    LOG.error("Default constructor could not be found for servlet class: " + servlet.getClass().getName(), e);
                    return;
                } catch (final NoSuchMethodException e) {
                    LOG.error("Default constructor could not be found for servlet class: " + servlet.getClass().getName(), e);
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
            writeLock.unlock();
        }

    }

    @Override
    public void registerServlet(final String id, final HttpServlet servlet, final Dictionary<String, String> initParams) throws ServletException {
        writeLock.lock();
        try {
            registerServlet0(id, servlet, initParams);
            implierCache.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unregisterServlet(final String id) {
        writeLock.lock();
        try {
            unregisterServlet0(id);
            implierCache.clear();
        } finally {
            writeLock.unlock();
        }
    }

}
