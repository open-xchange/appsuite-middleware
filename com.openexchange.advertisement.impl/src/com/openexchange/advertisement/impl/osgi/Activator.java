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

package com.openexchange.advertisement.impl.osgi;

import java.io.ByteArrayInputStream;
import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.LoggerFactory;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.RemoteAdvertisementService;
import com.openexchange.advertisement.impl.rmi.RemoteAdvertisementServiceImpl;
import com.openexchange.advertisement.impl.services.AbstractAdvertisementConfigService;
import com.openexchange.advertisement.impl.services.AccessCombinationAdvertisementConfigService;
import com.openexchange.advertisement.impl.services.GlobalAdvertisementConfigService;
import com.openexchange.advertisement.impl.services.TaxonomyTypesAdvertisementConfigService;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.reseller.ResellerService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DatabaseService.class, ContextService.class, ResellerService.class, CacheService.class,
                                ConfigViewFactory.class, ConfigurationService.class, UserService.class, UserPermissionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LoggerFactory.getLogger(Activator.class).info("starting bundle com.openexchange.advertisement.impl");

        Services.setServiceLookup(this);

        // Define cache regions
        {
            final String regionName = AbstractAdvertisementConfigService.CACHING_REGION;
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
                "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf), true);
        }

        registerService(AdvertisementConfigService.class, AccessCombinationAdvertisementConfigService.getInstance());
        registerService(AdvertisementConfigService.class, GlobalAdvertisementConfigService.getInstance());
        registerService(AdvertisementConfigService.class, TaxonomyTypesAdvertisementConfigService.getInstance());
        registerService(Reloadable.class, TaxonomyTypesAdvertisementConfigService.getInstance());

        // Register appropriate RMI stub
        {
            Dictionary<String, Object> props = new Hashtable<>(2);
            props.put("RMIName", RemoteAdvertisementService.RMI_NAME);
            registerService(Remote.class, new RemoteAdvertisementServiceImpl(), props);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LoggerFactory.getLogger(Activator.class).info("stopping bundle com.openexchange.advertisement.impl");

        super.stopBundle();
        Services.setServiceLookup(null);
    }

}
