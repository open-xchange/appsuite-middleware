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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.OXAdminCoreImpl;
import com.openexchange.admin.rmi.impl.OXPublication;
import com.openexchange.admin.rmi.impl.OXTaskMgmtImpl;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.config.ConfigurationService;

/**
 * {@link AdminDaemon} - The admin daemon.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AdminDaemon implements AdminDaemonService {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AdminDaemon.class);

    private static final AtomicReference<PropertyHandler> prop = new AtomicReference<PropertyHandler>(null);

    private static final AtomicReference<AdminCache> cache = new AtomicReference<AdminCache>(null);

    private static final Set<Pattern> ALLOWED_BUNDLE_NAMES;

    static {
        class RegexHelper {
            Pattern wildcardPattern(final String wildcard) {
                final StringBuilder s = new StringBuilder(wildcard.length());
                s.append('^');
                final int len = wildcard.length();
                for (int i = 0; i < len; i++) {
                    final char c = wildcard.charAt(i);
                    if (c == '*') {
                        s.append(".*");
                    } else if (c == '?') {
                        s.append(".");
                    } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\') {
                        s.append('\\');
                        s.append(c);
                    } else {
                        s.append(c);
                    }
                }
                s.append('$');
                return Pattern.compile(s.toString());
            }

            Pattern literalPattern(final String literal) {
                return Pattern.compile(Pattern.quote(literal));
            }
        }
        final RegexHelper regexHelper = new RegexHelper();
        // Initialize set
        final Set<Pattern> set = new HashSet<Pattern>(64);
        set.add(regexHelper.literalPattern("com.openexchange.admin"));
        set.add(regexHelper.wildcardPattern("com.openexchange.admin.*"));
        set.add(regexHelper.wildcardPattern("org.osgi.*"));
        set.add(regexHelper.wildcardPattern("org.eclipse.equinox.*"));
        set.add(regexHelper.wildcardPattern("org.eclipse.osgi.*"));
        set.add(regexHelper.wildcardPattern("java.*"));
        set.add(regexHelper.wildcardPattern("javax.*"));
        // Others
        set.add(regexHelper.literalPattern("com.openexchange.caching"));
        set.add(regexHelper.literalPattern("com.openexchange.calendar"));
        set.add(regexHelper.literalPattern("com.openexchange.common"));
        set.add(regexHelper.literalPattern("com.openexchange.config.cascade"));
        set.add(regexHelper.literalPattern("com.openexchange.configread"));
        set.add(regexHelper.literalPattern("com.openexchange.control"));
        set.add(regexHelper.literalPattern("com.openexchange.conversion"));
        set.add(regexHelper.literalPattern("com.openexchange.crypto"));
        set.add(regexHelper.literalPattern("com.openexchange.dataretention"));
        set.add(regexHelper.literalPattern("com.openexchange.datatypes.genericonf.storage"));
        set.add(regexHelper.literalPattern("com.openexchange.datatypes.genericonf"));
        set.add(regexHelper.literalPattern("com.openexchange.file.storage.composition"));
        set.add(regexHelper.literalPattern("com.openexchange.file.storage"));
        set.add(regexHelper.literalPattern("com.openexchange.global"));
        set.add(regexHelper.literalPattern("com.openexchange.html"));
        set.add(regexHelper.literalPattern("com.openexchange.i18n"));
        set.add(regexHelper.literalPattern("com.openexchange.management"));
        set.add(regexHelper.literalPattern("com.openexchange.messaging.generic"));
        set.add(regexHelper.literalPattern("com.openexchange.messaging"));
        set.add(regexHelper.literalPattern("com.openexchange.monitoring"));
        set.add(regexHelper.literalPattern("com.openexchange.oauth"));
        set.add(regexHelper.literalPattern("com.openexchange.proxy"));
        set.add(regexHelper.literalPattern("com.openexchange.publish.basic"));
        set.add(regexHelper.literalPattern("com.openexchange.publish"));
        set.add(regexHelper.literalPattern("com.openexchange.push"));
        set.add(regexHelper.literalPattern("com.openexchange.secret.recovery"));
        set.add(regexHelper.literalPattern("com.openexchange.secret"));
        set.add(regexHelper.literalPattern("com.openexchange.server"));
        set.add(regexHelper.literalPattern("com.openexchange.sql"));
        set.add(regexHelper.literalPattern("com.openexchange.subscribe"));
        set.add(regexHelper.literalPattern("com.openexchange.threadpool"));
        set.add(regexHelper.literalPattern("com.openexchange.tx"));
        set.add(regexHelper.literalPattern("com.openexchange.user.copy"));
        set.add(regexHelper.literalPattern("com.openexchange.usm.api"));
        set.add(regexHelper.literalPattern("com.openexchange.usm.database.ox"));
        set.add(regexHelper.literalPattern("com.openexchange.usm.journal.impl"));
        set.add(regexHelper.literalPattern("com.openexchange.usm.journal"));
        set.add(regexHelper.literalPattern("com.openexchange.usm.util"));
        set.add(regexHelper.literalPattern("com.openexchange.xerces.sun"));
        set.add(regexHelper.literalPattern("com.openexchange.xml"));
        ALLOWED_BUNDLE_NAMES = Collections.unmodifiableSet(set);
    }

    /**
     * Checks if specified bundle is contained in list of allowed bundles.
     *
     * @param bundle The bundle to check
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public static boolean isAllowdBundle(final Bundle bundle) {
        return isAllowdBundle(bundle.getSymbolicName());
    }

    /**
     * Checks if specified symbolic name is contained in list of allowed bundles.
     *
     * @param symbolicName The symbolic name to check
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public static boolean isAllowdBundle(final String symbolicName) {
        for (final Pattern p : ALLOWED_BUNDLE_NAMES) {
            if (p.matcher(symbolicName).matches()) {
                return true;
            }
        }
        return false;
    }

    private final List<ServiceRegistration<Remote>> services = new ArrayList<ServiceRegistration<Remote>>();

    /*
     * Write changes to this list cannot happen at the same time as the BundleListener delivers events in order and not concurrently. So
     * there's no need to deal with concurrency here
     */
    static List<Bundle> bundlelist = new CopyOnWriteArrayList<Bundle>();

    /**
     * Checks if a simple check shall be performed in order to determine if a bundle is needed for admin to work:
     * <p>
     * Bundle is <b>not</b> a fragment bundle <small><b>AND</b></small> its state is <code>ACTIVE</code>.
     *
     * @return <code>true</code> if a simple check is sufficient; otherwise <code>false</code>
     */
    static boolean checkSimple() {
        return true;
    }

    /**
     * Checks if specified bundle is <b>not</b> a fragment bundle.
     *
     * @param bundle The bundle to check
     * @return <code>true</code> if specified bundle is <b>not</b> a fragment bundle; else <code>false</code>
     */
    public static boolean isNoFragment(final Bundle bundle) {
        return (null == bundle.getHeaders().get(Constants.FRAGMENT_HOST));
    }

    /**
     * Checks if specified bundle is <b>not</b> a fragment bundle <small><b>AND</b></small> its state is <code>ACTIVE</code>.
     *
     * @param bundle The bundle to check
     * @return <code>true</code> if specified bundle is <b>not</b> a fragment bundle <small><b>AND</b></small> its state is <code>ACTIVE</code>; else <code>false</code>
     */
    public static boolean isNoFragmentAndActive(final Bundle bundle) {
        return (isNoFragment(bundle) && (Bundle.ACTIVE == bundle.getState()));
    }

    /**
     * This method is used for initialization of the list of current running bundles. The problem is that the listener itself will not get
     * any events before this bundle is started, so if any bundles are started beforehand you won't notice this here. The consequence is
     * that we have to build an initial list on startup
     *
     * @param context
     */
    public void getCurrentBundleStatus(final BundleContext context) {
        for (final Bundle bundle : context.getBundles()) {
            if (checkSimple()) {
                if (isNoFragmentAndActive(bundle)) {
                    bundlelist.add(bundle);
                    LOG.debug("{} already started before admin.", bundle.getSymbolicName());
                }
            } else {
                if (bundle.getState() == Bundle.ACTIVE) {
                    if (isAllowdBundle(bundle)) {
                        bundlelist.add(bundle);
                        LOG.debug("{} already started before admin.", bundle.getSymbolicName());
                    }
                } else if (bundle.getState() == Bundle.RESOLVED && null != bundle.getHeaders().get(Constants.FRAGMENT_HOST)) {
                    if (isAllowdBundle(bundle)) {
                        bundlelist.add(bundle);
                        LOG.debug("fragment {} already started before admin.", bundle.getSymbolicName());
                    }
                }
            }
        }
    }

    public void registerBundleListener(final BundleContext context) {
        final BundleListener bl = new BundleListener() {

            @Override
            public void bundleChanged(final BundleEvent event) {
                if (event.getType() == BundleEvent.STARTED && (checkSimple() ? isNoFragment(event.getBundle()) : isAllowdBundle(event.getBundle()))) {
                    bundlelist.add(event.getBundle());
                } else if (event.getType() == BundleEvent.STOPPED) {
                    bundlelist.remove(event.getBundle());
                }
                LOG.debug("{} changed to {}", event.getBundle().getSymbolicName(), event.getType());
            }
        };
        context.addBundleListener(bl);
    }

    public synchronized static AdminCache initCache(final ConfigurationService service) throws OXGenericException {
        AdminCache cache = AdminDaemon.cache.get();
        if (cache == null) {
            if (null == service) {
                throw new OXGenericException("Absent service: " + ConfigurationService.class.getName());
            }
            cache = new AdminCache();
            AdminDaemon.cache.set(cache);
            cache.initCache(service);
            ClientAdminThread.cache = cache;
            prop.set(cache.getProperties());
            LOG.info("Cache and Pools initialized!");
        } else if (ClientAdminThread.cache == null) {
            ClientAdminThread.cache = cache;
        }
        return cache;
    }

    public static AdminCache getCache() throws OXGenericException {
        AdminCache cache = AdminDaemon.cache.get();
        if (cache == null) {
            ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
            if (null == service) {
                service = AdminCache.getConfigurationService();
            }
            cache = initCache(service);
        }
        return cache;
    }

    public void initAccessCombinationsInCache() throws ClassNotFoundException, OXGenericException {
        AdminDaemon.cache.get().initAccessCombinations();
    }

    /**
     * Initializes & registers the RMI stubs
     *
     * @param context The associated bundle context
     */
    public void initRMI(BundleContext context) {
        try {
            final com.openexchange.admin.rmi.impl.OXUser oxuser_v2 = new com.openexchange.admin.rmi.impl.OXUser(context);
            final com.openexchange.admin.rmi.impl.OXGroup oxgrp_v2 = new com.openexchange.admin.rmi.impl.OXGroup();
            final com.openexchange.admin.rmi.impl.OXResource oxres_v2 = new com.openexchange.admin.rmi.impl.OXResource();
            final com.openexchange.admin.rmi.impl.OXLogin oxlogin_v2 = new com.openexchange.admin.rmi.impl.OXLogin(context);
            final com.openexchange.admin.rmi.impl.OXUtil oxutil_v2 = new com.openexchange.admin.rmi.impl.OXUtil();
            final OXAdminCoreImpl oxadmincore = new OXAdminCoreImpl(context);
            final OXTaskMgmtImpl oxtaskmgmt = new OXTaskMgmtImpl();
            final OXPublication oxpublication = new OXPublication();

            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXUserInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxuser_v2, properties));

            properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXGroupInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxgrp_v2, properties));

            properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXResourceInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxres_v2, properties));

            properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXLoginInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxlogin_v2, properties));

            properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXAdminCoreInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxadmincore, properties));

            properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXTaskMgmtInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxtaskmgmt, properties));

            properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXPublicationInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxpublication, properties));

            properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXUtilInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxutil_v2, properties));
        } catch (final RemoteException e) {
            LOG.error("Error creating RMI registry!", e);
        } catch (final StorageException e) {
            LOG.error("Error while creating one instance for RMI interface", e);
        }
    }

    public void unregisterRMI(BundleContext context) {
        for (ServiceRegistration<Remote> registration : services) {
            context.ungetService(registration.getReference());
        }
    }

    public static PropertyHandler getProp() {
        return prop.get();
    }

    /**
     * Gets the list of known bundles.
     *
     * @return The bundle list
     * @deprecated User order look-up through utilizing a {@link ServiceTracker}
     */
    @Deprecated
    public static final List<Bundle> getBundlelist() {
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
     * @deprecated Do proper service tracking through utilizing a {@link ServiceTracker}
     */
    @Deprecated
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
                                LOG.error("Missing service {} in bundle {}", serviceName, bundleSymbolicName);
                            }
                            try {
                                return clazz.cast(obj);
                            } catch (final ClassCastException e) {
                                LOG.error("Service {}({}) in bundle {} cannot be cast to an instance of {}", serviceName, ((null != obj) ? obj.getClass().getName() : "null"), bundleSymbolicName, clazz.getName());
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
     * @deprecated Do proper service tracking through utilizing a {@link ServiceTracker}
     */
    @Deprecated
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
