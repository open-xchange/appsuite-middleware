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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;
import org.xml.sax.SAXException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.log.LogFactory;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreConfigService;
import com.openexchange.solr.SolrCoreConfiguration;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrCoreStore;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.solr.SolrProperties;

/**
 * {@link EmbeddedSolrAccessImpl}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class EmbeddedSolrAccessImpl implements SolrAccessService {

    private static final String ISE_MSG = "The core container was null. The embedded solr server was not started up properly.";

    private static final String IAE_MSG = "Parameter `%s` must not be null.";

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(EmbeddedSolrAccessImpl.class));

    private final Lock mutex = new ReentrantLock();

    private CoreContainer coreContainer;

    private String serverAddress = null;

    private final SolrIndexMysql indexMysql;

    public EmbeddedSolrAccessImpl() {
        super();
        indexMysql = SolrIndexMysql.getInstance();
    }

    public void startUp() throws OXException {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String solrHome = config.getProperty(SolrProperties.SOLR_HOME);
        coreContainer = new CoreContainer(solrHome);
    }
    
    public void startCore(SolrCoreIdentifier identifier) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }
        if (coreContainer == null) {
            throw new IllegalStateException(ISE_MSG);
        }
        
        try {
            int module = identifier.getModule();
            com.openexchange.solr.SolrCore solrCore = getCoreOrCreateEnvironment(identifier);
            ConfigurationService config = Services.getService(ConfigurationService.class);
            String libDir = config.getProperty(SolrProperties.LIB_DIR);
            String schemaFile = getSchemaFileByModule(module);
            String configFile = getConfigFileByModule(module);
            String configDir = config.getProperty(SolrProperties.CONFIG_DIR);
            SolrCoreStore coreStore = indexMysql.getCoreStore(solrCore.getStore());
            SolrCoreConfiguration configuration = new SolrCoreConfiguration(coreStore.getUri(), identifier);
            String coreName = configuration.getIdentifier().toString();
            String dataDir = configuration.getDataDirPath();
            Properties properties = new Properties();
            properties.put("data.dir", dataDir);
            properties.put("logDir", "/var/log/open-xchange");
            properties.put("confDir", configDir);
            properties.put("libDir", libDir);
            CoreDescriptor coreDescriptor = new CoreDescriptor(coreContainer, coreName, configuration.getCoreDirPath());
            coreDescriptor.setDataDir(dataDir);
            coreDescriptor.setSchemaName(schemaFile);
            coreDescriptor.setConfigName(configFile);
            coreDescriptor.setCoreProperties(properties);
            SolrCore embeddedSolrCore = coreContainer.create(coreDescriptor);
            coreContainer.register(configuration.getIdentifier().toString(), embeddedSolrCore, false);
        } catch (final ParserConfigurationException e) {
            throw new OXException(e);
        } catch (final IOException e) {
            throw new OXException(e);
        } catch (final SAXException e) {
            throw new OXException(e);
        }
    }
    
    public void stopCore(SolrCoreIdentifier identifier) {
        SolrCore solrCore = coreContainer.remove(identifier.toString());
        if (solrCore != null) {
            solrCore.close();
        }
    }

    public void shutDown() throws OXException {
        if (coreContainer != null) {
            coreContainer.shutdown();
        }
    }

    public boolean hasActiveCore(final SolrCoreIdentifier identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }
        if (coreContainer == null) {
            throw new IllegalStateException(ISE_MSG);
        }

        return coreContainer.getCoreNames().contains(identifier.toString());
    }

    public Collection<String> getActiveCores() {
        return coreContainer.getCoreNames();
    }

    @Override
    public UpdateResponse add(final SolrCoreIdentifier identifier, final SolrInputDocument document, final boolean commit) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }
        if (document == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "document"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.add(document);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        } finally {
            commit(solrServer, commit);
        }
    }

    @Override
    public UpdateResponse add(final SolrCoreIdentifier identifier, final Collection<SolrInputDocument> documents, final boolean commit) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }
        if (documents == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "documents"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.add(documents);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        } finally {
            commit(solrServer, commit);
        }
    }

    @Override
    public UpdateResponse deleteById(final SolrCoreIdentifier identifier, final String id, final boolean commit) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }
        if (id == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "id"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.deleteById(id);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        } finally {
            commit(solrServer, commit);
        }
    }

    @Override
    public UpdateResponse deleteByQuery(final SolrCoreIdentifier identifier, final String query, final boolean commit) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }
        if (query == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "query"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.deleteByQuery(query);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        } finally {
            commit(solrServer, commit);
        }
    }

    @Override
    public UpdateResponse commit(final SolrCoreIdentifier identifier) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.commit();
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        }
    }

    @Override
    public UpdateResponse commit(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.commit(waitFlush, waitSearcher);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        }
    }

    @Override
    public UpdateResponse rollback(final SolrCoreIdentifier identifier) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.rollback();
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        }
    }

    @Override
    public UpdateResponse optimize(final SolrCoreIdentifier identifier) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.optimize();
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        }
    }

    @Override
    public UpdateResponse optimize(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.optimize(waitFlush, waitSearcher);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        }
    }

    @Override
    public UpdateResponse optimize(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher, final int maxSegments) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.optimize(waitFlush, waitSearcher, maxSegments);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        } catch (final IOException e) {
            rollback(solrServer);
            throw new OXException(e);
        }
    }

    @Override
    public QueryResponse query(final SolrCoreIdentifier identifier, final SolrParams params) throws OXException {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }
        if (params == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "params"));
        }

        final SolrServer solrServer = getSolrServer(identifier);
        try {
            return solrServer.query(params);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw new OXException(e);
        }
    }

    @Override
    public void freeResources(final SolrCoreIdentifier identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException(String.format(IAE_MSG, "identifier"));
        }

        stopCore(identifier);  
    }

    private String getConfigFileByModule(final int module) throws OXException {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String configFile;
        switch (module) {
        case Types.EMAIL:
            configFile = config.getProperty(SolrProperties.CONFIG_FILE_MAIL);
            break;
            
        case Types.INFOSTORE:
            configFile = config.getProperty(SolrProperties.CONFIG_FILE_INFOSTORE);
            break;
            
        case Types.ATTACHMENT:
            configFile = config.getProperty(SolrProperties.CONFIG_FILE_ATTACHMENTS);
            break;

        default:
            throw SolrExceptionCodes.UNKNOWN_MODULE.create(module);
        }

        return configFile;
    }

    private String getSchemaFileByModule(final int module) throws OXException {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String schemaFile;
        switch (module) {
        case Types.EMAIL:
            schemaFile = config.getProperty(SolrProperties.SCHEMA_FILE_MAIL);
            break;
            
        case Types.INFOSTORE:
            schemaFile = config.getProperty(SolrProperties.SCHEMA_FILE_INFOSTORE);
            break;
            
        case Types.ATTACHMENT:
            schemaFile = config.getProperty(SolrProperties.SCHEMA_FILE_ATTACHMENTS);
            break;

        default:
            throw SolrExceptionCodes.UNKNOWN_MODULE.create(module);
        }

        return schemaFile;
    }

    private SolrServer getSolrServer(final SolrCoreIdentifier identifier) throws OXException {
        if (coreContainer == null) {
            throw new IllegalStateException(ISE_MSG);
        }

        final EmbeddedSolrServer solrServer = new EmbeddedSolrServer(coreContainer, identifier.toString());
        return solrServer;
    }

    private String getLocalServerAddress() throws OXException {
        if (serverAddress != null) {
            return serverAddress;
        }

        try {
            final InetAddress addr = InetAddress.getLocalHost();
            return serverAddress = addr.getHostAddress();
        } catch (final UnknownHostException e) {
            throw new OXException(e);
        }
    }

    private void commit(final SolrServer solrServer, final boolean commit) throws OXException {
        if (commit) {
            try {
                solrServer.commit();
            } catch (final SolrServerException e) {
                throw new OXException(e);
            } catch (final IOException e) {
                throw new OXException(e);
            }
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

    private com.openexchange.solr.SolrCore getCoreOrCreateEnvironment(final SolrCoreIdentifier identifier) throws OXException {
        try {
            return indexMysql.getSolrCore(identifier.getContextId(), identifier.getUserId(), identifier.getModule());
        } catch (final OXException e) {
            if (e.similarTo(SolrExceptionCodes.CORE_ENTRY_NOT_FOUND)) {
                final SolrCoreConfigService coreService = Services.getService(SolrCoreConfigService.class);
                coreService.createCoreEnvironment(identifier);
                return indexMysql.getSolrCore(identifier.getContextId(), identifier.getUserId(), identifier.getModule());
            } else {
                throw e;
            }
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
