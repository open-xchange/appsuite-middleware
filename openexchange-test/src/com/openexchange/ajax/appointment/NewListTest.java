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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.appointment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AllResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.InsertResponse;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.UserParticipant;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class NewListTest extends AbstractAJAXSession {

    private static final int NUMBER = 10;

    private static final int DELETES = 2;

    /**
     * Default constructor.
     */
    public NewListTest(final String name) {
        super(name);
    }


    /**
     * This method tests the new handling of not more available objects for LIST
     * requests.
     */
    public void testRemovedObjectHandling() throws Throwable {
        final AJAXClient clientA = getClient();
        final int folderA = clientA.getValues().getPrivateAppointmentFolder();

        // Create some tasks.
        final Date appStart = new Date(Tools.getHour(0));
        final Date appEnd = new Date(Tools.getHour(1));
        final Date listStart = new Date(Tools.getHour(-1));
        final Date listEnd = new Date(Tools.getHour(2));
        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {
            final AppointmentObject app = new AppointmentObject();
            app.setTitle("New List Test " + (i + 1));
            app.setParentFolderID(folderA);
            app.setStartDate(appStart);
            app.setEndDate(appEnd);
            app.setIgnoreConflicts(true);
            app.addParticipant(new UserParticipant(clientA.getValues().getUserId()));
            inserts[i] = new InsertRequest(app, clientA.getValues().getTimeZone());
        }
        final MultipleResponse mInsert = (MultipleResponse) Executor.execute(
            getClient(), new MultipleRequest(inserts));
        final List<InsertResponse> toDelete = new ArrayList<InsertResponse>(NUMBER);
        final Iterator<AbstractAJAXResponse> iter = mInsert.iterator();
        while (iter.hasNext()) {
            toDelete.add((InsertResponse) iter.next());
        }

        // A now gets all of the folder.
        int[] columns = new int[] { AppointmentObject.TITLE, AppointmentObject
            .OBJECT_ID, AppointmentObject.FOLDER_ID };
        final AllResponse allR = (AllResponse) Executor.execute(clientA,
            new AllRequest(folderA, columns, listStart, listEnd));
        
        // Now B deletes some of them.
        final DeleteRequest[] deletes1 = new DeleteRequest[DELETES];
        for (int i = 0; i < deletes1.length; i++) {
            final InsertResponse insertR = toDelete.remove((NUMBER - DELETES)/2 + i); 
            deletes1[i] = new DeleteRequest(insertR.getId(), folderA, allR
                .getTimestamp());
        }
        Executor.multiple(clientA, new MultipleRequest(deletes1));

        // List request of A must now not contain the deleted objects and give
        // no error.
        final CommonListResponse listR = (CommonListResponse) Executor.execute(
            clientA, new ListRequest(allR.getListIDs(), columns, true));
        
        final DeleteRequest[] deletes2 = new DeleteRequest[toDelete.size()];
        for (int i = 0; i < deletes2.length; i++) {
            final InsertResponse insertR = toDelete.get(i);
            deletes2[i] = new DeleteRequest(insertR.getId(), folderA,
            listR.getTimestamp());
        }
        Executor.multiple(getClient(), new MultipleRequest(deletes2)); 
    }
}
