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

import static com.openexchange.java.Autoboxing.L;
import java.rmi.server.UID;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.mail.MailListField;
import com.openexchange.test.common.data.conversion.ical.Assert;
import com.openexchange.test.tryagain.TryAgain;
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
        testContext.acquireNoReplyUser();
        mailApi = new MailApi(getApiClient());
        userApi = new UserApi(getApiClient());
    }

    /**
     * Creates an event with a mail alarm and checks if a mail is successfully send to the inbox
     */
    @Test
    @TryAgain
    public void testBasicMailAlarm() throws ApiException, ChronosApiException, InterruptedException {

        String summary = "mailAlarmTest_" + new UID().toString();
        summary = summary.substring(0, 30); // reduce length to 30 to not being limited during mail generation
        long currentTime = System.currentTimeMillis();
        Calendar startTime = Calendar.getInstance(getUserTimeZone());
        startTime.setTimeInMillis(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(15)); // in 15 min 15 s
        DateTimeData startDate = DateTimeUtil.getDateTime(startTime);
        startTime.add(Calendar.HOUR, 1); // in 1 h 15 min 15 s
        DateTimeData endDate = DateTimeUtil.getDateTime(startTime);
        Alarm mailAlarm = AlarmFactory.createMailAlarm("-PT15M", null, null); // 15 min before start

        Calendar time = Calendar.getInstance(getUserTimeZone());
        long expectedSentDate = time.getTimeInMillis() + TimeUnit.SECONDS.toMillis(15);

        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(getCalendaruser(), summary, startDate, endDate, mailAlarm, folderId);
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // wait until the mail is send (15 seconds + 30 seconds as a buffer)
        Thread.sleep(TimeUnit.SECONDS.toMillis(45));

        checkMail(summary, time, getDates(expectedSentDate), 1);
    }

    /**
     * Creates an event with a mail alarm, updates the start time to a later time and check if no mail is send out
     *
     * @throws ChronosApiException
     * @throws ApiException
     * @throws InterruptedException
     */
    @Test
    @TryAgain
    public void testMoveOut() throws ApiException, ChronosApiException, InterruptedException {

        // Create event
        String summary = "testMoveOut_"+new UID().toString();
        long currentTime = System.currentTimeMillis();
        DateTimeData startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(30));
        DateTimeData endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1));
        Alarm mailAlarm = AlarmFactory.createMailAlarm("-PT15M", null, null);

        Calendar time = Calendar.getInstance(getUserTimeZone());
        long expectedSentDate = time.getTimeInMillis() + TimeUnit.SECONDS.toMillis(30);

        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(getCalendaruser(), summary, startDate, endDate, mailAlarm, folderId);
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

        checkMail(summary, time, getDates(expectedSentDate), 0);
    }

    List<Long> getDates(long expectedSentDate){
        return Collections.singletonList(L(expectedSentDate));
    }

    /**
     * Creates a mail alarm, updates the start time to a earlier time and check if a mail is send out on time
     *
     * @throws ChronosApiException
     * @throws ApiException
     * @throws InterruptedException
     */
    @Test
    @TryAgain
    public void testMoveIn() throws ApiException, ChronosApiException, InterruptedException {

        // Create event
        String summary = "testMoveIn_"+new UID().toString();
        long currentTime = System.currentTimeMillis();
        DateTimeData startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(30) + TimeUnit.HOURS.toMillis(3));
        DateTimeData endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1) + TimeUnit.HOURS.toMillis(3));
        Alarm mailAlarm = AlarmFactory.createMailAlarm("-PT15M", null, null);

        Calendar time = Calendar.getInstance(getUserTimeZone());
        long expectedSentDate = time.getTimeInMillis() + TimeUnit.SECONDS.toMillis(30);

        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(getCalendaruser(), summary, startDate, endDate, mailAlarm, folderId);
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
        checkMail(summary, time, getDates(expectedSentDate), 1);
    }

    private TimeZone getUserTimeZone() throws ApiException {
        UserResponse userResponse = userApi.getUser(null);
        Assert.assertNull(userResponse.getErrorDesc(), userResponse.getError());
        Assert.assertNotNull(userResponse.getData());
        UserData data = userResponse.getData();
        String timezone = data.getTimezone();
        return TimeZone.getTimeZone(timezone);
    }

    private void checkMail(String summary, Calendar time, List<Long> expectedSentDates, int mails) throws ApiException {
        MailsResponse mailResponse = mailApi.getAllMails("default0/INBOX", getColumns(), null, Boolean.FALSE, Boolean.FALSE, String.valueOf(MailListField.DATE.getField()), "DESC", null, null, Integer.valueOf(100), null);
        Assert.assertNull(mailResponse.getError());
        Assert.assertNotNull(mailResponse.getData());
        int found = 0;

        long delta = TimeUnit.SECONDS.toMillis(5);
        for (List<String> mail : mailResponse.getData()) {
            String subject = mail.get(2);
            if (subject != null && subject.contains(summary)) {
                if (mails == 0) {
                    Assert.fail("Mail found even though no mail should be sent out.");
                }
                long sentDate = Long.valueOf(mail.get(1)).longValue();
                sentDate = sentDate - time.getTimeZone().getOffset(sentDate);
                MailListElement element = new MailListElement();
                element.setFolder("default0/INBOX");
                element.setId(mail.get(0));
                mailApi.deleteMails(Collections.singletonList(element), L(Long.MAX_VALUE), null, null);
                found++;
                boolean matchAnySentDate = false;
                long closest = 0;
                for (Long expectedSentDate : expectedSentDates) {
                    if (Math.abs(sentDate - expectedSentDate.longValue()) <= delta) {
                        matchAnySentDate = true;
                        break;
                    }
                    if (closest == 0 || closest > Math.abs(sentDate - expectedSentDate.longValue())) {
                        closest = Math.abs(sentDate - expectedSentDate.longValue());
                    }
                }
                Assert.assertTrue("Wrong sent date. Expected a maximal difference of " + delta + " but was " + closest, matchAnySentDate);
            }
        }
        if (mails == 0) {
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
