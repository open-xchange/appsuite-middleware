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

package com.openexchange.groupware.update.tools.console.comparators;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * {@link AbstractComparator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
abstract class AbstractComparator implements Comparator<List<Object>>, Serializable {

    private static final long serialVersionUID = -2480717923936406913L;
    private final Class<?> clazz;
    private final int indexPosition;

    /**
     * Initialises a new {@link AbstractComparator}.
     */
    public AbstractComparator(Class<?> clazz, int indexPosition) {
        super();
        this.clazz = clazz;
        this.indexPosition = indexPosition;
    }

    @Override
    public int compare(List<Object> o1, List<Object> o2) {
        Object object1 = o1.get(indexPosition);
        Object object2 = o2.get(indexPosition);
        if (null == object1) {
            return null == object2 ? 0 : -1;
        }
        if (null == object2) {
            return 1;
        }
        if (false == o1.getClass().isAssignableFrom(clazz)) {
            return false == o2.getClass().isAssignableFrom(clazz) ? 0 : -1;
        }
        if (false == o2.getClass().isAssignableFrom(clazz)) {
            return 1;
        }
        return innerCompare(object1, object2);
    }

    /**
     * Compares the two objects
     * 
     * @param o1 The first object
     * @param o2 The second object
     * @return return one of -1, 0, or 1 according to whether
     *         first object is less than, equal to, or greater than the second.
     */
    protected abstract int innerCompare(Object o1, Object o2);
}
