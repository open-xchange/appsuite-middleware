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

import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.ical.ical4j.mapping.ICalTextMapping;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.property.Action;

/**
 * {@link ActionMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ActionMapping extends ICalTextMapping<VAlarm, Alarm> {

    /**
     * Initializes a new {@link ActionMapping}.
     */
	public ActionMapping() {
		super(Property.ACTION);
	}

	@Override
	protected String getValue(Alarm object) {
		return null != object.getAction() ? object.getAction().toString() : null;
	}

	@Override
	protected void setValue(Alarm object, String value) {
        object.setAction(new AlarmAction(value));
	}

	@Override
	protected Property createProperty() {
		return new Action();
	}

}
