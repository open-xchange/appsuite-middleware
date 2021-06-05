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


package com.openexchange.spamsettings.generic.servlet;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.spamsettings.generic.SpamSettingsParser;
import com.openexchange.spamsettings.generic.SpamSettingsWriter;
import com.openexchange.spamsettings.generic.osgi.SpamSettingsServiceRegistry;
import com.openexchange.spamsettings.generic.service.SpamSettingService;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public final class SpamSettingsServlet extends PermissionServlet {

    /**
     *
     */
    private static final long serialVersionUID = 2124511440962531967L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamSettingsServlet.class);

    /**
     * Initializes
     */
    public SpamSettingsServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        final Response response = new Response();

        final ServerSession session = getSessionObject(req);
        try {
            final String action = JSONUtility.checkStringParameter(req, "action");
            if ("get".equals(action)) {
                final JSONObject result = new JSONObject();
                result.put("formDescription", new SpamSettingsWriter().write(session));
                final SpamSettingService service = SpamSettingsServiceRegistry.getServiceRegistry().getService(SpamSettingService.class);
                result.put("value", getValue(session, service));
                response.setData(result);
            }
        } catch (OXException e) {
            LOG.error("Missing or wrong field action in JSON request", e);
            response.setException(e);
        } catch (JSONException e) {
            LOG.error("", e);
            response.setException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e));
        }

        /*
         * Close response and flush print writer
         */
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private JSONObject getValue(final ServerSession session, final SpamSettingService service) throws JSONException, OXException {
        final Map<String, Object> settings = service.getSettings(session);
        final JSONObject retval = new JSONObject();
        for (final Map.Entry<String, Object> entry : settings.entrySet()) {
            retval.put(entry.getKey(), entry.getValue());
        }
        return retval;
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        final Response response = new Response();

        final JSONObject obj = new JSONObject();

        try {
            final String action = JSONUtility.checkStringParameter(req, "action");
            if (action.equals("update")) {
                final String body = getBody(req);
                final JSONObject jsonObject = new JSONObject(body);
                final Map<String, Object> settings = new SpamSettingsParser().parse(getSessionObject(req), jsonObject);
                final SpamSettingService spamSettingService = SpamSettingsServiceRegistry.getServiceRegistry().getService(SpamSettingService.class);
                spamSettingService.writeSettings(getSessionObject(req), settings);
                obj.put("message", "Settings written");
            }
        } catch (OXException e) {
            LOG.error("Missing or wrong field action in JSON request", e);
            response.setException(e);
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
            response.setException(OXJSONExceptionCodes.JSON_READ_ERROR.create(e));
        }

        response.setData(obj);

        /*
         * Close response and flush print writer
         */
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(getSessionObject(req)));
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
