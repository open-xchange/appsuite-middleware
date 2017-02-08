
package com.openexchange.webdav.infostore.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.webdav.protocol.DummySessionHolder;
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

// Bug #9109
public class DropBoxScenarioTest {

    private Context ctx;
    private String user1;
    private String user2;

    private InfostoreWebdavFactory factory = null;

    WebdavPath dropBox = null;

    List<WebdavPath> clean = new ArrayList<WebdavPath>();

    private static String getUsername(final String un) {
        final int pos = un.indexOf('@');
        return pos == -1 ? un : un.substring(0, pos);
    }

    @Before
    public void setUp() throws Exception {
        final TestConfig config = new TestConfig();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

        user1 = getUsername(config.getUser());
        user2 = getUsername(config.getSecondUser());

        TestWebdavFactoryBuilder.setUp();

        final ContextStorage ctxstor = ContextStorage.getInstance();
        final int contextId = ctxstor.getContextId(ctxName);
        ctx = ctxstor.getContext(contextId);

        factory = (InfostoreWebdavFactory) TestWebdavFactoryBuilder.buildFactory();
        factory.beginRequest();
        try {

            switchUser(user1);
            createDropBox();

        } catch (final Exception x) {
            tearDown();
            throw x;
        }
    }

    @Test
    public void testAddToDropBox() {
        try {
            switchUser(user2);

            WebdavResource res = factory.resolveResource(dropBox.dup().append("testFile"));
            res.putBodyAndGuessLength(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }));
            clean.add(dropBox.dup().append("testFile"));
            res.create();

            switchUser(user1);

            res = factory.resolveResource(dropBox.dup().append("testFile"));
            final InputStream is = res.getBody();

            for (int i = 0; i < 10; i++) {
                assertEquals(i + 1, is.read());
            }
            assertEquals(-1, is.read());
            is.close();

        } catch (final OXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (final SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (final IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            switchUser(user1);
            for (final WebdavPath url : clean) {
                factory.resolveResource(url).delete();
            }
        } finally {
            factory.endRequest(200);
            TestWebdavFactoryBuilder.tearDown();
        }
    }

    private void switchUser(final String username) throws OXException, OXException, OXException, SQLException {
        factory.endRequest(200);
        factory.setSessionHolder(new DummySessionHolder(username, ctx));
        factory.beginRequest();
    }

    private void createDropBox() throws OXException, OXException, OXException {
        final Session session = factory.getSessionHolder().getSessionObject();
        final OXFolderManager mgr = OXFolderManager.getInstance(session);
        final OXFolderAccess acc = new OXFolderAccess(ContextStorage.getInstance().getContext(session.getContextId()));

        final FolderObject fo = acc.getDefaultFolder(session.getUserId(), FolderObject.INFOSTORE);

        final FolderObject newFolder = new FolderObject();
        newFolder.setFolderName("Drop Box " + System.currentTimeMillis());
        newFolder.setParentFolderID(fo.getObjectID());
        newFolder.setType(FolderObject.PUBLIC);
        newFolder.setModule(FolderObject.INFOSTORE);

        final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();

        // User is Admin and can read, write or delete everything
        OCLPermission perm = new OCLPermission();
        perm.setEntity(session.getUserId());
        perm.setFolderAdmin(true);
        perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
        perm.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
        perm.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
        perm.setGroupPermission(false);
        perms.add(perm);

        // Everybody can create objects, but may not read, write or delete

        perm = new OCLPermission();
        perm.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        perm.setFolderAdmin(false);
        perm.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
        perm.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
        perm.setWriteObjectPermission(OCLPermission.WRITE_OWN_OBJECTS); // after SCR-1997, "write own" is required, too
        perm.setDeleteObjectPermission(OCLPermission.NO_PERMISSIONS);
        perm.setGroupPermission(true);
        perms.add(perm);

        newFolder.setPermissions(perms);

        mgr.createFolder(newFolder, true, System.currentTimeMillis());
        dropBox = new WebdavPath("userstore", fo.getFolderName(), newFolder.getFolderName());

        clean.add(factory.resolveCollection(dropBox).getUrl());
    }
}
