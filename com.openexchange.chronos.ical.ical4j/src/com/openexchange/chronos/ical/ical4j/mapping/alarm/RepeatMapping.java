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

package com.openexchange.chronos.ical.ical4j.mapping.alarm;

import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Repeat;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;

/**
 * {@link RepeatMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RepeatMapping extends AbstractICalMapping<VAlarm, Alarm> {

    /**
     * Initializes a new {@link RepeatMapping}.
     */
	public RepeatMapping() {
		super();
	}

	@Override
	public void export(Alarm object, VAlarm component, ICalParameters parameters, List<OXException> warnings) {
        Repeat value = object.getRepeat();
        removeProperties(component, Property.REPEAT);
        removeProperties(component, Property.DURATION);
        if (null != value) {
            component.getProperties().add(new net.fortuna.ical4j.model.property.Repeat(value.getCount()));
            component.getProperties().add(new net.fortuna.ical4j.model.property.Duration(new ParameterList(), value.getDuration()));
        }
	}

	@Override
	public void importICal(VAlarm component, Alarm object, ICalParameters parameters, List<OXException> warnings) {
        Property property = component.getProperty(Property.REPEAT);
        if (null == property || null == property.getValue()) {
            object.setRepeat(null);
            return;
        }
        int count;
        try {
            count = Integer.parseInt(property.getValue());
        } catch (NumberFormatException e) {
            addConversionWarning(warnings, e, Property.REPEAT, "Ignoring REPEAT due invalid value");
            object.setRepeat(null);
            return;
        }
        property = component.getProperty(Property.DURATION);
        if (null == property || null == property.getValue()) {
            addConversionWarning(warnings, Property.REPEAT, "Ignoring REPEAT due to missing DURATION");
            object.setRepeat(null);
            return;
        }
        object.setRepeat(new Repeat(count, property.getValue()));
	}

}
