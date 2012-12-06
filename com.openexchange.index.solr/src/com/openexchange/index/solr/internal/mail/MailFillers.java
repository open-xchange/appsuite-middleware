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
import javax.mail.internet.AddressException;
import org.apache.solr.common.SolrDocument;
import com.openexchange.exception.OXException;
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
public final class MailFillers {

    /**
     * Initializes a new {@link MailFillers}.
     */
    private MailFillers() {
        super();
    }

    /**
     * Gets all available mail fillers
     * 
     * @return All available fillers
     */
    public static List<MailFiller> allFillers() {
        final List<MailFiller> list = new ArrayList<MailFiller>(12);
        list.add(COLOR_LABEL_FILLER);
        list.add(CONTENT_TYPE_FILLER);
        list.add(SIZE_FILLER);
        list.add(RECEIVED_DATE_FILLER);
        list.add(SENT_DATE_FILLER);
        list.add(FROM_FILLER);
        list.add(TO_FILLER);
        list.add(CC_FILLER);
        list.add(BCC_FILLER);
        list.add(FLAGS_FILLER);
        list.add(SUBJECT_FILLER);
        return list;
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

    /**
     * {@link FullNameFiller} - The <code>MailFiller</code> for full name.
     */
    public static final class FullNameFiller implements MailFiller {

        /**
         * The default filler for full name.
         */
        public static final FullNameFiller DEFAULT = new FullNameFiller();

        private final String fullName;

        protected FullNameFiller(final String fullName) {
            super();
            this.fullName = fullName;
        }

        private FullNameFiller() {
            this(null);
        }

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            mail.setFolder(null == fullName ? MailFillers.<String> getFieldValue(SolrMailField.FULL_NAME.solrName(), doc) : fullName);
        }
    }

    /**
     * {@link AccountIdFiller} - The <code>MailFiller</code> for account identifier.
     */
    public static final class AccountIdFiller implements MailFiller {

        /**
         * The default filler for account identifier.
         */
        public static final AccountIdFiller DEFAULT = new AccountIdFiller();

        private final int accountId;

        protected AccountIdFiller(final int accountId) {
            super();
            this.accountId = accountId;
        }

        private AccountIdFiller() {
            this(-1);
        }

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            mail.setAccountId(accountId < 0 ? MailFillers.<Integer> getFieldValue(SolrMailField.ACCOUNT.solrName(), doc).intValue() : accountId);
        }
    }

    public static final MailFiller COLOR_LABEL_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final Integer colorLabel = MailFillers.<Integer> getFieldValue(SolrMailField.COLOR_LABEL.solrName(), doc);
            if (colorLabel != null) {
                mail.setColorLabel(colorLabel.intValue());
            }            
        }
    };

    private static final MailFiller CONTENT_TYPE_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final Boolean hasAttachment = MailFillers.<Boolean> getFieldValue(SolrMailField.ATTACHMENT.solrName(), doc);
            if (hasAttachment != null) {
                mail.setHasAttachment(hasAttachment.booleanValue());
            }            
        }
    };

    private static final MailFiller SIZE_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final Long size = MailFillers.<Long> getFieldValue(SolrMailField.SIZE.solrName(), doc);
            if (null != size) {
                mail.setSize(size.longValue());
            }
        }
    };

    private static final MailFiller RECEIVED_DATE_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final Long time = MailFillers.<Long> getFieldValue(SolrMailField.RECEIVED_DATE.solrName(), doc);
            if (null != time) {
                mail.setReceivedDate(new Date(time.longValue()));
            }
        }
    };

    private static final MailFiller SENT_DATE_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final Long time = MailFillers.<Long> getFieldValue(SolrMailField.SENT_DATE.solrName(), doc);
            if (null != time) {
                mail.setSentDate(new Date(time.longValue()));
            }
        }
    };

    private static final MailFiller FROM_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final List<String> addressList = getFieldValue(SolrMailField.FROM.solrName(), doc);
            if (addressList != null && !addressList.isEmpty()) {
                for (String addr : addressList) {
                    try {
                        mail.addFrom(QuotedInternetAddress.parse(addr, false));
                    } catch (AddressException e) {
                        mail.addFrom(new PlainTextAddress(addr));
                    }
                }
            }
        }
    };

    private static final MailFiller TO_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final List<String> addressList = getFieldValue(SolrMailField.TO.solrName(), doc);
            if (addressList != null && !addressList.isEmpty()) {
                for (String addr : addressList) {
                    try {
                        mail.addFrom(QuotedInternetAddress.parse(addr, false));
                    } catch (AddressException e) {
                        mail.addFrom(new PlainTextAddress(addr));
                    }
                }
            }
        }
    };

    private static final MailFiller CC_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final List<String> addressList = getFieldValue(SolrMailField.CC.solrName(), doc);
            if (addressList != null && !addressList.isEmpty()) {
                for (String addr : addressList) {
                    try {
                        mail.addFrom(QuotedInternetAddress.parse(addr, false));
                    } catch (AddressException e) {
                        mail.addFrom(new PlainTextAddress(addr));
                    }
                }
            }
        }
    };

    private static final MailFiller BCC_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final List<String> addressList = getFieldValue(SolrMailField.BCC.solrName(), doc);
            if (addressList != null && !addressList.isEmpty()) {
                for (String addr : addressList) {
                    try {
                        mail.addFrom(QuotedInternetAddress.parse(addr, false));
                    } catch (AddressException e) {
                        mail.addFrom(new PlainTextAddress(addr));
                    }
                }
            }
        }
    };

    public static final MailFiller FLAGS_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            int flags = 0;
            Boolean b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_ANSWERED.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_ANSWERED;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_DELETED.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_DELETED;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_DRAFT.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_DRAFT;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_FLAGGED.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_FLAGGED;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_FORWARDED.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_FORWARDED;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_READ_ACK.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_READ_ACK;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_RECENT.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_RECENT;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_SEEN.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_SEEN;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_SPAM.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_SPAM;
            }
            b = MailFillers.<Boolean> getFieldValue(SolrMailField.FLAG_USER.solrName(), doc);
            if (null != b && b.booleanValue()) {
                flags |= MailMessage.FLAG_USER;
            }
            mail.setFlags(flags);

            final String userFlagsField = SolrMailField.USER_FLAGS.solrName();
            if (userFlagsField != null) {
                final Object ufs = doc.getFieldValue(userFlagsField);
                if (null != ufs) {
                    if (ufs instanceof String) {
                        mail.addUserFlag(ufs.toString());
                    } else {
                        @SuppressWarnings("unchecked") final List<String> ufl = (List<String>) ufs;
                        mail.addUserFlags(ufl.toArray(new String[ufl.size()]));
                    }
                }
            }
        }
    };

    private static final MailFiller SUBJECT_FILLER = new MailFiller() {

        @Override
        public void fill(final MailMessage mail, final SolrDocument doc) throws OXException {
            final String subject = getFieldValue(SolrMailField.SUBJECT.solrName(), doc);
            if (null != subject) {
                mail.setSubject(subject);
            }
        }
    };

    @SuppressWarnings("unchecked")
    protected static <V> V getFieldValue(final String name, final SolrDocument document) throws OXException {
        if (name == null) {
            return null;
        }
        
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
