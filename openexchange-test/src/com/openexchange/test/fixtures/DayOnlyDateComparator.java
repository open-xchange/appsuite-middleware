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

package com.openexchange.test.fixtures;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * @author Markus Wagner <markus.wagner@open-xchange.com>
 */
public class DayOnlyDateComparator implements Comparator<Date>, Serializable {

    private static final long serialVersionUID = 5478489828627817393L;

    @Override
    public int compare(final Date d1, final Date d2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);

        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        int difference = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (difference != 0) {
            return difference;
        }

        difference = cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR);
        if (difference != 0) {
            return difference;
        }
        return 0;
    }
}
