/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.appsuite.osgi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.appsuite.AppSuiteLoginRampUp;
import com.openexchange.appsuite.AppsLoadServlet;
import com.openexchange.appsuite.FileCacheProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Reloadable;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link AppsuiteActivator} - The activator for <code>"com.openexchange.appsuite"</code> bundle.
 */
public class AppsuiteActivator extends HousekeepingActivator implements ForcedReloadable {

    private AppsLoadServlet appsLoadServlet;
    private String alias;
    private final boolean isWindows = Optional.ofNullable(System.getProperty("os.name")).orElse("").toLowerCase().startsWith("windows");

    /**
     * Initializes a new {@link AppsuiteActivator}.
     */
    public AppsuiteActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, ConfigurationService.class, DispatcherPrefixService.class, Dispatcher.class, ThreadPoolService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AppsuiteActivator.class);

        // Get needed configuration service
        ConfigurationService config = getService(ConfigurationService.class);

        // Read apps and zone information
        File[] apps = getApps(config);
        File zoneinfo = new File(config.getProperty("com.openexchange.apps.tzdata", "/usr/share/zoneinfo/"));

        // Track FileCacheProvider
        final RankingAwareNearRegistryServiceTracker<FileCacheProvider> fileCacheProviderTracker = new RankingAwareNearRegistryServiceTracker<FileCacheProvider>(context, FileCacheProvider.class);
        rememberTracker(fileCacheProviderTracker);

        openTrackers();

        // Initialize Servlet
        AppsLoadServlet appsLoadServlet = new AppsLoadServlet(fileCacheProviderTracker);
        appsLoadServlet.reinitialize(apps, zoneinfo);
        this.appsLoadServlet = appsLoadServlet;

        // Register as reloadable
        registerService(Reloadable.class, this);

        // Register Servlet
        {
            String prefix = getService(DispatcherPrefixService.class).getPrefix();
            String alias = prefix + "apps/load";
            getService(HttpService.class).registerServlet(alias, appsLoadServlet, null, null);
            this.alias = alias;

            StringBuilder sb = new StringBuilder(128);
            for (File app : apps) {
                sb.append(':');
                sb.append(app.getPath());
            }
            logger.info("Servlet path \"apps/load\" successfully registered with \"apps\"={} and \"zoneinfo\"={}", sb.substring(1), zoneinfo.getPath());
        }

        // Register ramp-up service
        registerService(LoginRampUpService.class, new AppSuiteLoginRampUp(this));
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            String alias = this.alias;
            if (null != alias) {
                this.alias = null;
                HttpServices.unregister(alias, httpService);
            }
        }
        super.stopBundle();
    }

    private File[] getApps(ConfigurationService config) throws BundleException {
        String property = config.getProperty("com.openexchange.apps.path");
        if (property == null) {
            throw new BundleException("Missing property: com.openexchange.apps.path", BundleException.ACTIVATOR_ERROR);
        }

        String[] paths = property.split(":");
        List<File> apps = new ArrayList<File>(paths.length);
        Optional<String> winPrefix = Optional.empty();
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            if(winPrefix.isPresent()) {
                path = winPrefix.get() + ":" + path;
                winPrefix = Optional.empty();
            }
            File parent = new File(path);
            if(parent.exists() == false && isWindows && path.length() == 1) {
                // probably the drive name (e.g. the c of c:/folder/
                winPrefix = Optional.of(path);
                continue;
            }
            apps.add(path.endsWith("apps") ? parent : new File(parent, "apps"));
        }

        return apps.toArray(new File[apps.size()]);
    }

    // ----------------------------------------------------------------------------------------------------------------------

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        AppsLoadServlet appsLoadServlet = this.appsLoadServlet;
        if (null != appsLoadServlet) {
            try {
                // Read apps and zone information
                File[] apps = getApps(configService);
                File zoneinfo = new File(configService.getProperty("com.openexchange.apps.tzdata", "/usr/share/zoneinfo/"));

                // Reinitialize Servlet
                appsLoadServlet.reinitialize(apps, zoneinfo);
            } catch (Exception e) {
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AppsuiteActivator.class);
                logger.error("Failed to reinitialize {}", AppsLoadServlet.class.getSimpleName(), e);
            }
        }
    }
}
