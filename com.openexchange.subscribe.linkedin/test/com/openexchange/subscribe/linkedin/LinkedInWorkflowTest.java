
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

package com.openexchange.subscribe.linkedin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.ho.yaml.Yaml;

import com.openexchange.exceptions.StringComponent;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.crawler.ContactObjectsByHTMLPagesStep;
import com.openexchange.subscribe.crawler.ContactObjectsByVcardTextPagesStep;
import com.openexchange.subscribe.crawler.LoginPageStep;
import com.openexchange.subscribe.crawler.PageByUrlStep;
import com.openexchange.subscribe.crawler.PagesByLinkRegexStep;
import com.openexchange.subscribe.crawler.Step;
import com.openexchange.subscribe.crawler.TextPagesByLinkStep;
import com.openexchange.subscribe.crawler.Workflow;
import com.openexchange.subscribe.crawler.WorkflowException;
import com.openexchange.subscribe.crawler.WorkflowFactory;

import junit.framework.TestCase;

public class LinkedInWorkflowTest extends TestCase {
	
	public void testLinkedInByCreatingStepsManually() {
		
		// insert valid credentials here
		String username ="roxyexchanger@ox.io";
		String password ="secret";
		LinkedInSubscribeService service = new LinkedInSubscribeService();
		Workflow linkedInWorkflow = service.getWorkflow();
		
		Contact[] contacts = new Contact[0];
		try {
			contacts = linkedInWorkflow.execute(username, password);
		} catch (SubscriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue("There should be some contacts", contacts.length != 0);
		Contact firstContact = contacts[0];
		System.out.println("First contact retrieved is : " + firstContact.getDisplayName());
		Contact lastContact = contacts[contacts.length-1];
        System.out.println("last contact retrieved is : " + lastContact.getDisplayName());
        System.out.println("Number of contacts retrieved : " + Integer.toString(contacts.length));
	}
	
	public static void testInvalidWorkflow() {
		Workflow xingWorkflow = null;
		try {
			// insert valid location for the yml-file here
			// insert valid credentials in the file
			xingWorkflow = WorkflowFactory.createWorkflow("/Users/karstenwill/Desktop/InvalidWorkflow.yml");
			fail("Exception expected");
		} catch (SubscriptionException e) {
			assertEquals("Wrong exception", SubscriptionErrorMessage.INVALID_WORKFLOW.getDetailNumber(), e.getDetailNumber());
		}
	}
	
}