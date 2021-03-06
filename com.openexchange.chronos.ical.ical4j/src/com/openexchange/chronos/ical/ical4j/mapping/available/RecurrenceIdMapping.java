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

import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.ical.ical4j.mapping.ICalDateTimeMapping;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.RecurrenceId;

/**
 * {@link RecurrenceIdMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RecurrenceIdMapping extends ICalDateTimeMapping<Available, com.openexchange.chronos.Available> {

    /**
     * Initializes a new {@link RecurrenceIdMapping}.
     */
    public RecurrenceIdMapping() {
        super(Property.RECURRENCE_ID);
    }

    @Override
    protected DateTime getValue(com.openexchange.chronos.Available object) {
        com.openexchange.chronos.RecurrenceId value = object.getRecurrenceId();
        return null == value ? null : value.getValue();
    }

    @Override
    protected void setValue(com.openexchange.chronos.Available object, DateTime value) {
        object.setRecurrenceId(null != value ? new DefaultRecurrenceId(value) : null);
    }

    @Override
    protected DateProperty createProperty() {
        return new RecurrenceId();
    }

    @Override
    protected DateProperty getProperty(Available component) {
        return (DateProperty) component.getProperty(Property.RECURRENCE_ID);
    }
}
