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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.mail.internet.InternetAddress;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.session.Session;


/**
 * {@link SolrjAdapter}
 * <p>
 * http://wiki.apache.org/solr/Solrj
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrjAdapter implements IndexAdapter {

    private volatile CommonsHttpSolrServer server;

    /**
     * Initializes a new {@link SolrjAdapter}.
     */
    public SolrjAdapter() {
        super();
        
    }

    @Override
    public void start() throws OXException {
        try {
            final String url = "http://localhost:8983/solr";
            final CommonsHttpSolrServer server = new CommonsHttpSolrServer(url);
            server.setSoTimeout(1000);  // socket read timeout
            server.setConnectionTimeout(100);
            server.setDefaultMaxConnectionsPerHost(100);
            server.setMaxTotalConnections(100);
            server.setFollowRedirects(false);  // defaults to false
            // allowCompression defaults to false.
            // Server side must support gzip or deflate for this to have any effect.
            server.setAllowCompression(true);
            server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
            server.setParser(new XMLResponseParser()); // binary parser is used by default
            this.server = server;
        } catch (final MalformedURLException e) {
            
        }
    }

    @Override
    public void stop() throws OXException {
        final CommonsHttpSolrServer server = this.server;
        if (null != server) {
            final HttpClient httpClient = server.getHttpClient();
            ((MultiThreadedHttpConnectionManager) httpClient.getHttpConnectionManager()).shutdown();
            this.server = null;
        }
    }

    @Override
    public MailFields getIndexableFields() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#onSessionAdd(com.openexchange.session.Session)
     */
    @Override
    public void onSessionAdd(final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#onSessionGone(com.openexchange.session.Session)
     */
    @Override
    public void onSessionGone(final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#search(java.lang.String, com.openexchange.mail.search.SearchTerm, com.openexchange.mail.MailSortField, com.openexchange.mail.OrderDirection, com.openexchange.mail.MailField[], int, com.openexchange.session.Session)
     */
    @Override
    public List<MailMessage> search(final String optFullName, final SearchTerm<?> searchTerm, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int optAccountId, final Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#getMessages(java.lang.String[], java.lang.String, com.openexchange.mail.MailSortField, com.openexchange.mail.OrderDirection, com.openexchange.mail.MailField[], int, com.openexchange.session.Session)
     */
    @Override
    public List<MailMessage> getMessages(final String[] optMailIds, final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int accountId, final Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#deleteMessages(java.util.Collection, java.lang.String, int, com.openexchange.session.Session)
     */
    @Override
    public void deleteMessages(final Collection<String> mailIds, final String fullName, final int accountId, final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#containsFolder(java.lang.String, int, com.openexchange.session.Session)
     */
    @Override
    public boolean containsFolder(final String fullName, final int accountId, final Session session) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#change(java.util.Collection, com.openexchange.session.Session)
     */
    @Override
    public void change(final Collection<MailMessage> mails, final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#add(com.openexchange.mail.dataobjects.MailMessage, com.openexchange.session.Session)
     */
    @Override
    public void add(final MailMessage mail, final Session session) throws OXException {
        try {
            final CommonsHttpSolrServer server = this.server;

            final String uuid = UUID.randomUUID().toString();
            final SolrInputDocument inputDocument = createDocument(uuid, mail, mail.getAccountId(), session, System.currentTimeMillis(), detectLocale(mail.getSubject()));

            server.add(inputDocument);
        } catch (final SolrServerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.adapter.IndexAdapter#add(java.util.Collection, com.openexchange.session.Session)
     */
    @Override
    public void add(final Collection<MailMessage> mails, final Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    private static SolrInputDocument createDocument(final String uuid, final MailMessage mail, final int accountId, final Session session, final long stamp, final Locale locale) {
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
         * 
         * "From"
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
            SolrInputField field = new SolrInputField("from");
            String[] array = toStringArray(mail.getFrom());
            field.setValue(array, 1.0f);
            inputDocument.put("from", field);

            field = new SolrInputField("to");
            array = toStringArray(mail.getTo());
            field.setValue(array, 1.0f);
            inputDocument.put("to", field);
            
            field = new SolrInputField("cc");
            array = toStringArray(mail.getCc());
            field.setValue(array, 1.0f);
            inputDocument.put("cc", field);
            
            field = new SolrInputField("bcc");
            array = toStringArray(mail.getBcc());
            field.setValue(array, 1.0f);
            inputDocument.put("bcc", field);
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

            SolrInputField field = new SolrInputField("field_answered");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0), 1.0f);
            inputDocument.put("field_answered", field);

            field = new SolrInputField("field_deleted");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0), 1.0f);
            inputDocument.put("field_deleted", field);

            field = new SolrInputField("field_draft");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
            inputDocument.put("field_draft", field);

            field = new SolrInputField("field_flagged");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0), 1.0f);
            inputDocument.put("field_flagged", field);

            field = new SolrInputField("field_recent");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0), 1.0f);
            inputDocument.put("field_recent", field);

            field = new SolrInputField("field_seen");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0), 1.0f);
            inputDocument.put("field_seen", field);

            field = new SolrInputField("field_user");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0), 1.0f);
            inputDocument.put("field_user", field);

            field = new SolrInputField("field_spam");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0), 1.0f);
            inputDocument.put("field_spam", field);

            field = new SolrInputField("field_forwarded");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0), 1.0f);
            inputDocument.put("field_forwarded", field);

            field = new SolrInputField("field_read_ack");
            field.setValue(Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0), 1.0f);
            inputDocument.put("field_read_ack", field);
        }
        /*
         * Subject
         */
        {
            final String name = "subject_" + locale.getLanguage();
            final SolrInputField field = new SolrInputField(name);
            field.setValue(Long.valueOf(mail.getSize()), 1.0f);
            inputDocument.put(name, field);
        }
        return inputDocument;
    }

    private static String[] toStringArray(final InternetAddress[] addrs) {
        if (addrs == null || addrs.length <= 0) {
            return null;
        }
        final String[] sa = new String[addrs.length];
        for (int i = 0; i < addrs.length; i++) {
            sa[i] = addrs[i].toUnicodeString();
        }
        return sa;
    }

    private static Locale detectLocale(final String str) {
        return null;
    }

}
