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

package com.openexchange.find.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.facet.FacetInfo;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.find.spi.SearchConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SearchDriverManager implements ServiceTrackerCustomizer<ModuleSearchDriver, ModuleSearchDriver> {

    private static final String PROP_DISABLED_MODULES = "com.openexchange.find.disabledModules";

    private final ConcurrentMap<Module, SortedSet<ComparableDriver>> driversByModule;

    private final Cache<String, List<CachedConfig>> configCache;

    private final BundleContext context;

    private final ConfigViewFactory configViewFactory;

    public SearchDriverManager(final BundleContext context, final ConfigViewFactory configViewFactory) {
        super();
        this.context = context;
        this.configViewFactory = configViewFactory;
        driversByModule = new ConcurrentHashMap<Module, SortedSet<ComparableDriver>>();
        configCache = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.SECONDS).build();
    }

    /**
     * Determines the appropriate search driver for the given session and module.
     *
     * @param session The users session.
     * @param module The module to check.
     * @param lookUpInfo The look-up information, or {@link LookUpInfo#EMPTY} if not relevant
     * @param failOnMissingPermission Whether to throw an exception if the user is not even allowed
     * to search within the given module. If <code>false</code>, <code>null</code> is returned in that case.
     * @return The driver or <code>null</code> if no valid one is available.
     */
    public ModuleSearchDriver determineDriver(ServerSession session, Module module, LookUpInfo lookUpInfo, boolean failOnMissingPermission) throws OXException {
        ModuleSearchDriver determined = null;
        if (hasModulePermission(session, module)) {
            SortedSet<ComparableDriver> drivers = driversByModule.get(module);
            if (drivers != null) {
                AbstractFindRequest findRequest = lookUpInfo.getFindRequest();
                List<FacetInfo> facetInfos = lookUpInfo.getFacetInfos();
                for (ComparableDriver driver : drivers) {
                    if (null != findRequest && driver.getDriver().isValidFor(session, findRequest)) {
                        determined = driver.getDriver();
                        break;
                    }
                    if (null == facetInfos ? driver.getDriver().isValidFor(session) : driver.getDriver().isValidFor(session, facetInfos)) {
                        determined = driver.getDriver();
                        break;
                    }
                }
            }
        } else if (failOnMissingPermission) {
            throw FindExceptionCode.MODULE_DISABLED.create(module.getIdentifier(), I(session.getUserId()), I(session.getContextId()));
        }

        return determined;
    }

    /**
     * Determines all appropriate search drivers for the given session.
     *
     * @param session The users session.
     * @param module The module to check.
     * @return A list of drivers valid for the sessions user.
     */
    public List<ModuleSearchDriver> determineDrivers(ServerSession session) throws OXException {
        List<ModuleSearchDriver> drivers = new LinkedList<ModuleSearchDriver>();
        for (Module module : Module.values()) {
            ModuleSearchDriver driver = determineDriver(session, module, LookUpInfo.EMPTY, false);
            if (driver != null) {
                drivers.add(driver);
            }
        }

        return drivers;
    }

    private boolean hasModulePermission(ServerSession session, Module module) throws OXException {
        ConfigView configView = configViewFactory.getView(session.getUserId(), session.getContextId());
        List<String> disabledModules = Strings.splitAndTrim(configView.opt(PROP_DISABLED_MODULES, String.class, ""), ",");
        if (disabledModules.contains(module.getIdentifier())) {
            return false;
        }

        return true;
    }

    /**
     * Gets all module specific {@link SearchConfiguration}s for the given session.
     * Configurations are cached for 30 seconds, so subsequent calls are cheap within that
     * time frame.
     */
    public List<CachedConfig> getConfigurations(final ServerSession session) throws OXException {
        try {
            return configCache.get(session.getSessionID(), new Callable<List<CachedConfig>>() {
                @Override
                public List<CachedConfig> call() throws OXException {
                    List<ModuleSearchDriver> drivers = determineDrivers(session);
                    List<CachedConfig> configs = new ArrayList<CachedConfig>(drivers.size());
                    for (ModuleSearchDriver driver : drivers) {
                        SearchConfiguration searchConfig = driver.getSearchConfiguration(session);
                        configs.add(new CachedConfig(driver.getModule(), searchConfig));
                    }

                    return configs;
                }
            });
        } catch(ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }

            throw new OXException(cause);
        }
    }

    @Override
    public ModuleSearchDriver addingService(ServiceReference<ModuleSearchDriver> reference) {
        ModuleSearchDriver driver = context.getService(reference);
        Module module = driver.getModule();
        SortedSet<ComparableDriver> drivers = driversByModule.get(module);
        if (drivers == null) {
            drivers = Collections.synchronizedSortedSet(new TreeSet<ComparableDriver>());
            SortedSet<ComparableDriver> meantime = driversByModule.putIfAbsent(module, drivers);
            if (meantime != null) {
                drivers = meantime;
            }
        }

        drivers.add(new ComparableDriver(driver, getRanking(reference)));
        return driver;
    }

    @Override
    public void modifiedService(ServiceReference<ModuleSearchDriver> reference, ModuleSearchDriver driver) {
        // nothing to do
    }

    @Override
    public void removedService(ServiceReference<ModuleSearchDriver> reference, ModuleSearchDriver driver) {
        Module module = driver.getModule();
        SortedSet<ComparableDriver> drivers = driversByModule.get(module);
        if (drivers != null) {
            drivers.remove(new ComparableDriver(driver, getRanking(reference)));
        }
    }

    private int getRanking(ServiceReference<ModuleSearchDriver> reference) {
        int ranking = 0;
        Object rankingObj = reference.getProperty(Constants.SERVICE_RANKING);
        if (rankingObj != null && rankingObj instanceof Integer) {
            ranking = ((Integer) rankingObj).intValue();
        }

        return ranking;
    }

    private static final class ComparableDriver implements Comparable<ComparableDriver> {

        private final ModuleSearchDriver driver;

        private final int serviceRanking;

        public ComparableDriver(final ModuleSearchDriver driver, final int serviceRanking) {
            super();
            this.driver = driver;
            this.serviceRanking = serviceRanking;
        }

        public ModuleSearchDriver getDriver() {
            return driver;
        }


        public int getServiceRanking() {
            return serviceRanking;
        }

        @Override
        public int compareTo(ComparableDriver o) {
            return o.getServiceRanking() - serviceRanking;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((driver == null) ? 0 : driver.getClass().getName().hashCode());
            result = prime * result + ((driver == null) ? 0 : driver.getModule().ordinal());
            result = prime * result + serviceRanking;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ComparableDriver other = (ComparableDriver) obj;
            if (serviceRanking != other.serviceRanking) {
                return false;
            }
            if (driver == null) {
                if (other.driver != null) {
                    return false;
                }
            } else if (driver.getModule() != other.driver.getModule()) {
                return false;
            } else if (!driver.getClass().equals(other.driver.getClass())) {
                return false;
            }
            return true;
        }
    }

    static final class CachedConfig {

        private final Module module;

        private final SearchConfiguration searchConfig;


        public CachedConfig(Module module, SearchConfiguration searchConfig) {
            super();
            this.module = module;
            this.searchConfig = searchConfig;
        }

        public Module getModule() {
            return module;
        }

        public SearchConfiguration getSearchConfig() {
            return searchConfig;
        }
    }

}
