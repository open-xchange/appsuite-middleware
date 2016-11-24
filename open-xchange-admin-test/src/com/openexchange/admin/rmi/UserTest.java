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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.java.util.TimeZones;

/**
 *
 * @author cutmasta
 * @author d7
 */
public class UserTest extends AbstractTest {

    public final static String NAMED_ACCESS_COMBINATION_BASIC = "all";
    // list of chars that must be valid
    //    protected static final String VALID_CHAR_TESTUSER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";
    protected static final String VALID_CHAR_TESTUSER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // global setting for stored password
    protected static final String pass = "foo-user-pass";

    protected static OXUserInterface getUserClient() throws Exception {
        return (OXUserInterface) Naming.lookup(getRMIHostUrl() + OXUserInterface.RMI_NAME);
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(UserTest.class);
    }

    @Test
    public void testDefaultModuleAccess() throws Exception {
        // check whether all new options have been cleared in disableAll()
        final UserModuleAccess ret = new UserModuleAccess();
        ret.disableAll();
        ret.setWebmail(true);
        ret.setContacts(true);

        final Class clazz = ret.getClass();
        for (final Method m : clazz.getMethods()) {
            final String name = m.getName();
            if (!name.equals("getClass") && !name.equals("getPermissionBits") && !name.equals("getProperties") && !name.equals("getProperty") && (name.startsWith("is") || name.startsWith("get"))) {
                //System.out.println("*******" + name);
                boolean res = (Boolean) m.invoke(ret, null);
                if (name.endsWith("Webmail") || name.endsWith("Contacts") || name.endsWith("GlobalAddressBookDisabled")) {
                    assertTrue(name + " must return true", res);
                } else {
                    assertFalse(name + " must return false", res);
                }
            }
        }
    }

    @Test
    public void testCreate() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }
    }

    private User id(User createduser) {
        User user = new User();
        user.setId(createduser.getId());
        return user;
    }

    @Test
    public void testCreateWithContextModuleAccessRights() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, cred, null);

        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }
    }

    @Test
    public void testCreateWithNamedModuleAccessRights() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, NAMED_ACCESS_COMBINATION_BASIC, cred, null);

        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }
    }

    @Test
    public void testCreateMandatory() throws Exception {
        // this creates an user ONLY with mandatory fields set

        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User urs = getTestUserMandatoryFieldsObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUserMandatory(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

    }

    private void compareUserMandatory(User a, User b) {

        System.out.println("USERA" + a.toString());
        System.out.println("USERB" + b.toString());

        assertEquals("username not equal", a.getName(), b.getName());
        assertEquals("enabled not equal", a.getMailenabled(), b.getMailenabled());
        assertEquals("primaryemail not equal", a.getPrimaryEmail(), b.getPrimaryEmail());
        assertEquals("display name not equal", a.getDisplay_name(), b.getDisplay_name());
        assertEquals("firtname not equal", a.getGiven_name(), b.getGiven_name());

    }

    @Test(expected = NoSuchUserException.class)
    public void testDelete() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();

        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        // delete user
        oxu.delete(ctx, id(createduser), null, cred);

        // try to load user, this MUST fail
        oxu.getData(ctx, createduser, cred);
        fail("user not exists expected");
    }

    //@Test
    public void _disabledtestBug9027() throws Exception {

        // The same user cannot be created after if
        // was deleted due to infostore problems
        // Details: http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=9027

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();

        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        User createduser = oxu.create(ctx, urs, access, cred, null);

        // delete user
        oxu.delete(ctx, createduser, null, cred);

        // create same user again, this failes as described in the bug
        createduser = oxu.create(ctx, urs, access, cred, null);
    }

    @Test(expected = InvalidDataException.class)
    public void testDeleteEmptyUserList() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();

        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        // delete user
        oxu.delete(ctx, new User[0], null, cred);

        // try to load user, this MUST fail
        oxu.getData(ctx, createduser, cred);
        fail("user not exists expected");
    }

    @Test
    public void testGetData() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();

        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);
        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, createduser, cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }
    }

    @Test
    public void testGetDataByName() throws Exception {
        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();

        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        final User usernameuser = new User();
        usernameuser.setName(createduser.getName());

        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, usernameuser, cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }
    }

    /**
     * Tests if fix for bug 18866 still works.
     */
    @Test
    public void testPublicFolderEditableForUser() throws Exception {
        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        access.setPublicFolderEditable(true);
        final User usr = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        User createduser;
        try {
            createduser = oxu.create(ctx, usr, access, cred, null);
            fail("Creating a user with permission to edit public folder permissions should be denied.");
        } catch (final StorageException e) {
            // Everything is fine. Setting publicFolderEditable should be denied. See bugs 18866, 20369, 20635.
            access.setPublicFolderEditable(false);
            createduser = oxu.create(ctx, usr, access, cred, null);
        }

        // now load user from server and check if data is correct, else fail
        UserModuleAccess moduleAccess = oxu.getModuleAccess(ctx, createduser, cred);
        assertFalse("Editing public folder was allowed for a normal user.", moduleAccess.isPublicFolderEditable());

        moduleAccess.setPublicFolderEditable(true);
        try {
            oxu.changeModuleAccess(ctx, usr, moduleAccess, cred);
            fail("Setting publicfoldereditable to true was not denied by admin.");
        } catch (final StorageException e) {
            // This is expected.
        }
    }

    /**
     * Tests if fix for bug 18866 still works.
     */
    @Test
    public void testPublicFolderEditableForAdmin() throws Exception {
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final User usr = new User();
        // Administrator gets always principal identifier 2. The group users gets principal identifier 1.
        usr.setId(Integer.valueOf(2));

        // enable and test it.
        UserModuleAccess access = oxu.getModuleAccess(ctx, usr, cred);
        access.setPublicFolderEditable(true);
        oxu.changeModuleAccess(ctx, usr, access, cred);
        access = oxu.getModuleAccess(ctx, usr, cred);
        assertTrue("Flag publicfoldereditable does not survice roundtrip for context administrator.", access.isPublicFolderEditable());

        access.setPublicFolderEditable(false);
        oxu.changeModuleAccess(ctx, usr, access, cred);
        access = oxu.getModuleAccess(ctx, usr, cred);
        assertFalse("Flag publicfoldereditable does not survice roundtrip for context administrator.", access.isPublicFolderEditable());
    }

    @Test
    public void testGetDataByNameWithUserAuth() throws Exception {
        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();

        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        final User usernameuser = new User();
        usernameuser.setName(createduser.getName());

        final Credentials usercred = new Credentials(urs.getName(), urs.getPassword());
        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, usernameuser, usercred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }
    }

    @Test
    public void testGetDataByID() throws Exception {
        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();

        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        final User iduser = new User();
        iduser.setId(createduser.getId());

        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, iduser, cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }
    }

    @Test
    public void testGetModuleAccess() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess client_access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, client_access, cred, null);

        // get module access
        final UserModuleAccess srv_response = oxu.getModuleAccess(ctx, createduser, cred);

        // test if module access was set correctly
        compareUserAccess(client_access, srv_response);

    }

    @Test
    public void testChangeModuleAccess() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess client_access = new UserModuleAccess();
        final User usr = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, usr, client_access, cred, null);

        // get module access
        final UserModuleAccess srv_response = oxu.getModuleAccess(ctx, createduser, cred);

        // test if module access was set correctly
        compareUserAccess(client_access, srv_response);

        // now change server loaded module access and submit changes to the server
        srv_response.setCalendar(!srv_response.getCalendar());
        srv_response.setContacts(!srv_response.getContacts());
        srv_response.setDelegateTask(!srv_response.getDelegateTask());
        srv_response.setEditPublicFolders(!srv_response.getEditPublicFolders());
        srv_response.setIcal(!srv_response.getIcal());
        srv_response.setInfostore(!srv_response.getInfostore());
        srv_response.setReadCreateSharedFolders(!srv_response.getReadCreateSharedFolders());
        srv_response.setSyncml(!srv_response.getSyncml());
        srv_response.setTasks(!srv_response.getTasks());
        srv_response.setVcard(!srv_response.getVcard());
        srv_response.setWebdav(!srv_response.getWebdav());
        srv_response.setWebdavXml(!srv_response.getWebdavXml());
        srv_response.setWebmail(!srv_response.getWebmail());

        // submit changes
        oxu.changeModuleAccess(ctx, createduser, srv_response, cred);

        // load again and verify
        final UserModuleAccess srv_response_changed = oxu.getModuleAccess(ctx, createduser, cred);

        // test if module access was set correctly
        compareUserAccess(srv_response, srv_response_changed);

    }

    @Test
    public void testList() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess client_access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, client_access, cred, null);

        final User[] srv_response = oxu.list(ctx, "*", cred);

        assertTrue("Expected list size > 0 ", srv_response.length > 0);

        boolean founduser = false;
        for (final User element : srv_response) {
            if (element.getId().intValue() == createduser.getId().intValue()) {
                founduser = true;
            }
        }

        assertTrue("Expected to find added user in user list", founduser);
    }

    @Test
    public void testListUsersWithOwnFilestore() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);
        Filestore fs = null;
        Credentials master = DummyMasterCredentials();

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess client_access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(getRMIHostUrl() + OXUtilInterface.RMI_NAME);
        try {
            final User createduser = oxu.create(ctx, urs, client_access, cred, null);

            //test if filestore already exists
            Filestore[] filestores = oxutil.listFilestore("file:///", master, true);
            if (filestores != null && filestores.length != 0) {
                if (filestores.length != 1) {
                    fail("Unexpected failure. Multiple filestores already exists.");
                } else {
                    fs = filestores[0];
                }
            }

            if (fs == null) {
                //create new filestore
                fs = new Filestore();
                fs.setMaxContexts(10);
                fs.setSize(1024l);
                fs.setUrl("file:///");

                fs = oxutil.registerFilestore(fs, master);
            }
            //move user to new filestore
            oxu.moveFromContextToUserFilestore(ctx, urs, fs, 10, cred);
            Thread.sleep(500); //wait for move

            final User[] srv_response = oxu.listUsersWithOwnFilestore(ctx, cred, fs.getId());

            assertTrue("Expected list size > 0 ", srv_response.length > 0);

            boolean founduser = false;
            for (final User element : srv_response) {
                if (element.getId().intValue() == createduser.getId().intValue()) {
                    founduser = true;
                }
            }

            assertTrue("Expected to find added user in user list", founduser);
        } finally {
            oxu.delete(ctx, urs, null, cred);
            if (fs != null) {
                oxutil.unregisterFilestore(fs, master);
            }
        }
    }

    public OXTaskMgmtInterface getTaskInterface() throws MalformedURLException, RemoteException, NotBoundException {
        return (OXTaskMgmtInterface) Naming.lookup(getRMIHostUrl() + OXTaskMgmtInterface.RMI_NAME);
    }

    @Test
    public void testListAll() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess client_access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, client_access, cred, null);

        final User[] srv_response = oxu.listAll(ctx, cred);

        assertTrue("Expected list size > 0 ", srv_response.length > 0);

        boolean founduser = false;
        for (final User element : srv_response) {
            if (element.getId().intValue() == createduser.getId().intValue()) {
                founduser = true;
            }
        }

        assertTrue("Expected to find added user in user list", founduser);
    }

    @Test
    public void testChange() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User usr = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, usr, access, cred, null);
        // now load user from server and check if data is correct, else fail
        User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }

        // now change data
        srv_loaded = createChangeUserData(srv_loaded);
        // submit changes
        oxu.change(ctx, srv_loaded, cred);

        // load again
        final User user_changed_loaded = oxu.getData(ctx, id(srv_loaded), cred);
        // set Username to old value for verification
        srv_loaded.setName(createduser.getName());
        // remove deleted dynamic attribute for verification
        srv_loaded.getUserAttributes().get("com.openexchange.test").remove("deleteMe");
        if (srv_loaded.getId().equals(user_changed_loaded.getId())) {
            //verify data
            compareUser(srv_loaded, user_changed_loaded);
        } else {
            fail("Expected to get correct changed user data");
        }
    }

    @Test
    public void testChangeSingleAttributeNull() throws Exception {
        // set single values to null in the user object and then call change, what happens?

        //      get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User usr = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, usr, access, cred, null);
        // now load user from server and check if data is correct, else fail
        User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }

        HashSet<String> notallowed = getNotNullableFields();

        // loop through methods and change each attribute per single call and load and compare
        MethodMapObject[] meth_objects = getSetableAttributeMethods(usr.getClass());

        for (MethodMapObject map_obj : meth_objects) {
            if (notallowed.contains(map_obj.getMethodName())) {
                continue;
            }
            User tmp_usr = new User();
            if (!map_obj.getMethodName().equals("setId")) {
                // resolv by name
                tmp_usr.setId(srv_loaded.getId());
            } else {
                // server must resolv by name
                tmp_usr.setName(srv_loaded.getName());
            }

            if (map_obj.getMethodParameterType().equals("java.lang.Integer")) {
                map_obj.getSetter().invoke(tmp_usr, new Object[] { Integer.valueOf(-1) });

                System.out.println("Setting -1 via " + map_obj.getMethodName() + " -> " + map_obj.getGetter().invoke(tmp_usr));
            } else if (map_obj.getMethodParameterType().equals("java.lang.Boolean")) {
                map_obj.getSetter().invoke(tmp_usr, new Object[] { Boolean.FALSE });

                System.out.println("Setting false via " + map_obj.getMethodName() + " -> " + map_obj.getGetter().invoke(tmp_usr));
            } else {
                map_obj.getSetter().invoke(tmp_usr, new Object[] { null });

                System.out.println("Setting null via " + map_obj.getMethodName() + " -> " + map_obj.getGetter().invoke(tmp_usr));
            }

            // submit changes
            oxu.change(ctx, tmp_usr, cred);

            // load from server and compare the single changed value
            final User user_single_change_loaded = oxu.getData(ctx, id(srv_loaded), cred);

            if (!notallowed.contains(map_obj.getMethodName())) {
                // local and remote must be null
                assertEquals(map_obj.getGetter().getName().substring(3) + " not equal", map_obj.getGetter().invoke(tmp_usr), map_obj.getGetter().invoke(user_single_change_loaded));
            } else {
                // we wanted to change a attribute which cannot be changed by
                // server, so we check for not null
                assertNotNull(map_obj.getMethodName() + " cannot be null", map_obj.getGetter().invoke(user_single_change_loaded));
            }
        }
    }

    /**
     * This test should fail
     *
     * @throws Exception
     */
    @Test(expected = InvalidDataException.class)
    public void testChangeAllAttributesNull() throws Exception {
        // set all values to null in the user object and then call change, what
        // happens?

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User usr = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, usr, access, cred, null);
        // now load user from server and check if data is correct, else fail
        User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }

        // loop through methods and change each attribute per single call and load and compare
        MethodMapObject[] meth_objects = getSetableAttributeMethods(usr.getClass());
        User tmp_usr = new User();
        for (MethodMapObject map_obj : meth_objects) {
            if (!map_obj.getMethodName().equals("setId")) {
                // resolv by name
                tmp_usr.setId(srv_loaded.getId());
            } else {
                // server must resolv by name
                tmp_usr.setName(srv_loaded.getName());
            }
            if (!map_obj.getMethodName().equals("setUserAttribute")) {
                map_obj.getSetter().invoke(tmp_usr, new Object[] { null });
                System.out.println("Setting null via " + map_obj.getMethodName() + " -> " + map_obj.getGetter().invoke(tmp_usr));
            }

        }

        // submit changes
        oxu.change(ctx, tmp_usr, cred);

        // load from server and compare the single changed value
        final User user_single_change_loaded = oxu.getData(ctx, id(srv_loaded), cred);

        // TODO
        // special compare must be written that checks for special attributes like username etc which cannot be null
        compareUserSpecialForNulledAttributes(tmp_usr, user_single_change_loaded);
    }

    @Test
    public void testChangeAllAllowedAttributesNull() throws Exception {
        // set all values to null in the user object and then call change, what
        // happens?

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User usr = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, usr, access, cred, null);
        // now load user from server and check if data is correct, else fail
        User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }

        HashSet<String> notallowed = getNotNullableFields();

        // loop through methods and change each attribute per single call and load and compare
        MethodMapObject[] meth_objects = getSetableAttributeMethods(usr.getClass());
        User tmp_usr = (User) createduser.clone();
        for (MethodMapObject map_obj : meth_objects) {
            if (!map_obj.getMethodName().equals("setId")) {
                // resolv by name
                tmp_usr.setId(srv_loaded.getId());
            } else {
                // server must resolv by name
                tmp_usr.setName(srv_loaded.getName());
            }

            if (notallowed.contains(map_obj.methodName)) {
                continue;
            }
            map_obj.getSetter().invoke(tmp_usr, new Object[] { null });

            System.out.println("Setting null via " + map_obj.getMethodName() + " -> " + map_obj.getGetter().invoke(tmp_usr));
        }

        // submit changes
        oxu.change(ctx, tmp_usr, cred);

        // load from server and compare the single changed value
        final User user_single_change_loaded = oxu.getData(ctx, id(srv_loaded), cred);

        // TODO
        // special compare must be written that checks for special attributes like username etc which cannot be null
        compareUserSpecialForNulledAttributes(tmp_usr, user_single_change_loaded);
    }

    private HashSet<String> getNotNullableFields() {
        final HashSet<String> notallowed = new HashSet<String>();
        notallowed.add("setEmail1");
        notallowed.add("setFolderTree");
        notallowed.add("setDefaultSenderAddress");
        notallowed.add("setId");
        final String[] mandatoryMembersCreate = new User().getMandatoryMembersCreate();
        for (final String name : mandatoryMembersCreate) {
            final StringBuilder sb = new StringBuilder("set");
            sb.append(name.substring(0, 1).toUpperCase());
            sb.append(name.substring(1));
            notallowed.add(sb.toString());
        }
        notallowed.add("setMail_folder_drafts_name");
        notallowed.add("setMail_folder_sent_name");
        notallowed.add("setMail_folder_spam_name");
        notallowed.add("setMail_folder_trash_name");
        notallowed.add("setMail_folder_confirmed_ham_name");
        notallowed.add("setMail_folder_confirmed_spam_name");
        notallowed.add("setMail_folder_archive_full_name");
        notallowed.add("setGUI_Spam_filter_capabilities_enabled");
        notallowed.add("setPassword_expired");
        notallowed.add("setMailenabled");
        notallowed.add("setLanguage");
        notallowed.add("setTimezone");
        notallowed.add("setPasswordMech");
        notallowed.add("setUserAttribute");
        notallowed.add("setFilestoreId");
        notallowed.add("setFilestore_name");
        notallowed.add("setFilestoreOwner");
        return notallowed;
    }

    @Test
    public void testChangeSingleAttribute() throws Exception {
        // change only 1 attribute of user object per call

        //      get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User usr = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, usr, access, cred, null);
        // now load user from server and check if data is correct, else fail
        User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }

        // which attributes should not be edited in a single change call
        // because of trouble when server needs combined attribute changed like mail attributes
        // or server does not support it
        HashSet<String> notallowed = new HashSet<String>();

        // # mail attribs must be combined in a change #
        notallowed.add("setEmail1");
        notallowed.add("setFolderTree");
        notallowed.add("setPrimaryEmail");
        notallowed.add("setDefaultSenderAddress");
        notallowed.add("setMail_folder_drafts_name");
        notallowed.add("setMail_folder_sent_name");
        notallowed.add("setMail_folder_spam_name");
        notallowed.add("setMail_folder_trash_name");
        notallowed.add("setMail_folder_confirmed_ham_name");
        notallowed.add("setMail_folder_confirmed_spam_name");
        // #                                                                     #

        notallowed.add("setId");// we cannot change the id of a user, is a mandatory field for a change
        notallowed.add("setPassword");// server password is always different(crypted)
        notallowed.add("setPasswordMech");// server password is always different(crypted)
        notallowed.add("setName");// server does not support username change

        notallowed.add("setFilestoreId");
        notallowed.add("setFilestoreOwner");
        notallowed.add("setFilestore_name");
        // loop through methods and change each attribute per single call and load and compare
        MethodMapObject[] meth_objects = getSetableAttributeMethods(usr.getClass());

        for (MethodMapObject map_obj : meth_objects) {
            if (!notallowed.contains(map_obj.getMethodName())) {
                User tmp_usr = new User(srv_loaded.getId());
                if (map_obj.getMethodParameterType().equalsIgnoreCase("java.lang.String") && map_obj.getGetter().getParameterTypes().length == 0) {
                    String oldvalue = (String) map_obj.getGetter().invoke(srv_loaded);
                    if (map_obj.getMethodName().equals("setLanguage")) {
                        map_obj.getSetter().invoke(tmp_usr, "fr_FR");
                    } else if (map_obj.getMethodName().toLowerCase().contains("mail")) {
                        map_obj.getSetter().invoke(tmp_usr, getChangedEmailAddress(oldvalue, "_singlechange"));
                    } else {
                        map_obj.getSetter().invoke(tmp_usr, oldvalue + "-singlechange");
                    }
                    //System.out.println("Setting String via "+map_obj.getMethodName() +" -> "+map_obj.getGetter().invoke(tmp_usr));
                }
                if (map_obj.getMethodParameterType().equalsIgnoreCase("java.lang.Integer")) {
                    Integer oldvalue = (Integer) map_obj.getGetter().invoke(srv_loaded);
                    map_obj.getSetter().invoke(tmp_usr, oldvalue + 1);
                    //System.out.println("Setting Integer via "+map_obj.getMethodName() +" -> "+map_obj.getGetter().invoke(tmp_usr));
                }
                if (map_obj.getMethodParameterType().equalsIgnoreCase("java.lang.Boolean")) {
                    Boolean oldvalue = (Boolean) map_obj.getGetter().invoke(srv_loaded);
                    map_obj.getSetter().invoke(tmp_usr, !oldvalue);
                    //System.out.println("Setting Boolean via "+map_obj.getMethodName() +" -> "+map_obj.getGetter().invoke(tmp_usr));
                }
                if (map_obj.getMethodParameterType().equalsIgnoreCase("java.util.Date")) {
                    Date oldvalue = (Date) map_obj.getGetter().invoke(srv_loaded);
                    // set date to current +1 day
                    map_obj.getSetter().invoke(tmp_usr, new Date(oldvalue.getTime() + (24 * 60 * 60 * 1000)));
                    //System.out.println("Setting Date via "+map_obj.getMethodName() +" -> "+map_obj.getGetter().invoke(tmp_usr));
                }

                //  submit changes
                oxu.change(ctx, tmp_usr, cred);
                // load from server and compare the single changed value
                final User user_single_change_loaded = oxu.getData(ctx, id(srv_loaded), cred);

                // compare both string values , server and local copy must be same, else, the change was unsuccessfull
                if (map_obj.getGetter().getParameterTypes().length == 0) {
                    Object expected = map_obj.getGetter().invoke(tmp_usr);
                    Object actual = map_obj.getGetter().invoke(user_single_change_loaded);
                    assertEquals(map_obj.getGetter().getName().substring(3) + " not equal " + expected.getClass().getName() + " " + actual.getClass().getName(), expected, actual);
                }
            }
        }
    }

    public MethodMapObject[] getSetableAttributeMethods(final Class clazz) {

        Method[] theMethods = clazz.getMethods();
        List<MethodMapObject> tmplist = new ArrayList<MethodMapObject>();

        MethodMapObject map_obj = null;

        // first fill setter and other infos in map object
        for (Method method : theMethods) {
            String method_name = method.getName();
            if (method_name.startsWith("set")) {
                // check if it is a type we support
                if (method.getParameterTypes()[0].getName().equalsIgnoreCase("java.lang.String") || method.getParameterTypes()[0].getName().equalsIgnoreCase("java.lang.Integer") || method.getParameterTypes()[0].getName().equalsIgnoreCase("java.util.Date") || method.getParameterTypes()[0].getName().equalsIgnoreCase("java.lang.Boolean")) {

                    map_obj = new MethodMapObject();
                    map_obj.setMethodName(method_name);
                    map_obj.setMethodParameterType(method.getParameterTypes()[0].getName());
                    map_obj.setSetter(method);

                    tmplist.add(map_obj);
                }
            }
        }

        for (MethodMapObject obj_map : tmplist) {
            String obj_method_name = obj_map.getMethodName();
            for (Method method : theMethods) {
                String meth_name = method.getName();
                if (meth_name.startsWith("get")) {
                    if (meth_name.substring(3).equalsIgnoreCase(obj_method_name.substring(3))) {
                        obj_map.setGetter(method);
                        break;
                    }
                }
            }
        }

        // now fill the getter in the map obj

        return tmplist.toArray(new MethodMapObject[tmplist.size()]);
    }

    private class MethodMapObject {

        private Method getter = null;
        private Method setter = null;
        private String methodParameterType = null;
        private String methodName = null;

        /**
         * @return the getter
         */
        public Method getGetter() {
            return getter;
        }

        /**
         * @param getter the getter to set
         */
        public void setGetter(Method getter) {
            this.getter = getter;
        }

        /**
         * @return the methodName
         */
        public String getMethodName() {
            return methodName;
        }

        /**
         * @param methodName the methodName to set
         */
        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        /**
         * @return the methodType
         */
        public String getMethodParameterType() {
            return methodParameterType;
        }

        /**
         * @param methodType the methodType to set
         */
        public void setMethodParameterType(String methodType) {
            this.methodParameterType = methodType;
        }

        /**
         * @return the setter
         */
        public Method getSetter() {
            return setter;
        }

        /**
         * @param setter the setter to set
         */
        public void setSetter(Method setter) {
            this.setter = setter;
        }
    }

    @Test
    public void testChangeWithEmptyUserIdentifiedByID() throws Exception {
        // test a change with no data set only id set and compare the data aftewards

        // 1. create user
        // 2. check if user was created correctly
        // 3. change user data but send NO data
        // 4. load user again from server and compare with 1st created user
        //     must be equal, because we changed nothing

        // STEP 1
        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        // STEP 2
        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

        // STEP 3
        User emptyusr = new User(srv_loaded.getId());
        oxu.change(ctx, emptyusr, cred);

        // STEP 4
        // now load user from server and check if data is correct, else fail
        final User srv_loaded2 = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded2.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

    }

    @Test
    public void testChangeWithEmptyUserIdentifiedByName() throws Exception {
        // test a change with no data set ONLY username set and compare the data aftewards

        // STEP 1
        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        // STEP 2
        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

        // STEP 3
        User emptyusr = new User();
        emptyusr.setName(srv_loaded.getName());
        oxu.change(ctx, emptyusr, cred);

        // STEP 4
        // now load user from server and check if data is correct, else fail
        final User srv_loaded2 = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded2.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

    }

    @Test
    public void testChangeIdentifiedByName() throws Exception {
        // test a change with data set  but identified by username and compare the data aftewards

        // STEP 1
        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        // STEP 2
        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

        // STEP 3

        User emptyusr = createChangeUserData(srv_loaded);
        emptyusr.setId(null);// reset id, server must ident the user by username
        oxu.change(ctx, emptyusr, cred);

        // STEP 4
        // now load user from server and check if data is correct, else fail
        final User srv_loaded2 = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded2.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

    }

    @Test
    public void testChangeIdentifiedByID() throws Exception {
        // test a change with data set but identified by id and compare the data afterwards

        // STEP 1
        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, urs, access, cred, null);

        // STEP 2
        // now load user from server and check if data is correct, else fail
        final User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

        // STEP 3
        User emptyusr = createChangeUserData(srv_loaded);
        // reset username, server must ident the user by id
        // This is a dirty trick to circumvent the setter method. Don't do this at home ;-)
        final Field field = emptyusr.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(emptyusr, null);
        oxu.change(ctx, emptyusr, cred);

        // STEP 4
        // now load user from server and check if data is correct, else fail
        final User srv_loaded2 = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded2.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data for added user");
        }

    }

    @Test(expected = InvalidDataException.class)
    public void testChangeWithoutIdAndName() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final User usr = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        final User createduser = oxu.create(ctx, usr, access, cred, null);
        // now load user from server and check if data is correct, else fail
        User srv_loaded = oxu.getData(ctx, id(createduser), cred);
        if (createduser.getId().equals(srv_loaded.getId())) {
            //verify data
            compareUser(createduser, srv_loaded);
        } else {
            fail("Expected to get user data");
        }

        // now change data
        srv_loaded = createChangeUserData(srv_loaded);
        srv_loaded.setId(null);
        srv_loaded.setName(null);
        // submit changes
        oxu.change(ctx, srv_loaded, cred);
    }

    // This test is used to check how the change method deals with changing values which are null before changing
    @Test
    public void testChangeNullFields() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, Exception {
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);
        final OXLoginInterface oxl = (OXLoginInterface) Naming.lookup(getRMIHostUrl() + OXLoginInterface.RMI_NAME);
        // Here we get the user object of the admin from the database
        // The admin has no company set by default, so we can test here, how a change work on field's which
        // aren't set by default
        final User usr = oxl.login2User(ctx, cred);
        // passwordmech is set by login2user so we need to null it here for the change test
        // not to fail
        usr.setPasswordMech(null);
        final OXUserInterface user = (OXUserInterface) Naming.lookup(getRMIHostUrl() + OXUserInterface.RMI_NAME);
        usr.setNickname("test");

        usr.setCompany("test");
        usr.setSur_name("test");
        usr.setEmail1(usr.getPrimaryEmail());
        // Store username to be able to restore it after change
        final String username = usr.getName();
        // This is a dirty trick to circumvent the setter method. Don't do this at home ;-)
        final Field field = usr.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(usr, null);
        System.out.println(usr.isCompanyset());
        usr.setFilestoreId(null);
        usr.setMaxQuota(null);
        user.change(ctx, usr, cred);
        usr.setName(username);
        final User usr2 = oxl.login2User(ctx, cred);
        compareUser(usr, usr2);
    }

    public static User getTestUserMandatoryFieldsObject(final String ident, final String password) {
        final User usr = new User();

        usr.setName(ident);
        usr.setPassword(password);

        usr.setMailenabled(true);

        usr.setPrimaryEmail("primaryemail-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setEmail1("primaryemail-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setDisplay_name("Displayname " + ident);
        usr.setGiven_name(ident);
        usr.setSur_name("Lastname " + ident);

        return usr;
    }

    public static User getTestUserObject(final String ident, final String password, final Context context) {
        final User usr = new User();
        usr.setName(ident);
        usr.setPassword(password);
        usr.setMailenabled(true);
        usr.setPrimaryEmail("primaryemail-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setDisplay_name("Displayname " + ident);
        usr.setGiven_name(ident);
        usr.setSur_name("Lastname " + ident);
        usr.setLanguage("de_DE");
        // new for testing

        usr.setEmail1("primaryemail-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setEmail2("email2-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setEmail3("email3-" + ident + "@" + AbstractTest.TEST_DOMAIN);

        usr.setFilestoreId(null);
        usr.setFilestore_name(null);

        final HashSet<String> aliase = new HashSet<String>();
        aliase.add("alias1-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("alias2-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("alias3-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("email2-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("email3-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("primaryemail-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setAliases(aliase);

        final Calendar cal = Calendar.getInstance(TimeZones.UTC);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        usr.setBirthday(cal.getTime());
        usr.setAnniversary(cal.getTime());

        usr.setAssistant_name("assistants name");

        usr.setBranches("Branches");
        usr.setBusiness_category("Business Category");
        usr.setCity_business("Business City");
        usr.setCountry_business("Business Country");
        usr.setPostal_code_business("BusinessPostalCode");
        usr.setState_business("BusinessState");
        usr.setStreet_business("BusinessStreet");
        usr.setTelephone_callback("callback");
        usr.setCity_home("City");
        usr.setCommercial_register("CommercialRegister");
        usr.setCompany("Company");
        usr.setCountry_home("Country");
        usr.setDepartment("Department");
        usr.setEmployeeType("EmployeeType");
        usr.setFax_business("FaxBusiness");
        usr.setFax_home("FaxHome");
        usr.setFax_other("FaxOther");
        usr.setImapServer("imap://localhost:143");
        usr.setInstant_messenger1("InstantMessenger");
        usr.setInstant_messenger2("InstantMessenger2");
        usr.setTelephone_ip("IpPhone");
        usr.setTelephone_isdn("Isdn");
        usr.setMail_folder_drafts_name("MailFolderDrafts");
        usr.setMail_folder_sent_name("MailFolderSent");
        usr.setMail_folder_spam_name("MailFolderSpam");
        usr.setMail_folder_trash_name("MailFolderTrash");
        usr.setMail_folder_archive_full_name("MailFolderArchive");
        usr.setManager_name("ManagersName");
        usr.setMarital_status("MaritalStatus");
        usr.setCellular_telephone1("Mobile1");
        usr.setCellular_telephone2("Mobile2");
        usr.setInfo("MoreInfo");
        usr.setNickname("NickName");
        usr.setNote("Note");
        usr.setNumber_of_children("NumberOfChildren");
        usr.setNumber_of_employee("NumberOfEmployee");
        usr.setTelephone_pager("Pager");
        usr.setPassword_expired(false);
        usr.setTelephone_assistant("PhoneAssistant");
        usr.setTelephone_business1("PhoneBusiness");
        usr.setTelephone_business2("PhoneBusiness2");
        usr.setTelephone_car("PhoneCar");
        usr.setTelephone_company("PhoneCompany");
        usr.setTelephone_home1("PhoneHome");
        usr.setTelephone_home2("PhoneHome2");
        usr.setTelephone_other("PhoneOther");
        usr.setPosition("Position");
        usr.setPostal_code_home("PostalCode");
        usr.setEmail2("Privateemail2-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setEmail3("Privateemail3-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setProfession("Profession");
        usr.setTelephone_radio("Radio");
        usr.setRoom_number("1337");
        usr.setSales_volume("SalesVolume");
        usr.setCity_other("SecondCity");
        usr.setCountry_other("SecondCountry");
        usr.setMiddle_name("SecondName");
        usr.setPostal_code_other("SecondPostalCode");
        usr.setState_other("SecondState");
        usr.setStreet_other("SecondStreet");
        usr.setSmtpServer("smtp://localhost:25");
        usr.setSpouse_name("SpouseName");
        usr.setState_home("State");
        usr.setStreet_home("Street");
        usr.setSuffix("Suffix");
        usr.setTax_id("TaxId");
        usr.setTelephone_telex("Telex");
        usr.setTimezone("Europe/Berlin");
        usr.setTitle("Title");
        usr.setTelephone_ttytdd("TtyTdd");
        usr.setUrl("url");
        usr.setUserfield01("Userfield01");
        usr.setUserfield02("Userfield02");
        usr.setUserfield03("Userfield03");
        usr.setUserfield04("Userfield04");
        usr.setUserfield05("Userfield05");
        usr.setUserfield06("Userfield06");
        usr.setUserfield07("Userfield07");
        usr.setUserfield08("Userfield08");
        usr.setUserfield09("Userfield09");
        usr.setUserfield10("Userfield10");
        usr.setUserfield11("Userfield11");
        usr.setUserfield12("Userfield12");
        usr.setUserfield13("Userfield13");
        usr.setUserfield14("Userfield14");
        usr.setUserfield15("Userfield15");
        usr.setUserfield16("Userfield16");
        usr.setUserfield17("Userfield17");
        usr.setUserfield18("Userfield18");
        usr.setUserfield19("Userfield19");
        usr.setUserfield20("Userfield20");

        usr.setUserAttribute("com.openexchange.test", "simpleValue", "12");
        usr.setUserAttribute("com.openexchange.test", "staticValue", "42");
        usr.setUserAttribute("com.openexchange.test", "deleteMe", "23");

        return usr;
    }

    public static User getTestUserObject() {
        return getTestUserObject(VALID_CHAR_TESTUSER, "open-xchange", null);
    }

    //    private static int getContextID() throws Exception {
    //        final Credentials cred = DummyCredentials();
    //        final Context ctx = ContextTest.getTestContextObject(ContextTest.createNewContextID(cred), 10);
    //        final int id = ContextTest.addContext(ctx, getRMIHostUrl(), cred);
    //        return id;
    //    }

    public static void compareUser(final User a, final User b) {
        System.out.println("USERA" + a.toString());
        System.out.println("USERB" + b.toString());

        assertEquals("username not equal", a.getName(), b.getName());
        assertEquals("enabled not equal", a.getMailenabled(), b.getMailenabled());
        assertEquals("primaryemail not equal", a.getPrimaryEmail(), b.getPrimaryEmail());
        assertEquals("display name not equal", a.getDisplay_name(), b.getDisplay_name());
        assertEquals("firtname not equal", a.getGiven_name(), b.getGiven_name());
        assertEquals("lastname not equal", a.getSur_name(), b.getSur_name());
        assertEquals("language not equal", a.getLanguage(), b.getLanguage());
        // test aliasing comparing the content of the hashset
        assertEquals(a.getAliases(), b.getAliases());
        compareNonCriticFields(a, b);

    }

    private static void compareUserSpecialForNulledAttributes(final User a, final User b) {
        System.out.println("USERA" + a.toString());
        System.out.println("USERB" + b.toString());

        // all these attributes cannot be null | cannot changed by server to null/empty
        assertNotNull("username cannot be null", b.getName());
        assertNotNull("enabled cannot be null", b.getMailenabled());
        assertNotNull("primaryemail cannot be null", b.getPrimaryEmail());
        assertNotNull("display name cannot be null", b.getDisplay_name());
        assertNotNull("firstname name cannot be null", b.getGiven_name());
        assertNotNull("lastname name cannot be null", b.getSur_name());
        assertNotNull("language name cannot be null", b.getLanguage());

        // can alias be null?
        //assertEquals(a.getAliases(), b.getAliases());
        compareNonCriticFields(a, b);
    }

    private static void assertDatesAreEqualsAtYMD(String message, Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance(TimeZones.UTC);
        Calendar cal2 = Calendar.getInstance(TimeZones.UTC);
        if (date1 != null && date2 != null) {
            cal1.setTime(date1);
            cal2.setTime(date2);
            assertEquals(message, cal1.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
            assertEquals(message, cal1.get(Calendar.MONTH), cal2.get(Calendar.MONTH));
            assertEquals(message, cal1.get(Calendar.DAY_OF_MONTH), cal2.get(Calendar.DAY_OF_MONTH));
        }
    }

    private static void compareNonCriticFields(final User a, final User b) {
        assertDatesAreEqualsAtYMD("aniversary not equal", a.getAnniversary(), b.getAnniversary());
        assertEquals("assistants name not equal", a.getAssistant_name(), b.getAssistant_name());
        assertDatesAreEqualsAtYMD("birthday not equal", a.getBirthday(), b.getBirthday());
        assertEquals("branches not equal", a.getBranches(), b.getBranches());
        assertEquals("BusinessCategory not equal", a.getBusiness_category(), b.getBusiness_category());
        assertEquals("BusinessCity not equal", a.getCity_business(), b.getCity_business());
        assertEquals("BusinessCountry not equal", a.getCountry_business(), b.getCountry_business());
        assertEquals("BusinessPostalCode not equal", a.getPostal_code_business(), b.getPostal_code_business());
        assertEquals("BusinessState not equal", a.getState_business(), b.getState_business());
        assertEquals("BusinessStreet not equal", a.getStreet_business(), b.getStreet_business());
        assertEquals("callback not equal", a.getTelephone_callback(), b.getTelephone_callback());
        assertEquals("CommercialRegister not equal", a.getCommercial_register(), b.getCommercial_register());
        assertEquals("Company not equal", a.getCompany(), b.getCompany());
        assertEquals("Country not equal", a.getCountry_home(), b.getCountry_home());
        assertEquals("Department not equal", a.getDepartment(), b.getDepartment());
        assertEquals("EmployeeType not equal", a.getEmployeeType(), b.getEmployeeType());
        assertEquals("FaxBusiness not equal", a.getFax_business(), b.getFax_business());
        assertEquals("FaxHome not equal", a.getFax_home(), b.getFax_home());
        assertEquals("FaxOther not equal", a.getFax_other(), b.getFax_other());
        assertEquals("ImapServer not equal", a.getImapServerString(), b.getImapServerString());
        assertEquals("InstantMessenger not equal", a.getInstant_messenger1(), b.getInstant_messenger1());
        assertEquals("InstantMessenger2 not equal", a.getInstant_messenger2(), b.getInstant_messenger2());
        assertEquals("IpPhone not equal", a.getTelephone_ip(), b.getTelephone_ip());
        assertEquals("Isdn not equal", a.getTelephone_isdn(), b.getTelephone_isdn());
        assertEquals("MailFolderDrafts not equal", a.getMail_folder_drafts_name(), b.getMail_folder_drafts_name());
        assertEquals("MailFolderSent not equal", a.getMail_folder_sent_name(), b.getMail_folder_sent_name());
        assertEquals("MailFolderSpam not equal", a.getMail_folder_spam_name(), b.getMail_folder_spam_name());
        assertEquals("MailFolderTrash not equal", a.getMail_folder_trash_name(), b.getMail_folder_trash_name());
        assertEquals("MailFolderArchiveFull not equal", a.getMail_folder_archive_full_name(), b.getMail_folder_archive_full_name());
        assertEquals("ManagersName not equal", a.getManager_name(), b.getManager_name());
        assertEquals("MaritalStatus not equal", a.getMarital_status(), b.getMarital_status());
        assertEquals("Mobile1 not equal", a.getCellular_telephone1(), b.getCellular_telephone1());
        assertEquals("Mobile2 not equal", a.getCellular_telephone2(), b.getCellular_telephone2());
        assertEquals("MoreInfo not equal", a.getInfo(), b.getInfo());
        assertEquals("NickName not equal", a.getNickname(), b.getNickname());
        assertEquals("Note not equal", a.getNote(), b.getNote());
        assertEquals("NumberOfChildren not equal", a.getNumber_of_children(), b.getNumber_of_children());
        assertEquals("NumberOfEmployee not equal", a.getNumber_of_employee(), b.getNumber_of_employee());
        assertEquals("Pager not equal", a.getTelephone_pager(), b.getTelephone_pager());
        assertEquals("PasswordExpired not equal", a.getPassword_expired(), b.getPassword_expired());
        assertEquals("PhoneAssistant not equal", a.getTelephone_assistant(), b.getTelephone_assistant());
        assertEquals("PhoneBusiness not equal", a.getTelephone_business1(), b.getTelephone_business1());
        assertEquals("PhoneBusiness2 not equal", a.getTelephone_business2(), b.getTelephone_business2());
        assertEquals("PhoneCar not equal", a.getTelephone_car(), b.getTelephone_car());
        assertEquals("PhoneCompany not equal", a.getTelephone_company(), b.getTelephone_company());
        assertEquals("PhoneHome not equal", a.getTelephone_home1(), b.getTelephone_home1());
        assertEquals("PhoneHome2 not equal", a.getTelephone_home2(), b.getTelephone_home2());
        assertEquals("PhoneOther not equal", a.getTelephone_other(), b.getTelephone_other());
        assertEquals("Position not equal", a.getPosition(), b.getPosition());
        assertEquals("PostalCode not equal", a.getPostal_code_home(), b.getPostal_code_home());
        assertEquals("Email2 not equal", a.getEmail2(), b.getEmail2());
        assertEquals("Email3 not equal", a.getEmail3(), b.getEmail3());
        assertEquals("Profession not equal", a.getProfession(), b.getProfession());
        assertEquals("Radio not equal", a.getTelephone_radio(), b.getTelephone_radio());
        assertEquals("RoomNumber not equal", a.getRoom_number(), b.getRoom_number());
        assertEquals("SalesVolume not equal", a.getSales_volume(), b.getSales_volume());
        assertEquals("SecondCity not equal", a.getCity_other(), b.getCity_other());
        assertEquals("SecondCountry not equal", a.getCountry_other(), b.getCountry_other());
        assertEquals("SecondName not equal", a.getMiddle_name(), b.getMiddle_name());
        assertEquals("SecondPostalCode not equal", a.getPostal_code_other(), b.getPostal_code_other());
        assertEquals("SecondState not equal", a.getState_other(), b.getState_other());
        assertEquals("SecondStreet not equal", a.getStreet_other(), b.getStreet_other());
        assertEquals("SmtpServer not equal", a.getSmtpServerString(), b.getSmtpServerString());
        assertEquals("SpouseName not equal", a.getSpouse_name(), b.getSpouse_name());
        assertEquals("State not equal", a.getState_home(), b.getState_home());
        assertEquals("Street not equal", a.getStreet_home(), b.getStreet_home());
        assertEquals("Suffix not equal", a.getSuffix(), b.getSuffix());
        assertEquals("TaxId not equal", a.getTax_id(), b.getTax_id());
        assertEquals("Telex not equal", a.getTelephone_telex(), b.getTelephone_telex());
        assertEquals("Timezone not equal", a.getTimezone(), b.getTimezone());
        assertEquals("Title not equal", a.getTitle(), b.getTitle());
        assertEquals("TtyTdd not equal", a.getTelephone_ttytdd(), b.getTelephone_ttytdd());
        assertEquals("Url not equal", a.getUrl(), b.getUrl());
        assertEquals("Userfield01 not equal", a.getUserfield01(), b.getUserfield01());
        assertEquals("Userfield02 not equal", a.getUserfield02(), b.getUserfield02());
        assertEquals("Userfield03 not equal", a.getUserfield03(), b.getUserfield03());
        assertEquals("Userfield04 not equal", a.getUserfield04(), b.getUserfield04());
        assertEquals("Userfield05 not equal", a.getUserfield05(), b.getUserfield05());
        assertEquals("Userfield06 not equal", a.getUserfield06(), b.getUserfield06());
        assertEquals("Userfield07 not equal", a.getUserfield07(), b.getUserfield07());
        assertEquals("Userfield08 not equal", a.getUserfield08(), b.getUserfield08());
        assertEquals("Userfield09 not equal", a.getUserfield09(), b.getUserfield09());
        assertEquals("Userfield10 not equal", a.getUserfield10(), b.getUserfield10());
        assertEquals("Userfield11 not equal", a.getUserfield11(), b.getUserfield11());
        assertEquals("Userfield12 not equal", a.getUserfield12(), b.getUserfield12());
        assertEquals("Userfield13 not equal", a.getUserfield13(), b.getUserfield13());
        assertEquals("Userfield14 not equal", a.getUserfield14(), b.getUserfield14());
        assertEquals("Userfield15 not equal", a.getUserfield15(), b.getUserfield15());
        assertEquals("Userfield16 not equal", a.getUserfield16(), b.getUserfield16());
        assertEquals("Userfield17 not equal", a.getUserfield17(), b.getUserfield17());
        assertEquals("Userfield18 not equal", a.getUserfield18(), b.getUserfield18());
        assertEquals("Userfield19 not equal", a.getUserfield19(), b.getUserfield19());
        assertEquals("Userfield20 not equal", a.getUserfield20(), b.getUserfield20());
        final Hashtable<String, OXCommonExtension> aexts = a.getAllExtensionsAsHash();
        final Hashtable<String, OXCommonExtension> bexts = b.getAllExtensionsAsHash();
        if (aexts.size() == bexts.size()) {
            assertTrue("Extensions not equal: " + aexts.toString() + ",\n" + bexts.toString(), aexts.values().containsAll(bexts.values()));
            //          for (int i = 0; i < aexts.size(); i++) {
            //          final OXCommonExtensionInterface aext = aexts.get(i);
            //          final OXCommonExtensionInterface bext = bexts.get(i);
            //          assertTrue("Extensions not equal: " + aext.toString() + ",\n" + bext.toString(), aext.equals(bext));
            //          }
        }

        assertEquals("User Attributes not equal", a.getUserAttributes(), b.getUserAttributes());
    }

    private void compareUserAccess(final UserModuleAccess a, final UserModuleAccess b) {
        assertEquals("access calendar not equal", a.getCalendar(), b.getCalendar());
        assertEquals("access contacts not equal", a.getContacts(), b.getContacts());
        assertEquals("access delegatetasks not equal", a.getDelegateTask(), b.getDelegateTask());
        assertEquals("access edit public folders not equal", a.getEditPublicFolders(), b.getEditPublicFolders());
        assertEquals("access ical not equal", a.getIcal(), b.getIcal());
        assertEquals("access infostore not equal", a.getInfostore(), b.getInfostore());
        assertEquals("access ReadCreateSharedFolders not equal", a.getReadCreateSharedFolders(), b.getReadCreateSharedFolders());
        assertEquals("access syncml not equal", a.getSyncml(), b.getSyncml());
        assertEquals("access tasks not equal", a.getTasks(), b.getTasks());
        assertEquals("access vcard not equal", a.getVcard(), b.getVcard());
        assertEquals("access webdav not equal", a.getWebdav(), b.getWebdav());
        assertEquals("access webdav xml not equal", a.getWebdavXml(), b.getWebdavXml());
        assertEquals("access webmail not equal", a.getWebmail(), b.getWebmail());
    }

    public static User addUser(final Context ctx, final User usr, final UserModuleAccess access) throws Exception {
        // create new user
        final OXUserInterface oxu = getUserClient();
        return oxu.create(ctx, usr, access, DummyCredentials(), null);
    }

    //Uncomment this to use another context that 1
    //    public static Context getTestContextObject(final Credentials cred) {
    //        return getTestContextObject(1, 50);
    //    }

    private User createChangeUserData(final User usr) throws CloneNotSupportedException, URISyntaxException {

        // change all fields of the user

        final User retval = (User) usr.clone();
        retval.setFilestoreId(null);
        //retval.setName(null); // INFO: Commented because the server does not throw any exception if username is sent!
        retval.setPasswordMech(null);
        retval.setMailenabled(!usr.getMailenabled());

        // do not change primary mail, that's forbidden per default, see
        //PRIMARY_MAIL_UNCHANGEABLE in User.properties
        // retval.setPrimaryEmail(usr.getPrimaryEmail()+change_suffix);
        //retval.setEmail1(usr.getEmail1()+change_suffix);
        //retval.setDefaultSenderAddress(usr.getPrimaryEmail()+change_suffix);
        retval.setEmail2(getChangedEmailAddress(usr.getEmail2(), change_suffix));
        retval.setEmail3(getChangedEmailAddress(usr.getEmail3(), change_suffix));

        retval.setDisplay_name(usr.getDisplay_name() + change_suffix);
        retval.setGiven_name(usr.getGiven_name() + change_suffix);
        retval.setSur_name(usr.getSur_name() + change_suffix);
        retval.setLanguage("en_US");
        // new for testing

        final HashSet<String> aliases = usr.getAliases();
        final HashSet<String> lAliases = new HashSet<String>();
        for (final String element : aliases) {
            lAliases.add(getChangedEmailAddress(element, change_suffix));
        }
        lAliases.add(usr.getPrimaryEmail());

        retval.setAliases(lAliases);

        // set the dates to the actual + 1 day
        retval.setBirthday(new Date(usr.getBirthday().getTime() + (24 * 60 * 60 * 1000)));
        retval.setAnniversary(new Date(usr.getAnniversary().getTime() + (24 * 60 * 60 * 1000)));
        retval.setAssistant_name(usr.getAssistant_name() + change_suffix);
        retval.setBranches(usr.getBranches() + change_suffix);
        retval.setBusiness_category(usr.getBusiness_category() + change_suffix);
        retval.setCity_business(usr.getCity_business() + change_suffix);
        retval.setCountry_business(usr.getCountry_business() + change_suffix);
        retval.setPostal_code_business(usr.getPostal_code_business() + change_suffix);
        retval.setState_business(usr.getState_business() + change_suffix);
        retval.setStreet_business(usr.getStreet_business() + change_suffix);
        retval.setTelephone_callback(usr.getTelephone_callback() + change_suffix);
        retval.setCity_home(usr.getCity_home() + change_suffix);
        retval.setCommercial_register(usr.getCommercial_register() + change_suffix);
        retval.setCompany(usr.getCompany() + change_suffix);
        retval.setCountry_home(usr.getCountry_home() + change_suffix);
        retval.setDepartment(usr.getDepartment() + change_suffix);
        retval.setEmployeeType(usr.getEmployeeType() + change_suffix);
        retval.setFax_business(usr.getFax_business() + change_suffix);
        retval.setFax_home(usr.getFax_home() + change_suffix);
        retval.setFax_other(usr.getFax_other() + change_suffix);
        URI uri = new URI(usr.getImapServerString());
        retval.setImapServer(new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost() + change_suffix, uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString());
        retval.setInstant_messenger1(usr.getInstant_messenger1() + change_suffix);
        retval.setInstant_messenger2(usr.getInstant_messenger2() + change_suffix);
        retval.setTelephone_ip(usr.getTelephone_ip() + change_suffix);
        retval.setTelephone_isdn(usr.getTelephone_isdn() + change_suffix);
        retval.setMail_folder_drafts_name(usr.getMail_folder_drafts_name() + change_suffix);
        retval.setMail_folder_sent_name(usr.getMail_folder_sent_name() + change_suffix);
        retval.setMail_folder_spam_name(usr.getMail_folder_spam_name() + change_suffix);
        retval.setMail_folder_trash_name(usr.getMail_folder_trash_name() + change_suffix);
        retval.setMail_folder_archive_full_name(usr.getMail_folder_archive_full_name() + change_suffix);
        retval.setManager_name(usr.getManager_name() + change_suffix);
        retval.setMarital_status(usr.getMarital_status() + change_suffix);
        retval.setCellular_telephone1(usr.getCellular_telephone1() + change_suffix);
        retval.setCellular_telephone2(usr.getCellular_telephone2() + change_suffix);
        retval.setInfo(usr.getInfo() + change_suffix);
        retval.setNickname(usr.getNickname() + change_suffix);
        retval.setNote(usr.getNote() + change_suffix);
        retval.setNumber_of_children(usr.getNumber_of_children() + change_suffix);
        retval.setNumber_of_employee(usr.getNumber_of_employee() + change_suffix);
        retval.setTelephone_pager(usr.getTelephone_pager() + change_suffix);
        retval.setPassword_expired(!usr.getPassword_expired());
        retval.setTelephone_assistant(usr.getTelephone_assistant() + change_suffix);
        retval.setTelephone_business1(usr.getTelephone_business1() + change_suffix);
        retval.setTelephone_business2(usr.getTelephone_business2() + change_suffix);
        retval.setTelephone_car(usr.getTelephone_car() + change_suffix);
        retval.setTelephone_company(usr.getTelephone_company() + change_suffix);
        retval.setTelephone_home1(usr.getTelephone_home1() + change_suffix);
        retval.setTelephone_home2(usr.getTelephone_home2() + change_suffix);
        retval.setTelephone_other(usr.getTelephone_other() + change_suffix);
        retval.setPosition(usr.getPosition() + change_suffix);
        retval.setPostal_code_home(usr.getPostal_code_home() + change_suffix);
        retval.setProfession(usr.getProfession() + change_suffix);
        retval.setTelephone_radio(usr.getTelephone_radio() + change_suffix);
        retval.setRoom_number(usr.getRoom_number() + change_suffix);
        retval.setSales_volume(usr.getSales_volume() + change_suffix);
        retval.setCity_other(usr.getCity_other() + change_suffix);
        retval.setCountry_other(usr.getCountry_other() + change_suffix);
        retval.setMiddle_name(usr.getMiddle_name() + change_suffix);
        retval.setPostal_code_other(usr.getPostal_code_other() + change_suffix);
        retval.setState_other(usr.getState_other() + change_suffix);
        retval.setStreet_other(usr.getStreet_other() + change_suffix);
        uri = new URI(usr.getSmtpServerString());
        retval.setSmtpServer(new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost() + change_suffix, uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString());
        retval.setSpouse_name(usr.getSpouse_name() + change_suffix);
        retval.setState_home(usr.getState_home() + change_suffix);
        retval.setStreet_home(usr.getStreet_home() + change_suffix);
        retval.setSuffix(usr.getSuffix() + change_suffix);
        retval.setTax_id(usr.getTax_id() + change_suffix);
        retval.setTelephone_telex(usr.getTelephone_telex() + change_suffix);
        retval.setTimezone(usr.getTimezone());
        retval.setTitle(usr.getTitle() + change_suffix);
        retval.setTelephone_ttytdd(usr.getTelephone_ttytdd() + change_suffix);
        retval.setUrl(usr.getUrl() + change_suffix);
        retval.setUserfield01(usr.getUserfield01() + change_suffix);
        retval.setUserfield02(usr.getUserfield02() + change_suffix);
        retval.setUserfield03(usr.getUserfield03() + change_suffix);
        retval.setUserfield04(usr.getUserfield04() + change_suffix);
        retval.setUserfield05(usr.getUserfield05() + change_suffix);
        retval.setUserfield06(usr.getUserfield06() + change_suffix);
        retval.setUserfield07(usr.getUserfield07() + change_suffix);
        retval.setUserfield08(usr.getUserfield08() + change_suffix);
        retval.setUserfield09(usr.getUserfield09() + change_suffix);
        retval.setUserfield10(usr.getUserfield10() + change_suffix);
        retval.setUserfield11(usr.getUserfield11() + change_suffix);
        retval.setUserfield12(usr.getUserfield12() + change_suffix);
        retval.setUserfield13(usr.getUserfield13() + change_suffix);
        retval.setUserfield14(usr.getUserfield14() + change_suffix);
        retval.setUserfield15(usr.getUserfield15() + change_suffix);
        retval.setUserfield16(usr.getUserfield16() + change_suffix);
        retval.setUserfield17(usr.getUserfield17() + change_suffix);
        retval.setUserfield18(usr.getUserfield18() + change_suffix);
        retval.setUserfield19(usr.getUserfield19() + change_suffix);
        retval.setUserfield20(usr.getUserfield20() + change_suffix);

        retval.setUserAttribute("com.openexchange.test", "simpleValue", usr.getUserAttribute("com.openexchange.test", "simpleValue") + change_suffix);
        retval.setUserAttribute("com.openexchange.test", "newValue", change_suffix);
        retval.setUserAttribute("com.openexchange.test", "deleteMe", null);
        // Remove value
        return retval;
    }

    @Test
    public void testExists() throws Exception {

        // get context to create an user
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(cred);

        // create new user
        final OXUserInterface oxu = getUserClient();

        final User exists = getTestUserObject(VALID_CHAR_TESTUSER + System.currentTimeMillis(), pass, ctx);
        User notexists = new User();
        notexists.setName("Rumpelstilz");
        final User createduser = oxu.create(ctx, exists, cred, null);

        boolean existingexists = false;
        try {
            existingexists = oxu.exists(ctx, exists, cred);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // delete user
        oxu.delete(ctx, createduser, null, cred);

        try {
            assertFalse("nonexisting user must not exist", oxu.exists(ctx, notexists, cred));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue("created user does not exist", existingexists);
    }

}
