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

package com.openexchange.rmi.filestore;

import static org.junit.Assert.fail;
import java.io.File;
import java.rmi.Naming;
import java.util.HashSet;
import java.util.UUID;
import org.junit.After;
import org.junit.Test;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;

/**
 * {@link TestBug41727}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TestBug41727 {

    private static final String RMI_HOST = "rmi://localhost:1099/";

    private Filestore filestore;

    private Context context;

    @After
    public void Cleanup() {
        try {
            if (context != null && context.getId() > 0) {
                OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOST + OXContextInterface.RMI_NAME);
                long start = System.currentTimeMillis();
                oxctx.delete(context, getMasterCredentials());
                System.out.println("Context '" + context.getId() + "' was successfully deleted in " + (System.currentTimeMillis() - start) + " msec.");
            }

            if (filestore != null) {
                OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOST + OXUtilInterface.RMI_NAME);
                long start = System.currentTimeMillis();
                oxutil.unregisterFilestore(filestore, getMasterCredentials());
                System.out.println("Filestore '" + filestore.getId() + "' was successfully unregistered in " + (System.currentTimeMillis() - start) + " msec.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to delete context with identifier '" + context.getId() + "' : " + e.getMessage());
        }
    }

    @Test
    public void testBug41727() {
        // 1) Create context
        context = new Context();
        HashSet<String> set = new HashSet<String>();
        set.add("bug41727");
        context.setLoginMappings(set);
        context.setMaxQuota(1000000L);

        OXContextInterface oxctx = null;
        try {
            oxctx = (OXContextInterface) Naming.lookup(RMI_HOST + OXContextInterface.RMI_NAME);
            long start = System.currentTimeMillis();
            context = oxctx.create(context, getContextAdmin(), getMasterCredentials());
            System.out.println("Context '" + context.getId() + "' was successfully created in " + (System.currentTimeMillis() - start) + " msec.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to create context: " + e.getMessage());
        }

        // 2) Create users in context
        User someUser = null;
        User masterUser = null;
        OXUserInterface oxuser = null;
        try {
            oxuser = (OXUserInterface) Naming.lookup(RMI_HOST + OXUserInterface.RMI_NAME);
            // Create user A
            long start = System.currentTimeMillis();
            someUser = oxuser.create(context, createUser(), getContextAdminCredentials(), null);
            System.out.println("User A '" + someUser.getImapLogin() + "' was successfully created in " + (System.currentTimeMillis() - start) + " msec.");

            // Create user B
            start = System.currentTimeMillis();
            masterUser = oxuser.create(context, createUser(), getContextAdminCredentials(), null);
            System.out.println("Master user '" + masterUser.getImapLogin() + "' was successfully created in " + (System.currentTimeMillis() - start) + " msec.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to create users: " + e.getMessage());
        }

        // 3) Create physical storage
        File path = new File("/tmp/bug41727");
        if (!path.exists() && path.getParentFile().canWrite()) {
            path.deleteOnExit();
            boolean created = path.mkdir();
            if (!created) {
                fail("Unable to create physical filestore '" + path.getAbsolutePath() + "'.");
            }
            System.out.println("Created physical filestore under '" + path.getAbsolutePath() + "'");

            //Manually create filestore locations
            //4581_ctx_4_user_store
            File userFilestore = new File("/tmp/bug41727/" + context.getId() + "_ctx_" + someUser.getId() + "_user_store");
            created = userFilestore.mkdir();
            if (!created) {
                fail("Unable to create physical filestore '" + path.getAbsolutePath() + "' for user '" + someUser.getId() + "'");
            }

            File masterFilestore = new File("/tmp/bug41727/" + context.getId() + "_ctx_" + masterUser.getId() + "_user_store");
            created = masterFilestore.mkdir();
            if (!created) {
                fail("Unable to create physical filestore '" + path.getAbsolutePath() + "' for master user '" + masterUser.getId() + "'");
            }
        }

        // 4) Register filestore
        try {
            OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOST + OXUtilInterface.RMI_NAME);
            long start = System.currentTimeMillis();
            filestore = oxutil.registerFilestore(getFilestore(path.getAbsolutePath()), getMasterCredentials());
            System.out.println("Filestore '" + filestore.getId() + "' was successfully registered in " + (System.currentTimeMillis() - start) + " msec.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to register filestore: " + e.getMessage());
        }

        // 5) Assign both users to the same file storage
        try {
            long start = System.currentTimeMillis();
            oxuser.moveFromContextToUserFilestore(context, someUser, filestore, 500000L, getContextAdminCredentials());
            System.out.println("Moved user '" + someUser.getId() + "' to filestore '" + filestore.getId() + "' in " + (System.currentTimeMillis() - start) + " msec.");

            start = System.currentTimeMillis();
            oxuser.moveFromContextToUserFilestore(context, masterUser, filestore, 500000L, getContextAdminCredentials());
            System.out.println("Moved master user '" + masterUser.getId() + "' to filestore '" + filestore.getId() + "' in " + (System.currentTimeMillis() - start) + " msec.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to move users from context to user filestores: " + e.getMessage());
        }

        // 6) Move user's A filestore to user's B filestore with move master
        try {
            long start = System.currentTimeMillis();
            oxuser.moveFromUserFilestoreToMaster(context, someUser, masterUser, getContextAdminCredentials());
            System.out.println("Moved user '" + someUser.getId() + "' to filestore '" + filestore.getId() + "' of master user '" + masterUser.getId() + "' in " + (System.currentTimeMillis() - start) + " msec.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to move user's filestore to master user's filestore: " + e.getMessage());
        }

        // 7) Delete master user
        try {
            long start = System.currentTimeMillis();
            oxuser.delete(context, masterUser, null, getContextAdminCredentials());
            System.out.println("Deleted master user '" + masterUser.getId() + "' in " + (System.currentTimeMillis() - start) + " msec.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to delete master user '" + masterUser.getId() + "'");
        }

        // 8) Delete the other user
        try {
            long start = System.currentTimeMillis();
            oxuser.delete(context, someUser, null, getContextAdminCredentials());
            System.out.println("Deleted user '" + someUser.getId() + "' in " + (System.currentTimeMillis() - start) + " msec.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to delete user '" + someUser.getId() + "'");
        }
    }

    private Credentials getContextAdminCredentials() {
        return new Credentials("oxadmin", "secret");
    }

    private Filestore getFilestore(String path) {
        Filestore filestore = new Filestore();
        filestore.setMaxContexts(1000);
        filestore.setSize(1000000L);
        filestore.setUrl("file:" + path);

        return filestore;
    }

    private User createUser() {
        UUID random = UUID.randomUUID();
        User oxuser = new User();
        oxuser.setName(random.toString());
        oxuser.setDisplay_name("oxuser" + random);
        oxuser.setGiven_name("oxuser" + random);
        oxuser.setSur_name("oxuser" + random);
        oxuser.setPrimaryEmail("oxuser" + random + "@example.com");
        oxuser.setEmail1("oxuser" + random + "@example.com");
        oxuser.setPassword("secret");
        oxuser.setImapServer("dovecot.devel.open-xchange.com");
        oxuser.setImapLogin(random + "@" + random);
        return oxuser;
    }

    private Credentials getMasterCredentials() {
        Credentials credentials = new Credentials();
        credentials.setLogin("oxadminmaster");
        credentials.setPassword("secret");

        return credentials;
    }

    private User getContextAdmin() {
        User oxadmin = new User();
        oxadmin.setName("oxadmin");
        oxadmin.setDisplay_name("oxadmin");
        oxadmin.setGiven_name("oxadmin");
        oxadmin.setSur_name("oxadmin");
        oxadmin.setPrimaryEmail("oxadmin@example.com");
        oxadmin.setEmail1("oxadmin@example.com");
        oxadmin.setPassword("secret");
        return oxadmin;
    }
}
