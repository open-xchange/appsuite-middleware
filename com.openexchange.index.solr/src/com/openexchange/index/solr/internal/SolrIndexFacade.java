package com.openexchange.index.solr.internal;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacade;
import com.openexchange.session.Session;

public class SolrIndexFacade implements IndexFacade {
	
	private final ConfigurationService config;
	
	
	public SolrIndexFacade(final ConfigurationService config) {
		super();
		this.config = config;
	}

	@Override
	public IndexAccess aquireIndexAccess(int contextId, int userId, int module) throws OXException {
		return new SolrIndexAccess(contextId, userId, module, config);
	}

	@Override
	public void releaseIndexAccess(IndexAccess indexAccess) throws OXException {
		// TODO: implement me
	}

	@Override
	public IndexAccess aquireIndexAccess(int module, Session session)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

}
