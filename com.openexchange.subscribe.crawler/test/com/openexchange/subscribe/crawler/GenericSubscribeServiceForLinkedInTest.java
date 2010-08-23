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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import org.ho.yaml.Yaml;
import com.openexchange.subscribe.crawler.internal.PagePart;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;
import com.openexchange.subscribe.crawler.internal.Step;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForLinkedInTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForLinkedInTest() {
        // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("LinkedIn");
        crawler.setId("com.openexchange.subscribe.linkedin");
        crawler.setCrawlerApiVersion(614);
        crawler.setPriority(5);
        crawler.setQuirkyCookieQuotes(true);

        ArrayList<Step> steps = new ArrayList<Step>();

        steps.add(new StringByOAuthRequestStep(
            "",
            "",
            "vEPBnxJvXvqf9NsBby0kZ1hcgQCM7JBO7iCjlw4KIDhw_7lwPIln7zIvtP3dbL-i",
            "Ra7yTqolxUk_6UVpIAIsbv6kwLpIZCdNeUYxAA1n2Lnf05Dkr7D41dw-ivK-z4vA",
            "https://api.linkedin.com/uas/oauth/requestToken",
            // this may be api.linkedin.com instead of www.linkedin.com in the future (as originally documented by linkedin)
            "https://www.linkedin.com/uas/oauth/authorize",
            "https://api.linkedin.com/uas/oauth/accessToken",
            null,
            "email",
            "password",
            "http://api.linkedin.com/v1/people/~/connections:(first-name,last-name,phone-numbers,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url)"));

         ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
         pageParts.add(new PagePart("<person>"));
         pageParts.add(new PagePart("(<first-name>)([^<]*)(</first-name>)", "first_name"));
         pageParts.add(new PagePart("(<last-name>)([^<]*)(</last-name>)", "last_name"));
         pageParts.add(new PagePart("(<phone-type>work</phone-type>(?:\\s)*<phone-number>)([^<]*)(</phone-number>)", "telephone_business1"));
         pageParts.add(new PagePart("(<phone-type>mobile</phone-type>(?:\\s)*<phone-number>)([^<]*)(</phone-number>)", "cellular_telephone1"));
         pageParts.add(new PagePart("(<im-account-type>)([^<]*)(</im-account-type>)","instant_messenger1_type"));
         pageParts.add(new PagePart("(<im-account-name>)([^<]*)(</im-account-name>)","instant_messenger1"));
         
         pageParts.add(new PagePart("(<year>)([0-9]*)(</year>)","birthday_year"));
         pageParts.add(new PagePart("(<month>)([0-9]*)(</month>)","birthday_month_real"));
         pageParts.add(new PagePart("(<day>)([0-9]*)(</day>)","birthday_day"));
         pageParts.add(new PagePart("(<main-address>)([^<]*)(</main-address>)", "address_note"));
         pageParts.add(new PagePart("(<picture-url>)([^<]*)(</picture-url>)", "image"));
         pageParts.add(new PagePart("</person>"));
        
         PagePartSequence sequence = new PagePartSequence(pageParts, "", "</person>");
         steps.add(new ContactObjectsByStringAndPagePartSequenceStep(
         "Get the information of each contact from the xml-file returned by the API",
         sequence));

        crawler.finishUp(steps);

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }
}
