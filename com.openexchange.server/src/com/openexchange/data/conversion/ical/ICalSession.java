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

package com.openexchange.data.conversion.ical;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface ICalSession {

    Mode getMode();

    ZoneInfo getZoneInfo();

    /**
     * Sets the <code>X-WR-CALNAME</code> property in the underlying <code>VCALENDAR</code> component to the supplied value. According to
     * <a href="http://msdn.microsoft.com/en-us/library/ee157721">MSDN</a>, this property...
     * <ul>
     * <li>MUST be omitted if the iCalendar represents a single appointment or meeting.</li>
     * <li>SHOULD be set to the name of the folder representing the calendar being exported, if the iCal represents a calendar export</li>
     * <li>SHOULD instead be set to a more descriptive locale-dependent string containing the owner's name (e.g. 'Elizabeth Andersen
     * calendar' if the calendar is the owner's primary calendar</li>
     * </ul>
     *
     * @param name The name to set
     */
    void setName(String name);

}
