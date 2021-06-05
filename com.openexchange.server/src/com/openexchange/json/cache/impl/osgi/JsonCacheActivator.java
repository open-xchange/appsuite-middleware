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

package com.openexchange.json.cache.impl.osgi;

import com.openexchange.database.CreateTableService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.json.cache.JsonCacheService;
import com.openexchange.json.cache.JsonCaches;
import com.openexchange.json.cache.impl.JsonCacheServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link JsonCacheActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JsonCacheActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link JsonCacheActivator}.
     */
    public JsonCacheActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        /*
         * Register services for table creation
         */
        registerService(CreateTableService.class, new JsonCacheCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(
            new JsonCacheCreateTableTask(),
            new JsonCacheAddInProgressFieldTask(),
            new JsonCacheMediumTextTask(),
            new JsonCacheAddOtherFieldsTask(),
            new JsonCacheEnsureLatin1AsDefault()));
        registerService(DeleteListener.class, new JsonCacheDeleteListener());
        /*
         * Register cache service
         */
        final JsonCacheServiceImpl serviceImpl = new JsonCacheServiceImpl(this);
        registerService(JsonCacheService.class, serviceImpl);
        JsonCaches.CACHE_REFERENCE.set(serviceImpl);
    }

    @Override
    protected void stopBundle() throws Exception {
        JsonCaches.CACHE_REFERENCE.set(null);
        super.stopBundle();
    }

}
