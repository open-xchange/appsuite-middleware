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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.fail;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import junit.framework.TestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.contact.action.AdvancedSearchRequest;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.ContactUpdatesResponse;
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
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Streams;
import com.openexchange.java.UnsynchronizedByteArrayOutputStream;

/**
 * This class and ContactObject should be all that is needed to write contact-related tests. If multiple users are needed use multiple
 * instances of this class. Examples of tests using this class can be found in ExemplaryContactTestManagerTest.java
 *
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - refactoring a bit.
 */
public class ContactTestManager implements TestManager {

    protected boolean failOnError;

    protected List<Contact> createdEntities;

    protected AJAXClient client;

    protected ContactParser contactParser;

    protected TimeZone timeZone;

    protected Throwable lastException;

    protected AbstractAJAXResponse lastResponse;

    private int sleep = 0;

    @Override
    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public boolean getFailOnError() {
        return failOnError;
    }

    public void setCreatedEntities(final List<Contact> createdEntities) {
        this.createdEntities = createdEntities;
    }

    public List<Contact> getCreatedEntities() {
        return createdEntities;
    }

    public void setClient(final AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    private void setTimeZone(final TimeZone timezone) {
        this.timeZone = timezone;
    }

    private TimeZone getTimeZone() {
        return this.timeZone;
    }

    public void setContactParser(final ContactParser contactParser) {
        this.contactParser = contactParser;
    }

    public ContactParser getContactParser() {
        return contactParser;
    }

    public ContactTestManager(final AJAXClient client) throws OXException, IOException, SAXException, JSONException {
        this.setClient(client);
        this.setTimeZone(client.getValues().getTimeZone());
        setCreatedEntities(new LinkedList<Contact>());
        setContactParser(new ContactParser(true, timeZone));
    }

    public static Contact generateContact() {
        final Contact contact = generateContact(666);
        contact.removeParentFolderID();
        return contact;
    }

    public static Contact generateContact(final int folderId){
    	return generateContact(folderId, "surname");
    }

    public static Contact generateContact(final int folderId, final String lastname){
        final Contact contact = new Contact();
        contact.setGivenName("givenname");
        contact.setSurName(lastname);
        contact.setMiddleName("middlename");
        contact.setSuffix("suffix");
        contact.setEmail1("email1@ox.invalid");
        contact.setEmail2("email2@ox.invalid");
        contact.setEmail3("email3@ox.invalid");
        contact.setDisplayName("displayname");
        contact.setPosition("position");
        contact.setTitle("title");
        contact.setCompany("company");
    	contact.setParentFolderID(folderId);
        return contact;
    }

    /**
     * Creates a contact with all possible string, int and date fields
     * set to the value of their column number. Sorry, if you want to
     * check the date fields, you have to do a bit of parsing.
     *
     * Warning: objectID is being set, too. You might want to change
     * that for all but the most basic tests.
     */
    public static Contact generateFullContact(final int folderID){
    	final Contact contact = new Contact();
    	for(final int field: Contact.ALL_COLUMNS){
    		try {
    			contact.set(field, new Integer(field));
    		} catch(final ClassCastException e1) {
    			try {
        			contact.set(field, String.valueOf(field));
        		} catch(final ClassCastException e2) {
        			try {
            			contact.set(field, new Date(field));
            		} catch(final ClassCastException e3) {
            			//don't
            		}
        		}
    		}
    	}
    	contact.setEmail1("email1@hostinvalid");
    	contact.setEmail2("email2@hostinvalid");
    	contact.setEmail3("email3@hostinvalid");

    	contact.removeObjectID();
    	contact.setParentFolderID(folderID);
    	return contact;
    }
    /**
     * Creates a contact via HTTP-API and updates it with new id, timestamp and all other information that is updated after such requests.
     * Remembers this contact for cleanup later.
     */
    public Contact newAction(final Contact contactToCreate) {
        final InsertRequest request = new InsertRequest(contactToCreate, getFailOnError());
        InsertResponse response = null;
        try {
            response = getClient().execute(request, getSleep());
            response.fillObject(contactToCreate);
            lastResponse = response;
        } catch (final Exception e) {
            doExceptionHandling(e, "NewRequest");
        }
        getCreatedEntities().add(contactToCreate);
        return contactToCreate;
    }

    /**
     * Create multiple contacts via the HTTP-API at once
     */
    public void newAction(final Contact... contacts) {
        for (int i = 0; i < contacts.length; i++) {
            this.newAction(contacts[i]);
        }
    }

    /**
     * Create multiple contacts via a multiple request
     */
    public void newActionMultiple(final Contact... contacts) {
        final InsertRequest requests[] = new InsertRequest[contacts.length];
        int i;
        for (i = 0; i < requests.length; i++) {
            requests[i] = new InsertRequest(contacts[i], getFailOnError());
        }

        try {
            MultipleRequest<InsertResponse> request = MultipleRequest.create(requests);
            MultipleResponse<InsertResponse> responses = getClient().execute(request, getSleep());
            lastResponse = responses;
            Iterator<InsertResponse> it = responses.iterator();
            i = 0;
            while (it.hasNext()) {
                InsertResponse response = it.next();
                response.fillObject(contacts[i]);
                getCreatedEntities().add(contacts[i]);
                i++;
            }
        } catch (final Exception e) {
            doExceptionHandling(e, "NewRequest");
        }
    }

    /**
     * Updates a contact via HTTP-API and returns the same contact for convenience
     */
    public Contact updateAction(final Contact contact) {
    	return this.updateAction(contact.getParentFolderID(), contact);
    }

    /**
     * Updates a contact via HTTP-API and returns the same contact for convenience
     */
    public Contact updateAction(final int inFolder, final Contact contact) {
        final UpdateRequest request = new UpdateRequest(inFolder, contact, true);
        try {
            lastResponse = getClient().execute(request, getSleep());
            contact.setLastModified(lastResponse.getTimestamp());
            remember(contact);
        } catch (final Exception e) {
            doExceptionHandling(e, "UpdateRequest for folder " + contact.getParentFolderID() + " and object " + contact.getObjectID());
        }
        return contact;
    }

    /**
     * Deletes a contact via HTTP-API
     */
    public void deleteAction(final Contact contactToDelete) {
        final boolean oldValue = this.getFailOnError();
        this.setFailOnError(failOnError);
        try {
            contactToDelete.setLastModified(new Date(Long.MAX_VALUE));
            final DeleteRequest request = new DeleteRequest(contactToDelete, getFailOnError());
            lastResponse = getClient().execute(request, getSleep());
        } catch (final Exception e) {
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
    public Contact getAction(final Contact contact) {
        return getAction(contact.getParentFolderID(), contact.getObjectID());
    }

    /**
     * Get a contact via HTTP-API with no existing ContactObject
     */
    public Contact getAction(final int folderId, final int objectId) {
        Contact returnedContact = null;
        final GetRequest request = new GetRequest(folderId, objectId, getTimeZone(), false);
        GetResponse response = null;
        try {
            response = getClient().execute(request, getSleep());
            lastResponse = response;
            if (response.hasError() && getFailOnError()) {
                throw response.getException();
            }
            returnedContact = response.getContact();
        } catch (final Exception e) {
            doExceptionHandling(e, "GetRequest for folder " + folderId + " and object " + objectId);
            return null;
        }
        return returnedContact;
    }

    /**
     * removes all contacts inserted or updated by this Manager
     */
    @Override
    public void cleanUp() {
        for (final Contact contact : new Vector<Contact>(getCreatedEntities())) {
            final boolean old = getFailOnError();
            setFailOnError(false);
            deleteAction(contact);
            setFailOnError(old);
        }
    }

    public Contact[] allAction(final int folderId, final int[] columns, final int orderBy, final Order order, final String collation) {
        List<Contact> allContacts = new LinkedList<Contact>();
        final AllRequest request = new AllRequest(folderId, columns, orderBy, order, collation);
        try {
            final CommonAllResponse response = getClient().execute(request, getSleep());
            lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            allContacts = transform(data, columns);
        } catch (final Exception e) {
            doExceptionHandling(e, "AllRequest for folder " + folderId);
        }
        return allContacts.toArray(new Contact[]{});
    }

    public Contact[] allAction(final int folderId, final int[] columns) {
        List<Contact> allContacts = new LinkedList<Contact>();
        final AllRequest request = new AllRequest(folderId, columns);
        try {
            final CommonAllResponse response = getClient().execute(request, getSleep());
            lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            allContacts = transform(data, columns);
        } catch (final Exception e) {
            doExceptionHandling(e, "AllRequest for folder " + folderId);
        }
        return allContacts.toArray(new Contact[]{});

    }

	/**
     * get all contacts in one folder via the HTTP-API
     */
    public Contact[] allAction(final int folderId) {
        return allAction(folderId, Contact.ALL_COLUMNS);
    }

    /**
     * get all contacts specified by multiple int-arrays with 2 slots each (1st slot: folderId, 2nd slot objectId) via the HTTP-API
     */
    public Contact[] listAction(final int[]... folderAndObjectIds) {
        List<Contact> allContacts = new LinkedList<Contact>();
        final ListRequest request = new ListRequest(ListIDs.l(folderAndObjectIds), Contact.ALL_COLUMNS, true);
        try {
            final CommonListResponse response = getClient().execute(request, getSleep());
            lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            allContacts = transform(data);
        } catch (final Exception e) {
            doExceptionHandling(e, "ListRequest");
        }
        return allContacts.toArray(new Contact[]{});
    }

    /**
     * Search for contacts in a folder via the HTTP-EnumAPI. Use "-1" as folderId to search all available folders
     */
    public Contact[] searchAction(final String pattern, final int folderId) {
    	return searchAction(pattern, folderId, Contact.ALL_COLUMNS);
    }

    public Contact[] searchAction(final String pattern, final int folderId, final int... columns) {
    	return searchAction(pattern, folderId, -1, null, null, Contact.ALL_COLUMNS);
    }
    public Contact[] searchAction(final String pattern, final int folderId, final int orderBy, final Order order, final String collation, final int... columns) {
        List<Contact> allContacts = new LinkedList<Contact>();
        final String orderDir = order == null ? "ASC" : order.equals(Order.ASCENDING) ? "ASC" : "DESC";

        final SearchRequest request = new SearchRequest(pattern, false, folderId, columns, orderBy, orderDir, collation, failOnError);
        try {
            final SearchResponse response = getClient().execute(request, getSleep());
            lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            allContacts = transform(data, columns);
        } catch (final Exception e) {
            doExceptionHandling(
                e,
                "searching for contacts with pattern: " + pattern + ", in folder: " + Integer.toString(folderId) + e.getMessage());
        }
        return allContacts.toArray(new Contact[]{});
    }


    public Contact[] searchFirstletterAction(final String firstLetter, final int folderId) {
        List<Contact> allContacts = new LinkedList<Contact>();
        final SearchRequest request = new SearchRequest(firstLetter, true, folderId, Contact.ALL_COLUMNS, -1, null, true);
        try {
            final SearchResponse response = getClient().execute(request, getSleep());
            lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            allContacts = transform(data);
        } catch (final Exception e) {
            doExceptionHandling(
                e,
                "searching for contacts with first letter: " + firstLetter + ", in folder: " + Integer.toString(folderId) + e.getMessage());
        }
        return allContacts.toArray(new Contact[]{});
    }

    public Contact[] searchAction(final ContactSearchObject search) {
    	return searchAction(search, Contact.ALL_COLUMNS);
    }

    public Contact[] searchAction(final ContactSearchObject search, final int[] columns) {
    	return searchAction(search, columns, -1, null, null);
    }

    public Contact[] searchAction(final ContactSearchObject search, final int[] columns, final int orderBy, final Order order, final String collation) {
        List<Contact> allContacts = new LinkedList<Contact>();
        final SearchRequest request = new SearchRequest(search, columns, orderBy, order, collation, getFailOnError());
        try {
            final SearchResponse response = getClient().execute(request, getSleep());
            lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            allContacts = transform(data, columns);
        } catch (final Exception e) {
            doExceptionHandling(
                e,
                "searching for contacts with search-object: " +  search + ": " + e.getMessage());
        }
        return allContacts.toArray(new Contact[]{});
    }

    /**
     * Search for contacts in a folder via the HTTP-EnumAPI. Use "-1" as folderId to search all available folders
     */
    public Contact[] searchAction(final String pattern, final int folderId, final boolean initialSearch) {
        List<Contact> allContacts = new LinkedList<Contact>();
        final SearchRequest request = new SearchRequest(pattern, folderId, Contact.ALL_COLUMNS, true);
        try {
            final SearchResponse response = getClient().execute(request, getSleep());
            lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            allContacts = transform(data);
        } catch (final Exception e) {
            doExceptionHandling(
                e,
                "searching for contacts with pattern: " + pattern + ", in folder: " + Integer.toString(folderId) + e.getMessage());
        }
        return allContacts.toArray(new Contact[]{});
    }

    public Contact[] searchAction(final JSONObject filter, final int[] columns, final int orderBy, final Order order){
        List<Contact> contacts = new LinkedList<Contact>();
		final AdvancedSearchRequest request = new AdvancedSearchRequest(filter, columns, orderBy, order == Order.DESCENDING ? "DESC" : "ASC");
		try {
			final CommonSearchResponse response = getClient().execute(request, getSleep());
			lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            contacts = transform(data, columns);
		} catch (final Exception e) {
            doExceptionHandling(e,"searching for contacts with term: \n" + filter.toString() );
        }
        return contacts.toArray(new Contact[]{});
    }

    /**
     * Get contacts in a folder that were updated since a specific date via the HTTP-API
     */
    public Contact[] updatesAction(final int folderId, final Date lastModified) {
        List<Contact> allContacts = new LinkedList<Contact>();
        final UpdatesRequest request = new UpdatesRequest(folderId, Contact.ALL_COLUMNS, -1, null, lastModified);
        try {
            final ContactUpdatesResponse response = getClient().execute(request, getSleep());
            lastResponse = response;
            final JSONArray data = (JSONArray) response.getResponse().getData();
            allContacts = transform(data);
        } catch (final Exception e) {
            doExceptionHandling(e, "UpdateRequest");
        }
        return allContacts.toArray(new Contact[]{});
    }

    protected void doExceptionHandling(final Exception exception, final String action) {
        try {
            lastException = exception;
            throw exception;
        } catch (final OXException e) {
            if (getFailOnError()){
            	e.printStackTrace();
                fail("AjaxException occured during " + action + ": " + e.getMessage());
            }
        } catch (final IOException e) {
            if (getFailOnError()){
            	e.printStackTrace();
                fail("IOException occured during " + action + ": " + e.getMessage());
            }
        } catch (final SAXException e) {
            if (getFailOnError()){
            	e.printStackTrace();
                fail("SAXException occured during " + action + ": " + e.getMessage());
            }
        } catch (final JSONException e) {
            if (getFailOnError()){
            	e.printStackTrace();
                fail("JSONException occured during " + action + ": " + e.getMessage());
            }
        } catch (final Exception e) {
            if (getFailOnError()){
            	e.printStackTrace();
                fail("Unexpected exception occured during " + action + ": " + e.getMessage());
            }
        }
    }

    protected void doJanitorialTasks(final AbstractAJAXResponse response) throws OXException{
        lastResponse = response;
        if(response.hasError() && failOnError) {
            throw response.getException();
        }
    }

    private void remember(final Contact contact) {
        for (final Contact tempContact : getCreatedEntities()) {
            if (tempContact.getObjectID() == contact.getObjectID()) {
                getCreatedEntities().set(getCreatedEntities().indexOf(tempContact), contact);
            } else {
                getCreatedEntities().add(contact);
            }
        }
    }

    public List<Contact> transform(final JSONArray data) throws JSONException, OXException, IOException {
    	return transform(data, Contact.ALL_COLUMNS);
    }

    public List<Contact> transform(final JSONArray data, final int[] columns) throws JSONException, OXException, IOException {
        final List<Contact> contacts = new LinkedList<Contact>();
        for (int i = 0; i < data.length(); i++) {
            final JSONArray jsonArray = data.getJSONArray(i);
            final JSONObject jsonObject = new JSONObject();
            for (int a = 0; a < jsonArray.length(); a++) {
                if (!"null".equals(jsonArray.getString(a))) {
                    final String fieldname = ContactMapping.columnToFieldName(columns[a]);
                    if (fieldname != null) {
                        jsonObject.put(fieldname, jsonArray.get(a));
                    }
                }
            }
            final Contact contactObject = new Contact();
            getContactParser().parse(contactObject, jsonObject);

            if (null != contactObject.getImage1()) {
            	final String image1 = new String(contactObject.getImage1());
            	if (0 < image1.length() && image1.contains("image")) {
            		// interpret as image url, download real image
            	    String url = image1 + "&compress=false&rotate=false";
            		try {
                        contactObject.setImage1(loadImage(url));
                        contactObject.setNumberOfImages(1);
                    } catch (final Exception e) {
                        // Ignore possible error during download attempt
                    }
            	}
            }
            contacts.add(contactObject);
        }
        return contacts;
    }

    private byte[] loadImage(final String imageURL) throws OXException {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            AJAXSession ajaxSession = getClient().getSession();
            HttpGet httpRequest = new HttpGet(getClient().getProtocol() + "://" + getClient().getHostname() + imageURL);
            HttpResponse httpResponse = ajaxSession.getHttpClient().execute(httpRequest);
            inputStream = httpResponse.getEntity().getContent();
            int len = 8192;
            byte[] buf = new byte[len];
            outputStream = new UnsynchronizedByteArrayOutputStream(len << 2);
            for (int read; (read = inputStream.read(buf, 0, len)) > 0;) {
                outputStream.write(buf, 0, read);
            }
            return outputStream.toByteArray();
        } catch (ClientProtocolException e) {
        	throw new OXException(e);
		} catch (IOException e) {
        	throw new OXException(e);
		} finally {
		    Streams.close(inputStream, outputStream);
        }
    }

    @Override
    public boolean doesFailOnError() {
        return getFailOnError();
    }

    @Override
    public Throwable getLastException() {
        return this.lastException;
    }

    @Override
    public AbstractAJAXResponse getLastResponse() {
        return this.lastResponse;
    }

    @Override
    public boolean hasLastException() {
        return lastException != null;
    }

	public void setSleep(final int sleep) {
		this.sleep = sleep;
	}

	public int getSleep() {
		return sleep;
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
            put(ContactFields.MANAGER_NAME, Contact.MANAGER_NAME);
            put(ContactFields.MARITAL_STATUS, Contact.MARITAL_STATUS);
            put(ContactFields.MARK_AS_DISTRIBUTIONLIST, Contact.MARK_AS_DISTRIBUTIONLIST);
            put(ContactFields.NICKNAME, Contact.NICKNAME);
            put(ContactFields.NOTE, Contact.NOTE);
            put(ContactFields.NUMBER_OF_CHILDREN, Contact.NUMBER_OF_CHILDREN);
            put(ContactFields.NUMBER_OF_DISTRIBUTIONLIST, Contact.NUMBER_OF_DISTRIBUTIONLIST);
            put(ContactFields.NUMBER_OF_EMPLOYEE, Contact.NUMBER_OF_EMPLOYEE);
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
            put(ContactFields.EXTENDED_PROPERTIES, Contact.EXTENDED_PROPERTIES);

            put(ContactFields.YOMI_COMPANY, Contact.YOMI_COMPANY);
            put(ContactFields.YOMI_FIRST_NAME, Contact.YOMI_FIRST_NAME);
            put(ContactFields.YOMI_LAST_NAME, Contact.YOMI_LAST_NAME);

            put(ContactFields.ADDRESS_BUSINESS, Contact.ADDRESS_BUSINESS);
            put(ContactFields.ADDRESS_HOME, Contact.ADDRESS_HOME);
            put(ContactFields.ADDRESS_OTHER, Contact.ADDRESS_OTHER);

            put(ContactFields.UID, Contact.UID);
            put(ContactFields.IMAGE1_URL, Contact.IMAGE1_URL);

        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    private static void put(final String fieldname, final int column) throws Exception {
        if (!fields2columns.containsKey(fieldname) && !columns2fields.containsKey(I(column))) {
            fields2columns.put(fieldname, I(column));
            columns2fields.put(I(column), fieldname);
        } else {
            throw (new Exception("One Part of this combination is also mapped to something else!"));
        }
    }

    public static String columnToFieldName(final int column) {
        return columns2fields.get(I(column));
    }

    public static int fieldNameToColumn(final String fieldname) {
        return (fields2columns.get(fieldname)).intValue();
    }
}
