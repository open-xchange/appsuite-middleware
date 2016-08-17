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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.yaml.snakeyaml.Yaml;
import com.openexchange.subscribe.crawler.internal.Step;

/**
 * {@link GenericSubscribeServiceForGMXTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForGMXTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForGMX() {
        // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("gmx.de");
        crawler.setId("com.openexchange.subscribe.crawler.gmx");
        crawler.setPriority(2);
        List<Step<?, ?>> steps = new LinkedList<Step<?, ?>>();

        steps.add(new LoginPageByFormActionStep(
            "Log in",
            "http://www.gmx.de",
            "",
            "",
            "https://service.gmx.net/de/cgi/login",
            "id",
            "p",
            ".*/service\\.gmx\\.net\\/de\\/cgi\\/g\\.fcgi/addressbook.*",
            2,
            ""));
        //new ConditionalStep needed "https://service.gmx.net/de/cgi/g.fcgi/application/navigator"
        steps.add(new PageByLinkRegexStep("Click on the addressbook link in the menu to the left.", ".*/service\\.gmx\\.net\\/de\\/cgi\\/g\\.fcgi/addressbook.*"));
        steps.add(new PageByFrameNumberStep("Get the first iframe", 1));
        steps.add(new PageByLinkRegexStep("Click on the manage-Addressbook-Link in the upper right ","categories.*"));
        steps.add(new PageByLinkRegexStep("Click on the Export-link to the left","exportcontacts.*"));

        steps.add(new TextPageByMultiselectStep("Select Microsoft Outlook 2003 in the form and submit it.", "", "exportcontacts", 0, "raw_format", "csv_Outlook2003", "export"));
        HashMap<Integer, String> fieldMapping = new HashMap<Integer,String>();
        fieldMapping.put(0, "last_name");
        fieldMapping.put(1, "title");
        fieldMapping.put(2, "first_name");
        fieldMapping.put(5, "birthday");
        fieldMapping.put(6, "position");
        fieldMapping.put(7, "department");
        fieldMapping.put(8, "company");
        fieldMapping.put(9, "email1");
        fieldMapping.put(11, "email2");
        fieldMapping.put(13, "email3");
        fieldMapping.put(15, "telephone_business1");
        fieldMapping.put(16, "telephone_business2");
        fieldMapping.put(18, "telephone_home1");
        fieldMapping.put(19, "telephone_home1");
        fieldMapping.put(20, "cellular_telephone1");
        fieldMapping.put(21, "cellular_telephone2");
        fieldMapping.put(23, "fax_home");
        fieldMapping.put(24, "fax_business");
        fieldMapping.put(27, "street_business");
        fieldMapping.put(28, "city_business");
        fieldMapping.put(29, "postal_code_business");
        fieldMapping.put(30, "country_business");
        fieldMapping.put(31, "street_home");
        fieldMapping.put(32, "city_home");
        fieldMapping.put(33, "postal_code_home");
        fieldMapping.put(34, "country_home");
        fieldMapping.put(35, "street_other");
        fieldMapping.put(36, "city_other");
        fieldMapping.put(37, "postal_code_other");
        fieldMapping.put(38, "country_other");
        fieldMapping.put(39, "note");

        boolean ignoreFirstLine = true;
        steps.add(new ContactsByCsvFileStep("Map csv fields to Contact-Fields", ignoreFirstLine, fieldMapping));

        Workflow workflow = new Workflow(steps);
        crawler.setWorkflowString(new Yaml().dump(workflow));

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }
}
