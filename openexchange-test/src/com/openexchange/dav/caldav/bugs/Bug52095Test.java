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

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.jdom2.JDOMException;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.openexchange.ajax.user.UserResolver;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.ICalUtils;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.PermissionTools;

/**
 * {@link Bug52095Test}
 *
 * private appointments not visible in shared calendar
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug52095Test extends CalDAVTest {

    private CalendarTestManager manager2;
    private FolderObject sharedFolder;
    private String scheduleOutboxURL;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        scheduleOutboxURL = getScheduleOutboxURL();
        /*
         * as user b, create subfolder shared to user a
         */
        manager2 = new CalendarTestManager(getClient2());
        manager2.setFailOnError(true);
        sharedFolder = new FolderObject();
        sharedFolder.setModule(FolderObject.CALENDAR);
        sharedFolder.setParentFolderID(manager2.getPrivateFolder());
        sharedFolder.setPermissions(
            PermissionTools.P(Integer.valueOf(getClient2().getValues().getUserId()),
            PermissionTools.ADMIN, Integer.valueOf(getClient().getValues().getUserId()), "vr")
        );
        sharedFolder.setFolderName(randomUID());
        ftm.setClient(getClient2());
        sharedFolder = ftm.insertFolderOnServer(sharedFolder);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != manager2) {
                manager2.cleanUp();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testShowPrivateInFreeBusy() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next week at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Date startTime = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        Date endTime = calendar.getTime();
        calendar.add(Calendar.DATE, -1);
        Date from = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        Date until = calendar.getTime();
        /*
         * create private appointment for user b on server
         */
        String uid = randomUID();
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug52095Test");
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(startTime);
        appointment.setEndDate(endTime);
        appointment.setParentFolderID(manager2.getPrivateFolder());
        appointment.setPrivateFlag(true);
        manager2.insert(appointment);
        /*
         * query free/busy as user a
         */
        List<String> attendees = new ArrayList<String>();
        attendees.add(getClient2().getValues().getDefaultAddress());
        String freeBusyICal = generateFreeBusyICal(from, until, attendees);
        Document document = this.postFreeBusy(freeBusyICal, attendees);
        assertNotNull("got no free/busy result", document);
        /*
         * verify free/busy times
         */
        List<FreeBusyResponse> freeBusyResponses = FreeBusyResponse.create(document);
        assertEquals("got invalid number of responses", 1, freeBusyResponses.size());
        FreeBusyResponse freeBusyResponse = freeBusyResponses.get(0);
        assertTrue("recipient wrong", null != freeBusyResponse.recipient && freeBusyResponse.recipient.contains(attendees.get(0)));
        assertEquals("request status wrong", "2.0;Success", freeBusyResponse.requestStatus);
        assertEquals("response description wrong", "OK", freeBusyResponse.responseDescription);
        assertNotNull("got no claendar data", freeBusyResponse.calendarData);
        ICalResource iCalResource = new ICalResource(freeBusyResponse.calendarData);
        Map<String, List<FreeBusySlot>> freeBusy = extractFreeBusy(iCalResource);
        assertEquals("invalid number of attendees", 1, freeBusy.size());
        List<FreeBusySlot> freeBusySlots = freeBusy.get(attendees.get(0));
        assertNotNull("no free/busy slots found", freeBusySlots);
        FreeBusySlot matchingSlot = null;
        for (FreeBusySlot freeBusySlot : freeBusySlots) {
            if (matches(appointment, freeBusySlot)) {
                matchingSlot = freeBusySlot;
                break;
            }
        }
        assertNotNull("no matching free/busy slot found for appointment " + appointment, matchingSlot);
    }

    @Test
    public void testShowPrivateInSharedFolder() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next week at noon", TimeZone.getTimeZone("Europe/Berlin")));
        Date startTime = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        Date endTime = calendar.getTime();
        /*
         * fetch sync token for later synchronization
         */
        String sharedFolderID = String.valueOf(sharedFolder.getObjectID());
        SyncToken syncToken = new SyncToken(fetchSyncToken(sharedFolderID));
        /*
         * create private appointment in shared folder for user b on server
         */
        String uid = randomUID();
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug52095Test");
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(startTime);
        appointment.setEndDate(endTime);
        appointment.setParentFolderID(sharedFolder.getObjectID());
        appointment.setPrivateFlag(true);
        manager2.insert(appointment);
        /*
         * verify appointment on client as user a
         */
        ICalResource iCalResource = get(sharedFolderID, uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        String classification = iCalResource.getVEvent().getPropertyValue("CLASS");
        assertTrue("CLASS wrong", "PRIVATE".equals(classification) || "CONFIDENTIAL".equals(classification));
        assertNotEquals("SUMMARY is readable", appointment.getTitle(), iCalResource.getVEvent().getSummary());
        /*
         * verify appointment on client as user a via sync-collection report
         */
        Map<String, String> eTags = syncCollection(syncToken, sharedFolderID).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        iCalResource = assertContains(appointment.getUid(), calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        classification = iCalResource.getVEvent().getPropertyValue("CLASS");
        assertTrue("CLASS wrong", "PRIVATE".equals(classification) || "CONFIDENTIAL".equals(classification));
        assertNotEquals("SUMMARY is readable", appointment.getTitle(), iCalResource.getVEvent().getSummary());
    }

    private Map<String, List<FreeBusySlot>> extractFreeBusy(ICalResource iCalResource) throws ParseException {
        Map<String, List<FreeBusySlot>> freeBusy = new HashMap<String, List<FreeBusySlot>>();
        assertNotNull("No VFREEBUSY in iCal found", iCalResource.getVFreeBusys());
        for (Component freeBusyComponent : iCalResource.getVFreeBusys()) {
            Property attendee = freeBusyComponent.getProperty("ATTENDEE");
            assertNotNull("VFREEBUSY without ATTENDEE", attendee);
            ArrayList<FreeBusySlot> freeBusySlots = new ArrayList<Bug52095Test.FreeBusySlot>();
            List<Property> properties = freeBusyComponent.getProperties("FREEBUSY");
            for (Property property : properties) {
                String fbType = property.getAttribute("FBTYPE");
                List<Date[]> periods = ICalUtils.parsePeriods(property);
                for (Date[] dates : periods) {
                    freeBusySlots.add(new FreeBusySlot(dates[0], dates[1], fbType));
                }
            }
            String mail = attendee.getValue();
            if (mail.startsWith("mailto:")) {
                mail = mail.substring(7);
            }
            freeBusy.put(mail, freeBusySlots);
        }
        return freeBusy;
    }

    private static final class FreeBusyResponse {

        public String recipient, requestStatus, responseDescription, calendarData;

        public static List<FreeBusyResponse> create(Document document) throws JDOMException {
            List<FreeBusyResponse> fbResponses = new ArrayList<Bug52095Test.FreeBusyResponse>();
            NodeList nodes = document.getElementsByTagNameNS(PropertyNames.RESPONSE_CALDAV.getNamespace().getURI(), PropertyNames.RESPONSE_CALDAV.getName());
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                fbResponses.add(create(element));
            }
            return fbResponses;
        }

        public static FreeBusyResponse create(Element response) throws JDOMException {
            FreeBusyResponse fbResponse = new FreeBusyResponse();
            fbResponse.recipient = extractChildTextContent(PropertyNames.HREF, response);
            fbResponse.requestStatus = extractChildTextContent(PropertyNames.REQUEST_STATUS, response);
            fbResponse.responseDescription = extractChildTextContent(PropertyNames.RESPONSEDESCRIPTION, response);
            fbResponse.calendarData = extractChildTextContent(PropertyNames.CALENDAR_DATA, response);
            return fbResponse;
        }

    }

    private static final class FreeBusySlot {

        public Date start, end;
        public String fbType;

        public FreeBusySlot(Date start, Date end, String fbType) {
            this.start = start;
            this.end = end;
            this.fbType = fbType;
        }

    }

    private static boolean matches(Appointment appointment, FreeBusySlot freeBusySlot) {
        if (freeBusySlot.start.equals(appointment.getStartDate()) && freeBusySlot.end.equals(appointment.getEndDate())) {
            if (Appointment.FREE == appointment.getShownAs()) {
                return "FREE".equals(freeBusySlot.fbType);
            } else if (Appointment.ABSENT == appointment.getShownAs()) {
                return "BUSY-UNAVAILABLE".equals(freeBusySlot.fbType);
            } else if (Appointment.TEMPORARY == appointment.getShownAs()) {
                return "BUSY-TENTATIVE".equals(freeBusySlot.fbType);
            } else {
                return "BUSY".equals(freeBusySlot.fbType);
            }
        }
        return false;
    }

    private Document postFreeBusy(String freeBusyICal, List<String> recipients) throws Exception {
        PostMethod post = new PostMethod(super.getWebDAVClient().getBaseURI() + this.scheduleOutboxURL);
        post.addRequestHeader("Originator", "mailto:" + super.getAJAXClient().getValues().getDefaultAddress());
        if (null != recipients && 0 < recipients.size()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mailto:").append(recipients.get(0));
            for (int i = 0; i < recipients.size(); i++) {
                stringBuilder.append(", ").append("mailto:").append(recipients.get(i));
            }
            post.addRequestHeader("Recipient", stringBuilder.toString());
        }
        post.setRequestEntity(new StringRequestEntity(freeBusyICal, "text/calendar", null));
        String response = super.getWebDAVClient().doPost(post);
        assertNotNull("got no response", response);
        return DomUtil.parseDocument(new ByteArrayInputStream(response.getBytes()));
    }

    private String getScheduleOutboxURL() throws Exception {
        /*
         * discover principal URL
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
        PropFindMethod propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + "/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 1 == responses.length);
        String principalURL = extractHref(PropertyNames.CURRENT_USER_PRINCIPAL, responses[0]);
        assertNotNull("got no principal URL", principalURL);
        /*
         * discover schedule-outbox-url
         */
        props = new DavPropertyNameSet();
        props.add(PropertyNames.SCHEDULE_OUTBOX_URL);
        propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + principalURL, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 1 == responses.length);
        String scheduleOutboxURL = extractHref(PropertyNames.SCHEDULE_OUTBOX_URL, responses[0]);
        assertNotNull("got no schedule-outbox URL", scheduleOutboxURL);
        return scheduleOutboxURL;
    }

    protected String generateFreeBusyICal(Date from, Date until, List<String> attendees) throws OXException, IOException, JSONException {
        String organizerMail = new UserResolver(getAJAXClient()).getUser(getAJAXClient().getValues().getUserId()).getMail();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("BEGIN:VCALENDAR").append("\r\n").append("CALSCALE:GREGORIAN").append("\r\n").append("VERSION:2.0").append("\r\n").append("METHOD:REQUEST").append("\r\n").append("PRODID:-//Apple Inc.//iCal 5.0.2//EN").append("\r\n").append("BEGIN:VFREEBUSY").append("\r\n").append("UID:").append(randomUID()).append("\r\n").append("DTEND:").append(formatAsUTC(until)).append("\r\n");
        for (String attendee : attendees) {
            stringBuilder.append("ATTENDEE:mailto:").append(attendee).append("\r\n");
        }
        stringBuilder.append("DTSTART:").append(formatAsUTC(from)).append("\r\n").append("X-CALENDARSERVER-MASK-UID:").append(randomUID()).append("\r\n").append("DTSTAMP:").append(formatAsUTC(new Date())).append("\r\n").append("ORGANIZER:mailto:").append(organizerMail).append("\r\n").append("END:VFREEBUSY").append("\r\n").append("END:VCALENDAR").append("\r\n");
        return stringBuilder.toString();
    }

}
