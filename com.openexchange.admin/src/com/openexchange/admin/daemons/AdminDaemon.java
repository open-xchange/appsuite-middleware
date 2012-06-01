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

package com.openexchange.admin.daemons;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.OXAdminCoreInterface;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.OXLoginInterface;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.OXAdminCoreImpl;
import com.openexchange.admin.rmi.impl.OXTaskMgmtImpl;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.log.LogFactory;

public class AdminDaemon {

    static final Log LOG = LogFactory.getLog(AdminDaemon.class);

    private static PropertyHandler prop = null;

    private AdminCache cache = null;

    private final List<ServiceRegistration<Remote>> services = new ArrayList<ServiceRegistration<Remote>>();

    /*
     * Write changes to this list cannot happen at the same time as the BundleListener delivers events in order and not concurrently. So
     * there's no need to deal with concurrency here
     */
    static ArrayList<Bundle> bundlelist = new ArrayList<Bundle>();

    private static com.openexchange.admin.rmi.impl.OXUser oxuser_v2 = null;

    private static com.openexchange.admin.rmi.impl.OXGroup oxgrp_v2 = null;

    private static com.openexchange.admin.rmi.impl.OXResource oxres_v2 = null;

    private static com.openexchange.admin.rmi.impl.OXLogin oxlogin_v2 = null;

    private static OXAdminCoreImpl oxadmincore = null;

    private static OXTaskMgmtImpl oxtaskmgmt = null;

    /**
     * This method is used for initialization of the list of current running bundles. The problem is that the listener itself will not get
     * any events before this bundle is started, so if any bundles are started beforehand you won't notice this here. The consequence is
     * that we have to build an initial list on startup
     * 
     * @param context
     */
    public void getCurrentBundleStatus(BundleContext context) {
        for (final Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                bundlelist.add(bundle);
                if (LOG.isInfoEnabled()) {
                    LOG.info(bundle.getSymbolicName() + " already started before admin.");
                }
            } else if (bundle.getState() == Bundle.RESOLVED && null != bundle.getHeaders().get(Constants.FRAGMENT_HOST)) {
                bundlelist.add(bundle);
                if (LOG.isInfoEnabled()) {
                    LOG.info("fragment " + bundle.getSymbolicName() + " already started before admin.");
                }
            }
        }
    }

    public void registerBundleListener(final BundleContext context) {
        final BundleListener bl = new BundleListener() {

            public void bundleChanged(final BundleEvent event) {
                if (event.getType() == BundleEvent.STARTED) {
                    bundlelist.add(event.getBundle());
                } else if (event.getType() == BundleEvent.STOPPED) {
                    bundlelist.remove(event.getBundle());
                }
                LOG.debug(event.getBundle().getSymbolicName() + " changed to " + event.getType());
            }
        };
        context.addBundleListener(bl);
    }

    public void initCache() throws OXGenericException {
        this.cache = new AdminCache();

        this.cache.initCache();
        ClientAdminThread.cache = this.cache;
        prop = this.cache.getProperties();
        LOG.info("Cache and Pools initialized!");
    }

    public void initAccessCombinationsInCache() throws ClassNotFoundException, OXGenericException {
        this.cache.initAccessCombinations();
    }

    public void initRMI(final BundleContext context) {
        try {
            // Export all NEW Objects
            oxuser_v2 = new com.openexchange.admin.rmi.impl.OXUser(context);
            final OXUserInterface oxuser_stub_v2 = (OXUserInterface) UnicastRemoteObject.exportObject(oxuser_v2, 0);

            oxgrp_v2 = new com.openexchange.admin.rmi.impl.OXGroup(context);
            final OXGroupInterface oxgrp_stub_v2 = (OXGroupInterface) UnicastRemoteObject.exportObject(oxgrp_v2, 0);

            oxres_v2 = new com.openexchange.admin.rmi.impl.OXResource(context);
            final OXResourceInterface oxres_stub_v2 = (OXResourceInterface) UnicastRemoteObject.exportObject(oxres_v2, 0);

            oxlogin_v2 = new com.openexchange.admin.rmi.impl.OXLogin(context);
            final OXLoginInterface oxlogin_stub_v2 = (OXLoginInterface) UnicastRemoteObject.exportObject(oxlogin_v2, 0);

            oxadmincore = new OXAdminCoreImpl(context);
            final OXAdminCoreInterface oxadmincore_stub = (OXAdminCoreInterface) UnicastRemoteObject.exportObject(oxadmincore, 0);

            oxtaskmgmt = new OXTaskMgmtImpl();
            final OXTaskMgmtInterface oxtaskmgmt_stub = (OXTaskMgmtInterface) UnicastRemoteObject.exportObject(oxtaskmgmt, 0);
            // END of NEW export

            // bind all NEW Objects to registry
            services.add(context.registerService(Remote.class, oxuser_stub_v2, null));
            services.add(context.registerService(Remote.class, oxgrp_stub_v2, null));
            services.add(context.registerService(Remote.class, oxres_stub_v2, null));
            services.add(context.registerService(Remote.class, oxlogin_stub_v2, null));
            services.add(context.registerService(Remote.class, oxadmincore_stub, null));
            services.add(context.registerService(Remote.class, oxtaskmgmt_stub, null));

        } catch (final RemoteException e) {
            LOG.fatal("Error creating RMI registry!", e);
        } catch (final StorageException e) {
            LOG.fatal("Error while creating one instance for RMI interface", e);
        }
    }

    public void unregisterRMI(BundleContext context) {
        for (ServiceRegistration<Remote> registration : services) {
            context.ungetService(registration.getReference());
        }
    }

    public static PropertyHandler getProp() {
        return prop;
    }

    public static final ArrayList<Bundle> getBundlelist() {
        return bundlelist;
    }

    /**
     * Looks for a matching service reference inside all bundles provided through {@link #getBundlelist()}.
     * 
     * @param <S> Type of the service
     * @param bundleSymbolicName The bundle's symbolic name which offers the service
     * @param serviceName The service's name provided through "<i>name</i>" property
     * @param context The bundle context (on which {@link BundleContext#getService(ServiceReference)} is invoked)
     * @param clazz The service's class
     * @return The service if found; otherwise <code>null</code>
     */
    public static final <S extends Object> S getService(final String bundleSymbolicName, final String serviceName, final BundleContext context, final Class<? extends S> clazz) {
        for (final Bundle bundle : bundlelist) {
            if (bundle.getState() == Bundle.ACTIVE && bundleSymbolicName.equals(bundle.getSymbolicName())) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase(serviceName)) {
                            final Object obj = context.getService(servicereference);
                            if (null == obj) {
                                LOG.error("Missing service " + serviceName + " in bundle " + bundleSymbolicName);
                            }
                            try {
                                return clazz.cast(obj);
                            } catch (final ClassCastException e) {
                                LOG.error("Service " + serviceName + "(" + obj.getClass().getName() + ") in bundle " + bundleSymbolicName + " cannot be cast to an instance of " + clazz.getName());
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Ungets the service identified through given bundle's symbolic name and "<i>name</i>" property.
     * 
     * @param bundleSymbolicName The bundle's symbolic name which offers the service
     * @param serviceName The service's name provided through "<i>name</i>" property
     * @param context The bundle context (on which {@link BundleContext#ungetService(ServiceReference)} is invoked)
     */
    public static final void ungetService(final String bundleSymbolicName, final String serviceName, final BundleContext context) {
        for (final Bundle bundle : bundlelist) {
            if (bundle.getState() == Bundle.ACTIVE && bundleSymbolicName.equals(bundle.getSymbolicName())) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase(serviceName)) {
                            context.ungetService(servicereference);
                        }
                    }
                }
            }
        }
    }
}
