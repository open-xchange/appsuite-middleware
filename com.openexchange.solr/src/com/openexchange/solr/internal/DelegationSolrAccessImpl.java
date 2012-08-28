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

import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.solr.SolrProperties;
import com.openexchange.solr.osgi.SolrActivator;
import com.openexchange.solr.rmi.RMISolrAccessService;

/**
 * {@link DelegationSolrAccessImpl}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DelegationSolrAccessImpl implements SolrAccessService {

    public static final String SOLR_CORE_MAP = "solrCoreMap";

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DelegationSolrAccessImpl.class));

    private final EmbeddedSolrAccessImpl embeddedAccess;

    public DelegationSolrAccessImpl(EmbeddedSolrAccessImpl localDelegate) {
        super();
        embeddedAccess = localDelegate;
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
        if (embeddedAccess.hasActiveCore(identifier)) {
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            IMap<String, String> solrCores = hazelcast.getMap(SOLR_CORE_MAP);
            solrCores.lock(identifier.toString());
            try {                
                decrementCoreCount(hazelcast);
                solrCores.remove(identifier.toString());
                embeddedAccess.freeResources(identifier);
            } finally {
                solrCores.unlock(identifier.toString());
            }            
        }
    }

    public void shutDown() throws OXException {
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        Collection<String> activeCores = embeddedAccess.getActiveCores();
        IMap<String, Integer> solrNodes = hazelcast.getMap(SolrActivator.SOLR_NODE_MAP);
        String localUuid = hazelcast.getCluster().getLocalMember().getUuid();
        solrNodes.remove(localUuid);
        for (String coreName : activeCores) {
            IMap<String, String> solrCores = hazelcast.getMap(SOLR_CORE_MAP);
            solrCores.removeAsync(coreName);
        }

        embeddedAccess.shutDown();
    }

    public EmbeddedSolrAccessImpl getEmbeddedServerAccess() {
        return embeddedAccess;
    }

    private SolrAccessService getDelegate(SolrCoreIdentifier identifier) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException("Parameter `identifier` must not be null!");
        }

        long start = System.currentTimeMillis();
        ConfigurationService config = Services.getService(ConfigurationService.class);
        boolean isSolrNode = config.getBoolProperty(SolrProperties.IS_NODE, false);
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        String ownAddress = resolveSocketAddress(hazelcast.getCluster().getLocalMember().getInetSocketAddress());
        IMap<String, String> solrCores = hazelcast.getMap(SOLR_CORE_MAP);
        solrCores.lock(identifier.toString());
        try {
            String owner = solrCores.get(identifier.toString());
            if (LOG.isDebugEnabled()) {
                LOG.debug(Thread.currentThread().getName() + " got the lock for core " + identifier.toString() + ". Current owner: " + owner);
            }

            if (owner == null) {
                if (isSolrNode) {
                    try {
                        embeddedAccess.startCore(identifier);
                        incrementCoreCount(hazelcast);
                        solrCores.put(identifier.toString(), ownAddress);
                        
                        return embeddedAccess;
                    } catch (Exception e) {
                        throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
                    }
                } else {
                    Member elected = electCoreOwner(hazelcast, identifier);
                    FutureTask<String> task = new DistributedTask<String>(new StartCoreCallable(identifier, resolveSocketAddress(elected.getInetSocketAddress())), elected);
                    ExecutorService executorService = hazelcast.getExecutorService();
                    executorService.execute(task);
                    try {
                        String electedAddress = resolveSocketAddress(elected.getInetSocketAddress());
                        task.get(1, TimeUnit.SECONDS);
                        incrementCoreCount(hazelcast);                        
                        solrCores.put(identifier.toString(), electedAddress);
                        
                        return getRMIAccess(electedAddress);
                    } catch (InterruptedException e) {
                        throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
                    } catch (ExecutionException e) {
                        throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
                    } catch (TimeoutException e) {
                        throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
                    }
                }
            } else if (owner.equals(ownAddress)) {
                return embeddedAccess;
            }
            
            return getRMIAccess(owner);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Thread.currentThread().getName() + " released the lock for core " + identifier.toString() + ".");
                long diff = System.currentTimeMillis() - start;
                LOG.debug("getDelegate() lasted " + diff + "ms.");
            }
            solrCores.unlock(identifier.toString());
        }        
    }

    private Member electCoreOwner(HazelcastInstance hazelcast, SolrCoreIdentifier identifier) throws OXException {
        IMap<String, Integer> solrNodes = hazelcast.getMap(SolrActivator.SOLR_NODE_MAP);
        String lowestMember = null;
        Integer lowestCount = null;
        for (String memberUuid : solrNodes.keySet()) {
            if (memberUuid.equals(hazelcast.getCluster().getLocalMember().getUuid())) {
                continue;
            }

            Integer coreCount = solrNodes.get(memberUuid);
            if (lowestCount == null || coreCount < lowestCount) {
                lowestCount = coreCount;
                lowestMember = memberUuid;
            }
        }

        Member elected = null;
        if (lowestMember == null) {
            throw SolrExceptionCodes.DELEGATION_ERROR.create();
        } else {
            Set<Member> members = hazelcast.getCluster().getMembers();
            for (Member member : members) {
                if (member.getUuid().equals(lowestMember)) {
                    elected = member;
                    break;
                }
            }

            if (elected == null) {
                throw SolrExceptionCodes.DELEGATION_ERROR.create();
            }

            return elected;
            //
            // FutureTask<String> task = new DistributedTask<String>(new StartCoreCallable(identifier,
            // resolveSocketAddress(elected.getInetSocketAddress())), elected);
            // ExecutorService executorService = hazelcast.getExecutorService();
            // executorService.execute(task);
            // try {
            // incrementCoreCount(hazelcast);
            // return task.get(1, TimeUnit.SECONDS);
            // } catch (InterruptedException e) {
            // throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
            // } catch (ExecutionException e) {
            // throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
            // } catch (TimeoutException e) {
            // throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
            // }
        }
    }

    private void incrementCoreCount(HazelcastInstance hazelcast) {
        IMap<String, Integer> solrNodes = hazelcast.getMap(SolrActivator.SOLR_NODE_MAP);
        String localUuid = hazelcast.getCluster().getLocalMember().getUuid();
        solrNodes.lock(localUuid);
        try {
            Integer integer = solrNodes.get(localUuid);
            solrNodes.put(localUuid, new Integer(integer.intValue() + 1));
        } finally {
            solrNodes.unlock(localUuid);
        }
    }

    private void decrementCoreCount(HazelcastInstance hazelcast) {
        IMap<String, Integer> solrNodes = hazelcast.getMap(SolrActivator.SOLR_NODE_MAP);
        String localUuid = hazelcast.getCluster().getLocalMember().getUuid();
        solrNodes.lock(localUuid);
        try {
            Integer integer = solrNodes.get(localUuid);
            solrNodes.put(localUuid, new Integer(integer.intValue() - 1));
        } finally {
            solrNodes.unlock(localUuid);
        }
    }

    private String resolveSocketAddress(InetSocketAddress addr) {
        if (addr.isUnresolved()) {
            return addr.getHostName();
        } else {
            return addr.getAddress().getHostAddress();
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
