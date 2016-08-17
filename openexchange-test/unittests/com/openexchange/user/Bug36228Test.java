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

package com.openexchange.user;

import java.util.Date;
import java.util.UUID;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.TestServiceRegistry;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.sessiond.AddSessionParameter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.test.AjaxInit;
import com.openexchange.test.TestInit;
import junit.framework.TestCase;


/**
 * {@link Bug36228Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.2
 */
public class Bug36228Test extends TestCase {

    private static boolean init;
    private final String NEW_NAME = "testBug36228";
    private UserService us;
    private ContactService cs;
    private Context context;
    private User user;
    private Contact contact;
    private String oldName;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestInit.loadTestProperties();
        if (!init) {
            Init.startServer();
            init = true;
        }
        us = TestServiceRegistry.getInstance().getService(UserService.class);
        cs = TestServiceRegistry.getInstance().getService(ContactService.class);
        String contextName = AjaxInit.getAJAXProperty("contextName");
        context = ContextStorage.getStorageContext(getContextId(contextName));
        String login = AjaxInit.getAJAXProperty("login");
        int userId = getUserId(login);
        user = UserStorage.getInstance().getUser(userId, context);
    }

    @Override
    public void tearDown() throws Exception {
        if (null != oldName) {
            Contact update = new Contact();
            update.setSurName(oldName);
            cs.updateUser(createSession(user, context), String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID), String.valueOf(contact.getObjectID()), update, new Date());
        }
        super.tearDown();
    }

    public void testBug36228() throws Exception {
        Session session = createSession(user, context);
        contact = cs.getUser(session, user.getId());
        oldName = contact.getSurName();

        Contact update = new Contact();
        update.setSurName(NEW_NAME);
        cs.updateUser(session, String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID), String.valueOf(contact.getObjectID()), update, new Date());

        User u = us.getUser(user.getId(), context);
        Contact loaded = cs.getContact(session, String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID), String.valueOf(u.getContactId()));
        assertEquals("", NEW_NAME, loaded.getSurName());
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
            userId = UserStorage.getInstance().getUserId(userName, context);
        } else {
            userId = UserStorage.getInstance().getUserId(login, context);
        }
        return userId;
    }

    private Session createSession(final User u, final Context c) throws OXException {
        SessiondService sessiondService = TestServiceRegistry.getInstance().getService(SessiondService.class);
        return sessiondService.addSession(new AddSessionParameter() {

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public String getUserLoginInfo() {
                return u.getLoginInfo();
            }

            @Override
            public int getUserId() {
                return u.getId();
            }

            @Override
            public String getPassword() {
                return "";
            }

            @Override
            public String getHash() {
                return u.getDisplayName() + "hash";
            }

            @Override
            public String getFullLogin() {
                return u.getLoginInfo() + "@" + c.getName();
            }

            @Override
            public Context getContext() {
                return c;
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
            public SessionEnhancement getEnhancement() {
                return null;
            }
        });
    }

}
