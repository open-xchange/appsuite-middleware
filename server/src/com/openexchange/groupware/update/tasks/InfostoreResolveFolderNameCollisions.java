package com.openexchange.groupware.update.tasks;

public class InfostoreResolveFolderNameCollisions extends InfostoreRenamePersonalInfostoreFolders{
    @Override
	public int addedWithVersion() {
        return 12;
    }

    @Override
	protected int getParentFolder() {
        return -1;
    }
}
