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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Categories;

/**
 * {@link CategoriesMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CategoriesMapping extends AbstractICalMapping<VEvent, Event> {

	@Override
	public void export(Event object, VEvent component, ICalParameters parameters, List<OXException> warnings) {
		List<String> categories = object.getCategories();
		if (null == categories || 0 == categories.size()) {
			removeProperties(component, Property.CATEGORIES);
		} else {
			removeProperties(component, Property.CATEGORIES); // TODO: better merge?
			TextList textList = new TextList(categories.toArray(new String[categories.size()]));
			component.getProperties().add(new Categories(textList));
		}
	}

	@Override
	public void importICal(VEvent component, Event object, ICalParameters parameters, List<OXException> warnings) {
		PropertyList properties = component.getProperties(Property.CATEGORIES);
        if (null != properties && 0 < properties.size()) {
            List<String> categories = new ArrayList<String>();
            for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
                Categories property = (Categories) iterator.next();
                for (Iterator<?> i = property.getCategories().iterator(); i.hasNext();) {
                    categories.add(String.valueOf(i.next()));
                }
            }
            object.setCategories(categories);
        } else if (false == isIgnoreUnsetProperties(parameters)) {
			object.setCategories(null);
		}
	}

}
