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

package com.openexchange.ajax.share.actions;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;
import com.openexchange.share.ShareTarget;

/**
 * {@link DeleteLinkRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DeleteLinkRequest implements AJAXRequest<DeleteLinkResponse> {

    private final ShareTarget target;
    private final long timestamp;
    private final boolean failOnError = true;

    /**
     * Initializes a new {@link DeleteLinkRequest}.
     *
     * @param target The share target
     * @param timestamp The client timestamp
     */
    public DeleteLinkRequest(ShareTarget target, long timestamp) {
        super();
        this.timestamp = timestamp;
        this.target = target;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Params(AJAXServlet.PARAMETER_ACTION, "deleteLink", AJAXServlet.PARAMETER_TIMESTAMP, Long.toString(timestamp)).toArray();
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject json = ShareWriter.writeTarget(target);
        return json;
    }

    @Override
    public String getServletPath() {
        return "/ajax/share/management";
    }

    @Override
    public AbstractAJAXParser<DeleteLinkResponse> getParser() {
        return new AbstractAJAXParser<DeleteLinkResponse>(failOnError) {

            @Override
            protected DeleteLinkResponse createResponse(Response response) throws JSONException {
                return new DeleteLinkResponse(response);
            }
        };
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

}
