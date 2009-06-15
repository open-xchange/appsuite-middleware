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

package com.openexchange.server.osgiservice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.global.OXCloseable;

/**
 * {@link DynamicWhiteboardFactory}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DynamicWhiteboardFactory implements OXCloseable {

    private ServiceTracker tracker;

    public DynamicWhiteboardFactory(final BundleContext context) {
        this.tracker = new ServiceTracker(context, WhiteboardFactoryService.class.getName(), null);
        tracker.open();
    }

    public void close() throws AbstractOXException {
        tracker.close();
    }

    public <T> T createWhiteboardService(BundleContext context, Class<T> klass, Collection<OXCloseable> closeables, DynamicServiceStateListener listener) {
        WhiteboardFactoryService<T> factory = getFactory(klass);
        if (factory != null) {
            return factory.create(context, closeables);
        }
        ServiceTrackerInvocationHandler handler = new ServiceTrackerInvocationHandler(context, klass, listener);
        closeables.add(handler);
        return (T) Proxy.newProxyInstance(klass.getClassLoader(), new Class[] { klass }, handler);
    }

    private <T> WhiteboardFactoryService<T> getFactory(Class<T> klass) {
        Object[] services = tracker.getServices();
        if (services == null) {
            return null;
        }
        for (int i = 0; i < services.length; i++) {
            WhiteboardFactoryService factory = (WhiteboardFactoryService) services[i];
            if (factory.getType().equals(klass)) {
                return (WhiteboardFactoryService<T>) factory;
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

        public ServiceTrackerInvocationHandler(BundleContext context, Class klass, final DynamicServiceStateListener listener) {
            this.klass = klass;
            tracker = new ServiceTracker(context, klass.getName(), null) {

                @Override
                public Object addingService(ServiceReference reference) {
                    Object retval = super.addingService(reference);
                    if (listener != null) {
                        synchronized(this) {
                            adding = true;

                            if(supercedes(reference, tracker.getServiceReference())) {
                                override = retval;
                            }
                            listener.stateChanged();
                            adding = false;
                            override = null;
                        }
                    }
                    return retval;
                }

                private boolean supercedes(ServiceReference reference, ServiceReference otherReference) {
                    if(null == otherReference) {
                        return true;
                    }
                    Integer p1 = (Integer) reference.getProperty(Constants.SERVICE_RANKING);
                    if(p1 == null) {
                        return false;
                    }
                    Integer p2 = (Integer) otherReference.getProperty(Constants.SERVICE_RANKING);
                    if(p2 == null) {
                        return true;
                    }
                    if(p1 == p2) {
                        p1 = (Integer) reference.getProperty(Constants.SERVICE_ID);
                        p2 = (Integer) otherReference.getProperty(Constants.SERVICE_ID);
                    }
                    return p1 > p2;
                }

                @Override
                public void removedService(ServiceReference reference, Object service) {
                    super.removedService(reference, service);
                    if (listener != null) {
                        listener.stateChanged();
                    }
                }
            };
            tracker.open();
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(args == null || args.length == 0 && TO_STRING.equals(method.getName())) {
                return "OSGi Proxy ( "+klass.getName()+" ) "+hashCode();
            }
            return method.invoke(getDelegate(), args);
        }

        private Object getDelegate() {
            if(override != null) {
                return override;
            }
            if (tracker.size() == 0) {
                // Wait a second for service to hopefully reappear before failing
                try {
                    tracker.waitForService(1000);
                } catch (InterruptedException e) {
                    return null;
                }
                if (tracker.size() == 0) {
                    throw new IllegalStateException("Did not find an implementation of service " + klass.getName() + ".");
                }
            }
            return tracker.getService();
        }

        public void close() throws AbstractOXException {
            tracker.close();
        }

        public boolean isActive() {
            return adding || tracker.size() > 0;
        }

    }

    public boolean isActive(Object o) {
        if (!Proxy.isProxyClass(o.getClass())) {
            return true;
        }
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
        if(ServiceTrackerInvocationHandler.class.isInstance(invocationHandler)) {
            ServiceTrackerInvocationHandler handler = (ServiceTrackerInvocationHandler)invocationHandler;
            return handler.isActive();
        }
        return true;
    }

}
