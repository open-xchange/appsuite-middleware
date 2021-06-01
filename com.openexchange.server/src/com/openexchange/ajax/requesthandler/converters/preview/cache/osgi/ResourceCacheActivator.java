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

package com.openexchange.ajax.requesthandler.converters.preview.cache.osgi;

import static com.openexchange.ajax.requesthandler.cache.ResourceCacheProperties.CACHE_TYPE;
import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.ajax.requesthandler.cache.AbstractResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
import com.openexchange.ajax.requesthandler.converters.preview.cache.FileStoreResourceCacheImpl;
import com.openexchange.ajax.requesthandler.converters.preview.cache.RdbResourceCacheImpl;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.AddRefIdForPreviewCacheTable;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.ChangeDataToLongblob;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.ChangeFileNameAndTypeLength;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.DropDataFromPreviewCacheTable;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewCacheCreateDataTableService;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewCacheCreateDataTableTask;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewCacheCreateTableService;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewCacheCreateTableTask;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewCacheDeleteListener;
import com.openexchange.ajax.requesthandler.converters.preview.cache.groupware.PreviewTableUtf8Mb4UpdateTask;
import com.openexchange.ajax.requesthandler.converters.preview.cache.rmi.ResourceCacheRMIServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.TimerService;

/**
 * {@link ResourceCacheActivator} - Activator for resource cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceCacheActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ResourceCacheActivator}.
     */
    public ResourceCacheActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put("RMI_NAME", ResourceCacheRMIServiceImpl.RMI_NAME);
        registerService(Remote.class, new ResourceCacheRMIServiceImpl(), serviceProperties);
        track(TimerService.class, new SimpleRegistryListener<TimerService>() {

            @Override
            public void added(ServiceReference<TimerService> ref, TimerService service) {
                addService(TimerService.class, service);
            }

            @Override
            public void removed(ServiceReference<TimerService> ref, TimerService service) {
                removeService(TimerService.class);
            }
        });
        openTrackers();
        // Init service
        final AbstractResourceCache cache;
        final EventHandler eventHandler;
        {
            final ConfigurationService configurationService = getService(ConfigurationService.class);
            final String type = configurationService.getProperty(CACHE_TYPE, "FS").trim();
            if ("DB".equalsIgnoreCase(type)) {
                final RdbResourceCacheImpl rdbPreviewCacheImpl = new RdbResourceCacheImpl(this);
                cache = rdbPreviewCacheImpl;
                eventHandler = rdbPreviewCacheImpl;
            } else {
                final FileStoreResourceCacheImpl fileStorePreviewCache = new FileStoreResourceCacheImpl(this);
                cache = fileStorePreviewCache;
                eventHandler = fileStorePreviewCache;
            }
        }
        ResourceCacheRMIServiceImpl.CACHE_REF.set(cache);
        // Register stuff
        registerService(ResourceCache.class, cache);
        registerService(Reloadable.class, cache);
        ServerServiceRegistry.getInstance().addService(ResourceCache.class, cache);
        {
            final Dictionary<String, Object> d = new Hashtable<String, Object>(1);
            d.put(EventConstants.EVENT_TOPIC, new String[] { FileStorageEventConstants.UPDATE_TOPIC, FileStorageEventConstants.DELETE_TOPIC });
            registerService(EventHandler.class, eventHandler, d);
        }
        ResourceCaches.setResourceCache(cache);
        /*
         * Register update task, create table job and delete listener
         */
        registerService(CreateTableService.class, new PreviewCacheCreateTableService());
        registerService(CreateTableService.class, new PreviewCacheCreateDataTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new PreviewCacheCreateTableTask(), new AddRefIdForPreviewCacheTable(), new PreviewCacheCreateDataTableTask(), new DropDataFromPreviewCacheTable(), new ChangeFileNameAndTypeLength(), new ChangeDataToLongblob(), new PreviewTableUtf8Mb4UpdateTask()));
        registerService(DeleteListener.class, new PreviewCacheDeleteListener());
    }

    @Override
    protected void stopBundle() throws Exception {
        ResourceCaches.setResourceCache(null);
        super.stopBundle();
    }
}
