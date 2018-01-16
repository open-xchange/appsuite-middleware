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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.proxy.MockRequest;
import com.openexchange.ajax.proxy.StartMockServerRequest;
import com.openexchange.chronos.provider.ical.ICalCalendarConstants;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeData;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;

/**
 * {@link ICalCalendarProviderTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalCalendarProviderTest extends AbstractChronosTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client.execute(new StartMockServerRequest());
    }

    private void mock(String uri, String responseContent, int httpStatus) throws OXException, IOException, JSONException {
        mock(uri, responseContent, httpStatus, Collections.emptyMap());
    }

    private void mock(String uri, String responseContent, int httpStatus, Map<String, String> responseHeaders) throws OXException, IOException, JSONException {
        mock(uri, responseContent, httpStatus, responseHeaders, 0);
    }

    private void mock(String uri, String responseContent, int httpStatus, Map<String, String> responseHeaders, int delay) throws OXException, IOException, JSONException {
        InputStream stream = new ByteArrayInputStream(responseContent.getBytes(StandardCharsets.UTF_8.name()));

        MockRequest mockRequest = new MockRequest(uri, stream, httpStatus, responseHeaders, delay);
        client.execute(mockRequest);
    }

    //FIXME better object
    private String createAccount(JSONObject config) throws ChronosApiException, JsonParseException, JsonMappingException, ApiException, IOException, JSONException {
        JSONObject configuration = new JSONObject();
        configuration.put("configuration", config);
        FolderUpdateResponse calendarAccount = calendarAccountManager.createCalendarAccount(ICalCalendarConstants.PROVIDER_ID, "testFolder_" + System.nanoTime(), configuration, false);
        return calendarAccount.getData();
    }

    private String createFolderId(String accountId) {
        return "cal://" + accountId + "/0";
    }

    private long dateToMillis(String date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ");
        return formatter.parseDateTime(date).getMillis();
    }

    @Test
    public void testProbe_uriMissing_notFound() throws Exception {
        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider("ical");

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals("ICAL-PROV-4040", probe.getCode());
    }

    @Test
    public void testProbe_badURI_notFound() throws Exception {
        String externalUri = "http://localhost/files/notFound.ics";

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider("ical");

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
        data.setComOpenexchangeCalendarProvider("ical");

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
        data.setComOpenexchangeCalendarProvider("ical");

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNull(probe.getError());
        assertEquals("myCalendarFile", probe.getData().getTitle());
        assertEquals("ical", probe.getData().getComOpenexchangeCalendarProvider());
        assertNull(probe.getData().getComOpenexchangeCalendarConfig().getRefreshInterval());
    }

    @Test
    public void testProbe_everythingFineWithAdditionalProperties_returnProps() throws Exception {
        String externalUri = "http://example.com/files/myCalendarFile2.ics";
        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider("ical");

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNull(probe.getError());
        assertEquals("FC Schalke 04", probe.getData().getTitle());
        assertEquals("ical", probe.getData().getComOpenexchangeCalendarProvider());
        assertEquals("10080", probe.getData().getComOpenexchangeCalendarConfig().getRefreshInterval());
        assertEquals("Alle Spiele von FC Schalke 04", probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    //    @Test
    //    public void testEventsCreated() throws JSONException, ApiException, OXException, IOException, ChronosApiException {
    //        String externalUri = "http://example.com/files/testEventsCreated.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        String folderId = createFolderId(createAccount(ical));
    //
    //        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //        assertEquals(38, allEvents.size());
    //    }
    //
    //    @Test
    //    public void testFolderConfiguredCorrectly_nameAndDescriptionNotAvailable() throws JSONException, OXException, IOException, ApiException {
    //        String externalUri = "http://example.com/files/testFolderConfiguredCorrectly_nameAndDescriptionNotAvailable.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        String folderId = createFolderId(createAccount(ical));
    //
    //        //get events to fill events
    //        eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //
    //        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), folderId, "0", null);
    //        String folderName = folder.getData().getTitle();
    //
    //        assertEquals(externalUri, folderName);
    //        assertNull(folder.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    //    }
    //
    //    @Test
    //    public void testFolderConfiguredCorrectlyIfNameAndDescriptionIsInIcsAndEventsRetrieved() throws JSONException, OXException, IOException, ApiException {
    //        String externalUri = "http://example.com/files/testFolderConfiguredCorrectlyIfNameAndDescriptionIsInIcsAndEventsRetrieved.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_NAME_AND_DESC, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        String folderId = createFolderId(createAccount(ical));
    //
    //        //get events to fill table
    //        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //        assertEquals(102, allEvents.size());
    //
    //        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), folderId, "0", null);
    //        String folderName = folder.getData().getTitle();
    //
    //        assertEquals("FC Schalke 04", folderName);
    //        assertEquals("Alle Spiele von FC Schalke 04", folder.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    //    }
    //
    //    @Test
    //    public void testFolderConfiguredCorrectlyIfNameAndDescriptionIsInIcsWithoutRetrievingEvents() throws JSONException, OXException, IOException, ApiException {
    //        String externalUri = "http://example.com/files/testFolderConfiguredCorrectlyIfNameAndDescriptionIsInIcsWithoutRetrievingEvents.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_NAME_AND_DESC, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        String folderId = createFolderId(createAccount(ical));
    //
    //        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), folderId, "0", null);
    //        String folderName = folder.getData().getTitle();
    //
    //        assertEquals("FC Schalke 04", folderName);
    //        assertEquals("Alle Spiele von FC Schalke 04", folder.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    //    }
    //
    //    @Test
    //    public void testFolderUpdate() throws OXException, IOException, JSONException, ApiException {
    //        String externalUri = "http://example.com/files/testFolderUpdate.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_NAME_AND_DESC, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        String folderId = createFolderId(createAccount(ical));
    //
    //        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), folderId, "0", null);
    //
    //        FolderData folderData = folder.getData();
    //        folderData.setTitle("changed");
    //        FolderDataComOpenexchangeCalendarExtendedProperties extendedProperties = new FolderDataComOpenexchangeCalendarExtendedProperties();
    //        FolderDataComOpenexchangeCalendarExtendedPropertiesColor color = new FolderDataComOpenexchangeCalendarExtendedPropertiesColor();
    //        color.setValue("blue");
    //        extendedProperties.setColor(color);
    //        folderData.setComOpenexchangeCalendarExtendedProperties(extendedProperties);
    //
    //        FolderBody body = new FolderBody();
    //        body.setFolder(folderData);
    //
    //        foldersApi.updateFolder(defaultUserApi.getSession(), folderId, System.currentTimeMillis(), body, false, "0", null, false);
    //        FolderResponse folderReload = foldersApi.getFolder(defaultUserApi.getSession(), folderId, "0", null);
    //        assertEquals("changed", folderReload.getData().getTitle());
    //        assertEquals("Alle Spiele von FC Schalke 04", folderReload.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    //        assertEquals("blue", folderReload.getData().getComOpenexchangeCalendarExtendedProperties().getColor().getValue());
    //    }
    //
    //    @Test
    //    public void testFolderUpdateWithGettingEvents() throws OXException, IOException, JSONException, ApiException {
    //        String externalUri = "http://example.com/files/testFolderUpdateWithGettingEvents.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.RESPONSE_WITH_NAME_AND_DESC, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        String folderId = createFolderId(createAccount(ical));
    //
    //        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //
    //        FolderResponse folder = foldersApi.getFolder(defaultUserApi.getSession(), folderId, "0", null);
    //
    //        FolderData folderData = folder.getData();
    //        folderData.setTitle("changed");
    //        FolderDataComOpenexchangeCalendarExtendedProperties extendedProperties = new FolderDataComOpenexchangeCalendarExtendedProperties();
    //        FolderDataComOpenexchangeCalendarExtendedPropertiesColor color = new FolderDataComOpenexchangeCalendarExtendedPropertiesColor();
    //        color.setValue("blue");
    //        extendedProperties.setColor(color);
    //        FolderDataComOpenexchangeCalendarExtendedPropertiesDescription description = new FolderDataComOpenexchangeCalendarExtendedPropertiesDescription();
    //        description.setValue("Keine Lust auf description");
    //        extendedProperties.setDescription(description);
    //        folderData.setComOpenexchangeCalendarExtendedProperties(extendedProperties);
    //        FolderBody body = new FolderBody();
    //        body.setFolder(folderData);
    //
    //        foldersApi.updateFolder(defaultUserApi.getSession(), folderId, System.currentTimeMillis(), body, false, "0", null, false);
    //        FolderResponse folderReload = foldersApi.getFolder(defaultUserApi.getSession(), folderId, "0", null);
    //        assertEquals("changed", folderReload.getData().getTitle());
    //        assertEquals("Keine Lust auf description", folderReload.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    //
    //        List<EventData> allEventsReloaded = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //        assertEquals(allEvents.size(), allEventsReloaded.size());
    //    }
    //
    //    @Test
    //    public void testCalendarAccountUpdate_updateShouldContainAllFieldsAndEventsOnce() throws JSONException, OXException, IOException, ApiException {
    //        String externalUri = "http://example.com/files/testCalendarAccountUpdateWithUpdatedURI.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        String accountId = createAccount(ical);
    //
    //        String folderId = createFolderId(accountId);
    //        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //        assertEquals(38, allEvents.size());
    //
    //        JSONObject newConfiguration = new JSONObject();
    //        ical.put("refreshInterval", "100000");
    //        ical.put("color", "blue");
    //        newConfiguration.put("configuration", ical);
    //
    //        calendarAccountManager.updateCalendarAccount(accountId, System.currentTimeMillis(), newConfiguration.toString());
    //        List<EventData> allEventsAfterUriChange = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //        assertEquals(38, allEventsAfterUriChange.size());
    //
    //        FolderResponse folderReload = foldersApi.getFolder(defaultUserApi.getSession(), folderId, "0", null);
    //        assertEquals("blue", folderReload.getData().getComOpenexchangeCalendarExtendedProperties().getColor().getValue()); // should only pass after removing calendar account api
    //    }
    //
    //    @Test
    //    public void testCalendarAccountUriUpdateNotAllowed() throws JSONException, OXException, IOException, ApiException {
    //        String externalUri = "http://example.com/files/testCalendarAccountUpdateWithUpdatedURI.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        String accountId = createAccount(ical);
    //
    //        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, createFolderId(accountId));
    //        assertEquals(38, allEvents.size());
    //
    //        String externalUri2 = "http://example.com/files/testCalendarAccountUpdateWithUpdatedURI_invalidateCache.ics";
    //        mock(externalUri2, ICalCalendarProviderTestConstants.RESPONSE_WITH_NAME_AND_DESC, HttpStatus.SC_OK);
    //        JSONObject newConfiguration = new JSONObject();
    //        JSONObject newIcal = new JSONObject();
    //        newIcal.put("uri", externalUri2);
    //        newConfiguration.put("configuration", newIcal);
    //
    //        CalendarAccountResponse response = defaultUserApi.getChronosApi().updateAccount(defaultUserApi.getSession(), accountId, System.currentTimeMillis(), newConfiguration.toString());
    //        assertNotNull(response.getError());
    //        assertEquals("ICAL-PROV-4044", response.getCode());
    //    }
    //
    //    @Test
    //    public void testUnauthorized() throws OXException, IOException, JSONException, ApiException {
    //        String externalUri = "http://example.com/files/testUnauthorized.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_UNAUTHORIZED);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        JSONObject configuration = new JSONObject();
    //        configuration.put("configuration", ical);
    //
    //        CalendarAccountResponse response = defaultUserApi.getChronosApi().createAccount(defaultUserApi.getSession(), ICalCalendarConstants.PROVIDER_ID, configuration.toString());
    //
    //        assertNotNull(response.getError());
    //        assertEquals("CAL-4010", response.getCode());
    //    }
    //
    //    @Test
    //    public void testDeniedHost() throws OXException, IOException, JSONException, ApiException {
    //        String externalUri = "http://localhost/files/testDeniedHost.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        JSONObject configuration = new JSONObject();
    //        configuration.put("configuration", ical);
    //
    //        CalendarAccountResponse response = defaultUserApi.getChronosApi().createAccount(defaultUserApi.getSession(), ICalCalendarConstants.PROVIDER_ID, configuration.toString());
    //
    //        assertNotNull(response.getError());
    //        assertEquals("ICAL-PROV-4042", response.getCode());
    //    }
    //
    //    @Test
    //    public void testNotFound() throws Exception {
    //        String externalUri = "http://example.com/files/notFound.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        JSONObject configuration = new JSONObject();
    //        configuration.put("configuration", ical);
    //
    //        NewFolderBody body = new NewFolderBody();
    //        NewFolderBodyFolder folder = new NewFolderBodyFolder();
    //        folder.title("test it baby");
    //        folder.setSubscribed(Boolean.TRUE);
    //        folder.setModule("event");
    //        folder.setComOpenexchangeCalendarProvider("ical");
    //        
    //        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
    //        config.setUri(externalUri);
    //        folder.setComOpenexchangeCalendarConfig(config);
    //        body.setFolder(folder);
    //        FolderUpdateResponse createFolder = foldersApi.createFolder(this.getDefaultFolder(), defaultUserApi.getSession(), body, "0", "calendar");
    //        
    ////        CalendarAccountResponse response = defaultUserApi.getChronosApi().createAccount(defaultUserApi.getSession(), ICalCalendarConstants.PROVIDER_ID, configuration.toString());
    //
    //        assertNotNull(createFolder.getError());
    //        assertEquals("ICAL-PROV-4043", createFolder.getCode());
    //    }
    //
    //    @Test
    //    public void testTooBig() throws OXException, IOException, JSONException, ApiException {
    //        String externalUri = "http://example.com/files/tooBig.ics";
    //        Map<String, String> responseHeaders = new HashMap<>();
    //        responseHeaders.put(HttpHeaders.CONTENT_LENGTH, "100000000");
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK, responseHeaders);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        JSONObject configuration = new JSONObject();
    //        configuration.put("configuration", ical);
    //
    //        CalendarAccountResponse response = defaultUserApi.getChronosApi().createAccount(defaultUserApi.getSession(), ICalCalendarConstants.PROVIDER_ID, configuration.toString());
    //
    //        assertNotNull(response.getError());
    //        assertEquals("ICAL-PROV-4001", response.getCode());
    //    }
    //
    //    @Test
    //    public void testParallelGet_onlyAddOneResultSet() throws OXException, IOException, JSONException, ApiException, InterruptedException {
    //        String externalUri = "http://example.com/files/testParallelGet_onlyAddOneResultSet.ics";
    //        mock(externalUri, ICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK, null, 2);
    //
    //        JSONObject ical = new JSONObject();
    //        ical.put("uri", externalUri);
    //        JSONObject configuration = new JSONObject();
    //        configuration.put("configuration", ical);
    //
    //        String folderId = createFolderId(createAccount(ical));
    //
    //        ExecutorService executor = Executors.newWorkStealingPool();
    //
    //        Callable<Void> callable = new Callable<Void>() {
    //
    //            @Override
    //            public Void call() throws Exception {
    //                try {
    //                    eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //                } catch (ApiException e) {
    //                    // TODO Auto-generated catch block
    //                    e.printStackTrace();
    //                }
    //                return null;
    //            }
    //        };
    //        List<Callable<Void>> callables = Arrays.asList(callable, callable, callable);
    //        executor.invokeAll(callables).stream().map(future -> {
    //            try {
    //                return future.get();
    //            } catch (Exception e) {
    //                throw new IllegalStateException(e);
    //            }
    //        });
    //
    //        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
    //        assertEquals(38, allEvents.size());
    //    }
    //
    //    @Test
    //    public void testParallelGetForMultipleUsers_onlyAddOneResultSet() throws OXException, IOException, JSONException, ApiException, InterruptedException {
    //        
    //    }

}
