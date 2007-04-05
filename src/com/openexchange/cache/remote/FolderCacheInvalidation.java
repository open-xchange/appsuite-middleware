/**
 * 
 */
package com.openexchange.cache.remote;

import java.rmi.RemoteException;

import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.cache.FolderCacheNotEnabledException;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.contexts.ContextStorage;

/**
 * FolderCacheInvalidation
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class FolderCacheInvalidation implements GenericCacheInvalidationInterface {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(FolderCacheInvalidation.class);
	
	private static final String REMOTE_NAME = "FolderCacheInvalidation";
	
	private static final String ERR = "Folder could not be remote-removed";

	/* (non-Javadoc)
	 * @see com.openexchange.cache.remote.GenericCacheInvalidationInterface#getRemoteName()
	 */
	public String getRemoteName() {
		return REMOTE_NAME;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.cache.remote.GenericCacheInvalidationInterface#invalidateCacheElement(int, int)
	 */
	public void invalidateCacheElement(final int contextId, final int objectId) throws RemoteException {
		if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
			try {
				FolderCacheManager.getInstance().removeFolderObject(objectId, ContextStorage.getInstance().getContext(contextId));
			} catch (FolderCacheNotEnabledException e) {
				throw new RemoteException(ERR, e);
			} catch (OXException e) {
				throw new RemoteException(ERR, e);
			} catch (ContextException e) {
				throw new RemoteException(ERR, e);
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.openexchange.cache.remote.GenericCacheInvalidationInterface#invalidateContext(int)
	 */
	public void invalidateContext(final int contextId) throws RemoteException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("Method invalidateContext() not implemented");
		}
	}

}
