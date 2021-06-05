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

package com.openexchange.ajax.subscribe.actions;

import java.util.Collection;
import java.util.LinkedList;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.JSON;

/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class DeleteSubscriptionRequest extends AbstractSubscriptionRequest<DeleteSubscriptionResponse> {

    private Collection<Integer> IDs;

    public void setIDs(Collection<Integer> iDs) {
        IDs = iDs;
    }

    public Collection<Integer> getIDs() {
        return IDs;
    }

    public DeleteSubscriptionRequest() {
        super();
    }

    public DeleteSubscriptionRequest(int id) {
        this();
        IDs = new LinkedList<Integer>();
        IDs.add(Autoboxing.I(id));
    }

    public DeleteSubscriptionRequest(Collection<Integer> IDs) {
        this();
        setIDs(IDs);
    }

    @Override
    public Object getBody() throws JSONException {
        if (IDs == null) {
            throw new JSONException("Cannot create DeleteRequest: No IDs given for deletion!");
        }
        return JSON.collection2jsonArray(getIDs());
    }

    @Override
    public Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE) };
    }

    @Override
    public AbstractAJAXParser<DeleteSubscriptionResponse> getParser() {
        return new AbstractAJAXParser<DeleteSubscriptionResponse>(getFailOnError()) {

            @Override
            protected DeleteSubscriptionResponse createResponse(final Response response) throws JSONException {
                return new DeleteSubscriptionResponse(response);
            }
        };
    }

}
