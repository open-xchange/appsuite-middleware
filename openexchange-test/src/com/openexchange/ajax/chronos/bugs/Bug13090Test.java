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

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link Bug13090Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug13090Test extends AbstractChronosTest {

    /**
     * Initializes a new {@link Bug13090Test}.
     */
    public Bug13090Test() {
        super();
    }

    @Test
    public void testSeriesMove() throws Exception {
        EventData series = EventFactory.createSeriesEvent(getCalendaruser(), "Bug13090Test", 4, folderId);
        EventData createEvent = eventManager.createEvent(series, true);
        try {
            ChronosCalendarResultResponse response = defaultUserApi.getChronosApi().moveEvent(folderId, createEvent.getId(), L(eventManager.getLastTimeStamp()), defaultFolderId, Boolean.FALSE, null, null, Boolean.FALSE, null, null, null, null, Boolean.FALSE);
            eventManager.handleUpdate(response, true);
        } catch (ChronosApiException e) {
            assertEquals("Wrong exception code.", CalendarExceptionCodes.MOVE_SERIES_NOT_SUPPORTED.create().getErrorCode(), e.getErrorCode());
        }
    }

}
