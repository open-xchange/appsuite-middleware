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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.share.servlet.ShareServletStrings;
import com.openexchange.share.servlet.utils.LoginLocation;
import com.openexchange.share.servlet.utils.LoginLocationRegistry;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;

/**
 * {@link RedeemLoginLocationTokenServlet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class RedeemLoginLocationTokenServlet extends AbstractShareServlet {

    private static final long serialVersionUID = 235601793249651110L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RedeemLoginLocationTokenServlet.class);

    /**
     * Initializes a new {@link RedeemLoginLocationTokenServlet}.
     *
     * @param hashSalt The hash salt to use
     */
    public RedeemLoginLocationTokenServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Tools.disableCaching(response);
        Translator translator = Translator.EMPTY;
        try {
            try {
                TranslatorFactory translatorFactory = ShareServiceLookup.getService(TranslatorFactory.class, true);
                translator = translatorFactory.translatorFor(determineLocale(request, null));
            } catch (OXException e) {
                LOG.error("", e);
                new JSONObject(2).put("error", translator.translate(ShareServletStrings.INVALID_REQUEST)).write(response.getWriter());
                return;
            }

            request.getSession(true);
            response.setStatus(HttpServletResponse.SC_OK);
            AJAXServlet.setDefaultContentType(response);

            String token = request.getParameter("token");
            if (Strings.isEmpty(token)) {
                new JSONObject(2).put("error", translator.translate(ShareServletStrings.INVALID_REQUEST)).write(response.getWriter());
                return;
            }

            LoginLocation location = LoginLocationRegistry.getInstance().getIfPresent(token);
            if (null == location) {
                new JSONObject(2).put("error", translator.translate(ShareServletStrings.INVALID_REQUEST)).write(response.getWriter());
                return;
            }

            Map<String, String> loginLocationParameters = location.asMap(translator);
            JSONObject jLoginLocation = new JSONObject(loginLocationParameters.size());
            for (Entry<String, String> loginLocationParameter : loginLocationParameters.entrySet()) {
                jLoginLocation.put(loginLocationParameter.getKey(), loginLocationParameter.getValue());
            }
            jLoginLocation.write(response.getWriter());
        } catch (RateLimitedException e) {
            // Mark optional HTTP session as rate-limited
            HttpSession optionalHttpSession = request.getSession(false);
            if (optionalHttpSession != null) {
                optionalHttpSession.setAttribute(com.openexchange.servlet.Constants.HTTP_SESSION_ATTR_RATE_LIMITED, Boolean.TRUE);
            }
            // Send error response
            e.send(response);
        } catch (JSONException e) {
            if (e.getCause() instanceof IOException) {
                /*
                 * Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                 * throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                 */
                throw (IOException) e.getCause();
            }

            /*
             * Signal JSON syntax error (which cannot occur)
             */
            LOG.error("", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A JSON error occurred");
            } catch (IOException ioe) {
                LOG.error("", ioe);
            }
        }
    }

}
