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
import java.util.Date;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionException;
import junit.framework.TestCase;

public class MultithreadedHtmlUnitTest extends TestCase {

    public static void testMultithreadedHtmlUnit() {
        Date dateBefore = new Date();
        // insert valid poweruser-credentials (preferrably 100+ connections) of your choice here
        String username = "";
        String password = "";

        ArrayList<Step> listOfSteps = new ArrayList<Step>();
        listOfSteps.add(new LoginPageStep(
            "Login to www.linkedin.com",
            "https://www.linkedin.com/secure/login",
            "",
            "",
            "login",
            "session_key",
            "session_password",
            "LinkedIn | Home",
            "https://www.linkedin.com"));
        listOfSteps.add(new PageByUrlStep("Get to the contacts list", "http://www.linkedin.com/connections?trk=hb_side_cnts"));
        listOfSteps.add(new PageByUrlStep(
            "Get to the no-javascript contacts list",
            "http://www.linkedin.com/connectionsnojs?trk=cnx_nojslink"));
        listOfSteps.add(new AnchorsByLinkRegexStep(
            "Get all pages that link to a connections profile",
            "(/connectionsnojs\\?split_page=).*",
            "(/profile\\?viewProfile=).*(goback).*"));
        listOfSteps.add(new ContactObjectsByHTMLAnchorsMultithreadedStep(
            "Extract the contact information from these pages",
            "/addressBookExport?exportMemberVCard",
            "http://media.linkedin.com/mpr/mpr/shrink_80_80"));

        Workflow linkedInWorkflow = new Workflow(listOfSteps);

        Contact[] contacts = new Contact[0];
        try {
            contacts = linkedInWorkflow.execute(username, password);
        } catch (SubscriptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Date dateAfter = new Date();

        assertTrue("There should be some contacts", contacts.length != 0);
        for (Contact contact : contacts) {
            System.out.println("contact retrieved is : " + contact.getDisplayName());
            System.out.println("contacts first name : " + contact.getGivenName());
            System.out.println("contacts last name : " + contact.getSurName());
            System.out.println("contacts email address : " + contact.getEmail1());
            System.out.println("contacts note is : " + contact.getNote());

            System.out.println("----------");
        }
        System.out.println("Number of contacts retrieved : " + Integer.toString(contacts.length));
        System.out.println("Started import at : " + dateBefore.toString());
        System.out.println("Finished import at : " + dateAfter.toString());
    }

}
