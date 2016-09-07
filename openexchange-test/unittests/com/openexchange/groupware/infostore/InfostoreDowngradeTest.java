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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.osgi.service.event.Event;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionFactory;
import junit.framework.TestCase;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class InfostoreDowngradeTest extends TestCase {

    private Context ctx;
    private int folderId;
    private int userId;
    private InfostoreFacade database;
    private ServerSession session;
    private UserPermissionBits userConfig;

    private final List<DocumentMetadata> clean = new ArrayList<DocumentMetadata>();

    @Override
	public void setUp() throws Exception {
        Init.startServer();
        AJAXConfig.init();

        final TestConfig config = new TestConfig();
        ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId(config.getContextName()));
        userId = UserStorage.getInstance().getUserId(AJAXConfig.getProperty(AJAXConfig.Property.LOGIN), ctx);
        userConfig = UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, ctx);

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
        final UserConfiguration config = new UserConfiguration(new HashSet<String>(), userId,userConfig.getGroups() , ctx);
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
            IDTuple idTuple = new IDTuple(Long.toString(document.getFolderId()), Integer.toString(document.getId()));
            database.removeDocument(Collections.singletonList(idTuple), Long.MAX_VALUE, session);
        }
    }


    private void assertDeletedEvent(final int id) throws OXException {
        Event event = TestEventAdmin.getInstance().getNewestEvent();
        assertTrue(FileStorageEventHelper.isDeleteEvent(event));
        FileID fileID = new FileID(FileStorageEventHelper.extractObjectId(event));
        assertEquals(String.valueOf(id), fileID.getFileId());
    }

    private void assertNotFound(final int id) {
        try {
            database.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, session);
            fail("The document still exists!");
        } catch (final OXException e) {
            assertTrue(e.getMessage(), InfostoreExceptionCodes.NOT_EXIST.equals(e) || InfostoreExceptionCodes.DOCUMENT_NOT_EXIST.equals(e));
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
