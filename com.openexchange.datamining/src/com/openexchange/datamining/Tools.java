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

package com.openexchange.datamining;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * {@link Tools}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public final class Tools {

    static String humanReadableBytes(String string) {
        String returnString = "";
        BigInteger number = new BigInteger(string);
        if (number.equals(new BigInteger("9999999999")) || Integer.parseInt(string) == Integer.MAX_VALUE) {
            returnString = "INFINITE";
        } else if (number.equals(new BigInteger("1"))) {
            returnString = "1Byte";
        } else if (number.compareTo(new BigInteger("1000000000")) >= 0) {
            returnString = number.divide(new BigInteger("1000000000")).toString() + "GB";
        } else if (number.compareTo(new BigInteger("1000000")) >= 0) {
            returnString = number.divide(new BigInteger("1000000")).toString() + "MB";
        } else if (number.compareTo(new BigInteger("1000")) >= 0) {
            returnString = number.divide(new BigInteger("1000")).toString() + "KB";
        }
        return returnString;
    }

    /**
     * Get a time stamp of 30 days back in milliseconds
     * 
     * @return The time stamp
     */
    static String calculate30DaysBack() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        return String.valueOf(cal.getTime().getTime());
    }

}
