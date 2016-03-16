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

package com.openexchange.rest.client;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.rest.client.RESTRequest.Method;
import com.openexchange.rest.client.session.Session;

/**
 * {@link RESTRequestFactory}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RESTRequestFactory {

    /**
     * Create a complete {@link RESTRequest} with all values filled
     * 
     * @param method The HTTP {@link Method}
     * @param session The {@link Session} to use for this request.
     * @param host The host on which resides the REST API
     * @param apiVersion The API version to use.
     * @param path The URL path starting with a '/'.
     * @param expectedStatusCodes The expected status code(s) on successful response
     * @param body The request's JSON object
     * @param params Any URL parameters in a String array. with the even numbered elements the parameter names and odd numbered elements the
     *            values, e.g. <code>new String[] {"path", "/Public", "locale", "en"}</code>.
     * @param expectedStatusCodes
     * @return A {@link RESTRequest}
     */
    public static RESTRequest completeRequest(final Method method, final Session session, final String host, final int apiVersion, final String path, final List<String> params, final JSONObject body, final List<Integer> expectedStatusCodes) {
        final RESTRequest req = new RESTRequest(session, method, host, path, expectedStatusCodes);
        req.setAPIVersion(apiVersion).setParameters(params).setRequestBody(body);
        return req;
    }

    /**
     * Create a basic {@link RESTRequest}
     * 
     * @param method The HTTP {@link Method}
     * @param session The {@link Session} to use for this request.
     * @param host The host on which resides the REST API
     * @param path The URL path starting with a '/'.
     * @param expectedStatusCodes The expected status code(s) on successful response
     * @return A {@link RESTRequest}
     */
    public static RESTRequest basicRequest(final Method method, final Session session, final String host, final String path, final List<Integer> expectedStatusCodes) {
        return new RESTRequest(session, method, host, path, expectedStatusCodes);
    }
}
