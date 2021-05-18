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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.subscribe.google.parser.consumers;

import static com.openexchange.java.Autoboxing.i;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.function.BiConsumer;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Person;
import com.openexchange.groupware.container.Contact;

/**
 * {@link BirthdayConsumer} - Parses the birthday of the contact
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.1
 */
public class BirthdayConsumer implements BiConsumer<Person, Contact> {

    /**
     * Initialises a new {@link BirthdayConsumer}.
     */
    public BirthdayConsumer() {
        super();
    }

    @Override
    public void accept(Person person, Contact contact) {
        List<Birthday> birthdayList = person.getBirthdays();
        if (birthdayList == null || birthdayList.isEmpty()) {
            return;
        }
        Birthday birthday = birthdayList.get(0);
        Date birthdayDate = birthday.getDate();
        setBirthday(birthdayDate, contact);
    }

    /**
     * Sets the birthday {@link Date} to the contact
     * 
     * Google uses their own Date class to support different types of Dates, i.e.
     * 
     * A: 'A full date, with non-zero year, month and day values'
     * B: 'A month and day value, with a zero year, e.g. an anniversary'
     * C: 'A year on its own, with zero month and day values'
     * D: 'A year and month value, with a zero day, e.g. a credit card expiration date'
     * 
     * @see <a href="https://developers.google.com/resources/api-libraries/documentation/people/v1/java/latest/com/google/api/services/people/v1/model/Date.html">
     * 
     *      Therefore we need to parse this to a regular java.util.Date
     *      Currently we only support case A and B
     * 
     * @param birthdayDate The birthday {@link Date}
     * @param contact The {@link Contact}
     */
    private void setBirthday(Date birthdayDate, Contact contact) {
        if (birthdayDate == null) {
            return;
        }
        Integer year = birthdayDate.getYear();
        Integer month = birthdayDate.getMonth();
        Integer day = birthdayDate.getDay();
        if (month != null && i(month) > 0 && day != null && i(day) > 0) {
            LocalDate date;
            if (year == null || i(year) == 0) {
                date = LocalDate.of(1604, i(month), i(day));
            } else {
                date = LocalDate.of(i(year), i(month), i(day));
            }
            contact.setBirthday(java.util.Date.from(date.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant()));
        }
    }
}
