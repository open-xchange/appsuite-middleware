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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.search.AndTerm;
import com.openexchange.file.storage.search.CameraApertureTerm;
import com.openexchange.file.storage.search.CameraExposureTimeTerm;
import com.openexchange.file.storage.search.CameraFocalLengthTerm;
import com.openexchange.file.storage.search.CameraIsoSpeedTerm;
import com.openexchange.file.storage.search.CameraMakeTerm;
import com.openexchange.file.storage.search.CameraModelTerm;
import com.openexchange.file.storage.search.CaptureDateTerm;
import com.openexchange.file.storage.search.CategoriesTerm;
import com.openexchange.file.storage.search.ColorLabelTerm;
import com.openexchange.file.storage.search.ComparablePattern;
import com.openexchange.file.storage.search.ComparisonType;
import com.openexchange.file.storage.search.ContentTerm;
import com.openexchange.file.storage.search.CreatedByTerm;
import com.openexchange.file.storage.search.CreatedTerm;
import com.openexchange.file.storage.search.CurrentVersionTerm;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileMd5SumTerm;
import com.openexchange.file.storage.search.FileMimeTypeTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.FileSizeTerm;
import com.openexchange.file.storage.search.HeightTerm;
import com.openexchange.file.storage.search.LastModifiedTerm;
import com.openexchange.file.storage.search.LastModifiedUtcTerm;
import com.openexchange.file.storage.search.LockedUntilTerm;
import com.openexchange.file.storage.search.MediaDateTerm;
import com.openexchange.file.storage.search.MetaTerm;
import com.openexchange.file.storage.search.ModifiedByTerm;
import com.openexchange.file.storage.search.NotTerm;
import com.openexchange.file.storage.search.NumberOfVersionsTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.SequenceNumberTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.file.storage.search.UrlTerm;
import com.openexchange.file.storage.search.VersionCommentTerm;
import com.openexchange.file.storage.search.VersionTerm;
import com.openexchange.file.storage.search.WidthTerm;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchExceptionMessages;
import com.openexchange.search.SingleSearchTerm;

/**
 * {@link FileSearchTermParser}
 *
 * @author <a href="mailto:alexander.schulze-ardey@open-xchange.com">Alexander Schulze-Ardey</a>
 * @since v7.10.5
 */
public class FileSearchTermParser {

    private FileSearchTermParser() {}

    /**
     * Parses com.openexchange.search.SearchTerm<?> to com.openexchange.file.storage.search.SearchTerm<?>
     *
     * @param term
     * @return Parsed term
     * @throws OXException
     */
    public static SearchTerm<?> parseSearchTerm(final com.openexchange.search.SearchTerm<?> term) throws OXException {
        if (null == term) {
            return null;
        }

        if (SingleSearchTerm.class.isInstance(term)) {
            return convertTerm((SingleSearchTerm) term);
        } else if (CompositeSearchTerm.class.isInstance(term)) {
            return convertTerm((CompositeSearchTerm) term);
        } else {
            throw SearchExceptionMessages.SEARCH_FAILED.create("Term is neither single or complex: " + term);
        }
    }

    /**
     * Converts a com.openexchange.search.CompositeSearchTerm to com.openexchange.file.storage.search.SearchTerm<T>.
     *
     * @param term
     * @return
     * @throws OXException
     */
    private static SearchTerm<?> convertTerm(CompositeSearchTerm term) throws OXException {
        List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>();

        for (com.openexchange.search.SearchTerm<?> operand : term.getOperands()) {
            terms.add(parseSearchTerm(operand));
        }

        CompositeSearchTerm.CompositeOperation operation = (CompositeSearchTerm.CompositeOperation) term.getOperation();

        switch (operation) {
            case OR:
                return new OrTerm(terms);
            case AND:
                return new AndTerm(terms);
            case NOT:
                if (terms.size() != 1) {
                    throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
                }
                return new NotTerm(terms.get(0));
            default:
                throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
        }
    }

    /**
     * Converts a com.openexchange.search.SingleSearchTerm to com.openexchange.file.storage.search.SearchTerm<T>.
     *
     * @param term
     * @return
     * @throws OXException
     */
    private static SearchTerm<?> convertTerm(SingleSearchTerm term) throws OXException {
        SingleSearchTerm.SingleOperation operation = (SingleSearchTerm.SingleOperation) term.getOperation();
        Field field = null;
        String query = null;

        for (Operand<?> operand : term.getOperands()) {
            switch (operand.getType()) {
                case COLUMN:
                    field = Field.get((String) operand.getValue());
                    break;
                case CONSTANT:
                    query = (String) operand.getValue();
                    break;
                default:
                    throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
            }
        }

        if (field == null) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
        }

        final ComparisonType comparison = resolveComparison(operation);

        switch (field) {
            case CAMERA_APERTURE:
                return new CameraApertureTerm(getComparablePattern(File.Field.CAMERA_APERTURE, query, comparison));
            case CAMERA_EXPOSURE_TIME:
                return new CameraExposureTimeTerm(getComparablePattern(File.Field.CAMERA_EXPOSURE_TIME, query, comparison));
            case CAMERA_FOCAL_LENGTH:
                return new CameraFocalLengthTerm(getComparablePattern(File.Field.CAMERA_FOCAL_LENGTH, query, comparison));
            case CAMERA_ISO_SPEED:
                return new CameraIsoSpeedTerm(getComparablePattern(File.Field.CAMERA_ISO_SPEED, query, comparison));
            case CAMERA_MAKE:
                ensureComparisonType(File.Field.CAMERA_MAKE, comparison, ComparisonType.EQUALS);
                return new CameraMakeTerm(query);
            case CAMERA_MODEL:
                ensureComparisonType(File.Field.CAMERA_MODEL, comparison, ComparisonType.EQUALS);
                return new CameraModelTerm(query);
            case CAPTURE_DATE:
                return new CaptureDateTerm(getComparableDatePattern(File.Field.CAPTURE_DATE, query, comparison));
            case CATEGORIES:
                ensureComparisonType(File.Field.CATEGORIES, comparison, ComparisonType.EQUALS);
                return new CategoriesTerm(query);
            case COLOR_LABEL:
                return new ColorLabelTerm(getComparablePattern(File.Field.COLOR_LABEL, query, comparison));
            case CONTENT:
                ensureComparisonType(File.Field.CONTENT, comparison, ComparisonType.EQUALS);
                return new ContentTerm(query, true, true);
            case CREATED:
                return new CreatedTerm(getComparableDatePattern(File.Field.CREATED, query, comparison));
            case CREATED_BY:
                return new CreatedByTerm(getComparablePattern(File.Field.CREATED_BY, query, comparison));
            case CURRENT_VERSION:
                boolean currentVersion = Boolean.valueOf(query).booleanValue();
                return new CurrentVersionTerm(currentVersion);
            case DESCRIPTION:
                ensureComparisonType(File.Field.DESCRIPTION, comparison, ComparisonType.EQUALS);
                return new DescriptionTerm(query, true, true);
            case FILENAME:
                ensureComparisonType(File.Field.FILENAME, comparison, ComparisonType.EQUALS);
                return new FileNameTerm(query);
            case FILE_MD5SUM:
                ensureComparisonType(File.Field.FILE_MD5SUM, comparison, ComparisonType.EQUALS);
                return new FileMd5SumTerm(query);
            case FILE_MIMETYPE:
                ensureComparisonType(File.Field.FILE_MIMETYPE, comparison, ComparisonType.EQUALS);
                return new FileMimeTypeTerm(query, true, true);
            case FILE_SIZE:
                return new FileSizeTerm(getComparablePattern(File.Field.FILE_SIZE, query, comparison));
            case HEIGHT:
                return new HeightTerm(getComparablePattern(File.Field.HEIGHT, query, comparison));
            case LAST_MODIFIED:
                return new LastModifiedTerm(getComparableDatePattern(File.Field.LAST_MODIFIED, query, comparison));
            case LAST_MODIFIED_UTC:
                return new LastModifiedUtcTerm(getComparableDatePattern(File.Field.LAST_MODIFIED_UTC, query, comparison));
            case LOCKED_UNTIL:
                return new LockedUntilTerm(getComparableDatePattern(File.Field.LOCKED_UNTIL, query, comparison));
            case MEDIA_DATE:
                return new MediaDateTerm(getComparableDatePattern(File.Field.MEDIA_DATE, query, comparison));
            case META:
                ensureComparisonType(File.Field.META, comparison, ComparisonType.EQUALS);
                return new MetaTerm(query);
            case MODIFIED_BY:
                return new ModifiedByTerm(getComparablePattern(File.Field.MODIFIED_BY, query, comparison));
            case NUMBER_OF_VERSIONS:
                return new NumberOfVersionsTerm(getComparablePattern(File.Field.NUMBER_OF_VERSIONS, query, comparison));
            case SEQUENCE_NUMBER:
                return new SequenceNumberTerm(getComparablePattern(File.Field.SEQUENCE_NUMBER, query, comparison));
            case TITLE:
                ensureComparisonType(File.Field.TITLE, comparison, ComparisonType.EQUALS);
                return new TitleTerm(query, true, true);
            case URL:
                ensureComparisonType(File.Field.URL, comparison, ComparisonType.EQUALS);
                return new UrlTerm(query, true, true);
            case VERSION:
                ensureComparisonType(File.Field.VERSION, comparison, ComparisonType.EQUALS);
                return new VersionTerm(query);
            case VERSION_COMMENT:
                ensureComparisonType(File.Field.VERSION_COMMENT, comparison, ComparisonType.EQUALS);
                return new VersionCommentTerm(query, true);
            case WIDTH:
                return new WidthTerm(getComparablePattern(File.Field.WIDTH, query, comparison));
            case FOLDER_ID:
            case GEOLOCATION:
            case ID:
            case MEDIA_META:
            case MEDIA_STATUS:
            case OBJECT_PERMISSIONS:
            case ORIGIN:
            case SHAREABLE:
            case UNIQUE_ID:
            default:
                // Unknown term for field.
                throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
        }
    }

    /**
     * Resolve com.openexchange.search.SingleSearchTerm.SingleOperation to ComparisonType.
     *
     * @param operation
     * @return
     * @throws OXException
     */
    private static ComparisonType resolveComparison(SingleSearchTerm.SingleOperation operation) throws OXException {
        final ComparisonType comparison;

        SingleSearchTerm.SingleOperation singleOperation = SingleSearchTerm.SingleOperation.getSingleOperation(operation.getOperation());
        if(null == singleOperation) {
            throw SearchExceptionMessages.UNKNOWN_OPERATION.create();
        }
        switch (singleOperation) {
            case EQUALS:
                comparison = ComparisonType.EQUALS;
                break;
            case GREATER_THAN:
                comparison = ComparisonType.GREATER_THAN;
                break;
            case LESS_THAN:
                comparison = ComparisonType.LESS_THAN;
                break;
            case NOT_EQUALS:
            case LESS_OR_EQUAL:
            case ISNULL:
            case GREATER_OR_EQUAL:
            default:
                throw SearchExceptionMessages.UNKNOWN_OPERATION.create();
        }

        return comparison;
    }

    /**
     * Ensures ComparisonType for term.
     *
     * @param field The {@link Field} to check the {@link ComparisonType} for
     * @param comparison The {@link ComparisonType} to check
     * @param allowed The allowed {@link ComparisonType}
     * @throws OXException
     */
    private static void ensureComparisonType(File.Field field, ComparisonType comparison, ComparisonType allowed) throws OXException {
        if (!comparison.equals(allowed)) {
            throw SearchExceptionMessages.SEARCH_FAILED.create(String.format("Unallowed comparison '%s' for field '%s'. Allowed comparison: '%s'", comparison.name(), field.getName(), allowed.name()));
        }
    }

    /**
     * Get pattern for Number comparisons.
     *
     * @param field The {@link Field} to get the {@link ComparablePattern} for
     * @param query The query
     * @param comparison The {@link ComparisonType}
     * @return The {@link ComparablePattern}
     * @throws OXException in case the query is not a number
     */
    private static ComparablePattern<Number> getComparablePattern(File.Field field, String query, ComparisonType comparison) throws OXException {
        final Long value;

        try {
            value = Long.valueOf(query);
        } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
            throw SearchExceptionMessages.SEARCH_FAILED.create(String.format("Unable to parse query '%s' for field '%s' and comparison '%s'", query, field.getName(), comparison.name()));
        }

        return new ComparablePattern<Number>() {

            @Override
            public ComparisonType getComparisonType() {
                return comparison;
            }

            @Override
            public Number getPattern() {
                return value;
            }
        };

    }

    /**
     * Get pattern for date comparisons.
     *
     * @param query
     * @param comparison
     * @return
     * @throws OXException
     */
    private static ComparablePattern<Date> getComparableDatePattern(File.Field field, String query, ComparisonType comparison) throws OXException {
        final long value;

        try {
            value = Long.valueOf(query).longValue();
        } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
            throw SearchExceptionMessages.SEARCH_FAILED.create("Unable to parse query '" + query + "' for field '" + field.getName() + "'and comparison " + comparison.name());
        }

        return new ComparablePattern<Date>() {

            @Override
            public ComparisonType getComparisonType() {
                return comparison;
            }

            @Override
            public Date getPattern() {
                return new Date(value);
            }
        };

    }
}
