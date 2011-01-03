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

package com.openexchange.exceptions.osgi;

import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exceptions.ComponentAlreadyRegisteredException;
import com.openexchange.exceptions.ComponentRegistry;
import com.openexchange.exceptions.Exceptions;
import com.openexchange.groupware.Component;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class OSGiComponentRegistry implements ComponentRegistry, ServiceTrackerCustomizer {

    private ComponentRegistry delegate = null;

    private final BundleContext context;

    private final ServiceTracker serviceTracker;

    public OSGiComponentRegistry(final BundleContext context) {
        this.context = context;
        serviceTracker = new ServiceTracker(context, ComponentRegistry.class.getName(), this);
        serviceTracker.open();
    }

    public void close() {
        serviceTracker.close();
        ;
    }

    public void registerComponent(final Component component, final String applicationId, final Exceptions<?> exceptions) throws ComponentAlreadyRegisteredException {
        checkDelegate();
        delegate.registerComponent(component, applicationId, exceptions);
    }

    public void deregisterComponent(final Component component) {
        checkDelegate();
        delegate.deregisterComponent(component);
    }

    public Exceptions<?> getExceptionsForComponent(final Component component) {
        checkDelegate();
        return delegate.getExceptionsForComponent(component);
    }

    public List<Exceptions<?>> getExceptionsForApplication(final String applicationId) {
        checkDelegate();
        return delegate.getExceptionsForApplication(applicationId);
    }

    public List<Component> getComponents() {
        checkDelegate();
        return delegate.getComponents();
    }

    public List<String> getApplicationIds() {
        checkDelegate();
        return delegate.getApplicationIds();
    }

    public List<Exceptions<?>> getExceptions() {
        checkDelegate();
        return delegate.getExceptions();
    }

    private void checkDelegate() {
        if (delegate == null) {
            throw new ComponentRegistryUnavailableException();
        }
    }

    public Object addingService(final ServiceReference serviceReference) {
        final Object addedService = context.getService(serviceReference);
        if (ComponentRegistry.class.isAssignableFrom(addedService.getClass())) {
            delegate = (ComponentRegistry) addedService;
        }
        return addedService;
    }

    public void modifiedService(final ServiceReference serviceReference, final Object o) {
    }

    public void removedService(final ServiceReference serviceReference, final Object o) {
        delegate = null;
    }
}
