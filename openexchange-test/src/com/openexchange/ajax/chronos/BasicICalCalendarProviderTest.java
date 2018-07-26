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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import com.openexchange.ajax.chronos.manager.CalendarFolderManager;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeData;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeDataComOpenexchangeCalendarExtendedProperties;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeResponse;
import com.openexchange.testing.httpclient.models.ChronosFolderBody;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventsResponse;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedProperties;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedPropertiesColor;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedPropertiesDescription;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.MultipleEventDataError;
import com.openexchange.testing.httpclient.models.MultipleFolderEventsResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;

/**
 * {@link BasicICalCalendarProviderTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
@Ignore
public class BasicICalCalendarProviderTest extends AbstractExternalProviderChronosTest {

    public BasicICalCalendarProviderTest() {
        super(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);
    }

    private String createAccount(NewFolderBody body) throws ApiException {
        return this.folderManager.createFolder(body);
    }

    long dateToMillis(String date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ");
        return formatter.parseDateTime(date).getMillis();
    }

    @Test
    public void testProbe_containsSurrogateChars_returnException() throws ApiException {
        String externalUri = "http://example.com/files/test.\ud83d\udca9";

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        data.setComOpenexchangeCalendarExtendedProperties(new CalendarAccountProbeDataComOpenexchangeCalendarExtendedProperties());

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4041", probe.getCode());
    }

    @Test
    public void testCreate_containsSurrogateChars_returnException() throws ApiException {
        String externalUri = "http://example.com/files/test.\ud83d\udca9";

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);
        FolderUpdateResponse response = foldersApi.createFolder(CalendarFolderManager.DEFAULT_FOLDER_ID, defaultUserApi.getSession(), body, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);

        assertNotNull(response.getError());
        assertEquals(response.getError(), "ICAL-PROV-4041", response.getCode());
    }

    @Test
    public void testProbe_noScheme_returnException() throws ApiException {
        String externalUri = "example.com/files/test.ics";

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        data.setComOpenexchangeCalendarExtendedProperties(new CalendarAccountProbeDataComOpenexchangeCalendarExtendedProperties());

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4041", probe.getCode());
    }

    @Test
    public void testCreate_noScheme_returnException() throws ApiException {
        String externalUri = "example.com/files/test.ics";

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);
        FolderUpdateResponse response = foldersApi.createFolder(CalendarFolderManager.DEFAULT_FOLDER_ID, defaultUserApi.getSession(), body, CalendarFolderManager.TREE_ID, CalendarFolderManager.MODULE);

        assertNotNull(response.getError());
        assertEquals(response.getError(), "ICAL-PROV-4041", response.getCode());
    }

    @Test
    public void testProbe_noDescriptionAndFeedNameProvidedAndNotInICS_returnDefault() throws OXException, IOException, JSONException, ApiException {
        String uuid = UUID.randomUUID().toString();
        String externalUri = "http://example.com/files/" + uuid + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        data.setComOpenexchangeCalendarExtendedProperties(new CalendarAccountProbeDataComOpenexchangeCalendarExtendedProperties());

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNull(probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription());
        assertEquals(uuid, probe.getData().getTitle());
    }

    @Test
    public void testProbe_noDescriptionAndFeedNameProvided_returnFromFeed() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        data.setComOpenexchangeCalendarExtendedProperties(new CalendarAccountProbeDataComOpenexchangeCalendarExtendedProperties());

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertEquals("Alle Spiele von FC Schalke 04", probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
        assertEquals("FC Schalke 04", probe.getData().getTitle());
    }

    @Test
    public void testProbe_descriptionAndFeedNameProvidedByClient_returnProvidedValues() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        String title = "The awesome custom name!";
        data.setTitle(title);
        CalendarAccountProbeDataComOpenexchangeCalendarExtendedProperties comOpenexchangeCalendarExtendedProperties = new CalendarAccountProbeDataComOpenexchangeCalendarExtendedProperties();
        FolderDataComOpenexchangeCalendarExtendedPropertiesDescription description = new FolderDataComOpenexchangeCalendarExtendedPropertiesDescription();
        description.setValue("My custom description");
        comOpenexchangeCalendarExtendedProperties.setDescription(description);
        data.setComOpenexchangeCalendarExtendedProperties(comOpenexchangeCalendarExtendedProperties);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertEquals(description.getValue(), probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
        assertEquals(title, probe.getData().getTitle());
    }

    @Test
    public void testProbe_feedSizeTooBig_returnException() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(HttpHeaders.CONTENT_LENGTH, "100000000");
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK, responseHeaders);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4001", probe.getCode());
    }

    @Test
    public void testProbe_notFound_returnException() throws Exception {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4043", probe.getCode());
    }

    @Test
    public void testProbe_Unauthorized_returnException() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_UNAUTHORIZED);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4010", probe.getCode());
    }

    @Test
    public void testProbe_deniedHost_returnException() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://localhost/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4042", probe.getCode());
    }

    @Test
    public void testProbe_uriMissing_notFound() throws Exception {
        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4040", probe.getCode());
    }

    @Test
    public void testProbe_badURI_notFound() throws Exception {
        String externalUri = "http://localhost/files/" + UUID.randomUUID().toString() + ".ics";

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4042", probe.getCode());
    }

    @Test
    public void testProbe_uriNotFound_notFound() throws Exception {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNotNull(probe.getError());
        assertEquals(probe.getError(), "ICAL-PROV-4043", probe.getCode());
    }

    @Test
    public void testCreate_calendarAccountCreatedAndEventsRetrieved() throws JSONException, ApiException, OXException, IOException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);
        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);

        assertEquals(38, allEvents.size());
    }

    @Test
    public void testCreate_descriptionNotSet_useProvidedOptions() throws JSONException, OXException, IOException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String lFolderId = createAccount(body);

        //get events to fill events
        eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, lFolderId);

        FolderData folderData = folderManager.getFolder(lFolderId);
        String folderName = folderData.getTitle();

        assertEquals(folder.getTitle(), folderName);
        assertNull(folderData.getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testCreate_descriptionNotSet_useProvidedOptionsEvenTheFeedContainsInfos() throws JSONException, OXException, IOException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(102, allEvents.size());

        FolderData folderData = folderManager.getFolder(newFolderId);

        assertNull(folderData.getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testCreate_titleNotSet_useDefault() throws JSONException, OXException, IOException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

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

        FolderData folderData = folderManager.getFolder(newFolderId);
        String folderName = folderData.getTitle();

        assertEquals("Calendar", folderName);
    }

    @Test
    public void testCreateAccountWithDescriptionAndFeedNameProvidedByClient_returnProvidedValues() throws OXException, IOException, JSONException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        String title = RandomStringUtils.randomNumeric(30);
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

        FolderData folderData = folderManager.getFolder(newFolderId);

        assertEquals(description.getValue(), folderData.getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
        assertEquals(title, folderData.getTitle());
    }

    @Test
    public void testFolderUpdate() throws OXException, IOException, JSONException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        FolderData folderData = folderManager.getFolder(newFolderId);

        String changedTitle = "changed";
        folderData.setTitle(changedTitle);
        FolderDataComOpenexchangeCalendarExtendedProperties extendedProperties = new FolderDataComOpenexchangeCalendarExtendedProperties();
        FolderDataComOpenexchangeCalendarExtendedPropertiesColor color = new FolderDataComOpenexchangeCalendarExtendedPropertiesColor();
        color.setValue("blue");
        extendedProperties.setColor(color);
        folderData.setComOpenexchangeCalendarExtendedProperties(extendedProperties);

        FolderUpdateResponse updateResponse = folderManager.updateFolder(folderData);
        assertNull(updateResponse.getError());

        FolderData folderReload = folderManager.getFolder(newFolderId);
        assertEquals(changedTitle, folderReload.getTitle());
        assertEquals("blue", folderReload.getComOpenexchangeCalendarExtendedProperties().getColor().getValue());
        assertNull(folderReload.getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testFolderUpdateWithGettingEvents() throws OXException, IOException, JSONException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);

        FolderData folderData = folderManager.getFolder(newFolderId);

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

        FolderUpdateResponse updateResponse = folderManager.updateFolder(folderData);
        assertNull(updateResponse.getError());

        FolderData folderReload = folderManager.getFolder(newFolderId);
        assertEquals("changed", folderReload.getTitle());
        assertEquals(updatedDescription, folderReload.getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());

        List<EventData> allEventsReloaded = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(allEvents.size(), allEventsReloaded.size());
    }

    @Test
    public void testCalendarAccountUpdate_updateShouldContainAllFieldsAndEventsOnce() throws JSONException, OXException, IOException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        FolderData folderData = folderManager.getFolder(newFolderId);
        folderData.getComOpenexchangeCalendarConfig().setRefreshInterval(100000);
        FolderDataComOpenexchangeCalendarExtendedPropertiesColor folderDataComOpenexchangeCalendarExtendedPropertiesColor = new FolderDataComOpenexchangeCalendarExtendedPropertiesColor();
        folderDataComOpenexchangeCalendarExtendedPropertiesColor.setValue("blue");
        folderData.getComOpenexchangeCalendarExtendedProperties().setColor(folderDataComOpenexchangeCalendarExtendedPropertiesColor);

        FolderUpdateResponse updateResponse = folderManager.updateFolder(folderData);
        assertNull(updateResponse.getError());

        List<EventData> allEventsAfterUpdate = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEventsAfterUpdate.size());

        FolderData folderReload = folderManager.getFolder(newFolderId);
        assertEquals("blue", folderReload.getComOpenexchangeCalendarExtendedProperties().getColor().getValue());
    }

    @Test
    public void testUpdateCalendarAccountURI_notAllowed_returnException() throws JSONException, OXException, IOException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        String externalUri2 = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri2, BasicICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

        FolderData folderData = folderManager.getFolder(newFolderId);
        folderData.getComOpenexchangeCalendarConfig().setUri(externalUri2);

        try {
            folderManager.updateFolder(folderData, true);
            fail();
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("ICAL-PROV-4044", e.getErrorCode());
        }
    }

    @Test
    public void testParallelGet_onlyAddOnce() throws OXException, IOException, JSONException, ApiException, InterruptedException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK, null, 2);

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
        List<Callable<Void>> callables = Arrays.asList(callable, callable, callable, callable, callable, callable);
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

    @Test
    public void testGetSingleEvent() throws OXException, IOException, JSONException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        int nextInt = ThreadLocalRandom.current().nextInt(0, 37);
        EventData eventData = allEvents.get(nextInt);

        EventData event = eventManager.getEvent(newFolderId, eventData.getId());

        assertNotNull(event);
        assertEquals(allEvents.get(nextInt).getId(), event.getId());
        assertEquals(allEvents.get(nextInt).getRecurrenceId(), event.getRecurrenceId());
        assertEquals(allEvents.get(nextInt).getStartDate().getValue(), event.getStartDate().getValue());
        assertEquals(allEvents.get(nextInt).getEndDate().getValue(), event.getEndDate().getValue());
    }

    @Test
    public void testGetSingleRecurrence_butNotAvailalbe() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        int nextInt = ThreadLocalRandom.current().nextInt(0, 37);
        EventData eventData = allEvents.get(nextInt);
        try {
            eventManager.getEvent(newFolderId, eventData.getId(), "20000702T201500Z", true);
            fail();
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("CAL-4042", e.getErrorCode());
        }
    }

    @Test
    public void testGetEvents_filteredByTimeRange() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20170701T201500Z")), new Date(dateToMillis("20170801T201500Z")), false, newFolderId);
        assertEquals(2, allEvents.size());
    }

    @Test
    public void testListEvents() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        List<EventId> ids = createEventIDs(allEvents, 5, newFolderId);

        List<EventData> listedEvents = eventManager.listEvents(ids);
        assertEquals(5, listedEvents.size());
    }

    @Test
    public void testGetExpandedSeries() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.FEED_WITH_SERIES_AND_CHANGE_EXCEPTION, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)), true, newFolderId, null, null);
        assertEquals(6, allEvents.size());

        for (Iterator<EventData> iterator = allEvents.iterator(); iterator.hasNext();) {
            EventData eventData = iterator.next();
            if (!eventData.getSummary().contains("Test-Series")) {
                iterator.remove();
            }
        }
        assertEquals(6, allEvents.size());

        EventData master = null;
        for (EventData event : allEvents) {
            if (EventManager.isSeriesMaster(event)) {
                master = event;
                break;
            }
        }
        assertNull(master);
    }

    @Test
    public void testGetNonExpandedSeries() throws OXException, IOException, JSONException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.FEED_WITH_SERIES_AND_CHANGE_EXCEPTION, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)), false, newFolderId, null, null);
        assertEquals(3, allEvents.size());

        for (Iterator<EventData> iterator = allEvents.iterator(); iterator.hasNext();) {
            EventData eventData = iterator.next();
            if (!eventData.getSummary().contains("Test-Series")) {
                iterator.remove();
            }
        }
        assertEquals(3, allEvents.size());

        EventData master = null;
        EventData recurrence = null;
        for (EventData event : allEvents) {
            if (EventManager.isSeriesMaster(event)) {
                master = event;
            } else {
                recurrence = event;
            }

        }
        assertNotNull(master);
        assertNotNull(recurrence);

        String seriesId = master.getSeriesId();
        assertEquals(seriesId, recurrence.getSeriesId());
        EventData reloadedRecurringEvent = eventManager.getRecurringEvent(newFolderId, recurrence.getId(), recurrence.getRecurrenceId(), false);

        assertEquals(recurrence.getRecurrenceId(), reloadedRecurringEvent.getRecurrenceId());
        assertEquals(recurrence.getStartDate().getValue(), reloadedRecurringEvent.getStartDate().getValue());
        assertEquals(recurrence.getEndDate().getValue(), reloadedRecurringEvent.getEndDate().getValue());
    }

    // =====================================================================================
    // ============================== dealing with exceptions ==============================
    // =====================================================================================
    private static final int RETRY_INTERVAL = 4;
    private static final Map<String, String> CONFIG = new HashMap<>();
    static {
        CONFIG.put("com.openexchange.calendar.ical.retryAfterErrorInterval", "" + RETRY_INTERVAL);
    }

    @Override
    protected String getScope() {
        return "user";
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getReloadables() {
        return "ICalCalendarProviderReloadable";
    }

    @Test
    public void testGet_forbiddenWhileReading_returnSameExceptionForSecondRequest() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);
        EventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, false);
        EventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, false);

        assertEquals(initialAllEventResponse.getError(), "ICAL-PROV-5001", initialAllEventResponse.getCode());
        assertEquals(initialAllEventResponse.getError(), secondEventResponse.getError());
        assertEquals(initialAllEventResponse.getData(), secondEventResponse.getData());
        assertEquals(initialAllEventResponse.getCode(), secondEventResponse.getCode());
        assertEquals(initialAllEventResponse.getErrorId(), secondEventResponse.getErrorId());
        assertEquals(initialAllEventResponse.getErrorDesc(), secondEventResponse.getErrorDesc());
        assertEquals(initialAllEventResponse.getCategory(), secondEventResponse.getCategory());
        assertEquals(initialAllEventResponse.getCategories(), secondEventResponse.getCategories());
    }

    @Test
    public void testGet_forbiddenButSecondRequestOk_removeExceptionFromResponse() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        //        clear(externalUri);
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);
        EventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, false);
        clear(externalUri);

        assertNotNull(initialAllEventResponse.getError());
        assertEquals(initialAllEventResponse.getError(), "ICAL-PROV-5001", initialAllEventResponse.getCode());
        assertNotNull(initialAllEventResponse.getData());
        assertNotNull(initialAllEventResponse.getCode());
        assertNotNull(initialAllEventResponse.getErrorId());
        assertNotNull(initialAllEventResponse.getErrorDesc());
        assertNotNull(initialAllEventResponse.getCategory());
        assertNotNull(initialAllEventResponse.getCategories());

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(1) + 5000);
        } catch (InterruptedException e) {
            //
        }
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        EventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, true);

        assertNull(secondEventResponse.getError());
        assertNull(secondEventResponse.getCode());
        assertNull(secondEventResponse.getErrorId());
        assertNull(secondEventResponse.getErrorDesc());
        assertNull(secondEventResponse.getCategory());
        assertNull(secondEventResponse.getCategories());
        assertEquals(38, secondEventResponse.getData().size());
    }

    @Test
    public void testGet_forbiddenSecondRequestInBanTime_returnException() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        //        clear(externalUri);
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);
        EventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, false);
        clear(externalUri);

        assertNotNull(initialAllEventResponse.getError());
        assertEquals(initialAllEventResponse.getError(), "ICAL-PROV-5001", initialAllEventResponse.getCode());
        assertNotNull(initialAllEventResponse.getData());
        assertNotNull(initialAllEventResponse.getCode());
        assertNotNull(initialAllEventResponse.getErrorId());
        assertNotNull(initialAllEventResponse.getErrorDesc());
        assertNotNull(initialAllEventResponse.getCategory());
        assertNotNull(initialAllEventResponse.getCategories());

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(RETRY_INTERVAL) + 1000);
        } catch (InterruptedException e) {
            //
        }
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        EventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, true);

        assertNotNull(secondEventResponse.getError());
        assertEquals("CAL-CACHE-4230", secondEventResponse.getCode());
    }

    @Test
    public void testGet_forbiddenAndSecondRequestNotFound_changeExceptionInResponse() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        //        clear(externalUri);
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);
        EventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, false);
        clear(externalUri);

        assertNotNull(initialAllEventResponse.getError());
        assertEquals(initialAllEventResponse.getError(), "ICAL-PROV-5001", initialAllEventResponse.getCode());
        assertNotNull(initialAllEventResponse.getData());
        assertNotNull(initialAllEventResponse.getCode());
        assertNotNull(initialAllEventResponse.getErrorId());
        assertNotNull(initialAllEventResponse.getErrorDesc());
        assertNotNull(initialAllEventResponse.getCategory());
        assertNotNull(initialAllEventResponse.getCategories());

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(1) + 5000);
        } catch (InterruptedException e) {
            //
        }
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

        EventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, true);

        assertEquals(secondEventResponse.getError(), "ICAL-PROV-4043", secondEventResponse.getCode());
        assertNotNull(secondEventResponse.getError());
        assertNotNull(secondEventResponse.getData());
        assertNotNull(secondEventResponse.getCode());
        assertNotNull(secondEventResponse.getErrorId());
        assertNotNull(secondEventResponse.getErrorDesc());
        assertNotNull(secondEventResponse.getCategory());
        assertNotNull(secondEventResponse.getCategories());
    }

    @Test
    public void testGet_forbiddenAndSecondRequestInBanTime_returnException() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        //        clear(externalUri);
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);
        EventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, false);
        clear(externalUri);

        assertNotNull(initialAllEventResponse.getError());
        assertNotNull(initialAllEventResponse.getData());
        assertNotNull(initialAllEventResponse.getCode());
        assertNotNull(initialAllEventResponse.getErrorId());
        assertNotNull(initialAllEventResponse.getErrorDesc());
        assertNotNull(initialAllEventResponse.getCategory());
        assertNotNull(initialAllEventResponse.getCategories());
        assertEquals(initialAllEventResponse.getError(), "ICAL-PROV-5001", initialAllEventResponse.getCode());

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(RETRY_INTERVAL) + 1000);
        } catch (InterruptedException e) {
            //
        }
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

        EventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), newFolderId, null, null, null, false, false, true);

        assertNotNull(secondEventResponse.getError());
        assertEquals(secondEventResponse.getError(), "CAL-CACHE-4230", secondEventResponse.getCode());
    }

    @Test
    public void testMultipleGet_forbidden_returnExceptionWhenReadFromDB() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);

        ChronosFolderBody body = new ChronosFolderBody();
        body.addFoldersItem(newFolderId);
        //load resource data
        MultipleFolderEventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEventsForMultipleFolders(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), body, null, null, null, false, false);
        // return from db
        MultipleFolderEventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEventsForMultipleFolders(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), body, null, null, null, false, false);

        MultipleEventDataError initialResponseError = initialAllEventResponse.getData().get(0).getError();
        MultipleEventDataError secondResponseError = secondEventResponse.getData().get(0).getError();
        assertEquals("ICAL-PROV-5001", initialResponseError.getCode().toString());
        assertEquals(initialResponseError.getCode(), secondResponseError.getCode());
        assertEquals(initialResponseError.getErrorId(), secondResponseError.getErrorId());
        assertEquals(initialResponseError.getErrorDesc(), secondResponseError.getErrorDesc());
        assertEquals(initialResponseError.getCategory(), secondResponseError.getCategory());
        assertEquals(initialResponseError.getCategories(), secondResponseError.getCategories());
        assertEquals(0, initialAllEventResponse.getData().get(0).getEvents().size());
        assertEquals(0, secondEventResponse.getData().get(0).getEvents().size());
    }

    @Test
    public void testMultipleGet_forbiddenButSecondRequestOk_removeExceptionFromResponse() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        //        clear(externalUri);
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);
        ChronosFolderBody body = new ChronosFolderBody();
        body.addFoldersItem(newFolderId);

        MultipleFolderEventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEventsForMultipleFolders(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), body, null, null, null, false, true);
        clear(externalUri);
        MultipleEventDataError initialResponseError = initialAllEventResponse.getData().get(0).getError();

        assertNotNull(initialResponseError.getError());
        assertEquals(initialResponseError.getError(), "ICAL-PROV-5001", initialResponseError.getCode());
        assertNotNull(initialResponseError.getCode());
        assertNotNull(initialResponseError.getErrorId());
        assertNotNull(initialResponseError.getErrorDesc());
        assertNotNull(initialResponseError.getCategory());
        assertNotNull(initialResponseError.getCategories());

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(1) + 5000);
        } catch (InterruptedException e) {
            //
        }
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        MultipleFolderEventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEventsForMultipleFolders(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), body, null, null, null, false, true);

        MultipleEventDataError secondResponseError = secondEventResponse.getData().get(0).getError();

        assertNull(secondResponseError);
        assertEquals(38, secondEventResponse.getData().get(0).getEvents().size());
    }

    @Test
    public void testMultipleGet_forbiddenAndSecondRequestNotFound_changeExceptionInResponse() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        //        clear(externalUri);
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);
        ChronosFolderBody body = new ChronosFolderBody();
        body.addFoldersItem(newFolderId);

        MultipleFolderEventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEventsForMultipleFolders(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), body, null, null, null, false, true);
        clear(externalUri);
        MultipleEventDataError initialResponseError = initialAllEventResponse.getData().get(0).getError();

        assertEquals(0, initialAllEventResponse.getData().get(0).getEvents().size());
        assertNotNull(initialResponseError.getError());
        assertEquals(initialResponseError.getError(), "ICAL-PROV-5001", initialResponseError.getCode());
        assertNotNull(initialResponseError.getCode());
        assertNotNull(initialResponseError.getErrorId());
        assertNotNull(initialResponseError.getErrorDesc());
        assertNotNull(initialResponseError.getCategory());
        assertNotNull(initialResponseError.getCategories());
        assertEquals(initialResponseError.getError(), "ICAL-PROV-5001", initialResponseError.getCode());

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(1) + 5000);
        } catch (InterruptedException e) {
            //
        }
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

        MultipleFolderEventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEventsForMultipleFolders(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), body, null, null, null, false, true);
        MultipleEventDataError secondResponseError = secondEventResponse.getData().get(0).getError();

        assertEquals(0, secondEventResponse.getData().get(0).getEvents().size());
        assertEquals(secondResponseError.getError(), "ICAL-PROV-4043", secondResponseError.getCode());
        assertNotNull(secondResponseError.getError());
        assertNotNull(secondResponseError.getCode());
        assertNotNull(secondResponseError.getErrorId());
        assertNotNull(secondResponseError.getErrorDesc());

        assertNotEquals(initialResponseError.getError(), secondResponseError.getError());
        assertNotEquals(initialResponseError.getCode(), secondResponseError.getCode());
        assertNotEquals(initialResponseError.getErrorId(), secondResponseError.getErrorId());
        assertNotEquals(initialResponseError.getErrorDesc(), secondResponseError.getErrorDesc());
    }

    @Test
    public void testMultipleGet_forbiddenAndSecondRequestInBanTime_returnException() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/" + UUID.randomUUID().toString() + ".ics";
        //        clear(externalUri);
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_FORBIDDEN);

        String newFolderId = createDefaultAccount(externalUri);
        ChronosFolderBody body = new ChronosFolderBody();
        body.addFoldersItem(newFolderId);

        MultipleFolderEventsResponse initialAllEventResponse = defaultUserApi.getChronosApi().getAllEventsForMultipleFolders(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), body, null, null, null, false, true);
        clear(externalUri);
        MultipleEventDataError initialResponseError = initialAllEventResponse.getData().get(0).getError();

        assertEquals(0, initialAllEventResponse.getData().get(0).getEvents().size());
        assertNotNull(initialResponseError.getError());
        assertNotNull(initialResponseError.getCode());
        assertNotNull(initialResponseError.getErrorId());
        assertNotNull(initialResponseError.getErrorDesc());
        assertNotNull(initialResponseError.getCategory());
        assertNotNull(initialResponseError.getCategories());
        assertEquals(initialResponseError.getError(), "ICAL-PROV-5001", initialResponseError.getCode());

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(RETRY_INTERVAL) + 1000);
        } catch (InterruptedException e) {
            //
        }
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

        MultipleFolderEventsResponse secondEventResponse = defaultUserApi.getChronosApi().getAllEventsForMultipleFolders(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(new Date(dateToMillis("20000702T201500Z")).getTime()).getValue(), DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(), body, null, null, null, false, true);
        MultipleEventDataError secondResponseError = secondEventResponse.getData().get(0).getError();

        assertEquals(0, secondEventResponse.getData().get(0).getEvents().size());
        assertNotNull(secondResponseError.getError());
        assertEquals(secondResponseError.getError(), "CAL-CACHE-4230", secondResponseError.getCode());
        assertNotNull(secondResponseError.getCode());
        assertNotNull(secondResponseError.getErrorId());
        assertNotNull(secondResponseError.getErrorDesc());

        assertNotEquals(initialResponseError.getError(), secondResponseError.getError());
        assertNotEquals(initialResponseError.getCode(), secondResponseError.getCode());
        assertNotEquals(initialResponseError.getErrorId(), secondResponseError.getErrorId());
        assertNotEquals(initialResponseError.getErrorDesc(), secondResponseError.getErrorDesc());
    }

    private List<EventId> createEventIDs(List<EventData> allEvents, int maxEvents, String folder) {
        List<EventId> ids = new ArrayList<>();
        for (int i = 0; i < maxEvents; i++) {
            EventData eventData = allEvents.get(i);
            EventId id = new EventId();
            id.setFolder(folder);
            id.setId(eventData.getId());
            id.setRecurrenceId(eventData.getRecurrenceId());
            ids.add(id);
        }
        return ids;
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
