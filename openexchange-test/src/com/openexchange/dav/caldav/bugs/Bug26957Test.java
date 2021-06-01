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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.dav.caldav.CalDAVTest;

/**
 * {@link Bug26957Test}
 *
 * NPE in caldav caused by Evolution
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug26957Test extends CalDAVTest {

    @Test
    public void testPutICalWithoutVEvent() throws Exception {
        /*
         * try to create appointment
         */
        String uid = randomUID();
        String iCal = "BEGIN:VCALENDAR\nCALSCALE:GREGORIAN\nVERSION:2.0\nMETHOD:PUBLISH\nPRODID:-//Apple Inc.//Mac OS X 10.8.4//EN\nEND:VCALENDAR\n";
        assertEquals("response code wrong", HttpServletResponse.SC_FORBIDDEN, putICal(uid, iCal));
    }

}
