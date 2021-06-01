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
import java.util.UUID;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.FinalContactConstants;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;
import com.openexchange.groupware.container.Contact;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class GetAssociatedContactsRequest extends AbstractContactRequest<GetAssociatedContactsResponse> {

    protected UUID uuid;
    protected Contact contact;
    protected TimeZone tz;

    public GetAssociatedContactsRequest(UUID uid, TimeZone tz) {
        super();
        this.uuid = uid;
        this.tz = tz;
    }

    public GetAssociatedContactsRequest(Contact c, TimeZone tz) {
        super();
        this.contact = c;
        this.tz = tz;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        if (uuid != null) {
            return new Params(AJAXServlet.PARAMETER_ACTION, FinalContactConstants.ACTION_GET_ASSOCIATED.getName(), FinalContactConstants.PARAMETER_UUID.getName(), String.valueOf(uuid)).toArray();
        }

        Params params = new Params(AJAXServlet.PARAMETER_ACTION, FinalContactConstants.ACTION_GET_ASSOCIATED.getName());
        if (contact.containsUserField20()) {
            params.add(FinalContactConstants.PARAMETER_UUID.getName(), contact.getUserField20());
        } else {
            params.add(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(contact.getParentFolderID()), AJAXServlet.PARAMETER_ID, String.valueOf(contact.getObjectID()));
        }

        return params.toArray();
    }

    @Override
    public AbstractAJAXParser<GetAssociatedContactsResponse> getParser() {
        return new AbstractAJAXParser<GetAssociatedContactsResponse>(true) {

            @Override
            public GetAssociatedContactsResponse createResponse(final Response response) {
                return new GetAssociatedContactsResponse(response);
            }
        };
    }
}
