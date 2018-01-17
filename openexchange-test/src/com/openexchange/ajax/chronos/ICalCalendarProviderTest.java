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

package com.openexchange.ajax.chronos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.chronos.manager.CalendarFolderManager;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeData;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedProperties;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedPropertiesColor;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedPropertiesDescription;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;

/**
 * {@link ICalCalendarProviderTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
// TODO: Use the FolderManager instead of the foldersApi to create new folders
public class ICalCalendarProviderTest extends AbstractExternalProviderChronosTest {

    /**
     * Initialises a new {@link ICalCalendarProviderTest}.
     */
    public ICalCalendarProviderTest() {
        super(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);
    }

    private String createAccount(NewFolderBody body) throws ApiException {
        FolderUpdateResponse response = foldersApi.createFolder(CalendarFolderManager.DEFAULT_FOLDER_ID, defaultUserApi.getSession(), body, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        assertNull("Calendar account could not be created due an error.", response.getError());
        assertNotNull(response.getData());
        return response.getData();
    }

    private long dateToMillis(String date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ");
        return formatter.parseDateTime(date).getMillis();
    }

    @Test
    public void testProbeWithNescriptionAndFeedName_returnFromFeed() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testProbeWithNescriptionAndFeedName_returnFromFeed.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        data.setComOpenexchangeCalendarExtendedProperties(new FolderDataComOpenexchangeCalendarExtendedProperties());

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertEquals("Alle Spiele von FC Schalke 04", probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
        assertEquals("FC Schalke 04", probe.getData().getTitle());
    }

    @Test
    public void testProbeWithNoDescriptionAndFeedName_returnDefault() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testProbeWithNoDescriptionAndFeedName_returnDefault.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        data.setComOpenexchangeCalendarExtendedProperties(new FolderDataComOpenexchangeCalendarExtendedProperties());

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNull(probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription());
        assertEquals("Calendar", probe.getData().getTitle());
    }

    @Test
    public void testProbeWithDescriptionAndFeedNameProvidedByClient_returnProvidedValues() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testProbeWithDescriptionAndFeedNameProvidedByClient_returnProvidedValues.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        String title = "The awesome custom name!";
        data.setTitle(title);
        FolderDataComOpenexchangeCalendarExtendedProperties comOpenexchangeCalendarExtendedProperties = new FolderDataComOpenexchangeCalendarExtendedProperties();
        FolderDataComOpenexchangeCalendarExtendedPropertiesDescription description = new FolderDataComOpenexchangeCalendarExtendedPropertiesDescription();
        description.setValue("My custom description");
        comOpenexchangeCalendarExtendedProperties.setDescription(description);
        data.setComOpenexchangeCalendarExtendedProperties(comOpenexchangeCalendarExtendedProperties);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertEquals(description.getValue(), probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
        assertEquals(title, probe.getData().getTitle());
    }

    @Test
    public void testTooBig() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/tooBig.ics";
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(HttpHeaders.CONTENT_LENGTH, "100000000");
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK, responseHeaders);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals("ICAL-PROV-4001", probe.getCode());
    }

    @Test
    public void testNotFound() throws Exception {
        String externalUri = "http://example.com/files/notFound.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals("ICAL-PROV-4043", probe.getCode());
    }

    @Test
    public void testProbe_Unauthorized() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/testProbeUnauthorized.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_UNAUTHORIZED);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals("CAL-4010", probe.getCode());
    }

    @Test
    public void testDeniedHost() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://localhost/files/testDeniedHost.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals("ICAL-PROV-4042", probe.getCode());
    }

    @Test
    public void testProbe_uriMissing_notFound() throws Exception {
        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals("ICAL-PROV-4040", probe.getCode());
    }

    @Test
    public void testProbe_badURI_notFound() throws Exception {
        String externalUri = "http://localhost/files/notFound.ics";

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals("ICAL-PROV-4042", probe.getCode());
    }

    @Test
    public void testProbe_uriNotFound_notFound() throws Exception {
        String externalUri = "http://example.com/files/notFound.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals("ICAL-PROV-4043", probe.getCode());
    }

    @Test
    public void testProbe_everythingFine_ok() throws Exception {
        String externalUri = "http://example.com/files/myCalendarFile.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNull(probe.getError());
        assertEquals("myCalendarFile", probe.getData().getTitle());
        assertEquals(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID, probe.getData().getComOpenexchangeCalendarProvider());
        assertNull(probe.getData().getComOpenexchangeCalendarConfig().getRefreshInterval());
    }

    @Test
    public void testProbe_everythingFineWithAdditionalProperties_returnProps() throws Exception {
        String externalUri = "http://example.com/files/myCalendarFile2.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNull(probe.getError());
        assertEquals("FC Schalke 04", probe.getData().getTitle());
        assertEquals(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID, probe.getData().getComOpenexchangeCalendarProvider());
        assertEquals("10080", probe.getData().getComOpenexchangeCalendarConfig().getRefreshInterval());
        assertEquals("Alle Spiele von FC Schalke 04", probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testEventsCreated() throws JSONException, ApiException, OXException, IOException {
        String externalUri = "http://example.com/files/testEventsCreated.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());
    }

    @Test
    public void testFolderConfiguredCorrectly_nameAndDescriptionNotAvailableButTitle() throws JSONException, OXException, IOException, ApiException {
        String externalUri = "http://example.com/files/testFolderConfiguredCorrectly_nameAndDescriptionNotAvailable.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String lFolderId = createAccount(body);

        //get events to fill events
        eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, lFolderId);

        FolderResponse returnedFolder = foldersApi.getFolder(defaultUserApi.getSession(), lFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        String folderName = returnedFolder.getData().getTitle();

        assertEquals(folder.getTitle(), folderName);
        assertNull(returnedFolder.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testNameAndDescriptionInIcs_AndClientParamsEmpty_takeNameFromFeed() throws JSONException, OXException, IOException, ApiException {
        String externalUri = "http://example.com/files/testFolderConfiguredCorrectlyIfNameAndDescriptionIsInIcsAndEventsRetrieved.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(102, allEvents.size());

        FolderResponse folderResponse = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        String folderName = folderResponse.getData().getTitle();

        assertEquals("FC Schalke 04", folderName);
        assertEquals("Alle Spiele von FC Schalke 04", folderResponse.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testNameAndDescriptionIsInIcsWithoutRetrievingEvents() throws JSONException, OXException, IOException, ApiException {
        String externalUri = "http://example.com/files/testFolderConfiguredCorrectlyIfNameAndDescriptionIsInIcsWithoutRetrievingEvents.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);
        String newFolderId = createAccount(body);

        FolderResponse folderResponse = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        String folderName = folderResponse.getData().getTitle();

        assertEquals("FC Schalke 04", folderName);
        assertEquals("Alle Spiele von FC Schalke 04", folderResponse.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testFolderUpdate() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testFolderUpdate.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);

        FolderData folderData = folder.getData();
        String changedTitle = "changed";
        folderData.setTitle(changedTitle);
        FolderDataComOpenexchangeCalendarExtendedProperties extendedProperties = new FolderDataComOpenexchangeCalendarExtendedProperties();
        FolderDataComOpenexchangeCalendarExtendedPropertiesColor color = new FolderDataComOpenexchangeCalendarExtendedPropertiesColor();
        color.setValue("blue");
        extendedProperties.setColor(color);
        folderData.setComOpenexchangeCalendarExtendedProperties(extendedProperties);

        FolderBody body = new FolderBody();
        body.setFolder(folderData);

        FolderUpdateResponse updateResponse = foldersApi.updateFolder(defaultUserApi.getSession(), newFolderId, System.currentTimeMillis(), body, false, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE, false);
        assertNull(updateResponse.getError());

        FolderResponse folderReload = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        assertEquals(changedTitle, folderReload.getData().getTitle());
        assertEquals("blue", folderReload.getData().getComOpenexchangeCalendarExtendedProperties().getColor().getValue());
        assertEquals("Alle Spiele von FC Schalke 04", folderReload.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testFolderUpdateWithGettingEvents() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testFolderUpdateWithGettingEvents.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);

        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);

        FolderData folderData = folder.getData();
        folderData.setTitle("changed");
        FolderDataComOpenexchangeCalendarExtendedProperties extendedProperties = new FolderDataComOpenexchangeCalendarExtendedProperties();
        FolderDataComOpenexchangeCalendarExtendedPropertiesColor color = new FolderDataComOpenexchangeCalendarExtendedPropertiesColor();
        color.setValue("blue");
        extendedProperties.setColor(color);
        FolderDataComOpenexchangeCalendarExtendedPropertiesDescription description = new FolderDataComOpenexchangeCalendarExtendedPropertiesDescription();
        String updatedDescription = "Keine Lust auf description";
        description.setValue(updatedDescription);
        extendedProperties.setDescription(description);
        folderData.setComOpenexchangeCalendarExtendedProperties(extendedProperties);
        FolderBody body = new FolderBody();
        body.setFolder(folderData);

        FolderUpdateResponse updateResponse = foldersApi.updateFolder(defaultUserApi.getSession(), newFolderId, System.currentTimeMillis(), body, false, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE, false);
        assertNull(updateResponse.getError());

        FolderResponse folderReload = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        assertEquals("changed", folderReload.getData().getTitle());
        assertEquals(updatedDescription, folderReload.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());

        List<EventData> allEventsReloaded = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(allEvents.size(), allEventsReloaded.size());
    }

    @Test
    public void testCalendarAccountUpdate_updateShouldContainAllFieldsAndEventsOnce() throws JSONException, OXException, IOException, ApiException {
        String externalUri = "http://example.com/files/testCalendarAccountUpdateWithUpdatedURI.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        FolderData data = folder.getData();
        data.getComOpenexchangeCalendarConfig().setRefreshInterval("100000");
        FolderDataComOpenexchangeCalendarExtendedPropertiesColor folderDataComOpenexchangeCalendarExtendedPropertiesColor = new FolderDataComOpenexchangeCalendarExtendedPropertiesColor();
        folderDataComOpenexchangeCalendarExtendedPropertiesColor.setValue("blue");
        data.getComOpenexchangeCalendarExtendedProperties().setColor(folderDataComOpenexchangeCalendarExtendedPropertiesColor);

        FolderBody body = new FolderBody();
        body.setFolder(data);

        FolderUpdateResponse updateResponse = foldersApi.updateFolder(defaultUserApi.getSession(), newFolderId, System.currentTimeMillis(), body, false, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE, false);
        assertNull(updateResponse.getError());

        List<EventData> allEventsAfterUriChange = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
        assertEquals(38, allEventsAfterUriChange.size());

        FolderResponse folderReload = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        assertEquals("blue", folderReload.getData().getComOpenexchangeCalendarExtendedProperties().getColor().getValue()); // should only pass after removing calendar account api
    }

    @Test
    public void testCalendarAccountUriUpdateNotAllowed() throws JSONException, OXException, IOException, ApiException {
        String externalUri = "http://example.com/files/testCalendarAccountUpdateWithUpdatedURI.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        String externalUri2 = "http://example.com/files/testCalendarAccountUpdateWithUpdatedURI_invalidateCache.ics";
        mock(externalUri2, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);
        FolderData data = folder.getData();
        data.getComOpenexchangeCalendarConfig().setUri(externalUri2);
        FolderBody body = new FolderBody();
        body.setFolder(data);

        FolderUpdateResponse updateResponse = foldersApi.updateFolder(defaultUserApi.getSession(), newFolderId, System.currentTimeMillis(), body, false, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE, false);
        assertNotNull(updateResponse.getError());

        assertEquals("ICAL-PROV-4044", updateResponse.getCode());
    }

    @Test
    public void testCreateAccountWithDescriptionAndFeedNameProvidedByClient_returnProvidedValues() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testCreateAccountWithDescriptionAndFeedNameProvidedByClient_returnProvidedValues.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        String title = "The initial title";
        folder.setTitle(title);
        FolderDataComOpenexchangeCalendarExtendedProperties comOpenexchangeCalendarExtendedProperties = new FolderDataComOpenexchangeCalendarExtendedProperties();
        FolderDataComOpenexchangeCalendarExtendedPropertiesDescription description = new FolderDataComOpenexchangeCalendarExtendedPropertiesDescription();
        description.setValue("The nice description");
        comOpenexchangeCalendarExtendedProperties.setDescription(description);
        folder.setComOpenexchangeCalendarExtendedProperties(comOpenexchangeCalendarExtendedProperties);
        addPermissions(folder);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);
        String newFolderId = createAccount(body);

        FolderResponse folderResponse = foldersApi.getFolder(defaultUserApi.getSession(), newFolderId, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);

        assertEquals(description.getValue(), folderResponse.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
        assertEquals(title, folderResponse.getData().getTitle());
    }

    @Test
    public void testParallelGet_onlyAddOnce() throws OXException, IOException, JSONException, ApiException, InterruptedException {
        String externalUri = "http://example.com/files/testParallelGet_onlyAddOneResultSet.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK, null, 2);

        String newFolderId = createDefaultAccount(externalUri);

        ExecutorService executor = Executors.newWorkStealingPool();

        Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        List<Callable<Void>> callables = Arrays.asList(callable, callable, callable);
        executor.invokeAll(callables).stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());
    }

    private String createDefaultAccount(String externalUri) throws ApiException {
        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        return createAccount(body);
    }

    private NewFolderBodyFolder createFolder(String externalUri, FolderDataComOpenexchangeCalendarConfig config) {
        config.setEnabled(Boolean.TRUE);
        config.setUri(externalUri);
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule("event");
        folder.setComOpenexchangeCalendarConfig(config);
        folder.setSubscribed(Boolean.TRUE);
        folder.setTitle("testFolder_" + System.nanoTime());
        folder.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);
        folder.setComOpenexchangeCalendarExtendedProperties(new FolderDataComOpenexchangeCalendarExtendedProperties());
        return folder;
    }

    private void addPermissions(NewFolderBodyFolder folder) {
        FolderPermission perm = new FolderPermission();
        perm.setEntity(defaultUserApi.getCalUser());
        perm.setGroup(Boolean.FALSE);
        perm.setBits(403710016);

        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);

        folder.setPermissions(permissions);
    }
}
