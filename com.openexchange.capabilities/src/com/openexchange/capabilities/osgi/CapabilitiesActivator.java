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

package com.openexchange.capabilities.osgi;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.ServiceReference;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.groupware.CapabilityCreateTableService;
import com.openexchange.capabilities.groupware.CapabilityCreateTableTask;
import com.openexchange.capabilities.groupware.CapabilityDeleteListener;
import com.openexchange.capabilities.internal.CapabilityServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;

public class CapabilitiesActivator extends HousekeepingActivator {

    /** The service look-up */
    public static final AtomicReference<ServiceLookup> SERVICES = new AtomicReference<ServiceLookup>();

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DatabaseService.class, TimerService.class, CacheService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        SERVICES.set(this);
        final CapabilityCheckerRegistry capCheckers = new CapabilityCheckerRegistry(context);
        rememberTracker(capCheckers);

        /*
         * Create & register CapabilityService
         */
        final CapabilityServiceImpl capService = new CapabilityServiceImpl(this) {

            @Override
            public List<CapabilityChecker> getCheckers() {
                return capCheckers.getCheckers();
            }
        };
        registerService(CapabilityService.class, capService);

        track(Capability.class, new SimpleRegistryListener<Capability>() {

            @Override
            public void added(ServiceReference<Capability> ref, Capability service) {
                capService.getCapability(service.getId()).learnFrom(service);
            }

            @Override
            public void removed(ServiceReference<Capability> ref, Capability service) {
                // Nothing
            }

        });

        /*
         * Register update task, create table job and delete listener
         */
        registerService(CreateTableService.class, new CapabilityCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CapabilityCreateTableTask()));
        registerService(DeleteListener.class, new CapabilityDeleteListener());

        /*
         * Define cache regions
         */
        {
            final String regionName = "CapabilitiesContext";
            final byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                    "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                    "jcs.region."+regionName+".cacheattributes.MaxObjects=10000\n" +
                    "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                    "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
                    "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds=300\n" +
                    "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds=60\n" +
                    "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                    "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                    "jcs.region."+regionName+".elementattributes.MaxLifeSeconds=-1\n" +
                    "jcs.region."+regionName+".elementattributes.IdleTime=300\n" +
                    "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                    "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                    "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf));
        }
        {
            final String regionName = "CapabilitiesUser";
            final byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                    "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                    "jcs.region."+regionName+".cacheattributes.MaxObjects=1000000\n" +
                    "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                    "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
                    "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds=300\n" +
                    "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds=60\n" +
                    "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                    "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                    "jcs.region."+regionName+".elementattributes.MaxLifeSeconds=-1\n" +
                    "jcs.region."+regionName+".elementattributes.IdleTime=300\n" +
                    "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                    "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                    "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf));
        }

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        SERVICES.set(null);
        final CacheService cacheService = getService(CacheService.class);
        if (null != cacheService) {
            cacheService.freeCache("CapabilitiesContext");
            cacheService.freeCache("CapabilitiesUser");
        }
        super.stopBundle();
    }

}
