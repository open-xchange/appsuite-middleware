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

import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class RefreshSubscriptionRequest extends AbstractSubscriptionRequest<RefreshSubscriptionResponse> {

    private int subscriptionID = -1;

    private String folderID = null;

    public void setSubscriptionID(int subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public int getSubscriptionID() {
        return subscriptionID;
    }

    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }

    public String getFolderID() {
        return folderID;
    }

    public RefreshSubscriptionRequest() {
        super();
    }

    /**
     * Sets up a refresh request for either a folder or a subscription.
     *
     * @param subscriptionID ID of a subscription. Set to -1 if not wanted.
     * @param folderID ID of a folder. Set to null if not wanted.
     */
    public RefreshSubscriptionRequest(int subscriptionID, String folderID) {
        this();
        setSubscriptionID(subscriptionID);
        setFolderID(folderID);
    }

    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        Params params = new Params(AJAXServlet.PARAMETER_ACTION, "refresh");
        if (folderID != null) {
            params.add(AJAXServlet.PARAMETER_FOLDERID, folderID);
        }
        if (subscriptionID != -1) {
            params.add(AJAXServlet.PARAMETER_ID, String.valueOf(subscriptionID));
        }
        return params.toArray();
    }

    @Override
    public AbstractAJAXParser<RefreshSubscriptionResponse> getParser() {
        return new AbstractAJAXParser<RefreshSubscriptionResponse>(getFailOnError()) {

            @Override
            protected RefreshSubscriptionResponse createResponse(final Response response) throws JSONException {
                return new RefreshSubscriptionResponse(response);
            }
        };
    }

}
