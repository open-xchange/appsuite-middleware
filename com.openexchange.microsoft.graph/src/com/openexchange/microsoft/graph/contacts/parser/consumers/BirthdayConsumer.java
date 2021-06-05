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

package com.openexchange.microsoft.graph.contacts.parser.consumers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BiConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;

/**
 * {@link BirthdayConsumer} - Parses the birthday of the contact
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class BirthdayConsumer implements BiConsumer<JSONObject, Contact> {

    private static final Logger LOG = LoggerFactory.getLogger(BirthdayConsumer.class);

    /**
     * The birthday {@link Date} format
     * 
     * @see <a href="https://developers.google.com/contacts/v3/reference#gcBirthday">gContact:birthday</a>
     */
    private final static String dateFormatPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Thread local {@link SimpleDateFormat} using "yyyy-MM-ddTHH:mm:ssZ" as pattern.
     */
    private static final ThreadLocal<SimpleDateFormat> BIRTHDAY_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
            dateFormat.setTimeZone(TimeZones.UTC);
            return dateFormat;
        }
    };

    /**
     * Initialises a new {@link BirthdayConsumer}.
     */
    public BirthdayConsumer() {
        super();
    }

    @SuppressWarnings("unused")
    @Override
    public void accept(JSONObject t, Contact u) {
        if (!t.hasAndNotNull("birthday")) {
            return;
        }
        String birthday = t.optString("birthday");
        try {
            u.setBirthday(BIRTHDAY_FORMAT.get().parse(birthday));
        } catch (ParseException e) {
            LOG.warn("Unable to parse '{}' as a birthday.", birthday);
        }
    }
}
