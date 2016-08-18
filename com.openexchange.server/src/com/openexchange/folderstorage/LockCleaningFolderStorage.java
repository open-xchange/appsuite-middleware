package com.openexchange.folderstorage;

import com.openexchange.exception.OXException;

public interface LockCleaningFolderStorage {

    public void cleanLocksFor(Folder folder, int userIds[], final StorageParameters storageParameters) throws OXException;

}
