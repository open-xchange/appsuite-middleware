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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.converter.AbstractDocumentConverter;
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.text.TextFinder;


/**
 * {@link SolrMailDocumentConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrMailDocumentConverter extends AbstractDocumentConverter<MailMessage> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SolrMailDocumentConverter.class);


    public SolrMailDocumentConverter(FieldConfiguration fieldConfig) {
        super(fieldConfig);
    }

    @Override
    public IndexDocument<MailMessage> convert(SolrDocument document) throws OXException {
        return convertInternal(document);
    }

    @Override
    public IndexDocument<MailMessage> convert(SolrDocument document, Map<String, List<String>> highlightedFields) throws OXException {
        StandardIndexDocument<MailMessage> indexDocument = convertInternal(document);
        addHighlighting(indexDocument, highlightedFields);

        return indexDocument;
    }

    @Override
    public IndexResult<MailMessage> createIndexResult(long numFound, List<IndexDocument<MailMessage>> documents, Map<IndexField, Map<String, Long>> facetCounts) throws OXException {
        return new SolrIndexResult<MailMessage>(numFound, documents, facetCounts);
    }

    public SolrInputDocument convert(int contextId, int userId, IndexDocument<MailMessage> document) throws OXException {
        MailMessage mail = document.getObject();
        int accountId = mail.getAccountId();
        MailUUID uuid = MailUUID.newUUID(contextId, userId, accountId, mail.getFolder(), mail.getMailId());
        SolrInputDocument inputDocument = new SolrInputDocument();
        /*
         * Environmental fields
         */
        setFieldInDocument(inputDocument, MailIndexField.TIMESTAMP, System.currentTimeMillis());
        setFieldInDocument(inputDocument, MailIndexField.UUID, uuid.toString());
        setFieldInDocument(inputDocument, MailIndexField.ACCOUNT, String.valueOf(accountId));

        /*
         * Envelope data
         */
        setFieldInDocument(inputDocument, MailIndexField.FULL_NAME, mail.getFolder());
        setFieldInDocument(inputDocument, MailIndexField.ID, mail.getMailId());
        addFieldInDocument(inputDocument, MailIndexField.FROM, createAddressHeader(mail.getFrom()));
        addFieldInDocument(inputDocument, MailIndexField.TO, createAddressHeader(mail.getTo()));
        addFieldInDocument(inputDocument, MailIndexField.CC, createAddressHeader(mail.getCc()));
        addFieldInDocument(inputDocument, MailIndexField.BCC, createAddressHeader(mail.getBcc()));
        setFieldInDocument(inputDocument, MailIndexField.ATTACHMENT, mail.hasAttachment());
        setFieldInDocument(inputDocument, MailIndexField.COLOR_LABEL, mail.getColorLabel());
        setFieldInDocument(inputDocument, MailIndexField.SIZE, mail.getSize());
        setFieldInDocument(inputDocument, MailIndexField.RECEIVED_DATE, mail.getReceivedDate() == null ? null : mail.getReceivedDate().getTime());
        setFieldInDocument(inputDocument, MailIndexField.SENT_DATE, mail.getSentDate() == null ? null : mail.getSentDate().getTime());

        /*
         * Flags
         */
        final int flags = mail.getFlags();
        setFieldInDocument(inputDocument, MailIndexField.FLAG_ANSWERED, Boolean.valueOf((flags & MailMessage.FLAG_ANSWERED) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_DELETED, Boolean.valueOf((flags & MailMessage.FLAG_DELETED) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_DRAFT, Boolean.valueOf((flags & MailMessage.FLAG_DRAFT) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_FLAGGED, Boolean.valueOf((flags & MailMessage.FLAG_FLAGGED) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_RECENT, Boolean.valueOf((flags & MailMessage.FLAG_RECENT) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_SEEN, Boolean.valueOf((flags & MailMessage.FLAG_SEEN) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_USER, Boolean.valueOf((flags & MailMessage.FLAG_USER) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_SPAM, Boolean.valueOf((flags & MailMessage.FLAG_SPAM) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_FORWARDED, Boolean.valueOf((flags & MailMessage.FLAG_FORWARDED) > 0));
        setFieldInDocument(inputDocument, MailIndexField.FLAG_READ_ACK, Boolean.valueOf((flags & MailMessage.FLAG_READ_ACK) > 0));

        /*
         * User flags
         */
        final String[] userFlags = mail.getUserFlags();
        if (null != userFlags && userFlags.length > 0) {
            setFieldInDocument(inputDocument, MailIndexField.USER_FLAGS, Arrays.asList(userFlags));
        }

        /*
         * Subject
         */
        setFieldInDocument(inputDocument, MailIndexField.SUBJECT, mail.getSubject());

        String text = null;
        try {
            if (mail instanceof ContentAwareMailMessage) {
                ContentAwareMailMessage contentAwareMessage = (ContentAwareMailMessage) mail;
                text = contentAwareMessage.getPrimaryContent();
                if (text == null) {
                    TextFinder textFinder = new TextFinder();
                    text = textFinder.getText(mail);
                }
            } else {
                TextFinder textFinder = new TextFinder();
                text = textFinder.getText(mail);
            }
        } catch (Throwable t) {
            LOG.warn("Error during text extraction. Setting content to null.", t);
        }

        if (null != text) {
            String contentField = fieldConfig.getRawField(MailIndexField.CONTENT);
            if (contentField != null) {
                inputDocument.setField(contentField, text);
            }
        }

        String contentFlagField = fieldConfig.getRawField(MailIndexField.CONTENT_FLAG);
        if (contentFlagField != null) {
            inputDocument.setField(contentFlagField, Boolean.TRUE);
        }

        return inputDocument;
    }

    private StandardIndexDocument<MailMessage> convertInternal(SolrDocument document) throws OXException {
        MailMessage mail = new IDMailMessage();
        String idField = fieldConfig.getRawField(MailIndexField.ID);
        final String fullNameField = fieldConfig.getRawField(MailIndexField.FULL_NAME);
        final String accountField = fieldConfig.getRawField(MailIndexField.ACCOUNT);
        if (idField != null && document.containsKey(idField)) {
            mail.setMailId(document.getFieldValue(idField).toString());
        }
        if (fullNameField != null && document.containsKey(fullNameField)) {
            mail.setFolder(document.getFieldValue(fullNameField).toString());
        }
        if (accountField != null && document.containsKey(accountField)) {
            try {
                String fieldValue = (String) document.get(accountField);
                mail.setAccountId(Integer.parseInt(fieldValue));
            } catch (NumberFormatException e) {
                // ignore
            } catch (ClassCastException e) {
                // ignore
            }
        }

        mail.addFrom(calculateAddressField(MailIndexField.FROM, document));
        mail.addTo(calculateAddressField(MailIndexField.TO, document));
        mail.addCc(calculateAddressField(MailIndexField.CC, document));
        mail.addBcc(calculateAddressField(MailIndexField.BCC, document));

        Boolean hasAttachment = getFieldValue(MailIndexField.ATTACHMENT, document);
        if (hasAttachment != null) {
            mail.setHasAttachment(hasAttachment.booleanValue());
        }

        Integer colorLabel = getFieldValue(MailIndexField.COLOR_LABEL, document);
        if (colorLabel != null) {
            mail.setColorLabel(colorLabel.intValue());
        }

        Long size = getFieldValue(MailIndexField.SIZE, document);
        if (size != null) {
            mail.setSize(size.longValue());
        }

        Long sentDate = getFieldValue(MailIndexField.SENT_DATE, document);
        if (sentDate != null) {
            mail.setSentDate(new Date(sentDate.longValue()));
        }

        Long receivedDate = getFieldValue(MailIndexField.RECEIVED_DATE, document);
        if (receivedDate != null) {
            mail.setReceivedDate(new Date(receivedDate.longValue()));
        }

        setFlags(mail, document);

        String subject = getFieldValue(MailIndexField.SUBJECT, document);
        if (subject != null) {
            mail.setSubject(subject);
        }

        String documentId = String.valueOf(document.getFieldValue(fieldConfig.getUUIDField()));
        return new StandardIndexDocument<MailMessage>(documentId, mail);
    }

    private void setFlags(MailMessage mail, SolrDocument document) throws OXException {
        int flags = 0;
        Boolean b = getFieldValue(MailIndexField.FLAG_ANSWERED, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_ANSWERED;
        }
        b = getFieldValue(MailIndexField.FLAG_DELETED, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_DELETED;
        }
        b = getFieldValue(MailIndexField.FLAG_DRAFT, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_DRAFT;
        }
        b = getFieldValue(MailIndexField.FLAG_FLAGGED, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_FLAGGED;
        }
        b = getFieldValue(MailIndexField.FLAG_FORWARDED, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_FORWARDED;
        }
        b = getFieldValue(MailIndexField.FLAG_READ_ACK, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_READ_ACK;
        }
        b = getFieldValue(MailIndexField.FLAG_RECENT, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_RECENT;
        }
        b = getFieldValue(MailIndexField.FLAG_SEEN, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_SEEN;
        }
        b = getFieldValue(MailIndexField.FLAG_SPAM, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_SPAM;
        }
        b = getFieldValue(MailIndexField.FLAG_USER, document);
        if (b != null && b.booleanValue()) {
            flags |= MailMessage.FLAG_USER;
        }
        mail.setFlags(flags);

        Object userFlagsObject = getFieldValue(MailIndexField.USER_FLAGS, document);
        if (userFlagsObject != null) {
            if (userFlagsObject instanceof String) {
                mail.addUserFlag((String) userFlagsObject);
            } else {
                try {
                    List<String> flagList = (List<String>) userFlagsObject;
                    mail.addUserFlags(flagList.toArray(new String[flagList.size()]));
                } catch (ClassCastException e) {
                    // ignore
                }
            }
        }
    }

    private InternetAddress[] calculateAddressField(MailIndexField indexField, SolrDocument document) throws OXException {
        List<String> addressList = getFieldValue(indexField, document);
        if (addressList == null || addressList.isEmpty()) {
            return new InternetAddress[0];
        }

        InternetAddress[] addrs = new InternetAddress[addressList.size()];
        if (addressList != null && !addressList.isEmpty()) {
            for (int i = 0; i < addrs.length; i++) {
                String addr = addressList.get(i);
                try {
                    addrs[i] = new QuotedInternetAddress(addr, false);
                } catch (AddressException e) {
                    addrs[i] = new PlainTextAddress(addr);
                }
            }
        }

        return addrs;
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

}
