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

package com.openexchange.data.conversion.ical;

import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.ldap.User;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ICalParserBugTests extends AbstractICalParserTest {

    // Bug 11987
    public void testMultipleCalendars() throws ConversionError {
        final Date start1 = D("24/02/1981 10:00");
        final Date end1 = D("24/02/1981 12:00");

        final Date start2 = D("24/02/1981 10:00");
        final Date end2 = D("24/02/1981 12:00");

        final StringBuilder combiner = new StringBuilder();
        combiner.append(fixtures.veventWithUTCDTStartAndDTEnd(start1, end1))
                .append('\n')
                .append(fixtures.veventWithUTCDTStartAndDTEnd(start2, end2));

        final List<CalendarDataObject> appointments = parser.parseAppointments(combiner.toString(), TimeZone.getTimeZone("UTC"),null, new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());

        assertEquals(2, appointments.size());
    }

    //Deactivated test, private appointments with participants are allowed since OX 7.4
//    // Bug 11869
//    public void testAppShouldCorrectParticipantsInPrivateAppoitment() throws ConversionError {
//        final Date start = D("24/02/1981 10:00");
//        final Date end =   D("24/02/1981 12:00");
//        final String icalText = fixtures.veventWithSimpleProperties(start, end,
//        "CLASS"    ,    "PRIVATE",
//        "ATTENDEE" ,     "MAILTO:mickey@disney.invalid");
//
//        assertWarningWhenParsingAppointment(icalText, "Private appointments can not have attendees. Removing attendees and accepting appointment anyway.");
//
//        final Appointment appointment = parseAppointment(icalText);
//
//        assertNull(appointment.getParticipants());
//    }


    public void testAppShouldInterpretConfidentialAsPrivate() throws ConversionError {
        final String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "CLASS", "CONFIDENTIAL");
        final Appointment appointment = parseAppointment(icalText);

        assertTrue("CLASS:CONFIDENTIAL should resolve to private appointments", appointment.getPrivateFlag());
    }



    // Bug 11958 - a timezone element in a file should be relevant for all data, even if listed afterwards
    public void testTimezoneShouldBeRelevantForAllData() throws ConversionError{
        final String timezone = "BEGIN:VTIMEZONE\nTZID:/mozilla.org/20050126_1/America/New_York\nX-LIC-LOCATION:America/New_York\nBEGIN:STANDARD\nTZOFFSETFROM:-0400\nTZOFFSETTO:-0500\nTZNAME:EST\nDTSTART:19701025T020000\nRRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10\nEND:STANDARD\nBEGIN:DAYLIGHT\nTZOFFSETFROM:-0500\nTZOFFSETTO:-0400\nTZNAME:EDT\nDTSTART:19700405T020000\nRRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=1SU;BYMONTH=4\nEND:DAYLIGHT\nEND:VTIMEZONE\n";
        final String icalText1 = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n"+timezone+"BEGIN:VEVENT\nCREATED:20080216T152600Z\nLAST-MODIFIED:20080216T152600Z\nDTSTAMP:20080216T152600Z\nUID:3a289f91-f83a-4614-83c6-660c7740abd8\nSUMMARY:New York, 2008-08-31 09:00 - 10:00 (EST)\nDTSTART;TZID=/mozilla.org/20050126_1/America/New_York:20080831T090000\nDTEND;TZID=/mozilla.org/20050126_1/America/New_York:20080831T100000\nEND:VEVENT\nEND:VCALENDAR\n";
        final String icalText2 = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\nBEGIN:VEVENT\nCREATED:20080216T152600Z\nLAST-MODIFIED:20080216T152600Z\nDTSTAMP:20080216T152600Z\nUID:3a289f91-f83a-4614-83c6-660c7740abd8\nSUMMARY:New York, 2008-08-31 09:00 - 10:00 (EST)\nDTSTART;TZID=/mozilla.org/20050126_1/America/New_York:20080831T090000\nDTEND;TZID=/mozilla.org/20050126_1/America/New_York:20080831T100000\nEND:VEVENT\n"+ timezone + "END:VCALENDAR\n";
        final Appointment appointmentThatTroublesUs = parseAppointment(icalText1);
        final Appointment appointmentAsExpected = parseAppointment(icalText2);
        assertEquals("Start dates should be equal, independent of the placement of the timezone information",
                appointmentAsExpected.getStartDate(),
                appointmentThatTroublesUs.getStartDate());
        assertEquals("End dates should be equal, independent of the placement of the timezone information",
                appointmentAsExpected.getEndDate(),
                appointmentThatTroublesUs.getEndDate());
    }

    /* Bug 7470 - part III: Multiple line arguments are supposed to be 'unfolded'
     * according to RFC 2445 - iCal4J does not seem to do this. This test is meant to confirm
     * the wrapping parser does handle the case.
     */
    public void testShouldHandleMultipleLineArguments() throws ConversionError{
        final String foldedSummary = "This is a so called 'folded' argument\n meaning it is split\n into multiple lines\n each starting with a whitespace character";
        final String unfoldedSummary = foldedSummary.replace("\n ","");
        final String icalText = fixtures.veventWithSimpleProperties(new Date(), new Date(), "SUMMARY", foldedSummary);
        final Appointment appointment = parseAppointment(icalText);
        assertEquals("Unfolded summary does not match transformed title", unfoldedSummary, appointment.getTitle());
    }

    // Bug 16110
    public void testBug16110() throws Exception{
            String ical = "BEGIN:VCALENDAR\n"
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
            final Appointment appointment = parseAppointment(ical);
            assertEquals("All participants should be external", 17, appointment.getParticipants().length);
            assertEquals("Organizer should match", "ChinShimVeron.Koh@some-it.invalid", appointment.getOrganizer());
            boolean choegerFound = false, holgerFound = false;
            for(Participant p : appointment.getParticipants()){
                if(p.getEmailAddress().equals("choeger@open-xchange.com")) {
                    choegerFound = true;
                }
                if(p.getEmailAddress().equals("holger.achtziger@open-xchange.com")) { //note: now converted to lowercase. This is a behaviour change. Used to be different. But obviously no one cares to fix tests.
                    holgerFound = true;
                }
            }
            assertTrue("Should contain CHoeger", choegerFound);
            assertTrue("Should contain Holger", holgerFound);
    }

    public void testBug16237() throws Exception {
        String ical = "BEGIN:VCALENDAR\n"
                    + "METHOD:REQUEST\n"
                    + "PRODID:Microsoft Exchange Server 2007\n"
                    + "VERSION:2.0\n"
                    + "BEGIN:VTIMEZONE\n"
                    + "TZID:W. Europe Standard Time\n"
                    + "BEGIN:STANDARD\n"
                    + "DTSTART:16010101T030000\n"
                    + "TZOFFSETFROM:+0200\n"
                    + "TZOFFSETTO:+0100\n"
                    + "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10\n"
                    + "END:STANDARD\n"
                    + "BEGIN:DAYLIGHT\n"
                    + "DTSTART:16010101T020000\n"
                    + "TZOFFSETFROM:+0100\n"
                    + "TZOFFSETTO:+0200\n"
                    + "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=3\n"
                    + "END:DAYLIGHT\n"
                    + "END:VTIMEZONE\n"
                    + "BEGIN:VEVENT\n"
                    + "ORGANIZER;CN=:MAILTO:Marco.Duetsch@swisscom.com\n"
                    + "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=:MAILTO:ma\n"
                    + " rtin@ezuce.com\n"
                    + "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=:MAILTO:je\n"
                    + " rry@ezuce.com\n"
                    + "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=:MAILTO:fr\n"
                    + " ank.hoberg@open-xchange.com\n"
                    + "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=:MAILTO:st\n"
                    + " ephan.martin@open-xchange.com\n"
                    + "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=:MAILTO:An\n"
                    + " dreas.Schmid1@swisscom.com\n"
                    + "DESCRIPTION;LANGUAGE=de-DE:Dial-in Number:   +41 52 267 07 22\\nParticipant \n"
                    + " Pin-Code: 224745\\n\\nDial-in Number:\\n 0800 329 329  Switzerland\\n 0800 181\n"
                    + "  93 93  Germany\\n 1 866 591 43 61  USA\\n 1 866 927 08 47 Canada\\n\\nHello a\n"
                    + " ll\\,\\nas discussed within our last call\\, I send you the invite for the re\n"
                    + " view meeting.\\n\\nGoal of the meeting: review of the document \u201cProposal f\n"
                    + " or Swisscom\u201d\\n\\nCheers Marco\\n\\n\\n\\n\n"
                    + "SUMMARY;LANGUAGE=de-DE:Review \"Proposal for Swisscom\"\n"
                    + "DTSTART;TZID=W. Europe Standard Time:20100625T160000\n"
                    + "DTEND;TZID=W. Europe Standard Time:20100625T170000\n"
                    + "UID:040000008200E00074C5B7101A82E00800000000C0B499355111CB01000000000000000\n"
                    + " 010000000ADC7372333BA844A9150A8B9981434F4\n"
                    + "CLASS:PUBLIC\n"
                    + "PRIORITY:5\n"
                    + "DTSTAMP:20100621T125119Z\n"
                    + "TRANSP:OPAQUE\n"
                    + "STATUS:CONFIRMED\n"
                    + "SEQUENCE:0\n"
                    + "LOCATION;LANGUAGE=de-DE:conf-call\\, +41 52 267 07 22\\, pin-code: 224745 \n"
                    + "X-MICROSOFT-CDO-APPT-SEQUENCE:0\n"
                    + "X-MICROSOFT-CDO-OWNERAPPTID:1265985498\n"
                    + "X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE\n"
                    + "X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY\n"
                    + "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE\n"
                    + "X-MICROSOFT-CDO-IMPORTANCE:1\n"
                    + "X-MICROSOFT-CDO-INSTTYPE:0\n"
                    + "BEGIN:VALARM\n"
                    + "ACTION:DISPLAY\n"
                    + "DESCRIPTION:REMINDER\n"
                    + "TRIGGER;RELATED=START:-PT15M\n"
                    + "END:VALARM\n"
                    + "END:VEVENT\n"
                    + "END:VCALENDAR\n";

        parseAppointment(ical);
    }

    public void testBug16613() throws Exception{
        String ical = "BEGIN:VCALENDAR\n"
        +"VERSION:2.0\n"
        +"PRODID:-//Novell Inc//Groupwise 8.0.2 \n"
        +"METHOD:REQUEST\n"
        +"BEGIN:VTIMEZONE\n"
        +"TZID:(GMT-0500) Eastern Standard Time\n"
        +"BEGIN:STANDARD\n"
        +"TZOFFSETFROM:-0400\n"
        +"TZOFFSETTO:-0500\n"
        +"DTSTART:20001107T020000\n"
        +"RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=1SU;BYMONTH=11\n"
        +"TZNAME:Eastern Standard Time\n"
        +"END:STANDARD\n"
        +"BEGIN:DAYLIGHT\n"
        +"TZOFFSETFROM:-0500\n"
        +"TZOFFSETTO:-0400\n"
        +"DTSTART:20000314T020000\n"
        +"RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=2SU;BYMONTH=3\n"
        +"TZNAME:Eastern Daylight Time\n"
        +"END:DAYLIGHT\n"
        +"END:VTIMEZONE\n"
        +"BEGIN:VEVENT\n"
        +"SUMMARY:Videoconference between Open X-change and Novell\n"
        +"DTSTART;TZID=\"(GMT-0500) Eastern Standard Time\":20100824T083000\n"
        +"TRANSP:OPAQUE\n"
        +"X-GWSHOW-AS:BUSY\n"
        +"STATUS:CONFIRMED\n"
        +"X-GWITEM-TYPE:APPOINTMENT\n"
        +"DTSTAMP:20100728T164959Z\n"
        +"ORGANIZER;CN=\"Ron Hovsepian\";ROLE=CHAIR:MAILTO:123@ox.io\n"
        +"ATTENDEE;CN=\"WAL-VC-Old North Church-C52552\";PARTSTAT=NEEDS-ACTION;\n"
        +" RSVP=TRUE;ROLE=REQ-PARTICIPANT;CUTYPE=RESOURCE:\n"
        +" MAILTO:2323@ox.io\n"
        +"ATTENDEE;CN=\"PRV-VC-Arches\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=REQ-PARTICIPANT;CUTYPE=RESOURCE:\n"
        +" MAILTO:444@ox.io\n"
        +"ATTENDEE;CN=\"Colleen O'Keefe\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=REQ-PARTICIPANT:MAILTO:ff@ox.io\n"
        +"ATTENDEE;CN=\"Ken Muir\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=REQ-PARTICIPANT:MAILTO:fff@ox.io\n"
        +"ATTENDEE;CN=\"Ron Hovsepian\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=REQ-PARTICIPANT:MAILTO:123@ox.io\n"
        +"ATTENDEE;CN=\"Richard Seibt\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=REQ-PARTICIPANT:\n"
        +" MAILTO:richard.seibt@open-source-business-foundation.org\n"
        +"ATTENDEE;CN=\"Rafael Laguna de la Vera\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=REQ-PARTICIPANT:MAILTO:Rafael.Laguna@open-xchange.com\n"
        +"ATTENDEE;CN=\"Aditya Joshi\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=OPT-PARTICIPANT:MAILTO:1234@ox.io\n"
        +"ATTENDEE;CN=\"Melanie Reichel\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=OPT-PARTICIPANT:MAILTO:333@ox.io\n"
        +"ATTENDEE;CN=\"Jenalyn Cox\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=OPT-PARTICIPANT:MAILTO:fdfdfd@ox.io\n"
        +"ATTENDEE;CN=\"Keith Bengtson\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=OPT-PARTICIPANT:MAILTO:eeqq@ox.io\n"
        +"ATTENDEE;CN=\"Dianne Saunders\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=OPT-PARTICIPANT:MAILTO:vvv@ox.io\n"
        +"ATTENDEE;CN=\"Marie Roche\";PARTSTAT=NEEDS-ACTION;RSVP=TRUE;\n"
        +" ROLE=OPT-PARTICIPANT:MAILTO:3443@ox.io\n"
        +"DESCRIPTION:\n"
        +" 7/28/10:  resending appointment to cancel the use of the Nuremberg conferen\n"
        +" ce room.\\n \\nRafael Laguna and Richard Seibt will attend the meeting in ou\n"
        +" r Waltham office.\\n \\n \\n \\n\n"
        +"ATTACH;ENCODING=BASE64;VALUE=BINARY;ID=TEXT.htm:\n"
        +" PEhUTUw+PEhFQUQ+DQo8TUVUQSBjb250ZW50PSJ0ZXh0L2h0bWw7IGNoYXJzZXQ9d2luZG93cy0\n"
        +" xMjUyIiBodHRwLWVxdWl2PUNvbnRlbnQtVHlwZT4NCjxNRVRBIG5hbWU9R0VORVJBVE9SIGNvb\n"
        +" nRlbnQ9Ik1TSFRNTCA4LjAwLjYwMDEuMTg5MjgiPjwvSEVBRD4NCjxCT0RZIHN0eWxlPSJNQVJ\n"
        +" HSU46IDRweCA0cHggMXB4OyBGT05UOiAxMHB0IFRhaG9tYSI+DQo8RElWPjcvMjgvMTA6Jm5ic\n"
        +" 3A7IHJlc2VuZGluZyBhcHBvaW50bWVudCB0byBjYW5jZWwgdGhlIHVzZSBvZiB0aGUgTnVyZW1\n"
        +" iZXJnIGNvbmZlcmVuY2Ugcm9vbS48L0RJVj4NCjxESVY+Jm5ic3A7PC9ESVY+DQo8RElWPlJhZ\n"
        +" mFlbCBMYWd1bmEgYW5kIFJpY2hhcmQgU2VpYnQgd2lsbCBhdHRlbmQgdGhlIG1lZXRpbmcgaW4\n"
        +" gb3VyIFdhbHRoYW0gb2ZmaWNlLjwvRElWPg0KPERJVj4mbmJzcDs8L0RJVj4NCjxESVY+Jm5ic\n"
        +" 3A7PC9ESVY+DQo8RElWPiZuYnNwOzwvRElWPjwvQk9EWT48L0hUTUw+\n"
        +"DTEND;TZID=\"(GMT-0500) Eastern Standard Time\":20100824T100000\n"
        +"LOCATION:WAL-VC-Old North Church-C52552 / PRV-VC-Arches\n"
        +"UID:20100728T164959Z_D26000DF178\n"
        +"PRIORITY:5\n"
        +"CLASS:PUBLIC\n"
        +"X-GWCLASS:NORMAL\n"
        +"END:VEVENT\n"
        +"END:VCALENDAR\n";
        parseAppointment(ical);
        assertTrue("Should not fail ith parsing error before this", true);
    }

    //this is bug16895
    public void testDoesNotLikeEmailElementFromZimbra() throws Exception{
    	String ical = "BEGIN:VCALENDAR\n"
			+ "PRODID:Zimbra-Calendar-Provider\n"
			+ "VERSION:2.0\n"
			+ "CALSCALE:GREGORIAN\n"
			+ "VERSION:2.0\n"
			+ "METHOD:REQUEST\n"
			+ "PRODID:-//Apple Inc.//iCal 4.0.3//EN\n"
			+ "BEGIN:VTIMEZONE\n"
			+ "TZID:Europe/Paris\n"
			+ "BEGIN:DAYLIGHT\n"
			+ "TZOFFSETFROM:+0100\n"
			+ "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n"
			+ "DTSTART:19810329T020000\n"
			+ "TZNAME:GMT+02:00\n"
			+ "TZOFFSETTO:+0200\n"
			+ "END:DAYLIGHT\n"
			+ "BEGIN:STANDARD\n"
			+ "TZOFFSETFROM:+0200\n"
			+ "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n"
			+ "DTSTART:19961027T030000\n"
			+ "TZNAME:GMT+01:00\n"
			+ "TZOFFSETTO:+0100\n"
			+ "END:STANDARD\n"
			+ "END:VTIMEZONE\n"
			+ "BEGIN:VEVENT\n"
			+ "CREATED:20100916T122236Z\n"
			+ "UID:32B3BF02-6736-4AF9-A6B0-68E290E7EFED\n"
			+ "DTEND;TZID=\"Europe/Paris\":20100917T203000\n"
			+ "ATTENDEE;CN=Frank Hoberg;CUTYPE=INDIVIDUAL;EMAIL=frank.hoberg@open-xchange.c\n"
			+ " om;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:frank.hoberg@\n"
			+ " open-xchange.com\n"
			+ "ATTENDEE;CN=Douglas Randall (Randy) Parker;CUTYPE=INDIVIDUAL;EMAIL=randall.p\n"
			+ " arker@scality.com;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailt\n"
			+ " o:randall.parker@scality.com\n"
			+ "ATTENDEE;CN=Marc Villemade;CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED:mailto:m@scal\n"
			+ " ity.com\n"
			+ "TRANSP:OPAQUE\n"
			+ "SUMMARY:Sync up with Frank@OX about meeting in DC\n"
			+ "DTSTART;TZID=\"Europe/Paris\":20100917T193000\n"
			+ "DTSTAMP:20100916T161511Z\n"
			+ "ORGANIZER;CN=Marc Villemade:mailto:m@scality.com\n"
			+ "SEQUENCE:15\n"
			+ "END:VEVENT\n"
			+ "END:VCALENDAR";
        parseAppointment(ical);
        assertTrue("Should not fail with parsing error before this", true);
    }

    //bug 17562
    public void testShouldNotGetConfusedByDifferentlyCapitalizedAddresses() throws Exception{
    	User user = users.getUser(1);
    	String[] parts = user.getMail().split("@");
    	String localPart = parts[0];
    	String capitalizedLocalPart = localPart.toUpperCase();
    	String differentMail = capitalizedLocalPart + "@" + parts[1];

    	assertFalse("Precondition: New address should not match old address", differentMail.equals(user.getMail()));
    	assertTrue("Precondition: New address should match old address if ignoring capitalization", differentMail.equalsIgnoreCase(user.getMail()));

    	String ical = "BEGIN:VCALENDAR\n"
		+"PRODID:Open-Xchange\n"
		+"VERSION:2.0\n"
		+"CALSCALE:GREGORIAN\n"
		+"METHOD:REQUEST\n"
		+"BEGIN:VEVENT\n"
		+"DTSTAMP:20101116T074248Z\n"
		+"SUMMARY:Launch-Strategie\n"
		+"DESCRIPTION:ACHTUNG!\n"
		+"DTSTART:20101116T100000Z\n"
		+"DTEND:20101116T110000Z\n"
		+"CLASS:PUBLIC\n"
		+"LOCATION:TelCo Europe\n"
		+"TRANSP:OPAQUE\n"
		+"UID:1a756b4b-b685-48f8-a5ea-8146dd1805c7\n"
		+"CREATED:20101116T074246Z\n"
		+"LAST-MODIFIED:20101116T074246Z\n"
		+"ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL:mailto:"+differentMail+"\n"
		+"END:VEVENT\n"
		+"END:VCALENDAR\n";
    	CalendarDataObject appointment = parseAppointment(ical);
    	Participant[] participants = appointment.getParticipants();
    	assertEquals("Should find one participant", 1, participants.length);
    	Participant participant = participants[0];
    	assertEquals("Should find a user, not an external participant" , Participant.USER, participant.getType());
    }


}
