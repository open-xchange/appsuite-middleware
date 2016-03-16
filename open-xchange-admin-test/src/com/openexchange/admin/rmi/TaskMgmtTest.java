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
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;
import com.openexchange.admin.user.copy.rmi.TestTool;

public class TaskMgmtTest extends AbstractRMITest {

    private User admin;

    private OXContextInterface ci;

    private Context context;

    private String db_name;

    private Database client_db;

    private OXUtilInterface oxu;

    private OXTaskMgmtInterface ti;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        admin = newUser("oxadmin", "secret", "Admin User", "Admin", "User", "oxadmin@example.com");
        ci = getContextInterface();
        oxu = getUtilInterface();
        ti = getTaskInterface();
    }

    @Before
    public final void setupContexts() throws Exception {
        context = TestTool.createContext(ci, "TaskMgmtCtx_", admin, "all", superAdminCredentials);
        db_name = "db_" + System.currentTimeMillis();
        client_db = UtilTest.getTestDatabaseObject("localhost", db_name);
        if (null == client_db) {
            throw new NullPointerException("Database object is null");
        }
        client_db.setId(oxu.registerDatabase(client_db, superAdminCredentials).getId());
    }

    @After
    public final void tearDownContexts() throws Exception {
        try {
            if (ci != null && context != null && superAdminCredentials != null) {
                ci.delete(context, superAdminCredentials);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client_db != null) {
            oxu.unregisterDatabase(new Database(client_db.getId()), superAdminCredentials);
        }
        db_name = null;
        client_db = null;
    }

    @Test
    public void testGetTaskResultsContextCredentialsInt() throws MalformedURLException, RemoteException, NotBoundException, Exception {
        final Credentials cred = DummyCredentials();

        final int jobId = ci.moveContextDatabase(context, client_db, superAdminCredentials);

        ti.getTaskResults(context, cred, jobId);
        int counter = 0;
        boolean running = true;
        while (running && counter < 180) {
            try {
                ti.deleteJob(context, cred, jobId);
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
