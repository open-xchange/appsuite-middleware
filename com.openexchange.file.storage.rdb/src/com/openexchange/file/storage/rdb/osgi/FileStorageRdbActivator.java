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
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.rdb.Services;
import com.openexchange.file.storage.rdb.groupware.FileStorageRdbCreateTableTask;
import com.openexchange.file.storage.rdb.groupware.FileStorageRdbDeleteListener;
import com.openexchange.file.storage.rdb.internal.CachingFileStorageAccountStorage;
import com.openexchange.file.storage.rdb.internal.RdbFileStorageAccountManagerProvider;
import com.openexchange.file.storage.rdb.secret.RdbFileStorageSecretHandling;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
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

    private volatile WhiteboardSecretService secretService;

    /**
     * Initializes a new {@link FileStorageRdbActivator}.
     */
    public FileStorageRdbActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            DatabaseService.class, GenericConfigurationStorageService.class, ContextService.class, FileStorageServiceRegistry.class,
            CacheService.class, SecretEncryptionFactoryService.class, IDGeneratorService.class, CryptoService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServices(this);
            /*
             * Feed cache with additional cache configuration for file storage account cache
             */
            {
                /*
                 * FileStorageAccount region with 5 minutes time-out
                 */
                final String regionName = CachingFileStorageAccountStorage.getRegionName();
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
            /*
             * The update task/create table service
             */
            final FileStorageRdbCreateTableTask createTableTask = new FileStorageRdbCreateTableTask();
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(createTableTask));
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
        } catch (final Exception e) {
            org.slf4j.LoggerFactory.getLogger(FileStorageRdbActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            cleanUp();
            /*
             * Clear service registry
             */
            Services.setServices(null);
            final WhiteboardSecretService secretService = this.secretService;
            if (null != secretService) {
                secretService.close();
                this.secretService = null;
            }
        } catch (final Exception e) {
            org.slf4j.LoggerFactory.getLogger(FileStorageRdbActivator.class).error("", e);
            throw e;
        }
    }

}
