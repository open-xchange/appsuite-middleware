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

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.groupware.container.Contact;

/**
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public abstract class AbstractContactRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    /**
     * URL of the contacts AJAX interface.
     */
    static final String URL = "/ajax/contacts";

    /**
     * Default constructor.
     */
    protected AbstractContactRequest() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletPath() {
        return URL;
    }

    protected JSONObject convert(final Contact contactObj) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        final ContactWriter contactWriter = new ContactWriter(TimeZone.getTimeZone("UTC"));
        contactWriter.writeContact(contactObj, jsonObj, null);
        return jsonObj;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }
}
