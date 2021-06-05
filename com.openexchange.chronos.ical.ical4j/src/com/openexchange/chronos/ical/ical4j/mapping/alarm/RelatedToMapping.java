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
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.parameter.RelType;

/**
 * {@link RelatedToMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RelatedToMapping extends AbstractICalMapping<VAlarm, Alarm> {

    /**
     * Initializes a new {@link RelatedToMapping}.
     */
	public RelatedToMapping() {
		super();
	}

	@Override
	public void export(Alarm object, VAlarm component, ICalParameters parameters, List<OXException> warnings) {
        RelatedTo value = object.getRelatedTo();
        removeProperties(component, Property.RELATED_TO);
        if (null != value) {
            net.fortuna.ical4j.model.property.RelatedTo property = new net.fortuna.ical4j.model.property.RelatedTo(value.getValue());
            if (null != value.getRelType()) {
                property.getParameters().add(new RelType(value.getRelType()));
            }
            component.getProperties().add(property);
        }
	}

	@Override
	public void importICal(VAlarm component, Alarm object, ICalParameters parameters, List<OXException> warnings) {
        Property property = component.getProperty(Property.RELATED_TO);
        if (null == property) {
            object.setRelatedTo(null);
        } else {
            object.setRelatedTo(new RelatedTo(optParameterValue(property, Parameter.RELTYPE), property.getValue()));
		}
	}

}
