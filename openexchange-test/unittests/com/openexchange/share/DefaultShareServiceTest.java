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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.TestServiceRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.SessionModifyCallback;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.impl.DefaultShareService;
import com.openexchange.test.AjaxInit;
import com.openexchange.test.TestInit;

/**
 * {@link DefaultShareServiceTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class DefaultShareServiceTest extends TestCase {

    private static boolean init;
    private Context ctx;
    private List<User> users;
    private DefaultShareService service;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestInit.loadTestProperties();
        if (!init) {
            Init.startServer();
            init = true;
        }
        String context = AjaxInit.getAJAXProperty("contextName");
        ctx = ContextStorage.getStorageContext(getContextId(context));
        String login1 = AjaxInit.getAJAXProperty("login");
        String login2 = AjaxInit.getAJAXProperty("seconduser");
        String login3 = AjaxInit.getAJAXProperty("thirdlogin");
        String login4 = AjaxInit.getAJAXProperty("fourthlogin");
        User user1 = UserStorage.getInstance().getUser(getUserId(login1), ctx);
        User user2 = UserStorage.getInstance().getUser(getUserId(login2), ctx);
        User user3 = UserStorage.getInstance().getUser(getUserId(login3), ctx);
        User user4 = UserStorage.getInstance().getUser(getUserId(login4), ctx);
        users = new ArrayList<User>(4);
        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);
        service = TestServiceRegistry.getInstance().getService(DefaultShareService.class);
//        createTestShares();
    }

    @Override
    public void tearDown() throws Exception {
//        service.removeShares(ctx.getContextId());
        if (init) {
            Init.stopServer();
        }
        super.tearDown();
    }

    private int getContextId(String login) throws OXException {
        int ctxId = -1;
        int pos = login.indexOf("@");
        String ctxName = login.substring(pos + 1);
        ctxId = ContextStorage.getInstance().getContextId(ctxName);
        return ctxId;
    }

    private int getUserId(String login) throws OXException {
        int userId = -1;
        int pos = login.indexOf("@");
        if (pos > -1) {
            String userName = login.substring(0, pos);
            userId = UserStorage.getInstance().getUserId(userName, ctx);
        } else {
            userId = UserStorage.getInstance().getUserId(login, ctx);
        }
        return userId;
    }

    private Session createSession(final User user, final Context context) throws OXException {
        SessiondService sessiondService = TestServiceRegistry.getInstance().getService(SessiondService.class);
        return sessiondService.addSession(new AddSessionParameter() {

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public String getUserLoginInfo() {
                return user.getLoginInfo();
            }

            @Override
            public int getUserId() {
                return user.getId();
            }

            @Override
            public String getPassword() {
                return "";
            }

            @Override
            public String getHash() {
                return user.getDisplayName() + "hash";
            }

            @Override
            public String getFullLogin() {
                return user.getLoginInfo() + "@" + context.getName();
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public String getClientToken() {
                return "UnittestToken";
            }

            @Override
            public String getClientIP() {
                return "localhost";
            }

            @Override
            public String getClient() {
                return "Unittest";
            }

            @Override
            public String getAuthId() {
                return UUID.randomUUID().toString();
            }

            @Override
            public SessionModifyCallback getCallback() {
                return null;
            }
        });
    }

//    private void createTestShares() throws OXException {
//        for (User user : users) {
//            Session session = createSession(user, ctx);
//            OXFolderAccess access = new OXFolderAccess(ctx);
//            FolderObject share1 = access.getDefaultFolder(user.getId(), FolderObject.CONTACT);
//            int count = new Random().nextInt(5);
//            List<ShareRecipient> guests = new ArrayList<ShareRecipient>(count);
//            for (int i = 1; i <= count; i++) {
//                guests.add(new AnonymousRecipient());
//            }
//            service.addTarget(session, new Share(share1.getModule(), String.valueOf(share1.getObjectID())), guests);
//
//            FolderObject share2 = access.getDefaultFolder(user.getId(), FolderObject.CALENDAR);
//            count = new Random().nextInt(5);
//            guests = new ArrayList<ShareRecipient>(count);
//            for (int i = 1; i <= count; i++) {
//                guests.add(new AnonymousRecipient());
//            }
//            service.addTarget(session, new Share(share2.getModule(), String.valueOf(share2.getObjectID())), guests);
//        }
//    }
//
//    public void testRemoveByUserAndContext() throws Exception {
//        int user = users.get(new Random().nextInt(users.size())).getId();
//        List<ShareList> before = service.getAllShares(ctx.getContextId(), user);
//        List<ShareList> all = service.getAllShares(ctx.getContextId());
//        if (before.size() > 0) {
//            service.removeShares(ctx.getContextId(), user);
//            List<ShareList> after = service.getAllShares(ctx.getContextId(), user);
//            assertEquals("Not all shares got deleted.", all.size() - before.size(), after.size());
//        }
//    }
//
//    public void testRemoveByToken() throws Exception {
//        List<ShareList> before = service.getAllShares(ctx.getContextId());
//        List<String> toDelete = new ArrayList<String>(before.size());
//        for (ShareList share : before) {
//            boolean delete = new Random().nextBoolean();
//            if (delete) {
//                toDelete.add(share.getToken());
//            }
//        }
//        service.removeShares(toDelete);
//        List<ShareList> after = service.getAllShares(ctx.getContextId());
//        assertEquals("Other shares deleted.", before.size() - toDelete.size(), after.size());
//        for (ShareList share : after) {
//            String token = share.getToken();
//            assertFalse("Token " + token + " should have been deleted.", toDelete.contains(token));
//        }
//    }
//
//    public void testRemoveByTokenAndContext() throws Exception {
//        List<ShareList> before = service.getAllShares(ctx.getContextId());
//        List<String> toDelete = new ArrayList<String>(before.size());
//        for (ShareList share : before) {
//            boolean delete = new Random().nextBoolean();
//            if (delete) {
//                toDelete.add(share.getToken());
//            }
//        }
//        service.removeShares(toDelete);
//        List<ShareList> after = service.getAllShares(ctx.getContextId());
//        assertEquals("Other shares deleted.", before.size() - toDelete.size(), after.size());
//        for (ShareList share : after) {
//            String token = share.getToken();
//            assertFalse("Token " + token + " should have been deleted.", toDelete.contains(token));
//        }
//    }
//
//    public void testRemoveByContext() throws Exception {
//        service.removeShares(ctx.getContextId());
//        List<ShareList> shares = service.getAllShares(ctx.getContextId());
//        assertTrue("Not all shares in context have been deleted.", shares.isEmpty());
//    }
//
//    public void testListByContext() throws Exception {
//        List<ShareList> shares = service.getAllShares(ctx.getContextId());
//        assertFalse("No shares found.", shares.isEmpty());
//        for (ShareList share : shares) {
//            assertEquals("Share does not belong to context.", ctx.getContextId(), share.getContextID());
//        }
//    }
//
//    public void testListByContextAndUser() throws Exception {
//        int user = users.get(new Random().nextInt(users.size())).getId();
//        List<ShareList> shares = service.getAllShares(ctx.getContextId(), user);
//        assertFalse("No shares found.", shares.isEmpty());
//        for (ShareList share : shares) {
//            assertEquals("Share does not belong to context.", ctx.getContextId(), share.getContextID());
//            assertEquals("Share was not created by user.", user, share.getCreatedBy());
//        }
//    }
//
}
