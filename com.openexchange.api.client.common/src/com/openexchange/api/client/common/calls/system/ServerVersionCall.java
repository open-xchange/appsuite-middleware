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

package com.openexchange.api.client.common.calls.system;

import java.util.Map;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.version.ServerVersion;

/**
 * {@link ServerVersionCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ServerVersionCall extends AbstractGetCall<ServerVersion> {

    @Override
    public boolean appendSessionToPath() {
        return false;
    }

    @Override
    @NonNull
    public String getModule() {
        return "version";
    }

    @Override
    protected String getAction() {
        return "version";
    }

    @Override
    public HttpResponseParser<ServerVersion> getParser() {
        return new ServerVersionParser();
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {}

    /**
     * {@link ServerVersionParser}
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.5
     */
    private final static class ServerVersionParser extends AbstractHttpResponseParser<ServerVersion> {

        /** Simple class to delay initialization until needed */
        private static class LoggerHolder {

            static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ServerVersionCall.class);
        }

        /**
         * Initializes a new {@link ServerVersionParser}.
         */
        public ServerVersionParser() {
            super();
        }

        @Override
        public ServerVersion parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
            JSONObject json = commonResponse.getJSONObject();
            String serverVersion = json.getString("version");
            if (Strings.isEmpty(serverVersion)) {
                return null;
            }
            /*
             * Something like "7.10.5-Rev1"
             */
            try {
                return ServerVersion.parse(serverVersion);
            } catch (Exception e) {
                LoggerHolder.LOG.info("Unable to parse version string from {}", serverVersion, e);
            }
            return null;
        }
    }
}
