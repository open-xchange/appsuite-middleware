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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import com.openexchange.exception.OXException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionFactory;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class InfostoreDowngradeTest extends TestCase {

    private Context ctx;
    private int folderId;
    private int userId;
    private InfostoreFacade database;
    private ServerSession session;
    private UserPermissionBits permissionBits;
    private User user;

    private final List<DocumentMetadata> clean = new ArrayList<DocumentMetadata>();

    @Override
	public void setUp() throws Exception {
        Init.startServer();
        AJAXConfig.init();

        ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext"));
        userId = UserStorage.getInstance().getUserId(AJAXConfig.getProperty(AJAXConfig.Property.LOGIN), ctx);
        user = UserStorage.getInstance().getUser(userId, ctx);
        permissionBits = UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, ctx);

        final OXFolderAccess access = new OXFolderAccess(ctx);
        final FolderObject fo = access.getDefaultFolder(userId, FolderObject.INFOSTORE);
        folderId = fo.getObjectID();

        database = new InfostoreFacadeImpl(new DBPoolProvider());
        database.setTransactional(true);

        session = ServerSessionFactory.createServerSession(userId, ctx, "Blubb");

        TestEventAdmin.getInstance().clearEvents();

    }

    @Override
	public void tearDown() throws Exception {
        deleteAll();
        Init.stopServer();
    }

    private void runDelete() {
        final UserConfiguration config = new UserConfiguration(new HashSet<String>(), userId,permissionBits.getGroups() , ctx);
        Connection con = null;
        try {
            con = DBPool.pickupWriteable(ctx);
            final DowngradeEvent event = new DowngradeEvent(config, con, ctx);
            new InfostoreDowngrade().downgradePerformed(event);
        } catch (final OXException x) {
            x.printStackTrace();
            fail(x.getMessage());
        } finally {
            if(con != null) {
                DBPool.pushWrite(ctx, con);
            }
        }
    }

    public void testDowngrade() throws OXException {
        final DocumentMetadata dm = createDocumentInStandardFolder();

        runDelete();

        assertNotFound(dm.getId());
        assertDeletedEvent(dm.getId());
    }

    private void deleteAll() throws OXException {
        for(final DocumentMetadata document : clean) {
            database.removeDocument(new int[]{document.getId()}, Long.MAX_VALUE, session);
        }
    }


    private void assertDeletedEvent(final int id) {
        final CommonEvent event = TestEventAdmin.getInstance().getNewest();

        assertEquals(CommonEvent.DELETE, event.getAction());

        final DocumentMetadata dm = (DocumentMetadata) event.getActionObj();
        assertEquals(id, dm.getId());
    }

    private void assertNotFound(final int id) {
        try {
            database.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, ctx, user, permissionBits);
            fail("The document still exists!");
        } catch (final OXException e) {
            assertEquals(e.getMessage(), 300, e.getCode());
        }
    }

    private DocumentMetadata createDocumentInStandardFolder() throws OXException {
        final DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setTitle("documentInStandardFolder");
        dm.setFolderId(folderId);
        dm.setId(InfostoreFacade.NEW);
        database.saveDocumentMetadata(dm,Long.MAX_VALUE,session);
        clean.add(dm);
        return dm;
    }

}
