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

package com.openexchange.chronos.ical.ical4j.mapping.available;

import java.util.Date;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;

/**
 * {@link DurationMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DurationMapping extends AbstractICalMapping<Available, com.openexchange.chronos.Available> {

    /**
     * Initialises a new {@link DurationMapping}.
     */
    public DurationMapping() {
        super();
    }

    @Override
    public void export(com.openexchange.chronos.Available object, Available component, ICalParameters parameters, List<OXException> warnings) {
        removeProperties(component, Property.DURATION); // stick to DTEND for export
    }

    @Override
    public void importICal(Available component, com.openexchange.chronos.Available object, ICalParameters parameters, List<OXException> warnings) {
        Duration duration = (Duration) component.getProperty(Property.DURATION);
        if (null == duration || null == duration.getDuration()) {
            return;
        }
        // If duration and startDate are set, then try to determine the endDate
        DtStart dtStart = (DtStart) component.getProperty(Property.DTSTART);
        if (null != dtStart && null != dtStart.getDate()) {
            com.openexchange.chronos.Available available = new com.openexchange.chronos.Available();
            new DtStartMapping().importICal(component, available, parameters, warnings);
            DateTime startDate = available.getStartTime();
            java.util.Date endDate = duration.getDuration().getTime(new Date(startDate.getTimestamp()));
            object.setEndTime(new DateTime(endDate.getTime()));
        }
    }
}
