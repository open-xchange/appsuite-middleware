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
    public Contact getContact() throws OXException, OXException {
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
        } else {
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
