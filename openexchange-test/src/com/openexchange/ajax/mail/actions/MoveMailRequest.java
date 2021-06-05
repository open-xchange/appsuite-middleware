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
 * {@link MoveMailRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MoveMailRequest extends AbstractMailRequest<UpdateMailResponse> {

    public MoveMailRequest(String origin, String destination, String mailID, boolean failOnError) {
        super();
        this.origin = origin;
        this.destination = destination;
        this.mailID = mailID;
        this.failOnError = failOnError;
    }

    public MoveMailRequest(String origin, String destination, String mailID) {
        this(origin, destination, mailID, true);
    }

    private final String destination, origin, mailID;

    private final boolean failOnError;

    @Override
    public Object getBody() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("folder_id", destination);
        return json;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter(Mail.PARAMETER_ACTION, Mail.ACTION_UPDATE));
        list.add(new Parameter(Mail.PARAMETER_FOLDERID, origin)); /*
                                                                   * yes, PARAMETER_FOLDERID is actually "folder", while the key of the
                                                                   * JSONObject in the body is actually "folder_id"
                                                                   */
        list.add(new Parameter(Mail.PARAMETER_ID, mailID));

        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends UpdateMailResponse> getParser() {
        return new AbstractAJAXParser<UpdateMailResponse>(failOnError) {

            @Override
            protected UpdateMailResponse createResponse(final Response response) {
                return new UpdateMailResponse(response);
            }
        };
    }

}
