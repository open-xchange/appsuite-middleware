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

import com.openexchange.chronos.ical.ical4j.mapping.ICalTextMapping;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.property.Location;

/**
 *
 * {@link LocationMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LocationMapping extends ICalTextMapping<Available, com.openexchange.chronos.Available> {

    /**
     * Initialises a new {@link LocationMapping}.
     */
    public LocationMapping() {
        super(Property.LOCATION);
    }

    @Override
    protected String getValue(com.openexchange.chronos.Available object) {
        return object.getLocation();
    }

    @Override
    protected void setValue(com.openexchange.chronos.Available object, String value) {
        object.setLocation(value);
    }

    @Override
    protected Property createProperty() {
        return new Location();
    }

}
