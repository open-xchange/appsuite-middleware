/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

        private static final String REVISION_ID = "Rev";

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
            String[] splitBy = Strings.splitBy(serverVersion, '-', true);
            if (null == splitBy || 2 != splitBy.length) {
                return null;
            }
            String buildNumber = splitBy[1];
            if (Strings.isNotEmpty(buildNumber) && buildNumber.startsWith(REVISION_ID) && buildNumber.length() > REVISION_ID.length()) {
                buildNumber = buildNumber.substring(REVISION_ID.length());
            }

            try {
                return new ServerVersion(splitBy[0], buildNumber);
            } catch (Exception e) {
                LoggerHolder.LOG.info("Unable to parse version string from {}", serverVersion, e);
            }
            return null;
        }
    }
}
