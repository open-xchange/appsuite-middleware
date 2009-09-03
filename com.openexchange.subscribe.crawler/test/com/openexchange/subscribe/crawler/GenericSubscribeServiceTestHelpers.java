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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

import org.ho.yaml.Yaml;

import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionException;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public abstract class GenericSubscribeServiceTestHelpers extends TestCase {

	public GenericSubscribeServiceTestHelpers() {
		super();
	}

	public GenericSubscribeServiceTestHelpers(String name) {
		super(name);
	}

	protected void findOutIfThereAreContactsForThisConfiguration(
			String username, String password, CrawlerDescription crawler) {
	            Calendar rightNow = Calendar.getInstance();
	            long before = rightNow.getTime().getTime();
				//create a GenericSubscribeService that uses this CrawlerDescription
			    GenericSubscribeService service = new GenericSubscribeService(crawler.getDisplayName(), crawler.getId(), crawler.getWorkflowString());
				
				Workflow testWorkflow = service.getWorkflow();
				Contact[] contacts = new Contact[0];
				try {
					contacts = testWorkflow.execute(username, password);
				} catch (SubscriptionException e) {
					e.printStackTrace();
				}
				assertTrue("There are no contacts for crawler : " + crawler.getDisplayName(), contacts.length != 0);
				System.out.println("Crawler is : " + crawler.getDisplayName());
				for (Contact contact: contacts) {
					System.out.println("contact retrieved is : " + contact.getDisplayName());
					System.out.println("contacts first name : " + contact.getGivenName());
					System.out.println("contacts last name : " + contact.getSurName());
					System.out.println("contacts title : " + contact.getTitle());
					System.out.println("contacts email address : " + contact.getEmail1());
					System.out.println("contacts mobile phone number : " + contact.getCellularTelephone1());
					System.out.println("contacts birthday : " + contact.getBirthday());					
					System.out.println("contacts picture type : " + contact.getImageContentType());
					System.out.println("contacts city of work : " + contact.getCityBusiness());
					
			        System.out.println("----------");
				}
			    System.out.println("Number of contacts retrieved : " + Integer.toString(contacts.length));
			    rightNow = Calendar.getInstance();
			    long after = rightNow.getTime().getTime();
			    System.out.println("Time : " + Long.toString((after - before)/1000) + " seconds");
			}

	/**
	 * Create a file of this CrawlerDescription for later use
	 * @param crawler
	 */
	protected void dumpThis(CrawlerDescription crawler, String filename) {
		try {
			Yaml.dump(crawler, new File("../open-xchange-development/crawlers/" + filename + ".yml"));
			Yaml.dump(crawler, new File("conf/crawlers/" + filename + ".yml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}