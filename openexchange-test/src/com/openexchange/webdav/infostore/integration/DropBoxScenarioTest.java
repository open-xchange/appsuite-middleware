package com.openexchange.webdav.infostore.integration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.webdav.protocol.DummySessionHolder;
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavResource;

// Bug #9109
public class DropBoxScenarioTest extends TestCase{
	
	
	private Context ctx;
	private String user1;
	private String user2;
	
	
	private InfostoreWebdavFactory factory = null;
	
	String dropBox = null;
	
	List<String> clean = new ArrayList<String>();
	
	@Override
	public void setUp() throws Exception {
		
        user1 = "thorben";
        user2 = "francisco"; //FIXME
        
		TestWebdavFactoryBuilder.setUp();

		final ContextStorage ctxstor = ContextStorage.getInstance();
        final int contextId = ctxstor.getContextId("defaultcontext");
        ctx = ctxstor.getContext(contextId);

        factory = (InfostoreWebdavFactory) TestWebdavFactoryBuilder.buildFactory();
        factory.beginRequest();
		try {
		
			switchUser(user1);
			createDropBox();
			
		} catch (Exception x) {
			tearDown();
			throw x;
		}
	}
	
	@Test public void testAddToDropBox(){
		try {
			switchUser(user2);
			
			WebdavResource res = factory.resolveResource(dropBox+"/testFile");
			res.putBodyAndGuessLength(new ByteArrayInputStream(new byte[]{1,2,3,4,5,6,7,8,9,10}));
			clean.add(dropBox+"/testFile");
			res.create();
			
			switchUser(user1);
			
			res = factory.resolveResource(dropBox+"/testFile");
			InputStream is = res.getBody();
			
			for(int i = 0; i < 10; i++) {
				assertEquals(i+1, is.read());
			}
			assertEquals(-1, is.read());
			is.close();
			
		} catch (LdapException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (DBPoolingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (OXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (WebdavException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Override
	public void tearDown() throws Exception {
		try {
			switchUser(user1);
			for(String url : clean) {
				factory.resolveResource(url).delete();
			}
		} finally {
			factory.endRequest(200);
			TestWebdavFactoryBuilder.tearDown();
		}
	}
	
	
	private void switchUser(String username) throws LdapException, DBPoolingException, OXException, SQLException {
		factory.endRequest(200);
		factory.setSessionHolder(new DummySessionHolder(username, ctx));
		factory.beginRequest();
	}
	
	private void createDropBox() throws OXException, WebdavException{
		SessionObject session = factory.getSessionHolder().getSessionObject();
		OXFolderManager mgr = new OXFolderManagerImpl(session);
		OXFolderAccess acc = new OXFolderAccess(session.getContext());
		
		FolderObject fo = acc.getDefaultFolder(session.getUserObject().getId(), FolderObject.INFOSTORE);
		
		FolderObject newFolder = new FolderObject();
		newFolder.setFolderName("Drop Box");
		newFolder.setParentFolderID(fo.getObjectID());
		newFolder.setType(FolderObject.PUBLIC);
		newFolder.setModule(FolderObject.INFOSTORE);
		
		ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
		
		// User is Admin and can read, write or delete everything
		OCLPermission perm = new OCLPermission();
		perm.setEntity(session.getUserObject().getId());
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
		perm.setWriteObjectPermission(OCLPermission.NO_PERMISSIONS);
		perm.setDeleteObjectPermission(OCLPermission.NO_PERMISSIONS);
		perm.setGroupPermission(true);
		perms.add(perm);
		
		newFolder.setPermissions(perms);
		
		mgr.createFolder(newFolder, true, System.currentTimeMillis());
		dropBox = fo.getFolderName()+"/"+newFolder.getFolderName();
		
		clean.add(factory.resolveCollection(dropBox).getUrl());
	}
}
