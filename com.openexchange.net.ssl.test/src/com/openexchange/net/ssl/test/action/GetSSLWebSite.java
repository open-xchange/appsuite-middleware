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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.net.ssl.test.action;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetSSLWebSite}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetSSLWebSite implements AJAXActionService {

    private ServiceLookup services;

    /**
     * Initialises a new {@link GetSSLWebSite}.
     */
    public GetSSLWebSite(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.requesthandler.AJAXActionService#perform(com.openexchange.ajax.requesthandler.AJAXRequestData, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        String url = requestData.getParameter("url", String.class, false);
        if (!url.startsWith("https")) {
            throw new OXException(1138, "We only accept 'https' URLs. Deal with it. 8:-) 8-)");
        }

        try {
            URLConnection urlConnection = new URL(url).openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
            HttpsURLConnection httpsConnection = (HttpsURLConnection) httpConnection;

            SSLSocketFactory sslSocketFactory = services.getService(SSLSocketFactoryProvider.class).getDefault();
            httpsConnection.setSSLSocketFactory(sslSocketFactory);
            httpConnection = httpsConnection;

            httpConnection.connect();
            return new AJAXRequestResult(parseHeaderFields(httpConnection.getHeaderFields()));
        } catch (IOException | JSONException e) {
            throw new OXException(1138, "An unexpected error occurred", e);
        }
    }

    /**
     * Parse the header fields
     * 
     * @param map The header fields as {@link Map}
     * @return A {@link JSONObject} with all header fields
     * @throws JSONException If a JSON parsing error occurs
     */
    private JSONObject parseHeaderFields(Map<String, List<String>> map) throws JSONException {
        JSONObject json = new JSONObject();
        for (String key : map.keySet()) {
            List<String> list = map.get(key);
            JSONArray array = new JSONArray(list.size());
            for (String entry : list) {
                array.put(entry);
            }
            if (Strings.isEmpty(key)) {
                json.put("responseMessage", array);
            } else {
                json.put(key, array);
            }
        }
        return json;
    }
}
