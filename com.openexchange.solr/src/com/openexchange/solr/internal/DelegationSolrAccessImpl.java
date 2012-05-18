/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.solr.internal;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.service.messaging.Message;
import com.openexchange.service.messaging.MessagingService;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCore;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.solr.SolrProperties;
import com.openexchange.solr.rmi.RMISolrAccessService;

/**
 * {@link DelegationSolrAccessImpl}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DelegationSolrAccessImpl implements SolrAccessService {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DelegationSolrAccessImpl.class));

    private final EmbeddedSolrAccessImpl embeddedAccess;

    private final SolrIndexMysql indexMysql;
        

    public DelegationSolrAccessImpl(EmbeddedSolrAccessImpl localDelegate) {
        super();
        embeddedAccess = localDelegate;
        indexMysql = SolrIndexMysql.getInstance();
    }

    /**
     * @param identifier
     * @param document
     * @param commit
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#add(com.openexchange.solr.SolrCoreIdentifier, org.apache.solr.common.SolrInputDocument,
     *      boolean)
     */
    @Override
    public UpdateResponse add(SolrCoreIdentifier identifier, SolrInputDocument document, boolean commit) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.add(identifier, document, commit);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add took " + response.getElapsedTime() + "ms for 1 document.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @param documents
     * @param commit
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#add(com.openexchange.solr.SolrCoreIdentifier, java.util.Collection, boolean)
     */
    @Override
    public UpdateResponse add(SolrCoreIdentifier identifier, Collection<SolrInputDocument> documents, boolean commit) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.add(identifier, documents, commit);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add took " + response.getElapsedTime() + "ms for " + documents.size() + " documents.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @param id
     * @param commit
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#deleteById(com.openexchange.solr.SolrCoreIdentifier, java.lang.String, boolean)
     */
    @Override
    public UpdateResponse deleteById(SolrCoreIdentifier identifier, String id, boolean commit) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.deleteById(identifier, id, commit);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete took " + response.getElapsedTime() + "ms for 1 document.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @param query
     * @param commit
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#deleteByQuery(com.openexchange.solr.SolrCoreIdentifier, java.lang.String, boolean)
     */
    @Override
    public UpdateResponse deleteByQuery(SolrCoreIdentifier identifier, String query, boolean commit) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.deleteByQuery(identifier, query, commit);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete took " + response.getElapsedTime() + "ms for query " + query + ".");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#commit(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse commit(SolrCoreIdentifier identifier) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.commit(identifier);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Commit took " + response.getElapsedTime() + "ms.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @param waitFlush
     * @param waitSearcher
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#commit(com.openexchange.solr.SolrCoreIdentifier, boolean, boolean)
     */
    @Override
    public UpdateResponse commit(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.commit(identifier, waitFlush, waitSearcher);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Commit took " + response.getElapsedTime() + "ms.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#rollback(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse rollback(SolrCoreIdentifier identifier) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.rollback(identifier);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rollback took " + response.getElapsedTime() + "ms.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#optimize(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse optimize(SolrCoreIdentifier identifier) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.optimize(identifier);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Optimize took " + response.getElapsedTime() + "ms.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @param waitFlush
     * @param waitSearcher
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#optimize(com.openexchange.solr.SolrCoreIdentifier, boolean, boolean)
     */
    @Override
    public UpdateResponse optimize(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.optimize(identifier, waitFlush, waitSearcher);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Optimize took " + response.getElapsedTime() + "ms.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @param waitFlush
     * @param waitSearcher
     * @param maxSegments
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#optimize(com.openexchange.solr.SolrCoreIdentifier, boolean, boolean, int)
     */
    @Override
    public UpdateResponse optimize(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher, int maxSegments) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        UpdateResponse response = delegate.optimize(identifier, waitFlush, waitSearcher, maxSegments);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Optimize took " + response.getElapsedTime() + "ms.");
        }
        
        return response;
    }

    /**
     * @param identifier
     * @param params
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#query(com.openexchange.solr.SolrCoreIdentifier,
     *      org.apache.solr.common.params.SolrParams)
     */
    @Override
    public QueryResponse query(SolrCoreIdentifier identifier, SolrParams params) throws OXException {
        SolrAccessService delegate = getDelegate(identifier);
        QueryResponse response = delegate.query(identifier, params);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Query took " + response.getElapsedTime() + "ms and returned " + response.getResults().size() + " elements.");
        }
        
        return response;
    }
    
	@Override
	public void freeResources(SolrCoreIdentifier identifier) {
		embeddedAccess.freeResources(identifier);		
	}
    
    private SolrAccessService getDelegate(SolrCoreIdentifier identifier) throws OXException {
    	if (identifier == null) {
    		throw new IllegalArgumentException("Parameter `identifier` must not be null!");
    	}
    	
    	ConfigurationService config = Services.getService(ConfigurationService.class);
		boolean isSolrNode = config.getBoolProperty(SolrProperties.IS_NODE, false);
		if (isSolrNode) {
			/*
			 * Three possibilities here:
			 * 1. The core is already started within our node
			 * 	  => use embedded solr instance
			 * 
			 * 2. The core is not started yet
			 *    => start it up locally and return embedded solr instance
			 *    
			 * 3. The core is started on another node
			 *    => connect via RMI and return remote solr instance
			 */
			if (embeddedAccess.hasActiveCore(identifier) || embeddedAccess.startCore(identifier)) {
			    if (LOG.isDebugEnabled()) {
			        LOG.debug("Returning local solr access.");
			    }
			    
	            return embeddedAccess;
	        }
			
			int contextId = identifier.getContextId();
	        int userId = identifier.getUserId();
	        int module = identifier.getModule();			
			SolrCore solrCore = indexMysql.getSolrCore(contextId, userId, module);			
			if (solrCore.isActive()) {
				return getRMIAccess(solrCore.getServer());
			}
			
	        throw SolrExceptionCodes.DELEGATION_ERROR.create();	
		} else {
			/*
			 * Two possibilities here:
			 * 1. The core is already started on another node.
			 *    => connect via RMI and return remote solr instance
			 * 
			 * 2. The core is not started yet.
			 *    => Delegate core start up to solr nodes. And then?
			 */
			int contextId = identifier.getContextId();
	        int userId = identifier.getUserId();
	        int module = identifier.getModule();
			SolrCore solrCore = indexMysql.getSolrCore(contextId, userId, module);
			if (solrCore.isActive()) {
				return getRMIAccess(solrCore.getServer());
			}			
			
			MessagingService msgService = Services.getService(MessagingService.class);
        	Map<String, Serializable> properties = new HashMap<String, Serializable>();
        	properties.put(MessagingConstants.PROP_IDENTIFIER, identifier);
        	Message msg = new Message(MessagingConstants.START_CORE_TOPIC, properties);
        	msgService.postMessage(msg);
        	if (LOG.isDebugEnabled()) {
                LOG.debug("Requested a remote solr core startup for core " + identifier.toString() + ".");
            }

        	// FIXME: Try to sleep and reconnect here?
        	throw SolrExceptionCodes.CORE_NOT_STARTED.create(identifier.toString());
		}   
    }
    
    
    private static ConcurrentMap<String, RMISolrAccessService> rmiCache = new ConcurrentHashMap<String, RMISolrAccessService>();
    
    private SolrAccessService getRMIAccess(String server) throws OXException {
    	RMISolrAccessService rmiAccess = rmiCache.get(server);
    	if (rmiAccess == null) {
    		rmiAccess = updateRmiCache(server);
    	} else {
    		try {
				rmiAccess.pingRmi();
			} catch (RemoteException e) {
				rmiAccess = updateRmiCache(server);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Ping failed for remote access on " + server + ". Reconnect.");
				}
			}    		
    	}
        
    	if (LOG.isDebugEnabled()) {
			LOG.debug("Returning remote solr access to server " + server + ".");
		}
        return new SolrAccessServiceRmiWrapper(rmiAccess);
    }
    
    private RMISolrAccessService updateRmiCache(String server) throws OXException {
    	try {
    		rmiCache.remove(server);
	    	ConfigurationService config = Services.getService(ConfigurationService.class);
	        int rmiPort = config.getIntProperty("RMI_PORT", 1099);
	        Registry registry = LocateRegistry.getRegistry(server, rmiPort);
	        RMISolrAccessService rmiAccess = (RMISolrAccessService) registry.lookup(RMISolrAccessService.RMI_NAME);

	        RMISolrAccessService cachedRmiAccess = rmiCache.putIfAbsent(server, rmiAccess);
	        if (cachedRmiAccess == null) {
	        	return rmiAccess;
	        }
	        
	        return cachedRmiAccess;
    	} catch (RemoteException e) {
            throw new OXException(e);
        } catch (NotBoundException e) {
            throw new OXException(e);
        }        
    }
}
