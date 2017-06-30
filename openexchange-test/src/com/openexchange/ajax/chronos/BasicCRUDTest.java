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

package com.openexchange.ajax.chronos;

import static org.junit.Assert.assertNull;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.EventConflictResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import edu.emory.mathcs.backport.java.util.Collections;
import net.fortuna.ical4j.model.property.Attendee;

/**
 *
 * {@link BasicCRUDTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicCRUDTest extends AbstractChronosTest {

    @SuppressWarnings("unchecked")
    private EventData createSingleEvent() throws URISyntaxException {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.setCalAddress(new URI("mailto:" + this.testUser.getLogin()));
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(System.currentTimeMillis());
        singleEvent.setEndDate(System.currentTimeMillis() + 5000);
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);
        return singleEvent;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testCreateSingle() throws ApiException, URISyntaxException {
        EventConflictResponse createEvent = api.createEvent(session, "cal://0/54", createSingleEvent(), false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNull(createEvent.getData());
        EventId eventId = new EventId();
        eventId.setId(createEvent.getData().getId());
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, createEvent.getData().getId(), null, null, null);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNull(createEvent.getData());
        EventUtil.compare(createEvent.getData(), eventResponse.getData());
    }
}
