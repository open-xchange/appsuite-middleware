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
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.solr.SolrCoreConfigService;
import com.openexchange.index.solr.SolrIndexExceptionCodes;


/**
 * {@link SolrCoreConfigServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrCoreConfigServiceImpl implements SolrCoreConfigService {    

    private final SolrIndexMysql indexMysql;
    

    public SolrCoreConfigServiceImpl() {
        super();
        indexMysql = SolrIndexMysql.getInstance();
    }
    
    @Override
    public List<SolrCoreStore> getAllStores() throws OXException {
        return indexMysql.getCoreStores();
    }

    @Override
    public int registerCoreStore(final SolrCoreStore store) throws OXException {
        return indexMysql.createCoreStoreEntry(store);
    }

    @Override
    public void modifyCoreStore(final SolrCoreStore store) throws OXException {
        indexMysql.updateCoreStoreEntry(store);
    }

    @Override
    public void unregisterCoreStore(final int storeId) throws OXException {
        indexMysql.removeCoreStoreEntry(storeId);
    }
    
    @Override
    public void createCoreEnvironment(final int cid, final int uid, final int module) throws OXException {
        indexMysql.createCoreEntry(cid, uid, module);
        
        final SolrCoreStore store = indexMysql.getCoreStore(cid, uid, module);
        final String baseUri = store.getUri();
        final SolrIndexIdentifier identifier = new SolrIndexIdentifier(cid, uid, module);
        final SolrCoreConfiguration config = new SolrCoreConfiguration(baseUri, identifier);
        
        URI instanceUri = null;
        URI dataUri = null;
        try {
            instanceUri = new URI(config.getInstanceDir());
            dataUri = new URI(config.getDataDir());
            final File instanceDir = new File(instanceUri);            
            if (instanceDir.exists()) {
                throw SolrIndexExceptionCodes.INSTANCE_DIR_EXISTS.create(instanceDir.toString());
            }
            
            if (instanceDir.mkdir()) {                
                final File dataDir = new File(dataUri);
                dataDir.mkdir();
            }
        } catch (final URISyntaxException e) {
            final String uri;
            if (instanceUri == null) {
                uri = config.getInstanceDir();
            } else {
                uri = config.getDataDir();
            }
            throw IndexExceptionCodes.URI_PARSE_ERROR.create(e, uri);
        }
    }
    
    @Override
    public void removeCoreEnvironment(final int cid, final int uid, final int module) throws OXException {
        final SolrCoreStore store = indexMysql.getCoreStore(cid, uid, module);
        final String baseUri = store.getUri();
        final SolrIndexIdentifier identifier = new SolrIndexIdentifier(cid, uid, module);
        final SolrCoreConfiguration config = new SolrCoreConfiguration(baseUri, identifier);
         
        try {
            final URI uri = new URI(config.getInstanceDir());
            final File instanceDir = new File(uri);
            if (instanceDir.exists()) {
                deleteDir(instanceDir);
            }
        } catch (final URISyntaxException e) {
            throw IndexExceptionCodes.URI_PARSE_ERROR.create(e, config.getInstanceDir());
        }        
        
        indexMysql.removeCoreEntry(cid, uid, module);
    }
    
    @Override
    public boolean coreEnvironmentExists(int contextId, int userId, int module) throws OXException {
        return indexMysql.coreEntryExists(contextId, userId, module);
    }
    
    private boolean deleteDir(final File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                final boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
