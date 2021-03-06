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

package com.openexchange.data.conversion.ical.ical4j;

import com.openexchange.data.conversion.ical.ICalItem;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Uid;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ICal4jItem implements ICalItem {

    private final CalendarComponent component;

    public ICal4jItem(final CalendarComponent component) {
        super();
        this.component = component;
    }

    @Override
    public String getUID() {
        final Property property = component.getProperty(Property.UID);
        return null == property ? null : property.getValue();
    }

    @Override
    public void setUID(final String value) {
        if (component.getProperties().contains(Property.UID)) {
            ((Uid) component.getProperty(Property.UID)).setValue(value);
        } else {
            component.getProperties().add(new Uid(value));
        }
    }
}
