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
import com.openexchange.exception.OXException;

/**
 * {@link PutIfAbsent}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface PutIfAbsent {

    /**
     * If the specified key is not already associated with a value, associate it with the given value. The action is performed atomically.
     *
     * @param key The key with which the specified value is to be associated
     * @param value The value to be associated with the specified key
     * @return The previous value associated with the specified key, or <tt>null</tt> if there was no mapping for the key.
     * @throws NullPointerException if the specified key or value is null, and this map does not permit null keys or values
     * @throws OXException If a cache error occurs
     */
    Serializable putIfAbsent(Serializable key, Serializable value) throws OXException;
}
