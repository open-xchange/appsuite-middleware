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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.SpecialAlphanumSortContactComparator;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.search.SearchTerm;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactRequest}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactRequest {

    /**
     * Contact fields that are not persistent.
     */
    private static final EnumSet<ContactField> VIRTUAL_FIELDS = EnumSet.of(ContactField.IMAGE1_URL, ContactField.LAST_MODIFIED_UTC);

    private final AJAXRequestData request;
    private final ServerSession session;

    public ContactRequest(final AJAXRequestData request, final ServerSession session) throws OXException {
        super();
        this.request = request;
        this.session = session;
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
    	final int leftHandLimit = this.getLeftHandLimit();
    	int rightHandLimit = this.getRightHandLimit();
        if (rightHandLimit == 0) {
            rightHandLimit = 50000;
        }            
        final SortOptions sortOptions = new SortOptions(leftHandLimit,  rightHandLimit - leftHandLimit);
        if (false == isInternalSort()) {
       		sortOptions.setCollation(this.getCollation());
        	final int sort = this.getSort();
        	if (0 < sort) {
        		final ContactField sortField = ContactMapper.getInstance().getMappedField(sort);
        		if (null == sortField) {
        			throw new IllegalArgumentException("no mapped field for sort order '" + sort + "'.");
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
     * @throws OXException
     */
    public void sortInternalIfNeeded(final List<Contact> contacts) throws OXException {
    	if (this.isInternalSort() && null != contacts && 1 < contacts.size()) {
    		final int sort = this.getSort();
            if (0 == sort || Contact.SPECIAL_SORTING == sort) {
                Collections.sort(contacts, new SpecialAlphanumSortContactComparator(session.getUser().getLocale()));
            } else if (Contact.USE_COUNT_GLOBAL_FIRST == sort) {
                Collections.sort(contacts, new UseCountComparator(true, session.getUser().getLocale())); 
            }
    	}
    }
    
    /**
     * Gets the requested contact fields.
     * 
     * @return the fields
     * @throws OXException
     */
    public ContactField[] getFields() throws OXException {
    	return getFields((ContactField[])null);
//    	final int[] columnIDs = RequestTools.getColumnsAsIntArray(request, "columns");
//    	if (this.isInternalSort()) {
//        	return ContactMapper.getInstance().getFields(columnIDs, VIRTUAL_FIELDS, ContactField.LAST_MODIFIED,
//        			ContactField.YOMI_LAST_NAME, ContactField.SUR_NAME, ContactField.YOMI_FIRST_NAME, ContactField.GIVEN_NAME, 
//        			ContactField.DISPLAY_NAME, ContactField.YOMI_COMPANY, ContactField.COMPANY, ContactField.EMAIL1, ContactField.EMAIL2, 
//        			ContactField.USE_COUNT);
//    	} else {
//        	return ContactMapper.getInstance().getFields(columnIDs, VIRTUAL_FIELDS, ContactField.LAST_MODIFIED);
//    	}
    }    

    public ContactField[] getFields(final ContactField...mandatoryFields) throws OXException {
    	ContactField[] fields = null;
    	if (this.isInternalSort()) {
    		fields = new ContactField[] { 
    			ContactField.LAST_MODIFIED, ContactField.YOMI_LAST_NAME, ContactField.SUR_NAME, 
				ContactField.YOMI_FIRST_NAME, ContactField.GIVEN_NAME, ContactField.DISPLAY_NAME, ContactField.YOMI_COMPANY, 
				ContactField.COMPANY, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.USE_COUNT };
    	} else {
    		fields = new ContactField[] { ContactField.LAST_MODIFIED };
    	}
    	if (null != mandatoryFields) {
    		fields = Arrays.add(fields, mandatoryFields);
    	}
    	final int[] columnIDs = RequestTools.getColumnsAsIntArray(request, "columns");
    	return ContactMapper.getInstance().getFields(columnIDs, VIRTUAL_FIELDS, fields);
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
	    return SearchTermParser.parse(jsonArray);
    }

    /**
     * Gets the requested folder ID ('folder').
     * 
     * @return the folder ID
     * @throws OXException
     */
    public String getFolderID() throws OXException {
    	final String folderID = request.getParameter("folder");
    	if (null == folderID || 0 == folderID.length()) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("folder");
    	}
    	return folderID;
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
    
    /**
     * Gets a map containing the requested folder IDs as keys mapped to the 
     * corresponding object IDs as values. 
     * @return
     * @throws OXException
     * @throws JSONException
     */
    public Map<String, List<String>> getListIDs() throws OXException, JSONException {
        final JSONArray jsonArray = (JSONArray)request.getData();
        if (jsonArray == null) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("data");
        }
        final Map<String, List<String>> ids = new HashMap<String, List<String>>();
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            final String folderID = jsonObject.getString("folder");
            if (false == ids.containsKey(folderID)) {
            	ids.put(folderID, new ArrayList<String>());
            }
            ids.get(folderID).add(jsonObject.getString("id"));
        }
        return ids;
    }

    public int getId() throws OXException {
    	//TODO: as String 
        if (request.isSet("id")) {
            return request.getParameter("id", int.class);
        }
        return session.getUser().getContactId();
    }

    public int getFolder() throws OXException {
    	//TODO: as String 
        return request.getParameter("folder", int.class);
    }

    public TimeZone getTimeZone() {
        final String timezone = request.getParameter("timezone");
        if (timezone == null) {
            return TimeZoneUtils.getTimeZone(session.getUser().getTimeZone());
        } else {
            return TimeZoneUtils.getTimeZone(timezone);
        }
    }

    public ServerSession getSession() {
        return session;
    }

    public int[] getColumns() throws OXException {
        return checkOrInsertLastModified(removeVirtual(RequestTools.getColumnsAsIntArray(request, "columns")));
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

    public int[][] getListRequestData() throws OXException {
        final JSONArray data = (JSONArray) request.getData();
        if (data == null) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("data");
        }

        return RequestTools.buildObjectIdAndFolderId(data);
    }

    public boolean containsImage() throws OXException {
        return request.hasUploads();
    }

    public JSONObject getContactJSON(final boolean isUpload) throws OXException {
        if (isUpload) {
            final String jsonField = request.getUploadEvent().getFormField("json");
            try {
                return new JSONObject(jsonField);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, jsonField);
            }
        } else {
            return (JSONObject) request.getData();
        }
    }

    public UploadEvent getUploadEvent() throws OXException {
        return request.getUploadEvent();
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
        final JSONArray json = (JSONArray) request.getData();
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
        final JSONObject json = (JSONObject) request.getData();
        try {
            return json.getInt("folder_id");
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
        }
    }

    public Object getData() {
        return request.getData();
    }

    private int[] removeVirtual(final int[] columns) {
        final TIntList helper = new TIntArrayList(columns.length);
        for (final int col : columns) {
            if (col == Contact.LAST_MODIFIED_UTC) {
                // SKIP
            } else if (col == Contact.IMAGE1_URL) {
                helper.add(Contact.IMAGE1);
            } else {
                helper.add(col);
            }

        }
        return helper.toArray();
    }

    private int[] checkOrInsertLastModified(final int[] columns) {
        if (!Arrays.contains(columns, Contact.LAST_MODIFIED)) {
            final int[] newColumns = new int[columns.length + 1];
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
            newColumns[columns.length] = Contact.LAST_MODIFIED;

            return newColumns;
        } else {
            return columns;
        }
    }

    public String getIgnore() {
        return request.getParameter("ignore");
    }

}
