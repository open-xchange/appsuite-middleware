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
import org.junit.Assert;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.proxy.ClearRequest;
import com.openexchange.ajax.proxy.MockRequest;
import com.openexchange.ajax.proxy.MockRequestMethod;
import com.openexchange.ajax.proxy.MockResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderCalendarConfig;
import com.openexchange.testing.httpclient.models.FolderCalendarExtendedProperties;
import com.openexchange.testing.httpclient.models.FolderData;

/**
 * {@link AbstractExternalProviderChronosTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
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
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().useEnhancedApiClients().build();
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
        MockResponse mockResponse = getClient().execute(mockRequest);
        Object data = mockResponse.getData();
        Assert.assertFalse("Request could not be mocked", null == data);
        Assert.assertTrue("Request could not be mocked", String.class.isAssignableFrom(data.getClass()));
        Assert.assertTrue("Request could not be mocked", "ok".equalsIgnoreCase((String) data));
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
    protected FolderData createFolder(String folderName, String module, JSONObject config, JSONObject extendedProperties) throws ApiException, ChronosApiException, JsonParseException, JsonMappingException, IOException {
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
        FolderCalendarConfig expectedConfig = objectMapper.readValue(config.toString(), FolderCalendarConfig.class);
        FolderCalendarExtendedProperties expectedProperties = objectMapper.readValue(extProperties.toString(), FolderCalendarExtendedProperties.class);
        assertEquals(expectedConfig, actualFolderData.getComOpenexchangeCalendarConfig());
        assertEquals(expectedProperties, actualFolderData.getComOpenexchangeCalendarExtendedProperties());
        return actualFolderData;
    }
}
