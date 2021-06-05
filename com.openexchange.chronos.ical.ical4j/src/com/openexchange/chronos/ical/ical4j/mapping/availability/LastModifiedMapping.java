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

package com.openexchange.chronos.ical.ical4j.mapping.availability;

import java.util.Date;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.ical.ical4j.mapping.ICalUtcMapping;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAvailability;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.UtcProperty;

/**
 *
 * {@link LastModifiedMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LastModifiedMapping extends ICalUtcMapping<VAvailability, Availability> {

    /**
     * Initialises a new {@link LastModifiedMapping}.
     */
    public LastModifiedMapping() {
        super(Property.LAST_MODIFIED);
    }

    @Override
    protected Date getValue(Availability object) {
        return object.getLastModified();
    }

    @Override
    protected void setValue(Availability object, Date value) {
        object.setLastModified(value);
    }

    @Override
    protected UtcProperty createProperty() {
        return new LastModified();
    }
}
