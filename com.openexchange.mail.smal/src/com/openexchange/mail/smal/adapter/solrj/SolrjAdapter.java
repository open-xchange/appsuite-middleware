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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.mail.internet.InternetAddress;
import org.apache.solr.client.solrj.SolrQuery;
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
import com.openexchange.langdetect.LanguageDetectionService;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.SMALExceptionCodes;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.mail.smal.adapter.solrj.cache.CommonsHttpSolrServerCache;
import com.openexchange.server.ServiceExceptionCode;
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

    private void rollback(final CommonsHttpSolrServer solrServer) {
        if (null != solrServer) {
            try {
                solrServer.rollback();
            } catch (final SolrServerException e) {
                LOG.warn("Commit to Solr server failed.", e);
            } catch (final IOException e) {
                LOG.warn("Commit to Solr server failed.", e);
            } catch (final RuntimeException e) {
                LOG.warn("Commit to Solr server failed.", e);
            }
        }
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
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
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
            for (int i = 0; i < size; i++) {
                final SolrDocument solrDocument = results.get(i);
                
            }
            
        } catch (final SolrServerException e) {
            throw SMALExceptionCodes.INDEX_FAULT.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        // TODO Auto-generated method stub
        return null;
    }

    private MailMessage readDocument() {
        final MailMessage mail = new IDMailMessage(null, null);
        
        return mail;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#getMessages(java.lang.String[], java.lang.String,
     * com.openexchange.mail.MailSortField, com.openexchange.mail.OrderDirection, com.openexchange.mail.MailField[], int,
     * com.openexchange.session.Session)
     */
    @Override
    public List<MailMessage> getMessages(final String[] optMailIds, final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int accountId, final Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#deleteMessages(java.util.Collection, java.lang.String, int,
     * com.openexchange.session.Session)
     */
    @Override
    public void deleteMessages(final Collection<String> mailIds, final String fullName, final int accountId, final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#containsFolder(java.lang.String, int, com.openexchange.session.Session)
     */
    @Override
    public boolean containsFolder(final String fullName, final int accountId, final Session session) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void change(final Collection<MailMessage> mails, final Session session) throws OXException {
        if (null == mails || mails.isEmpty()) {
            return;
        }
        final List<MailMessage> list = mails instanceof List ? (List) mails : new ArrayList<MailMessage>(mails);
        final Map<String, MailMessage> map = new HashMap<String, MailMessage>(list.size());
        for (final MailMessage mail : list) {
            map.put(mail.getMailId(), mail);
        }
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("context", session.getContextId());
            solrQuery.set("user", session.getUserId());
            solrQuery.set("account", list.get(0).getAccountId());
            solrQuery.set("full_name", list.get(0).getFolder());
            {
                final List<String> ids = new ArrayList<String>(list.size());
                for (final MailMessage mail : mails) {
                    ids.add(mail.getMailId());
                }
                solrQuery.add("id", ids.toArray(new String[ids.size()]));
            }
            final QueryResponse queryResponse = solrServer.query(solrQuery, METHOD.POST);
            final SolrDocumentList results = queryResponse.getResults();
            final int size = results.size();
            for (int i = 0; i < size; i++) {
                final SolrDocument solrDocument = results.get(i);
                final SolrInputDocument inputDocument = new SolrInputDocument();
                {
                    final int flags = map.get(solrDocument.getFieldValue("id")).getFlags();

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
                for (final Entry<String, Object> entry : solrDocument.entrySet()) {
                    final String name = entry.getKey();
                    if (!name.startsWith("flag_")) {
                        final SolrInputField field = new SolrInputField(name);
                        field.setValue(entry.getValue(), 1.0f);
                        inputDocument.put(name, field);
                    }
                }
                solrServer.add(inputDocument);
            }
            solrServer.commit();
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
    public void add(final MailMessage mail, final Session session) throws OXException {
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final String uuid = UUID.randomUUID().toString();
            solrServer.add(createDocument(uuid, mail, mail.getAccountId(), session, System.currentTimeMillis()));
            solrServer.commit();
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
    public void add(final Collection<MailMessage> mails, final Session session) throws OXException {
        CommonsHttpSolrServer solrServer = null;
        try {
            solrServer = solrServerFor(session, true);
            final long now = System.currentTimeMillis();
            for (final MailMessage mail : mails) {
                final String uuid = UUID.randomUUID().toString();
                solrServer.add(createDocument(uuid, mail, mail.getAccountId(), session, now));
            }
            solrServer.commit();
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

    private static SolrInputDocument createDocument(final String uuid, final MailMessage mail, final int accountId, final Session session, final long stamp) {
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

            preparation = new AddressesPreparation(mail.getTo());
            field = new SolrInputField("to_personal");
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put("to_personal", field);
            field = new SolrInputField("to_addr");
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put("to_addr", field);

            preparation = new AddressesPreparation(mail.getCc());
            field = new SolrInputField("cc_personal");
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put("cc_personal", field);
            field = new SolrInputField("cc_addr");
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put("cc_addr", field);

            preparation = new AddressesPreparation(mail.getBcc());
            field = new SolrInputField("bcc_personal");
            field.setValue(preparation.personalList, 1.0f);
            inputDocument.put("bcc_personal", field);
            field = new SolrInputField("bcc_addr");
            field.setValue(preparation.addressList, 1.0f);
            inputDocument.put("bcc_addr", field);
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
            final SolrInputField field = new SolrInputField(name);
            field.setValue(subject, 1.0f);
            inputDocument.put(name, field);
        }
        return inputDocument;
    }

    private static Locale detectLocale(final String str) throws OXException {
        try {
            return SMALServiceLookup.getServiceStatic(LanguageDetectionService.class).findLanguages(str).get(0);
        } catch (final IllegalStateException e) {
            // Missing service
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, LanguageDetectionService.class.getName());
        }
    }

    private static final Set<Locale> KNOWN_LOCALES;

    static {
        final Set<Locale> set = new HashSet<Locale>(10);
        set.add(new Locale("en"));
        set.add(new Locale("de"));
        set.add(new Locale("fr"));
        set.add(new Locale("nl"));
        set.add(new Locale("sv"));
        set.add(new Locale("es"));
        set.add(new Locale("ja"));
        set.add(new Locale("pl"));
        set.add(new Locale("it"));
        set.add(new Locale("zh"));
        set.add(new Locale("hu"));
        set.add(new Locale("sk"));
        set.add(new Locale("cs"));
        set.add(new Locale("cs"));
        KNOWN_LOCALES = Collections.unmodifiableSet(set);
    }

    private static boolean isSupportedLocale(final Locale locale) {
        final String language = locale.getLanguage();
        for (final Locale loc : KNOWN_LOCALES) {
            if (language.equals(loc.getLanguage())) {
                return true;
            }
        }
        return false;
    }

    private static final class AddressesPreparation {

        protected final String personalList;

        protected final String addressList;

        protected AddressesPreparation(final InternetAddress[] addrs) {
            super();
            if (addrs == null || addrs.length <= 0) {
                personalList = "";
                addressList = "";
            } else {
                final StringBuilder pl = new StringBuilder(64);
                final StringBuilder al = new StringBuilder(128);
                for (int i = 0; i < addrs.length; i++) {
                    final InternetAddress address = addrs[i];
                    final String personal = address.getPersonal();
                    if (!isEmpty(personal)) {
                        pl.append(',').append(preparePersonal(personal));
                    }
                    al.append(',').append(preparePersonal(address.getAddress()));
                }
                pl.deleteCharAt(0);
                al.deleteCharAt(0);
                personalList = pl.toString();
                addressList = al.toString();
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
