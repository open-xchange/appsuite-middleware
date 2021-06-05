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

import static com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools.toDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.DateProperty;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class LastModified <T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {

    public LastModified() {
        super();
    }

    @Override
    public boolean isSet(final U calendar) {
        return calendar.containsLastModified() && calendar.getLastModified() != null;
    }

    @Override
    public void emit(final Mode mode, final int index, final U calendar, final T t, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        final net.fortuna.ical4j.model.property.LastModified lastModified = new net.fortuna.ical4j.model.property.LastModified();
        lastModified.setDate(toDateTime(calendar.getLastModified()));
        t.getProperties().add(lastModified);
    }

    @Override
    public boolean hasProperty(final T t) {
        return null != t.getProperty("LAST-MODIFIED");
    }

    @Override
    public void parse(final int index, final T component, final U cObj, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        final DateProperty property = (DateProperty) component.getProperty("LAST-MODIFIED");
        final Date lastModified = ParserTools.parseDate(component, property, timeZone);
        cObj.setLastModified(lastModified);
    }
}
