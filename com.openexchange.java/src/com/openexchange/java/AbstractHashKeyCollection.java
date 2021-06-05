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

package com.openexchange.java;

import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link AbstractHashKeyCollection}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractHashKeyCollection<C> {

    protected final AtomicReference<HashKeyGenerator> generatorReference;

    /**
     * Initializes a new {@link AbstractHashKeyCollection}.
     */
    protected AbstractHashKeyCollection() {
        super();
        generatorReference = new AtomicReference<HashKeyGenerator>();
    }

    /**
     * Sets given generator.
     *
     * @param generator The generator
     * @return This collection with generator applied
     */
    public C setGenerator(final HashKeyGenerator generator) {
        generatorReference.set(generator);
        return thisCollection();
    }

    /**
     * Returns this collection.
     *
     * @return This cleared collection
     */
    protected abstract C thisCollection();

    /**
     * Creates a new {@link HashKey} for specified <code>String</code> key.
     *
     * @param s The <code>String</code> key
     * @return The hash key
     */
    protected HashKey newKey(final String s) {
        final HashKeyGenerator generator = generatorReference.get();
        return null == generator ? HashKey.valueOf(s) : generator.newHashKey(s);
    }

}
