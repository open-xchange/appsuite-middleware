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
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
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
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * This class and ContactObject should be all that is needed to write contact-related tests. If multiple users are needed use multiple
 * instances of this class. Examples of tests using this class can be found in ExemplaryContactTestManagerTest.java
 * 
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - refactoring a bit.
 */
public class ContactTestManager {

    private boolean failOnError;

    private List<Contact> createdEntities;

    private AJAXClient client;

    private ContactParser contactParser;
    
    private TimeZone timeZone;

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean getFailOnError() {
        return failOnError;
    }

    public void setCreatedEntities(List<Contact> createdEntities) {
        this.createdEntities = createdEntities;
    }

    public List<Contact> getCreatedEntities() {
        return createdEntities;
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    private void setTimeZone(TimeZone timezone){
        this.timeZone = timezone;
    }
    
    private TimeZone getTimeZone(){
        return this.timeZone;
    }
    
    public void setContactParser(ContactParser contactParser) {
        this.contactParser = contactParser;
    }

    public ContactParser getContactParser() {
        return contactParser;
    }

    public ContactTestManager(AJAXClient client) throws AjaxException, IOException, SAXException, JSONException {
        this.setClient(client);
        this.setTimeZone(client.getValues().getTimeZone());
        setCreatedEntities(new LinkedList<Contact>());
        setContactParser(new ContactParser());
    }

    /**
     * Creates a contact via HTTP-API and updates it with new id, timestamp and all other information that is updated after such requests.
     * Remembers this contact for cleanup later.
     */
    public Contact newAction(Contact contactToCreate) {
        InsertRequest request = new InsertRequest(contactToCreate);
        InsertResponse response = null;
        try {
            response = getClient().execute(request);
        } catch (Exception e) {
            doExceptionHandling(e, "NewRequest");
        }
        response.fillObject(contactToCreate);
        getCreatedEntities().add(contactToCreate);
        return contactToCreate;
    }

    /**
     * Create multiple contacts via the HTTP-API at once
     */
    public void newAction(Contact[] contacts) {
        for (int i = 0; i < contacts.length; i++) {
            this.newAction(contacts[i]);
        }
    }

    /**
     * Updates a contact via HTTP-API and returns the same contact for convenience
     */
    public Contact updateAction(Contact contact) {
        UpdateRequest request = new UpdateRequest(contact);
        try {
            getClient().execute(request);
            remember(contact);
        } catch (Exception e) {
            doExceptionHandling(e, "UpdateRequest for folder " + contact.getParentFolderID() + " and object " + contact.getObjectID());
        }
        return contact;
    }

    /**
     * Deletes a contact via HTTP-API
     */
    public void deleteAction(Contact contactToDelete) {
        boolean oldValue = this.getFailOnError();
        this.setFailOnError(failOnError);
        try {
            contactToDelete.setLastModified(new Date(Long.MAX_VALUE));
            DeleteRequest request = new DeleteRequest(contactToDelete, getFailOnError());
            getClient().execute(request);
        } catch (Exception e) {
            doExceptionHandling(
                e,
                "DeleteRequest for folder " + contactToDelete.getParentFolderID() + " and object " + contactToDelete.getObjectID());
        } finally {
            this.setFailOnError(oldValue);
        }
        getCreatedEntities().remove(contactToDelete); // TODO: does this find the right contact, or does equals() suck, too?
    }

    /**
     * Get a contact via HTTP-API with an existing ContactObject
     */
    public Contact getAction(Contact contact) {
        return getAction(contact.getParentFolderID(), contact.getObjectID());
    }

    /**
     * Get a contact via HTTP-API with no existing ContactObject
     */
    public Contact getAction(final int folderId, final int objectId) {
        Contact returnedContact = null;
        GetRequest request = new GetRequest(folderId, objectId, getTimeZone());
        GetResponse response = null;
        try {
            response = getClient().execute(request);
            if(response.hasError() && getFailOnError()) 
                throw response.getException();
            returnedContact = response.getContact();
        } catch (Exception e) {
            doExceptionHandling(e, "GetRequest for folder " + folderId + " and object " + objectId);
            return null;
        } 
        return returnedContact;
    }

    /**
     * removes all contacts inserted or updated by this Manager
     */
    public void cleanUp() {
        for (Contact contact : new Vector<Contact>(getCreatedEntities())) {
            boolean old = getFailOnError();
            setFailOnError(false);
            deleteAction(contact);
            setFailOnError(old);
        }
    }

    public Contact[] allAction(int folderId, int[] columns) {
        Vector<Contact> allContacts = new Vector<Contact>();
        AllRequest request = new AllRequest(folderId, columns);
        try {
            CommonAllResponse response = getClient().execute(request);
            final JSONArray data = (JSONArray) response.getResponse().getData();
            for (int i = 0; i < data.length(); i++) {
                JSONArray temp = data.optJSONArray(i);
                int tempObjectId = temp.getInt(0);
                int tempFolderId = temp.getInt(1);
                Contact tempContact = getAction(tempFolderId, tempObjectId);
                allContacts.add(tempContact);
            }
        } catch (Exception e) {
            doExceptionHandling(e, "AllRequest for folder " + folderId);
        }
        Contact[] contactArray = new Contact[allContacts.size()];
        allContacts.copyInto(contactArray);
        return contactArray;

    }

    /**
     * get all contacts in one folder via the HTTP-API
     */
    public Contact[] allAction(int folderId) {
        return allAction(folderId, new int[] { Contact.OBJECT_ID });
    }

    /**
     * get all contacts specified by multiple int-arrays with 2 slots each (1st slot: folderId, 2nd slot objectId) via the HTTP-API
     */
    public Contact[] listAction(final int[]... folderAndObjectIds) {
        Vector<Contact> allContacts = new Vector<Contact>();
        ListRequest request = new ListRequest(ListIDs.l(folderAndObjectIds), Contact.ALL_COLUMNS, true);
        try {
            CommonListResponse response = getClient().execute(request);
            final JSONArray data = (JSONArray) response.getResponse().getData();
            this.convertJSONArray2Vector(data, allContacts);
        } catch (Exception e) {
            doExceptionHandling(e, "ListRequest");
        }
        Contact[] contactArray = new Contact[allContacts.size()];
        allContacts.copyInto(contactArray);
        return contactArray;
    }

    /**
     * Search for contacts in a folder via the HTTP-API. Use "-1" as folderId to search all available folders
     */
    public Contact[] searchAction(String pattern, int folderId) {
        Vector<Contact> allContacts = new Vector<Contact>();
        SearchRequest request = new SearchRequest(pattern, folderId, Contact.ALL_COLUMNS, true);
        try {
            SearchResponse response = getClient().execute(request);
            final JSONArray data = (JSONArray) response.getResponse().getData();
            this.convertJSONArray2Vector(data, allContacts);
        } catch (Exception e) {
            doExceptionHandling(
                e,
                "searching for contacts with pattern: " + pattern + ", in folder: " + Integer.toString(folderId) + e.getMessage());
        }
        Contact[] contactArray = new Contact[allContacts.size()];
        allContacts.copyInto(contactArray);
        return contactArray;
    }

    /**
     * Get contacts in a folder that were updated since a specific date via the HTTP-API
     */
    public Contact[] updatesAction(int folderId, Date lastModified) {
        Vector<Contact> allContacts = new Vector<Contact>();
        UpdatesRequest request = new UpdatesRequest(folderId, Contact.ALL_COLUMNS, -1, null, lastModified);
        try {
            CommonUpdatesResponse response = getClient().execute(request);
            final JSONArray data = (JSONArray) response.getResponse().getData();
            this.convertJSONArray2Vector(data, allContacts);
        } catch (Exception e) {
            doExceptionHandling(e, "UpdateRequest");
        }
        Contact[] contactArray = new Contact[allContacts.size()];
        allContacts.copyInto(contactArray);
        return contactArray;
    }

    private void doExceptionHandling(Exception exception, String action) {
        try {
            throw exception;
        } catch (AjaxException e) {
            if (getFailOnError())
                fail("AjaxException occured during " + action + ": " + e.getMessage());
        } catch (IOException e) {
            if (getFailOnError())
                fail("IOException occured during " + action + ": " + e.getMessage());
        } catch (SAXException e) {
            if (getFailOnError())
                fail("SAXException occured during " + action + ": " + e.getMessage());
        } catch (JSONException e) {
            if (getFailOnError())
                fail("JSONException occured during " + action + ": " + e.getMessage());
        } catch (AbstractOXException e){
            if (getFailOnError())
                fail("AbstractOXException occured during " + action + ": " + e.getMessage());
        } catch (Exception e) {
            if (getFailOnError())
                fail("Unexpected exception occured during " + action + ": " + e.getMessage());
        }

    }

    private void remember(Contact contact) {
        for (Contact tempContact : getCreatedEntities()) {
            if (tempContact.getObjectID() == contact.getObjectID()) {
                getCreatedEntities().set(getCreatedEntities().indexOf(tempContact), contact);
            } else {
                getCreatedEntities().add(contact);
            }
        }
    }

    private void convertJSONArray2Vector(JSONArray data, Vector<Contact> allContacts) throws JSONException, OXException, OXJSONException {
        for (int i = 0; i < data.length(); i++) {
            final JSONArray jsonArray = data.getJSONArray(i);
            JSONObject jsonObject = new JSONObject();
            for (int a = 0; a < jsonArray.length(); a++) {
                if (!"null".equals(jsonArray.getString(a))) {
                    String fieldname = ContactMapping.columnToFieldName(Contact.ALL_COLUMNS[a]);
                    if (fieldname != null)
                        jsonObject.put(fieldname, jsonArray.getString(a));
                }
            }
            Contact contactObject = new Contact();
            getContactParser().parse(contactObject, jsonObject);
            allContacts.add(contactObject);
        }
    }

}

final class ContactMapping extends TestCase {

    private static HashMap<Integer, String> columns2fields;

    private static HashMap<String, Integer> fields2columns;

    static {
        fields2columns = new HashMap<String, Integer>();
        columns2fields = new HashMap<Integer, String>();

        try {
            put(ContactFields.ANNIVERSARY, Contact.ANNIVERSARY);
            put(ContactFields.ASSISTANT_NAME, Contact.ASSISTANT_NAME);
            put(ContactFields.BIRTHDAY, Contact.BIRTHDAY);
            put(ContactFields.BRANCHES, Contact.BRANCHES);
            put(ContactFields.BUSINESS_CATEGORY, Contact.BUSINESS_CATEGORY);
            put(ContactFields.CELLULAR_TELEPHONE1, Contact.CELLULAR_TELEPHONE1);
            put(ContactFields.CELLULAR_TELEPHONE2, Contact.CELLULAR_TELEPHONE2);
            put(ContactFields.CITY_BUSINESS, Contact.CITY_BUSINESS);
            put(ContactFields.CITY_HOME, Contact.CITY_HOME);
            put(ContactFields.CITY_OTHER, Contact.CITY_OTHER);
            put(ContactFields.COMMERCIAL_REGISTER, Contact.COMMERCIAL_REGISTER);
            put(ContactFields.COMPANY, Contact.COMPANY);
            // has no equivalent in ContactObject put(ContactFields.CONTAINS_IMAGE1, ContactObject);
            put(ContactFields.COUNTRY_BUSINESS, Contact.COUNTRY_BUSINESS);
            put(ContactFields.COUNTRY_HOME, Contact.COUNTRY_HOME);
            put(ContactFields.COUNTRY_OTHER, Contact.COUNTRY_OTHER);
            put(ContactFields.DEFAULT_ADDRESS, Contact.DEFAULT_ADDRESS);
            put(ContactFields.DEPARTMENT, Contact.DEPARTMENT);
            put(ContactFields.DISPLAY_NAME, Contact.DISPLAY_NAME);
            put(ContactFields.DISTRIBUTIONLIST, Contact.DISTRIBUTIONLIST);
            put(ContactFields.EMAIL1, Contact.EMAIL1);
            put(ContactFields.EMAIL2, Contact.EMAIL2);
            put(ContactFields.EMAIL3, Contact.EMAIL3);
            put(ContactFields.EMPLOYEE_TYPE, Contact.EMPLOYEE_TYPE);
            put(ContactFields.FAX_BUSINESS, Contact.FAX_BUSINESS);
            put(ContactFields.FAX_HOME, Contact.FAX_HOME);
            put(ContactFields.FAX_OTHER, Contact.FAX_OTHER);
            put(ContactFields.FIRST_NAME, Contact.GIVEN_NAME);
            put(ContactFields.IMAGE1, Contact.IMAGE1);
            put(ContactFields.INFO, Contact.INFO);
            put(ContactFields.INSTANT_MESSENGER1, Contact.INSTANT_MESSENGER1);
            put(ContactFields.INSTANT_MESSENGER2, Contact.INSTANT_MESSENGER2);
            put(ContactFields.LAST_NAME, Contact.SUR_NAME);
            put(ContactFields.LINKS, Contact.LINKS);
            put(ContactFields.MANAGER_NAME, Contact.MANAGER_NAME);
            put(ContactFields.MARITAL_STATUS, Contact.MARITAL_STATUS);
            put(ContactFields.MARK_AS_DISTRIBUTIONLIST, Contact.MARK_AS_DISTRIBUTIONLIST);
            put(ContactFields.NICKNAME, Contact.NICKNAME);
            put(ContactFields.NOTE, Contact.NOTE);
            put(ContactFields.NUMBER_OF_CHILDREN, Contact.NUMBER_OF_CHILDREN);
            put(ContactFields.NUMBER_OF_DISTRIBUTIONLIST, Contact.NUMBER_OF_DISTRIBUTIONLIST);
            put(ContactFields.NUMBER_OF_EMPLOYEE, Contact.NUMBER_OF_EMPLOYEE);
            put(ContactFields.NUMBER_OF_LINKS, Contact.NUMBER_OF_LINKS);
            put(ContactFields.POSITION, Contact.POSITION);
            put(ContactFields.POSTAL_CODE_BUSINESS, Contact.POSTAL_CODE_BUSINESS);
            put(ContactFields.POSTAL_CODE_HOME, Contact.POSTAL_CODE_HOME);
            put(ContactFields.POSTAL_CODE_OTHER, Contact.POSTAL_CODE_OTHER);
            put(ContactFields.PROFESSION, Contact.PROFESSION);
            put(ContactFields.ROOM_NUMBER, Contact.ROOM_NUMBER);
            put(ContactFields.SALES_VOLUME, Contact.SALES_VOLUME);
            put(ContactFields.SECOND_NAME, Contact.MIDDLE_NAME);
            put(ContactFields.SPOUSE_NAME, Contact.SPOUSE_NAME);
            put(ContactFields.STATE_BUSINESS, Contact.STATE_BUSINESS);
            put(ContactFields.STATE_HOME, Contact.STATE_HOME);
            put(ContactFields.STATE_OTHER, Contact.STATE_OTHER);
            put(ContactFields.STREET_BUSINESS, Contact.STREET_BUSINESS);
            put(ContactFields.STREET_HOME, Contact.STREET_HOME);
            put(ContactFields.STREET_OTHER, Contact.STREET_OTHER);
            put(ContactFields.SUFFIX, Contact.SUFFIX);
            put(ContactFields.TAX_ID, Contact.TAX_ID);
            put(ContactFields.TELEPHONE_ASSISTANT, Contact.TELEPHONE_ASSISTANT);
            put(ContactFields.TELEPHONE_BUSINESS1, Contact.TELEPHONE_BUSINESS1);
            put(ContactFields.TELEPHONE_BUSINESS2, Contact.TELEPHONE_BUSINESS2);
            put(ContactFields.TELEPHONE_CALLBACK, Contact.TELEPHONE_CALLBACK);
            put(ContactFields.TELEPHONE_CAR, Contact.TELEPHONE_CAR);
            put(ContactFields.TELEPHONE_COMPANY, Contact.TELEPHONE_COMPANY);
            put(ContactFields.TELEPHONE_HOME1, Contact.TELEPHONE_HOME1);
            put(ContactFields.TELEPHONE_HOME2, Contact.TELEPHONE_HOME2);
            put(ContactFields.TELEPHONE_IP, Contact.TELEPHONE_IP);
            put(ContactFields.TELEPHONE_ISDN, Contact.TELEPHONE_ISDN);
            put(ContactFields.TELEPHONE_OTHER, Contact.TELEPHONE_OTHER);
            put(ContactFields.TELEPHONE_PAGER, Contact.TELEPHONE_PAGER);
            put(ContactFields.TELEPHONE_PRIMARY, Contact.TELEPHONE_PRIMARY);
            put(ContactFields.TELEPHONE_RADIO, Contact.TELEPHONE_RADIO);
            put(ContactFields.TELEPHONE_TELEX, Contact.TELEPHONE_TELEX);
            put(ContactFields.TELEPHONE_TTYTDD, Contact.TELEPHONE_TTYTDD);
            put(ContactFields.TITLE, Contact.TITLE);
            put(ContactFields.URL, Contact.URL);
            // has no equivalent in ContactObject put(ContactFields.USER_ID, ContactObject);
            put(ContactFields.USERFIELD01, Contact.USERFIELD01);
            put(ContactFields.USERFIELD02, Contact.USERFIELD02);
            put(ContactFields.USERFIELD03, Contact.USERFIELD03);
            put(ContactFields.USERFIELD04, Contact.USERFIELD04);
            put(ContactFields.USERFIELD05, Contact.USERFIELD05);
            put(ContactFields.USERFIELD06, Contact.USERFIELD06);
            put(ContactFields.USERFIELD07, Contact.USERFIELD07);
            put(ContactFields.USERFIELD08, Contact.USERFIELD08);
            put(ContactFields.USERFIELD09, Contact.USERFIELD09);
            put(ContactFields.USERFIELD10, Contact.USERFIELD10);
            put(ContactFields.USERFIELD11, Contact.USERFIELD11);
            put(ContactFields.USERFIELD12, Contact.USERFIELD12);
            put(ContactFields.USERFIELD13, Contact.USERFIELD13);
            put(ContactFields.USERFIELD14, Contact.USERFIELD14);
            put(ContactFields.USERFIELD15, Contact.USERFIELD15);
            put(ContactFields.USERFIELD16, Contact.USERFIELD16);
            put(ContactFields.USERFIELD17, Contact.USERFIELD17);
            put(ContactFields.USERFIELD18, Contact.USERFIELD18);
            put(ContactFields.USERFIELD19, Contact.USERFIELD19);
            put(ContactFields.USERFIELD20, Contact.USERFIELD20);

            put(ContactFields.CATEGORIES, Contact.CATEGORIES);
            put(ContactFields.COLORLABEL, Contact.COLOR_LABEL);
            put(ContactFields.CREATED_BY, Contact.CREATED_BY);
            put(ContactFields.CREATION_DATE, Contact.CREATION_DATE);
            put(ContactFields.FOLDER_ID, Contact.FOLDER_ID);
            put(ContactFields.ID, Contact.OBJECT_ID);
            put(ContactFields.LAST_MODIFIED, Contact.LAST_MODIFIED);
            put(ContactFields.LAST_MODIFIED_UTC, Contact.LAST_MODIFIED_UTC);
            put(ContactFields.MODIFIED_BY, Contact.MODIFIED_BY);
            put(ContactFields.NUMBER_OF_ATTACHMENTS, Contact.NUMBER_OF_ATTACHMENTS);
            put(ContactFields.PRIVATE_FLAG, Contact.PRIVATE_FLAG);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void put(String fieldname, int column) throws Exception {
        if (!fields2columns.containsKey(fieldname) && !columns2fields.containsKey(I(column))) {
            fields2columns.put(fieldname, I(column));
            columns2fields.put(I(column), fieldname);
        } else
            throw (new Exception("One Part of this combination is also mapped to something else!"));
    }

    public static String columnToFieldName(int column) {
        return columns2fields.get(I(column));
    }

    public static int fieldNameToColumn(String fieldname) {
        return (fields2columns.get(fieldname)).intValue();
    }
}
