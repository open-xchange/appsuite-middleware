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
import java.util.Date;
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
 * {@link UpdateLinkRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class UpdateLinkRequest implements AJAXRequest<UpdateLinkResponse> {

    private final ShareTarget target;
    private final long timestamp;
    private final TimeZone timeZone;

    private final boolean failOnError = true;
    private String password = null;
    private boolean containsPassword;
    private Date expiryDate;
    private boolean containsExpiryDate;

    /**
     * Initializes a new {@link UpdateLinkRequest} with UTC as time zone
     *
     * @param target The share target
     * @param timestamp The client timestamp
     */
    public UpdateLinkRequest(ShareTarget target, long timestamp) {
        super();
        this.target = target;
        this.timestamp = timestamp;
        this.timeZone = TimeZones.UTC;
    }

    /**
     * Initializes a new {@link UpdateLinkRequest}.
     *
     * @param target The share target
     * @param timeZone The client timezone
     * @param timestamp The client timestamp
     */
    public UpdateLinkRequest(ShareTarget target, TimeZone timeZone, long timestamp) {
        super();
        this.target = target;
        this.timestamp = timestamp;
        this.timeZone = timeZone;
    }

    public void setPassword(String password) {
        this.password = password;
        containsPassword = true;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
        containsExpiryDate = true;
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
        return new Params(AJAXServlet.PARAMETER_ACTION, "updateLink", AJAXServlet.PARAMETER_TIMESTAMP, Long.toString(timestamp)).toArray();
    }

    @Override
    public AbstractAJAXParser<UpdateLinkResponse> getParser() {
        return new Parser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject json = ShareWriter.writeTarget(target);
        if (containsExpiryDate) {
            if (null == expiryDate) {
                json.put("expiry_date", JSONObject.NULL);
            } else {
                long date = expiryDate.getTime();
                if (null != timeZone) {
                    date += timeZone.getOffset(date);
                }
                json.put("expiry_date", date);
            }
        }
        if (containsPassword) {
            json.put("password", null == password ? JSONObject.NULL : password);
        }
        return json;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    private static final class Parser extends AbstractAJAXParser<UpdateLinkResponse> {

        /**
         * Initializes a new {@link Parser}.
         *
         * @param failOnError
         */
        protected Parser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected UpdateLinkResponse createResponse(Response response) throws JSONException {
            return new UpdateLinkResponse(response);
        }

    }

}
