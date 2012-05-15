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

package com.openexchange.mail.smal.impl.index;

import com.openexchange.index.solr.mail.SolrMailConstants;

/**
 * {@link SearchTerm2Query} - Transforms a search term to a query.
 * 
 * @see http://lucene.apache.org/java/2_4_0/queryparsersyntax.html#Range Searches
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchTerm2Query implements SolrMailConstants {

//    private static final class SearchTerm2QueryVisitor implements SearchTermVisitor {
//
//        private static final Set<Locale> KNOWN_LOCALES = IndexConstants.KNOWN_LOCALES;
//
//        /**
//         * The query builder.
//         */
//        protected final StringBuilder queryBuilder;
//
//        protected SearchTerm2QueryVisitor() {
//            super();
//            queryBuilder = new StringBuilder(48);
//        }
//
//        protected SearchTerm2QueryVisitor reset() {
//            queryBuilder.setLength(0);
//            return this;
//        }
//
//        @Override
//        public void visit(final ANDTerm term) {
//            final SearchTerm<?>[] terms = term.getPattern();
//            queryBuilder.append('(');
//            queryBuilder.append(searchTerm2Query(terms[0]));
//            for (int i = 1; i < terms.length; i++) {
//                queryBuilder.append(" AND ");
//                queryBuilder.append(searchTerm2Query(terms[i]));
//            }
//            queryBuilder.append(')');
//        }
//
//        @Override
//        public void visit(final BccTerm term) {
//            stringPattern(Arrays.asList(FIELD_BCC_PERSONAL, FIELD_BCC_ADDR), term.getPattern());
//        }
//
//        @Override
//        public void visit(final BodyTerm term) {
//            final Set<Locale> knownLocales = KNOWN_LOCALES;
//            final List<String> names = new ArrayList<String>(knownLocales.size());
//            final StringBuilder tmp = new StringBuilder(FIELD_CONTENT_PREFIX); // 8
//            for (final Locale loc : knownLocales) {
//                tmp.setLength(8);
//                tmp.append(loc.getLanguage());
//                names.add(tmp.toString());
//            }
//            stringPattern(names, term.getPattern());
//        }
//
//        @Override
//        public void visit(final BooleanTerm term) {
//            throw new IllegalStateException("Unsupported search term: " + BooleanTerm.class.getName());
//        }
//
//        @Override
//        public void visit(final CcTerm term) {
//            stringPattern(Arrays.asList(FIELD_CC_PERSONAL, FIELD_CC_ADDR), term.getPattern());
//        }
//
//        @Override
//        public void visit(final FlagTerm term) {
//            int flags = term.getPattern().intValue();
//            final boolean set = flags >= 0;
//            if (!set) {
//                flags *= -1;
//            }
//            final String andConcat = " AND ";
//            queryBuilder.append('(');
//            final int off = queryBuilder.length();
//            if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_ANSWERED).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_DELETED) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_DELETED).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_DRAFT) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_DRAFT).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_FLAGGED).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_RECENT) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_RECENT).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_SEEN) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_SEEN).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_USER) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_USER).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_SPAM) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_SPAM).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_FORWARDED).append(':').append(set);
//            }
//            if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
//                queryBuilder.append(andConcat).append(FIELD_FLAG_READ_ACK).append(':').append(set);
//            }
//            queryBuilder.delete(off, off + andConcat.length()); // Delete first >>" AND "<< prefix
//            queryBuilder.append(')');
//        }
//
//        @Override
//        public void visit(final FromTerm term) {
//            stringPattern(Arrays.asList(FIELD_FROM_PERSONAL, FIELD_FROM_ADDR), term.getPattern());
//        }
//
//        @Override
//        public void visit(final HeaderTerm term) {
//            throw new IllegalStateException("Unsupported search term: " + HeaderTerm.class.getName());
//        }
//
//        @Override
//        public void visit(final NOTTerm term) {
//            queryBuilder.append("NOT (");
//            queryBuilder.append(searchTerm2Query(term.getPattern()));
//            queryBuilder.append(')');
//        }
//
//        @Override
//        public void visit(final ORTerm term) {
//            final SearchTerm<?>[] terms = term.getPattern();
//            queryBuilder.append('(');
//            queryBuilder.append(searchTerm2Query(terms[0]));
//            for (int i = 1; i < terms.length; i++) {
//                queryBuilder.append(" OR ");
//                queryBuilder.append(searchTerm2Query(terms[i]));
//            }
//            queryBuilder.append(')');
//        }
//
//        @Override
//        public void visit(final ReceivedDateTerm term) {
//            final ComparablePattern<Date> comparablePattern = term.getPattern();
//            final long time = comparablePattern.getPattern().getTime();
//            final String name = FIELD_RECEIVED_DATE;
//            switch (comparablePattern.getComparisonType()) {
//            case EQUALS:
//                queryBuilder.append('(').append(name).append(':').append(time).append(')');
//                break;
//            case GREATER_THAN:
//                queryBuilder.append('(').append(name).append(':').append('[').append(time + 1).append(" TO ").append(Long.MAX_VALUE).append(
//                    ']').append(')');
//                break;
//            case LESS_THAN:
//                queryBuilder.append('(').append(name).append(':').append('[').append(Long.MIN_VALUE).append(" TO ").append(time - 1).append(
//                    ']').append(')');
//                break;
//            default:
//                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
//            }
//        }
//
//        @Override
//        public void visit(final SentDateTerm term) {
//            final ComparablePattern<Date> comparablePattern = term.getPattern();
//            final long time = comparablePattern.getPattern().getTime();
//            final String name = FIELD_SENT_DATE;
//            switch (comparablePattern.getComparisonType()) {
//            case EQUALS:
//                queryBuilder.append('(').append(name).append(':').append(time).append(')');
//                break;
//            case GREATER_THAN:
//                queryBuilder.append('(').append(name).append(':').append('[').append(time + 1).append(" TO ").append(Long.MAX_VALUE).append(
//                    ']').append(')');
//                break;
//            case LESS_THAN:
//                queryBuilder.append('(').append(name).append(':').append('[').append(Long.MIN_VALUE).append(" TO ").append(time - 1).append(
//                    ']').append(')');
//                break;
//            default:
//                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
//            }
//        }
//
//        @Override
//        public void visit(final SizeTerm term) {
//            final ComparablePattern<Integer> comparablePattern = term.getPattern();
//            switch (comparablePattern.getComparisonType()) {
//            case EQUALS:
//                queryBuilder.append('(').append(FIELD_SIZE).append(':').append(comparablePattern.getPattern().intValue()).append(')');
//                break;
//            case GREATER_THAN:
//                queryBuilder.append('(').append(FIELD_SIZE).append(':').append('[').append(comparablePattern.getPattern().intValue() + 1).append(
//                    " TO ").append(Integer.MAX_VALUE).append(']').append(')');
//                break;
//            case LESS_THAN:
//                queryBuilder.append('(').append(FIELD_SIZE).append(':').append('[').append(0).append(" TO ").append(
//                    comparablePattern.getPattern().intValue() - 1).append(']').append(')');
//                break;
//            default:
//                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
//            }
//        }
//
//        @Override
//        public void visit(final SubjectTerm term) {
//            final Set<Locale> knownLocales = KNOWN_LOCALES;
//            final List<String> names = new ArrayList<String>(knownLocales.size());
//            final StringBuilder tmp = new StringBuilder(FIELD_SUBJECT_PREFIX); // 8
//            for (final Locale loc : knownLocales) {
//                tmp.setLength(8);
//                tmp.append(loc.getLanguage());
//                names.add(tmp.toString());
//            }
//            stringPattern(names, term.getPattern());
//        }
//
//        @Override
//        public void visit(final ToTerm term) {
//            stringPattern(Arrays.asList(FIELD_TO_PERSONAL, FIELD_TO_ADDR), term.getPattern());
//        }
//
//        private void stringPattern(final List<String> names, final String pattern) {
//            queryBuilder.append('(');
//            queryBuilder.append(names.get(0)).append(':').append('"').append(pattern).append('"');
//            for (int i = 1; i < names.size(); i++) {
//                queryBuilder.append(" OR ");
//                queryBuilder.append(names.get(i)).append(':').append('"').append(pattern).append('"');
//            }
//            queryBuilder.append(')');
//        }
//
//    }
//
//    /**
//     * Initializes a new {@link SearchTerm2Query}.
//     */
//    private SearchTerm2Query() {
//        super();
//    }
//
//    /**
//     * Transforms specified search term to a query.
//     * 
//     * @param searchTerm The search term
//     * @return The resulting query
//     */
//    public static StringBuilder searchTerm2Query(final SearchTerm<?> searchTerm) {
//        if (null == searchTerm) {
//            return null;
//        }
//        final SearchTerm2QueryVisitor visitor = new SearchTerm2QueryVisitor();
//        searchTerm.accept(visitor);
//        return visitor.queryBuilder;
//    }

}
