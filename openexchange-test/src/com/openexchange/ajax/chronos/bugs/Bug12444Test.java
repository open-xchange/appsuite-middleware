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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.Category;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link Bug12444Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public final class Bug12444Test extends AbstractChronosTest {

    /**
     * Default constructor.
     *
     * @param name test name.
     */
    public Bug12444Test() {
        super();
    }

    @Test
    public void testExternalWithoutEmail() throws Throwable {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug12444Test", folderId);
        List<Attendee> attendees = event.getAttendees();
        Attendee externalWithoutEmail = new Attendee();
        externalWithoutEmail.setCn("External");
        externalWithoutEmail.setCuType(CuTypeEnum.INDIVIDUAL);
        attendees.add(externalWithoutEmail);
        ChronosCalendarResultResponse response = defaultUserApi.getChronosApi().createEvent(folderId, event, Boolean.FALSE, null, Boolean.FALSE, null, null, null, Boolean.FALSE, null);

        assertNotNull("Server responded not with expected exception.", response.getError());
        assertEquals("Wrong exception code.", CalendarExceptionCodes.INVALID_CALENDAR_USER.getPrefix()+"-"+CalendarExceptionCodes.INVALID_CALENDAR_USER.getNumber(), response.getCode());
        assertEquals("Wrong exception category.", Category.CATEGORY_USER_INPUT.getType().getName(), response.getCategories());
    }

}
