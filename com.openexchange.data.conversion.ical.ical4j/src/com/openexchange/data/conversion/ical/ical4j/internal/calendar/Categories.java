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
package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Categories<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {
    @Override
    public boolean isSet(final U calendar) {
        return calendar.containsCategories();
    }

    @Override
    public void emit(final Mode mode, final int index, final U u, final T t, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        final String categories = u.getCategories();
        if (null == categories){
            return;
        }
        t.getProperties().add(new net.fortuna.ical4j.model.property.Categories(categories));
    }

    @Override
    public boolean hasProperty(final T t) {
        return true;
    }

    @Override
    public void parse(final int index, final T component, final U cObj, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
       final PropertyList categoriesList = component.getProperties("CATEGORIES");
        final StringBuilder bob = new StringBuilder();
        for(int i = 0, size = categoriesList.size(); i < size; i++) {
            final net.fortuna.ical4j.model.property.Categories categories = (net.fortuna.ical4j.model.property.Categories) categoriesList.get(i);
            for(final Iterator<Object> catObjects = categories.getCategories().iterator(); catObjects.hasNext();) {
                bob.append(catObjects.next()).append(',');
            }
        }
        if (bob.length() > 0) {
            bob.setLength(bob.length()-1);
        }
        String value = bob.toString();
        cObj.setCategories(Strings.isEmpty(value) ? null : value);
    }
}
