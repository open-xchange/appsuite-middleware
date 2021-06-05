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
import org.json.JSONArray;
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
 * {@link SendLinkRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class SendLinkRequest implements AJAXRequest<SendLinkResponse> {

    private final ShareTarget target;
    private final String recipient;
    private final String message;
    private boolean failOnError = true;

    /**
     * Initializes a new {@link SendLinkRequest} with UTC as time zone
     *
     * @param target The share target
     * @param recipient The recipient
     */
    public SendLinkRequest(ShareTarget target, String recipient) {
        this(target, recipient, null);
    }

    /**
     * Initializes a new {@link SendLinkRequest}.
     *
     * @param target The share target
     * @param recipient The recipient
     * @param message The message
     */
    public SendLinkRequest(ShareTarget target, String recipient, String message) {
        super();
        this.target = target;
        this.recipient = recipient;
        this.message = message;
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
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Params(AJAXServlet.PARAMETER_ACTION, "sendLink").toArray();
    }

    @Override
    public AbstractAJAXParser<SendLinkResponse> getParser() {
        return new Parser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject body = ShareWriter.writeTarget(target);
        JSONArray jRecipients = new JSONArray();
        JSONArray jRecipient = new JSONArray();
        jRecipient.put(recipient);
        jRecipients.put(jRecipient);
        body.put("recipients", jRecipients);
        if (message != null) {
            body.put("message", message);
        }
        return body;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    private static final class Parser extends AbstractAJAXParser<SendLinkResponse> {

        /**
         * Initializes a new {@link Parser}.
         *
         * @param failOnError
         */
        protected Parser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected SendLinkResponse createResponse(Response response) throws JSONException {
            return new SendLinkResponse(response);
        }

    }

}
