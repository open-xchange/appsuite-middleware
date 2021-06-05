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
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.Trigger.Related;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;

/**
 * {@link TriggerMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class TriggerMapping extends AbstractICalMapping<VAlarm, Alarm> {

    /**
     * Initializes a new {@link TriggerMapping}.
     */
	public TriggerMapping() {
		super();
	}

	@Override
	public void export(Alarm object, VAlarm component, ICalParameters parameters, List<OXException> warnings) {
		Trigger value = object.getTrigger();
		if (null == value) {
			removeProperties(component, Property.TRIGGER);
		} else {
			net.fortuna.ical4j.model.property.Trigger property = component.getTrigger();
			if (null == property) {
				property = new net.fortuna.ical4j.model.property.Trigger();
				component.getProperties().add(property);
			}
			if (null != value.getDateTime()) {
				/*
				 * export as absolute date-time
				 */
				property.setDateTime(new DateTime(value.getDateTime()));
			} else if (null != value.getDuration()) {
				/*
				 * export as relative duration
				 */
				property.setDuration(new Dur(value.getDuration()));
				property.getParameters().removeAll(Parameter.RELATED);
				if (Related.END.equals(value.getRelated())) {
					property.getParameters().add(net.fortuna.ical4j.model.parameter.Related.END);
				} else if (Related.START.equals(value.getRelated())) {
					property.getParameters().add(net.fortuna.ical4j.model.parameter.Related.START);
				}
			}
		}
	}

	@Override
	public void importICal(VAlarm component, Alarm object, ICalParameters parameters, List<OXException> warnings) {
		net.fortuna.ical4j.model.property.Trigger property = component.getTrigger();
		if (null == property) {
			object.setTrigger(null);
		} else {
			Trigger value = new Trigger();
			if (null != property.getDateTime()) {
				/*
				 * import as absolute date-time
				 */
				value.setDateTime(property.getDateTime());
				value.setDuration(null);
				value.setRelated(null);
			} else if (null != property.getDuration()) {
				/*
				 * import as relative duration
				 */
				value.setDateTime(null);
                Dur dur = property.getDuration();
                value.setDuration(AlarmUtils.getDuration(dur.isNegative(), dur.getWeeks(), dur.getDays(), dur.getHours(), dur.getMinutes(), dur.getSeconds()));
				Parameter parameter = property.getParameter(Parameter.RELATED);
				if (null != parameter) {
					if (net.fortuna.ical4j.model.parameter.Related.END.equals(parameter)) {
						value.setRelated(Related.END);
                    } else if (net.fortuna.ical4j.model.parameter.Related.START.equals(parameter)) {
						value.setRelated(Related.START);
					} else {
						addConversionWarning(warnings, "TRIGGER", "Invalid \"RELATED\" parameter:" + parameter);
					}
				} else {
					value.setRelated(null);
				}
			}
			object.setTrigger(value);
		}
	}

}
