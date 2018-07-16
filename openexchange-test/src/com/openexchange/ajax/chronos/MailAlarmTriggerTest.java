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

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.data.conversion.ical.Assert;
import com.openexchange.mail.MailListField;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link MailAlarmTriggerTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MailAlarmTriggerTest extends AbstractAlarmTriggerTest {

    private static final MailListField[] COLUMNS = new MailListField[] { MailListField.ID, MailListField.SENT_DATE, MailListField.SUBJECT }; // MailListField.FROM causes errors

    private MailApi mailApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mailApi = new MailApi(apiClient);
    }

    /**
     * Creates a mail alarm and checks if a mail is successfully send to the inbox
     *
     * @throws ChronosApiException
     * @throws ApiException
     * @throws InterruptedException
     */
    @Test
    public void testBasicMailAlarm() throws ApiException, ChronosApiException, InterruptedException {

        long currentTime = System.currentTimeMillis();
        DateTimeData startDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16));
        DateTimeData endDate = DateTimeUtil.getDateTime(currentTime + TimeUnit.MINUTES.toMillis(16) + TimeUnit.HOURS.toMillis(1));
        Alarm mailAlarm = AlarmFactory.createMailAlarm("-PT15M", null, null, null);
        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testCreateSingleAlarmTrigger", startDate, endDate, mailAlarm, folderId);
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // wait until the mail is send (1 minute + 30 seconds as a buffer)
        Thread.sleep(TimeUnit.SECONDS.toMillis(90));

        MailsResponse mailResponse = mailApi.getAllMails(getSessionId(), "default0/INBOX", getColumns(), null, false, false, String.valueOf(MailListField.DATE.getField()), "DESC", null, null, 100, null);
        Assert.assertNull(mailResponse.getError());
        Assert.assertNotNull(mailResponse.getData());
        for (List<String> mail : mailResponse.getData()) {
            // TODO identify the reminder mail
        }

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
