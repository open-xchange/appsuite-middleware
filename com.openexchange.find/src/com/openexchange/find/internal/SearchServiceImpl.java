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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.ModuleConfig;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.SearchService;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.tools.session.ServerSession;


/**
 * The implementation of the {@link SearchService} interface.
 * Collects all {@link ModuleSearchDriver} implementations and
 * chooses an appropriate one on every request for a given module
 * and session.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SearchServiceImpl implements SearchService, ServiceTrackerCustomizer<ModuleSearchDriver, ModuleSearchDriver> {

    private final ConcurrentMap<Module, SortedSet<ComparableDriver>> driversByModule;

    private final BundleContext context;

    public SearchServiceImpl(final BundleContext context) {
        super();
        this.context = context;
        driversByModule = new ConcurrentHashMap<Module, SortedSet<ComparableDriver>>();
    }

    @Override
    public Map<Module, ModuleConfig> getConfiguration(ServerSession session) throws OXException {
        Map<Module, ModuleConfig> configs = new HashMap<Module, ModuleConfig>(driversByModule.size());
        for (Module module : driversByModule.keySet()) {
            ModuleSearchDriver driver = determineDriver(session, module);
            if (driver != null) {
                configs.put(module, driver.getConfiguration(session));
            }
        }

        return configs;
    }

    @Override
    public AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, Module module, ServerSession session) throws OXException {
        return requireDriver(session, module).autocomplete(autocompleteRequest, session);
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, Module module, ServerSession session) throws OXException {
        return requireDriver(session, module).search(searchRequest, session);
    }

    private ModuleSearchDriver requireDriver(ServerSession session, Module module) throws OXException {
        ModuleSearchDriver determined = determineDriver(session, module);
        if (determined == null) {
            throw FindExceptionCode.MISSING_DRIVER.create(module.name(), session.getUserId(), session.getContextId());
        }
        return determined;
    }

    private ModuleSearchDriver determineDriver(ServerSession session, Module module) throws OXException {
        ModuleSearchDriver determined = null;
        SortedSet<ComparableDriver> drivers = driversByModule.get(module);
        if (drivers != null) {
            for (ComparableDriver driver : drivers) {
                if (driver.getDriver().isValidFor(session)) {
                    determined = driver.getDriver();
                    break;
                }
            }
        }

        return determined;
    }

    /*
     * ServiceTrackerCustomizer implementations
     */

    @Override
    public ModuleSearchDriver addingService(ServiceReference<ModuleSearchDriver> reference) {
        ModuleSearchDriver driver = context.getService(reference);
        Module module = driver.getModule();
        SortedSet<ComparableDriver> drivers = driversByModule.get(module);
        if (drivers == null) {
            drivers = Collections.synchronizedSortedSet(new TreeSet<SearchServiceImpl.ComparableDriver>());
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
            if (driver == null) {
                if (other.driver != null) {
                    return false;
                }
            } else if (driver.getModule() != other.driver.getModule()) {
                return false;
            } else if (!driver.getClass().equals(other.driver.getClass())) {
                return false;
            }
            if (serviceRanking != other.serviceRanking) {
                return false;
            }
            return true;
        }

    }

}
