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

package com.openexchange.ajax.chronos.schedjoules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;
import org.junit.Test;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.openexchange.ajax.chronos.factory.CalendarFolderConfig;
import com.openexchange.ajax.chronos.factory.CalendarFolderExtendedProperty;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.configuration.asset.AssetType;
import com.openexchange.testing.httpclient.models.FolderData;

/**
 * {@link BasicSchedJoulesProviderTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicSchedJoulesProviderTest extends AbstractSchedJoulesProviderTest {

    /**
     * Initialises a new {@link BasicSchedJoulesProviderTest}.
     */
    public BasicSchedJoulesProviderTest() {
        super();
    }

    /**
     * Tests the initial creation of a calendar account with
     * a SchedJoules subscription to a non existing calendar
     */
    @Test
    public void testCreateFolderWithNonExistingSchedJoulesCalendar() throws Exception {
        try {
            Asset asset = assetManager.getAsset(AssetType.json, "schedjoulesPageNotFoundResponse.json");
            mock("http://example.com/pages/" + NON_EXISTING_CALENDAR, assetManager.readAssetString(asset), HttpStatus.SC_NOT_FOUND, RESPONSE_HEADERS);

            JSONObject config = new JSONObject();
            config.put(CalendarFolderConfig.ITEM_ID.getFieldName(), NON_EXISTING_CALENDAR);

            folderManager.createFolder(MODULE, PROVIDER_ID, "testCreateAccountWithNonExistingSchedJoulesCalendar", config, new JSONObject(), true);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("SCHEDJOULES-0007", e.getErrorCode());
        }
    }

    /**
     * Tests the initial creation of a calendar folder with
     * a SchedJoules subscription to an invalid calendar, i.e. a 'page' item_class
     */
    @Test
    public void testCreateFolderWithInvalidSchedJoulesCalendar() throws Exception {
        try {
            Asset rootPageAsset = assetManager.getAsset(AssetType.json, "schedjoulesRootPageResponse.json");
            mock("http://example.com/pages/" + ROOT_PAGE, assetManager.readAssetString(rootPageAsset), HttpStatus.SC_OK, RESPONSE_HEADERS);

            JSONObject config = new JSONObject();
            config.put(CalendarFolderConfig.ITEM_ID.getFieldName(), ROOT_PAGE);

            folderManager.createFolder(MODULE, PROVIDER_ID, "testCreateAccountWithInvalidSchedJoulesCalendar", config, new JSONObject(), true);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("SCHEDJOULES-0001", e.getErrorCode());
        }
    }

    /**
     * Tests the initial creation of a calendar folder with
     * a SchedJoules subscription to a random calendar.
     */
    @Test
    public void testCreateFolderWithSchedJoulesSubscription() throws Exception {
        createFolder(CALENDAR_ONE, folderName);
    }

    /**
     * Tests the update of a SchedJoules calendar folder by changing the colour and
     * renaming the folder.
     */
    @Test
    public void testUpdateFolderChangeColorAndRename() throws Exception {
        FolderData folderData = createFolder(CALENDAR_TWO, folderName);

        String expectedColor = "white";
        String expectedTitle = "testUpdateFolderChangeColorAndRename";
        folderData.getComOpenexchangeCalendarExtendedProperties().getColor().setValue(expectedColor);
        folderData.setTitle(expectedTitle);

        folderManager.updateFolder(folderData);

        FolderData actualFolderData = folderManager.getFolder(folderData.getId());

        JSONObject expectedConfig = new JSONObject();
        expectedConfig.put(CalendarFolderConfig.REFRESH_INTERVAL.getFieldName(), 1440);
        expectedConfig.put(CalendarFolderConfig.ITEM_ID.getFieldName(), CALENDAR_TWO);
        expectedConfig.put(CalendarFolderConfig.LOCALE.getFieldName(), "en");

        JSONObject extendedProperties = defaultExtendedProperties();
        extendedProperties.getJSONObject(CalendarFolderExtendedProperty.COLOR.getFieldName()).put("value", expectedColor);
        assertFolderData(actualFolderData, expectedTitle, expectedConfig, extendedProperties);

        assertEquals(expectedColor, folderData.getComOpenexchangeCalendarExtendedProperties().getColor().getValue());
    }

    /**
     * Tests the deletion of a SchedJoules calendar folder
     */
    @Test
    public void testDeleteSchedJoulesSubscription() throws Exception {
        FolderData folderData = createFolder(CALENDAR_THREE, folderName);
        String folderId = folderData.getId();

        folderManager.deleteFolder(folderId);
        try {
            folderManager.getFolder(folderId, true);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("CAL-4044", e.getErrorCode());
        }
    }

    /**
     * Tests for a locale change
     */
    @Test
    public void testChangeLocale() throws Exception {
        FolderData folderData = createFolder(CALENDAR_THREE, folderName);

        String expectedLocale = "de";
        folderData.getComOpenexchangeCalendarConfig().setLocale(expectedLocale);

        folderManager.updateFolder(folderData);

        folderData = folderManager.getFolder(folderData.getId());

        JSONObject expectedConfig = new JSONObject();
        expectedConfig.put(CalendarFolderConfig.REFRESH_INTERVAL.getFieldName(), 1440);
        expectedConfig.put(CalendarFolderConfig.ITEM_ID.getFieldName(), CALENDAR_THREE);
        expectedConfig.put(CalendarFolderConfig.LOCALE.getFieldName(), "de");

        assertFolderData(folderData, folderName, expectedConfig, defaultExtendedProperties());
    }

    /**
     * Tests for an refresh interval change
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Test
    public void testChangeToInvalidRefreshInterval() throws Exception {
        FolderData folderData = createFolder(CALENDAR_THREE, folderName);
        folderData.getComOpenexchangeCalendarConfig().setRefreshInterval(123);
        try {
            folderManager.updateFolder(folderData, true);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("SCHEDJOULES-0011", e.getErrorCode());
        }
    }
}
