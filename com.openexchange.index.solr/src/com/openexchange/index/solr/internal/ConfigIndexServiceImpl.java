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

package com.openexchange.index.solr.internal;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.solr.ConfigIndexService;
import com.openexchange.index.solr.IndexExceptionCodes;
import com.openexchange.index.solr.IndexServer;
import com.openexchange.index.solr.IndexUrl;
import com.openexchange.index.solr.SolrCoreStore;


/**
 * {@link ConfigIndexServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConfigIndexServiceImpl implements ConfigIndexService {
    
    private final ConfigurationService config;
    

    public ConfigIndexServiceImpl(final ConfigurationService config) {
        super();
        this.config = config;
    }

    @Override
    public IndexUrl getReadOnlyURL(final int cid, final int uid, final int module) throws OXException {
        final ConfigIndexMysql indexMysql = ConfigIndexMysql.getInstance();
        final boolean hasActiveCore = indexMysql.hasActiveCore(cid, uid, module);
        if (!hasActiveCore) {
            final SolrCoreStore coreStore = indexMysql.getCoreStore(cid, uid, module);
            final SolrCore core = startUpSolrCore(cid, uid, module, coreStore);
            final String server = config.getProperty("com.openexchange.index.solrHost");            
            if (!indexMysql.activateCoreEntry(cid, uid, module, server)) {
                /*
                 * Somebody else tried to start up a core for this index and was faster.
                 */
                shutDownSolrCore(core);
            }
        }
        
        final SolrCore core = indexMysql.getSolrCore(cid, uid, module);
        fillIndexServer(core.getServer());
        final IndexUrlImpl indexUrl = new IndexUrlImpl(core);
        
        return indexUrl;
    }

    @Override
    public IndexUrl getWriteURL(final int cid, final int uid, final int module) throws OXException {
        // TODO: Until now there is now difference between read and write connection.
        // Change this here if it's going to be implemented.
        return getReadOnlyURL(cid, uid, module);
    }
    
    @Override
    public List<SolrCoreStore> getAllStores() throws OXException {
        return ConfigIndexMysql.getInstance().getCoreStores();
    }

    @Override
    public int registerCoreStore(final SolrCoreStore store) throws OXException {
        return ConfigIndexMysql.getInstance().createCoreStoreEntry(store);
    }

    @Override
    public void modifyCoreStore(final SolrCoreStore store) throws OXException {
        ConfigIndexMysql.getInstance().updateCoreStoreEntry(store);
    }

    @Override
    public void unregisterCoreStore(final int storeId) throws OXException {
        ConfigIndexMysql.getInstance().removeCoreStoreEntry(storeId);
    }
    
    @Override
    public void stopCore(final int cid, final int uid, final int module) throws OXException {
        final ConfigIndexMysql indexMysql = ConfigIndexMysql.getInstance();
        if (indexMysql.hasActiveCore(cid, uid, module)) {
            final SolrCore core = indexMysql.getSolrCore(cid, uid, module);
            indexMysql.deactivateCoreEntry(cid, uid, module);
            shutDownSolrCore(core);
        }
    }
    
    @Override
    public void createCore(final int cid, final int uid, final int module) throws OXException {
        final ConfigIndexMysql indexMysql = ConfigIndexMysql.getInstance();
        final SolrCoreStore store = indexMysql.getCoreStore(cid, uid, module);
        final String baseUri = store.getUri();
        final String coreName = SolrCoreStore.getCoreName(cid, uid, module);
        final String instanceUriStr = baseUri + File.pathSeparator + coreName;
        final String dataUriStr = baseUri + File.pathSeparator + coreName + File.pathSeparator + "data";
        URI instanceUri = null;
        URI dataUri = null;
        try {
            instanceUri = new URI(instanceUriStr);            
            final File instanceDir = new File(instanceUri);            
            if (instanceDir.exists()) {
                throw IndexExceptionCodes.INSTANCE_DIR_EXISTS.create(instanceDir.toString());
            }
            
            if (instanceDir.mkdir()) {                
                dataUri = new URI(dataUriStr);
                final File dataDir = new File(dataUri);
                dataDir.mkdir();
            }
        } catch (final URISyntaxException e) {
            final String uri;
            if (instanceUri == null) {
                uri = instanceUriStr;
            } else {
                uri = dataUriStr;
            }
            
            throw IndexExceptionCodes.URI_PARSE_ERROR.create(e, uri);
        }  
    }
    
    @Override
    public void deleteCore(final int cid, final int uid, final int module) throws OXException {
        stopCore(cid, uid, module);
        final ConfigIndexMysql indexMysql = ConfigIndexMysql.getInstance();
        final SolrCoreStore store = indexMysql.getCoreStore(cid, uid, module);
        final String baseUri = store.getUri();
        final String coreName = SolrCoreStore.getCoreName(cid, uid, module);
        final String uriStr = baseUri + File.pathSeparator + coreName;        
        try {
            final URI uri = new URI(uriStr);
            final File instanceDir = new File(uri);
            if (instanceDir.exists()) {
                deleteDir(instanceDir);
            }
        } catch (final URISyntaxException e) {
            throw IndexExceptionCodes.URI_PARSE_ERROR.create(e, uriStr);
        }        
    }
    
    private boolean deleteDir(final File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                final boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
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
