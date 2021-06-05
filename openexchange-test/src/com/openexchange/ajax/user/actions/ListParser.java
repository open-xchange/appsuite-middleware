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
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractListParser;
import com.openexchange.groupware.container.Contact;

/**
 * {@link ListParser}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ListParser extends AbstractListParser<ListResponse> {

    /**
     * Initializes a new {@link ListParser}.
     */
    public ListParser(final boolean failOnError, final int[] columns) {
        super(failOnError, columns);
    }

    @Override
    protected ListResponse createResponse(final Response response) throws JSONException {
        final ListResponse listR = super.createResponse(response);
        final List<Contact> users = new ArrayList<Contact>();
        for (final Object[] data : listR) {
            assertEquals("Object data array length is different as column array length.", getColumns().length, data.length);
            final Contact user = new Contact();
            for (int i = 0; i < getColumns().length; i++) {
                user.set(getColumns()[i], data[i]);
            }
            users.add(user);
        }
        listR.setUsers(users.toArray(new Contact[users.size()]));
        return listR;
    }

    @Override
    protected ListResponse instantiateResponse(final Response response) {
        return new ListResponse(response);
    }
}
