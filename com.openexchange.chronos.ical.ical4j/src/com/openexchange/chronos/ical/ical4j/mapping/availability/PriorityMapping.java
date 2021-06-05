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

import java.util.List;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAvailability;
import net.fortuna.ical4j.model.property.Priority;

/**
 * {@link PriorityMapping}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class PriorityMapping extends AbstractICalMapping<VAvailability, Availability> {

    /**
     * Initialises a new {@link PriorityMapping}.
     */
    public PriorityMapping() {
        super();
    }

    @Override
    public void export(Availability object, VAvailability component, ICalParameters parameters, List<OXException> warnings) {
        if (object.getPriority() >= 0 && object.getPriority() <= 9) {
            component.getProperties().add(new Priority(object.getPriority()));
        }
    }

    @Override
    public void importICal(VAvailability component, Availability object, ICalParameters parameters, List<OXException> warnings) {
        Priority priority = (Priority) component.getProperty(Property.PRIORITY);
        if (priority != null) {
            object.setPriority(priority.getLevel());
        }
    }
}
