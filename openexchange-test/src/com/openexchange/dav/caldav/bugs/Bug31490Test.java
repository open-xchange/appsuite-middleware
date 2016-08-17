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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug31490Test}
 *
 * Duplicate UIDs & changing owners in CalDAV result in auto-deletes
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug31490Test extends CalDAVTest {

	@Test
	public void testImportMalformedAttendee() throws Exception {
		/*
		 * create appointment at client
		 */
		String uid = randomUID();
    	Date start = TimeTools.D("next sunday at 14:15");
    	Date end = TimeTools.D("next sunday at 14:45");
    	String iCal =
    	    "BEGIN:VCALENDAR\r\n" +
	        "PRODID:-//SurGATE Outlook DAV Client/v3.1\r\n" +
	        "VERSION:2.0\r\n" +
	        "METHOD:PUBLISH\r\n" +
	        "X-MS-OLK-FORCEINSPECTOROPEN:TRUE\r\n" +
	        "BEGIN:VTIMEZONE\r\n" +
	        "TZID:America/Detroit\r\n" +
	        "BEGIN:STANDARD\r\n" +
	        "RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=+1SU\r\n" +
	        "DTSTART:16011101T020000\r\n" +
	        "TZOFFSETFROM:-0400\r\n" +
	        "TZOFFSETTO:-0500\r\n" +
	        "END:STANDARD\r\n" +
	        "BEGIN:DAYLIGHT\r\n" +
	        "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=+2SU\r\n" +
	        "DTSTART:16010302T020000\r\n" +
	        "TZOFFSETFROM:-0500\r\n" +
	        "TZOFFSETTO:-0400\r\n" +
	        "END:DAYLIGHT\r\n" +
	        "END:VTIMEZONE\r\n" +
	        "BEGIN:VEVENT\r\n" +
	        "ATTENDEE;PARTSTAT=ACCEPTED;CN=\"'Jason Magouirk' <jason.magouirk@mocgnigass\r\n" +
	        "\te.mwo>\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Jason Magouirk' <jason.magouirk@m\r\n" +
	        "\tocgnigasse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=DECLINED;CN=\"'Duy Doan' <duy.doan@mocgnigasse.mwo>\";ROLE\r\n" +
	        "\t=REQ-PARTICIPANT;RSVP=TRUE:'Duy Doan' <duy.doan@mocgnigasse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=DECLINED;CN=\"'Michael Mohammed' <mike.mohammed@mocgnigas\r\n" +
	        "\tse.mwo>\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Michael Mohammed' <mike.mohammed\r\n" +
	        "\t@mocgnigasse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=DECLINED;CN=\"'David Kerr' <david.kerr@mocgnigasse.mwo>\";\r\n" +
	        "\tROLE=REQ-PARTICIPANT;RSVP=TRUE:'David Kerr' <david.kerr@mocgnigasse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=DECLINED;CN=\"'Estanislao Utrilla' <estanislao.gines@mocg\r\n" +
	        "\tnigasse.mwo>\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Estanislao Utrilla' <estani\r\n" +
	        "\tslao.gines@mocgnigasse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=ACCEPTED;CN=\"'Benoit Raymond' <benoit.raymond@mocgnigass\r\n" +
	        "\te.mwo>\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Benoit Raymond' <benoit.raymond@m\r\n" +
	        "\tocgnigasse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=ACCEPTED;CN=\"'Sophia Xu' <sophia.xu@mocgnigasse.mwo>\";RO\r\n" +
	        "\tLE=REQ-PARTICIPANT;RSVP=TRUE:'Sophia Xu' <sophia.xu@mocgnigasse.mwo>\r\n" +
            "ATTENDEE;PARTSTAT=ACCEPTED;CN=\"'Ted Corning' <" + client.getValues().getDefaultAddress() + ">\r\n" +
            "\t\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Ted Corning' <"+ client.getValues().getDefaultAddress() + "\r\n" +
	        "ATTENDEE;PARTSTAT=ACCEPTED;CN=\"'Paolo Biancolli' <paolo.biancolli@mocgniga\r\n" +
	        "\tsse.mwo>\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Paolo Biancolli' <paolo.biancol\r\n" +
	        "\tli@mocgnigasse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=ACCEPTED;CN=\"'Pat Matthews' <pat.matthews@mocgnigasse.mw\r\n" +
	        "\to>\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Pat Matthews' <pat.matthews@mocgnigas\r\n" +
	        "\tse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=ACCEPTED;CN=\"'Pedro Romana' <pedro.romana@mocgnigasse.mw\r\n" +
	        "\to>\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Pedro Romana' <pedro.romana@mocgnigas\r\n" +
	        "\tse.mwo>\r\n" +
	        "ATTENDEE;PARTSTAT=TENTATIVE;CN=\"'Stacy Lanier' <stacy.lanier@mocgnigasse.m\r\n" +
	        "\two>\";ROLE=REQ-PARTICIPANT;RSVP=TRUE:'Stacy Lanier' <stacy.lanier@mocgniga\r\n" +
	        "\tsse.mwo>\r\n" +
            "CREATED:" + formatAsUTC(TimeTools.D("yesterday noon")) + "\r\n" +
	        "CLASS:PUBLIC\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
	        "LAST-MODIFIED:20140305T210711Z\r\n" +
	        "RRULE:FREQ=WEEKLY;BYDAY=WE\r\n" +
            "DTSTART;TZID=America/Detroit:" + format(start, "America/Detroit") + "\r\n" +
            "DTEND;TZID=America/Detroit:" + format(end, "America/Detroit") + "\r\n" +
	        "LOCATION:Bridge: 5227343\\\\; US: +1 303-248-9655\\\\;  Toll Free/Int'l: 866-3\r\n" +
	        "\t65-4406\r\n" +
	        "TRANSP:OPAQUE\r\n" +
	        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "UID:" + uid + "\r\n" +
	        "DESCRIPTION:Team, \\n\\nI am re-sending since for Mariner, but using individ\r\n" +
	        "\tual addresses\\ninstead of the mailing list, since that didn't seem to go \r\n" +
	        "\twell.\\nPlus I couldn't get it to save with a reminder set, no matter\\nwha\r\n" +
	        "\tt I tried.\\n\\nThis is a meeting where we discuss issues, including approa\r\n" +
	        "\tches to \\ntroubleshooting, data collection for possible escalation, gener\r\n" +
	        "\tal \\nproduct questions, etc. While the meeting is mainly for Email Mx, \\n\r\n" +
	        "\tEdge Gx, and Extensions, others products (Directory, WebEdge, etc) \\ncan \r\n" +
	        "\talso be discussed. \\n\\nThe dial-in info is: \\nReady-Access Number: +1 303\r\n" +
	        "\t-248-9655 \\nToll Free Ready-Access Number (if needed): 866-365-4406 \\\\n7-\r\n" +
	        "\tDigit Access Code: 5227343 \\n\\nThanks! \\nTed \\n\\n\\n\\n\r\n" +
	        "X-ALT-DESC;FMTTYPE=text/html:<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//\r\n" +
	        "\tEN\">\\n<HTML>\\n<HEAD>\\n<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html;\r\n" +
	        "\t charset=utf-8\">\\n<META NAME=\"Generator\" CONTENT=\"MS Exchange Server vers\r\n" +
	        "\tion 14.02.5004.000\">\\n<TITLE> TPS Troubleshooting Roundtable</TITLE>\\n</H\r\n" +
	        "\tEAD>\\n<BODY>\\n<!-- Converted from text/plain format -->\\n\\n<P><FONT SIZE=\r\n" +
	        "\t2>Team,<BR>\\n<BR>\\nI am re-sending since for Mariner, but using individua\r\n" +
	        "\tl addresses<BR>\\ninstead of the mailing list, since that didn't seem to g\r\n" +
	        "\to well.<BR>\\nPlus I couldn't get it to save with a reminder set, no matte\r\n" +
	        "\tr<BR>\\nwhat I tried.<BR>\\n<BR>\\nThis is a meeting where we discuss issues\r\n" +
	        "\t, including approaches to<BR>\\ntroubleshooting, data collection for possi\r\n" +
	        "\tble escalation, general<BR>\\nproduct questions, etc. While the meeting is\r\n" +
	        "\t mainly for Email Mx,<BR>\\nEdge Gx, and Extensions, others products (Dire\r\n" +
	        "\tctory, WebEdge, etc)<BR>\\ncan also be discussed.<BR>\\n<BR>\\nThe dial-in i\r\n" +
	        "\tnfo is:<BR>\\nReady-Access Number: +1 303-248-9655<BR>\\nToll Free Ready-Ac\r\n" +
	        "\tcess Number (if needed): 866-365-4406 \\\\n7-Digit Access Code: 5227343<BR>\r\n" +
	        "\t\\n<BR>\\nThanks!<BR>\\nTed<BR>\\n<BR>\\n<BR>\\n<BR>\\n</FONT>\\n</P>\\n\\n</BODY>\\\r\n" +
	        "\tn</HTML>\r\n" +
	        "SUMMARY: TPS Troubleshooting Roundtable\r\n" +
	        "LOCATION:Bridge: 5227343\\\\; US: +1 303-248-9655\\\\;  Toll Free/Int'l: 866-3\r\n" +
	        "\t65-4406\r\n" +
	        "PRIORITY:5\r\n" +
	        "SEQUENCE:8\r\n" +
	        "END:VEVENT\r\n"
	    ;
		assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        super.rememberForCleanUp(appointment);
        assertNotNull("No participants found", appointment.getParticipants());
        assertEquals("incorrect number of participants", 12, appointment.getParticipants().length);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ATTENDEEs found", iCalResource.getVEvent().getProperties("ATTENDEE"));
        assertEquals("incorrect number of ATTENDEEs", 12, iCalResource.getVEvent().getProperties("ATTENDEE").size());
	}

}
