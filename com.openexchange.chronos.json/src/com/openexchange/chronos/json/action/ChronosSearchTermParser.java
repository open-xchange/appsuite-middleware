/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.json.action;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.SearchTermFields;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchExceptionMessages;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;

/**
 * The {@link ChronosSearchTermParser}. Parses {@link JSONArray} objects to {@link SearchTerm}s
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ChronosSearchTermParser extends SearchTermParser {

    @SuppressWarnings("hiding")
    public static final ChronosSearchTermParser INSTANCE = new ChronosSearchTermParser();

    private ChronosSearchTermParser() {
        super();
    }

    @Override
    public SearchTerm<?> parseSearchTerm(JSONArray jsonArray) throws OXException {
        if (null == jsonArray) {
            return null;
        }
        String operation = getOperation(jsonArray);
        CompositeOperation compositeOperation = CompositeOperation.getCompositeOperation(operation);
        return compositeOperation == null ? parseSingleOperation(jsonArray, operation) : parseCompositeOperation(jsonArray, compositeOperation);
    }

    @Override
    protected Operand<?> parseOperand(JSONObject operand) throws OXException {
        String fieldName = operand.optString(SearchTermFields.FIELD);
        if (Strings.isEmpty(fieldName)) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_FIELD.create(SearchTermFields.FIELD);
        }
        EventField field = EventMapper.getInstance().getMappedField(fieldName);
        if (null == field) {
            throw SearchExceptionMessages.PARSING_FAILED_UNSUPPORTED_OPERAND.create(fieldName);
        }
        switch (field) {
            case SUMMARY:
            case LOCATION:
            case DESCRIPTION:
            case ORGANIZER:
            case ATTENDEES:
            case URL:
            case UID:
            case TIMESTAMP:
            case CREATED:
            case CREATED_BY:
            case LAST_MODIFIED:
            case MODIFIED_BY:
            case SEQUENCE:
            case CATEGORIES:
            case COLOR:
            case RECURRENCE_RULE:
            case TRANSP:
            case STATUS:
            case CLASSIFICATION:
                return new ColumnFieldOperand<EventField>(field);
            default:
                throw SearchExceptionMessages.PARSING_FAILED_UNSUPPORTED_OPERAND.create(fieldName);
        }
    }

    ///////////////////////////////////////// HELPERS /////////////////////////////////////////

    /**
     * Extracts the operation from the specified {@link JSONArray}
     *
     * @param jsonArray The {@link JSONArray}
     * @return The operation
     * @throws OXException if the operation cannot be extracted
     */
    private static String getOperation(JSONArray jsonArray) throws OXException {
        try {
            return jsonArray.getString(0);
        } catch (JSONException e) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_OPERATION.create(e, new Object[0]);
        }
    }

    /**
     * Parses the specified single operation and returns it as a {@link SearchTerm}
     *
     * @param jsonArray The {@link JSONArray} containing the operation
     * @param operation The operation
     * @return The {@link SearchTerm}
     * @throws OXException if an unknown operation is encountered
     */
    private SearchTerm<?> parseSingleOperation(JSONArray jsonArray, String operation) throws OXException {
        SingleOperation singleOperation = SingleOperation.getSingleOperation(operation);
        if (null == singleOperation) {
            throw SearchExceptionMessages.UNKNOWN_OPERATION.create(operation);
        }
        return parseSingleOperands(jsonArray, singleOperation.newInstance(), singleOperation.getMaxOperands());
    }

    /**
     * Parses the specified {@link CompositeOperation}
     *
     * @param jsonArray The {@link JSONArray} containing the operation
     * @param compositeOperation The {@link CompositeOperation} to parse
     * @return The {@link SearchTerm}
     * @throws OXException if an error is occurred
     */
    private SearchTerm<?> parseCompositeOperation(JSONArray jsonArray, CompositeOperation compositeOperation) throws OXException {
        return parseCompositeOperands(jsonArray, compositeOperation.newInstance(), compositeOperation.getMaxTerms());
    }
}
