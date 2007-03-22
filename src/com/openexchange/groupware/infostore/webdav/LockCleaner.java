package com.openexchange.groupware.infostore.webdav;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.FolderEvent;
import com.openexchange.event.InfostoreEvent;
import com.openexchange.groupware.FolderLockManager;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.sessiond.SessionObject;

public class LockCleaner implements FolderEvent, InfostoreEvent {

	private static final Log LOG = LogFactory.getLog(LockCleaner.class);
	
	private EntityLockManager infoLockManager;
	private FolderLockManager folderLockManager;

	public LockCleaner(FolderLockManager folderLockManager, EntityLockManager infoLockManager) {
		this.folderLockManager = folderLockManager;
		this.infoLockManager = infoLockManager;
	}

	
	public void folderDeleted(FolderObject folderObj, SessionObject sessionObj) {
		try {
			folderLockManager.removeAll(folderObj.getObjectID(), sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
		} catch (OXException e) {
			LOG.fatal("Couldn't remove folder locks from folder "+folderObj.getObjectID()+" in context "+sessionObj.getContext().getContextId()+". Run the consistency tool.");
		}
	}

	
	public void infoitemDeleted(DocumentMetadata metadata, SessionObject sessionObj) {
		try {
			infoLockManager.removeAll(metadata.getId(), sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
		} catch (OXException e) {
			LOG.fatal("Couldn't remove locks from infoitem "+metadata.getId()+" in context "+sessionObj.getContext().getContextId()+". Run the consistency tool.");
		}	
	}

	public void folderCreated(FolderObject folderObj, SessionObject sessionObj) {
		
	}
	
	public void folderModified(FolderObject folderObj, SessionObject sessionObj) {
		
	}

	public void infoitemCreated(DocumentMetadata metadata, SessionObject sessionObject) {
		
	}
	
	public void infoitemModified(DocumentMetadata metadata, SessionObject sessionObject) {
		
	}

}
