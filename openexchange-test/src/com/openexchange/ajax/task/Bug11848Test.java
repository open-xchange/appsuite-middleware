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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.ListID;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11848Test extends AbstractAJAXSession {

    private static final int NUMBER = 100;

    /**
     * Default constructor.
     */
    public Bug11848Test(final String name) {
        super(name);
    }

    public void testSorting() throws Throwable {
        final AJAXClient client = getClient();
        final int folder = client.getValues().getPrivateTaskFolder();
        final TimeZone tz = client.getValues().getTimeZone();
        final Task[] tasks = new Task[NUMBER];
        {
            final Calendar calendar = new GregorianCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            for (int i = 0; i < NUMBER; i++) {
                final Task task = new Task();
                task.setTitle("test for bug 11848 " + i);
                task.setParentFolderID(folder);
                task.setStartDate(calendar.getTime());
                task.setEndDate(calendar.getTime());
                calendar.add(Calendar.DATE, 1);
                tasks[i] = task;
            }
        }
        final MultipleResponse<InsertResponse> mInsert;
        {
            final InsertRequest[] requests = new InsertRequest[NUMBER];
            for (int i = 0; i < NUMBER; i++) {
                requests[i] = new InsertRequest(tasks[i], tz);
            }
            mInsert = client.execute(MultipleRequest.create(requests));
            for (int i = 0; i < NUMBER; i++) {
                mInsert.getResponse(i).fillTask(tasks[i]);
            }
        }
        try {
            final AllRequest request = new AllRequest(folder, new int[] { Task.OBJECT_ID }, Task.END_DATE, Order.ASCENDING);
            final CommonAllResponse response = client.execute(request);
            int pos = 0;
            for (final ListID identifier : response.getListIDs()) {
                final Task task = tasks[pos];
                if (identifier.getObject().equals(String.valueOf(task.getObjectID()))) {
                    pos++;
                    if (pos >= NUMBER) {
                        break;
                    }
                }
            }
        } finally {
            final DeleteRequest[] requests = new DeleteRequest[NUMBER];
            for (int i = 0; i < NUMBER; i++) {
                requests[i] = new DeleteRequest(mInsert.getResponse(i));
            }
            client.execute(MultipleRequest.create(requests));
        }
    }
}
