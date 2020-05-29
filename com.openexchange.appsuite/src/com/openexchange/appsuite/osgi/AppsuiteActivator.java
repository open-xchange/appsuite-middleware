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
                httpService.unregister(alias);
                this.alias = null;
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
