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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
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
            response.setContentType("text/javascript; charset=UTF-8");

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

            Map<String, String> loginLocationParameters = location.asMap();
            JSONObject jLoginLocation = new JSONObject(loginLocationParameters.size());
            for (Entry<String, String> loginLocationParameter : loginLocationParameters.entrySet()) {
                jLoginLocation.put(loginLocationParameter.getKey(), loginLocationParameter.getValue());
            }
            jLoginLocation.write(response.getWriter());
        } catch (RateLimitedException e) {
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
            } catch (final IOException ioe) {
                LOG.error("", ioe);
            }
        }
    }

}
