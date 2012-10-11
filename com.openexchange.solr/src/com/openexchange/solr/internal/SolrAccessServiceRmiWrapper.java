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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.solr.rmi.RMISolrAccessService;

/**
 * {@link SolrAccessServiceRmiWrapper}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrAccessServiceRmiWrapper implements SolrAccessService {

    private final RMISolrAccessService rmiAccessService;

    /**
     * Initializes a new {@link SolrAccessServiceRmiWrapper}.
     */
    public SolrAccessServiceRmiWrapper(final RMISolrAccessService rmiAccessService) {
        super();
        this.rmiAccessService = rmiAccessService;
    }

    @Override
    public UpdateResponse add(final SolrCoreIdentifier identifier, final SolrInputDocument document, final boolean commit) throws OXException {
        try {
            return rmiAccessService.addRmi(identifier, document, commit);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse add(final SolrCoreIdentifier identifier, final Collection<SolrInputDocument> documents, final boolean commit) throws OXException {
        try {
            return rmiAccessService.addRmi(identifier, documents, commit);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse deleteById(final SolrCoreIdentifier identifier, final String id, final boolean commit) throws OXException {
        try {
            return rmiAccessService.deleteByIdRmi(identifier, id, commit);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse deleteByQuery(final SolrCoreIdentifier identifier, final String query, final boolean commit) throws OXException {
        try {
            return rmiAccessService.deleteByQueryRmi(identifier, query, commit);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse commit(final SolrCoreIdentifier identifier) throws OXException {
        try {
            return rmiAccessService.commitRmi(identifier);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse commit(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher) throws OXException {
        try {
            return rmiAccessService.commitRmi(identifier, waitFlush, waitSearcher);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse rollback(final SolrCoreIdentifier identifier) throws OXException {
        try {
            return rmiAccessService.rollbackRmi(identifier);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse optimize(final SolrCoreIdentifier identifier) throws OXException {
        try {
            return rmiAccessService.optimizeRmi(identifier);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse optimize(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher) throws OXException {
        try {
            return rmiAccessService.optimizeRmi(identifier, waitFlush, waitSearcher);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public UpdateResponse optimize(final SolrCoreIdentifier identifier, final boolean waitFlush, final boolean waitSearcher, final int maxSegments) throws OXException {
        try {
            return rmiAccessService.optimizeRmi(identifier, waitFlush, waitSearcher, maxSegments);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }

    @Override
    public QueryResponse query(final SolrCoreIdentifier identifier, final SolrParams params) throws OXException {
        try {
            return rmiAccessService.queryRmi(identifier, params);
        } catch (final RemoteException e) {
            throw handleRemoteException(e);
        }
    }
    
	@Override
	public void freeResources(SolrCoreIdentifier identifier) {
		return;
	}

    private static OXException handleRemoteException(final RemoteException remoteException) {
        return SolrExceptionCodes.REMOTE_ERROR.create(remoteException, remoteException.getMessage());
    }

}
