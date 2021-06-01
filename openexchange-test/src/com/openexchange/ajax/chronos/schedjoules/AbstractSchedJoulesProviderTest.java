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

package com.openexchange.ajax.chronos.schedjoules;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.chronos.AbstractExternalProviderChronosTest;
import com.openexchange.ajax.chronos.factory.CalendarFolderConfig;
import com.openexchange.ajax.chronos.factory.CalendarFolderExtendedProperty;
import com.openexchange.ajax.proxy.MockRequestMethod;
import com.openexchange.test.common.asset.Asset;
import com.openexchange.test.common.asset.AssetType;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.models.FolderData;

/**
 * {@link AbstractSchedJoulesProviderTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractSchedJoulesProviderTest extends AbstractExternalProviderChronosTest {

    private static final Map<String, String> CONFIG = new HashMap<String, String>();
    static {
        CONFIG.put("com.openexchange.calendar.schedjoules.host", "example.com");
        CONFIG.put("com.openexchange.calendar.schedjoules.scheme", "http");
    }

    static final Map<String, String> RESPONSE_HEADERS = Collections.singletonMap(HttpHeaders.CONTENT_TYPE, "application/json");
    static final String MODULE = "event";
    static final String PROVIDER_ID = "schedjoules";
    static final int ROOT_PAGE = 115673;
    static final int NON_EXISTING_CALENDAR = 31145;
    static final int CALENDAR_ONE = 90734;
    static final int CALENDAR_TWO = 24428282;
    static final int CALENDAR_THREE = 24428313;
    String folderName = null;

    private static final int DEFAULT_REFRESH_INTERVAL = 1440;
    private static final String DEFAULT_LOCALE = "en";

    static final long TIMESTAMP = 1516370400000L;

    /**
     * Initialises a new {@link AbstractSchedJoulesProviderTest}.
     *
     * @param providerId
     */
    public AbstractSchedJoulesProviderTest() {
        super(PROVIDER_ID);
    }

    /**
     * Setup the test
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderName = "testfolder_" + System.nanoTime();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().useEnhancedApiClients().build();
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getScope() {
        return "context";
    }

    /**
     * Creates a folder with a subscription to schedjoules feed with the specified item identifier and the specified name
     *
     * @param itemId The item identifier
     * @param folderName The folder name
     * @return The {@link FolderData} of the created folder
     * @throws Exception
     */
    FolderData createFolder(int itemId, String folderName) throws Exception {
        Asset pageAsset = assetManager.getAsset(AssetType.json, "schedjoulesPageResponse.json");
        mock("http://example.com/pages/" + itemId, assetManager.readAssetString(pageAsset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        Asset calendarAsset = assetManager.getAsset(AssetType.ics, "schedjoulesCalendarResponse.ics");
        mock(MockRequestMethod.GET, "http://example.com/calendars/dbe807996754?l=en&x=239a18", assetManager.readAssetString(calendarAsset), HttpStatus.SC_OK, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, "text/calendar"), 0);

        Map<String, String> copy = new HashMap<>();
        copy.putAll(RESPONSE_HEADERS);
        copy.put("ETag", "9dcd50c9806b5b316dfbc31dd6739d22");
        mock(MockRequestMethod.HEAD, "http://example.com/calendars/dbe807996754?l=en&x=239a18", " ", HttpStatus.SC_OK, copy, 0);

        JSONObject config = new JSONObject();
        config.put(CalendarFolderConfig.ITEM_ID.getFieldName(), itemId);
        config.put(CalendarFolderConfig.REFRESH_INTERVAL.getFieldName(), DEFAULT_REFRESH_INTERVAL);
        config.put(CalendarFolderConfig.LOCALE.getFieldName(), DEFAULT_LOCALE);

        return createFolder(folderName, MODULE, config, defaultExtendedProperties());
    }

    /**
     * Prepare a {@link JSONObject} with the default extended properties
     *
     * @return a {@link JSONObject} with the default extended properties
     * @throws JSONException if a JSON parsing error is occurred
     */
    protected JSONObject defaultExtendedProperties() throws JSONException {
        JSONObject extendedProperties = new JSONObject();
        JSONObject v = new JSONObject();
        v.put("value", "TRANSPARENT");
        extendedProperties.put(CalendarFolderExtendedProperty.SCHEDULE_TRANSP.getFieldName(), v);

        v = new JSONObject();
        v.put("value", "false");
        extendedProperties.put(CalendarFolderExtendedProperty.COLOR.getFieldName(), new JSONObject());
        extendedProperties.put(CalendarFolderExtendedProperty.DESCRIPTION.getFieldName(), new JSONObject());
        extendedProperties.put(CalendarFolderExtendedProperty.LAST_UPDATE.getFieldName(), new JSONObject());

        return extendedProperties;
    }
}
