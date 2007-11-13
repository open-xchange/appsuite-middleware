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

package com.openexchange.ajax.task;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonUpdatesResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.framework.CommonUpdatesRequest.Ignore;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug10119Test extends AbstractTaskTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Bug10119Test.class);

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public Bug10119Test(final String name) {
        super(name);
    }

    /**
     * Checks if the updates action works correctly if 1 item is deleted and
     * another created.
     * @throws Throwable if an exception occurs.
     */
    public void testFunambol() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getPrivateTaskFolder();
        final TimeZone timeZone = client.getTimeZone();
        final Date beforeInsert = client.getServerTime();
        final MultipleResponse mInsert;
        {
            final InsertRequest[] initialInserts = new InsertRequest[2];
            for (int i = 0; i < initialInserts.length; i++) {
                final Task task = new Task();
                task.setParentFolderID(folderId);
                task.setTitle("Initial" + (i + 1));
                initialInserts[i] = new InsertRequest(task, timeZone);
            }
            mInsert =  Executor.multiple(client, new MultipleRequest(
                initialInserts));
        }
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID,
            Task.FOLDER_ID };
        final CommonUpdatesResponse uResponse;
        {
            final UpdatesRequest uRequest = new UpdatesRequest(folderId,
                columns, 0, null, beforeInsert);
            uResponse = TaskTools.updates(client,
                uRequest);
            LOG.info("Updates size after 2 initial inserts: " + uResponse.size());
            assertTrue("Can't find initial inserts", uResponse.size() >= 2);
        }
        // Delete one
        {
            final InsertResponse secondInsert = (InsertResponse) mInsert
                .getResponse(1);
            TaskTools.delete(client, new DeleteRequest(folderId,
                secondInsert.getId(), secondInsert.getTimestamp()));
        }
        // Insert one
        final InsertResponse iResponse;
        {
            final Task task = new Task();
            task.setParentFolderID(folderId);
            task.setTitle("anotherInsert");
            iResponse = TaskTools.insert(client, new InsertRequest(task,
                timeZone));
        }
        // Check if we see 2 updates, 1 insert and 1 delete.
        {
            final CommonUpdatesResponse uResponse2 = TaskTools.updates(client,
                new UpdatesRequest(folderId, columns, 0, null,
                uResponse.getTimestamp(), Ignore.NONE));
            LOG.info("Updates size after 1 create and 1 delete: " + uResponse2.size());
            assertTrue("Can't get created and deleted item.", uResponse2.size() >= 2);
        }
        // Delete all.
        {
            final DeleteRequest[] deletes = new DeleteRequest[2];
            final InsertResponse firstInsert = (InsertResponse) mInsert
                .getResponse(0);
            deletes[0] = new DeleteRequest(folderId, firstInsert.getId(),
                firstInsert.getTimestamp());
            deletes[1] = new DeleteRequest(folderId, iResponse.getId(),
                iResponse.getTimestamp());
            Executor.multiple(client, new MultipleRequest(deletes));
        }
    }
}
