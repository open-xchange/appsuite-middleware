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

import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.chronos.ical.ical4j.mapping.ICalDateTimeMapping;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtStart;

/**
 * {@link DtStartMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DtStartMapping extends ICalDateTimeMapping<VFreeBusy, FreeBusyData> {

    /**
     * Initializes a new {@link DtStartMapping}.
     */
	public DtStartMapping() {
		super(Property.DTSTART);
	}

	@Override
    protected DateTime getValue(FreeBusyData object) {
		return object.getStartDate();
	}

	@Override
    protected void setValue(FreeBusyData object, DateTime value) {
		object.setStartDate(value);
	}

	@Override
	protected DateProperty createProperty() {
		return new DtStart();
	}

    @Override
    protected DateProperty getProperty(VFreeBusy component) {
        return component.getStartDate();
    }

}
