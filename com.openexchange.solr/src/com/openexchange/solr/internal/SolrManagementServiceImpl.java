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

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;
import org.xml.sax.SAXException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.solr.SolrManagementService;
import com.openexchange.solr.SolrProperties;


/**
 * {@link SolrManagementServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrManagementServiceImpl implements SolrManagementService {
    
    private SolrServer solrServer;

    private final CoreContainer coreContainer;
    
    
    public SolrManagementServiceImpl() throws OXException {
        super();
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String solrHome = config.getProperty(SolrProperties.PROP_SOLR_HOME);
        final String solrXmlPath = config.getProperty(SolrProperties.PROP_SOLR_XML);
        final File solrXml = new File(solrXmlPath);
        coreContainer = new CoreContainer();
        try {
            coreContainer.load(solrHome, solrXml);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        
    }
    
    public void startUp() {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        solrServer = new EmbeddedSolrServer(coreContainer, config.getProperty(SolrProperties.PROP_DEFAULT_CORE_NAME));
    }
    
    @Override
    public void createAndStartCore(final String coreName, final String instanceDir, final String dataDir, final String schemaPath, final String configPath) throws OXException {
        final CoreDescriptor coreDescriptor = new CoreDescriptor(coreContainer, coreName, instanceDir);
        coreDescriptor.setDataDir(dataDir);
        coreDescriptor.setSchemaName(schemaPath);
        coreDescriptor.setConfigName(configPath);
        SolrCore solrCore;
        try {
            solrCore = coreContainer.create(coreDescriptor);
            coreContainer.register(coreName, solrCore, false);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    @Override
    public void shutdownCore(final String coreName) {
        final SolrCore solrCore = coreContainer.remove(coreName);
        if (solrCore != null) {
            solrCore.close();
        }
    }
    
    public void shutdown() {
        coreContainer.shutdown();        
        solrServer = null;
    }
    
    @Override
    public void reloadCore(final String coreName) throws OXException {
        try {
            coreContainer.reload(coreName);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    

}
