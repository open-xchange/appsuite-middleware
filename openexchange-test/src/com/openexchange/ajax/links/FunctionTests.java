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

package com.openexchange.ajax.links;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.links.actions.AllRequest;
import com.openexchange.ajax.links.actions.AllResponse;
import com.openexchange.ajax.links.actions.DeleteRequest;
import com.openexchange.ajax.links.actions.InsertRequest;
import com.openexchange.ajax.task.TaskTools;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FunctionTests extends AbstractAJAXSession {

    /**
     * @param name
     */
    public FunctionTests(final String name) {
        super(name);
    }

    /**
     * @throws Exception
     */
    public void testNew() throws Exception {
        final AJAXClient client = getClient();
        int fid1 = client.getValues().getPrivateContactFolder();
        int oid1;
        {
            ContactObject co = new ContactObject();
            co.setSurName("Meier");
            co.setGivenName("Herbert");
            co.setDisplayName("Meier, Herbert");
            co.setParentFolderID(fid1);
            final com.openexchange.ajax.contact.action.InsertResponse response =
                (InsertResponse) Executor.execute(client,
                new com.openexchange.ajax.contact.action.InsertRequest(co));
            oid1 = response.getId();
        }
        int fid2 = client.getValues().getPrivateAppointmentFolder();
        int oid2;
        {
            AppointmentObject ao = new AppointmentObject();
            ao.setTitle("Nasenmann");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 8);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.setTimeZone(TimeZone.getTimeZone("GMT"));

            long startTime = c.getTimeInMillis();
            long endTime = startTime + 3600000;

            ao.setStartDate(new Date(startTime));
            ao.setEndDate(new Date(endTime));
            ao.setLocation("Location");
            ao.setShownAs(AppointmentObject.ABSENT);
            ao.setParentFolderID(fid2);
            ao.setIgnoreConflicts(true);

            final TimeZone tz = client.getValues().getTimeZone();
            final com.openexchange.ajax.appointment.action.InsertResponse response =
                (com.openexchange.ajax.appointment.action.InsertResponse) Executor.execute(client,
            new com.openexchange.ajax.appointment.action.InsertRequest(ao, tz));
            oid2 = response.getId();
        }
        /*
         *  Now Build The Link Object
         * 
         */
        
        LinkObject lo = new LinkObject();
        lo.setFirstFolder(fid1);
        lo.setFirstId(oid1);
        lo.setFirstType(com.openexchange.groupware.Types.CONTACT);
        lo.setSecondFolder(fid2);
        lo.setSecondId(oid2);
        lo.setSecondType(com.openexchange.groupware.Types.APPOINTMENT);

        final InsertRequest request = new InsertRequest(lo);
        final CommonInsertResponse response = LinkTools.insert(client, request);
    }

    /**
     * Creates a private contact and a task and links them.
     * @throws Throwable if some exception occurs.
     */
    public void testContactAndTask() throws Throwable {
        final AJAXClient client = getClient();
        final int taskFolder = client.getValues().getPrivateTaskFolder();
        final int contactFolder = client.getValues().getPrivateContactFolder();
        final Task task = new Task();
        {
            task.setTitle("Link contact and task test.");
            task.setParentFolderID(taskFolder);
            final com.openexchange.ajax.task.actions.InsertResponse response =
                TaskTools.insert(client, new com.openexchange.ajax.task.actions
                .InsertRequest(task, client.getValues().getTimeZone()));
            task.setObjectID(response.getId());
            task.setLastModified(response.getTimestamp());
        }
        final ContactObject contact = new ContactObject();
        {
            contact.setDisplayName("Link contact and task test.");
            contact.setParentFolderID(contactFolder);
            final com.openexchange.ajax.contact.action.InsertResponse response =
                (com.openexchange.ajax.contact.action.InsertResponse) Executor
                .execute(client, new com.openexchange.ajax.contact.action
                .InsertRequest(contact));
            contact.setObjectID(response.getId());
            // We have to get the contact because the insert response does not
            // contain the timestamp.
            final com.openexchange.ajax.contact.action.GetResponse gResponse =
                (com.openexchange.ajax.contact.action.GetResponse) Executor
                .execute(client, new com.openexchange.ajax.contact.action
                .GetRequest(contactFolder, response));
            contact.setLastModified(gResponse.getTimestamp());
        }
        try {
            final LinkObject link = new LinkObject(contact.getObjectID(),
                Types.CONTACT, contact.getParentFolderID(), task.getObjectID(),
                Types.TASK, task.getParentFolderID(), -1);
            final CommonInsertResponse response = LinkTools.insert(client,
                new InsertRequest(link));
            assertFalse("Inserting link failed.", response.hasError());
            final AllResponse allR = LinkTools.all(client, new AllRequest(
                contact.getObjectID(), Types.CONTACT, contact.getParentFolderID()));
            final LinkObject[] links = allR.getLinks();
            assertTrue("Too few links found.", links.length >= 1);
            LinkTools.delete(client, new DeleteRequest(link));
        } finally {
            TaskTools.delete(client, new com.openexchange.ajax.task.actions
                .DeleteRequest(task));
            Executor.execute(client, new com.openexchange.ajax.contact.action
                .DeleteRequest(contact));
        }
    }
}
