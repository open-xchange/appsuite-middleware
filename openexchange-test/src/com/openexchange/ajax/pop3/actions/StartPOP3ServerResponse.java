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

package com.openexchange.ajax.pop3.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.writer.ResponseWriter;

/**
 * Depends on bundle com.openexchange.test.pop3, contained in backend-test.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class StartPOP3ServerResponse extends AbstractAJAXResponse {

    private String host;
    private int port;

    /**
     * Initializes a new {@link StartPOP3ServerResponse}.
     * 
     * @param response
     * @throws JSONException
     */
    protected StartPOP3ServerResponse(Response response) throws JSONException {
        super(response);
        JSONObject json = ResponseWriter.getJSON(response);
        JSONObject data = json.getJSONObject("data");
        host = data.getString("host");
        port = data.getInt("port");
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

}
