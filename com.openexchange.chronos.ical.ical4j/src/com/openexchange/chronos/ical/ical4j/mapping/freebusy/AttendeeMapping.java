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

package com.openexchange.chronos.ical.ical4j.mapping.freebusy;

import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.chronos.ical.ical4j.mapping.ICalAttendeeMapping;
import net.fortuna.ical4j.model.component.VFreeBusy;

/**
 * {@link AttendeeMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeMapping extends ICalAttendeeMapping<VFreeBusy, FreeBusyData> {

    @Override
    protected List<Attendee> getValue(FreeBusyData object) {
        return object.getAttendees();
    }

    @Override
    protected void setValue(FreeBusyData object, List<Attendee> value) {
        object.setAttendees(value);
    }

}
