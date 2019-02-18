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

import static org.hamcrest.Matchers.is;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipMockFactory;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.HTMLWrapper;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.MockUser;
import com.openexchange.junit.Assert;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link ReplyITipAnalyzerTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, CalendarUtils.class })
public class ReplyITipAnalyzerTest {

    private final static int CONTEXT_ID = 42;

    private MockUser user;

    private Context context;

    private Event original;

    private Event update;

    private HTMLWrapper wrapper;

    ITipMessage message;

    @InjectMocks
    private ReplyITipAnalyzer replyITipAnalyzer;

    @Mock
    private UserService userService;

    @Mock
    private ContextService contextService;

    private CalendarSession session;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        original = ChronosTestTools.createEvent(CONTEXT_ID, (EventField[]) null);
        update = EventMapper.getInstance().copy(original, new Event(), (EventField[]) null);
        wrapper = new HTMLWrapper();

        message = ITipMockFactory.mockITipMessage(ITipMethod.REPLY, update);

        ITipMockFactory.injectUtil(replyITipAnalyzer, ITipMockFactory.mockUtil(original));

        user = ChronosTestTools.convertToUser(original.getCreatedBy());
        context = ITipMockFactory.getContext(CONTEXT_ID);

        ServerSession serverSession = ITipMockFactory.getServerSession(context, CONTEXT_ID, user, user.getId());
        CalendarUtilities u = ITipMockFactory.mockUtilities();
        session = ITipMockFactory.mockCalendarSession(CONTEXT_ID, user.getId(), serverSession, u);

        // Mock used service classes
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(UserService.class)).thenReturn(this.userService);
        PowerMockito.when(Services.getService(UserService.class, true)).thenReturn(this.userService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);
        PowerMockito.when(Services.getService(ContextService.class, true)).thenReturn(this.contextService);

        // Mock settings
        PowerMockito.when(contextService.getContext(ArgumentMatchers.anyInt())).thenReturn(context);
        PowerMockito.when(userService.getUser(ArgumentMatchers.anyInt(), ArgumentMatchers.any())).thenReturn(user);
    }

    @Test(expected = OXException.class)
    public void testAnalyze_ReplyWithMoreThanOneAttendee_ThrowsException() throws Exception {
        // update contains 3 attendees, so error should be thrown
        replyITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);
    }

    @Test
    public void testAnalyze_WrongMethodUsed_EmptyAnalysis() throws Exception {
        Mockito.when(message.getMethod()).thenReturn(ITipMethod.COUNTER);
        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);
        Assert.assertThat("Should only be one", Integer.valueOf(analyze.getAnnotations().size()), is(Integer.valueOf(1)));
        Assert.assertThat("Should be none", analyze.getAnnotations().get(0).getMessage(), is(Messages.NONE));
    }

    @Test
    public void testAnalyze_PartyCrasherReply_AcceptPartyCrasher() throws Exception {
        Attendee crasher = ChronosTestTools.createAttendee(CONTEXT_ID, (AttendeeField[]) null);
        update.setAttendees(Collections.singletonList(crasher));
        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);
        Assert.assertThat("Party crasher should have been found", analyze.getActions().toArray()[0], is(ITipAction.ACCEPT_PARTY_CRASHER));
        Assert.assertTrue("Party crasher should have been added", analyze.getChanges().get(0).getNewEvent().getAttendees().contains(crasher));
    }

    @Test
    public void testAnalyze_ConformITipReply_AllGood() throws Exception {
        Attendee copy = getAttendeeCopy();
        copy.setPartStat(ParticipationStatus.DECLINED);
        update.setAttendees(Collections.singletonList(copy));

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);
        Assert.assertThat("Should have been UPDATE action", analyze.getActions().toArray()[0], is(ITipAction.UPDATE));
        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Optional<Attendee> processed = newEvent.getAttendees().stream().filter(a -> copy.getEntity() == a.getEntity()).findAny();
        Assert.assertTrue("REPLY attendee is missing.", processed.isPresent());
        Assert.assertThat("Status not changed", processed.get().getPartStat(), is(ParticipationStatus.DECLINED));
    }

    @Test
    public void testAnalyze_NothingUpdated_NoAction() throws Exception {
        Attendee a = getAttendee();
        update.setAttendees(Collections.singletonList(a));

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);
        Assert.assertTrue("Nothing was changed, so there should be no action.", analyze.getActions().isEmpty());
    }

    /**
     * Test for bug 57733
     * 
     * @throws Exception If test fails
     */
    @Test
    public void testAnalyze_UpperCaseMailto_NoPartyCrasherAdded() throws Exception {
        Attendee externalAttendee = ChronosTestTools.createExternalAttendee(CONTEXT_ID % 13, (AttendeeField[]) null);
        original.getAttendees().add(externalAttendee);

        Attendee copy = copy(externalAttendee);
        String uri = externalAttendee.getUri();
        //convert 'mailto:' to 'MAILTO:'
        uri = uri.substring(7);
        uri = "MAILTO:" + uri;
        copy.setUri(uri);
        copy.setPartStat(ParticipationStatus.TENTATIVE);
        update.setAttendees(Collections.singletonList(copy));

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);
        Assert.assertThat("Should have been UPDATE action", analyze.getActions().toArray()[0], is(ITipAction.UPDATE));
        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Assert.assertTrue("REPLY attendee is missing.", newEvent.getAttendees().stream().filter(a -> copy.getEntity() == a.getEntity()).findAny().isPresent());
        Assert.assertThat("Status not changed", Integer.valueOf(newEvent.getAttendees().size()), is(Integer.valueOf(original.getAttendees().size())));
    }

    /**
     * Test for bug 59647
     * 
     * @throws Exception If test fails
     */
    @Test
    public void testAnalyze_ReplyForNonOrganizer_IgnoreAction() throws Exception {
        Attendee copy = getAttendeeCopy();
        copy.setPartStat(ParticipationStatus.DECLINED);
        update.setAttendees(Collections.singletonList(copy));

        // Create event unknown user, that 'accidentally' gets the mail
        MockUser user = new MockUser(42);
        user.setMail("notOrganizer@example.org");
        user.setTimeZone("");
        user.setLocale(Locale.US);

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, null, wrapper, null, user, context, session);
        Assert.assertThat("Should have been IGNORE action", analyze.getActions().toArray()[0], is(ITipAction.IGNORE));
        Assert.assertTrue("There should be no diff displayed to the client!", analyze.getChanges().isEmpty());
    }

    private Attendee getAttendee() {
        return original.getAttendees().get(0).getEntity() != user.getId() ? original.getAttendees().get(0) : original.getAttendees().get(1);
    }

    private Attendee getAttendeeCopy() throws OXException {
        return copy(getAttendee());
    }

    private Attendee copy(Attendee original) throws OXException {
        return AttendeeMapper.getInstance().copy(original, new Attendee(), (AttendeeField[]) null);
    }

}
