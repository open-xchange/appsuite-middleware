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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;

/**
 * {@link Bug55068Test}
 *
 * accepting an appointment using a mobile (android) phone deletes the assigned resource
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug55068Test extends CalDAVTest {

    @Test
    public void testManagedAttachmentDisposition() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create appointment on server & attach a file attachment
         */
        String uid = randomUID();
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setUid(uid);
        appointment.setTitle("Bug55068Test");
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(TimeTools.D("next friday at 2 pm", TimeZone.getTimeZone("Europe/Berlin")));
        appointment.setEndDate(TimeTools.D("next friday at 3 pm", TimeZone.getTimeZone("Europe/Berlin")));
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        appointment = catm.insert(appointment);
        getClient().execute(new AttachRequest(appointment, "test.txt", new ByteArrayInputStream(randomUID().getBytes()), "text/plain")).getId();
        /*
         * verify appointment on client as user a
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        Property attachProperty = iCalResource.getVEvent().getProperty("ATTACH");
        assertNotNull(attachProperty);
        /*
         * check plain GET response on attachment resource
         */
        GetMethod get = null;
        try {
            get = new GetMethod(attachProperty.getValue());
            Assert.assertEquals("response code wrong", StatusCodes.SC_OK, getWebDAVClient().executeMethod(get));
            Header responseHeader = get.getResponseHeader("Content-Disposition");
            assertNotNull(responseHeader);
            assertTrue(responseHeader.getValue().toLowerCase().startsWith("attachment"));
        } finally {
            release(get);
        }
    }

}
