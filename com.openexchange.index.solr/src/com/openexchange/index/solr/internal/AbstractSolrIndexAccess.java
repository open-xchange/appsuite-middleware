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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreIdentifier;

/**
 * {@link AbstractSolrIndexAccess}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractSolrIndexAccess<V> implements IndexAccess<V> {

    protected final int contextId;

    protected final int userId;

    protected final int module;
    
    private final SolrCoreIdentifier identifier;

    private final AtomicInteger retainCount;

    private long lastAccess;
        

    /**
     * Initializes a new {@link AbstractSolrIndexAccess}.
     * 
     * @param identifier The Solr index identifier
     */
    protected AbstractSolrIndexAccess(final SolrCoreIdentifier identifier) {
        super();
        this.identifier = identifier;
        this.contextId = identifier.getContextId();
        this.userId = identifier.getUserId();
        this.module = identifier.getModule();
        lastAccess = System.currentTimeMillis();
        retainCount = new AtomicInteger(0);
    }
    
    /*
     * Public methods
     */
    public void releaseCore() {        
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);
        accessService.freeResources(identifier);
    }

    public SolrCoreIdentifier getIdentifier() {
        return identifier;
    }

    public int incrementRetainCount() {
        return retainCount.incrementAndGet();
    }

    public int decrementRetainCount() {
        return retainCount.decrementAndGet();
    }
    
    public boolean isRetained() {
    	return retainCount.get() != 0;
    }

    public long getLastAccess() {
        return lastAccess;
    }
    
    /*
     * Protected methods
     */
    protected UpdateResponse addDocument(final SolrInputDocument document) throws OXException {        
        return addDocument(document, true);
    }
    
    protected UpdateResponse addDocuments(final Collection<SolrInputDocument> documents) throws OXException {
        return addDocuments(documents, true);
    }
    
    protected UpdateResponse addDocument(final SolrInputDocument document, final boolean commit) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.add(identifier, document, commit);        
        return response;
    }
    
    protected UpdateResponse addDocuments(final Collection<SolrInputDocument> documents, final boolean commit) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);
        final UpdateResponse response = accessService.add(identifier, documents, commit);
        return response;
    }
    
    protected UpdateResponse commit() throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.commit(identifier);        
        return response;
    }
    
    protected UpdateResponse optimize() throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.optimize(identifier);        
        return response;
    }
    
    protected SolrResponse deleteDocumentById(final String id) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.deleteById(identifier, id, true);
        return response;
    }
    
    protected SolrResponse deleteDocumentsByQuery(final String query) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.deleteByQuery(identifier, query, true);        
        return response;
    }
    
    protected QueryResponse query(final SolrParams query) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final QueryResponse response = accessService.query(identifier, query);
        return response;
    }

}
