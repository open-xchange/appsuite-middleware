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

package com.openexchange.tools.oxfolder.property.osgi;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import com.openexchange.caching.CacheService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.folder.FolderDeleteListenerService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;
import com.openexchange.tools.oxfolder.property.impl.CachingFolderUserPropertyStorage;
import com.openexchange.tools.oxfolder.property.impl.FolderSubscriptionHelperImpl;
import com.openexchange.tools.oxfolder.property.impl.FolderUserPropertyDeleteListener;
import com.openexchange.tools.oxfolder.property.impl.RdbFolderUserPropertyStorage;
import com.openexchange.tools.oxfolder.property.sql.CreateFolderUserPropertyTable;
import com.openexchange.tools.oxfolder.property.sql.CreateFolderUserPropertyTask;
import com.openexchange.tools.oxfolder.property.sql.OXFolderUserPropertyConvertUtf8ToUtf8mb4Task;

/**
 * {@link FolderUserPropertyActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class FolderUserPropertyActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link FolderUserPropertyActivator}.
     *
     */
    public FolderUserPropertyActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, CacheService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // Register UpdateTask
        DatabaseService dbService = getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        registerService(UpdateTaskProviderService.class, () -> Arrays.asList(new CreateFolderUserPropertyTask(dbService), new OXFolderUserPropertyConvertUtf8ToUtf8mb4Task()));
        registerService(CreateTableService.class, new CreateFolderUserPropertyTable());

        // Initialize cache region
        {
            String regionName = CachingFolderUserPropertyStorage.getRegionName();
            byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
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
                "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf));
        }

        // Register FolderUserPropertyStorage
        RdbFolderUserPropertyStorage rdbStorage = new RdbFolderUserPropertyStorage(this);
        FolderUserPropertyStorage storage = new CachingFolderUserPropertyStorage(rdbStorage);

        registerService(FolderSubscriptionHelper.class, new FolderSubscriptionHelperImpl(storage));
        registerService(FolderUserPropertyStorage.class, storage);
        FolderUserPropertyDeleteListener delListener = new FolderUserPropertyDeleteListener(rdbStorage);
        registerService(FolderDeleteListenerService.class, delListener);
        registerService(DeleteListener.class, delListener);
    }

}
