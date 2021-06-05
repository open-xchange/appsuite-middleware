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

package com.openexchange.file.storage.rdb.osgi;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import com.openexchange.caching.CacheService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.file.storage.AdministrativeFileStorageAccountStorage;
import com.openexchange.file.storage.FileStorageAccountDeleteListener;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.rdb.Services;
import com.openexchange.file.storage.rdb.groupware.FileStorageMetaDataColumnsTask;
import com.openexchange.file.storage.rdb.groupware.FileStorageConvertUtf8ToUtf8mb4Task;
import com.openexchange.file.storage.rdb.groupware.FileStorageRdbCreateTableTask;
import com.openexchange.file.storage.rdb.groupware.FileStorageRdbDeleteListener;
import com.openexchange.file.storage.rdb.internal.CachingFileStorageAccountStorage;
import com.openexchange.file.storage.rdb.internal.DeleteListenerRegistry;
import com.openexchange.file.storage.rdb.internal.FileStorageExternalAccountProvider;
import com.openexchange.file.storage.rdb.internal.RdbAdministrativeFileStorageAccountStorage;
import com.openexchange.file.storage.rdb.internal.RdbFileStorageAccountManagerProvider;
import com.openexchange.file.storage.rdb.secret.RdbFileStorageSecretHandling;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.cache.service.FolderCacheInvalidationService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.lock.LockService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.EncryptedItemDetectorService;
import com.openexchange.secret.recovery.SecretMigrator;

/**
 * {@link FileStorageRdbActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class FileStorageRdbActivator extends HousekeepingActivator {

    private WhiteboardSecretService secretService;

    /**
     * Initializes a new {@link FileStorageRdbActivator}.
     */
    public FileStorageRdbActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        // @formatter:off
        return new Class<?>[] {
            DatabaseService.class, GenericConfigurationStorageService.class, ContextService.class, FileStorageServiceRegistry.class,
            CacheService.class, SecretEncryptionFactoryService.class, IDGeneratorService.class, CryptoService.class, FolderCacheInvalidationService.class };
        // @formatter:on
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            Services.setServices(this);
            DeleteListenerRegistry.initInstance();
            track(FileStorageAccountDeleteListener.class, new DeleteListenerServiceTracker(context));
            /*
             * Feed cache with additional cache configuration for file storage account cache
             */
            {
                /*
                 * FileStorageAccount region with 5 minutes time-out
                 */
                final String regionName = CachingFileStorageAccountStorage.getRegionName();
                // @formatter:off
                final byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                		"jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                		"jcs.region."+regionName+".cacheattributes.MaxObjects=10000000\n" +
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
                // @formatter:on
            }
            /*
             * Initialize and start service trackers
             */
            final NearRegistryServiceTracker<FileStorageService> fileStorageServiceTracker = new NearRegistryServiceTracker<FileStorageService>(context, FileStorageService.class);
            rememberTracker(fileStorageServiceTracker);
            trackService(LockService.class);
            openTrackers();
            /*
             * Initialize and register services
             */
            registerService(FileStorageAccountManagerProvider.class, new RdbFileStorageAccountManagerProvider());
            AdministrativeFileStorageAccountStorage adminAccountStorage = new RdbAdministrativeFileStorageAccountStorage(this);
            registerService(AdministrativeFileStorageAccountStorage.class, adminAccountStorage);
            registerService(ExternalAccountProvider.class, new FileStorageExternalAccountProvider(adminAccountStorage));
            /*
             * The update task/create table service
             */
            final FileStorageRdbCreateTableTask createTableTask = new FileStorageRdbCreateTableTask();

            // @formatter:off
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(
                createTableTask,
                new FileStorageConvertUtf8ToUtf8mb4Task(),
                new FileStorageMetaDataColumnsTask()));
            // @formatter:on
            registerService(CreateTableService.class, createTableTask);
            /*
             * The delete listener
             */
            registerService(DeleteListener.class, new FileStorageRdbDeleteListener());
            /*
             * Secret Handling
             */
            {
                final RdbFileStorageSecretHandling secretHandling = new RdbFileStorageSecretHandling() {

                    @Override
                    protected Collection<FileStorageService> getFileStorageServices() {
                        return Collections.unmodifiableList(fileStorageServiceTracker.getServiceList());
                    }
                };
                registerService(EncryptedItemDetectorService.class, secretHandling);
                registerService(SecretMigrator.class, secretHandling);
                registerService(EncryptedItemCleanUpService.class, secretHandling);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(FileStorageRdbActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        try {
            super.stopBundle();
            /*
             * Clear service registry
             */
            Services.setServices(null);
            final WhiteboardSecretService secretService = this.secretService;
            if (null != secretService) {
                secretService.close();
                this.secretService = null;
            }
            DeleteListenerRegistry.releaseInstance();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(FileStorageRdbActivator.class).error("", e);
            throw e;
        }
    }

}
