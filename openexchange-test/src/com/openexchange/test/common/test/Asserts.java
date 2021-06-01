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

package com.openexchange.test.common.test;

import java.util.Calendar;
import java.util.Date;

/**
 * {@link Asserts}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Asserts {

    public static void assertEquals(String message, Date date1, Date date2, int precision) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        if (precision <= Calendar.SECOND) {
            equalize(cal1, cal2, Calendar.MILLISECOND);
        }
        if (precision <= Calendar.MINUTE) {
            equalize(cal1, cal2, Calendar.SECOND);
        }
        if (precision <= Calendar.HOUR_OF_DAY) {
            equalize(cal1, cal2, Calendar.MINUTE);
        }
        if (precision <= Calendar.DAY_OF_MONTH) {
            equalize(cal1, cal2, Calendar.HOUR_OF_DAY);
        }
        if (precision <= Calendar.MONTH) {
            equalize(cal1, cal2, Calendar.DAY_OF_MONTH);
        }
        if (precision <= Calendar.YEAR) {
            equalize(cal1, cal2, Calendar.MONTH);
        }

        org.junit.Assert.assertEquals(message, cal1.getTime(), cal2.getTime());
    }

    private static void equalize(Calendar c1, Calendar c2, int field) {
        c1.set(field, 0);
        c2.set(field, 0);
    }
}
