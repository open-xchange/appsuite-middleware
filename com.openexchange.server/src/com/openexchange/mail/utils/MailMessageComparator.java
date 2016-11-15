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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.mail.utils;

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
import com.openexchange.java.Collators;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.PlainTextAddress;

/**
 * {@link MailMessageComparator} - A {@link Comparator comparator} for {@link MailMessage messages}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessageComparator implements Comparator<MailMessage> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailMessageComparator.class);

    private static final String STR_EMPTY = "";

    // ------------------------------------------------------------------------------------------------------------------------ //

    /**
     * Gets the prepared mail fields for search.
     *
     * @param mailFields The requested mail fields by client
     * @param sortField The sort field
     * @return The prepared mail fields for search
     */
    public static MailFields prepareMailFieldsForSearch(final MailField[] mailFields, final MailSortField sortField) {
        return StorageUtility.prepareMailFieldsForSearch(mailFields, sortField);
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    private static interface IFieldComparer {

        int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException;

        int compareFieldsDesc(final MailMessage msg1, final MailMessage msg2) throws MessagingException;
    }

    private static abstract class FieldComparer implements IFieldComparer {

        protected FieldComparer() {
            super();
        }

        @Override
        public int compareFieldsDesc(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
            // Negate ASC order
            int result = compareFields(msg1, msg2);
            if (0 == result) {
                return result;
            }
            result = -result;
            return result;
        }
    }

    private static abstract class LocalizedFieldComparer extends FieldComparer {

        public final Locale locale;

        public final Collator collator;

        public LocalizedFieldComparer(final Locale locale) {
            super();
            this.locale = locale;
            collator = Collators.getSecondaryInstance(locale);
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    private final IFieldComparer fieldComparer;
    private final boolean descendingDir;

    /**
     * Initializes a new {@link MailMessageComparator} sorting by header <code>Date</code> (a.k.a. sent date).
     *
     * @param descendingDirection <code>true</code> for descending order; otherwise <code>false</code>
     * @param locale The locale
     */
    public MailMessageComparator(final boolean descendingDirection, final Locale locale) {
        this(MailSortField.SENT_DATE, descendingDirection, locale);
    }

    /**
     * Initializes a new {@link MailMessageComparator}.
     *
     * @param sortField The sort field
     * @param descendingDirection <code>true</code> for descending order; otherwise <code>false</code>
     * @param locale The locale
     */
    public MailMessageComparator(final MailSortField sortField, final boolean descendingDirection, final Locale locale) {
        this(sortField, descendingDirection, locale, MailProperties.getInstance().isUserFlagsEnabled());
    }

    /**
     * Initializes a new {@link MailMessageComparator}.
     *
     * @param sortField The sort field
     * @param descendingDirection <code>true</code> for descending order; otherwise <code>false</code>
     * @param locale The locale
     * @param userFlagsEnabled <code>true</code> to signal support for user flags; otherwise <code>false</code>
     */
    public MailMessageComparator(final MailSortField sortField, final boolean descendingDirection, final Locale locale, final boolean userFlagsEnabled) {
        super();
        descendingDir = descendingDirection;
        if (MailSortField.COLOR_LABEL.equals(sortField) && !userFlagsEnabled) {
            fieldComparer = DUMMY_COMPARER;
        } else {
            IFieldComparer tmp = COMPARERS.get(sortField);
            if (null == tmp) {
                tmp = createFieldComparer(sortField, locale);
            }
            fieldComparer = tmp;
        }
    }

    @Override
    public int compare(final MailMessage msg1, final MailMessage msg2) {
        try {
            return descendingDir ? fieldComparer.compareFieldsDesc(msg1, msg2) : fieldComparer.compareFields(msg1, msg2);
        } catch (final MessagingException e) {
            LOG.error("", e);
            return 0;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    static int compareAddrs(final Address[] addrs1, final Address[] addrs2, final Locale locale, final Collator collator) {
        if (isEmptyAddrArray(addrs1) && !isEmptyAddrArray(addrs2)) {
            return -1;
        } else if (!isEmptyAddrArray(addrs1) && isEmptyAddrArray(addrs2)) {
            return 1;
        } else if (isEmptyAddrArray(addrs1) && isEmptyAddrArray(addrs2)) {
            return 0;
        }
        return collator.compare(getCompareStringFromAddress(addrs1[0], locale), getCompareStringFromAddress(addrs2[0], locale));
    }

    private static boolean isEmptyAddrArray(final Address[] addrs) {
        return ((addrs == null) || (addrs.length == 0));
    }

    private static String getCompareStringFromAddress(final Address addr, final Locale locale) {
        if (addr instanceof PlainTextAddress) {
            return ((PlainTextAddress) addr).getAddress().toLowerCase(locale);
        } else if (addr instanceof InternetAddress) {
            final InternetAddress ia1 = (InternetAddress) addr;
            final String personal = ia1.getPersonal();
            if ((personal != null) && (personal.length() > 0)) {
                /*
                 * Personal is present. Skip leading quotes.
                 */
                return (personal.charAt(0) == '\'') || (personal.charAt(0) == '"') ? personal.substring(1).toLowerCase(locale) : personal.toLowerCase(locale);
            }
            return IDNA.toIDN(ia1.getAddress()).toLowerCase(locale);
        } else {
            return STR_EMPTY;
        }
    }

    static int compareByReceivedDate(final MailMessage msg1, final MailMessage msg2, final boolean asc) throws MessagingException {
        final Date d1 = msg1.getReceivedDate();
        final Date d2 = msg2.getReceivedDate();
        if (null == d1) {
            if (null == d2) {
                return 0;
            }
            return asc ? -1 : 1;
        } else if (null == d2) {
            return asc ? 1 : -1;
        } else {
            return asc ? d1.compareTo(d2) : d2.compareTo(d1);
        }
    }

    private static final EnumMap<MailSortField, IFieldComparer> COMPARERS;

    static {
        COMPARERS = new EnumMap<MailSortField, IFieldComparer>(MailSortField.class);
        COMPARERS.put(MailSortField.SENT_DATE, new FieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                final Date d1 = msg1.getSentDate();
                final Date d2 = msg2.getSentDate();
                if (null == d1) {
                    if (null == d2) {
                        return 0;
                    }
                    return -1;
                } else if (null == d2) {
                    return 1;
                } else {
                    return d1.compareTo(d2);
                }
            }
        });
        COMPARERS.put(MailSortField.RECEIVED_DATE, new FieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                return compareByReceivedDate(msg1, msg2, true);
            }
        });
        COMPARERS.put(MailSortField.FLAG_SEEN, new FieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                if (msg1.isSeen()) {
                    if (msg2.isSeen()) {
                        return compareByReceivedDate(msg1, msg2, false);
                    }
                    return 1;
                }
                if (msg2.isSeen()) {
                    return -1;
                }
                return compareByReceivedDate(msg1, msg2, false);
            }
        });
        COMPARERS.put(MailSortField.FLAG_ANSWERED, new FieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                if (msg1.isAnswered()) {
                    if (msg2.isAnswered()) {
                        return compareByReceivedDate(msg1, msg2, false);
                    }
                    return 1;
                }
                if (msg2.isAnswered()) {
                    return -1;
                }
                return compareByReceivedDate(msg1, msg2, false);
            }
        });
        COMPARERS.put(MailSortField.FLAG_ANSWERED, new FieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                if (msg1.isForwarded()) {
                    if (msg2.isForwarded()) {
                        return compareByReceivedDate(msg1, msg2, false);
                    }
                    return 1;
                }
                if (msg2.isForwarded()) {
                    return -1;
                }
                return compareByReceivedDate(msg1, msg2, false);
            }
        });
        COMPARERS.put(MailSortField.FLAG_DRAFT, new FieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                if (msg1.isDraft()) {
                    if (msg2.isDraft()) {
                        return compareByReceivedDate(msg1, msg2, false);
                    }
                    return 1;
                }
                if (msg2.isDraft()) {
                    return -1;
                }
                return compareByReceivedDate(msg1, msg2, false);
            }
        });
        COMPARERS.put(MailSortField.FLAG_FLAGGED, new FieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                if (msg1.isFlagged()) {
                    if (msg2.isFlagged()) {
                        return compareByReceivedDate(msg1, msg2, false);
                    }
                    return 1;
                }
                if (msg2.isFlagged()) {
                    return -1;
                }
                return compareByReceivedDate(msg1, msg2, false);
            }
        });
        COMPARERS.put(MailSortField.SIZE, new FieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                final long size1 = msg1.getSize();
                final long size2 = msg2.getSize();
                return (size1 < size2 ? -1 : (size1 == size2 ? 0 : 1));
            }
        });
        COMPARERS.put(MailSortField.COLOR_LABEL, new IFieldComparer() {

            @Override
            public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                final int result = compAsc(msg1, msg2);
                return result == 0 ? compareByReceivedDate(msg1, msg2, false) : result;
            }

            private int compAsc(final MailMessage msg1, final MailMessage msg2) {
                final int cl1 = msg1.getColorLabel();
                final int cl2 = msg2.getColorLabel();
                if (cl1 <= 0) {
                    return cl2 <= 0 ? 0 : 1;
                }
                if (cl2 <= 0) {
                    return cl1 <= 0 ? 0 : -1;
                }
                return (cl1 < cl2 ? -1 : (cl1 == cl2 ? 0 : 1));
            }

            @Override
            public int compareFieldsDesc(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                final int result = compDesc(msg1, msg2);
                return result == 0 ? compareByReceivedDate(msg1, msg2, false) : result;
            }

            private int compDesc(final MailMessage msg1, final MailMessage msg2) {
                final int cl1 = msg1.getColorLabel();
                final int cl2 = msg2.getColorLabel();
                if (cl1 <= 0) {
                    return cl2 <= 0 ? 0 : 1;
                }
                if (cl2 <= 0) {
                    return cl1 <= 0 ? 0 : -1;
                }
                return (cl1 < cl2 ? 1 : (cl1 == cl2 ? 0 : -1));
            }
        });
    }

    private static FieldComparer DUMMY_COMPARER = new FieldComparer() {

        @Override
        public int compareFields(final MailMessage msg1, final MailMessage msg2) {
            return 0;
        }
    };

    private static FieldComparer createFieldComparer(final MailSortField sortCol, final Locale locale) {
        switch (sortCol) {
        case FROM:
            return new LocalizedFieldComparer(locale) {

                @Override
                public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                    return compareAddrs(msg1.getFrom(), msg2.getFrom(), locale, collator);
                }
            };
        case TO:
            return new LocalizedFieldComparer(locale) {

                @Override
                public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                    return compareAddrs(msg1.getTo(), msg2.getTo(), locale, collator);
                }
            };
        case CC:
            return new LocalizedFieldComparer(locale) {

                @Override
                public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                    return compareAddrs(msg1.getCc(), msg2.getCc(), locale, collator);
                }
            };
        case SUBJECT:
            return new LocalizedFieldComparer(locale) {

                @Override
                public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                    final String sub1 = msg1.getSubject();
                    final String sub2 = msg2.getSubject();
                    return collator.compare(sub1 == null ? STR_EMPTY : sub1, sub2 == null ? STR_EMPTY : sub2);
                }
            };
        case ACCOUNT_NAME:
            return new LocalizedFieldComparer(locale) {

                @Override
                public int compareFields(final MailMessage msg1, final MailMessage msg2) throws MessagingException {
                    final String name1 = msg1.getAccountName();
                    final String name2 = msg2.getAccountName();
                    return collator.compare(name1 == null ? STR_EMPTY : name1, name2 == null ? STR_EMPTY : name2);
                }
            };
        default:
            throw new UnsupportedOperationException("Unknown sort column value " + sortCol);
        }
    }

    private static final Integer COLOR_FLAG_MIN = Integer.valueOf(-99);

    static Integer getColorFlag(final String[] userFlags) {
        for (int i = 0; i < userFlags.length; i++) {
            final String userFlag = userFlags[i];
            if (MailMessage.isColorLabel(userFlag)) {
                // A color flag; parse its integer value
                final int cf = MailMessage.parseColorLabel(userFlag, COLOR_FLAG_MIN.intValue());
                return MailMessage.COLOR_LABEL_NONE == cf ? COLOR_FLAG_MIN : Integer.valueOf(cf * -1);
            }
        }
        return COLOR_FLAG_MIN;
    }

}
