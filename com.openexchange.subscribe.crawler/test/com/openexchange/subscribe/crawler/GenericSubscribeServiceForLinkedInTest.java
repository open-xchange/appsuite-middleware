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

import java.util.ArrayList;
import org.ho.yaml.Yaml;

//import com.openexchange.server.services.ServerServiceRegistry;
//import com.openexchange.timer.TimerService;
//import com.openexchange.timer.internal.TimerImpl;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForLinkedInTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForLinkedInTest() {
        // insert valid credentials here
        String username = "roxyexchanger@ox.io";
        String password = "secret";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("LinkedIn");
        crawler.setId("com.openexchange.subscribe.linkedin");

        // initiate the TimerService for MultiThreading
        // final TimerImpl timer = new TimerImpl();
        // timer.start();
        // ServerServiceRegistry.getInstance().addService(TimerService.class, timer);

        ArrayList<Step> listOfSteps = new ArrayList<Step>();

        listOfSteps.add(new LoginPageStep(
            "Login to www.linkedin.com",
            "https://www.linkedin.com/secure/login",
            "",
            "",
            "login",
            "session_key",
            "session_password",
            "/connections?trk=hb_side_cnts",
            "https://www.linkedin.com"));
        listOfSteps.add(new PageByUrlStep("Get to the contacts list", "http://www.linkedin.com/connections?trk=hb_side_cnts"));
        listOfSteps.add(new PageByUrlStep(
            "Get to the no-javascript contacts list",
            "http://www.linkedin.com/connectionsnojs?trk=cnx_nojslink"));
        listOfSteps.add(new AnchorsByLinkRegexStep(
            "Get all pages that link to a connections profile",
            "(/connectionsnojs\\?split_page=).*",
            "(/profile\\?viewProfile=).*(goback).*"));
        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart(
            "(<img src=\")([a-zA-Z://\\._0-9]*)(\" class=\"photo\" width=\"80\" height=\"80\" alt=\"[a-zA-Z\u00e4\u00f6\u00fc\\s]*\">)",
            "image"));
        pageParts.add(new PagePart("(<h1 class=\"n fn\">)"));
        pageParts.add(new PagePart("(span class=\"given-name\">)([a-zA-Z\u00e4\u00f6\u00fc]*)(</span>)", "first_name"));
        pageParts.add(new PagePart("(span class=\"family-name\">)([a-zA-Z\u00e4\u00f6\u00fc]*)(</span>)", "last_name"));
        pageParts.add(new PagePart("(<p class=\"title\">[\\s]*)([a-zA-Z\u00e4\u00f6\u00fc\\x20]*)([\\s]*</p>)", "title"));
        pageParts.add(new PagePart(
            "<dt>(Phone:|Telefon:|Tel\u00e9fono:|T.l.phone :)</dt>[\\s]*<dd>[\\s]*<p>[\\s]*"+VALID_PHONE_REGEX+"<span class=\"type\">\\((Mobile|mobile)\\)",
            "cellular_telephone1"));
        pageParts.add(new PagePart(
            "<dt>(Phone:|Telefon:|Tel\u00e9fono:|T.l.phone :</dt>[\\s]*<dd>[\\s]*<p>[\\s]*)"+VALID_PHONE_REGEX+"<span class=\"type\">\\((Mobile|mobile)\\)",
            "telephone_home1"));
        pageParts.add(new PagePart(
            "<dt>(Phone:|Telefon:|Tel\u00e9fono:|T.l.phone :)</dt>[\\s]*<dd>[\\s]*<p>[\\s]*"+VALID_PHONE_REGEX+"<span class=\"type\">\\((Mobile|mobile)\\)",
            "telephone_business1"));
        pageParts.add(new PagePart(
            "<dt>(Address:|Adresse:|Adresse :|Direcci.n:)<\\/dt>[\\s]*<dd>[\\s]*<p>(.*\\s.*\\s.*)(<\\/dd>)",
            "address_note"));
        pageParts.add(new PagePart(
            "<dt>(IM:|Mensaje instant.neo:|Messagerie instantan.e :) <\\/dt>[\\s]*<dd>[\\s]*<p>[\\s]*([^<]*)(<\\/p>)",
            "instant_messenger1"));
        
        
        pageParts.add(new PagePart("(mailto:)"+VALID_EMAIL_REGEX+"(\")", "email1"));

        PagePartSequence sequence = new PagePartSequence(pageParts, "");
        listOfSteps.add(new ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(
            "Get the information of each contact from the individual webpages",
            sequence));

        Workflow workflow = new Workflow(listOfSteps);
        crawler.setWorkflowString(Yaml.dump(workflow));

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the crawler description was updated to get the new config-files
        //dumpThis(crawler, crawler.getDisplayName());
    }
}
