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

package com.openexchange.test.common.data.conversion.ical;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * {@link Tools}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Tools {

    private Tools() {
        super();
    }

    private static SimpleDateFormat utc = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    static {
        utc.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String formatForICal(Date date) {
        return utc.format(date);
    }

    public static String formatForICal(Date date, boolean useDateValue) {
        return useDateValue ? dateFormat.format(date) : utc.format(date);
    }

    public static String formatForICalWithoutTimezone(Date date) {
        return new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(date);
    }
}
