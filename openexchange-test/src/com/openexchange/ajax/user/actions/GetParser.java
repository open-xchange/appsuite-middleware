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

package com.openexchange.ajax.user.actions;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.user.UserImpl4Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.parser.UserParser;

public class GetParser extends AbstractAJAXParser<GetResponse> {

    private final TimeZone timeZone;

    private final int userId;

    GetParser(boolean failOnError, int userId, TimeZone timeZone) {
        super(failOnError);
        this.userId = userId;
        this.timeZone = timeZone;
    }

    @Override
    protected GetResponse createResponse(final Response response) {
        return new GetResponse(response);
    }

    @Override
    public GetResponse parse(final String body) throws JSONException {
        final GetResponse retval = super.parse(body);
        if (false == retval.hasError() && null != retval.getData()) {
            try {
                JSONObject data = (JSONObject) retval.getData();
                Contact contact = UserParser.parseUserContact(data, timeZone);
                retval.setContact(contact);
                final UserImpl4Test user = new UserImpl4Test();
                user.setId(userId);
                user.setDisplayName(contact.getDisplayName());
                user.setGivenName(contact.getGivenName());
                user.setSurname(contact.getSurName());
                user.setMail(contact.getEmail1());
                JSONArray jGroups = data.optJSONArray("groups");
                if (jGroups != null) {
                    int[] groups = new int[jGroups.length()];
                    for (int i = 0; i < jGroups.length(); i++) {
                        groups[i] = jGroups.getInt(i);
                    }
                    user.setGroups(groups);
                }
                user.setGuestCreatedBy(data.optInt("guest_created_by", 0));
                if (data.has(UserField.IMAGE1_URL.getName())) {
                    retval.setImageUrl(data.getString(UserField.IMAGE1_URL.getName()));
                }
                retval.setUser(user);
            } catch (OXException e) {
                throw new JSONException(e);
            }
        }
        return retval;
    }
}
