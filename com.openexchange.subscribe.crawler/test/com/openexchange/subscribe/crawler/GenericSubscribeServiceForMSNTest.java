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
import java.util.LinkedList;
import java.util.List;
import com.openexchange.subscribe.crawler.internal.PagePart;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;
import com.openexchange.subscribe.crawler.internal.Step;

/**
 * {@link GenericSubscribeServiceForMSNTest}
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForMSNTest extends GenericSubscribeServiceTestHelpers {

    public void testMSN() {
        // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("msn.de");
        crawler.setId("com.openexchange.subscribe.crawler.msn.de");
        crawler.setCrawlerApiVersion(618);
        crawler.setMobileUserAgentEnabled(true);
        crawler.setJavascriptEnabled(true);
        crawler.setMultiThreadedHttpConnectionManager(true);
        crawler.setPriority(3);
        
        List<Step> steps = new LinkedList<Step>();        
        
        steps.add(new LoginPageByFormActionStep(
            "Log into the mobile version as the full one does not work",
            "https://mid.live.com/si/login.aspx",
            "",
            "",
            "login.aspx",
            "LoginTextBox",
            "PasswordTextBox",
            ".*allservices.*",
            1,
            "",
            "PasswordSubmit"));
        steps.add(new PageByLinkRegexStep("Click on 'All services'", ".*allservices.*"));
        PageByUrlStep pageByUrlStep = new PageByUrlStep();
        pageByUrlStep.setDescription("Open Contacts");
        pageByUrlStep.setUrl("http://profile.live.com/contacts");
        steps.add(pageByUrlStep);
        steps.add(new AnchorsByLinkRegexStep(
            "Get all contact links. this is just one long list, no subpages here",
            "",
            ".*contactId.*",
            ".*contactId=(.*)",
            true));
        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart("(id=\"ic2_name\"\\s*title=\")" + "([^\"]*)" + "(\")", "display_name"));
        pageParts.add(new PagePart("(tel\\:[^>]*>)"+VALID_PHONE_REGEX+"((?:[^>]*>){4}M<)", "cellular_telephone1"));
        pageParts.add(new PagePart("(tel\\:[^>]*>(?:[^>]*>))"+VALID_PHONE_REGEX+"((?:[^>]*>){4}H<)", "telephone_home1"));
        pageParts.add(new PagePart("(tel\\:[^>]*>(?:[^>]*>))"+VALID_PHONE_REGEX+"((?:[^>]*>){4}W<)", "telephone_business1"));        
        pageParts.add(new PagePart("(mailto[^>]*>)([^<]*)((?:[^>]*>){2}<\\/a>)", "email2"));
        pageParts.add(new PagePart("(mailto[^>]*>)([^<]*)((?:[^>]*>){4}W<)", "email1"));
        //As only one address can be saved in address_note we save private first then overwrite with business as it is more important
        pageParts.add(new PagePart("(Address(?:[^>]*>){3})([^<]*)((?:[^>]*>){4}H<)", "address_note",true));
        pageParts.add(new PagePart("(Address(?:[^>]*>){15})([^<]*)((?:[^>]*>){4}W<)", "address_note",true));
        //(Birthday(?:[^>]*>){3})([^\s]*)
        pageParts.add(new PagePart("(Birthday(?:[^>]*>){3})([^\\s<]*)()", "birthday_month_string",true));
        pageParts.add(new PagePart("(Birthday(?:[^>]*>){3}[^0-9]*)([0-9]*)()", "birthday_day",true));
        pageParts.add(new PagePart("(Birthday(?:[^>]*>){3}[^,]*,\\s)([0-9]*)(<)", "birthday_year",true));
        PagePartSequence sequence = new PagePartSequence(pageParts, "");
        steps.add(new ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(
            "Get the information of each contact from the individual webpages",
            sequence));
        
        crawler.finishUp(steps);

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }

}
