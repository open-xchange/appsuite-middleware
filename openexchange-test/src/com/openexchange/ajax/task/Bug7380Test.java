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

import java.util.List;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Task;

/**
 * Tests problem described in bug #7380.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug7380Test extends AbstractTaskTest {

    /**
     * @param name
     */
    public Bug7380Test(final String name) {
        super(name);
    }

    /**
     * Tests if bug #7380 appears again.
     * @throws Throwable if this test fails.
     */
    public void testBug() throws Throwable {
        final AJAXClient client = getClient();
        final Task task = new Task();
        task.setTitle("Test bug #7380");
        task.setParentFolderID(getPrivateFolder());
        final AJAXSession session = getSession();
        final List<Participant> participants = ParticipantTools.getParticipants(
            session.getConversation(), AJAXConfig.getProperty(AJAXConfig
            .Property.HOSTNAME), session.getId(), 1, true, client.getValues()
            .getUserId());
        task.setParticipants(participants);
        final InsertResponse iResponse = client.execute(new InsertRequest(task, client.getValues().getTimeZone()));
        task.setObjectID(iResponse.getId());
        final GetResponse gResponse = TaskTools.get(client,
            new GetRequest(getPrivateFolder(), task.getObjectID()));
        task.setLastModified(gResponse.getTimestamp());
        task.setParticipants((Participant[]) null);
        final UpdateResponse uResponse = TaskTools.update(client,
            new UpdateRequest(task, client.getValues().getTimeZone()));
        task.setLastModified(uResponse.getTimestamp());
        client.execute(new DeleteRequest(task));
    }
}
