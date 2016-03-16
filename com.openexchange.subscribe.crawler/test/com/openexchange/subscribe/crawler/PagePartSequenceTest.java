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

package com.openexchange.subscribe.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.subscribe.crawler.internal.PagePart;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class PagePartSequenceTest extends TestCase {

    public void testPagePartSequence() {
        String page = getStringFromFile("test-resources/GoogleMailResultTestPage.txt");

        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart(
            "(<input[\\s]{1}name=ct_nm[\\s]{1}id=ct_nm[\\s]{1}size=[0-9]{2}[\\s]{1}value=\")([a-zA-Z\\s]*)(\"><br></td>)",
            "display_name"));
        pageParts.add(new PagePart(
            "(<input[\\s]{1}name=ct_em[\\s]{1}id=ct_em[\\s]{1}size=[0-9]{2}[\\s]{1}value=\")([a-z@A-Z\\.]*)(\"><br></td>)",
            "email1"));
        pageParts.add(new PagePart(
            "(<textarea[\\s]{1}name=ctf_n[\\s]{1}id=ctf_n[\\s]{1}cols=[0-9]{1,2}[\\s]{1}rows=[0-9]{1,2}>)([^<]*)(</textarea>)",
            "note"));
        pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_00_00_e\"[\\s]value=\")([a-zA-Z@\\.]*)(\">)", "email2"));
        pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_00_01_p\"[\\s]value=\")([+0-9\\s]*)(\">)", "telephone_home1"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_00_02_m\"[\\s]value=\")([+0-9\\s]*)(\">)",
            "cellular_telephone2"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_00_03_i\"[\\s]value=\")([a-z\\.@A-Z]*)(\">)",
            "instant_messenger2"));
        pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_01_00_e\"[\\s]value=\")([a-zA-Z@\\.]*)(\">)", "email3"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_01_01_p\"[\\s]value=\")([+0-9\\s]*)(\">)",
            "telephone_business1"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_01_02_m\"[\\s]value=\")([+0-9\\s]*)(\">)",
            "cellular_telephone1"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_01_03_i\"[\\s]value=\")([a-z\\.@A-Z]*)(\">)",
            "instant_messenger1"));
        PagePartSequence sequence = new PagePartSequence(pageParts, page);
        HashMap<String, String> map = sequence.retrieveInformation();

        assertEquals("Kevin Ortiz", map.get("display_name"));
        assertEquals("topshooter@gmail.com", map.get("email1"));
        assertEquals("some personal note", map.get("note"));
        assertEquals("personal@mail.com", map.get("email2"));
        assertEquals("+49 221 987456", map.get("telephone_home1"));
        assertEquals("+49 171 234765", map.get("cellular_telephone2"));
        assertEquals("personal@im.com", map.get("instant_messenger2"));
        assertEquals("business@mail.com", map.get("email3"));
        assertEquals("+49 221 123000", map.get("telephone_business1"));
        assertEquals("+49 171 123000", map.get("cellular_telephone1"));
        assertEquals("business@im.com", map.get("instant_messenger1"));

    }

    private String getStringFromFile(String filename) {
        File file = new File(filename);
        StringBuilder contents = new StringBuilder();

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String page = contents.toString();
        return page;
    }

    public void testWebDeSubpage(){
        String page =">51379\u00a0Leverkusen<br>Germany</td>";
        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart("(>)([0-9]*)()", "postal_code_home"));
        pageParts.add(new PagePart("()([a-zA-Z\u00e4\u00f6\u00fc]*)(<br)", "city_home"));
        pageParts.add(new PagePart("(>)([a-zA-Z\u00e4\u00f6\u00fc]*)(<\\/td>)", "country_home"));

        PagePartSequence sequence = new PagePartSequence(pageParts, page);
        HashMap<String, String> map = sequence.retrieveInformation();

        assertEquals("51379", map.get("postal_code_home"));
        assertEquals("Leverkusen", map.get("city_home"));
        assertEquals("Germany", map.get("country_home"));
    }

    public void testRetrieveMultipleInformation(){
        String page ="<FIRST_NAME>Peter</FIRST_NAME><LAST_NAME>Mueller</LAST_NAME>\n"
            +"<FIRST_NAME>Hans-Georg</FIRST_NAME><LAST_NAME>Walter</LAST_NAME>";
        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart("(<FIRST_NAME>)([a-zA-Z\u00e4\u00f6\u00fc\\-]*)(<\\/FIRST_NAME>)", "first_name"));
        pageParts.add(new PagePart("(<LAST_NAME>)([a-zA-Z\u00e4\u00f6\u00fc\\-]*)(<\\/LAST_NAME>)", "last_name"));

        PagePartSequence sequence = new PagePartSequence(pageParts, page);
        ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) sequence.retrieveMultipleInformation();
        boolean peterFound = false;
        boolean hansGeorgFound = false;

        for (HashMap<String, String> result : results){
            if (result.containsKey("first_name") && result.containsKey("last_name")){
                if (result.get("first_name").equals("Peter") && result.get("last_name").equals("Mueller")){
                    peterFound = true;
                } else if (result.get("first_name").equals("Hans-Georg") && result.get("last_name").equals("Walter")){
                    hansGeorgFound = true;
                }
            }
        }

        assertTrue("contact Peter Mueller was not retrieved", peterFound);
        assertTrue("contact Hans-Georg Walter was not retrieved", hansGeorgFound);
    }

    public void testYahooCom(){
        String page = getStringFromFile("test-resources/YahooCom.html");
        String VALID_NAME = GenericSubscribeServiceTestHelpers.VALID_NAME;
        String VALID_EMAIL_REGEX = GenericSubscribeServiceTestHelpers.VALID_EMAIL_REGEX;
        String VALID_PHONE_REGEX = GenericSubscribeServiceTestHelpers.VALID_PHONE_REGEX;
        String VALID_ADDRESS_PART = GenericSubscribeServiceTestHelpers.VALID_ADDRESS_PART;

        String crapBefore = "[^0-9\\+\\(\\)]*";

        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart("(<h1>\\s)"+VALID_NAME+"(</h1>)","display_name"));
        pageParts.add(new PagePart("(qa_compose1[^>]*>)"+VALID_EMAIL_REGEX+"(<)","email1"));
        // add a filler to be sure we are in the phone numbers part
        pageParts.add(new PagePart("(<h2>(Phone|Telefon)</h2>)"));
        pageParts.add(new PagePart("(Home|Privat):"+crapBefore+VALID_PHONE_REGEX+"()","telephone_home1"));
        pageParts.add(new PagePart("(Work|Gesch.ftlich):"+crapBefore+VALID_PHONE_REGEX+"()","telephone_business1"));
        pageParts.add(new PagePart("(Mobile|Handy):"+crapBefore+VALID_PHONE_REGEX+"()","cellular_telephone1"));
        // add a filler to be sure we are in the work part
        pageParts.add(new PagePart("(<h2>(Work|Gesch.ftlich)</h2>)"));
        pageParts.add(new PagePart("<dt>[\\s]*(Company|Firma):[\\s]*<\\/dt>[\\s]*<dd>[\\s]*<div>[\\s]*([^<]*)(<\\/div>)","company"));
        pageParts.add(new PagePart("<dt>[\\s]*(Title|Titel):[\\s]*<\\/dt>[\\s]*<dd>[\\s]*<div>[\\s]*([^<]*)(<\\/div>)","title"));
        pageParts.add(new PagePart("(Address|Adresse):[\\s]*<\\/dt>[\\s]*<dd>[\\s]*<div>[\\s]*("+VALID_ADDRESS_PART+")(<br \\/>)", "street_business"));
        pageParts.add(new PagePart("(<br \\/>)*([0-9]*)(\\s)", "postal_code_business"));
        pageParts.add(new PagePart("()("+VALID_ADDRESS_PART+")()", "city_business"));
        // add a filler to be sure we are in the instant messenger part
        pageParts.add(new PagePart("(<h2>Instant Messenger</h2>)"));
        pageParts.add(new PagePart("(AIM|Google Talk|Skype|Windows Live|Yahoo):[\\s]*<\\/dt>[\\s]*<dd>[\\s]*<div>[\\s]*([^<]*)(<\\/div>)","instant_messenger1",1));
        // add a filler to be sure we are in the personal address
        pageParts.add(new PagePart("(<h2>(Personal|Pers.nliche Daten)</h2>)"));
        pageParts.add(new PagePart("(Address|Adresse):[\\s]*<\\/dt>[\\s]*<dd>[\\s]*<div>[\\s]*("+VALID_ADDRESS_PART+")(<br \\/>)*", "street_home"));
        pageParts.add(new PagePart("(<br \\/>\\s)([0-9]*)(\\s)", "postal_code_home"));
        pageParts.add(new PagePart("()("+VALID_ADDRESS_PART+")()", "city_home"));
        pageParts.add(new PagePart("(Birthday|Geburtstag):[^0-9]*([0-9]{2})(\\/)","birthday_month"));
        pageParts.add(new PagePart("()([0-9]{2})(\\/)","birthday_day"));
        pageParts.add(new PagePart("()([0-9]{4})(<)","birthday_year"));
        PagePartSequence sequence = new PagePartSequence(pageParts, "");
        sequence.setPage(page);
        Map<String, String> map = sequence.retrieveInformation();

        assertTrue("display_name not good, should be 'Willi Winzig' but is '"+map.get("display_name")+"'",map.get("display_name").contains("Willi Winzig"));
        assertTrue("telephone_home1 should be in here", map.containsKey("telephone_home1"));
        assertTrue("telephone_home1 should be '06331 148973' but is '"+map.get("telephone_home1")+"'", map.get("telephone_home1").equals("06331 148973"));
        assertTrue("There should not be a work phone number", !map.containsKey("telephone_business1"));
        assertTrue("cellular_telephone1 should be '01520 12345678' but is '"+map.get("cellular_telephone1")+"'", map.get("cellular_telephone1").equals("01520 12345678"));
        System.out.println(map.get("street_business"));
        System.out.println(map.get("postal_code_business"));
        System.out.println(map.get("city_business"));
        System.out.println(map.get("street_home"));
        System.out.println(map.get("postal_code_home"));
        System.out.println(map.get("city_home"));
//        assertTrue("Some Address should be there", map.containsKey("address_note"));
//        assertTrue("The preferred Address should be the business one not "+ map.get("address_note"), map.get("address_note").contains("Totenweg"));
        assertTrue("Instant Messenger should be in", map.containsKey("instant_messenger1"));
        assertTrue("Instant Messenger has the right value", map.get("instant_messenger1").contains("hss7ps"));
    }
}
