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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.osgi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.tools.global.OXCloseable;

/**
 * {@link DynamicWhiteboardFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DynamicWhiteboardFactory implements OXCloseable {

    private final ServiceTracker tracker;

    public DynamicWhiteboardFactory(final BundleContext context) {
        tracker = new ServiceTracker(context, WhiteboardFactoryService.class.getName(), null);
        tracker.open();
    }

    @Override
    public void close() throws OXException {
        tracker.close();
    }

    public <T> T createWhiteboardService(final BundleContext context, final Class<T> klass, final Collection<OXCloseable> closeables, final DynamicServiceStateListener listener) {
        final WhiteboardFactoryService<T> factory = getFactory(klass);
        if (factory != null) {
            return factory.create(context, closeables);
        }
        final ServiceTrackerInvocationHandler handler = new ServiceTrackerInvocationHandler(context, klass, listener);
        closeables.add(handler);
        return (T) Proxy.newProxyInstance(klass.getClassLoader(), new Class[] { klass }, handler);
    }

    private <T> WhiteboardFactoryService<T> getFactory(final Class<T> klass) {
        final Object[] services = tracker.getServices();
        if (services == null) {
            return null;
        }
        for (final Object service : services) {
            final WhiteboardFactoryService factory = (WhiteboardFactoryService) service;
            if (factory.getType().equals(klass)) {
                return factory;
            }
        }
        return null;
    }

    private static final class ServiceTrackerInvocationHandler implements InvocationHandler, OXCloseable {

        private static final String TO_STRING = "toString";

        private ServiceTracker tracker;

        private boolean adding = false;

        private Object override = null;

        private Class klass;

        public ServiceTrackerInvocationHandler(final BundleContext context, final Class klass, final DynamicServiceStateListener listener) {
            this.klass = klass;
            tracker = new ServiceTracker(context, klass.getName(), null) {

                @Override
                public Object addingService(final ServiceReference reference) {
                    final Object retval = super.addingService(reference);
                    if (listener != null) {
                        synchronized (this) {
                            adding = true;

                            if (supercedes(reference, tracker.getServiceReference())) {
                                override = retval;
                            }
                            listener.stateChanged();
                            adding = false;
                            override = null;
                        }
                    }
                    return retval;
                }

                private boolean supercedes(final ServiceReference reference, final ServiceReference otherReference) {
                    if (null == otherReference) {
                        return true;
                    }
                    Integer p1 = (Integer) reference.getProperty(Constants.SERVICE_RANKING);
                    if (p1 == null) {
                        return false;
                    }
                    Integer p2 = (Integer) otherReference.getProperty(Constants.SERVICE_RANKING);
                    if (p2 == null) {
                        return true;
                    }
                    if (p1.equals(p2)) {
                        p1 = (Integer) reference.getProperty(Constants.SERVICE_ID);
                        p2 = (Integer) otherReference.getProperty(Constants.SERVICE_ID);
                    }
                    return p1.intValue() > p2.intValue();
                }

                @Override
                public void removedService(final ServiceReference reference, final Object service) {
                    super.removedService(reference, service);
                    if (listener != null) {
                        listener.stateChanged();
                    }
                }
            };
            tracker.open();
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if((args == null || args.length == 0) && TO_STRING.equals(method.getName())) {
                return "OSGi Proxy ( "+klass.getName()+" ) "+hashCode();
            }
            try {
                return method.invoke(getDelegate(), args);
            } catch (final InvocationTargetException x) {
                if(null == x.getCause()) {
                    throw x;
                }
                throw x.getCause();
            }
        }

        private Object getDelegate() {
            if (override != null) {
                return override;
            }
            if (tracker.size() == 0) {
                // Wait a second for service to hopefully reappear before failing
                try {
                    tracker.waitForService(1000);
                } catch (final InterruptedException e) {
                    // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                    Thread.currentThread().interrupt();
                    return null;
                }
                if (tracker.size() == 0) {
                    throw new IllegalStateException("Did not find an implementation of service " + klass.getName() + ".");
                }
            }
            return tracker.getService();
        }

        @Override
        public void close() throws OXException {
            tracker.close();
        }

        public boolean isActive() {
            return adding || tracker.size() > 0;
        }

    }

    public boolean isActive(final Object o) {
        if (!Proxy.isProxyClass(o.getClass())) {
            return true;
        }
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
        if (ServiceTrackerInvocationHandler.class.isInstance(invocationHandler)) {
            final ServiceTrackerInvocationHandler handler = (ServiceTrackerInvocationHandler) invocationHandler;
            return handler.isActive();
        }
        return true;
    }

}
