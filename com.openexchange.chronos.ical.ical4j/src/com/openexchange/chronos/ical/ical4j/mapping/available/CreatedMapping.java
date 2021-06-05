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
import com.openexchange.chronos.ical.ical4j.mapping.ICalUtcMapping;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.UtcProperty;

/**
 *
 * {@link CreatedMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CreatedMapping extends ICalUtcMapping<Available, com.openexchange.chronos.Available> {

    /**
     * Initialises a new {@link CreatedMapping}.
     */
    public CreatedMapping() {
        super(Property.CREATED);
    }

    @Override
    protected Date getValue(com.openexchange.chronos.Available object) {
        return object.getCreated();
    }

    @Override
    protected void setValue(com.openexchange.chronos.Available object, Date value) {
        object.setCreated(value == null ? new Date() : value);
    }

    @Override
    protected UtcProperty createProperty() {
        return new Created();
    }
}
