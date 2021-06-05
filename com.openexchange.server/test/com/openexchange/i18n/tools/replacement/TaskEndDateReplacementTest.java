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

package com.openexchange.i18n.tools.replacement;

import static org.junit.Assert.assertEquals;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.java.util.TimeZones;

/**
 * {@link TaskEndDateReplacementTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TaskEndDateReplacementTest {

    @Test
    public final void testGetReplacement() {
        Locale locale = Locale.ENGLISH;
        TimeZone tz = TimeZones.UTC;
        Calendar cal = new GregorianCalendar(tz, locale);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        EndDateReplacement replacement = new EndDateReplacement(cal.getTime(), true, true, locale, tz);
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        String expected = String.format(locale, Notifications.FORMAT_DUE_DATE, format.format(cal.getTime()));
        assertEquals("Due date for tasks is printed wrong in task notification mails.", expected, replacement.getReplacement());
    }
}
