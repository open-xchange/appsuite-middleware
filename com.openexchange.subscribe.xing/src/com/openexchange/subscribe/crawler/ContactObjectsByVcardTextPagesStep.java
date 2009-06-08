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
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * This step takes TextPages (sourcecode of a HtmlPage) that each contain a vcard and converts them to ContactObjects for OX
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactObjectsByVcardTextPagesStep extends AbstractStep implements
		Step<ContactObject[], List<TextPage>> {
	
	private List<TextPage> pages;
	private ContactObject[] contactObjectsArray;
	
	public ContactObjectsByVcardTextPagesStep() {
		
	}

	public void execute(WebClient webClient) {
		Vector<ContactObject> contactObjects = new Vector<ContactObject>();
		final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
		
		for (TextPage page : pages) {
			byte[] vcard = page.getContent().getBytes();
    		final VersitDefinition def = Versit.getDefinition("text/x-vcard");
    		VersitDefinition.Reader versitReader;
				
    		try {
    			versitReader = def.getReader(new ByteArrayInputStream(vcard), "ISO-8859-1");
    			VersitObject versitObject = def.parse(versitReader);
    			ContactObject contactObject = oxContainerConverter.convertContact(versitObject);
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
		return LIST_OF_TEXT_PAGES;
	}

	public String outputType() {
		return LIST_OF_CONTACT_OBJECTS;
	}

	public ContactObject[] getOutput() {
		return contactObjectsArray;
	}

	public void setInput(List<TextPage> input) {
		this.pages = input;
	}

	public List<TextPage> getPages() {
		return pages;
	}

	public void setPages(List<TextPage> pages) {
		this.pages = pages;
	}

	public ContactObject[] getContactObjectsArray() {
		return contactObjectsArray;
	}

	public void setContactObjectsArray(ContactObject[] contactObjectsArray) {
		this.contactObjectsArray = contactObjectsArray;
	}

}
