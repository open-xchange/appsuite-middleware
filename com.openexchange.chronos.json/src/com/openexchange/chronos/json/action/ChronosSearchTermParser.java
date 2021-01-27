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
