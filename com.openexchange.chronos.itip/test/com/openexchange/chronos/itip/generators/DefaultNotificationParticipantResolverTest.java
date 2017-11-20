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

package com.openexchange.chronos.itip.generators;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.ChronosTestTools;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.MockUser;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link DefaultNotificationParticipantResolverTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, UserSettingMailStorage.class, CalendarUtils.class })
public class DefaultNotificationParticipantResolverTest {

    private static final int CONTEXT_ID = 999999;

    // -------- MOCK OBJECTS --------
    @Mock
    private ServiceLookup services;

    @Mock
    private ITipIntegrationUtility util;

    @Mock
    private UserService userService;

    @Mock
    private ConfigurationService config;

    @Mock
    private ResourceService resources;

    @Mock
    private UserSettingMailStorage userSettingMailStorage;

    @Mock
    private CalendarSession session;

    // --------- PRIVATE OBJECTS ------------
    private DefaultNotificationParticipantResolver resolver;

    private final Context context = new ContextImpl(CONTEXT_ID);

    @Before
    public void setUp() throws Exception {
        // Init
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.mockStatic(UserSettingMailStorage.class);

        // Mock used service classes
        PowerMockito.when(Services.getService(UserService.class)).thenReturn(this.userService);
        PowerMockito.when(Services.getService(ResourceService.class)).thenReturn(this.resources);
        PowerMockito.when(Services.getService(ConfigurationService.class)).thenReturn(this.config);

        // Mock settings
        UserSettingMailStorage userSettingMailStorage = Mockito.mock(UserSettingMailStorage.class);
        PowerMockito.when(UserSettingMailStorage.getInstance()).thenReturn(userSettingMailStorage);
        PowerMockito.when(userSettingMailStorage.getUserSettingMail(Matchers.anyInt(), Matchers.eq(Matchers.eq(context)))).thenReturn(null);

        // Mock util
        PowerMockito.when(util.getFolderIdForUser(Matchers.any(CalendarSession.class), Matchers.anyString())).thenReturn(null);

        // Mock session
        PowerMockito.when(Boolean.valueOf(session.contains(Matchers.anyString()))).thenReturn(Boolean.FALSE);

        // Create service to test
        resolver = new DefaultNotificationParticipantResolver(util);
    }

    private void prepareServices(Event updated, User onBehalfOf) throws OXException {
        List<MockUser> attendeesAsUser = ChronosTestTools.convertToUser(updated.getAttendees());
        PowerMockito.when(userService.getUser(updated.getCreatedBy().getEntity(), context)).thenReturn(onBehalfOf);
        PowerMockito.when(userService.getUser(Matchers.any(Context.class), (int[]) Matchers.any())).thenReturn(attendeesAsUser.toArray(new MockUser[] {}));
    }

    @Test
    public void testResolveAllRecipients_OnlyInternalAttendees_AllGood() throws OXException {

        // Setup test data
        Event updated = ChronosTestTools.createEvent(CONTEXT_ID, null);
        User user;
        User onBehalfOf = user = ChronosTestTools.convertToUser(updated.getCreatedBy());

        prepareServices(updated, onBehalfOf);

        List<NotificationParticipant> participants = resolver.resolveAllRecipients(null, updated, user, onBehalfOf, context, session);
        Assert.assertFalse("No participants resolved", participants.isEmpty());
        for (NotificationParticipant participant : participants) {
            Attendee attendee = updated.getAttendees().stream().filter(a -> a.getEntity() == participant.getIdentifier()).findFirst().get();
            Assert.assertThat("Participants mail should have been the alias", participant.getEmail(), is(attendee.getEMail()));
            Assert.assertThat("Participants comment should have been the same as the attendees", participant.getComment(), is(attendee.getComment()));
            Assert.assertThat("Participants status should be 'accepted'", participant.getConfirmStatus(), is(ParticipationStatus.ACCEPTED));
        }
    }

}
