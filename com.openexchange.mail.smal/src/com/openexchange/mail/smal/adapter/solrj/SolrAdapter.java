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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.adapter.solrj;

import static com.openexchange.mail.mime.QuotedInternetAddress.toIDN;
import static com.openexchange.mail.smal.adapter.IndexAdapters.detectLocale;
import static com.openexchange.mail.smal.adapter.IndexAdapters.isEmpty;
import static com.openexchange.mail.smal.adapter.solrj.SolrUtils.commitWithTimeout;
import static com.openexchange.mail.smal.adapter.solrj.SolrUtils.rollback;
import static java.util.Collections.singletonList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.index.ConfigIndexService;
import com.openexchange.index.IndexUrl;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.SMALExceptionCodes;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.mail.smal.adapter.IndexAdapters;
import com.openexchange.mail.smal.adapter.solrj.contentgrab.SolrTextFillerQueue;
import com.openexchange.mail.smal.adapter.solrj.contentgrab.TextFiller;
import com.openexchange.mail.smal.adapter.solrj.management.CommonsHttpSolrServerManagement;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link SolrAdapter}
 * <p>
 * http://wiki.apache.org/solr/Solrj
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrAdapter implements IndexAdapter, SolrConstants {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(SolrAdapter.class));

    private static final int QUERY_ROWS = 1000;

    private static final int DELETE_ROWS = 25;

    private static final int CHANGE_ROWS = 25;

    private static final int GET_ROWS = 25;

    private volatile CommonsHttpSolrServerManagement solrServerManagement;

    private volatile SolrTextFillerQueue textFillerQueue;

    private final EnumMap<MailSortField, String> sortField2Name;

    private final EnumMap<MailField, List<String>> field2Name;

    private final MailFields mailFields;

    /**
     * Initializes a new {@link SolrAdapter}.
     */
    public SolrAdapter() {
        super();
        {
            final EnumMap<MailSortField, String> map = new EnumMap<MailSortField, String>(MailSortField.class);
            map.put(MailSortField.ACCOUNT_NAME, FIELD_ACCOUNT);
            map.put(MailSortField.CC, FIELD_CC_PLAIN);
            map.put(MailSortField.COLOR_LABEL, FIELD_COLOR_LABEL);
            map.put(MailSortField.FLAG_SEEN, FIELD_FLAG_SEEN);
            map.put(MailSortField.FROM, FIELD_FROM_PLAIN);
            map.put(MailSortField.RECEIVED_DATE, FIELD_RECEIVED_DATE);
            map.put(MailSortField.SENT_DATE, FIELD_SENT_DATE);
            map.put(MailSortField.SIZE, FIELD_SIZE);
            map.put(MailSortField.SUBJECT, FIELD_SUBJECT_PLAIN);
            map.put(MailSortField.TO, FIELD_TO_PLAIN);
            sortField2Name = map;
        }
        {
            final EnumMap<MailField, List<String>> map = new EnumMap<MailField, List<String>>(MailField.class);
            map.put(MailField.ACCOUNT_NAME, singletonList(FIELD_ACCOUNT));
            map.put(MailField.ID, singletonList(FIELD_ID));
            map.put(MailField.FOLDER_ID, singletonList(FIELD_FULL_NAME));
            map.put(MailField.FROM, singletonList(FIELD_FROM_PLAIN));
            map.put(MailField.TO, singletonList(FIELD_TO_PLAIN));
            map.put(MailField.CC, singletonList(FIELD_CC_PLAIN));
            map.put(MailField.BCC, singletonList(FIELD_BCC_PLAIN));
            map.put(MailField.FLAGS, Arrays.asList(FIELD_FLAG_ANSWERED, FIELD_FLAG_DELETED, FIELD_FLAG_DRAFT, FIELD_FLAG_FLAGGED, FIELD_FLAG_FORWARDED, FIELD_FLAG_READ_ACK, FIELD_FLAG_RECENT, FIELD_FLAG_SEEN, FIELD_FLAG_SPAM, FIELD_FLAG_USER));
            map.put(MailField.SIZE, singletonList(FIELD_SIZE));
            {
                final Set<Locale> knownLocales = IndexAdapters.KNOWN_LOCALES;
                final List<String> names = new ArrayList<String>(knownLocales.size());
                final StringBuilder tmp = new StringBuilder(FIELD_SUBJECT_PREFIX); //8
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
            // final Set<Locale> knownLocales = IndexAdapters.KNOWN_LOCALES;
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
        mailFields = new MailFields(field2Name.keySet());
    }

    private IndexUrl indexUrlFor(final Session session, final boolean readWrite) throws OXException {
        final ConfigIndexService configIndexService = SMALServiceLookup.getServiceStatic(ConfigIndexService.class);
        if (readWrite) {
            return configIndexService.getWriteURL(session.getContextId(), session.getUserId(), Types.EMAIL);
        }
        return configIndexService.getReadOnlyURL(session.getContextId(), session.getUserId(), Types.EMAIL);
    }

    private CommonsHttpSolrServer solrServerFor(final Session session, final boolean readWrite) throws OXException {
        if (readWrite) {
            return solrServerManagement.newSolrServer(indexUrlFor(session, true));
        }
        return solrServerManagement.getSolrServer(indexUrlFor(session, false));
    }

    @Override
    public void start() throws OXException {
        final CommonsHttpSolrServerManagement cache = solrServerManagement = new CommonsHttpSolrServerManagement(100, 300000);
        final SolrTextFillerQueue q = textFillerQueue = new SolrTextFillerQueue(cache);
        q.start();
    }

    @Override
    public void stop() throws OXException {
        final CommonsHttpSolrServerManagement solrServerCache = this.solrServerManagement;
        if (null != solrServerCache) {
            solrServerCache.shutDown();
            this.solrServerManagement = null;
        }
    }

    @Override
    public MailFields getIndexableFields() throws OXException {
        return mailFields;
    }

    @Override
    public void onSessionAdd(final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSessionGone(final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<MailMessage> search(final String query, final MailField[] fields, final Session session) throws OXException, InterruptedException {
        try {
            final CommonsHttpSolrServer solrServer = solrServerFor(session, false);
            final MailFields mailFields = new MailFields(fields);
            /*
             * Page-wise retrieval
             */
            final Integer rows = Integer.valueOf(QUERY_ROWS);
            int off;
            final long numFound;
            final List<MailMessage> mails;
            {
                final SolrQuery solrQuery = new SolrQuery().setQuery(query);
                solrQuery.setStart(Integer.valueOf(0));
                solrQuery.setRows(rows);
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                numFound = results.getNumFound();
                if (numFound <= 0) {
                    return Collections.emptyList();
                }
                mails = new ArrayList<MailMessage>((int) numFound);
                final int size = results.size();
                for (int i = 0; i < size; i++) {
                    mails.add(readDocument(results.get(i), mailFields));
                }
                off = size;
            }
            final Thread thread = Thread.currentThread();
            while (off < numFound) {
                if (thread.isInterrupted()) {
                    // Clears the thread's interrupted flag
                    throw new InterruptedException("Thread interrupted while paging through Solr results.");
                }
                final SolrQuery solrQuery = new SolrQuery().setQuery(query);
                solrQuery.setStart(Integer.valueOf(off));
                solrQuery.setRows(rows);
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                final int size = results.size();
                if (size <= 0) {
                    break;
                }
                for (int i = 0; i < size; i++) {
                    mails.add(readDocument(results.get(i), mailFields));
                }
                off += size;
            }
            return mails;
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public List<MailMessage> search(final String optFullName, final SearchTerm<?> searchTerm, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int optAccountId, final Session session) throws OXException, InterruptedException {
        try {
            final CommonsHttpSolrServer solrServer = solrServerFor(session, false);
            final MailFields mailFields = new MailFields(fields);
            if (null != sortField) {
                final MailField sf = MailField.getField(sortField.getField());
                if (null != sf) {
                    mailFields.add(sf);
                }
            }
            final String query;
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
                query = queryBuilder.toString();
            }
            /*
             * Page-wise retrieval
             */
            final Integer rows = Integer.valueOf(QUERY_ROWS);
            int off;
            final long numFound;
            final List<MailMessage> mails;
            {
                final SolrQuery solrQuery = new SolrQuery().setQuery(query);
                solrQuery.setStart(Integer.valueOf(0));
                solrQuery.setRows(rows);
                // TODO: solrQuery.setFields();
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                numFound = results.getNumFound();
                if (numFound <= 0) {
                    return Collections.emptyList();
                }
                mails = new ArrayList<MailMessage>((int) numFound);
                final int size = results.size();
                for (int i = 0; i < size; i++) {
                    mails.add(readDocument(results.get(i), mailFields));
                }
                off = size;
                
                System.out.println("SolrjAdapter.search() requested " + off +" of " + numFound + " mails from index for:\n" + query);
                
                
            }
            final Thread thread = Thread.currentThread();
            while (off < numFound) {
                if (thread.isInterrupted()) {
                    // Clears the thread's interrupted flag
                    throw new InterruptedException("Thread interrupted while paging through Solr results.");
                }
                final SolrQuery solrQuery = new SolrQuery().setQuery(query);
                solrQuery.setStart(Integer.valueOf(off));
                solrQuery.setRows(rows);
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                final int size = results.size();
                if (size <= 0) {
                    break;
                }
                for (int i = 0; i < size; i++) {
                    mails.add(readDocument(results.get(i), mailFields));
                }
                off += size;
                
                System.out.println("SolrjAdapter.search() requested " + off +" of " + numFound + " mails from index for " + optFullName);
                
                
            }
            if (null != sortField) {
                Collections.sort(mails, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getUserLocaleLazy(session)));
            }
            return mails;
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static Locale getUserLocaleLazy(final Session session) throws OXException {
        Locale locale = (Locale) session.getParameter("solr.userLocale");
        if (null == locale) {
            if (session instanceof ServerSession) {
                final ServerSession serverSession = (ServerSession) session;
                locale = serverSession.getUser().getLocale();
            } else {
                final Context context = SMALServiceLookup.getServiceStatic(ContextService.class).getContext(session.getContextId());
                locale = SMALServiceLookup.getServiceStatic(UserService.class).getUser(session.getUserId(), context).getLocale();
            }
            session.setParameter("solr.userLocale", locale);
        }
        return locale;
    }

    private static final MailFields FIELDS_ADDRESSES = new MailFields(MailField.FROM, MailField.TO, MailField.CC, MailField.BCC);

    private MailMessage readDocument(final SolrDocument document, final MailFields mailFields) throws OXException {
        if (SolrTextFillerQueue.checkSolrDocument(document)) {
            textFillerQueue.add(TextFiller.fillerFor(document), true);
        }
        final MailFields fields = null == mailFields ? new MailFields(true) : mailFields;
        final MailMessage mail = new IDMailMessage(document.getFieldValue(FIELD_ID).toString(), document.getFieldValue(FIELD_FULL_NAME).toString());
        mail.setAccountId(SolrAdapter.<Integer> getFieldValue(FIELD_ACCOUNT, document).intValue());
        if (fields.contains(MailField.COLOR_LABEL)) {
            mail.setColorLabel(SolrAdapter.<Integer> getFieldValue(FIELD_COLOR_LABEL, document).intValue());
        }
        if (fields.contains(MailField.CONTENT_TYPE)) {
            mail.setHasAttachment(SolrAdapter.<Boolean> getFieldValue(FIELD_ATTACHMENT, document).booleanValue());
        }
        if (fields.contains(MailField.SIZE)) {
            final Long size = SolrAdapter.<Long> getFieldValue(FIELD_SIZE, document);
            if (null != size) {
                mail.setSize(size.longValue());
            }
        }
        if (fields.contains(MailField.RECEIVED_DATE)) {
            final Long time = SolrAdapter.<Long> getFieldValue(FIELD_RECEIVED_DATE, document);
            if (null != time) {
                mail.setReceivedDate(new Date(time.longValue()));
            }
        }
        if (fields.contains(MailField.SENT_DATE)) {
            final Long time = SolrAdapter.<Long> getFieldValue(FIELD_SENT_DATE, document);
            if (null != time) {
                mail.setSentDate(new Date(time.longValue()));
            }
        }
        if (fields.containsAny(FIELDS_ADDRESSES)) {
            String addressList;
            if (fields.contains(MailField.FROM) && !isEmpty((addressList = getFieldValue(FIELD_FROM_PLAIN, document)))) {
                try {
                    mail.addFrom(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addFrom(new PlainTextAddress(addressList));
                }
            }
            if (fields.contains(MailField.TO) && !isEmpty((addressList = getFieldValue(FIELD_TO_PLAIN, document)))) {
                try {
                    mail.addTo(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addTo(new PlainTextAddress(addressList));
                }
            }
            if (fields.contains(MailField.CC) && !isEmpty((addressList = getFieldValue(FIELD_CC_PLAIN, document)))) {
                try {
                    mail.addCc(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addCc(new PlainTextAddress(addressList));
                }
            }
            if (fields.contains(MailField.BCC) && !isEmpty((addressList = getFieldValue(FIELD_BCC_PLAIN, document)))) {
                try {
                    mail.addBcc(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addBcc(new PlainTextAddress(addressList));
                }
            }
        }
        if (fields.contains(MailField.FLAGS)) {
            int flags = 0;
            Boolean b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_ANSWERED, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_ANSWERED;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_DELETED, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_DELETED;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_DRAFT, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_DRAFT;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_FLAGGED, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_FLAGGED;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_FORWARDED, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_FORWARDED;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_READ_ACK, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_READ_ACK;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_RECENT, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_RECENT;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_SEEN, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_SEEN;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_SPAM, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_SPAM;
            }
            b = SolrAdapter.<Boolean> getFieldValue(FIELD_FLAG_USER, document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_USER;
            }
            mail.setFlags(flags);
        }
        // Subject
        if (fields.contains(MailField.SUBJECT)) {
            final StringBuilder pre = new StringBuilder(FIELD_SUBJECT_PREFIX);
            for (final Locale l : IndexAdapters.KNOWN_LOCALES) {
                pre.setLength(8);
                final String subject = getFieldValue(pre.append(l.getLanguage()).toString(), document);
                if (null != subject) {
                    mail.setSubject(subject);
                    break;
                }
            }
        }
        // Return mail
        return mail;
    }

    @SuppressWarnings("unchecked")
    private static <V> V getFieldValue(final String name, final SolrDocument document) throws OXException {
        final Object value = document.getFieldValue(name);
        if (null == value) {
            return null;
        }
        try {
            return (V) value;
        } catch (final ClassCastException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected type: " + e.getMessage());
        }
    }

    @Override
    public List<MailMessage> getMessages(final String[] optMailIds, final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int accountId, final Session session) throws OXException, InterruptedException {
        if (null == optMailIds || 0 == optMailIds.length) {
            return search(fullName, null, sortField, order, fields, accountId, session);
        }
        try {
            final CommonsHttpSolrServer solrServer = solrServerFor(session, false);
            final MailFields mailFields = new MailFields(fields);
            if (null != sortField) {
                final MailField sf = MailField.getField(sortField.getField());
                if (null != sf) {
                    mailFields.add(sf);
                }
            }
            final StringBuilder queryBuilder = new StringBuilder(128);
            {
                queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
                queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
                if (accountId >= 0) {
                    queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
                }
                if (null != fullName) {
                    queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
                }
            }
            final int size = optMailIds.length;
            final List<MailMessage> mails = new ArrayList<MailMessage>(size);
            final String fieldId = FIELD_ID;
            final Thread thread = Thread.currentThread();
            final int resetLen = queryBuilder.length();
            int off = 0;
            while (off < size) {
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Thread interrupted while getting Solr documents.");
                }
                int endIndex = off + GET_ROWS;
                if (endIndex >= size) {
                    endIndex = size;
                }
                final int len = endIndex - off;
                final String[] ids = new String[len];
                System.arraycopy(optMailIds, off, ids, 0, len);
                queryBuilder.setLength(resetLen);
                queryBuilder.append(" AND (").append(fieldId).append(':').append(ids[0]);
                for (int i = 1; i < len; i++) {
                    queryBuilder.append(" OR ").append(fieldId).append(':').append(ids[i]);
                }
                queryBuilder.append(')');
                mails.addAll(getMessageSublist(queryBuilder.toString(), solrServer, mailFields));
                off = endIndex;
            }
            if (null != sortField) {
                Collections.sort(mails, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getUserLocaleLazy(session)));
            }
            return mails;
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private List<MailMessage> getMessageSublist(final String query, final CommonsHttpSolrServer solrServer, final MailFields mailFields) throws OXException, InterruptedException {
        try {
            /*
             * Page-wise retrieval
             */
            final Integer rows = Integer.valueOf(QUERY_ROWS);
            int off;
            final long numFound;
            final List<MailMessage> mails;
            {
                final SolrQuery solrQuery = new SolrQuery().setQuery(query);
                solrQuery.setStart(Integer.valueOf(0));
                solrQuery.setRows(rows);
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                numFound = results.getNumFound();
                if (numFound <= 0) {
                    return Collections.emptyList();
                }
                mails = new ArrayList<MailMessage>((int) numFound);
                final int size = results.size();
                for (int i = 0; i < size; i++) {
                    mails.add(readDocument(results.get(i), mailFields));
                }
                off = size;
            }
            final Thread thread = Thread.currentThread();
            while (off < numFound) {
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Thread interrupted while paging through Solr results.");
                }
                final SolrQuery solrQuery = new SolrQuery().setQuery(query);
                solrQuery.setStart(Integer.valueOf(off));
                solrQuery.setRows(rows);
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                final int size = results.size();
                if (size <= 0) {
                    break;
                }
                for (int i = 0; i < size; i++) {
                    mails.add(readDocument(results.get(i), mailFields));
                }
                off += size;
            }
            return mails;
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void deleteMessages(final Collection<String> mailIds, final String fullName, final int accountId, final Session session) throws OXException, InterruptedException {
        if (null == mailIds || mailIds.isEmpty()) {
            return;
        }
        CommonsHttpSolrServer solrServer = null;
        String query = null;
        boolean ran = false;
        try {
            solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
            if (accountId >= 0) {
                queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
            }
            if (null != fullName) {
                queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
            }
            final int resetLen = queryBuilder.length();
            final int size = mailIds.size();
            final List<String> list;
            if (mailIds instanceof List) {
                list = (List<String>) mailIds;
            } else {
                list = new ArrayList<String>(size);
                list.addAll(mailIds);
            }
            final String fieldId = FIELD_ID;
            final Thread thread = Thread.currentThread();
            int off = 0;
            /*
             * TODO: Check if single deletes are faster
             */
            while (off < size) {
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Thread interrupted while deleting Solr documents.");
                }
                int endIndex = off + DELETE_ROWS;
                if (endIndex >= size) {
                    endIndex = size;
                }
                final List<String> subList = list.subList(off, endIndex);
                queryBuilder.setLength(resetLen);
                final Iterator<String> iterator = subList.iterator();
                queryBuilder.append(" AND (").append(fieldId).append(':').append(iterator.next());
                while (iterator.hasNext()) {
                    queryBuilder.append(" OR ").append(fieldId).append(':').append(iterator.next());
                }
                queryBuilder.append(')');
                query = queryBuilder.toString();
                solrServer.deleteByQuery(query);
                off = endIndex;
            }
            ran = true;
            /*
             * Commit without timeout
             */
            commitWithTimeout(solrServer);
        } catch (final SolrServerException e) {
            if (!ran) {
                LOG.debug("SolrServer.deleteByQuery() failed for query:\n" + query);
                
                System.out.println("SolrServer.deleteByQuery() failed for query:\n" + query);
            }
            rollback(solrServer);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            if (!ran) {
                LOG.debug("SolrServer.deleteByQuery() failed for query:\n" + query);
                
                System.out.println("SolrServer.deleteByQuery() failed for query:\n" + query);
            }
            rollback(solrServer);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            if (!ran) {
                LOG.debug("SolrServer.deleteByQuery() failed for query:\n" + query);
                
                System.out.println("SolrServer.deleteByQuery() failed for query:\n" + query);
            }
            rollback(solrServer);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void deleteFolder(final String fullName, final int accountId, final Session session) throws OXException {
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
            if (accountId >= 0) {
                queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
            }
            if (null != fullName) {
                queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
            }
            solrServer.deleteByQuery(queryBuilder.toString());
            /*
             * Commit without timeout
             */
            commitWithTimeout(solrServer);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean containsFolder(final String fullName, final int accountId, final Session session) throws OXException {
        try {
            final CommonsHttpSolrServer solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
            if (accountId >= 0) {
                queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
            }
            if (null != fullName) {
                queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
            }
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            solrQuery.setRows(Integer.valueOf(0)); // No results needed
            final QueryResponse queryResponse = solrServer.query(solrQuery);
            return queryResponse.getResults().getNumFound() > 0;
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final MailFields CHANGE_FIELDS = new MailFields(MailField.FOLDER_ID, MailField.ID, MailField.FLAGS, MailField.COLOR_LABEL);

    @Override
    public void change(final List<MailMessage> mails, final Session session) throws OXException, InterruptedException {
        if (null == mails || mails.isEmpty()) {
            return;
        }
        CommonsHttpSolrServer solrServer = null;
        boolean rollback = false;
        try {
            solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
            final int accountId = mails.get(0).getAccountId();
            if (accountId >= 0) {
                queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
            }
            final String fullName = mails.get(0).getFolder();
            if (null != fullName) {
                queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
            }
            final int resetLen = queryBuilder.length();
            final String fieldId = FIELD_ID;
            final int size = mails.size();
            final Thread thread = Thread.currentThread();
            int off = 0;
            while (off < size) {
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Thread interrupted while changing Solr documents.");
                }
                int endIndex = off + CHANGE_ROWS;
                if (endIndex >= size) {
                    endIndex = size;
                }
                final List<MailMessage> subList = mails.subList(off, endIndex);
                queryBuilder.setLength(resetLen);
                final Iterator<MailMessage> iterator = subList.iterator();
                queryBuilder.append(" AND (").append(fieldId).append(':').append(iterator.next().getMailId());
                while (iterator.hasNext()) {
                    queryBuilder.append(" OR ").append(fieldId).append(':').append(iterator.next().getMailId());
                }
                queryBuilder.append(')');
                /*
                 * Change sub-list
                 */
                changeSublist(subList, queryBuilder.toString(), solrServer);
                rollback = true;
                off = endIndex;
            }
            /*
             * Commit without timeout
             */
            commitWithTimeout(solrServer);
        } catch (final OXException e) {
            rollback(rollback ? solrServer : null);
            throw e;
        } catch (final InterruptedException e) {
            rollback(rollback ? solrServer : null);
            throw e;
        } catch (final SolrServerException e) {
            rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void changeSublist(final List<MailMessage> mailSublist, final String query, final CommonsHttpSolrServer solrServer) throws OXException, InterruptedException {
        if (null == mailSublist || mailSublist.isEmpty()) {
            return;
        }
        final int size = mailSublist.size();
        final Map<String, MailMessage> map = new HashMap<String, MailMessage>(size);
        for (final MailMessage mail : mailSublist) {
            map.put(mail.getMailId(), mail);
        }
        try {
            /*
             * Page-wise retrieval
             */
            final Integer rows = Integer.valueOf(QUERY_ROWS);
            int off;
            final long numFound;
            final List<SolrDocument> documents;
            {
                final SolrQuery solrQuery = new SolrQuery().setQuery(query);
                solrQuery.setStart(Integer.valueOf(0));
                solrQuery.setRows(rows);
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                numFound = results.getNumFound();
                if (numFound <= 0) {
                    return;
                }
                documents = new ArrayList<SolrDocument>((int) numFound);
                final int rsize = results.size();
                for (int i = 0; i < rsize; i++) {
                    final SolrDocument solrDocument = results.get(i);
                    mailSublist.add(readDocument(solrDocument, CHANGE_FIELDS));
                }
                off = rsize;
            }
            final Thread thread = Thread.currentThread();
            while (off < numFound) {
                if (thread.isInterrupted()) {
                    throw new InterruptedException("Thread interrupted while paging through Solr results.");
                }
                final SolrQuery solrQuery = new SolrQuery().setQuery(query);
                solrQuery.setStart(Integer.valueOf(off));
                solrQuery.setRows(rows);
                final QueryResponse queryResponse = solrServer.query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();
                final int rsize = results.size();
                if (rsize <= 0) {
                    break;
                }
                for (int i = 0; i < rsize; i++) {
                    final SolrDocument solrDocument = results.get(i);
                    mailSublist.add(readDocument(solrDocument, CHANGE_FIELDS));
                }
                off += rsize;
            }
            solrServer.add(new SolrDocumentIterator(documents, map, textFillerQueue));
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void addContent(final MailMessage mail, final Session session) throws OXException {
        textFillerQueue.add(TextFiller.fillerFor(
            new MailUUID(session.getContextId(), session.getUserId(), mail.getAccountId(), mail.getFolder(), mail.getMailId()).getUUID(),
            mail,
            session), true);
    }

    @Override
    public void addContents() throws OXException {
    	textFillerQueue.signalConsume();
    }

    @Override
    public void add(final MailMessage mail, final Session session) throws OXException {
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final int accountId = mail.getAccountId();
            final MailUUID uuid = new MailUUID(session.getContextId(), session.getUserId(), accountId, mail.getFolder(), mail.getMailId());
            solrServer.add(createDocument(uuid.getUUID(), mail, accountId, session, System.currentTimeMillis()));
            /*
             * Commit without timeout
             */
            commitWithTimeout(solrServer);
            textFillerQueue.add(TextFiller.fillerFor(uuid.getUUID(), mail, session), false);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void add(final List<MailMessage> mails, final Session session) throws OXException {
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final List<TextFiller> fillers = new ArrayList<TextFiller>(mails.size());
            final long now = System.currentTimeMillis();
            try {
                solrServer.add(new MailDocumentIterator(mails.iterator(), session, now, fillers));
            } catch (final SolrException e) {
                // Batch failed
                rollback(solrServer);
                fillers.clear();
                for (final Iterator<SolrInputDocument> it = new MailDocumentIterator(mails.iterator(), session, now, null); it.hasNext();) {
                    final SolrInputDocument inputDocument = it.next();
                    try {
                        solrServer.add(inputDocument);
                        fillers.add(TextFiller.fillerFor(inputDocument));
                    } catch (final Exception addFailed) {
                        LOG.warn("Mail input document could not be added: id=" + inputDocument.getFieldValue("id") + " fullName=" + inputDocument.getFieldValue("full_name"), addFailed);
                    }
                }
            }
            /*
             * Commit without timeout
             */
            commitWithTimeout(solrServer);
            textFillerQueue.add(fillers, false);
        } catch (final SolrServerException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(solrServer);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    protected static SolrInputDocument createDocument(final String uuid, final MailMessage mail, final int accountId, final Session session, final long stamp) {
        final SolrInputDocument inputDocument = new SolrInputDocument();
        /*
         * Un-analyzed fields
         */
        {
            final SolrInputField field = new SolrInputField(FIELD_TIMESTAMP);
            field.setValue(Long.valueOf(stamp), 1.0f);
            inputDocument.put(FIELD_TIMESTAMP, field);
        }
        {
            final SolrInputField field = new SolrInputField(FIELD_UUID);
            field.setValue(uuid, 1.0f);
            inputDocument.put(FIELD_UUID, field);
        }
        {
            final SolrInputField field = new SolrInputField(FIELD_CONTEXT);
            field.setValue(Long.valueOf(session.getContextId()), 1.0f);
            inputDocument.put(FIELD_CONTEXT, field);
        }
        {
            final SolrInputField field = new SolrInputField(FIELD_USER);
            field.setValue(Long.valueOf(session.getUserId()), 1.0f);
            inputDocument.put(FIELD_USER, field);
        }
        {
            final SolrInputField field = new SolrInputField(FIELD_ACCOUNT);
            field.setValue(Integer.valueOf(accountId), 1.0f);
            inputDocument.put(FIELD_ACCOUNT, field);
        }
        {
            final SolrInputField field = new SolrInputField(FIELD_FULL_NAME);
            field.setValue(mail.getFolder(), 1.0f);
            inputDocument.put(FIELD_FULL_NAME, field);
        }
        {
            final SolrInputField field = new SolrInputField(FIELD_ID);
            field.setValue(mail.getMailId(), 1.0f);
            inputDocument.put(FIELD_ID, field);
        }
        /*-
         * Address fields
         
            "From"
            "To"
            "Cc"
            "Bcc"
            "Reply-To"
            "Sender"
            "Errors-To"
            "Resent-Bcc"
            "Resent-Cc"
            "Resent-From"
            "Resent-To"
            "Resent-Sender"
            "Disposition-Notification-To"
            
         */
        {
            AddressesPreparation preparation = new AddressesPreparation(mail.getFrom());
            SolrInputField field = new SolrInputField(FIELD_FROM_PERSONAL);
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put(FIELD_FROM_PERSONAL, field);
            field = new SolrInputField(FIELD_FROM_ADDR);
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put(FIELD_FROM_ADDR, field);
            field = new SolrInputField(FIELD_FROM_PLAIN);
            field.setValue(preparation.line, 1.0f);
            inputDocument.put(FIELD_FROM_PLAIN, field);

            preparation = new AddressesPreparation(mail.getTo());
            field = new SolrInputField(FIELD_TO_PERSONAL);
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put(FIELD_TO_PERSONAL, field);
            field = new SolrInputField(FIELD_TO_ADDR);
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put(FIELD_TO_ADDR, field);
            field = new SolrInputField(FIELD_TO_PLAIN);
            field.setValue(preparation.line, 1.0f);
            inputDocument.put(FIELD_TO_PLAIN, field);

            preparation = new AddressesPreparation(mail.getCc());
            field = new SolrInputField(FIELD_CC_PERSONAL);
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put(FIELD_CC_PERSONAL, field);
            field = new SolrInputField(FIELD_CC_ADDR);
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put(FIELD_CC_ADDR, field);
            field = new SolrInputField(FIELD_CC_PLAIN);
            field.setValue(preparation.line, 1.0f);
            inputDocument.put(FIELD_CC_PLAIN, field);

            preparation = new AddressesPreparation(mail.getBcc());
            field = new SolrInputField(FIELD_BCC_PERSONAL);
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put(FIELD_BCC_PERSONAL, field);
            field = new SolrInputField(FIELD_BCC_ADDR);
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put(FIELD_BCC_ADDR, field);
            field = new SolrInputField(FIELD_BCC_PLAIN);
            field.setValue(preparation.line, 1.0f);
            inputDocument.put(FIELD_BCC_PLAIN, field);
        }
        /*
         * Attachment flag
         */
        {
            final SolrInputField field = new SolrInputField(FIELD_ATTACHMENT);
            field.setValue(Boolean.valueOf(mail.hasAttachment()), 1.0f);
            inputDocument.put(FIELD_ATTACHMENT, field);
        }
        /*
         * Write color label
         */
        {
            final SolrInputField field = new SolrInputField(FIELD_COLOR_LABEL);
            field.setValue(Integer.valueOf(mail.getColorLabel()), 1.0f);
            inputDocument.put(FIELD_COLOR_LABEL, field);
        }
        /*
         * Write size
         */
        {
            final SolrInputField field = new SolrInputField(FIELD_SIZE);
            field.setValue(Long.valueOf(mail.getSize()), 1.0f);
            inputDocument.put(FIELD_SIZE, field);
        }
        /*
         * Write date fields
         */
        {
            java.util.Date d = mail.getReceivedDate();
            if (null != d) {
                final SolrInputField field = new SolrInputField(FIELD_RECEIVED_DATE);
                field.setValue(Long.valueOf(d.getTime()), 1.0f);
                inputDocument.put(FIELD_RECEIVED_DATE, field);
            }
            d = mail.getSentDate();
            if (null != d) {
                final SolrInputField field = new SolrInputField(FIELD_SENT_DATE);
                field.setValue(Long.valueOf(d.getTime()), 1.0f);
                inputDocument.put(FIELD_SENT_DATE, field);
            }
        }
        /*
         * Write flags
         */
        {
            final int flags = mail.getFlags();

            SolrInputField field = new SolrInputField(FIELD_FLAG_ANSWERED);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_ANSWERED, field);

            field = new SolrInputField(FIELD_FLAG_DELETED);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_DELETED, field);

            field = new SolrInputField(FIELD_FLAG_DRAFT);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_DRAFT, field);

            field = new SolrInputField(FIELD_FLAG_FLAGGED);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FLAGGED) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_FLAGGED, field);

            field = new SolrInputField(FIELD_FLAG_RECENT);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_RECENT, field);

            field = new SolrInputField(FIELD_FLAG_SEEN);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_SEEN, field);

            field = new SolrInputField(FIELD_FLAG_USER);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_USER, field);

            field = new SolrInputField(FIELD_FLAG_SPAM);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_SPAM, field);

            field = new SolrInputField(FIELD_FLAG_FORWARDED);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_FORWARDED, field);

            field = new SolrInputField(FIELD_FLAG_READ_ACK);
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0), 1.0f);
            inputDocument.put(FIELD_FLAG_READ_ACK, field);
        }
        /*
         * Subject
         */
        {
            final String subject = mail.getSubject();
            SolrInputField field = new SolrInputField(FIELD_SUBJECT_PLAIN);
            field.setValue(subject, 1.0f);
            inputDocument.put(FIELD_SUBJECT_PLAIN, field);

            String language;
            try {
                final Locale detectedLocale = detectLocale(subject);
                if (null == detectedLocale || !isSupportedLocale(detectedLocale)) {
                    language = "en";
                } else {
                    language = detectedLocale.getLanguage();
                }
            } catch (final Exception e) {
                language = "en";
            }
            final String name = FIELD_SUBJECT_PREFIX + language;
            field = new SolrInputField(name);
            field.setValue(subject, 1.0f);
            inputDocument.put(name, field);
        }
        return inputDocument;
    }

    private static boolean isSupportedLocale(final Locale locale) {
        final String language = locale.getLanguage();
        for (final Locale loc : IndexAdapters.KNOWN_LOCALES) {
            if (language.equals(loc.getLanguage())) {
                return true;
            }
        }
        return false;
    }

    private static final class SolrDocumentIterator implements Iterator<SolrInputDocument> {

        private final Iterator<SolrDocument> iterator;

        private final Map<String, MailMessage> mailMap;

        private final SolrTextFillerQueue textFillerQueue;

        protected SolrDocumentIterator(final Collection<SolrDocument> documents, final Map<String, MailMessage> mailMap, final SolrTextFillerQueue textFillerQueue) {
            super();
            this.textFillerQueue = textFillerQueue;
            iterator = documents.iterator();
            this.mailMap = mailMap;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public SolrInputDocument next() {
            final SolrDocument document = iterator.next();
            if (SolrTextFillerQueue.checkSolrDocument(document)) {
                try {
                    textFillerQueue.add(TextFiller.fillerFor(document), true);
                } catch (final OXException e) {
                    // Ignore
                }
            }
            final Map<String, Object> documentFields = document.getFieldValueMap();
            final SolrInputDocument inputDocument = new SolrInputDocument();
            final MailMessage mail = mailMap.get(document.getFieldValue(FIELD_ID));
            {
                final int flags = mail.getFlags();

                SolrInputField field = new SolrInputField(FIELD_FLAG_ANSWERED);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_ANSWERED, field);
                documentFields.remove(FIELD_FLAG_ANSWERED);

                field = new SolrInputField(FIELD_FLAG_DELETED);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_DELETED, field);
                documentFields.remove(FIELD_FLAG_DELETED);

                field = new SolrInputField(FIELD_FLAG_DRAFT);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_DRAFT, field);
                documentFields.remove(FIELD_FLAG_DRAFT);

                field = new SolrInputField(FIELD_FLAG_FLAGGED);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FLAGGED) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_FLAGGED, field);
                documentFields.remove(FIELD_FLAG_FLAGGED);

                field = new SolrInputField(FIELD_FLAG_RECENT);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_RECENT, field);
                documentFields.remove(FIELD_FLAG_RECENT);

                field = new SolrInputField(FIELD_FLAG_SEEN);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_SEEN, field);
                documentFields.remove(FIELD_FLAG_SEEN);

                field = new SolrInputField(FIELD_FLAG_USER);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_USER, field);
                documentFields.remove(FIELD_FLAG_USER);

                field = new SolrInputField(FIELD_FLAG_SPAM);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_SPAM, field);
                documentFields.remove(FIELD_FLAG_SPAM);

                field = new SolrInputField(FIELD_FLAG_FORWARDED);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_FORWARDED, field);
                documentFields.remove(FIELD_FLAG_FORWARDED);

                field = new SolrInputField(FIELD_FLAG_READ_ACK);
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0), 1.0f);
                inputDocument.put(FIELD_FLAG_READ_ACK, field);
                documentFields.remove(FIELD_FLAG_READ_ACK);
            }
            {
                final int colorLabel = mail.getColorLabel();

                final SolrInputField field = new SolrInputField(FIELD_COLOR_LABEL);
                field.setValue(Integer.valueOf(colorLabel), 1.0f);
                inputDocument.put(FIELD_COLOR_LABEL, field);
                documentFields.remove(FIELD_COLOR_LABEL);
            }
            for (final Entry<String, Object> entry : documentFields.entrySet()) {
                final String name = entry.getKey();
                final SolrInputField field = new SolrInputField(name);
                field.setValue(entry.getValue(), 1.0f);
                inputDocument.put(name, field);
            }
            return inputDocument;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    private static final class MailDocumentIterator implements Iterator<SolrInputDocument> {

        private final Iterator<MailMessage> iterator;

        private final Session session;

        private final long now;

        private final List<TextFiller> fillers;

        protected MailDocumentIterator(final Iterator<MailMessage> iterator, final Session session, final long now, final List<TextFiller> fillers) {
            super();
            this.fillers = fillers;
            this.iterator = iterator;
            this.session = session;
            this.now = now;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public SolrInputDocument next() {
            final MailMessage mail = iterator.next();
            final MailUUID uuid = new MailUUID(session.getContextId(), session.getUserId(), mail.getAccountId(), mail.getFolder(), mail.getMailId());
            final SolrInputDocument inputDocument = createDocument(uuid.getUUID(), mail, mail.getAccountId(), session, now);
            if (null != fillers) {
                fillers.add(TextFiller.fillerFor(uuid.getUUID(), mail, session));
            }
            return inputDocument;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    private static final class AddressesPreparation {

        protected final List<String> personalList;

        protected final List<String> addressList;

        protected final String line;

        protected AddressesPreparation(final InternetAddress[] addrs) {
            super();
            if (addrs == null || addrs.length <= 0) {
                personalList = Collections.emptyList();
                addressList = Collections.emptyList();
                line = null;
            } else {
                final List<String> pl = new LinkedList<String>();
                final List<String> al = new LinkedList<String>();
                final StringBuilder lineBuilder = new StringBuilder(256);
                for (int i = 0; i < addrs.length; i++) {
                    final InternetAddress address = addrs[i];
                    final String personal = address.getPersonal();
                    if (!isEmpty(personal)) {
                        pl.add(preparePersonal(personal));
                    }
                    al.add(prepareAddress(address.getAddress()));
                    lineBuilder.append(", ").append(address.toString());
                }
                personalList = pl;
                addressList = al;
                lineBuilder.delete(0, 2);
                line = lineBuilder.toString();
            }
        }

        private static String preparePersonal(final String personal) {
            return MIMEMessageUtility.quotePhrase(MIMEMessageUtility.decodeMultiEncodedHeader(personal), false);
        }

        private static final String DUMMY_DOMAIN = "@unspecified-domain";

        private static String prepareAddress(final String address) {
            final String decoded = MIMEMessageUtility.decodeMultiEncodedHeader(address);
            final int pos = decoded.indexOf(DUMMY_DOMAIN);
            if (pos >= 0) {
                return toIDN(decoded.substring(0, pos));
            }
            return toIDN(decoded);
        }

    }

}
