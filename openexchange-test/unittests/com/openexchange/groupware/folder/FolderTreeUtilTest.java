package com.openexchange.groupware.folder;

import java.util.List;
import java.util.Random;

import com.openexchange.groupware.impl.FolderTreeUtil;
import com.openexchange.groupware.impl.FolderTreeUtilImpl;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tx.DBPoolProvider;

public class FolderTreeUtilTest extends FolderTestCase {
	private FolderTreeUtil treeUtil = new FolderTreeUtilImpl(new DBPoolProvider());

	private Random r = new Random();
	
	public void testPathIDs() throws Exception {
		FolderObject folder = mkdir(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, "folder"+r.nextInt());
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
