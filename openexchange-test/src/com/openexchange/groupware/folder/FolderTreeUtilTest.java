package com.openexchange.groupware.folder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.groupware.FolderTreeUtil;
import com.openexchange.groupware.FolderTreeUtilImpl;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.server.DBPool;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderAction;
import com.openexchange.tools.oxfolder.OXFolderLogicException;
import com.openexchange.tools.oxfolder.OXFolderPermissionException;

public class FolderTreeUtilTest extends FolderTestCase {
	FolderTreeUtil treeUtil = new FolderTreeUtilImpl(new DBPoolProvider());

	public void testPathIDs() throws Exception {
		FolderObject folder = mkdir(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, "folder");
		clean.add(folder);
		
		FolderObject subfolder = mkdir(folder.getObjectID(), "subfolder");
		
		FolderObject subsubfolder = mkdir(subfolder.getObjectID(), "subsubfolder");
		
		List<Integer> ids = treeUtil.getPath(subsubfolder.getObjectID(), ctx, user, userConfig);
		
		assertEquals(5, ids.size());
		assertEquals((int)folder.getObjectID(), (int) ids.get(2));
		assertEquals((int)subfolder.getObjectID(), (int) ids.get(3));
		assertEquals((int)subsubfolder.getObjectID(), (int) ids.get(4));
		
	}
	
}
