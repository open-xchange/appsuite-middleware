package com.openexchange.groupware.update.tasks;

public class InfostoreResolveFolderNameCollisions extends InfostoreRenamePersonalInfostoreFolders{
    public int addedWithVersion() {
        return 12;
    }

    protected int getParentFolder() {
        return -1;
    }
}
