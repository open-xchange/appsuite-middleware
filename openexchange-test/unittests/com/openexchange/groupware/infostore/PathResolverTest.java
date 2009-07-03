package com.openexchange.groupware.infostore;

import static com.openexchange.webdav.protocol.WebdavPathTest.assertComponents;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.tools.CalendarContextToolkit;
import com.openexchange.groupware.calendar.tools.CalendarTestConfig;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.paths.impl.PathResolverImpl;
import com.openexchange.groupware.infostore.webdav.InMemoryAliases;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderLogicException;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderPermissionException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionFactory;
import com.openexchange.webdav.protocol.WebdavPath;

public class PathResolverTest extends TestCase {

	private final DBProvider provider = new DBPoolProvider();
	private final InfostoreFacade database = new InfostoreFacadeImpl(provider);
	private final PathResolverImpl pathResolver = new PathResolverImpl(provider, database);
	
	private int root;
	
	private int id9;
    private int id8;
	private int id7;
	private int id6;
	private int id5;
	private int id4;
	private int id3;
	private int id2;
	private int id;
	
	private final int type = FolderObject.PUBLIC;
	
	ServerSession session;
	private Context ctx = null;
	private User user;
	private UserConfiguration userConfig;


    @Override
	public void setUp() throws Exception {
		Init.startServer();
		database.setTransactional(true);
		ctx = getContext();

        final UserStorage userStorage = UserStorage.getInstance();
        final UserConfigurationStorage userConfigStorage = UserConfigurationStorage.getInstance();

        session = ServerSessionFactory.createServerSession(userStorage.getUserId(getUsername(), ctx), ctx, "gnitzelgnatzel");
		user = userStorage.getUser(session.getUserId(), ctx);
		userConfig = userConfigStorage.getUserConfiguration(session.getUserId(), ctx);
		
		findRoot();
		
		pathResolver.startTransaction();
        root = mkdir(root, "folder-"+System.currentTimeMillis());
        id = mkdir(root, "this");
		id2 = mkdir(id, "is");
		id3 = mkdir(id2, "a");
		id4 = mkdir(id3, "nice");
		id5 = mkdir(id4, "path");
		id6 = touch(id5,"document.txt");
		id7 = mkdir(id5,"path");
		id8 = mkdir(id7,"path");
		id9 = mkdir(id8,"path");
		
	}
	
	private Context getContext() throws ContextException {
	    try {
            final CalendarTestConfig config = new CalendarTestConfig();
            final CalendarContextToolkit tools = new CalendarContextToolkit();
            final String ctxName = config.getContextName();
            return null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);
        } catch (final ConfigurationException e) {
            e.printStackTrace();
            return null;
        }
	}

	@Override
	public void tearDown() throws Exception {
		pathResolver.finish();
        rmdir(id9);
        rmdir(id8);
		rmdir(id7);
		rmdir(id5);
		rmdir(id4);
		rmdir(id3);
		rmdir(id2);
		rmdir(id);
        rmdir(root);
        Init.stopServer();
	}

	private void findRoot() throws Exception {
		final OXFolderAccess oxfa = new OXFolderAccess(ctx);
		root = oxfa.getDefaultFolder(user.getId(), FolderObject.INFOSTORE).getObjectID();
	}

	private String getUsername() {
	    try {
            final CalendarTestConfig config = new CalendarTestConfig();
            final String userName = config.getUser();
            final int pos = userName.indexOf('@');
            return pos == -1 ? userName : userName.substring(0, pos);
        } catch (final ConfigurationException e) {
            e.printStackTrace();
            return null;
        }
	}

	public void testResolvePathDocument() throws Exception {
		Resolved resolved = pathResolver.resolve(root, new WebdavPath("/this/is/a/nice/path/document.txt"), ctx, user, userConfig);
		assertTrue(resolved.isDocument());
		assertFalse(resolved.isFolder());
		assertEquals(id6, resolved.getId());
		
		resolved = pathResolver.resolve(id2, new WebdavPath("a/nice/path/document.txt"), ctx, user, userConfig);
		assertTrue(resolved.isDocument());
		assertFalse(resolved.isFolder());
		assertEquals(id6, resolved.getId());
	}

	public void testResolvePathFolder() throws Exception {
		Resolved resolved = pathResolver.resolve(root, new WebdavPath("/this/is/a/nice/path"), ctx, user, userConfig);
		assertFalse(resolved.isDocument());
		assertTrue(resolved.isFolder());
		assertEquals(id5, resolved.getId());
		
		resolved = pathResolver.resolve(id2, new WebdavPath("a/nice/path"), ctx, user, userConfig);
		assertFalse(resolved.isDocument());
		assertTrue(resolved.isFolder());
		assertEquals(id5, resolved.getId());
	}

	public void testGetPathDocument() throws Exception {
		final WebdavPath path = pathResolver.getPathForDocument(root, id6, ctx, user, userConfig);
        assertComponents(path, "this", "is", "a","nice","path","document.txt");
	}

	public void testGetPathFolder() throws Exception {
		final WebdavPath path = pathResolver.getPathForFolder(root, id5, ctx, user, userConfig);
        assertComponents(path, "this","is","a","nice","path");
	
	}
	
	public void testNotExists() throws Exception {
		try {
			pathResolver.resolve(root, new WebdavPath("/i/dont/exist"), ctx, user, userConfig);
			fail("Expected OXObjectNotFoundException");
		} catch (final OXObjectNotFoundException x) {
			assertTrue(true);
		}
	}
    
    // Bug 12279
    public void testCaseSensitive() throws Exception {
        try {
			pathResolver.resolve(root, new WebdavPath("/this/is/a/nice/path/DoCuMeNt.txt"), ctx, user, userConfig);
			fail("Expected OXObjectNotFoundException");
		} catch (final OXObjectNotFoundException x) {
			assertTrue(true);
		}
        try {
            pathResolver.resolve(root, new WebdavPath("/this/is/a/nice/PaTh"), ctx, user, userConfig);
            fail("Expected OXObjectNotFoundException");
        } catch (final OXObjectNotFoundException x) {
            assertTrue(true);
        }
    }

    // Bug 12618
    public void testRespectsAliases() throws OXException {
        final WebdavFolderAliases aliases = new InMemoryAliases();
        aliases.registerNameWithIDAndParent("ALIAS!", id5, id4);
        pathResolver.setAliases(aliases);

        final Resolved resolved = pathResolver.resolve(root, new WebdavPath("/this/is/a/nice/ALIAS!"), ctx, user, userConfig);
        assertFalse(resolved.isDocument());
        assertTrue(resolved.isFolder());
        assertEquals(id5, resolved.getId());

        pathResolver.clearCache();

        final WebdavPath path = pathResolver.getPathForFolder(root, id5, ctx, user, userConfig);
        assertComponents(path, "this","is","a","nice","ALIAS!");

        pathResolver.clearCache();

        try {
            pathResolver.resolve(root, new WebdavPath("this/ALIAS!/a/nice"), ctx, user, userConfig);
            fail("Expected OXObjectNotFoundException");
        } catch (final OXObjectNotFoundException x) {
            assertTrue(true);
        }
    }

    private int mkdir(final int parent, final String name) throws SQLException, OXFolderPermissionException, Exception {
	
		//OXFolderAction oxfa = new OXFolderAction(session);
		final FolderObject folder = new FolderObject();
		folder.setFolderName(name);
		folder.setParentFolderID(parent);
		folder.setType(type);
		folder.setModule(FolderObject.INFOSTORE);
		
		final OCLPermission perm = new OCLPermission();
		perm.setEntity(user.getId());
		perm.setFolderAdmin(true);
		perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setGroupPermission(false);
		
		// All others may read and write
		
		final OCLPermission perm2 = new OCLPermission();
		perm2.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
		perm2.setGroupPermission(true);
		perm2.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
		perm2.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
		perm2.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
		perm2.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
		
		folder.setPermissionsAsArray(new OCLPermission[]{perm, perm2});
		
		Connection writeCon = null;
		try {
			writeCon = provider.getWriteConnection(ctx);
            final OXFolderManager oxma = OXFolderManager.getInstance(session, writeCon, writeCon);
            oxma.createFolder(folder, true, System.currentTimeMillis());
        } finally {
            if (writeCon != null) {
                provider.releaseWriteConnection(ctx, writeCon);
			}
		}

        //oxfa.createFolder(folder, session, true, writeCon, writeCon, false);
		return folder.getObjectID();
	}
	
	private int touch(final int parent, final String filename) throws Exception {
		final DocumentMetadata m = new DocumentMetadataImpl();
		m.setFolderId(parent);
		m.setFileName(filename);
		m.setId(InfostoreFacade.NEW);
		database.startTransaction();
		
		try {
			database.saveDocument(m, new ByteArrayInputStream(new byte[10]), Long.MAX_VALUE, session);
			database.commit();
		} catch (final Exception x) {
			database.rollback();
			throw x;
		} finally {
			database.finish();	
		}
		return m.getId();
	}
	
	private void rmdir(final int id) throws SQLException, OXFolderPermissionException, OXFolderLogicException, Exception {
//		OXFolderAction oxfa = new OXFolderAction(session);
//		oxfa.deleteFolder(id, session, true, Long.MAX_VALUE);
		final OXFolderManager oxma = OXFolderManager.getInstance(session);
		oxma.deleteFolder(new FolderObject(id), true, System.currentTimeMillis());
	}

}
