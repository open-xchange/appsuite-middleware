package com.openexchange.index.solr.internal;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.TriggerType;
import com.openexchange.index.solr.IndexUrl;
import com.openexchange.log.Log;

public class SolrIndexAccess implements IndexAccess {
	
    private final int contextId;
    
    private final int userId;
    
    private final int module;
    
    private final SolrIndexIdentifier identifier;
    
    private final SolrCoreManager solrManager;
    
    private final AtomicInteger retainCount;
    
    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(SolrIndexAccess.class));
	
	
	public SolrIndexAccess(final SolrIndexIdentifier identifier) {
        super();
        this.identifier = identifier;
        this.contextId = identifier.getContextId();
        this.userId = identifier.getUserId();
        this.module = identifier.getModule();
        solrManager = new SolrCoreManager(contextId, userId, module);
        retainCount = new AtomicInteger(0);
	}

	@Override
	public void addEnvelopeData(IndexDocument document) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addEnvelopeData(Collection<IndexDocument> documents)
			throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addContent(IndexDocument document) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addContent(Collection<IndexDocument> documents)
			throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAttachments(IndexDocument document) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAttachments(Collection<IndexDocument> documents)
			throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(String id) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteByQuery(String query) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public IndexResult query(QueryParameters parameters) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriggerType getTriggerType() {
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public void release() {
        try {
            solrManager.releaseIndexUrl();
        } catch (OXException e) {
            LOG.error("Error while releasing index url.", e);
        }        
    }
    
    public SolrIndexIdentifier getIdentifier() {
        return identifier;
    }
    
    public int incrementRetainCount() {
        return retainCount.incrementAndGet();
    }
    
    public int decrementRetainCount() {
        return retainCount.decrementAndGet();
    }
    
    public boolean isPrimary() {
        return solrManager.isPrimary();
    }
    
    private IndexUrl getIndexUrl() throws OXException {
        return solrManager.getIndexUrl();
    }

    public long getLastAccess() {
        return solrManager.getLastAccess();
    }

}
