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

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.data.conversion.ical.Assert;
import com.openexchange.mail.MailListField;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailListElement;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.MailApi;
import com.openexchange.testing.httpclient.modules.UserApi;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link MailAlarmTriggerTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MailAlarmTriggerTest extends AbstractAlarmTriggerTest {

    private static final MailListField[] COLUMNS = new MailListField[] { MailListField.ID, MailListField.SENT_DATE, MailListField.SUBJECT }; // MailListField.FROM causes errors

    private MailApi mailApi;

    private UserApi userApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mailApi = new MailApi(apiClient);
        userApi = new UserApi(apiClient);
    }

    /**
     * Creates an event with a mail alarm and checks if a mail is successfully send to the inbox
     *
     * @throws ChronosApiException
     * @throws ApiException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testBasicMailAlarm() throws ApiException, ChronosApiException, InterruptedException {

        String summary = "testCreateSingleAlarmTrigger_"+new UID().toString();
        long currentTime = System.currentTimeMillis();
        DateTimeData startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(15));
        DateTimeData endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1));
        Alarm mailAlarm = AlarmFactory.createMailAlarm("-PT15M", null, null, null);

        Calendar time = Calendar.getInstance(getUserTimeZone());
        long expectedSentDate = time.getTimeInMillis() + TimeUnit.SECONDS.toMillis(15);

        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), summary, startDate, endDate, mailAlarm, folderId);
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // wait until the mail is send (30 seconds + 15 seconds as a buffer)
        Thread.sleep(TimeUnit.SECONDS.toMillis(45));

        checkMail(summary, time, Collections.singletonList(expectedSentDate), 1);
    }

    /**
     * Creates an event with a mail alarm, updates the start time to a later time and check if no mail is send out
     *
     * @throws ChronosApiException
     * @throws ApiException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMoveOut() throws ApiException, ChronosApiException, InterruptedException {

        // Create event
        String summary = "testMoveOut_"+new UID().toString();
        long currentTime = System.currentTimeMillis();
        DateTimeData startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(30));
        DateTimeData endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1));
        Alarm mailAlarm = AlarmFactory.createMailAlarm("-PT15M", null, null, null);

        Calendar time = Calendar.getInstance(getUserTimeZone());
        long expectedSentDate = time.getTimeInMillis() + TimeUnit.SECONDS.toMillis(30);

        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), summary, startDate, endDate, mailAlarm, folderId);
        EventData event = eventManager.createEvent(toCreate);
        EventData createdEvent = getAndAssertAlarms(event, 1, folderId);

        // Update event
        startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(30) + TimeUnit.HOURS.toMillis(3));
        endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1) + TimeUnit.HOURS.toMillis(3));
        createdEvent.setStartDate(startDate);
        createdEvent.setEndDate(endDate);
        eventManager.updateEvent(createdEvent, false, false);

        // wait until the mail is send (30 seconds + 15 seconds as a buffer)
        Thread.sleep(TimeUnit.SECONDS.toMillis(45));

        checkMail(summary, time, Collections.singletonList(expectedSentDate), 0);
    }

    /**
     * Creates a mail alarm, updates the start time to a earlier time and check if a mail is send out on time
     *
     * @throws ChronosApiException
     * @throws ApiException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMoveIn() throws ApiException, ChronosApiException, InterruptedException {

        // Create event
        String summary = "testMoveIn_"+new UID().toString();
        long currentTime = System.currentTimeMillis();
        DateTimeData startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(30) + TimeUnit.HOURS.toMillis(3));
        DateTimeData endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1) + TimeUnit.HOURS.toMillis(3));
        Alarm mailAlarm = AlarmFactory.createMailAlarm("-PT15M", null, null, null);

        Calendar time = Calendar.getInstance(getUserTimeZone());
        long expectedSentDate = time.getTimeInMillis() + TimeUnit.SECONDS.toMillis(30);

        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), summary, startDate, endDate, mailAlarm, folderId);
        EventData event = eventManager.createEvent(toCreate);
        EventData createdEvent = getAndAssertAlarms(event, 1, folderId);

        // Update event
        startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(30) );
        endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1) );
        createdEvent.setStartDate(startDate);
        createdEvent.setEndDate(endDate);
        eventManager.updateEvent(createdEvent, false, false);

        // wait until the mail is send (30 seconds + 15 seconds as a buffer)
        Thread.sleep(TimeUnit.SECONDS.toMillis(45));
        checkMail(summary, time, Collections.singletonList(expectedSentDate), 1);
    }

    /**
     * Creates an event series with a mail alarm and two occurrences and checks if a mail is send out on time for both occurrences.
     *
     * @throws ChronosApiException
     * @throws ApiException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testEventSeries() throws ApiException, ChronosApiException, InterruptedException {

        // Create event
        String summary = "testEventSeries_"+new UID().toString();
        long currentTime = System.currentTimeMillis();
        DateTimeData startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(30));
        DateTimeData endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1));
        Alarm mailAlarm = AlarmFactory.createMailAlarm("-PT15M", null, null, null);

        Calendar time = Calendar.getInstance(getUserTimeZone());
        List<Long> expectedSentDates = new ArrayList<>();
        expectedSentDates.add(new Long(time.getTimeInMillis() + TimeUnit.SECONDS.toMillis(30)));
        expectedSentDates.add(new Long(time.getTimeInMillis() + TimeUnit.SECONDS.toMillis(30) + TimeUnit.MINUTES.toMillis(1)));

        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser(), summary, 2, folderId, RecurringFrequency.MINUTELY);
        toCreate.setStartDate(startDate);
        toCreate.setEndDate(endDate);
        toCreate.setAlarms(Collections.singletonList(mailAlarm));
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // wait until the mails are send (90 seconds + 15 seconds as a buffer)
        Thread.sleep(TimeUnit.SECONDS.toMillis(105));

        checkMail(summary, time, expectedSentDates, 2);
    }

    private TimeZone getUserTimeZone() throws ApiException {
        UserResponse userResponse = userApi.getUser(getSessionId(), null);
        Assert.assertNull(userResponse.getErrorDesc(), userResponse.getError());
        Assert.assertNotNull(userResponse.getData());
        UserData data = userResponse.getData();
        String timezone = data.getTimezone();
        return TimeZone.getTimeZone(timezone);
    }

    @SuppressWarnings("unchecked")
    private void checkMail(String summary, Calendar time, List<Long> expectedSentDates, int mails) throws ApiException {
        MailsResponse mailResponse = mailApi.getAllMails(getSessionId(), "default0/INBOX", getColumns(), null, false, false, String.valueOf(MailListField.DATE.getField()), "DESC", null, null, 100, null);
        Assert.assertNull(mailResponse.getError());
        Assert.assertNotNull(mailResponse.getData());
        int found = 0;

        long delta = TimeUnit.SECONDS.toMillis(5);
        for (List<String> mail : mailResponse.getData()) {
            String subject = mail.get(2);
            if( subject != null && subject.contains(summary)) {
                if(mails == 0) {
                    Assert.fail("Mail found even though no mail should be sent out.");
                }
                Long sentDate = Long.valueOf(mail.get(1));
                sentDate = sentDate - time.getTimeZone().getOffset(sentDate);
                MailListElement element = new MailListElement();
                element.setFolder("default0/INBOX");
                element.setId(mail.get(0));
                mailApi.deleteMails(getSessionId(), Collections.singletonList(element), Long.MAX_VALUE);
                found++;
                boolean matchAnySentDate = false;
                long closest = 0;
                for(Long expectedSentDate : expectedSentDates) {
                    if(Math.abs(sentDate - expectedSentDate) <= delta){
                        matchAnySentDate = true;
                        break;
                    } else {
                        if(closest==0 || closest > Math.abs(sentDate - expectedSentDate)) {
                            closest = Math.abs(sentDate - expectedSentDate);
                        }
                    }
                }
                Assert.assertTrue("Wrong sent date. Expected a maximal difference of "+delta+" but was "+closest, matchAnySentDate);
            }
        }
        if(mails == 0) {
            return;
        }
        Assert.assertEquals("Found a wrong amount of notification mails.", mails, found);
    }

    private static final String COMMA = ",";
    private String getColumns() {

        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (MailListField field : COLUMNS) {
            if (first) {
                result.append(field.getField());
                first = false;
            } else {
                result.append(COMMA).append(field.getField());
            }
        }

        return result.toString();
    }
}
