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
import static com.openexchange.mail.smal.adapter.IndexAdapters.isEmpty;
import static com.openexchange.mail.smal.adapter.solrj.SolrUtils.detectLocale;
import java.io.IOException;
import java.util.ArrayList;
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
import java.util.UUID;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
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
import com.openexchange.mail.smal.adapter.solrj.cache.CommonsHttpSolrServerCache;
import com.openexchange.session.Session;

/**
 * {@link SolrjAdapter}
 * <p>
 * http://wiki.apache.org/solr/Solrj
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrjAdapter implements IndexAdapter {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(SolrjAdapter.class));

    private volatile CommonsHttpSolrServerCache solrServerCache;

    /**
     * Initializes a new {@link SolrjAdapter}.
     */
    public SolrjAdapter() {
        super();
    }

    private IndexUrl indexUrlFor(final Session session, final boolean readWrite) throws OXException {
        final ConfigIndexService configIndexService = SMALServiceLookup.getServiceStatic(ConfigIndexService.class);
        if (readWrite) {
            return configIndexService.getWriteURL(session.getContextId(), session.getUserId(), Types.EMAIL);
        }
        return configIndexService.getReadOnlyURL(session.getContextId(), session.getUserId(), Types.EMAIL);
    }

    private CommonsHttpSolrServer solrServerFor(final Session session, final boolean readWrite) throws OXException {
        return solrServerCache.getSolrServer(indexUrlFor(session, readWrite));
    }

    @Override
    public void start() throws OXException {
        solrServerCache = new CommonsHttpSolrServerCache(100, 300000);
    }

    @Override
    public void stop() throws OXException {
        final CommonsHttpSolrServerCache solrServerCache = this.solrServerCache;
        if (null != solrServerCache) {
            solrServerCache.shutDown();
            this.solrServerCache = null;
        }
    }

    private static final MailFields INDEXABLE_FIELDS = new MailFields(
        MailField.ACCOUNT_NAME,
        MailField.ID,
        MailField.FOLDER_ID,
        MailField.FROM,
        MailField.TO,
        MailField.CC,
        MailField.BCC,
        MailField.FLAGS,
        MailField.SIZE,
        MailField.SUBJECT,
        MailField.RECEIVED_DATE,
        MailField.SENT_DATE);

    @Override
    public MailFields getIndexableFields() throws OXException {
        return INDEXABLE_FIELDS;
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
    public List<MailMessage> search(final String optFullName, final SearchTerm<?> searchTerm, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int optAccountId, final Session session) throws OXException {
        try {
            final CommonsHttpSolrServer solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append("user:").append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append("context:").append(session.getContextId()).append(')');
            if (optAccountId >= 0) {
                queryBuilder.append(" AND (").append("account:").append(optAccountId).append(')');
            }
            if (null != optFullName) {
                queryBuilder.append(" AND (").append("full_name:").append(optFullName).append(')');
            }
            queryBuilder.append(" AND (").append(SearchTerm2Query.searchTerm2Query(searchTerm)).append(')');
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            final QueryResponse queryResponse = solrServer.query(solrQuery);
            final SolrDocumentList results = queryResponse.getResults();
            final int size = results.size();
            final List<MailMessage> mails = new ArrayList<MailMessage>(size);
            for (int i = 0; i < size; i++) {
                final SolrDocument solrDocument = results.get(i);
                mails.add(readDocument(solrDocument));
            }
            return mails;
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static MailMessage readDocument(final SolrDocument document) throws OXException {
        final MailMessage mail = new IDMailMessage(document.getFieldValue("id").toString(), document.getFieldValue("full_name").toString());
        mail.setAccountId(SolrjAdapter.<Integer> getFieldValue("account", document).intValue());
        mail.setSize(SolrjAdapter.<Long> getFieldValue("size", document).longValue());
        mail.setReceivedDate(new Date(SolrjAdapter.<Long> getFieldValue("received_date", document).longValue()));
        mail.setSentDate(new Date(SolrjAdapter.<Long> getFieldValue("sent_date", document).longValue()));
        {
            String addressList = getFieldValue("from_plain", document);
            if (!isEmpty(addressList)) {
                try {
                    mail.addFrom(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addFrom(new PlainTextAddress(addressList));
                }
            }
            addressList = getFieldValue("to_plain", document);
            if (!isEmpty(addressList)) {
                try {
                    mail.addTo(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addTo(new PlainTextAddress(addressList));
                }
            }
            addressList = getFieldValue("cc_plain", document);
            if (!isEmpty(addressList)) {
                try {
                    mail.addCc(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addCc(new PlainTextAddress(addressList));
                }
            }
            addressList = getFieldValue("bcc_plain", document);
            if (!isEmpty(addressList)) {
                try {
                    mail.addBcc(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addBcc(new PlainTextAddress(addressList));
                }
            }
        }
        {
            int flags = 0;
            Boolean b = SolrjAdapter.<Boolean> getFieldValue("flag_answered", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_ANSWERED;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_deleted", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_DELETED;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_draft", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_DRAFT;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_flagged", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_FLAGGED;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_forwarded", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_FORWARDED;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_read_ack", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_READ_ACK;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_recent", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_RECENT;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_seen", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_SEEN;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_spam", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_SPAM;
            }
            b = SolrjAdapter.<Boolean> getFieldValue("flag_user", document);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_USER;
            }
            mail.setFlags(flags);
        }
        // Subject
        final StringBuilder pre = new StringBuilder("subject_");
        for (final Locale l : SolrUtils.KNOWN_LOCALES) {
            pre.setLength(8);
            final String subject = getFieldValue(pre.append(l.getLanguage()).toString(), document);
            if (null != subject) {
                mail.setSubject(subject);
                break;
            }
        }
        // Return mail
        return mail;
    }

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
    public List<MailMessage> getMessages(final String[] optMailIds, final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int accountId, final Session session) throws OXException {
        try {
            final CommonsHttpSolrServer solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append("user:").append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append("context:").append(session.getContextId()).append(')');
            if (accountId >= 0) {
                queryBuilder.append(" AND (").append("account:").append(accountId).append(')');
            }
            if (null != fullName) {
                queryBuilder.append(" AND (").append("full_name:").append(fullName).append(')');
            }
            if (null != optMailIds) {
                final int length = optMailIds.length;
                if (length > 0) {
                    queryBuilder.append(" AND (").append("id:").append(optMailIds[0]);
                    for (int i = 1; i < length; i++) {
                        queryBuilder.append(" OR id:").append(optMailIds[i]);
                    }
                    queryBuilder.append(')');
                }
            }
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            if (null != sortField && null != order) {
                final String name = SORTFIELD2NAME.get(sortField);
                if (null != name) {
                    solrQuery.setSortField(name, OrderDirection.ASC.equals(order) ? ORDER.asc : ORDER.desc);
                }
            }
            final QueryResponse queryResponse = solrServer.query(solrQuery);
            final SolrDocumentList results = queryResponse.getResults();
            final int size = results.size();
            final List<MailMessage> mails = new ArrayList<MailMessage>(size);
            for (int i = 0; i < size; i++) {
                final SolrDocument solrDocument = results.get(i);
                mails.add(readDocument(solrDocument));
            }
            return mails;
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void deleteMessages(final Collection<String> mailIds, final String fullName, final int accountId, final Session session) throws OXException {
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append("user:").append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append("context:").append(session.getContextId()).append(')');
            if (accountId >= 0) {
                queryBuilder.append(" AND (").append("account:").append(accountId).append(')');
            }
            if (null != fullName) {
                queryBuilder.append(" AND (").append("full_name:").append(fullName).append(')');
            }
            if (null != mailIds && !mailIds.isEmpty()) {
                final Iterator<String> iterator = mailIds.iterator();
                queryBuilder.append(" AND (").append("id:").append(iterator.next());
                while (iterator.hasNext()) {
                    queryBuilder.append(" OR id:").append(iterator.next());
                }
                queryBuilder.append(')');
            }
            solrServer.deleteByQuery(queryBuilder.toString());
            solrServer.commit();
        } catch (final SolrServerException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean containsFolder(final String fullName, final int accountId, final Session session) throws OXException {
        try {
            final CommonsHttpSolrServer solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append("user:").append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append("context:").append(session.getContextId()).append(')');
            if (accountId >= 0) {
                queryBuilder.append(" AND (").append("account:").append(accountId).append(')');
            }
            if (null != fullName) {
                queryBuilder.append(" AND (").append("full_name:").append(fullName).append(')');
            }
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            final QueryResponse queryResponse = solrServer.query(solrQuery);
            return !queryResponse.getResults().isEmpty();
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void change(final List<MailMessage> mails, final Session session) throws OXException {
        if (null == mails || mails.isEmpty()) {
            return;
        }
        final int size = mails.size();
        final Map<String, MailMessage> map = new HashMap<String, MailMessage>(size);
        for (final MailMessage mail : mails) {
            map.put(mail.getMailId(), mail);
        }
        CommonsHttpSolrServer solrServer = null;
        boolean rollback = false;
        try {
            solrServer = solrServerFor(session, true);
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append("user:").append(session.getUserId()).append(')');
            queryBuilder.append(" AND (").append("context:").append(session.getContextId()).append(')');
            final int accountId = mails.get(0).getAccountId();
            if (accountId >= 0) {
                queryBuilder.append(" AND (").append("account:").append(accountId).append(')');
            }
            final String fullName = mails.get(0).getFolder();
            if (null != fullName) {
                queryBuilder.append(" AND (").append("full_name:").append(fullName).append(')');
            }
            queryBuilder.append(" AND (").append("id:").append(mails.get(0).getMailId());
            for (int i = 1; i < size; i++) {
                queryBuilder.append(" OR id:").append(mails.get(i).getMailId());
            }
            queryBuilder.append(')');
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            final QueryResponse queryResponse = solrServer.query(solrQuery, METHOD.POST);
            final SolrDocumentList results = queryResponse.getResults();
            rollback = true;
            solrServer.add(new SolrDocumentIterator(results, map));
            solrServer.commit();
        } catch (final SolrServerException e) {
            SolrUtils.rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            SolrUtils.rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            SolrUtils.rollback(rollback ? solrServer : null);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void add(final MailMessage mail, final Session session) throws OXException {
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final String uuid = UUID.randomUUID().toString();
            solrServer.add(createDocument(uuid, mail, mail.getAccountId(), session, System.currentTimeMillis()));
            solrServer.commit();
        } catch (final SolrServerException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void add(final List<MailMessage> mails, final Session session) throws OXException {
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final Iterator<SolrInputDocument> iter = new MailDocumentIterator(mails.iterator(), session, System.currentTimeMillis());
            solrServer.add(iter);
            solrServer.commit();
        } catch (final SolrServerException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final IOException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            SolrUtils.rollback(solrServer);
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    protected static SolrInputDocument createDocument(final String uuid, final MailMessage mail, final int accountId, final Session session, final long stamp) {
        final SolrInputDocument inputDocument = new SolrInputDocument();
        /*
         * Un-analyzed fields
         */
        {
            final SolrInputField field = new SolrInputField("timestamp");
            field.setValue(Long.valueOf(stamp), 1.0f);
            inputDocument.put("timestamp", field);
        }
        {
            final SolrInputField field = new SolrInputField("uuid");
            field.setValue(uuid, 1.0f);
            inputDocument.put("uuid", field);
        }
        {
            final SolrInputField field = new SolrInputField("context");
            field.setValue(Long.valueOf(session.getContextId()), 1.0f);
            inputDocument.put("context", field);
        }
        {
            final SolrInputField field = new SolrInputField("user");
            field.setValue(Long.valueOf(session.getUserId()), 1.0f);
            inputDocument.put("user", field);
        }
        {
            final SolrInputField field = new SolrInputField("account");
            field.setValue(Integer.valueOf(accountId), 1.0f);
            inputDocument.put("account", field);
        }
        {
            final SolrInputField field = new SolrInputField("full_name");
            field.setValue(mail.getFolder(), 1.0f);
            inputDocument.put("full_name", field);
        }
        {
            final SolrInputField field = new SolrInputField("id");
            field.setValue(mail.getMailId(), 1.0f);
            inputDocument.put("id", field);
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
            SolrInputField field = new SolrInputField("from_personal");
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put("from_personal", field);
            field = new SolrInputField("from_addr");
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put("from_addr", field);
            field = new SolrInputField("from_plain");
            field.setValue(mail.getFirstHeader("From"), 1.0f);
            inputDocument.put("from_plain", field);

            preparation = new AddressesPreparation(mail.getTo());
            field = new SolrInputField("to_personal");
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put("to_personal", field);
            field = new SolrInputField("to_addr");
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put("to_addr", field);
            field = new SolrInputField("to_plain");
            field.setValue(mail.getFirstHeader("To"), 1.0f);
            inputDocument.put("to_plain", field);

            preparation = new AddressesPreparation(mail.getCc());
            field = new SolrInputField("cc_personal");
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put("cc_personal", field);
            field = new SolrInputField("cc_addr");
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put("cc_addr", field);
            field = new SolrInputField("cc_plain");
            field.setValue(mail.getFirstHeader("Cc"), 1.0f);
            inputDocument.put("cc_plain", field);

            preparation = new AddressesPreparation(mail.getBcc());
            field = new SolrInputField("bcc_personal");
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put("bcc_personal", field);
            field = new SolrInputField("bcc_addr");
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put("bcc_addr", field);
            field = new SolrInputField("bcc_plain");
            field.setValue(mail.getFirstHeader("Bcc"), 1.0f);
            inputDocument.put("bcc_plain", field);
        }
        /*
         * Write size
         */
        {
            final SolrInputField field = new SolrInputField("size");
            field.setValue(Long.valueOf(mail.getSize()), 1.0f);
            inputDocument.put("size", field);
        }
        /*
         * Write date fields
         */
        {
            java.util.Date d = mail.getReceivedDate();
            if (null != d) {
                final SolrInputField field = new SolrInputField("received_date");
                field.setValue(Long.valueOf(d.getTime()), 1.0f);
                inputDocument.put("received_date", field);
            }
            d = mail.getSentDate();
            if (null != d) {
                final SolrInputField field = new SolrInputField("sent_date");
                field.setValue(Long.valueOf(d.getTime()), 1.0f);
                inputDocument.put("sent_date", field);
            }
        }
        /*
         * Write flags
         */
        {
            final int flags = mail.getFlags();

            SolrInputField field = new SolrInputField("flag_answered");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0), 1.0f);
            inputDocument.put("flag_answered", field);

            field = new SolrInputField("flag_deleted");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0), 1.0f);
            inputDocument.put("flag_deleted", field);

            field = new SolrInputField("flag_draft");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
            inputDocument.put("flag_draft", field);

            field = new SolrInputField("flag_flagged");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
            inputDocument.put("flag_flagged", field);

            field = new SolrInputField("flag_recent");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0), 1.0f);
            inputDocument.put("flag_recent", field);

            field = new SolrInputField("flag_seen");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0), 1.0f);
            inputDocument.put("flag_seen", field);

            field = new SolrInputField("flag_user");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0), 1.0f);
            inputDocument.put("flag_user", field);

            field = new SolrInputField("flag_spam");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0), 1.0f);
            inputDocument.put("flag_spam", field);

            field = new SolrInputField("flag_forwarded");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0), 1.0f);
            inputDocument.put("flag_forwarded", field);

            field = new SolrInputField("flag_read_ack");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0), 1.0f);
            inputDocument.put("flag_read_ack", field);
        }
        /*
         * Subject
         */
        {
            final String subject = mail.getSubject();
            SolrInputField field = new SolrInputField("subject_plain");
            field.setValue(subject, 1.0f);
            inputDocument.put("subject_plain", field);

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
            final String name = "subject_" + language;
            field = new SolrInputField(name);
            field.setValue(subject, 1.0f);
            inputDocument.put(name, field);
        }
        return inputDocument;
    }

    private static boolean isSupportedLocale(final Locale locale) {
        final String language = locale.getLanguage();
        for (final Locale loc : SolrUtils.KNOWN_LOCALES) {
            if (language.equals(loc.getLanguage())) {
                return true;
            }
        }
        return false;
    }

    private static final class SolrDocumentIterator implements Iterator<SolrInputDocument> {

        private final Iterator<SolrDocument> iterator;

        private final Map<String, MailMessage> mailMap;

        protected SolrDocumentIterator(final SolrDocumentList results, final Map<String, MailMessage> mailMap) {
            super();
            iterator = results.iterator();
            this.mailMap = mailMap;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public SolrInputDocument next() {
            final SolrDocument document = iterator.next();
            final Set<Entry<String, Object>> documentFields = document.entrySet();
            final SolrInputDocument inputDocument = new SolrInputDocument();
            {
                final int flags = mailMap.get(document.getFieldValue("id")).getFlags();

                SolrInputField field = new SolrInputField("flag_answered");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0), 1.0f);
                inputDocument.put("flag_answered", field);
                documentFields.remove("flag_answered");

                field = new SolrInputField("flag_deleted");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0), 1.0f);
                inputDocument.put("flag_deleted", field);
                documentFields.remove("flag_deleted");

                field = new SolrInputField("flag_draft");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
                inputDocument.put("flag_draft", field);
                documentFields.remove("flag_draft");

                field = new SolrInputField("flag_flagged");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
                inputDocument.put("flag_flagged", field);
                documentFields.remove("flag_flagged");

                field = new SolrInputField("flag_recent");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0), 1.0f);
                inputDocument.put("flag_recent", field);
                documentFields.remove("flag_recent");

                field = new SolrInputField("flag_seen");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0), 1.0f);
                inputDocument.put("flag_seen", field);
                documentFields.remove("flag_seen");

                field = new SolrInputField("flag_user");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0), 1.0f);
                inputDocument.put("flag_user", field);
                documentFields.remove("flag_user");

                field = new SolrInputField("flag_spam");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0), 1.0f);
                inputDocument.put("flag_spam", field);
                documentFields.remove("flag_spam");

                field = new SolrInputField("flag_forwarded");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0), 1.0f);
                inputDocument.put("flag_forwarded", field);
                documentFields.remove("flag_forwarded");

                field = new SolrInputField("flag_read_ack");
                field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0), 1.0f);
                inputDocument.put("flag_read_ack", field);
                documentFields.remove("flag_read_ack");
            }
            for (final Entry<String, Object> entry : documentFields) {
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

        protected MailDocumentIterator(final Iterator<MailMessage> iterator, final Session session, final long now) {
            super();
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
            return createDocument(UUID.randomUUID().toString(), mail, mail.getAccountId(), session, now);
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    private static final class AddressesPreparation {

        protected final List<String> personalList;

        protected final List<String> addressList;

        protected AddressesPreparation(final InternetAddress[] addrs) {
            super();
            if (addrs == null || addrs.length <= 0) {
                personalList = Collections.emptyList();
                addressList = Collections.emptyList();
            } else {
                final List<String> pl = new LinkedList<String>();
                final List<String> al = new LinkedList<String>();
                for (int i = 0; i < addrs.length; i++) {
                    final InternetAddress address = addrs[i];
                    final String personal = address.getPersonal();
                    if (!isEmpty(personal)) {
                        pl.add(preparePersonal(personal));
                    }
                    al.add(prepareAddress(address.getAddress()));
                }
                personalList = pl;
                addressList = al;
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

    private static EnumMap<MailSortField, String> SORTFIELD2NAME;

    static {
        final EnumMap<MailSortField, String> map = new EnumMap<MailSortField, String>(MailSortField.class);
        map.put(MailSortField.ACCOUNT_NAME, "account");
        map.put(MailSortField.CC, "cc_plain");
        map.put(MailSortField.COLOR_LABEL, "color_label");
        map.put(MailSortField.FLAG_SEEN, "flag_seen");
        map.put(MailSortField.FROM, "from_plain");
        map.put(MailSortField.RECEIVED_DATE, "received_date");
        map.put(MailSortField.SENT_DATE, "sent_date");
        map.put(MailSortField.SIZE, "size");
        map.put(MailSortField.SUBJECT, "subject_plain");
        map.put(MailSortField.TO, "to_plain");
    }

}
