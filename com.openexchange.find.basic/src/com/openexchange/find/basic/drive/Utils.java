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

package com.openexchange.find.basic.drive;

import static com.openexchange.find.basic.drive.Constants.QUERY_FIELDS;
import static com.openexchange.find.common.CommonConstants.FIELD_DATE;
import static com.openexchange.java.Strings.isEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.search.AndTerm;
import com.openexchange.file.storage.search.ComparablePattern;
import com.openexchange.file.storage.search.ComparisonType;
import com.openexchange.file.storage.search.ContentTerm;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileMimeTypeTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.FileSizeTerm;
import com.openexchange.file.storage.search.LastModifiedTerm;
import com.openexchange.file.storage.search.NotTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.file.storage.search.VersionCommentTerm;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.basic.common.Comparison;
import com.openexchange.find.common.CommonConstants;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.util.TimeFrame;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.java.util.TimeZones;


/**
 * {@link Utils} - Utilities for drive search.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utils {

    /**
     * Initializes a new {@link Utils}.
     */
    private Utils() {
        super();
    }

    public static enum OP {
        AND, OR
    }

    public static SearchTerm<?> prepareSearchTerm(final SearchRequest searchRequest) throws OXException {
        final List<SearchTerm<?>> facetTerms = new LinkedList<SearchTerm<?>>();
        for (DriveFacetType type : DriveFacetType.values()) {
            final List<ActiveFacet> facets = searchRequest.getActiveFacets(type);
            if (facets != null && !facets.isEmpty()) {
                final Pair<OP, OP> ops = operationsFor(type);
                final List<Filter> filters = new LinkedList<Filter>();
                for (final ActiveFacet facet : facets) {
                    final Filter filter = facet.getFilter();
                    if (filter != Filter.NO_FILTER) {
                        filters.add(filter);
                    }
                }

                facetTerms.add(prepareFilterTerm(filters, ops.getFirst(), ops.getSecond()));
            }
        }

        List<ActiveFacet> dateFacets = searchRequest.getActiveFacets(CommonFacetType.DATE);
        if (dateFacets != null && !dateFacets.isEmpty()) {
            ActiveFacet dateFacet = dateFacets.get(0);
            Filter dateFilter = dateFacet.getFilter();
            if (dateFilter == Filter.NO_FILTER) {
                String timeFramePattern = dateFacet.getValueId();
                TimeFrame timeFrame = TimeFrame.valueOf(timeFramePattern);
                if (timeFrame == null) {
                    throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(timeFramePattern, FIELD_DATE);
                }

                Comparison fromComparison;
                Comparison toComparison;
                if (timeFrame.isInclusive()) {
                    fromComparison = Comparison.GREATER_EQUALS;
                    toComparison = Comparison.LOWER_EQUALS;
                } else {
                    fromComparison = Comparison.GREATER_THAN;
                    toComparison = Comparison.LOWER_THAN;
                }

                long from = timeFrame.getFrom();
                long to = timeFrame.getTo();
                if (to < 0L) {
                    facetTerms.add(buildDateTerm(fromComparison, from));
                }

                SearchTerm<?> fromTerm = buildDateTerm(fromComparison, from);
                SearchTerm<?> toTerm = buildDateTerm(toComparison, to);
                facetTerms.add(new AndTerm(Arrays.<SearchTerm<?>> asList(fromTerm, toTerm)));
            } else {
                facetTerms.add(prepareFilterTerm(Collections.singletonList(dateFilter), OP.OR, OP.OR));
            }
        }

        final SearchTerm<?> queryTerm = prepareQueryTerm(searchRequest.getQueries());
        SearchTerm<?> facetTerm = null;
        if (!facetTerms.isEmpty()) {
            if (facetTerms.size() == 1) {
                facetTerm = facetTerms.get(0);
            } else {
                facetTerm = new AndTerm(facetTerms);
            }
        }

        if (facetTerm == null || queryTerm == null) {
            return (facetTerm == null) ? queryTerm : facetTerm;
        }

        return new AndTerm(Arrays.<SearchTerm<?>> asList(queryTerm, facetTerm));
    }

    public static Pair<OP, OP> operationsFor(DriveFacetType type) {
        OP fieldOP = OP.OR;
        OP queryOP = OP.OR;
        switch (type) {
            case FILE_NAME:
                fieldOP = OP.OR;
                queryOP = OP.AND;
                break;

            case FILE_DESCRIPTION:
                fieldOP = OP.OR;
                queryOP = OP.AND;
                break;

            case FILE_CONTENT:
                fieldOP = OP.OR;
                queryOP = OP.AND;
                break;

            default:
                break;
        }

        return new Pair<OP, OP>(fieldOP, queryOP);
    }

    public static SearchTerm<?> prepareQueryTerm(final List<String> queries) throws OXException {
        if (queries == null || queries.isEmpty()) {
            return null;
        }

        return Utils.termFor(QUERY_FIELDS, queries, OP.OR, OP.AND);
    }

    public static SearchTerm<?> prepareFilterTerm(final List<Filter> filters, final OP fieldOP, final OP queryOP) throws OXException {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        final int size = filters.size();
        if (size == 1) {
            return Utils.termFor(filters.get(0), fieldOP, queryOP);
        }

        final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(size);
        for (final Filter filter : filters) {
            terms.add(Utils.termFor(filter, fieldOP, queryOP));
        }
        return new AndTerm(terms);
    }

    /**
     * Gets the search term for given field and query
     *
     * @param field The field identifier
     * @param query The query
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If field is unknown
     */
    public static SearchTerm<?> termForQuery(final String field, final String query) throws OXException {
        if (isEmpty(field) || isEmpty(query)) {
            return null;
        }

        if (Constants.FIELD_GLOBAL.equals(field)) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(4);
            terms.add(new FileNameTerm(query));
            terms.add(new TitleTerm(query, true, true));
            terms.add(new DescriptionTerm(query, true, true));
            terms.add(new VersionCommentTerm(query, true));
            return new OrTerm(terms);
        } else if (Constants.FIELD_FILE_NAME.equals(field)) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(2);
            terms.add(new FileNameTerm(query));
            terms.add(new TitleTerm(query, true, true));
            return new OrTerm(terms);
        } else if (Constants.FIELD_FILE_DESC.equals(field)) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(2);
            terms.add(new DescriptionTerm(query, true, true));
            terms.add(new VersionCommentTerm(query, true));
            return new OrTerm(terms);
        } else if (Constants.FIELD_FILE_CONTENT.equals(field)) {
            return new ContentTerm(query, true, true);
        } else if (Constants.FIELD_FILE_TYPE.equals(field)) {
            return buildFileTypeTerm(query);
        } else if (Constants.FIELD_FILE_SIZE.equals(field)) {
            final long bytes = parseFilesizeQuery(query);
            final ComparisonType comparison = parseComparisonType(query);
            final ComparablePattern<Number> pattern = new ComparablePattern<Number>() {

                @Override
                public ComparisonType getComparisonType() {
                    return comparison;
                }

                @Override
                public Number getPattern() {
                    return Long.valueOf(bytes);
                }

            };
            return new FileSizeTerm(pattern);
        } else if (CommonConstants.FIELD_DATE.equals(field)) {
            final Pair<Comparison, Long> pair = parseDateQuery(query);
            return buildDateTerm(pair.getFirst(), pair.getSecond().longValue());
        }
        throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
    }

    /**
     * Gets the search term for given field and queries.
     *
     * @param field The field identifier
     * @param queries The queries
     * @param queryOP The operation to concatenate multiple queries with
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If field is unknown
     */
    public static SearchTerm<?> termForField(final String field, final List<String> queries, final OP queryOP) throws OXException {
        final int size = queries.size();
        if (size > 1) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(size);
            for (final String query : queries) {
                final SearchTerm<?> term = termForQuery(field, query);
                if (null != term) {
                    terms.add(term);
                }
            }

            if (terms.isEmpty()) {
                return null;
            }

            if (queryOP == OP.OR) {
                return new OrTerm(terms);
            } else {
                return new AndTerm(terms);
            }
        }

        return termForQuery(field, queries.iterator().next());
    }

    /**
     * Gets the search term for given fields and queries.
     *
     * @param fields The field identifiers
     * @param queries The queries
     * @param fieldOP The operation to concatenate multiple fields with
     * @param queryOP The operation to concatenate multiple queries with
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If a field is unknown
     */
    public static SearchTerm<?> termFor(final List<String> fields, final List<String> queries, final OP fieldOP, final OP queryOP) throws OXException {
        final int size = fields.size();
        if (size > 1) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(size);
            for (final String field : fields) {
                final SearchTerm<?> term = termForField(field, queries, queryOP);
                if (null != term) {
                    terms.add(term);
                }
            }

            if (terms.isEmpty()) {
                return null;
            }

            if (fieldOP == OP.OR) {
                return new OrTerm(terms);
            } else {
                return new AndTerm(terms);
            }
        }

        return termForField(fields.iterator().next(), queries, queryOP);
    }

    /**
     * Gets the search term for specified filter.
     *
     * @param filter The filter
     * @param fieldOP The operation to concatenate multiple fields with
     * @param queryOP The operation to concatenate multiple queries with
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If filter is invalid
     */
    public static SearchTerm<?> termFor(final Filter filter, final OP fieldOP, final OP queryOP) throws OXException {
        if (null == filter) {
            return null;
        }

        final List<String> fields = filter.getFields();
        if (fields == null || fields.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_FIELDS.create(filter);
        }

        final List<String> queries = filter.getQueries();
        if (queries == null || queries.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_QUERIES.create(filter);
        }

        return termFor(fields, queries, fieldOP, queryOP);
    }

    private final static Pattern pattern = Pattern.compile("([<>=]) ([\\d.]+)([GMK]B)", Pattern.CASE_INSENSITIVE);

    /**
     * Parses the file size.
     *
     * @param query The query to parse the file size from
     * @return The file size in bytes
     * @throws OXException If parsing fails
     */
    public static long parseFilesizeQuery(String query) throws OXException {
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String number = matcher.group(2);
            String suffix = matcher.group(3);
            int power = 0;
            if ("TB".equals(suffix.toUpperCase())) {
                power = 4;
            } else if ("GB".equals(suffix.toUpperCase())) {
                power = 3;
            } else if ("MB".equals(suffix.toUpperCase())) {
                power = 2;
            } else if ("KB".equals(suffix.toUpperCase())) {
                power = 1;
            }
            BigDecimal decimal = new BigDecimal(number);
            return decimal.multiply(BigDecimal.valueOf(1024).pow(power)).longValue();
        }
        throw FindExceptionCode.PARSING_ERROR.create(query);
    }

    /**
     * Parses the comparison type.
     *
     * @param query The query to parse from
     * @return The comparison type
     * @throws OXException If parse attempt fails
     */
    public static ComparisonType parseComparisonType(final String query) throws OXException {
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String comparison = matcher.group(1);
            if (">".equals(comparison)) {
                return ComparisonType.GREATER_THAN;
            } else if ("<".equals(comparison)) {
                return ComparisonType.LESS_THAN;
            } else if ("=".equals(comparison)){
                return ComparisonType.EQUALS;
            }
        }
        throw FindExceptionCode.PARSING_ERROR.create(query);
    }

    /**
     * Parses a date query.
     *
     * @param query The date query to parse
     * @return The parsed date query
     * @throws OXException If parse attempt fails
     */
    public static Pair<Comparison, Long> parseDateQuery(final String query) throws OXException {
        if (Strings.isEmpty(query)) {
            throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, CommonConstants.FIELD_DATE);
        }

        Comparison comparison;
        long timestamp;
        Calendar cal = new GregorianCalendar(TimeZones.UTC);
        if (CommonConstants.QUERY_LAST_WEEK.equals(query)) {
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else if (CommonConstants.QUERY_LAST_MONTH.equals(query)) {
            cal.add(Calendar.MONTH, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else if (CommonConstants.QUERY_LAST_YEAR.equals(query)) {
            cal.add(Calendar.YEAR, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else {
            return null;
        }

        return new Pair<Comparison, Long>(comparison, Long.valueOf(timestamp));
    }

    private static SearchTerm<?> buildDateTerm(final Comparison comparison, final long timestamp) {
        ComparablePattern<Date> pattern = null;
        switch (comparison) {
        case EQUALS:
            pattern = new ComparablePattern<Date>() {

                @Override
                public Date getPattern() {
                    return new Date(timestamp);
                }

                @Override
                public ComparisonType getComparisonType() {
                    return ComparisonType.EQUALS;
                }
            };
            break;
        case GREATER_THAN:
            pattern = new ComparablePattern<Date>() {

                @Override
                public Date getPattern() {
                    return new Date(timestamp);
                }

                @Override
                public ComparisonType getComparisonType() {
                    return ComparisonType.GREATER_THAN;
                }
            };
            break;
        case LOWER_THAN:
            pattern = new ComparablePattern<Date>() {

                @Override
                public Date getPattern() {
                    return new Date(timestamp);
                }

                @Override
                public ComparisonType getComparisonType() {
                    return ComparisonType.LESS_THAN;
                }
            };
            break;
        case GREATER_EQUALS:
            pattern = new ComparablePattern<Date>() {

                @Override
                public Date getPattern() {
                    return new Date(timestamp - 1);
                }

                @Override
                public ComparisonType getComparisonType() {
                    return ComparisonType.GREATER_THAN;
                }
            };
            break;
        case LOWER_EQUALS:
            pattern = new ComparablePattern<Date>() {

                @Override
                public Date getPattern() {
                    return new Date(timestamp + 1);
                }

                @Override
                public ComparisonType getComparisonType() {
                    return ComparisonType.LESS_THAN;
                }
            };
            break;
        }

        return null == pattern ? null : new LastModifiedTerm(pattern);
    }

    /**
     * Builds a search term to match the file type based on the supplied query.
     *
     * @param query The query to use, i.e. the type's ID
     * @return The search term
     */
    private static SearchTerm<?> buildFileTypeTerm(String query) {
        String[] patterns;
        if (FileType.DOCUMENTS.getIdentifier().equals(query)) {
            patterns = Constants.FILETYPE_PATTERNS_DOCUMENTS;
        } else if (FileType.IMAGES.getIdentifier().equals(query)) {
            patterns = Constants.FILETYPE_PATTERNS_IMAGES;
        } else if (FileType.VIDEO.getIdentifier().equals(query)) {
            patterns = Constants.FILETYPE_PATTERNS_VIDEO;
        } else if (FileType.AUDIO.getIdentifier().equals(query)) {
            patterns = Constants.FILETYPE_PATTERNS_AUDIO;
        } else if (FileType.OTHER.getIdentifier().equals(query)) {
            // negate all other patterns
            String[][] patternsToNegate = {
                Constants.FILETYPE_PATTERNS_DOCUMENTS,
                Constants.FILETYPE_PATTERNS_IMAGES,
                Constants.FILETYPE_PATTERNS_VIDEO,
                Constants.FILETYPE_PATTERNS_AUDIO
            };
            List<SearchTerm<?>> searchTerms = new ArrayList<SearchTerm<?>>();
            for (String[] toNegate : patternsToNegate) {
                for (String pattern : toNegate) {
                    searchTerms.add(new FileMimeTypeTerm(pattern, true, false));
                }
            }
            return new NotTerm(new OrTerm(searchTerms));
        } else {
            patterns = null;
        }
        if (null == patterns || 0 == patterns.length) {
            return new FileMimeTypeTerm(query); // fall back to query
        } else if (1 == patterns.length) {
            return new FileMimeTypeTerm(patterns[0], true, false);
        } else {
            List<SearchTerm<?>> searchTerms = new ArrayList<SearchTerm<?>>(patterns.length);
            for (String pattern : patterns) {
                searchTerms.add(new FileMimeTypeTerm(pattern, true, false));
            }
            return new OrTerm(searchTerms);
        }
    }

}
