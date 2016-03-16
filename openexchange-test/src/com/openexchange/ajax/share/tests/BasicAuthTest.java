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

package com.openexchange.ajax.share.tests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link BasicAuthTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class BasicAuthTest extends ShareTest {

    private CalendarTestManager calendarManager;
    private FolderObject folder;

    /**
     * Initializes a new {@link BasicAuthTest}.
     *
     * @param name The test name
     */
    public BasicAuthTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        calendarManager = new CalendarTestManager(client);
    }

    @Override
    protected void tearDown() throws Exception {
        CalendarTestManager calendarManager = this.calendarManager;
        if (null != calendarManager) {
            calendarManager.cleanUp();
            this.calendarManager = null;
        }

        if (null != folder) {
            client.execute(new DeleteRequest(EnumAPI.OX_OLD, false, folder).setFailOnErrorParam(Boolean.FALSE));
            folder = null;
        }

        super.tearDown();
    }

    public void testBasicAuth() throws Exception {
        EnumAPI api = EnumAPI.OUTLOOK;
        int module = FolderObject.CALENDAR;
        OCLGuestPermission guestPermission = createNamedGuestPermission("horst@example.com", "Horst Example", "secret");

        // Create a calendar folder having a share permission
        FolderObject folder = insertSharedFolder(api, module, getDefaultFolder(module), guestPermission);
        this.folder = folder;

        // Check permissions
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);

        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);

        // Create some appointments for that folder
        {
            int n = 1;
            {
                Appointment appointment = new Appointment();
                appointment.setStartDate(D("11.11.2014 08:00"));
                appointment.setEndDate(D("11.11.2014 09:00"));
                appointment.setTitle("Appointment #" + Integer.toString(n++));
                appointment.setParentFolderID(folder.getObjectID());
                appointment.setIgnoreConflicts(true);

                UserParticipant user = new UserParticipant(client.getValues().getUserId());
                user.setConfirm(Appointment.NONE);
                UserParticipant guestParticipant = new UserParticipant(guestPermission.getEntity());
                user.setConfirm(Appointment.NONE);

                appointment.setParticipants(new Participant[] { user, guestParticipant });
                appointment.setUsers(new UserParticipant[] { user, guestParticipant });

                calendarManager.insert(appointment);
            }

            {
                Appointment appointment = new Appointment();
                appointment.setStartDate(D("11.11.2014 10:00"));
                appointment.setEndDate(D("11.11.2014 11:00"));
                appointment.setTitle("Appointment #" + Integer.toString(n++));
                appointment.setParentFolderID(folder.getObjectID());
                appointment.setIgnoreConflicts(true);

                UserParticipant user = new UserParticipant(client.getValues().getUserId());
                user.setConfirm(Appointment.NONE);
                UserParticipant guestParticipant = new UserParticipant(guestPermission.getEntity());
                user.setConfirm(Appointment.NONE);

                appointment.setParticipants(new Participant[] { user, guestParticipant });
                appointment.setUsers(new UserParticipant[] { user, guestParticipant });

                calendarManager.insert(appointment);
            }

            {
                Appointment appointment = new Appointment();
                appointment.setStartDate(D("12.11.2014 08:00"));
                appointment.setEndDate(D("12.11.2014 09:00"));
                appointment.setTitle("Appointment #" + Integer.toString(n++));
                appointment.setParentFolderID(folder.getObjectID());
                appointment.setIgnoreConflicts(true);

                UserParticipant user = new UserParticipant(client.getValues().getUserId());
                user.setConfirm(Appointment.NONE);
                UserParticipant guestParticipant = new UserParticipant(guestPermission.getEntity());
                user.setConfirm(Appointment.NONE);

                appointment.setParticipants(new Participant[] { user, guestParticipant });
                appointment.setUsers(new UserParticipant[] { user, guestParticipant });

                calendarManager.insert(appointment);
            }

            {
                Appointment appointment = new Appointment();
                appointment.setStartDate(D("12.11.2014 10:00"));
                appointment.setEndDate(D("12.11.2014 11:00"));
                appointment.setTitle("Appointment #" + Integer.toString(n++));
                appointment.setParentFolderID(folder.getObjectID());
                appointment.setIgnoreConflicts(true);

                UserParticipant user = new UserParticipant(client.getValues().getUserId());
                user.setConfirm(Appointment.NONE);
                UserParticipant guestParticipant = new UserParticipant(guestPermission.getEntity());
                user.setConfirm(Appointment.NONE);

                appointment.setParticipants(new Participant[] { user, guestParticipant });
                appointment.setUsers(new UserParticipant[] { user, guestParticipant });

                calendarManager.insert(appointment);
            }
        }

        // Check access to share (via guest client)
        String shareURL = discoverShareURL(guest);
        GuestClient guestClient =  resolveShare(shareURL, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);

        // Access that folder through ICal
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpParams httpParams = httpClient.getParams();
        httpParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        httpParams.setParameter("Content-Disposition", "attachment");
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

        String password = ShareTest.getPassword(guestPermission.getRecipient());
        if (false == Strings.isEmpty(password)) {
            String username = ShareTest.getUsername(guestPermission.getRecipient());
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials anonymousCredentials = new UsernamePasswordCredentials(username, password);
            credentialsProvider.setCredentials(org.apache.http.auth.AuthScope.ANY, anonymousCredentials);
            httpClient.setCredentialsProvider(credentialsProvider);
        }

        HttpGet httpGet = new HttpGet(shareURL);
        httpGet.setHeader("Accept", "text/calendar");
        httpGet.setHeader("User-Agent", "Microsoft Outlook");

        HttpResponse httpResponse = httpClient.execute(httpGet);
        assertEquals("Wrong HTTP status", 200, httpResponse.getStatusLine().getStatusCode());

        Header contentTypeHeader = httpResponse.getFirstHeader("Content-Type");
        assertNotNull("missing content-type header", contentTypeHeader);
        assertEquals("Unexpected Content-Type header", "text/calendar", contentTypeHeader.getValue());

        HttpEntity entity = httpResponse.getEntity();
        assertNotNull("No file downloaded", entity);
        byte[] downloadedFile = EntityUtils.toByteArray(entity);
        Assert.assertNotNull(downloadedFile);

        String ical = new String(downloadedFile, "UTF-8");
        assertTrue("Received content seems not be an ICal: " + ical, ical.startsWith("BEGIN:VCALENDAR"));
    }

}
