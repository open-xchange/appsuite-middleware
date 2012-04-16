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

package com.openexchange.indexedSearch.json.mail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.solr.mail.SolrMailConstants;
import com.openexchange.index.solr.mail.SolrMailField;
import com.openexchange.indexedSearch.json.FieldResults;
import com.openexchange.indexedSearch.json.SearchHandler;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BooleanTerm;
import com.openexchange.mail.search.NOTTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.service.MailAttributeFetcher;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.Operation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailSearchHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailSearchHandler implements SearchHandler, SolrMailConstants {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailSearchHandler}.
     */
    public MailSearchHandler(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<FieldResults> search(final JSONObject jsonQuery, final int[] range, final int[] fields, final AJAXRequestData requestData, final ServerSession session) throws OXException {
        final IndexFacadeService indexFacade = services.getService(IndexFacadeService.class);
        if (null == indexFacade) {
            return Collections.emptyList();
        }
        IndexAccess<MailMessage> indexAccess = null;
        try {
            indexAccess = indexFacade.acquireIndexAccess(com.openexchange.groupware.Types.EMAIL, session);
            // Parse query
            final MailQuery query = MailQuery.queryFor(jsonQuery);
            final MailField[] mailFields = MailField.getFields(fields);
            final boolean[] more = new boolean[1];
            final List<String> names = query.getNames();
            final List<FieldResults> retval = new LinkedList<FieldResults>();
            int i = 0;
            for (final com.openexchange.search.SearchTerm<?> searchTerm : query.getTerms()) {
                try {
                    more[0] = false;
                    final String name = null == names ? null : names.get(i++);
                    final String queryString;
                    {
                        final String userField = SolrMailField.USER.solrName();
                        final String contextField = SolrMailField.CONTEXT.solrName();
                        final String accountField = SolrMailField.ACCOUNT.solrName();
                        final String fullNameField = SolrMailField.FULL_NAME.solrName();
                        
                        final StringBuilder queryBuilder = new StringBuilder(128);
                        boolean first = true;
                        if (userField != null) {
                            queryBuilder.append('(').append(userField).append(':').append(session.getUserId()).append(')');
                            first = false;
                        }
                        
                        if (contextField != null) {
                            if (first) {
                                first = false;
                            } else {
                                queryBuilder.append(" AND ");
                            }
                            queryBuilder.append('(').append(contextField).append(':').append(session.getContextId()).append(')');
                        }
                        
                        
                        final int accountId = query.getAccountId();
                        if (accountId >= 0 && accountField != null) {
                            if (first) {
                                first = false;
                            } else {
                                queryBuilder.append(" AND ");
                            }
                            queryBuilder.append('(').append(accountField).append(':').append(accountId).append(')');
                        }
                        
                        final String fullName = query.getFullName();
                        if (fullName != null && fullNameField != null) {
                            if (first) {
                                first = false;
                            } else {
                                queryBuilder.append(" AND ");
                            }
                            queryBuilder.append('(').append(fullNameField).append(":\"").append(fullName).append("\")");
                        }
                        if (null != searchTerm) {
                            queryBuilder.append(" AND (").append(SearchTerm2Query.searchTerm2Query(map(searchTerm))).append(')');
                        }
                        queryString = queryBuilder.toString();
                    }
                    final Map<String, Object> params = new HashMap<String, Object>(4);
                    // TODO: params.put("fields", mailFields);
                    params.put("sort", MailIndexField.RECEIVED_DATE);
                    params.put("order", "desc");
                    final QueryParameters queryParameter =
                        new QueryParameters.Builder(queryString).setOffset(range[0]).setLength(range[1] - range[0]).setType(
                            IndexDocument.Type.MAIL).setParameters(params).build();
                    final IndexResult<MailMessage> indexResult = indexAccess.query(queryParameter);
                    if (range[1] > 0 && range[1] < indexResult.getNumFound()) {
                        more[0] = true;
                    }
                    final List<IndexDocument<MailMessage>> results = indexResult.getResults();
                    final List<MailMessage> mails = new ArrayList<MailMessage>(results.size());
                    for (final IndexDocument<MailMessage> indexDocument : results) {
                        mails.add(indexDocument.getObject());
                    }
                    retval.add(new FieldResults(name, "mail", results, more[0]));
                } catch (final InterruptedException e) {
                    // Thread interrupted
                    Thread.currentThread().interrupt();
                    return Collections.emptyList();
                }
            }
            // Prepare AJAX request data for mail ResultConverter
            requestData.setAction(AJAXServlet.ACTION_ALL);
            requestData.putParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
            requestData.putParameter(AJAXServlet.PARAMETER_COLUMNS, toCSV(jsonQuery.getJSONArray(AJAXServlet.PARAMETER_COLUMNS)));
            return retval;
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            if (null != indexAccess) {
                indexFacade.releaseIndexAccess(indexAccess);
            }
        }
    }

    /**
     * Generates an appropriate mail search term from specified search term.
     * 
     * @param searchTerm The search term
     * @return An appropriate mail search term
     * @throws IllegalArgumentException If an appropriate mail search term cannot be generated
     */
    private static SearchTerm<?> map(final com.openexchange.search.SearchTerm<?> searchTerm) {
        final Operation operation = searchTerm.getOperation();
        if (CompositeOperation.AND.equals(operation)) {
            final com.openexchange.search.SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            final int length = searchTerms.length;
            if (length == 0) {
                return BooleanTerm.TRUE;
            }
            ANDTerm andTerm;
            if (1 == length) {
                andTerm = new ANDTerm(map(searchTerms[0]), BooleanTerm.TRUE); // Neutral element
            } else {
                andTerm = new ANDTerm(map(searchTerms[0]), map(searchTerms[1]));
                for (int i = 2; i < length; i++) {
                    andTerm = new ANDTerm(andTerm, map(searchTerms[i]));
                }
            }
            return andTerm;
        }
        if (CompositeOperation.OR.equals(operation)) {
            final com.openexchange.search.SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            final int length = searchTerms.length;
            if (length == 0) {
                return BooleanTerm.TRUE;
            }
            ORTerm orTerm;
            if (1 == length) {
                orTerm = new ORTerm(map(searchTerms[0]), BooleanTerm.FALSE); // Neutral element
            } else {
                orTerm = new ORTerm(map(searchTerms[0]), map(searchTerms[1]));
                for (int i = 2; i < length; i++) {
                    orTerm = new ORTerm(orTerm, map(searchTerms[i]));
                }
            }
            return orTerm;
        }
        if (CompositeOperation.NOT.equals(operation)) {
            final com.openexchange.search.SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            final int length = searchTerms.length;
            if (length == 0) {
                return BooleanTerm.TRUE;
            }
            return new NOTTerm(map(searchTerms[0]));
        }
        final Object[] values = getNameAndConstant((Operand[]) searchTerm.getOperands());
        if (null == values) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Invalid values for single search term: {0}",
                Arrays.toString(searchTerm.getOperands())));
        }
        final SearchTerm<?> term =
            MailAttributeFetcher.getInstance().getSearchTerm(values[0].toString(), getSingleOperation(operation), values[1]);
        return null == term ? BooleanTerm.TRUE : term;
    }

    private static SingleOperation getSingleOperation(final Operation operation) {
        if (SingleOperation.EQUALS.equals(operation)) {
            return SingleOperation.EQUALS;
        }
        if (SingleOperation.GREATER_THAN.equals(operation)) {
            return SingleOperation.GREATER_THAN;
        }
        if (SingleOperation.LESS_THAN.equals(operation)) {
            return SingleOperation.LESS_THAN;
        }
        throw new IllegalArgumentException(MessageFormat.format("Unknown single search term operation: {0}", operation));
    }

    private static Object[] getNameAndConstant(@SuppressWarnings("unchecked") final Operand[] operands) {
        if (Operand.Type.CONSTANT.equals((operands[0]).getType())) {
            return new Object[] { operands[1].getValue().toString(), operands[0].getValue() };
        } else if (operands.length > 1 && Operand.Type.CONSTANT.equals((operands[1]).getType())) {
            return new Object[] { operands[0].getValue().toString(), operands[1].getValue() };
        }
        return null;
    }

    private static String toCSV(final JSONArray jArray) throws JSONException {
        if (null == jArray) {
            return "";
        }
        final int len = jArray.length();
        if (0 == len) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(len << 2);
        sb.append(jArray.get(0));
        for (int i = 1; i < len; i++) {
            sb.append(',').append(jArray.get(i));
        }
        return sb.toString();
    }

}
