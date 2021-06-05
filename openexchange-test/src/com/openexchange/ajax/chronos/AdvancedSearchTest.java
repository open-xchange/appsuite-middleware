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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.google.common.collect.ImmutableList;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.AssertUtil;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.java.util.TimeZones;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ContactData;
import com.openexchange.testing.httpclient.models.ContactListElement;
import com.openexchange.testing.httpclient.models.ContactResponse;
import com.openexchange.testing.httpclient.models.ContactUpdateResponse;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MultipleEventData;
import com.openexchange.testing.httpclient.models.MultipleFolderEventsResponse;
import com.openexchange.testing.httpclient.modules.ContactsApi;
import com.openexchange.time.TimeTools;

/**
 * {@link AdvancedSearchTest} - HTTP Tests for the 'advancedSearch' action of the 'chronos' module.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
@SuppressWarnings("synthetic-access")
public class AdvancedSearchTest extends AbstractChronosTest {

    private static final String FIELD = "field";
    private static final String FILTER = "filter";
    private static final String FOLDERS = "folders";
    private static ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        }
    };

    private Date start;
    private Date end;
    private String rangeStart;
    private String rangeEnd;
    private List<ContactListElement> contactsToCleanup;

    /**
     * Initializes a new {@link AdvancedSearchTest}.
     */
    public AdvancedSearchTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        start = new Date(System.currentTimeMillis() - 31536000000L);
        end = new Date(System.currentTimeMillis() + 31536000000L);
        rangeEnd = DATE_FORMATTER.get().format(end);
        rangeStart = DATE_FORMATTER.get().format(start);
    }

    @Override
    public void tearDown() throws Exception {
        if (null != contactsToCleanup && false == contactsToCleanup.isEmpty()) {
            try {
                new ContactsApi(getApiClient()).deleteContacts(L(CalendarUtils.DISTANT_FUTURE), contactsToCleanup);
            } catch (Exception e) {
                // ignore
            }
        }
        super.tearDown();
    }

    ////////////////////////////////////////////// TESTS ////////////////////////////////////////////////////////

    /**
     * Creates an event in a specific folder and searches via the {@link ChronosJsonFields#SUMMARY} field in specific folder.
     */
    @Test
    public void testSearchSummaryInFolder() throws Exception {
        createEventInFolder("testSearchSummaryInFolderShouldNotFindMe" + System.nanoTime(), folderId);

        String summary = "testSearchSummaryInFolder" + System.nanoTime();
        EventData expectedEvent = createEventInFolder(summary, folderId);

        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summary).inFolder(folderId));

        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEvent);
    }

    /**
     * Creates an event in a specific folder and searches via the {@link ChronosJsonFields#SUMMARY} field in specific folder
     * with wildcards.
     */
    @Test
    public void testSearchSummaryWildcardsInFolder() throws Exception {
        createEventInFolder("HoodiniTestSearchSummaryInFolderShouldNotFindMe" + System.nanoTime(), folderId);

        String summaryA = "testSearchSummaryInFolderA" + System.nanoTime();
        EventData expectedEventA = createEventInFolder(summaryA, folderId);
        String summaryB = "testSearchSummaryInFolderB" + System.nanoTime();
        EventData expectedEventB = createEventInFolder(summaryB, folderId);
        String summaryC = "testSearchSummaryInFolderC" + System.nanoTime();
        EventData expectedEventC = createEventInFolder(summaryC, folderId);

        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, "testSearchSummaryInFolde*").inFolder(folderId));

        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEventA, expectedEventB, expectedEventC);
    }

    /**
     * Creates an event in a specific folder and searches via the {@link ChronosJsonFields#SUMMARY} field
     * in different folders with wildcards.
     */
    @Test
    public void testSearchSummaryWildcardsInDifferentFolders() throws Exception {
        createEventInFolder("HoodiniTestSearchSummaryInFolderShouldNotFindMe" + System.nanoTime(), folderId);

        String summaryA = "testSearchSummaryInFolderA" + System.nanoTime();
        EventData expectedEventA = createEventInFolder(summaryA, folderId);
        String summaryB = "testSearchSummaryInFolderB" + System.nanoTime();
        EventData expectedEventB = createEventInFolder(summaryB, defaultFolderId);
        String summaryC = "testSearchSummaryInFolderC" + System.nanoTime();
        EventData expectedEventC = createEventInFolder(summaryC, folderId);

        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, "testSearchSummaryInFolde*").inFolder(folderId).inFolder(defaultFolderId));
        assertEvent(assertResultsForFolder(eventData, defaultFolderId).getEvents(), expectedEventB);
        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEventA, expectedEventC);
    }

    /**
     * Creates an event in a specific folder and searches via the {@link ChronosJsonFields#DESCRIPTION} field in specific folder.
     */
    @Test
    public void testSearchDescriptionInFolder() throws Exception {
        createEventInFolder("testSearchDescriptionInFolderShouldNotFindMe" + System.nanoTime(), folderId);

        String summary = "testSearchDescriptionInFolder" + System.nanoTime();
        String description = "Description: testSearchDescriptionInFolder" + System.nanoTime() + " " + UUID.randomUUID().toString();
        EventData expectedEvent = createEventInFolder(summary, description, folderId);

        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.DESCRIPTION, description).inFolder(folderId));

        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEvent);
    }

    /**
     * Creates an event in a specific folder and searches via the {@link ChronosJsonFields#SUMMARY} field in specific folder
     * while fetching only the specific fields.
     */
    @Test
    public void testSearchSummaryInFolderRequestSpecificFields() throws Exception {
        createEventInFolder("testSearchSummaryInFolderShouldNotFindMe" + System.nanoTime(), folderId);

        String summary = "testSearchSummaryInFolder" + System.nanoTime();
        EventData createdEvent = createEventInFolder(summary, folderId);

        String fields = "folder,summary,id,createdBy,startDate,endDate";
        EventData expectedEvent = eventManager.getEvent(folderId, createdEvent.getId(), fields);
        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summary).withReturnFields(fields).inFolder(folderId));
        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEvent);
    }

    /**
     * Creates an event in a specific folder and searches via the {@link ChronosJsonFields#SUMMARY} field in all folders
     */
    @Test
    public void testSearchSummaryAllFolders() throws Exception {
        createEventInFolder("testSearchSummaryAllFoldersShouldNotFindMe" + System.nanoTime(), folderId);

        String summary = "testSearchSummaryAllFolders" + System.nanoTime();
        EventData expectedEvent = createEventInFolder(summary, folderId);

        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summary));

        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEvent);
    }

    /**
     * Creates an event in a specific folder and searches via the
     * {@link ChronosJsonFields#SUMMARY} AND {@link ChronosJsonFields#CREATED_BY} field in specific folder
     */
    @Test
    public void testSearchSummaryAndCreatorInFolder() throws Exception {
        createEventInFolder("testSearchSummaryAndCreatorInFolderShouldNotFindMe" + System.nanoTime(), folderId);

        String summary = "testSearchSummaryAndCreatorInFolder" + System.nanoTime();
        EventData expectedEvent = createEventInFolder(summary, folderId);

        // First search only in parent folder and assert that the event is not present there 
        SearchPerformer searchPerformer = new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summary).withField(ChronosJsonFields.CREATED_BY, Integer.toString(getUserId())).inFolder(defaultFolderId);
        List<MultipleEventData> eventData = performSearch(searchPerformer);
        assertNoResultsForFolder(eventData, folderId);

        // Check in the proper folder
        searchPerformer.resetFolders().inFolder(folderId);
        eventData = performSearch(searchPerformer);

        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEvent);
    }

    /**
     * Creates an event in a specific folder and searches via the
     * {@link ChronosJsonFields#SUMMARY} AND {@link ChronosJsonFields#CREATED_BY} field in specific folder
     */
    @Test
    public void testSearchSummaryAndCreatorInAllFolders() throws Exception {
        createEventInFolder("testSearchSummaryAndCreatorInAllFoldersShouldNotFindMe" + System.nanoTime(), folderId);

        String summary = "testSearchSummaryAndCreatorInFolder" + System.nanoTime();
        EventData expectedEvent = createEventInFolder(summary, folderId);

        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summary).withField(ChronosJsonFields.CREATED_BY, Integer.toString(getUserId())));

        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEvent);
    }

    /**
     * Creates an event in a specific folder and searches via the summary AND createdBy field in specific folder
     */
    @Test
    public void testSearchSummaryAOrSummaryBInFolder() throws Exception {
        String summaryA = "testSearchSummaryAInFolder" + System.nanoTime();
        String summaryB = "testSearchSummaryBInFolder" + System.nanoTime();

        EventData createEventA = createEventInFolder(summaryA, folderId);
        EventData createEventB = createEventInFolder(summaryB, folderId);

        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summaryA).withField(ChronosJsonFields.SUMMARY, summaryB).withLogicalOperation(LogicalOperation.OR).inFolder(folderId));

        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), createEventA, createEventB);
    }

    /**
     * Creates an event in different folders and searches via the
     * {@link ChronosJsonFields#SUMMARY} AND {@link ChronosJsonFields#CREATED_BY} in specific folder
     */
    @Test
    public void testSearchSummaryAOrSummaryBInDifferentFolders() throws Exception {
        String summaryA = "testSearchSummaryAInFolder" + System.nanoTime();
        String summaryB = "testSearchSummaryBInFolder" + System.nanoTime();

        EventData createEventA = createEventInFolder(summaryA, defaultFolderId);
        EventData createEventB = createEventInFolder(summaryB, folderId);

        // Should only find event b
        List<MultipleEventData> eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summaryA).withField(ChronosJsonFields.SUMMARY, summaryB).withLogicalOperation(LogicalOperation.OR).inFolder(folderId));
        assertNoResultsForFolder(eventData, defaultFolderId);
        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), createEventB);

        // Should only find event a
        eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summaryA).withField(ChronosJsonFields.SUMMARY, summaryB).withLogicalOperation(LogicalOperation.OR).inFolder(defaultFolderId));
        assertNoResultsForFolder(eventData, folderId);
        assertEvent(assertResultsForFolder(eventData, defaultFolderId).getEvents(), createEventA);

        // Should find both
        eventData = performSearch(new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summaryA).withField(ChronosJsonFields.SUMMARY, summaryB).withLogicalOperation(LogicalOperation.OR).inFolder(defaultFolderId).inFolder(folderId));
        assertEvent(assertResultsForFolder(eventData, defaultFolderId).getEvents(), createEventA);
        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), createEventB);
    }

    /**
     * Creates an event series with 5 occurrences and searches via the {@link ChronosJsonFields#SUMMARY}
     * AND {@link ChronosJsonFields#CREATED_BY} field in specific folder. Expands series.
     */
    @Test
    public void testSearchSummaryExpandSeriesInFolder() throws Exception {
        createSeriesInFolder("testSearchSummaryExpandSeriesShouldNotFindMe" + System.nanoTime(), folderId);

        String summary = "testSearchSummaryExpandSeriesInFolder" + System.nanoTime();
        EventData expectedEvent = createSeriesInFolder(summary, folderId);

        // First search only in parent folder and assert that the event is not present there 
        SearchPerformer searchPerformer = new SearchPerformer().withField(ChronosJsonFields.SUMMARY, summary).withField(ChronosJsonFields.CREATED_BY, Integer.toString(getUserId())).withExpand(true).inFolder(defaultFolderId);
        List<MultipleEventData> eventData = performSearch(searchPerformer);
        assertNoResultsForFolder(eventData, folderId);

        // Check in the proper folder
        searchPerformer.resetFolders().inFolder(folderId).withExtendedEntities(true);
        eventData = performSearch(searchPerformer);

        List<EventData> expectedEvents = eventManager.getAllEvents(folderId, start, end, true);
        expectedEvents = getEventsByUid(expectedEvents, expectedEvent.getUid());
        assertEquals("Expected 5 occurrences", 5, expectedEvents.size());
        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEvents.toArray(new EventData[] {}));
    }

    /**
     * Creates an event series with 3 occurrences and searches via the {@link ChronosJsonFields#SUMMARY}
     * AND {@link ChronosJsonFields#CREATED_BY} field in specific folder. Expands series.
     */
    @Test
    public void testSearchRRuleExpandSeriesInFolder() throws Exception {
        createSeriesInFolder("testSearchRRuleExpandSeriesInFolderShouldNotFindMe" + System.nanoTime(), folderId);

        String summary = "testSearchRRuleExpandSeriesInFolder" + System.nanoTime();
        createSeriesInFolder(summary, folderId);// Series with same name but different occurrence count

        EventData expectedEvent3 = createSeriesInFolder(summary, 3, folderId);
        String rrule = expectedEvent3.getRrule();

        // First search only in parent folder and assert that the event is not present there 
        SearchPerformer searchPerformer = new SearchPerformer().withField(ChronosJsonFields.RECURRENCE_RULE, rrule).withField(ChronosJsonFields.SUMMARY, summary).inFolder(defaultFolderId).withExpand(true);
        List<MultipleEventData> eventData = performSearch(searchPerformer);
        assertNoResultsForFolder(eventData, folderId);

        // Check in the proper folder
        searchPerformer.resetFolders().inFolder(folderId).withExtendedEntities(true);
        eventData = performSearch(searchPerformer);

        List<EventData> expectedEvents = eventManager.getAllEvents(folderId, start, end, true);
        expectedEvents = getEventsByUid(expectedEvents, expectedEvent3.getUid());
        assertEquals("Expected 3 occurrences", 3, expectedEvents.size());
        assertEvent(assertResultsForFolder(eventData, folderId).getEvents(), expectedEvents.toArray(new EventData[] {}));
    }

    @Test
    public void testSearchBirthdayBySummary() throws Exception {
        /*
         * create contact with birthday
         */
        Date birthday = TimeTools.D("next saturday at midnight", TimeZones.UTC);
        ContactData contactData = createContact(randomUid(), randomUid(), randomUid() + "@example.org", birthday);
        /*
         * search for this birthday event occurrence
         */
        String birthdaysCalendarFolder = getBirthdayCalendarFolder();
        MultipleFolderEventsResponse searchResponse = new SearchPerformer()
            .withLogicalOperation(LogicalOperation.AND)
            .withField(ChronosJsonFields.SUMMARY, contactData.getFirstName())
            .withField(ChronosJsonFields.SUMMARY, contactData.getLastName())
            .inFolder(birthdaysCalendarFolder)
            .withExpand(true)
            .execute();
        ;
        assertResultForFolder(searchResponse.getData(), birthdaysCalendarFolder, contactData.getUid());
    }
    
    @Test
    public void testSearchBirthdayByAttendee() throws Exception {
        /*
         * create contact with birthday
         */
        Date birthday = TimeTools.D("next saturday at midnight", TimeZones.UTC);
        ContactData contactData = createContact(randomUid(), randomUid(), randomUid() + "@example.org", birthday);
        /*
         * search for this birthday event occurrence
         */
        String birthdaysCalendarFolder = getBirthdayCalendarFolder();
        MultipleFolderEventsResponse searchResponse = new SearchPerformer()
            .withField(ChronosJsonFields.ATTENDEES, contactData.getEmail1())
            .inFolder(birthdaysCalendarFolder)
            .withExpand(true)
            .execute();
        ;
        assertResultForFolder(searchResponse.getData(), birthdaysCalendarFolder, contactData.getUid());
    }

    @Test
    public void testSearchBirthdayByLastModified() throws Exception {
        /*
         * create contact with birthday
         */
        Date birthday = TimeTools.D("next saturday at midnight", TimeZones.UTC);
        ContactData contactData = createContact(randomUid(), randomUid(), randomUid() + "@example.org", birthday);
        /*
         * search for this birthday event occurrence
         */
        String birthdaysCalendarFolder = getBirthdayCalendarFolder();
        MultipleFolderEventsResponse searchResponse = new SearchPerformer()
            .withField(ChronosJsonFields.LAST_MODIFIED, String.valueOf(contactData.getLastModified()))
            .inFolder(birthdaysCalendarFolder)
            .withExpand(true)
            .execute();
        ;
        assertResultForFolder(searchResponse.getData(), birthdaysCalendarFolder, contactData.getUid());
    }

    ////////////////////////////////////// HELPERS ///////////////////////////////////////


    /**
     * Creates a contact with birthday.
     * 
     * @param firstName The first name to set
     * @param lastName The last name to set
     * @param birthday The birthday to set
     * @return The created contact
     */
    private ContactData createContact(String firstName, String lastName, String email1, Date birthday) throws Exception {
        ContactsApi contactsApi = new ContactsApi(getApiClient());
        String folderId = getDefaultContactFolder();
        ContactData contactData = new ContactData();
        contactData.setFirstName(firstName);
        contactData.setLastName(lastName);
        contactData.setEmail1(email1);
        contactData.setBirthday(L(birthday.getTime()));
        contactData.setFolderId(folderId);
        ContactUpdateResponse createResponse = contactsApi.createContact(contactData);
        assertNull(createResponse.getErrorDesc(), createResponse.getError());
        ContactListElement listElement = new ContactListElement();
        listElement.setFolder(folderId);
        listElement.setId(createResponse.getData().getId());
        if (null == contactsToCleanup) {
            contactsToCleanup = new ArrayList<ContactListElement>();
        }
        contactsToCleanup.add(listElement);
        ContactResponse getResponse = contactsApi.getContact(listElement.getId(), listElement.getFolder());
        assertNull(getResponse.getErrorDesc(), getResponse.getError());
        return getResponse.getData();
    }

    private static String randomUid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Asserts the specified event
     *
     * @param events The events list
     * @param expectedEvents The expected event
     */
    private void assertEvent(List<EventData> events, EventData... expectedEvents) {
        assertEquals("Response eventData count does not match", expectedEvents.length, events.size());
        int counter = 0;
        for (EventData event : expectedEvents) {
            AssertUtil.assertEventsEqual(event, events.get(counter++));
        }
    }

    /**
     * Asserts that there are no found events in the search results for a specific folder.
     *
     * @param eventDataLists The search results
     * @param folderId The expected folder identifier
     */
    private static void assertNoResultsForFolder(List<MultipleEventData> eventDataLists, String folderId) {
        for (MultipleEventData multipleEventData : eventDataLists) {
            if (folderId.equals(multipleEventData.getFolder())) {
                assertTrue("Unexpected results for folder " + folderId, null == multipleEventData.getEvents() || multipleEventData.getEvents().isEmpty());
            }
        }
    }

    /**
     * Asserts that there is at least one found event in the search results for a specific folder.
     *
     * @param eventDataLists The search results
     * @param folderId The expected folder identifier
     * @return The matching event data list for the folder
     */
    private static MultipleEventData assertResultsForFolder(List<MultipleEventData> eventDataLists, String folderId) {
        for (MultipleEventData multipleEventData : eventDataLists) {
            if (folderId.equals(multipleEventData.getFolder())) {
                assertNotNull("No results for folder " + folderId, multipleEventData.getEvents());
                assertTrue("No results for folder " + folderId, 0 < multipleEventData.getEvents().size());
                return multipleEventData;
            }
        }
        fail("No results for folder " + folderId);
        return null;
    }

    /**
     * Asserts that there is a found event matching the expected UID in the search results for a specific folder.
     *
     * @param eventDataLists The search results
     * @param folderId The expected folder identifier
     * @param expectedUid The UID to lookup the result for
     * @return The matching event in the results of the folder
     */
    private static EventData assertResultForFolder(List<MultipleEventData> eventDataLists, String folderId, String expectedUid) {
        MultipleEventData multipleEventData = assertResultsForFolder(eventDataLists, folderId);
        for (EventData foundEventData : multipleEventData.getEvents()) {
            if (expectedUid.equals(foundEventData.getUid())) {
                return foundEventData;
            }
        }
        fail("No results for uid " + expectedUid);
        return null;
    }

    /**
     * Performs an advanced search with the specified search performer
     *
     * @param performer The search performer
     * @param expectedResultCount The expected folder results count
     * @return The results
     * @throws ApiException if an API error is occurred
     * @throws JSONException if a JSON error is occurred
     */
    private List<MultipleEventData> performSearch(SearchPerformer performer) throws ApiException, JSONException {
        MultipleFolderEventsResponse response = performer.execute();
        System.out.println(response.toJson());
        List<MultipleEventData> data = response.getData();
        assertNotNull("Response data is null", data);
        return data;
    }

    /**
     * Creates a single two hour event in the specified folder and
     * with the specified summary
     *
     * @param summary The summary
     * @param folderId The folder identifier
     * @return The {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    private EventData createEventInFolder(String summary, String folderId) throws ApiException {
        return eventManager.createEvent(EventFactory.createSingleTwoHourEvent(getUserId(), summary, folderId), true);
    }

    /**
     * Creates a single two hour event in the specified folder and
     * with the specified summary and description
     *
     * @param summary The summary
     * @param description The description
     * @param folderId The folder identifier
     * @return The {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    private EventData createEventInFolder(String summary, String description, String folderId) throws ApiException {
        return eventManager.createEvent(EventFactory.createSingleTwoHourEvent(getUserId(), summary, description, folderId), true);
    }

    /**
     * Creates a series with 5 occurrences in the specified folder and with the specified summary
     *
     * @param summary The summary
     * @param folderId The folder identifier
     * @return The {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ParseException if a parsing error is occurred
     */
    private EventData createSeriesInFolder(String summary, String folderId) throws ApiException, ParseException {
        return createSeriesInFolder(summary, 5, folderId);
    }

    /**
     * Creates a series with the specified amount of occurrences in the specified folder and with the specified summary
     *
     * @param summary The summary
     * @param folderId The folder identifier
     * @return The {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ParseException if a parsing error is occurred
     */
    private EventData createSeriesInFolder(String summary, int occurrences, String folderId) throws ApiException, ParseException {
        Calendar cal = DateTimeUtil.getUTCCalendar();
        DateTimeData startDate = DateTimeUtil.getDateTime(cal);
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));
        return eventManager.createEvent(EventFactory.createSeriesEvent(getUserId(), summary, startDate, endDate, occurrences, folderId), true);
    }

    /**
     * Prepares the search body with the specified parameters, logical operation and optional folder ids
     *
     * @param searchParams The search parameters
     * @param logicalOperation The logical operation
     * @param folders The optional folder ids
     * @return The search body as string
     * @throws JSONException if a JSON error is occurred
     */
    private String prepareSearchFor(List<List<String>> searchParams, LogicalOperation logicalOperation, String... folders) throws JSONException {
        JSONArray operands = new JSONArray();
        operands.put(logicalOperation.name().toLowerCase());
        for (List<String> entries : searchParams) {
            if (entries.size() != 3) {
                continue;
            }
            JSONObject f = new JSONObject().put(FIELD, entries.get(0));
            JSONArray op = new JSONArray().put(entries.get(1));
            operands.put(op.put(f).put(entries.get(2)));
        }
        JSONObject json = new JSONObject().put(FILTER, operands);
        return appendFolders(json, folders).toString();
    }

    /**
     * Appends the specified folders to the specified body
     * 
     * @param body The json body
     * @param folders The folders
     * @throws JSONException if a JSON error is occurred
     */
    private JSONObject appendFolders(JSONObject body, String[] folders) throws JSONException {
        if (folders == null || folders.length <= 0) {
            return body;
        }
        JSONArray fids = new JSONArray();
        for (String f : folders) {
            fids.put(f);
        }
        return body.put(FOLDERS, fids);
    }

    //////////////////////////////////// NESTED CLASSES ////////////////////////////////////

    /**
     * {@link SearchPerformer} - Prepares and executes an advanced search request
     */
    private class SearchPerformer {

        private final List<List<String>> filter;
        private List<String> folderIds;
        private String returnFields;
        private boolean expand = false;
        private LogicalOperation operation = LogicalOperation.AND;
        private boolean extendedEntities;

        /**
         * Initializes a new {@link AdvancedSearchTest.SearchPerformer}.
         * 
         * <p>Defaults with <code>AND</code> logical operation and <code>false</code> for expand series.</p>
         */
        private SearchPerformer() {
            super();
            filter = new LinkedList<>();
            folderIds = new LinkedList<>();
        }

        /**
         * Clears the folder ids
         *
         * @return This instance
         */
        private SearchPerformer resetFolders() {
            folderIds.clear();
            return this;
        }

        /**
         * Sets the logical operation for all operands
         *
         * @param operation The logical operation to set
         * @return This instance
         */
        private SearchPerformer withLogicalOperation(LogicalOperation operation) {
            this.operation = operation;
            return this;
        }

        /**
         * Adds a folder id in the search-able folders
         *
         * @param folderId The folder id to add
         * @return This instance
         */
        private SearchPerformer inFolder(String folderId) {
            folderIds.add(folderId);
            return this;
        }

        /**
         * Adds the field to the search term with the EQUALS operand
         *
         * @param field The field name
         * @param value The field's value
         * @return This instance
         */
        private SearchPerformer withField(String field, String value) {
            withField(field, Operand.EQUALS, value);
            return this;
        }

        /**
         * Adds the field to the search term
         *
         * @param field The field name
         * @param operand The operand
         * @param value The field's value
         * @return This instance
         */
        private SearchPerformer withField(String field, Operand operand, String value) {
            filter.add(ImmutableList.of(field, operand.getSymbol(), value));
            return this;
        }

        /**
         * Sets the fields to return
         *
         * @param fields The fields to return
         * @return This instance
         */
        private SearchPerformer withReturnFields(String fields) {
            this.returnFields = fields;
            return this;
        }

        /**
         * Whether to expand series
         *
         * @param expand The flag to expand series (defaults to false)
         * @return This instance
         */
        private SearchPerformer withExpand(boolean expand) {
            this.expand = expand;
            return this;
        }

        /**
         * Whether to extend the attendee entities with contact information
         *
         * @param extend The flag to extend the attendee entities (The attendeeties ;)
         * @return This instance
         */
        private SearchPerformer withExtendedEntities(boolean extend) {
            this.extendedEntities = extend;
            return this;
        }

        /**
         * Executes the request
         *
         * @return The response
         * @throws ApiException if an API error is occurred
         * @throws JSONException if a JSON error is occurred
         */
        private MultipleFolderEventsResponse execute() throws ApiException, JSONException {
            return chronosApi.searchChronosAdvanced(rangeStart, rangeEnd, prepareSearchFor(filter, operation, folderIds.toArray(new String[] {})), returnFields, null, null, B(expand), B(extendedEntities));
        }
    }

    //////////////////////////////////////////// NESTED ENUMS ///////////////////////////////////////

    /**
     * {@link LogicalOperation} - The logical operations
     */
    private enum LogicalOperation {
        AND,
        OR;
    }

    /**
     * {@link Operand} - The operands
     */
    private enum Operand {

        EQUALS("="),
        GREATER_THAN(">"),
        LESS_THAN("<");

        private String symbol;

        /**
         * Initializes a new {@link Operand}.
         * 
         * @param symbol The symbol
         */
        Operand(String symbol) {
            this.symbol = symbol;

        }

        /**
         * The operand's symbol
         *
         * @return the operand's symbol
         */
        String getSymbol() {
            return symbol;
        }
    }
}
