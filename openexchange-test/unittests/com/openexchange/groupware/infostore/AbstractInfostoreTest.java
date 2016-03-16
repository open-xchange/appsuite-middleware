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

package com.openexchange.groupware.infostore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.test.TestInit;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionFactory;


/**
 * {@link AbstractInfostoreTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class AbstractInfostoreTest extends TestCase{
    protected InfostoreFacade infostore;

    protected Context ctx = null;
    protected User user = null;
    protected User user2 = null;

    protected UserPermissionBits permissionBits = null;
    protected UserPermissionBits permissionBits2 = null;

    protected int folderId;
    protected int folderId2;

    protected ServerSession session;
    protected ServerSession session2;

    protected List<DocumentMetadata> clean;
    protected List<FolderObject> cleanFolders = null;

    protected DBProvider provider = null;

    @Override
    public void setUp() throws Exception {
        clean = new ArrayList<DocumentMetadata>();
        cleanFolders = new ArrayList<FolderObject>();

        TestInit.loadTestProperties();
        Init.startServer();

        ContextStorage.getInstance();

        final TestConfig config = new TestConfig();
        final String userName = config.getUser();
        final String userName2 = config.getSecondUser();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);
        user = UserStorage.getInstance().getUser(tools.resolveUser(userName, ctx), ctx);
        user2 = UserStorage.getInstance().getUser(tools.resolveUser(userName2, ctx), ctx);


        session = ServerSessionFactory.createServerSession(user.getId(), ctx, "blupp");
        session2 = ServerSessionFactory.createServerSession(user2.getId(), ctx, "blupp2");

        permissionBits = session.getUserPermissionBits();
        permissionBits2 = session2.getUserPermissionBits();

        folderId = getPrivateInfostoreFolder(ctx,user,session);
        folderId2 = getPrivateInfostoreFolder(ctx, user2, session2);

        provider = new DBPoolProvider();
        infostore = new InfostoreFacadeImpl(provider);

    }

    public int getPrivateInfostoreFolder(final Context context, final User usr, final ServerSession sess) throws OXException {
        final OXFolderAccess oxfa = new OXFolderAccess(context);
        return oxfa.getDefaultFolder(usr.getId(), FolderObject.INFOSTORE).getObjectID();
    }

    @Override
    public void tearDown() throws Exception{
        for(final DocumentMetadata dm : clean) {
            IDTuple idTuple = new IDTuple(Long.toString(dm.getFolderId()), Integer.toString(dm.getId()));
            infostore.removeDocument(Collections.singletonList(idTuple), System.currentTimeMillis(), session);
        }

        final OXFolderManager oxma = OXFolderManager.getInstance(session);
        for(final FolderObject folder : cleanFolders) {
            oxma.deleteFolder(folder, false, System.currentTimeMillis());
        }

        Init.stopServer();
    }

    protected InfostoreFacade getInfostore(){
        return infostore;
    }

    protected ServerSession getSession() {
        return session;
    }

    public ServerSession getSession2() {
        return session2;
    }

    public Context getCtx() {
        return ctx;
    }

    public int getFolderId() {
        return folderId;
    }

    public User getUser() {
        return user;
    }

    public User getUser2() {
        return user2;
    }

    public UserPermissionBits getUserPermissionBits() {
        return permissionBits;
    }

    public UserPermissionBits getUserPermissionBits2() {
        return permissionBits2;
    }

    public int getFolderId2() {
        return folderId2;
    }

    public DBProvider getProvider() {
        return provider;
    }


}
