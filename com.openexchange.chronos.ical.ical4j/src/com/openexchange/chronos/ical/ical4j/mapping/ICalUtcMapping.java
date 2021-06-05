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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.util.Date;
import java.util.List;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.UtcProperty;

/**
 * {@link ICalUtcMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalUtcMapping<T extends Component, U> extends AbstractICalMapping<T, U> {

	private final String propertyName;

    /**
     * Initializes a new {@link ICalUtcMapping}.
     *
     * @param propertyName The name of the mapping's property
     */
	protected ICalUtcMapping(String propertyName) {
		super();
		this.propertyName = propertyName;
	}

	protected abstract Date getValue(U object);

	protected abstract void setValue(U object, Date value);

	protected abstract UtcProperty createProperty();

	@Override
	public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
		Date value = getValue(object);
        if (null == value) {
            removeProperties(component, propertyName);
        } else {
            UtcProperty property = (UtcProperty) component.getProperty(propertyName);
            if (null == property) {
                property = createProperty();
                component.getProperties().add(property);
            }
            property.setDateTime(new DateTime(value));
		}
	}

	@Override
	public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
		UtcProperty property = (UtcProperty) component.getProperty(propertyName);
        if (null != property) {
            setValue(object, property.getDate());
        } else if (false == isIgnoreUnsetProperties(parameters)) {
            setValue(object, null);
        }
	}

}
