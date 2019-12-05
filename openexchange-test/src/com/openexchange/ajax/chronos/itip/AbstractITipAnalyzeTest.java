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

package com.openexchange.ajax.chronos.itip;

import static com.openexchange.ajax.chronos.itip.ITipUtil.convertToAttendee;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import com.openexchange.java.Strings;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.models.Analysis;
import com.openexchange.testing.httpclient.models.Analysis.ActionsEnum;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailDestinationData;

/**
 * {@link AbstractITipAnalyzeTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public abstract class AbstractITipAnalyzeTest extends AbstractITipTest {

    protected MailDestinationData mailData;

    protected EventData createdEvent;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        rememberForCleanup(createdEvent);
        rememberMail(apiClient, mailData);
        super.tearDown();
    }

    protected Attendee prepareCommonAttendees(EventData event) {
        Attendee replyingAttendee = convertToAttendee(testUserC2, Integer.valueOf(0));
        replyingAttendee.setPartStat(PartStat.NEEDS_ACTION.getStatus());
        // organizer gets added automatically
        event.setAttendees(Collections.singletonList(replyingAttendee));
        CalendarUser c = new CalendarUser();
        c.cn(userResponseC1.getData().getDisplayName());
        c.email(userResponseC1.getData().getEmail1());
        c.entity(Integer.valueOf(userResponseC1.getData().getId()));
        event.setOrganizer(c);
        event.setCalendarUser(c);

        return replyingAttendee;
    }

    protected Attendee prepareAttendees(EventData event) {
        Attendee replyingAttendee = convertToAttendee(testUserC2, apiClientC2.getUserId());
        replyingAttendee.setEntity(Integer.valueOf(0));
        replyingAttendee.setPartStat(PartStat.NEEDS_ACTION.getStatus());

        Attendee organizer = convertToAttendee(testUser, apiClient.getUserId());
        organizer.setPartStat(PartStat.ACCEPTED.getStatus());

        List<Attendee> attendees = new ArrayList<Attendee>(2);
        attendees.add(organizer);
        attendees.add(replyingAttendee);

        event.setAttendees(attendees);
        CalendarUser c = new CalendarUser();
        c.cn(userResponseC1.getData().getDisplayName());
        c.email(userResponseC1.getData().getEmail1());
        c.entity(Integer.valueOf(userResponseC1.getData().getId()));
        event.setOrganizer(c);
        event.setCalendarUser(c);

        return replyingAttendee;
    }

    /**
     * Prepares a new event with necessary attributes set.
     *
     * @param eventData The event do base an delta event on
     * @return An new event
     */
    protected EventData prepareDeltaEvent(EventData eventData) {
        EventData delta = new EventData();
        delta.setId(eventData.getId());
        if (Strings.isNotEmpty(eventData.getFolder())) {
            delta.setFolder(eventData.getFolder());
        }
        if (null != eventData.getRecurrenceId()) {
            delta.setRecurrenceId(eventData.getRecurrenceId());
        }
        if (null != eventData.getAttendees()) {
            delta.setAttendees(eventData.getAttendees());
        }
        return delta;
    }

    protected void analyze(String mailId) throws Exception {
        analyze(mailId, CustomConsumers.UPDATE.getConsumer());
    }

    protected void analyze(String mailId, CustomConsumers consumer) throws Exception {
        analyze(mailId, consumer.getConsumer());
    }

    protected void analyze(String mailId, Consumer<Analysis> analysisValidator) throws Exception {
        AnalyzeResponse response = analyze(ITipUtil.constructBody(mailId));
        Assert.assertThat("Should have no error", response.getErrorId(), nullValue());

        analyze(response, analysisValidator);
    }

    protected void analyze(AnalyzeResponse response, CustomConsumers consumer) {
        analyze(response, consumer.getConsumer());
    }

    protected void analyze(AnalyzeResponse response, Consumer<Analysis> analysisValidator) {
        List<Analysis> data = response.getData();
        Assert.assertThat("No Analyze", data, notNullValue());
        Assert.assertThat("Only one event should have been analyzed", Integer.valueOf(data.size()), is(Integer.valueOf(1)));

        if (null == analysisValidator) {
            CustomConsumers.ACTIONS.getConsumer().accept(data.get(0));
        } else {
            analysisValidator.accept(data.get(0));
        }
    }

    public enum CustomConsumers {
        /** Validates that the response does contain all actions */
        ALL((Analysis t) -> {
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.ACCEPT) || t.getActions().contains(ActionsEnum.ACCEPT_AND_IGNORE_CONFLICTS));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.TENTATIVE));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.DECLINE));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.UPDATE));
        }),
        /** Validates that the response does contain actions to set the users status */
        ACTIONS((Analysis t) -> {
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.ACCEPT) || t.getActions().contains(ActionsEnum.ACCEPT_AND_IGNORE_CONFLICTS));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.TENTATIVE));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.DECLINE));

            assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.UPDATE));
        }),
        /** Validates that the response does contain the UPDATE action */
        UPDATE((Analysis t) -> {
            assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.ACCEPT) || t.getActions().contains(ActionsEnum.ACCEPT_AND_IGNORE_CONFLICTS));
            assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.TENTATIVE));
            assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.DECLINE));

            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.UPDATE));
        }),
        /** Validates that the response doesn't contain any action */
        EMPTY((Analysis t) -> {
            assertTrue("There should be no action!", t.getActions().isEmpty());
        }),
        /** Validates that the response does contain the party crasher action */
        PARTY_CRASHER((Analysis t) -> {
            assertTrue("There should be onyl one action!", t.getActions().size() == 1);
            assertTrue("Unwanted action!", t.getActions().contains(ActionsEnum.ACCEPT_PARTY_CRASHER));
        }),
        /** Validates that the response does contain the cancel action */
        CANCEL((Analysis t) -> {
            assertTrue("There should be only one action!", t.getActions().size() == 1);
            assertTrue("Unwanted action!", t.getActions().contains(ActionsEnum.DELETE));
        }),
        /** Validates that the response does contain the cancel action */
        IGNORE((Analysis t) -> {
            assertTrue("There should be only one action!", t.getActions().size() == 1);
            assertTrue("Unwanted action!", t.getActions().contains(ActionsEnum.IGNORE));
        }),
        ;

        private Consumer<Analysis> consumer;

        CustomConsumers(Consumer<Analysis> consumer) {
            this.consumer = consumer;
        }

        public Consumer<Analysis> getConsumer() {
            return consumer;
        }

    }
}
