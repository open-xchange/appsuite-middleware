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

package com.openexchange.groupware.filestore;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.sql.Connection;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;

public class CachingFilestoreStorage extends FilestoreStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingFilestoreStorage.class);
    private static final String REGION_NAME = "Filestore";

    private final FilestoreStorage delegate;

    public CachingFilestoreStorage(final FilestoreStorage fs) {
        this.delegate = fs;
    }

    @Override
    public Filestore getFilestore(final int id) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getFilestore(id);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        final Object object = cache.get(I(id));
        if (object instanceof Filestore) {
            return (Filestore) object;
        }
        final Filestore filestore = delegate.getFilestore(id);
        cache.put(I(id), filestore, false);
        return filestore;
    }

    @Override
    public Filestore getFilestore(URI uri) throws OXException {
        return delegate.getFilestore(uri);
    }

    @Override
    public Filestore getFilestore(final Connection con, final int id) throws OXException {
        final CacheService service = ServerServiceRegistry.getInstance().getService(CacheService.class);
        Cache filestoreCache = null;
        if (service != null) {
            try {
                filestoreCache = service.getCache(REGION_NAME);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
        Filestore retval = null;
        if (null != filestoreCache) {
            retval = (Filestore) filestoreCache.get(I(id));
        }
        if (null == retval) {
            retval = delegate.getFilestore(con, id);
            if (null != filestoreCache) {
                try {
                    filestoreCache.put(I(id), retval, false);
                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
        }
        return retval;
    }
}
