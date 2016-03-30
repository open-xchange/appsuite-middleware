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

import static com.openexchange.ajax.task.TaskTools.valuesForUpdate;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * This test ensures that bug 22305 does not occur again. 
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug22305Test extends AbstractTaskTest {

    private AJAXClient anton, berta;
    private int antonId, bertaId;
    private TimeZone bertaTZ;
    private Task task, bertaTask;

    public Bug22305Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        anton = getClient();
        antonId = anton.getValues().getUserId();
        berta = new AJAXClient(User.User2);
        bertaId = berta.getValues().getUserId();
        bertaTZ = berta.getValues().getTimeZone();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test for bug 22305");
        task.addParticipant(new UserParticipant(antonId));
        task.addParticipant(new UserParticipant(bertaId));
        InsertRequest request = new InsertRequest(task, getTimeZone());
        InsertResponse response = anton.execute(request);
        response.fillTask(task);
        bertaTask = valuesForUpdate(task, berta.getValues().getPrivateTaskFolder());
        bertaTask.addParticipant(new UserParticipant(bertaId));
        UpdateRequest uReq = new UpdateRequest(bertaTask, bertaTZ);
        UpdateResponse uResp = berta.execute(uReq);
        task.setLastModified(uResp.getTimestamp());
        bertaTask.setLastModified(uResp.getTimestamp());
    }

    @Override
    protected void tearDown() throws Exception {
        DeleteRequest request = new DeleteRequest(bertaTask);
        berta.execute(request);
        super.tearDown();
    }

    public void testConfirmWithIdOnlyInBody() throws Throwable {
        bertaTask = valuesForUpdate(bertaTask);
        bertaTask.setNote("Update to test for NullPointerException");
        UpdateRequest request = new UpdateRequest(bertaTask, bertaTZ, false);
        UpdateResponse response = berta.execute(request);
        assertFalse(response.hasError());
        bertaTask.setLastModified(response.getTimestamp());
    }
}
