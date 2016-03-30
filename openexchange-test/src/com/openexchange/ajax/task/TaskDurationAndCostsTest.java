/*
 *
    OPEN-XCHANGE legal information
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
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.TimeZone;
import junit.framework.AssertionFailedError;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link TaskDurationAndCostsTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TaskDurationAndCostsTest extends AbstractAJAXSession {

    @SuppressWarnings("hiding")
    private AJAXClient client;

    private Task task;

    private TimeZone tz;

    /**
     * Initializes a new {@link TaskDurationAndCostsTest}.
     */
    public TaskDurationAndCostsTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        task = new Task();
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("Set task duration and costs test");
        task.setActualDuration(L(2));
        task.setActualCosts(new BigDecimal("2.0"));
        task.setTargetDuration(L(10));
        task.setTargetCosts(new BigDecimal("10.0"));
        InsertRequest request = new InsertRequest(task, tz);
        InsertResponse response = client.execute(request);
        response.fillTask(task);
    }

    @Override
    public void tearDown() throws Exception {
        DeleteRequest req = new DeleteRequest(task);
        client.execute(req);
    }

    public void testDurationAndCosts() throws Exception {
        task.setTargetCosts(new BigDecimal("11.5"));
        task.setActualCosts(new BigDecimal("4.728"));
        task.setActualDuration(L(7));
        task.setTargetDuration(L(15));
        UpdateRequest req = new UpdateRequest(task, tz, false);
        try {
            UpdateResponse response = client.execute(req);
            task.setLastModified(response.getTimestamp());
        } catch (AssertionFailedError e) {
            fail("Setting costs and duration failed!");
        }
        GetRequest request = new GetRequest(task);
        GetResponse response = client.execute(request);
        task.setLastModified(response.getTimestamp());
        Task test = response.getTask(tz);
        // We have in the database NUMERIC(12,2). So round to 3 valid digits in this case. Rounding is necessary because JSON internally
        // parses a float value to java.lang.Double causing rounding issues.
        assertEquals("Actual costs not equal", task.getActualCosts().round(new MathContext(3)), test.getActualCosts().round(new MathContext(3)));
        assertEquals("Target costs not equal", task.getTargetCosts(), test.getTargetCosts());
        assertEquals("Actual duration not equal", task.getActualDuration(), test.getActualDuration());
        assertEquals("Target duration not equal", task.getTargetDuration(), test.getTargetDuration());
    }
}
