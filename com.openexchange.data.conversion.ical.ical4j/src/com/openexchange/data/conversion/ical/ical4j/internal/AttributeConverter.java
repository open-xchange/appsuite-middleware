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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import net.fortuna.ical4j.model.component.CalendarComponent;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface AttributeConverter<T extends CalendarComponent, U extends CalendarObject> {

    /**
     * Checks if the attribute is defined in the task.
     * @param task Task object.
     * @return <code>true</code> if the attribute is defined.
     */
    boolean isSet(U u);

    void emit(Mode mode, int index, U u, T t, List<ConversionWarning> warnings, Context ctx, Object... args) throws ConversionError;

    boolean hasProperty(T t);

    void parse(int index, T t, U u, TimeZone timeZone, Context ctx, List<ConversionWarning> warnings) throws ConversionError;

    public void verify(int index, U object, List<ConversionWarning> warnings) throws ConversionError;
}
