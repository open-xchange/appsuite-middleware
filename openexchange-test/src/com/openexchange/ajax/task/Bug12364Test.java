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

package com.openexchange.ajax.task;

import java.util.TimeZone;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12364Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     */
    public Bug12364Test(final String name) {
        super(name);
    }

    public void testMoveTasks() throws Throwable {
        final AJAXClient myClient = getClient();
        final TimeZone tz = myClient.getValues().getTimeZone();
        final FolderObject folder1;
        final FolderObject folder2;
        {
            folder1 = Create.createPublicFolder(myClient,
                "bug 12364 test folder 1", FolderObject.TASK);
            folder2 = Create.createPublicFolder(myClient,
                "bug 12364 test folder 2", FolderObject.TASK);
        }
        try {
            // Create tasks.
            final Task task1 = new Task();
            task1.setTitle("bug 12364 test 1");
            task1.setParentFolderID(folder1.getObjectID());
            final Task task2 = new Task();
            task2.setTitle("bug 12364 test 2");
            task2.setParentFolderID(folder2.getObjectID());
            TaskTools.insert(myClient, task1, task2);
            // Move them
            task1.setParentFolderID(folder2.getObjectID());
            task2.setParentFolderID(folder1.getObjectID());
            final UpdateRequest request1 = new UpdateRequest(folder1.getObjectID(), task1, tz);
            final UpdateRequest request2 = new UpdateRequest(folder2.getObjectID(), task2, tz);
            myClient.execute(MultipleRequest.create(new UpdateRequest[] {
                request1,
                request2
            }));
        } finally {
            myClient.execute(new DeleteRequest(EnumAPI.OX_OLD, folder1, folder2));
        }
    }
}
