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

package com.openexchange.mail.smal.impl.index;

import static com.openexchange.index.solr.mail.SolrMailUtility.releaseAccess;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.solr.mail.SolrMailConstants;
import com.openexchange.index.solr.mail.SolrMailUtility;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link IndexAccessAdapter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexAccessAdapter implements SolrMailConstants {

    /**
     * Initializes a new {@link IndexAccessAdapter}.
     */
    public IndexAccessAdapter() {
        super();
    }

    /**
     * Performs the query derived from given search term.
     * 
     * @param optAccountId The optional account identifier or <code>-1</code> to not restrict to a certain account
     * @param optFullName The optional full name to restrict search results to specified folder
     * @param searchTerm The search term
     * @param sortField The sort field
     * @param order The order direction
     * @param fields The fields to pre-fill in returned {@link MailMessage} instances; if <code>null</code> all available fields are filled
     * @param indexRange The index range
     * @param session The session
     * @return The search result
     * @throws OXException If search fails
     * @throws InterruptedException If processing is interrupted
     */
    public List<MailMessage> search(final int optAccountId, final String optFullName, final SearchTerm<?> searchTerm, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final IndexRange indexRange, final Session session) throws OXException, InterruptedException {
        final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        if (null == facade) {
            // Index service missing
            return Collections.emptyList();
        }
        IndexAccess<MailMessage> indexAccess = null;
        try {
            indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
            final MailFields mailFields = new MailFields(fields);
            if (null != sortField) {
                final MailField sf = MailField.getField(sortField.getField());
                if (null != sf) {
                    mailFields.add(sf);
                }
            }
            final QueryParameters.Builder builder;
            {
                final StringBuilder queryBuilder = new StringBuilder(128);
                queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
                queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
                if (optAccountId >= 0) {
                    queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(optAccountId).append(')');
                }
                if (null != optFullName) {
                    queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(optFullName).append("\")");
                }
                if (null != searchTerm) {
                    queryBuilder.append(" AND (").append(SearchTerm2Query.searchTerm2Query(searchTerm)).append(')');
                }
                builder = new QueryParameters.Builder(queryBuilder.toString());
            }
            if (null != sortField) {
                final Map<String, Object> params = new HashMap<String, Object>(2);
                final MailField field = MailField.getField(sortField.getField());
                final List<String> list = SolrMailUtility.getField2NameMap().get(field);
                params.put("sort", list.get(0));
                params.put("order", OrderDirection.DESC.equals(order) ? "desc" : "asc");
                builder.setParameters(params);
            }
            final QueryParameters parameters = builder.setOffset(0).setLength(Integer.MAX_VALUE).setType(IndexDocument.Type.MAIL).build();
            final IndexResult<MailMessage> result = indexAccess.query(parameters);
            List<IndexDocument<MailMessage>> documents = result.getResults();
            if (indexRange != null) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if ((documents == null) || documents.isEmpty()) {
                    return Collections.emptyList();
                }
                if ((fromIndex) > documents.size()) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return Collections.emptyList();
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= documents.size()) {
                    toIndex = documents.size();
                }
                documents = documents.subList(fromIndex, toIndex);
            }
            return IndexDocumentHelper.messagesFrom(documents);
        } finally {
            releaseAccess(facade, indexAccess);
        }
    }

}
