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

import static com.openexchange.java.Autoboxing.L;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.task.actions.AbstractTaskRequest;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * Implements test case 1803 partly.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class SmokeTest extends AbstractTaskTest {

    /**
     * @param name
     */
    public SmokeTest(final String name) {
        super(name);
    }

    /**
     * Tests inserting a private task.
     * http://testlink6.open-xchange.com/testlink/lib/execute/execSetResults.php?level=testcase&id=1803
     * @throws Throwable if an error occurs.
     */
    public void testCase1803() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateTaskFolder();
        final Task task = Create.createWithDefaults();
        task.setParentFolderID(folderId);
        task.setTitle("Buy a birthday gift for Mr. K\u00e4rner");
        final TimeZone timeZone = client.getValues().getTimeZone();
        final DateFormat dateF = new SimpleDateFormat("dd.MM.yyyy", client
            .getValues().getLocale());
        dateF.setTimeZone(timeZone);
        task.setStartDate(dateF.parse("26.02.2007"));
        task.setEndDate(dateF.parse("27.02.2007"));
        task.setStatus(Task.IN_PROGRESS);
        task.setPriority(Task.HIGH);
        task.setPercentComplete(75);
        task.setTargetDuration(L(2));
        task.setActualDuration(L(2));
        final InsertResponse insertR = client.execute(new InsertRequest(task, timeZone));
        final GetResponse getR = TaskTools.get(client, new GetRequest(insertR));
        final Task reload = getR.getTask(timeZone);
        TaskTools.compareAttributes(task, reload);
        final CommonAllResponse allR = TaskTools.all(client, new AllRequest(
            folderId, AbstractTaskRequest.GUI_COLUMNS, AllRequest.GUI_SORT,
            AllRequest.GUI_ORDER));
        boolean foundObject = false;
        for (final Object[] rowValues : allR) {
            if (rowValues[0].equals(Integer.valueOf(insertR.getId()))) {
                foundObject = true;
            }
        }
        assertTrue("All request on folder did not found created object.",
            foundObject);
        // TODO Use list an check if list contains the entered attributes.
        client.execute(new DeleteRequest(reload));
    }
}
