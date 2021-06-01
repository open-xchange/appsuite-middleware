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

package com.openexchange.contacts.json.converters;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.contacts.json.mapping.ColumnParser;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactJSONResultConverter} - The result converter for contact module.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactJSONResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link JSONResultConverter}.
     */
    public ContactJSONResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "contact";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
    	/*
    	 * determine timezone
    	 */
        String timeZoneID = requestData.getParameter("timezone");
        if (null == timeZoneID) {
        	timeZoneID = session.getUser().getTimeZone();
        }
        /*
         * check and convert result object
         */
        Object resultObject = result.getResultObject();
        if (resultObject instanceof Contact) {
            /*
             * Only one contact to convert
             */
            resultObject = convertSingleContact((Contact)resultObject, timeZoneID, session);
        } else {
            /*
             * get requested column IDs
             */
            String columns = requestData.getParameter("columns");
            ContactField[] fields = null != columns ?
                ContactMapper.getInstance().getFields(ColumnParser.parseColumns(columns)) : ContactMapper.getInstance().getAllFields();
            /*
             * Convert list of contacts
             */
        	if ("updates".equals(requestData.getAction())) {
            	/*
            	 * result contains a Map<String, List<Contact>> to decide between deleted and modified contacts
            	 */
                @SuppressWarnings("unchecked") final Map<String, List<Contact>> contactMap = (Map<String, List<Contact>>) resultObject;
                if (contactMap == null) {
                    throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(Map.class.getName(), "null");
                }
                final List<Contact> modified = contactMap.get("modified");
                final List<Contact> deleted = contactMap.get("deleted");
                JSONArray jsonArray = convertListOfContacts(modified, fields, timeZoneID, session);
                if (null != deleted && 0 < deleted.size()) {
                    addObjectIdsToResultArray(jsonArray, deleted);
                }
                resultObject = jsonArray;
            } else {
            	/*
            	 * A list of contacts to convert
            	 */
                @SuppressWarnings("unchecked") final List<Contact> contacts = (List<Contact>) resultObject;
                if (contacts == null) {
                    throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(Map.class.getName(), "null");
                }
                resultObject = convertListOfContacts(contacts, fields, timeZoneID, session);
            }
        }
        result.setResultObject(resultObject, "json");
    }

    private JSONObject convertSingleContact(Contact contact, String timeZoneID, Session session) throws OXException {
    	try {
    		// Always add NUMBER_OF_IMAGES to contact result (bug #13960)
    		ContactField[] fields = ContactMapper.getInstance().getAssignedFields(contact, ContactField.NUMBER_OF_IMAGES);
			return ContactMapper.getInstance().serialize(contact, fields, timeZoneID, session);
		} catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
		}
    }

    private JSONArray convertListOfContacts(List<Contact> contacts, ContactField[] fields, String timeZoneID, Session session) throws OXException {
    	try {
    	    return ContactMapper.getInstance().serialize(contacts, fields, timeZoneID, session);
		} catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
		}
    }

    private void addObjectIdsToResultArray(JSONArray resultArray, List<Contact> contacts) {
        for (Contact contact : contacts) {
            resultArray.put(contact.getObjectID());
        }
    }

}
