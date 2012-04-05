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

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.IndexSchema;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
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
		final String solrHome = config.getProperty(SolrProperties.PROP_SOLR_HOME);

		/*
		 * Set some properties without the server would not start.
		 */
		System.setProperty("solr.core.name", "");
		System.setProperty("solr.core.dataDir", "");
		System.setProperty("solr.core.instanceDir", "");
		System.setProperty("logDir", "");
		coreContainer = new CoreContainer(solrHome);
	}

	public boolean startCore(final SolrCoreIdentifier identifier) throws OXException {
		if (coreContainer == null) {
			// TODO: throw exception
		}

		final int contextId = identifier.getContextId();
		final int userId = identifier.getUserId();
		final int module = identifier.getModule();
		mutex.lock();
		try {
			final com.openexchange.solr.SolrCore solrCore = getCoreOrCreateEnvironment(contextId, userId, module);
			if (solrCore.isActive()) {
				if (solrCore.getServer().equals(getLocalServerAddress())) {
					indexMysql.deactivateCoreEntry(contextId, userId, module);
				} else {
					return false;
				}
			}

			final SolrCoreStore coreStore = indexMysql.getCoreStore(solrCore.getStore());
			final SolrCoreConfiguration configuration = new SolrCoreConfiguration(coreStore.getUri(), identifier);
			final String coreName = configuration.getIdentifier().toString();
			final String dataDir = configuration.getDataDirPath();
			final CoreDescriptor coreDescriptor = new CoreDescriptor(coreContainer, coreName, configuration.getCoreDirPath());
			coreDescriptor.setDataDir(dataDir);

			final ConfigurationService config = Services.getService(ConfigurationService.class);
			final String schemaPath = config.getProperty(SolrProperties.PROP_SCHEMA_MAIL);
			final String configPath = config.getProperty(SolrProperties.PROP_CONFIG_MAIL);
			final SolrConfig solrConfig = new SolrConfig(null, new InputSource(new FileReader(configPath)));
			final IndexSchema schema = new IndexSchema(solrConfig, null, new InputSource(new FileReader(schemaPath)));
			final SolrCore embeddedSolrCore = new SolrCore(coreName, dataDir, solrConfig, schema, coreDescriptor);
			if (indexMysql.activateCoreEntry(contextId, userId, module, getLocalServerAddress())) {
				coreContainer.register(configuration.getIdentifier().toString(), embeddedSolrCore, false);
				return true;
			} else {
				embeddedSolrCore.close();
				return false;
			}
		} catch (final ParserConfigurationException e) {
			throw new OXException(e);
		} catch (final IOException e) {
			throw new OXException(e);
		} catch (final SAXException e) {
			throw new OXException(e);
		} finally {
			mutex.unlock();
		}
	}

	public boolean stopCore(final SolrCoreIdentifier identifier) throws OXException {
		if (coreContainer != null) {
			// TODO: throw exception
		}

		try {
			final int contextId = identifier.getContextId();
			final int userId = identifier.getUserId();
			final int module = identifier.getModule();
			indexMysql.deactivateCoreEntry(contextId, userId, module);
			mutex.lock();
			final SolrCore solrCore = coreContainer.remove(identifier.toString());
			if (solrCore != null) {
				// FIXME : remove optimize and implement a suitable
				// optimize-strategy
				optimize(identifier);
				solrCore.close();
				return true;
			}
		} finally {
			mutex.unlock();
		}

		return false;
	}

	public void shutDown() throws OXException {
		final String server = getLocalServerAddress();
		final Set<Integer> contextIds = new HashSet<Integer>();
		final Collection<String> activeCores = getActiveCores();
		for (final String core : activeCores) {
			try {
				final SolrCoreIdentifier identifier = new SolrCoreIdentifier(core);
				contextIds.add(identifier.getContextId());
			} catch (final OXException e) {
				// Parsing error. Ignore this core.
			}
		}

		for (final Integer contextId : contextIds) {
			indexMysql.deactivateCoresForServer(server, contextId);
		}

		if (coreContainer != null) {
			coreContainer.shutdown();
		}
	}

	public boolean hasActiveCore(final SolrCoreIdentifier identifier) throws OXException {
		if (coreContainer != null) {
			// TODO: throw exception
		}

		return coreContainer.getCoreNames().contains(identifier.toString());
	}

	public Collection<String> getActiveCores() {
		return coreContainer.getCoreNames();
	}

	@Override
	public UpdateResponse add(SolrCoreIdentifier identifier, SolrInputDocument document, boolean commit) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.add(document);
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		} finally {
			commit(solrServer, commit);
		}
	}

	@Override
	public UpdateResponse add(SolrCoreIdentifier identifier, Collection<SolrInputDocument> documents, boolean commit) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.add(documents);
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		} finally {
			commit(solrServer, commit);
		}
	}

	@Override
	public UpdateResponse deleteById(SolrCoreIdentifier identifier, String id, boolean commit) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.deleteById(id);
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		} finally {
			commit(solrServer, commit);
		}
	}

	@Override
	public UpdateResponse deleteByQuery(SolrCoreIdentifier identifier, String query, boolean commit) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.deleteByQuery(query);
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		} finally {
			commit(solrServer, commit);
		}
	}

	@Override
	public UpdateResponse commit(SolrCoreIdentifier identifier) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.commit();
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		}
	}

	@Override
	public UpdateResponse commit(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.commit(waitFlush, waitSearcher);
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		}
	}

	@Override
	public UpdateResponse rollback(SolrCoreIdentifier identifier) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.rollback();
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		}
	}

	@Override
	public UpdateResponse optimize(SolrCoreIdentifier identifier) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.optimize();
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		}
	}

	@Override
	public UpdateResponse optimize(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.optimize(waitFlush, waitSearcher);
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		}
	}

	@Override
	public UpdateResponse optimize(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher, int maxSegments) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.optimize(waitFlush, waitSearcher, maxSegments);
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		} catch (IOException e) {
			rollback(solrServer);
			throw new OXException(e);
		}
	}

	@Override
	public QueryResponse query(SolrCoreIdentifier identifier, SolrParams params) throws OXException {
		final SolrServer solrServer = getSolrServer(identifier);
		try {
			return solrServer.query(params);
		} catch (SolrServerException e) {
			rollback(solrServer);
			throw new OXException(e);
		}
	}

	@Override
	public void freeResources(SolrCoreIdentifier identifier) {
		try {
			stopCore(identifier);
		} catch (final OXException e) {
			LOG.error("Could not stop core " + identifier.toString(), e);
		}
	}

	private SolrServer getSolrServer(final SolrCoreIdentifier identifier) throws OXException {
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

	private void commit(SolrServer solrServer, boolean commit) throws OXException {
		if (commit) {
			try {
				solrServer.commit();
			} catch (SolrServerException e) {
				throw new OXException(e);
			} catch (IOException e) {
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

	private com.openexchange.solr.SolrCore getCoreOrCreateEnvironment(final int contextId, final int userId, final int module) throws OXException {
		try {
			return indexMysql.getSolrCore(contextId, userId, module);
		} catch (final OXException e) {
			if (e.similarTo(SolrExceptionCodes.CORE_ENTRY_NOT_FOUND)) {
				final SolrCoreConfigService coreService = Services.getService(SolrCoreConfigService.class);
				coreService.createCoreEnvironment(contextId, userId, module);
				return indexMysql.getSolrCore(contextId, userId, module);
			} else {
				throw e;
			}
		}
	}

	private static final String MARKER = " ---=== /!\\ ===--- ";

	/**
	 * Checks whether the supplied <tt>Throwable</tt> is one that needs to be
	 * rethrown and swallows all others.
	 * 
	 * @param t
	 *            The <tt>Throwable</tt> to check
	 */
	private void handleThrowable(final Throwable t) {
		if (t instanceof ThreadDeath) {
			LOG.fatal(MARKER + "Thread death" + MARKER, t);
			throw (ThreadDeath) t;
		}
		if (t instanceof VirtualMachineError) {
			LOG.fatal(MARKER + "The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating." + MARKER, t);
			throw (VirtualMachineError) t;
		}
		// All other instances of Throwable will be silently swallowed
	}

}
