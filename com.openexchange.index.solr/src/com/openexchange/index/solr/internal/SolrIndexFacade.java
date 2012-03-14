package com.openexchange.index.solr.internal;

import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacade;
import com.openexchange.session.Session;

public class SolrIndexFacade implements IndexFacade {
	
	private final ConfigurationService config;
	
	private final ConcurrentHashMap<SolrIndexIdentifier, SolrIndexAccess> accessMap;
	
	
	public SolrIndexFacade(final ConfigurationService config) {
		super();
		this.config = config;
		accessMap = new ConcurrentHashMap<SolrIndexIdentifier, SolrIndexAccess>();
	}

	@Override
	public IndexAccess aquireIndexAccess(final int contextId, final int userId, final int module) throws OXException {
	    final SolrIndexIdentifier identifier = new SolrIndexIdentifier(contextId, userId, module);
		if (!accessMap.containsKey(identifier)) {
		    accessMap.putIfAbsent(identifier, new SolrIndexAccess(identifier, config));
		}
		
		final SolrIndexAccess keptIndexAccess = accessMap.get(identifier);
		keptIndexAccess.incrementRetainCount();
		return keptIndexAccess;
	}

	@Override
	public void releaseIndexAccess(final IndexAccess indexAccess) throws OXException {
	    final SolrIndexAccess solrIndexAccess = (SolrIndexAccess) indexAccess;
	    final SolrIndexIdentifier identifier = solrIndexAccess.getIdentifier();
	    if (accessMap.containsKey(identifier)) {
	        final SolrIndexAccess keptIndexAccess = accessMap.get(identifier);
	        final int retainCount = keptIndexAccess.decrementRetainCount();
	        if (retainCount == 0) {
	            keptIndexAccess.release();
	        }
	    }		
	}

	@Override
	public IndexAccess aquireIndexAccess(final int module, final Session session) throws OXException {
		return aquireIndexAccess(session.getContextId(), session.getUserId(), module);
	}

}
