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

package com.openexchange.server.osgiservice;

import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


/**
 * A {@link HousekeepingActivator} helps with housekeeping tasks like remembering service trackers or service registrations
 * and cleaning them up later.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class HousekeepingActivator extends DeferredActivator {

    private List<ServiceTracker> serviceTrackers = new LinkedList<ServiceTracker>();
    private List<ServiceRegistration> serviceRegistrations = new LinkedList<ServiceRegistration>();

    @Override
    protected void handleAvailability(Class<?> clazz) {
        // Override if needed
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        // Override if needed
    }
    
    @Override
    protected void stopBundle() throws Exception {
        cleanUp();
    }

    protected <T> void registerService(Class<T> klass, T service, Dictionary properties) {
        serviceRegistrations.add(context.registerService(klass.getName(), service, properties));
    }
    
    protected <T> void registerService(Class<T> klass, T service) {
        registerService(klass, service, null);
    }
    
    protected void rememberTracker(ServiceTracker tracker) {
       serviceTrackers.add(tracker);
    }
    
    protected void forgetTracker(ServiceTracker tracker) {
        serviceTrackers.remove(tracker);
    }
    
    protected ServiceTracker track(Class<?> klass, ServiceTrackerCustomizer customizer) {
        ServiceTracker tracker = new ServiceTracker(context, klass.getName(), customizer);
        rememberTracker(tracker);
        return tracker;
    }
    
    protected ServiceTracker track(Filter filter, ServiceTrackerCustomizer customizer) {
        ServiceTracker tracker = new ServiceTracker(context, filter, customizer);
        rememberTracker(tracker);
        return tracker;
    }
    
    protected ServiceTracker track(Class<?> klass) {
        return track(klass, (ServiceTrackerCustomizer) null);
    }
    
    protected ServiceTracker track(Filter filter) {
        return track(filter, (ServiceTrackerCustomizer) null);
    }
    
    protected <T> ServiceTracker track(Class<?> klass, final SimpleRegistryListener<T> listener) {
        return track(klass, new ServiceTrackerCustomizer() {

            public Object addingService(ServiceReference arg0) {
                Object service = context.getService(arg0);
                listener.added(arg0, (T) service);
                return service;
            }

            public void modifiedService(ServiceReference arg0, Object arg1) {
               // Don't care
                
            }

            public void removedService(ServiceReference arg0, Object arg1) {
                listener.removed(arg0, (T) arg1);
                context.ungetService(arg0);
            }
            
        });
    }
    
    protected <T> ServiceTracker track(Filter filter, final SimpleRegistryListener<T> listener) {
        return track(filter, new ServiceTrackerCustomizer() {

            public Object addingService(ServiceReference arg0) {
                Object service = context.getService(arg0);
                listener.added(arg0, (T) service);
                return service;
            }

            public void modifiedService(ServiceReference arg0, Object arg1) {
                // TODO Auto-generated method stub
                
            }

            public void removedService(ServiceReference arg0, Object arg1) {
                listener.removed(arg0, (T) arg1);
                context.ungetService(arg0);
            }
            
        });
    }
    
    protected void openTrackers() {
        for (ServiceTracker tracker : new LinkedList<ServiceTracker>(serviceTrackers)) {
            tracker.open();
        }
    }
    
    protected void closeTrackers() {
        for (ServiceTracker tracker : new LinkedList<ServiceTracker>(serviceTrackers)) {
            tracker.close();
        }
    }
    
    protected void clearTrackers() {
        serviceTrackers.clear();
    }
    
    protected void unregisterServices() {
        for (ServiceRegistration reg : serviceRegistrations) {
            reg.unregister();
        }
        serviceRegistrations.clear();
    }
    
    protected void cleanUp() {
        closeTrackers();
        clearTrackers();
        unregisterServices();
    }
    
}
