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
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
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

    private String serverAddress = null;

    private final SolrIndexMysql indexMysql;
        

    public DelegationSolrAccessImpl(final EmbeddedSolrAccessImpl localDelegate) {
        super();
        this.embeddedAccess = localDelegate;
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
    public UpdateResponse add(final SolrCoreIdentifier identifier, final SolrInputDocument document, final boolean commit) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.add(identifier, document, commit);
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
    public UpdateResponse add(final SolrCoreIdentifier identifier, final Collection<SolrInputDocument> documents, final boolean commit) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.add(identifier, documents, commit);
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
    public UpdateResponse deleteById(final SolrCoreIdentifier identifier, final String id, final boolean commit) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.deleteById(identifier, id, commit);
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
    public UpdateResponse deleteByQuery(final SolrCoreIdentifier identifier, final String query, final boolean commit) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.deleteByQuery(identifier, query, commit);
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#commit(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse commit(final SolrCoreIdentifier identifier) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.commit(identifier);
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
    public UpdateResponse commit(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.commit(identifier, waitFlush, waitSearcher);
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#rollback(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse rollback(final SolrCoreIdentifier identifier) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.rollback(identifier);
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#optimize(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse optimize(final SolrCoreIdentifier identifier) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.optimize(identifier);
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
    public UpdateResponse optimize(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.optimize(identifier, waitFlush, waitSearcher);
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
    public UpdateResponse optimize(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher, final int maxSegments) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.optimize(identifier, waitFlush, waitSearcher, maxSegments);
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
    public QueryResponse query(final SolrCoreIdentifier identifier, final SolrParams params) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.query(identifier, params);
    }
    
	@Override
	public void freeResources(final SolrCoreIdentifier identifier) {
		embeddedAccess.freeResources(identifier);		
	}
    
    private SolrAccessService getDelegate(final SolrCoreIdentifier identifier) throws OXException {
    	final ConfigurationService config = Services.getService(ConfigurationService.class);
		final boolean isSolrNode = config.getBoolProperty(SolrProperties.PROP_IS_NODE, true);
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
			if (embeddedAccess.hasActiveCore(identifier) || embeddedAccess.startCore(identifier, false)) {
	            return embeddedAccess;
	        }
			
			final int contextId = identifier.getContextId();
	        final int userId = identifier.getUserId();
	        final int module = identifier.getModule();			
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
			final int contextId = identifier.getContextId();
	        final int userId = identifier.getUserId();
	        final int module = identifier.getModule();
			SolrCore solrCore = indexMysql.getSolrCore(contextId, userId, module);
			if (solrCore.isActive()) {
				return getRMIAccess(solrCore.getServer());
			}
			
			final MessagingService msgService = Services.getService(MessagingService.class);
        	final Map<String, Serializable> properties = new HashMap<String, Serializable>();
        	properties.put(MessagingConstants.PROP_IDENTIFIER, identifier);
        	final Message msg = new Message(MessagingConstants.START_CORE_TOPIC, properties);
        	msgService.postMessage(msg);

        	
        	// FIXME
        	return null;
		}   
    }
    
    
    private static final ConcurrentMap<String, RMISolrAccessService> rmiCache = new ConcurrentHashMap<String, RMISolrAccessService>();
    
    private SolrAccessService getRMIAccess(final String server) throws OXException {
    	RMISolrAccessService rmiAccess = rmiCache.get(server);
    	if (rmiAccess == null) {
    		rmiAccess = updateRmiCache(server);
    	} else {
    		try {
				rmiAccess.pingRmi();
			} catch (final RemoteException e) {
				rmiAccess = updateRmiCache(server);
			}    		
    	}
        
        return new SolrAccessServiceRmiWrapper(rmiAccess);
    }
    
    private RMISolrAccessService updateRmiCache(final String server) throws OXException {
    	try {
    		rmiCache.remove(server);
	    	final ConfigurationService config = Services.getService(ConfigurationService.class);
	        final int rmiPort = config.getIntProperty("RMI_PORT", 1099);
	        final Registry registry = LocateRegistry.getRegistry(server, rmiPort);
	        final RMISolrAccessService rmiAccess = (RMISolrAccessService) registry.lookup(RMISolrAccessService.RMI_NAME);

	        final RMISolrAccessService cachedRmiAccess = rmiCache.putIfAbsent(server, rmiAccess);
	        if (cachedRmiAccess == null) {
	        	return rmiAccess;
	        }
	        
	        return cachedRmiAccess;
    	} catch (final RemoteException e) {
            throw new OXException(e);
        } catch (final NotBoundException e) {
            throw new OXException(e);
        }        
    }

    private String getLocalServerAddress() throws OXException {
        if (serverAddress != null) {
            return serverAddress;
        }

        try {
            final InetAddress addr = InetAddress.getLocalHost();
            return serverAddress = addr.getHostAddress();
        } catch (final UnknownHostException e) {
            throw new OXException(e);
        }
    }
}
