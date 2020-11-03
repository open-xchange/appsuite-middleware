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

package com.openexchange.file.storage.search;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;

/**
 * {@link ToJsonSearchTermVisitor} - A visitor that transforms visited {@link SearchTerm} into JSON
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ToJsonSearchTermVisitor implements SearchTermVisitor {

    private static final String JSON_FIELD = "field";
    private static final String JSON_FILTER = "filter";

    private static final String OP_AND = "and";
    private static final String OP_NOT = "not";
    private static final String OP_OR = "or";
    private static final String OP_EQ = "=";
    private static final String OP_LT = "<";
    private static final String OP_GT = ">";

    private final JSONArray filter;

    /**
     * Initializes a new {@link ToJsonSearchTermVisitor}.
     */
    public ToJsonSearchTermVisitor() {
        this.filter = new JSONArray();
    }

    /**
     * Internal method to add an operator for the given {@link ComparisonType}
     *
     * @param operator The ComparisonType to add as operator
     */
    private void addOperator(ComparisonType operator) {
        switch (operator) {
            case EQUALS:
                addOperator(OP_EQ);
                break;
            case GREATER_THAN:
                addOperator(OP_GT);
                break;
            case LESS_THAN:
                addOperator(OP_LT);
                break;
            default:
                break;
        }
    }

    /**
     * Internal method to add an operator to the JSON
     *
     * @param operator The operator to add
     */
    private void addOperator(String operator) {
        filter.put(operator);
    }

    /**
     * Internal method to add a search field to the JSON
     *
     * @param field The field to add
     * @throws OXException
     */
    private void addField(File.Field field) throws OXException {
        addField(field.getName());
    }

    /**
     * Internal method to add a search field to the JSON
     *
     * @param fieldName The name of the field to add
     * @throws OXException
     */
    private void addField(String fieldName) throws OXException {
        try {
            JSONObject field = new JSONObject();
            field.put(JSON_FIELD, fieldName);
            filter.put(field);
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Internal method to add a search value to the JSON
     *
     * @param value The value to add
     */
    private void addValue(Boolean value) {
        addValue(value.toString());
    }

    /**
     * Internal method to add a search value to the JSON
     *
     * @param value The value to add
     */
    private void addValue(Date value) {
        addValue(String.valueOf(value.getTime()));
    }

    /**
     * Internal method to add a search value to the JSON
     *
     * @param value The value to add
     */
    private void addValue(Number value) {
        addValue(String.valueOf(value));
    }

    /**
     * Internal method to add a search value to the JSON
     *
     * @param value The value to add
     */
    private void addValue(String operator) {
        filter.put(operator);
    }

    /**
     * Internal method to add a sub-pattern(a nested SearchTerm like AND, OR, NOT etc) to the JSON
     *
     * @param sub The sub-pattern to add
     * @throws OXException
     */
    private void addSubPattern(SearchTerm<?> sub) throws OXException {
        ToJsonSearchTermVisitor subVisitor = new ToJsonSearchTermVisitor();
        sub.visit(subVisitor);
        filter.put(subVisitor.getJSONFilter());
    }

    /**
     * Gets the inner filter as JSONArray
     *
     * @return The filter as JSONArray
     */
    public JSONArray getJSONFilter() {
        return filter;
    }

    /**
     * Creates the final JSON object
     *
     * @return The final JSON object representing the visited {@link SearchTerm}
     * @throws OXException
     */
    public JSONObject createJSON() throws OXException {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FILTER, getJSONFilter());
            return jsonObject;
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void visit(AndTerm term) throws OXException {
        addOperator(OP_AND);
        for (SearchTerm<?> t : term.getPattern()) {
            addSubPattern(t);
        }
    }

    @Override
    public void visit(OrTerm term) throws OXException {
        addOperator(OP_OR);
        for (SearchTerm<?> t : term.getPattern()) {
            addSubPattern(t);
        }
    }

    @Override
    public void visit(NotTerm term) throws OXException {
        addOperator(OP_NOT);
        addSubPattern(term.getPattern());
    }

    @Override
    public void visit(MetaTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.MEDIA_META);
        addValue(term.getPattern());
    }

    @Override
    public void visit(NumberOfVersionsTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.NUMBER_OF_VERSIONS);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(LastModifiedUtcTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.LAST_MODIFIED_UTC);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(ColorLabelTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.COLOR_LABEL);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(CurrentVersionTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.CURRENT_VERSION);
        addValue(term.getPattern());
    }

    @Override
    public void visit(VersionCommentTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.VERSION_COMMENT);
        addOperator(term.getPattern());
    }

    @Override
    public void visit(FileMd5SumTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.FILE_MD5SUM);
        addValue(term.getPattern());
    }

    @Override
    public void visit(LockedUntilTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.LOCKED_UNTIL);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(CategoriesTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.CATEGORIES);
        addValue(term.getPattern());
    }

    @Override
    public void visit(SequenceNumberTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.SEQUENCE_NUMBER);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(FileMimeTypeTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.FILE_MIMETYPE);
        addValue(term.getPattern());
    }

    @Override
    public void visit(FileNameTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.FILENAME);
        addValue(term.getPattern());
    }

    @Override
    public void visit(LastModifiedTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.LAST_MODIFIED);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(CreatedTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.CREATED);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(ModifiedByTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.MODIFIED_BY);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(TitleTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.TITLE);
        addValue(term.getPattern());
    }

    @Override
    public void visit(VersionTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.VERSION);
        addValue(term.getPattern());
    }

    @Override
    public void visit(ContentTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.CONTENT);
        addValue(term.getPattern());
    }

    @Override
    public void visit(FileSizeTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.FILE_SIZE);
        addValue(String.valueOf(term.getPattern().getPattern()));
    }

    @Override
    public void visit(DescriptionTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.DESCRIPTION);
        addValue(term.getPattern());
    }

    @Override
    public void visit(UrlTerm term) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.URL);
        addValue(term.getPattern());
    }

    @Override
    public void visit(CreatedByTerm term) throws OXException {
        addOperator(term.getPattern().getComparisonType());
        addField(File.Field.CREATED_BY);
        addValue(term.getPattern().getPattern());
    }

    @Override
    public void visit(MediaDateTerm mediaDateTerm) throws OXException {
        addOperator(mediaDateTerm.getPattern().getComparisonType());
        addField(File.Field.MEDIA_DATE);
        addValue(mediaDateTerm.getPattern().getPattern());
    }

    @Override
    public void visit(CaptureDateTerm captureDateTerm) throws OXException {
        addOperator(captureDateTerm.getPattern().getComparisonType());
        addField(File.Field.CAPTURE_DATE);
        addValue(captureDateTerm.getPattern().getPattern());
    }

    @Override
    public void visit(CameraIsoSpeedTerm cameraIsoSpeedTerm) throws OXException {
        addOperator(cameraIsoSpeedTerm.getPattern().getComparisonType());
        addField(File.Field.CAMERA_ISO_SPEED);
        addValue(cameraIsoSpeedTerm.getPattern().getPattern());
    }

    @Override
    public void visit(CameraApertureTerm cameraApertureTerm) throws OXException {
        addOperator(cameraApertureTerm.getPattern().getComparisonType());
        addField(File.Field.CAMERA_APERTURE);
        addValue(cameraApertureTerm.getPattern().getPattern());
    }

    @Override
    public void visit(CameraExposureTimeTerm cameraExposureTimeTerm) throws OXException {
        addOperator(cameraExposureTimeTerm.getPattern().getComparisonType());
        addField(File.Field.CAMERA_EXPOSURE_TIME);
        addValue(cameraExposureTimeTerm.getPattern().getPattern());
    }

    @Override
    public void visit(CameraFocalLengthTerm cameraFocalLengthTerm) throws OXException {
        addOperator(cameraFocalLengthTerm.getPattern().getComparisonType());
        addField(File.Field.CAMERA_FOCAL_LENGTH);
        addValue(cameraFocalLengthTerm.getPattern().getPattern());
    }

    @Override
    public void visit(WidthTerm widthTerm) throws OXException {
        addOperator(widthTerm.getPattern().getComparisonType());
        addField(File.Field.WIDTH);
        addValue(widthTerm.getPattern().getPattern());
    }

    @Override
    public void visit(HeightTerm heightTerm) throws OXException {
        addOperator(heightTerm.getPattern().getComparisonType());
        addField(File.Field.HEIGHT);
        addValue(heightTerm.getPattern().getPattern());
    }

    @Override
    public void visit(CameraModelTerm cameraModelTerm) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.CAMERA_MODEL);
        addValue(cameraModelTerm.getPattern());
    }

    @Override
    public void visit(CameraMakeTerm cameraMakeTerm) throws OXException {
        addOperator(OP_EQ);
        addField(File.Field.CAMERA_MAKE);
        addValue(cameraMakeTerm.getPattern());
    }
}
