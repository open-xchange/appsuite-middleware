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
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.groupware.container.CalendarObject;
import net.fortuna.ical4j.model.component.CalendarComponent;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public abstract class AbstractVerifyingAttributeConverter<T extends CalendarComponent, U extends CalendarObject> implements AttributeConverter<T, U >{

    /**
     * Default constructor.
     */
    protected AbstractVerifyingAttributeConverter() {
        super();
    }

    private ObjectVerifier<U> verifier;

    public void setVerifier(final ObjectVerifier<U> verifier) {
        this.verifier = verifier;
    }

    @Override
    public void verify(final int index, final U object, final List<ConversionWarning> warnings) throws ConversionError {
        if (null == verifier) {
            return;
        }
        this.verifier.verify(index, object, warnings);
    }

    protected boolean isSet(final U calendarObject, final int value) {
        return calendarObject.contains(value) && calendarObject.get(value) !=null;
    }

}
