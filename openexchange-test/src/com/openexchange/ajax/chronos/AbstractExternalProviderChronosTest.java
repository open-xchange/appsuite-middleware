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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.proxy.MockRequest;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractExternalProviderChronosTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractExternalProviderChronosTest extends AbstractChronosTest {

    /**
     * Initialises a new {@link AbstractExternalProviderChronosTest}.
     */
    public AbstractExternalProviderChronosTest() {
        super();
    }

    /**
     * Mocks an external provider request with the specified URI, response content/payload, and status code
     * 
     * @param uri The URI of the external source
     * @param responseContent The response content/payload
     * @param httpStatus The response status code
     * @throws OXException if an error is occurred
     * @throws IOException if an I/O error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    void mock(String uri, String responseContent, int httpStatus) throws OXException, IOException, JSONException {
        mock(uri, responseContent, httpStatus, Collections.emptyMap());
    }

    /**
     * Mocks an external provider request with the specified URI, response content/payload, status code and response headers
     * 
     * @param uri The URI of the external source
     * @param responseContent The response content/payload
     * @param httpStatus The response status code
     * @param responseHeaders the response headers
     * @throws OXException if an error is occurred
     * @throws IOException if an I/O error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    void mock(String uri, String responseContent, int httpStatus, Map<String, String> responseHeaders) throws OXException, IOException, JSONException {
        mock(uri, responseContent, httpStatus, responseHeaders, 0);
    }

    /**
     * Mocks an external provider request with the specified URI, response content/payload,
     * status code, response headers and simulated response time
     * 
     * @param uri The URI of the external source
     * @param responseContent The response content/payload
     * @param httpStatus The response status code
     * @param responseHeaders the response headers
     * @param delay The simulated delay/response time
     * @throws OXException if an error is occurred
     * @throws IOException if an I/O error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    void mock(String uri, String responseContent, int httpStatus, Map<String, String> responseHeaders, int delay) throws OXException, IOException, JSONException {
        InputStream stream = new ByteArrayInputStream(responseContent.getBytes(StandardCharsets.UTF_8.name()));
        MockRequest mockRequest = new MockRequest(uri, stream, httpStatus, responseHeaders, delay);
        client.execute(mockRequest);
    }
}
