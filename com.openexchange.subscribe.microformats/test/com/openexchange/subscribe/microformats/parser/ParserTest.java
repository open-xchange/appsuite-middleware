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

package com.openexchange.subscribe.microformats.parser;

import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;


/**
 * {@link ParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class ParserTest extends TestCase {
    public void testCollect() throws OXException {
        final String text = "<html><head /><body><div class='ox_contact someOtherClass'><span class='ox_bla someOtherClass'>Bla</span><span class='ox_blupp'>Blupp</span></div><div class='ox_contact'><span class='ox_bla'>Bla2</span><span class='ox_blupp'>Blupp2</span></div></body></html>";
        final List<Map<String, String>> parsed = parse(text);

        assertNotNull("Parsed was null", parsed);
        assertEquals("Expected two elements", 2, parsed.size());

        final Map blaMap = parsed.get(0);
        final Map blaMap2 = parsed.get(1);

        assertEquals("Bla", blaMap.get("ox_bla"));
        assertEquals("Blupp", blaMap.get("ox_blupp"));

        assertEquals("Bla2", blaMap2.get("ox_bla"));
        assertEquals("Blupp2", blaMap2.get("ox_blupp"));
    }

    public void testCollectImageSources() throws OXException {
        final String text = "<html><head /><body><div class='ox_contact someOtherClass'><img src=\"http://www.open-xchange.com/bla.png\" class=\"ox_image\" /> <span class='ox_bla someOtherClass'>Bla</span><span class='ox_blupp'>Blupp</span></div><div class='ox_contact'><span class='ox_bla'>Bla2</span><span class='ox_blupp'>Blupp2</span></div></body></html>";
        final List<Map<String, String>> parsed = parse(text);
        final Map blaMap = parsed.get(0);


        assertEquals("Bla", blaMap.get("ox_bla"));
        assertEquals("Blupp", blaMap.get("ox_blupp"));
        assertEquals("http://www.open-xchange.com/bla.png", blaMap.get("ox_image"));

    }

    public void testCollectAnchorHREFs() throws OXException {
        final String text = "<html><head /><body><div class='ox_contact someOtherClass'><a href=\"http://www.open-xchange.com/bla.png\" class=\"ox_file\">Download</a> </div></body></html>";
        final List<Map<String, String>> parsed = parse(text);
        final Map blaMap = parsed.get(0);


        assertEquals("http://www.open-xchange.com/bla.png", blaMap.get("ox_file"));

    }

    public void testCollectDeeplyNested() throws OXException {
        final String text = "<html><head /><body><div class='ox_contact'><div><span class='ox_bla'>Bla</span><span class='ox_blupp bla'>Blupp</span></div><!-- comment --> </div><div class='ox_contact'><div><div><span class='ox_bla'>Bla2</span></div><span class='ox_blupp'>Blupp2</span></div></div></body></html>";
        final List<Map<String, String>> parsed = parse(text);

        assertNotNull("Parsed was null", parsed);
        assertEquals("Expected two elements", 2, parsed.size());

        final Map blaMap = parsed.get(0);
        final Map blaMap2 = parsed.get(1);

        assertEquals("Bla", blaMap.get("ox_bla"));
        assertEquals("Blupp", blaMap.get("ox_blupp"));

        assertEquals("Bla2", blaMap2.get("ox_bla"));
        assertEquals("Blupp2", blaMap2.get("ox_blupp"));
    }

    public void testLong() throws OXException {
        String text = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"   \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">  <html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"de\"> <head>    <title>OX PubSub</title>    </head>" +
                "  <body>    <div id=\"contacts\">       <bla class=\"ox_contact\" id=\"contact_0\">           <tr>            <td class=\"key\">Name:</td>            <td class=\"value\"> <span class=\"ox_title\">Dr.</span>  <span class=\"ox first_name\">Christian</span> <span class=\"ox_last_name\">Mehrens</span>            </td>           </tr>               <!--ox_street_home-->             <tr> <td class=\"key\">Postal code home:</td> <td class=\"ox_postal_code_home value\">58566</td> </tr>  <!--ox_postal_code_home-->          <tr> <td class=\"key\">City home:</td> <td class=\"ox_city_home value\">Kierspe</td> </tr>  <!--ox_city_home-->             <tr> <td class=\"key\">State home:</td> <td class=\"ox_state_home value\">Nordrhein-Westfalen</td> </tr>  <!--ox_state_home-->          <tr> <td class=\"key\">Country home:</td> <td class=\"ox_country_home value\">Deutschland</td> </tr>  <!--ox_country_home\"-->              <tr> <td class=\"key\">Street business:</td> <td class=\"ox_street_business value\">Feldm\u00d4\u00f8\u03a9hleplatz 1</td> </tr>  <!--ox_street_business\"-->            <tr> <td class=\"key\">Postal code business:</td> <td class=\"ox_postal_code_business value\">40545</td> </tr>  <!--ox_postal_code_business\"-->            <tr> <td class=\"key\">City business:</td> <td class=\"ox_city_business value\">D\u00fcsseldorf</td> </tr>  <!--ox_city_business\"-->            <tr> <td class=\"key\">State business:</td> <td class=\"ox_state_business value\">Nordrhein-Westfalen</td> </tr>  <!--ox_state_business\"-->            <tr> <td class=\"key\">Country business:</td> <td class=\"ox_country_business value\">Deutschland</td> </tr>  <!--ox_country_business\"-->           <!--ox_street_other\"-->            <!--ox_postal_code_other\"-->           <!--ox_city_other\"-->              <!--ox_state_other\"-->             <!--ox_country_other\"-->          <tr><td class=\"key\">Birthday:</td><td class=\"ox_birthday value\">1981-05-30</td></tr>             <!--ox_marital_status-->            <!--ox_number_of_children\"-->              <!--ox profession-->            <!--ox nickname-->              <!--ox_first_name-->                       <tr>  <td class=\"key\">Note:</td> <td class=\"ox_note value\">28.05.2009, 16:09 - XING - http://www.xing.com</td> </tr>  <!--ox note-->             <!--ox department-->            <!--ox position-->             <tr> <td class=\"key\">Employee type:</td> <td class=\"ox_employee_type value\">Rechtsanwalt</td>  </tr>  <!--ox_employee_type-->            <!--ox_room_number-->           <!--ox_number_of_employees-->           <!--ox_sales_volume-->              <!--ox_tax_id-->            <!--ox_commercial_register-->           <!--ox branches-->              <!--ox_business_category-->             <!--ox info-->              <!--ox_manager_name-->              <!--ox_assistant_name-->           <tr> <td class=\"key\">Telephone home1:</td> <td class=\"ox_telephone_home1 value\">+49-2359-290905</td> </tr>  <!--ox_telephone_home1-->            <!--ox_telephone_home2-->              <tr> <td class=\"key\">Telephone business1:</td> <td class=\"ox_telephone_business1 value\">+49-211-49790</td> </tr>  <!--ox_telephone_business1-->              <!--ox_telephone_business2-->           <!--ox_telephone_other-->          <tr> <td class=\"key\">Fax business:</td> <td class=\"ox_fax_business value\">+49-211-4979103</td>  </tr>  <!--ox_fax_business-->            <!--telephone_callback-->           <!--ox_telephone_car-->             <!--ox_telephone_company-->             <!--ox_fax_home-->             <tr> <td class=\"key\">Cellular telephone1:</td> <td class=\"ox_cellular_telephone1 value\">+49-172-2520174</td>  </tr>  <!--ox_cellular_telephone1-->          <tr> <td class=\"key\">Cellular telephone2:</td> <td class=\"ox_cellular_telephone2 value\">+49-171-1611455</td> </tr>  <!--ox_cellular_telephone2-->            <!--ox_fax_other-->" +
                "            <tr> <td class=\"key\">Email1:</td> <td class=\"ox_email1 value\">christian.mehrens@freshfields.com</td>  </tr>  <!--ox email1-->           <tr> <td class=\"key\">Email2:</td> <td class=\"ox_email2 value\">christianmehrens@gmx.net</td> </tr> <tr> <td class=\"key\">Company:</td> <td class=\"ox_company value\">Freshfields Bruckhaus Deringer</td> </tr></bla>        <div class=\"back\"><a href=\"#nav\">Back to the top</a></div>       " +
                "</div> </body> </html>";
        final List<Map<String, String>> parsed = parse(text);
        assertNotNull(parsed);
    }

    public void testRemovalOfTrailingWhitespaces() throws OXException{
        final String text =
            "<html><head /><body>" +
        		"<div class='ox_contact'>" +
        		    "<span class='ox_bla'> Bla </span>" +
        		 "</div>" +
        	"</body></html>";
        final List<Map<String, String>> parsed = parse(text);

        assertNotNull("Parsed was null", parsed);
        assertEquals("Expected one element", 1, parsed.size());

        final Map blaMap = parsed.get(0);

        assertEquals("Should remove trailing whitespaces", "Bla", blaMap.get("ox_bla"));
    }

    protected abstract List<Map<String, String>> parse(String text) throws OXException;
}
