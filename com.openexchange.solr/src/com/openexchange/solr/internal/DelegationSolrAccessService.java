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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.concurrent.ConcurrentHashSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCore;
import com.openexchange.solr.SolrCoreConfiguration;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrCoreStore;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.solr.rmi.SolrServerRMI;

/**
 * {@link DelegationSolrAccessService}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DelegationSolrAccessService implements SolrAccessService {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DelegationSolrAccessService.class));

    private SolrAccessService localDelegate;

    private String serverAddress = null;

    private final SolrIndexMysql indexMysql;
    
    private final Set<SolrCoreIdentifier> startedCores;
    

    public DelegationSolrAccessService(final SolrAccessService defaultDelegate) {
        super();
        this.localDelegate = defaultDelegate;
        indexMysql = SolrIndexMysql.getInstance();
        startedCores = new ConcurrentHashSet<SolrCoreIdentifier>();
    }

    /**
     * @param identifier
     * @param instanceDir
     * @param dataDir
     * @param schemaPath
     * @param configPath
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#startCore(com.openexchange.solr.SolrCoreIdentifier, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public boolean startCore(SolrCoreConfiguration configuration) throws OXException {
        final SolrCoreIdentifier identifier = configuration.getIdentifier();
        final int contextId = identifier.getContextId();
        final int userId = identifier.getUserId();
        final int module = identifier.getModule();
        if (!startedCores.contains(identifier)) {            
            if (!indexMysql.hasActiveCore(contextId, userId, module)) {
                boolean started = localDelegate.startCore(configuration);
                if (started) {
                    if (!indexMysql.activateCoreEntry(contextId, userId, module, getServerAddress())) {
                        /*
                         * Somebody else tried to start up a core for this index and was faster.
                         */
                        localDelegate.stopCore(identifier);
                    }
                
                    startedCores.add(identifier);
                    return true;
                }
            }
        }
        
        LOG.warn("Could not start solr core. There already seems to be an active one for user " + userId + " and module " + module + " in context " + contextId + ".");
        return false;
    }

    /**
     * @param identifier
     * @throws OXException 
     * @see com.openexchange.solr.SolrAccessService#stopCore(com.openexchange.solr.SolrCoreIdentifier)
     */
    public boolean stopCore(SolrCoreIdentifier identifier) throws OXException {
        final int contextId = identifier.getContextId();
        final int userId = identifier.getUserId();
        final int module = identifier.getModule();
        if (startedCores.remove(identifier)) {
            indexMysql.deactivateCoreEntry(contextId, userId, module);
            localDelegate.stopCore(identifier);
            
            return true;       
        }
        
        LOG.info("User " + userId + " in context " + contextId + " tried to stop a solr core for module " + module + " that was not started by this instance.");
        return false;
    }

    /**
     * @param identifier
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#reloadCore(com.openexchange.solr.SolrCoreIdentifier)
     */
    public void reloadCore(SolrCoreIdentifier identifier) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        delegate.reloadCore(identifier);
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
    public UpdateResponse add(SolrCoreIdentifier identifier, SolrInputDocument document, boolean commit) throws OXException {
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
    public UpdateResponse add(SolrCoreIdentifier identifier, Collection<SolrInputDocument> documents, boolean commit) throws OXException {
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
    public UpdateResponse deleteById(SolrCoreIdentifier identifier, String id, boolean commit) throws OXException {
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
    public UpdateResponse deleteByQuery(SolrCoreIdentifier identifier, String query, boolean commit) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.deleteByQuery(identifier, query, commit);
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#commit(com.openexchange.solr.SolrCoreIdentifier)
     */
    public UpdateResponse commit(SolrCoreIdentifier identifier) throws OXException {
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
    public UpdateResponse commit(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.commit(identifier, waitFlush, waitSearcher);
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#rollback(com.openexchange.solr.SolrCoreIdentifier)
     */
    public UpdateResponse rollback(SolrCoreIdentifier identifier) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.rollback(identifier);
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#optimize(com.openexchange.solr.SolrCoreIdentifier)
     */
    public UpdateResponse optimize(SolrCoreIdentifier identifier) throws OXException {
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
    public UpdateResponse optimize(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher) throws OXException {
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
    public UpdateResponse optimize(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher, int maxSegments) throws OXException {
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
    public QueryResponse query(SolrCoreIdentifier identifier, SolrParams params) throws OXException {
        final SolrAccessService delegate = getDelegate(identifier);
        return delegate.query(identifier, params);
    }
    
    private SolrAccessService getDelegate(final SolrCoreIdentifier identifier) throws OXException {
        if (startedCores.contains(identifier)) {
            return localDelegate;
        }
        
        final int contextId = identifier.getContextId();
        final int userId = identifier.getUserId();
        final int module = identifier.getModule();
        SolrCore solrCore = indexMysql.getSolrCore(contextId, userId, module);        
        if (!solrCore.isActive()) {
            final SolrCoreStore coreStore = indexMysql.getCoreStore(contextId, userId, module);
            final SolrCoreConfiguration configuration = new SolrCoreConfiguration(coreStore.getUri(), identifier);
            if (startCore(configuration)) {                
                return localDelegate;   
            } else {
                /*
                 * There was no active core but we were not able to start it.
                 * We have to reload the core status from database to double check if another node was faster.
                 */
                solrCore = indexMysql.getSolrCore(contextId, userId, module);
            }            
        }

        if (solrCore.isActive()) {
            // TODO: cache stubs
            try {
                final ConfigurationService config = Services.getService(ConfigurationService.class);
                final int rmiPort = config.getIntProperty("RMI_PORT", 1099);
                final Registry registry = LocateRegistry.getRegistry(solrCore.getServer(), rmiPort);
                final SolrServerRMI solrRMI = (SolrServerRMI) registry.lookup(SolrServerRMI.RMI_NAME);

                return solrRMI;
            } catch (RemoteException e) {
                throw new OXException(e);
            } catch (NotBoundException e) {
                throw new OXException(e);
            }
        }
        
        throw SolrExceptionCodes.DELEGATION_ERROR.create();
    }

    private String getServerAddress() throws OXException {
        if (serverAddress != null) {
            return serverAddress;
        }

        try {
            final InetAddress addr = InetAddress.getLocalHost();
            return serverAddress = addr.getHostAddress();
        } catch (UnknownHostException e) {
            throw new OXException(e);
        }
    }
}
