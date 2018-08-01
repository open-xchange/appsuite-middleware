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

package com.openexchange.chronos.ical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.time.TimeTools;

/**
 * {@link Bug59654Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.1
 */
public class Bug59654Test extends ICalTest {

    @Test
    public void testNewLines() throws Exception {
        /*
         * prepare event with attendee comment
         */
        Event event = new Event();
        event.setUid(UUID.randomUUID().toString());
        event.setStartDate(new DateTime(TimeTools.D("next sunday at 12:30").getTime()));
        event.setEndDate(new DateTime(TimeTools.D("next sunday at 13:30").getTime()));
        ExtendedProperties extendedProperties = new ExtendedProperties();
        List<ExtendedPropertyParameter> parameters = new ArrayList<>();
        parameters.add(new ExtendedPropertyParameter("X-CALENDARSERVER-ATTENDEE-REF", "urn:uuid:00000001-0000-1b22-00fc-c0e11e000003"));
        parameters.add(new ExtendedPropertyParameter("X-CALENDARSERVER-DTSTAMP", "20180528T121803Z"));
        String comment = "First line\nSecond Line\nThird Line";
        extendedProperties.add(new ExtendedProperty("X-CALENDARSERVER-ATTENDEE-COMMENT", comment, parameters));
        event.setExtendedProperties(extendedProperties);
        /*
         * check attendee comment after multiple import/export roundtrips
         */
        String exportedICal = exportEvent(event);
        Event importedEvent = importEvent(exportedICal);
        for (int i = 0; i < 10; i++) {
            exportedICal = exportEvent(importedEvent);
            importedEvent = importEvent(exportedICal);
        }
        ExtendedProperties importedExtendedProperties = importedEvent.getExtendedProperties();
        assertNotNull(importedExtendedProperties);
        assertEquals(extendedProperties.get("X-CALENDARSERVER-ATTENDEE-COMMENT"), importedExtendedProperties.get("X-CALENDARSERVER-ATTENDEE-COMMENT"));
    }

}
