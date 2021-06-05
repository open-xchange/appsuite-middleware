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

package com.openexchange.groupware.container;

import java.util.Date;

/**
 * {@link Differ}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class Differ<T extends DataObject> {

    /**
     * Calculates the Difference of two Objects. If the Objects do not differ the return value is null.
     *
     * @param original
     * @param update
     * @return
     */
    public abstract Difference getDifference(T original, T update);

    public abstract int getColumn();

    public static boolean isDifferent(DataObject original, DataObject update, int column) {

        if (!update.contains(column)) { // no update
            return false;
        }

        if (!original.contains(column) && update.contains(column)) { // set
            return true;
        }

        // Both set
        Object v1 = unpack(original.get(column));
        Object v2 = unpack(update.get(column));
        if (v1 == v2) { // Same reference, works on most autoboxed primitives.
            return false;
        }

        if (v1 == null) {
            return true;
        }

        if (v2 == null) {
            return true;
        }

        if (v1.equals(v2)) {
            return false;
        }

        return true;
    }

    // Some Objects may need unpacking to ignore certain object features (like timezones in dates, we don't care for those)
    private static Object unpack(Object object) {
        if (Date.class.isInstance(object)) {
            return ((Date)object).getTime();
        }
        return object;
    }

    protected Difference isArrayDifferent(Object[] original, Object[] update) {

        Difference difference = new Difference();

        boolean isDifferent = false;

        for (Object o : original) {
            boolean found = false;
            for (Object u : update) {
                if (o.equals(u)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                difference.getRemoved().add(o);
                isDifferent = true;
            }
        }

        for (Object u : update) {
            boolean found = false;
            for (Object o : original) {
                if (u.equals(o)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                difference.getAdded().add(u);
                isDifferent = true;
            }
        }

        return isDifferent ? difference : null;
    }

}
