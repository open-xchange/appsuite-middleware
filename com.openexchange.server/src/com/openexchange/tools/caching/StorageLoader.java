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

package com.openexchange.tools.caching;

import java.io.Serializable;
import com.openexchange.caching.CacheKey;
import com.openexchange.exception.OXException;

/**
 * {@link StorageLoader} helps the {@link SerializedCachingLoader} with loading the necessary data from the storage layer.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @param <T>
 */
public interface StorageLoader<T> {

    /**
     * @return the key for the value. Normally an instance of {@link CacheKey}.
     */
    Serializable getKey();

    /**
     * This method is called if the data is not available in the cache and needs to be loaded from the storage layer.
     * @return the data loaded from the storage layer.
     * @throws OXException
     */
    T load() throws OXException;
}