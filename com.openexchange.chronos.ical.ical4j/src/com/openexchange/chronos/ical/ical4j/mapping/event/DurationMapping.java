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

package com.openexchange.chronos.ical.ical4j.mapping.event;

import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;

/**
 * {@link DurationMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DurationMapping extends AbstractICalMapping<VEvent, Event> {

    /**
     * Initializes a new {@link DurationMapping}.
     */
	public DurationMapping() {
        super();
	}

    @Override
    public void export(Event object, VEvent component, ICalParameters parameters, List<OXException> warnings) {
        removeProperties(component, Property.DURATION); // stick to DTEND for export
    }

    @Override
    public void importICal(VEvent component, Event object, ICalParameters parameters, List<OXException> warnings) {
        Duration duration = component.getDuration();
        if (null == duration || null == duration.getDuration()) {
            return;
        }
        DtStart dtStart = component.getStartDate();
        if (null != dtStart && null != dtStart.getDate()) {
            Event event = new Event();
            new DtStartMapping().importICal(component, event, parameters, warnings);
            DateTime startDate = event.getStartDate();
            object.setEndDate(startDate.addDuration(org.dmfs.rfc5545.Duration.parse(duration.getValue())));
        }
    }

}
