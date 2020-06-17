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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.webdav;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.ajax.framework.ProvisioningSetup;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.java.Charsets;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.restclient.invoker.ApiClient;
import com.openexchange.testing.restclient.modules.InternetFreeBusyApi;

/**
 * {@link FreeBusyTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */

@RunWith(BlockJUnit4ClassRunner.class)
public class FreeBusyTest extends AbstractChronosTest {

    private static final Map<String, String> CONFIG = new HashMap<String, String>();

    private static final int DEFAULT_WEEKS_FUTURE = 4;

    private static final int DEFAULT_WEEKS_PAST = 1;

    private Date testDate;

    private EventData busyDate;

    private EventData freeDate;

    private String userName;

    private String server;

    private int contextid;

    private InternetFreeBusyApi api;

    private String testResourceName;

    private TestUser restUser;

    private String hostname;

    private String protocol;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        /*
         * Initialize internet free busy API with generated REST API client
         */
        ProvisioningSetup.init();
        ApiClient restClient = new ApiClient();
        restClient.setBasePath(getBasePath());
        restUser = TestContextPool.getRestUser();
        restClient.setUsername(restUser.getUser());
        restClient.setPassword(restUser.getPassword());
        String authorizationHeaderValue = "Basic " + Base64.encodeBase64String((restUser.getUser() + ":" + restUser.getPassword()).getBytes(Charsets.UTF_8));
        restClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
        api = new InternetFreeBusyApi(restClient);
        /*
         * Prepare config
         */
        CONFIG.put(FreeBusyProperty.PUBLISH_INTERNET_FREEBUSY.getFQPropertyName(), "true");
        /*
         * Create test data
         */
        userName = testUser.getUser();
        server = testUser.getContext();
        contextid = getClient().getValues().getContextId();
        testResourceName = "test-resource-1";
        this.testDate = new Date();
        this.busyDate = createEvent(TranspEnum.OPAQUE, FreeBusyTest.class.getName() + "_Busy", 10, 11);
        this.freeDate = createEvent(TranspEnum.TRANSPARENT, FreeBusyTest.class.getName() + "_Free", 11, 12);
    }

    protected String getBasePath() {
        if (hostname == null) {
            this.hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        }

        if (protocol == null) {
            this.protocol = AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL);
            if (this.protocol == null) {
                this.protocol = "http";
            }
        }
        return this.protocol + "://" + this.hostname + ":8009";
    }

    @Override
    public void tearDown() throws Exception {
        TestContextPool.backContext(testContext);
        super.tearDown();
    }

    // -------------------------------Positive tests--------------------------------------------------------------

    /**
     * 
     * Tests whether valid iCalendar data is returned,
     * if only contentId, user name and server name are given.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_MinimumValidData_Status200() throws Exception, ApiException {
        super.setUpConfiguration();
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, null, null, null);
        validateICal(icalResponse, userName, server, DEFAULT_WEEKS_PAST, DEFAULT_WEEKS_FUTURE, false);
    }

    /**
     * 
     * Tests whether valid iCalendar data is returned,
     * if contentId, resource name and server name are given.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_Resource_Status200() throws Exception, ApiException {
        super.setUpConfiguration();
        String icalResponse = api.getFreeBusy(I(contextid), testResourceName, server, null, null, null);
        validateICal(icalResponse, testResourceName, server, DEFAULT_WEEKS_PAST, DEFAULT_WEEKS_FUTURE, false);
    }

    /**
     * 
     * Tests whether valid iCalendar data is returned for a resource, if the value simple is set to true.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_ResourceSimple_Status200() throws Exception, ApiException {
        super.setUpConfiguration();
        String icalResponse = api.getFreeBusy(I(contextid), testResourceName, server, null, null, B(true));
        validateICal(icalResponse, testResourceName, server, DEFAULT_WEEKS_PAST, DEFAULT_WEEKS_FUTURE, true);
    }

    /**
     * 
     * Tests whether valid iCalendar data is returned, if all parameters are given.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_MaximumValidData_Status200() throws Exception {
        super.setUpConfiguration();
        int weeksPast = 12;
        int weeksFuture = 26;
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, I(weeksPast), I(weeksFuture), B(false));
        validateICal(icalResponse, userName, server, weeksPast, weeksFuture, false);
    }

    /**
     * 
     * Tests whether valid iCalendar data is returned, if the value simple is set to true.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_Simple_Status200() throws Exception {
        super.setUpConfiguration();
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, null, null, B(true));
        validateICal(icalResponse, userName, server, DEFAULT_WEEKS_PAST, DEFAULT_WEEKS_FUTURE, true);
    }

    /**
     * 
     * Tests whether valid iCalendar data is returned, if the requested time range into future is greater than the configured maximum.
     * In this case the time range up to the configured time range is expected.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_PropertyWeeksFuture_Status200() throws Exception {
        int maxTimerangeFuture = 1;
        CONFIG.put(FreeBusyProperty.INTERNET_FREEBUSY_MAXIMUM_TIMERANGE_FUTURE.getFQPropertyName(), I(maxTimerangeFuture).toString());
        super.setUpConfiguration();
        int weeksFuture = 2;
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, null, I(weeksFuture), null);
        validateICal(icalResponse, userName, server, DEFAULT_WEEKS_PAST, maxTimerangeFuture, false);
    }

    /**
     * 
     * Tests whether valid iCalendar data is returned, if the requested time range into past is greater than the configured maximum.
     * In this case the time range down to the configured time range is expected.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_PropertyWeeksPast_Status200() throws Exception {
        int maxTimerangePast = 1;
        CONFIG.put(FreeBusyProperty.INTERNET_FREEBUSY_MAXIMUM_TIMERANGE_PAST.getFQPropertyName(), I(maxTimerangePast).toString());
        super.setUpConfiguration();
        int weeksPast = 2;
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, I(weeksPast), null, null);
        validateICal(icalResponse, userName, server, maxTimerangePast, DEFAULT_WEEKS_FUTURE, false);
    }

    /**
     * 
     * Tests the edge case where the maximum time range into the past property is set to 0.
     * In this case no past free busy times should be returned.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_PropertyWeeksPast0_Status200() throws Exception {
        int maxTimerangePast = 0;
        CONFIG.put(FreeBusyProperty.INTERNET_FREEBUSY_MAXIMUM_TIMERANGE_PAST.getFQPropertyName(), I(maxTimerangePast).toString());
        super.setUpConfiguration();
        int weeksPast = 2;
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, I(weeksPast), null, null);
        validateICal(icalResponse, userName, server, maxTimerangePast, DEFAULT_WEEKS_FUTURE, false);
    }

    /**
     * 
     * Tests the edge case where the time range into the past parameter is set to 0.
     * In this case no past free busy times should be returned.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_WeeksPast0_Status200() throws Exception {
        super.setUpConfiguration();
        int weeksPast = 0;
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, I(weeksPast), null, null);
        validateICal(icalResponse, userName, server, weeksPast, DEFAULT_WEEKS_FUTURE, false);
    }

    /**
     * 
     * Tests the edge case where the maximum time range into the future property is set to 0.
     * In this case no future free busy times should be returned.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_PropertyWeeksFuture0_Status200() throws Exception {
        int maxTimerangeFuture = 0;
        CONFIG.put(FreeBusyProperty.INTERNET_FREEBUSY_MAXIMUM_TIMERANGE_FUTURE.getFQPropertyName(), I(maxTimerangeFuture).toString());
        super.setUpConfiguration();
        int weeksFuture = 2;
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, null, I(weeksFuture), null);
        validateICal(icalResponse, userName, server, DEFAULT_WEEKS_PAST, maxTimerangeFuture, false);
    }

    /**
     * 
     * Tests the edge case where the time range into the future parameter is set to 0.
     * In this case no future free busy times should be returned.
     *
     * @throws Exception if changing the configuration fails
     * @throws ApiException if an error occurred by getting the free busy data
     */
    @Test
    public void testGetFreeBusy_WeeksFuture0_Status200() throws Exception {
        super.setUpConfiguration();
        int weeksFuture = 0;
        String icalResponse = api.getFreeBusy(I(contextid), userName, server, null, I(weeksFuture), null);
        validateICal(icalResponse, userName, server, DEFAULT_WEEKS_PAST, weeksFuture, false);
    }

    // -------------------------------Depend on property tests--------------------------------------------------------------

    /**
     * 
     * Tests whether getting free busy data failed with code 404,
     * if free busy data is not published for the requested user.
     *
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_PublishDisabled_Status404() throws Exception {
        CONFIG.put(FreeBusyProperty.PUBLISH_INTERNET_FREEBUSY.getFQPropertyName(), "false");
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(contextid), userName, server, null, null, null);
            fail("Expected error code 404");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(404, e.getCode());
        }
    }

    // -------------------------------Invalid input tests--------------------------------------------------------------

    /**
     * 
     * Tests whether getting free busy data failed with code 400,
     * if context id is null.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_ContextIdNull_Status400() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(null, userName, server, null, null, null);
            fail("Expected error code 400");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(400, e.getCode());
        }
    }

    /**
     * 
     * Tests whether getting free busy data failed with code 404,
     * if context id is negative.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_ContextIdNegative_Status404() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(-1), userName, server, null, null, null);
            fail("Expected error code 404");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(404, e.getCode());
        }
    }

    /**
     * 
     * Tests whether getting free busy data failed with code 400,
     * if user name is null.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_UsernameNull_Status400() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(contextid), null, server, null, null, null);
            fail("Expected error code 400");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(400, e.getCode());
        }
    }

    /**
     * 
     * Tests whether getting free busy data failed with code 400,
     * if server name is null.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_ServerNull_Status400() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(contextid), userName, null, null, null, null);
            fail("Expected error code 400");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(400, e.getCode());
        }
    }

    /**
     * 
     * Tests whether getting free busy data failed with code 404,
     * if context id does not fit to user and server name.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_WrongContextId_Status404() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(contextid + 1), userName, server, null, null, null);
            fail("Expected error code 404");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(404, e.getCode());
        }
    }

    /**
     * 
     * Tests whether getting free busy data failed with code 404,
     * if the user name is not existing in the context.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_WrongUserName_Status404() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(contextid), userName + "Test", server, null, null, null);
            fail("Expected error code 404");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(404, e.getCode());
        }
    }

    /**
     * 
     * Tests whether getting free busy data failed with code 404,
     * if the server name does not fit to context id and user name.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_WrongServer_Status404() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(contextid), userName, server + ".test", null, null, null);
            fail("Expected error code 404");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(404, e.getCode());
        }
    }

    /**
     * 
     * Tests whether getting free busy data failed with code 400,
     * if the time range into past value is negative.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_WeeksStartNegative_Status400() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(contextid), userName, server, I(-1), null, null);
            fail("Expected error code 400");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(400, e.getCode());
        }
    }

    /**
     * 
     * Tests whether getting free busy data failed with code 400,
     * if the time range into future value is negative.
     * 
     * @throws Exception if changing the configuration fails
     */
    @Test
    public void testGetFreeBusy_WeeksEndNegative_Status400() throws Exception {
        super.setUpConfiguration();
        try {
            api.getFreeBusy(I(contextid), userName, server, null, I(-1), null);
            fail("Expected error code 400");
        } catch (com.openexchange.testing.restclient.invoker.ApiException e) {
            assertEquals(400, e.getCode());
        }
    }



    // -------------------------------Helper methods--------------------------------------------------------------

    private EventData createEvent(TranspEnum visibility, String name, int start, int end) throws ApiException {
        EventData event = new EventData();
        event.setPropertyClass("PUBLIC");
        Attendee attendeeUser = new Attendee();
        attendeeUser.entity(getApiClient().getUserId());
        attendeeUser.cuType(CuTypeEnum.INDIVIDUAL);
        Attendee attendeeRessouce = new Attendee();
        attendeeRessouce.setUri(CalendarUtils.getURI(testResourceName + "@" + server));
        attendeeRessouce.cuType(CuTypeEnum.RESOURCE);
        List<Attendee> attendees = new ArrayList<>();
        attendees.add(attendeeUser);
        attendees.add(attendeeRessouce);
        event.setAttendees(attendees);
        event.setTransp(visibility);
        event.setSummary(name);
        Date startDate = CalendarUtils.add(new Date(), Calendar.HOUR_OF_DAY, start);
        event.setStartDate(DateTimeUtil.getDateTime("utc", startDate.getTime()));
        Date endDate = CalendarUtils.add(new Date(), Calendar.HOUR_OF_DAY, end);
        event.setEndDate(DateTimeUtil.getDateTime("utc", endDate.getTime()));

        return eventManager.createEvent(event);
    }

    private void validateICal(String content, String userName, String serverName, int weeksPast, int weeksFuture, boolean simple) throws ParseException {
        assertTrue("Is no VCalendar", content.startsWith("BEGIN:VCALENDAR") && content.contains("END:VCALENDAR"));
        assertTrue("Contains no VFreeBusy", content.contains("BEGIN:VFREEBUSY") && content.contains("END:VFREEBUSY"));
        String startDate = this.busyDate.getStartDate().getValue();
        String endDate = this.busyDate.getEndDate().getValue();
        if (weeksFuture > 0) {
            if (simple) {
                assertTrue("Contains no simple free busy data.", content.contains("FREEBUSY:"));

                assertTrue("Does not contain the busy data", content.contains(startDate) && content.contains(endDate));
            } else {
                assertTrue("Contains no extended free busy data.", content.contains("FREEBUSY;FBTYPE=BUSY:"));
                assertTrue("Does not contain the busy data", content.contains(startDate) && content.contains(endDate));
                assertTrue("Contains no extended free busy data.", content.contains("FREEBUSY;FBTYPE=FREE:"));
                startDate = this.freeDate.getStartDate().getValue();
                endDate = this.freeDate.getEndDate().getValue();
                assertTrue("Does not contain the free data", content.contains(startDate) && content.contains(endDate));
            }
        }

        Date expectedStart = CalendarUtils.add(this.testDate, Calendar.WEEK_OF_YEAR, -weeksPast);
        int startIndex = content.indexOf("DTSTART") + 8;
        String dtStart = content.substring(startIndex, startIndex + 17);
        Date actualStart = DateTimeUtil.parseZuluDateTime(dtStart);
        assertTrue("Contains wrong start time", Math.abs(expectedStart.getTime() - actualStart.getTime()) < 5000);

        Date expectedEnd = CalendarUtils.add(this.testDate, Calendar.WEEK_OF_YEAR, weeksFuture);
        int endIndex = content.indexOf("DTEND") + 6;
        String dtEnd = content.substring(endIndex, endIndex + 17);
        Date actualEnd = DateTimeUtil.parseZuluDateTime(dtEnd);
        assertTrue("Contains wrong end time", Math.abs(expectedEnd.getTime() - actualEnd.getTime()) < 5000);

        assertTrue("Does not contain the user name", content.contains(userName));
        assertTrue("Does not contain the server name", content.contains(serverName));
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getScope() {
        return "context";
    }

}
