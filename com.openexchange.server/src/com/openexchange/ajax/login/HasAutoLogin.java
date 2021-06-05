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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.AJAXServlet.ACTION_AUTOLOGIN;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link HasAutoLogin} implements the hasAutoLogin action.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.2
 */
public final class HasAutoLogin implements LoginRequestHandler {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HasAutoLogin.class);
    }

    private static final HasAutoLogin INSTANCE = new HasAutoLogin();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static HasAutoLogin getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private HasAutoLogin() {
        super();
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        AJAXServlet.setDefaultContentType(resp);
        try {
            final JSONObject json = new JSONObject(2);
            json.put(ACTION_AUTOLOGIN, true); // Keeping this for compatibility...
            json.write(resp.getWriter());
            requestContext.getMetricProvider().recordSuccess();
        } catch (JSONException e) {
            LoggerHolder.LOG.error(LoginServlet.RESPONSE_ERROR, e);
            LoginServlet.sendError(resp);
            requestContext.getMetricProvider().recordHTTPStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
