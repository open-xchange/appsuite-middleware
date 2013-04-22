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

package com.openexchange.index.solr.internal.mail;

import static com.openexchange.index.solr.internal.LuceneQueryTools.buildQueryStringWithOr;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.FacetParameters;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.querybuilder.SolrQueryBuilder;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.solr.SolrCoreIdentifier;

/**
 * {@link SolrMailIndexAccess}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrMailIndexAccess extends AbstractSolrIndexAccess<MailMessage> {

    private final SolrQueryBuilder queryBuilder;

    private final SolrMailDocumentConverter converter;

    private final Locale userLocale;

    /**
     * Initializes a new {@link SolrMailIndexAccess}.
     */
    public SolrMailIndexAccess(SolrCoreIdentifier identifier, SolrQueryBuilder queryBuilder, FieldConfiguration fieldConfig, Locale userLocale) {
        super(identifier, fieldConfig);
        this.queryBuilder = queryBuilder;
        this.userLocale = userLocale;
        converter = new SolrMailDocumentConverter(fieldConfig);
    }

    @Override
    public boolean isIndexed(String accountId, String folderId) throws OXException {
        return isIndexed(Types.EMAIL, accountId, folderId);
    }

    @Override
    public Set<? extends IndexField> getIndexedFields() {
        return fieldConfig.getIndexedFields();
    }

    @Override
    public void addDocument0(IndexDocument<MailMessage> document) throws OXException {
        addSolrDocument(convertToDocument(document));
    }

    @Override
    public void addDocuments0(Collection<IndexDocument<MailMessage>> documents) throws OXException {
        if (documents.isEmpty()) {
            return;
        }

        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<MailMessage> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }

        addSolrDocuments(inputDocuments);
    }

    @Override
    public void deleteById0(String id) throws OXException {
        deleteDocumentById(id);
    }

    @Override
    public void deleteByQuery0(QueryParameters parameters) throws OXException {
        Set<MailIndexField> fields = EnumSet.noneOf(MailIndexField.class);
        fields.add(MailIndexField.ACCOUNT);
        fields.add(MailIndexField.FULL_NAME);
        fields.add(MailIndexField.ID);

        IndexResult<MailMessage> indexResult = query(parameters, fields);
        List<IndexDocument<MailMessage>> documents = indexResult.getResults();
        Set<String> uuids = new HashSet<String>(documents.size());
        for (IndexDocument<MailMessage> document : documents) {
            uuids.add(MailUUID.newUUID(contextId, userId, document.getObject()).toString());
        }

        String deleteQuery = buildQueryStringWithOr(fieldConfig.getUUIDField(), uuids);
        if (deleteQuery != null) {
            deleteDocumentsByQuery(deleteQuery);
        }
    }

    @Override
    public IndexResult<MailMessage> query0(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException {
        IndexField sortField = parameters.getSortField();
        Order order = parameters.getOrder();
        boolean sortManually = false;

        Set<IndexField> modifiedFields = new HashSet<IndexField>(fields);
        QueryParameters newParameters = parameters;
        if (sortField != null) {
            modifiedFields.add(sortField);
            if (sortField instanceof MailIndexField) {
                if (sortField.equals(MailIndexField.FROM) || sortField.equals(MailIndexField.TO)
                    || sortField.equals(MailIndexField.CC) || sortField.equals(MailIndexField.BCC)) {
                    sortManually = true;
                    newParameters = new QueryParameters.Builder()
                        .setSearchTerm(parameters.getSearchTerm())
                        .setHandler(parameters.getHandler())
                        .setAccountFolders(parameters.getAccountFolders())
                        .setIndexIds(parameters.getIndexIds())
                        .setModule(parameters.getModule())
                        .setOffset(parameters.getOff())
                        .setLength(parameters.getLen())
                        .build();
                }
            }
        }

        SolrQuery solrQuery = queryBuilder.buildQuery(newParameters);
        setFieldList(solrQuery, modifiedFields);
        IndexResult<MailMessage> indexResult = queryChunkWise(
            fieldConfig.getUUIDField(),
            converter,
            solrQuery,
            newParameters.getOff(),
            newParameters.getLen(),
            100);

        if (indexResult.equals(Indexes.emptyResult())) {
            return indexResult;
        }

        if (sortManually) {
            MailIndexField mailSortField = (MailIndexField) sortField;
            if (mailSortField.hasMailField() && mailSortField.getMailField().getListField() != null) {
                MailSortField compareField = MailSortField.getField(mailSortField.getMailField().getListField().getField());
                if (compareField != null) {
                    boolean orderDesc = order == null ? false : order == Order.DESC ? true : false;
                    MailDocumentComparator documentComparator = new MailDocumentComparator(new MailMessageComparator(
                        compareField,
                        orderDesc,
                        userLocale));
                    Collections.sort(indexResult.getResults(), documentComparator);
                }
            }
        }

        return indexResult;
    }

    @Override
    public IndexResult<MailMessage> query0(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException {
        // Nothing to do
        return null;
    }

    private SolrInputDocument convertToDocument(IndexDocument<MailMessage> document) throws OXException {
        return converter.convert(contextId, userId, document);
    }

}
