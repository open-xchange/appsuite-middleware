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

package com.openexchange.contacts.json;

import static com.openexchange.java.Autoboxing.I;
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
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contacts.json.mapping.ColumnParser;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.SpecialAlphanumSortContactComparator;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.java.Autoboxing;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.collections.PropertizedList;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link ContactRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactRequest {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {

        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactRequest.class);
    }

    private final AJAXRequestData request;
    private final ServerSession session;
    private final User user;

    /**
     * Initializes a new {@link ContactRequest}.
     *
     * @param request The request
     * @param session The session
     */
    public ContactRequest(final AJAXRequestData request, final ServerSession session) {
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
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
        }
    }

    /**
     * Gets a value indicating whether the results should be sorted internally
     * or not, i.e. the 'sort' field of the request is set to a magic value.
     *
     * @return <code>true</code>, if the results should be sorted internally,
     *         <code>false</code>, otherwise
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
        if (isInternalSort()) {
            return sortOptions;
        }
        // Left-hand and right-hand limit only applicable if no internal sorting is supposed to be performed
        int leftHandLimit = this.getLeftHandLimit();
        if (0 < leftHandLimit) {
            sortOptions.setRangeStart(leftHandLimit);
        }
        int rightHandLimit = this.getRightHandLimit();
        if (0 < rightHandLimit) {
            if (rightHandLimit < leftHandLimit) {
                throw OXJSONExceptionCodes.INVALID_VALUE.create(Autoboxing.valueOf(rightHandLimit), "right_hand_limit");
            }
            sortOptions.setLimit(rightHandLimit - leftHandLimit);
        }

        sortOptions.setCollation(this.getCollation());
        int sort = this.getSort();
        if (0 < sort) {
            ContactField sortField = ContactMapper.getInstance().getMappedField(sort);
            if (null == sortField) {
                throw OXJSONExceptionCodes.INVALID_VALUE.create(I(sort), "sort");
            }
            sortOptions.setOrderBy(new SortOrder[] { SortOptions.Order(sortField, getOrder()) });
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
                try {
                    Collections.sort(contacts, new UseCountComparator(user.getLocale()));
                } catch (IllegalArgumentException e) {
                    // The Comparator contract is violated
                    LoggerHolder.LOG.error("Comparator contract is violated by UseCountComparator with locale {}", user.getLocale(), e);
                    Collections.sort(contacts, UseCountComparator.FALLBACK_USE_COUNT_COMPARATOR);
                }
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
        if (null == contacts || 0 == contacts.size()) {
            return false;
        }
        switch (this.getSort()) {
            case Contact.SPECIAL_SORTING:
                Collections.sort(contacts, new SpecialAlphanumSortContactComparator(user.getLocale()));
                return true;
            case Contact.USE_COUNT_GLOBAL_FIRST:
                Collections.sort(contacts, new UseCountComparator(user.getLocale()));
                return true;
            case 0:
                Collections.sort(contacts, RequestTools.getAnnualDateComparator(dateField, reference));
                return true;
            default:
                return false;
        }
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
            contacts = new PropertizedList<>(contacts).setProperty("more", Integer.valueOf(size));
        }

        return contacts;
    }

    /**
     * Gets the supposed number of contacts according to optional left-hand and right-hand parameters.
     * <p>
     * <b>Note</b>: Actual number of contacts in result set might be less.
     *
     * @return The supposed number of contacts
     * @throws OXException If parameters cannot be read
     */
    public int getLimit() throws OXException {
        int leftHandLimit = this.optInt(AJAXServlet.LEFT_HAND_LIMIT);
        int rightHandLimit = this.optInt(AJAXServlet.RIGHT_HAND_LIMIT);

        if (leftHandLimit < 0) {
            return rightHandLimit <= 0 ? -1 : rightHandLimit;
        }

        if (rightHandLimit <= 0) {
            return rightHandLimit == 0 ? 0 : -1;
        }

        return rightHandLimit - leftHandLimit;
    }

    /**
     * Gets the requested contact fields.
     *
     * @return the fields
     * @throws OXException
     */
    public ContactField[] getFields() throws OXException {
        return getFields((ContactField[]) null);
    }

    public ContactField[] getFields(ContactField... mandatoryFields) throws OXException {
        /*
         * get requested column IDs
         */
        int[] columnIDs = ColumnParser.parseColumns(request.requireParameter("columns"));
        /*
         * determine mandatory fields
         */
        ContactField[] fields;
        if (this.isInternalSort() || Arrays.contains(columnIDs, ContactMapper.getInstance().get(ContactField.SORT_NAME).getColumnID().intValue())) {
            fields = new ContactField[] { ContactField.LAST_MODIFIED, ContactField.YOMI_LAST_NAME, ContactField.SUR_NAME, ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.YOMI_FIRST_NAME, ContactField.GIVEN_NAME, ContactField.DISPLAY_NAME, ContactField.YOMI_COMPANY, ContactField.COMPANY, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.DEPARTMENT, ContactField.USE_COUNT };
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
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
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
     */
    public String optFolderID() {
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
        return (JSONObject) data;
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

    public TimeZone getTimeZone() {
        final String timezone = request.getParameter("timezone");
        return timezone == null ? TimeZoneUtils.getTimeZone(user.getTimeZone()) : TimeZoneUtils.getTimeZone(timezone);
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

    public boolean isRequireEmail() throws OXException {
        return false == request.containsParameter("email") || request.getParameter("email", boolean.class).booleanValue();
    }

    public String getQuery() {
        return request.getParameter("query");
    }

    /**
     * Gets and returns the contact identifiers of the request
     *
     * @return A {@link List} with the requested {@link ContactID}s
     * @throws OXException if the 'data' field is missing
     */
    public List<ContactID> getContactIds() throws OXException {
        JSONArray data = (JSONArray) request.getData();
        if (data == null) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("data");
        }

        return RequestTools.getContactIds(data);
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
            Map<String, List<String>> objectIDsPerFolder = new HashMap<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objectAndFolderID = jsonArray.getJSONObject(i);
                String folderID = objectAndFolderID.getString("folder");
                List<String> objectIDs = objectIDsPerFolder.get(folderID);
                if (null == objectIDs) {
                    objectIDs = new ArrayList<>();
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
        JSONArray jsonArray = (JSONArray) request.getData();
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
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, jsonField);
        }
    }

    public UploadEvent getUploadEvent() throws OXException {
        long maxSize = sysconfMaxUpload();
        return request.getUploadEvent(-1L, maxSize > 0 ? maxSize : -1L);
    }

    /**
     * Returns the {@link ContactID} from the request payload
     *
     * @return The {@link ContactID}
     * @throws OXException if a JSON error is occurred
     */
    public ContactID getContactID() throws OXException {
        JSONObject json = JSONObject.class.cast(request.getData());
        try {
            return new ContactID(json.getString("folder"), json.getString("id"));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
        }
    }

    public long getTimestamp() throws OXException {
        return request.getParameter("timestamp", long.class).longValue();
    }

    public int[] getUserIds() throws OXException {
        final JSONArray json = (JSONArray) request.requireData();
        final int userIdArray[] = new int[json.length()];
        for (int i = 0; i < userIdArray.length; i++) {
            try {
                userIdArray[i] = json.getInt(i);
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
            }
        }

        return userIdArray;
    }

    public int getFolderFromJSON() throws OXException {
        final JSONObject json = (JSONObject) request.requireData();
        try {
            return json.getInt("folder_id");
        } catch (JSONException e) {
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

    private static long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if (null == sizeS) {
            return 0;
        }
        return Long.parseLong(sizeS);
    }

}
