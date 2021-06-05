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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.ajax.user.UserImpl4Test;
import com.openexchange.groupware.container.Contact;

/**
 * Stores the response of searched users.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class SearchResponse extends CommonSearchResponse {

    private final int[] userImplAttributes;

    /**
     * @param response
     */
    public SearchResponse(final Response response, final int[] columns) {
        super(response);
        this.userImplAttributes = columns;
    }

    public UserImpl4Test[] getUser() {
        final List<UserImpl4Test> list = new ArrayList<UserImpl4Test>();
        for (final Object[] data : this) {
            final UserImpl4Test user = new UserImpl4Test();
            for (final int attribute : userImplAttributes) {
                final Object value = data[getColumnPos(attribute)];
                switch (attribute) {
                    case Contact.INTERNAL_USERID:
                        if (JSONObject.NULL == value) {
                            break;
                        }
                        user.setId(((Integer) value).intValue());
                        break;
                    case Contact.EMAIL1:
                        if (JSONObject.NULL == value) {
                            user.setMail(null);
                            break;
                        }
                        user.setMail((String) value);
                        break;
                    case Contact.DISPLAY_NAME:
                        user.setDisplayName((value == JSONObject.NULL) ? null : (String) value);
                        break;
                    case Contact.GIVEN_NAME:
                        user.setGivenName((value == JSONObject.NULL) ? null : (String) value);
                        break;
                    case Contact.SUR_NAME:
                        user.setSurname((value == JSONObject.NULL) ? null : (String) value);
                        break;
                }
            }
            list.add(user);
        }
        return list.toArray(new UserImpl4Test[list.size()]);
    }
}
