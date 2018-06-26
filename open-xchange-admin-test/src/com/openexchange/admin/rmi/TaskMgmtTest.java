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

package com.openexchange.admin.rmi;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;
import com.openexchange.admin.rmi.factory.UserFactory;
import com.openexchange.admin.user.copy.rmi.TestTool;

public class TaskMgmtTest extends AbstractRMITest {

    private User admin;

    private Context context;

    private String db_name;

    private Database client_db;

    private OXTaskMgmtInterface ti;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        admin = UserFactory.createUser("oxadmin", "secret", "Admin User", "Admin", "User", "oxadmin@example.com");
        ti = getTaskInterface();
    }

    @Before
    public final void setupContexts() throws Exception {
        context = TestTool.createContext(getContextManager(), "TaskMgmtCtx_", admin, "all", superAdminCredentials);
        db_name = "db_" + System.currentTimeMillis();
        client_db = UtilTest.getTestDatabaseObject("localhost", db_name);
        if (null == client_db) {
            throw new NullPointerException("Database object is null");
        }
        client_db.setId(getDatabaseManager().register(client_db, Boolean.FALSE, Integer.valueOf(0)).getId());
    }

    @After
    public final void tearDownContexts() throws Exception {
        getContextManager().cleanUp();
        if (client_db != null) {
            getDatabaseManager().unregister(new Database(client_db.getId()));
        }
        db_name = null;
        client_db = null;
    }

    @Test
    public void testGetTaskResultsContextCredentialsInt() throws MalformedURLException, RemoteException, NotBoundException, Exception {
        final int jobId = getContextManager().moveContextDatabase(context, client_db);

        ti.getTaskResults(context, contextAdminCredentials, jobId);
        int counter = 0;
        boolean running = true;
        while (running && counter < 180) {
            try {
                ti.deleteJob(context, contextAdminCredentials, jobId);
                running = false;
                System.out.println("Task moveContextDatabase finished");
            } catch (TaskManagerException e) {
                Thread.sleep(1000);
            }
            counter++;
        }
        System.out.println("Task moveContextDatabase counter: " + counter);
    }
}
