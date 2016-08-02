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
import java.util.LinkedList;
import java.util.List;
import org.yaml.snakeyaml.Yaml;
import com.openexchange.subscribe.crawler.internal.PagePart;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;
import com.openexchange.subscribe.crawler.internal.Step;

/**
 * {@link GenericSubscribeServiceForYahooComTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForYahooComTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForYahooCom() {

        // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("yahoo.com");
        crawler.setId("com.openexchange.subscribe.crawler.yahoocom");
        crawler.setCrawlerApiVersion(616);
        crawler.setPriority(8);
        List<Step<?, ?>> steps = new LinkedList<Step<?, ?>>();

        String crapBefore = "[^0-9\\+\\(\\)]*";

        steps.add(new LoginPageByFormActionStep(
            "Log in",
            "http://address.yahoo.com/index.php",
            "",
            "",
            "https://login.yahoo.com/config/login?",
            "login",
            "passwd",
            "(.*contact_list.*)",
            1,
            ""));
        steps.add(new PageByUrlStep("We are not automatically redirected so we have to call this manually", "http://address.yahoo.com/"));
        steps.add(new PageByLinkRegexStep("Click on Classic Contacts", ".*contact_list.*"));
        steps.add(new AnchorsByLinkRegexStep("Get the links to all contact detail pages", "", ".*detailed_contact.*", true));
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
        pageParts.add(new PagePart("<dt>[\\s]*(Title|Titel):[\\s]*<\\/dt>[\\s]*<dd>[\\s]*<div>[\\s]*([^<]*)(<\\/div>)","title"));//
        pageParts.add(new PagePart("(?s)(Address|Adresse):(?:[^>]*>){3}\\s*("+VALID_ADDRESS_PART+")(<br \\/>)", "street_business", true));
        pageParts.add(new PagePart("(?s)(Address|Adresse):(?:[^>]*>){5}\\s*([0-9]*)()", "postal_code_business", true));
        pageParts.add(new PagePart("(?s)(Address|Adresse):(?:[^>]*>){5}\\s*[0-9]*\\s*([^0-9<]*)(<)", "city_business"));
        // add a filler to be sure we are in the instant messenger part
        pageParts.add(new PagePart("(<h2>Instant Messenger</h2>)"));
        pageParts.add(new PagePart("(AIM|Google Talk|Skype|Windows Live|Yahoo):[\\s]*<\\/dt>[\\s]*<dd>[\\s]*<div>[\\s]*([^<]*)(<\\/div>)","instant_messenger1",1));
        // add a filler to be sure we are in the personal address
        pageParts.add(new PagePart("(<h2>(Personal|Pers.nliche Daten)</h2>)"));
        pageParts.add(new PagePart("(?s)(Address|Adresse):(?:[^>]*>){3}\\s*("+VALID_ADDRESS_PART+")(<br \\/>)", "street_home", true));
        pageParts.add(new PagePart("(?s)(Address|Adresse):(?:[^>]*>){5}\\s*([0-9]*)()", "postal_code_home", true));
        pageParts.add(new PagePart("(?s)(Address|Adresse):(?:[^>]*>){5}\\s*[0-9]*\\s*([^0-9<]*)(<)", "city_home"));
        pageParts.add(new PagePart("(Birthday|Geburtstag):[^0-9]*([0-9]{2})(\\/)","birthday_month"));
        pageParts.add(new PagePart("()([0-9]{2})(\\/)","birthday_day"));
        pageParts.add(new PagePart("()([0-9]{4})(<)","birthday_year"));
        PagePartSequence sequence = new PagePartSequence(pageParts, "");
        steps.add(new ContactObjectsByHTMLAnchorsAndPagePartSequenceStep("Get each contacts details", sequence, "", ""));

        Workflow workflow = new Workflow(steps);
        //workflow.setUseThreadedRefreshHandler(true);
        crawler.setWorkflowString(new Yaml().dump(workflow));

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }
}
