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

import static net.fortuna.ical4j.model.Property.CLASS;
import static net.fortuna.ical4j.model.property.Clazz.CONFIDENTIAL;
import static net.fortuna.ical4j.model.property.Clazz.PRIVATE;
import static net.fortuna.ical4j.model.property.Clazz.PUBLIC;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Clazz;

/**
 * {@link Klass} - Represents the attribute converter for <code>"CLASS"</code> element.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Klass<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T, U> {

    public Klass() {
        super();
    }

    @Override
    public boolean isSet(final U cObj) {
        return cObj.containsPrivateFlag();
    }

    @Override
    public void emit(final Mode mode, final int index, final U cObj, final T component, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        if (cObj.getPrivateFlag()) {
            component.getProperties().add(PRIVATE);
        } else {
            component.getProperties().add(PUBLIC);
        }
    }

    @Override
    public boolean hasProperty(final T component) {
        return component.getProperty(CLASS) != null;
    }

    @Override
    public void parse(final int index, final T component, final U cObj, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        final Clazz clazz = (Clazz) component.getProperty(CLASS);
        // Parse non-empty value
        if (PRIVATE.equals(clazz) || CONFIDENTIAL.equals(clazz)) {
            cObj.setPrivateFlag(true);
        } else if (PUBLIC.equals(clazz)) {
            cObj.setPrivateFlag(false);
        } else {
            final String value = clazz.getValue();
            if (com.openexchange.java.Strings.isEmpty(value)) {
                warnings.add(new ConversionWarning(index, Code.EMPTY_CLASS, new Object[0]));
            } else {
                warnings.add(new ConversionWarning(index, Code.UNKNOWN_CLASS, value));
            }
        }
    }
}
