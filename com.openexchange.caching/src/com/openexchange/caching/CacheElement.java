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

package com.openexchange.caching;

import java.io.Serializable;

/**
 * {@link CacheElement} - Every item in the cache is wrapped in a cache element. This contains information about the element: the region
 * name, the key, the value, and the element attributes.
 * <p>
 * The element attributes have lots of useful information about each element, such as when they were created, how long they have to live,
 * and if they are allowed to be spooled, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CacheElement extends Serializable {

    /**
     * Gets the cache name attribute of the cache element object. The cache name is also known as the region name.
     *
     * @return The cacheName value
     */
    public String getCacheName();

    /**
     * Gets the key attribute of the cache element object
     *
     * @return The key value
     */
    public Serializable getKey();

    /**
     * Gets the value attribute of the cache element object
     *
     * @return The value
     */
    public Serializable getVal();

    /**
     * Gets the attributes attribute of the cache element object
     *
     * @return The attributes value
     */
    public ElementAttributes getElementAttributes();

    /**
     * Sets the attributes attribute of the cache element object
     *
     * @param attr The new attributes value
     */
    public void setElementAttributes(ElementAttributes attr);

}
