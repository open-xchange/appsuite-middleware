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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.mail.internet.AddressException;
import org.apache.solr.common.SolrDocument;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;


/**
 * {@link MailFillers}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFillers implements SolrMailConstants {

    /**
     * Initializes a new {@link MailFillers}.
     */
    private MailFillers() {
        super();
    }

    /**
     * Gets the mail fillers for given fields
     * 
     * @param mailFields The fields or <code>null</code> for all available fillers
     * @return The appropriate fillers
     */
    public static List<MailFiller> fillersFor(final MailFields mailFields) {
        final List<MailFiller> list = new ArrayList<MailFiller>(12);
        final MailFields fields = null == mailFields ? new MailFields(true) : mailFields;
        if (fields.contains(MailField.COLOR_LABEL)) {
            list.add(COLOR_LABEL_FILLER);
        }
        if (fields.contains(MailField.CONTENT_TYPE)) {
            list.add(CONTENT_TYPE_FILLER);
        }
        if (fields.contains(MailField.SIZE)) {
            list.add(SIZE_FILLER);
        }
        if (fields.contains(MailField.RECEIVED_DATE)) {
            list.add(RECEIVED_DATE_FILLER);
        }
        if (fields.contains(MailField.SENT_DATE)) {
            list.add(SENT_DATE_FILLER);
        }
        if (fields.contains(MailField.FROM)) {
            list.add(FROM_FILLER);
        }
        if (fields.contains(MailField.TO)) {
            list.add(TO_FILLER);
        }
        if (fields.contains(MailField.CC)) {
            list.add(CC_FILLER);
        }
        if (fields.contains(MailField.BCC)) {
            list.add(BCC_FILLER);
        }
        if (fields.contains(MailField.FLAGS)) {
            list.add(FLAGS_FILLER);
        }
        if (fields.contains(MailField.SUBJECT)) {
            list.add(SUBJECT_FILLER);
        }
        return list;
    }

    protected static final Set<Locale> KNOWN_LOCALES = IndexConstants.KNOWN_LOCALES;

    /**
     * A mail filler.
     */
    public static interface MailFiller {

        /**
         * Fills specified mail from given Solr document.
         *
         * @param mail The mail
         * @param doc The Solr document
         * @throws OXException If filling fails
         */
        void fill(MailMessage mail, SolrDocument doc) throws OXException;
    }

    private static final class FullNameFiller implements MailFiller {

        private final String fullName;

        protected FullNameFiller(final String fullName) {
            super();
            this.fullName = fullName;
        }

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            mail.setFolder(fullName);
        }
    }

    private static final class AccountIdFiller implements MailFiller {

        private final int accountId;

        protected AccountIdFiller(final int accountId) {
            super();
            this.accountId = accountId;
        }

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            mail.setAccountId(accountId);
        }
    }

    private static final MailFiller COLOR_LABEL_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            mail.setColorLabel(MailFillers.<Integer> getFieldValue(FIELD_COLOR_LABEL, doc).intValue());
        }
    };

    private static final MailFiller CONTENT_TYPE_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            mail.setHasAttachment(MailFillers.<Boolean> getFieldValue(FIELD_ATTACHMENT, doc).booleanValue());
        }
    };

    private static final MailFiller SIZE_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final Long size = MailFillers.<Long> getFieldValue(FIELD_SIZE, doc);
            if (null != size) {
                mail.setSize(size.longValue());
            }
        }
    };

    private static final MailFiller RECEIVED_DATE_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final Long time = MailFillers.<Long> getFieldValue(FIELD_RECEIVED_DATE, doc);
            if (null != time) {
                mail.setReceivedDate(new Date(time.longValue()));
            }
        }
    };

    private static final MailFiller SENT_DATE_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final Long time = MailFillers.<Long> getFieldValue(FIELD_SENT_DATE, doc);
            if (null != time) {
                mail.setSentDate(new Date(time.longValue()));
            }
        }
    };

    private static final MailFiller FROM_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final String addressList = getFieldValue(FIELD_FROM_PLAIN, doc);
            if (!isEmpty(addressList)) {
                try {
                    mail.addFrom(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addFrom(new PlainTextAddress(addressList));
                }
            }
        }
    };

    private static final MailFiller TO_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final String addressList = getFieldValue(FIELD_TO_PLAIN, doc);
            if (!isEmpty(addressList)) {
                try {
                    mail.addTo(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addTo(new PlainTextAddress(addressList));
                }
            }
        }
    };

    private static final MailFiller CC_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final String addressList = getFieldValue(FIELD_CC_PLAIN, doc);
            if (!isEmpty(addressList)) {
                try {
                    mail.addCc(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addCc(new PlainTextAddress(addressList));
                }
            }
        }
    };

    private static final MailFiller BCC_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final String addressList = getFieldValue(FIELD_BCC_PLAIN, doc);
            if (!isEmpty(addressList)) {
                try {
                    mail.addBcc(QuotedInternetAddress.parse(addressList, false));
                } catch (final AddressException e) {
                    mail.addBcc(new PlainTextAddress(addressList));
                }
            }
        }
    };

    private static final MailFiller FLAGS_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            int flags = 0;
            Boolean b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_ANSWERED, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_ANSWERED;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_DELETED, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_DELETED;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_DRAFT, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_DRAFT;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_FLAGGED, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_FLAGGED;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_FORWARDED, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_FORWARDED;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_READ_ACK, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_READ_ACK;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_RECENT, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_RECENT;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_SEEN, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_SEEN;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_SPAM, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_SPAM;
            }
            b = MailFillers.<Boolean> getFieldValue(FIELD_FLAG_USER, doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_USER;
            }
            mail.setFlags(flags);

            final Object ufs = doc.getFieldValue(FIELD_USER_FLAGS);
            if (null != ufs) {
                if (ufs instanceof String) {
                    mail.addUserFlag(ufs.toString());
                } else {
                    @SuppressWarnings("unchecked")
                    final List<String> ufl = (List<String>) ufs;
                    mail.addUserFlags(ufl.toArray(new String[ufl.size()]));
                }
            }
        }
    };

    private static final MailFiller SUBJECT_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final StringBuilder pre = new StringBuilder(FIELD_SUBJECT_PREFIX);
            for (final Locale l : KNOWN_LOCALES) {
                pre.setLength(8);
                final String subject = getFieldValue(pre.append(l.getLanguage()).toString(), doc);
                if (null != subject) {
                    mail.setSubject(subject);
                    break;
                }
            }
        }
    };

    @SuppressWarnings("unchecked")
    protected static <V> V getFieldValue(final String name, final SolrDocument document) throws OXException {
        final Object value = document.getFieldValue(name);
        if (null == value) {
            return null;
        }
        try {
            return (V) value;
        } catch (final ClassCastException e) {
            throw IndexExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected type: " + e.getMessage());
        }
    }

    protected static boolean isEmpty(final String string) {
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
