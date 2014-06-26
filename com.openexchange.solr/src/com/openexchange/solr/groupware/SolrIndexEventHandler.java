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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.solr.groupware;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrIndexEventProperties;
import com.openexchange.solr.internal.DelegationSolrAccessImpl;
import com.openexchange.solr.internal.Services;
import com.openexchange.solr.internal.SolrCoreTools;
import com.openexchange.solr.rmi.RMISolrAccessService;


/**
 * {@link SolrIndexEventHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrIndexEventHandler implements EventHandler {

    private final DelegationSolrAccessImpl solrAccess;


    public SolrIndexEventHandler(DelegationSolrAccessImpl solrAccess) {
        super();
        this.solrAccess = solrAccess;
    }

    @Override
    public void handleEvent(Event event) {
        try {
            if (event.getTopic().equals(SolrIndexEventProperties.TOPIC_LOCK_INDEX)) {
                Integer contextId = (Integer) event.getProperty(SolrIndexEventProperties.PROP_CONTEXT_ID);
                Integer userId = (Integer) event.getProperty(SolrIndexEventProperties.PROP_USER_ID);
                Integer module = (Integer) event.getProperty(SolrIndexEventProperties.PROP_MODULE);
                SolrCoreIdentifier identifier = new SolrCoreIdentifier(contextId, userId, module);
                HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
                String ownAddress = SolrCoreTools.resolveSocketAddress(hazelcast.getCluster().getLocalMember().getInetSocketAddress());
                IMap<String, String> solrCores = hazelcast.getMap(SolrCoreTools.SOLR_CORE_MAP);
                String owner = solrCores.get(identifier.toString());
                if (owner != null) {
                    if (owner.equals(ownAddress)) {
                        solrAccess.freeResources(identifier);
                    } else {
                        ConfigurationService config = Services.getService(ConfigurationService.class);
                        int rmiPort = config.getIntProperty("com.openexchange.rmi.port", 1099);
                        Registry registry = LocateRegistry.getRegistry(owner, rmiPort);
                        RMISolrAccessService rmiAccess = (RMISolrAccessService) registry.lookup(RMISolrAccessService.RMI_NAME);
                        rmiAccess.freeResources(identifier);
                    }
                }
            }
        } catch (Throwable t) {
        }
    }

}
