package com.openexchange.index.solr.internal;

import java.util.Collection;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.TriggerType;
import com.openexchange.index.solr.IndexServer;
import com.openexchange.index.solr.IndexUrl;
import com.openexchange.index.solr.SolrCoreStore;

public class SolrIndexAccess implements IndexAccess {
	
	private final int contextId;
	
	private final int userId;
	
	private final int module;
	
	private final ConfigurationService config;
	
	
	public SolrIndexAccess(final int contextId, final int userId, final int module, final ConfigurationService config) {
		super();
		this.contextId = contextId;
		this.userId = userId;
		this.module = module;
		this.config = config;
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
	
	private IndexUrl getIndexUrl() throws OXException {
		final ConfigIndexMysql indexMysql = ConfigIndexMysql.getInstance();
        final boolean hasActiveCore = indexMysql.hasActiveCore(contextId, userId, module);
        if (!hasActiveCore) {
            final SolrCoreStore coreStore = indexMysql.getCoreStore(contextId, userId, module);
            final SolrCore core = startUpSolrCore(contextId, userId, module, coreStore);
            final String server = config.getProperty("com.openexchange.index.solrHost");            
            if (!indexMysql.activateCoreEntry(contextId, userId, module, server)) {
                /*
                 * Somebody else tried to start up a core for this index and was faster.
                 */
                shutDownSolrCore(core);
            }
        }
        
        final SolrCore core = indexMysql.getSolrCore(contextId, userId, module);
        fillIndexServer(core.getServer());
        final IndexUrlImpl indexUrl = new IndexUrlImpl(core);
        
        return indexUrl;
	}
	
	private void fillIndexServer(final IndexServer server) {
        server.setConnectionTimeout(config.getIntProperty("com.openexchange.index.connectionTimeout", 100));
        server.setSoTimeout(config.getIntProperty("com.openexchange.index.socketTimeout", 1000));
        server.setMaxConnectionsPerHost(config.getIntProperty("com.openexchange.index.maxConnections", 100));
    }
    
    private SolrCore startUpSolrCore(final int cid, final int uid, final int module, final SolrCoreStore coreStore) throws OXException {
        final String solrHost = config.getProperty("com.openexchange.index.solrHost");  
        final IndexServer indexServer = new IndexServer();
        indexServer.setUrl(solrHost);
        fillIndexServer(indexServer);
        
        final SolrCore core = new SolrCore(cid, uid, module);
        core.setStore(coreStore);
        core.setServer(indexServer);
        /*
         * TODO: Start up Solr core on this machine using underlying kippdata management service.
         * Return the cores name. 
         */
        return core;
    }
    
    private void shutDownSolrCore(final SolrCore core) throws OXException {
        /*
         * TODO: Shut down Solr core on this machine using underlying kippdata management service.
         */
    }

}
