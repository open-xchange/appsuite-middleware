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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.groupware.importexport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.TestCase;
import com.openexchange.tools.versit.ICalendar;
import com.openexchange.tools.versit.Parameter;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.VCard;
import com.openexchange.tools.versit.old.VCard21;

public class VersitParserTest extends TestCase {

    public List<VersitObject> parseICal(final String data) throws IOException {
        final InputStream is = new ByteArrayInputStream(data.getBytes());
        final List<VersitObject> ret = new LinkedList<VersitObject>();

        final VersitDefinition def = ICalendar.definition;
        final VersitDefinition.Reader versitReader;
        VersitObject rootVersitObject;

        boolean hasMoreObjects = true;

        versitReader = def.getReader(is, "UTF-8");
        rootVersitObject = def.parseBegin(versitReader);
        ret.add(rootVersitObject);

        while (hasMoreObjects) {
            VersitObject versitObject = null;
            versitObject = def.parseChild(versitReader, rootVersitObject);
            if (versitObject == null) {
                hasMoreObjects = false;
                break;
            }
            ret.add(versitObject);

        }
        return ret;
    }

    public List<VersitObject> parseVCard21(final String data) throws IOException {
        final InputStream is = new ByteArrayInputStream(data.getBytes());
        final List<VersitObject> ret = new LinkedList<VersitObject>();

        final VersitDefinition def = VCard21.definition;
        final VersitDefinition.Reader versitReader;
        VersitObject rootVersitObject;

        boolean hasMoreObjects = true;

        versitReader = def.getReader(is, "UTF-8");
        rootVersitObject = def.parseBegin(versitReader);
        ret.add(rootVersitObject);

        while (hasMoreObjects) {
            VersitObject versitObject = null;
            versitObject = def.parseChild(versitReader, rootVersitObject);
            if (versitObject == null) {
                hasMoreObjects = false;
                break;
            }
            ret.add(versitObject);

        }
        return ret;
    }

    public List<VersitObject> parseVCard3(final String data) throws IOException {
        final InputStream is = new ByteArrayInputStream(data.getBytes());
        final List<VersitObject> ret = new LinkedList<VersitObject>();

        final VersitDefinition def = VCard.definition;
        final VersitDefinition.Reader versitReader;
        VersitObject rootVersitObject;

        boolean hasMoreObjects = true;

        versitReader = def.getReader(is, "UTF-8");
        rootVersitObject = def.parseBegin(versitReader);
        ret.add(rootVersitObject);

        while (hasMoreObjects) {
            VersitObject versitObject = null;
            versitObject = def.parseChild(versitReader, rootVersitObject);
            if (versitObject == null) {
                hasMoreObjects = false;
                break;
            }
            ret.add(versitObject);

        }
        return ret;
    }

    /*
     * Parsing of ATTENDEE property
     */
    public void test7470() throws IOException {
        final String ical = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "METHOD:REQUEST\n" + "BEGIN:VEVENT\n" + "ATTENDEE;CN=\"Camil Bartkowiak (cbartkowiak@oxhemail.open-xchange.com)\";RSVP=TRUE:mailto:cbartkowiak@oxhemail.open-xchange.com\n" + "CLASS:PUBLIC\n" + "CREATED:20070521T150327Z\n" + "DTEND:20070523T090000Z\n" + "DTSTAMP:20070521T150327Z\n" + "DTSTART:20070523T083000Z\n" + "SUMMARY;LANGUAGE=de:Simple Appointment with participant\n" + "END:VEVENT\n" + "END:VCALENDAR\n";
        parseICal(ical);
    }

    /*
     * Parsing of RRULE with COUNT ("only ten times")
     */
    public void test7732() throws IOException {
        final String ical = "BEGIN:VCALENDAR\n" + "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\n" + "VERSION:2.0\n" + "METHOD:PUBLISH\n" + "BEGIN:VEVENT\n" + "CLASS:PUBLIC\n" + "CREATED:20070531T130514Z\n" + "DESCRIPTION:\\n\n" + "DTEND:20070912T083000Z\n" + "DTSTAMP:20070531T130514Z\n" + "DTSTART:20070912T080000Z\n" + "LAST-MODIFIED:20070531T130514Z\n" + "LOCATION:loc\n" + "PRIORITY:5\n" + "RRULE:FREQ=DAILY;COUNT=10\n" + "SEQUENCE:0\n" + "SUMMARY;LANGUAGE=de:Daily iCal\n" + "TRANSP:OPAQUE\n" + "UID:040000008200E00074C5B7101A82E008000000005059CADA94A3C701000000000000000010000000A1B56CAC71BB0948833B0C11C333ADB0\n" + "END:VEVENT\n" + "END:VCALENDAR";
        parseICal(ical);
    }

    /*
     * Parsing of RRULE with negative values ("last sunday of april")
     */
    public void test7735() throws IOException {
        final String ical = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\n" + "BEGIN:VEVENT\n" + "DTSTART:20070814T150000Z\n" + "DTEND:20070814T163000Z\n" + "LOCATION:Olpe\nSUMMARY:Komplizierte Intervalle\n" + "DESCRIPTION:Jeden ersten Sonntag im April\n" + "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=4\n" + "END:VEVENT\n" + "END:VCALENDAR";
        final List<VersitObject> list = parseICal(ical);
        assertEquals("Two elements in list?", list.size(), 2);
    }

    /*
     * Parsing empty properties
     */
    public void test8527() throws IOException {
        final String ical = "BEGIN:VCALENDAR\n" + "METHOD:REQUEST\n" + "PRODID:Microsoft CDO for Microsoft Exchange\n" + "VERSION:2.0\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20070719T155206Z\n" + "DTSTART;TZID=\"(GMT) Greenwich Mean Time/Dublin/Edinburgh/London\":20070724T1\n" + " 10000\n" + "SUMMARY:Open-Xchange discussion \n" + "DTEND;TZID=\"(GMT) Greenwich Mean Time/Dublin/Edinburgh/London\":20070724T113\n" + " 000\n" + "PRIORITY:5\n" + "CLASS:\n" + "DESCRIPTION:Added after empty element\\, but still parsed. Yeah!\n" + "CREATED:20070719T154738Z\n" + "LAST-MODIFIED:20070719T155206Z\n" + "STATUS:CONFIRMED\n" + "TRANSP:OPAQUE\n" + "END:VEVENT\n" + "END:VCALENDAR";
        final List<VersitObject> list = parseICal(ical);
        final VersitObject obj = list.get(1);
        assertEquals("Properties after empty are parsed?", "Added after empty element, but still parsed. Yeah!", obj.getProperty(
            "DESCRIPTION").getValue());
        assertEquals("All properties are parsed?", 10, obj.getPropertyCount());
    }

    public void test9765() throws IOException {
        String vcard = "BEGIN:VCARD\n" + "VERSION:2.1\n" + "FN:Hallo Test\n" + "N:Test;Hallo\n" + "EMAIL;INTERNET:test.hallo@open-xchange.com\n" + "GEO:37,386013;-122,082932\n" + "END:VCARD\n";
        final List<VersitObject> list = parseVCard21(vcard);
        final VersitObject obj = list.get(0);
        ArrayList<Double> geo = (ArrayList<Double>) obj.getProperty("GEO").getValue();

        assertEquals(2, geo.size());
        assertEquals(37.386013, geo.get(0));
        assertEquals(-122.082932, geo.get(1));
    }

    public void test9762() throws IOException {
        String vcard = "BEGIN:VCARD\n" + "VERSION:2.1\n" + "FN:Hallo Test\n" + "N:Test;Hallo\n" + "EMAIL;INTERNET:test.hallo@open-xchange.com\n" + "GEO:-32.33,44.53\n" + "END:VCARD\n";
        final List<VersitObject> list = parseVCard21(vcard);
        final VersitObject obj = list.get(0);
        ArrayList<Double> geo = (ArrayList<Double>) obj.getProperty("GEO").getValue();

        assertEquals(2, geo.size());
        assertEquals(-32.33, geo.get(0));
        assertEquals(44.53, geo.get(1));
    }

    public void test9763() throws IOException {
        String vcard = "BEGIN:VCARD\n" + "VERSION:2.1\n" + "FN:Hallo Test\n" + "N:Test;Hallo\n" + "EMAIL;INTERNET:test.hallo@open-xchange.com\n" + "TZ:-04:00\n" + "END:VCARD\n";
        parseVCard21(vcard);
    }

    public void test9815() throws IOException {
        String vcard = "BEGIN:VCARD\n" + "VERSION:3.0\n" + "N:Pope;John\n" + "REV:1997-11-15\n" + "URL:http://www.swbyps.restaurant.french/~chezchic.html\n" + "END:VCARD\n";
        parseVCard21(vcard);
    }

    public void test9766() throws IOException, URISyntaxException {
        String vcard = "BEGIN:VCARD\n" + "VERSION:3.0\n" + "N:Pope;John\n" + "PHOTO;VALUE=URL:http://www.open-xchange.com/wiki/images/8/84/Cisco.jpg\n" + "END:VCARD\n";

        final List<VersitObject> list = parseVCard3(vcard);
        final VersitObject obj = list.get(0);

        URI imageUrl = (URI) obj.getProperty("PHOTO").getValue();
        assertEquals(new URI("http://www.open-xchange.com/wiki/images/8/84/Cisco.jpg"), imageUrl);
    }

    public void testBug9771() throws IOException {
        String vcard = "BEGIN:VCARD\n" + "VERSION:3.0\n" + "N:Pope;John\n" + "TEL;TYPE=home;TYPE=cell:123435235\n" + "END:VCARD\n";

        final List<VersitObject> list = parseVCard3(vcard);
        final VersitObject obj = list.get(0);

        Property property = obj.getProperty("TEL");
        Parameter parameter = property.getParameter(0);

        assertEquals(2, parameter.getValueCount());

        Set<String> expected = new HashSet<String>();
        expected.add("home");
        expected.add("cell");

        for(int i = 0; i < 2; i++) {
            String value = parameter.getValue(i).getText();
            assertTrue( value.toString(), expected.remove( value ));
        }

        assertTrue(expected.isEmpty());
    }
}
