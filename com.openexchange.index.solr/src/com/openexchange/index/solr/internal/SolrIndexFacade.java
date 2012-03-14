package com.openexchange.index.solr.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacade;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;

public class SolrIndexFacade implements IndexFacade {
	
	private final ConcurrentHashMap<SolrIndexIdentifier, SolrIndexAccess> accessMap;
	
	
	public SolrIndexFacade() {
		super();
		accessMap = new ConcurrentHashMap<SolrIndexIdentifier, SolrIndexAccess>();
		final TimerService timerService = IndexServiceLookup.getInstance().getService(TimerService.class);
		timerService.scheduleAtFixedRate(new SolrCoreShutdownTask(this), SolrCoreShutdownTask.TIMEOUT, SolrCoreShutdownTask.TIMEOUT, TimeUnit.MINUTES);
	}

	@Override
	public IndexAccess aquireIndexAccess(final int contextId, final int userId, final int module) throws OXException {
	    final SolrIndexIdentifier identifier = new SolrIndexIdentifier(contextId, userId, module);
		if (!accessMap.containsKey(identifier)) {
		    accessMap.putIfAbsent(identifier, new SolrIndexAccess(identifier));
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
	
	public List<SolrIndexAccess> getActivePrimaryAccesses() {
	    final List<SolrIndexAccess> accessList = new ArrayList<SolrIndexAccess>();
	    for (final SolrIndexAccess access : accessMap.values()) {
	        if (access.isPrimary()) {
	            accessList.add(access);
	        }
	    }
	    
	    return accessList;
	}
	
	public void removeFromCache(final List<SolrIndexIdentifier> identifiers) {
	    for (final SolrIndexIdentifier identifier : identifiers) {
	        accessMap.remove(identifier);
	    }	    
	}

}
