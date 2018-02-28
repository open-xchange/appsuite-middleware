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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.chronos.manager.CalendarFolderManager;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeData;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.CalendarAccountProbeResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarConfig;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedProperties;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedPropertiesColor;
import com.openexchange.testing.httpclient.models.FolderDataComOpenexchangeCalendarExtendedPropertiesDescription;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * {@link BasicICalCalendarProviderTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class BasicICalCalendarProviderTest extends AbstractExternalProviderChronosTest {

    public BasicICalCalendarProviderTest() {
        super(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);
    }

    private String createAccount(NewFolderBody body) throws ApiException {
        return this.folderManager.createFolder(body);
    }

    private long dateToMillis(String date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ");
        return formatter.parseDateTime(date).getMillis();
    }

    @Test
    public void testProbe_noDescriptionAndFeedNameProvidedAndNotInICS_returnDefault() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testProbe_noDescriptionAndFeedNameProvidedAndNotInICS_returnDefault.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        CalendarAccountProbeData data = new CalendarAccountProbeData();
        data.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);

        CalendarAccountProbeDataComOpenexchangeCalendarConfig config = new CalendarAccountProbeDataComOpenexchangeCalendarConfig();
        config.setUri(externalUri);
        data.setComOpenexchangeCalendarConfig(config);
        data.setComOpenexchangeCalendarExtendedProperties(new FolderDataComOpenexchangeCalendarExtendedProperties());

        CalendarAccountProbeResponse probe = defaultUserApi.getChronosApi().probe(defaultUserApi.getSession(), data);

        assertNull(probe.getData().getComOpenexchangeCalendarExtendedProperties().getDescription());
        assertEquals("testProbe_noDescriptionAndFeedNameProvidedAndNotInICS_returnDefault", probe.getData().getTitle());
    }

    @Test
    public void testProbe_noDescriptionAndFeedNameProvided_returnFromFeed() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testProbeWithNoDescriptionAndFeedName_returnFromFeed.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.RESPONSE_WITH_ADDITIONAL_PROPERTIES, HttpStatus.SC_OK);

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
    public void testProbe_descriptionAndFeedNameProvidedByClient_returnProvidedValues() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testProbeWithDescriptionAndFeedNameProvidedByClient_returnProvidedValues.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

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
    public void testProbe_feedSizeTooBig_returnException() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testProbe_feedSizeTooBig_returnException.ics";
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
        assertEquals("ICAL-PROV-4001", probe.getCode());
    }

    @Test
    public void testProbe_notFound_returnException() throws Exception {
        String externalUri = "http://example.com/files/testProbe_notFound_returnException.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

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
    public void testProbe_Unauthorized_returnException() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/testProbe_Unauthorized_returnException.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_UNAUTHORIZED);

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
    public void testProbe_deniedHost_returnException() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://localhost/files/testProbe_deniedHost_returnException.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

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
        String externalUri = "http://localhost/files/testProbe_badURI_notFound.ics";

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
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_NOT_FOUND);

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
    public void testCreate_calendarAccountCreatedAndEventsRetrieved() throws JSONException, ApiException, OXException, IOException {
        String externalUri = "http://example.com/files/testEventsCreated.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);
        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);

        assertEquals(38, allEvents.size());
    }

    @Test
    public void testCreate_descriptionNotSet_useProvidedOptions() throws JSONException, OXException, IOException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/testFolderConfiguredCorrectly_nameAndDescriptionNotAvailable.ics";
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
        String externalUri = "http://example.com/files/testFolderConfiguredCorrectlyIfNameAndDescriptionIsInIcsAndEventsRetrieved.ics";
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
        String externalUri = "http://example.com/files/testFolderConfiguredCorrectlyIfNameAndDescriptionIsInIcsAndEventsRetrieved.ics";
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
        String externalUri = "http://example.com/files/testCreateAccountWithDescriptionAndFeedNameProvidedByClient_returnProvidedValues.ics";
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
        String externalUri = "http://example.com/files/testFolderUpdate.ics";
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

        FolderBody body = new FolderBody();
        body.setFolder(folderData);

        FolderUpdateResponse updateResponse = folderManager.updateFolder(folderData);
        assertNull(updateResponse.getError());

        FolderData folderReload = folderManager.getFolder(newFolderId);
        assertEquals(changedTitle, folderReload.getTitle());
        assertEquals("blue", folderReload.getComOpenexchangeCalendarExtendedProperties().getColor().getValue());
        assertNull(folderReload.getComOpenexchangeCalendarExtendedProperties().getDescription().getValue());
    }

    @Test
    public void testFolderUpdateWithGettingEvents() throws OXException, IOException, JSONException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/testFolderUpdateWithGettingEvents.ics";
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
        String externalUri = "http://example.com/files/testCalendarAccountUpdate_updateShouldContainAllFieldsAndEventsOnce.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        FolderData folderData = folderManager.getFolder(newFolderId);
        folderData.getComOpenexchangeCalendarConfig().setRefreshInterval("100000");
        FolderDataComOpenexchangeCalendarExtendedPropertiesColor folderDataComOpenexchangeCalendarExtendedPropertiesColor = new FolderDataComOpenexchangeCalendarExtendedPropertiesColor();
        folderDataComOpenexchangeCalendarExtendedPropertiesColor.setValue("blue");
        folderData.getComOpenexchangeCalendarExtendedProperties().setColor(folderDataComOpenexchangeCalendarExtendedPropertiesColor);

        FolderUpdateResponse updateResponse = folderManager.updateFolder(folderData);
        assertNull(updateResponse.getError());

        List<EventData> allEventsAfterUpdate = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, folderId);
        assertEquals(38, allEventsAfterUpdate.size());

        FolderData folderReload = folderManager.getFolder(newFolderId);
        assertEquals("blue", folderReload.getComOpenexchangeCalendarExtendedProperties().getColor().getValue()); // should only pass after removing calendar account api
    }

    @Test
    public void testUpdateCalendarAccountURI_notAllowed_returnException() throws JSONException, OXException, IOException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/testUpdateCalendarAccountURI_notAllowed_returnException.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_OK);

        String newFolderId = createDefaultAccount(externalUri);

        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);
        assertEquals(38, allEvents.size());

        String externalUri2 = "http://example.com/files/testCalendarAccountUpdateWithUpdatedURI_invalidateCache.ics";
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
        String externalUri = "http://example.com/files/testParallelGet_onlyAddOneResultSet.ics";
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
        String externalUri = "http://example.com/files/testGetSingleEvent.ics";
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
    public void testGetSingleRecurrence_butNotAvailalbe() throws OXException, IOException, JSONException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/testGetSingleRecurrence_butNotAvailalbe.ics";
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
        String externalUri = "http://example.com/files/testGetEvents_filteredByTimeRange.ics";
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
        String externalUri = "http://example.com/files/testListEvents.ics";
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
    public void testGetChangeException() throws OXException, IOException, JSONException, ApiException, ChronosApiException {
        String externalUri = "http://example.com/files/testGetChangeException.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.FEED_WITH_CHANGE_EXCEPTION, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        String columns = "recurrenceId, seriesId, id, summary";
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)), true, newFolderId, null, columns);
        for (Iterator<EventData> iterator = allEvents.iterator(); iterator.hasNext();) {
            EventData eventData = iterator.next();
            if (!eventData.getSummary().contains("Test-Series")) {
                iterator.remove();
            }
        }
        assertEquals(4, allEvents.size());

        EventData master = null;
        for (EventData event : allEvents) {
            if (EventManager.isSeriesMaster(event)) {
                master = event;
                break;
            }
        }
        assertNotNull(master);

        String seriesId = master.getId();
        EventData recurrence = allEvents.get(2);
        assertFalse(EventManager.isSeriesMaster(recurrence));

        EventData reloadedRecurringEvent = eventManager.getRecurringEvent(seriesId, recurrence.getRecurrenceId(), false);

        assertEquals(seriesId, reloadedRecurringEvent.getId());
        assertEquals(recurrence.getRecurrenceId(), reloadedRecurringEvent.getRecurrenceId());
        assertEquals(recurrence.getStartDate().getValue(), reloadedRecurringEvent.getStartDate().getValue());
        assertEquals(recurrence.getEndDate().getValue(), reloadedRecurringEvent.getEndDate().getValue());
    }

    @Test
    public void testGetSeriesMaster() throws OXException, IOException, JSONException, ApiException {
        String externalUri = "http://example.com/files/testGetSeriesMaster.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.FEED_WITH_CHANGE_EXCEPTION, HttpStatus.SC_OK);

        FolderDataComOpenexchangeCalendarConfig config = new FolderDataComOpenexchangeCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);
        folder.setTitle(null);
        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        String newFolderId = createAccount(body);

        //get events to fill table
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20180123T123000Z")), new Date(dateToMillis("20180123T123000Z")), false, newFolderId);
        assertEquals(1, allEvents.size());
        assertTrue(EventManager.isSeriesMaster(allEvents.get(0)));
    }
    
    @Test
    public void testGet_Unauthorized_returnException() throws ApiException, OXException, IOException, JSONException {
        String externalUri = "http://example.com/files/testGet_Unauthorized_returnException.ics";
        mock(externalUri, BasicICalCalendarProviderTestConstants.GENERIC_RESPONSE, HttpStatus.SC_UNAUTHORIZED);

        String newFolderId = createDefaultAccount(externalUri);
//        EventsResponse eventsResponse = userApi.getChronosApi().getAllEvents(userApi.getSession(), DateTimeUtil.getZuluDateTime(from.getTime()).getValue(), DateTimeUtil.getZuluDateTime(until.getTime()).getValue(), folder, fields, order, sort, expand, true, false);
        List<EventData> allEvents = eventManager.getAllEvents(new Date(dateToMillis("20000702T201500Z")), new Date(System.currentTimeMillis()), false, newFolderId);

        System.out.println();
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
