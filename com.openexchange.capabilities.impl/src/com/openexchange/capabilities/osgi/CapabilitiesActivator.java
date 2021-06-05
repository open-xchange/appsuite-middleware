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

package com.openexchange.capabilities.osgi;

import static com.openexchange.capabilities.internal.AbstractCapabilityService.getCapability;
import static com.openexchange.java.Charsets.UTF_8;
import java.io.ByteArrayInputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.groupware.CapabilityConvertUtf8ToUtf8mb4Task;
import com.openexchange.capabilities.groupware.CapabilityCreateTableService;
import com.openexchange.capabilities.groupware.CapabilityCreateTableTask;
import com.openexchange.capabilities.groupware.CapabilityDeleteListener;
import com.openexchange.capabilities.groupware.MakeNotNullUpdateTask;
import com.openexchange.capabilities.internal.CapabilityReloadable;
import com.openexchange.capabilities.internal.CapabilityServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.userconfiguration.PermissionConfigurationChecker;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

public class CapabilitiesActivator extends HousekeepingActivator {

    /** The service look-up */
    public static final AtomicReference<ServiceLookup> SERVICES = new AtomicReference<ServiceLookup>();

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, ContextService.class, ConfigurationService.class, ConfigViewFactory.class, UserPermissionService.class, DatabaseService.class, TimerService.class, CacheService.class, SessiondService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        SERVICES.set(this);

        PermissionAvailabilityServiceRegistry tracker = new PermissionAvailabilityServiceRegistry(context);
        track(PermissionAvailabilityService.class, tracker);

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
                "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds=360\n" +
                "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds=60\n" +
                "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                "jcs.region."+regionName+".elementattributes.MaxLifeSeconds=-1\n" +
                "jcs.region."+regionName+".elementattributes.IdleTime=360\n" +
                "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes(UTF_8);
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf), true);
        }
        {
            final String regionName = "CapabilitiesUser";
            final byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                "jcs.region."+regionName+".cacheattributes.MaxObjects=1000000\n" +
                "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
                "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds=360\n" +
                "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds=60\n" +
                "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                "jcs.region."+regionName+".elementattributes.MaxLifeSeconds=-1\n" +
                "jcs.region."+regionName+".elementattributes.IdleTime=360\n" +
                "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes(UTF_8);
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf), true);
        }
        {
            final String regionName = "Capabilities";
            final byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                "jcs.region."+regionName+".cacheattributes.MaxObjects=1000000\n" +
                "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
                "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds=360\n" +
                "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds=60\n" +
                "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                "jcs.region."+regionName+".elementattributes.MaxLifeSeconds=-1\n" +
                "jcs.region."+regionName+".elementattributes.IdleTime=360\n" +
                "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes(UTF_8);
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf), true);
        }
        {
            final EventHandler eventHandler = new CapabilitiesEventHandler(this);
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
            dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
            registerService(EventHandler.class, eventHandler, dict);
        }

        final CapabilityCheckerRegistry capCheckers = new CapabilityCheckerRegistry(context, this);
        rememberTracker(capCheckers);

        /*
         * Create & register CapabilityService
         */
        final CapabilityServiceImpl capService = new CapabilityServiceImpl(this, capCheckers, tracker);
        registerService(Reloadable.class, new CapabilityReloadable(capService));
        registerService(CapabilityService.class, capService);

        track(Capability.class, new SimpleRegistryListener<Capability>() {

            @Override
            public void added(ServiceReference<Capability> ref, Capability capability) {
                getCapability(capability.getId());
            }

            @Override
            public void removed(ServiceReference<Capability> ref, Capability service) {
                // Nothing
            }

        });

        trackService(ServerConfigService.class);
        trackService(PermissionConfigurationChecker.class);

        /*
         * Register update task, create table job and delete listener
         */
        registerService(CreateTableService.class, new CapabilityCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CapabilityCreateTableTask(), new MakeNotNullUpdateTask(), new CapabilityConvertUtf8ToUtf8mb4Task()));
        registerService(DeleteListener.class, new CapabilityDeleteListener());

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        SERVICES.set(null);
        final CacheService cacheService = getService(CacheService.class);
        if (null != cacheService) {
            cacheService.freeCache("CapabilitiesContext");
            cacheService.freeCache("CapabilitiesUser");
            cacheService.freeCache("Capabilities");
        }
        super.stopBundle();
    }

}
