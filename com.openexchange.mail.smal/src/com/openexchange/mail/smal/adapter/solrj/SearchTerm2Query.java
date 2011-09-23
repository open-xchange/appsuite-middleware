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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BccTerm;
import com.openexchange.mail.search.BodyTerm;
import com.openexchange.mail.search.CcTerm;
import com.openexchange.mail.search.ComparablePattern;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.FromTerm;
import com.openexchange.mail.search.NOTTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.ReceivedDateTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SentDateTerm;
import com.openexchange.mail.search.SizeTerm;
import com.openexchange.mail.search.SubjectTerm;
import com.openexchange.mail.search.ToTerm;

/**
 * {@link SearchTerm2Query} - Transforms a search term to a query.
 * 
 * @see http://lucene.apache.org/java/2_4_0/queryparsersyntax.html#Range Searches
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchTerm2Query {

    /**
     * Initializes a new {@link SearchTerm2Query}.
     */
    private SearchTerm2Query() {
        super();
    }

    /**
     * Transforms specified search term to a query.
     * 
     * @param searchTerm The search term
     * @return The resulting query
     */
    public static StringBuilder searchTerm2Query(final SearchTerm<?> searchTerm) {
        if (null == searchTerm) {
            return null;
        }
        final StringBuilder queryBuilder = new StringBuilder(32);
        if (searchTerm instanceof ANDTerm) {
            final ANDTerm andTerm = (ANDTerm) searchTerm;
            final SearchTerm<?>[] terms = andTerm.getPattern();
            queryBuilder.append('(');
            queryBuilder.append(searchTerm2Query(terms[0]));
            for (int i = 1; i < terms.length; i++) {
                queryBuilder.append(" AND ");
                queryBuilder.append(searchTerm2Query(terms[i]));
            }
            queryBuilder.append(')');
            return queryBuilder;
        }
        if (searchTerm instanceof ORTerm) {
            final ORTerm orTerm = (ORTerm) searchTerm;
            final SearchTerm<?>[] terms = orTerm.getPattern();
            queryBuilder.append('(');
            queryBuilder.append(searchTerm2Query(terms[0]));
            for (int i = 1; i < terms.length; i++) {
                queryBuilder.append(" OR ");
                queryBuilder.append(searchTerm2Query(terms[i]));
            }
            queryBuilder.append(')');
            return queryBuilder;
        }
        if (searchTerm instanceof NOTTerm) {
            final NOTTerm notTerm = (NOTTerm) searchTerm;
            queryBuilder.append("NOT (");
            queryBuilder.append(searchTerm2Query(notTerm.getPattern()));
            queryBuilder.append(')');
            return queryBuilder;
        }
        final Object pattern = searchTerm.getPattern();
        if (pattern instanceof String) {
            final String sPattern = (String) pattern;
            queryBuilder.append('(');
            final List<String> names = getFieldNameFor(searchTerm);
            queryBuilder.append(names.get(0)).append(':').append('"').append(sPattern).append('"');
            for (int i = 0; i < names.size(); i++) {
                queryBuilder.append(" OR ");
                queryBuilder.append(names.get(i)).append(':').append('"').append(sPattern).append('"');
            }
            queryBuilder.append(')');
            return queryBuilder;
        }
        /*
         * Size term
         */
        if (searchTerm instanceof SizeTerm) {
            final SizeTerm sizeTerm = (SizeTerm) searchTerm;
            final ComparablePattern<Integer> comparablePattern = sizeTerm.getPattern();
            switch (comparablePattern.getComparisonType()) {
            case EQUALS:
                return queryBuilder.append('(').append("size").append(':').append(comparablePattern.getPattern().intValue()).append(')');
            case GREATER_THAN:
                return queryBuilder.append('(').append("size").append(':').append('[').append(comparablePattern.getPattern().intValue() + 1).append(
                    " TO ").append(Integer.MAX_VALUE).append(']').append(')');
            case LESS_THAN:
                return queryBuilder.append('(').append("size").append(':').append('[').append(0).append(" TO ").append(
                    comparablePattern.getPattern().intValue() - 1).append(']').append(')');
            default:
                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
            }
        }
        /*
         * Flag term
         */
        if (searchTerm instanceof FlagTerm) {
            final FlagTerm flagTerm = (FlagTerm) searchTerm;
            int flags = flagTerm.getPattern().intValue();
            final boolean set = flags >= 0;
            if (!set) {
                flags *= -1;
            }
            queryBuilder.append('(');
            if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
                queryBuilder.append(" AND flag_answered:").append(set);
            }
            if ((flags & MailMessage.FLAG_DELETED) > 0) {
                queryBuilder.append(" AND flag_deleted:").append(set);
            }
            if ((flags & MailMessage.FLAG_DRAFT) > 0) {
                queryBuilder.append(" AND flag_draft:").append(set);
            }
            if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
                queryBuilder.append(" AND flag_flagged:").append(set);
            }
            if ((flags & MailMessage.FLAG_RECENT) > 0) {
                queryBuilder.append(" AND flag_recent:").append(set);
            }
            if ((flags & MailMessage.FLAG_SEEN) > 0) {
                queryBuilder.append(" AND flag_seen:").append(set);
            }
            if ((flags & MailMessage.FLAG_USER) > 0) {
                queryBuilder.append(" AND flag_user:").append(set);
            }
            if ((flags & MailMessage.FLAG_SPAM) > 0) {
                queryBuilder.append(" AND flag_spam:").append(set);
            }
            if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
                queryBuilder.append(" AND flag_forwarded:").append(set);
            }
            if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
                queryBuilder.append(" AND flag_read_ack:").append(set);
            }
            queryBuilder.append(')');
            return queryBuilder;
        }
        /*
         * Date terms
         */
        boolean isRecDate;
        if ((isRecDate = (searchTerm instanceof ReceivedDateTerm)) || (searchTerm instanceof SentDateTerm)) {
            @SuppressWarnings("unchecked") final SearchTerm<ComparablePattern<java.util.Date>> dateTerm =
                (SearchTerm<ComparablePattern<java.util.Date>>) searchTerm;
            final ComparablePattern<Date> comparablePattern = dateTerm.getPattern();
            final long time = comparablePattern.getPattern().getTime();
            final String name = isRecDate ? "received_date" : "sent_date";
            switch (comparablePattern.getComparisonType()) {
            case EQUALS:
                return queryBuilder.append('(').append(name).append(':').append(time).append(')');
            case GREATER_THAN:
                return queryBuilder.append('(').append(name).append(':').append('[').append(time + 1).append(" TO ").append(Long.MAX_VALUE).append(
                    ']').append(')');
            case LESS_THAN:
                return queryBuilder.append('(').append(name).append(':').append('[').append(Long.MIN_VALUE).append(" TO ").append(time - 1).append(
                    ']').append(')');
            default:
                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
            }
        }
        throw new IllegalStateException("Unknown search term: " + searchTerm.getClass().getName());
    }

    private static List<String> getFieldNameFor(final SearchTerm<?> searchTerm) {
        if (searchTerm instanceof BccTerm) {
            return Arrays.asList("bcc_personal", "bcc_addr");
        }
        if (searchTerm instanceof BodyTerm) {
            return Arrays.asList("content_en", "content_de", "content_fr", "content_nl", "content_sv", "content_es", "content_it");
        }
        if (searchTerm instanceof CcTerm) {
            return Arrays.asList("cc_personal", "cc_addr");
        }
        if (searchTerm instanceof FromTerm) {
            return Arrays.asList("from_personal", "from_addr");
        }
        if (searchTerm instanceof ReceivedDateTerm) {
            return Arrays.asList("received_date");
        }
        if (searchTerm instanceof SentDateTerm) {
            return Arrays.asList("sent_date");
        }
        if (searchTerm instanceof SizeTerm) {
            return Arrays.asList("size");
        }
        if (searchTerm instanceof SubjectTerm) {
            return Arrays.asList("subject_en", "subject_de", "subject_fr", "subject_nl", "subject_sv", "subject_es", "subject_it");
        }
        if (searchTerm instanceof ToTerm) {
            return Arrays.asList("to_personal", "to_addr");
        }
        throw new IllegalStateException("Unsupported search term: " + searchTerm.getClass().getName());
    }

}
