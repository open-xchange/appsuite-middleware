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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
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
 * {@link CustomLuceneTranslator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CustomLuceneTranslator implements QueryTranslator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomLuceneTranslator.class);

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
            return visitor.query.toString();
        }

        throw new TranslationException("The given object must be of type com.openexchange.mail.search.SearchTerm<?>.");
    }


    private static final class TranslatorVisitor implements SearchTermVisitor {

        private final Configuration config;

        private final String name;

        private final FieldConfiguration fieldConfig;

        private Query query;


        public TranslatorVisitor(String name, Configuration config, FieldConfiguration fieldConfig) {
            super();
            this.name = name;
            this.config = config;
            this.fieldConfig = fieldConfig;
        }

        public Query getQuery() {
            return query;
        }

        @Override
        public void visit(ANDTerm term) {
            SearchTerm<?>[] terms = term.getPattern();
            if (terms == null || terms.length == 0) {
                LOG.warn("AND term was empty. Skipping in search query...");
                return;
            }

            BooleanQuery booleanQuery = new BooleanQuery();
            TranslatorVisitor visitor;
            for (int i = 0; i < terms.length; i++) {
                SearchTerm<?> searchTerm = terms[i];
                visitor = new TranslatorVisitor(name, config, fieldConfig);
                searchTerm.accept(visitor);
                Query queryForTerm = visitor.getQuery();
                if (queryForTerm != null) {
                    booleanQuery.add(queryForTerm, Occur.MUST);
                }
            }

            query = booleanQuery;
        }

        @Override
        public void visit(ORTerm term) {
            SearchTerm<?>[] terms = term.getPattern();
            if (terms == null || terms.length == 0) {
                LOG.warn("AND term was empty. Skipping in search query...");
                return;
            }

            BooleanQuery booleanQuery = new BooleanQuery();
            TranslatorVisitor visitor;
            for (int i = 0; i < terms.length; i++) {
                SearchTerm<?> searchTerm = terms[i];
                visitor = new TranslatorVisitor(name, config, fieldConfig);
                searchTerm.accept(visitor);
                Query queryForTerm = visitor.getQuery();
                if (queryForTerm != null) {
                    booleanQuery.add(queryForTerm, Occur.SHOULD);
                }
            }

            query = booleanQuery;
        }

        @Override
        public void visit(NOTTerm term) {
            BooleanQuery booleanQuery = new BooleanQuery();
            TranslatorVisitor visitor = new TranslatorVisitor(name, config, fieldConfig);
            term.getPattern().accept(visitor);
            Query queryForTerm = visitor.getQuery();
            if (queryForTerm != null) {
                booleanQuery.add(queryForTerm, Occur.MUST_NOT);
            }
            query = booleanQuery;
        }

        @Override
        public void visit(FromTerm term) {
            constructQuery(MailIndexField.FROM, term);
        }

        @Override
        public void visit(ToTerm term) {
            constructQuery(MailIndexField.TO, term);
        }

        @Override
        public void visit(CcTerm term) {
            constructQuery(MailIndexField.CC, term);
        }

        @Override
        public void visit(BccTerm term) {
            constructQuery(MailIndexField.BCC, term);
        }

        @Override
        public void visit(SubjectTerm term) {
            constructQuery(MailIndexField.SUBJECT, term);
        }

        @Override
        public void visit(BodyTerm term) {
            constructQuery(MailIndexField.CONTENT, term);
        }

        @Override
        public void visit(FlagTerm term) {
            int flags = term.getPattern().intValue();
            boolean set = flags >= 0;
            if (!set) {
                flags *= -1;
            }

            BooleanQuery booleanQuery = new BooleanQuery();
            if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_ANSWERED, set);
            }
            if ((flags & MailMessage.FLAG_DELETED) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_DELETED, set);
            }
            if ((flags & MailMessage.FLAG_DRAFT) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_DRAFT, set);
            }
            if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_FLAGGED, set);
            }
            if ((flags & MailMessage.FLAG_RECENT) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_RECENT, set);
            }
            if ((flags & MailMessage.FLAG_SEEN) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_SEEN, set);
            }
            if ((flags & MailMessage.FLAG_USER) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_USER, set);
            }
            if ((flags & MailMessage.FLAG_SPAM) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_SPAM, set);
            }
            if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_FORWARDED, set);
            }
            if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
                appendFlag(booleanQuery, MailIndexField.FLAG_READ_ACK, set);
            }

            query = booleanQuery;
        }

        private void appendFlag(BooleanQuery booleanQuery, MailIndexField mailField, boolean value) {
            Set<String> solrFields = fieldConfig.getSolrFields(mailField);
            if (solrFields == null || solrFields.isEmpty()) {
                LOG.warn("Did not find index fields for parameter {}. Skipping this field in search query...", mailField);
                return;
            }

            booleanQuery.add(new TermQuery(new Term(solrFields.iterator().next(), Boolean.toString(value))), Occur.MUST);
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
                    query = new TermQuery(new Term(name, comparablePattern.getPattern().toString()));
                    break;
                case GREATER_THAN:
                    query = NumericRangeQuery.newIntRange(name, comparablePattern.getPattern().intValue(), Integer.MAX_VALUE, false, true);
                    break;
                case LESS_THAN:
                    query = NumericRangeQuery.newIntRange(name, 0, comparablePattern.getPattern().intValue(), true, false);
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
                    query = new TermQuery(new Term(name, String.valueOf(time)));
                    break;
                case GREATER_THAN:
                    query = NumericRangeQuery.newLongRange(name, time, Long.MAX_VALUE, false, true);
                    break;
                case LESS_THAN:
                    query = NumericRangeQuery.newLongRange(name, 0L, time, true, false);
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
                    query = new TermQuery(new Term(name, String.valueOf(time)));
                    break;
                case GREATER_THAN:
                    query = NumericRangeQuery.newLongRange(name, time, Long.MAX_VALUE, false, true);
                    break;
                case LESS_THAN:
                    query = NumericRangeQuery.newLongRange(name, 0L, time, true, false);
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

        private void constructQuery(IndexField indexField, SearchTerm<String> term) {
            Set<String> solrFields = fieldConfig.getSolrFields(indexField);
            if (solrFields == null || solrFields.isEmpty()) {
                LOG.warn("Did not find index fields for field {}. Skipping this field in search query...", indexField);
                return;
            }

            String pattern = LuceneQueryTools.escapeButWildcards(term.getPattern());
            Iterator<String> it = solrFields.iterator();
            if (solrFields.size() == 1) {
                try {
                    QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, it.next(), new StandardAnalyzer(Version.LUCENE_CURRENT));
                    query = parser.parse(pattern);
                } catch (ParseException e) {
                    LOG.warn("Could not parse query.", e);
                }
            } else {
                BooleanQuery booleanQuery = new BooleanQuery();
                while (it.hasNext()) {
                    try {
                        QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, it.next(), new StandardAnalyzer(Version.LUCENE_CURRENT));
                        booleanQuery.add(parser.parse(pattern), Occur.SHOULD);
                    } catch (ParseException e) {
                        LOG.warn("Could not parse query.", e);
                    }
                }
                query = booleanQuery;
            }
        }
    }

}
