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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * This step takes HtmlPages that each contain contact information and converts them to ContactObjects for OX
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactObjectsByHTMLAnchorsAndPagePartSequenceStep extends AbstractStep implements
		Step<Contact[], List<HtmlAnchor>> {
	
	private List<HtmlAnchor> anchors;
	private Contact[] contactObjectsArray;
	private static final ContactSanitizer SANITIZER = new ContactSanitizer();
	private PagePartSequence pageParts;
	
	
	public ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(String description, PagePartSequence pageParts) {
		this.description = description;
	}
	
	public ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(){
		
	}

	public void execute(WebClient webClient) {
		Vector<Contact> contactObjects = new Vector<Contact>();
		final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
		//int counter=0;
		for (HtmlAnchor anchor : anchors) {			
    		try {
    			//counter ++;
    			//System.out.println("***** Contact No. " + Integer.toString(counter));
    			//System.out.println("Free Memory"+Runtime.getRuntime().freeMemory());
    			HtmlPage page = anchor.click();
    			Contact contact = new Contact();

    			pageParts.setPage(page.getWebResponse().getContentAsString());
    			HashMap<String, String> map = pageParts.retrieveInformation();
    			
    			//set the contact«s information
    			if (map.containsKey("display_name")) contact.setDisplayName(map.get("display_name"));
    			if (map.containsKey("first_name")) contact.setGivenName(map.get("first_name"));
    			if (map.containsKey("last_name")) contact.setSurName(map.get("last_name"));
    			if (map.containsKey("middle_name")) contact.setMiddleName(map.get("middle_name")); 
    			if (map.containsKey("title")) contact.setTitle(map.get("title")); 
    			if (map.containsKey("street_home")) contact.setStreetHome(map.get("street_home"));
    			if (map.containsKey("postal_code_home")) contact.setPostalCodeHome(map.get("postal_code_home"));
    			if (map.containsKey("city_home")) contact.setCityHome(map.get("city_home"));
    			if (map.containsKey("state_home")) contact.setStateHome(map.get("state_home"));
    			if (map.containsKey("country_home")) contact.setCountryHome(map.get("country_home"));
    			if (map.containsKey("street_business")) contact.setStreetBusiness(map.get("street_business"));
    			if (map.containsKey("postal_code_business")) contact.setPostalCodeBusiness(map.get("postal_code_business"));
    			if (map.containsKey("city_business")) contact.setCityBusiness(map.get("city_business"));
    			if (map.containsKey("state_business")) contact.setStateBusiness(map.get("state_business"));
    			if (map.containsKey("country_business")) contact.setCountryBusiness(map.get("country_business"));    			
    			if (map.containsKey("email1")) contact.setEmail1(map.get("email1"));
    			if (map.containsKey("email2")) contact.setEmail2(map.get("email2"));
    			if (map.containsKey("email3")) contact.setEmail3(map.get("email3"));
    			if (map.containsKey("telephone_home1")) contact.setTelephoneHome1(map.get("telephone_home1"));
    			if (map.containsKey("telephone_business1")) contact.setTelephoneBusiness1(map.get("telephone_business1"));    			
    			if (map.containsKey("cellular_telephone1")) contact.setCellularTelephone1(map.get("cellular_telephone1"));
    			if (map.containsKey("cellular_telephone2")) contact.setCellularTelephone2(map.get("cellular_telephone2"));    			
    			if (map.containsKey("fax_home")) contact.setFaxHome(map.get("fax_home"));
    			if (map.containsKey("fax_business")) contact.setFaxBusiness(map.get("fax_business"));
    			if (map.containsKey("company")) contact.setCompany(map.get("company"));
    			if (map.containsKey("position")) contact.setPosition(map.get("position"));
    			if (map.containsKey("employee_type")) contact.setEmployeeType(map.get("employee_type"));
    			if (map.containsKey("department")) contact.setDepartment(map.get("department"));
    			if (map.containsKey("note")) contact.setNote(map.get("note"));    			    			    			
    			if (map.containsKey("profession")) contact.setProfession(map.get("profession"));
    			if (map.containsKey("url")) contact.setURL(map.get("url"));
    			if (map.containsKey("instant_messenger1")) contact.setInstantMessenger1(map.get("instant_messenger1"));
    			if (map.containsKey("instant_messenger2")) contact.setInstantMessenger2(map.get("instant_messenger2"));
    	
    			//TODO: handle birthdays somehow
    			
    			//add the image from a url to the contact
    			if (map.containsKey("image")){
    				OXContainerConverter.loadImageFromURL(contact, map.get("image"));
    			}	
    			
    			SANITIZER.sanitize(contact);
    			contactObjects.add(contact);
    			
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
			executedSuccessfully = true;
		}
		
		contactObjectsArray = new Contact[contactObjects.size()];
	    for (int i=0; i<contactObjectsArray.length && i< contactObjects.size(); i++){
	    	contactObjectsArray[i] = contactObjects.get(i);
	    }
	    
		
	}

	public String inputType() {
		return LIST_OF_HTML_ANCHORS;
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

	public static ContactSanitizer getSANITIZER() {
		return SANITIZER;
	}

	public PagePartSequence getPageParts() {
		return pageParts;
	}

	public void setPageParts(PagePartSequence pageParts) {
		this.pageParts = pageParts;
	}

	
}
