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
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.tools.versit.Property;
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
public class ContactObjectsByHTMLPagesStep extends AbstractStep implements
		Step<ContactObject[], List<HtmlPage>> {
	
	private List<HtmlPage> pages;
	private ContactObject[] contactObjectsArray;
	private static final ContactSanitizer SANITIZER = new ContactSanitizer();
	private String vcardUrl, pictureUrl;
	
	public ContactObjectsByHTMLPagesStep(String description, String vcardUrl, String pictureUrl) {
		this.description = description;
		this.vcardUrl = vcardUrl;
		this.pictureUrl = pictureUrl;
	}

	public void execute(WebClient webClient) {
		Vector<ContactObject> contactObjects = new Vector<ContactObject>();
		final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
		final VersitDefinition def = Versit.getDefinition("text/x-vcard");
		VersitDefinition.Reader versitReader;
		String encoding = "ISO-8859-1";
		
		for (HtmlPage page : pages) {			
    		try {
    			ContactObject contactObject = new ContactObject();
    			TextPage vcardPage = null;
    			String imageUrl = "";
    			
    			for (HtmlAnchor link: page.getAnchors()){
    				//if there is a vcard linked
    				if (link.getHrefAttribute().startsWith(vcardUrl)) {
    					vcardPage = link.click();
    				}
    			}
    			
    			//if there is a contact picture in an <img>-tag
    			if (page.getWebResponse().getContentAsString().contains(pictureUrl)){
	    			int startIndex = page.getWebResponse().getContentAsString().indexOf(pictureUrl);
	    			String substring = page.getWebResponse().getContentAsString().substring(startIndex);
	    			imageUrl = substring.substring(0, substring.indexOf("\""));
	    		}	
				
    			
    			if (vcardPage != null){
    				String vcardString = vcardPage.getWebResponse().getContentAsString(encoding);
    				// include the picture url in the vcard if there is one
    				if (!imageUrl.equals("")){
    					int indexEnd = vcardString.indexOf("END:VCARD");
    					String textUntilEnd = vcardString.substring(0, indexEnd);
    					vcardString = textUntilEnd + "PHOTO;VALUE=URI:" + imageUrl + "\n" + "END:VCARD" + "\n";
    				}
    				byte[] vcard = vcardString.getBytes();
    				versitReader = def.getReader(new ByteArrayInputStream(vcard), encoding);
        			VersitObject versitObject = def.parse(versitReader);
        			Property property = versitObject.getProperty("FN");
        			//System.out.println("Full name in the versit object : " + property.getValue().toString());
        			contactObject = oxContainerConverter.convertContact(versitObject);
        			//System.out.println("Full name in the contact object : " +contactObject.getDisplayName());
    			}
    			
    			//TODO: Add other form of getting content from a page here (a list of regex2contactobject-field mappings ordered by appearance on the page)
    			
    			SANITIZER.sanitize(contactObject);
    			contactObjects.add(contactObject);
    			
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
		
		contactObjectsArray = new ContactObject[contactObjects.size()];
	    for (int i=0; i<contactObjectsArray.length && i< contactObjects.size(); i++){
	    	contactObjectsArray[i] = contactObjects.get(i);
	    }
	    
		
	}

	public String inputType() {
		return LIST_OF_HTML_PAGES;
	}

	public String outputType() {
		return LIST_OF_CONTACT_OBJECTS;
	}

	public ContactObject[] getOutput() {
		return contactObjectsArray;
	}

	public void setInput(List<HtmlPage> input) {
		this.pages = input;
	}

	public List<HtmlPage> getPages() {
		return pages;
	}

	public void setPages(List<HtmlPage> pages) {
		this.pages = pages;
	}

	public ContactObject[] getContactObjectsArray() {
		return contactObjectsArray;
	}

	public void setContactObjectsArray(ContactObject[] contactObjectsArray) {
		this.contactObjectsArray = contactObjectsArray;
	}

}
