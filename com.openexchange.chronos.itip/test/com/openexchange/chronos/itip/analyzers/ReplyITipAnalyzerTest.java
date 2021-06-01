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

package com.openexchange.chronos.itip.analyzers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
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
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link ReplyITipAnalyzerTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
{ Services.class, CalendarUtils.class })
public class ReplyITipAnalyzerTest {

    private final static int CONTEXT_ID = 42;

    private Map<String, String> headers;

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

    @Mock
    private RegionalSettingsService regionalSettingsService;

    private CalendarSession session;

    private Attendee replyingAttendee;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        replyingAttendee = ChronosTestTools.createAttendee(-1, (AttendeeField[]) null);
        replyingAttendee.setUri(CalendarUtils.getURI(replyingAttendee.getEMail()));
        original = ChronosTestTools.createEvent(CONTEXT_ID, (EventField[]) null);
        original.getAttendees().add(replyingAttendee);
        update = EventMapper.getInstance().copy(original, new Event(), (EventField[]) null);
        wrapper = new HTMLWrapper();

        message = ITipMockFactory.mockITipMessage(ITipMethod.REPLY, update);

        ITipMockFactory.injectUtil(replyITipAnalyzer, ITipMockFactory.mockUtil(original));

        user = ChronosTestTools.convertToUser(original.getCreatedBy());
        context = ITipMockFactory.getContext(CONTEXT_ID);
        headers = Collections.singletonMap("from", replyingAttendee.getEMail());

        ServerSession serverSession = ITipMockFactory.getServerSession(context, CONTEXT_ID, user, user.getId());
        CalendarUtilities u = ITipMockFactory.mockUtilities();
        session = ITipMockFactory.mockCalendarSession(CONTEXT_ID, user.getId(), serverSession, u);

        // Mock used service classes
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(UserService.class)).thenReturn(this.userService);
        PowerMockito.when(Services.getService(UserService.class, true)).thenReturn(this.userService);
        PowerMockito.when(Services.getService(ContextService.class)).thenReturn(this.contextService);
        PowerMockito.when(Services.getService(ContextService.class, true)).thenReturn(this.contextService);
        PowerMockito.when(Services.getService(RegionalSettingsService.class)).thenReturn(this.regionalSettingsService);

        // Mock settings
        PowerMockito.when(contextService.getContext(ArgumentMatchers.anyInt())).thenReturn(context);
        PowerMockito.when(userService.getUser(ArgumentMatchers.anyInt(), ArgumentMatchers.any())).thenReturn(user);
        PowerMockito.when(regionalSettingsService.get(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(null);
    }

    @Test(expected = OXException.class)
    public void testAnalyze_ReplyWithMoreThanOneAttendee_ThrowsException() throws Exception {
        // update contains 3 attendees, so error should be thrown
        replyITipAnalyzer.analyze(message, headers, wrapper, null, user, context, session);
    }

    @Test
    public void testAnalyze_WrongMethodUsed_EmptyAnalysis() throws Exception {
        Mockito.when(message.getMethod()).thenReturn(ITipMethod.COUNTER);
        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, headers, wrapper, null, user, context, session);
        Assert.assertThat("Should only be one", Integer.valueOf(analyze.getAnnotations().size()), is(Integer.valueOf(1)));
        Assert.assertThat("Should be none", analyze.getAnnotations().get(0).getMessage(), is(Messages.NONE));
    }

    @Test
    public void testAnalyze_PartyCrasherReply_AcceptPartyCrasher() throws Exception {
        Attendee crasher = ChronosTestTools.createAttendee(CONTEXT_ID, (AttendeeField[]) null);
        update.setAttendees(Collections.singletonList(crasher));
        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, headers, wrapper, null, user, context, session);
        Assert.assertThat("Party crasher should have been found", analyze.getActions().toArray()[0], is(ITipAction.ACCEPT_PARTY_CRASHER));
        Assert.assertTrue("Party crasher should have been added", analyze.getChanges().get(0).getNewEvent().getAttendees().contains(crasher));
    }

    @Test
    public void testAnalyze_ConformITipReply_AllGood() throws Exception {
        Attendee copy = getAttendeeCopy();
        copy.setPartStat(ParticipationStatus.DECLINED);
        update.setAttendees(Collections.singletonList(copy));

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, headers, wrapper, null, user, context, session);
        Assert.assertThat("Should have been UPDATE action", analyze.getActions().toArray()[0], is(ITipAction.UPDATE));
        Event newEvent = analyze.getChanges().get(0).getNewEvent();
        Optional<Attendee> processed = newEvent.getAttendees().stream().filter(a -> copy.getEntity() == a.getEntity()).findAny();
        Assert.assertTrue("REPLY attendee is missing.", processed.isPresent());
        Assert.assertThat("Status not changed", processed.get().getPartStat(), is(ParticipationStatus.DECLINED));
    }

    @Test
    public void testAnalyze_NothingUpdated_NoAction() throws Exception {
        update.setAttendees(Collections.singletonList(replyingAttendee));

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, headers, wrapper, null, user, context, session);
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

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, headers, wrapper, null, user, context, session);
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

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, headers, wrapper, null, user, context, session);
        Assert.assertThat("Should have been IGNORE action", analyze.getActions().toArray()[0], is(ITipAction.IGNORE));
        Assert.assertTrue("There should be no diff displayed to the client!", analyze.getChanges().isEmpty());
    }

    /**
     * Test for MWB-935
     *
     * @throws Exception If test fails
     */
    @Test
    public void testAnalyze_WrongSender_IgnoreActionAdded() throws Exception {
        Attendee copy = getAttendeeCopy();
        copy.setPartStat(ParticipationStatus.DECLINED);
        update.setAttendees(Collections.singletonList(copy));

        ITipAnalysis analyze = replyITipAnalyzer.analyze(message, Collections.singletonMap("from", "evil@example.org"), wrapper, null, user, context, session);
        Assert.assertTrue("Update action should still be advised", analyze.getActions().contains(ITipAction.UPDATE));
        // No IGNORE action implemented, UI doesn't hide the button. Don't send atm
        // Assert.assertTrue("Ignore action must be advised", analyze.getActions().contains(ITipAction.IGNORE));
        Assert.assertThat("There should be a hint for the user", analyze.getAnnotations(), is(notNullValue()));
        Assert.assertThat("Ignore not advised.", analyze.getAnnotations().get(0).getMessage(), containsString("Best ignore it"));
    }

    private Attendee getAttendeeCopy() throws OXException {
        return copy(replyingAttendee);
    }

    private Attendee copy(Attendee original) throws OXException {
        return AttendeeMapper.getInstance().copy(original, new Attendee(), (AttendeeField[]) null);
    }

}
