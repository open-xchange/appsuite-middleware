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

package com.openexchange.control.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.groupware.update.Updater;
import com.openexchange.java.Streams;
import com.openexchange.version.Version;

/**
 * {@link GeneralControl} - Provides several methods to manage OSGi application.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GeneralControl implements GeneralControlMBean, MBeanRegistration {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GeneralControl.class);

    private static final String INITIAL_LOCATION = "initial@";

    private final BundleContext bundleContext;
    private MBeanServer server;

    /**
     * Initializes a new {@link GeneralControl}.
     *
     * @param bundleContext The associated bundle context
     */
    public GeneralControl(final BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
    }

    private Bundle getBundleByName(final String name, final Bundle[] bundle) {
        if (null == name) {
            return null;
        }

        for (Bundle element : bundle) {
            if (name.equals(element.getSymbolicName())) {
                return element;
            }
        }
        return null;
    }

    @Override
    public List<Map<String, String>> list() {
        LOG.info("control command: list");
        Bundle[] bundles = bundleContext.getBundles();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>(bundles.length);
        for (Bundle bundle : bundles) {
            Map<String, String> map = new HashMap<String, String>(4);
            map.put("bundlename", bundle.getSymbolicName());
            map.put("status", resolvState(bundle.getState()));
            list.add(map);
        }

        return list;
    }

    @Override
    public void start(final String name) throws MBeanException {
        LOG.info("control command: start bundle {}", name);
        Bundle bundle = getBundleByName(name, bundleContext.getBundles());
        if (bundle == null) {
            throw new MBeanException(null, "bundle " + name + " not found");
        }
        try {
            bundle.start();
        } catch (final BundleException exc) {
            LOG.error("cannot start bundle: {}", name, exc);
            String message = exc.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void stop(final String name) throws MBeanException {
        LOG.info("control command: stop bundle {}", name);
        Bundle bundle = getBundleByName(name, bundleContext.getBundles());
        if (bundle == null) {
            throw new MBeanException(null, "bundle " + name + " not found");
        }
        try {
            bundle.stop();
        } catch (final BundleException exc) {
            LOG.error("cannot stop bundle: {}", name, exc);
            String message = exc.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void restart(final String name) throws MBeanException {
        stop(name);
        start(name);
    }

    @Override
    public void install(final String location) throws MBeanException {
        LOG.info("install bundle: {}", location);
        try {
            bundleContext.installBundle(location);
        } catch (final BundleException exc) {
            LOG.error("cannot install bundle: {}", location, exc);
            String message = exc.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void uninstall(final String name) throws MBeanException {
        LOG.info("uninstall bundle: {}", name);
        Bundle bundle = getBundleByName(name, bundleContext.getBundles());
        if (bundle == null) {
            throw new MBeanException(null, "bundle " + name + " not found");
        }
        try {
            bundle.uninstall();
        } catch (final BundleException exc) {
            LOG.error("cannot uninstall bundle: {}", name, exc);
            String message = exc.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void update(final String name, final boolean autofresh) throws MBeanException {
        LOG.info("control command: update bundle: {}", name);
        Bundle bundle = getBundleByName(name, bundleContext.getBundles());
        if (bundle == null) {
            throw new MBeanException(null, "bundle " + name + " not found");
        }

        InputStream in = null;
        try {
            String location = bundle.getLocation();
            if (location.startsWith(INITIAL_LOCATION)) {
                // Prepended by Equinox
                location = location.substring(INITIAL_LOCATION.length());
            }
            in = new URL(location).openStream();

            bundle.update(in);
            if (autofresh) {
                freshPackages(bundleContext);
            }
        } catch (final BundleException exc) {
            LOG.error("cannot update bundle: {}", name, exc);
            String message = exc.getMessage();
            throw new MBeanException(new Exception(message), message);
        } catch (MalformedURLException exc) {
            LOG.error("cannot update bundle: {}", name, exc);
            String message = exc.getMessage();
            throw new MBeanException(new Exception(message), message);
        } catch (IOException exc) {
            LOG.error("cannot update bundle: {}", name, exc);
            String message = exc.getMessage();
            throw new MBeanException(new Exception(message), message);
        } catch (RuntimeException exc) {
            LOG.error("cannot update bundle: {}", name, exc);
            String message = exc.getMessage();
            throw new MBeanException(new Exception(message), message);
        } finally {
            Streams.close(in);
        }
    }

    @Override
    public void refresh() {
        LOG.info("control command: refresh");
        freshPackages(bundleContext);
    }

    @Override
    public boolean shutdown() {
        LOG.info("control command: shutdown");
        return shutdown(bundleContext, false);
    }

    @Override
    public boolean shutdown(boolean waitForExit) {
        LOG.info("control command: shutdown");
        return shutdown(bundleContext, waitForExit);
    }

    /**
     * Shutdown of active bundles through closing system bundle
     *
     * @param bundleContext The bundle context
     * @param waitForExit <code>true</code> to wait for the OSGi framework being shut down completely; otherwise <code>false</code>
     * @return <code>true</code> if the shutdown did complete successfully. This is only valid if waitForExit parameter is set to
     *         <code>true</code>.
     */
    public static boolean shutdown(BundleContext bundleContext, boolean waitForExit) {
        boolean completed = false;
        try {
            /*
             * Simply shut-down the system bundle to enforce invocation of close() method on all running bundles
             */
            Bundle systemBundle = getSystemBundleSafe(bundleContext);
            if (null != systemBundle && systemBundle.getState() == Bundle.ACTIVE) {
                LOG.info("Stopping system bundle...");
                // Note that stopping process is done in a separate thread
                systemBundle.stop();
                if (waitForExit) {
                    // Wait on condition. The BundleListener for the system bundle does not work reliably therefore we have to poll the
                    // state.
                    long timeout = System.currentTimeMillis() + 120 * 1000;
                    while (!completed && System.currentTimeMillis() < timeout) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            LOG.error("", e);
                        }
                        completed = Bundle.RESOLVED == systemBundle.getState();
                    }
                }
            }
        } catch (BundleException e) {
            LOG.error("", e);
        }
        return completed;
    }

    private static Bundle getSystemBundleSafe(BundleContext bundleContext) {
        try {
            return bundleContext.getBundle(0);
        } catch (Exception e) {
            LOG.debug("Unable to acquire system bundle", e);
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> services() {
        LOG.info("control command: services");
        final List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();

        ServiceReference[] services;
        try {
            /*
             * Null parameters to get all services from BundleContext.getServiceReferences(String clazz, String filter);
             */
            final String clazz = null;
            final String filter = null;
            services = bundleContext.getServiceReferences(clazz, filter);
            if (services != null) {
                final int size = services.length;
                if (size > 0) {
                    for (int j = 0; j < size; j++) {
                        final Map<String, Object> hashMap = new HashMap<String, Object>();

                        final ServiceReference service = services[j];

                        hashMap.put("service", service.toString());
                        hashMap.put("registered_by", service.getBundle().toString());

                        final Bundle[] usedByBundles = service.getUsingBundles();
                        final List<String> bundleList = new ArrayList<String>();
                        if (usedByBundles != null) {
                            for (Bundle usedByBundle : usedByBundles) {
                                final String bundleName = usedByBundle.getSymbolicName();
                                if (bundleName != null) {
                                    bundleList.add(bundleName);
                                }
                            }
                        }

                        if (bundleList.size() > 0) {
                            hashMap.put("bundles", bundleList);
                        }

                        serviceList.add(hashMap);
                    }
                }
            }
        } catch (final InvalidSyntaxException exc) {
            LOG.error("", exc);
        }

        return serviceList;
    }

    @Override
    public String version() {
        return Version.getInstance().getVersionString();
    }

    @Override
    public boolean updateTasksRunning() {
        return updateTasksInProgess() || configDBMigrationsRunning();
    }

    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName nameArg) throws Exception {
        ObjectName name = nameArg;
        if (name == null) {
            name = new ObjectName(new StringBuilder(server.getDefaultDomain()).append(":name=").append(this.getClass().getName()).toString());
        }
        this.server = server;
        return name;
    }

    @Override
    public void postRegister(final Boolean registrationDone) {
        LOG.trace("postRegister() with {}", registrationDone);
    }

    @Override
    public void preDeregister() throws Exception {
        LOG.trace("preDeregister()");
    }

    @Override
    public void postDeregister() {
        LOG.trace("postDeregister()");
    }

    public Integer getNbObjects() {
        try {
            return Integer.valueOf((server.queryMBeans(new ObjectName("*:*"), null)).size());
        } catch (final Exception e) {
            return Integer.valueOf(-1);
        }
    }

    private boolean updateTasksInProgess() {
        return !Updater.getInstance().getLocallyScheduledTasks().isEmpty();
    }

    private boolean configDBMigrationsRunning() {
        ServiceReference<DBMigrationExecutorService> ref = bundleContext.getServiceReference(DBMigrationExecutorService.class);
        if (ref != null) {
            DBMigrationExecutorService migrationExecutor = bundleContext.getService(ref);
            if (migrationExecutor != null) {
                try {
                    return migrationExecutor.migrationsRunning();
                } finally {
                    bundleContext.ungetService(ref);
                }
            }
        }

        return false;
    }

    private static String resolvState(final int state) {
        // TODO: add all states
        switch (state) {
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.STOPPING:
                return "STOPPING";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "UNKNOWN";
        }
    }

    protected static void freshPackages(final BundleContext bundleContext) {
        final ServiceReference serviceReference = bundleContext.getServiceReference("org.osgi.service.packageadmin.PackageAdmin");
        final PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(serviceReference);
        packageAdmin.refreshPackages(null);
    }

}
