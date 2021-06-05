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

package com.openexchange.ajax.contact.action;

import static com.openexchange.java.Autoboxing.l;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonUpdatesParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link ContactUpdatesParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContactUpdatesParser extends CommonUpdatesParser<ContactUpdatesResponse> {

    protected ContactUpdatesParser(boolean failOnError, int[] columns) {
        super(failOnError, columns);
    }

    @Override
    protected ContactUpdatesResponse createResponse(Response response) throws JSONException {
        /*
         * Calling super.createResponse initiates the modified and deleted ids for the update response
         */
        ContactUpdatesResponse retval = super.createResponse(response);
        JSONArray rows = (JSONArray) response.getData();
        if (rows == null) {
            return retval;
        }
        List<Contact> contacts = new ArrayList<Contact>();
        for (int i = 0, size = rows.length(); i < size; i++) {
            Object arrayOrId = rows.get(i);
            if (!JSONArray.class.isInstance(arrayOrId)) {
                continue;
            }
            JSONArray row = rows.getJSONArray(i);
            Contact contact = new Contact();
            for (int colIndex = 0; colIndex < getColumns().length; colIndex++) {
                Object value = row.get(colIndex);
                if (value == JSONObject.NULL) {
                    continue;
                }
                int column = getColumns()[colIndex];
                if (column == Contact.LAST_MODIFIED_UTC) {
                    continue;
                }
                value = transform(value, column);
                contact.set(column, value);
            }
            contacts.add(contact);
        }
        retval.setContacts(contacts);
        return retval;
    }

    @Override
    protected ContactUpdatesResponse instantiateResponse(Response response) {
        return new ContactUpdatesResponse(response);
    }

    private Object transform(Object actual, int column) throws JSONException {
        switch (column) {
            case Contact.CREATION_DATE:
            case Contact.LAST_MODIFIED:
            case Contact.ANNIVERSARY:
            case Contact.BIRTHDAY:
                return new Date(l((Long) actual));
            case Contact.IMAGE1:
                return ((String) actual).getBytes();
            case Contact.DISTRIBUTIONLIST:
                return transformDistributionList((JSONArray) actual);

        }
        return actual;
    }

    private DistributionListEntryObject[] transformDistributionList(JSONArray arr) throws JSONException {
        DistributionListEntryObject[] results = new DistributionListEntryObject[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = (JSONObject) arr.get(i);
            DistributionListEntryObject entry = new DistributionListEntryObject();
            if (obj.has("display_name")) {
                entry.setDisplayname(obj.getString("display_name"));
            }
            if (obj.has("mail")) {
                try {
                    entry.setEmailaddress(obj.getString("mail"));
                } catch (OXException e) {
                    // don't set E-Mail at all
                }
            }
            if (obj.has("mail_field")) {
                entry.setEmailfield(obj.getInt("mail_field"));
            }
        }
        return results;
    }
}
