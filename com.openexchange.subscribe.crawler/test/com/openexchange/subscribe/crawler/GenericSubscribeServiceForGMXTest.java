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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import org.ho.yaml.Yaml;

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
        List<Step> steps = new LinkedList<Step>();

        steps.add(new LoginPageStep(
            "Log in",
            "https://www.gmx.de",
            "",
            "",
            "login1",
            "id",
            "p",
            "https://service.gmx.net",        
            ""));
        steps.add(new PageByLinkRegexStep("Click on the addressbook link in the menu to the left.", "https:\\/\\/service\\.gmx\\.net\\/de\\/cgi\\/g\\.fcgi/addressbook.*"));
        steps.add(new PageByLinkRegexStep("Click on the options link in the menu to the left.", "https:\\/\\/service\\.gmx\\.net\\/de/cgi/addrbk\\.fcgi.*site=options"));
        steps.add(new PageByLinkRegexStep("Click on the import/export link in the middle of the screen.", "https:\\/\\/service\\.gmx\\.net\\/de/cgi/addrbk\\.fcgi.*site=importexport"));
        steps.add(new UnexpectedPageByMultiselectStep("Select Microsoft Outlook 2002 in the form and submit it.", "export_form", "", 0, "dataformat", "o2002", "b_export"));
        HashMap<Integer, String> fieldMapping = new HashMap<Integer,String>();
        fieldMapping.put(0, "title");
        fieldMapping.put(1, "first_name");
        fieldMapping.put(2, "last_name");
        fieldMapping.put(3, "company");
        fieldMapping.put(4, "department");
        fieldMapping.put(5, "position");
        fieldMapping.put(6, "street_business");
        fieldMapping.put(7, "city_business");
        fieldMapping.put(8, "postal_code_business");
        fieldMapping.put(9, "country_business");
        fieldMapping.put(10, "street_home");
        fieldMapping.put(11, "city_home");
        fieldMapping.put(12, "postal_code_home");
        fieldMapping.put(13, "country_home");
        fieldMapping.put(14, "street_other");        
        fieldMapping.put(17, "city_other");
        fieldMapping.put(18, "postal_code_other");
        fieldMapping.put(19, "country_other");
        fieldMapping.put(20, "fax_business");
        fieldMapping.put(21, "telephone_business1");
        fieldMapping.put(22, "fax_home");
        fieldMapping.put(23, "telephone_home1");
        fieldMapping.put(24, "cellular_telephone1");
        fieldMapping.put(25, "fax_other");
        fieldMapping.put(26, "telephone_other");
        fieldMapping.put(27, "cellular_telephone2");
        fieldMapping.put(28, "email1");
        fieldMapping.put(29, "email2");
        fieldMapping.put(30, "email3");
        fieldMapping.put(31, "birthday");
        fieldMapping.put(32, "note");
        fieldMapping.put(33, "url");        
        boolean ignoreFirstLine = true;
        steps.add(new ContactsByCsvFileStep("Map csv fields to Contact-Fields", ignoreFirstLine, fieldMapping));

        Workflow workflow = new Workflow(steps);
        crawler.setWorkflowString(Yaml.dump(workflow));

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler);
        // uncomment this if the if the crawler description was updated to get the new config-files
        //dumpThis(crawler, crawler.getDisplayName());
    }
}
