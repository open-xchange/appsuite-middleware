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

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.solr.IndexServer;
import com.openexchange.index.solr.IndexUrl;
import com.openexchange.index.solr.SolrCoreStore;


/**
 * {@link SolrCoreManager}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrCoreManager {
    
    private final SolrIndexMysql indexMysql;
    
    private final int contextId;
    
    private final int userId;
    
    private final int module;
    
    private boolean isPrimary;
    
    private IndexUrl cachedIndexUrl;
    
    private long lastAccess;
    
    
    public SolrCoreManager(final int contextId, final int userId, final int module) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.module = module;
        isPrimary = false;
        cachedIndexUrl = null;
        indexMysql = SolrIndexMysql.getInstance();
        lastAccess = System.currentTimeMillis();
    }
    
    public IndexUrl getIndexUrl() throws OXException {
        lastAccess = System.currentTimeMillis();
        if (isPrimary && cachedIndexUrl != null) {
            return cachedIndexUrl;
        }
        
        final boolean hasActiveCore = indexMysql.hasActiveCore(contextId, userId, module);
        if (!hasActiveCore) {
            final SolrCoreStore coreStore = indexMysql.getCoreStore(contextId, userId, module);
            final SolrCore core = startUpSolrCore(coreStore);
            final ConfigurationService config = Services.getService(ConfigurationService.class);
            final String server = config.getProperty("com.openexchange.index.solrHost");            
            if (indexMysql.activateCoreEntry(contextId, userId, module, server)) {
                isPrimary = true;
                cachedIndexUrl = fetchIndexUrl();
                
                return cachedIndexUrl; 
            } else {
                /*
                 * Somebody else tried to start up a core for this index and was faster.
                 */
                shutDownSolrCore(core);
            }
        }
        
        return fetchIndexUrl();
    }
    
    public void releaseIndexUrl() throws OXException {
        if (isPrimary) {
            try {
                if (indexMysql.hasActiveCore(contextId, userId, module)) {
                    final SolrCore core = indexMysql.getSolrCore(contextId, userId, module);
                    indexMysql.deactivateCoreEntry(contextId, userId, module);
                    shutDownSolrCore(core);
                }
            } finally {
                isPrimary = false;
                cachedIndexUrl = null;
            }            
        }
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public long getLastAccess() {
        return lastAccess;
    }
    
    private IndexUrl fetchIndexUrl() throws OXException {
        final SolrCore core = indexMysql.getSolrCore(contextId, userId, module);
        fillIndexServer(core.getServer());
        final IndexUrlImpl indexUrl = new IndexUrlImpl(core);
        
        return indexUrl;
    }
    
    private void fillIndexServer(final IndexServer server) {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        server.setConnectionTimeout(config.getIntProperty("com.openexchange.index.connectionTimeout", 100));
        server.setSoTimeout(config.getIntProperty("com.openexchange.index.socketTimeout", 1000));
        server.setMaxConnectionsPerHost(config.getIntProperty("com.openexchange.index.maxConnections", 100));
    }
    
    private SolrCore startUpSolrCore(final SolrCoreStore coreStore) throws OXException {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String solrHost = config.getProperty("com.openexchange.index.solrHost");  
        final IndexServer indexServer = new IndexServer();
        indexServer.setUrl(solrHost);
        fillIndexServer(indexServer);
        
        final SolrCore core = new SolrCore(new SolrIndexIdentifier(contextId, userId, module));
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
