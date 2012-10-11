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
import com.openexchange.contacts.json.RequestTools;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactJSONResultConverter} - The result converter for contact module.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactJSONResultConverter implements ResultConverter {

//    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactJSONResultConverter.class));
//
//    private static final TIntObjectMap<String> SPECIAL_COLUMNS;
//
//    static {
//        final TIntObjectMap<String> map = new TIntObjectHashMap<String>(12);
//        map.put(Contact.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, "date");
//        map.put(Contact.CREATION_DATE, "date");
//        map.put(Contact.LAST_MODIFIED, "date");
//        map.put(Contact.BIRTHDAY, "date");
//        map.put(Contact.ANNIVERSARY, "date");
//        map.put(Contact.IMAGE_LAST_MODIFIED, "date");
//        map.put(Contact.IMAGE1_URL, "image");
//        map.put(Contact.LAST_MODIFIED_UTC, "date_utc");
//        map.put(Contact.DISTRIBUTIONLIST, "distributionlist");
//        map.put(Contact.LINKS, "links");
//        map.put(Contact.DEFAULT_ADDRESS, "remove_if_zero");
//        SPECIAL_COLUMNS = map;
//    }

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
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
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
            int[] columnIDs = RequestTools.getColumnsAsIntArray(requestData, "columns");
            ContactField[] fields = null != columnIDs ? ContactMapper.getInstance().getFields(columnIDs) : 
            	ContactMapper.getInstance().getAllFields();        
            /*
             * Convert list of contacts
             */
        	if ("updates".equals(requestData.getAction())) {
            	/*
            	 * result contains a Map<String, List<Contact>> to decide between deleted and modified contacts
            	 */
                @SuppressWarnings("unchecked") final Map<String, List<Contact>> contactMap = (Map<String, List<Contact>>) resultObject;
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
                resultObject = convertListOfContacts(contacts, fields, timeZoneID, session);
            }
        }
        result.setResultObject(resultObject, "json");
    }
    
    private JSONObject convertSingleContact(Contact contact, String timeZoneID, Session session) throws OXException {
    	try {
    		// always add NUMBER_OF_IMAGES to contact result (bug #13960)
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

//    private JSONObject convertSingleContact(final Contact contact, final ServerSession session) throws OXException {
//        final JSONObject json = new JSONObject();
//        final ContactGetter cg = new ContactGetter();
//        for (final int column : Contact.JSON_COLUMNS) {
//            final ContactField field = ContactField.getByValue(column);
//            if (field != null && !field.getAjaxName().isEmpty()) {
//                try {
//                    final Object value = field.doSwitch(cg, contact);
//
//                    if (isSpecial(column)) {
//                        final Object special = convertSpecial(field, contact, cg, session);
//                        if (special != null && !String.valueOf(special).isEmpty()) {
//                            final String jsonKey = field.getAjaxName();
//                            json.put(jsonKey, special);
//                        }
//                    } else {
//                        if (value != null && !String.valueOf(value).isEmpty()) {
//                            final String jsonKey = field.getAjaxName();
//                            json.put(jsonKey, value);
//                        }
//                    }
//                } catch (final JSONException e) {
//                    OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
//                }
//            }
//        }
//        return json;
//    }
//
//    private JSONArray convertListOfContacts(final List<Contact> contacts, final int[] columns, final ServerSession session) throws OXException {
//        final JSONArray resultArray = new JSONArray();
//        for (final Contact contact : contacts) {
//            final JSONArray contactArray = new JSONArray();
//
//            final ContactGetter cg = new ContactGetter();
//            for (final int column : columns) {
//                final ContactField field = ContactField.getByValue(column);
//                if (field != null && !field.getAjaxName().isEmpty()) {
//                    final Object value = field.doSwitch(cg, contact);
//                    if (isSpecial(column)) {
//                        final Object special = convertSpecial(field, contact, cg, session);
//                        contactArray.put(special == null ? JSONObject.NULL : special);
//                    } else if (value == null) {
//                        contactArray.put(JSONObject.NULL);
//                    } else {
//                        contactArray.put(value);
//                    }
//                } else {
//                    LOG.warn("Did not find field or json name for column: " + column);
//                    contactArray.put(JSONObject.NULL);
//                }
//            }
//            resultArray.put(contactArray);
//        }
//        return resultArray;
//    }

    private void addObjectIdsToResultArray(final JSONArray resultArray, final List<Contact> contacts) {
        for (final Contact contact : contacts) {
            resultArray.put(contact.getObjectID());
        }
    }

//    private boolean isSpecial(final int column) {
//        return SPECIAL_COLUMNS.containsKey(column);
//    }
//
//    private Object convertSpecial(final ContactField field, final Contact contact, final ContactGetter cg, final ServerSession session) throws OXException {
//        final String type = SPECIAL_COLUMNS.get(field.getNumber());
//        if (type.equals("date")) {
//            final Object value = field.doSwitch(cg, contact);
//            if (value != null) {
//                final Date date = (Date) value;
//                return date.getTime();
//            }
//
//            return null;
//        } else if (type.equals("date_utc")) {
//            // Set last_modified_utc
//            final Date lastModified = contact.getLastModified();
//            final Calendar calendar = new GregorianCalendar();
//            calendar.setTime(lastModified);
//            final int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
//            calendar.add(Calendar.MILLISECOND, -offset);
//
//            return calendar.getTime().getTime();
//        } else if (type.equals("image")) {
//            String imageUrl = null;
//            {
//                final byte[] imageData = contact.getImage1();
//                if (imageData != null) {
//                    final ContactImageDataSource imgSource = ContactImageDataSource.getInstance();
//                    final ImageLocation il =
//                        new ImageLocation.Builder().folder(String.valueOf(contact.getParentFolderID())).id(
//                            String.valueOf(contact.getObjectID())).build();
//                    imageUrl = imgSource.generateUrl(il, session);
//                }
//            }
//            return imageUrl;
//        } else if (type.equals("distributionlist")) {
//            JSONArray distributionList = null;
//            try {
//                distributionList = getDistributionListAsJSONArray(contact);
//            } catch (final JSONException e) {
//                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
//            }
//
//            return distributionList;
//        } else if (type.equals("links")) {
//            JSONArray links = null;
//            try {
//                links = getLinksAsJSONArray(contact);
//            } catch (final JSONException e) {
//                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
//            }
//
//            return links;
//        } else if (type.equals("remove_if_zero")) {
//            final Integer value = (Integer) field.doSwitch(cg, contact);
//            if (value != null) {
//                final int intValue = value.intValue();
//                if (intValue != 0) {
//                    return intValue;
//                }
//            }
//
//            return null;
//        } else {
//            return null;
//        }
//    }
//
//    private JSONArray getDistributionListAsJSONArray(final Contact contact) throws JSONException {
//        final DistributionListEntryObject[] distributionList = contact.getDistributionList();
//        if (distributionList == null) {
//            return null;
//        }
//
//        final JSONArray jsonArray = new JSONArray();
//        for (int i = 0; i < distributionList.length; i++) {
//            final JSONObject entry = new JSONObject();
//            final int emailField = distributionList[i].getEmailfield();
//
//            if (!(emailField == DistributionListEntryObject.INDEPENDENT)) {
//                entry.put(DistributionListFields.ID, distributionList[i].getEntryID());
//            }
//
//            entry.put(DistributionListFields.MAIL, distributionList[i].getEmailaddress());
//            entry.put(DistributionListFields.DISPLAY_NAME, distributionList[i].getDisplayname());
//            entry.put(DistributionListFields.MAIL_FIELD, emailField);
//
//            jsonArray.put(entry);
//        }
//
//        return jsonArray;
//    }
//
//    private JSONArray getLinksAsJSONArray(final Contact contact) throws JSONException {
//        final LinkEntryObject[] links = contact.getLinks();
//
//        if (links != null) {
//            final JSONArray jsonLinks = new JSONArray();
//            for (int i = 0; i < links.length; i++) {
//                final LinkEntryObject link = links[i];
//                final JSONObject jsonLink = new JSONObject();
//
//                if (link.containsLinkID()) {
//                    jsonLink.put(ContactFields.ID, link.getLinkID());
//                }
//
//                jsonLink.put(ContactFields.DISPLAY_NAME, link.getLinkDisplayname());
//                jsonLinks.put(jsonLink);
//            }
//
//            return jsonLinks;
//        }
//        return null;
//    }

}
