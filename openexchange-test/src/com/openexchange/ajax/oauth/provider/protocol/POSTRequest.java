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

package com.openexchange.ajax.oauth.provider.protocol;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import com.openexchange.ajax.oauth.provider.EndpointTest;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class POSTRequest extends AbstractRequest<POSTRequest> {

    String sessionId;
    String login;
    String password;
    boolean accessDenied;

    public POSTRequest() {
        super();
    }

    public static GETRequest newGETRequest() {
        return new GETRequest();
    }

    public POSTRequest setLogin(String login) {
        this.login = login;
        return this;
    }

    public POSTRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public POSTRequest setAccessDenied() {
        accessDenied = true;
        return this;
    }

    public POSTResponse submit(HttpClient client) throws IOException {
        Map<String, String> requestSpecificParams = new HashMap<>(5);
        requestSpecificParams.put("access_denied", Boolean.toString(accessDenied));
        if (login != null) {
            requestSpecificParams.put("login", login);
        }
        if (password != null) {
            requestSpecificParams.put("password", password);
        }

        List<NameValuePair> params = prepareParams(requestSpecificParams);
        try {
            HttpPost request = new HttpPost(new URIBuilder()
                .setScheme(scheme)
                .setHost(hostname)
                .setPath(EndpointTest.AUTHORIZATION_ENDPOINT)
                .build());

            for (String header : headers.keySet()) {
                String value = headers.get(header);
                if (value == null) {
                    request.removeHeaders(header);
                } else {
                    request.setHeader(header, value);
                }
            }

            request.setEntity(new UrlEncodedFormEntity(params));
            return new POSTResponse(this, client.execute(request));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
