package com.openexchange.index;

import com.openexchange.exception.OXException;

public interface IndexFacade {
	
	IndexAccess aquireIndexAccess(int contextId, int userId, int module)  throws OXException;
	
	void releaseIndexAccess(IndexAccess indexAccess) throws OXException;
	

}
