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

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ContactJSONDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactJSONDataHandler implements DataHandler {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactJSONDataHandler}.
     *
     * @param services A service lookup reference
     */
    public ContactJSONDataHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    @Override
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException {
        if (null == session) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create("session");
        }

        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        InputStream inputStream = (InputStream) data.getData();
        int folderID = optIntProperty(data.getDataProperties(), DataProperties.PROPERTY_FOLDER_ID);
        int objectID = optIntProperty(data.getDataProperties(), DataProperties.PROPERTY_ID);
        SearchIterator<VCardImport> searchIterator = null;
        try {
            /*
             * parse vCards
             */
            JSONArray jsonArray = new JSONArray();
            VCardService vCardService = services.getService(VCardService.class);
            searchIterator = vCardService.importVCards(inputStream, vCardService.createParameters(session).setKeepOriginalVCard(false));
            while (searchIterator.hasNext()) {
                Contact contact = null;
                VCardImport vCardImport = null;
                try {
                    vCardImport = searchIterator.next();
                    contact = vCardImport.getContact();
                } finally {
                    Streams.close(vCardImport);
                }
                /*
                 * apply folder/object identifier is defined
                 */
                if (-1 != folderID) {
                    contact.setParentFolderID(folderID);
                }
                if (-1 != objectID) {
                    contact.setObjectID(objectID);
                }
                /*
                 * convert contact to JSON
                 */
                jsonArray.put(convertContact(contact, serverSession));
            }
            /*
             * return JSON array of converted contacts
             */
            ConversionResult result = new ConversionResult();
            result.setData(jsonArray);
            return result;
        } finally {
            SearchIterators.close(searchIterator);
            Streams.close(inputStream);
        }
    }

    private static int optIntProperty(DataProperties properties, String propertyName) {
        String value = properties.get(propertyName);
        if (Strings.isNotEmpty(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                org.slf4j.LoggerFactory.getLogger(ContactJSONDataHandler.class).warn(
                    "unable to parse numerical value \"{}\" for \"{}\": {}", value, propertyName, e.getMessage(), e);
            }
        }
        return -1;
    }

    /**
     * Converts a contact parsed from a vCard to it's common JSON representation, preserving a binary image in the <code>image1</code>
     * field.
     *
     * @param contact The contact to convert
     * @param session The server session
     * @return The contact as JSON object
     */
    private static JSONObject convertContact(Contact contact, ServerSession session) throws OXException {
        /*
         * remove contact image temporary to prevent image URL generation for not existing contact
         */
        byte[] image1 = contact.getImage1();
        if (null != image1) {
            contact.removeImage1();
        }
        /*
         * determine relevant fields
         */
        Set<ContactField> setFields = new HashSet<ContactField>();
        for (Entry<ContactField, ? extends JsonMapping<? extends Object, Contact>> entry : ContactMapper.getInstance().getMappings().entrySet()) {
            JsonMapping<? extends Object, Contact> mapping = entry.getValue();
            if (mapping.isSet(contact) && null != mapping.get(contact)) {
                setFields.add(entry.getKey());
            }
        }
        ContactField[] fields = setFields.toArray(new ContactField[setFields.size()]);
        /*
         * serialize contact to json
         */
        try {
            JSONObject json = ContactMapper.getInstance().serialize(contact, fields, session.getUser().getTimeZone(), session);
            /*
             * include contact image inline if available
             */
            if (null != image1) {
                json.put(ContactMapper.getInstance().get(ContactField.IMAGE1).getAjaxName(), Base64.encodeBase64String(image1));
                json.put(ContactMapper.getInstance().get(ContactField.NUMBER_OF_IMAGES).getAjaxName(), 1);
                String contentType = null != contact.getImageContentType() ? contact.getImageContentType() : "image/jpeg";
                json.put(ContactMapper.getInstance().get(ContactField.IMAGE1_CONTENT_TYPE).getAjaxName(), contentType);
            }
            return json;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

}
