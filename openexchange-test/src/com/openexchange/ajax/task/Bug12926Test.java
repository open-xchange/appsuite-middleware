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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;


/**
 * {@link Bug12926Test}
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12926Test extends AbstractTaskTest {

    private AJAXClient client;

    private int userId;

    private int folderId;

    private TimeZone tz;

    private Task task;

    /**
     * Default constructor.
     * @param name test name.
     */
    public Bug12926Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId = client.getValues().getUserId();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        task = Create.createWithDefaults(folderId, "Bug 12926 test task");
        final List<Participant> participants = ParticipantTools.getParticipants(client, 1, userId);
        participants.add(new UserParticipant(userId));
        task.setParticipants(participants);
        final InsertResponse insertR = client.execute(new InsertRequest(task, tz));
        insertR.fillTask(task);
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(task));
        super.tearDown();
    }

    public void testTaskStaysInDelegatorFolder() throws OXException, IOException, SAXException, JSONException {
        final Task task2 = new Task();
        task2.setObjectID(task.getObjectID());
        task2.setParentFolderID(task.getParentFolderID());
        task2.setLastModified(task.getLastModified());
        final List<Participant> participants = new ArrayList<Participant>();
        for (final Participant participant : task.getParticipants()) {
            if (participant.getIdentifier() != userId) {
                participants.add(participant);
            }
        }
        task2.setParticipants(participants);
        final UpdateResponse uResponse = client.execute(new UpdateRequest(task, tz));
        task.setLastModified(uResponse.getTimestamp());
        final GetResponse getR = client.execute(new GetRequest(task.getParentFolderID(), task.getObjectID(), false));
        if (getR.hasError()) {
            fail(getR.getException().getMessage());
        }
    }
}
