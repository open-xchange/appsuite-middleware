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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.index.solr.mail.SolrMailConstants;
import com.openexchange.index.solr.mail.SolrMailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BccTerm;
import com.openexchange.mail.search.BodyTerm;
import com.openexchange.mail.search.BooleanTerm;
import com.openexchange.mail.search.CcTerm;
import com.openexchange.mail.search.ComparablePattern;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.FromTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.NOTTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.ReceivedDateTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SearchTermVisitor;
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
public final class SearchTerm2Query implements SolrMailConstants {

    private static final class SearchTerm2QueryVisitor implements SearchTermVisitor {

        protected final StringBuilder queryBuilder;

        protected SearchTerm2QueryVisitor() {
            super();
            queryBuilder = new StringBuilder(48);
        }

        protected SearchTerm2QueryVisitor reset() {
            queryBuilder.setLength(0);
            return this;
        }

        @Override
        public void visit(final ANDTerm term) {
            final SearchTerm<?>[] terms = term.getPattern();
            queryBuilder.append('(');
            queryBuilder.append(searchTerm2Query(terms[0]));
            for (int i = 1; i < terms.length; i++) {
                queryBuilder.append(" AND ");
                queryBuilder.append(searchTerm2Query(terms[i]));
            }
            queryBuilder.append(')');
        }

        @Override
        public void visit(final BccTerm term) {
            if (SolrMailField.BCC.isIndexed()) {
                List<String> fields = Collections.singletonList(SolrMailField.BCC.solrName());
                stringPattern(fields, term.getPattern());
            }
        }

        @Override
        public void visit(final BodyTerm term) {
            // TODO: use locales?
            if (SolrMailField.CONTENT.isIndexed()) {
                List<String> fields = Collections.singletonList(SolrMailField.CONTENT.solrName());
                stringPattern(fields, term.getPattern());
            }
        }

        @Override
        public void visit(final BooleanTerm term) {
            throw new IllegalStateException("Unsupported search term: " + BooleanTerm.class.getName());
        }

        @Override
        public void visit(final CcTerm term) {
            if (SolrMailField.CC.isIndexed()) {
                List<String> fields = Collections.singletonList(SolrMailField.CC.solrName());
                stringPattern(fields, term.getPattern());
            }
        }

        @Override
        public void visit(final FlagTerm term) {
            int flags = term.getPattern().intValue();
            final boolean set = flags >= 0;
            if (!set) {
                flags *= -1;
            }
            final String andConcat = " AND ";
            queryBuilder.append('(');
            final int off = queryBuilder.length();
            if ((flags & MailMessage.FLAG_ANSWERED) > 0 && SolrMailField.FLAG_ANSWERED.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_ANSWERED.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_DELETED) > 0 && SolrMailField.FLAG_DELETED.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_DELETED.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_DRAFT) > 0 && SolrMailField.FLAG_DRAFT.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_DRAFT.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_FLAGGED) > 0 && SolrMailField.FLAG_FLAGGED.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_FLAGGED.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_RECENT) > 0 && SolrMailField.FLAG_RECENT.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_RECENT.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_SEEN) > 0 && SolrMailField.FLAG_SEEN.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_SEEN.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_USER) > 0 && SolrMailField.FLAG_USER.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_USER.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_SPAM) > 0 && SolrMailField.FLAG_SPAM.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_SPAM.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_FORWARDED) > 0 && SolrMailField.FLAG_FORWARDED.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_FORWARDED.solrName()).append(':').append(set);
            }
            if ((flags & MailMessage.FLAG_READ_ACK) > 0 && SolrMailField.FLAG_READ_ACK.isIndexed()) {
                queryBuilder.append(andConcat).append(SolrMailField.FLAG_READ_ACK.solrName()).append(':').append(set);
            }
            queryBuilder.delete(off, off + andConcat.length()); // Delete first >>" AND "<< prefix
            queryBuilder.append(')');
        }

        @Override
        public void visit(final FromTerm term) {
            if (SolrMailField.FROM.isIndexed()) {
                List<String> fields = Collections.singletonList(SolrMailField.FROM.solrName());
                stringPattern(fields, term.getPattern());
            }
        }

        @Override
        public void visit(final HeaderTerm term) {
            throw new IllegalStateException("Unsupported search term: " + HeaderTerm.class.getName());
        }

        @Override
        public void visit(final NOTTerm term) {
            queryBuilder.append("NOT (");
            queryBuilder.append(searchTerm2Query(term.getPattern()));
            queryBuilder.append(')');
        }

        @Override
        public void visit(final ORTerm term) {
            final SearchTerm<?>[] terms = term.getPattern();
            queryBuilder.append('(');
            queryBuilder.append(searchTerm2Query(terms[0]));
            for (int i = 1; i < terms.length; i++) {
                queryBuilder.append(" OR ");
                queryBuilder.append(searchTerm2Query(terms[i]));
            }
            queryBuilder.append(')');
        }

        @Override
        public void visit(final ReceivedDateTerm term) {
            final ComparablePattern<Date> comparablePattern = term.getPattern();
            final long time = comparablePattern.getPattern().getTime();
            if (SolrMailField.RECEIVED_DATE.isIndexed()) {
                final String name = SolrMailField.RECEIVED_DATE.solrName();
                switch (comparablePattern.getComparisonType()) {
                    case EQUALS:
                        queryBuilder.append('(').append(name).append(':').append(time).append(')');
                        break;
                    case GREATER_THAN:
                        queryBuilder.append('(').append(name).append(':').append('[').append(time + 1).append(" TO ").append(Long.MAX_VALUE).append(
                            ']').append(')');
                        break;
                    case LESS_THAN:
                        queryBuilder.append('(').append(name).append(':').append('[').append(Long.MIN_VALUE).append(" TO ").append(time - 1).append(
                            ']').append(')');
                        break;
                    default:
                        throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
                }
            }
        }

        @Override
        public void visit(final SentDateTerm term) {
            final ComparablePattern<Date> comparablePattern = term.getPattern();
            final long time = comparablePattern.getPattern().getTime();
            if (SolrMailField.SENT_DATE.isIndexed()) {
                final String name = SolrMailField.SENT_DATE.solrName();
                switch (comparablePattern.getComparisonType()) {
                case EQUALS:
                    queryBuilder.append('(').append(name).append(':').append(time).append(')');
                    break;
                case GREATER_THAN:
                    queryBuilder.append('(').append(name).append(':').append('[').append(time + 1).append(" TO ").append(Long.MAX_VALUE).append(
                        ']').append(')');
                    break;
                case LESS_THAN:
                    queryBuilder.append('(').append(name).append(':').append('[').append(Long.MIN_VALUE).append(" TO ").append(time - 1).append(
                        ']').append(')');
                    break;
                default:
                    throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
                }
            }
        }

        @Override
        public void visit(final SizeTerm term) {
            final ComparablePattern<Integer> comparablePattern = term.getPattern();
            if (SolrMailField.SIZE.isIndexed()) {
                final String name = SolrMailField.SIZE.solrName();
                switch (comparablePattern.getComparisonType()) {
                    case EQUALS:
                        queryBuilder.append('(').append(name).append(':').append(comparablePattern.getPattern().intValue()).append(
                            ')');
                        break;
                    case GREATER_THAN:
                        queryBuilder.append('(').append(name).append(':').append('[').append(
                            comparablePattern.getPattern().intValue() + 1).append(" TO ").append(Integer.MAX_VALUE).append(']').append(')');
                        break;
                    case LESS_THAN:
                        queryBuilder.append('(').append(name).append(':').append('[').append(0).append(" TO ").append(
                            comparablePattern.getPattern().intValue() - 1).append(']').append(')');
                        break;
                    default:
                        throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
                }
            }
        }

        @Override
        public void visit(final SubjectTerm term) {
            // TODO: use locales?
            if (SolrMailField.SUBJECT.isIndexed()) {
                List<String> fields = Collections.singletonList(SolrMailField.SUBJECT.solrName());
                stringPattern(fields, term.getPattern());
            }
        }

        @Override
        public void visit(final ToTerm term) {
            if (SolrMailField.TO.isIndexed()) {
                List<String> fields = Collections.singletonList(SolrMailField.TO.solrName());
                stringPattern(fields, term.getPattern());
            }            
        }

        private void stringPattern(final List<String> names, final String pattern) {
            queryBuilder.append('(');
            queryBuilder.append(names.get(0)).append(':').append('"').append(pattern).append('"');
            for (int i = 1; i < names.size(); i++) {
                queryBuilder.append(" OR ");
                queryBuilder.append(names.get(i)).append(':').append('"').append(pattern).append('"');
            }
            queryBuilder.append(')');
        }
    }

    /**
     * Initializes a new {@link SearchTerm2Query}.
     */
    private SearchTerm2Query() {
        super();
    }

    /**
     * Transforms specified search term to a query.
     * 
     * @param mailSearchTerm The mail search term
     * @return The resulting query
     */
    public static StringBuilder searchTerm2Query(final SearchTerm<?> mailSearchTerm) {
        if (null == mailSearchTerm) {
            return null;
        }
        final SearchTerm2QueryVisitor visitor = new SearchTerm2QueryVisitor();
        mailSearchTerm.accept(visitor);
        return visitor.queryBuilder;
    }

}
