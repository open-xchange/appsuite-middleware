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

package com.openexchange.subscribe.microformats.objectparser;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.Expectations;
import com.openexchange.tools.encoding.Base64;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class OXHCardParserTest extends TestCase {

    public static final String gifBase64 = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEB"
        +"AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEB"
        +"AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAANAA0DASIA"
        +"AhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQA"
        +"AAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3"
        +"ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWm"
        +"p6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEA"
        +"AwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSEx"
        +"BhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElK"
        +"U1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3"
        +"uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD+lXx/"
        +"+2L8Qb74g+O4NX1u38H+D/hP+1Do/gVz4YhvxfXvg3T7bVm1VdcklvLyW+e9h0We8Njpdtp6XEqy"
        +"x/Z7t4IWh9U+A3jLx/8AEyfxtqfhH4nW3jPRtNutF07zdI8VeKZk068MWp3UsM8Wq6bp7Qvc2k9h"
        +"MrQ+d5qgiUo0QB+afih+xj4pk+MnxY8a+Av2jvH/AMNX+IXii+8R6tp/h7TIx+91WebUxZz3Nvre"
        +"nRajb6Ze3l6+lNd2TSWkNy8LGXLySfp7+yP8Lbz4bfBnQtA8SeNNW+KOuG41G81Hxn4qs7Ya5qk9"
        +"/qV5qIW5dZbkmCxivY9OsInmle3sbOCHzWRUSP8AknIfCPxAz3xElxD4gY7iHC5Tl887qYCrhuIc"
        +"O44p4uKwGFpYeGX53iFh6NTBww+KjTxOSU1h3h61P2kK2ITXzGCwObSx7q4+VaOHhPEThL21OSnz"
        +"SlCjGDp4mTpp0pRnyywqjH2bTkpOJ//Z";

    public static final String HCARD_CONTENT =
            "<span class=\"fn n\">\n" +
                "<span class=\"given-name\">Terry</span>\n" +
                "<span class=\"additional-name\">Tiberius</span>\n" +
                "<span class=\"family-name\">Tester</span>\n" +
            "</span>\n" +
            "<div class=\"bday\">1970-1-31</div>\n" +
            "<div class=\"org\">World Class Testers Inc.</div>\n" +
            "<a class=\"email\" href=\"mailto:tester@open-xchange.com\">some-email</a>\n" +
            "<a class=\"email\" href=\"mailto:tester2@open-xchange.com\">home-email <span class=\"type\">home</span></a>\n" +
            "<a class=\"email\" href=\"mailto:tester3@open-xchange.com\">work-email <span class=\"type\">work</span></a>\n" +
            "<a class=\"email\" href=\"mailto:ignoreme@open-xchange.com\">ignore-email</a>\n" +
            "<div class=\"role\">Test subject</div>\n" +
            "<div class=\"adr\">\n" +
                "<span class=\"type\">work</span>\n"+
                "<div class=\"street-address\">Workingstreet 67</div>\n" +
                "<span class=\"locality\">Workingcity</span>, \n" +
                "<span class=\"region\">Workingstate</span>, \n" +
                "<span class=\"postal-code\">6677</span>\n" +
                "<span class=\"country-name\">Elbownia</span>\n" +
            "</div>\n" +
            "<div class=\"adr\">\n" +
                "<span class=\"type\">home</span>\n"+
                "<div class=\"street-address\">Somestreet 69</div>\n" +
                "<span class=\"locality\">Somecity</span>, \n" +
                "<span class=\"region\">Somestate</span>, \n" +
                "<span class=\"postal-code\">6666</span>\n" +
                "<span class=\"country-name\">Elbonia</span>\n" +
            "</div>\n" +
            "<div class=\"tel\">+666 34 54 74 94</div>\n" +
            "<div class=\"tel\">(<span class=\"type\">fax</span> <span class=\"type\">home</span>) <span class=\"value\">+666 34 54 74 95</span></div>\n" +
            "<div class=\"tel\">(<span class=\"type\">fax</span>) <span class=\"value\">+666 34 54 74 96</span></div>\n" +
            "<div class=\"tel\">(<span class=\"type\">work</span>) <span class=\"value\">+666 34 54 74 97</span></div>\n" +
            "<div class=\"note\">Nice guy. Tester. Testers are rad.</div>\n" +
            "<div class=\""+OXMFVisitor.OXMF_PREFIX+"userfield01\">One of them userfields that are not used in HCard at all.</div>\n" +
            "<div class=\""+OXMFVisitor.OXMF_PREFIX+"userfield02\">Another userfield.</div>\n" +
            "<p style=\"font-size:smaller;\">This <a href=\"http://microformats.org/wiki/hcard\">hCard</a> created with the <a href=\"http://microformats.org/code/hcard/creator\">hCard creator</a>.</p>\n";

    public static final String HCARD_SNIPPET =
        "<div id=\"Terry-Tiberius-Tester\" class=\"vcard\">\n" +
        HCARD_CONTENT +
        "</div>";

    public static final Expectations EXPECTED_CONTENTS = new Expectations(){{
        put(Contact.GIVEN_NAME, "Terry");
        put(Contact.SUR_NAME, "Tester");
        put(Contact.MIDDLE_NAME, "Tiberius");
        put(Contact.COMPANY, "World Class Testers Inc.");
        put(Contact.EMAIL3, "tester@open-xchange.com"); //this is the only place left
        put(Contact.EMAIL2, "tester2@open-xchange.com"); //this is home
        put(Contact.EMAIL1, "tester3@open-xchange.com"); //this is work
        put(Contact.POSITION, "Test subject");

        put(Contact.FAX_HOME, "+666 34 54 74 95");
        put(Contact.FAX_BUSINESS, "+666 34 54 74 96"); //because the first fax number stored was "home", the non-defined will be "work", the default one
        put(Contact.TELEPHONE_BUSINESS1, "+666 34 54 74 97");
        put(Contact.TELEPHONE_OTHER, "+666 34 54 74 94");

        put(Contact.STREET_BUSINESS, "Workingstreet 67");
        put(Contact.CITY_BUSINESS, "Workingcity");
        put(Contact.STATE_BUSINESS, "Workingstate");
        put(Contact.POSTAL_CODE_BUSINESS, "6677");
        put(Contact.COUNTRY_BUSINESS, "Elbownia");

        put(Contact.STREET_HOME, "Somestreet 69");
        put(Contact.CITY_HOME, "Somecity");
        put(Contact.STATE_HOME, "Somestate");
        put(Contact.POSTAL_CODE_HOME, "6666");
        put(Contact.COUNTRY_HOME, "Elbonia");
        put(Contact.NOTE, "Nice guy. Tester. Testers are rad.");
        try {
        put(Contact.BIRTHDAY, new SimpleDateFormat("yyyy-MM-dd zzz").parse("1970-01-31 UTC"));
        } catch( Exception e){
            e.printStackTrace();
        }
    }};

    private OXHCardParser parser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = new OXHCardParser();
    }


    public void testShouldReadWellformedHtml() {
        final String html = "<html><head><title>OX hCard Parsing Test</title></head><body>" + HCARD_SNIPPET + "</body></html>";
        List<Contact> results = parser.parse(html);
        assertEquals("Should extract exactly one element", 1, results.size());
        EXPECTED_CONTENTS.verify(results.get(0));
    }

    public void testShouldReadMinimalHtml() {
        final String html = "<html><body>" + HCARD_SNIPPET + "</body></html>";
        List<Contact> results = parser.parse(html);
        assertEquals("Should extract exactly one element", 1, results.size());
        EXPECTED_CONTENTS.verify(results.get(0));
    }

    public void testShouldReadHtmlWithoutClosingElements() {
        final String html = "<html><head><body>" + HCARD_SNIPPET + "</html>";
        List<Contact> results = parser.parse(html);
        assertEquals("Should extract exactly one element", 1, results.size());
        EXPECTED_CONTENTS.verify(results.get(0));

    }

    public void testShouldReadHtmlWithoutQuotedAttributeValues() {
        final String html = "<html><head><title>OX hCard Parsing Test</title></head><body>" + HCARD_SNIPPET + "</body></html>".replace("\"", "");
        List<Contact> results = parser.parse(html);
        assertEquals("Should extract exactly one element", 1, results.size());
        EXPECTED_CONTENTS.verify(results.get(0));
    }

    public void testShouldCreateTwoElementsEvenIfGivenTwoWithTheSameID(){
        final String html = "<html><head><title>OX hCard Parsing Test</title></head><body>"
            + HCARD_SNIPPET
            + HCARD_SNIPPET
            +"</body></html>";
        List<Contact> results = parser.parse(html);
        assertEquals("Should extract exactly one element", 2, results.size());
        EXPECTED_CONTENTS.verify(results.get(0));
    }

    public void testShouldCreateTwoElementsIfGivenTwoSeparateElements() {
        final String html = "<html><head><title>OX hCard Parsing Test</title></head><body>"
            + "<div id=\"hcard-One-Tester\" class=\"vcard\">\n"
            + HCARD_CONTENT
            +"</div>"
            + "<div id=\"hcard-Two-Tester\" class=\"vcard\">\n"
            + HCARD_SNIPPET
            +"</div>"
            +"</body></html>";        List<Contact> results = parser.parse(html);
        assertEquals("Should extract exactly two elements", 2, results.size());
        EXPECTED_CONTENTS.verify(results.get(0));
    }

    public void testShouldDealWithMoreThanOneValuePerAttribute(){
        String html = "<div class=\"vcard\"><span class=\"fn n\">\n" +
        "<span class=\"given-name bullshit value\">Terry</span>\n" +
        "<span class=\"bullshit-value additional-name\">Tiberius</span>\n" +
        "<span class=\"family-name familyname\">Tester</span>\n" +
        "</span>\n</div>";

        Expectations expectations = new Expectations(){{
            put(Contact.GIVEN_NAME, "Terry");
            put(Contact.SUR_NAME, "Tester");
            put(Contact.MIDDLE_NAME, "Tiberius");
        }};

        expectations.verify(parser.parse(html).get(0));
    }

    public void testShouldDealWithMoreThanOneValueForAdditionalNames(){
        String html =
            "<div class=\"vcard\">" +
                "<span class=\"fn n\">\n" +
                    "<span class=\"additional-name\">Tiberius</span>\n" +
                    "<span class=\"additional-name\">Thaddeus</span>\n" +
                    "<span class=\"additional-name\">Theseus</span>\n" +
                "</span>\n" +
            "</div>";

        Expectations expectations = new Expectations(){{
            put(Contact.MIDDLE_NAME, "Tiberius Thaddeus Theseus");
        }};

        expectations.verify(parser.parse(html).get(0));
    }

    public void testShouldDealWithPhotoUrl(){
        String html =  "<div class=\"vcard\"><span class=\"fn n\">\n" +
        "<span class=\"given-name bullshit value\">Terry</span>\n" +
        "<span class=\"bullshit-value additional-name\">Tiberius</span>\n" +
        "<span class=\"family-name familyname\">Tester</span>\n" +
        "</span>\n" +
        "<img class=\"photo\" src=\"http://www.google.de/intl/de_de/images/logo.gif\"/>\n" + //FIXME Use local logo, make google happy
        "</div>";


        Contact contact = parser.parse(html).get(0);

        final Collection<OXException> warnings = contact.getWarnings();
        if (null != warnings && !warnings.isEmpty()) {
            final OXException warning = warnings.iterator().next();
            final Throwable cause = warning.getCause();
            if (cause instanceof java.net.UnknownHostException) {
                // Host "www.google.de" currently not reachable
                return;
            }
        }

        assertTrue("Should contain an image but does not", contact.containsImage1());
    }

    public void testShouldDealWithPhotoUrlToMissingImage(){
        String html =  "<div class=\"vcard\"><span class=\"fn n\">\n" +
        "<span class=\"given-name bullshit value\">William</span>\n" +
        "<span class=\"bullshit-value additional-name\">Tiberius</span>\n" +
        "<span class=\"family-name familyname\">Shatner</span>\n" +
        "</span>\n" +
        "<img class=\"photo\" src=\"http://example.com/william_shatner.jpg\"/>\n" +
        "</div>";

        Contact contact = parser.parse(html).get(0);
        assertFalse("Should not contain an image", contact.containsImage1());
    }

    public void testShouldDealWithInlinedPhoto(){
        String html = "<table class=\"vcard\">" +
            "<div class=\"fn n\">\n" +
                "<td>Given name</td><td><span class=\"given-name bullshit value\">Terry</span></td>\n" +
                "<td>Family name</td><td><span class=\"family-name familyname\">Tester</span></td>\n" +
                "<td>Photo</td><td><img class=\"photo\" src=\"data:image/gif;base64,"+gifBase64+"\" />\n" +
            "</div>\n" +
         "</table>";
        Contact contact = parser.parse(html).get(0);
        assertTrue("Should contain an image but does not", contact.containsImage1());
        String actual = Base64.encode(contact.getImage1());
        assertEquals("Should remember this is a .gif", "image/gif", contact.getImageContentType());
        assertEquals("Should have same contents", gifBase64, actual);
    }

    public void testShouldDealWithTableLayouts(){
        String html =
            "<table class=\"vcard\">" +
                "<div class=\"fn n\">\n" +
                    "<td>Given name</td><td><span class=\"given-name bullshit value\">Terry</span></td>\n" +
                    "<td>Additional name</td><td><span class=\"bullshit-value additional-name\">Tiberius</span></td>\n" +
                    "<td>Family name</td><td><span class=\"family-name familyname\">Tester</span></td>\n" +
                "</div>\n" +
            "</table>";

        Expectations expectations = new Expectations(){{
            put(Contact.GIVEN_NAME, "Terry");
            put(Contact.SUR_NAME, "Tester");
            put(Contact.MIDDLE_NAME, "Tiberius");
        }};

        expectations.verify(parser.parse(html).get(0));
    }

    public void testShouldFindOXMFData(){
        String html = "<div class=\"vcard\" id=\"a\""+HCARD_CONTENT+"</div>"
        + "<div class=\"vcard\" id=\"b\""+HCARD_CONTENT+"</div>"
        + "<div class=\"vcard\" id=\"c\""+HCARD_CONTENT+"</div>";

        List<Contact> contacts = parser.parse(html);
        int expectedNumber = 3;
        assertEquals("Should find "+ expectedNumber +" contacts", expectedNumber, contacts.size());
        List<Map<String, String>> oxmfdata = parser.getOXMFData();
        assertEquals("Should find "+ expectedNumber +" sets of OXMF data", expectedNumber, contacts.size());

        int i = 0;
        for(Map<String, String> map: oxmfdata){
            assertEquals("Should find two elements for contact #"+(i++), 2, map.size());

            String value = map.get(OXMFVisitor.OXMF_PREFIX+"userfield01");
            assertEquals("Should get the value entered on first field", "One of them userfields that are not used in HCard at all.", value);

            value = map.get(OXMFVisitor.OXMF_PREFIX+"userfield02");
            assertEquals("Should get the value entered on first field", "Another userfield.", value);
        }
    }

    public void testShouldMergeDataProperlyIfUsingOXMFElementValue(){
        String html =
            "<div class=\"vcard\" id=\"a\">" +
                "<span class=\"fn n\">\n" +
                    "<span class=\"given-name\">Terry</span>\n" +
                "</span>\n" +
                "<span class=\""+OXMFVisitor.OXMF_PREFIX+"spouseName\">" +
                    "spouse1" +
                "</span>" +
            "</div>" +
            "<div class=\"vcard\" id=\"b\">"+
                "<span class=\"fn n\">\n" +
                    "<span class=\"given-name\">Toni</span>\n" +
                "</span>\n" +
                "<span class=\""+OXMFVisitor.OXMF_PREFIX+"spouseName\">" +
                    "spouse2" +
                "</span>" +
            "</div>";

        List<Contact> contacts = parser.parse(html);
        int expectedNumber = 2;
        assertEquals("Should find "+ expectedNumber +" contacts", expectedNumber, contacts.size());

        int i = 0;
        for(Contact contact: contacts){
            assertEquals("Should have included OX-specific information (spouseName) in contact", "spouse"+(++i), contact.getSpouseName());
        }
    }

    public void testShouldMergeDataProperlyIfUsingSeparateValueElement(){
        String html =
            "<div class=\"vcard\" id=\"a\">" +
                "<span class=\"fn n\">\n" +
                    "<span class=\"given-name\">Terry</span>\n" +
                "</span>\n" +
                "<span class=\""+OXMFVisitor.OXMF_PREFIX+"spouseName\">" +
                    "<span class=\"value\">spouse1</span>" +
                "</span>" +
            "</div>" +
            "<div class=\"vcard\" id=\"b\">"+
                "<span class=\"fn n\">\n" +
                    "<span class=\"given-name\">Toni</span>\n" +
                "</span>\n" +
                "<span class=\""+OXMFVisitor.OXMF_PREFIX+"spouseName\">" +
                    "<span class=\"value\">spouse2</span>" +
                "</span>" +
            "</div>";

        List<Contact> contacts = parser.parse(html);
        int expectedNumber = 2;
        assertEquals("Should find "+ expectedNumber +" contacts", expectedNumber, contacts.size());

        int i = 0;
        for(Contact contact: contacts){
            assertEquals("Should have included OX-specific information (spouseName) in contact", "spouse"+(++i), contact.getSpouseName());
        }
    }


    public void testShouldPrioritizeSeparateValueOverElementValue(){
        String html =
            "<div class=\"vcard\" id=\"a\">" +
                "<span class=\"fn n\">\n" +
                    "<span class=\"given-name\">Terry</span>\n" +
                "</span>\n" +
                "<span class=\""+OXMFVisitor.OXMF_PREFIX+"spouseName\">" +
                    "no spouse 1"+
                    "<span class=\"value\">spouse1</span>" +
                "</span>" +
            "</div>" +
            "<div class=\"vcard\" id=\"b\">"+
                "<span class=\"fn n\">\n" +
                    "<span class=\"given-name\">Toni</span>\n" +
                "</span>\n" +
                "<span class=\""+OXMFVisitor.OXMF_PREFIX+"spouseName\">" +
                    "no spouse 2"+
                    "<span class=\"value\">spouse2</span>" +
                "</span>" +
            "</div>";

        List<Contact> contacts = parser.parse(html);
        int expectedNumber = 2;
        assertEquals("Should find "+ expectedNumber +" contacts", expectedNumber, contacts.size());

        int i = 0;
        for(Contact contact: contacts){
            assertEquals("Should have included OX-specific information (spouseName) in contact", "spouse"+(++i), contact.getSpouseName());
        }
    }

    public void testShouldNotMakeNullFieldsEmptyStringFields(){
        List<Contact> contacts = parser.parse(HCARD_SNIPPET);
        Contact actual = contacts.get(0);

        assertNull("Street (other) was not in the hcard, should be null", actual.getStreetOther());
    }

    public void testShouldStripTrailingWhitespaces(){
        String html =
            "<div class=\"vcard\" id=\"a\">" +
                "<span class=\"fn n\">\n" +
                    "<span class=\"given-name\"> Terry </span>\n" +
                "</span>\n" +
            "</div>";
        List<Contact> contacts = parser.parse(html);
        Contact actual = contacts.get(0);
        assertEquals("Should contain the given name without any whitespaces around it", "Terry", actual.getGivenName());
    }

}
