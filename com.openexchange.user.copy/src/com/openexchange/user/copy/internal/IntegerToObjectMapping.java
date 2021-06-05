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

package com.openexchange.user.copy.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.user.copy.ObjectMapping;


/**
 * {@link IntegerToObjectMapping}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class IntegerToObjectMapping<T> implements ObjectMapping<T> {

    private final Map<Integer, Integer> idMapping;

    private final Map<Integer, T> sourceMapping;

    private final Map<Integer, T> destinationMapping;


    public IntegerToObjectMapping() {
        super();
        idMapping = new HashMap<Integer, Integer>();
        sourceMapping = new HashMap<Integer, T>();
        destinationMapping = new HashMap<Integer, T>();
    }

    /**
     * @see com.openexchange.user.copy.ObjectMapping#getSource(int)
     */
    @Override
    public T getSource(final int id) {
        return sourceMapping.get(I(id));
    }

    /**
     * @see com.openexchange.user.copy.ObjectMapping#getSourceKeys()
     */
    @Override
    public Set<Integer> getSourceKeys() {
        final Set<Integer> keySet = new HashSet<Integer>(idMapping.keySet());

        return keySet;
    }

    /**
     * @see com.openexchange.user.copy.ObjectMapping#getDestination(java.lang.Object)
     */
    @Override
    public abstract T getDestination(T source);

    protected T getDestinationById(final Integer id) {
        final Integer dstObjId = idMapping.get(id);
        if (dstObjId != null) {
            return destinationMapping.get(dstObjId);
        }

        return null;
    }

    public void addMapping(final Integer sourceId, final T source, final Integer destinationId, final T destination) {
        idMapping.put(sourceId, destinationId);
        sourceMapping.put(sourceId, source);
        destinationMapping.put(destinationId, destination);
    }

}
