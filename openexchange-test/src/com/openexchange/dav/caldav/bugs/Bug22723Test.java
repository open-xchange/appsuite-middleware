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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.*;
import java.util.Date;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug22723Test} - appointments deleted in webgui do not disappear in Lightning
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug22723Test extends CalDAVTest {

	@Test
	public void testSynchronizeDeletion() throws Exception {
	    /*
	     * create a new folder on the server
	     */
	    String subFolderName = "testfolder_" + randomUID();
	    FolderObject subFolder = super.createFolder(subFolderName);
	    super.rememberForCleanUp(subFolder);
	    String subFolderID = Integer.toString(subFolder.getObjectID());
        /*
         * fetch initial sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken(subFolderID));
        /*
         * create appointment on client
         */
        String uid = randomUID();
        String summary = "hello";
        String location = "here";
        Date start = TimeTools.D("tomorrow at 6am");
        Date end = TimeTools.D("tomorrow at 8am");
        String iCal = generateICal(start, end, uid, summary, location);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(subFolderID, uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(subFolderID, uid);
        super.rememberForCleanUp(appointment);
        assertAppointmentEquals(appointment, start, end, uid, summary, location);
        /*
         * delete appointment on server
         */
        super.delete(appointment);
        /*
         * verify deletion on server
         */
        assertNull("Appointment not deleted on server", super.getAppointment(subFolderID, uid));
        /*
         * verify deletion on client
         */
        SyncCollectionResponse syncCollectionResponse = super.syncCollection(syncToken, subFolderID);
        assertTrue("no resource deletions reported on sync collection", 0 < syncCollectionResponse.getHrefsStatusNotFound().size());
        boolean found = false;
        for (String href : syncCollectionResponse.getHrefsStatusNotFound()) {
            if (null != href && href.contains(uid)) {
                found = true;
                break;
            }
        }
        assertTrue("appointment not reported as deleted", found);
	}

}
