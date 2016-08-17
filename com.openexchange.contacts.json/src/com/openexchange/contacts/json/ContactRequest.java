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

package com.openexchange.contacts.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.fields.SearchTermFields;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contacts.json.mapping.ColumnParser;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.SpecialAlphanumSortContactComparator;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchExceptionMessages;
import com.openexchange.search.SearchTerm;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.collections.PropertizedList;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactRequest {

    private final AJAXRequestData request;
    private final ServerSession session;
    private final User user;

    /**
     * Initializes a new {@link ContactRequest}.
     *
     * @param request The request
     * @param session The session
     * @throws OXException
     */
    public ContactRequest(final AJAXRequestData request, final ServerSession session) throws OXException {
        super();
        this.request = request;
        this.session = session;
        user = session.getUser();
    }

    /**
     * Constant for not-found number.
     */
    public static final int NOT_FOUND = -9999;

    /**
     * Gets optional <code>int</code> parameter.
     *
     * @param name The parameter name
     * @return The <code>int</code> or {@link #NOT_FOUND} (<code>-9999</code>)
     * @throws OXException If parameter is an invalid number value
     */
    public int optInt(final String name) throws OXException {
        final String parameter = request.getParameter(name);
        if (null == parameter) {
            return NOT_FOUND;
        }
        try {
            return Integer.parseInt(parameter.trim());
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
        }
    }

    /**
     * Gets a value indicating whether the results should be sorted internally
     * or not, i.e. the 'sort' field of the request is set to a magic value.
     *
     * @return <code>true</code>, if the results should be sorted internally,
     * <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean isInternalSort() throws OXException {
    	final int sort = this.getSort();
    	return 0 == sort || Contact.SPECIAL_SORTING == sort || Contact.USE_COUNT_GLOBAL_FIRST == sort;
    }

    /**
     * Gets the requested sort options.
     *
     * @return the sort options
     * @throws OXException
     */
    public SortOptions getSortOptions() throws OXException {
        SortOptions sortOptions = new SortOptions();
        if (false == isInternalSort()) {
            // Left-hand and right-hand limit only applicable if no internal sorting is supposed to be performed
            int leftHandLimit = this.getLeftHandLimit();
            if (0 < leftHandLimit) {
                sortOptions.setRangeStart(leftHandLimit);
            }
            int rightHandLimit = this.getRightHandLimit();
            if (0 < rightHandLimit) {
                if (rightHandLimit < leftHandLimit) {
                    throw OXJSONExceptionCodes.INVALID_VALUE.create(rightHandLimit, "right_hand_limit");
                }
                sortOptions.setLimit(rightHandLimit - leftHandLimit);
            }

       		sortOptions.setCollation(this.getCollation());
        	int sort = this.getSort();
        	if (0 < sort) {
        		ContactField sortField = ContactMapper.getInstance().getMappedField(sort);
        		if (null == sortField) {
                    throw OXJSONExceptionCodes.INVALID_VALUE.create(sort, "sort");
        		}
        		sortOptions.setOrderBy(new SortOrder[] { SortOptions.Order(sortField, getOrder()) });
        	}
        }
    	return sortOptions;
    }

    /**
     * Sort the supplied contacts internally according to the requested 'sort' field.
     *
     * @param contacts the contacts to sort
     * @return <code>true</code> if internal sorting was performed; otherwise <code>false</code>
     * @throws OXException If internal sorting fails
     */
    public boolean sortInternalIfNeeded(List<Contact> contacts) throws OXException {
        if (this.isInternalSort() && null != contacts && 1 < contacts.size()) {
            int sort = this.getSort();
            if (0 == sort || Contact.SPECIAL_SORTING == sort) {
                Collections.sort(contacts, new SpecialAlphanumSortContactComparator(user.getLocale()));
                return true;
            } else if (Contact.USE_COUNT_GLOBAL_FIRST == sort) {
                Collections.sort(contacts, new UseCountComparator(user.getLocale()));
                return true;
            }
        }
        return false;
    }

    /**
     * Sort the supplied contacts internally according to the requested 'sort' field, falling back to a special comparison by annual date
     * with a reference date if not set otherwise.
     *
     * @param contacts The contacts to sort
     * @param reference the reference date
     * @return <code>true</code> if internal sorting was performed; otherwise <code>false</code>
     * @throws OXException If internal sorting fails
     */
    public boolean sortInternalIfNeeded(List<Contact> contacts, ContactField dateField, Date reference) throws OXException {
        if (null != contacts && 1 < contacts.size()) {
            int sort = this.getSort();
            if (Contact.SPECIAL_SORTING == sort) {
                Collections.sort(contacts, new SpecialAlphanumSortContactComparator(user.getLocale()));
                return true;
            } else if (Contact.USE_COUNT_GLOBAL_FIRST == sort) {
                Collections.sort(contacts, new UseCountComparator(user.getLocale()));
                return true;
            } else if (0 == sort) {
                Collections.sort(contacts, RequestTools.getAnnualDateComparator(dateField, reference));
                return true;
            }
        }
        return false;
    }

    /**
     * Slices given contact list according possibly available left-hand and right-hand parameters
     *
     * @param toSlice The contact list to slice
     * @return The sliced contact list
     * @throws OXException If slice attempt fails
     */
    public List<Contact> slice(List<Contact> toSlice) throws OXException {
        List<Contact> contacts = toSlice;

        int leftHandLimit = this.optInt(AJAXServlet.LEFT_HAND_LIMIT);
        int rightHandLimit = this.optInt(AJAXServlet.RIGHT_HAND_LIMIT);
        if (leftHandLimit >= 0 || rightHandLimit > 0) {
            int size = contacts.size();
            int fromIndex = leftHandLimit > 0 ? leftHandLimit : 0;
            int toIndex = rightHandLimit > 0 ? (rightHandLimit > size ? size : rightHandLimit) : size;
            if ((fromIndex) > size) {
                contacts = Collections.<Contact> emptyList();
            } else if (fromIndex >= toIndex) {
                contacts = Collections.<Contact> emptyList();
            } else {
                /*
                 * Check if end index is out of range
                 */
                if (toIndex < size) {
                    contacts = contacts.subList(fromIndex, toIndex);
                } else if (fromIndex > 0) {
                    contacts = contacts.subList(fromIndex, size);
                }
            }
            contacts = new PropertizedList<Contact>(contacts).setProperty("more", Integer.valueOf(size));
        }

        return contacts;
    }

    /**
     * Gets the requested contact fields.
     *
     * @return the fields
     * @throws OXException
     */
    public ContactField[] getFields() throws OXException {
    	return getFields((ContactField[])null);
    }

    public ContactField[] getFields(ContactField...mandatoryFields) throws OXException {
        /*
         * get requested column IDs
         */
        int[] columnIDs = ColumnParser.parseColumns(request.requireParameter("columns"));
        /*
         * determine mandatory fields
         */
    	ContactField[] fields;
    	if (this.isInternalSort() || Arrays.contains(columnIDs, ContactMapper.getInstance().get(ContactField.SORT_NAME).getColumnID())) {
    		fields = new ContactField[] {
    			ContactField.LAST_MODIFIED, ContactField.YOMI_LAST_NAME, ContactField.SUR_NAME, ContactField.MARK_AS_DISTRIBUTIONLIST,
				ContactField.YOMI_FIRST_NAME, ContactField.GIVEN_NAME, ContactField.DISPLAY_NAME, ContactField.YOMI_COMPANY,
                ContactField.COMPANY, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.DEPARTMENT };
    	} else {
    		fields = new ContactField[] { ContactField.LAST_MODIFIED };
    	}
    	if (null != mandatoryFields) {
    		fields = Arrays.add(fields, mandatoryFields);
    	}
    	/*
    	 * get mapped fields
    	 */
    	return ColumnParser.getFieldsToQuery(columnIDs, fields);
    }

    /**
     * Gets a search term from the json array named 'filter in the request.
     * @return the search term
     * @throws OXException
     */
    public SearchTerm<?> getSearchFilter() throws OXException {
    	JSONArray jsonArray = this.getJSONData().optJSONArray("filter");
		if (null == jsonArray) {
			throw OXJSONExceptionCodes.MISSING_FIELD.create("filter");
		}
	    return ContactSearchTermParser.INSTANCE.parseSearchTerm(jsonArray);
    }

    /**
     * Gets the requested start date ('start').
     *
     * @return The start date
     * @throws OXException
     */
    public Date getStart() throws OXException {
        return getDate("start");
    }

    /**
     * Gets the requested end date ('end').
     *
     * @return The end date
     * @throws OXException
     */
    public Date getEnd() throws OXException {
        return getDate("end");
    }

    private Date getDate(String parameterName) throws OXException {
        String value = request.getParameter(parameterName);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameterName);
        }
        try {
            return new Date(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, value);
        }
    }

    /**
     * Gets the requested folder ID ('folder').
     *
     * @return The folder ID
     * @throws OXException
     */
    public String getFolderID() throws OXException {
        String folderID = optFolderID();
        if (null == folderID || 0 == folderID.length()) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("folder");
        }
        return folderID;
    }

    /**
     * Gets the requested folder ID ('folder').
     *
     * @return The folder ID, or <code>null</code> if not present
     * @throws OXException
     */
    public String optFolderID() throws OXException {
        return request.getParameter("folder");
    }

    /**
     * Gets the requested object ID ('id').
     *
     * @return
     * @throws OXException
     */
    public String getObjectID() throws OXException {
    	final String folderID = request.getParameter("id");
    	if (null == folderID || 0 == folderID.length()) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("id");
    	}
    	return folderID;
    }

    /**
     * Gets the request's data as JSON object.
     *
     * @return the JSON object
     * @throws OXException
     */
    public JSONObject getJSONData() throws OXException {
    	Object data = request.getData();
    	if (null == data) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("data");
    	} else if (false == JSONObject.class.isInstance(data)) {
    		throw OXJSONExceptionCodes.INVALID_VALUE.create("data", data.toString());
    	}
    	return (JSONObject)data;
    }

    /**
     * Gets the folder ID from the json data object.
     *
     * @return the folder ID
     * @throws OXException
     */
    public String getFolderIDFromData() throws OXException {
    	String folderID = this.getJSONData().optString("folder_id");
    	if (null == folderID || 0 == folderID.length()) {
    		throw OXJSONExceptionCodes.MISSING_FIELD.create("folder_id");
    	}
    	return folderID;
    }

    public int getId() throws OXException {
    	//TODO: as String
        if (request.isSet("id")) {
            return request.getParameter("id", int.class);
        }
        return user.getContactId();
    }

    public int getFolder() throws OXException {
    	//TODO: as String
        return request.getParameter("folder", int.class);
    }

    public TimeZone getTimeZone() {
        final String timezone = request.getParameter("timezone");
        if (timezone == null) {
            return TimeZoneUtils.getTimeZone(user.getTimeZone());
        } else {
            return TimeZoneUtils.getTimeZone(timezone);
        }
    }

    public ServerSession getSession() {
        return session;
    }

    public int getSort() throws OXException {
        return RequestTools.getNullableIntParameter(request, "sort");
    }

    public Order getOrder() {
        return OrderFields.parse(request.getParameter("order"));
    }

    public String getCollation() {
        return request.getParameter("collation");
    }

    public int getLeftHandLimit() throws OXException {
        return RequestTools.getNullableIntParameter(request, "left_hand_limit");
    }

    public int getRightHandLimit() throws OXException {
        return RequestTools.getNullableIntParameter(request, "right_hand_limit");
    }

    public boolean isExcludeAdmin() throws OXException {
        return request.containsParameter("admin") && false == request.getParameter("admin", boolean.class);
    }

    public boolean isRequireEmail() throws OXException {
        return false == request.containsParameter("email") || request.getParameter("email", boolean.class);
    }

    public String getQuery() throws OXException {
        return request.getParameter("query");
    }

    public int[][] getListRequestData() throws OXException {
        final JSONArray data = (JSONArray) request.getData();
        if (data == null) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("data");
        }

        return RequestTools.buildObjectIdAndFolderId(data);
    }

    /**
     * Gets a map containing object IDs for a folder from the supplied {@link JSONArray} that is expected in the format
     * <code>[{"folder":55,"id":"456"}, {"folder":32,"id":"77"}, {"folder":55,"id":"18"}, ...]</code>
     *
     * @param jsonArray The JSON array to get the data for
     * @return The object IDs per folder
     * @throws OXException
     */
    public Map<String, List<String>> getObjectIDsPerFolder(JSONArray jsonArray) throws OXException {
        try {
            Map<String, List<String>> objectIDsPerFolder = new HashMap<String, List<String>>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objectAndFolderID = jsonArray.getJSONObject(i);
                String folderID = objectAndFolderID.getString("folder");
                List<String> objectIDs = objectIDsPerFolder.get(folderID);
                if (null == objectIDs) {
                    objectIDs = new ArrayList<String>();
                    objectIDsPerFolder.put(folderID, objectIDs);
                }
                objectIDs.add(objectAndFolderID.getString("id"));
            }
            return objectIDsPerFolder;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e);
        }
    }

    /**
     * Gets a map containing object IDs for a folder from the request's data object that is expected in the format
     * <code>[{"folder":55,"id":"456"}, {"folder":32,"id":"77"}, {"folder":55,"id":"18"}, ...]</code>
     *
     * @param jsonArray The JSON array to get the data for
     * @return The object IDs per folder
     * @throws OXException
     */
    public Map<String, List<String>> getObjectIDsPerFolder() throws OXException {
        JSONArray jsonArray = (JSONArray)request.getData();
        if (null == jsonArray) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("data");
        }
        return getObjectIDsPerFolder(jsonArray);
    }

    public boolean containsImage() throws OXException {
        long maxSize = sysconfMaxUpload();
        return request.hasUploads(-1L, maxSize > 0 ? maxSize : -1L);
    }

    public JSONObject getContactJSON(final boolean isUpload) throws OXException {
        if (!isUpload) {
            return (JSONObject) request.requireData();
        }
        long maxSize = sysconfMaxUpload();
        String jsonField = request.getUploadEvent(-1L, maxSize > 0 ? maxSize : -1L).getFormField("json");
        try {
            return new JSONObject(jsonField);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, jsonField);
        }
    }

    public UploadEvent getUploadEvent() throws OXException {
        long maxSize = sysconfMaxUpload();
        return request.getUploadEvent(-1L, maxSize > 0 ? maxSize : -1L);
    }

    public int[] getDeleteRequestData() throws OXException {
        final JSONObject json = (JSONObject) request.getData();
            final int[] data = new int[2];
            try {
                data[0] = json.getInt("id");
                data[1] = json.getInt("folder");
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
            }
            return data;
    }

    public long getTimestamp() throws OXException {
        return request.getParameter("timestamp", long.class);
    }

    public int[] getUserIds() throws OXException {
        final JSONArray json = (JSONArray) request.requireData();
        final int userIdArray[] = new int[json.length()];
        for (int i = 0; i < userIdArray.length; i++) {
            try {
                userIdArray[i] = json.getInt(i);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
            }
        }

        return userIdArray;
    }

    public int getFolderFromJSON() throws OXException {
        final JSONObject json = (JSONObject) request.requireData();
        try {
            return json.getInt("folder_id");
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
        }
    }

    public Object getData() {
        return request.getData();
    }

    public String getIgnore() {
        return request.getParameter("ignore");
    }

    /**
     * Gets the associated {@code AJAXRequestData} instance.
     *
     * @return The request data
     */
    public AJAXRequestData getRequest() {
        return request;
    }

    /**
     * {@link ContactSearchTermParser}
     *
     * Custom {@link SearchTermParser} producing {@link ContactFieldOperand}s
     * from ajax names.
     */
    private static final class ContactSearchTermParser extends SearchTermParser {

        public static final ContactSearchTermParser INSTANCE = new ContactSearchTermParser();

        private ContactSearchTermParser() {
            super();
        }

        @Override
        protected Operand<?> parseOperand(final JSONObject operand) throws OXException {
            if (false == operand.hasAndNotNull(SearchTermFields.FIELD)) {
                throw SearchExceptionMessages.PARSING_FAILED_MISSING_FIELD.create(SearchTermFields.FIELD);
            }
            ContactField field = ContactMapper.getInstance().getMappedField(operand.optString(SearchTermFields.FIELD));
            return new ContactFieldOperand(field);
        }
    }

    private static long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if(null == sizeS) {
            return 0;
        }
        return Long.parseLong(sizeS);
    }

}
