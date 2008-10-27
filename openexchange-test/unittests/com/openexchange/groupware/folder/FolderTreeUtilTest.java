package com.openexchange.groupware.folder;

import java.util.List;
import java.util.Random;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.impl.FolderTreeUtil;
import com.openexchange.groupware.impl.FolderTreeUtilImpl;
import com.openexchange.groupware.tx.DBPoolProvider;

public class FolderTreeUtilTest extends FolderTestCase {
	private final FolderTreeUtil treeUtil = new FolderTreeUtilImpl(new DBPoolProvider());

	private final Random r = new Random();
	
	public void testPathIDs() throws Exception {
		final FolderObject folder = mkdir(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, "folder"+r.nextInt());
		clean.add(folder);
		
		final FolderObject subfolder = mkdir(folder.getObjectID(), "subfolder");
		
		final FolderObject subsubfolder = mkdir(subfolder.getObjectID(), "subsubfolder");
		
		final List<Integer> ids = treeUtil.getPath(subsubfolder.getObjectID(), ctx, user, userConfig);
		
		assertEquals(6, ids.size());

        assertEquals(folder.getObjectID(), (int) ids.get(3));
		assertEquals(subfolder.getObjectID(), (int) ids.get(4));
		assertEquals(subsubfolder.getObjectID(), (int) ids.get(5));
		
	}
	
}
