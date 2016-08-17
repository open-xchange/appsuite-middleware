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
 * Works but preferred way for crawling Google is API crawling.
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForGoogleWebTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForGoogleMail() {
        // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("GoogleMail");
        crawler.setId("com.openexchange.subscribe.crawler.googlemail");
        List<Step<?, ?>> steps = new LinkedList<Step<?, ?>>();

        steps.add(new LoginPageByFormActionStep(
            "Login to Google Mail",
            "https://mail.google.com",
            "",
            "",
            "https://www.google.com/accounts/ServiceLoginAuth?service=mail",
            "Email",
            "Passwd",
            "(\\?v=cl)|(\\?ui=html&zy=e)",
            1,
            "https://mail.google.com"));
        steps.add(new PageByUrlStep("Get the basic html view of all contacts", "https://mail.google.com/mail/?ui=html&zy=e&v=cl&pnl=a"));
        steps.add(new AnchorsByLinkRegexStep("Get all contacts on a page", "NO_SUBPAGES", "(\\?v=ct&ct_id=[0-9a-zA-Z]*)"));
        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart(
            "(<input[\\s]{1}name=[\"]*ct_nm[\"]*[\\s]{1}id=[\"]*ct_nm[\"]*[\\s]{1}size=[\"]*[0-9]{2}[\"]*[\\s]{1}value=\")"+VALID_NAME+"(\"><br></td>)",
            "display_name"));
        pageParts.add(new PagePart(
            "(<input[\\s]{1}name=[\"]*ct_em[\"]*[\\s]{1}id=[\"]*ct_em[\"]*[\\s]{1}size=[\"]*[0-9]{2}[\"]*[\\s]{1}value=\")"+VALID_EMAIL_REGEX+"(\"><br></td>)",
            "email1"));
        pageParts.add(new PagePart(
            "(<textarea[\\s]{1}name=[\"]*ctf_n[\"]*[\\s]{1}id=[\"]*ctf_n[\"]*[\\s]{1}cols=[\"]*[0-9]{1,2}[\"]*[\\s]{1}rows=[\"]*[0-9]{1,2}[\"]*>)([^<]*)(</textarea>)",
            "note"));
        pageParts.add(new PagePart("(<input[\\s]size=[\"]*[0-9]{2}[\"]*[\\s]name=\"ctsf_00_00_e\"[\\s]value=\")"+VALID_EMAIL_REGEX+"(\">)", "email2"));
        pageParts.add(new PagePart("(<input[\\s]size=[\"]*[0-9]{2}[\"]*[\\s]name=\"ctsf_00_01_p\"[\\s]value=\")([+0-9\\s]*)(\">)", "telephone_home1"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[\"]*[0-9]{2}[\"]*[\\s]name=\"ctsf_00_02_m\"[\\s]value=\")([+0-9\\s]*)(\">)",
            "cellular_telephone2"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[\"]*[0-9]{2}[\"]*[\\s]name=\"ctsf_00_03_i\"[\\s]value=\")([a-z\\.@A-Z0-9]*)(\">)",
            "instant_messenger2"));
        pageParts.add(new PagePart("(<input[\\s]size=[\"]*[0-9]{2}[\"]*[\\s]name=\"ctsf_01_00_e\"[\\s]value=\")"+VALID_EMAIL_REGEX+"(\">)", "email3"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[\"]*[0-9]{2}[\"]*[\\s]name=\"ctsf_01_01_p\"[\\s]value=\")([+0-9\\s]*)(\">)",
            "telephone_business1"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[\"]*[0-9]{2}[\"]*[\\s]name=\"ctsf_01_02_m\"[\\s]value=\")([+0-9\\s]*)(\">)",
            "cellular_telephone1"));
        pageParts.add(new PagePart(
            "(<input[\\s]size=[\"]*[0-9]{2}[\"]*[\\s]name=\"ctsf_01_03_i\"[\\s]value=\")([a-z\\.@A-Z0-9]*)(\">)",
            "instant_messenger1"));
        PagePartSequence sequence = new PagePartSequence(pageParts, "");
        steps.add(new ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(
            "Get the information of each contact from the individual webpages",
            sequence));

        Workflow workflow = new Workflow(steps);
        crawler.setWorkflowString(new Yaml().dump(workflow));

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler);
        //uncomment this if the if the crawler description was updated to get the new config-files
        //dumpThis(crawler, crawler.getDisplayName());
    }
}
