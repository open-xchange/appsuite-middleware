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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class GetResponse extends AbstractAJAXResponse {

    private Contact contactObj;
    private final TimeZone timeZone;
    private String imageUrl;

    /**
     * @param response
     */
    public GetResponse(final Response response, TimeZone tz) {
        super(response);
        this.timeZone = tz;
    }

    /**
     * @return the contact
     * @throws OXException parsing the contact out of the response fails.
     */
    public Contact getContact() throws OXException {
        if (null == contactObj) {
            this.contactObj = new Contact();
            JSONObject json = (JSONObject) getResponse().getData();
            new ContactParser(true, timeZone).parse(contactObj, json);
        }

        return contactObj;
    }

    /**
     * @param contactObj the contact to set
     */
    public void setContact(final Contact contactObj) {
        this.contactObj = contactObj;
    }

    /**
     * Gets the imageUrl
     *
     * @return The imageUrl
     * @throws OXException
     */
    public String getImageUrl() throws OXException {
        extractImageUrl();
        return imageUrl;
    }

    public String getImageUid() throws OXException {
        final String imageUrl = getImageUrl();
        if (imageUrl == null) {
            return null;
        }
        final String path = "/ajax/image?uid=";
        int index = imageUrl.indexOf(path);

        String uid;
        try {
            uid = URLDecoder.decode(imageUrl.substring(index + path.length(), imageUrl.length()), "UTF-8");
            return uid;
        } catch (UnsupportedEncodingException e) {
            throw OXException.general(e.getMessage());
        }
    }

    private void extractImageUrl() throws OXException {
        JSONObject json = (JSONObject) getResponse().getData();
        if (imageUrl == null && json.hasAndNotNull("image1_url")) {
            try {
                this.imageUrl = json.getString("image1_url");
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
            }
        }
    }
}
