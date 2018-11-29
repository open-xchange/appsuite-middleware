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

package com.openexchange.chronos.itip.analyzers;

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import java.util.Collections;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.ChronosTestTools;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipMockFactory;
import com.openexchange.chronos.itip.generators.HTMLWrapper;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.MockUser;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link UpdateITipAnalyzerTest2} - Test the analyze call
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, CalendarUtils.class })
public class UpdateITipAnalyzerTest2 {

    /**
     * Shift date two hours in the future
     */
    private static final Duration DURATION = new Duration(1, 0, 2, 0, 0);

    private final static int CONTEXT_ID = 1337;

    @InjectMocks
    private UpdateITipAnalyzer updateITipAnalyzer;

    private MockUser user;

    private Context context;

    private Event original;

    private Event update;

    private HTMLWrapper wrapper;

    ITipMessage message;

    @Mock
    private UserService userService;

    @Mock
    private ContextService contextService;

    private CalendarSession session;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        original = ChronosTestTools.createEvent(CONTEXT_ID, null);
        update = EventMapper.getInstance().copy(original, new Event(), (EventField[]) null);
        wrapper = new HTMLWrapper();

        ITipMockFactory.injectUtil(updateITipAnalyzer, ITipMockFactory.mockUtil(original));

        user = ChronosTestTools.convertToUser(original.getCreatedBy());
        context = ITipMockFactory.getContext(CONTEXT_ID);

        ServerSession serverSession = ITipMockFactory.getServerSession(context, CONTEXT_ID, user, user.getId());
        CalendarUtilities u = ITipMockFactory.mockUtilities();
        session = ITipMockFactory.mockCalendarSession(CONTEXT_ID, user.getId(), serverSession, u);

        // Mock used service classes
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(UserService.class)).thenReturn(this.userService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);

        // Mock settings
        PowerMockito.when(contextService.getContext(ArgumentMatchers.anyInt())).thenReturn(context);
        PowerMockito.when(userService.getUser(ArgumentMatchers.anyInt(), ArgumentMatchers.any())).thenReturn(user);

    }

    /*
     * ------------------------------ REQUEST TESTS ------------------------------
     */
    // <-- Tests for bug 57883
    @Test
    public void testAnalyze_request_DifferentTimeZones_Start() throws Exception {
        message = ITipMockFactory.mockITipMessage(ITipMethod.REQUEST, update);

        long timestamp = System.currentTimeMillis();
        DateTime o = generateDateTime(timestamp);
        DateTime u = generateDateTime(timestamp, "Europe/Berlin");

        original.setStartDate(o);
        update.setStartDate(u);

        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Timezone wasn't changed", newEvent.getStartDate(), is(u));
        Assert.assertThat("Timezone was changed", newEvent.getEndDate(), is(original.getEndDate()));

        Assert.assertThat("Diff description contains more elements then expected", I(analyze.getChanges().get(0).getDiffDescription().size()), is(I(1)));
    }

    @Test
    public void testAnalyze_request_DifferentTimeZones_End() throws Exception {
        message = ITipMockFactory.mockITipMessage(ITipMethod.REQUEST, update);

        long timestamp = System.currentTimeMillis();
        DateTime o = generateDateTime(timestamp);
        DateTime u = generateDateTime(timestamp, "Europe/Berlin");

        original.setEndDate(o);
        update.setEndDate(u);

        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Timezone wasn't changed", newEvent.getEndDate(), is(u));
        Assert.assertThat("Timezone was changed", newEvent.getStartDate(), is(original.getStartDate()));

        Assert.assertThat("Diff description contains more elements then expected", I(analyze.getChanges().get(0).getDiffDescription().size()), is(I(1)));
    }

    @Test
    public void testAnalyze_request_DifferentTimeZones_Both() throws Exception {
        message = ITipMockFactory.mockITipMessage(ITipMethod.REQUEST, update);

        long timestamp = System.currentTimeMillis();
        DateTime o = generateDateTime(timestamp);
        DateTime u = generateDateTime(timestamp, "Europe/Berlin");

        original.setStartDate(o);
        original.setEndDate(generateDateTime(timestamp + 10000));

        update.setStartDate(u);
        update.setEndDate(generateDateTime(timestamp + 10000, "Europe/Berlin"));

        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Timezone wasn't changed", newEvent.getStartDate(), is(u));
        Assert.assertThat("Timezone wasn't changed", newEvent.getEndDate(), is(update.getEndDate()));

        Assert.assertThat("Diff description contains more elements then expected", I(analyze.getChanges().get(0).getDiffDescription().size()), is(I(1)));
    }

    @Test
    public void testAnalyze_request_DifferentTimeZones_BothDifferent() throws Exception {
        message = ITipMockFactory.mockITipMessage(ITipMethod.REQUEST, update);

        long timestamp = System.currentTimeMillis();
        DateTime o = generateDateTime(timestamp);
        DateTime u = generateDateTime(timestamp, "Europe/Berlin");

        original.setStartDate(o);
        original.setEndDate(generateDateTime(timestamp + 10000));

        update.setStartDate(u);
        update.setEndDate(generateDateTime(timestamp + 10000, "Asia/Bangkok"));

        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Timezone wasn't changed", newEvent.getStartDate(), is(u));
        Assert.assertThat("Timezone wasn't changed", newEvent.getEndDate(), is(update.getEndDate()));

        Assert.assertThat("Diff description contains more elements then expected", I(analyze.getChanges().get(0).getDiffDescription().size()), is(I(2)));
    }

    @Test
    public void testAnalyze_request_DifferentTimeZones_AndDIfferentTime() throws Exception {
        message = ITipMockFactory.mockITipMessage(ITipMethod.REQUEST, update);

        long timestamp = System.currentTimeMillis();
        DateTime o = generateDateTime(timestamp);
        DateTime u = generateDateTime(timestamp + 20000, "Europe/Berlin");

        original.setStartDate(o);
        original.setEndDate(generateDateTime(timestamp + 10000));

        update.setStartDate(u);
        update.setEndDate(generateDateTime(timestamp + 20000, "Asia/Bangkok"));

        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Timezone wasn't changed", newEvent.getStartDate(), is(u));
        Assert.assertThat("Timezone wasn't changed", newEvent.getEndDate(), is(update.getEndDate()));

        Assert.assertThat("Diff description contains more elements then expected", I(analyze.getChanges().get(0).getDiffDescription().size()), is(I(3)));
    }
    // End tests for 57883 -->

    /*
     * ------------------------------ COUNTER TESTS ------------------------------
     */
    // Microsoft counter tests

    @Test
    public void testAnalyze_Microsoft_ChangedStartDate() throws Exception {
        prepareMicrosoftCounter();
        update.setStartDate(update.getStartDate().addDuration(DURATION));
        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("End date was updated!", newEvent.getEndDate(), is(original.getEndDate()));
        Assert.assertThat("Start date wasn't updated!", newEvent.getStartDate(), is(update.getStartDate()));
        Assert.assertThat("Start date wasn't updated!", newEvent.getStartDate(), not(original.getStartDate()));
    }

    @Test
    public void testAnalyze_Microsoft_ChangedEndtDate() throws Exception {
        prepareMicrosoftCounter();

        update.setEndDate(update.getEndDate().addDuration(DURATION));
        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Start date was updated!", newEvent.getStartDate(), is(original.getStartDate()));
        Assert.assertThat("End date wasn't updated!", newEvent.getEndDate(), is(update.getEndDate()));
        Assert.assertThat("End date wasn't updated!", newEvent.getEndDate(), not(original.getEndDate()));
    }

    @Test
    public void testAnalyze_Microsoft_ChangedStartEndDate() throws Exception {
        prepareMicrosoftCounter();
        update.setStartDate(update.getStartDate().addDuration(DURATION));
        update.setEndDate(update.getEndDate().addDuration(DURATION));
        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Start date wasn't updated!", newEvent.getStartDate(), is(update.getStartDate()));
        Assert.assertThat("Start date wasn't updated!", newEvent.getStartDate(), not(original.getStartDate()));
        Assert.assertThat("Start date wasn't updated!", newEvent.getEndDate(), is(update.getEndDate()));
        Assert.assertThat("Start date wasn't updated!", newEvent.getEndDate(), not(original.getEndDate()));
    }

    @Test
    public void testAnalyze_Microsoft_NotChangedLocation() throws Exception {
        prepareMicrosoftCounter();
        String location = "Germany/Olpe";
        update.setLocation(location);
        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Location shouldn't have changed", newEvent.getLocation(), not(location));
        Assert.assertThat("Location isn't the same location anymore.", newEvent.getLocation(), is(original.getLocation()));
    }

    @Test
    public void testAnalyze_Microsoft_AttendeeAccepted() throws Exception {
        testParticipantChange(ParticipationStatus.ACCEPTED);
    }

    @Test
    public void testAnalyze_Microsoft_AttendeeTentatived() throws Exception {
        testParticipantChange(ParticipationStatus.TENTATIVE);
    }

    @Test
    public void testAnalyze_Microsoft_AttendeeDeclined() throws Exception {
        testParticipantChange(ParticipationStatus.DECLINED);
    }

    private void testParticipantChange(ParticipationStatus status) throws Exception {
        prepareMicrosoftCounter();

        Attendee attendee = update.getAttendees().get(0);
        attendee.setPartStat(status);
        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Participant status wasn't changed", newEvent.getAttendees().stream().filter(a -> attendee.getEntity() == a.getEntity()).findFirst().get().getPartStat(), is(status));
    }

    @Test
    public void testAnalyze_Microsoft_MoreChanged() throws Exception {
        /*
         * Add more attendees to simulate a fixed Microsoft iTip implementation
         * that can change ever field on a counter.
         */
        prepareMicrosoftCounter();

        update.setAttendees(original.getAttendees());

        String location = "Germany/Olpe";
        update.setLocation(location);

        String summary = "New summary";
        update.setSummary(summary);

        ITipAnalysis analyze = updateITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);

        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertThat("Location should have changed", newEvent.getLocation(), is(location));
        Assert.assertThat("Location hasn't changed.", newEvent.getLocation(), not(original.getLocation()));

        Assert.assertThat("Summary should have changed", newEvent.getSummary(), is(summary));
        Assert.assertThat("Summary hasn't changed.", newEvent.getSummary(), not(original.getSummary()));
    }

    /*
     * ------------------------------ PUBLISH TESTS ------------------------------
     */
    //    @Test
    //    public void testAnalyze_Foo_Bar() throws Exception {
    //        
    //    }

    /*
     * --------------------------------- HELPERS ---------------------------------
     */

    private void prepareMicrosoftCounter() throws OXException {
        Attendee attendee = getAttendee();
        attendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
        update.setAttendees(Collections.singletonList(copy(attendee)));
        message = ITipMockFactory.mockITipMessage(ITipMethod.COUNTER, update, true);
    }

    private Attendee getAttendee() {
        return original.getAttendees().get(0).getEntity() != user.getId() ? original.getAttendees().get(0) : original.getAttendees().get(1);
    }

    private Attendee copy(Attendee original) throws OXException {
        return AttendeeMapper.getInstance().copy(original, new Attendee(), (AttendeeField[]) null);
    }

    private DateTime generateDateTime(long timestamp) {
        String id = "UTC";
        return generateDateTime(timestamp, id);
    }

    private DateTime generateDateTime(long timestamp, String id) {
        return new DateTime(TimeZone.getTimeZone(id), timestamp);
    }

}
