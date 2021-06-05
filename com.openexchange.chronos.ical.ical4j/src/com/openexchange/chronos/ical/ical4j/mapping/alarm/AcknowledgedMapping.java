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

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.extensions.caldav.property.Acknowledged;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;

/**
 * {@link AcknowledgedMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AcknowledgedMapping extends AbstractICalMapping<VAlarm, Alarm> {

    /**
     * Initializes a new {@link AcknowledgedMapping}.
     */
	public AcknowledgedMapping() {
        super();
	}

    @Override
    public void export(Alarm object, VAlarm component, ICalParameters parameters, List<OXException> warnings) {
        removeProperties(component, Acknowledged.PROPERTY_NAME);
        Date value = object.getAcknowledged();
        if (null != value) {
            component.getProperties().add(new Acknowledged(new DateTime(value)));
        }
    }

    @Override
    public void importICal(VAlarm component, Alarm object, ICalParameters parameters, List<OXException> warnings) {
        Property property = component.getProperty(Acknowledged.PROPERTY_NAME);
        if (null == property || null == property.getValue()) {
            object.setAcknowledged(null);
        } else {
            try {
                object.setAcknowledged(new Date(new DateTime(property.getValue()).getTime()));
            } catch (ParseException e) {
                addConversionWarning(warnings, e, Acknowledged.PROPERTY_NAME, e.getMessage());
            }
        }
    }

}
