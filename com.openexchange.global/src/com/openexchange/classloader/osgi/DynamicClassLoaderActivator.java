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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.openexchange.classloader.osgi;

import java.util.Hashtable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.classloader.DynamicClassLoaderManager;
import com.openexchange.classloader.impl.DynamicClassLoaderManagerFactory;

/**
 * Originally taken from <a href="from http://sling.apache.org/site/apache-sling.html">Apache Sling</a>.
 * <p>
 * &nbsp;&nbsp;<img src="http://sling.apache.org/site/media.data/logo.png">
 * <p>
 * <p>
 * &nbsp;&nbsp;<img src="http://sling.apache.org/site/media.data/logo.png">
 * <p>
 * This activator registers the dynamic class loader manager. It listens for bundle events and reregisters the class loader manager if a
 * bundle event for a used bundle occurs.
 */
public class DynamicClassLoaderActivator implements SynchronousBundleListener, BundleActivator {

    /** A service tracker for the package admin. */
    private volatile ServiceTracker<PackageAdmin, PackageAdmin> packageAdminTracker;

    /** The service registration for the dynamic class loader manager. */
    private volatile ServiceRegistration<?> serviceReg;

    /** The dynamic class loader service factory. */
    private volatile DynamicClassLoaderManagerFactory service;

    /** The bundle context. */
    private volatile BundleContext bundleContext;

    /**
     * Initializes a new {@link DynamicClassLoaderActivator}.
     */
    public DynamicClassLoaderActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) {
        this.bundleContext = context;

        final ServiceTracker<PackageAdmin, PackageAdmin> packageAdminTracker =
            new ServiceTracker<PackageAdmin, PackageAdmin>(context, PackageAdmin.class, null);
        packageAdminTracker.open();
        this.packageAdminTracker = packageAdminTracker;

        // register service
        this.registerManagerFactory();
        context.addBundleListener(this);
    }

    /**
     * Register the dynamic class loader manager factory.
     */
    protected void registerManagerFactory() {
        final Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(Constants.SERVICE_DESCRIPTION, "Dynamic Class Loader Service");
        props.put(Constants.SERVICE_VENDOR, "The Apache Software Foundation");
        final DynamicClassLoaderManagerFactory service =
            new DynamicClassLoaderManagerFactory(this.bundleContext, this.packageAdminTracker.getService());
        this.service = service;
        this.serviceReg = bundleContext.registerService(new String[] {DynamicClassLoaderManager.class.getName()}, service, props);
    }

    /**
     * Unregister the dynamic class loader manager factory.
     */
    protected void unregisterManagerFactory() {
        final ServiceRegistration<?> serviceReg = this.serviceReg;
        if (serviceReg != null) {
            serviceReg.unregister();
            this.serviceReg = null;
        }
        final DynamicClassLoaderManagerFactory service = this.service;
        if (service != null) {
            this.service = null;
        }
    }

    @Override
    public void stop(final BundleContext context) {
        context.removeBundleListener(this);
        this.unregisterManagerFactory();
        final ServiceTracker<PackageAdmin, PackageAdmin> packageAdminTracker = this.packageAdminTracker;
        if (packageAdminTracker != null) {
            packageAdminTracker.close();
            this.packageAdminTracker = null;
        }
        this.bundleContext = null;
    }

    @Override
    public void bundleChanged(final BundleEvent event) {
        final boolean reload;
        final int type = event.getType();
        if (type == BundleEvent.RESOLVED || type == BundleEvent.STARTED) {
            final Bundle bundle = event.getBundle();
            reload = this.service.isBundleUsed(bundle.getBundleId()) || this.service.hasUnresolvedPackages(bundle);
        } else if (type == BundleEvent.UNRESOLVED) {
            reload = this.service.isBundleUsed(event.getBundle().getBundleId());
        } else {
            reload = false;
        }
        if (reload) {
            this.unregisterManagerFactory();
            this.registerManagerFactory();
        }
    }
}
