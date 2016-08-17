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

package com.openexchange.contact.vcard;

import java.awt.image.BufferedImage;
import java.util.List;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.java.util.TimeZones;
import com.openexchange.time.TimeTools;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * {@link BasicTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BasicTest extends VCardTest {

    private final String NEWLINE = System.getProperty("line.separator");

    /**
     * Initializes a new {@link BasicTest}.
     */
    public BasicTest() {
        super();
    }

    public void testImportVCard1() throws Exception {
        /*
         * http://de.wikipedia.org/wiki/VCard#vCard_2.1
         */
        String vCardString =
            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "N:Mustermann;Erika\r\n"+
            "FN:Erika Mustermann\r\n"+
            "ORG:Wikipedia\r\n"+
            "TITLE:Oberleutnant\r\n"+
            "PHOTO;JPEG:https://upload.wikimedia.org/wikipedia/commons/3/3d/Erika_Mustermann_2010.jpg\r\n"+
            "TEL;WORK;VOICE:(0221) 9999123\r\n"+
            "TEL;HOME;VOICE:(0221) 1234567\r\n"+
            "ADR;HOME:;;Heidestrasse 17;Koeln;;51147;Deutschland\r\n"+
            "EMAIL;PREF;INTERNET:erika@mustermann.de\r\n"+
            "REV:20140301T221110Z\r\n"+
            "END:VCARD\r\n"
        ;
        VCard vCard = parse(vCardString);
        Contact contact = getMapper().importVCard(vCard, null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Erika Mustermann", contact.getDisplayName());
        assertEquals("Mustermann", contact.getSurName());
        assertEquals("Erika", contact.getGivenName());
        assertEquals("Wikipedia", contact.getCompany());
        assertEquals("Oberleutnant", contact.getPosition());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
        assertEquals("(0221) 9999123", contact.getTelephoneBusiness1());
        assertEquals("(0221) 1234567", contact.getTelephoneHome1());
        assertEquals("Heidestrasse 17", contact.getStreetHome());
        assertEquals("Koeln", contact.getCityHome());
        assertEquals("51147", contact.getPostalCodeHome());
        assertEquals("Deutschland", contact.getCountryHome());
        assertEquals("erika@mustermann.de", contact.getEmail1());
    }

    public void testImportVCard2() throws Exception {
        /*
         * http://de.wikipedia.org/wiki/VCard#vCard_3.0
         */
        String vCardString =
            "BEGIN:VCARD\r\n"+
            "VERSION:3.0\r\n"+
            "N:Mustermann;Erika\r\n"+
            "FN:Erika Mustermann\r\n"+
            "ORG:Wikipedia\r\n"+
            "TITLE:Oberleutnant\r\n"+
            "PHOTO;VALUE=URL;TYPE=JPEG:https://upload.wikimedia.org/wikipedia/commons/3/3d/Erika_Mustermann_2010.jpg\r\n"+
            "TEL;TYPE=WORK,VOICE:+49 221 9999123\r\n"+
            "TEL;TYPE=HOME,VOICE:+49 221 1234567\r\n"+
            "ADR;TYPE=HOME:;;Heidestra\u00dfe 17;K\u00f6ln;;51147;Germany\r\n"+
            "EMAIL;TYPE=PREF,INTERNET:erika@mustermann.de\r\n"+
            "URL:http://de.wikipedia.org/\r\n"+
            "REV:2014-03-01T22:11:10Z\r\n"+
            "END:VCARD\r\n"
        ;
        VCard vCard = parse(vCardString);
        Contact contact = getMapper().importVCard(vCard, null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Erika Mustermann", contact.getDisplayName());
        assertEquals("Mustermann", contact.getSurName());
        assertEquals("Erika", contact.getGivenName());
        assertEquals("Wikipedia", contact.getCompany());
        assertEquals("Oberleutnant", contact.getPosition());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
        assertEquals("+49 221 9999123", contact.getTelephoneBusiness1());
        assertEquals("+49 221 1234567", contact.getTelephoneHome1());
        assertEquals("Heidestra\u00dfe 17", contact.getStreetHome());
        assertEquals("K\u00f6ln", contact.getCityHome());
        assertEquals("51147", contact.getPostalCodeHome());
        assertEquals("Germany", contact.getCountryHome());
        assertEquals("erika@mustermann.de", contact.getEmail1());
        assertEquals("http://de.wikipedia.org/", contact.getURL());
    }

    public void testImportVCard3() throws Exception {
        /*
         * http://de.wikipedia.org/wiki/VCard#vCard_4.0
         */
        String vCardString =
            "BEGIN:VCARD\r\n"+
            "VERSION:4.0\r\n"+
            "N:Mustermann;Erika;;;\r\n"+
            "FN:Erika Mustermann\r\n"+
            "ORG:Wikipedia\r\n"+
            "TITLE:Oberleutnant\r\n"+
            "PHOTO;MEDIATYPE=image/jpeg:https://upload.wikimedia.org/wikipedia/commons/3/3d/Erika_Mustermann_2010.jpg\r\n"+
            "TEL;TYPE=work,voice;VALUE=uri:tel:+49-221-9999123\r\n"+
            "TEL;TYPE=home,voice;VALUE=uri:tel:+49-221-1234567\r\n"+
            "ADR;TYPE=home;LABEL=\"Heidestra\u00dfe 17\\n51147 K\u00f6ln\\nDeutschland\"\r\n"+
            " :;;Heidestra\u00dfe 17;K\u00f6ln;;51147;Germany\r\n"+
            "EMAIL:erika@mustermann.de\r\n"+
            "REV:20140301T221110Z\r\n"+
            "END:VCARD\r\n"
        ;
        VCard vCard = parse(vCardString);
        Contact contact = getMapper().importVCard(vCard, null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Erika Mustermann", contact.getDisplayName());
        assertEquals("Mustermann", contact.getSurName());
        assertEquals("Erika", contact.getGivenName());
        assertEquals("Wikipedia", contact.getCompany());
        assertEquals("Oberleutnant", contact.getPosition());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
        assertEquals("+49-221-9999123", contact.getTelephoneBusiness1());
        assertEquals("+49-221-1234567", contact.getTelephoneHome1());
        assertEquals("Heidestra\u00dfe 17" + NEWLINE + "51147 K\u00f6ln" + NEWLINE + "Deutschland", contact.getAddressHome());
        assertEquals("Heidestra\u00dfe 17", contact.getStreetHome());
        assertEquals("K\u00f6ln", contact.getCityHome());
        assertEquals("51147", contact.getPostalCodeHome());
        assertEquals("Germany", contact.getCountryHome());
        assertEquals("erika@mustermann.de", contact.getEmail1());
    }

    public void testImportVCard4() throws Exception {
        /*
         * http://www.w3.org/2002/12/cal/vcard-examples/john-doe.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "N:Doe;John;;;\r\n" +
            "FN:John Doe\r\n" +
            "ORG:Example.com Inc.;\r\n" +
            "TITLE:Imaginary test person\r\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:johnDoe@example.org\r\n" +
            "TEL;type=WORK;type=pref:+1 617 555 1212\r\n" +
            "TEL;type=WORK:+1 (617) 555-1234\r\n" +
            "TEL;type=CELL:+1 781 555 1212\r\n" +
            "TEL;type=HOME:+1 202 555 1212\r\n" +
            "item1.ADR;type=WORK:;;2 Enterprise Avenue;Worktown;NY;01111;USA\r\n" +
            "item1.X-ABADR:us\r\n" +
            "item2.ADR;type=HOME;type=pref:;;3 Acacia Avenue;Hoemtown;MA;02222;USA\r\n" +
            "item2.X-ABADR:us\r\n" +
            "NOTE:John Doe has a long and varied history\\, being documented on more police files that anyone else. Reports of his death are alas numerous.\r\n" +
            "item3.URL;type=pref:http\\://www.example/com/doe\r\n" +
            "item3.X-ABLabel:_$!<HomePage>!$_\r\n" +
            "item4.URL:http\\://www.example.com/Joe/foaf.df\r\n" +
            "item4.X-ABLabel:FOAF\r\n" +
            "item5.X-ABRELATEDNAMES;type=pref:Jane Doe\r\n" +
            "item5.X-ABLabel:_$!<Friend>!$_\r\n" +
            "CATEGORIES:Work,Test group\r\n" +
            "X-ABUID:5AD380FD-B2DE-4261-BA99-DE1D1DB52FBE\\:ABPerson\r\n" +
            "END:VCARD\r\n"
        ;
        VCard vCard = parse(vCardString);
        Contact contact = getMapper().importVCard(vCard, null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("John Doe", contact.getDisplayName());
        assertEquals("Doe", contact.getSurName());
        assertEquals("John", contact.getGivenName());
        assertEquals("Example.com Inc.", contact.getCompany());
        assertEquals("Imaginary test person", contact.getPosition());
        assertEquals("+1 617 555 1212", contact.getTelephoneBusiness1());
        assertEquals("+1 (617) 555-1234", contact.getTelephoneBusiness2());
        assertEquals("+1 781 555 1212", contact.getCellularTelephone1());
        assertEquals("+1 202 555 1212", contact.getTelephoneHome1());
        assertEquals("2 Enterprise Avenue", contact.getStreetBusiness());
        assertEquals("Worktown", contact.getCityBusiness());
        assertEquals("NY", contact.getStateBusiness());
        assertEquals("01111", contact.getPostalCodeBusiness());
        assertEquals("USA", contact.getCountryBusiness());
        assertEquals("3 Acacia Avenue", contact.getStreetHome());
        assertEquals("Hoemtown", contact.getCityHome());
        assertEquals("MA", contact.getStateHome());
        assertEquals("02222", contact.getPostalCodeHome());
        assertEquals("USA", contact.getCountryHome());
        assertEquals("John Doe has a long and varied history, being documented on more police files that anyone else. Reports of his death are alas numerous.", contact.getNote());
        assertEquals("http://www.example/com/doe", contact.getURL());
        assertEquals("Work,Test group", contact.getCategories());
    }

    public void testImportVCard5() throws Exception {
        /*
         * https://github.com/nuovo/vCard-parser/blob/master/Example.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "N:Doe;John;Q.,Public\r\n" +
            "FN;CHARSET=UTF-8:John Doe\r\n" +
            "TEL;TYPE=WORK,VOICE:(111) 555-1212\r\n" +
            "TEL;TYPE=HOME,VOICE:(404) 555-1212\r\n" +
            "TEL;TYPE=HOME,TYPE=VOICE:(404) 555-1213\r\n" +
            "EMAIL;TYPE=PREF,INTERNET:forrestgump@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:example@example.com\r\n" +
            "ADR;TYPE=HOME:;;42 Plantation St.;Baytown;LA;30314;United States of America\r\n" +
            "URL:https://www.google.com/\r\n" +
            "PHOTO;VALUE=URL;TYPE=PNG:https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Example_svg.svg/200px-Example_svg.svg.png\r\n" +
            "AGENT:BEGIN:VCARD\r\n" +
            " VERSION:3.0\r\n" +
            " N:Doe;John;Q.,Public\r\n" +
            " FN:John Doe\r\n" +
            " TEL;TYPE=WORK,VOICE:(111) 555-1212\r\n" +
            " TEL;TYPE=HOME,VOICE:(404) 555-1212\r\n" +
            " TEL;TYPE=HOME,TYPE=VOICE:(404) 555-1213\r\n" +
            " EMAIL;TYPE=PREF,INTERNET:forrestgump@example.com\r\n" +
            " EMAIL;TYPE=INTERNET:example@example.com\r\n" +
            " PHOTO;VALUE=URL;TYPE=PNG:https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Example_svg.svg/200px-Example_svg.svg.png\r\n" +
            " END:VCARD\r\n" +
            "END:VCARD"
        ;
        VCard vCard = parse(vCardString);
        Contact contact = getMapper().importVCard(vCard, null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("John Doe", contact.getDisplayName());
        assertEquals("Doe", contact.getSurName());
        assertEquals("John", contact.getGivenName());
        assertEquals("Q. Public", contact.getMiddleName());
        assertEquals("(111) 555-1212", contact.getTelephoneBusiness1());
        assertEquals("(404) 555-1212", contact.getTelephoneHome1());
        assertEquals("(404) 555-1213", contact.getTelephoneHome2());
        assertEquals("forrestgump@example.com", contact.getEmail1());
        assertEquals("42 Plantation St.", contact.getStreetHome());
        assertEquals("Baytown", contact.getCityHome());
        assertEquals("LA", contact.getStateHome());
        assertEquals("30314", contact.getPostalCodeHome());
        assertEquals("United States of America", contact.getCountryHome());
        assertEquals("https://www.google.com/", contact.getURL());
        assertEquals("image/png", contact.getImageContentType());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
    }

    public void testImportVCard6() throws Exception {
        /*
         * https://github.com/mangstadt/ez-vcard/blob/master/src/test/resources/ezvcard/io/text/John_Doe_ANDROID.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:2.1\r\n" +
            "EMAIL;PREF:john.doe@company.com\r\n" +
            "CATEGORIES:My Contacts\r\n" +
            "END:VCARD\r\n" +
            "BEGIN:VCARD\r\n" +
            "VERSION:2.1\r\n" +
            "EMAIL;PREF:jane.doe@company.com\r\n" +
            "CATEGORIES:My Contacts\r\n" +
            "END:VCARD\r\n" +
            "BEGIN:VCARD\r\n" +
            "VERSION:2.1\r\n" +
            "N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=20=C3=91=20=C3=91=20=C3=91=20;;;;\r\n" +
            "FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20\r\n" +
            "TEL;CELL;PREF:123456789\r\n" +
            "CATEGORIES:My Contacts\r\n" +
            "END:VCARD\r\n" +
            "BEGIN:VCARD\r\n" +
            "VERSION:2.1\r\n" +
            "N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=\r\n" +
            "=20=C3=91=20=C3=91=20=C3=91;;;;\r\n" +
            "FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=\r\n" +
            "=20=C3=91=20=C3=91=20=C3=91\r\n" +
            "TEL;CELL;PREF:123456\r\n" +
            "TEL;HOME:234567\r\n" +
            "TEL;CELL:3456789\r\n" +
            "TEL;HOME:45678901\r\n" +
            "CATEGORIES:My Contacts\r\n" +
            "NOTE;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=\r\n" +
            "=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=\r\n" +
            "=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20\r\n" +
            "NOTE;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=\r\n" +
            "=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=\r\n" +
            "=C3=91=20=C3=91=20=C3=91=20=C3=91=20=C3=91=20\r\n" +
            "END:VCARD\r\n" +
            "BEGIN:VCARD\r\n" +
            "VERSION:2.1\r\n" +
            "N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=20=C3=91=20;=C3=91=20=C3=91=20=C3=91=20;;;\r\n" +
            "FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=20=C3=91=20=C3=91=20=C3=91=20\r\n" +
            "TEL;CELL;PREF:123456\r\n" +
            "TEL;WORK:123456\r\n" +
            "TEL;WORK;FAX:123456\r\n" +
            "EMAIL;PREF;WORK:bob@company.com\r\n" +
            "EMAIL;PREF;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91\r\n" +
            "ORG;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91\r\n" +
            "ORG;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91\r\n" +
            "URL:www.company.com\r\n" +
            "URL:http://www.company.com\r\n" +
            "PHOTO;ENCODING=BASE64;JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQE\r\n" +
            " CAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/\r\n" +
            " 2wBDAQICAgICAgUDAwUKBwYHCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKC\r\n" +
            " b2wxBqZRuiZQ8z9VmhGMY/wA803yB/dr4Z8a/tiftAaX8DPhLYzeM9U0a98WeCtH0tvFv9mW8\r\n" +
            " 39ra1qWnWcseqSLIirBYWkn2gXc8TP5L3EI8qTcNnrOp/tsa7P8AseD9prxf8N7zwesGtiwuN\r\n" +
            " Ni1D7Tv/eFVmhuJoofOjO5VJKrh1dRu2ZM+zZKg2fO+g/8ABEP45eC/Dmn+HPBX7ZWhab/Zt2\r\n" +
            " 9xAyfDS2kWF3WRWeNXlPlsVkKkrjhn5+Yg/Xn7HPwP+PHwC+GV74L/AGiv2kLn4natc6kLm21\r\n" +
            " u6077L9ni8qNPJEe9wVDozgZwN5AA6n1ouMnJpkpDPk9cD+ZqnVqTiqUn6GtStiffUqnNzO+/\r\n" +
            " /B8zlvG/wN+D3xF+IXhL4u+O/AdnqfibwFNdS+D9WuVYyaW1yiRzmIbsDesaA5B+6OlX/EXg3\r\n" +
            " wH4t1fT/EPizwRDqF7pyXENhczMzNCk8kbyqPRWaCIsvIOxfSttSxIKjpSPdvEyBEyd+1/xP/\r\n" +
            " 1qcXpyvdC5tDmdH+CHwg8K/EGb4w6B8PtKs/E95pkljJrUK4nFqxhJhz0K/wCj2/H/AExX0pu\r\n" +
            " jfBH4TaR8ZtX/AGgvDfw7tbXxd4g09LPVvXXKt9qnhQQqI3OcY220Axxwi/jxv7S/7eH7Kn7H\r\n" +
            " R0lP2g/iBeaJJrOuRaVYNa6PcXXm3MymRYz5UbhV2jJY4xxz1r0fwR498K+PtGbXfCGr/a7ZJ\r\n" +
            " 9j7w3CvijVraDxdbyWg/4lFlOVN2WP8AZ7GUhnJKv5Ifacule3Qz+QTKD3HanDUELEk9T6UKS\r\n" +
            " juVGVjxGe31HUdS1/4da/8Ask6APDvgrV4W8GzHT1mi1DzhPLK8UL2IS1O9V3PE8py+Tgnnn/\r\n" +
            " AmoQfD/wAeab4J8F/8E3tC0qz1LTJJ9V1/RdO+zR2p8yTFviLTVWUsYYGO50+/nB2Lv+jnuoZ\r\n" +
            " l2S9ByOKYLrys+SP0p+0iPnP/2Q==\r\n" +
            "\r\n" +
            "END:VCARD\r\n" +
            "BEGIN:VCARD\r\n" +
            "VERSION:2.1\r\n" +
            "N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=C3=91=C3=91=C3=91;;;;\r\n" +
            "FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=C3=91=C3=91=C3=91\r\n" +
            "TEL;CELL;PREF:55556666\r\n" +
            "EMAIL;PREF:henry@company.com\r\n" +
            "ORG;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "\r\n" +
            "ORG;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=80\r\n" +
            "ORG;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=C3=91=\r\n" +
            "\r\n" +
            "CATEGORIES:My Contacts\r\n" +
            "END:VCARD"
        ;
        List<VCard> vCards = Ezvcard.parse(vCardString).all();
        /*
         * verify 1st imported contact
         */
        Contact contact = getMapper().importVCard(vCards.get(0), null, null, null);
        assertNotNull(contact);
        assertEquals("john.doe@company.com", contact.getEmail1());
        assertEquals("My Contacts", contact.getCategories());
        /*
         * verify 2nd imported contact
         */
        contact = getMapper().importVCard(vCards.get(1), null, null, null);
        assertNotNull(contact);
        assertEquals("jane.doe@company.com", contact.getEmail1());
        assertEquals("My Contacts", contact.getCategories());
        /*
         * verify 3rd imported contact
         */
        contact = getMapper().importVCard(vCards.get(2), null, null, null);
        assertNotNull(contact);
        assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ", contact.getDisplayName());
        assertEquals("\u00d1 \u00d1 \u00d1 \u00d1", contact.getSurName());
        assertEquals("123456789", contact.getCellularTelephone1());
        assertEquals("My Contacts", contact.getCategories());
        /*
         * verify 4th imported contact
         */
        contact = getMapper().importVCard(vCards.get(3), null, null, null);
        assertNotNull(contact);
        assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1", contact.getDisplayName());
        assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1", contact.getSurName());
        assertEquals("123456", contact.getCellularTelephone1());
        assertEquals("234567", contact.getTelephoneHome1());
        assertEquals("3456789", contact.getCellularTelephone2());
        assertEquals("45678901", contact.getTelephoneHome2());
        assertEquals("My Contacts", contact.getCategories());
        assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ", contact.getNote());
        /*
         * verify 5th imported contact
         */
        contact = getMapper().importVCard(vCards.get(4), null, null, null);
        assertNotNull(contact);
        assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 ", contact.getDisplayName());
        assertEquals("\u00d1 \u00d1", contact.getSurName());
        assertEquals("\u00d1 \u00d1 \u00d1", contact.getGivenName());
        assertEquals("123456", contact.getCellularTelephone1());
        assertEquals("123456", contact.getTelephoneBusiness1());
        assertEquals("123456", contact.getFaxBusiness());
        assertEquals("bob@company.com", contact.getEmail1());
        assertEquals("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1", contact.getCompany());
        assertEquals("www.company.com", contact.getURL());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
//        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
//        assertNotNull(bufferedImage);
//        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
        /*
         * verify 6th imported contact
         */
        contact = getMapper().importVCard(vCards.get(5), null, null, null);
        assertNotNull(contact);
        assertEquals("\u00d1\u00d1\u00d1\u00d1", contact.getDisplayName());
        assertEquals("\u00d1\u00d1\u00d1\u00d1", contact.getSurName());
        assertEquals("55556666", contact.getCellularTelephone1());
        assertEquals("henry@company.com", contact.getEmail1());
        assertEquals("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1", contact.getCompany());
        assertEquals("My Contacts", contact.getCategories());
    }

    public void testImportVCard7() throws Exception {
        /*
         * https://github.com/mangstadt/ez-vcard/blob/master/src/test/resources/ezvcard/io/text/John_Doe_BLACK_BERRY.vcf
         */
        String vCardString = "BEGIN:VCARD\r\n" +
            "VERSION:2.1\r\n" +
            "FN:John Doe\r\n" +
            "N:Doe;john;;;\r\n" +
            "ORG:Acme Solutions\r\n" +
            "TEL;TYPE=CELL:+96123456789\r\n" +
            "PHOTO;ENCODING=BASE64:/9j/4QFaRXhpZgAASUkqAAgAAAAAABABAgABAAAAAAAAABIBAwABAAAAAQAAABoBBQABAAAAhgAAABsBBQABAAAAjgAAACgBAwABAAAAAgAAADEBAgAWAAAAlgAAADIBAgABAAAAAAAAABMCAwABAAAAAQAAAGmHBAABAAAArAAAAAAAAABIAAAAAQAAAEgAAAABAAAAUmltIEV4aWYgVmVyc2lvbjEuMDBhAAwAmoIFAAEAAABCAQAAAJAHAAQAAAAwMjIwA5ACAAEAAAAAAAAAAZEHAAQAAAABAgMABpIFAAEAAABKAQAACJIDAAEAAAAAAAAACZIDAAEAAAAgAAAAfJIHAAAAAAAAAAAAAaADAAEAAAABAAAAAqADAAEAAABCAAAAA6ADAAEAAABkAAAAC6QHAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAEAAAD/2wCEABIMDRANCxIQDhAUExIVGywdGxgYGzYnKSAsQDlEQz85Pj1HUGZXR0thTT0+WXlaYWltcnNyRVV9hnxvhWZwcm4BExQUGxcbNB0dNG5JPklubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubv/EAaIAAAEFAQEBAQEBAAAAAAAAAAABAgMEBQYHCAkKCwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoLEAACAQMDAgQDBQUEBAAAAX0BAgMABBEFEiExQQYTUWEHInEUMoGRoQgjQrHBFVLR8CQzYnKCCQoWFxgZGiUmJygpKjQ1Njc4OTpDREVGR0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4eLj5OXm5+jp6vHy8/T19vf4+foRAAIBAgQEAwQHBQQEAAECdwABAgMRBAUhMQYSQVEHYXETIjKBCBRCkaGxwQkjM1LwFWJy0QoWJDThJfEXGBkaJicoKSo1Njc4OTpDREVGR0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoKDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uLj5OXm5+jp6vLz9PX29/j5+v/AABEIAGQAQgMBEQACEQEDEQH/2gAMAwEAAhEDEQA/AOGoAKACgBQCTgdaAHmFlHIoAYaAAEjpQAlABQAUAFABQB0mh6MHtvPn4L8gegoAl1G0tUhb5gD2oA5mRQrkA5oAZQAUAFABQAUAS20XmzKCGK5G7b1xQB3s1tJHpymzTduUbR6UAczqOl6wsZluIn8v2xgUAYjAgkHOfegBKACgAoAKACgC/o09xDeYtnRGcYZnAIA68/lQBq6j4quHIisyI40GC2PvH19qAJpnv7/TP7Q88mNlIeJJCQGHHTt9KAOYdtzZIwaAGkYoASgAoAKACgDp5tKWHwtayxOsZufmlaU4zzwPpQBiS6fcRywx7Qxmx5ZU5D5OODQB0GpwPoFiLaNiN4y/fJxzQBy8rB2yOvegBlACUAFABQAo6igD0a1jnurJGk8n7OsKJFG65xgD5vr1oAybCyvbTUoZt4mUPzzu2j2oAt+NkW4sUlH3lHNAHCUAFACUAFABQAo6igD0qyfzdCiS7SMK0QGNp6fnQBy4W10/URNHK6orcLnAIoAm13VBcbViIMLKpz6UAczJjzGx0zQA2gBKAFoAKAFXgjNAHZ6hrNuNNjWJvmKAcHp/nigDkZpWduSaAGCVguM8UAMoASgAoAKAHqOM0ANNADg7KBzkDsaALWba7ljjjh8h3IUncSufX1/nQBZvfDt/ZffRXHqhzQBmPGyHDAg0ANKkdQaAEoAKAH5wuKAFXnk0AMbrQAqEqQwOCDkfWgD0TT9Xs9WsI98sYnCgSIx2898Z6igDL1b7HArHfEh9iCf8aAOWup1lc+X09aAK4GRmgA/GgBQMtg0APXk80AMYfNQAh64FADvagBMUALjH1oAQ+goAdt96ADHGWoAXkDNADDwOepoAaOtADyeaAFxt/wB7+VAAqknmgCWC2lnfbGhZsgYHv0pqLlsJtLcGgkRinyMLoiz61elAEJ5OaAEBe3tQAuOc0AShQBxx70ASafqD2E3mois2QRuVyeQ+ZIzkLliSeta8zJ5Uf/9k=\r\n" +
            "\r\n" +
            "NOTE:\r\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("John Doe", contact.getDisplayName());
        assertEquals("Doe", contact.getSurName());
        assertEquals("john", contact.getGivenName());
        assertEquals("Acme Solutions", contact.getCompany());
        assertEquals("+96123456789", contact.getCellularTelephone1());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
//        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
//        assertNotNull(bufferedImage);
//        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
    }

    public void testImportVCard8() throws Exception {
        /*
         * https://github.com/mangstadt/ez-vcard/blob/master/src/test/resources/ezvcard/io/text/John_Doe_EVOLUTION.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "X-COUCHDB-APPLICATION-ANNOTATIONS:{\"Evolution\":{\"revision\":\"2012-03-05T13:3\r\n" +
            " 2:54Z\"}}\r\n" +
            "X-AIM;TYPE=HOME;X-COUCHDB-UUID=\"cb9e11fc-bb97-4222-9cd8-99820c1de454\":johnn\r\n" +
            " y5@aol.com\r\n" +
            "URL;X-COUCHDB-UUID=\"0abc9b8d-0845-47d0-9a91-3db5bb74620d\":http://www.ibm.co\r\n" +
            " m\r\n" +
            "TEL;X-COUCHDB-UUID=\"c2fa1caa-2926-4087-8971-609cfc7354ce\";TYPE=CELL:905-666\r\n" +
            " -1234\r\n" +
            "TEL;X-COUCHDB-UUID=\"fbfb2722-4fd8-4dbf-9abd-eeb24072fd8e\";TYPE=WORK,VOICE:9\r\n" +
            " 05-555-1234\r\n" +
            "UID:477343c8e6bf375a9bac1f96a5000837\r\n" +
            "N:Doe;John;Richter\\, James;Mr.;Sr.\r\n" +
            "X-EVOLUTION-FILE-AS:Doe\\, John\r\n" +
            "FN:Mr. John Richter\\, James Doe Sr.\r\n" +
            "NICKNAME:Johny\r\n" +
            "X-EVOLUTION-SPOUSE:Maria\r\n" +
            "ORG:IBM;Accounting;Dungeon\r\n" +
            "TITLE:Money Counter\r\n" +
            "X-EVOLUTION-MANAGER:Big Blue\r\n" +
            "X-EVOLUTION-ASSISTANT:Little Red\r\n" +
            "CATEGORIES:VIP\r\n" +
            "NOTE:THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"A\r\n" +
            " S IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES\\, INCLUDING\\, BUT NOT LIMITED \r\n" +
            " TO\\, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULA\r\n" +
            " R PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRI\r\n" +
            " BUTORS BE LIABLE FOR ANY DIRECT\\, INDIRECT\\, INCIDENTAL\\, SPECIAL\\, EXEMPL\r\n" +
            " ARY\\, OR CONSEQUENTIAL DAMAGES (INCLUDING\\, BUT NOT LIMITED TO\\, PROCUREME\r\n" +
            " NT OF SUBSTITUTE GOODS OR SERVICES\\; LOSS OF USE\\, DATA\\, OR PROFITS\\; OR \r\n" +
            " BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY\\, WHE\r\n" +
            " THER IN CONTRACT\\, STRICT LIABILITY\\, OR TORT (INCLUDING NEGLIGENCE OR OTH\r\n" +
            " ERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE\\, EVEN IF ADVIS\r\n" +
            " ED OF THE POSSIBILITY OF SUCH DAMAGE.\r\n" +
            "EMAIL;TYPE=WORK;X-COUCHDB-UUID=\"83a75a5d-2777-45aa-bab5-76a4bd972490\":john.\r\n" +
            " doe@ibm.com\r\n" +
            "ADR;TYPE=HOME:ASB-123;;15 Crescent moon drive;Albaney;New York;12345;United\r\n" +
            " States of America\r\n" +
            "BDAY:1980-03-22\r\n" +
            "X-EVOLUTION-ANNIVERSARY:1980-03-22\r\n" +
            "REV:2012-03-05T13:32:54Z\r\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("http://www.ibm.com", contact.getURL());
        assertEquals("905-666-1234", contact.getCellularTelephone1());
        assertEquals("905-555-1234", contact.getTelephoneBusiness1());
        assertEquals("477343c8e6bf375a9bac1f96a5000837", contact.getUid());
        assertEquals("Doe", contact.getSurName());
        assertEquals("John", contact.getGivenName());
        assertEquals("Richter, James", contact.getMiddleName());
        assertEquals("Mr.", contact.getTitle());
        assertEquals("Sr.", contact.getSuffix());
        assertEquals("Mr. John Richter, James Doe Sr.", contact.getDisplayName());
        assertEquals("Johny", contact.getNickname());
        assertEquals("Maria", contact.getSpouseName());
        assertEquals("IBM", contact.getCompany());
        assertEquals("Accounting", contact.getDepartment());
        assertEquals("Dungeon", contact.getBranches());
        assertEquals("Money Counter", contact.getPosition());
        assertEquals("Big Blue", contact.getManagerName());
        assertEquals("Little Red", contact.getAssistantName());
        assertEquals("VIP", contact.getCategories());
        String expectedNote =
            "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, " +
            "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE " +
            "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, " +
            "SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; " +
            "LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN " +
            "CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS " +
            "SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
        ;
        assertEquals(expectedNote, contact.getNote());
        assertEquals("john.doe@ibm.com", contact.getEmail1());
        assertEquals("15 Crescent moon drive", contact.getStreetHome());
        assertEquals("Albaney", contact.getCityHome());
        assertEquals("New York", contact.getStateHome());
        assertEquals("12345", contact.getPostalCodeHome());
        assertEquals("UnitedStates of America", contact.getCountryHome());
        assertEquals(TimeTools.D("1980-03-22 00:00:00", TimeZones.UTC), contact.getBirthday());
//        assertEquals(TimeTools.D("1980-03-22 00:00:00", TimeZones.UTC), contact.getAnniversary());
    }

    public void testImportVCard9() throws Exception {
        /*
         * https://github.com/mangstadt/ez-vcard/blob/master/src/test/resources/ezvcard/io/text/John_Doe_GMAIL.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "FN:Mr. John Richter, James Doe Sr.\r\n" +
            "N:Doe;John;Richter\\, James;Mr.;Sr.\r\n" +
            "X-PHONETIC-FIRST-NAME:Jon\r\n" +
            "X-PHONETIC-LAST-NAME:Dow\r\n" +
            "EMAIL;TYPE=INTERNET;TYPE=HOME:john.doe@ibm.com\r\n" +
            "TEL;TYPE=CELL:905-555-1234\r\n" +
            "TEL;TYPE=HOME:905-666-1234\r\n" +
            "ADR;TYPE=HOME:;Crescent moon drive\\n555-asd\\nNice Area\\, Albaney\\, New York\r\n" +
            " 12345\\nUnited States of America;;;;;\r\n" +
            "ORG:IBM\r\n" +
            "TITLE:Money Counter\r\n" +
            "BDAY:1980-03-22\r\n" +
            "URL;TYPE=WORK:http\\://www.ibm.com\r\n" +
            "item1.X-ABDATE:1975-03-01\r\n" +
            "item1.X-ABLabel:_$!<Anniversary>!$_\r\n" +
            "item2.X-ABRELATEDNAMES:Jenny\r\n" +
            "item2.X-ABLabel:_$!<Spouse>!$_\r\n" +
            "NOTE:THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \\\"\r\n" +
            " AS IS\\\" AND ANY EXPRESS OR IMPLIED WARRANTIES\\, INCLUDING\\, BUT NOT LIMITE\r\n" +
            " D TO\\, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICU\r\n" +
            " LAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONT\r\n" +
            " RIBUTORS BE LIABLE FOR ANY DIRECT\\, INDIRECT\\, INCIDENTAL\\, SPECIAL\\, EXEM\r\n" +
            " PLARY\\, OR CONSEQUENTIAL DAMAGES (INCLUDING\\, BUT NOT LIMITED TO\\, PROCURE\r\n" +
            " MENT OF SUBSTITUTE GOODS OR SERVICES\\; LOSS OF USE\\, DATA\\, OR PROFITS\\; O\r\n" +
            " R BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY\\, W\r\n" +
            " HETHER IN CONTRACT\\, STRICT LIABILITY\\, OR TORT (INCLUDING NEGLIGENCE OR O\r\n" +
            " THERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE\\, EVEN IF ADV\r\n" +
            " ISED OF THE POSSIBILITY OF SUCH DAMAGE.\\nFavotire Color: Blue\r\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Doe", contact.getSurName());
        assertEquals("John", contact.getGivenName());
        assertEquals("Richter, James", contact.getMiddleName());
        assertEquals("Mr.", contact.getTitle());
        assertEquals("Sr.", contact.getSuffix());
        assertEquals("Mr. John Richter, James Doe Sr.", contact.getDisplayName());
        assertEquals("Jon", contact.getYomiFirstName());
        assertEquals("Dow", contact.getYomiLastName());
        assertEquals("john.doe@ibm.com", contact.getEmail2());
        assertEquals("905-555-1234", contact.getCellularTelephone1());
        assertEquals("905-666-1234", contact.getTelephoneHome1());
        assertEquals("IBM", contact.getCompany());
        assertEquals("Money Counter", contact.getPosition());
        assertEquals("john.doe@ibm.com", contact.getEmail2());
        assertEquals("http://www.ibm.com", contact.getURL());
        String expectedNote =
            "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, " +
            "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE " +
            "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, " +
            "SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; " +
            "LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN " +
            "CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS " +
            "SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue"
        ;
        assertEquals(expectedNote, contact.getNote());
    }

    public void testImportVCard10() throws Exception {
        /*
         * https://github.com/mangstadt/ez-vcard/blob/master/src/test/resources/ezvcard/io/text/John_Doe_IPHONE.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "PRODID:-//Apple Inc.//iOS 5.0.1//EN\r\n" +
            "N:Doe;John;Richter,James;Mr.;Sr.\r\n" +
            "FN:Mr. John Richter James Doe Sr.\r\n" +
            "NICKNAME:Johny\r\n" +
            "ORG:IBM;Accounting\r\n" +
            "TITLE:Money Counter\r\n" +
            "item1.EMAIL;type=INTERNET;type=pref:john.doe@ibm.com\r\n" +
            "TEL;type=CELL;type=VOICE;type=pref:905-555-1234\r\n" +
            "TEL;type=HOME;type=VOICE:905-666-1234\r\n" +
            "TEL;type=WORK;type=VOICE:905-777-1234\r\n" +
            "TEL;type=HOME;type=FAX:905-888-1234\r\n" +
            "TEL;type=WORK;type=FAX:905-999-1234\r\n" +
            "TEL;type=PAGER:905-111-1234\r\n" +
            "item2.TEL:905-222-1234\r\n" +
            "item2.X-ABLabel:_$!<AssistantPhone>!$_\r\n" +
            "item3.ADR;type=HOME;type=pref:;;Silicon Alley 5,;New York;New York;12345;United States of America\r\n" +
            "item3.X-ABADR:Silicon Alley\r\n" +
            "item4.ADR;type=WORK:;;Street4\\nBuilding 6\\nFloor 8;New York;;12345;USA\r\n" +
            "item4.X-ABADR:Street 4, Building 6,\\n Floor 8\\nNew York\\nUSA\r\n" +
            "item5.URL;type=pref:http\\://www.ibm.com\r\n" +
            "item5.X-ABLabel:_$!<HomePage>!$_\r\n" +
            "BDAY;value=date:2012-06-06\r\n" +
            "PHOTO;ENCODING=b;TYPE=JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/4QBYRXhpZgAATU0AKgAA\r\n" +
            " AAgAAgESAAMAAAABAAEAAIdpAAQAAAABAAAAJgAAAAAAA6ABAAMAAAABAAEAAKACAAQAAAABAA\r\n" +
            " ABQKADAAQAAAABAAABQAAAAAD/2wBDAAIBAQIBAQICAQICAgICAwUDAwMDAwYEBAMFBwYHBwcG\r\n" +
            " BgYHCAsJBwgKCAYGCQ0JCgsLDAwMBwkNDg0MDgsMDAv/2wBDAQICAgMCAwUDAwULCAYICwsLCw\r\n" +
            " sLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwv/wAARCAFAAUAD\r\n" +
            " ASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAw\r\n" +
            " UFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJico\r\n" +
            " KSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5\r\n" +
            " iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3\r\n" +
            " +Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQ\r\n" +
            " J3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2\r\n" +
            " Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoq\r\n" +
            " OkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oA\r\n" +
            " DAMBAAIRAxEAPwD3H9mkj/hnL4fj18N6d0/69Y67V1wwAz7ZFcR+zR/ybp8P8HH/ABTencf9us\r\n" +
            " ddqWL43HOK5GdaH8Z2tTslAQAfbNRg4bLUCUse70hjgu1sN/F1pHzuHsaUtucEjFSJ+8U7qAGh\r\n" +
            " SelGNvLdKesKuCGOB60hQAY6gUAIQw5UcU9F5G4VJsIBCgEH1qSKIsuRgGgCPbtOBn8qkA3DI7\r\n" +
            " 0/b83q1TrGAgNADIFwSG61KIQenNOhTcxKge1V9X8QQaFA0l66BFGSd3SmlcVicIVPTioL7UYL\r\n" +
            " GMvdSJGi9SxwK+W/2nv+CnOh/C6a90L4cqNZ8SRJ88YBaK2z6kdSK+OvjB+2b4zvdYstc8f+Jd\r\n" +
            " SsrC6Ty4tOjjIVn6E4HbmlH3thXsfqDr/x78LeGh/xONVt4lXoxPWvNPE/7deg218tv4cia5Qy\r\n" +
            " iLzmbC/Wvz51n4zal4/8OyRWH2i5ns0DWgnIRZCT1OT0q14P8TixtUl8RSWU2pXimJYYZt0ccp\r\n" +
            " GBk+x71olYl66n2N4t/bp1VvFslpp9r5dkAPKfYSrjuSak0/8Aao1Xw7fOdUltJwy+Z5aAgkHk\r\n" +
            " AV8XaZ8R9T8F+PJtE8YTDUzpQRp7hY/lj3c4VvQZHJ4qz8cPjDYaH44tbrwIX8QXeqRIJVi/1a\r\n" +
            " kcbQMckeoquUjmR9qz/tlazdLDc25s7e2lO1UIIYEetbZ/aru9X00mbT5XeMjLA4BHqDXxHf8A\r\n" +
            " jfTfiZ4Rspry8l0y+0S4aE2UZ/egkfNkH39a1Phj451rxxpN7oHhi7ltY0fet7cRMswwOh5xj8\r\n" +
            " KORbj57bH3r4V/aQs2kgj1qGWEOvD+n1r1HRtYtdcsxNYyiVGGQQa/Nb4a/FDVdLuJIrrU7bxK\r\n" +
            " LaQRPDFJ93HB4I5wR2r0PQv2mvEXh3xSIn06bT7TjyWik3KcgdQaOUpVO596CIZG3HIyDSugx+\r\n" +
            " 7OK8O+Ff7TMWoXsUPiie2gacKUw4IwfXB4/wDrGvZtP16HU4g8E0bp1+UismncpSTLJXnpgU1o\r\n" +
            " xj932qyF4x61CIyc4oasUQGJicgikYFpuKmxgNkdKZgKcnORRbqAx8hgF/GkUHf3xUhcsOOlGQ\r\n" +
            " F560gELAHDHGaa8Y7DI9aRiGYZp0sgAwvSglu24zOAKNvqGpy/OmTTXXaCcnigadyRYd6kycAe\r\n" +
            " tNRTtbb69adksBknHpSoMcDoaBSTexGVJOOp9qkU4PNKVIPyk8daRlAHFPlGtjyn9mYZ/Zw8Aj\r\n" +
            " 18N6d/6TR12zKEOF6Vxf7NMe39nL4f7SOfDenf+ksddsVAPzEGhghiEMu1utPA2jAoVFX7pOaV\r\n" +
            " WBxyM/WkMF/1ef4u1KOD8ueetIRnnmlUHPFADo4yXIY5HWpR05qO3GWG7sakILsfL7UAPibGc+\r\n" +
            " mKmUkrg1CikAd8nmp1BHO2gBYE84Ed16VYiUlOM/l1pLeDaVMZHNUPHXjax+HXh241HXnEdvbo\r\n" +
            " zs2fQZovbcRD4/8AHWm/DXwjd6v4quIbW0tU3O8hwF/OvgX9rf8Ab1b4m6bPYfDi9kstMxma8I\r\n" +
            " KSyDoFjXrt/wBo1y/7b37cc3xl01bFoXstLF1i2tklLNdHorOB0FfIsHxB0q0u9cad7q4nMgEr\r\n" +
            " SNiNiD9xe+M9qXxEt9jpbgatY+JTeaNd2EFpd8Azf66645YnqcVjfEfxbd+GrHRotV+ya7Ad8z\r\n" +
            " k5JtSxxt2579efSs/UNJvPHHi2yv7i6GnWU7pHGjybCBjnb6Z9BUWs+HpvhRdq+lLDrMd9cEMx\r\n" +
            " UyNCq9Bj1PrW8VbQR3eifE24jtNSsptNhGn39tsM84Cparjhhj+laHwg+Fw8TeGbe303xAplnu\r\n" +
            " CwMWd2M8hR1/Go01C48X6DqOmPYLG93AhCdGjA54A/Ac1Ho/wbv9B0DQYtD1S80y9md5LqGIFp\r\n" +
            " 5BkYAA6DFVYVzo5/H+heEfFfiLRdUt4ZtjJDcXKM0jSYGNzlumD2Na3xK+J3hz4VeJ/CWn/Dm3\r\n" +
            " h1/Ub6ATAW6iYwMxwVDL1b+VcfJ4S0W7svF2m2GoxHUtSaEyRzfKUYdQ7dyT+Vd3N4D0T4HJ4Y\r\n" +
            " vPiFNaC8tFEltHCd0gVhxnb2zg0/UyGf8Jf4E8IfDrXfF9okk+oi7Ed3Yg7ZYCSfXOBnv2rV+F\r\n" +
            " fxS0/47vLe2MsGiWNnamRo7kDzMDqwIxk9u9c18O/g6fF//CVz3SxTaDciSRpokGwbvmDv2Azj\r\n" +
            " j2qx4/8AhuZ/ib4CNo9vdWctj9lZomC27FW6AJ9eR71oopoYvhCx8P8Ahe0utT+FHiNdQ1SW48\r\n" +
            " 6Oykyu0E4IKZyev1qP4oa58S7r4n2V54btbqCKO2WaUbCLfaCdylW/izz61k33wUP7NnhyHxXp\r\n" +
            " djLLqreIX83zZ2aOG1LEKu0eo79q1Lj4w+OdO+KOuajr9te3Xg+CNJpGKF4IY3wFO4nI4JBIo5\r\n" +
            " UI7vw/LpV/4Zh1vxN4hTSLhz8nkvmISE8+Yo5jIP4cmvUvCH7S1t8I4tPtdfv7nMzbJLl5d8Mm\r\n" +
            " fccY6cj15rzrVvDvw+1Hwloul+KPEVosl+pnTZ80bFiNoZwOoz6VR1fxRpfwx8B6rpOraKusQa\r\n" +
            " Xn7NcyHEU4IyACPu8EjI4zioZV2j9AfhR+0zonjaxURXsUjjGCGBx6/UV6nbXSXkavAwYP6dq/\r\n" +
            " LrwN8SNnwxl134ZaDcXGnEJKyq+LnTpDjehI4YDOc9+9fUX7O/7ST3draDWJZSUVQxckjnHDe4\r\n" +
            " rKUSlJn1DcKVbLUwgjsTmm2N7HrNlHPZsHSQA5qbyyvXNS9jVakTY5HQj3pqpvOMinyD5sHpTc\r\n" +
            " lG+UdqSRLbTGhQetNZVZiBmnkbh8oNNcb/vVJTVxiuR8oHAocleWBGe1SL8vTH5UjpuGXzQCVh\r\n" +
            " YjtBzUlRqu1AIycU5XwfmoGIRtPPvTUjZE+f8AnS7t4BpwcOcYH4CncDyn9mp9v7OXw/H/AFLW\r\n" +
            " nf8ApLHXaNLggEda4z9mjDfs5fD/ANf+Eb07/wBJY67ZocnkLx60MSEKEnK96DCvQED8adjZyD\r\n" +
            " z79KXO0HIGfakMaisE+bpT41KvknjFImGb5hUjDEZA6daaVwFjBzj9akVCH+QHmo4NwQZqdWwe\r\n" +
            " TRYB+zb9wZ9asWwMqbdpqKBd54P/ANerMQEMW5yAKGrANubyHTrYyTthUGee9fCv7f37WUerX2\r\n" +
            " oeFtHvBHZQKBPMXwuf7v8AnrXrP7en7TyfCvwxJpehun2+5UnzN/8Aqxjrivyb8S+LfEPxK1LV\r\n" +
            " pdaMcVg1wcyS5V2XuwJ+8T2oiuYnyLepeO/+Ej1PV5o7SJI0UW8My8sx7MD2+grlfDz+GNIvdQ\r\n" +
            " uNeuLq9vkO+WMDbAX6YB68etR23xQtrxW0zw3Z/ZMN9nhmk6Njrn3NbvhvwBoumaTd6d4jv7S5\r\n" +
            " 1a4kF1cbQSbdM5A47+1awstGS9DL1g6j4v8AidpcunWb2Wn/AGYLGoJKhgP4R2J613fw71bTvC\r\n" +
            " cg8P8AjGcpql2GuFYx42j+EFj94n0HFcvB8T7nwz8Tf7HPkz6atq0kbQxbpFymQC3Y+9V/iDqF\r\n" +
            " 18UNX8C38GnTWzWs6WUap873AL53SHqDnt6VpFJuzIlsei+P/Fmn6T4dtrnQNRkGqz3QS7VRzB\r\n" +
            " AByzHsc9B7V2Hg743j4XeK/Dlz9kfVZ9Tbdp00zlV4HDMf4uoOPavGviL4Yg8KeJvGNtfPLcAS\r\n" +
            " FS+NsiTEZ2Mo7dq7Txfo58Z+NvA2g6MftMeg6XHLIIcr9muWGQFI4OOO9DSi9CUr6mf/AMIzez\r\n" +
            " f8LN8RwnN3pl15qybMRLPI4ORH/Fxmut0yz1D4t+PPA138RLi7v/7TtzHCIVCxyOg+6MdAOOK7\r\n" +
            " DxVp158Gf2WdYOs/2cus+P8AUUie4ZsyFFPPljGAcck9Md+a6D9nArY6xa61oVzFeSeFNKkaC3\r\n" +
            " nT/VTMP9eo9Md8VXOJI4yK41Q/B74k+GbO/Wzjt5gJB5G2WIBiAjt1xisP4D3118Evilp2heMb\r\n" +
            " xru2azW7iEsO9EDrncoY9RjHvzXf/DDT5fiVazf2JO15Jreu28mssqAmZPM3lCDzywHpkVp/Gw\r\n" +
            " Wfj39umZZ7K2aLQLaDA3qi28agnyWA7sAcDqOPWqi+a4GF4E+OGu6p8Xr7TPjS0UXg/VLxBY3E\r\n" +
            " 6ApDCW+XdjoTnPpxXf8Aiv4heEfGfjvWvBcNvcW9s0SW8MzSKDJGfusR0Knsa8o+I/wiufCP7M\r\n" +
            " Vnea+1vb6/4414Xum6aJDLcx2okxGJWOPLBA47dqb8RvhVqi6tfeJ4SUuNC0G01GcWzbvLOdpS\r\n" +
            " QD/dPpT31A9A179nzw02jQ3Xj3T76fTvDmmMtvDaTGN2kVjtJPfgk59hXH6Z+0LffDv4EQ3l9o\r\n" +
            " VjqekjVTaxwXY3yMpG5QZfXaDjHpVv4oftaeLLfVPh9Y+HLTT4tPu9OLXcQYyR34YlCjlh947S\r\n" +
            " R9RXqOgeE9F+Gel6BDrF5pmnXGo5vLbT72EOiPKNq5D5AK5wCfWpl2BHn+t6jrEHhfV9f+Ek15\r\n" +
            " DoeovBOtvDEA8EzDa6kAds/Qiup+EPjPXvEuhSTrBdNf6ZDt1CFE2PLg5WUD1x2riPFvifxr8G\r\n" +
            " /DkemaZdRx2194hkM7xwDbbA42hh3Tgj0Ga9A17wNqjSTeJvC2qvHcmzCyxWXO58Z3Y9wPu9qh\r\n" +
            " qwz6g/ZR/aZj1OyGn6oZ0RVztmUq684xz719JWF+mpwrJbtujcZFfml8Avi7rviDT5o9Uswmt2\r\n" +
            " o84yNCE+1Jn7p/uuR1H419tfs6fG628aeHrSGfIl2hASMMeO47Ecg+4rKUV0NYPoeqTL8+KjZS\r\n" +
            " DwM1PKvGf1phPyNj8KjlNCNhxkdRUSsBznmpdvpimN98BM4NHKA0yAtkilSZcEc++aJLcgZycU\r\n" +
            " ohCJljzU7AL5mDjjH5Un3jzTQgfrk0uDjCLTTsAcYG0daer7ielNg+ZcHr0pM46UPUDyn9mw+X\r\n" +
            " +zp8P8n/mW9O/9JY67gthcjmuJ/ZmVX/Zx8AK+efDenf8ApLHXbSoRjHShiQ4qSvPelzvfHtTW\r\n" +
            " 5XqcU8pg/LSGCdaeACw4qNRhsGp4wDnYMUXsAgUlsITirEScZ644qOOP5snjFWrdFX75ouA+GI\r\n" +
            " 5yBXI/H/4p2nwi+F+p6vfyiP7NEWRf4nboAPxrtRtWNnY42ivz8/4KU/tGx+O/Eg8H6JtW201h\r\n" +
            " JczkkfvB0XjoByTUzkxHyv8AF/46at8Y/Her3niaZzBbjfNMxyBnpEgHQgV5NrurJ4yjF7onm7\r\n" +
            " EQwkZwvoATWD4k1C98V/E68tPDt3LFpNnEygRjCsx6lvUmr8/iq2+Gl/pmjTRCOzeEu8gG4tJj\r\n" +
            " PPetorsRe2ovhTUPC3hrz7a+hS4v9OQ3AaXlEk9dvt0rlZ4LXX/HmsanYtJEk9uJZPmOQoI3nH\r\n" +
            " qe1VX8KXXim6v9Uu41CahatFCiOG3MCCOOxrpfA5l0+5tJvEEMBuEspFk+TDKoHRh3PStrpWsJ\r\n" +
            " yvqReGJdI1PxHpV9o91PbQmKSOSOXIYIAfmGffj8a7f4I/aNI8R6Lc3EzO0ckl2FJ3j5eUY/oK\r\n" +
            " 5Xwvpek2vhyxtBJM+rSxyXCvIR5cAYn5cfh0rvvhxr8t54fL6BHEl5aM0SFsBWZeSFPXJ4wKhh\r\n" +
            " a5q6l4m0dtUnm1y3tW1HW5Sbp5025ck4BDd8dO9M0nxfqHgb4iI1rcRT3N4Q0CKQfs6L/eXkZA\r\n" +
            " HGPWoL691H4s+I4rjx3Yxt/YNs8siyRjdM45JZx1AGML61YvfGWmeOfG/h/SvB+nG3vltWa8ZI\r\n" +
            " WFxsA+4AR34/KhCuj2XVfiTovxI8P3mifEnRLq7v7FzK9uigCzXbuGwMc5PHIxUPhPw5L8Sfir\r\n" +
            " Y6v8P9Sg0VmtorH7OR5kJSMY2t6E4Gc8Vn6n4M0OL4TXWtaZbXeu6rrY8ptQi3m500FcbjwMjA\r\n" +
            " 24NXfhd8OtR8WfDm3s/hJawNqWiRTXlzHcOP+Jhz90K2OMAj6nrVxt1E0M8K3eteBfG/irxbqJ\r\n" +
            " tbGw0m5U3elacwikuHVwoSJQCM5yTxj6VsftDeC/DHiDwrqHjrwLZ3mi+KPEsIbTrQzh7d7uNl\r\n" +
            " 3ySE42MUPA6Z6V5T8F9au9G122uvidpt/Y2viy9uEjtrVlcmSMfMhWQ8DnAJPtXsHjvSdJ+Nfx\r\n" +
            " M8M+HV1qfwdaaJZi7utLmjVFjAJHmk4G5sAgY/vVoklsQcpffDjxD8SvDfh/xV47isdG13w3bq\r\n" +
            " J2ujsg1ONTmMo6nYGHzHGOccV7B4Ku4fEvw58UWmlT2WoXfioRrdmFC3lBE2jb/skc88fM1clF\r\n" +
            " 8TtXv/ANmvxLovizStOewtLs2wgumZbt7YsBFOoY4ZTxwoBGawPhVdR/Cr4z+HJPhOuoWXhi/0\r\n" +
            " t7TU7+53SRG4kztXnJhA6Bh7UwOxPwE0fUPgnq0etT2unxeHdNuIoklJ3tNMV2RqSRwmCQc5+a\r\n" +
            " vnv4k/AnWvEPwt8H67opv1i0a0kNxI7SNLKFcsW+bOduBj2FexfDOS88X/ABp1+21DU0OgeJ4G\r\n" +
            " sLxPO8xIbiHISUr6gj7wxXeSLaXPwT8Q3dzqcmpQ29m2mKLRiYrZskOGyMgnA5HFIDjfAvxE8B\r\n" +
            " eNvhjp6ePvFSvqF2gillZd0cpI2iR8nKkdx2rO0vxN4h+CGs6wXjOo2elRhxiAzC+3AiNlKkbv\r\n" +
            " kI5/GuK+OP7OOoal4N0M+Dg0Gn+F9EfWdQKwhT5xYYRjx/Cob8fevR/h9+2J4GTwb4Uj8QTX8F\r\n" +
            " 5cqh+0opBjkT5QPLYAnaT2NDQFHS/FN78TPh9P4806ebSbm3ilhvbeAE+dtbMbFf7+Mqfda9W/\r\n" +
            " Zi+M9xfeFNI1fUUWC+uJVt7mVfli88EgD0BYAHHrmvILXw341/Z88YeJdYtdW0zUfC2rXCXDwS\r\n" +
            " xbmLv0ccfLnPJ/St++8dXWgeGLXxHo+xvDOsXEbXek7AvlTlsO6N+JfHqBUtRKTsz9NPAWujxF\r\n" +
            " 4Ytrg4cOvLdRmtMjGRjGa+ev2a/jNbWU50cylrbKrAWb5lLDdzn1HNfQ0ci3CB42BUjOfWuaV0\r\n" +
            " bcyGOvzZpjLk59KmaIbuSfwqJsc7TwPUc0kyhhkAXGMZp4755zTByeO1NMhDY5/Ok9wHOAMcfl\r\n" +
            " RJIYwAAaX7pxJSscpStcBsbBRkHmkpBhlG3pmnkY6Ln8aAPKv2aEH/DOXw/IHP8Awjenf+ksdd\r\n" +
            " v2xXEfs0/8m2/D/wD7FvTv/SWOu28o+o6ZpsEKFDcc1J/npUag7cr2p6ZJXPXNIByxhm4b5qkg\r\n" +
            " X5iMimKNp571NGgz7igiMuYkWHrk5+gqzF85xjoOKhhXcSQRxVooI1LHAwMk0Fnnn7TPxwtPgf\r\n" +
            " 4BlvNT+Z5lZY0BwzNjsK/Fj9qn42ajqXjS3Gju1udcuHnuX+88jMcKpHpX2t/wVN/aL0+bxHHp\r\n" +
            " Gl3DXEulwySTbTnZjk/n0r89NV8YXCW954nk06JpZFQWUsh3bQfvEA8ZA4pQ97oK9tCTVfEFx8\r\n" +
            " HvDFlEscM17qE2+6m2/OwPoPQetcpr8y+O/FDahE6LHZusio5Id+OSPUUsniO4+JfiqDUL8qlt\r\n" +
            " HEoUk8gZ5xXdeGvC8ms6/cRWMdsYFRXKug2FBzu3duK6V7urIeqsVfh74Fj03THn1CfElxcEwR\r\n" +
            " qMliechfYV1MY0wm4uruNrtLOERMsQ+aQ/3DmuNhtIbbW7jVG1Az2tjKTbbQVLOxxsUV2en/EW\r\n" +
            " z8PeFf8Aib2lvNeSXO1IQuDHn+Jsd+aljdkYvhybTbCzu9SutPV5muFt7S03F/KH8R9c5Iro/B\r\n" +
            " PiCTwDpV3Pq+mvc30V2zx2uzBjQ9NuRzk8kiqXgqzu/D9vq2ubYLq8ubgPBaGQlETIyqg8knr+\r\n" +
            " FdDc6jcwXmt6/rsgS+ljVkt7hfJMaqM+WgOfvEZz9KokxvDet6h4H0TWNY1KaSDVJJjNDaMnBQ\r\n" +
            " n7oUn3PHeugufiHD4r8faRq9hLaaRrF9AkM0NvbmAzoAckBuhx6Hmsq51uf4q/FvRdX8QWdzp1\r\n" +
            " veW6SxxMgAVQyoXx3wM9cnvXfeMPFOlvpWpaT4sg057zwxqTJaXLR4mmjH3URwe4OcUbGctzr/\r\n" +
            " B3jvQ/C3wp16LUbu7S9uD5Ajgi3ZK8gPnhR2/Gum+Hmn3/AMRviloepfDaVtMOh2EdtLEfmjkA\r\n" +
            " GWaRsjGc5z2xXjWm/BGLxd8LvHnim01u+guLGzS7a0V9qszOByDwQEzx14r0bwHpVvr3w18K6l\r\n" +
            " 8PEW+13w+kl1cWYfB1aIYZ1Y9CQvTg9MVUVcLmV4X0zUIvj/4ib4iJHpNimqmK1ijAnjdiCwMZ\r\n" +
            " JwA3Xg9zXf8AxZ+GVv8AED42z6n4g1i506Oy0IXBiyqTz7UJSJDwHBYDr2ry7QLvTvjZ8S7i/w\r\n" +
            " DCcuoaHBfXxu7W2ml8v+zxEuXDDpyARzxjivRvFC3XxTnm1GRzYw+GbZ0YKdpu4l5wSfuE4OF9\r\n" +
            " +BWiVhJHP3/iu2+P/wAC/CGpfFi3j0pfDWqC3vWto3jliB4wWydwIAIHXINd34G8W6nqnww8eL\r\n" +
            " e2k8Hgu0ma30+LYC727cbvN4O7I3Bs5GcV5r8a4z8XvB3gpZbabQbKLUWiluLCQiaZFGSl3Dz8\r\n" +
            " 643K/Qhu3Sum+G3xKh0/4Oap4c+IUjafo1xcm3stO2rDPeRFyFuEkPJPyjIzjNMDV+AKR+HPFU\r\n" +
            " 9z4N0Xf4fS1DmW+LyieXksfMBIU4HA9an8E+LxY/Gy40n4X6HJN4c8Q6a91PaT3REN9KXJLRLy\r\n" +
            " AyBgdnGcZrQ+FfiPUdJuPG+jTaauj+DY7T/iWM6CSS5XYQx+0D+PIDAH0xVT4a+KJbOw17Q4LM\r\n" +
            " WlrYJHeabrl4R56g4Hmoqjb8pJBXOSKBlOx+J0fxZ+Dnje0uftul6ppzIkzTEeTdQBwhVHAByM\r\n" +
            " jgj6Hjjyb45/Cf8Atm/8P+Jry2vI/DPhi1tUnHyr5TyMdzoP4jn58fWvYrHXNL8KWfinw9pN3Y\r\n" +
            " X2s22led58Fq4t5Pld9jMDjeV3MByDWb8SZ2+Lf7MWgHw7bypLqN3DbXqBWeIsgwNvsVU8D1NA\r\n" +
            " Ha6N8avBmqeDrC102/hmN1aG3F55Ynt7nOVBlQ8oQG59OteYeA9fh8BXh+H/AMWbe2NpfXTyaX\r\n" +
            " MBv2sWKoV9R04Getcd+0h8JtP8DXFp4sFxJoWk6dqFvbwWlrDujuWZQJnZQeNrbgD36dq9U8Qa\r\n" +
            " 54O+LraW3gpBdar4dhW9slEy4vsKPNRCRkOQpwp6EUnd6MLnX/B/xpqF34zmtfEsdppmraQsNu\r\n" +
            " YI3wrRKxVJBntnjPvX3l8K/FS+KPDFsXAEsKhGKnIOK/PbwzqOjeJviXqF7qQuLC/s7H7PPbFM\r\n" +
            " F4DwrKexBByOecGvpn9ij4iT6P4aksPFhPmKzeTKxIEsOSY3+pQqce9YzjbUaZ9LP8oPscVC4y\r\n" +
            " TUttcJc26PC25TyCO9I42gk1zmsZXIlQ/w/rSEY64yKc8uBj1puNyfLkn+lBY1m3HJpJZdqcda\r\n" +
            " ERgwJ6UrfM3QcVUQBIxz15NKJPWhWwPrR5w96T3A8q/ZoI/4Zw+H4P8A0Lenf+ksddux7MRkcV\r\n" +
            " xH7NLqP2cfh/8AL/zLenf+ksddxwP4Rz7UMEPQBUUjilJJbJGMU0HegUZ4qRhvI7dqQhyjLDNS\r\n" +
            " qMScdKjC4YVYVNwoBJbonthtkBU9K4/9of4mWvw1+Geo31zcxQ3BgcRBj95scV2tmpCk8Yxnmv\r\n" +
            " g3/gpR8c5da8RxeGNIljFpCrNclWzlumPyyaTl0sC1PjD4qXw8Y+I7y81N2v7nUGPmhiMrECSc\r\n" +
            " H3xXz58RviPJ4+0qCxsIFs7SK88goF4iVRgDA7V67qHiu0gtrm7smDCNjbRnrgd2/KvEfD1s6a\r\n" +
            " nrF0QzWUUoliZ1wZDn7wJ7GtaUOX3glHax13grTV8Oxae4gilt496TqoyGwMgD0q14W8R3PjGa\r\n" +
            " 6j0ppLe8vd8HkIMRoo6YOMVZ1C7bRX0eGKImO+cTXA3DhSOhA61F4T1Iax41mxb3Fo9tOY7eOO\r\n" +
            " PbGFIxkkdT7Vbd9RW0K+i2Nl4T0vU7TxOZL6DTVEg8px88pOAmR0564rW8P+K7HQPB2oa5d2kV\r\n" +
            " 5O10gCqfMS2QgAAZ7ngn0q34Xn034e/2vH4vC6hAkyrscj5XdujewrV8Daf4fv8AxZqNle6fDa\r\n" +
            " abCv2hEBLKz55bA46DikiXpuQ/CLxNLPrGtSpd21zMYxLbwsCiQ5GSAT3OMVsan4k1T4u/EbRb\r\n" +
            " HxTY/ZbLS7RjFb7F/wBIk9Xbqw478VBb+D30z4p3VxLDImg3WxopHwu5wOGPovSvULvwymo69Y\r\n" +
            " weJdOt0lEYcuyh0mhJ3Icqf096JNKPM3ZE06c6slGKuziv2j/EY1afwjq/wjsri0u7e1XTtUi8\r\n" +
            " rBYBuXz0UehHNafh74TL4p0giW6insJphM63JLGSUdlfGc5PNdt4L/Zjv/G+t3VxfRPY2UsuY4\r\n" +
            " HlLFV6Z9s9favbvAH7JENmkTajcSyrENioTwPcj1ryq+cUqT01PqMFwpiMRrVdjxAa/efD3RDo\r\n" +
            " brbacskTRvHFtuFuFf8AibPBYehHHao/hjolz8LEtb6z1iLUoLY7FjELJdIjHJPHBGTyB+tfX2\r\n" +
            " gfsq6AHDyw7io+YEZ5rufDn7N/hzTs79OhO7ABK+tcSz1zfurQ9uPBlJJXk7nwLrOu+EPA3iwa\r\n" +
            " pB9qmM0jTiGImVrV3+8cFRgd9pBrrfh/faRZ+I21XxHremTeGNSu4zdWcv7g27noWUjkE4yeOf\r\n" +
            " wr7L1T9i3wbr3iGG4utPtwT98BAM+maoeLP+CZPgfxTem505Z7OfZldj4DAdmB4P4100s67owr\r\n" +
            " 8FqzcJs+P/i/8Dr/AMCfE+81nQdUaTQ9UQTeHRbyLLp9wwOTbyPksjBc9jnjBo+O8V58WNa8H6\r\n" +
            " F4t0SztrW3tGvYWspdk0Uu3DICRkkkA4zgnkelegfFr/glr4ustQuI/Cmp3+peH2lFzFZx3Zhk\r\n" +
            " tpEGQ8eBtH5c15H4u+Jni74U+F7zQfi5oDyeINCkS60Oe9QRzSMrAAeaMAgrkEEV6NHMIVWrHz\r\n" +
            " 2M4exGFu1qkR+HfGninWfgrqWg+ONQisbK9v8A7BNp1xZm31GwKkGKUScZVgOQcZ+hrpPgv8TX\r\n" +
            " 8Pya94Tl0q5u9OjtxC2oXJCSQkjiXbyPLz3BxWD4x8S+Dv2o7218YzQ6jo3iKKJLfVtKFztAcD\r\n" +
            " akq56gYwevFdhqevX2ueGdB8M+J49P0+/WGS1tJRL5Juoj1jl3dxwR+legpJnhOLi7S3Oo+Ffj\r\n" +
            " /wAOaHqfiLStRkj1TxaunRKt1aRCSFoSpwkhHAOd2MjNVfhr4J1H/hDvFuqaPqN/JpOoOb4WbN\r\n" +
            " 8tvcDH3Ik4RMgcjGCenOay/Aev2HgPwp4ut1tEu9QsY2EGp2kG6CXYoJj87qxPYH0OKf4P+HyX\r\n" +
            " /hSb4j3GsajD5zIUhtp8gqflbKdGRh8vIHPJqZX6CNv4g+EbT4waJ4QuPElotkBKZHhJ3wzBMl\r\n" +
            " s57YJ4P92vK7PwVp/gT9oi18Y/Ea7XR11yE3+l6bHA4JiRQgYqB+7JAVueCD2r2XRPEsWt+GtC\r\n" +
            " m1TQLzTYbS4cLLNIR5iE4EhyAFwRyDxg1hfHXwreaD8SdS8W60kd41jp4sLCKOXct758ZTYoPA\r\n" +
            " 52Y54wMU3e2gBoaeGdO1y78RzamDb+IIZIknZQSZMqTE/ptIAGO3Ndh8FbzWPB/hTTLXWSTPps\r\n" +
            " 00SS7ifNjLFowfYLkZNeWfAnwGmv/s66haa5H5bXF4BH5jbnVl/1u0Ho64OQcZxXaR65f+BdV8\r\n" +
            " L2OqNeT6Tf26wrI4G8kYBc+wIz75rN32YH3v8ABPxBD4k8EQzW8omx/EH3fn+v5V1brlTxxXzz\r\n" +
            " +x54yis9Wv8ASQ0f2Oc+baMO395T75zX0O3+rz6VjJGsFrcryIvc4pqkeYAOh/WnzRh2GG6+1M\r\n" +
            " MTRNwDzUGgoPJzik8vnORQxCMGGcj8qN+45OOaAGsNmCfuioiNvep3QuvFN8r92ARnvTA8r/Zn\r\n" +
            " Uf8ADOHw/Pf/AIRvTv8A0ljruM5PzfhXEfs0c/s4/D/H/Qt6d/6Sx124j+Ulx06UMSE/Qj9ani\r\n" +
            " BCfNnj3pkKZOX7frUh+YkYxmkDJUAZVz1zVmKPrt/Kq0S7R/u1bgyrDGcGgForFXxXrcfhjwtf\r\n" +
            " X12dsdrC0jHPoK/G39pHxzd/EP4larq0zgRzCVY4ySCWbIH6V+jn/BSD4vt8NfgZNBYN+/1MmE\r\n" +
            " gNghcHPHpX5Cy+KtU8S+I9Lv8AVA4t0glnljU+h+U+46VC1lYfQ5Txwup6KdK0zRlcQW6+Xcvg\r\n" +
            " Nv3nnP4U1b5fC/gDVMRtNe25WBFcFkVSfvn8K2PBmtL4+vb5NQjCuzbcBTgnPGD7UzxR4lHhG6\r\n" +
            " 1HTjb200jR7YE25ZnX+Jq6L2RCZStI57/R5fEFzfHy54kt44wgChh0xnn8q173UtQhm04+HYT5\r\n" +
            " MQAM23l5W6nHfrWV/wAI+PEvw/kS+a4t/ssf2hxIdp3HuMfpipv+EiePSNEguIrhIbQA+Zg7pH\r\n" +
            " HqOtPdCvfQovoc+i6zdaF4oZkGszjdcSLlUJOTuPb/AOtXsuh/CSPwNeA6VdPdvLGqvcBC0OAO\r\n" +
            " gJ+orL8MCHVdQNxqlvLqtxdzqsO+PKW6kffOfp+Fek+C/BFx4j1qR5TPHpcZ2xRbjtmbGC5/L9\r\n" +
            " K5cTioYWHNI9HLssq5hVjTplj4Z/DzUPFk8tzLB5lqreXEl0okBAHLBh6nkV7n4B+CGn6XcJM1\r\n" +
            " vlgAeTuwfx7Vo+BdBTS9ISK0EaIigEL2r0DwtDbOo89Sje5618djMyq4vrofruV5Hh8tivdTl3\r\n" +
            " Lvh3w/DDABH0AHbn/OK7TQNNBgHyYUHO0Mct71T0qzEsaiRVwBkcY7V13hWxTIVQVZec7TXmK+\r\n" +
            " 1z2Gk9bFjQrBjkRR7GZvvZ4P0rqtF00S7lfOeuD0pNA0ZbhDlAWGMjPXPpXW2WhiCNUb5d/QD+\r\n" +
            " Ku2jBKJzVWoJHOSbLRHkcAbT8oP8RxxU1tqj70MLqW29CevPfFM8fwrp1yEwVVVHHUZrJ07UE8\r\n" +
            " 1d5UOUOPpmqejNaEVNM9P0oefbQ+YqsQOT1Fc18Wv2YvB/7QHh5rLxlY285OGV3UB1YdSG7Yq1\r\n" +
            " 4fvjdIh3qPXuyj0rrdLhTzCJATkZDZ4rSFaUHeLOSvh43sfl9+1d/wR513wjbavqvwbll1nTrv\r\n" +
            " AezjlKXcQBzujf29P0rzDwd8bH02ztdB/aD06a08UeEZUSC6uIfLkvbM/KyTqeGYDGGA/h96/Z\r\n" +
            " ifQ1upDtV2UDcEXjivnP8AbZ/4J96J+0tolxqmnWqWXieyiZbe6jGDKMfcfjoRXr4bNZU2lNHy\r\n" +
            " Wb8OwxV6kfiPir4m+FbHSlsrDwc0Q8M65L9qeSOfy0GT1z2wWIx71Th+G+kfBW6h0HRIv7Rupo\r\n" +
            " GuhafbTEl/A+WC5zjepHUdc15x8K9d1bwp8RLTwT8ZdITUbDSNW+wSW1zJ5b20m1vKIycgPt4Y\r\n" +
            " cZAB616N8RPBF34S8VXOq6Tp1zqEum3inSbxJ9qNAyhhDcIR8pByNwOBivp6E1UjzJn5tiKE8N\r\n" +
            " UdOa1R0dr4l1hPh39l+MFumkaRdtLHHKbxpHsYnLJmcuTgjA5X5SCDT9C0eL4K+NNH8GaEbrxR\r\n" +
            " p+q6ZPdQam7+aNOc5VdjnIKnn8utYGpfFDQPjn4p03Rvj1PpOnwXQEsaxFpFKkBQhJPdgcg8cH\r\n" +
            " 2rXv8AxTH4H8f2MXhnw9eatplnAlrPbwy7YbWIMdzQyYyx+ZiVOTgdhWxicPrngTV/Cmp+GNM8\r\n" +
            " Oi8gmvNQN/eNH/qZVLK20AfKrfM24+hFdtBcJoXxUuvC/i1b+5s9IRdTsrh03G1cOC8e7qV+bg\r\n" +
            " Z6VtfFjwRc+IfBVpqnw21S70O/8P3UlxDPPyiqADtx3U4C81ynwp+IzT+EIdb+L8C2d/q8k1lb\r\n" +
            " OrAnYT8jMf4tuf8AOKynuB6t4P8AE8ngMpqvhva8H2triOND85V+T9eQQfavtLwR4oj8Z+GLW/\r\n" +
            " tB8k8atjPQkV8I6d4fksX0q3jng/c72m2NkICM5x2B5/WvoH9jT4nyava32iX6zxXGmnG5lxG6\r\n" +
            " ZwCjHqMCsZrQtSaPfwpEm0DjNI4CqS3IPH0pY/3keQ+ffOaJOevQdqzNIu6IgqsflJFBX5fk5x\r\n" +
            " Qy5YFeMUuefl4FXylDV9W60/POc49sU1lzilJy2TxU21sB5V+zOcfs5/D8t/0LenZH/brHXc5D\r\n" +
            " D5c4rhf2Zvn/AGcfABP/AELenD/yVjrusLGPmbGTgZoYkOTjoT9D0p2eeTTYsE4Y/NmnJ8zEHt\r\n" +
            " SGTR8x8d6u2zAEbs+1VoFAjo1rVY9E0a6upV+W2jLn3wM0mxH5vf8ABYD42W2o/Fux8LJP5v7n\r\n" +
            " yRH/AAoTyx46kgV8N+PvHA0vwpdXVjBHEXlFkrHrEnTpXqv7X/xEsvHX7R+oa/4imEIt7l47eP\r\n" +
            " BLs59/QCvGvHNzBrPiiz0O3gd1gIunL/8ALXI/WnCFlditzFn4e3Y8GeG0vb8faLi4uxHESCox\r\n" +
            " 3b369Kz5tJfR/Hq674muGmjubpzbQY+YIO+PQmuu8ZiDTfh3IHVI5omVYtwGY2IrkZNEmksdP1\r\n" +
            " PxDOCbJVjcBt2/J4C1sgSsSaD53xC1C50+7e6tJbqYuEYbUJU5GPaul8GDUdO8W/YNR0+O/sLX\r\n" +
            " MxlkIKkj1PYVSill8T+Mk1DToZYltYsWiq23d2wR3J5qn4u8daxoFqmjXsMiNcziNwGDTSZPC+\r\n" +
            " o60m+VXHTg5Ssup3/gy9ufi58RpLLSn221tOHneNcLGo4EanvX1Z4R8NLpdoisAqooAXp/OvL/\r\n" +
            " ANmv4RxfCjwXb3WtRlru7/ezELlmJ5/SvS7vxc9xcnyYGitl53bSMgdeT0r4jMsVLFVLR2P2LI\r\n" +
            " 8uhltBXXvPc9D0KCK0tgsbbgOqjnOfrXaeH9NE+1kHmYGBu/wrw9vi3Z6DEsmozDdnhN2cD1OB\r\n" +
            " /Wuj0/8Aa98PaMEGoXUdqqAfLkFn/LpXEqMmttD0pYuMdEz6A0qzN9ZeX/q33AE/d3H2rvfDsP\r\n" +
            " 8AZ8ZjLqFI+9zgV88eFP2x/CniTMNlIzMCCc4Un3BPWvUfB/xnsPEUix2ZykgDDce359aORw0a\r\n" +
            " KjiZTVj2DwnKV1VlbAXjPoa6xb1JbuUsQUWMKo7g15XZ+NLaeUizbA4346jnHStEeKnt7mIRHm\r\n" +
            " Q4JB4A5rdVYpEzUqjNj4lqJNrvy2ASR1H1rm9AtvtlzKwBZ4xknkbj/Wrfi/XfMsY3lHQY3kZB\r\n" +
            " 5rP0G+BtmfdtLD5cEgEUm03c0pOUEzvPCAiglg8x8GTPBAwOK9F0+SKZspKJUCgEKM7e+TivFd\r\n" +
            " D1g3kyPbSApGwGOmD0rufCOsvbzKZn8ssMYzxj1PtVRsyalrXvqelWmjSXmx40Cng5OQNtbtvo\r\n" +
            " HmwsGjC+hPVvxFc/ZeO4baOEF0lAbYdp45HBzXZ6dqMF9D5ouY/NUYCg8DngVvyI8qtVkz8vf+\r\n" +
            " C7H7Gyr4Ws/iT8NrVbLXNMmQ3k0CY82PdwzgdSD3r5z0/9oW91C3svC/xH8yO38W6Usa6nHHlY\r\n" +
            " 514ViQeSD1HBr9nfj14A0f4s+A9S0DxXBDPa6hbNDKjDIIYev+elfhh/wUC+Dmu/sefEDR/Dep\r\n" +
            " ulzpl3Kz6TqM5KqFBOIpMfxDgBvpXsZVive9nNnyef5f7Wn7WK1O/+HEPhDS9Eu9L8SX2na9qM\r\n" +
            " UjQMq24Vgp9JMZyCq+44ra8NeMtd8NazqdxZeHlh8FeZ9mikE0ruJirP5yqWKMpIKk9ucVhfAv\r\n" +
            " xlH8f/AIcaV4m0fTdKbxZolpJZatZyRfLdEEFZSVxgkAAnBHFT6t4l1rTtNlk8beIbCXRL140m\r\n" +
            " 06EkWmlSF1K+X3AQB8kd89q+jhrqfBv3XynT+JG1vTWl8S+JfEdlH4V12BdOu7MxJIse8lRK5H\r\n" +
            " cFgOMcViTX+m+IvDmi+AfGekjT78IRaXkT/LcTxLncntIgzg9+lWfin4K8N6R4TZtOGr6tpclt\r\n" +
            " bSyPppWXznBzvEWfnyW5AroPCmhaD8QIdC8VW2o2f2TS4QtnNKhiZZAChDxk/IwJAIz0pS3EjI\r\n" +
            " 1mKXRvjLY2thvFnq2mGCVTnDOF4YHsfkHHrXqXwA1y68NSaODci4NzGUmcMCeTnDY4yBkVwPxA\r\n" +
            " 1q6+HuqafrFzHGbWJHhuiwDGIsBsdPowz3yDW58PNXitfFPhy90om3h1RD+5O0Asdp6evLEfU1\r\n" +
            " Eilufc3hh2m0iF/LZY2G5ee39avuCFJUjaeuetUfCCSReGLNbjkiMDP4VdI556Vi9zcYV29CTn\r\n" +
            " 1pu3nI60o5Y/45pN3I6GkAJk8Pjnv6UB8Hg0p+Vc/hSbUz94nNWgPK/2Yyx/Zy8AZ6nw3p3/AK\r\n" +
            " Sx13OMjkVwv7Mwz+zl8P8AP/Qt6d/6Sx13RwFJ6VLEGPSpIAS+PXvTYn544OKni3EHgYppXGSx\r\n" +
            " qWQfMQK88/au8STeHfgxqc9nKI5EjLBumMetejwDYfkHB4rxT/goFfQ2f7POqpKds0y4jPf8KU\r\n" +
            " 0krglc/Hz4o6bDr3i3zruaOQQXD319IpyYl649ifes7w0Wn8UXvizUAscEsBi0+3YA+YAODjvW\r\n" +
            " DdRCzt/E3264mLa1eNHbrnLsoPzHHp2rqbDTLnSZ9P1G/e3Gl2VmI0jI+eP1wOxNaJWQLRmDYT\r\n" +
            " nxhKbTWbg7rw+dKJMAIRzt6da5zSNQj8V6pcaSXkS388iJslQGHHX/AArem1Oe9TUNYu7AQW8M\r\n" +
            " pWCRmUIF9AvUsR3rnfEev29/a6ddeG4ZoQoyUiQkhs9x696aBpHUeFzqej394lj81tYxkCaVsY\r\n" +
            " wf1FJ8AfD0vxN/aKt/t7C5itmFzKW52nstZfjSe78OaEfNie0S72zOHOZNuOSwPTPpXsP/AAT8\r\n" +
            " 8En7Zfa7sH/EwfMbYzhR0rhzCt7KjJ9z2+H8G8Xi4rotT6Zurae6+S3jOxcAAJjA9KzfEWkalf\r\n" +
            " WbW1rKyhugKnI+nXmux1UtFDuZlOBjYFAyfrXCfEH4u6d8LtPW414JJM2TFCDlyfoOn418XS55\r\n" +
            " uyWp+vSjCNPnqOyOD8R/s+6r4mLNf6hIgfPzo7Aj9a4e++B9x4XnaPUNUEgBIDyYOfqcVm/F39\r\n" +
            " q3xlrWgSal4dtE0zSjL5KTAB3lY9NufpXzLc/FHxb8RPGEGn3er3Mk99OtvGJrjy41dmwMsSAo\r\n" +
            " yep4FfR4bLq1SPvux8Ljs/wuHlyxi2z7D8MxPocwXSJo2VPRgcfT8q9d+F/xIu9FvI5J7p1IIw\r\n" +
            " CcDHpjNfn/AD6L4o8A317HqlzfWF3pN01pdiK58xY5F6/MCVYfT869L+G/x11hZYotdlW5bjZc\r\n" +
            " L0cVhjMulS13PQyvOqWIfK1Y/Trwf8XxdzQukijPzNk5Ga9R8N+JpNcNuVMZJb+EdutfB/wf+J\r\n" +
            " st40aTO+84I9D+NfX/AOzNeNrVzGLneQCB7jNfL1YShLlPqaM4yjzpnrPi6NhpUYTKL1PvXBeI\r\n" +
            " fiC2iwulxu2L8oC44OetfRl94Bj1bw6kvk+Yyx5XA4//AF8V8o/tMlPCEdx5CBJmO5lbgr9TW0\r\n" +
            " rwWhlCpzto4bxz+1Bq/hS6I0BZJCjglnkKquPUdx+dZUf/AAUx1HTpfs17b/aHDbTJb52fQ5Jr\r\n" +
            " 55+N37Q48KgrqqiaRwcRKuXcfTP6nArw6+/bW0u1uVGteGMWp6fvAM+vUYNduHwlWquaMTmx2M\r\n" +
            " w+F0qTsfpT4P8A+CjdnqSrFOrwSO3BLKyH2I3Z/Svevhv+1IdRgS8sriOaGdAF2y/eYZzjnjGa\r\n" +
            " /Kf4O/tYfCbxLdomrWMemXLsD5k8Y4/4EM/rX2z8CH8H69FG/hC9jkSQBlMbZU+h44p1aVWi7V\r\n" +
            " FY5aUqdePNTkmfbHh342rqdlnUJ0MyjILn7y49elfLv/BZv4IQ/tE/slarqOn2wOr+GVN/b4Uk\r\n" +
            " lV+9gjtjJ/CvY/CvhCOLTUls7lpZcc5O5fxB68Ve8Q2K6p4D1PS/EMAWK/tpbaWNThW3KQSAev\r\n" +
            " XtSw0/Z1YyFiaXtqEoyR+Gf7HHxT1T4fa/pUvh7UXj+2Qyw3kPLCRV4ZG/4AxI/Cvrm+h8Habe\r\n" +
            " S6V4ztNSdYFivLWR4iEZ8CTDuOqkAnBBBBYe9fEa6Va/An9o3XdP8VR3EenaRqE8ZaI7ZIAS2w\r\n" +
            " qTkHkqeR2xX2l8J7XUPHnhLSdAe8tNY1XRYw32i4AxqNo0ZKKex2tuQ8nGelfeYefNG5+RYyk6\r\n" +
            " dRo1vCmonQ9Xsbn4XeHbrU7W0MzwsLkERRMxcBCejDsGznpWz/wi19qnwV1efwsltHpE17PcXg\r\n" +
            " dcT2qNuJZF6/f7ccdq434aeL/Hvhme/g8LaVYQ+FdTnkC2Fkh862eIk5UleFbbjAJGT0BrtvBf\r\n" +
            " wwsPCU3/AAk82oXFmurRGXULCe6MnnBlwwKngfP+PNbuKZyE+k6fJc/CPQtH8XXIu9RnsWWJj8\r\n" +
            " zSsikoTnuRjrVb4avb/EnT9FWytJNMvdAmjk8hnO+J1xuBIPIBHHsa6KPx1oetadp2tX8b2lrb\r\n" +
            " ubdmBDeQ+Tgt/s9ewqv8PTpmneMdUismma4v51vIpgmIXgYArtb16VnOKsNbn3x4SzN4Xs5Jzy\r\n" +
            " 8SkKD90Yq39elZvw6ulvfBVgwZXxCo3DvWketct7nQNICLhe9MZdwGOoqRyHGUHFMVgYz60AN2\r\n" +
            " lRx34NKgwOc00OeCOT6U9uGp36AeWfs0qp/Zw8AYHP8AwjWnf+ksddq0Z2fQ1xf7Mqn/AIZu8A\r\n" +
            " bwQf8AhG9Ox/4Cx122cjFDEh0Gfu4465NTk988dMVBG+DzU0Q3Bj37Uhlmz4wfSvmr/gqRqJt/\r\n" +
            " gNOtoXWdDuXAyPxr6Xt1MaDBAJ/KvlP/AIKT/EO103wKbR2jd53CnkflSk9Ghpn5Qz+GPtPi3S\r\n" +
            " 5NZkybSGSaVeg4+bB9O3NV9Y8Qya54Il1q7uhbRWkzJHEp+ViTjn2ArotGuPt93rLatGcTzSRz\r\n" +
            " HqI4gO9eZ+KVhtNCQ6E4uNElcq8QO0rjjp3raLuiZlifXp/E2hLplpdRXDwnz0Zek2T02/0q1o\r\n" +
            " Vpd6X4ogtWiUyyMJAvRIyBzkVmeMdKs9A8N6XeeFYZVMBV0aMEsR3DEepq7deILzVk/tu4Q2OY\r\n" +
            " igEny+Ue+B3zVNXREdGJ49up/GXiRdItpTd3mrOIvufLGueWHpX2n+zf4AXwB4QsbGzQsbaEKS\r\n" +
            " OpPfNfMf7H/gJvG/jObxFfpvjjAhhyuN3qRX3b8O/DyERjCqDgZxXyOdYu79lHofqnCmVujRde\r\n" +
            " S1ZjeL9Xu47DFvA7tn7p4r5/8Y+B7jx14yC+JHnaJmw3BCAdlX+tfcWn/Du11mZvPhVlUAYHc/\r\n" +
            " Sp9U+BOlSoN1jGjL0bblvyrx6GIdP1PqMTCM4qEj5El/Zcn8TeC20jSbi4isQySxrw3kspypQk\r\n" +
            " cHI9a8n8U/8ABMPWPEPiGfVW1bzrl3Msqy27szEnqWzya+/LvwlrHhy2MWiuiwDnZGu3j3qC68\r\n" +
            " bavpVn88CeYq5G4AH9K9Ojm1aOx42KyTDV1dwR+e/xM/Z+1D4c+BX0iC3ddx3zSy5Z5G7sRjAH\r\n" +
            " tXiPhDwR4h0jXlk0svNHG/zIxwCM/pX3X8f/ABDe+NPOh1BI1RWLH1J968F0/QLttXb+z02qx+\r\n" +
            " ZweD9K9RY2coNy6nkTyalRmvZ9Dr/hfqc1vc2FvIoMikFucfhX6J/sRrHdXELXICswAPbcK+BP\r\n" +
            " h/4XlS/jkn2O69OcYPvX6B/sg2iWGiWzwgCTgk9fyFfM4189RWPrcFRfsrNH3Hd2kdr4NVrbhl\r\n" +
            " TjA/rXzR+0N+z9beM/DWo6pqZd1sE8x1XnceeD3NfQaaiG8OWqzSECWPnd/Sue161W1065hkXz\r\n" +
            " Y515UrkP6Zq4K7cepySToPmsfiX8YvgFqmp/Ep3trWe7t7lxIW2YYKf4SG9OmK8F/as+CkunX1\r\n" +
            " zHplrLbXCRq1vFIhQFf4gpPGc1+mX7T0ep6P4yluJtPQWqucpFGFMY7EYrN+Hll4b+I9tFHqtz\r\n" +
            " as6cNBcwLIGz7NxXZQxs8JJcxOa5LTzSn7Rdj8vfgN4Mg8Y+ANQ8IXngQ6h4t1DUIm0rWY53ju\r\n" +
            " rXJAMZjXiRDg9emTzXt3in4aeP/wBh7xvYHS9Ruktp1U+WCWWNu6uvpnvX6w/A/wDZe8I2siTa\r\n" +
            " VZaBaOGDCSCIW74PXkLwfcGvSPGf7IfhfxVpklvquk6ZeLK5ZiW8047YYgHA7+tetVzCGLp+/Z\r\n" +
            " nzWX5O8tq80JM+S/2Ff23P+Fo2iaf4izZaxDhZYmb/AMeHqDX2VBs8Q6VuCxytKuQZSePc46V8\r\n" +
            " seKP+CaUfw78cW+t/DKK4gWJiwKMT5Q/u56lfavqz4SaDcw+HraPWoytwqBXHBU++e9fPL3WfV\r\n" +
            " V6kKsfc3Pwo/4K3fDw+B/2rtZmuGe3t9UdZNyD92DgA555rO/Z2/arv/Bdx4PbRtQWcWcr299D\r\n" +
            " IfmMRGAPUgjn2xX3P/wXs/Zlh1n4ft4n0u2XzbH/AFkgH+TX48/DDXLbQPF1lJr/AJgst5Ejp9\r\n" +
            " 5MjAP4GvsMtre2w6tuj8vz/C+wxPkz9X/DB0PXNRutOPimK3TW0kuYVuEZJLN3B4UcZHOQeQce\r\n" +
            " orE8D3Xgv4G+A7nW7zVjqEVuhi1DT7x3nXUJtzeYYV7EgheMfMvbrXnn7K+u3fxR8I+HLcW8Op\r\n" +
            " 6v4auTawTkkrdWjNvUt/sBkA/2d5r0u58cfC+7ur/w5PbmwsNQlaSTTLwsyWt6nIMMqqcbskc8\r\n" +
            " HIz1FevGXMtT5ppRbSO08c6hpfxK+DMq+HdLuYI7l4phD9nNvLwwIyhyQ2AAfYZ71Y1vxPovw6\r\n" +
            " 0DRGiIOUS3iYnYUXbnYw/vAg/jS+B9c8Q2ni6WPxbplvdWWtLGsV2rDzFXawUlR0AEYU9cH61S\r\n" +
            " 1o6MsE8WlQG5u9MuBO8U8e2OFh8oAHQnB/XtWTA+1v2TrgXHwyiP2h7kSHeCx6A849O9elTp8o\r\n" +
            " ZMYzxXkn7Gckt18KY7i7BDTuSBgADBI4x9P0r1qRsYHYdq5zaKsrDCSB8/emjBADdqe5DDjtUS\r\n" +
            " oxORjHck9KaVyh2FH3cCkUhj82aR5MY20FgB8gwaQHmH7M74/Zw+H/3f+Rb07vz/AMesddrXD/\r\n" +
            " sxk/8ADOPgDd/0Lenf+ksddwCAAHzmmwQ6MZ5qaNtq1ChwcAceuantyM4akBat03oAa+Ff+CwN\r\n" +
            " 5aeGNH0q5v43mFxJshjQcs9fdlsCqjNfFf8AwV88FDxD4b8OXcpKtZXRMXvkc57Y96Tt1Fq9j8\r\n" +
            " 3fGa3NjoyQaCyRtO4nvC+N0SH1+vSuJ8SNDH4Pa00KH7PPATKysucJnJP41o/EzT9U/tHXo5Ls\r\n" +
            " vLfsHTawAVU6CucfRdWn8DQ6teXge4ubfyZIioU7Q3BBHWtYRfUJPTUda2c2ieGbuPWme9juAJ\r\n" +
            " LdUyNvfGe1cr8S/HM+sRWWmwYjd9qmNO2exPrWn4p8cNq3hCK3tVe2mtcLOxG0HHTFeeeEZjN4\r\n" +
            " +0+XVGBQ3KlmP1q5WUW/IqhHnml0uj9D/wBkbwQPDXgewgePaUjGcdSSMk19LeE7dYHQgEADOS\r\n" +
            " Ohrx79n0ofD8UkOGjKArj6V7Po85dgY8EHGQTivzbFyc6rk+5+94OmoUIRjtoej+CL2aOZwm1m\r\n" +
            " 6hiuAPXiutsbAJAZWLMXO8Nyc/X0rgPDGo/ZRKQRyOQeSfau38MauslgIbhyqqOXfotccm73R6\r\n" +
            " EqHMtEUtW8PyTM8km4jGNoPIrzjx3pu+OVVUjKkd8rXtLFWsB5KrOvPIbGB61518WrqG3sS9oA\r\n" +
            " CRyc7evrXXh5NI86VKV7Hyv498OhHlNwC7MTkjmuRstDt3dUgjVAh5JHFdr8SL8y3MkUjFnzkh\r\n" +
            " Rz9RWR4biCxfvAx75IrvlUlbVkvDJTuS6RpSacMqD1zkV9gfsnX4vbG3S3chwRkdBivkZr9Y3w\r\n" +
            " 4LnlVA7mvqL9iaSSGSGS4DgFwCpXkGuCpFyaZ6eGhemz7agiBsdPhTawRQ7E9cn19a02s1lURy\r\n" +
            " oJFbC4Pb3rKj1KOOe288AAxhV5OT9a1rK4MdxGcgknILdhmtlaMvM4a8LLY82+LPwatNbgL6fB\r\n" +
            " GJFK7mKhlYZGa8d139krw9c3Ud+1j9kmDYZoCUVu/K/Q4zX1d4m1yHSrpPtagK4VSHA2A8/n2r\r\n" +
            " m9dgt9VytmyyZAK8jJPfH4V3xmlpJXPLftI/BscD8PPhHaeH7SI2U12u05UM5xj8a9T8K2VvZQ\r\n" +
            " A+QfNHGQ2VP4Gsi2MVmsUYLIFG37pYqM966LQpEmbcdhPXaO1YzcVdJGEo1HuajafJqsZEqqke\r\n" +
            " cYVh0PTj602Pw/Ho277NGnPLlflKgd/etLT0DzFrdSzjj2PFXNUtC1sxm4I525xisKjdrmlKEk\r\n" +
            " 1zI+S/8Agpz4GX4hfsseMrYIsrQ6fJKoY55VT/hX84Wk+HpLiGUi0luFD/NsHKrk8j8m/Kv6cv\r\n" +
            " 21ZbOz/Z98Wz6tIkUA0ybJ4/uMAD+OBX8648Aat4FkZdSI06/vl+36crOGEkYkOUPZWr2cjr+z\r\n" +
            " hKN9TxeIMEqsoVGtFp/wDo/2SfjRqPwi8aaN/YuoXENrczvEYiCu+Nsgrk8EDPSvtH9nTT5/Gn\r\n" +
            " 7SXjDw/wDEa/0G7ttc0ufVNI1GNQ8e1GjBuWI/1flQvJKc4JKhRkkV8IfAeGw+IHjwaH8RjeWM\r\n" +
            " 63ZuNPuLYqkkDP8AeTDKRtOFPselfc/wP8VeF/2f9CW3+FGmQWPjLxloi69Zy6rcPfteMHZYlu\r\n" +
            " Ztiq+ydC/2KFFhDKHmlZE219hQ5Z7H5hi6MqE3fqet+O7y58OfFV9HeTTtOsrq4efTZ02+ZBDI\r\n" +
            " SJLaZ2xukUjYUOGVlI6jmvfNYQWd1rHhRX1S9uA0MXmKf3zqQASv97jJ78VynxP+GniHx38CtB\r\n" +
            " 8ZrbaZrfhOHWIlGo6fcOf7bvLtnS5lbd85AniyrkknzN527sDqf2bPE/h3xz4rtPB+nQXyazoj\r\n" +
            " teGSbOS4ba4I6cHIpVYqOxgndn2/+zZoN/4e+CGgW3icxnUGtxJcbU24ZiTjH0xXatEMHBpNJj\r\n" +
            " 26TbqhztjA5HPSmyNslPauJHSMYbWOOR60mPkI6f1pzADheQO9MB3Hjk0mrgNYbT0J/CmltvQ4\r\n" +
            " p2WQ4YGo8Fhk5+lMDzH9mY4/Zz8AZ/6FvTv/AEljrudvILVwn7NP/Jt/w/x/0Lenf+ksddw3zY\r\n" +
            " 74psETKu05FSRjkGobcZHz5qxCVQHcDSAtqojA2jp718wf8FQ7dLv4RK90DtQlgx6IQK+nrcLg\r\n" +
            " bTkmvlv/AIKw+IrLwp8AWk1WUmW7lFvbxqASGPU+2KiavZFRdnc/IHx+l54g1bVNUjvRaWcUYh\r\n" +
            " EfAJbPBJ7DvXD+JvFNw+knTrmQ3EEKZFxGflc9hkdK6n4uaxaWMr6ZdzEm4QSMydH56fWuEitr\r\n" +
            " bTNBlg+3A2fmsxhPPlnHBNdSRjJXbRgHUjqNpBb3xcowJd2blvSm32iSLbLPCgyHAUDrj1q7oW\r\n" +
            " kPqr272+0+QQPmGFAz3ro3QLdySaiALflCE6D3rCpW5XZHsYfB80dT65/YB+LUXiXwVHpt++L3\r\n" +
            " T8K4fqw7GvrbSIEuLNHkcAg5yvevy7+DHiu/+EfixdS8MOJbW6T54z0Uehr7a+BP7VGl+PFWwu\r\n" +
            " 5JIL3aAFPQn29a+QzPCtVHOK0Z+pZHj1Uoxp1HZrQ+htL1H9+DbbiQMnHGPeuw0K9N5gW7gscL\r\n" +
            " jPX61wGgESuPnysi8BeortNEtf7PAa2RZHbhssMgeteNUStY+vg0kdqY8IGZnBxt5fOD/KvM/i\r\n" +
            " 3cpJphW8AdmJJ4yeOw/KutvvEYs7Ei7EcJi6BTnccdceteF/HP4kLplpI0jcckYYZ+vH4VrQi2\r\n" +
            " jKrFHj/j2+Nx4gSOFh8zenQ1dFxFpW1JsqSvABryLxT8RWvvFESWjvl3APPPJ5GO9eq6b4Turq\r\n" +
            " yS41AyZZRhTj5R6V31ociSfU4YTVWTSexqaTCt7dWzpgZcA568mvsz9kXwywurbBUB+cngfSvk\r\n" +
            " HwtpbT6vb4HyowGCOCfWvvH9muzis7TT4oIiZgoVhn7xrjbu0kdkH7Omz3jV9HjtIrby3VcEFh\r\n" +
            " 97Ptn/AArRDxG0jY7DtbljwSPpRr3h+XQrqIXkSqJUEiAdvXA7Via95l5a4tQQzKVBHGPrTmnF\r\n" +
            " 3Zzxj7ZeRm/GPWY72GA2s7PDC21mXho+OP6/nWF4O8Wyala7bjhUUhSDyV/oe30rw/xJ8WdR0P\r\n" +
            " xZqGn67IQtvKyrzgMueOOprq/h/wCN1uCrTgvEVDEYwQfrnpUQxaqy0NJ4H2ME3qeyxWUl5GrR\r\n" +
            " BySQTz0+vrXb+F/DizWqluXLArtBz9eK4bwJrEerW6pI+CvyhhxnPPNeyeHLRbTSoTap5gXnPQ\r\n" +
            " j1x/8ArrvhFT3PHxMlHRIkbSVtrXzAzAMMgDv9fxrC1zUBDE+51OcZIHAxWlq+ooymESIm88HO\r\n" +
            " Auf8ivm/9tr9rKz/AGbvBl3PNJHd6lIhFpbDGXkPTjPI/wDrVhVXQvD03UaPnn/gq18co/Eut6\r\n" +
            " f8M9CuxEk+y91UpIEymcpG3sdpdv8AZXPevy9+Ldhp974ql8QXuqw3Gn6rDLZWquN+wRBgkiYH\r\n" +
            " yKHQY/3q9d/aO/aNk1DxDL/wnMBl8SeMnU3dyGVntYPuiMDJ52kjaQAAPevCf2kvBVloWjzQ+G\r\n" +
            " L43NvpNwI7P1mAfByegySTgcV1ZdSfN7+lzDNa8adFxpq/Lv6l74t+C9NtZtF8W+EZobe8jSBn\r\n" +
            " txglzjDMMHPDA9se9fUvwU8atZfDTRLvWJ7KTSfBuvXBuNN1C6WKx1G31CAXFusvzhmZbhZAih\r\n" +
            " JPmP3Djj498Jvp58HtPr5n/tMqEeKQEbF6nIPb3Br6M+GWppF8HvEbW1peagz+H7fVLeG28zzW\r\n" +
            " lsbs5KLGQ5IS6zhWTI4JANfUZXV5ZONz884ppwqKNWMbNn0nrn7QPi7x14D1CTSbbR7HxlLYW9\r\n" +
            " s/hmG6jbTfh5p6MJjHFCq4a8mfynk3AlECKxDnaMj9mD4lWcPxbWXwte2XiHVr24it7gfZ2ilt\r\n" +
            " 45CAWRvZycqfrnFZHwp8dp4csLL/AIQbwe3iKeG8tdVu7byxp6X145VYEu7kxoZZDuwkEaeXGi\r\n" +
            " uV3sWlXtP2S5fDfh79pG6s2lt7W907Vzp8hSAJ9qU52MnI39VySMnrXrVdrnxUI2dz9CLNTBaI\r\n" +
            " JOPlFQytzkAcVK8oeLbGOF4Bz1xUM3AyOlcKOoa75OMCosbSevoKkyMH5f1qMjjPamAxnO/BP5\r\n" +
            " 808ZzwQKQHac4yfrQeevbpSWoHlv7M3P7OHgAnp/wjenY/8BY67hRkd64f9mjj9nH4f/8AYt6d\r\n" +
            " /wCksdduPvUpMESpjoMmphJkcqar+WW4HeplYYxQr9QLls5ZMOpIIr4j/wCCzfhm11v4UaTDrE\r\n" +
            " 5h8u6MkAGfmOOVNfbMHyqD2HNfOX/BVu3j/wCGQtauRaQ3UsLptDRhmTJ5ZSeV/ClPRDjufiP4\r\n" +
            " stbbUJ4ru82yT2kuzBbAMecEZrhviFpz6Xr08WmRt9musMqjlQB7jvXca1oNpqGiGON2Vb4s6p\r\n" +
            " uyRIDyM155PNfzawP7SMiJEdgDfcOOOldLaSJteWp0+nQNB4XnS1UrMyrnnBqjFcNfaikNysjG\r\n" +
            " SMKW52gj146VqiP7HAj27MyuQSp5474rT8OG31a5uwgjWZIsK3/PQH27fjXBzbn1EKCmopuxp6\r\n" +
            " Xbf2e1vcQywMAuUh8wKJfXk/yqXw148S08XSXfhl5reWF18sseFbvz061x7ySWV+oYlp7JwWVh\r\n" +
            " 8jpn0rsfh5p1pqEVxJPJ9mhcswEaKGQ+gz1rlqQXLzM7sNXqOp7OGlj374Tft1apo14IfEkQvj\r\n" +
            " G3ls27bn1HGa+l/Av7a/h7VtODbpLK7X5MZUj6cV+b1rNB4c1OTyY7icFS8bZ24Yevas7TPGRs\r\n" +
            " JSZZ2W8lcysm792M/wAIA71xVsshWvJI+hw3EU8PaFTVn6S+P/2w9BtbaRVv4WyPl2sM8/rXzH\r\n" +
            " 8bv2mU8RSPHortITkL82M14J4uhm8HWlhql7fhrjUlMy2hY7YwOOTk803QjFr0dpfa9M6FGL5i\r\n" +
            " OxJPQEdTj1p0cvp0Y33DEcQ1cTJ0UrPqXJfHWradqQunhQPGwcZOehzXrGuf8FD4tGs7SI6bNc\r\n" +
            " lUAlCkIUOOcHHNeT3GspBq0gmRZ4ON6Zxj6GrWlfDzQvHOvvFbQNPJ5e5YYCBI3GdoBPb1repT\r\n" +
            " oys6sdEcFKvi4KUcLV1fc+pPgL+01p3xOEWp+Gb0iS3YGezmxui+o7j3r7E/Z4/abhufE9kt3I\r\n" +
            " UCsBwQBn6V+Pdz4E1b4UeKtK1PwJLdadJdBmUTLgHBIKkD7w4xzXovgn9szWNA8URNqdkbGeEh\r\n" +
            " XZmwmfXHXB9q8/F5dzWqYd6eZ7uX586cfYZhH3l1Wx/QR4g+Pa6lax3WqTRww28XzSOQqhQO5/\r\n" +
            " qa5jw3+154A8R6g2n6L4m0i9vi2xYYLlJGDehAJr8Jf2j/ANrX4hfG7VE06/8AF9xdeHoYgzaf\r\n" +
            " ZyC2ts+hCndJ9W5pfBPjjxJ8NPh8+u+EUt40tnRJijjzY93Qj3z+Peuetl1RRT5veZ0YPN6VSc\r\n" +
            " 48tkup+n/7YsEui+OH1Gy8xBdMZiNw+UHrxVD4QfFeNSsT4DkYPzDke3Oa+F/Cfxg1G+0ey1HW\r\n" +
            " F1h7q7laGHJEqO4G4oMsd3Fdp4U+Nl7pOljVdRka02SNEUkiKNE4Gdsg6r16Hr2rzHg5wlc+lW\r\n" +
            " NpVKfLzbH6gfDzxgLu5hSO4MScbk5+cZHevdNI+K9ro+nxw3t0IegDM/yjnr71+Ieh/wDBUHxP\r\n" +
            " canLpNpeWWnyIDCoa3ZmY+oYEn868u8Xft4eP7/x9c2niXxr4iubfIZEysESqT02LwB7jJwK9K\r\n" +
            " hhqyunueBXzDDSSblo3Y/bD9qb9u/wN8IdHu7a21i3vNbkXAtoH3yFvTA6V+VXxx+PWqfGL4hX\r\n" +
            " eo+I9YL38rbbHTVkJliw2QSvXoPpXgXxr+I/iXS/F8V3plzEdNu0jmllgDO67u0jNllJ/A0eJ/\r\n" +
            " iRpOleLrO88PlxfwhEEMETZdtuWOC2AvJ6nnk96pYSd+aWoPMadHmpwTXS7LvxR1PT/FHwzuNf\r\n" +
            " 8SReTqyzPaBIV/f7hklgv91VC5PqcVQ0e30/UvgFNBHLbXN1YgF5iSWmQ48vaT05JyDyCKT9pL\r\n" +
            " w7N9js9ZuI5LJ7aXZPDbqQWSVQQ3Xj1wO1R/CTw1pmm+H7k2N59vS+RoPKwWKOTkbiOUII6+9d\r\n" +
            " kIxVCL/pHjzqTeLqRkvs2NjXdfTxz4SttPihso9RtI4kiaLr/q2U7+OT688Fa9c/ZZ8fXHhzwZ\r\n" +
            " ot94NvrK31rSjd6fOJNThspLKO4gKGXfKwwVdI2HXnHB6V5X4Y8L2CeDLRdUljtNQtJd8j4JVV\r\n" +
            " Vzkt+RHPpXN+OtT0jQLmyg+z2t0+qXAkceXvdlzkEEjr06c9K7Mtl+8fKj5/ieny4eFST1Z9ka\r\n" +
            " x8UNI/Zn+GWnXfg+a78feJ7lJW0sy6gt9B9pkz5k9zchUDDd95Y13SAKrzMny1F8PfAviXxb8f\r\n" +
            " PAXizx0dQn8SSvA2ozQtF5TygBlDqh4+Vj82OeleXfDrxh4a1bwTc2+oWmpR36W8kKFCn2eLIC\r\n" +
            " P8rA7WGcg/3sfSvTP2JPhf8RP+FlQPe3/2ixm1a3+yqZFDQWsYG6RwCT9zCkdCe1e/KV1qfn9r\r\n" +
            " OyP1P0Od308G6wGckqB6dqnOFBU9SeKSJEEaCDbtx8uOmKTKNkMSD9a5tjYjdSQd1NDZUCnuxz\r\n" +
            " hSOKZt8xCSOKAEdtq5FNOZBgYpMk9e1IGyT7UAeZfs0c/s5eAMED/im9O/9JY67bhMZxz09q4n\r\n" +
            " 9mj/AJNx+H//AGLenf8ApLHXasu7HtUuIkSRud3DdKlTO0HgmoUYhOcVJEcjPvVt3GWrSQ5Ack\r\n" +
            " V87/8ABRT4Za94++BmoaZ4UZrqeV/NtkLD94wGTFIOmCM4PrX0PF0ye/NQ+KNCg8VaBPY36gxT\r\n" +
            " oVPr9QexqJK6BPU/nY+L2iv4D1pFbT2ikt3McsUXzNE/cEZ4Oc81xOgaLba5YzXV75hLzEYkbJ\r\n" +
            " Br6g/4KR/s9av8Hfjhqq6dHLHHLK8kDXJ4mVudwYcGvmf+zpbe7tbryz58h23EWQQtaL346m1J\r\n" +
            " pVOV7Mo2uo3EU97Yvh5EjJjDJwf8KoeFmulW9licrdFfk5+9jtXcal4baCC4v7JUZ1iOSeoH1r\r\n" +
            " kPCNlHLrAeR3Dbw+zs49veuWE48rZ7NWhJShqT6v4m+1WqDUk2XKrglThj/QitjQNfcaEovgsl\r\n" +
            " sWI3hcZyOOnQiuU+JVpIuvn7M5MEjZUYxtPpWt8Lbj7T5ljOu6B/lbnp+HatJJOkmKniJrFSpm\r\n" +
            " rPJbpam4juJVVFwyIch/pnofassWMd0YJ9Lk+1KJN7MMb19jSeNTa6MpjtJJAy9Vz/AE9KzfBl\r\n" +
            " 5LbyK8IbaXOSSNpOKUINwv3HUqp11GSs0T/EHU5Nc1FH+cpEm1Qx+VR3Fa2geKEm0JILqLKxpt\r\n" +
            " BX+D/P9Ky9euY9SSWR4ljlBxnGA9Y2la2dJndZgdkg+8Oar2fPBJdDN13RrObejOottQFtfLDI\r\n" +
            " N0dx91zyV5xkmtPwh4luPCnix5JIzPFFIDBKD86FT1QjtWZ4Qv7OZpp7yJppmQoiJyAfUirWn2\r\n" +
            " s89qY7OGYupL5I5Qev0rCSSbi0ehh+d2qweqZ6L4s8aaZ48vNHk8JJJ9shkIngmlKRgMcnbljy\r\n" +
            " Tk5q9qmt+HNZ1WXTNZvZLZWKkC4t9xBAwVLLyMHuMgivHWjmgvUk0ON5zDy8qxkZx7dvrXQaJ8\r\n" +
            " W200jZBPFdyqQJiRlT+INYTwa05WetRzaXve2jq99BPEXguDwJq6f2Hcrqem3QZWmUhlSTsFwc\r\n" +
            " 9PUCu10611Pwdoeoadqizz6RND5sxVN6umRtIPYgjBP4c15pf37Xt9ayfZoUlVy7SJkbj3Lete\r\n" +
            " ueFfG154r8LJo4l0q4XzCzG6bCQqcEhsc4HXjmoxKmkm1fub5bKk5zS91vbsVfhX40b4kXQt9c\r\n" +
            " a3ih0lGe23MbdoEUE4jI43EgZJ611fgawudV0/VNRk1SW4s7i2hmlmJAbexI2sp43AKRkdsVzf\r\n" +
            " hrw2o8Q6le22onUdOtISsM20QwyTFceYw6hAefU4rf+H/2SDSk07R9SM9nGCHPAMztwX9lwAo9\r\n" +
            " BXn4lW1iux6+CcrxjUetn1PLvFuqxeH76TVfhrczswy91NcIqyK3ov8Ac7+9WfhZrek/FHxRbS\r\n" +
            " +L/JWSzdXb5vmuUzhgT3IJB57A11P7Slx4R+H3guG28IzXN3qF6cSQtEBHCMcs5YZZj2xxXkHw\r\n" +
            " K0Cy1vxC66jcT2jpk5ixlgR/dNenCKrYd1LWZ89OcsNmUaEZKSvquh7X8UbfxNpXxA1/TPBEAu\r\n" +
            " 7S7nS6tSAqpAxIbcuevAx9DXl3h5/Ekvii5jj0xv7S06689XgAMaEHlfQjkjA7V7P/AMJ9e6Qt\r\n" +
            " 3e21u13qFlalN6DDpEq8szdBgY57ZrzyH4nCXSGl01vsmoh1e3ZG4g7kk9yfSubDTk4ONlbY9H\r\n" +
            " MKNL6xGSm73vbp6HQfEzxNreqeAdPs9S0u9tmhn864BjZdqjkZPZADx+VZnwbkSPVIdT1KK58h\r\n" +
            " HIidSFSVwSSD69eta3i681/WfhVqctwktv58KNcSM/Eq8knr0rW+EkKJ4P0Wxubm1MFtA8skRP\r\n" +
            " AlbhQzDOCccVlzclK1upp7PnxPO5a8qauHhu+fxr4k1GXTmhtheuYZExnco9P4Tyfrmuf+Kmkx\r\n" +
            " 6VY3iRvNezORChSHzI7TBADOf4fujpXZeE0tdO+G2rJotxb212b6RLWZ2ALj+LYR1ANUfhmbG1\r\n" +
            " 8P6y8Etw97dxmFyMIsa45cAnrkE16GXwcZOp0Z8vxJiI1VGlu1uen/ALKukXfiLwY2k6wsJtbi\r\n" +
            " ykd7m3QKA6rkb93JyefWvvD/AIJ0/DCy0iyvPEME1xc3TYtwJVwgBG7gnqfmFfAn7LXhSKDTLp\r\n" +
            " ND1HUI7K3cyW0LlWLMWztfv8x7c8Gv1o/ZG0mST4FaQ+tWB02WSMnyQnlleTglR7V6kpHySg07\r\n" +
            " s9KsCDFIUG0B8KPQYH9c0rthst2HpSwwrbjaqlQvGT3FMllBA3cVk9Sg5LbgefpUZYpnjj6Uis\r\n" +
            " ygns3SkBLZwcgdaAAjb1I5pCo2kxknNDrvBz+FAXyQBu5xQB5l+zIc/s4eAc/9C3p3/pLHXa5w\r\n" +
            " Fz681xP7My4/Zx8Ae/hvTv8A0ljrtnGQPrRIEO3ZBwe9TIyjOMYxVcIVPI4qSIZYg9qSVgLcf3\r\n" +
            " BVm3PHBqrEcjB4x096lgbBIPWmCPMP2kP2W/Dfxz0eZfGthHqMbRMCjEAqdp+ZW/hOcc1+F37Q\r\n" +
            " Xg6TwB8TNW0u3he0+xTSQfOhYgBsKc9zjvX9FuxZrdkYL8w/iryL47fsqaB8V/AmrWV9oektq1\r\n" +
            " xE4hne0QlGI4w4GRU83Le5UXqfgz4bv7i10eaDXASPLO1hg7xjvXMaTAmk66ZGVGhflQDgr6Ee\r\n" +
            " or7A+KX/AATi+JXw4m1W51/w95Oj2oYiaORHyn975TnFfHPj8yeAPEognjEqwPwSCBj6965opO\r\n" +
            " fLFas+jhXvQjOWyZzXibV5dQupvtxDFH5PTI7V0XhJP7Kt47zTIjGxX52UZ/Hmm3tvEZWvDGj2\r\n" +
            " N2okVs/cPoa3/Ctn9r0S4FyiiFk25B4H0rapL3OWxGEoylXcnLXczviHaW+pWsaXjB7iMhkdOD\r\n" +
            " Kp7HPcVh+E9KzrFxYagzQCYfunkOMN26U3x3ZXegXEcaO0sTfNHL97P41b8K2yeIrq13yr9olZ\r\n" +
            " UkVx0Gev6VcVandGUv3+LWlnc7F/h/Jd+GJpbOEXIjUpNEoy8bD+MZ7e1eSXmmTxxb9jqqsVye\r\n" +
            " D+Ir6A0fXbjw3HqmoWUYU20P8ApETcrKgOCwHftmua+3KfA93eWmkR3y6m7ASDkR5JwDjoQfXt\r\n" +
            " XPhsRKC2urnrZjl1KqklK0rXseSaF4h1DRblZNKlZWJ/hGSa3rf4l+IJHlWae52Nx8sXT8hXU+\r\n" +
            " GPhDLrFoslvGIrqEgvAw27s9wehr6T/Zm8B+FfElgsGtrbJcg4IkwCf8a1rY2nBNpajyjhzEY2\r\n" +
            " ap+15L7GV+z74n8D+KvhdpGn+ONBs4dUtd7S3zQv514zHP7xiewwABx3710c37NXgu7N3d6Yib\r\n" +
            " 3kYxxlwYkGeAox1/GvpvwJ+y34P1HRz9mS2LsAR05xyf0zXqvhf9hPwx4ohiFm2nKJNoO5wu3P\r\n" +
            " Ukeleb9a5nzbH3H+o1SNPWv+B+Qfx7hX4d+KZLbTZYDhA3klQcfSvM5fiFMpV4YWt5/WNsfpX7\r\n" +
            " 93H/BHj4YX2gXeq6/b6bczohZikYbbgHkyNwMV+Q37cnwG8M+F/jHfWHwev7K8tLNysht5A6Kw\r\n" +
            " /hDDgn6V30q8JqzWh8ZmvD+IwMHUhX5ntseIeC/FFxqzPazieVBmRgoJLkc9O9exaHpA0rR7XV\r\n" +
            " fCd03l3oKTJuy9tIDwNnGemevFY/7IvhuK4+I1vaTWwe7vp1iJYj5FVslR7tjH511XiCyXVX1q\r\n" +
            " 30CJdH1a1nmNxExPlkK/3kznnnHFefi6ynWcIrQ7smws4YVVpvmbuvNHD/tAeFLjWNAtNSuIbx\r\n" +
            " pQxBvLl8xsAPu5HAPtXBfDvQdTtr6G8gL20IYR/aMZEZPTd6V6d498YXvh34R2+nePdKvVfVx5\r\n" +
            " 1lc3DcMgY4CjnYuQT0yeMcU34GaUfipompaJplzFE9xGrSFh9wLltw9cnjjmumNacMPytaHBWw\r\n" +
            " 1HEZheErSt17ndfDJIPHl7f6JqczJfywNHPITmHK9iCfmBPXtgknpXK/CiHSdc13xJpniO2srm\r\n" +
            " drecCW1QLGnlghNmOOWGal1rwtY6X4I1vxD9ouEnsIRYpb2zFS0nA8yQ4479Oua4L4WaneeCtV\r\n" +
            " t76bT1uY548zQyr/AeFbjnPvXHCkp05ThI9apjJU69GnVhtfX10PStQg1G++CkFrHdqrR3CwKs\r\n" +
            " 4LCfBOCBgjjgc/hWV8LfAEsPidpLu5uFvLBCbuAxEBmB4UY7bscGtr4ifFNL7QfD1tpUMdks8/\r\n" +
            " 2iYMMHYr4VBxyMhjk811eqalZ+F9B1HxLsL3+snytyBiFkIAOB0Gcd6zjKooqnb4i8SqMJOstX\r\n" +
            " HQ5Dx3ptvrF8VnnjiuLWLyIYEiOzzMg8ED5QOct7Guv+Eml6jrPhu/snae0j2BZrpEWZmcHlf9\r\n" +
            " 0gDp0ziuZ1pJbbwpBHBcyYvlCb4kWSZWc5xnjHFfRv7OnwG8a6j8NTJ4X0x9Rv5Z1ZLZAVVEwQ\r\n" +
            " emRnoee9e/Sj7OCi+h+d46u69SU+7PX/APgmd+zxDrzLN4r09Zb6FBKs7xbFjQnIUJgDOe+K/S\r\n" +
            " O1tlsLGOCABVjQJgegFeS/safDHWvh58J4k+IVpbWOqXLiaSFNpeMYwFZl4bvzXrckpHXFVJ3O\r\n" +
            " BJ31ImbkhjxUUhO7jpT5ME5HP8qjeTZ68+1SUNH3z6UKem7tSM23k/dPT3pqTCQsBxg4oAGcq2\r\n" +
            " VI/GhiGXC0yX5XVfWlVCDxzQB5r+zI2P2cPAHr/wAI3p3/AKSx125cbMEYNcT+zIf+Mb/AP/Yt\r\n" +
            " 6d/6Sx12wcqox3NNiQsbbjwcex5zUgwWPljafU0xWJcj0qRIyTlfpSGSwlmQdAasQcHLY3VGh4\r\n" +
            " weop8Uec9aALdv8zDk+vWtGz4Kkgdao2aBmAIxxV+17cZqHqwWgzxb4KtfGWhXVjqUMMsd1E0U\r\n" +
            " iuMhlYYINfjN/wAFPP8Aglx4p+EsF94p8KafFqfhxHdpZLZSzWyZyPMHoPWv2wsCd43AfnUfjT\r\n" +
            " 4aab8UPB2oaH4ntY7jT9TgaCeM45Vhg4J789aipBfEtzrwuKlSfI1dM/l2i0/HgGdLZy4ib5ow\r\n" +
            " DnH0NYfgbxDKL17WSRlsZiFYE/dPqK+rv2sv2Wh8BPiv4gsPA/m3ej293LEvmDMiIDwCw64r5Z\r\n" +
            " 8U6Amm6+WEjCF3AdVGGU/SufD4lVFKLPexmEnh/Z1lotC54yD6bYNaXXzBDuifBx+B9D1ql8Pb\r\n" +
            " wTy7uTdq+FYDqPetX4nJ9t8OwNYyGUQYj+bk9K4/wFPNp2vQyRq0hLf6tTyfXjvXZTi5UnY4q0\r\n" +
            " nSxkXuj3v4fR2/xj8H3/hq8dLfVLcFrac/IRz8yse6nIry+10HXvh/cXtlczXFtAd0FxCjZ5Bw\r\n" +
            " Rj2ru7XXrfTYJb4RyW1xGhUn7soTnn/axzzXAW/ipvEms3d/rUklyWwjANt8zHALevGM1yYaE/\r\n" +
            " ecfhZ7WZTozjSUn+8t+B6p+zdpbeILuJLi8uLrTZrqKzkjcr8+4nPOMjpjit34g6lpngPX9Lud\r\n" +
            " FjaK3e5eKSySQjywpxwxyQxPPNcT8JfGC6PLDNpbtBb/AGxJWj28KyHsa9W+IPguw+MPwruI9C\r\n" +
            " uootVW5+2yJu3M6qDuMDfjkqcGvOrLkrrn2Z9DgpueBTpfxFqjL8c/GTxVpVzBrPw38Q6vY6LI\r\n" +
            " RGtq0yvMWXqQoH3c969F8Aftn+LdT8JCDSfF93bavL8kYjRVZSOuSRjP8q+evCekauEWDwTrEu\r\n" +
            " r2ywtDKl25QWZ7lSc8ZFcpePc6d4veL7fHe3K5aV7UsqhvrW/1aNbS+xjHPcVg5KUrrm3V7r5H\r\n" +
            " 2D4I/bh+Iniqz1rwx8SvGHiDUtHmBhv4zO3IGfl3R44OMEA4NeAfEP4Talarc+INJS2tYLy6ZN\r\n" +
            " Ps/OJcIACXIb1A79a1vglrt54T8QwLFA1va61GVJKZB9Tx1rpo08M+KzqGp/GrXbmLwz4eVxAL\r\n" +
            " dPnuZDnbGqHGWJ4yenPauVOVKs1HVHo1eXH4Xmqu1rvXS3+Z88+CPE2ofD3xtZXrXO64a4M5eN\r\n" +
            " v3qNzzxX018P8AR9P8a+EbOXRopbvV9SlE7pKfMLFhho8Dn7wz/wACr5303TtP+I/iW61Twlb/\r\n" +
            " AGW4jnRbK0TG6RQSTntwi5J9a+gPgd8TrPSbDUrjwzYBr9oPs9uIkIa4ZuAVXnacn5sfpWuZ+8\r\n" +
            " lKCs+p5/DF6bcJS5o3bXoef/tceGbzxJq1ppepanF9r062ZljyohUZwEQDLFjjHPpXnf7Ovh3V\r\n" +
            " oPG0Z0u8S2nRgksbNglR1/lWb8YL7XNB+LupL4ljeHUZnDlN+5oy3OMjuM9O3Suj+G2g3Gs+Jd\r\n" +
            " Lu9JcyHdm4eP5vLceoHPNdii6GG5XLSx4qqRxeZOuoNNOx2P7SvxN0v7LZeDdHtDaqrC4vbwgG\r\n" +
            " S5c9RwB8o7VjaFYXmp+JtMLQiMTIsalRhcpwUB/3cZ/GtnxnpsHj/wCJ8smpo0NtYwhPNVSGRs\r\n" +
            " YUD3Y4HtT9F8MXOieE7mVpvtEsc3k20snqTyEB7HjpXJzwp0lDrY9eFGtWrSqS1XT5Ew8KxfFX\r\n" +
            " 9oG3tNRO+ztGEUkcOFQKoG1U9vU/Wui+K+n3uteIrLRPDmRDazeYgC7vMB+Xex9O3FdP8CvAEv\r\n" +
            " h2WTVdZX7FHLbPDHNMAGkc9dmewrvf2Uf2TNS/al+IDvoOtSQW+XlkuHjLoArAEE54PHAFaYGM\r\n" +
            " qlRzlstjyM+xMcNSjSjpJ6s4q1+BE154s0fSLi+trCZFS4jSCLLB88nA++ccYOOtfrP+xr8Cn+\r\n" +
            " C/w1t49RupL27uh9odmj2hAwyFHfitj4U/sreDfhPplrHp2jWdxewkSvdToJZGkxgsGbkfhXos\r\n" +
            " kmzCx8CvXPiHK45yFY7eO/Heq7kDk8/WmvLvJ5prP82R6UCEeTcMcUi5JyDwOtNZjjIORUbs54\r\n" +
            " GMUASHAAz26e1IcDJA/SmJn+OhGznc3Q0AOXBA3DJ+maA+D8wxTWkwfkPFJu3tzQB5x+zGg/4Z\r\n" +
            " w8AFs/8AIt6d/wCksddsqA525/GuJ/ZnXH7OHw/D5wfDenf+ksddygywHUYpsS2BW+Ujbg9zUk\r\n" +
            " aZWgKNpz3p8QxnPakPcWOP5vmqxCeTtqFBjGepNWYUGDjrSbsBbgyVANXLbgccfTvVOAFSA2K8\r\n" +
            " 8+Nf7YvgH9nnTJp/iFrMavD1hgG+TPp9altXuOEXPY9ls1w/bn2re0ogsF4yR6V8KaD/AMFzPg\r\n" +
            " 7e3UiTw+IohHyrm2Uhh+B4rzr4pf8ABw7Y6zrY0P8AY28A3nirUekt/qs32aztj6kJksPfIFEm\r\n" +
            " rc3RGtOhUc4pLVtHD/tSaVBrHxk8Ww36I8cmozLgjp81fIPx6/ZkSxkbUPD1k1wFO9lHI/Gvp/\r\n" +
            " VvF9/8QtTn1zxalpHquqObi5FqD5KyNyQm7nGaFsI9QgEd2m7Jx0r4ZYieHqtp6XP2aGEhisPG\r\n" +
            " nVXY/PHx1oRttMdZLcwC4bOCMbSBiuO8A6OsfiWN51lzATyo4HpX318ZP2TtP8d+H7k+FY3jum\r\n" +
            " BJiyMM3qD2NfIet/DvVvhrrV3Ff2k2FAWSGRdpBB+8DX0uDzGFeDhfU+VzTKZ0K8anLeKJ/GV3\r\n" +
            " D4n8J3FrGqxOczQv/EGHVfoa8h0K2lttU8pj5hzzG2QGA7V3GpaoLCyEF5JIyzAsjf3cnpnvXO\r\n" +
            " 6Pos0EbXk8YMUZJV2OS2a9LD/uYPzPn8xf1itGSWq3Ov8Ahh4ySz1CTTr3TjFaPl90Z3yRN078\r\n" +
            " FfY1694j1Tw94e+GJufDdw1nqwcMxiPmW8xHP3D88Tj24IrxfwLdJpbreRSp5s7kMCNo24/xr1\r\n" +
            " K+GifFPwRdpqxSyvbKLMhSTy/tGB8rZAOTnjpzkV52JinVi2tD6XKqs44SdNSXNbS5zXwn+IGo\r\n" +
            " eIdG1Ow0/SrESakTGbotsOCe+P5jFcD/AGFN4Y8a3eleIGTzRJuMiyZGPr3zVv4b7NH1CSLVFK\r\n" +
            " 27EqMyFGP0zwa6a++HugyatLc2zTLbiPaoJ6yNjGD3APPbpW940ZSUdmeaqc8VSpyfxRet2dT4\r\n" +
            " f8Rax8Px/ZuuWf2vTL1RNZXBG427AZOxh0yOMd8Vn+IzD8RfCzWHkppdnGjyhUUmW4k9Dk4xnF\r\n" +
            " VdK8XNqGiNpVw76h5JPklSflkXoR9RWn8NPE0Ov6Vrtvr7Q6HdW8RWO7EZcqcfdOehP4fWuXlc\r\n" +
            " P3ltbns3VRew5m4tM8u+Hkk/g7U55JbaR5LMFZsA5hib5ScjkdcZ96+pPhnPYT2cVz8O7J7aaW\r\n" +
            " AJAYIwJWwOdzMcevIFeR/s/wCmy2s2pLfMl4uov/pUkqlhLAjbjgn+8do711Him5TwR8PrqfSr\r\n" +
            " 67tbyA+XZ21q2CFJ+8+Accdqzxr9vNQW9y8mjLAYbnvo0/6RjW+gyeNPjHqN54m02zjk0bEaKM\r\n" +
            " s9/cO2EUnOOMljgcbTTbHRrv4Zalr2s/D4RKsF0Le2MqZRpC2CVHfGDzXS/AON/EHhu71GNJ4b\r\n" +
            " 65c26zT7iWZuGYe+3OSegPFen6B+yD41+LV7pun+GNNSTS7PErXJfbF5jDAH4DnA5ya5q+NVGV\r\n" +
            " p6JHXSwPPRVSF7u/4nmHwC0XxD48ut2nWEuq65qEzQwQAbjNNyxbaOOgPJr6q+Bf8AwT68WfFe\r\n" +
            " Sym+Kdm+gwW03myWk0ZErAHoMD5Sfxr6R/Y7/wCCdth8EIrLUfEd8brUrJd8QijwkMpXDN82d3\r\n" +
            " GRX0jqt/b6aITcv5Qd1iBzktn+HivKr4l15Xij1cLhlSpqN7nlEn/BPrwh8SLGyPiVbq1t9L2x\r\n" +
            " Qx2biIfL13ZBzmvZPhr8FPDHwc0iCz+Hml2unQQcBYlClz/eYgck1q+ELgHSZSBhPOYDrx0q5L\r\n" +
            " cjGNxr7DAwtQhLyPyLOZynjKvM9mPuLg7iT1qqJ8k5HemPcAseRULT/McYHNdZ5ZKzAd/emiXd\r\n" +
            " kL/OoHYEcHJpgbbnBGaAJ2lx99semO9Jg+ZjJ61CkhH3s808TfPk5zQApyrEHofSlCAgZIANNa\r\n" +
            " Xn5c8+tB4BZuhoAVhsxtBweaXYe4xTUkCr867R6mgKNmWO33FNK4HA/s0DP7N/w+B5z4b07/0l\r\n" +
            " jrt1UD7orjP2ZYCP2bvh9nj/AIprTjx0/wCPWOu3MG1dzMAvqeBSb1BDWj2nDHBqQR8ZZTXK+L\r\n" +
            " /jFoXgiN11G682VBzHENxrhf8AhrqOeZl0zR5doHytJKADz3wKlysy1BtXR7RENzVz/jb4q6Z4\r\n" +
            " DhIuJFubtuEhjYZP1PavHPFn7Qus6tpzPatFaQgEOIR0H+8ea4G78UHWprKZpmZZATyTknNZSq\r\n" +
            " X2NKdK+56j4l/aN1XVrS5Sw8qzjVTlU+83/AjX5Rf8FLvG2qeJPjVaaTaTymNYfNkBJwzE96/R\r\n" +
            " 5Yo4bJ5blvk2Hg9Sa/M79uZppf2l7ma8XYjwL5fHYelZudvU9HC0U2eFeI7K4ivrLQdHmeW/1L\r\n" +
            " AmZeiKewr6e+Dfw9s/hn4OS00mIeewBnfHzOx6kmvnr4KQDX/jpLPdfP5AO3jpX1RpsOYgUPRs\r\n" +
            " YA615Ga4hq1O/Q+x4dwcZqVZrW+h6L4aPmWFuEyCR0zxW3bxvldicg5OKwfDUnl2keBggYya6S\r\n" +
            " wZGGQ5B6EdBXzk7S1PtKUe5taHcl2PysrKc9Kh+J3wX0n4seGprbWIFWRo8Ryonzxn1Bp1nEY3\r\n" +
            " wTg9iMkmui0vUBHCBKTnpwetZRvFpo2qJVYuMtj8+/jb+xf4i+H3hW8eILqUcNwTGI1YSRoe7D\r\n" +
            " 0ryrRra5Tw+bbXLGWCaIFVl24V8dm9K/VvVdHi8SW7cKhxnlRjPvXi/wAav2ZdI8beHp7Qwixv\r\n" +
            " /wDWJPHGF5/2iOOfWvdwWcOaUaiPk8bkcE3VpPVrY+A/Dnh19TguRD5Ezp80MYkwxHcY6561Md\r\n" +
            " de1vorWxdoXYiLJyDF7H8RXo+t/sb+KfA8st1YwG6CscGHn3ByPWvOdV+FfiBNfN3qljL5aMHb\r\n" +
            " apyPrivehXo1G25HzlTCYrDWtB/I7nUdF1zx1eafF4ii3OWBgaKBEQgKQSzdz+Ncp4FhE3iCXR\r\n" +
            " 9evhYpaStEZCu8A9Af61qeBfiBrVlqL2EN/dQsQwSFxwmASAfY4rm/DQv4PijDcSq4mmmWVsjg\r\n" +
            " nOTj2rO8kpRk0dblGU6UqcXd7mxrlvH8PdYtLTw7dRX0+oRi4WSKPBiJJBwew716Fp0Vh4e+BP\r\n" +
            " iC2mFvNq2vSAK7qUcEf3T1br6Yry/xNJa31/qEWuWs8Cus7QyoTlHzlOPTPGPeva/2aPgVrXxe\r\n" +
            " +GVjDHb7XsLhpxNNn50YAKM9+ULfjWONnCnCM3Lax2YCE51atNrS2nlfc4/9mvRZ9Nj1c3F0++\r\n" +
            " G0aEooyQSeQSR1GP516xon7NGp/GHw5aS+Dbc/aFdh512cRMgC5OP4sHOOO1fTP7Pf7Ilp4SsJ\r\n" +
            " 5NYtbS5vb1908ghBD5B+X/PrX018NfgvZ6faR/ZrJIo4B8g2hdg9CB0r5+rjp1KnNA9qlSp4ek\r\n" +
            " qL1Pn/APZw/YPg07w7po1iza6WzUyOoBClj/Fj3PFfanw8+H1l4N0SC2js4IduCEABXOOoqfw/\r\n" +
            " pCaNZBIUG0oBuBwSPetRpzOzDBQ9jngcdq4rOTbnqzqi3NpLYluZ9haEEAMDtwOlZ2owxSWkUk\r\n" +
            " 0asYzmMtyQeRu/XpV+wsVWFpLosBGOpOSf881j6lcfaXIiztTp6Yqk7HbGSeljjvg/+03Zar+1\r\n" +
            " nqXwh1Yw2119khvtPuC3+sZ03MjD88V7h4l0C88M3nkaxCyMeVbqrj1Br83f2ktVf4Tf8FPfhZ\r\n" +
            " 4t0j902sSW9lK3Yss4i/8AQZAPwr9nNS8HWfjTQIY9URMlM5xlh+NfX5LUdWjyX2PyTirDrD4t\r\n" +
            " zj9o+bHcKxz602RhvPPvXWfEf4PX3gy+Jtg89rJyj7en1rkZbC4jYh45Mgc8V6zjynzkW2AkDd\r\n" +
            " DQjh8jODUCSsf4gccdMZpfMGeevtSHclyY2wOc9zS78dyTUBkCYwDkdBnrTzjOWxn60AP8z5ea\r\n" +
            " ek25Pp2qNSc5WnyysE+QD39aBivx8w59j0pdwkUgHmmBwq4PO79KXzQDkkUAfHnwq/aP8Rf8KR\r\n" +
            " 8E2Oj3A0+C38P2MQKIMnFvGM5P0rTm+Iur6xN5mpare3DkDPzkIfwHFeI+B/HNlYfBjwkZxKqx\r\n" +
            " 6RZxttUkk+QnSu08E+OoL+GF44ZBHtOCRjP1rjc3c74UdrnTaxJLdnN8xZmPc5A+tc7dXkmmXR\r\n" +
            " ihddgbk54Ue5qtqHiqbVdUuYI5RAB054f6V5r8TNYvtI1RFSW4KRL85SQgNzyaOe+5sqfY9pbx\r\n" +
            " PELOazvVXZgAuDjgjqKoadZPDoVjdW0izQwznJU84Jri/AccWpOt9sLWskBHzNuIJGCa634Vaw\r\n" +
            " b601XRbthIYF8y3yeoHYVCdhumlsdbrsb32iW0cRbJkySPfpXxN/wVQ+Gn/COXuh67bqMtmGUj\r\n" +
            " tnpmvujw9Ct3ZQRtw2M854IrxT/got8L3+IfwKv5LOESTWQMq8c5XmnN7G2Hladj81f2etU+wf\r\n" +
            " F5w/BnBwPWvrTRCsz5hL5BGRmvh/Q9Xl8PeMra6i+WSKTB7dDX2d8MNdi1ywhuoHG25jByBwD6\r\n" +
            " V5Oc0LtVEfY8NYhKDpy3uen+GSJYsYwCOOOldJYD7MBvz7gdBXL+GX2SkS/NgfjXT253A+byD0\r\n" +
            " Br5t+7ofYUpXbNeM8LhyO/PWtGxmPlgDDYAAx1z71zf2x7WRQ5Iibj7ucfjWnpt6UkKyRkhOpI\r\n" +
            " wSPag2Oq029TG1lPzHHPFM1e2ivo99w4VQvQ8iqMV7vjEkT5Rh8vHUfWntdvJhGDFXz9RQly7G\r\n" +
            " Mo82hieJ9Kg07T0azbcW5MZYYP0Fcde+GtN1SMs1p9/kkKo/n712XiBJJYWaBkzGMAsMmsVQ6Z\r\n" +
            " SQIzHBwh5IreFRnO6SgefXXwA8MX/iM6obELeYALIAA2OM1pW3wL8MzatFefYLU3MKkLN0I9sd\r\n" +
            " 67rRvC0mpTCSL5Y93+r6kmux0D4Xx6tfiEW8pUDdkLwPxrZVJtayOSolG7sea6N+zv4b1raNR0\r\n" +
            " qC8icktujB3d/rXuvwv+D4hso4NFtre1tYgAiRxbUCjoMflxXT+Bfgx5MiQAFY1IY5GGx2ANeu\r\n" +
            " +GPDUdnCF2AbOAMYz+FRdvRs5ZTu7rQzfA3w6TR4kDjdjHP+16V3VjpQtoXUIQTycdvan6RabZ\r\n" +
            " XMrAKnRduAfcmrst0sibs5UEZOOfwrOSSdkaxhbUglWS3KpId3y5+50H4UiuAzRj5g5G1ieR/w\r\n" +
            " DWp902XLRKzDGDyAT+NRxSgyL9wKBgAetS9DojU5SWYvbw+UvQHnBz1rC1yQwacyoCpA4Ldzmt\r\n" +
            " m6lADCNtpx64ya5jxXMHgVCXGOee9Q3ctVHJpHwX/wAFCfFCaf8AtXfBLzUMssOtJIyr3X7RHx\r\n" +
            " /47mv3V+H0x1fw7YXRCpHNApRevBFfz4ftKanL8Vv+Covwu8PaYpuVt9Vt4tin7uXDE49gCfwr\r\n" +
            " +iH4f6THo/hixtEBAt4QoOPSvscipuME/I/OeLqkXWt1RH4m0db7TTHMgcbSADya8CsbU6b8VL\r\n" +
            " 2yuFUxM3yIRxivpLVIx5eIxyM9PpXhvi20+y/FmW7uYHWCXaN4HFe5UWx8lRd7opeLfgYuoRmf\r\n" +
            " Qv3Ez87QdqP/AIV5prnhq+8M3DRaxbyRMpwDjINfTunOn2ZfLIcFQKpeJdBg1iMx3UUcgYd1yR\r\n" +
            " +FDgmtCIux8whixBB4B5yOlKzZGFr22X4H6TfpJ50bxhskNGduK4Dx38Ir3woWntGN3Zj+JV5X\r\n" +
            " 6is3FotTucnE5UYzTzIRjf3qBW2oATyOtSZ/d8d6kskDBvu8inKoY4bGKhaTa48v7uKl8wHkDI\r\n" +
            " wKAPxXg/bv+FL/AAv8O6ZNrl1Heafp1vBOi6dccSJEqsN2zB5B5FbXwm/4KO/C/wANJJb+JNdu\r\n" +
            " hEgPlP8A2dcNn2wEr83aKp4SD7jWPqJWsj9TF/4KYfBSTU1luNfvNmOQNKuev/fusbx5/wAFCP\r\n" +
            " gprcLnTPEN7JJuJUHS7kcfjHX5mUU/qkAWPqJ30P0Y+GP/AAUT+GnhS7vLfVNfuhp8q5ixp1w2\r\n" +
            " 1vTASt7wl/wUs+E3hrx3DqC69ctAcq7HS7jIU9eNlfmTRS+qQ8xvMKj6L+vmfr/Zf8FZ/gZYyv\r\n" +
            " 5Xim92iTzFP9jXfX/v3TPE3/BVf4CeJNKv7K78UXzwXaEAHRrvAyOR/q6/IOih4ODVncSx9Ra2\r\n" +
            " R6V8XPFPhu4+ImozfD+9e70prlpLeQwPESpOfusARXpX7Pf7U2g+CLdrTxjeyw2y/NGRbvJg/R\r\n" +
            " Qa+a6KmtgqdeHJO9jrw2eYjCT9pBK/z/zPvrSP28PhtaTBptauRxg/6BP/APEV0lp/wUO+FITF\r\n" +
            " xrt0Ppptx/8AEV+cNFec+HsK3e8vvX+R7MeN8fHaMPuf/wAkfo4f+ChvwqLHdrty6sOQdNuDz/\r\n" +
            " 3xT7L/AIKJ/C21dc+ILsoh4B064z+eyvzgopf6u4XvL71/kX/r1mH8sPuf/wAkfpbD/wAFI/hT\r\n" +
            " BIRHr9yEbkj+y7jj/wAcq7B/wUq+ESM/meIbzaRx/wASy5/+Ir8xaKP9XcL3l96/yJ/15zD+WH\r\n" +
            " 3P/wCSP0xn/wCCkvwluVKy65dHHf8As245H/fFRWn/AAUW+EEVzvk1y7IAxzplxz/5Dr806Ka4\r\n" +
            " fwy6y+9f5CfG+PatyQ+5/wDyR+rXhr/gpz8DdMhD3XiG8Dn70f8AZN0d34+XXpXhz/grz+znp1\r\n" +
            " uq3fivU92P+gJd8flFX4t0VX9gYfvL71/kYz4xx094x+5/5n7j2P8AwWm/ZytEGPGGo7h0/wCJ\r\n" +
            " DeHj/v1W1a/8Fx/2cLXOPGWosSOv9gXvH/kKvweop/2Fh+7/AA/yMv8AWvGfyx+5/wCZ+9S/8F\r\n" +
            " 0P2bVRceMtSyw+cf2Dfc/+Qqrt/wAF0P2cnk2jxfqKRkYONCvf/jVfg7RS/sDDd396/wAjT/W/\r\n" +
            " G/yx+5/5n7yXH/Bcj9mxpEMfjLUioXkHQL3r/wB+qE/4Lkfs2iJlPjLUh6Y0C9/+NV+DdFS+Hs\r\n" +
            " M+svvX+QLjDG/yx+5/5n7xyf8ABcr9nCaPa/jHUhz1/sG96f8Afqua8T/8Fs/2f72ynOn+LNSk\r\n" +
            " mZTsX+xLwc9hkxV+INFT/q7hv5pfev8AIf8ArjjukY/c/wDM/RD9iv8Abi+D+if8FJh8VP2kPE\r\n" +
            " dzpnh7R0mnsGXSrm6aaZgEUFI0Zlwu45IFfrxp/wDwcx/se2qIrfETWcKuP+RW1L/4xX8u9Fev\r\n" +
            " hsNDCx5IHhY/MKuY1PaVbX8v6Z/UHqX/AAc1fsh+XK9p8QNYlkP3V/4RfURn84K5C0/4ON/2Sb\r\n" +
            " yweHVPGurI08heRz4av2A54A/c9hX81dFbtJnJCbhsf022f/ByN+yDZQqkXj/VsIOP+KX1H/4x\r\n" +
            " Tb7/AIOTP2SJWUweP9WBHf8A4RjUQR/5Ar+ZSii2lhczP6ZX/wCDkT9kd4wD4/1cE9SPDOo//G\r\n" +
            " KJ/wDg47/ZCu7Vo5/H2qncMYPhfUcEe/7iv5mqKOUOZn9Bnjf/AILk/soXN48/hTx9qe1znyj4\r\n" +
            " b1Ac+xMNc9/w/P8A2bQBjxpqR9f+JBff/Gq/BSipdNMpVGj96x/wXR/Zs7+M9S/8EF9/8ap//D\r\n" +
            " 9T9mwR4XxpqYOf+gBff/Gq/BGil7KIe1Z//9k=\r\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Doe", contact.getSurName());
        assertEquals("John", contact.getGivenName());
        assertEquals("Richter James", contact.getMiddleName());
        assertEquals("Mr.", contact.getTitle());
        assertEquals("Sr.", contact.getSuffix());
        assertEquals("Mr. John Richter James Doe Sr.", contact.getDisplayName());
        assertEquals("Johny", contact.getNickname());
        assertEquals("IBM", contact.getCompany());
        assertEquals("Accounting", contact.getDepartment());
        assertEquals("Money Counter", contact.getPosition());
        assertEquals("john.doe@ibm.com", contact.getEmail1());
        assertEquals("905-555-1234", contact.getCellularTelephone1());
        assertEquals("905-666-1234", contact.getTelephoneHome1());
        assertEquals("905-777-1234", contact.getTelephoneBusiness1());
        assertEquals("905-888-1234", contact.getFaxHome());
        assertEquals("905-999-1234", contact.getFaxBusiness());
        assertEquals("905-111-1234", contact.getTelephonePager());
        assertEquals("Silicon Alley 5", contact.getStreetHome());
        assertEquals("New York", contact.getCityHome());
        assertEquals("New York", contact.getStateHome());
        assertEquals("12345", contact.getPostalCodeHome());
        assertEquals("United States of America", contact.getCountryHome());
        assertEquals("Street4" + NEWLINE + "Building 6" + NEWLINE + "Floor 8", contact.getStreetBusiness());
        assertEquals("New York", contact.getCityBusiness());
        assertEquals("12345", contact.getPostalCodeBusiness());
        assertEquals("USA", contact.getCountryBusiness());
        assertEquals("http://www.ibm.com", contact.getURL());
        assertEquals(TimeTools.D("2012-06-06 00:00:00", TimeZones.UTC), contact.getBirthday());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
    }

    public void testImportVCard11() throws Exception {
        /*
         * https://github.com/mangstadt/ez-vcard/blob/master/src/test/resources/ezvcard/io/text/John_Doe_LOTUS_NOTES.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "PRODID:-//Apple Inc.//Address Book 6.1//EN\r\n" +
            "N:Doe;John;Johny;Mr.;I\r\n" +
            "FN:Mr. Doe John I Johny\r\n" +
            "NICKNAME:Johny\\,JayJay\r\n" +
            "ORG:IBM;SUN\r\n" +
            "TITLE:Generic Accountant\r\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:john.doe@ibm.com\r\n" +
            "EMAIL;type=INTERNET;type=WORK:billy_bob@gmail.com\r\n" +
            "TEL;type=CELL;type=VOICE;type=pref:+1 (212) 204-34456\r\n" +
            "TEL;type=WORK;type=FAX:00-1-212-555-7777\r\n" +
            "item1.ADR;type=HOME;type=pref:;;25334\\nSouth cresent drive\\, Building 5\\, 3rd floo r;New York;New York;NYC887;U.S.A.\r\n" +
            "NOTE:THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\\nAND ANY EXPRESS OR IMPLIED WARRANTIES\\, INCLUDING\\, BUT NOT LIMITED TO \\, THE\\nIMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR P URPOSE\\nARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTOR S BE\\nLIABLE FOR ANY DIRECT\\, INDIRECT\\, INCIDENTAL\\, SPECIAL\\, EXEMPLARY\\, OR\\nCONSEQUENTIAL DAMAGES (INCLUDING\\, BUT NOT LIMITED TO\\, PROCUREMENT OF\\n SUBSTITUTE GOODS OR SERVICES\\; LOSS OF USE\\, DATA\\, OR PROFITS\\; OR BUSINESS \\nINTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY\\, WHETHER IN\\n CONTRACT\\, STRICT LIABILITY\\, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)\\nA RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE\\, EVEN IF ADVISED OF THE\\n POSSIBILITY OF SUCH DAMAGE.\r\n" +
            "item2.URL;type=pref:http://www.sun.com\r\n" +
            "item2.X-ABLabel:_$!<HomePage>!$_\r\n" +
            "BDAY;value=date:1980-05-21\r\n" +
            "PHOTO;ENCODING=b;TYPE=JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/4QBARXhpZgAATU0AKgAA\r\n" +
            " AAgAAYdpAAQAAAABAAAAGgAAAAAAAqACAAQAAAABAAAAyKADAAQAAAABAAAAyAAAAAD/2wBDAA\r\n" +
            " IBAQIBAQICAQICAgICAwUDAwMDAwYEBAMFBwYHBwcGBgYHCAsJBwgKCAYGCQ0JCgsLDAwMBwkN\r\n" +
            " Dg0MDgsMDAv/2wBDAQICAgMCAwUDAwULCAYICwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCw\r\n" +
            " sLCwsLCwsLCwsLCwsLCwsLCwsLCwv/wAARCADIAMgDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEA\r\n" +
            " AAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhBy\r\n" +
            " JxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZ\r\n" +
            " WmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxM\r\n" +
            " XGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAA\r\n" +
            " AAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFE\r\n" +
            " KRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVm\r\n" +
            " Z2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyM\r\n" +
            " nK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9/KKKKAIN59T+dG8+\r\n" +
            " p/OkorkuzMXefU/nRvPqfzpKKLsBd59T+dG8+p/OkqC+u/skZYkDAzyaLsCUyNk/MfzrkPihq9\r\n" +
            " 3p9s5sLq4gIjQ5jkK878djWd4p+L8Wg3TI91aJgnhp0XufUe1eY/FX9oSCWCQfbrD/AFaj/j5j\r\n" +
            " /v8A0q0xn5C/8Fm/20vjH8LvGjRfDP4s/Ezw7Fx8ml+KL6zX/V2Z6RSju7/99H1NflT4i/4Ktf\r\n" +
            " tRwaxMsP7Sfx9RRtwF+IOrgD5R/wBPNfcv/BajxwniLxozLNC/T7sit/yzs/T6V+TviPD6zMcj\r\n" +
            " nb/6CK6YbHZBaHuH/D1/9qX/AKOV+P8A/wCHC1f/AOSa2vAH/BVT9qC98SRJeftIfHuVDjKv8Q\r\n" +
            " NWYH5l7G4r5nZdtbnw7k8vxNET7f8Aoa1omg5T+hz/AIIe/tYfFP4s+V/wtT4leP8AxNnbn+1f\r\n" +
            " EN3e5/48v+esjf33/wC+j6mv2a+Hl7Ne6buvZpZm8qM5dyxyQc9a/n4/4IYfEZPDXk7ri3j+79\r\n" +
            " +VV/58vUe1ftR8Nf2h4V07Bv7D/VRf8vMXofasJbmM9z6Aorh/CHxUj8Rz7VubaT/cmRvX0HtX\r\n" +
            " awTCZMgg/Q1NyAlYjOCagMjZPzH86ml71AQcnitqRtHYVZGLcsfzp+8+p/Oo1B3U+rY7IcrEsM\r\n" +
            " k9fWpqgT74+tT1EjOYUUUVJAUUUUAQ7DSFCBT6R/u1lZGTQyiiisWSKBuNcV8Zdfk8P6HPJA20\r\n" +
            " rAzdf9l/celdsn3q80/aTJHhW7x1+yv/AOgSVdhrU/HX/gpL/wAFIPEfwl+I+oWmk6hLEsTyAA\r\n" +
            " Tyr0luB2nX+4K+GPFX/BY7xhf3c6SatOQGK/8AH1P2Y/8AT1Wt/wAFqJ7hfjNq/lSzL+9m6MR/\r\n" +
            " y3vK/MjU7q7/ALVucz3P+tb+NvU1eljVHun7SX7VOqfGbUvO1m5kmJ7mR27RjvI39wV4LesLi5\r\n" +
            " Z26nH8qQySsP3ryN9STThCXPAJNZuTR30o3RXkG081No982n3wljOCP8Qf6VoWvhebUMbEkOfQ\r\n" +
            " H/CrX/Cu7oDIjnx/un/4mqVVdWU4nv8A+y3+2trXwU2/2LeSQ4x0lkX/AJ5f3ZV/55ivp/wZ/w\r\n" +
            " AFk/GVveRxLq8+GXH/AB9T9lP/AE9V+at/o11o3Rp0/Ej+ntT/AAzqV0uuQg3E/wDF/wAtD/dN\r\n" +
            " PmuYSjqf0ff8Evf+CgGv/GLxQsOsX0soPrNI38Fye8zf3BX68/D/AFd9V0svK2SP8W/wr+cv/g\r\n" +
            " hbeTS+O18yWRuvVif+Wd7X9EnwgJOhNn2/9Cen5mEjrmO7rSbB70tFaRdkJNoaUx0ptSHoajrR\r\n" +
            " O5SYqffH1qeoE++PrU9KQpBRRRUkhRRRQAmwe9J5Yp1FLlQWIivPNG2nHqaSiyFZBjFea/tGoG\r\n" +
            " 8L3ef+fV//AECSvSz1rzb9on/kWLr/AK9n/wDQJKTWhB/N1/wWX0pJvjFq5YDmWb/0feV+aera\r\n" +
            " FH/aVycL/rW7e59q/Tz/AILHDPxh1bP/AD1m/wDR93X5tanFv1K4AHWVv5mvNqVGmdUYqyORvt\r\n" +
            " PSBsADFP0PT1ubsq+MD/A1vS+E5r85hOB9B/j7Vf8AC/w6uhekluPoPQ/7VS6mm56NKOiPa/gH\r\n" +
            " 8EdO8TtbfbI4zvRicqv/AD1A7oa92b9kvQ/7G8wQwZx/cj/u/wDXOqH7Kfg5rdrLzxnEb/8Ao8\r\n" +
            " e9fUT6BGvho/J+p/ufWvnMXjJwnZM6lTTVz83f2pPg/YeDt32BEXr0Cj/nr6KP7or54sIfI8Rw\r\n" +
            " hf7zj9DX2r+3pYLb79o9e/8A13r4wX/kZoP9+T+Rr6PLajqU02cNaNmfrz/wQk+bx4ue2f8A0X\r\n" +
            " e1/RX8IEH9hN+H/oT1/Op/wQjH/FeL+P8A6Lva/os+EH/ICb8P/QnrvZwVNGdbsFGwUtFOLMxC\r\n" +
            " gNJ5Qp1FaxYXGiMA06iimAUUUUAFFFFADfMNHmGm0UANZ/mo3/Wkf71JV2RdkS59a82/aKP/AB\r\n" +
            " TF1/17P/6BJXpFecftDxmTwxdAf8+r/wDoElRIiyP5x/8AgshJj4wat/11m/8AR93X536Zp41H\r\n" +
            " Vpw3edx/Wv0c/wCCxGgS3Xxd1Uof+Ws3p/z3u/evgTwl4SmOsTZbn7RJ2Hp9a8mtHVnRDQ+r/w\r\n" +
            " BkX9jGy+KWkCW6HJ92/vSjtKP7lfUGjf8ABLXS4LdJAOue7+pH/PxW1/wTB8IA+GAZ1yfr/t3P\r\n" +
            " vX6CWPg6H+yIsxc8/wAR/vfWvmcXiakJuKPVo25T4V8Mfsf2ngLb5Ax5YK9W7nd/z1NaviP4ep\r\n" +
            " ZaI6oOBn1/un/ar6Z8d+Egok8pdvI75/h+teY+N/CzrpcmPf8Ak3vXlSnOcrs3ukj8tv8Agor4\r\n" +
            " eFnv2+//ALce9fB8tvs8UQdfvy/yNfov/wAFMtEeEybvf/2596/PW6sWXxTBz/HL/I19jlcmqa\r\n" +
            " PLry97c/WT/ghWvl+Ol/H/ANF3tf0SfCCY/wBhN+H/AKE9fzuf8EOXFt44Unnr/wCi72v6FfhF\r\n" +
            " q6Lobgj0/wDQnr1k7nDJ3Z3qSlm5qSqNrqCyyDHerobcMitoK5ItFFFabEthRRRQNBRRRQMKKK\r\n" +
            " KAI6KKKuxdhpTJo8v3p1MmuFgGZDTGPrifjRpv9o6DcJ6wMP8Ax1/8a6C58bWFqxE0mCP9pf8A\r\n" +
            " GuV+IXj7TbnTZVSUEmMj7yejf7VZ7mZ+Lf8AwUw/Zg/4TL4k6jNt+/JIenrLcn/nmf71fIHhf9\r\n" +
            " h4Jqcp2Hmdz09v+uNfql+2Bo8Ov+L7uS3XcGZ+wP8AHL7H1rwzw78PsXzHyv8Alq38Pt/u1jKi\r\n" +
            " pGiZ61/wTs/Zb/sPw6FKfp/tXH/TP3r7i074C7tMjBXpnt7/AO5Xnf7EvhlbTQhvTH4f7U3tX1\r\n" +
            " tpunRCxQY9ew9TXh4jAwlNto6YVpJHzf4i/Ze/tHd8vUjt7Y/551w3jz9kHGiSNtPft/st/wBM\r\n" +
            " q+zm0iFuq/oP8Kw/HuhQN4elwvPPYf3W9q5Hl9PsX7dn8+v/AAVv/Z8/4R3zePXt/wBfX+wPSv\r\n" +
            " y81v4e/ZvE0Jx0kl/l/u1+5H/BaPwg935v2RP73Rf+vz0FfkV4p8A3g8RRkRdJJf4W/wDia9fD\r\n" +
            " U1TikjlqVG3qfY//AASLv/8AhEfFqv06/wDoF39P71ftv8Lfjz5GllS3X/Fv9uvxA/4J7Wknh7\r\n" +
            " xCGuxsH0I/hn9cetfp38PviDbRWu1pef8AeHq3+1XbBGDZ9q+Cvix/bOoRJn73/wAUB/ePrXqG\r\n" +
            " mX32qIH1Gf5V8ofBTxfDfa9arHJnOP4h/wA9F96+o/C8wms0I/uD+QreKBGsZOKb59B6Go60Su\r\n" +
            " UkmSibJHvT6gT74+tT0NWBpIKKKKkQUUUUAVPtH1oFxk96iorQ6uRFkScVxXxZ8df8Inp80mcb\r\n" +
            " MfqUHqPWuzX7teNftTzPFoV15Qz9z/0KKgztqfnl+0j/AMFfv+Fba1fw+bjyHjH3/WHf/wA/Ir\r\n" +
            " xbQv8AguL/AMJlqrWnm53S+V9/3A/5+z/er4d/4KIeK9Wt/GOuC2g3ASwY+dB/y5j1FfLXwK8Y\r\n" +
            " 61P46AktsD7fj/WR/wDPRPamopmVj+iL4I/8ZQ6JDqY+b7SFb1+8qN/t/wDPX1/+v6/on7EBRI\r\n" +
            " 5Nn3xv6eo/6415L/wR3spNW+EOlPqC7GMUOeQf+WFp6fU1+kmk+FIBptsc/wDLJex9B71LRLk0\r\n" +
            " ecfA74Mf8IRYhNuP/wBb/wCwP71evW9r5UIXHSm2WmpajCVbAxWEqMW7sFNorNFiqHiKw+3aY8\r\n" +
            " Z75/ka036VDcIHjIasHQiS6sj4V/bv/Y3/AOFyb/lznPQZ/wCe/wD0yb+/XxH4q/4JG/vTP5f3\r\n" +
            " HY/d9SP+nav2R8eeF4dT/wBYcZ+v+17+9ee+KvhnanSJju5yOx/vD/apcqiTz33Pxh+JXwu/4Z\r\n" +
            " KtvtZG3Hrx3Uf3U/57f57+ZW//AAVP/sLV47cOPmz/ABf7Of8An4HrX0z/AMFj9LHhzwczW3zH\r\n" +
            " j2/jtPXPrX4d+LPiBdW/jO2AXj5u4/55j/ZrWIXuz98/2D/29/8AhY/xD0i1L/6/H8X/AE9Rr/\r\n" +
            " z2PrX64/CLXf7b0SCTP3oVb/x1Pc+tfzW/8ElvHjT/ABm8OrcnaDtz3/5f4fRa/oq/Zz8UWjeG\r\n" +
            " LXzJufsqfwN/cj9q2iaI9YPQ1HUEWtW1xxDLkn/ZP+FTBgw4rSJpHYcn3x9anqBPvj61PSkKQU\r\n" +
            " UUVJIUUUUAUfL96UR81LSMwXrU8503YKMDmuA+NXg1/FOmzxxdX2/zT3HpXdtexIcM36GobiSC\r\n" +
            " 6GGfr7GjnZnqfiZ+1Z/wSk1b4h+INSmtxxO8ZHKdoAve5FeL/DL/AIIp63oPiUTyLx9q8zrH/f\r\n" +
            " U/8/Z9K/fu++H1lqEjFn+8fRvp/eqCH4UWUL7lbvno3/xVaKZJ85fsA/s13Pwc8BWNneDDRJGD\r\n" +
            " 07RwDtI39yvr2yHlWcKn+FFH6Vn6NoEWlxhYTkAe/t7+1aQGBVGLZNEcg06mQ/dp9Q9xETDIqN\r\n" +
            " huFTP96k61m9RNGJremm5+7/nrXM+KPDrnRZzn+7/6EPeu7lQNmsvxPCP7Dn/4D/6EK5pSsydj\r\n" +
            " 8Y/+C3mkNB4IYt7f+jLKv5//AIjN9j8WQux+7u/9AWv6Gv8Aguam3wM34f8Aoyyr+eH4vnGvL+\r\n" +
            " P/AKClVGVwufTX7D37UFr8JPiDpV7dn5bfGev/AD8xv2jb+7X6t/CH/gtromg6fawSNz5aR9JP\r\n" +
            " RB/z6H0r+eiz1+XS2Dw8lfp659K19H+LV9FqVsFT/lqvdfUf7NdEHdGsHdH9Vn7Kn/BUbS/jJ4\r\n" +
            " ptbOzbJldB0fu8Q726/wB+vvDwN4vTxHDCY/8AlpGr/mpPoK/mT/4I0fEK61b4w6Qky4Blh7j/\r\n" +
            " AJ72f+z71/Rv+zxOZrKwLd7WE/8AkI1adjWL0PW0++PrU9QJ98fWp6G7ikFFFFIkKKKKAKHnVn\r\n" +
            " 6trqWSN5pA2/T1x61Pv9/1rjfHt20UU+0njHf/AG6+V/tSR3qmcf4t/aO0nQ7xo7mWIEY6snoP\r\n" +
            " 9v3rO0b9qTRdQlAimhJP+0nv/wBNPavkH4/eMLm28RTCNp+NvQn+7H71x3w28bXUt7EHa4PXuf\r\n" +
            " RverjmTua/V1a5+o/hbxTBrcMb25B3gngjs2PWujjhLAHnnmvHfgPqTXOj2Rdjkq/U8/62vZra\r\n" +
            " QGCM5H3R/KvQhjG0cU42dh0cZFP2GnKQRxilrqjinYx5EIsnljBpftAqKb71MrpjNyVyGkifzN\r\n" +
            " 2TSbj7Uxfu0tRKRDeoMeuazPE//IEn/wCA/wDoQrRc1neJ/wDkBz/8B/8AQhXDVqalKNz8fP8A\r\n" +
            " guhj/hBWxnt/6Msq/ng+L4/4ny8+v/oKV/Q9/wAFz/8AkRm/D/0ZZV/PB8X/APkPL+P/AKClbU\r\n" +
            " ZXRDVjj5RuUgU3S7c/2rb5/wCeq/zFSYzVjSk/4mltx/y1Xt7iuhSsa01ofpd/wRVgK/GbSCP+\r\n" +
            " e0P/AKPs6/pO/ZzQ/YrD/r1h/wDRTV/N1/wRZXHxm0fj/lrD/wCj7Ov6SP2dDiysM/8APrD/AO\r\n" +
            " imrjr4t03ZGqietxqfMX61aqBCN4571PW+DruvFtimrBRRRXYQFFFFAGD5orkvHMBmhnwM5x/6\r\n" +
            " HXT1g+J13xS/h/6FX8//AOsNU9qMLs+CPjr4HnvPEExRCQcdj/dT2rjvAPgK4s7uMsjcZ7H0b2\r\n" +
            " r3/wCKWmiTV5DsU9Ow/urXLaFpgilXCLx7D3rpp8QVG0erDDJxPon4M6xHpel2iytgqrZ/7+Zr\r\n" +
            " 1qy8a27RoPMHQDqP8a8F8ITeVbxAEjAPT/ersdMviSnzN2HU+1fT4bOJySOKpgYns2maot5GGj\r\n" +
            " OQf/rVeEmetcr4Ln3WCHJPA/kK6RWJUcnpX0eHxkpxTZ5FWmoysOmcbqZvHrRN9+mV7lGreKOZ\r\n" +
            " xJVkGOtKZRUNFaORHKSs4bpWf4n/AOQHP/wH/wBCFXY+9UvE/wDyA5/+A/8AoQrx8TWcZ2NIwV\r\n" +
            " j8fP8AgugMeBWz7f8Aoyyr+eD4vKTrq49//QUr+h//AILpH/ih3/D/ANGWVfzxfFtSdeTn1/8A\r\n" +
            " QUrvwk3KNy1RTOYt7NpnAUVq6P4flbUbc7D/AK1e3uPaotEj3TqOp/8Ar12OhW3+m23A++vb3F\r\n" +
            " Y4nEuk7I6qWHVj9Av+CM2lSW3xj0guv/LWH/0fZ1/Rv+z1IEsrDPa1h/8ARRr+eX/gkBDs+Lmk\r\n" +
            " nA/1kP8A6OtK/oT+ADYtLLP/AD7Rf+izXymZZtOnIcqKR7DDODKo9SKvVkWz5uY/94fzrXr3uF\r\n" +
            " sZLGUqjl0a/I5a8eVoKKKK+pMAooooA8d/4Wpaf3/0b/4msnXfiXaTRvh+uOx9f92vmf8A4S2T\r\n" +
            " +/8AoP8ACmv4pkkGN/8A46P8K/jJZxI++WVJdTpPHOrpeag7JznHY+i+1c9pt0I3Gc/5zVCa+M\r\n" +
            " 7ZJyTUXnFCDmvRo5m9GdSwqirXPVtC8VxQQICegPY+v0rVl+JNtpyBpXwAM9D/APE+1eJDWGiJ\r\n" +
            " +b9P/rVy3xM8Uy22lTFX6RMeg9G9q+tweavRWOeeFTPftS/bS0LwfIYb662MnBHlSHpx2iP92k\r\n" +
            " 03/goT4au2wl7yCR/qZfT/AK4V+JX7bnx1utF8a3iRz7QHf+AH/lpMP7ntXg/gH9pO8l1Ng1z/\r\n" +
            " AMt5B/q1/u/9c6/Ssur89JM8Gvg1zM/qc+D/AMcbD4h2vm6XL5g/3GHd/VB/dNeowEzRBlBwa/\r\n" +
            " ML/gkJ8QJvEnhFWmk3df4QP47v/ZHpX6daA/maRET3z/6Ea+loVfdR41aHJJol2H0oZCB0NWV+\r\n" +
            " b71MuQFiJFdPtLoxsZd9qi2mfNOPw+tY3iDxLE+lTLu5OOx/vD2rO+Il2Yhwcf5auB1HUi0bAt\r\n" +
            " 1Pp7183jsTyzZ20qPMj4Z/4K5fDu48f+EWj05N5OO4H8dr6sP7tfij8Sv2J9dvdYV47X1/5ax/\r\n" +
            " 3V/6a1/R18ZfC0fiO0KzLv8AxI7p7j0rwnVvgNazzgm36f8ATQ+3+3XnS4glhvdSO6nhU0fhTp\r\n" +
            " f7EevQyqWtOn/TWP1/6610mkfsca1DdwM1rwHXP72P1H/TWv2kl+AdqqnNvj/gZ/8Ai6h/4Uba\r\n" +
            " p/yx6f7Z/wDi68rE8SSk9jrp4VHxv/wTk+DV98PviLYT6jFsVHjP3lPSW3PZz/dNfsR8Ivihaa\r\n" +
            " Vb2gmkwVgjU8HshH92vkiz8CxeGnEkMe3bz94n+p9K0Y/Fctlwr4A46D/CvncZmjryuW8GmfeX\r\n" +
            " hr4v2Op+I9PtopcvcXMcSja3JZgP7vvXrtfm78DPGkt18bfB0TPkSa5ZKeB3nQelfpFX6JwBV9\r\n" +
            " rh6z/vL8jxszo+ylFBRRRX355gUUUUAfmjk+opwORVTcfU1JG2Rzmv4gVCx+rJ3Lsa+vWlZC1S\r\n" +
            " W67gOlTmIdq6Iy5ClG5mSRZB4rk/iVYPcaVMEQsTE3QH0au5aAZqjrWmpdQMJFDZXHI9jXqUcy\r\n" +
            " 9m1oN4e6Pym/bU+FGpaz4zu3tLGaRWd+RHIf45vQe9eGeAfgTrEWpMW0y4H7+Q/wCql/u/7tfr\r\n" +
            " H8Svg3p+uanI9zZRSFieTCp7t6qfWsDw3+zzpUNxuGm24+dj/wAe6en+5X6Dl3EXLCMbHl1sFf\r\n" +
            " W57T/wSI8HXegeEVW9t3iPP3lYfx3fqPev048P/LpEIP8Atf8AoRr5O/Yt8DW3h3RglrAkY9kA\r\n" +
            " /im9APWvq7TmKWiDsM9PrX6Ll2Ye2pKVj4/HUeSo0aG4etV9TuVgsmZ2AHr+Bpdx9TWL48uWtv\r\n" +
            " D0rISCM9/9lq9eNbmRxch4Z+018ZNL8Hbv7U1CCDGfvSRL/wA9P7zD+6a+dNV/a78NRz7W12zy\r\n" +
            " WYD/AEi3/wDi6+dv+C1Hxz1LwP5v9m309vjd92Zk/wCfz0cf3RX5C+Kv21vEMfiKJBrl7gySj/\r\n" +
            " j8l7f9ta56uX/WPePSoR0P6BLP48aP4tOzTdTt52/2ZYj/AOgsfQ1oLqCXIzGwI/Cvym/4J8/t\r\n" +
            " E6r418QBNQ1K4nHo1w7fwz+rn+7X6a+Cb9rrTy0hJx7+7V8FnVH6tV5T1qFO6OhlYHkVXkXdnF\r\n" +
            " OD8806NQzD614ipe1N37pm3+nPdRkIpYn0BrKm8JXD9LeQ5/2W/wAK9F8MaYl5dKroCCR1HuK7\r\n" +
            " ez8GQOFzBGeP7g/wo+o8/UXOeUfA3wrcW/xr8HyPA4Eet2TE7W4xOntX6P18r/DzwfBa+P8AQ5\r\n" +
            " UhQGPULdgdg4xIp9K+qK/TOBaHsKFZf3l+R4Oby5px9Aooor7o8cKKKKAPz0/4V1d/8+0v/ftv\r\n" +
            " /iacnw4u+1tL/wB+2/8Aia+r/wDhX9l/z6x/98J/hQfAFkOlrHn/AHE/wr+O3hT7lZn5HzFbfD\r\n" +
            " +7CD/R34/6Zn/4mm3vg25tUJaFxj1Q/wCFfUI8C2gH/HtH/wB8p/hXM+PvCdtbWkhigQYx/Cvq\r\n" +
            " vtXJUo8pvTzLmaVj5S1fWE0+R1ldVK+px2z61lTeLoJcqsqHt94f41z/AMZtVew1S7WJyoDIOp\r\n" +
            " /5557V5tpniieW+ZWmcjzMdW9R7140q/LOx9TRj7SNz1e8hOpyF4huB9Of89asaJoEolBMbdT/\r\n" +
            " AAn0+lWfhnEt/psbTDeSB1+i+v1r0TRtEhIX92vPsPSvpcFitEc1WkeyfszWjW2nAMpH4e8le/\r\n" +
            " 2b4t1/H+dePfBC1WGxyigfQe7167bORCK/ZshrXw8fQ+AzKFqrRcEnFY/jqM3Ph+VEBJOe3+y1\r\n" +
            " aQf071HeRi4tysgyD/hX1FOfu3PN5T8c/wDgs9+z/qnxCMv9ladcXGd33IHf/n89EP8AeH51+R\r\n" +
            " 3ir9g3xNJr8cg0DUCBJKc/YZe//bGv6pvjB8ENI8df8hjTre5/34om/v8A95T/AHj+deTar+xV\r\n" +
            " 4UkVnbw9Ykg5z9mte5/3Kwq519X92x0058uh+Ln7Bv7O+q/DnXhJq+m3Nsvq9u6fwz+qD+8Pzr\r\n" +
            " 9HPBmsx2dkUlkAJ/2h6n3rr/jJ8CNI8BWZk0XTLe2b1SKJe6f3VH94/nXiE+ry2eoIiSMoOeMn\r\n" +
            " 0r4TOcb9brcx7uD96Nz3DQ5Rq1wiW/zlvTnviuusPAd1cKpW3c554jP+FcR+z2/9o+JLNLk7w2\r\n" +
            " OvP/LVR3r7K8GeCrOexjL28ZJQH7q+g9qWDpc6OfFVvZux4n4S8CXUF4pe3cAY/wCWZ9R7V39h\r\n" +
            " 4YmVRmJ+g/hP+Feo2XguzifK26D/AICv+FaUfhu3Uf6lP++V/wAK9alhbo4njDzbwjoEsPizS3\r\n" +
            " aNgEu4iTtPGHHtXutc3Y6HDDewusSAq6kHaOOa6SvuuF6XsqVRea/I8zG1fayTCiiivqDiCiii\r\n" +
            " gDl6KKK/ktnuEdch8Tv+PJ/w/mtFFebW2Z1UPjR+dPx+/wCQve/7yf8AoqvJtF/5CJ/66/1FFF\r\n" +
            " fJ1vjP0XB/w0fRnwa/5A8X0X/0FK9X0j/Vp/ntRRX0GC2RNbZnuvwc/wCPH/Pq9ep2/wDqhRRX\r\n" +
            " 7ZkH+7R9D88zT+PIl/u049DRRX1lP4TzBF/pUGp/8ejfh/MUUV8tmX8Rh1Plv9sj/kCn/P8AFD\r\n" +
            " Xw5rH/ACGE/H/0EUUV8zifiPocB/DPeP2Xv+RvsPw/9HpX3v4C/wCQbH/1zH8loor18u2PPzH4\r\n" +
            " joovv1NRRX0FHY8mQ6L/AFq/UVeoor7Hh7+HP1Oer0CiiivoTE//2Q==\r\n" +
            "UID:0e7602cc-443e-4b82-b4b1-90f62f99a199\r\n" +
            "X-ABUID:0E7602CC-443E-4B82-B4B1-90F62F99A199:ABPerson\r\n" +
            "GEO:-2.600000;3.400000\r\n" +
            "CLASS:Public\r\n" +
            "PROFILE:VCard\r\n" +
            "TZ:1:00\r\n" +
            "LABEL;TYPE=HOME,PARCEL,PREF:John Doe\\nNew York\\, NewYork\\,\\nSouth Crecent Dr\r\n" +
            " ive\\,\\nBuilding 5\\, floor 3\\,\\nUSA\r\n" +
            "SORT-STRING:JOHN\r\n" +
            "ROLE:Counting Money\r\n" +
            "X-GENERATOR:Cardme Generator\r\n" +
            "SOURCE:Whatever\r\n" +
            "MAILER:Mozilla Thunderbird\r\n" +
            "NAME:VCard for John Doe\r\n" +
            "X-LONG-STRING:12345678901234567890123456789012345678901234567890123456789012\r\n" +
            " 34567890123456789012345678901234567890\r\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Doe", contact.getSurName());
        assertEquals("John", contact.getGivenName());
        assertEquals("Johny", contact.getMiddleName());
        assertEquals("Mr.", contact.getTitle());
        assertEquals("I", contact.getSuffix());
        assertEquals("Mr. Doe John I Johny", contact.getDisplayName());
        assertEquals("Johny,JayJay", contact.getNickname());
        assertEquals("IBM", contact.getCompany());
        assertEquals("SUN", contact.getDepartment());
        assertEquals("Generic Accountant", contact.getPosition());
        assertEquals("john.doe@ibm.com", contact.getEmail1());
        assertEquals("+1 (212) 204-34456", contact.getCellularTelephone1());
        assertEquals("00-1-212-555-7777", contact.getFaxBusiness());
        assertEquals("25334" + NEWLINE + "South cresent drive, Building 5, 3rd floo r", contact.getStreetHome());
        assertEquals("New York", contact.getCityHome());
        assertEquals("New York", contact.getStateHome());
        assertEquals("NYC887", contact.getPostalCodeHome());
        assertEquals("U.S.A.", contact.getCountryHome());
        String expectedNote =
            "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"" + NEWLINE +
            "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO , THE" + NEWLINE +
            "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR P URPOSE" + NEWLINE +
            "ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTOR S BE" + NEWLINE +
            "LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR" + NEWLINE +
            "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF" + NEWLINE +
            " SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS " + NEWLINE +
            "INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN" + NEWLINE +
            " CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)" + NEWLINE +
            "A RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE" + NEWLINE + " " +
            "POSSIBILITY OF SUCH DAMAGE.";
        assertEquals(expectedNote, contact.getNote());
        assertEquals("http://www.sun.com", contact.getURL());
        assertEquals(TimeTools.D("1980-05-21 00:00:00", TimeZones.UTC), contact.getBirthday());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
        assertEquals("0e7602cc-443e-4b82-b4b1-90f62f99a199", contact.getUid());
        assertEquals("Counting Money", contact.getProfession());
    }

    public void testImportVCard12() throws Exception {
        /*
         * https://github.com/mangstadt/ez-vcard/blob/master/src/test/resources/ezvcard/io/text/John_Doe_MAC_ADDRESS_BOOK.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "N:Doe;John;Richter\\,James;Mr.;Sr.\r\n" +
            "FN:Mr. John Richter\\,James Doe Sr.\r\n" +
            "NICKNAME:Johny\r\n" +
            "X-PHONETIC-FIRST-NAME:Jon\r\n" +
            "X-PHONETIC-LAST-NAME:Dow\r\n" +
            "ORG:IBM;Accounting\r\n" +
            "TITLE:Money Counter\r\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:john.doe@ibm.com\r\n" +
            "TEL;type=WORK;type=pref:905-777-1234\r\n" +
            "TEL;type=HOME:905-666-1234\r\n" +
            "TEL;type=CELL:905-555-1234\r\n" +
            "TEL;type=HOME;type=FAX:905-888-1234\r\n" +
            "TEL;type=WORK;type=FAX:905-999-1234\r\n" +
            "TEL;type=PAGER:905-111-1234\r\n" +
            "item1.TEL:905-222-1234\r\n" +
            "item1.X-ABLabel:AssistantPhone\r\n" +
            "item2.ADR;type=HOME;type=pref:;;Silicon Alley 5\\,;New York;New York;12345;United States of America\r\n" +
            "item2.X-ABADR:Silicon Alley\r\n" +
            "item3.ADR;type=WORK:;;Street4\\nBuilding 6\\nFloor 8;New York;;12345;USA\r\n" +
            "item3.X-ABADR:Street 4, Building 6,\\nFloor 8\\nNew York\\nUSA\r\n" +
            "NOTE:THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \\\"AS IS\\\" AND ANY EXPRESS OR IMPLIED WARRANTIES\\, INCLUDING\\, BUT NOT LIMITED TO\\, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT\\, INDIRECT\\, INCIDENTAL\\, SPECIAL\\, EXEMPLARY\\, OR CONSEQUENTIAL DAMAGES (INCLUDING\\, BUT NOT LIMITED TO\\, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES\\; LOSS OF USE\\, DATA\\, OR PROFITS\\; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY\\, WHETHER IN CONTRACT\\, STRICT LIABILITY\\, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE\\, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\\nFavotire Color\\: Blue\r\n" +
            "item4.URL;type=pref:http\\://www.ibm.com\r\n" +
            "item4.X-ABLabel:_$!<HomePage>!$_\r\n" +
            "BDAY;value=date:2012-06-06\r\n" +
            "PHOTO;BASE64:\r\n" +
            " /9j/4AAQSkZJRgABAQAAAQABAAD/4QBARXhpZgAATU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAAAA\r\n" +
            " AqACAAQAAAABAAABAKADAAQAAAABAAABAAAAAAD/2wBDAAIBAQIBAQICAQICAgICAwUDAwMDAwYE\r\n" +
            " BAMFBwYHBwcGBgYHCAsJBwgKCAYGCQ0JCgsLDAwMBwkNDg0MDgsMDAv/2wBDAQICAgMCAwUDAwUL\r\n" +
            " CAYICwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwv/wAAR\r\n" +
            " CAEAAQADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgED\r\n" +
            " AwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRol\r\n" +
            " JicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWW\r\n" +
            " l5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3\r\n" +
            " +Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3\r\n" +
            " AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5\r\n" +
            " OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaan\r\n" +
            " qKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIR\r\n" +
            " AxEAPwD9/KKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKK\r\n" +
            " KACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAoooo\r\n" +
            " AKKKKACiiigAooooAKKKKACiiigAooooAKKK8t/bU/aq0X9iX9l/xf8AE/x5bzXth4WtBMtpC22S\r\n" +
            " 9nd1ihhDEHbvlkjUtg7QScHGKaTk0kJtRV2epUV/PP4k/wCDrP8AaGv9buZfDPhn4W6dYPITBbya\r\n" +
            " Zd3DxJngNJ9qXccdTtGfQdK29B/4OQ/2t/E/wr17xponhf4Uz+HfDV7aafqFyNKuAYZ7rzfIQIb3\r\n" +
            " c27yJOVBA284yK7Xl9Vb2+85frtN9z9/aK/nq1T/AIOe/wBqrQ7mWHWvCnw5s5oEWSSOfw5fRtGr\r\n" +
            " EBWYNdggEkYJ65pk/wDwdB/tTWrlbnwv8No2AckN4dvQRs+/1u/4e/p3o/s+r5feH12n5n9DFFfi\r\n" +
            " N8N/+CzX7d3xc+Ful+L/AId+Bvg3qWma6lzJpdsiquo6mtuzJMbaxbUhPNsZWBCITxwDXkY/4Oev\r\n" +
            " 2q200Xq+E/hybMzfZhP/AMI5feUZc48vd9rxuz/D1pLA1HomvvG8ZBbp/cf0K0V/PNrf/B0N+1L4\r\n" +
            " au1g8R+GPhrp87LvEdz4evYnK5IyA12DjIPPtXvX/BOr/g6A8Q/FL486L4N/bZ8OeF9P0rxNdxWF\r\n" +
            " rr2hpLarps8jbUNzFLLIGiLFQXVlKZJIYdFLAVYq9gjjKcnY/Z+iiiuM6gooooAKKKKACiiigAoo\r\n" +
            " ooAKKKKACiiigAooooAKKKKACiiigAooooAK+Q/+C8ng2Txv/wAEmvjDb2qb5LPT7bUQPRbe9t5m\r\n" +
            " P4IjGtH/AIKy/wDBUnQf+CXPwS0zXtX0Z/E3iXxNdvZaJpAuPs6TMihpZpZdrFY4wyZwpJZ0Axks\r\n" +
            " Px8/aK/4Ocfiz+0f8FPGPgLxR4B+GtnovjLSrnR7iSCO9a4ginjaMsjNc7d6hsglcZA4rsw2GqTc\r\n" +
            " akVpc5cRXpxTg3rY/N2vsP8AYw/a9+F37IPwV0q38WahqnijW/E3jfRdd1bS7bSCq+H7bT/tg81Z\r\n" +
            " ZW2XEubmKREAAyhBIr4685fWkMiE5OM17tSCqKzZ5EJuDuj7v1r/AIKLfD3wx8Hrrwv4q1K9+Omv\r\n" +
            " Q6Q1s2teI7K9t49TMutW14lsWSdLkJbRQyyKWcAySMoBXBbrvjZ/wVY+EnxC0H4gaP8AY9VuV1LT\r\n" +
            " /F9z4c1BdPKS2N9qU10kUDhjnyZ4LiJmPJR7eL/ar84g6A8Y5o8xPasfq0e5p7d9j7T+Bf7Vfwb8\r\n" +
            " J/C39mrXfil4r8S2nif4BXuo6rJ4d03RXll1ueTUPtdvGt2zrHFGdiq7HcQGICmuq8If8FNfAf8A\r\n" +
            " ZWm694o8QeIrG2uPD9tod18OodNdtMtL8aol3LqqSBvLIADTAhfO8w7Puc18Al0b7200F0PXbQ8O\r\n" +
            " m73D27StY9//AOCk/wC0JoP7TX7UN54p+G2unXNKurdliJi1GM2wNxM6xkX88r5CuCRGViBPyovN\r\n" +
            " eE2V01jeRTwHDwuHX6g5FVxIgPGBS+cvrW0IqEVEylLmdz+zb4ZeKYvHPw28Pa3YP5kGsaZbX0bf\r\n" +
            " 3lliVwfyYVuV/Oz8Gv8Ag6L+MvwW+EXhbwdongj4aX1l4U0i10e3uLuG+M88dvCsSvIVugCxVATg\r\n" +
            " AZ7Cul/4i1/jf/0T34Uf9+L/AP8AkuvEeX1b6fmessbTtqf0A0V/P9/xFr/G/wD6J78KP+/F/wD/\r\n" +
            " ACXR/wARa/xv/wCie/Cj/vxf/wDyXS/s+t2/Ef12kf0A0V/P9/xFr/G//onvwo/78X//AMl0f8Ra\r\n" +
            " /wAb/wDonvwo/wC/F/8A/JdH9n1u34h9dpH9ANFfz/f8Ra/xv/6J78KP+/F//wDJdH/EWv8AG/8A\r\n" +
            " 6J78KP8Avxf/APyXR/Z9bt+IfXaR/QDRX8/3/EWv8b/+ie/Cj/vxf/8AyXR/xFr/ABv/AOie/Cj/\r\n" +
            " AL8X/wD8l0f2fW7fiH12kf0A0V/P9/xFr/G//onvwo/78X//AMl0f8Ra/wAb/wDonvwo/wC/F/8A\r\n" +
            " /JdH9n1u34h9dpH9ANFfz/f8Ra/xv/6J78KP+/F//wDJdH/EWv8AG/8A6J78KP8Avxf/APyXR/Z9\r\n" +
            " bt+IfXaR/QDRX8/3/EWv8b/+ie/Cj/vxf/8AyXR/xFr/ABv/AOie/Cj/AL8X/wD8l0f2fW7fiH12\r\n" +
            " kf0A0V+AEf8AwdsfG0SKZvh58KmTPzAQ34JHsftRx+Vfpv8A8Efv+Cumkf8ABUz4feIGn0D/AIRT\r\n" +
            " xj4PeBdV05Ln7RbyxzB/LngkIDbSYpAUYZUgcnINZ1cJUpR5pLQuniadV8qep9kUUUVzHQH1rgda\r\n" +
            " /ao+Gvh3VpbHXfHvhK1vIH8uSGTVYQ6N/dI3cH2rh/8Ago38TL74Y/st6tN4cllt7rVp49NE0bbW\r\n" +
            " iSTcz4PuqMv/AAKvxy8Y+KHsNTSeOXHkNv3BsZ5A4/MUPRCvqe2/8HRFppP7U3wc+FOsfs56rY+N\r\n" +
            " tU8NavfW11Z6LMt7NHBcQxsZWWMkhVa2Vc+sgr8ZoP2aviVcput/Avit1HGV0yU/+y+4r9KbPxib\r\n" +
            " XTILQKZPPuC6EfxK4UKeR6qf5VpaX4vtbHTl8p5N0108IlLYHlsmBISORyytjHQV00cdOlHlSOer\r\n" +
            " hI1ZczZ+YY/Zr+JRCkeBfFR3HA/4lkvP/jvuKmsP2W/ihqkmzTvAHi6dgpbCaXKTgcE/d96/S3R7\r\n" +
            " +4kBt5I5fMV9jxtxuIz8oYdCTjB9QPSugtbPVrK9W40RrzMIUSblP7tuOH69OPwx7VbzWS3SIWXJ\r\n" +
            " 9T8spf2XPihDE0k3gDxcqIcMx0qYBfr8vFVX/Z2+Isb7X8E+KA3TB02XP/oNfrfqlrfXXiCK5s7B\r\n" +
            " 5Ib1V86FG2q5YYbZ6HPPA55I6Gsjxdp1xaz3AJtreK2Yoq+X8zAnABxz+J/OpWbS7Ir+zY9z8ph+\r\n" +
            " z18QznHgrxPx1/4l0vH/AI7UkX7N/wASJyBD4H8UsT0xpsp/9lr9KNTiNvo0s7zFZo35jLYYk5HT\r\n" +
            " 0HHP+FZOg+NGiufKdzvU9M9a0WZSfQh4CK6nwb4Z/YX+N3jOXZ4T+FPj7UnH8Nvok8h/ILW+P+CY\r\n" +
            " n7SbH5fgX8VT/wBy1df/ABFftt/wTe8S3/jfVvsGi39u1zbRlhb3BJO3cpJVh/D0yhDdARg8j9Dv\r\n" +
            " DFjJeWiPdo8Nyo2yxk52sPQ9x6GnHMZvoZywaTsmfye/8Ovv2lv+iEfFf/wmLv8A+N0H/gl/+0qO\r\n" +
            " vwJ+K/8A4TN3/wDG6/rcXTpMdf0pp0+Xtj8qv+0JdhfU0fySH/gmD+0oOvwK+K3/AITN3/8AG6Q/\r\n" +
            " 8ExP2kx1+BfxV/8ACauv/jdf1tPYSDqqflSGxb/nnGaPr8uwvqiP5Jf+HY37SX/RC/ip/wCE1df/\r\n" +
            " ABFH/Dsb9pL/AKIX8VP/AAmrr/4iv61zYHPMSflQbAn/AJYr+VH1+XYPqnmfyUH/AIJj/tJf9EL+\r\n" +
            " Kv8A4TV1/wDG6T/h2R+0j/0Qv4qf+E1df/G6/rXOnkj/AFK/lR/Z3/TBfyo+vy7C+qeZ/JR/w7J/\r\n" +
            " aR/6IZ8VP/Cauv8A43R/w7K/aQ/6IZ8VP/Cauv8A43X9azaYp6wD8qb/AGYuflg/Sj6/LsH1TzP5\r\n" +
            " Kz/wTJ/aRHX4GfFT/wAJq6/+N0n/AA7M/aQ/6Ib8U/8Awmrr/wCIr+tUabjrAPypn9loWO6DOfQU\r\n" +
            " fX5dhfVT+S0/8EzP2jx1+BvxT/8ACauv/iKQ/wDBM79o/v8AA74pf+E3df8AxFf1oyaTEcboCPwo\r\n" +
            " bSICP9Saf1+XYPqvmfyX/wDDs39o/wD6Id8U/wDwm7r/AOIoH/BMz9o89Pgb8U//AAm7r/4iv6zv\r\n" +
            " 7Gt2XmI0z+w7bfyhFH1+XYX1Y/k1/wCHZX7SB/5ob8U//Cbuv/iK/Wn/AINav2SvHH7Nmu/GLW/2\r\n" +
            " kPB/iPwReanBpljpqa3YS2TXUatcvMYxIBuAPk59Miv1iGgWxHKkfhSf8I/bKDhWrOri5VYuDW5p\r\n" +
            " TounJSRvN4gsUGXvLcD3kFW1YOoKkEHkEd65Y+HrZudrVreGVFqkttFnZFh1z23Z4H4qT+NcTjod\r\n" +
            " kJybs0fO3/BW65Np+yPM6gEjVIevb93NzX41eMrtvENneW2nKWlguBLHgcvG2AR+GFP4+9fs9/wV\r\n" +
            " e0KXXv2RL1LU4aDVLSQn0G4r/wCz1+bXhj9n20j1GCXUCjg/dYIQcnHGc81wYrFKg0md1Gj7RN3P\r\n" +
            " ENG8MavefZZLVZpreIKsWcnylOXAHp8xYkdmPvXf+GvglrWqMg0qGRra5X7hGAhPYf3uma+qPBnw\r\n" +
            " m8PeF7FJtRsYLlX+ZcrkZ9fY/hXoXhW2sJrSIWdtBGpOVVV7deK86eOc9Ebxo26Hg3wn/ZJ1PVQk\r\n" +
            " etWMc4mXaF3hHQ8HcpGc49Mda+m/h/8AsrppdnBcarDZSXKJsYFQQ/u3HB5PHvXqfwn06OJ0lt41\r\n" +
            " A24OwYHP8+1dvJB9pLFQJVBwSn+fatoXnG7M5RVz538Sfs0WjwFreGO2QNvCIAdpzyFJ6dBx/KvJ\r\n" +
            " vjN+zxpfh/RbyezSedo4yJJQm9n3JweR1Bxzjjk8GvtBgsN0Q4yD2PSud8S+GbfxHp97YzIscU6M\r\n" +
            " pCrgknkHPb6d6yas9GU46H4z6/52sXGqxRKRPp4BZQcsy7goIz2yVGP9v2AHAvdT21/CSkse3Egf\r\n" +
            " HG0nr68Ej86+lPjR4ft/gV8c9UsdahN/Be75lWJCJbeONlYtz94Yibp0CnpXkXxJ8B3fh6+e6uYp\r\n" +
            " Q5ZLaBSwIjBd0X5SP+maKB23DPFdtKvpqZTs2ej/ALCfxyPw7+Nuj3LzyAxXCFtuQSpIDL1H8JYV\r\n" +
            " +5Hhe+g1nR7a90+VJ4rmIOkinIcHkV/Oh8DPt+mfEDRb0RSNbpcRQzN0LK5xyOOCp6+x9Dj97P2T\r\n" +
            " r+QfDS0sLiQy/YFEccnVZUwCpB9wQefWuqEk3oYyVj1fYaPLoL8UCTJ6GtiLgUz3pPLNL5mDyDS7\r\n" +
            " 80BcTZx2oEfrj8qUtxmgN/ewKBieX9Pyo8vjoPyoMq56jNIbqNfvOg+poFcXygeoH5UnlD2/KmPq\r\n" +
            " ECcvNEB/vCon12zQ4a5gB/3xRYXNFdSwYR7UeSPaqv8AwkViTxdRH6HNMPiiwHW5jP0p2E5x7l3y\r\n" +
            " BnkCl8kHqBVJfElm33Zg30BpzeILUDPm/pSDmi+pa8lfSj7MnpVP/hI7X/nqPypf+Ehte8v6GgOa\r\n" +
            " L6lv7On90UhtkPaqh8R2gODMufTFQ3njTTdO2/b7uOIP93dxmnZg5w7ml9nT0FQwRiPU5toxmJP5\r\n" +
            " vWYnxI0WVsRX8LH0FX9J1K31iSW402VZUwsZI7EZP/swp7JiU4SdkzwP/gqrr0Xhj9h/xZf3m/y7\r\n" +
            " eWyJKAbhm7iGRn61+eHw7+JNhq+geYbnGwb2WQbMrz36dutfoL/wV40o6v8A8E8fiIi4zFFZyjJw\r\n" +
            " DtvYDX5KfDXWJr7w3M1miuVt2VVJ5Q7ec8e30r5nOpuFSNu36nsYFJwfqfY3gPVx4m0oeXKskUoK\r\n" +
            " Aqdw4A6Hv2/Gum8ICTTLoxuruUY7+cdu34HFfPPwbvZbGxAFyYxGd2xR93PQeuec/hXfeFfG9xeS\r\n" +
            " r9iuC4mJJdjnOCBkDsPvenSvJhiNrnTKnZ2R9e/DPXzbyQxWYFwpZC54GxC33seuO3/6q9O+1JbW\r\n" +
            " rMRtJXAzknv+v+NeD/s9wSalax3N+kRG3C5QHjHykdhkDpye/evX5NRRkVCoyCOFByeB0xz3/l61\r\n" +
            " 72GqNwTZw1IpSsQ3MgiQHc+eDn0HvnrWfbXYW9aRirqSM8jtVrWbuO1sHjVDhl288Ajjuea5eHWV\r\n" +
            " MbgOFJ7/AKVnUmotalxi5I+Wf+CnfwghvltfF+ggJfaUHedYowWnQoQQSegClufU18r/ABc8e2Xx\r\n" +
            " BOuXEafv5rCVFiycrcxTFl2+gcAcDHJH4fbn7a3iPT7n4bajb6neW8KToRKWfadhDfLg98n06gH6\r\n" +
            " /mL4w+MGnaZZyu81o/25r1UcyoXBaRYl2jODxsbpngHPWnCeuhySWrRraJ8X4dd8a+GZ/DcQj8y9\r\n" +
            " SPy2UKgiViGyM4yUOecYyelfsh8BPiVe+G/AmweWk3yMvyAKYyoKFfbHHoNuK/DP4D39vrPxv0ey\r\n" +
            " 0eBZ9MmvUuI0jO5pAWQqu4HnKzHOODjiv3J1fQJNH0G1itkEQgtY02AcLhRx+Ferg1zts8vMakqS\r\n" +
            " XKd7D8ctTkQEyoM/7Iqzp3xk1We8WOCSBmkIUeaAFGffjH1zXjlrLfGNfLxV23kv4jkjODXo8p4y\r\n" +
            " xdRbtnvbeLvEXlgxrpDk+kq4/wDQ6ifxd4m3KBBpHJ5/fLx/4/Xltp44lgtVim8P6VIqjG5vMy3u\r\n" +
            " fmpw8cgtx4a0jj/rp/8AFVHL5HX9bT+0/wAf8j1IeKvEpYBoNIPuZV/+LrH8UfEPWNMUQ6gmmp5w\r\n" +
            " IzFhnH0wxxXDHx75YO3w5pAJ9fMx/wCh1zeqapd3N1JLDbxwK7bvLjJ2r7DJpqN+hFTGWXuydztJ\r\n" +
            " vGF02c3Eh+r0yHxJKY8tM3X+9Xn1zqN4F+6fXrVvQFur2MPKWAzjGarlOT28mztTrk9wQFdiPrVm\r\n" +
            " 2+0SY8xm596q6ZpixLHk+ma2yixqAW4pXsdFOLlq2WNPiYpksc9KsmIiT2ospkMI+YdOxoa+UEjP\r\n" +
            " zHsBUtnXGKSLkEoRfmOPenz3/lWrPHHJKACQEXczfQDqa8f/AGiv2g9I+BXgq61jxLZXeozrIttZ\r\n" +
            " 2MMxDXszHCoB0GT1OOBk1+e37Xf/AAV++KXwUh3WOlLp8Fy6rH9jjVbO1k67GlZWaVxwCAQvBHY1\r\n" +
            " MpqO44y5tEfpN4m+MGsW961voXhy4hK8Ga+fy1HvsXJ/UVseGfifNd2hXXRapKkZctbJIckdvmz/\r\n" +
            " AEr4V/Zk/wCCqfjLxFazX/xz0+1vNFW++wzTRW4hkicD5zHgDIRvlIIPI6ivt/RNVTXrGPU/D9xH\r\n" +
            " c6ff2Rlglj5V1OCCKqMlLY55c6le+hyHiT4qS+MbOG80C6nsI4bo24ldSxkbjoq56e+BXc6V8OFu\r\n" +
            " Ykfxdf3GoSY65K/1Nea/sa2cl38PtRGs4uHGrTEeYM46YxmvcYwcjI9qpK+pnQj7T35dTK1LQ9M8\r\n" +
            " PaZu0q0AbI+Ykk9fU11vwRvDeaFett2hbrAH/AFrmfF2bfRHIzyy9unIrofgAd/hG7fnJvXH5IlO\r\n" +
            " XwnZh1aukjlP+Chfhb/hMv2J/iTYYB3aLLMM/wDTIrL/AOyV+RfgDwRc6L4CupJkGY4JnfcOqhGJ\r\n" +
            " JPYcE+p/l+0v7SGlDXP2fPHFoQG8/Qr1cHof3D1+Rfg7Uk+IXjG+8PzE/wBmRWphuRGQDKXj+ZSc\r\n" +
            " Hs5GPr7V8tnkbygz6XAy3Rwvhn4kreReT4edGiJVHkkGPNcYPHGQuTjPfAPevdfgz4OutSurWeUq\r\n" +
            " xvXEa5yAmB8wJ9MkivkT4H/8VH4qm+1YEdtcNFMExhSvylh7bh+NfoH+zRqFkt5DFevGrLjYCMoD\r\n" +
            " jDfTIwfwFeFQpc8kmenWagtD6E+HOhSaFoCJG9vGpwNvlHK5H+91/wA810Mvm200bs8beWu4koeg\r\n" +
            " +jY7/TrXN2utpY3EUt9NDBFEpbOf3bjBGdx44PXp/jIP2hvAtjq40zxBrmmQ3Mh2hHnG8++0Hof6\r\n" +
            " mvoYJWS2PLbuy54hluLm3dU8tsjGc4z/AMBJOOe+e/0rkpLkySKAzbeWZemDwMf/AFq7vUb/AEnV\r\n" +
            " 7GF9DuIp4JkbZIr7l4Gfz/KuJ1KAG4Kwr91sZOOfb8v6Vx4qEro3pSTR4H+3R8NtQ8a/De4m0Azl\r\n" +
            " LdTLMsYXAjVSWJB68hTjvivxS/agin0DxjdwDa0FvIm2MncxO77renRfl9vXp/Qr4y0ZdW8GajaX\r\n" +
            " QIS4tXQk8kZB6evP8q/AX9vnwr/wh3xLvre+Kkw3AiyB8u3fuJwP4huPX+nKw6s9TOp8Wh6P/wAE\r\n" +
            " gfhJd/G/9sTwdaaNaC9ttLvI7yXa2PMRAzSLnuP3OAD6/jX7/a74d1e9sXfVdMvIJJVPCoWUYHqK\r\n" +
            " /Ef/AIN9I/Elj+0LqGp+HrK+fTNJup1e7hBQFZP4BnG7pIfYE561+0nxK+I1/wD8K5eePUb2F05c\r\n" +
            " rKQ64IPY+lfQYGpHWKep4Ga0ZRfPJPlscX8TPGuhfAbw0NU+Lms2Xh+yAwJLycR7z/dVerH2AJrE\r\n" +
            " +Dn7WHw3+OWqfYPhr400XU79vu2wuPKnb/djk2s34CvmX4B/s+af/wAFBvF7fEz9p261HxAdVuJl\r\n" +
            " 0vR5ZW+yaXarIVjRUXG4lVBYnqSeM10H7U//AAS0+HOi2f8AafwhtrvwZ4hssT2d/YzSIIpVwysV\r\n" +
            " LYPIHTB9663iYp2sccMsco8x916b8N9SuoQ0UEjAjjHPFOn+H17bOBcRSIT61y//AAT4+IutftCf\r\n" +
            " smeEvE3jHVrxNYnhktb7y8BXmgleB3A/2jFu/wCBV2Wt+IJtKvXGoX7NFbTyoZJH2jaucE9u1a8x\r\n" +
            " nOhCCu0ysvwu1O6UGKByPY1m6n8NtVtOHgkBY8fNXX+EAfFXh+1udP8AEBeSaFJHWEqyozKCRwfe\r\n" +
            " uG+Lk+oaPK9rJfTXCrc2Tq5yrDdcKGBweeB+tO5FWjCEeaz+8G+D2uXPzQ2khX3YDNWtL+GmqaPY\r\n" +
            " Mt/bSKwJJOQQKhfU74MQLqdV9BIeP1rmfih8a9H+Cvwy1XW/ijqM0ek26yCQ+YS0hOflXnrjcfYA\r\n" +
            " ntTb5TKEKbezOoXwz4j1LUYls4I4bdJsEj5mdBjuemee1ZPjrwP468Q3M9toEc9jAs4jEqsoxHu5\r\n" +
            " Zeck4ya+FT/wXi0sfE/StO8HeGJdQtbwsfI+2Ot55YbG4fLtDHBIVhkj619X/s4/E62+P9jr2v8A\r\n" +
            " gnV7m40rXZ0lgEkhY2p2ndE6Z+VlPBGfccEVCkp7F8iS6/f/AMA9q8B/D6/0GyniL3N15svmb5ZN\r\n" +
            " 7fdUEZ+oP51g6R8LvFVh8e9X1bVZriXQLuwhS3hMn7uGRT82Fz1PBzjvW58P/NtPDkKySOW56Ej8\r\n" +
            " q1ZLycvzM5CjONx4pWOulNKCsj84v+Cs3xwudA+JIsLcKzaDGotoTlg95OzJHkDnhQDj0J9a634B\r\n" +
            " f8EafB/xH07Sde/a88W+LvG2vhVvJLGfVPs+k2U2QwjjgiVcqh+Xk4bBO0A4Hh/7a2sJ40/bja12\r\n" +
            " G9ez8UWm+LBIZI0QdBnJBL9j96vs/wDZu/aC8SePdWubPVPAt/o2kIQLW+nZ1ecFN+WjdBs42jGd\r\n" +
            " 2TgqMHHBObdRnThYws79TyX49f8ABMD4d6KNR1j4Qtq/h7Xt811HL9ukurV5mJY+ZBKWG1mJzt2n\r\n" +
            " nIIrqf8Agjf+0mfjH4G8TeBfEjpHrHhOeQLbhwxiXfslRT3QPtYH/bPpW34t+InxB8UePptKvfC8\r\n" +
            " K6JfArDdQogNshDf6wtcbnb5cErGANwwGHzV8i/sGXOofs2/8FZtbRoLmPw94tv59PD7CIpJJIw5\r\n" +
            " AbodkrYOK0ou0iq3LdOx+ovwj+H0/gy1v7ZbZ1jkuGmU7epJOf0AruEs5EUCSJxXJ+CfE8+oC9dL\r\n" +
            " h2CXLKp3HjGOK6AaxdEHdNIcerV3K5xxcIrQTxdpc15oxS3hmcll4VSe4rovgvpL6R4LC3CsjzXE\r\n" +
            " khVhgjkL/wCy1yPifxlqGkaSz2FzLG4dBke5Fdz8KdSuNZ8B2N3qkhlnn8xnYjr+8YD9AKJv3TXD\r\n" +
            " OMqztvYv+NdOXWPB2rWk2Sl1ZTQtj0ZGH9a/Bj/gmv8AFNPHPiHXpZZt0/8AaMhRScMVJOByfX0r\r\n" +
            " 9+WUMpDDIPBHrX8wP7H9vffCf9v/AMa+EUnZI9B8S6hpki7ziTybt0wMf7h/CvBzmnemqnb9T6PL\r\n" +
            " 1zOcV2PZ/CHww8ZfCL4oeMY9T0W8jW91G5uLKQxlo5VaRmAU4+bGeR6fhnyz4/ftj/HP4IXd/deD\r\n" +
            " rQ2WnxoC8tzaZjt8Yxhj06gZJPbjjn9w9C8N+HfFfgexiubK1uC8KM5dASWx1x61+eX/AAV8/ZFk\r\n" +
            " 8S6dB4gnab/hGbFwkOl2qhTe3JOQWyR8iIrMzH+8oHIrxVS9lLnvdHbCqqs0pqx+WXir/gqD8efi\r\n" +
            " nJJH4j8XTLbsrBLdVWGHBAB4JC9ARzn1wT07j9nP46eKtdvZP7W8W+E0a7dbWZrrU0jMZYnCiaQh\r\n" +
            " FZ9pGWJ3Ed8Gu4/ZZ+FHhFfEt+njjSnuzqsclkbdNNEhhRwQPLkDHBGcZwOhzX0N+yL/AMEvtF1v\r\n" +
            " xrBqF9c/afD0l3ZXt1pJsLhE1VoC/lm4gkKxIcM4+UEgFhkFufThUo1o9jnxMZUZ8sY6dyx8C59f\r\n" +
            " uviDHoH/AAkd3puv+Uskdt5eJJIssVaJujxks37xWZeBjkDP6Gfs/wCi+KNB0Bbfxvqd5qnyKVku\r\n" +
            " NpZOvcdeo7ngfgOW8Af8EvfDUM9trWkx/wBkNZXJutPtYdwjsnLEnyRuUQB/4kQoh9M5J98sfB0+\r\n" +
            " g2gjvRK0kagNIyfL06cE/q3fvXJ7OS3WhN43vEzdWMdvp8y3X7xZEZWX1GDX4nf8FEfhxa/ED9oB\r\n" +
            " rbS5Gee5ufs06OxywzujkyehCEqe+PL64Br9q9Vk/wBGYAkup4BPB/D9a/NL9ob4ZXPjv/gopbeH\r\n" +
            " fDmlw6i16JNRlgVTvHlQxMVOPu+YzoM+meM9c4TtJ2NKkLWZ9kfCf9kjT/2ef2GNS0LwbNNpxtPD\r\n" +
            " RSXULcCO5mfynbzWdcHOWboenevPf+Cbni3V/HX7M2q2OrSXNyYbpWgEj5YhlDMo3HAzg8epr1L4\r\n" +
            " PftdD42aLeeB/iZYQeHriWA6VcWKqQyKV2cZA49+1Y/wR+G0/wCzt+zRZQagsUF5cXjSTrkq6qkg\r\n" +
            " VQv5d+xrqy5qtiIyh0TuLOJrDZfUp1lq3G3/AADQ/Zu/Zom8CfDuy1O41CGKPw/FDb7EimW4jlTa\r\n" +
            " p2OkihFLbiQUJJPJxxXWfGL4V3PxDlvtf+1YNlOYZxeS3InQLgr5OJRGuBuypjO716isPVvilc+H\r\n" +
            " L+70WJbG+0htSzdQyB5DgklXwgYtgnBGCQazb34skPremaFY6fZ2OoTxxNLG8oe8kAb5sSop44G4\r\n" +
            " +voK9iSdzxoSpuCtsfQH7A2jL8P/ANnz+yYo3j+yanfygFSFCy3Mkq7c9tsi/jmvALebX/23/ip4\r\n" +
            " 10v+0YtE0fw7q89jHBLCZhLgsu/AZcMSGbcc44AHWvorwQmseE/DMNnZ3enFdnJktndyfdhIAfTp\r\n" +
            " 0ArivBvw5tvhz428Q31hM9vc69J9ub7MNm+RVORznqccA56+uK6K/MoI4qE4VKnL0PC/H3wFuv2D\r\n" +
            " /Hlr8QPB/jC9V4JkhvrKZ3FtfRs4xHsDbMsWC8rxkEYIr6T+KXjO38Z6HBq/hySO4g1IaZcWzZ+W\r\n" +
            " QPMrLz6HIrz39rn4VwftD/Ca58OWlzerqet3KvG08hf7J5cwcOEY/J91GK8HA6ZPOingv/hUnwY8\r\n" +
            " O6NLdiaHRf7LsEl2lSUiliXcxJOSeTmjDc1nfYzzJKC5Yljwd4l8e3/i4x+OtK0Cy0ghsSW11I8o\r\n" +
            " P8OQygfWvm/9tQ3/AMX/ANo/Rfh1aTwW+gWcUd1e3kxX7NbtKwaRpCx2nEQYLn/loYh0Jr6ouPHu\r\n" +
            " jzusVvqNm7v8qjzQST6da8L+G3h/S/GWueMtM+KtnPO15ej7TdJOY3eIyziNRtwV2tFJnJIKsvpT\r\n" +
            " rzUo2TOLBwUp2Zofto/sR+DfG3wHTVvhP4U0aTxv4YtRNoskQWJrkIARB5q44cAgE5w2018Of8ET\r\n" +
            " v2pfGHh/9qW/8Ba3Df2tpq2pype6ZdJmWB/LkYltwDK6GM5PGRuznNffHjf4feCfid4UPhbUxdab\r\n" +
            " pGkRmS1ntLkoIWV8qYmViG+UDoCACQcZIr5YtfhND4X/AG1tB8f/AA2tbywlEscKnb+6vodk6JIx\r\n" +
            " A5kZ7edCuQdkR4IYVjTqJXPTxVPaVtT9LPCHjO1uNUbSIlb7TBAJ2YY2bScYz68Vp6pr9tpyXTXL\r\n" +
            " ECCIyHjoAK5f4XavZ6xaXUmlzea9nO9nONpHlyIRuXnrjiuJ/bi+P2lfs3fs9674l8QXCRyxW5jt\r\n" +
            " 41bEkzEgYUevP612OSSbOOMZRilLc/PnVPE39m/8FD7i7uRHNLcalf3UqPjb5sCxcA/UV+hFx8Qp\r\n" +
            " V8J6fqPgXw9da1PHL/pGn2c0FvO+UILAzMinacZGc855r8G7X9pHW/FnxRu9fd2e4isNYnEi/wAU\r\n" +
            " lxA4Vsn0cgg9cAV+iHwR/aI07w5pmkeHf+Cia2PhXxC1klzYa9LHE+na/bsinzI5JFKBxkb43AKs\r\n" +
            " cjg15nU9DD3grM+19V+Id5ofwrtk1WG3t9XaH9/GkwmMDddpcABsZ6gAV8Afs/fGibxR+17Yw+IX\r\n" +
            " tGt9N1mc2Pk5O4C+kZ5XyT87MZBleNsajtW78df2l9M8ZW8vw9/4J6Qw67r2txNPc6tZwolpp8Gd\r\n" +
            " rTvIqhT/AHUA4zj0wfN/gT+zf4i+BHxN0rSfiLZy23iLw8ixXOTuDsWkYsrfxDezHd3DCqUrWbJx\r\n" +
            " D5j9Rf2atefVPC2qzXOSy6jMo564Nekw6j5kJcAjPr2ryf8AZ0tJdH8NajHOD+81CWUAj1xXoKaj\r\n" +
            " vLocYJI6160FeKPHjOy18yLxvqxXSSrYyZI/5ivY/hjALf4e6MqDbutUcj3Ybj+pNeAeOrsi0A3f\r\n" +
            " xoK+jvDNkdN8OafbOQzW9tHGSO+1QP6UqmiO7L3zVJvyL1fzjfGv4Wy/B3/gsh8ZGv1EEDeLrvUA\r\n" +
            " meWS7b7ShHP92YE1/RzX4gf8FUfg7NpP/BYvXrojbb+KdK03UowP4gIVt2OfXfbNXh5y39Wdu6Pp\r\n" +
            " stdqrv2Ptn4Baw8vgqweBCI2hAXDZzj2PSrPxo+Ett8ZfCMun3aI7Kd0Zkzx6j6EZ9qvfBCztofh\r\n" +
            " RotvDhJ7ODY5XGDzn867XR/3KgTLg54xzn/PpXjU4uyT6o6aiUtT418FfsEeEvB+rSNc2p0yd7jP\r\n" +
            " mRkiPBH3dmfl6HkY7cen1R8F/hx4f8J+UHijuGIwHErfKOx+Zjkc1N460q1unIgtgZ0YbQ7DkY/T\r\n" +
            " IxXN6UkumarbwI+woW+7IZAD9eMY/XmrjahLRGbg6urZ9BxXVhFasliXwy4AB69emeO1c54sMy6P\r\n" +
            " JLA4ZMjcCM7R7HJ7Y/n7VpeHtFL6Kjap5J2jnqcDj3wDz09vpXO/ETxJEbL7LpzAxtwxA7E+v1r0\r\n" +
            " Ks+WF5GVON5aHGay3nrI4xuwOBx36f59a+e/+CfulaX4/wD2oPij43usSNokr6dDdEghFVv3hB6j\r\n" +
            " BRBu6HZj+Hn6GcsNJnlPO1CRx1x9Pxrk/wBn74d6N4c8Ma5HouniH+3z5V60B8trgFGDEdMEbzx2\r\n" +
            " +ua8un8Sdjpq6tRPK7H4Pan8QP2ovEHjqSBNJ8LLdGaF9wMk6Jj51QdAxHGcGvUPjVbzeJfBU0uo\r\n" +
            " jy7c48rcwQDLLk/l3r0uy8JaL4U8Kx2MEbR29lH829uNo5O4mvi74k/FS++JXi+aW+vXuLOOR/st\r\n" +
            " uOEiXOFwo49BnBJ4/D1sqw/s3NvrueXn8njI043sl+SOY+M3h/xN478f634g/Zq8SWOg6vaXe6SO\r\n" +
            " 4ga5stQiXaiyvGMMrAKh3r/dwQcDHLSfDv4h3MT65+054y0vWIIctZ6bokMltb3Tdd8rN+8kBbtw\r\n" +
            " vBznNU/iz4/1X4UNFL4Zg8tbm6D/AGtJFH2VW/55j724khcHg565IB0/Bk/ibxh4Fsr34vTWovp1\r\n" +
            " MksNmzF2VjlFkc8gKrKpIADFTjgYPqOD5r3PLjZRSse4/Eb/AIKA+L4/AuiJ8HPDi3Mk9irX2q3Z\r\n" +
            " DiCZflcR2ync2SCQxOB6V8xH9vzxZ4T8cR+Lfiz4s1kw2rNZxssKSWkDM2B51llFdCVUMFZJADlX\r\n" +
            " BGD65p0X2PTl+xxFVtUURxrn7uSuPbp169fWuW+PvwO8MfHrwa+j/EK3mMU7fu7m3byrmBlAIZXx\r\n" +
            " hhk/dYMPbPNbqalpJaGSp8jUo7jPEn/BRTQfHyQsnjGPxd471j/iX6bHoGlT6PpHh5JMLLc4ndpp\r\n" +
            " J9pc79zDChQEBZm9U8K/tjalr2kWWi/FVftOnXF9CqaiDtuI1jlVsyoBhsheowR3zX502X7Oj/sc\r\n" +
            " fFiHUPEN/wD2po1xE39lXfl7N7kYZZByFdVPTODuBB6ge7W/j631XQdLuLdy4FwsjYP95mX+oreN\r\n" +
            " GnFLk2ZnWk6r94/Ry3+DegTvBe2E1zKOJUIlBVu4IwOQa1fDHg6wbRPEurlbaK9lt3lMs0ghRGtv\r\n" +
            " PZS8h4VSXwxPbntXin7OPx507Q/hutj40vGgntLmSKAOjtmIhXXkA8fORXjP7YH7dd1p2j3/AMOf\r\n" +
            " A0Nwjanv+236DCmKR2ZYQM7sspGTjvjvXLVUKMXoYYKm51+SJ3/g34teKvjj4/bR/ht8ItT8GJFG\r\n" +
            " 8F74m1qdF09Ic8m3CMzSlv7ihQe5Uc14n8Fv+ChEvwl/a11nw9K9v4q+Gmh3g0+GU2cBuZZU81nu\r\n" +
            " 45SuSVmurkoAQCrEdwRa/bK/bKuf2e/hs/w6+Ecb/wDCU21sPtt5M7PDYQNCJBlSQWlVWRdpG3kH\r\n" +
            " rwvx98G9FuU1eygaYSn5TK5TDytnJJPck5OfevOnP2bSifRYfD+2TlU10P6BvgzoeieLvC0es/Cu\r\n" +
            " dJ9J1uRrz7TtYea7HDHawyDkHivnz/gp1/wTI1b9r3SrJfCWr/ZwESK7SSV2eWIOGKxqxEacqOmC\r\n" +
            " 2TkjGa+qfgn8NLDwX8F/DmgpBFNaaNBFAQ65y3lgliPXLPmty9+Hfh/VXVdS0mxnBO8B4gQD9Ole\r\n" +
            " lKPNGzOBU4y2PyO+J3/BGe3+EPh3SNMRbS4k1a1SGdoJCRF5kycFwo81ykMgz8qrvwMjk/Vfxe/Z\r\n" +
            " b8K/GT4a2vhf4v8Ah6y13SLIKkMU6keXhQFaN1IZGwMZUg9q+rde8MWmr30enapZW1uLWMnTWiXC\r\n" +
            " OgXlAOishJ47ggjuF5i+8O/aY8aahMRX5gAMryOefWuSdLldzenG1zxj9kb9jHwb8GNTttC+Dfh2\r\n" +
            " 30nTfOSS8kUvI8oX5trSSFmIUE4BOAW96+gPi5+zZ4f+K3iSK8120/0q0fzEkj+V34wQfUEY4P8A\r\n" +
            " dFaPgcL4VtprXw3bf2jrMp/e44gscjIEsnQHnJUZY8cY5rfEWoeHpVMUkmsQ8BlYqJ0OOcE4DD2O\r\n" +
            " D7npW1KHLHUJKL0see6l4MPhx7h9HXEbyl2XHKk9iKyXuip/fwqfXjmvVNU+zeKLWSbTjLDPD8si\r\n" +
            " yRNG6+zIwBx7/lXH6jpsJLxuUaWP7w712QlpY8vEYfld0edeKNSgvdc0rTFkjS61C8jjjiY4LZ4/\r\n" +
            " nivq1VCqAvAHAr5ktdMTUfjD4VtlhjY/2qkmSPuhI3c/olfTlTV6GmWRa535/wBfmFfmz/wV7+FD\r\n" +
            " TftpfDfxJbIhbVdBl0/33W1wX6+/2pQPpX6TV8a/8Fj9Kh0v4f8AgLxVKwWXSNcfTwW4GLmEt19d\r\n" +
            " 1suPqa8vM4e0w01/W572Ely1Ym58ENFi0z4bQG6lVWkUMAh9ABgDA9+fr7VpajKYpdu1/L6BmH3f\r\n" +
            " rXFfCf4v6X4e+F9hLr9uzShQUYuShOB91Ryx471BbftheHPFnjGHw1Y2niK41e73GJG8P3kUbDuw\r\n" +
            " keJUx75xXj88PZxVzvUZOTdh3jD4u2B8SyWWoBIpI1+9nk9e+PYfQ12Pg+w03WpzfIUkdHGwlcMp\r\n" +
            " JHODxnOee+a+Vv26/BuseA9cs/E+giZYXkUXkYBZV6YPoB0Gf51137PPxSl8Q6DCEYplCSC3APAI\r\n" +
            " OBz1PeuRYhwqctRGjp3jeLPqHxH4zNrZG3gcAqu18dW+vHbJrz++1B9U1HGwhQMAZP8An1qOKaTU\r\n" +
            " XBdgCVw2DlfXHP8AKrtvbmG6VSScYwcdO3860rVpVnYmEVTRneKLg2fhW7eIujPF8p44P0NcdceJ\r\n" +
            " B8NfD2m6hNcTypPcOSgAJfKod+c4GNuPfdmul8d3Hm2LJPGZoeF2jjk9z6+uPbNcNFf6J41s/wCw\r\n" +
            " Ib5JdWsoN4gIwsyHJDxnoTg/MOCCD25rqwNKFWpy1NjzsfWqUVzU9zxT9rL9t/XPFGl3uh+FrdtL\r\n" +
            " sW/4+5WbdPPHnlSeiAgN6578V5B4K8fSXF75XliLABKd1AG5ensM8e+O9ei/GH4WCZb0qu9Cynno\r\n" +
            " ACd388fjXzRrvie48C6ndhhJ5caMItx+bKqMEnqOoI9MEd6+jhSjSVorQ8KWInVlzSdzvPjfeLr3\r\n" +
            " iHSLG3JeGPZOxORghsgeg5RSPdT7V6V8LFW9+GunwXJaWaENGGII2xpIx4J46FM180n4rR6h4su8\r\n" +
            " yBmLiPOMFcAZHI9vT36Y3e9/BzxNFH4LtVmBP2hrjccZOMoScDPZSOc/zpXuzd/Cjs9C1swXSxHa\r\n" +
            " fPmUnJzhRlsfrVXX9Rhg0d0nkjQQeWqHPOGZcg9uQ/H4DnFUbqc6XqZmkISMEiNkBJz6Ljr16n8K\r\n" +
            " 5Dxx4wjij0gXM5jEk3nyHO1pAiPgADjkk8H0Jz0qkrGbdhnx9+G9p8X/AIW6joJUfbbpDNp7MQNl\r\n" +
            " wp/dsCcY3HCEekhr5++B0xudOs7W5yDGRG6t1BB6H8RXvfg3xBc67q9rNdZO5jhMYUKWG0L7/Lz7\r\n" +
            " 15zoHwV1ey+Omsw6fHusJ9Qa5i2rgIsh37R7Ddj8K0g+hnJ3R9ifA7wdZeKdIkj1SASGOON0OeTw\r\n" +
            " Qf6V8F/tl+JW8K/to+KoLFzcWttdwiGFWztKxRhlz25U/rX6XfBDwjN4Y0GIsAZWhwB6nHH6ivxi\r\n" +
            " +M/ijU/+F1eJbjxp5qapJfzfaVlBDq5c5BB6Vx4/4VY2yeMfrE5vsevftYfG9P2i/jJrniSxhewt\r\n" +
            " 78wRRREDesUSIhViOpYI2f8Ae9qf+y5oE/jr4saJpelpun1C/htYx6l3VR/OvDW+I1jaxBbl8n69\r\n" +
            " a+rf+CQdvD47/bL8CJtLxJqIuSAM4EatJ/7LXn04uc1dH0VarCFJ2fQ/fC+I8MW16rlfL8pJxj6E\r\n" +
            " Nx9VX86etxvgidOwBz7Yqv43bFt/pSIYVjeJwWzlGH+IFZfgG4VPDFjbGeW6NvAkPnSkGSXaAu5i\r\n" +
            " MDccZPHWvdktDwKcrM6K9sYNUs1F0hcxOJo2Q4dHHQqfXqPcEg8E1zmmW0EerzGeB2iQF0VlKFyT\r\n" +
            " 8q4PQk10lo22LA7U5LONbxrg/M7BRz2xnp+ZrFxvubp9UWYIV0zS1SJUDKvIUYBb6fWqkkRRAyy4\r\n" +
            " YnHPRj1qa8uAoXPOTnFch4l8aXdrIx0izjkSM/NJNL5YP0wCT+OKpK5M5KKNrXPEJhto4z/rXJ3D\r\n" +
            " 2HfP5fnXmV7qjR+MN00h/eq2f7p4J/mKtyfECbxJqvk3VokBgjYuyybw2SMdAPQ1yniPWPs+qW9w\r\n" +
            " Y53VS64jQuTkHsPxraKsefiKt1dM6n4Vab/b3xrsJgRjTBNdHvuzEYhj/v6D+Fe+V4T+yNqS+JvE\r\n" +
            " /iC8S0v7YWcMUINxCYw/mMxOM9ceUPzr3as5u7OjLl+65u7f+QV8tf8ABZ34Y3vxS/4J0+PYPDFs\r\n" +
            " t1qmji11i2Ug5XyLmN5WBHIIh87n0zX1LWL8SPB8PxC+Hmu6Df5EOtafPYuQASBJGyZAPGRuzWNS\r\n" +
            " HtIuL6o9GMnFqS6H5NfsPftvHxP4G0zSWtYEuNIiWGXLbHBBKsxZs98entz1+rfAvxGt57tprm3W\r\n" +
            " O6Y4eXdu3j1Zuw5zxX51fsz/AAl0fwV8f/EviKPXGk0qa5KpbW42ujgkMXXjadw9Pzr7U8O+PPAV\r\n" +
            " /YfZ4brULUDlfOAZQ3cjnPXIx7DGO3ytKpybvY9SUJs9X8b3ulfELw7NpWu26Tw3EZjcuwPXjr26\r\n" +
            " 57dq8Y+GfwtPw11ieO0YtbpJgDPVcjJXnp0GD0x7VhfHP436B8PLGxm0rW9OJupFjjQSjexyTwpx\r\n" +
            " xjcfofwruPAfi8eMrW0uYQp3orBw+CSc8EYGO2MVFaUajv1CDcXZnpOjSI2wwqQNoznv9BTNS1TN\r\n" +
            " tMIATICc4B+b9OnvVex1D7LY3Dytt8n7p7Dvk/nVDTL2eS7nY4VCMsGkw4IPUEZG3p+fOKzWrSKm\r\n" +
            " 7alHxbfTy2ciQvAkmPk3s21n4AwB26/hXxV8d/ibqnw0/bW8Pz+ELmeGC7h0+aSynUpku2GIHYnr\r\n" +
            " nHOfevsXW7o6pcrb6JG7PeTIkbId2AxwpIA+UD1x29K+Of20tLjm/wCCuvww8M2cJCajb6YyJtwr\r\n" +
            " RxTSFxkccLGa2s1b1M4L2jfofan7VvwPTw1qKXumwCOz1NeUUYWNyMkfj1/Cvgv9pP4Iy3N7JJpM\r\n" +
            " Lu65LlRgDPv9cHvjAx61+yvxc+HafEHwHLaGMNMqBosjkOBx/h+NfIPjD9n67uxKl9YkckkEDgiv\r\n" +
            " sYu61PlK9KVKei0Z+RmjfDPVLbxS7uhRHmZyQfVs9/8AP6Y+nfgxG0Pgq2ivYzG1qHKl22/elI69\r\n" +
            " +M17b4q/ZdW3mfzLRVJOe3FR6T8IE03TEhSLywzYJUZIIJP9ajksy3N2R5frmrtDarE0p8oqPmHV\r\n" +
            " Tyev1z0/DvXn3jHWNV1zX47DRtPN6kUHmSuinERPCjOOOVcke/brXuHir4SNDK8umL5E6n5TjKuB\r\n" +
            " 2ZRwfT1HrXHa34HuYbWV4beCGZX824iQZW44x83T+EdD6YqkiOZMxfBPh2TQY4Ddyo8gEKZQgndt\r\n" +
            " yxHTIJYD321798CfA0Wt3E12YB5kDryRnKnOP5GvKvCmhyssMc8SqUYPwC2wAjA/D0r6l/Yv8BXH\r\n" +
            " ivxNqWnaKA7C1WYtIcAlWxj2+/Vrcid5xstzrtLsHtYEGwDaOw6V+X3/AAW+8OeFNK+Nnh680LS7\r\n" +
            " Zdfv7Rjq8sLBfNYEeWXXGd4XvkEgj0r9ePjF4On+CXw71LxH4vWL7HpcPmOkThpJDkAKvuSQK/Dv\r\n" +
            " 9qiz1P43/GHVdd1UNIt3dSTIjc+WCxwoPsMCsayc42ReX03Sq81TQ+bNZ8MW15Ahiyp9CTx+Pev0\r\n" +
            " P/4IJ+DVtP2sPDE06kqsd1hhzt/0aQA18hN8HZbq7ijEBALAcV+mv/BFrwDB8N/imNS1S1uJvLsJ\r\n" +
            " IovKKL5bNtBcl2UABS2ec89DXJhlLnSke1jJwdO6P0p+JupXUPhy9XSojd3yQuI1A4Y7TgmvLf2e\r\n" +
            " /jePHOiJ9pIS/hJiuINhRopV+8hB6Ee9eySx2utanAYfOjDEHKxEo3TO6UAqx9sivmnxz4JufhL8\r\n" +
            " VLnxJ4AtxI0V00Graf8Ad/tGJchZUPQTKpwCfvDg9Bj1FaTPHnzQtK59Nabqy3FpE6/xLnmrhuxG\r\n" +
            " TzkjFcZ4V8Sxarolnd2IkENzEsqq4wwDDPI7HnmtfUNWEEDbgYznkHrWbVzqjPS4eJPGdvYZFzIF\r\n" +
            " 2ryTxiuC8SfFHS3t5Wt/Mu5YkLJEpCh2xwMsQBn1ql8TNZ8N6Jatf/EPVxp8TH92nnndIfRU5yfZ\r\n" +
            " Rk1w0l3qXj+3aL4Y+Ep7XTgCov8AVkMDN6FYz+8bPuBVxskctScjp9H1q5urSW81lolvJkAMUbZS\r\n" +
            " Dk4UHvwTk9zntioJNT8zY5ywVsnv3rS074bXHhnw1bRzRzS3O398xU43Y7eg61nyaDcICqRSAE/3\r\n" +
            " TxVI4Kzlax7X+zbpptvBl1cyKAbu7Yq2PvKqhR/49v8A1r0Oue+FWk/2L8O9JhOdzQCVs9cv85/9\r\n" +
            " Croawnuz3cNDkpRj5BRTJZxEPmNUrnX4bf77j86k3Pxl/ae/ZUvPhl+2R8R9P0PxFPZRX+uz6vDG\r\n" +
            " sIULFdj7T5WdwGweZjjB+Qc8nPWfBH/gnF4fnvrjxH8TvGfiHxJHdfvnsLrVJWso+hA8kEIBk9Gy\r\n" +
            " fl4Nd/8A8Fs21/wF8TPC/jn4baXb6nY6tbf2bfOd26C4ifdFkgj76MQOn+pbk5ArkP2dfiXq/iTw\r\n" +
            " bBLrVstuFODGpG1TxngZx3H4V8zi37CrKLWn+Z7NGrF01JPU1PiT/wAEyvhL8TPD121t4XsodTd1\r\n" +
            " eDU4v3Mlq6klPK2gHGcA5znuKtfsteAtQ+DNw3hnxBez3TWi5gnmO55AOBk+mOMj8a9h0rxLLFGQ\r\n" +
            " ct8h+QZJGM+n8vpWZb6q2ueJYHvIwEjG7zMcjqD069uT71xT9+zQpz7s6jV5Vh0djcNjzsB+RwPo\r\n" +
            " fy7da56/1iTw7pjveS/ZohGCxk5bPQ5x+X4jNbusX9mnOsyIsaguSRjbjqR9Dj/Irk9KuIfiJrrX\r\n" +
            " iLL/AGXBKPL8z5ftWOrAf3N2evXAI4A3XCDbuznqSvsdR8HLCZrqfVNQbCupWJCvJ5YmQn1+Yge3\r\n" +
            " 1r538ZeCYPiX/wAFzPhW9piR/D3hO41C6A52YmkjTPp/rifpX1T4s8WaR8O/BNzq3iCaK3s7CFpC\r\n" +
            " CRk4BOBXzT/wR90m6+Pv7Z3xT+MPiZWYTLHpGnZ58iNfmKD22+T07g120qTnUijSm/Z05zv0t95+\r\n" +
            " nyJmHC8YFfOX7Q82oeFvEsphlk+z3Cl0x2PQj/PrX0fIrGHAIHFeffGP4ZN440kqWUPGCUOOh9K+\r\n" +
            " li7M8HFQlUj7m58l6rr0+oufNkJ+oFYl1DI8QCuTnJX65Ndt4o+Eep6NeypsDBT1BrJg8CapBGS1\r\n" +
            " uxzyMjpWm+qPO5ZrRnKnSft0YVxlgemKyL/wGjyMTGJWmB3Iw4b6+vpXpUOiTwQn7VZygjg4XNX9\r\n" +
            " M8LNqgdoIWDRgE71wTzjA9f/AK1MLM8qsvhSLRl+zoYkyCEVyQMHjA6L+Fez/A/SrzwL5l1pbSW0\r\n" +
            " jxeX8jYOMg/0FXdE8CTTyputz6DjpXYW3hGW3gwsZHHTFNK5lUbtoeW/tOeJNe8WeFLixl33trOP\r\n" +
            " KcSEsozxyM/qPSvirW/2bmmnciFgSc9K/RXWvAM2oRSLglXGCMVzNz8C1lJIi6+q0uVEwqOO5+f2\r\n" +
            " k/s2StqkZ8oud3TbX2x+wz8G7rwBqUGo6l5Ygxs8vq3POfQdK3LX4IfYrwP5P3favRvDdo/hvQY/\r\n" +
            " KQKfNC4x7GhQSNVWlPRnvWk3dpdSRmONyzAHcST/AFryr4y2P2vxXrUMCL9qhVJoxj/WIVBH4hlb\r\n" +
            " 8x61ueHvE7WNzCJ8hAQPm/8Ar1zHxZ1waf43ub5Tua0lVZf9qB1Ab8ihb/gNSlZnbOadNEXw81BL\r\n" +
            " vwvai1bMa5VcdgCRirOrak9zIsOnyGQKMhuT+HPNY+kzR6NPOtoAkUs5kVRwMsBn9a2NOMczS3K/\r\n" +
            " KJXOBnO3Bxj8wat6mSlpa5laF4DtrTUDqF3bQS6jNy13KoaRB/dQnlVHoOpyepp3ivxe+mw7tMcR\r\n" +
            " 2enOJJXHWaRTkKPYHkn147GrGu6rKjJaaWxNzc5G88+Sn8T49ugHckds1z/xHt0tvA93HbhwixhQ\r\n" +
            " Aeeo/WhbmM5cqbR0EXx0v5NKjk8uybcu4ZrQ8IftBXOtaza6c2mWZmvpUt1cMflLEKD+ua8x8K6a\r\n" +
            " knhy2VlkYiMZB6jNdl8G9Ft7f4lWM0mEWwDXMpbI42lUHHQliCM9QjUWVrs4qNevWqRjGW59Logj\r\n" +
            " QKvAUYFLWbbeIoLn/VuD+NXorhZR8pBrnPqDO18yeW3k5zXmPjd9RCv9l3Z9q9fmgEo+YZqjdeHY\r\n" +
            " LkfvEB/CgD5A+KfhvUfGWmXGneIbT7bZT/fikGQccgj0IPcV5fZfB7VvCoKaDBbeSpyiy2hZl65G\r\n" +
            " VdRg5PYV9/XHw+s52y0S/lVd/hbp8n3oEP4VlUoU63xxuVGbjsz4H8SaZ4vvLYw20dtboM7WiglD\r\n" +
            " AH3831APT2rHjl8Z6N8ttZWbx4UMDbvl8DAyfM+o6V+hUnwh01+tvH+VQSfBTS5PvW0fP+zWf1Si\r\n" +
            " vsjc5Pdn5s+J7rxzq08Pn2sX2aI7nhSJwsx56nfwD6DHT8Ks23xb8baDj7N4ZsX2cjiQZ4x61+i8\r\n" +
            " nwI0eQ/NaxH/AICKrv8As96K/wB60i/75FP6pR/lJ5mflX+0H4o+Ivxt0X+zr/TotPsjkvHDG7eY\r\n" +
            " fclqvfsh/HTxt+xh4Ll0fwn4Js9W8+Z55Z5pZImkZmJyQoPQYH4Cv0/f9m7Q362cP/fIqF/2YtAf\r\n" +
            " 71nD/wB8itIUoQ+FA25Llb0Ph25/4KyfFW3XCfDPTTjpi+mH/stZ95/wV9+LSIyr8L9NYH1vpf8A\r\n" +
            " 4ivux/2VvDz9bKA/8AFRP+yZ4cfrYwf98CtuZmXso/02fnL4g/4Kj/FbVrgvP8L9MwTkqLuTn/xy\r\n" +
            " s9v+CoXxLhOG+EemOB2OoSj/ANp1+kz/ALInhputhb/98Co2/Y98Mt/zD7f/AL4FHOw9lH+mz82v\r\n" +
            " +HqfxJhP/JG9Lb2/tKX/AON1bsf+CuXxL09iY/gtpRz/ANRSb/41X6Ln9jnwz/0D7f8A74FIf2N/\r\n" +
            " DH/QPt/++BRzMfs49j4C0v8A4LRfE7TnyvwO0dz33arNz/5CrYj/AOC43xPYc/AjQR/3FJv/AIzX\r\n" +
            " 3N/wxv4Y/wCgfbf98Cgfsc+Gf+gfb/8AfApczHyI+HR/wWy+JV0f3vwJ0A/9xOX/AONVND/wWY+I\r\n" +
            " s4+b4F+Hx/3EZv8A41X2+v7Hvhkf8w+3/wC+BUi/sieGl6WFv/3wKfMxezj2PiJP+Cufj67+/wDB\r\n" +
            " DQVJ/wCojMf/AGnVXXP+Cl/xA8U6c1ufhPpNoGIYNHfTZH/jtfdafsm+HF6WMH/fAqZP2VvDydLK\r\n" +
            " D/vgUc7E6MH0Pz/s/wBtz4gywCNPh9pyDjrczNj3HHB9xTNC/au+Ld1qmsS+LtE03U4dSmMlvGYH\r\n" +
            " iazQ4/d7g37wfKDkjJJbnmv0Ij/Zi0CP7tnD/wB8ip4/2btDQ8WcP/fIp+0kR9Xp9vzPhS0/aX8e\r\n" +
            " 3enLbT+FbHaE2bt8u7HQc569Oa6TQf2mPHlvZxQnwzp7CJQu5zKSfcndyfevs1P2e9FT7tpF/wB8\r\n" +
            " irEfwI0eP7ttF/3yKPaSBYamuh8iaZ8fvGjTPI/hrS98uMkrKeB0H3unJP41s/8AC5/Fur25hvvD\r\n" +
            " mjmF/vL5cuT+O+vqiP4K6XH922iH/ARU8fwh01OlvH+VHOx+wp9j5p0/4neIYdPEOmeGNCtmIwZD\r\n" +
            " DM5A9h5oGfrke1HhRfECXjyT+a0kzb5HIwXP4cD2A4HQV9Op8LdPT7sCflU8Hw8soGysKflUuTZU\r\n" +
            " aUIO8VY848EPqRCfat/416h4eMnlr52antfDcFt/q0A/Cr0NssX3RikaEtFFFABRRRQAUUUUAFFF\r\n" +
            " FABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUU\r\n" +
            " AFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQB\r\n" +
            " /9k=\r\n" +
            "item5.X-ABRELATEDNAMES;type=pref:Jenny\r\n" +
            "item5.X-ABLabel:Spouse\r\n" +
            "X-ABUID:6B29A774-D124-4822-B8D0-2780EC117F60\\:ABPerson\r\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Doe", contact.getSurName());
        assertEquals("John", contact.getGivenName());
        assertEquals("Richter,James", contact.getMiddleName());
        assertEquals("Mr.", contact.getTitle());
        assertEquals("Sr.", contact.getSuffix());
        assertEquals("Mr. John Richter,James Doe Sr.", contact.getDisplayName());
        assertEquals("Johny", contact.getNickname());
        assertEquals("Jon", contact.getYomiFirstName());
        assertEquals("Dow", contact.getYomiLastName());
        assertEquals("IBM", contact.getCompany());
        assertEquals("Accounting", contact.getDepartment());
        assertEquals("Money Counter", contact.getPosition());
        assertEquals("john.doe@ibm.com", contact.getEmail1());
        assertEquals("905-777-1234", contact.getTelephoneBusiness1());
        assertEquals("905-666-1234", contact.getTelephoneHome1());
        assertEquals("905-555-1234", contact.getCellularTelephone1());
        assertEquals("905-888-1234", contact.getFaxHome());
        assertEquals("905-999-1234", contact.getFaxBusiness());
        assertEquals("905-111-1234", contact.getTelephonePager());
        assertEquals("Silicon Alley 5,", contact.getStreetHome());
        assertEquals("New York", contact.getCityHome());
        assertEquals("New York", contact.getStateHome());
        assertEquals("12345", contact.getPostalCodeHome());
        assertEquals("United States of America", contact.getCountryHome());
        assertEquals("Street4" + NEWLINE + "Building 6" + NEWLINE + "Floor 8", contact.getStreetBusiness());
        assertEquals("New York", contact.getCityBusiness());
        assertEquals("12345", contact.getPostalCodeBusiness());
        assertEquals("USA", contact.getCountryBusiness());
        String expectedNote =
            "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, " +
            "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE " +
            "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, " +
            "SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR " +
            "SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, " +
            "WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE " +
            "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue"
        ;
        assertEquals(expectedNote, contact.getNote());
        assertEquals("http://www.ibm.com", contact.getURL());
        assertEquals(TimeTools.D("2012-06-06 00:00:00", TimeZones.UTC), contact.getBirthday());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
    }

    public void testImportVCard13() throws Exception {
        /*
         * https://github.com/mangstadt/ez-vcard/blob/master/src/test/resources/ezvcard/io/text/John_Doe_MS_OUTLOOK.vcf
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:2.1\r\n" +
            "N;LANGUAGE=en-us:Doe;John;Richter,James;Mr.;Sr.\r\n" +
            "FN:Mr. John Richter James Doe Sr.\r\n" +
            "NICKNAME:Johny\r\n" +
            "ORG:IBM;Accounting\r\n" +
            "TITLE:Money Counter\r\n" +
            "NOTE:THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD \'\'AS IS\'\' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GEORGE EL-HADDAD OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\r\n" +
            "TEL;WORK;VOICE:(905) 555-1234\r\n" +
            "TEL;HOME;VOICE:(905) 666-1234\r\n" +
            "ADR;WORK;PREF:;;Cresent moon drive;Albaney;New York;12345;United States of America\r\n" +
            "LABEL;WORK;PREF;ENCODING=QUOTED-PRINTABLE:Cresent moon drive=0D=0A=\r\n" +
            "Albaney, New York 12345\r\n" +
            "ADR;HOME:;;Silicon Alley 5,;New York;New York;12345;United States of America\r\n" +
            "LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
            "New York, New York 12345\r\n" +
            "X-MS-OL-DEFAULT-POSTAL-ADDRESS:2\r\n" +
            "URL;WORK:http://www.ibm.com\r\n" +
            "ROLE:Counting Money\r\n" +
            "BDAY:19800322\r\n" +
            "X-MS-ANNIVERSARY:20110113\r\n" +
            "EMAIL;PREF;INTERNET:john.doe@ibm.cm\r\n" +
            "X-MS-IMADDRESS:johny5@aol.com\r\n" +
            "PHOTO;TYPE=JPEG;ENCODING=BASE64:\r\n" +
            " /9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQY\r\n" +
            " GBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYa\r\n" +
            " KCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAAR\r\n" +
            " CAAaACADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAA\r\n" +
            " AgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkK\r\n" +
            " FhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWG\r\n" +
            " h4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl\r\n" +
            " 5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREA\r\n" +
            " AgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYk\r\n" +
            " NOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOE\r\n" +
            " hYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk\r\n" +
            " 5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDzS20gtj5a1R4feMqJYnQlQwDAjII4Irub\r\n" +
            " PQTx8ldNYacfs6213F59sM7R0eL1Kn+nT+dZyk1sQ2zyUaBkfc/SpB4ZOPuc/Su61vSo5tTf\r\n" +
            " SrZySEDKWQgyFgSowSBgEDdz7Z71S8ITNB4luPD2szQvdvM4t/KYsoGNwUNgZwAecCpVWLfK\r\n" +
            " NO56ba2sSqpIGD0Na1vDBxwKwYifU1dhJx1NJq5m1cw/Enw9Gt6ydRbXLmNljMcUW0FYgSCc\r\n" +
            " YI9KXwX4EXwz4hm1STUhdM8RiCtGM4JByWz7DpXQ72/vH86idm/vH86ShZ3uNXtY/9k=\r\n" +
            "\r\n" +
            "X-MS-OL-DESIGN;CHARSET=utf-8:<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>\r\n" +
            "X-MS-MANAGER:Big Blue\r\n" +
            "X-MS-ASSISTANT:Jenny\r\n" +
            "REV:20120305T131933Z\r\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Doe", contact.getSurName());
        assertEquals("John", contact.getGivenName());
        assertEquals("Richter James", contact.getMiddleName());
        assertEquals("Mr.", contact.getTitle());
        assertEquals("Sr.", contact.getSuffix());
        assertEquals("Mr. John Richter James Doe Sr.", contact.getDisplayName());
        assertEquals("Johny", contact.getNickname());
        assertEquals("IBM", contact.getCompany());
        assertEquals("Accounting", contact.getDepartment());
        assertEquals("Money Counter", contact.getPosition());
        String expectedNote =
            "THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED " +
            "TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL " +
            "GEORGE EL-HADDAD OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL " +
            "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR " +
            "BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT " +
            "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE " +
            "POSSIBILITY OF SUCH DAMAGE."
        ;
        assertEquals(expectedNote, contact.getNote());
        assertEquals("(905) 555-1234", contact.getTelephoneBusiness1());
        assertEquals("(905) 666-1234", contact.getTelephoneHome1());
        assertEquals("Cresent moon drive", contact.getStreetBusiness());
        assertEquals("Albaney", contact.getCityBusiness());
        assertEquals("New York", contact.getStateBusiness());
        assertEquals("12345", contact.getPostalCodeBusiness());
        assertEquals("United States of America", contact.getCountryBusiness());
        assertEquals("Cresent moon drive\r\nAlbaney, New York 12345", contact.getAddressBusiness());
        assertEquals("Silicon Alley 5", contact.getStreetHome());
        assertEquals("New York", contact.getCityHome());
        assertEquals("New York", contact.getStateHome());
        assertEquals("12345", contact.getPostalCodeHome());
        assertEquals("United States of America", contact.getCountryHome());
        assertEquals("Silicon Alley 5,\r\nNew York, New York 12345", contact.getAddressHome());
        assertEquals("http://www.ibm.com", contact.getURL());
        assertEquals("Counting Money", contact.getProfession());
        assertEquals(TimeTools.D("1980-03-22 00:00:00", TimeZones.UTC), contact.getBirthday());
//        assertEquals(TimeTools.D("2011-01-13 00:00:00", TimeZones.UTC), contact.getAnniversary());
        assertEquals("john.doe@ibm.cm", contact.getEmail1());
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
        assertEquals("Big Blue", contact.getManagerName());
        assertEquals("Jenny", contact.getAssistantName());
    }

}
