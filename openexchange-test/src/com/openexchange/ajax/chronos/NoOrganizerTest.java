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

import static com.openexchange.ajax.chronos.manager.CalendarFolderManager.CALENDAR_MODULE;
import static com.openexchange.ajax.chronos.manager.CalendarFolderManager.TREE_ID;
import static com.openexchange.java.Autoboxing.I;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.CalendarFolderManager;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link NoOrganizerTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class NoOrganizerTest extends AbstractChronosTest {

    private EventManager publicEventManager;

    private String publicFolderId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        List<FolderPermission> permissions = new ArrayList<>();
        FolderPermission perm = new FolderPermission();
        perm.setEntity(I(getCalendaruser()));
        perm.setGroup(Boolean.FALSE);
        perm.setBits(I(403710016));
        permissions.add(perm);
        // All users and groups
        perm = new FolderPermission();
        perm.setEntity(I(0));
        perm.setGroup(Boolean.TRUE);
        perm.setBits(I(4227332));
        permissions.add(perm);

        folder.setPermissions(permissions);
        folder.setModule("event");
        folder.setTitle("Public folder from " + this.getClass().getSimpleName());
        folder.setSubscribed(Boolean.TRUE);
        body.setFolder(folder);
        FolderUpdateResponse response = foldersApi.createFolder("2", defaultUserApi.getSession(), body, TREE_ID, CalendarFolderManager.MODULE, null);
        assertNotNull(response);
        assertNull(response.getError(), response.getErrorDesc());
        publicFolderId = response.getData();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            publicEventManager.cleanUp();
            if (null != publicFolderId) {
                foldersApi.deleteFolders(defaultUserApi.getSession(), singletonList(publicFolderId), TREE_ID, null, CALENDAR_MODULE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);
            }
        } finally {
            super.tearDown();

        }
    }

    @Test
    public void testCrreateEventWithoutOrganizerAttending() throws Exception {
        /*
         * Prepare event, organizer not attending in event
         */
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        DateTimeData startDate = DateTimeUtil.getDateTime("UTC", cal.getTimeInMillis() + TimeUnit.DAYS.toMillis(1));
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, (cal.getTimeInMillis() + TimeUnit.HOURS.toMillis(2)));

        EventData eventData = EventFactory.createSingleEvent(getCalendaruser(), "No oragnizer", startDate, endDate, publicFolderId);
        eventData.setOrganizer(null);
        int userId = getClient().getValues().getUserId();
        UserResponse userResponse = new UserApi(getApiClient()).getUser(getApiClient().getSession(), String.valueOf(userId));
        CalendarUser c = new CalendarUser();
        c.uri("mailto:" + userResponse.getData().getEmail1());
        c.cn(userResponse.getData().getDisplayName());
        c.email(userResponse.getData().getEmail1());
        c.entity(Integer.valueOf(userResponse.getData().getId()));
        eventData.setCalendarUser(c);
        int secondUserId = getClient2().getValues().getUserId();
        eventData.setAttendees(singletonList(AttendeeFactory.createIndividual(I(secondUserId))));

        publicEventManager = new EventManager(defaultUserApi, publicFolderId);
        EventData createEvent = publicEventManager.createEvent(eventData, true);
        Assert.assertFalse("Organizer should not be in the event", createEvent.getAttendees().stream().filter(a -> null != a.getEntity() && a.getEntity().intValue() == userId).findAny().isPresent());

    }

}
