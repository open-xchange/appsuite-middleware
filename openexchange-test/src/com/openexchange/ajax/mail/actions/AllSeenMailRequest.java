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

package com.openexchange.ajax.mail.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link AllSeenMailRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AllSeenMailRequest extends AbstractMailRequest<AllSeenMailResponse> {

    private final String folderID;

    /**
     * Initializes a new {@link AllSeenMailRequest}.
     */
    public AllSeenMailRequest(final String folderID) {
        super();
        this.folderID = folderID;
    }

    @Override
    public Object getBody() throws JSONException {
        return new JSONObject(0);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        final List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter(Mail.PARAMETER_ACTION, "all_seen"));
        list.add(new Parameter(Mail.PARAMETER_FOLDERID, folderID));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends AllSeenMailResponse> getParser() {
        return new AbstractAJAXParser<AllSeenMailResponse>(true) {

            @Override
            protected AllSeenMailResponse createResponse(final Response response) throws JSONException {
                return new AllSeenMailResponse(response);
            }
        };
    }

}
