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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * This step takes HtmlPages that each contain contact information and converts them to ContactObjects for OX
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
//TODO: Expand this to handle single fields (consisting of fieldname and a regex) of a contact by extracting them from a webpage
public class ContactObjectsByHTMLAnchorsMultithreadedStep extends AbstractStep implements
		Step<Contact[], List<HtmlAnchor>> {
	
	private List<HtmlAnchor> anchors;
	private Contact[] contactObjectsArray;
	private static final ContactSanitizer SANITIZER = new ContactSanitizer();
	private String vcardUrl, pictureUrl;
	List<Contact> synchronizedContacts;
	
	public ContactObjectsByHTMLAnchorsMultithreadedStep(String description, String vcardUrl, String pictureUrl) {
		this.description = description;
		this.vcardUrl = vcardUrl;
		this.pictureUrl = pictureUrl;
		
		synchronizedContacts = Collections.synchronizedList(new ArrayList<Contact>());
		
	}

	public void execute(WebClient webClient) {
		List<Contact> contactObjects = new ArrayList<Contact>();
		
		
		// create a threadpool that works through all links with 1 thread for every 10 contacts
		
		ThreadPool pool = new ThreadPool(anchors);
		executedSuccessfully = true;
		
		synchronized (synchronizedContacts){
			contactObjectsArray = new Contact[synchronizedContacts.size()];
		    for (int i=0; i<contactObjectsArray.length && i< synchronizedContacts.size(); i++){
		    	contactObjectsArray[i] = synchronizedContacts.get(i);
		    }
		}    
	    
		
	}
	
	public void getOneResult (HtmlAnchor anchor, WebClient webClient) {
		Contact contact = new Contact();		
    		try {
    			
    			VersitDefinition.Reader versitReader;
    			OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
    			VersitDefinition def = Versit.getDefinition("text/x-vcard");
    			
    			String encoding = "ISO-8859-1";
    			HtmlPage page = webClient.getPage("https://www.linkedin.com"+anchor.getHrefAttribute());
    			
    			TextPage vcardPage = null;
    			String imageUrl = "";
    			//System.out.println("***** vcardURL : " + vcardUrl);
    			for (HtmlAnchor link: page.getAnchors()){
    				//if there is a vcard linked
    				
    				//System.out.println("***** Links href : " + link.getHrefAttribute());
    				if (link.getHrefAttribute().contains(vcardUrl)) {
    					vcardPage = link.click();
    				}
    			}
    			
    			//if there is a contact picture in an <img>-tag get its Url
    			if (page.getWebResponse().getContentAsString().contains(pictureUrl)){
	    			int startIndex = page.getWebResponse().getContentAsString().indexOf(pictureUrl);
	    			String substring = page.getWebResponse().getContentAsString().substring(startIndex);
	    			imageUrl = substring.substring(0, substring.indexOf("\""));
	    		}	
				
    			
    			if (vcardPage != null){
    				byte[] vcard = vcardPage.getWebResponse().getContentAsBytes();
    				
    				versitReader = def.getReader(new ByteArrayInputStream(vcard), encoding);
        			VersitObject versitObject = def.parse(versitReader);
        			contact = oxContainerConverter.convertContact(versitObject);
    			}
    			
    			//add the image from a url to the contact
    			if (!imageUrl.equals("")){
    				OXContainerConverter.loadImageFromURL(contact, imageUrl);
    			}	
    			
    			//TODO: Add other form of getting content from a page here (a list of regex2contactobject-field mappings ordered by appearance on the page)
    			
    			SANITIZER.sanitize(contact);
    			System.out.println("Contacts Display Name : " + contact.getDisplayName());
    			synchronized (synchronizedContacts){
    				synchronizedContacts.add(contact);
    				System.out.println("***** Number of contacts so far : " +Integer.toString(synchronizedContacts.size()));
    			}
    			
    		} catch (final VersitException e){
    			e.printStackTrace();
    			this.exception = e;
    		} catch (ConverterException e) {
				e.printStackTrace();
				this.exception = e;
			} catch (IOException e) {
				e.printStackTrace();
				this.exception = e;
			}
	}

	public String inputType() {
		return LIST_OF_HTML_PAGES;
	}

	public String outputType() {
		return LIST_OF_CONTACT_OBJECTS;
	}

	public Contact[] getOutput() {
		return contactObjectsArray;
	}

	public void setInput(List<HtmlAnchor> input) {
		this.anchors = input;
	}

	public List<HtmlAnchor> getAnchors() {
		return anchors;
	}

	public void setAnchors(List<HtmlAnchor> pages) {
		this.anchors = pages;
	}

	public Contact[] getContactObjectsArray() {
		return contactObjectsArray;
	}

	public void setContactObjectsArray(Contact[] contactObjectsArray) {
		this.contactObjectsArray = contactObjectsArray;
	}
	
	public class WorkerThread extends Thread {
		private ThreadPool pool;
		private final WebClient webClient;

		  public WorkerThread(ThreadPool thePool) {
		    pool = thePool;
		    this.webClient = new WebClient(BrowserVersion.FIREFOX_2);
			// Javascript needs to be disabled for security reasons
			webClient.setJavaScriptEnabled(false);
			// simple login
			HtmlPage loginPage;
			try {
				loginPage = webClient.getPage("https://www.linkedin.com/secure/login");
				HtmlForm loginForm = loginPage.getFormByName("login");
			    HtmlTextInput userfield = loginForm.getInputByName("session_key");
			    userfield.setValueAttribute("juergen.geck@gmx.de");
			    HtmlPasswordInput passwordfield = loginForm.getInputByName("session_password");
			    passwordfield.setValueAttribute("npj817");
			    final HtmlPage pageAfterLogin = (HtmlPage)loginForm.submit(null);
			    System.out.println("***** Worker logged in");
			} catch (FailingHttpStatusCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }

		  public void run() {  
			
		    while (true) {
		      // blocks until job
		      HtmlAnchor link = pool.getNext();
		      try {
		    	  getOneResult(link, webClient);
		      } catch (Exception e) {
		        // Ignore exceptions thrown from jobs
		        e.printStackTrace();
		      }
		    }
		  }
		}

		public class ThreadPool {
		  private LinkedList<HtmlAnchor> tasks = new LinkedList<HtmlAnchor>();

		  public ThreadPool(List<HtmlAnchor> links) {
			for (HtmlAnchor link : links){
				tasks.addLast(link);
			}
			int maxNumberOfWorkers = links.size() / 10;
		    for (int i = 0; i < maxNumberOfWorkers; i++) {
		    	System.out.println("***** No. of Workers : " + Integer.toString(i+1));
		      Thread thread = new WorkerThread(this);
		      thread.start();
		    }
		  }

		  public HtmlAnchor getNext() {
		    HtmlAnchor returnVal = null;
		    synchronized (tasks) {
		    	while (tasks.isEmpty()) {
		            try {
		              tasks.wait();
		            } catch (InterruptedException ex) {
		              System.err.println("Interrupted");
		            }
		          }
		          returnVal = tasks.removeFirst();
		    }
		    return returnVal;
		  }
	}
	

}
