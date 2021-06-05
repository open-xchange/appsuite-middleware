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

package com.openexchange.contact.vcard.impl.mapping;

import static com.openexchange.java.Autoboxing.i;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.util.PartialDate;

/**
 * {@link AbstractDateMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractDateMapping<T extends DateOrTimeProperty> extends SimpleMapping<T> {

    /**
     * Initializes a new {@link AbstractDateMapping}.
     *
     * @param field The mapped contact column identifier
     * @param propertyClass The vCard property class
     * @param propertyName The affected vCard property name
     * @param contactFields The affected contact fields
     */
    protected AbstractDateMapping(int field, Class<T> propertyClass, String propertyName, ContactField... contactFields) {
        super(field, propertyClass, propertyName, contactFields);
    }

    protected abstract T newProperty();

    @Override
    protected T exportProperty(Contact contact, List<OXException> warnings) {
        T property = newProperty();
        exportProperty(contact, property, warnings);
        return property;
    }

    @Override
    protected void exportProperty(Contact contact, T property, List<OXException> warnings) {
        Object value = contact.get(field);
        if (null != value && Date.class.isInstance(value)) {
            /*
             * adjust date for serialization through ez-vcard (that uses local timezone)
             */
            Date adjustedDate = new Date(subtractTimeZoneOffset(((Date) value).getTime(), TimeZone.getDefault()));
            property.setDate(adjustedDate, false);
        } else {
            property.setDate(null, false);
        }
    }

    @Override
    protected void importProperty(T property, Contact contact, List<OXException> warnings) {
        Date value = property.getDate();
        if (null == value) {
            PartialDate partialDate = property.getPartialDate();
            if (partialDate == null) {
                contact.set(field, null);
                return;
            }

            value = toDate(partialDate, 1604, 0, 0, 0, 0, 0);
        }

        /*
         * adjust date after deserialization through ez-vcard (that uses local timezone)
         */
        Date adjustedDate = new Date(addTimeZoneOffset(value.getTime(), TimeZone.getDefault()));
        contact.set(field, adjustedDate);
    }

    protected static long addTimeZoneOffset(long date, TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

    protected static long subtractTimeZoneOffset(long date, TimeZone timeZone) {
        return null == timeZone ? date : date - timeZone.getOffset(date);
    }

    /**
     * Creates a Date based on a given PartialDate. Enriches the Date with given optional values, if they are missing in the PartialDate.
     *
     * @return
     */
    protected Date toDate(PartialDate pd, int optYear, int optMonth, int optDay, int optHour, int optMinute, int optSecond) {
        Calendar c = Calendar.getInstance();

        // @formatter:off
        c.set(pd.getYear() == null ? optYear : i(pd.getYear()),
              pd.getMonth() == null ? optMonth : i(pd.getMonth())-1,
              pd.getDate() == null ? optDay : i(pd.getDate()),
              pd.getHour() == null ? optHour : i(pd.getHour()),
              pd.getMinute() == null ? optMinute : i(pd.getMinute()),
              pd.getSecond() == null ? optSecond : i(pd.getSecond()));
        // @formatter:on

        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }
}
