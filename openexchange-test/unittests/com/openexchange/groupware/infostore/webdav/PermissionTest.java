package com.openexchange.groupware.infostore.webdav;

import junit.framework.TestCase;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.oxfolder.*;
import com.openexchange.api2.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionHolder;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.configuration.AJAXConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


public class PermissionTest extends TestCase implements SessionHolder {

    private Context ctx;
    private Session session;

    private User user1;
    private User user2;

    private User cleanupUser;

    private Session session1;
    private Session session2;

    private FolderObject root;

    private InfostoreWebdavFactory factory;

    private List<FolderObject> clean = new ArrayList<FolderObject>();

    public void setUp() throws Exception {
        Init.startServer();
        AJAXConfig.init();
        final ContextStorage ctxstor = ContextStorage.getInstance();
        final int contextId = ctxstor.getContextId("defaultcontext");
        ctx = ctxstor.getContext(contextId);

        UserStorage userStorage = UserStorage.getInstance();
        UserConfigurationStorage userConfigStorage = UserConfigurationStorage.getInstance();

        session1 = SessionObjectWrapper.createSessionObject(userStorage.getUserId(AJAXConfig.getProperty(AJAXConfig.Property.LOGIN), ctx), ctx, getClass().getName());
		user1 = userStorage.getUser(session1.getUserId(), ctx);

        session2 = SessionObjectWrapper.createSessionObject(userStorage.getUserId(AJAXConfig.getProperty(AJAXConfig.Property.SECONDUSER), ctx), ctx, getClass().getName());
		user2 = userStorage.getUser(session2.getUserId(), ctx);

        final OXFolderAccess oxfa = new OXFolderAccess(ctx);
		root = oxfa.getFolderObject(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);

        cleanupUser = user1;
        switchUser(user1);

        TestWebdavFactoryBuilder.setUp();
		factory = (InfostoreWebdavFactory) TestWebdavFactoryBuilder.buildFactory();
        factory.setSessionHolder(this);
        factory.beginRequest();
    }

    public void tearDown() throws Exception {
        switchUser(cleanupUser);
        Collections.reverse(clean);
        for(FolderObject folderobject : clean) {
			rm(folderobject.getObjectID());
		}
        clean.clear();
        factory.endRequest(200);
        Init.stopServer();

    }

    // Bug 10395
    public void testListSubfolders() throws Exception{
        
        FolderObject parentFolder =  createFolder(root, "parent",
                adminPermission(user1),
                permission(user2, false, OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));

        createFolder(parentFolder, "sub",
                adminPermission(user1),
                permission(user2, false, OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));

        switchUser(user2);

        WebdavCollection collection = factory.resolveCollection(new WebdavPath("parent"));

        List<WebdavResource> children = collection.getChildren();
        assertEquals(1, children.size());
        assertEquals("sub", children.get(0).getDisplayName());
    }

    public OCLPermission adminPermission(User user) {
        return permission(user, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
    }

    public OCLPermission permission(User user, boolean fadmin, int fp, int rp, int wp, int dp) {
        OCLPermission perm = new OCLPermission();
        perm.setAllPermission(fp,rp,wp,dp);
        perm.setEntity(user.getId());
        perm.setFolderAdmin(fadmin);
        perm.setGroupPermission(false);
        return perm;
    }

    public FolderObject createFolder(FolderObject parent, String fname, OCLPermission...permissions) throws OXException, DBPoolingException {
        if(permissions.length == 0) {
            permissions = new OCLPermission[] {adminPermission(cleanupUser)};
        }
        Connection writecon = null;
        try {
        	writecon = DBPool.pickupWriteable(ctx);
	        final OXFolderManager oxma = new OXFolderManagerImpl(session, writecon, writecon);
	        FolderObject fo = new FolderObject();
	        fo.setFolderName(fname);
	        fo.setParentFolderID(parent.getObjectID());
	        fo.setModule(FolderObject.INFOSTORE);
	        fo.setType(FolderObject.PUBLIC);
	        fo.setPermissionsAsArray(permissions);
	        fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            clean.add(fo);
            System.out.println("ADD "+fo.getFolderName()+" : "+fo.getObjectID());
            return fo;
        } finally {
        	if(writecon != null)
        		DBPool.pushWrite(ctx, writecon);
        }
    }

    public void switchUser(User user) {
        if(user.getId() == user1.getId()) {
          session = session1;
        } else if (user.getId() == user2.getId()) {
          session = session2;
        } else {
            throw new IllegalArgumentException("I don't know user "+user.getId());
        }
    }

    protected void rm(int objectID) throws SQLException, OXFolderPermissionException, OXFolderLogicException, Exception {
		//OXFolderAction ofa = new OXFolderAction(session);
		final OXFolderManager oxma = new OXFolderManagerImpl(session);
		//ofa.deleteFolder(objectID, session, true, System.currentTimeMillis());
		oxma.deleteFolder(new FolderObject(objectID), true, System.currentTimeMillis());
	}

    public Session getSessionObject() {
        return session;
    }
}
