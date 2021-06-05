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
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;
import com.openexchange.java.util.TimeZones;
import com.openexchange.share.ShareTarget;

/**
 * {@link GetLinkRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GetLinkRequest implements AJAXRequest<GetLinkResponse> {

    private final ShareTarget target;
    private final TimeZone timeZone;
    private boolean failOnError = true;

    /**
     * Initializes a new {@link GetLinkRequest} with UTC as time zone
     *
     * @param target The share target
     */
    public GetLinkRequest(ShareTarget target) {
        super();
        this.target = target;
        this.timeZone = TimeZones.UTC;
    }

    /**
     * Initializes a new {@link GetLinkRequest}.
     *
     * @param target The share target
     * @param timeZone The client timezone
     */
    public GetLinkRequest(ShareTarget target, TimeZone timeZone) {
        super();
        this.target = target;
        this.timeZone = timeZone;
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
        return new Params(AJAXServlet.PARAMETER_ACTION, "getLink").toArray();
    }

    @Override
    public AbstractAJAXParser<GetLinkResponse> getParser() {
        return new Parser(failOnError, timeZone);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return ShareWriter.writeTarget(target);
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    private static final class Parser extends AbstractAJAXParser<GetLinkResponse> {

        private final TimeZone timeZone;

        /**
         * Initializes a new {@link Parser}.
         *
         * @param failOnError
         */
        protected Parser(boolean failOnError, TimeZone timeZone) {
            super(failOnError);
            this.timeZone = timeZone;
        }

        @Override
        protected GetLinkResponse createResponse(Response response) throws JSONException {
            if (!response.hasError()) {
                JSONObject data = (JSONObject) response.getData();
                ShareLink shareLink = new ShareLink(data, timeZone);
                return new GetLinkResponse(response, shareLink);
            }

            return new GetLinkResponse(response, null);
        }

    }

}
