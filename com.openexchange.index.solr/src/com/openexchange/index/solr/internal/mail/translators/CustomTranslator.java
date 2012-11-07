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

package com.openexchange.index.solr.internal.mail.translators;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.index.solr.internal.mail.SolrMailField;
import com.openexchange.index.solr.internal.querybuilder.Configuration;
import com.openexchange.index.solr.internal.querybuilder.QueryTranslator;
import com.openexchange.index.solr.internal.querybuilder.TranslationException;
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
 * {@link CustomTranslator}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CustomTranslator implements QueryTranslator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CustomTranslator.class);

    private Configuration config;
    
    private String name;
    

    @Override
    public void init(String name, Configuration config) throws TranslationException {
        this.name = name;
        this.config = config;
    }

    @Override
    public String translate(Object o) throws TranslationException {
        if (o instanceof SearchTerm<?>) {
            TranslatorVisitor visitor = new TranslatorVisitor(name, config);
            ((SearchTerm<?>) o).accept(visitor);
            return visitor.queryBuilder.toString();
        }

        throw new TranslationException("The given object must be of type com.openexchange.mail.search.SearchTerm<?>.");
    }


    private static final class TranslatorVisitor implements SearchTermVisitor {

        private final Configuration config;

        private final StringBuilder queryBuilder;

        private final String name;


        public TranslatorVisitor(String name, Configuration config) {
            super();
            this.name = name;
            this.config = config;
            queryBuilder = new StringBuilder(48);
        }

        @Override
        public void visit(ANDTerm term) {
            SearchTerm<?>[] terms = term.getPattern();
            if (terms == null || terms.length == 0) {
                LOG.warn("AND term was empty. Skipping in search query...");
                return;
            }

            queryBuilder.append('(');
            TranslatorVisitor visitor = new TranslatorVisitor(name, config);
            terms[0].accept(visitor);
            queryBuilder.append(visitor.queryBuilder);
            for (int i = 1; i < terms.length; i++) {
                queryBuilder.append(" AND ");
                SearchTerm<?> searchTerm = terms[i];
                visitor = new TranslatorVisitor(name, config);
                searchTerm.accept(visitor);
                queryBuilder.append(visitor.queryBuilder);
            }
            queryBuilder.append(')');
        }

        @Override
        public void visit(ORTerm term) {
            SearchTerm<?>[] terms = term.getPattern();
            if (terms == null || terms.length == 0) {
                LOG.warn("OR term was empty. Skipping in search query...");
                return;
            }

            queryBuilder.append('(');
            TranslatorVisitor visitor = new TranslatorVisitor(name, config);
            terms[0].accept(visitor);
            queryBuilder.append(visitor.queryBuilder);
            for (int i = 1; i < terms.length; i++) {
                queryBuilder.append(" OR ");
                SearchTerm<?> searchTerm = terms[i];
                visitor = new TranslatorVisitor(name, config);
                searchTerm.accept(visitor);
                queryBuilder.append(visitor.queryBuilder);
            }
            queryBuilder.append(')');
        }

        @Override
        public void visit(NOTTerm term) {
            queryBuilder.append("NOT (");
            TranslatorVisitor visitor = new TranslatorVisitor(name, config);
            term.getPattern().accept(visitor);
            queryBuilder.append(visitor.queryBuilder);
            queryBuilder.append(')');
        }

        @Override
        public void visit(FromTerm term) {
            String parameterName = SolrMailField.FROM.parameterName();
            appendStringTerm(parameterName, term);
        }

        @Override
        public void visit(ToTerm term) {
            String parameterName = SolrMailField.TO.parameterName();
            appendStringTerm(parameterName, term);
        }

        @Override
        public void visit(CcTerm term) {
            String parameterName = SolrMailField.CC.parameterName();
            appendStringTerm(parameterName, term);
        }

        @Override
        public void visit(BccTerm term) {
            String parameterName = SolrMailField.BCC.parameterName();
            appendStringTerm(parameterName, term);
        }

        @Override
        public void visit(SubjectTerm term) {
            String parameterName = SolrMailField.SUBJECT.parameterName();
            appendStringTerm(parameterName, term);
        }

        @Override
        public void visit(BodyTerm term) {
            String parameterName = SolrMailField.CONTENT.parameterName();
            appendStringTerm(parameterName, term);
        }

        @Override
        public void visit(FlagTerm term) {
            int flags = term.getPattern().intValue();
            boolean set = flags >= 0;
            if (!set) {
                flags *= -1;
            }
            String andConcat = " AND ";
            queryBuilder.append('(');
            int off = queryBuilder.length();
            if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
                appendFlag(SolrMailField.FLAG_ANSWERED, set);
            }
            if ((flags & MailMessage.FLAG_DELETED) > 0) {
                appendFlag(SolrMailField.FLAG_DELETED, set);
            }
            if ((flags & MailMessage.FLAG_DRAFT) > 0) {
                appendFlag(SolrMailField.FLAG_DRAFT, set);
            }
            if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
                appendFlag(SolrMailField.FLAG_FLAGGED, set);
            }
            if ((flags & MailMessage.FLAG_RECENT) > 0) {
                appendFlag(SolrMailField.FLAG_RECENT, set);
            }
            if ((flags & MailMessage.FLAG_SEEN) > 0) {
                appendFlag(SolrMailField.FLAG_SEEN, set);
            }
            if ((flags & MailMessage.FLAG_USER) > 0) {
                appendFlag(SolrMailField.FLAG_USER, set);
            }
            if ((flags & MailMessage.FLAG_SPAM) > 0) {
                appendFlag(SolrMailField.FLAG_SPAM, set);
            }
            if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
                appendFlag(SolrMailField.FLAG_FORWARDED, set);
            }
            if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
                appendFlag(SolrMailField.FLAG_READ_ACK, set);
            }
            queryBuilder.delete(off, off + andConcat.length()); // Delete first >>" AND "<< prefix
            queryBuilder.append(')');
        }

        private void appendFlag(SolrMailField mailField, boolean value) {
            String parameterName = mailField.parameterName();
            List<String> indexFields = config.getIndexFields(parameterName);
            if (indexFields == null || indexFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter " + parameterName + ". Skipping this field in search query...");
                return;
            }

            String andConcat = " AND ";
            queryBuilder.append(andConcat).append(indexFields.get(0)).append(':').append(value);
        }

        @Override
        public void visit(SizeTerm term) {
            String parameterName = SolrMailField.SIZE.parameterName();
            List<String> indexFields = config.getIndexFields(parameterName);
            if (indexFields == null || indexFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter " + parameterName + ". Skipping this field in search query...");
                return;
            }

            ComparablePattern<Integer> comparablePattern = term.getPattern();
            String name = indexFields.get(0);
            switch (comparablePattern.getComparisonType()) {
                case EQUALS:
                    queryBuilder.append('(').append(name).append(':').append(comparablePattern.getPattern().intValue()).append(')');
                    break;
                case GREATER_THAN:
                    queryBuilder.append('(').append(name).append(':').append('[').append(comparablePattern.getPattern().intValue() + 1).append(
                        " TO ").append(Integer.MAX_VALUE).append(']').append(')');
                    break;
                case LESS_THAN:
                    queryBuilder.append('(').append(name).append(':').append('[').append(0).append(" TO ").append(
                        comparablePattern.getPattern().intValue() - 1).append(']').append(')');
                    break;
                default:
                    throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
            }
        }

        @Override
        public void visit(SentDateTerm term) {
            String parameterName = SolrMailField.SENT_DATE.parameterName();
            List<String> indexFields = config.getIndexFields(parameterName);
            if (indexFields == null || indexFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter " + parameterName + ". Skipping this field in search query...");
                return;
            }

            ComparablePattern<Date> comparablePattern = term.getPattern();
            long time = comparablePattern.getPattern().getTime();
            String name = indexFields.get(0);
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

        @Override
        public void visit(ReceivedDateTerm term) {
            String parameterName = SolrMailField.RECEIVED_DATE.parameterName();
            List<String> indexFields = config.getIndexFields(parameterName);
            if (indexFields == null || indexFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter " + parameterName + ". Skipping this field in search query...");
                return;
            }

            ComparablePattern<Date> comparablePattern = term.getPattern();
            long time = comparablePattern.getPattern().getTime();
            String name = indexFields.get(0);
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

        @Override
        public void visit(BooleanTerm term) {
            throw new IllegalStateException("Unsupported search term: " + BooleanTerm.class.getName());
        }

        @Override
        public void visit(HeaderTerm term) {
            throw new IllegalStateException("Unsupported search term: " + HeaderTerm.class.getName());
        }

        private void appendStringTerm(String parameterName, SearchTerm<String> term) {
            Set<String> keys = config.getKeys(name);
            if (!keys.contains(name + '.' + parameterName)) {
                LOG.warn("Did not find key '" + name + '.' + parameterName + "'. Skipping this field in search query...");
                return;
            }
            List<String> indexFields = config.getIndexFields(name + '.' + parameterName);
            if (indexFields == null || indexFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter " + parameterName + ". Skipping this field in search query...");
                return;
            }

            String pattern = term.getPattern();
            queryBuilder.append('(');
            queryBuilder.append(indexFields.get(0)).append(':').append('"').append(pattern).append('"');
            for (int i = 1; i < indexFields.size(); i++) {
                String indexField = indexFields.get(i);
                queryBuilder.append(" OR ");
                queryBuilder.append(indexField).append(':').append('"').append(pattern).append('"');
            }
            queryBuilder.append(')');
        }

    }

}
