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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import ezvcard.property.Birthday;

/**
 * {@link BirthdayMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BirthdayMapping extends AbstractDateMapping<Birthday> {

    private static final String OMMIT_YEAR = "X-APPLE-OMIT-YEAR";

    /**
     * Initializes a new {@link BirthdayMapping}.
     */
    public BirthdayMapping() {
        super(Contact.BIRTHDAY, Birthday.class, "BIRTHDAY", ContactField.BIRTHDAY);
    }

    @Override
    protected Birthday newProperty() {
        return new Birthday((Date) null);
    }

    @Override
    protected void exportProperty(Contact contact, Birthday property, List<OXException> warnings) {
        Object value = contact.get(field);
        if (null != value && Date.class.isInstance(value)) {
            /*
             * adjust date for serialization through ez-vcard (that uses local timezone)
             */
            Date adjustedDate = new Date(subtractTimeZoneOffset(((Date) value).getTime(), TimeZone.getDefault()));
            adjustedDate = adjustForNoYear(adjustedDate, property);
            property.setDate(adjustedDate, false);
        } else {
            property.setDate(null, false);
        }
    }

    /**
     * Maps the years 1 (legacy OX implementation) and 1604 (the apple way) to 1604 and adds the "X-APPLE-OMIT-YEAR" parameter.
     *
     * @param adjustedDate
     * @param property
     * @return
     */
    private Date adjustForNoYear(Date adjustedDate, Birthday property) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(adjustedDate);
        if (calendar.get(Calendar.YEAR) != 1 && calendar.get(Calendar.YEAR) != 1604) {
            return adjustedDate;
        }
        property.addParameter(OMMIT_YEAR, "1604");
        calendar.set(Calendar.YEAR, 1604);
        return new Date(calendar.getTimeInMillis());
    }

}
