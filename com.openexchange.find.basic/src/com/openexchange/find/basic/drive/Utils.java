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

package com.openexchange.find.basic.drive;

import static com.openexchange.java.Strings.isEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.search.ComparablePattern;
import com.openexchange.file.storage.search.ComparisonType;
import com.openexchange.file.storage.search.ContentTerm;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileMimeTypeTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.FileSizeTerm;
import com.openexchange.file.storage.search.LastModifiedTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.file.storage.search.VersionCommentTerm;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.basic.drive.BasicDriveDriver.Comparison;
import com.openexchange.find.drive.DriveConstants;
import com.openexchange.find.facet.Filter;
import com.openexchange.groupware.infostore.DocumentMetadata;
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
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(3);
            terms.add(new FileNameTerm(query));
            terms.add(new TitleTerm(query, true, true));
            terms.add(new DescriptionTerm(query, true, true));
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
            return new FileMimeTypeTerm(query);
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
        } else if (Constants.FIELD_TIME.equals(field)) {
            final Pair<Comparison, Long> pair = parseTimeQuery(query);
            return buildTimeTerm(pair.getFirst(), pair.getSecond().longValue());
        }
        throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
    }

    /**
     * Gets the search term for given field and queries.
     *
     * @param field The field identifier
     * @param queries The queries
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If field is unknown
     */
    public static SearchTerm<?> termForField(final String field, final List<String> queries) throws OXException {
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

            return new OrTerm(terms);
        }

        return termForQuery(field, queries.iterator().next());
    }

    /**
     * Gets the search term for given fields and queries.
     *
     * @param fields The field identifiers
     * @param queries The queries
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If a field is unknown
     */
    public static SearchTerm<?> termFor(final List<String> fields, final List<String> queries) throws OXException {
        final int size = fields.size();
        if (size > 1) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(size);
            for (final String field : fields) {
                final SearchTerm<?> term = termForField(field, queries);
                if (null != term) {
                    terms.add(term);
                }
            }

            if (terms.isEmpty()) {
                return null;
            }

            return new OrTerm(terms);
        }

        return termForField(fields.iterator().next(), queries);
    }

    /**
     * Gets the search term for specified filter.
     *
     * @param filter The filter
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If filter is invalid
     */
    public static SearchTerm<?> termFor(final Filter filter) throws OXException {
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

        return termFor(fields, queries);
    }

    public static File documentMetadata2File(final DocumentMetadata doc) {
        File file = new File() {

            @Override
            public String getProperty(String key) {
                return doc.getProperty(key);
            }

            @Override
            public Set<String> getPropertyNames() {
                return doc.getPropertyNames();
            }

            @Override
            public Date getLastModified() {
                return doc.getLastModified();
            }

            @Override
            public void setLastModified(Date now) {
                doc.setLastModified(now);
            }

            @Override
            public Date getCreated() {
                return doc.getCreationDate();
            }

            @Override
            public void setCreated(Date creationDate) {
                doc.setCreationDate(creationDate);
            }

            @Override
            public int getModifiedBy() {
                return doc.getModifiedBy();
            }

            @Override
            public void setModifiedBy(int lastEditor) {
                doc.setModifiedBy(lastEditor);
            }

            @Override
            public String getFolderId() {
                return String.valueOf(doc.getFolderId());
            }

            @Override
            public void setFolderId(String folderId) {
                doc.setFolderId(Long.parseLong(folderId));
            }

            @Override
            public String getTitle() {
                return doc.getTitle();
            }

            @Override
            public void setTitle(String title) {
                doc.setTitle(title);
            }

            @Override
            public String getVersion() {
                return String.valueOf(doc.getVersion());
            }

            @Override
            public void setVersion(String version) {
                doc.setVersion(Integer.parseInt(version));
            }

            @Override
            public String getContent() {
                return doc.getCategories();
            }

            @Override
            public long getFileSize() {
                return doc.getFileSize();
            }

            @Override
            public void setFileSize(long length) {
                doc.setFileSize(length);
            }

            @Override
            public String getFileMIMEType() {
                return doc.getFileMIMEType();
            }

            @Override
            public void setFileMIMEType(String type) {
                doc.setFileMIMEType(type);
            }

            @Override
            public String getFileName() {
                return doc.getFileName();
            }

            @Override
            public void setFileName(String fileName) {
                doc.setFileName(fileName);
            }

            @Override
            public String getId() {
                return String.valueOf(doc.getId());
            }

            @Override
            public void setId(String id) {
                doc.setId(Integer.parseInt(id));
            }

            @Override
            public int getCreatedBy() {
                return doc.getCreatedBy();
            }

            @Override
            public void setCreatedBy(int cretor) {
                doc.setCreatedBy(cretor);
            }

            @Override
            public String getDescription() {
                return doc.getDescription();
            }

            @Override
            public void setDescription(String description) {
                doc.setDescription(description);
            }

            @Override
            public String getURL() {
                return doc.getURL();
            }

            @Override
            public void setURL(String url) {
                doc.setURL(url);
            }

            @Override
            public long getSequenceNumber() {
                return doc.getSequenceNumber();
            }

            @Override
            public String getCategories() {
                return doc.getCategories();
            }

            @Override
            public void setCategories(String categories) {
                doc.setCategories(categories);
            }

            @Override
            public Date getLockedUntil() {
                return doc.getLockedUntil();
            }

            @Override
            public void setLockedUntil(Date lockedUntil) {
                doc.setLockedUntil(lockedUntil);
            }

            @Override
            public String getFileMD5Sum() {
                return doc.getFileMD5Sum();
            }

            @Override
            public void setFileMD5Sum(String sum) {
                doc.setFileMD5Sum(sum);
            }

            @Override
            public int getColorLabel() {
                return doc.getColorLabel();
            }

            @Override
            public void setColorLabel(int color) {
                doc.setColorLabel(color);
            }

            @Override
            public boolean isCurrentVersion() {
                return doc.isCurrentVersion();
            }

            @Override
            public void setIsCurrentVersion(boolean bool) {
                doc.setIsCurrentVersion(bool);
            }

            @Override
            public String getVersionComment() {
                return doc.getVersionComment();
            }

            @Override
            public void setVersionComment(String string) {
                doc.setVersionComment(string);
            }

            @Override
            public void setNumberOfVersions(int numberOfVersions) {
                doc.setNumberOfVersions(numberOfVersions);
            }

            @Override
            public int getNumberOfVersions() {
                return doc.getNumberOfVersions();
            }

            @Override
            public Map<String, Object> getMeta() {
                return Collections.emptyMap();
            }

            @Override
            public void setMeta(Map<String, Object> properties) {
                // nothing to do
            }

            @Override
            public File dup() {
                // not needed here
                return null;
            }

            @Override
            public void copyInto(File other) {
                // not needed here
            }

            @Override
            public void copyFrom(File other) {
                // not needed here
            }

            @Override
            public void copyInto(File other, Field... fields) {
                // not needed here
            }

            @Override
            public void copyFrom(File other, Field... fields) {
                // not needed here
            }

            @Override
            public Set<Field> differences(File other) {
                // not needed here
                return null;
            }

            @Override
            public boolean equals(File other, Field criterium, Field... criteria) {
                // not needed here
                return false;
            }

            @Override
            public boolean matches(String pattern, Field... fields) {
                // not needed here
                return false;
            }

        };
        return file;
    }

    private final static Pattern pattern = Pattern.compile("([<>=]) ([\\d.]+)([GMK]B)", Pattern.CASE_INSENSITIVE);

    private static long parseFilesizeQuery(String query) throws OXException {
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

    private static ComparisonType parseComparisonType(final String query) throws OXException {
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

    private static Pair<Comparison, Long> parseTimeQuery(final String query) throws OXException {
        if (Strings.isEmpty(query)) {
            throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, Constants.FIELD_TIME);
        }

        Comparison comparison;
        long timestamp;
        Calendar cal = new GregorianCalendar(TimeZones.UTC);
        if (DriveConstants.FACET_VALUE_LAST_WEEK.equals(query)) {
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else if (DriveConstants.FACET_VALUE_LAST_MONTH.equals(query)) {
            cal.add(Calendar.MONTH, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else if (DriveConstants.FACET_VALUE_LAST_YEAR.equals(query)) {
            cal.add(Calendar.YEAR, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else {
            /*
             * This block just preserves the code, as we likely have to implement
             * custom time ranges in the future. Currently this else path should
             * never be called.
             *
             * Idea: Introduce an additional custom time facet that might be set,
             * but is not part of autocomplete responses. If it is set, we also
             * should not deliver the normal time facet in autocomplete responses.
             *
             * {
             *   'facet':'time_custom',
             *   'value':'>=12345678900'
             * }
             */
            char[] chars = query.toCharArray();
            String sTimestamp;
            if (chars.length > 1) {
                int offset = 0;
                final char firstChar = chars[0];
                if (firstChar == '<') {
                    offset = 1;
                    comparison = Comparison.LOWER_THAN;
                    if (chars[1] == '=') {
                        offset = 2;
                        comparison = Comparison.LOWER_EQUALS;
                    }
                } else if (firstChar == '>') {
                    offset = 1;
                    comparison = Comparison.GREATER_THAN;
                    if (chars[1] == '=') {
                        offset = 2;
                        comparison = Comparison.GREATER_EQUALS;
                    }
                } else {
                    comparison = Comparison.EQUALS;
                }

                sTimestamp = String.copyValueOf(chars, offset, chars.length);
            } else {
                comparison = Comparison.EQUALS;
                sTimestamp = query;
            }

            try {
                timestamp = Long.parseLong(sTimestamp);
            } catch (NumberFormatException e) {
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, Constants.FIELD_TIME);
            }
        }

        return new Pair<Comparison, Long>(comparison, Long.valueOf(timestamp));
    }

    private static SearchTerm<?> buildTimeTerm(final Comparison comparison, final long timestamp) {
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

}
