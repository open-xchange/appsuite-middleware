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
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.osgi.SolrActivator;


/**
 * {@link StartCoreCallable}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class StartCoreCallable implements Callable<String>, Serializable {
    
    private static final long serialVersionUID = 6374396689822777915L;
    
    private final SolrCoreIdentifier identifier;
    
    private boolean startedCore = false;
    
    
    public StartCoreCallable(SolrCoreIdentifier identifier) {
        super();
        this.identifier = identifier;
    }
    
    public boolean startedCore() {
        return startedCore;
    }

    @Override
    public String call() throws Exception {
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        ILock lock = hazelcast.getLock("solrCoreStartupLock_" + identifier.toString());
        lock.lock();
        try {                    
            InetSocketAddress socketAddress = hazelcast.getCluster().getLocalMember().getInetSocketAddress();
            String newOwner;
            if (socketAddress.isUnresolved()) {
                newOwner = socketAddress.getHostName();
            } else {
                newOwner = socketAddress.getAddress().getHostAddress();
            }
            
            IMap<String, String> solrCores = hazelcast.getMap(DelegationSolrAccessImpl.SOLR_CORE_MAP);
            String owner = solrCores.putIfAbsent(identifier.toString(), newOwner);
            if (owner == null) {
                DelegationSolrAccessImpl accessService = (DelegationSolrAccessImpl) Services.getService(SolrAccessService.class);
                EmbeddedSolrAccessImpl embeddedAccess = accessService.getEmbeddedServerAccess();                
                embeddedAccess.startCore(identifier);
                startedCore = true;
                IMap<String, Integer> solrNodes = hazelcast.getMap(SolrActivator.SOLR_NODE_MAP);
                String localUuid = hazelcast.getCluster().getLocalMember().getUuid();
                solrNodes.lock(localUuid);
                try {
                    Integer integer = solrNodes.get(localUuid);
                    solrNodes.put(localUuid, new Integer(integer.intValue() + 1));
                } finally {
                    solrNodes.unlock(localUuid);
                }                
                
                return newOwner;
            }
            
            return owner;
        } finally {
            lock.unlock();
        }
    }

}
