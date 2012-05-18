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

import java.rmi.RemoteException;
import java.util.Collection;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.exception.OXException;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.rmi.RMISolrAccessService;

/**
 * {@link RMISolrAccessImpl}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class RMISolrAccessImpl implements RMISolrAccessService {

    private final SolrAccessService solrService;
    

    public RMISolrAccessImpl(final SolrAccessService solrService) {
        super();
        this.solrService = solrService;
    }

    @Override
    public UpdateResponse addRmi(final SolrCoreIdentifier identifier, final Collection<SolrInputDocument> documents, final boolean commit) throws RemoteException, OXException {
        return solrService.add(identifier, documents, commit);
    }

    @Override
    public UpdateResponse addRmi(final SolrCoreIdentifier identifier, final SolrInputDocument document, final boolean commit) throws RemoteException, OXException {
        return solrService.add(identifier, document, commit);
    }

    @Override
    public UpdateResponse deleteByIdRmi(final SolrCoreIdentifier identifier, final String id, final boolean commit) throws RemoteException, OXException {
        return solrService.deleteById(identifier, id, commit);
    }

    @Override
    public UpdateResponse deleteByQueryRmi(final SolrCoreIdentifier identifier, final String query, final boolean commit) throws RemoteException, OXException {
        return solrService.deleteByQuery(identifier, query, commit);
    }

    @Override
    public UpdateResponse commitRmi(final SolrCoreIdentifier identifier) throws RemoteException, OXException {
        return solrService.commit(identifier);
    }

    @Override
    public UpdateResponse commitRmi(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher) throws RemoteException, OXException {
        return solrService.commit(identifier, waitFlush, waitSearcher);
    }

    @Override
    public UpdateResponse rollbackRmi(final SolrCoreIdentifier identifier) throws RemoteException, OXException {
        return solrService.rollback(identifier);
    }

    @Override
    public UpdateResponse optimizeRmi(final SolrCoreIdentifier identifier) throws RemoteException, OXException {
        return solrService.optimize(identifier);
    }

    @Override
    public UpdateResponse optimizeRmi(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher) throws RemoteException, OXException {
        return solrService.optimize(identifier, waitFlush, waitSearcher);
    }

    @Override
    public UpdateResponse optimizeRmi(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher, final int maxSegments) throws RemoteException, OXException {
        return solrService.optimize(identifier, waitFlush, waitSearcher, maxSegments);
    }

    @Override
    public QueryResponse queryRmi(final SolrCoreIdentifier identifier, final SolrParams params) throws RemoteException, OXException {
        return solrService.query(identifier, params);
    }

	@Override
	public void pingRmi() throws RemoteException {
		return;		
	}
}
