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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.notify;

import java.io.ByteArrayInputStream;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.mail.mime.ContentType;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Bug16110Test extends ParticipantNotifyTest {
    protected String ical = "BEGIN:VCALENDAR\n"
        +"METHOD:REQUEST\n"
        +"PRODID:Microsoft CDO for Microsoft Exchange\n"
        +"VERSION:2.0\n"
        +"BEGIN:VTIMEZONE\n"
            +"TZID:(GMT+09.00) Tokyo/Osaka/Sapporo\n"
            +"X-MICROSOFT-CDO-TZID:20\n"
            +"BEGIN:STANDARD\n"
                +"DTSTART:16010101T000000\n"
                +"TZOFFSETFROM:+0900\n"
                +"TZOFFSETTO:+0900\n"
            +"END:STANDARD\n"
            +"BEGIN:DAYLIGHT\n"
                +"DTSTART:16010101T000000\n"
                +"TZOFFSETFROM:+0900\n"
                +"TZOFFSETTO:+0900\n"
            +"END:DAYLIGHT\n"
        +"END:VTIMEZONE\n"
        +"BEGIN:VEVENT\n"
        +"DTSTAMP:20100514T031141Z\n"
        +"DTSTART;TZID=\"(GMT+09.00) Tokyo/Osaka/Sapporo\":20100514T143000\n"
        +"SUMMARY:Updated: OX status Meeting\n"
        +"UID:040000008200E00074C5B7101A82E0080000000020EEAA4753F3CA01000000000000000\n"
        +" 0100000009A5B902A6C1C7E43AF1988197DCE07C2\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Ramesh G\n"
        +" opal\":MAILTO:Ramesh.Gopal@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Imai Tom\n"
        +" oko\":MAILTO:Tomoko.Imai@shinseibank.com\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"PengSeng\n"
        +"  Fong\":MAILTO:PengSeng.Fong@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Okuyama \n"
        +" Makoto\":MAILTO:Makoto.Okuyama@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Nakamura\n"
        +"  Nanako\":MAILTO:Nanako.Nakamura@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;X-REPLYTIME=20100514T02\n"
        +" 5600Z;RSVP=FALSE;CN=\"Suresh Bollu\":MAILTO:Suresh.Bollu@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Zhuang, \n"
        +" Xiaoye\":MAILTO:xiaoye.zhuang@somefinancial.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Anand Ku\n"
        +" mbhare\":MAILTO:Anand.Kumbhare@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Praveen \n"
        +" Kumar\":MAILTO:Praveen.Kumar@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Amogh Ku\n"
        +" lkarni\":MAILTO:Amogh.Kulkarni@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Carsten \n"
        +" Hoeger\":MAILTO:choeger@open-xchange.com\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Mathews \n"
        +" Jacob\":MAILTO:Mathews.Jacob@some-it.invalid\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Holger A\n"
        +" chtziger\":MAILTO:Holger.Achtziger@open-xchange.com\n"
        +"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;X-REPLYTIME=20100514T02\n"
        +" 5400Z;RSVP=FALSE;CN=\"Ido Nansyuu\":MAILTO:Nansyuu.Ido@some-it.invalid\n"
        +"ATTENDEE;ROLE=OPT-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Pieter F\n"
        +" ranken\":MAILTO:Pieter.Franken@somebank.com\n"
        +"ATTENDEE;ROLE=OPT-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Yamazaki\n"
        +"  Chika\":MAILTO:Chika.Yamazaki@some-it.invalid\n"
        +"ATTENDEE;ROLE=OPT-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=FALSE;CN=\"Wenlong \n"
        +" Wu\":MAILTO:Wenlong.Wu@some-it.invalid\n"
        +"ORGANIZER;CN=\"ChinShimVeron Koh\":MAILTO:ChinShimVeron.Koh@some-it.invalid\n"
        +"LOCATION:\n"
        +"DTEND;TZID=\"(GMT+09.00) Tokyo/Osaka/Sapporo\":20100514T153000\n"
        +"DESCRIPTION:Please note that the meeting is today 14 May.\\N\\NDear all\\,\\N\\N\n"
        +" We are targeting to migrate 300 users to Open Xchange in Production on Mon\n"
        +" day. Please attend the meeting for activity and status updates.\\N\\NPune te\n"
        +" am\\, please help to connect VC.\\NCan someone help to book the meeting room\n"
        +" s in Japan as I am unable to book any from my side.\\N\\N\\N[Date and Venue]\\n"
        +" N14 May 2010 2:30-3:30 JST   @Meeting Room 2/Singapore\\N\\N[Agenda]\\N1) Perfo\n"
        +" rmance issue fix status\\N2) H/W availability\\N3) Monitoring & Security\\N4)\n"
        +"  Migration plan including communication plan\\N5) Weekend Support/Personnel\n"
        +"  required\\N\\NThanks & Regards\\,\\NVeron Koh\\N \\NProject Management Unit (PM\n"
        +" U)\\NPhone: +65 6403 5049 (External)\\NPhone: 35049 (Internal)\\NEmail:  Chin\n"
        +" ShimVeron.Koh@some-it.invalid\\N\n"
        +"SEQUENCE:0\n"
        +"PRIORITY:5\n"
        +"CLASS:\n"
        +"CREATED:20100514T031142Z\n"
        +"LAST-MODIFIED:20100514T031143Z\n"
        +"STATUS:CONFIRMED\n"
        +"TRANSP:OPAQUE\n"
        +"X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n"
        +"X-MICROSOFT-CDO-INSTTYPE:0\n"
        +"X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY\n"
        +"X-MICROSOFT-CDO-ALLDAYEVENT:FALSE\n"
        +"X-MICROSOFT-CDO-IMPORTANCE:1\n"
        +"X-MICROSOFT-CDO-OWNERAPPTID:1968068570\n"
        +"X-MICROSOFT-CDO-APPT-SEQUENCE:1\n"
        +"X-MICROSOFT-CDO-ATTENDEE-CRITICAL-CHANGE:20100514T031141Z\n"
        +"X-MICROSOFT-CDO-OWNER-CRITICAL-CHANGE:20100514T031141Z\n"
        +"BEGIN:VALARM\n"
        +"ACTION:DISPLAY\n"
        +"DESCRIPTION:REMINDER\n"
        +"TRIGGER;RELATED=START:-PT00H15M00S\n"
        +"END:VALARM\n"
        +"END:VEVENT\n"
        +"END:VCALENDAR\n";



    public void testAddICalAttachment() throws Exception{
        final AppointmentState state = new AppointmentState(null, null, null);
        final TestMailObject mailObject = new TestMailObject();
        final Appointment obj = convertFromICal(new ByteArrayInputStream(ical.getBytes()) );

        state.modifyExternal(mailObject, obj, session);

        final ContentType ct = mailObject.getTheContentType();

        assertEquals(ct.getCharsetParameter(),"utf-8");
        assertEquals(ct.getPrimaryType(), "text");
        assertEquals(ct.getSubType(), "calendar");

        assertEquals("appointment.ics", mailObject.getTheFilename());

        try {

            final Appointment obj2 = convertFromICal(mailObject.getTheInputStream());

            assertEquals("Start date should match", obj.getStartDate().getTime(), obj2.getStartDate().getTime());
            assertEquals("End date should match", obj.getEndDate().getTime(), obj2.getEndDate().getTime());
            assertEquals("Title should match", obj.getTitle(), obj2.getTitle());
            assertEquals("Participant amount should match", obj.getParticipants().length, obj2.getParticipants().length);
            assertEquals("Organizer should match", obj.getOrganizer(), obj2.getOrganizer());
        } catch (final Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }

    }
}
