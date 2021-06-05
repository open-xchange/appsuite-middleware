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

package com.openexchange.regional;

/**
 * {@link RegionalSettingField}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public enum RegionalSettingField {
    TIME("time", 1),
    TIME_LONG("timeLong", 2),
    DATE("date", 4),
    DATE_SHORT("dateShort", 8),
    DATE_MEDIUM("dateMedium", 16),
    DATE_LONG("dateLong", 32),
    DATE_FULL("dateFull", 64),
    NUMBER("number", 128),
    FIRST_DAY_OF_WEEK("firstDayOfWeek", 256),
    FIRST_DAY_OF_YEAR("firstDayOfYear", 512);

    private final String name;
    private final int flag;

    /**
     * Initializes a new {@link RegionalSettingField}.
     */
    private RegionalSettingField(String name, int flag) {
        this.name = name;
        this.flag = flag;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the flag
     *
     * @return The flag
     */
    public int getFlag() {
        return flag;
    }
}
