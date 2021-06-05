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

import static com.openexchange.data.conversion.ical.ical4j.internal.ParserTools.isDateOrMidnight;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.XProperty;

/**
 * {@link XMicrosoftCdoAlldayEvent}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class XMicrosoftCdoAlldayEvent<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T, U> {

    public static final String property = "X-MICROSOFT-CDO-ALLDAYEVENT";

    @Override
    public boolean isSet(U calendar) {
        if (calendar instanceof Appointment) {
            return ((Appointment) calendar).getFullTime();
        }
        return false;
    }

    @Override
    public void emit(Mode mode, int index, U calendar, T component, List<ConversionWarning> warnings, Context ctx, Object... args) throws ConversionError {
        if (Boolean.FALSE.equals(calendar.getProperty("com.openexchange.data.conversion.ical.useXMicrosoftCDOAllDayEvent"))) {
            return;
        }
        if (calendar instanceof Appointment) {
            if (((Appointment) calendar).getFullTime()) {
                component.getProperties().add(new XProperty("X-MICROSOFT-CDO-ALLDAYEVENT", "TRUE"));
            }
        }
    }

    @Override
    public boolean hasProperty(T component) {
        return component.getProperty(property) != null;
    }

    @Override
    public void parse(int index, T component, U calendar, TimeZone timeZone, Context ctx, List<ConversionWarning> warnings) throws ConversionError {
        if (Boolean.FALSE.equals(calendar.getProperty("com.openexchange.data.conversion.ical.useXMicrosoftCDOAllDayEvent"))) {
            return;
        }
        if (component.getProperty(property).getValue().equalsIgnoreCase("true")) {
            if (calendar instanceof Appointment) {
                if (isDateOrMidnight(component.getProperty(Property.DTSTART)) && isDateOrMidnight(component.getProperty(Property.DTEND))) {
                    ((Appointment) calendar).setFullTime(true);
                }
            }
        }
    }

}
