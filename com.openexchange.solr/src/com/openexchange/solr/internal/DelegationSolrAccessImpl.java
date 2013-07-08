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

import java.rmi.AccessException;
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
import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.Member;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.log.LogFactory;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.solr.SolrProperties;
import com.openexchange.solr.rmi.RMISolrAccessService;
import com.openexchange.solr.rmi.RMISolrException;

/**
 * {@link DelegationSolrAccessImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DelegationSolrAccessImpl implements SolrAccessService {

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
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            return delegate.add(identifier, document, commit);
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Add took " + diff + "ms for 1 document.");
            }
        }
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
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.add(identifier, documents, commit);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Add took " + diff + "ms for " + documents.size() + " documents.");
            }
        }
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
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.deleteById(identifier, id, commit);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Delete by id took " + diff + "ms.");
            }
        }
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
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.deleteByQuery(identifier, query, commit);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Delete by query took " + diff + "ms.");
            }
        }
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#commit(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse commit(SolrCoreIdentifier identifier) throws OXException {
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.commit(identifier);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Commit took " + diff + "ms.");
            }
        }
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
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.commit(identifier, waitFlush, waitSearcher);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Commit took " + diff + "ms.");
            }
        }
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#rollback(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse rollback(SolrCoreIdentifier identifier) throws OXException {
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.rollback(identifier);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Rollback took " + diff + "ms.");
            }
        }
    }

    /**
     * @param identifier
     * @return
     * @throws OXException
     * @see com.openexchange.solr.SolrAccessService#optimize(com.openexchange.solr.SolrCoreIdentifier)
     */
    @Override
    public UpdateResponse optimize(SolrCoreIdentifier identifier) throws OXException {
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.optimize(identifier);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Optimize took " + diff + "ms.");
            }
        }
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
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.optimize(identifier, waitFlush, waitSearcher);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Optimize took " + diff + "ms.");
            }
        }
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
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            UpdateResponse response = delegate.optimize(identifier, waitFlush, waitSearcher, maxSegments);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Optimize took " + diff + "ms.");
            }
        }
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
        long start = System.currentTimeMillis();
        try {
            SolrAccessService delegate = getDelegate(identifier);
            QueryResponse response = delegate.query(identifier, params);
            return response;
        } finally {
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Query took " + diff + "ms.");
            }
        }
    }

    @Override
    public void freeResources(SolrCoreIdentifier identifier) {
        if (embeddedAccess.hasActiveCore(identifier)) {
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            if (hazelcast != null) {
                LifecycleService lifecycleService = hazelcast.getLifecycleService();
                if (lifecycleService != null && lifecycleService.isRunning()) {
                    IMap<String, String> solrCores = hazelcast.getMap(SolrCoreTools.SOLR_CORE_MAP);
                    solrCores.lock(identifier.toString());
                    try {
                        SolrCoreTools.decrementCoreCount(hazelcast, hazelcast.getCluster().getLocalMember());
                        solrCores.remove(identifier.toString());
                        embeddedAccess.freeResources(identifier);
                    } finally {
                        solrCores.unlock(identifier.toString());
                    }
                }
            }
        }
    }

    public void shutDown() throws OXException {
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        if (hazelcast != null) {
            LifecycleService lifecycleService = hazelcast.getLifecycleService();
            if (lifecycleService != null && lifecycleService.isRunning()) {
                Collection<String> activeCores = embeddedAccess.getActiveCores();
                IMap<String, Integer> solrNodes = hazelcast.getMap(SolrCoreTools.SOLR_NODE_MAP);
                String localAddress = hazelcast.getCluster().getLocalMember().getInetSocketAddress().getAddress().getHostAddress();
                solrNodes.remove(localAddress);
                for (String coreName : activeCores) {
                    IMap<String, String> solrCores = hazelcast.getMap(SolrCoreTools.SOLR_CORE_MAP);
                    solrCores.removeAsync(coreName);
                }
            }
        }

        embeddedAccess.shutDown();
    }

    public EmbeddedSolrAccessImpl getEmbeddedServerAccess() {
        return embeddedAccess;
    }

    public SolrAccessService getDelegate(SolrCoreIdentifier identifier) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException("Parameter `identifier` must not be null!");
        }

        long start = System.currentTimeMillis();
        ConfigurationService config = Services.getService(ConfigurationService.class);
        boolean isSolrNode = config.getBoolProperty(SolrProperties.IS_NODE, false);
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        String ownAddress = SolrCoreTools.resolveSocketAddress(hazelcast.getCluster().getLocalMember().getInetSocketAddress());
        IMap<String, String> solrCores = hazelcast.getMap(SolrCoreTools.SOLR_CORE_MAP);
        solrCores.lock(identifier.toString());
        try {
            String owner = solrCores.get(identifier.toString());
            if (owner == null) {
                if (isSolrNode) {
                    try {
                        embeddedAccess.startCore(identifier);
                        SolrCoreTools.incrementCoreCount(hazelcast, hazelcast.getCluster().getLocalMember());
                        solrCores.put(identifier.toString(), ownAddress);

                        return embeddedAccess;
                    } catch (Throwable e) {
                        if (embeddedAccess.hasActiveCore(identifier)) {
                            embeddedAccess.stopCore(identifier);
                        }
                        throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
                    }
                } else {
                    try {
                        return startRemoteCore(solrCores, hazelcast, identifier);
                    } catch (OXException e) {
                        if (SolrExceptionCodes.CORE_NOT_STARTED.equals(e)) {
                            // Retry
                            return startRemoteCore(solrCores, hazelcast, identifier);
                        } else {
                            throw e;
                        }
                    }
                }
            } else if (owner.equals(ownAddress)) {
                if (embeddedAccess.hasActiveCore(identifier)) {
                    return embeddedAccess;
                } else {
                    try {
                        embeddedAccess.startCore(identifier);
                        return embeddedAccess;
                    } catch (Throwable t) {
                        solrCores.remove(identifier.toString());
                        throw SolrExceptionCodes.DELEGATION_ERROR.create(t);
                    }
                }
            }

            try {
                return getCachedRMIAccess(identifier, owner);
            } catch (OXException e) {
                if (SolrExceptionCodes.CORE_NOT_STARTED.equals(e)) {
                    // Retry
                    return startRemoteCore(solrCores, hazelcast, identifier);
                } else {
                    throw e;
                }
            }
        } finally {
            solrCores.unlock(identifier.toString());
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("getDelegate() lasted " + diff + "ms.");
            }
        }
    }

    private SolrAccessServiceRmiWrapper startRemoteCore(IMap<String, String> solrCores, HazelcastInstance hazelcast, SolrCoreIdentifier identifier) throws OXException {
        Member elected = electCoreOwner(hazelcast, identifier);
        FutureTask<String> task = new DistributedTask<String>(new StartCoreCallable(identifier, SolrCoreTools.resolveSocketAddress(elected.getInetSocketAddress())), elected);
        ExecutorService executorService = hazelcast.getExecutorService();
        executorService.execute(task);
        try {
            String electedAddress = SolrCoreTools.resolveSocketAddress(elected.getInetSocketAddress());
            task.get();
            SolrCoreTools.incrementCoreCount(hazelcast, elected);
            solrCores.put(identifier.toString(), electedAddress);

            return getCachedRMIAccess(identifier, electedAddress);
        } catch (InterruptedException e) {
            throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
        } catch (ExecutionException e) {
            throw SolrExceptionCodes.DELEGATION_ERROR.create(e);
        }
    }

    private Member electCoreOwner(HazelcastInstance hazelcast, SolrCoreIdentifier identifier) throws OXException {
        IMap<String, Integer> solrNodes = hazelcast.getMap(SolrCoreTools.SOLR_NODE_MAP);
        String lowestMember = null;
        Integer lowestCount = null;
        for (String memberAddress : solrNodes.keySet()) {
            if (memberAddress.equals(hazelcast.getCluster().getLocalMember().getInetSocketAddress().getAddress().getHostAddress())) {
                continue;
            }

            Integer coreCount = solrNodes.get(memberAddress);
            if (lowestCount == null || coreCount < lowestCount) {
                lowestCount = coreCount;
                lowestMember = memberAddress;
            }
        }

        Member elected = null;
        if (lowestMember == null) {
            throw SolrExceptionCodes.DELEGATION_ERROR.create();
        } else {
            Set<Member> members = hazelcast.getCluster().getMembers();
            for (Member member : members) {
                if (member.getInetSocketAddress().getAddress().getHostAddress().equals(lowestMember)) {
                    elected = member;
                    break;
                }
            }

            if (elected == null) {
                throw SolrExceptionCodes.DELEGATION_ERROR.create();
            }

            return elected;
        }
    }

    private SolrAccessServiceRmiWrapper getRMIAccess(SolrCoreIdentifier identifier, String server) throws OXException {
        try {
            ConfigurationService config = Services.getService(ConfigurationService.class);
            int rmiPort = config.getIntProperty("com.openexchange.rmi.port", 1099);
            Registry registry = LocateRegistry.getRegistry(server, rmiPort);
            RMISolrAccessService rmiAccess = (RMISolrAccessService) registry.lookup(RMISolrAccessService.RMI_NAME);
            rmiAccess.pingRmi(identifier);
            return new SolrAccessServiceRmiWrapper(rmiAccess);
        } catch (AccessException e) {
            throw new OXException(e);
        } catch (RemoteException e) {
            throw new OXException(e);
        } catch (NotBoundException e) {
            throw new OXException(e);
        } catch (RMISolrException e) {
            OXException exception = new OXException(e.getErrorCode(), e.getMessage(), OXExceptionConstants.MESSAGE_ARGS_EMPTY);
            exception.setPrefix("SOL");
            throw exception;
        }
    }

    private static ConcurrentMap<String, RMISolrAccessService> rmiCache = new ConcurrentHashMap<String, RMISolrAccessService>();

    private SolrAccessServiceRmiWrapper getCachedRMIAccess(SolrCoreIdentifier identifier, String server) throws OXException {
        RMISolrAccessService rmiAccess = rmiCache.get(server);
        if (rmiAccess == null) {
            rmiAccess = updateRmiCache(server);
        } else {
            try {
                rmiAccess.pingRmi(identifier);
            } catch (RemoteException e) {
                rmiAccess = updateRmiCache(server);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Ping failed for remote access on " + server + ". Reconnect.");
                }
            } catch (RMISolrException e) {
                OXException exception = new OXException(e.getErrorCode(), e.getMessage(), OXExceptionConstants.MESSAGE_ARGS_EMPTY);
                exception.setPrefix("SOL");
                throw exception;
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
            int rmiPort = config.getIntProperty("com.openexchange.rmi.port", 1099);
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
