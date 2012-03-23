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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.solr.SolrCoreConfigService;
import com.openexchange.solr.SolrManagementService;
import com.openexchange.solr.rmi.SolrServerRMI;

/**
 * {@link AbstractSolrIndexAccess}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractSolrIndexAccess<V> implements IndexAccess<V> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractSolrIndexAccess.class));

    protected final int contextId;

    protected final int userId;

    protected final int module;
    
    private final SolrIndexMysql indexMysql;

    private final SolrIndexIdentifier identifier;

    private final AtomicInteger retainCount;

    private boolean isPrimary;

    private long lastAccess;

    private String serverAddress = null;
    

    /**
     * Initializes a new {@link AbstractSolrIndexAccess}.
     * 
     * @param identifier The Solr index identifier
     */
    public AbstractSolrIndexAccess(final SolrIndexIdentifier identifier) {
        super();
        this.identifier = identifier;
        this.contextId = identifier.getContextId();
        this.userId = identifier.getUserId();
        this.module = identifier.getModule();
        isPrimary = false;
        indexMysql = SolrIndexMysql.getInstance();
        lastAccess = System.currentTimeMillis();
        retainCount = new AtomicInteger(0);
    }
    
    /*
     * Public methods
     */
    @Override
    public void release() {
        try {
            if (indexMysql.hasActiveCore(contextId, userId, module)) {
                final SolrCore core = indexMysql.getSolrCore(contextId, userId, module);
                indexMysql.deactivateCoreEntry(contextId, userId, module);
                shutDownSolrCore(core);
            }
        } catch (final OXException e) {
            LOG.warn(e.getLogMessage(), e);
        } finally {
            isPrimary = false;
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
        return isPrimary;
    }

    public long getLastAccess() {
        return lastAccess;
    }
    
    /*
     * Protected methods
     */
    protected SolrResponse addDocument(final SolrInputDocument document, final boolean commit) throws OXException {
        final UpdateRequest request = new UpdateRequest();
        request.add(document);
        
        return request(request, commit);
    }
    
    protected SolrResponse addDocuments(final Collection<SolrInputDocument> documents, final boolean commit) throws OXException {
        final UpdateRequest request = new UpdateRequest();
        request.add(documents);
        
        return request(request, commit);
    }
    
    protected SolrResponse commit() throws OXException {
        final UpdateRequest request = new UpdateRequest();
        request.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
        
        return request(request, false);
    }
    
    protected SolrResponse optimize() throws OXException {
        final UpdateRequest request = new UpdateRequest();
        request.setAction(AbstractUpdateRequest.ACTION.OPTIMIZE, true, true, 1);
        
        return request(request, false);
    }
    
    protected SolrResponse deleteDocumentById(final String id) throws OXException {
        final UpdateRequest request = new UpdateRequest();
        request.deleteById(id);
        
        return request(request, true);
    }
    
    protected SolrResponse deleteDocumentsByQuery(final String query) throws OXException {
        final UpdateRequest request = new UpdateRequest();
        request.deleteByQuery(query);
        
        return request(request, true);
    }
    
    protected QueryResponse query(final SolrParams query) throws OXException {
        final QueryRequest request = new QueryRequest(query);     
        final SolrResponse solrResponse = request(request, false);
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(solrResponse.getResponse());
        queryResponse.setElapsedTime(solrResponse.getElapsedTime());
        
        return queryResponse;
    }

    /*
     * Private methods
     */
    private SolrResponse request(final SolrRequest request, final boolean commit) throws OXException {
        lastAccess = System.currentTimeMillis();
        if (isPrimary) {
            return requestLocally(request, commit);
        }

        final boolean hasActiveCore = indexMysql.hasActiveCore(contextId, userId, module);
        if (!hasActiveCore) {
            final SolrCoreStore coreStore = indexMysql.getCoreStore(contextId, userId, module);
            final SolrCore core = startUpSolrCore(coreStore);
            final String server = getServerAddress();
            if (indexMysql.activateCoreEntry(contextId, userId, module, server)) {
                isPrimary = true;
                return requestLocally(request, commit);
            } else {
                /*
                 * Somebody else tried to start up a core for this index and was faster.
                 */
                shutDownSolrCore(core);
            }
        }
        
        return requestRemote(request, commit);
    }
    
    private SolrResponse requestLocally(final SolrRequest request, final boolean commit) throws OXException {
        final SolrManagementService solrService = Services.getService(SolrManagementService.class);
        return solrService.request(request, identifier.toString(), commit);
    }
    
    private SolrResponse requestRemote(final SolrRequest request, final boolean commit) throws OXException {
        final SolrCore core = indexMysql.getSolrCore(contextId, userId, module);
        final String coreServer = core.getServer();
        try {
            final ConfigurationService config = Services.getService(ConfigurationService.class);
            final int rmiPort = config.getIntProperty("RMI_PORT", 1099);
            final Registry registry = LocateRegistry.getRegistry(coreServer, rmiPort);
            final SolrServerRMI solrRMI = (SolrServerRMI) registry.lookup(SolrServerRMI.RMI_NAME);
            
            return solrRMI.request(request, identifier.toString(), commit);
        } catch (RemoteException e) {
            throw new OXException(e);
        } catch (NotBoundException e) {
            throw new OXException(e);
        }
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

    private SolrCore startUpSolrCore(final SolrCoreStore coreStore) throws OXException {
        final SolrIndexIdentifier identifier = new SolrIndexIdentifier(contextId, userId, module);
        final SolrCore core = new SolrCore(identifier);
        core.setStore(coreStore);
        core.setServer(getServerAddress());
        /*
         * TODO: Start up Solr core on this machine using underlying kippdata management service. Return the cores name.
         */
        {
            final SolrManagementService solrService = Services.getService(SolrManagementService.class);
            final SolrCoreConfigService coreService = Services.getService(SolrCoreConfigService.class);
            if (coreService.coreEnvironmentExists(contextId, userId, module)) {
                coreService.createCoreEnvironment(contextId, userId, module);
            }

            final SolrCoreConfiguration coreConfig = new SolrCoreConfiguration(coreStore.getUri(), identifier);
            solrService.createAndStartCore(
                coreConfig.getCoreName(),
                coreConfig.getInstanceDir(),
                coreConfig.getDataDir(),
                coreConfig.getSchemaPath(),
                coreConfig.getConfigPath());
        }

        return core;
    }

    private void shutDownSolrCore(final SolrCore core) throws OXException {
        final SolrManagementService solrService = Services.getService(SolrManagementService.class);
        solrService.shutdownCore(core.getIdentifier().toString());
    }

}
