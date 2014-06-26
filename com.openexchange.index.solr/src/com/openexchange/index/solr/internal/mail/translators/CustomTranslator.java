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

package com.openexchange.index.solr.internal.mail.translators;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.index.IndexField;
import com.openexchange.index.solr.internal.LuceneQueryTools;
import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.querybuilder.Configuration;
import com.openexchange.index.solr.internal.querybuilder.QueryTranslator;
import com.openexchange.index.solr.internal.querybuilder.TranslationException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomTranslator.class);

    private Configuration config;

    private String name;

    private FieldConfiguration fieldConfig;

    @Override
    public void init(String name, Configuration config, FieldConfiguration fieldConfig) throws TranslationException {
        this.name = name;
        this.config = config;
        this.fieldConfig = fieldConfig;
    }

    @Override
    public String translate(Object o) throws TranslationException {
        if (o instanceof SearchTerm<?>) {
            TranslatorVisitor visitor = new TranslatorVisitor(name, config, fieldConfig);
            ((SearchTerm<?>) o).accept(visitor);
            return visitor.queryBuilder.toString();
        }

        throw new TranslationException("The given object must be of type com.openexchange.mail.search.SearchTerm<?>.");
    }

    private static final class TranslatorVisitor implements SearchTermVisitor {

        private final Configuration config;

        private final StringBuilder queryBuilder;

        private final String name;

        private final FieldConfiguration fieldConfig;

        public TranslatorVisitor(String name, Configuration config, FieldConfiguration fieldConfig) {
            super();
            this.name = name;
            this.config = config;
            this.fieldConfig = fieldConfig;
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
            TranslatorVisitor visitor = new TranslatorVisitor(name, config, fieldConfig);
            terms[0].accept(visitor);
            queryBuilder.append(visitor.queryBuilder);
            for (int i = 1; i < terms.length; i++) {
                queryBuilder.append(" AND ");
                SearchTerm<?> searchTerm = terms[i];
                visitor = new TranslatorVisitor(name, config, fieldConfig);
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
            TranslatorVisitor visitor = new TranslatorVisitor(name, config, fieldConfig);
            terms[0].accept(visitor);
            queryBuilder.append(visitor.queryBuilder);
            for (int i = 1; i < terms.length; i++) {
                queryBuilder.append(" OR ");
                SearchTerm<?> searchTerm = terms[i];
                visitor = new TranslatorVisitor(name, config, fieldConfig);
                searchTerm.accept(visitor);
                queryBuilder.append(visitor.queryBuilder);
            }
            queryBuilder.append(')');
        }

        @Override
        public void visit(NOTTerm term) {
            queryBuilder.append("NOT (");
            TranslatorVisitor visitor = new TranslatorVisitor(name, config, fieldConfig);
            term.getPattern().accept(visitor);
            queryBuilder.append(visitor.queryBuilder);
            queryBuilder.append(')');
        }

        @Override
        public void visit(FromTerm term) {
            appendStringTerm(MailIndexField.FROM, term);
        }

        @Override
        public void visit(ToTerm term) {
            appendStringTerm(MailIndexField.TO, term);
        }

        @Override
        public void visit(CcTerm term) {
            appendStringTerm(MailIndexField.CC, term);
        }

        @Override
        public void visit(BccTerm term) {
            appendStringTerm(MailIndexField.BCC, term);
        }

        @Override
        public void visit(SubjectTerm term) {
            appendStringTerm(MailIndexField.SUBJECT, term);
        }

        @Override
        public void visit(BodyTerm term) {
            appendStringTerm(MailIndexField.CONTENT, term);
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
                appendFlag(MailIndexField.FLAG_ANSWERED, set);
            }
            if ((flags & MailMessage.FLAG_DELETED) > 0) {
                appendFlag(MailIndexField.FLAG_DELETED, set);
            }
            if ((flags & MailMessage.FLAG_DRAFT) > 0) {
                appendFlag(MailIndexField.FLAG_DRAFT, set);
            }
            if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
                appendFlag(MailIndexField.FLAG_FLAGGED, set);
            }
            if ((flags & MailMessage.FLAG_RECENT) > 0) {
                appendFlag(MailIndexField.FLAG_RECENT, set);
            }
            if ((flags & MailMessage.FLAG_SEEN) > 0) {
                appendFlag(MailIndexField.FLAG_SEEN, set);
            }
            if ((flags & MailMessage.FLAG_USER) > 0) {
                appendFlag(MailIndexField.FLAG_USER, set);
            }
            if ((flags & MailMessage.FLAG_SPAM) > 0) {
                appendFlag(MailIndexField.FLAG_SPAM, set);
            }
            if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
                appendFlag(MailIndexField.FLAG_FORWARDED, set);
            }
            if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
                appendFlag(MailIndexField.FLAG_READ_ACK, set);
            }
            queryBuilder.delete(off, off + andConcat.length()); // Delete first >>" AND "<< prefix
            queryBuilder.append(')');
        }

        private void appendFlag(MailIndexField mailField, boolean value) {
            Set<String> solrFields = fieldConfig.getSolrFields(mailField);
            if (solrFields == null || solrFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter {}. Skipping this field in search query...", mailField);
                return;
            }

            String andConcat = " AND ";
            queryBuilder.append(andConcat).append(solrFields.iterator().next()).append(':').append(value);
        }

        @Override
        public void visit(SizeTerm term) {
            Set<String> solrFields = fieldConfig.getSolrFields(MailIndexField.SIZE);
            if (solrFields == null || solrFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter {}. Skipping this field in search query...", MailIndexField.SIZE);
                return;
            }

            ComparablePattern<Integer> comparablePattern = term.getPattern();
            String name = solrFields.iterator().next();
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
            Set<String> solrFields = fieldConfig.getSolrFields(MailIndexField.SENT_DATE);
            if (solrFields == null || solrFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter " + MailIndexField.SENT_DATE.toString() + ". Skipping this field in search query...");
                return;
            }

            ComparablePattern<Date> comparablePattern = term.getPattern();
            String name = solrFields.iterator().next();
            long time = comparablePattern.getPattern().getTime();
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
            Set<String> solrFields = fieldConfig.getSolrFields(MailIndexField.RECEIVED_DATE);
            if (solrFields == null || solrFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter " + MailIndexField.RECEIVED_DATE.toString() + ". Skipping this field in search query...");
                return;
            }

            ComparablePattern<Date> comparablePattern = term.getPattern();
            String name = solrFields.iterator().next();
            long time = comparablePattern.getPattern().getTime();
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

        private void appendStringTerm(IndexField indexField, SearchTerm<String> term) {
            Set<String> solrFields = fieldConfig.getSolrFields(indexField);
            if (solrFields == null || solrFields.isEmpty()) {
                LOG.warn("Did not find index fields for field {}. Skipping this field in search query...", indexField);
                return;
            }

            String orig = term.getPattern();
            if (orig == null) {
                return;
            }
            
            orig = orig.trim();
            boolean isPhrase = false;
            if (orig.length() > 1 && orig.startsWith("\"") && orig.endsWith("\"")) {
                orig = orig.substring(1, orig.length() - 1);
                isPhrase = true;
            }
            
            if (orig.length() == 0) {
                return;
            }
            
            String pattern = LuceneQueryTools.escapeButWildcards(orig);
            Iterator<String> it = solrFields.iterator();
            queryBuilder.append('(');
            queryBuilder.append(it.next()).append(':').append(pattern);
            while (it.hasNext()) {
                String solrField = it.next();
                queryBuilder.append(" OR ");
                queryBuilder.append(solrField).append(':');
                if (isPhrase) {
                    queryBuilder.append("\"");
                    queryBuilder.append(pattern);
                    queryBuilder.append("\"");
                } else {
                    queryBuilder.append(pattern);
                }
            }
            queryBuilder.append(')');
        }
    }

}
