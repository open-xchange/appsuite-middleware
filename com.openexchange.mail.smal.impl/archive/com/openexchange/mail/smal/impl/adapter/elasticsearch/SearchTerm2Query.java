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

package com.openexchange.mail.smal.impl.adapter.elasticsearch;

import java.util.Date;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
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
     * @return The resulting query (builder)
     */
    public static QueryBuilder searchTerm2Query(final SearchTerm<?> searchTerm) {
        if (null == searchTerm) {
            return null;
        }
        if (searchTerm instanceof ANDTerm) {
            final ANDTerm andTerm = (ANDTerm) searchTerm;
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            for (final SearchTerm<?> term : andTerm.getPattern()) {
                boolQuery.must(searchTerm2Query(term));
            }
            return boolQuery;
        }
        if (searchTerm instanceof ORTerm) {
            final ORTerm orTerm = (ORTerm) searchTerm;
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            for (final SearchTerm<?> term : orTerm.getPattern()) {
                boolQuery.should(searchTerm2Query(term));
            }
            return boolQuery;
        }
        if (searchTerm instanceof NOTTerm) {
            final NOTTerm notTerm = (NOTTerm) searchTerm;
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.mustNot(searchTerm2Query(notTerm.getPattern()));
            return boolQuery;
        }
        final Object pattern = searchTerm.getPattern();
        if (pattern instanceof String) {
            final String sPattern = (String) pattern;
            if (searchTerm.containsWildcard()) {
                return QueryBuilders.wildcardQuery(getFieldNameFor(searchTerm), sPattern);
            }
            return QueryBuilders.wildcardQuery(getFieldNameFor(searchTerm), "*" + sPattern + "*");
        }
        /*
         * Size term
         */
        if (searchTerm instanceof SizeTerm) {
            final SizeTerm sizeTerm = (SizeTerm) searchTerm;
            final ComparablePattern<Integer> comparablePattern = sizeTerm.getPattern();
            switch (comparablePattern.getComparisonType()) {
            case EQUALS:
                return QueryBuilders.termQuery(Constants.FIELD_SIZE, comparablePattern.getPattern().intValue());
            case GREATER_THAN: {
                final RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(Constants.FIELD_SIZE);
                rangeQuery.gt(comparablePattern.getPattern().intValue());
                return rangeQuery;
            }
            case LESS_THAN: {
                final RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(Constants.FIELD_SIZE);
                rangeQuery.lt(comparablePattern.getPattern().intValue());
                return rangeQuery;
            }
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
            final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_ANSWERED, set));
            }
            if ((flags & MailMessage.FLAG_DELETED) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_DELETED, set));
            }
            if ((flags & MailMessage.FLAG_DRAFT) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_DRAFT, set));
            }
            if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_FLAGGED, set));
            }
            if ((flags & MailMessage.FLAG_RECENT) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_RECENT, set));
            }
            if ((flags & MailMessage.FLAG_SEEN) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_SEEN, set));
            }
            if ((flags & MailMessage.FLAG_USER) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_USER, set));
            }
            if ((flags & MailMessage.FLAG_SPAM) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_SPAM, set));
            }
            if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_FORWARDED, set));
            }
            if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
                boolQuery.must(QueryBuilders.termQuery(Constants.FIELD_FLAG_READ_ACK, set));
            }
            return boolQuery;
        }
        /*
         * Date terms
         */
        boolean isRecDate;
        if ((isRecDate = (searchTerm instanceof ReceivedDateTerm)) || (searchTerm instanceof SentDateTerm)) {
            @SuppressWarnings("unchecked") final SearchTerm<ComparablePattern<java.util.Date>> dateTerm =
                (SearchTerm<ComparablePattern<java.util.Date>>) searchTerm;
            final ComparablePattern<Date> comparablePattern = dateTerm.getPattern();
            switch (comparablePattern.getComparisonType()) {
            case EQUALS:
                return QueryBuilders.termQuery(isRecDate ? Constants.FIELD_RECEIVED_DATE : Constants.FIELD_SENT_DATE, comparablePattern.getPattern().getTime());
            case GREATER_THAN: {
                final RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(isRecDate ? Constants.FIELD_RECEIVED_DATE : Constants.FIELD_SENT_DATE);
                rangeQuery.gt(comparablePattern.getPattern().getTime());
                return rangeQuery;
            }
            case LESS_THAN: {
                final RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(isRecDate ? Constants.FIELD_RECEIVED_DATE : Constants.FIELD_SENT_DATE);
                rangeQuery.lt(comparablePattern.getPattern().getTime());
                return rangeQuery;
            }
            default:
                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
            }
        }
        throw new IllegalStateException("Unknown search term: " + searchTerm.getClass().getName());
    }

    private static String getFieldNameFor(final SearchTerm<?> searchTerm) {
        if (searchTerm instanceof BccTerm) {
            return Constants.FIELD_BCC;
        }
        if (searchTerm instanceof BodyTerm) {
            return Constants.FIELD_BODY;
        }
        if (searchTerm instanceof CcTerm) {
            return Constants.FIELD_CC;
        }
        if (searchTerm instanceof FromTerm) {
            return Constants.FIELD_FROM;
        }
        if (searchTerm instanceof ReceivedDateTerm) {
            return Constants.FIELD_RECEIVED_DATE;
        }
        if (searchTerm instanceof SentDateTerm) {
            return Constants.FIELD_SENT_DATE;
        }
        if (searchTerm instanceof SizeTerm) {
            return Constants.FIELD_SIZE;
        }
        if (searchTerm instanceof SubjectTerm) {
            return Constants.FIELD_SUBJECT;
        }
        if (searchTerm instanceof ToTerm) {
            return Constants.FIELD_TO;
        }
        throw new IllegalStateException("Unsupported search term: " + searchTerm.getClass().getName());
    }

}
