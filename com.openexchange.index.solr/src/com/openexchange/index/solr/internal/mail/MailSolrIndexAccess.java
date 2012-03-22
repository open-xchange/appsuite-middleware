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

package com.openexchange.index.solr.internal.mail;

import static com.openexchange.index.solr.internal.SolrUtils.detectLocale;
import static java.util.Collections.singletonList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.TriggerType;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.SolrIndexIdentifier;
import com.openexchange.index.solr.internal.mail.MailFillers.MailFiller;
import com.openexchange.index.solr.mail.SolrMailConstants;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.text.TextFinder;

/**
 * {@link MailSolrIndexAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailSolrIndexAccess extends AbstractSolrIndexAccess<MailMessage> implements SolrMailConstants {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MailSolrIndexAccess.class));

    private static final EnumMap<MailField, List<String>> field2Name;

    private static final Set<String> allFields;

    private static final MailFields mailFields;

    static {
        {
            final EnumMap<MailField, List<String>> map = new EnumMap<MailField, List<String>>(MailField.class);
            map.put(MailField.ACCOUNT_NAME, singletonList(FIELD_ACCOUNT));
            map.put(MailField.ID, singletonList(FIELD_ID));
            map.put(MailField.FOLDER_ID, singletonList(FIELD_FULL_NAME));
            map.put(MailField.FROM, singletonList(FIELD_FROM_PLAIN));
            map.put(MailField.TO, singletonList(FIELD_TO_PLAIN));
            map.put(MailField.CC, singletonList(FIELD_CC_PLAIN));
            map.put(MailField.BCC, singletonList(FIELD_BCC_PLAIN));
            map.put(MailField.FLAGS, Arrays.asList(
                FIELD_FLAG_ANSWERED,
                FIELD_FLAG_DELETED,
                FIELD_FLAG_DRAFT,
                FIELD_FLAG_FLAGGED,
                FIELD_FLAG_FORWARDED,
                FIELD_FLAG_READ_ACK,
                FIELD_FLAG_RECENT,
                FIELD_FLAG_SEEN,
                FIELD_FLAG_SPAM,
                FIELD_FLAG_USER,
                FIELD_USER_FLAGS));
            map.put(MailField.SIZE, singletonList(FIELD_SIZE));
            {
                final Set<Locale> knownLocales = IndexConstants.KNOWN_LOCALES;
                final List<String> names = new ArrayList<String>(knownLocales.size());
                final StringBuilder tmp = new StringBuilder(FIELD_SUBJECT_PREFIX); // 8
                for (final Locale loc : knownLocales) {
                    tmp.setLength(8);
                    tmp.append(loc.getLanguage());
                    names.add(tmp.toString());
                }
                map.put(MailField.SUBJECT, names);
            }
            map.put(MailField.RECEIVED_DATE, singletonList(FIELD_RECEIVED_DATE));
            map.put(MailField.SENT_DATE, singletonList(FIELD_SENT_DATE));
            map.put(MailField.COLOR_LABEL, singletonList(FIELD_COLOR_LABEL));
            map.put(MailField.CONTENT_TYPE, singletonList(FIELD_ATTACHMENT));
            // {
            // final Set<Locale> knownLocales = IndexConstants.KNOWN_LOCALES;
            // final List<String> names = new ArrayList<String>(knownLocales.size());
            // final StringBuilder tmp = new StringBuilder("content_"); //8
            // for (final Locale loc : knownLocales) {
            // tmp.setLength(8);
            // tmp.append(loc.getLanguage());
            // names.add(tmp.toString());
            // }
            // map.put(MailField.BODY, names);
            // }
            field2Name = map;
        }
        {
            final Set<String> set = new HashSet<String>(16);
            for (final List<String> fields : field2Name.values()) {
                for (final String field : fields) {
                    set.add(field);
                }
            }
            allFields = set;
        }
        mailFields = new MailFields(field2Name.keySet());
    }

    /**
     * Gets the field2name mapping
     *
     * @return The field2name mapping
     */
    public static EnumMap<MailField, List<String>> getField2name() {
        return field2Name;
    }

    /**
     * Gets the indexable fields.
     * 
     * @return The indexable fields
     */
    public static MailFields getIndexableFields() {
        return mailFields;
    }

    /*-
     * ------------------- Member stuff --------------------
     */

    /**
     * The trigger type.
     */
    private final TriggerType triggerType;

    /**
     * The helper instance.
     */
    protected final SolrInputDocumentHelper helper;

    /**
     * Initializes a new {@link MailSolrIndexAccess}.
     * 
     * @param identifier The Solr server identifier
     * @param triggerType The trigger type
     */
    public MailSolrIndexAccess(final SolrIndexIdentifier identifier, final TriggerType triggerType) {
        super(identifier);
        this.triggerType = triggerType;
        helper = SolrInputDocumentHelper.getInstance();
    }

    @Override
    public void addEnvelopeData(final IndexDocument<MailMessage> document) throws OXException {
        final SolrInputDocument solrDocument = helper.inputDocumentFor(document.getObject(), userId, contextId);
        addDocument(solrDocument, true);
    }

    @Override
    public void addEnvelopeData(final Collection<IndexDocument<MailMessage>> col) throws OXException, InterruptedException {
        if (col == null || col.isEmpty()) {
            return;
        }
        final List<IndexDocument<MailMessage>> documents;
        if (col instanceof List) {
            documents = (List<IndexDocument<MailMessage>>) col;
        } else {
            documents = new ArrayList<IndexDocument<MailMessage>>(col);
        }

        final int chunkSize = ADD_ROWS;
        final int size = documents.size();
        int off = 0;
        while (off < size) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Thread interrupted while adding Solr input documents.");
            }
            int endIndex = off + chunkSize;
            if (endIndex >= size) {
                endIndex = size;
            }
            final List<IndexDocument<MailMessage>> subList = documents.subList(off, endIndex);
            final List<SolrInputDocument> solrDocuments = helper.inputDocumentsFor(subList, userId, contextId);
            addDocuments(solrDocuments, true);
            off = endIndex;
        }
    }

    @Override
    public void addContent(final IndexDocument<MailMessage> document) throws OXException {
        final MailMessage mailMessage = document.getObject();
        final int accountId = mailMessage.getAccountId();
        final MailUUID uuid = new MailUUID(contextId, userId, accountId, mailMessage.getFolder(), mailMessage.getMailId());
        /*
         * Check if envelope data already present
         */
        SolrDocument solrDocument = null;
        while (null == solrDocument) {
            StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(FIELD_UUID).append(":\"").append(uuid.getUUID()).append("\")");
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            queryBuilder = null;
            solrQuery.setStart(Integer.valueOf(0));
            solrQuery.setRows(Integer.valueOf(1));            
            final QueryResponse queryResponse = query(solrQuery.getQuery());
            final SolrDocumentList results = queryResponse.getResults();
            final long numFound = results.getNumFound();
            if (numFound <= 0) {
                addEnvelopeData(document);
            } else {
                solrDocument = results.get(0);
            }
        }
        /*
         * Create input document
         */
        final SolrInputDocument inputDocument = new SolrInputDocument();
        for (final Entry<String, Object> entry : solrDocument.entrySet()) {
            final String name = entry.getKey();
            final SolrInputField field = new SolrInputField(name);
            field.setValue(entry.getValue(), 1.0f);
            inputDocument.put(name, field);
        }
        /*
         * Add content
         */
        final TextFinder textFinder = new TextFinder();
        final String text = textFinder.getText(mailMessage);
        if (null != text) {
            final Locale locale = detectLocale(text);
            inputDocument.setField(FIELD_CONTENT_PREFIX + locale.getLanguage(), text);
        }
        inputDocument.setField(FIELD_CONTENT_FLAG, Boolean.TRUE);        
        addDocument(inputDocument, true);
    }

    @Override
    public void addContent(final Collection<IndexDocument<MailMessage>> documents) throws OXException, InterruptedException {
        for (final IndexDocument<MailMessage> document : documents) {
            if (Thread.interrupted()) {
                // Clears the thread's interrupted flag
                throw new InterruptedException("Thread interrupted while adding mail contents.");
            }
            addContent(document);
        }
    }

    @Override
    public void addAttachments(final IndexDocument<MailMessage> document) throws OXException {
        addContent(document);
    }

    @Override
    public void addAttachments(final Collection<IndexDocument<MailMessage>> documents) throws OXException, InterruptedException {
        addContent(documents);
    }

    @Override
    public void change(final IndexDocument<MailMessage> document, final String... fields) throws OXException {
        if (null == fields || 0 == fields.length) {
            return;
        }

        change(document, new HashSet<String>(Arrays.asList(fields)));
    }

    private void change(final IndexDocument<MailMessage> document, final Set<String> fields) throws OXException {
        final MailMessage mailMessage = document.getObject();
        final int accountId = mailMessage.getAccountId();
        final MailUUID uuid = new MailUUID(contextId, userId, accountId, mailMessage.getFolder(), mailMessage.getMailId());
        /*
         * Check if envelope data already present
         */
        SolrDocument solrDocument = null;
        {
            StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(FIELD_UUID).append(":\"").append(uuid.getUUID()).append("\")");
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            queryBuilder = null;
            solrQuery.setStart(Integer.valueOf(0));
            solrQuery.setRows(Integer.valueOf(1));
            final QueryResponse queryResponse = query(solrQuery.getQuery());
            final SolrDocumentList results = queryResponse.getResults();
            final long numFound = results.getNumFound();
            if (numFound <= 0) {
                // Nothing to change
                return;
            }
            solrDocument = results.get(0);
        }
        /*
         * Create input document
         */
        final SolrInputDocument inputDocument = new SolrInputDocument();
        for (final Entry<String, Object> entry : solrDocument.entrySet()) {
            final String name = entry.getKey();
            final SolrInputField field = new SolrInputField(name);
            field.setValue(entry.getValue(), 1.0f);
            inputDocument.put(name, field);
        }
        /*
         * Write color label
         */
        final boolean all = fields.contains(ALL_FIELDS);
        if (all || fields.contains(FIELD_COLOR_LABEL)) {
            final SolrInputField field = new SolrInputField(FIELD_COLOR_LABEL);
            field.setValue(Integer.valueOf(mailMessage.getColorLabel()), 1.0f);
            inputDocument.put(FIELD_COLOR_LABEL, field);
        }
        /*
         * Write flags
         */
        {
            final int flags = mailMessage.getFlags();

            if (all || fields.contains(FIELD_FLAG_ANSWERED)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_ANSWERED);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_ANSWERED, field);
            }

            if (all || fields.contains(FIELD_FLAG_DELETED)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_DELETED);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_DELETED, field);
            }

            if (all || fields.contains(FIELD_FLAG_DRAFT)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_DRAFT);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_DRAFT, field);
            }

            if (all || fields.contains(FIELD_FLAG_FLAGGED)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_FLAGGED);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FLAGGED) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_FLAGGED, field);
            }

            if (all || fields.contains(FIELD_FLAG_RECENT)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_RECENT);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_RECENT, field);
            }

            if (all || fields.contains(FIELD_FLAG_SEEN)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_SEEN);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_SEEN, field);
            }

            if (all || fields.contains(FIELD_FLAG_USER)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_USER);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_USER, field);
            }

            if (all || fields.contains(FIELD_FLAG_SPAM)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_SPAM);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_SPAM, field);
            }

            if (all || fields.contains(FIELD_FLAG_FORWARDED)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_FORWARDED);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_FORWARDED, field);
            }

            if (all || fields.contains(FIELD_FLAG_READ_ACK)) {
                final SolrInputField field = new SolrInputField(FIELD_FLAG_READ_ACK);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_READ_ACK, field);
            }
        }
        /*
         * User flags
         */
        if (all || fields.contains(FIELD_USER_FLAGS)){
            final String[] userFlags = mailMessage.getUserFlags();
            if (null != userFlags && userFlags.length > 0) {
                final SolrInputField field = new SolrInputField(FIELD_USER_FLAGS);
                field.setValue(Arrays.asList(userFlags), 1.0f);
                inputDocument.put(FIELD_USER_FLAGS, field);
            }
        }
        
        addDocument(inputDocument, true);
    }

    @Override
    public void change(final Collection<IndexDocument<MailMessage>> documents, final String... fields) throws OXException, InterruptedException {
        if (null == fields || 0 == fields.length) {
            return;
        }

        for (final IndexDocument<MailMessage> document : documents) {
            if (Thread.interrupted()) {
                // Clears the thread's interrupted flag
                throw new InterruptedException("Thread interrupted while changing mail contents.");
            }
            change(document, new HashSet<String>(Arrays.asList(fields)));
        }
    }

    @Override
    public void deleteById(final String id) throws OXException {
        final SolrResponse response = deleteDocumentById(id);
        // TODO: Check response?
//        CommonsHttpSolrServer solrServer = null;
//        final String query = null;
//        boolean ran = false;
//        try {
//            solrServer = solrServerFor();
//            solrServer.deleteById(id);
//            ran = true;
//            /*
//             * Commit sane
//             */
//            commitSane(solrServer);
//        } catch (final SolrServerException e) {
//            if (!ran) {
//                LOG.debug("MailSolrIndexAccess.deleteById() failed for query:\n" + query);
//            }
//            rollback(solrServer);
//            throw SolrIndexExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
//        } catch (final IOException e) {
//            if (!ran) {
//                LOG.debug("MailSolrIndexAccess.deleteById() failed for query:\n" + query);
//            }
//            rollback(solrServer);
//            throw IndexExceptionCodes.IO_ERROR.create(e, e.getMessage());
//        } catch (final RuntimeException e) {
//            if (!ran) {
//                LOG.debug("MailSolrIndexAccess.deleteById() failed for query:\n" + query);
//            }
//            rollback(solrServer);
//            throw IndexExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
//        }
    }

    @Override
    public void deleteByQuery(final String query) throws OXException {
        if (isEmpty(query)) {
            return;
        }
        
        final SolrResponse response = deleteDocumentsByQuery(query);
        // TODO: Check response?
//        CommonsHttpSolrServer solrServer = null;
//        boolean ran = false;
//        try {
//            solrServer = solrServerFor();
//            solrServer.deleteByQuery(query);
//            ran = true;
//            /*
//             * Commit sane
//             */
//            commitSane(solrServer);
//        } catch (final SolrServerException e) {
//            if (!ran) {
//                LOG.debug("MailSolrIndexAccess.deleteByQuery() failed for query:\n" + query);
//            }
//            rollback(solrServer);
//            throw SolrIndexExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
//        } catch (final IOException e) {
//            if (!ran) {
//                LOG.debug("MailSolrIndexAccess.deleteByQuery() failed for query:\n" + query);
//            }
//            rollback(solrServer);
//            throw IndexExceptionCodes.IO_ERROR.create(e, e.getMessage());
//        } catch (final RuntimeException e) {
//            if (!ran) {
//                LOG.debug("MailSolrIndexAccess.deleteByQuery() failed for query:\n" + query);
//            }
//            rollback(solrServer);
//            throw IndexExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
//        }
    }

    @Override
    public IndexResult<MailMessage> query(final QueryParameters parameters) throws OXException, InterruptedException {
        if (null == parameters) {
            return Indexes.emptyResult();
        }

        final String queryString = parameters.getQueryString();
        final int length = parameters.getLen();
        /*
         * Page-wise retrieval
         */
        final int maxRows = QUERY_ROWS;
        final String sortField = (String) parameters.getParameters().get("sort");
        final ORDER order = "desc".equalsIgnoreCase((String) parameters.getParameters().get("order")) ? ORDER.desc : ORDER.asc;
        final String[] fieldArray;
        int off = parameters.getOff();
        int end;
        final List<IndexDocument<MailMessage>> mails;
        final List<MailFiller> mailFillers;
        final MailIndexResult result;
        {
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryString);
            solrQuery.setStart(Integer.valueOf(off));
            solrQuery.setRows(Integer.valueOf(length > maxRows ? maxRows : length));
            if (null != sortField) {
                solrQuery.setSortField(sortField, order);
            }
            final Set<String> set = allFields;
            fieldArray = set.toArray(new String[set.size()]);
            solrQuery.setFields(fieldArray);
            final QueryResponse queryResponse = query(solrQuery.getQuery());
            final SolrDocumentList results = queryResponse.getResults();
            final long numFound = results.getNumFound();
            if (numFound <= 0) {
                return Indexes.emptyResult();
            }
            result = new MailIndexResult(numFound);
            end = off + length;
            if (end > numFound) {
                end = (int) numFound;
            }
            mails = new ArrayList<IndexDocument<MailMessage>>(end - off);
            mailFillers = MailFillers.allFillers();
            final int size = results.size();
            for (int i = 0; i < size; i++) {
                mails.add(helper.readDocument(results.get(i), mailFillers));
            }
            off += size;
        }
        while (off < end) {
            if (Thread.interrupted()) {
                // Clears the thread's interrupted flag
                throw new InterruptedException("Thread interrupted while paging through Solr results.");
            }
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryString);
            solrQuery.setStart(Integer.valueOf(off));
            int rows = end - off;
            rows = rows > maxRows ? maxRows : rows;
            solrQuery.setRows(Integer.valueOf(rows));
            if (null != sortField) {
                solrQuery.setSortField(sortField, order);
            }
            solrQuery.setFields(fieldArray);
            final QueryResponse queryResponse = query(solrQuery.getQuery());
            final SolrDocumentList results = queryResponse.getResults();
            final int size = results.size();
            if (size <= 0) {
                break;
            }
            for (int i = 0; i < size; i++) {
                mails.add(helper.readDocument(results.get(i), mailFillers));
            }
            off += size;
        }
        result.setResults(mails);
        return result;
    }

    @Override
    public TriggerType getTriggerType() {
        return triggerType;
    }

    /**
     * Adds mandatory fields to specified set:
     * <ul>
     * <li>UUID</li>
     * <li>ID</li>
     * <li>FULL_NAME</li>
     * <li>ACCOUNT</li>
     * <li>USER</li>
     * <li>CONTEXT</li>
     * <li>CONTENT_FLAG</li>
     * </ul>
     * 
     * @param set The set to add to
     */
    private static void addMandatoryFields(final Set<String> set) {
        set.add(FIELD_UUID);
        set.add(FIELD_ID);
        set.add(FIELD_FULL_NAME);
        set.add(FIELD_ACCOUNT);
        set.add(FIELD_USER);
        set.add(FIELD_CONTEXT);
        set.add(FIELD_CONTENT_FLAG);
    }

    /**
     * Checks for an empty string.
     */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
