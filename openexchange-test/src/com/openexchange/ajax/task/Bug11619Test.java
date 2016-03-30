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
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * Updates a task and adds an external participant.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11619Test extends AbstractTaskTest {

    /**
     * Default constructor.
     * @param name test name
     */
    public Bug11619Test(final String name) {
        super(name);
    }

    /**
     * Creates and updates a task and adds on updating an external participant.
     * @throws Throwable if some problem occurs.
     */
    public void testExternalParticipant() throws Throwable {
        final AJAXClient client = getClient();
        final TimeZone tz = getTimeZone();
        Task task = Create.createWithDefaults(getPrivateFolder(), "Bug 11619 test");
        final InsertResponse insertR = client.execute(new InsertRequest(task, tz));
        try {
            {
                final GetRequest request = new GetRequest(insertR);
                final GetResponse response = client.execute(request);
                task = response.getTask(tz);
            }
            {
                task.setParticipants(new Participant[] {
                    new ExternalUserParticipant("test@example.org")
                });
                final UpdateRequest request = new UpdateRequest(task, tz);
                final UpdateResponse response = TaskTools.update(client, request);
                task.setLastModified(response.getTimestamp());
            }
            {
                final GetRequest request = new GetRequest(insertR);
                final GetResponse response = client.execute(request);
                task = response.getTask(tz);
                assertTrue("External participant get lost.",
                    task.getParticipants().length > 0);
            }
        } finally {
            final GetRequest request = new GetRequest(insertR);
            final GetResponse response = client.execute(request);
            task = response.getTask(tz);
            client.execute(new DeleteRequest(task));
        }
    }
}
