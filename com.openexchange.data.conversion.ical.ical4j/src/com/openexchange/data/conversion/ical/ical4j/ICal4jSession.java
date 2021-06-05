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

package com.openexchange.data.conversion.ical.ical4j;

import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ZoneInfo;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.XProperty;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ICal4jSession implements ICalSession {

    private Calendar calendar = new Calendar();
    private final Mode mode;
    private int index;

    /**
     * Default constructor.
     * @param mode
     */
    public ICal4jSession(Mode mode) {
        super();
        this.mode = mode;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public ZoneInfo getZoneInfo() {
        return mode.getZoneInfo();
    }

    @Override
    public void setName(String name) {
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", name));
    }

    /**
     * @return the calendar
     */
    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar cal) {
        calendar = cal;
        index = 0;
    }

    /**
     * Counts the number of elements already parsed for error messages
     * @return
     */
    public int getAndIncreaseIndex() {
        return index++;
    }


}
