package com.openexchange.groupware.infostore.webdav;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;


public class PermissionTest extends TestCase implements SessionHolder {

    private Context ctx;
    private Session session;

    private User user;
    private User user1;
    private User user2;

    private UserPermissionBits userConfig;
    private UserPermissionBits userConfig1;
    private UserPermissionBits userConfig2;

    private User cleanupUser;

    private Session session1;
    private Session session2;

    private FolderObject root;

    private InfostoreWebdavFactory factory;

    private final List<FolderObject> clean = new ArrayList<FolderObject>();

    private static String getUsername(final String un) {
        final int pos = un.indexOf('@');
        return pos == -1 ? un : un.substring(0, pos);
    }

    @Override
	public void setUp() throws Exception {
        Init.startServer();
        AJAXConfig.init();

        final TestConfig config = new TestConfig();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

        final UserStorage userStorage = UserStorage.getInstance();
        final UserPermissionBitsStorage userConfigStorage = UserPermissionBitsStorage.getInstance();

        session1 = SessionObjectWrapper.createSessionObject(userStorage.getUserId(getUsername(AJAXConfig.getProperty(AJAXConfig.Property.LOGIN)), ctx), ctx, getClass().getName());
		user1 = userStorage.getUser(session1.getUserId(), ctx);
        userConfig1 = userConfigStorage.getUserPermissionBits(user1.getId(),ctx);

        session2 = SessionObjectWrapper.createSessionObject(userStorage.getUserId(getUsername(AJAXConfig.getProperty(AJAXConfig.Property.SECONDUSER)), ctx), ctx, getClass().getName());
		user2 = userStorage.getUser(session2.getUserId(), ctx);
        userConfig2 = userConfigStorage.getUserPermissionBits(user2.getId(), ctx);

        final OXFolderAccess oxfa = new OXFolderAccess(ctx);
		root = oxfa.getFolderObject(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);

        cleanupUser = user1;
        switchUser(user1);

        TestWebdavFactoryBuilder.setUp();
		factory = (InfostoreWebdavFactory) TestWebdavFactoryBuilder.buildFactory();
        factory.setSessionHolder(this);
        factory.beginRequest();
    }

    @Override
	public void tearDown() throws Exception {
        switchUser(cleanupUser);
        Collections.reverse(clean);
        for(final FolderObject folderobject : clean) {
			rm(folderobject.getObjectID());
		}
        clean.clear();
        factory.endRequest(200);
        Init.stopServer();

    }

    // Bug 10395
    public void testListSubfolders() throws Exception{

        final FolderObject parentFolder =  createFolder(root, "parent",
                adminPermission(user1),
                permission(user2, false, OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));

        createFolder(parentFolder, "sub",
                adminPermission(user1),
                permission(user2, false, OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));

        switchUser(user2);

        final WebdavCollection collection = factory.resolveCollection(new WebdavPath("public_infostore","parent"));

        final List<WebdavResource> children = collection.getChildren();
        assertEquals(1, children.size());
        assertEquals("sub", children.get(0).getDisplayName());
    }

    // Bug 10051
    public void testProppatchDocumentWithoutWritePermissions() throws Exception{
        final FolderObject testFolder = createFolder(root, "test"+ System.currentTimeMillis(),
                    adminPermission(user1),
                    permission(user2, false, OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));

        final DocumentMetadata document = touch(testFolder, "testDocument");

        final WebdavResource resource = factory.resolveResource(new WebdavPath("public_infostore", testFolder.getFolderName(), document.getFileName()));

        switchUser(user2);
        final WebdavProperty prop = new WebdavProperty("http://www.open-xchange.com/testProperties","test");
        prop.setValue("foo");
        resource.putProperty(prop);
        try {
            resource.save();
            fail("Shouldn't be able to save this, as user2 doesn't have write permissions");
        } catch(final WebdavProtocolException x) {
            if(x.getStatus() != 403) {
                x.printStackTrace();
            }
            assertEquals("Expected Forbidden Status", 403, x.getStatus());
        }
    }

    // Bug 10051
    public void testProppatchFolderWithoutWritePermissions() throws Exception {
        final FolderObject testFolder =  createFolder(root, "test"+ System.currentTimeMillis(),
                adminPermission(user1),
                permission(user2, false, OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));

        final WebdavCollection collection = factory.resolveCollection(new WebdavPath("public_infostore", testFolder.getFolderName()));

        switchUser(user2);
        final WebdavProperty prop = new WebdavProperty("http://www.open-xchange.com/testProperties","test");
        prop.setValue("foo");
        collection.putProperty(prop);
        try {
            collection.save();
            fail("Shouldn't be able to save this, as user2 doesn't have write permissions");
        } catch(final WebdavProtocolException x) {
            if (x.getStatus() != 403) {
                x.printStackTrace();
            }
            assertEquals("Expected Forbidden Status", 403, x.getStatus());
        }
    }

    // Bug 10052
    public void testUpdateDocumentWithWritePermissionsOnly() throws Exception {
        final FolderObject testFolder = createFolder(root, "test"+ System.currentTimeMillis(),
                adminPermission(user1),
                permission(user2, false, OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS));

        final DocumentMetadata document = touch(testFolder, "testDocument");

        final WebdavResource resource = factory.resolveResource(new WebdavPath("public_infostore", testFolder.getFolderName(), document.getFileName()));

        switchUser(user2);

        try {
            resource.putBodyAndGuessLength(new ByteArrayInputStream(new byte[] {1,2,3}));
            resource.save();
            assertTrue(true);
        } catch (final OXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    // Bug 10052
    public void testUpdateFolderWithWritePermissionsOnly() throws Exception {
        final FolderObject testFolder = createFolder(root, "test"+ System.currentTimeMillis(),
                adminPermission(user1),
                permission(user2, false, OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS));

        final WebdavCollection collection = factory.resolveCollection(new WebdavPath("public_infostore", testFolder.getFolderName()));

        switchUser(user2);

        try {
            collection.setDisplayName("rename");
            collection.save();
            assertTrue(true);
        } catch (final OXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    //Bug 10706
    public void testDontDulicateDocumentsWithCreateAndWritePermissions() throws Exception {
        final FolderObject testFolder = createFolder(root, "test"+ System.currentTimeMillis(),
                adminPermission(user1),
                permission(user2, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.WRITE_OWN_OBJECTS, OCLPermission.NO_PERMISSIONS));

        switchUser(user2);

        WebdavResource resource = factory.resolveResource(new WebdavPath("public_infostore", testFolder.getFolderName(), "test.bin"));

        resource.putBodyAndGuessLength(new ByteArrayInputStream(new byte[0]));
        resource.create();

        factory.endRequest(200);
        factory.beginRequest();

        resource = factory.resolveResource(new WebdavPath("public_infostore", testFolder.getFolderName(), "test.bin"));

        resource.putBodyAndGuessLength(new ByteArrayInputStream(new byte[] {1,2,3}));
        assertTrue(resource.exists());
        resource.save();

        // Verify that it has not doubled, but was overwritten (correctly)
        switchUser(user1);

        final TimedResult documents = factory.getDatabase().getDocuments(testFolder.getObjectID(), getContext(), user, userConfig);

        final Map<String, Integer> counter =  new HashMap<String,Integer>();
        for(final DocumentMetadata metadata : SearchIteratorAdapter.toIterable((SearchIterator<DocumentMetadata>)documents.results())) {
            final String name = metadata.getFileName();
            assertNotNull(name);
            assertEquals("test.bin", name);
            Integer value = 0;
            if(null != counter.get(name)) {
                value = counter.get(name);
            }
            value += 1;
            counter.put(name, value);
        }
        assertTrue(counter.values().size() > 0);
        for(final Integer count : counter.values()) {
            assertEquals(new Integer(1), count);
        }
    }


    //Bug 10706
    public void testDontDulicateOtherPersonsDocumentWithCreateAndWritePermissions() throws Exception {
        final FolderObject testFolder = createFolder(root, "test"+ System.currentTimeMillis(),
                adminPermission(user1),
                permission(user2, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.WRITE_OWN_OBJECTS, OCLPermission.NO_PERMISSIONS));

        WebdavResource resource = factory.resolveResource(new WebdavPath("public_infostore", testFolder.getFolderName(), "test.bin"));

        resource.putBodyAndGuessLength(new ByteArrayInputStream(new byte[0]));
        resource.create();


        switchUser(user2);

        resource = factory.resolveResource(new WebdavPath("public_infostore", testFolder.getFolderName(), "test.bin"));

        try {
            resource.putBodyAndGuessLength(new ByteArrayInputStream(new byte[] {1,2,3}));
            resource.save();
            fail("Could update document even without write permissions to it");
        } catch (final WebdavProtocolException x) {
            if(x.getStatus() != 403) {
                x.printStackTrace();
            }
            assertEquals(403,x.getStatus());
        }
    }


    public void testDisallowSavingInRootVirtualFolder() throws Exception {
        final WebdavResource res = factory.resolveResource("/test.txt");
        try {
            res.putBodyAndGuessLength(new ByteArrayInputStream(new byte[]{1,2,3}));
            res.save();
            fail("Shouldn't be able to save in root folder");
        } catch (final WebdavProtocolException x) {
            if(x.getStatus() != 403) {
                x.printStackTrace();
            }
            assertEquals(403,x.getStatus());
        }

    }

    public OCLPermission adminPermission(final User user) {
        return permission(user, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
    }

    public OCLPermission permission(final User user, final boolean fadmin, final int fp, final int rp, final int wp, final int dp) {
        final OCLPermission perm = new OCLPermission();
        perm.setAllPermission(fp,rp,wp,dp);
        perm.setEntity(user.getId());
        perm.setFolderAdmin(fadmin);
        perm.setGroupPermission(false);
        return perm;
    }

    public FolderObject createFolder(final FolderObject parent, final String fname, OCLPermission...permissions) throws OXException, OXException {
        if(permissions.length == 0) {
            permissions = new OCLPermission[] {adminPermission(cleanupUser)};
        }
        Connection writecon = null;
        try {
        	writecon = DBPool.pickupWriteable(ctx);
	        final OXFolderManager oxma = OXFolderManager.getInstance(session, writecon, writecon);
	        FolderObject fo = new FolderObject();
	        fo.setFolderName(fname);
	        fo.setParentFolderID(parent.getObjectID());
	        fo.setModule(FolderObject.INFOSTORE);
	        fo.setType(FolderObject.PUBLIC);
	        fo.setPermissionsAsArray(permissions);
	        fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            clean.add(fo);
            return fo;
        } finally {
        	if(writecon != null) {
				DBPool.pushWrite(ctx, writecon);
			}
        }
    }

    private DocumentMetadata touch(final FolderObject testFolder, final String fileName) throws Exception{
        final InfostoreFacade infostore = new InfostoreFacadeImpl(new DBPoolProvider());
        infostore.startTransaction();
        try {
            final DocumentMetadata document = new DocumentMetadataImpl();
            document.setFileName(fileName);
            document.setFolderId(testFolder.getObjectID());
            final InputStream data = new ByteArrayInputStream(new byte[] {1});
            infostore.saveDocument(document,data,System.currentTimeMillis(), ServerSessionAdapter.valueOf(session, getContext()));
            return document;
        } catch (final Exception x) {
            infostore.rollback();
            throw x;
        } finally {
            infostore.finish();
        }
    }


    public void switchUser(final User user) {
        if(user.getId() == user1.getId()) {
          session = session1;
          this.user = user1;
          userConfig = userConfig1;
        } else if (user.getId() == user2.getId()) {
          session = session2;
          this.user = user2;
          userConfig = userConfig2;
        } else {
            throw new IllegalArgumentException("I don't know user "+user.getId());
        }
    }

    protected void rm(final int objectID) throws SQLException, OXException, OXException, Exception {
		//OXFolderAction ofa = new OXFolderAction(session);
		final OXFolderManager oxma = OXFolderManager.getInstance(session);
		//ofa.deleteFolder(objectID, session, true, System.currentTimeMillis());
		oxma.deleteFolder(new FolderObject(objectID), true, System.currentTimeMillis());
	}

    @Override
    public Session getSessionObject() {
        return session;
    }

    @Override
    public Context getContext() {
    	return ctx;
    }

    /* (non-Javadoc)
     * @see com.openexchange.sessiond.impl.SessionHolder#getUser()
     */
    @Override
    public User getUser() {
        // Nothing to do
        return null;
    }
}
