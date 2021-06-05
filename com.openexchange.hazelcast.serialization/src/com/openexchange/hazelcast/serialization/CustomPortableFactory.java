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


package com.openexchange.hazelcast.serialization;

import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.Portable;

/**
 * {@link CustomPortableFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface CustomPortableFactory {

    /**
     * Creates a new {@link Portable} instance.
     *
     * @return The new Portable instance
     */
    Portable create();

    /**
     * Gets the unique class ID of the {@link Portable} this factory produces. <p/>
     *
     * Make sure to return the same ID as in {@link CustomPortable#getClassId()} of the corresponding {@link CustomPortable}
     * implementation.
     *
     * @return The class ID.
     */
    int getClassId();
    
    /**
     * Gets the ClassDefinition of of the associated Portable. This is used to register ClassDefinitions with Hazelcast before a Portable is
     * written for the first time. This way we can persist nested Portables with possible null values.
     * 
     * @see https://github.com/sancar/hazelcast/commit/fb447bdc89b8d448066ba517d22c891dc60b1534
     * @return Null or the ClassDefinition of of the associated Portable
     *
     */
     ClassDefinition getClassDefinition();

}
