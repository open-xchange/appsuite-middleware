/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.user.json.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.user.json.Constants;
import com.openexchange.user.json.dto.UserContact;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.filter.UserCensorship;
import com.openexchange.user.json.mapping.UserMapper;

/**
 * {@link SearchAction} - Maps the action to a <tt>search</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class SearchAction extends AbstractUserAction {

    /**
     * The <tt>search</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_SEARCH;

    /**
     * Initializes a new {@link SearchAction}.
     */
    public SearchAction(ServiceLookup services) {
        super(services);
    }

    @SuppressWarnings("deprecation")
    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        final int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
        final int orderBy = parseIntParameter(AJAXServlet.PARAMETER_SORT, request);
        final Order order = OrderFields.parse(request.getParameter(AJAXServlet.PARAMETER_ORDER));
        final String collation = request.getParameter(AJAXServlet.PARAMETER_COLLATION);
        final JSONObject jData = (JSONObject) request.requireData();
        /*
         * Contact search object
         */
        ContactSearchObject contactSearch = new ContactSearchObject();
        contactSearch.addFolder(Constants.USER_ADDRESS_BOOK_FOLDER_ID);
        try {
	        if (jData.has(SearchFields.PATTERN)) {
	            contactSearch.setPattern(parseString(jData, SearchFields.PATTERN));
	        }
	        if (jData.has("startletter")) {
	            contactSearch.setStartLetter(parseBoolean(jData, "startletter"));
	        }
	        if (jData.has("emailAutoComplete") && jData.getBoolean("emailAutoComplete")) {
	            contactSearch.setEmailAutoComplete(true);
	        }
	        if (jData.has("orSearch") && jData.getBoolean("orSearch")) {
	            contactSearch.setOrSearch(true);
	        }
	        contactSearch.setSurname(parseString(jData, ContactFields.LAST_NAME));
	        contactSearch.setDisplayName(parseString(jData, ContactFields.DISPLAY_NAME));
	        contactSearch.setGivenName(parseString(jData, ContactFields.FIRST_NAME));
	        contactSearch.setCompany(parseString(jData, ContactFields.COMPANY));
	        contactSearch.setEmail1(parseString(jData, ContactFields.EMAIL1));
	        contactSearch.setEmail2(parseString(jData, ContactFields.EMAIL2));
	        contactSearch.setEmail3(parseString(jData, ContactFields.EMAIL3));
	        contactSearch.setDynamicSearchField(parseJSONIntArray(jData, "dynamicsearchfield"));
	        contactSearch.setDynamicSearchFieldValue(parseJSONStringArray(jData, "dynamicsearchfieldvalue"));
	        contactSearch.setPrivatePostalCodeRange(parseJSONStringArray(jData, "privatepostalcoderange"));
	        contactSearch.setBusinessPostalCodeRange(parseJSONStringArray(jData, "businesspostalcoderange"));
	        contactSearch.setPrivatePostalCodeRange(parseJSONStringArray(jData, "privatepostalcoderange"));
	        contactSearch.setOtherPostalCodeRange(parseJSONStringArray(jData, "otherpostalcoderange"));
	        contactSearch.setBirthdayRange(parseJSONDateArray(jData, "birthdayrange"));
	        contactSearch.setAnniversaryRange(parseJSONDateArray(jData, "anniversaryrange"));
	        contactSearch.setNumberOfEmployeesRange(parseJSONStringArray(jData, "numberofemployee"));
	        contactSearch.setSalesVolumeRange(parseJSONStringArray(jData, "salesvolumerange"));
	        contactSearch.setCreationDateRange(parseJSONDateArray(jData, "creationdaterange"));
	        contactSearch.setLastModifiedRange(parseJSONDateArray(jData, "lastmodifiedrange"));
	        contactSearch.setCatgories(parseString(jData, "categories"));
	        contactSearch.setSubfolderSearch(parseBoolean(jData, "subfoldersearch"));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * Sort options
         */
    	UserField orderField = UserMapper.getInstance().getMappedField(orderBy);
    	SortOptions sortOptions = new SortOptions(collation);
    	if (null == orderField) {
    		// Sort field is a contact field: pass as it is
    		final ContactField sortField = ContactMapper.getInstance().getMappedField(orderBy);
    		if (null != sortField) {
    			sortOptions.setOrderBy(new SortOrder[] { SortOptions.Order(sortField, order) });
    		}
    	}
    	/*
    	 * Perform search
    	 */
    	Date lastModified = new Date(0);
    	final List<UserContact> userContacts = new ArrayList<UserContact>();
        final ContactService contactService = services.getService(ContactService.class);
        final ContactField[] contactFields = ContactMapper.getInstance().getFields(columns,
        		ContactField.INTERNAL_USERID, ContactField.LAST_MODIFIED);
        final UserService userService = services.getService(UserService.class);
        UserField[] userFields = UserMapper.getInstance().getFields(columns);
        boolean needsUserData = null != userFields && 0 < userFields.length;
        UserCensorship censorship = needsUserData ? getUserCensorship(session) : null;
        SearchIterator<Contact> searchIterator = null;
        try {
        	searchIterator = contactService.searchUsers(session, contactSearch, contactFields, sortOptions);
        	/*
        	 * Process results
        	 */
            while (searchIterator.hasNext()) {
            	final Contact contact = searchIterator.next();
            	/*
            	 * Check last modified
            	 */
            	if (contact.getLastModified().after(lastModified)) {
            		lastModified = contact.getLastModified();
            	}
                /*
                 * Get corresponding user & apply censorship if needed
                 */
                User user;
                if (needsUserData && 0 < contact.getInternalUserId()) {
                    user = userService.getUser(contact.getInternalUserId(), session.getContext());
                    if (null != censorship && session.getUserId() != user.getId()) {
                        user = censorship.censor(user);
                    }
                } else {
                    UserImpl placeholder = new UserImpl();
                    placeholder.setId(contact.getInternalUserId());
                    user = placeholder;
                }
            	userContacts.add(new UserContact(contact, user));
            }
        } finally {
        	if (null != searchIterator) {
        		searchIterator.close();
        	}
        }
        /*
         * Sort by users if a user field was denoted by sort field
         */
        if (1 < userContacts.size()) {
        	final UserField orderByUserField = UserMapper.getInstance().getMappedField(orderBy);
        	if (null != orderByUserField) {
        		Collections.sort(userContacts, UserContact.getComparator(
        				orderByUserField, session.getUser().getLocale(), Order.DESCENDING.equals(order)));
        	}
        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(userContacts, lastModified, "usercontact");
    }

    /**
     * Parses optional field out of specified JSON object.
     *
     * @param jsonObj The JSON object to parse
     * @param name The optional field name
     * @return The optional field's value or <code>null</code> if there's no such field
     * @throws JSONException If a JSON error occurs
     */
    private static String parseString(final JSONObject jsonObj, final String name) throws JSONException {
        String retval = null;
        if (jsonObj.hasAndNotNull(name)) {
            final String test = jsonObj.getString(name);
            if (0 != test.length()) {
                retval = test;
            }
        }
        return retval;
    }

    private static boolean parseBoolean(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.has(name)) {
            return false;
        }

        return jsonObj.getBoolean(name);
    }

    private static int[] parseJSONIntArray(final JSONObject jsonObj, final String name) throws JSONException, OXException {
        if (!jsonObj.has(name)) {
            return null;
        }

        final JSONArray tmp = jsonObj.getJSONArray(name);
        if (tmp == null) {
            return null;
        }

        try {
            final int i[] = new int[tmp.length()];
            for (int a = 0; a < tmp.length(); a++) {
                i[a] = tmp.getInt(a);
            }

            return i;
        } catch (NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

    /**
     * Parses optional array field out of specified JSON object
     *
     * @param jsonObj The JSON object to parse
     * @param name The optional array field's name
     * @return The optional array field's value as an array of {@link String} or <code>null</code> if there's no such field
     * @throws JSONException If a JSON error occurs
     */
    private static String[] parseJSONStringArray(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.hasAndNotNull(name)) {
            return null;
        }
        final JSONArray tmp = jsonObj.getJSONArray(name);
        final String s[] = new String[tmp.length()];
        for (int a = 0; a < tmp.length(); a++) {
            s[a] = tmp.getString(a);
        }
        return s;
    }

    private static Date[] parseJSONDateArray(final JSONObject jsonObj, final String name) throws JSONException, OXException {
        if (!jsonObj.has(name)) {
            return null;
        }

        final JSONArray tmp = jsonObj.getJSONArray(name);
        if (tmp == null) {
            return null;
        }

        try {
            final Date d[] = new Date[tmp.length()];
            for (int a = 0; a < tmp.length(); a++) {
                d[a] = new Date(tmp.getLong(a));
            }

            return d;
        } catch (NumberFormatException exc) {
            throw OXJSONExceptionCodes.INVALID_VALUE.create(exc, name, tmp);
        }
    }

}
