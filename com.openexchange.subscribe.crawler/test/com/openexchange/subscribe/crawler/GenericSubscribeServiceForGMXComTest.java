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

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.httpclient.NameValuePair;
import com.openexchange.subscribe.crawler.internal.Step;


/**
 * {@link GenericSubscribeServiceForGMXComTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForGMXComTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForGMXComTest() {
        // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("gmx.com");
        crawler.setId("com.openexchange.subscribe.crawler.gmx.com");
        crawler.setCrawlerApiVersion(618);
        //crawler.setJavascriptEnabled(true);
        crawler.setPriority(12);

        ArrayList<Step<?, ?>> listOfSteps = new ArrayList<Step<?, ?>>();
        listOfSteps.add(new LoginPageByFormActionReturningStringStep("Log into gmx.com", "https://www.gmx.com", "", "", ".*wicket\\:interface.*", "TextfieldEmail", "TextfieldPassword", 1, "", "ButtonLogin", "community=([0-9]*)&lang"));

        ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new NameValuePair("what", "PERSON"));
        parameters.add(new NameValuePair("idList", ""));
        parameters.add(new NameValuePair("format", "csv_Outlook2003_eng"));

        listOfSteps.add(new TextPageByPostRequestStep("Call the export", "https://www.gmx.com/callgate-6.42.4.0/coms8/ImportExportService/exportContacts", parameters, "accountId"));

        HashMap<Integer, String> fieldMapping = new HashMap<Integer,String>();
        fieldMapping.put(0, "last_name");
        fieldMapping.put(1, "title");
        fieldMapping.put(2, "first_name");
        fieldMapping.put(5, "birthday");
        fieldMapping.put(6, "position");
        fieldMapping.put(7, "department");
        fieldMapping.put(8, "company");
        fieldMapping.put(9, "email1");
        fieldMapping.put(10, "email2");
        fieldMapping.put(11, "email3");
        fieldMapping.put(12, "telephone_business1");
        fieldMapping.put(13, "telephone_home1");
        fieldMapping.put(14, "cellular_telephone1");
        fieldMapping.put(15, "cellular_telephone2");
        fieldMapping.put(16, "telephone_other1");
        fieldMapping.put(17, "fax_home");
        fieldMapping.put(18, "fax_business");
        fieldMapping.put(19, "fax_other");
        fieldMapping.put(20, "url");
        fieldMapping.put(21, "street_business");
        fieldMapping.put(22, "city_business");
        fieldMapping.put(23, "postal_code_business");
        fieldMapping.put(24, "country_business");
        fieldMapping.put(25, "street_home");
        fieldMapping.put(26, "city_home");
        fieldMapping.put(27, "postal_code_home");
        fieldMapping.put(28, "country_home");
        fieldMapping.put(29, "street_other");
        fieldMapping.put(30, "city_other");
        fieldMapping.put(31, "postal_code_other");
        fieldMapping.put(32, "country_other");
        fieldMapping.put(33, "note");

        boolean ignoreFirstLine = true;
        listOfSteps.add(new ContactsByCsvFileStep("Map csv fields to Contact-Fields", ignoreFirstLine, fieldMapping));

        crawler.finishUp(listOfSteps);

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }
}
