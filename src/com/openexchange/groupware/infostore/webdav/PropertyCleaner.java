package com.openexchange.groupware.infostore.webdav;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.FolderEvent;
import com.openexchange.event.InfostoreEvent;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.sessiond.SessionObject;

public class PropertyCleaner implements FolderEvent, InfostoreEvent {
	
	private PropertyStore infoProperties;
	private PropertyStore folderProperties;
	
	private static final Log LOG = LogFactory.getLog(PropertyCleaner.class);

	public PropertyCleaner(final PropertyStore folderProperties, final PropertyStore infoProperties){
		this.folderProperties = folderProperties;
		this.infoProperties = infoProperties;
		
	}

	public void folderCreated(final FolderObject folderObj, final SessionObject sessionObj) {

	}

	public void folderDeleted(final FolderObject folderObj, final SessionObject sessionObj) {
		try {
			folderProperties.startTransaction();
			folderProperties.removeAll(folderObj.getObjectID(), sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			folderProperties.commit();
		} catch (final TransactionException e) {
			LOG.fatal(e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} catch (final OXException e) {
			LOG.fatal(e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} finally {
			try {
				folderProperties.finish();
			} catch (final TransactionException e) {
				LOG.error(e);
			}
		}
	}

	public void folderModified(final FolderObject folderObj, final SessionObject sessionObj) {

	}

	public void infoitemCreated(final DocumentMetadata metadata,
			final SessionObject sessionObject) {

	}

	public void infoitemDeleted(final DocumentMetadata metadata,
			final SessionObject sessionObject) {

	}

	public void infoitemModified(final DocumentMetadata metadata,
			final SessionObject sessionObj) {
		try {
			infoProperties.startTransaction();
			infoProperties.removeAll(metadata.getId(), sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			infoProperties.commit();
		} catch (final TransactionException e) {
			LOG.fatal(e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} catch (final OXException e) {
			LOG.fatal(e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} finally {
			try {
				infoProperties.finish();
			} catch (final TransactionException e) {
				LOG.error(e);
			}
		}
	}

}
