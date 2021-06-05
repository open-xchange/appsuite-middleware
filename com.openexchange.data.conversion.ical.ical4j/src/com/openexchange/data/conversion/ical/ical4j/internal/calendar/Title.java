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

import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Summary;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Title<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {

    public Title() {
        super();
    }

    @Override
    public boolean isSet(final U calendarObject) {
        return calendarObject.containsTitle();
    }

    @Override
    public void emit(final Mode mode, final int index, final U calendarObject, final T calendarComponent, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        calendarComponent.getProperties().add(new Summary(calendarObject.getTitle()));
    }

    @Override
    public boolean hasProperty(final T calendarComponent) {
        return null != calendarComponent.getProperty(Property.SUMMARY);
    }

    @Override
    public void parse(final int index, final T calendarComponent, final U calendarObject, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        int descMaxLength = 255; //hack for 20972
        String desc = calendarComponent.getProperty(Property.SUMMARY).getValue();
        if (desc.length() > descMaxLength) {
            desc = desc.substring(0, descMaxLength);
            warnings.add(new ConversionWarning(index, ConversionWarning.Code.TRUNCATION_WARNING, desc));
        }
        calendarObject.setTitle(desc);
    }
}
