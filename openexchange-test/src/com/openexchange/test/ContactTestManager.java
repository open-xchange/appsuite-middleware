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

package com.openexchange.test;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.contact.action.SearchRequest;
import com.openexchange.ajax.contact.action.SearchResponse;
import com.openexchange.ajax.contact.action.UpdateRequest;
import com.openexchange.ajax.contact.action.UpdatesRequest;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.CommonUpdatesResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.tools.servlet.AjaxException;

/**
 * This class and ContactObject should be all that is needed to write contact-related tests. 
 * If multiple users are needed use multiple instances of this class. Examples of tests using this class can be found in ExemplaryContactTestManagerTest.java
 * 
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
*/
public class ContactTestManager {
	private Vector<ContactObject> insertedOrUpdatedContacts;
	private AJAXClient client;
	private ContactParser contactParser;
	private ContactWriter contactWriter;
	
	public ContactTestManager(AJAXClient client) {
		this.client = client;
		insertedOrUpdatedContacts = new Vector<ContactObject>();
		contactParser = new ContactParser();
		try {
			contactWriter = new ContactWriter(client.getValues().getTimeZone());
		} catch (AjaxException e) {
			fail("AjaxException during initialisation of ContactTestManager: "+e.getMessage());
		} catch (IOException e) {
			fail("IOException during initialisation of ContactTestManager: "+e.getMessage());
		} catch (SAXException e) {
			fail("SAXException during initialisation of ContactTestManager: "+e.getMessage());
		} catch (JSONException e) {
			fail("JSONException during initialisation of ContactTestManager: "+e.getMessage());
		}
	}

	/**
	 * Creates a contact via HTTP-API and updates it with new id,
	 * timestamp and all other information that is updated after
	 * such requests. Remembers this contact for cleanup later.
	 *
	 */
	public ContactObject insertContactOnServer(ContactObject contactToCreate){
		InsertRequest request = new InsertRequest(contactToCreate);
		InsertResponse response = null;
		try {
			response = client.execute(request);
		} catch (AjaxException e) {
			fail("AjaxException during contact creation: "+e.getMessage());
		} catch (IOException e) {
			fail("IOException during contact creation: "+e.getMessage());
		} catch (SAXException e) {
			fail("SAXException during contact creation: "+e.getMessage());
		} catch (JSONException e) {
			fail("JSONException during contact creation: "+e.getMessage());
		}
		response.fillObject(contactToCreate);
		insertedOrUpdatedContacts.add(contactToCreate);
		return contactToCreate;
	}
	
	/**
	 * Create multiple contacts via the HTTP-API at once
	 */
	public void insertContactsOnServer(ContactObject[] contacts) {
		for (int i=0; i<contacts.length; i++) {
			this.insertContactOnServer(contacts[i]);
		}
	}
	
	/**
	 * Updates a contact via HTTP-API
	 * and returns the same contact for convenience
	 */
	public ContactObject updateContactOnServer(ContactObject contact){
		UpdateRequest request = new UpdateRequest(contact);
		try {
			client.execute(request);
			remember(contact);
		} catch (AjaxException e) {
			fail("AjaxException while updating contact with ID " + contact.getObjectID()+ ": " + e.getMessage());
		} catch (IOException e) {
			fail("IOException while updating contact with ID " + contact.getObjectID()+ ": " + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException while updating contact with ID " + contact.getObjectID()+ ": " + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException while updating contact with ID " + contact.getObjectID()+ ": " + e.getMessage());
		}
		return contact;
	}
	
	/**
	 * Deletes a contact via HTTP-API
	 * 
	 */
	public void deleteContactOnServer(ContactObject contactToDelete){
		deleteContactOnServer(contactToDelete, true);
	}
	
	/**
	 * Deletes a contact via HTTP-API
	 * 
	 */
	public void deleteContactOnServer(ContactObject contactToDelete, boolean failOnError){
		try{
		    contactToDelete.setLastModified( new Date( Long.MAX_VALUE ) );
			DeleteRequest request = new DeleteRequest(contactToDelete);
			client.execute(request);
		} catch (AjaxException e) {
			if (failOnError)
				fail("AjaxException while deleting contact with ID " + contactToDelete.getObjectID()+ ": " + e.getMessage());
		} catch (IOException e) {
			if (failOnError)
				fail("IOException while deleting contact with ID " + contactToDelete.getObjectID()+ ": " + e.getMessage());
		} catch (SAXException e) {
			if (failOnError)
				fail("SAXException while deleting contact with ID " + contactToDelete.getObjectID()+ ": " + e.getMessage());
		} catch (JSONException e) {
			if (failOnError)
				fail("JSONException while deleting contact with ID " + contactToDelete.getObjectID()+ ": " + e.getMessage());
		}
	}
	
	/**
	 * Get a contact via HTTP-API with an existing ContactObject
	 */
	public ContactObject getContactFromServer(ContactObject contact){
		return getContactFromServer(contact.getParentFolderID(), contact.getObjectID(), true);
	}
	
	/**
	 * Get a contact via HTTP-API with an existing ContactObject
	 */
	public ContactObject getContactFromServer(ContactObject contact, boolean failOnError){
		return getContactFromServer(contact.getParentFolderID(), contact.getObjectID(), failOnError);
	}
	
	/**
	 * Get a contact via HTTP-API with no existing ContactObject
	 */
	public ContactObject getContactFromServer(final int folderId, final int objectId ) {
		return getContactFromServer( folderId, objectId, true);
	}
	
	/**
	 * Get a contact via HTTP-API with no existing ContactObject
	 */
	public ContactObject getContactFromServer(final int folderId, final int objectId, boolean failOnError ) {
		ContactObject returnedContact = null;
		GetRequest request = new GetRequest(folderId, objectId);
		GetResponse response = null;
		try {
			response = (GetResponse) client.execute(request);
			returnedContact = response.getContact();
		} catch (AjaxException e) {
			if (failOnError)
				fail("AjaxException while getting contact with ID " + objectId + ": " + e.getMessage());
		} catch (IOException e) {
			if (failOnError)
				fail("IOException while getting contact with ID " + objectId + ": " + e.getMessage());
		} catch (SAXException e) {
			if (failOnError)
				fail("SAXException while getting contact with ID " + objectId + ": " + e.getMessage());
		} catch (JSONException e) {
			if (failOnError)
				fail("JSONException while getting contact with ID " + objectId + ": " + e.getMessage());
		} catch (OXException e) {
			if (failOnError)
				fail("OXException while getting contact with ID " + objectId + ": " + e.getMessage());
		}
		return returnedContact;
	}

	/**
	 * removes all contacts inserted or updated by this Manager
	 */
	public void cleanUp(){
		for(ContactObject contact: insertedOrUpdatedContacts){
			deleteContactOnServer(contact);
		}
	}
	
	/**
	 * get all contacts in one folder via the HTTP-API
	 */
	public ContactObject[] getAllContactsOnServer (int folderId) {
		Vector <ContactObject> allContacts = new Vector<ContactObject>();
		AllRequest request = new AllRequest (folderId, new int [] {ContactObject.OBJECT_ID});
		try {
			CommonAllResponse response = client.execute(request);
			final JSONArray data = (JSONArray) response.getResponse().getData();
			for (int i=0; i < data.length(); i++) {
				JSONArray temp = (JSONArray) data.optJSONArray(i);
				int tempObjectId = temp.getInt(0);
				int tempFolderId = temp.getInt(1);
				ContactObject tempContact = getContactFromServer(tempFolderId, tempObjectId);
				allContacts.add(tempContact);
			}
		} catch (AjaxException e) {
			fail("AjaxException occured while getting all contacts for folder with id: " + folderId + ": " + e.getMessage());
		} catch (IOException e) {
			fail("IOException occured while getting all contacts for folder with id: " + folderId + ": " + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException occured while getting all contacts for folder with id: " + folderId + ": " + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException occured while getting all contacts for folder with id: " + folderId + ": " + e.getMessage());
		}
		ContactObject[] contactArray = new ContactObject[allContacts.size()];
		allContacts.copyInto(contactArray);
		return contactArray;
	}
	
	/**
	 * get all contacts specified by multiple int-arrays with 2 slots each (1st slot: folderId, 2nd slot objectId) via the HTTP-API
	 */
	public ContactObject[] listContactsOnServer (final int[]... folderAndObjectIds) {
		Vector <ContactObject> allContacts = new Vector<ContactObject>();
		ListRequest request = new ListRequest(ListIDs.l(folderAndObjectIds), ContactObject.ALL_COLUMNS ,true);
		try {
			CommonListResponse response = client.execute(request);
			final JSONArray data = (JSONArray) response.getResponse().getData();
			this.convertJSONArray2Vector(data, allContacts);
		} catch (AjaxException e) {
			fail("AjaxException occured while getting a list of contacts : " + e.getMessage());
		} catch (IOException e) {
			fail("IOException occured while getting a list of contacts : " + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException occured while getting a list of contacts : " + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException occured while getting a list of contacts : " + e.getMessage());
		} catch (Exception e) {
			fail("Exception occured while getting a list of contacts : " + e.getMessage());
		}
		ContactObject[] contactArray = new ContactObject[allContacts.size()];
		allContacts.copyInto(contactArray);
		return contactArray;
	}
	
	/**
	 * Search for contacts in a folder via the HTTP-API. Use "-1" as folderId to search all available folders
	 */
	public ContactObject [] searchForContactsOnServer (String pattern, int folderId) {
		Vector <ContactObject> allContacts = new Vector<ContactObject>();
		SearchRequest request = new SearchRequest(pattern, folderId, ContactObject.ALL_COLUMNS, true);
		try {
			SearchResponse response = client.execute(request);
			final JSONArray data = (JSONArray) response.getResponse().getData();
			this.convertJSONArray2Vector(data, allContacts);
		} catch (AjaxException e) {
			fail("AjaxException occured while searching for contacts with pattern: "+ pattern + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (IOException e) {
			fail("IOException occured while searching for contacts with pattern: "+ pattern + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException occured while searching for contacts with pattern: "+ pattern + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException occured while searching for contacts with pattern: "+ pattern + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (Exception e) {
			fail("Exception occured while searching for contacts with pattern: "+ pattern + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		}
		ContactObject[] contactArray = new ContactObject[allContacts.size()];
		allContacts.copyInto(contactArray);
		return contactArray;
	}
	
	/**
	 * Get contacts in a folder that were updated since a specific date via the HTTP-API 
	 */
	public ContactObject [] getUpdatedContactsOnServer (int folderId, Date lastModified) {
		Vector <ContactObject> allContacts = new Vector<ContactObject>();
		UpdatesRequest request = new UpdatesRequest(folderId, ContactObject.ALL_COLUMNS, -1, null, lastModified);
		try {
			CommonUpdatesResponse response = (CommonUpdatesResponse) client.execute(request);
			final JSONArray data = (JSONArray) response.getResponse().getData();
			this.convertJSONArray2Vector(data, allContacts);
		} catch (AjaxException e) {
			fail("AjaxException occured while getting contacts updated since date: "+ lastModified + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (IOException e) {
			fail("IOException occured while getting contacts updated since date: "+ lastModified + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException occured while getting contacts updated since date: "+ lastModified + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException occured while getting contacts updated since date: "+ lastModified + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (Exception e) {
			fail("Exception occured while getting contacts updated since date: "+ lastModified + ", in folder: " + Integer.toString(folderId) + e.getMessage());
		}
		ContactObject[] contactArray = new ContactObject[allContacts.size()];
		allContacts.copyInto(contactArray);
		return contactArray;
	}
	
	private void remember (ContactObject contact) {
		for (ContactObject tempContact: insertedOrUpdatedContacts) {
			if (tempContact.getObjectID() == contact.getObjectID()) {
				insertedOrUpdatedContacts.set(insertedOrUpdatedContacts.indexOf(tempContact), contact);
			}
			else {
				insertedOrUpdatedContacts.add(contact);
			}
		}
	}
	
	private void convertJSONArray2Vector(JSONArray data, Vector allContacts) throws JSONException, OXException {
		for (int i=0; i < data.length(); i++) {
			final JSONArray jsonArray = data.getJSONArray(i);
			JSONObject jsonObject = new JSONObject();
			for (int a=0; a < jsonArray.length(); a++){
				if (!"null".equals(jsonArray.getString(a))){
					String fieldname = ContactMapping.columnToFieldName(ContactObject.ALL_COLUMNS[a]);
					jsonObject.put(fieldname, jsonArray.getString(a));
				}	
			}
			ContactObject contactObject = new ContactObject();
			contactParser.parse(contactObject, jsonObject);
			allContacts.add(contactObject);	
		}	
	}
	
	
}
final class ContactMapping extends TestCase{
	
	private static HashMap columns2fields;
	private static HashMap fields2columns;
	
	static {
		fields2columns = new HashMap();
		columns2fields = new HashMap();
		
		try {
			put(ContactFields.ANNIVERSARY, ContactObject.ANNIVERSARY);
			put(ContactFields.ASSISTANT_NAME, ContactObject.ASSISTANT_NAME);
			put(ContactFields.BIRTHDAY, ContactObject.BIRTHDAY);
			put(ContactFields.BRANCHES, ContactObject.BRANCHES);
			put(ContactFields.BUSINESS_CATEGORY, ContactObject.BUSINESS_CATEGORY);
			put(ContactFields.CELLULAR_TELEPHONE1, ContactObject.CELLULAR_TELEPHONE1);
			put(ContactFields.CELLULAR_TELEPHONE2, ContactObject.CELLULAR_TELEPHONE2);
			put(ContactFields.CITY_BUSINESS, ContactObject.CITY_BUSINESS);
			put(ContactFields.CITY_HOME, ContactObject.CITY_HOME);
			put(ContactFields.CITY_OTHER, ContactObject.CITY_OTHER);
			put(ContactFields.COMMERCIAL_REGISTER, ContactObject.COMMERCIAL_REGISTER);
			put(ContactFields.COMPANY, ContactObject.COMPANY);
			//has no equivalent in ContactObject put(ContactFields.CONTAINS_IMAGE1, ContactObject);
			put(ContactFields.COUNTRY_BUSINESS, ContactObject.COUNTRY_BUSINESS);
			put(ContactFields.COUNTRY_HOME, ContactObject.COUNTRY_HOME);
			put(ContactFields.COUNTRY_OTHER, ContactObject.COUNTRY_OTHER);
			put(ContactFields.DEFAULT_ADDRESS, ContactObject.DEFAULT_ADDRESS);
			put(ContactFields.DEPARTMENT, ContactObject.DEPARTMENT);
			put(ContactFields.DISPLAY_NAME, ContactObject.DISPLAY_NAME);
			put(ContactFields.DISTRIBUTIONLIST, ContactObject.DISTRIBUTIONLIST);
			put(ContactFields.EMAIL1, ContactObject.EMAIL1);
			put(ContactFields.EMAIL2, ContactObject.EMAIL2);
			put(ContactFields.EMAIL3, ContactObject.EMAIL3);
			put(ContactFields.EMPLOYEE_TYPE, ContactObject.EMPLOYEE_TYPE);
			put(ContactFields.FAX_BUSINESS, ContactObject.FAX_BUSINESS);
			put(ContactFields.FAX_HOME, ContactObject.FAX_HOME);
			put(ContactFields.FAX_OTHER, ContactObject.FAX_OTHER);
			put(ContactFields.FIRST_NAME, ContactObject.GIVEN_NAME);
			put(ContactFields.IMAGE1, ContactObject.IMAGE1);
			put(ContactFields.INFO, ContactObject.INFO);
			put(ContactFields.INSTANT_MESSENGER1, ContactObject.INSTANT_MESSENGER1);
			put(ContactFields.INSTANT_MESSENGER2, ContactObject.INSTANT_MESSENGER2);
			put(ContactFields.LAST_NAME, ContactObject.SUR_NAME);
			put(ContactFields.LINKS, ContactObject.LINKS);
			put(ContactFields.MANAGER_NAME, ContactObject.MANAGER_NAME);
			put(ContactFields.MARITAL_STATUS, ContactObject.MARITAL_STATUS);
			put(ContactFields.MARK_AS_DISTRIBUTIONLIST, ContactObject.MARK_AS_DISTRIBUTIONLIST);
			put(ContactFields.NICKNAME, ContactObject.NICKNAME);
			put(ContactFields.NOTE, ContactObject.NOTE);
			put(ContactFields.NUMBER_OF_CHILDREN, ContactObject.NUMBER_OF_CHILDREN);
			put(ContactFields.NUMBER_OF_DISTRIBUTIONLIST, ContactObject.NUMBER_OF_DISTRIBUTIONLIST);
			put(ContactFields.NUMBER_OF_EMPLOYEE, ContactObject.NUMBER_OF_EMPLOYEE);
			put(ContactFields.NUMBER_OF_LINKS, ContactObject.NUMBER_OF_LINKS);
			put(ContactFields.POSITION, ContactObject.POSITION);
			put(ContactFields.POSTAL_CODE_BUSINESS, ContactObject.POSTAL_CODE_BUSINESS);
			put(ContactFields.POSTAL_CODE_HOME, ContactObject.POSTAL_CODE_HOME);
			put(ContactFields.POSTAL_CODE_OTHER, ContactObject.POSTAL_CODE_OTHER);
			put(ContactFields.PROFESSION, ContactObject.PROFESSION);
			put(ContactFields.ROOM_NUMBER, ContactObject.ROOM_NUMBER);
			put(ContactFields.SALES_VOLUME, ContactObject.SALES_VOLUME);
			put(ContactFields.SECOND_NAME, ContactObject.MIDDLE_NAME);
			put(ContactFields.SPOUSE_NAME, ContactObject.SPOUSE_NAME);
			put(ContactFields.STATE_BUSINESS, ContactObject.STATE_BUSINESS);
			put(ContactFields.STATE_HOME, ContactObject.STATE_HOME);
			put(ContactFields.STATE_OTHER, ContactObject.STATE_OTHER);
			put(ContactFields.STREET_BUSINESS, ContactObject.STREET_BUSINESS);
			put(ContactFields.STREET_HOME, ContactObject.STREET_HOME);
			put(ContactFields.STREET_OTHER, ContactObject.STREET_OTHER);
			put(ContactFields.SUFFIX, ContactObject.SUFFIX);
			put(ContactFields.TAX_ID, ContactObject.TAX_ID);
			put(ContactFields.TELEPHONE_ASSISTANT, ContactObject.TELEPHONE_ASSISTANT);
			put(ContactFields.TELEPHONE_BUSINESS1, ContactObject.TELEPHONE_BUSINESS1);
			put(ContactFields.TELEPHONE_BUSINESS2, ContactObject.TELEPHONE_BUSINESS2);
			put(ContactFields.TELEPHONE_CALLBACK, ContactObject.TELEPHONE_CALLBACK);
			put(ContactFields.TELEPHONE_CAR, ContactObject.TELEPHONE_CAR);
			put(ContactFields.TELEPHONE_COMPANY, ContactObject.TELEPHONE_COMPANY);
			put(ContactFields.TELEPHONE_HOME1, ContactObject.TELEPHONE_HOME1);
			put(ContactFields.TELEPHONE_HOME2, ContactObject.TELEPHONE_HOME2);
			put(ContactFields.TELEPHONE_IP, ContactObject.TELEPHONE_IP);
			put(ContactFields.TELEPHONE_ISDN, ContactObject.TELEPHONE_ISDN);
			put(ContactFields.TELEPHONE_OTHER, ContactObject.TELEPHONE_OTHER);
			put(ContactFields.TELEPHONE_PAGER, ContactObject.TELEPHONE_PAGER);
			put(ContactFields.TELEPHONE_PRIMARY, ContactObject.TELEPHONE_PRIMARY);
			put(ContactFields.TELEPHONE_RADIO, ContactObject.TELEPHONE_RADIO);
			put(ContactFields.TELEPHONE_TELEX, ContactObject.TELEPHONE_TELEX);
			put(ContactFields.TELEPHONE_TTYTDD, ContactObject.TELEPHONE_TTYTDD);
			put(ContactFields.TITLE, ContactObject.TITLE);
			put(ContactFields.URL, ContactObject.URL);
			//has no equivalent in ContactObject put(ContactFields.USER_ID, ContactObject);
			put(ContactFields.USERFIELD01, ContactObject.USERFIELD01);
			put(ContactFields.USERFIELD02, ContactObject.USERFIELD02);
			put(ContactFields.USERFIELD03, ContactObject.USERFIELD03);
			put(ContactFields.USERFIELD04, ContactObject.USERFIELD04);
			put(ContactFields.USERFIELD05, ContactObject.USERFIELD05);
			put(ContactFields.USERFIELD06, ContactObject.USERFIELD06);
			put(ContactFields.USERFIELD07, ContactObject.USERFIELD07);
			put(ContactFields.USERFIELD08, ContactObject.USERFIELD08);
			put(ContactFields.USERFIELD09, ContactObject.USERFIELD09);
			put(ContactFields.USERFIELD10, ContactObject.USERFIELD10);
			put(ContactFields.USERFIELD11, ContactObject.USERFIELD11);
			put(ContactFields.USERFIELD12, ContactObject.USERFIELD12);
			put(ContactFields.USERFIELD13, ContactObject.USERFIELD13);
			put(ContactFields.USERFIELD14, ContactObject.USERFIELD14);
			put(ContactFields.USERFIELD15, ContactObject.USERFIELD15);
			put(ContactFields.USERFIELD16, ContactObject.USERFIELD16);
			put(ContactFields.USERFIELD17, ContactObject.USERFIELD17);
			put(ContactFields.USERFIELD18, ContactObject.USERFIELD18);
			put(ContactFields.USERFIELD19, ContactObject.USERFIELD19);
			put(ContactFields.USERFIELD20, ContactObject.USERFIELD20);
			
			put(ContactFields.CATEGORIES, ContactObject.CATEGORIES);
			put(ContactFields.COLORLABEL, ContactObject.COLOR_LABEL);
			put(ContactFields.CREATED_BY, ContactObject.CREATED_BY);
			put(ContactFields.CREATION_DATE, ContactObject.CREATION_DATE);
			put(ContactFields.FOLDER_ID, ContactObject.FOLDER_ID);
			put(ContactFields.ID, ContactObject.OBJECT_ID);
			put(ContactFields.LAST_MODIFIED, ContactObject.LAST_MODIFIED);
			put(ContactFields.LAST_MODIFIED_UTC, ContactObject.LAST_MODIFIED_UTC);
			put(ContactFields.MODIFIED_BY, ContactObject.MODIFIED_BY);
			put(ContactFields.NUMBER_OF_ATTACHMENTS, ContactObject.NUMBER_OF_ATTACHMENTS);
			put(ContactFields.PRIVATE_FLAG, ContactObject.PRIVATE_FLAG);
			
			
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private static void put(String fieldname, int column) throws Exception {
		if (!fields2columns.containsKey(fieldname) && !columns2fields.containsKey(column)) {
			fields2columns.put(fieldname, column);
			columns2fields.put(column, fieldname);
		}
		else throw (new Exception("One Part of this combination is also mapped to something else!"));
	}
	
	public static String columnToFieldName (int column) {
		return (String)columns2fields.get(column);
	}
	
	public static int fieldNameToColumn (String fieldname) {
		return Integer.valueOf((Integer)fields2columns.get(fieldname));
	}
}
