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

import java.util.Date;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;

/**
 * Tests problem described in bug #9295.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug9252Test extends AbstractTaskTest {

    private AJAXClient client1;

    private AJAXClient client2;

    /**
     * @param name
     */
    public Bug9252Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        client2 = new AJAXClient(AJAXClient.User.User2);
    }

    /**
     * Tests if tasks in public folders created by other users can be read.
     * @throws Throwable if this test fails.
     */
    public void testReadAccess() throws Throwable {
        // Create public folder.
        final FolderObject folder = Create.setupPublicFolder(
            "Bug9295TaskFolder", FolderObject.TASK, client1.getValues()
            .getUserId());
        folder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        final CommonInsertResponse fInsertR = client1.execute(
            new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        folder.setObjectID(fInsertR.getId());
        try {
            // Create a task in there.
            final Task task = com.openexchange.groupware.tasks.Create
                .createWithDefaults();
            task.setParentFolderID(folder.getObjectID());
            task.setTitle("Test bug #9295");
            final InsertResponse iResponse = client1.execute(new InsertRequest(task, client1.getValues().getTimeZone()));
            task.setObjectID(iResponse.getId());
            // Now second user tries to read the task.
            final GetResponse gResponse = TaskTools.get(client2,
                new GetRequest(folder.getObjectID(), task.getObjectID()));
            final Task reload = gResponse.getTask(client2.getValues()
                .getTimeZone());
            TaskTools.compareAttributes(task, reload);
        } finally {
            client1.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), new Date()));
        }
    }
}
