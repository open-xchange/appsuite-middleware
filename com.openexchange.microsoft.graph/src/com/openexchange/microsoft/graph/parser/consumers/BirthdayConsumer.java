/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2018-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.microsoft.graph.parser.consumers;

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
 * @since v7.10.1
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

    /*
     * (non-Javadoc)
     * 
     * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
     */
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
