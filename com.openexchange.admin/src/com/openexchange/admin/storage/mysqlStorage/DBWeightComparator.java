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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.Comparator;

/**
 * {@link DBWeightComparator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DBWeightComparator implements Comparator<DatabaseHandle> {

    private final int totalUnits;
    private final int totalWeight;

    public DBWeightComparator(final int totalUnits, final int totalWeight) {
        super();
        this.totalUnits = totalUnits;
        this.totalWeight = totalWeight;
    }

    @Override
    public int compare(DatabaseHandle db1, DatabaseHandle db2) {
        int missingUnits1 = isFull(db1) ? Integer.MIN_VALUE : getMissingUnits(db1);
        int missingUnits2 = isFull(db2) ? Integer.MIN_VALUE : getMissingUnits(db2);
        return I(missingUnits1).compareTo(I(missingUnits2));
    }

    private int getMissingUnits(final DatabaseHandle db) {
        return getAverageUnits() - db.getCount();
    }

    private int getAverageUnits() {
        return totalUnits * 100 / totalWeight;
    }

    private boolean isFull(final DatabaseHandle db) {
        return db.getCount() >= i(db.getMaxUnits());
    }
}
