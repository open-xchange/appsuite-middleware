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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.hazelcast.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.hazelcast.osgi.HazelcastActivator;
import com.openexchange.hazelcast.osgi.OXMap;

/**
 * {@link Hazelcasts} - Utility class for Hazelcast.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Hazelcasts {

    /**
     * Sets the <tt>ClassLoader</tt> of the current thread to the one obtained from specified class that is supposed to reside in target
     * bundle. Inspired by <a href="https://groups.google.com/forum/?fromgroups#!topic/hazelcast/zJavJ1ouMnk">OSGi support of Hazelcast</a>
     * <p>
     * <b>Usage:</b>
     * 
     * <pre>
     * // Switch current thread's class loader
     * ClassLoaderModifier modifier = new ClassLoaderModifier();
     * modifier.setClassLoader(this.getClass());
     * try {
     *     // Do Hazelcast stuff now &amp; restore afterwards
     * } finally {
     *     modifier.restoreClassLoader();
     * }
     * </pre>
     */
    public static final class ClassLoaderModifier {

        private volatile ClassLoader classLoader;

        /**
         * Initializes a new {@link Hazelcasts.ClassLoaderModifier}.
         */
        public ClassLoaderModifier() {
            super();
        }

        /**
         * Sets the <tt>ClassLoader</tt> of the current thread to the one obtained from specified class that is supposed to reside in target
         * bundle. Inspired by <a href="https://groups.google.com/forum/?fromgroups#!topic/hazelcast/zJavJ1ouMnk">OSGi support of
         * Hazelcast</a>
         * <p>
         * <b>Usage:</b>
         * 
         * <pre>
         * // Switch current thread's class loader
         * ClassLoaderModifier modifier = new ClassLoaderModifier();
         * modifier.setClassLoader(this.getClass());
         * try {
         *     // Do Hazelcast stuff now &amp; restore afterwards
         * } finally {
         *     modifier.restoreClassLoader();
         * }
         * </pre>
         * 
         * @param clazz A class from target bundle; e.g. the activator
         */
        public void setClassLoader(final Class<?> clazz) {
            /*
             * Cache the current context class loader.
             */
            final Thread currentThread = Thread.currentThread();
            final ClassLoader ccl = currentThread.getContextClassLoader();
            /*
             * Get the classloader of a class from inside the bundle & set it as context class loader
             */
            currentThread.setContextClassLoader(clazz.getClassLoader());
            this.classLoader = ccl;
            /*
             * Ready for Hazelcast stuff
             */
        }

        /**
         * Restores the previously removed class loader in current thread.
         */
        public void restoreClassLoader() {
            final ClassLoader ccl = this.classLoader;
            if (null != ccl) {
                // Reset the context class loader to the cached loader
                Thread.currentThread().setContextClassLoader(ccl);
            }
        }

    } // End of ClassLoaderModifier

    /**
     * Initializes a new {@link Hazelcasts}.
     */
    private Hazelcasts() {
        super();
    }

    /**
     * Gets registered <tt>HazelcastInstance</tt>.
     * 
     * @return The <tt>HazelcastInstance</tt> or <code>null</code> if not initialized, yet
     */
    public static HazelcastInstance getHazelcastInstance() {
        return HazelcastActivator.REF_HAZELCAST_INSTANCE.get();
    }

    private static ThreadLocal<ClassLoaderModifier> MODIFIER = new ThreadLocal<ClassLoaderModifier>();

    /**
     * Sets the <tt>ClassLoader</tt> of the current thread to the one obtained from specified class that is supposed to reside in target
     * bundle.
     * <p>
     * <b>Usage:</b>
     * 
     * <pre>
     * // Switch current thread's class loader
     * Hazelcasts.setClassLoader(this.getClass());
     * try {
     *     // Do Hazelcast stuff now &amp; restore afterwards
     * } finally {
     *     Hazelcasts.restoreClassLoader();
     * }
     * </pre>
     * 
     * @param clazz A class from target bundle; e.g. the activator
     * @throws IllegalStateException If current thread's class loader has already been modified (and not yet {@link #restoreClassLoader()
     *             restored})
     * @see #restoreClassLoader()
     * @see ClassLoaderModifier
     */
    public static void setClassLoader(final Class<?> bundleClass) {
        if (null == bundleClass) {
            return;
        }
        ClassLoaderModifier classLoaderModifier = MODIFIER.get();
        if (null != classLoaderModifier) {
            throw new IllegalStateException(
                "Current thread's class loader has already been modified (and not yet restored): " + Thread.currentThread().getName());
        }
        classLoaderModifier = new ClassLoaderModifier();
        classLoaderModifier.setClassLoader(bundleClass);
        MODIFIER.set(classLoaderModifier);
    }

    /**
     * Restores the previously removed class loader in current thread.
     * 
     * @see #setClassLoader(Class)
     */
    public static void restoreClassLoader() {
        final ClassLoaderModifier classLoaderModifier = MODIFIER.get();
        if (null != classLoaderModifier) {
            classLoaderModifier.restoreClassLoader();
            MODIFIER.set(null);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrapWithClassloader(final Class<?> classLoaderSource, final Class<T> type, final T delegate) {
    	if (delegate instanceof OXMap) {
    		((OXMap) delegate).setClassLoaderSource(classLoaderSource);
    		return delegate;
    	}
    	return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, new ClassLoaderInvocationHandler(
            delegate,
            classLoaderSource));
    }

    private static final class ClassLoaderInvocationHandler implements InvocationHandler {

        private final Object delegate;

        private final Class<?> cl;

        public ClassLoaderInvocationHandler(final Object delegate, final Class<?> cl) {
            this.cl = cl;
            this.delegate = delegate;
        }

        @Override
        public Object invoke(final Object self, final Method method, final Object[] arguments) throws Throwable {
            final ClassLoaderModifier modifier = new ClassLoaderModifier();
            try {
                modifier.setClassLoader(cl);
                return method.invoke(delegate, arguments);
            } finally {
                modifier.restoreClassLoader();
            }
        }

    }

}
