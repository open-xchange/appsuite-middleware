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
 * {@link GenericSubscribeServiceForWebDeTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForWebDeTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForWebDe() {
        // insert valid credentials here
        String username = "peter.mueller1131@web.de";
        String password = "r0deld0del";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("web.de");
        crawler.setId("com.openexchange.subscribe.crawler.webde");
        crawler.setCrawlerApiVersion(618);
        crawler.setPriority(3);
        List<Step<?, ?>> steps = new LinkedList<Step<?, ?>>();

        steps.add(new LoginPageByFormActionReturningStringStep(
            "Log in",
            "http://web.de",
            "",
            "",
            "https:\\/\\/login.web.de\\/intern\\/login\\?hal=true",
            "username",
            "password",
            2,
            "web.de",
            "",
            "Augenblick bitte"));
        //(https:\\/\\/uas.web.de\\/intern\\/jump\\?serviceID=mobile.web.mail.webde.live&session.*)
//        steps.add(new ConditionalPageByLinkRegexStep(
//            "Hit the link to web.de freemail if the user did not log out last time.",
//            "(/online/frame.*)"));
//        steps.add(new PageByLinkRegexStep("Use the site without frames", "/online/[^h]+.*"));
//        steps.add(new PageByLinkRegexStep("This is a fancy page that does not appear in the browser.", "/online/adressbuch/.*"));
//        steps.add(new PageByLinkRegexStep("Go to the contacts list.", "/online/adressbuch/.*"));
        steps.add(new PageByUrlStep("Go to the address book", "https://mm.web.de/contacts?2"));
        steps.add(new AnchorsByLinkRegexStep("Get each contact.", "", "adr_show.*", true));
        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart("(width=\"282\"><b>)"+VALID_NAME+"(<)", "display_name"));
        pageParts.add(new PagePart("E\\-Mail\\-Adressen"));
        pageParts.add(new PagePart("(<b>Privat</b></td><td class=\"b\" width=\"277\"><a href=[^>]*>)"+VALID_EMAIL_REGEX+"(<)", "email2"));
        pageParts.add(new PagePart("(<b>B.ro</b></td><td class=\"b\" width=\"277\"><a href=[^>]*>)"+VALID_EMAIL_REGEX+"(<)", "email1"));
        pageParts.add(new PagePart("Telefonnummern"));
        pageParts.add(new PagePart("(<b>Privat<\\/b><\\/td><td class=\"b\" width=\"277\">)"+VALID_PHONE_REGEX+"(<)", "telephone_home1"));
        pageParts.add(new PagePart("(<b>Mobil<\\/b><\\/td><td class=\"b\" width=\"277\">[^>]*>)"+VALID_PHONE_REGEX+"(<)", "cellular_telephone1"));
        pageParts.add(new PagePart("(<b>B.ro<\\/b><\\/td><td class=\"b\" width=\"277\">)"+VALID_PHONE_REGEX+"(<)", "telephone_business1"));
        pageParts.add(new PagePart("Adressen"));
        pageParts.add(new PagePart("(<b>Privat<\\/b><\\/td><td class=\"b\" width=\"507\">)([^<]*)(<br)", "street_home"));
        pageParts.add(new PagePart("(>)([0-9]*)()", "postal_code_home"));
        pageParts.add(new PagePart("()([a-zA-Z\u00e4\u00f6\u00fc]*)(<br)", "city_home"));
        pageParts.add(new PagePart("(>)([a-zA-Z\u00e4\u00f6\u00fc]*)(<\\/td>)", "country_home"));
        pageParts.add(new PagePart("(<b>B.ro<\\/b><\\/td><td class=\"b\" width=\"507\">)([^<]*)(<br)", "street_business"));
        pageParts.add(new PagePart("(>)([0-9]*)()", "postal_code_business"));
        pageParts.add(new PagePart("()([a-zA-Z\u00e4\u00f6\u00fc]*)(<br)", "city_business"));
        pageParts.add(new PagePart("(>)([a-zA-Z\u00e4\u00f6\u00fc]*)(<\\/td>)", "country_business"));

        PagePartSequence sequence = new PagePartSequence(pageParts, "");

        steps.add(new ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(
            "Get the information of each contact from the individual webpages",
            sequence));

        Workflow workflow = new Workflow(steps);
        workflow.setUseThreadedRefreshHandler(true);
        crawler.setWorkflowString(new Yaml().dump(workflow));

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        //uncomment this if the if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }

}
