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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.TriggerType;
import com.openexchange.index.solr.IndexUrl;

/**
 * {@link SolrIndexAccess} - The Solr {@link IndexAccess} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SolrIndexAccess implements IndexAccess {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrIndexAccess.class));
	
    private final int contextId;
    
    private final int userId;
    
    private final int module;
    
    private final SolrIndexIdentifier identifier;
    
    private final SolrCoreManager solrManager;
    
    private final AtomicInteger retainCount;
	
	
	public SolrIndexAccess(final SolrIndexIdentifier identifier, final ConfigurationService config) {
        super();
        this.identifier = identifier;
        this.contextId = identifier.getContextId();
        this.userId = identifier.getUserId();
        this.module = identifier.getModule();
        solrManager = new SolrCoreManager(contextId, userId, module, config);
        retainCount = new AtomicInteger(0);
	}

	@Override
	public void addEnvelopeData(final IndexDocument document) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addEnvelopeData(final Collection<IndexDocument> documents)
			throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addContent(final IndexDocument document) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addContent(final Collection<IndexDocument> documents)
			throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAttachments(final IndexDocument document) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAttachments(final Collection<IndexDocument> documents)
			throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(final String id) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteByQuery(final String query) throws OXException {
		// TODO Auto-generated method stub

	}

	@Override
	public IndexResult query(final QueryParameters parameters) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TriggerType getTriggerType() {
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public void release() {
        try {
            solrManager.releaseIndexUrl();
        } catch (final OXException e) {
            LOG.warn(e.getLogMessage(), e);
        }        
    }
    
    public SolrIndexIdentifier getIdentifier() {
        return identifier;
    }
    
    public int incrementRetainCount() {
        return retainCount.incrementAndGet();
    }
    
    public int decrementRetainCount() {
        return retainCount.decrementAndGet();
    }
    
    private IndexUrl getIndexUrl() throws OXException {
        return solrManager.getIndexUrl();
    }

}
