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
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Sequence;

/**
 * {@link SequenceMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SequenceMapping extends AbstractICalMapping<VEvent, Event> {

	@Override
	public void export(Event object, VEvent component, ICalParameters parameters, List<OXException> warnings) {
        int value = object.getSequence();
        if (0 > value) {
			removeProperties(component, Property.SEQUENCE);
		} else {
			Sequence property = component.getSequence();
			if (null == property) {
				property = new Sequence();
				component.getProperties().add(property);
			}
            property.setValue(String.valueOf(value));
		}
	}

	@Override
	public void importICal(VEvent component, Event object, ICalParameters parameters, List<OXException> warnings) {
		Sequence property = component.getSequence();
        if (null != property) {
            object.setSequence(property.getSequenceNo());
        } else if (false == isIgnoreUnsetProperties(parameters)) {
            object.setSequence(0);
        }
	}

}
