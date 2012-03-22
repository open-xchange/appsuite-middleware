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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
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
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrManagementServiceImpl.class));

    private CoreContainer coreContainer;
    

    public SolrManagementServiceImpl() {
        super();
    }

    public void startUp() throws OXException {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String solrHome = config.getProperty(SolrProperties.PROP_SOLR_HOME);
        final String solrXmlPath = config.getProperty(SolrProperties.PROP_SOLR_XML);
        final File solrXml = new File(solrXmlPath);
        coreContainer = new CoreContainer();
        try {
            coreContainer.load(solrHome, solrXml);
        } catch (final ParserConfigurationException e) {
            throw new OXException(e);
        } catch (final IOException e) {
            throw new OXException(e);
        } catch (final SAXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void createAndStartCore(final String coreName, final String instanceDir, final String dataDir, final String schemaPath, final String configPath) throws OXException {
        if (coreContainer == null) {
            // TODO: throw exception
        }
        
        final CoreDescriptor coreDescriptor = new CoreDescriptor(coreContainer, coreName, instanceDir);
        coreDescriptor.setDataDir(dataDir);
        coreDescriptor.setSchemaName(schemaPath);
        coreDescriptor.setConfigName(configPath);
        SolrCore solrCore;
        try {
            solrCore = coreContainer.create(coreDescriptor);
            coreContainer.register(coreName, solrCore, false);
        } catch (final ParserConfigurationException e) {
            throw new OXException(e);
        } catch (final IOException e) {
            throw new OXException(e);
        } catch (final SAXException e) {
            throw new OXException(e);
        }

    }

    @Override
    public void shutdownCore(final String coreName) {
        if (coreContainer != null) {
            // TODO: throw exception
        }
        
        final SolrCore solrCore = coreContainer.remove(coreName);
        if (solrCore != null) {
            solrCore.close();
        }
    }

    public void shutdown() {
        if (coreContainer != null) {
            coreContainer.shutdown();
        }
    }

    @Override
    public void reloadCore(final String coreName) throws OXException {
        if (coreContainer != null) {
            // TODO: throw exception
        }
        
        try {
            coreContainer.reload(coreName);
        } catch (final ParserConfigurationException e) {
            throw new OXException(e);
        } catch (final IOException e) {
            throw new OXException(e);
        } catch (final SAXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public SolrResponse request(final SolrRequest request, final String coreName, final boolean commit) throws OXException {
        if (coreContainer != null) {
            // TODO: throw exception
        }
        
        final EmbeddedSolrServer solrServer = new EmbeddedSolrServer(coreContainer, coreName);
        try {            
            final SolrResponse response = request.process(solrServer);            
            if (commit) {
                final UpdateRequest commitRequest = new UpdateRequest();
                commitRequest.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
                commitRequest.process(solrServer);
            }
            
            return response;
        } catch (SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        }
    }
    
    private void rollback(final SolrServer solrServer) {
        try {
            solrServer.rollback();
        } catch (final Throwable t) {
            LOG.warn("Rollback of Solr server failed.", t);
            handleThrowable(t);
        }
    }
    
    private static final String MARKER = " ---=== /!\\ ===--- ";

    /**
     * Checks whether the supplied <tt>Throwable</tt> is one that needs to be rethrown and swallows all others.
     *
     * @param t The <tt>Throwable</tt> to check
     */
    private void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            LOG.fatal(MARKER + "Thread death" + MARKER, t);
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            LOG.fatal(
                MARKER + "The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating." + MARKER,
                t);
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

}
