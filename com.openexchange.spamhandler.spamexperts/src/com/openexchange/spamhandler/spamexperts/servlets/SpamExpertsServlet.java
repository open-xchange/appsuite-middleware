
package com.openexchange.spamhandler.spamexperts.servlets;

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

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.DataServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.spamexperts.management.SpamExpertsConfig;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * Servlet which returns needed Data for the Spamexperts Iframe Plugin to redirect
 * and authenticate to an external GUI.
 *
 * Also does jobs for the other GUI Plugin
 *
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 *
 */
public final class SpamExpertsServlet extends DataServlet {

    private static final long serialVersionUID = -8914926421736440078L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamExpertsServlet.class);

    private final transient SpamExpertsConfig config;

    private transient ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link SpamExpertsServlet}.
     *
     * @param config The configuration to use
     * @param serviceLookup The {@link ServiceLookup}
     */
    public SpamExpertsServlet(SpamExpertsConfig config, ServiceLookup serviceLookup) {
        super();
        this.config = config;
        this.serviceLookup = serviceLookup;
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Session session = getSessionObject(req);

        Response response = new Response();
        try {
            String action = parseMandatoryStringParameter(req, PARAMETER_ACTION);

            JSONObject jsonObj;
            try {
                jsonObj = convertParameter2JSONObject(req);
            } catch (JSONException e) {
                LOG.error("", e);
                response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
                writeResponse(response, resp, session);
                return;
            }

            SpamExpertsServletRequest proRequest = new SpamExpertsServletRequest(session, config, serviceLookup.getServiceSafe(HttpClientService.class).getHttpClient("spamexperts"));
            Object responseObj = proRequest.action(action, jsonObj);
            response.setData(responseObj);
        } catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
        }

        writeResponse(response, resp, session);
    }
}
