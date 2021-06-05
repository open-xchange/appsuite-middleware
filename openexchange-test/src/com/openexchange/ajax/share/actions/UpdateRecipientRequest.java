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
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonDeleteParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;
import com.openexchange.share.recipient.AnonymousRecipient;

/**
 * {@link UpdateRecipientRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpdateRecipientRequest implements AJAXRequest<AbstractAJAXResponse> {

    private final boolean failOnError;
    private final AnonymousRecipient recipient;
    private final int entity;

    /**
     * Initializes a new {@link UpdateRecipientRequest}.
     *
     * @param entity The guest entity to update with the recipient definition
     * @param recipient The recipient to update
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     */
    public UpdateRecipientRequest(int entity, AnonymousRecipient recipient, boolean failOnError) {
        super();
        this.entity = entity;
        this.failOnError = failOnError;
        this.recipient = recipient;
    }

    /**
     * Initializes a new {@link UpdateRecipientRequest}.
     *
     * @param entity The guest entity to update with the recipient definition
     * @param recipient The recipient to update
     */
    public UpdateRecipientRequest(int entity, AnonymousRecipient recipient) {
        this(entity, recipient, true);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public String getServletPath() {
        return "/ajax/share/management";
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        return new Params(AJAXServlet.PARAMETER_ACTION, "updateRecipient", "entity", String.valueOf(entity)).toArray();
    }

    @Override
    public CommonDeleteParser getParser() {
        return new CommonDeleteParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", recipient.getType().toString().toLowerCase());
        jsonObject.put("password", recipient.getPassword());
        jsonObject.put("bits", recipient.getBits());
        return jsonObject;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[0];
    }

}
