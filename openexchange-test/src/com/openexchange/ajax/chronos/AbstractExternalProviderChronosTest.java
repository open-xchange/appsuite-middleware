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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.proxy.ClearRequest;
import com.openexchange.ajax.proxy.MockRequest;
import com.openexchange.ajax.proxy.MockRequestMethod;
import com.openexchange.ajax.proxy.StartMockServerRequest;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedProperties;

/**
 * {@link AbstractExternalProviderChronosTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(BlockJUnit4ClassRunner.class)
public abstract class AbstractExternalProviderChronosTest extends AbstractChronosTest {

    private final String providerId;

    /**
     * Initialises a new {@link AbstractExternalProviderChronosTest}.
     */
    public AbstractExternalProviderChronosTest(String providerId) {
        super();
        this.providerId = providerId;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpConfiguration();
        getClient().execute(new StartMockServerRequest());
    }
    
    protected void clear(String uri) throws OXException, IOException, JSONException {
        ClearRequest clearRequest = new ClearRequest(uri);
        getClient().execute(clearRequest);
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
    protected void mock(String uri, String responseContent, int httpStatus) throws OXException, IOException, JSONException {
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
    protected void mock(String uri, String responseContent, int httpStatus, Map<String, String> responseHeaders) throws OXException, IOException, JSONException {
        mock(MockRequestMethod.GET, uri, responseContent, httpStatus, responseHeaders, 0);
    }

    /**
     * Mocks an external provider request with the specified URI, response content/payload, status code and response headers
     * 
     * @param method The HTTP method
     * @param uri The URI of the external source
     * @param responseContent The response content/payload
     * @param httpStatus The response status code
     * @throws OXException if an error is occurred
     * @throws IOException if an I/O error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    protected void mock(MockRequestMethod method, String uri, String responseContent, int httpStatus) throws OXException, IOException, JSONException {
        mock(method, uri, responseContent, httpStatus, Collections.emptyMap(), 0);
    }

    /**
     * Mocks an external provider request with the specified URI, response content/payload, status code and response headers
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
    protected void mock(String uri, String responseContent, int httpStatus, Map<String, String> responseHeaders, int delay) throws OXException, IOException, JSONException {
        mock(MockRequestMethod.GET, uri, responseContent, httpStatus, responseHeaders, 0);
    }

    /**
     * Mocks an external provider request with the specified URI, response content/payload,
     * status code, response headers and simulated response time
     * 
     * @param method The HTTP method
     * @param uri The URI of the external source
     * @param responseContent The response content/payload
     * @param httpStatus The response status code
     * @param responseHeaders the response headers
     * @param delay The simulated delay/response time
     * @throws OXException if an error is occurred
     * @throws IOException if an I/O error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    protected void mock(MockRequestMethod method, String uri, String responseContent, int httpStatus, Map<String, String> responseHeaders, int delay) throws OXException, IOException, JSONException {
        InputStream stream = new ByteArrayInputStream(responseContent.getBytes(StandardCharsets.UTF_8.name()));
        MockRequest mockRequest = new MockRequest(method, uri, stream, httpStatus, responseHeaders, delay);
        getClient().execute(mockRequest);
    }

    /**
     * Creates a folder with a subscription to schedjoules feed
     * with the specified item identifier and the specified name
     * and asserts the response.
     * 
     * @param itemId The item identifier
     * @param folderName The folder name
     * @return The {@link FolderData} of the created folder
     * @throws ApiException
     * @throws ChronosApiException
     * @throws JSONException
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    protected FolderData createFolder(String folderName, String module, JSONObject config, JSONObject extendedProperties) throws ApiException, ChronosApiException, JSONException, JsonParseException, JsonMappingException, IOException {
        String folderId = folderManager.createFolder(module, providerId, folderName, config, new JSONObject());
        assertNotNull("No folder identifier returned", folderId);
        return assertFolderData(folderManager.getFolder(folderId), folderName, config, extendedProperties);
    }

    /**
     * Asserts the specified {@link FolderData}.
     * 
     * @param actualFolderData The actual {@link FolderData} to assert
     * @param expectedTitle The expected title
     * @return The {@link FolderData}
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    protected FolderData assertFolderData(FolderData actualFolderData, String expectedTitle, JSONObject config, JSONObject extProperties) throws JsonParseException, JsonMappingException, IOException {
        assertNotNull("The folder data is 'null'", actualFolderData);
        assertEquals("The title does not match", expectedTitle, actualFolderData.getTitle());
        assertEquals("The provider identifier does not match", actualFolderData.getComOpenexchangeCalendarProvider(), providerId);
        assertNotNull("The extended properties configuration is 'null'", actualFolderData.getComOpenexchangeCalendarExtendedProperties());
        assertNotNull("The calendar configuration is 'null'", actualFolderData.getComOpenexchangeCalendarConfig());

        ObjectMapper objectMapper = new ObjectMapper();
        FolderDataComOpenexchangeCalendarConfig expectedConfig = objectMapper.readValue(config.toString(), FolderDataComOpenexchangeCalendarConfig.class);
        FolderDataComOpenexchangeCalendarExtendedProperties expectedProperties = objectMapper.readValue(extProperties.toString(), FolderDataComOpenexchangeCalendarExtendedProperties.class);
        assertEquals(expectedConfig, actualFolderData.getComOpenexchangeCalendarConfig());
        assertEquals(expectedProperties, actualFolderData.getComOpenexchangeCalendarExtendedProperties());
        return actualFolderData;
    }
}
