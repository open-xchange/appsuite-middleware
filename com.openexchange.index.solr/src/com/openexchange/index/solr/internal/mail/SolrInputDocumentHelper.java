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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.mail.MailFillers.MailFiller;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link SolrInputDocumentHelper} - Helper for <code>SolrInputDocument</code> to <code>MailMessage</code> conversion and vice versa.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrInputDocumentHelper {

    private static final SolrInputDocumentHelper INSTANCE = new SolrInputDocumentHelper();

    /**
     * Gets the instance
     * 
     * @return The instance
     */
    public static SolrInputDocumentHelper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link SolrInputDocumentHelper}.
     */
    private SolrInputDocumentHelper() {
        super();
    }

    /**
     * Reads a <code>IndexDocument&lt;MailMessage&gt;</code> from given <code>SolrDocument</code>.
     * 
     * @param document The Solr document
     * @param mailFillers The fillers to use
     * @return The filled <code>IndexDocument&lt;MailMessage&gt;</code> instance
     * @throws OXException If reading <code>MailMessage</code> from given <code>SolrDocument</code> fails
     */
    public IndexDocument<MailMessage> readDocument(final SolrDocument document, final List<MailFiller> mailFillers) throws OXException {
        /*
         * Parse id, full name and account id
         */
        final MailMessage mail = new IDMailMessage();
        final String idField = SolrMailField.ID.solrName();
        final String fullNameField = SolrMailField.FULL_NAME.solrName();
        final String accountField = SolrMailField.ACCOUNT.solrName();
        if (idField != null && document.containsKey(idField)) {
            mail.setMailId(document.getFieldValue(idField).toString());
        }
        if (fullNameField != null && document.containsKey(fullNameField)) {
            mail.setFolder(document.getFieldValue(fullNameField).toString());
        }
        if (accountField != null && document.containsKey(accountField)) {           
            try {
                String fieldValue = (String) MailFillers.getFieldValue(accountField, document);
                mail.setAccountId(Integer.parseInt(fieldValue));
            } catch (NumberFormatException e) {
                // ignore
            } catch (ClassCastException e) {
                // ignore
            }
        }
        /*
         * Iterate mail fillers
         */
        for (final MailFiller mailFiller : mailFillers) {
            mailFiller.fill(mail, document);
        }

        return new StandardIndexDocument<MailMessage>(mail);
    }

    /**
     * Gets the {@link SolrInputDocument} for specified {@link MailMessage} instance.
     * 
     * @param mail The mail message
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The newly created {@link SolrInputDocument} for specified {@link MailMessage} instance
     */
    public SolrInputDocument inputDocumentFor(final MailMessage mail, final int userId, final int contextId) {
        final int accountId = mail.getAccountId();
        final MailUUID uuid = new MailUUID(contextId, userId, accountId, mail.getFolder(), mail.getMailId());
        return createDocument(uuid.toString(), mail, accountId, userId, contextId, System.currentTimeMillis());
    }

    public List<SolrInputDocument> inputDocumentsFor(final List<IndexDocument<MailMessage>> messages, final int userId, final int contextId) {
        final List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
        for (final IndexDocument<MailMessage> message : messages) {
            final MailMessage mail = message.getObject();
            documents.add(inputDocumentFor(mail, userId, contextId));
        }

        return documents;
    }
    
    private static List<Object> createAddressHeader(final InternetAddress[] addrs) {
        if (addrs == null || addrs.length == 0) {
            return null;
        }
        final List<Object> retval = new ArrayList<Object>(addrs.length);
        for (final InternetAddress addr : addrs) {
            if (addr instanceof QuotedInternetAddress) {
                retval.add(((QuotedInternetAddress) addr).toUnicodeString());
            } else {
                String quoted;
                try {
                    quoted = new QuotedInternetAddress(addr.toUnicodeString()).toUnicodeString();
                } catch (final AddressException e) {
                    quoted = addr.toUnicodeString();
                }
                
                retval.add(quoted);
            }
        }
        return retval;
    }

    private static SolrInputDocument createDocument(final String uuid, final MailMessage mail, final int accountId, final int userId, final int contextId, final long stamp) {
        final SolrInputDocument inputDocument = new SolrInputDocument();

        /*
         * Environmental fields
         */
        setFieldInDocument(inputDocument, SolrMailField.TIMESTAMP, stamp);
        setFieldInDocument(inputDocument, SolrMailField.UUID, uuid);
        setFieldInDocument(inputDocument, SolrMailField.ACCOUNT, String.valueOf(accountId));

        /*
         * Envelope data
         */
        setFieldInDocument(inputDocument, SolrMailField.FULL_NAME, mail.getFolder());
        setFieldInDocument(inputDocument, SolrMailField.ID, mail.getMailId());        
        addFieldInDocument(inputDocument, SolrMailField.FROM, createAddressHeader(mail.getFrom()));        
        addFieldInDocument(inputDocument, SolrMailField.TO, createAddressHeader(mail.getTo()));
        addFieldInDocument(inputDocument, SolrMailField.CC, createAddressHeader(mail.getCc()));
        addFieldInDocument(inputDocument, SolrMailField.BCC, createAddressHeader(mail.getBcc()));        
        setFieldInDocument(inputDocument, SolrMailField.ATTACHMENT, mail.hasAttachment());
        setFieldInDocument(inputDocument, SolrMailField.COLOR_LABEL, mail.getColorLabel());
        setFieldInDocument(inputDocument, SolrMailField.SIZE, mail.getSize());
        setFieldInDocument(inputDocument, SolrMailField.RECEIVED_DATE, mail.getReceivedDate() == null ? null : mail.getReceivedDate().getTime());
        setFieldInDocument(inputDocument, SolrMailField.SENT_DATE, mail.getSentDate() == null ? null : mail.getSentDate().getTime());

        /*
         * Flags
         */
        final int flags = mail.getFlags();
        setFieldInDocument(inputDocument, SolrMailField.FLAG_ANSWERED, Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_DELETED, Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_DRAFT, Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_FLAGGED, Boolean.valueOf((flags & MailMessage.FLAG_FLAGGED) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_RECENT, Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_SEEN, Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_USER, Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_SPAM, Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_FORWARDED, Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0));
        setFieldInDocument(inputDocument, SolrMailField.FLAG_READ_ACK, Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0));

        /*
         * User flags
         */
        final String[] userFlags = mail.getUserFlags();
        if (null != userFlags && userFlags.length > 0) {
            setFieldInDocument(inputDocument, SolrMailField.USER_FLAGS, Arrays.asList(userFlags));
        }

        /*
         * Subject
         */
        setFieldInDocument(inputDocument, SolrMailField.SUBJECT, mail.getSubject());

        return inputDocument;
    }

    public static void setFieldInDocument(final SolrInputDocument inputDocument, final SolrMailField field, final Object value) {
        final String fieldName = field.solrName();
        if (fieldName != null && value != null) {
            inputDocument.remove(fieldName);
            inputDocument.addField(field.solrName(), value);
        }
    }
    
    public static void addFieldInDocument(final SolrInputDocument inputDocument, final SolrMailField field, final List<Object> values) {
        final String fieldName = field.solrName();
        if (fieldName != null && values != null && !values.isEmpty()) {
            inputDocument.addField(field.solrName(), values);
        }
    }
}
