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

package com.openexchange.ajax.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.SearchTermFields;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchExceptionMessages;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.AttachmentOperand;
import com.openexchange.search.internal.operands.AttachmentOperand.AttachmentOperandType;
import com.openexchange.search.internal.operands.ColumnOperand;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.search.internal.operands.HeaderOperand;

/**
 * {@link SearchTermParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchTermParser {

    /**
     * The generic {@link SearchTermParser} instance.
     */
    public static final SearchTermParser INSTANCE = new SearchTermParser();

    /**
     * Parses specified search term JSON array.
     *
     * @param jsonArray The search term JSON array.
     * @return The parsed instance of search term.
     * @throws OXException If parsing fails.
     */
    public static SearchTerm<?> parse(JSONArray jsonArray) throws OXException {
        return INSTANCE.parseSearchTerm(jsonArray);
    }

    /**
     * Parses the single operands from the specified json array and adds them
     * to the specified {@link SingleSearchTerm}
     *
     * @param array The json array containing the operands
     * @param singleSearchTerm the term to add the operands to
     * @param maxTerms the maximum amount of allowed terms
     * @throws OXException if the array does not contain the appropriate amount of terms
     */
    public static void parseSingleOperands(SingleSearchTerm singleSearchTerm, JSONArray array, int maxTerms) throws OXException {
        INSTANCE.parseSingleOperands(array, singleSearchTerm, maxTerms);
    }

    /**
     * Initializes a new {@link SearchTermParser}.
     */
    protected SearchTermParser() {
        super();
    }

    /**
     * Parses the single operands from the specified json array and adds them
     * to the specified {@link SingleSearchTerm}
     *
     * @param array The json array containing the operands
     * @param singleSearchTerm the term to add the operands to
     * @param maxTerms the maximum amount of allowed terms
     * @return The passed single search term reference, with the added operands
     * @throws OXException if the array does not contain the appropriate amount of terms
     */
    public SingleSearchTerm parseSingleOperands(JSONArray array, SingleSearchTerm singleSearchTerm, int maxTerms) throws OXException {
        int len = array.length();
        if (len < 2) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        } else if (len > maxTerms + 1) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        }
        for (int i = 1; i < len; i++) {
            JSONObject operand = array.optJSONObject(i);
            if (null == operand) {
                singleSearchTerm.addOperand(parseConstantOperand(array.opt(i)));
            } else {
                singleSearchTerm.addOperand(parseOperand(operand));
            }
        }
        return singleSearchTerm;
    }

    /**
     * Parses the specified constant operand
     *
     * @param o The constant operand to parse
     * @return The {@link ConstantOperand}
     */
    protected ConstantOperand<?> parseConstantOperand(Object o) {
        if (o == null) {
            return ConstantOperand.NULL;
        }
        return new ConstantOperand<Object>(o);
    }

    /**
     * Parses the specified operand and returns it as {@link Operand}
     *
     * @param operand The {@link JSONObject} containing the operand that shall be parsed
     * @return The parsed {@link Operand}
     * @throws OXException if an invalid search term is encountered
     */
    protected Operand<?> parseOperand(JSONObject operand) throws OXException {
        if (!operand.hasAndNotNull(SearchTermFields.FIELD) && !operand.hasAndNotNull(SearchTermFields.ATTACHMENT) && !operand.hasAndNotNull(SearchTermFields.HEADER)) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
        }

        if (operand.hasAndNotNull(SearchTermFields.FIELD)) {
            return new ColumnOperand(operand.optString(SearchTermFields.FIELD));
        } else if (operand.hasAndNotNull(SearchTermFields.HEADER)) {
            return new HeaderOperand(operand.optString(SearchTermFields.HEADER));
        } else {
            AttachmentOperandType type = AttachmentOperandType.getByName(operand.optString(SearchTermFields.ATTACHMENT));
            if (type == null) {
                throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
            }
            return new AttachmentOperand(type);
        }
    }

    /**
     * Parses specified search term JSON array.
     *
     * @param jsonArray The search term JSON array.
     * @return The parsed instance of search term.
     * @throws OXException If parsing fails.
     */
    public SearchTerm<?> parseSearchTerm(JSONArray jsonArray) throws OXException {
        if (null == jsonArray) {
            return null;
        }

        String operation;
        try {
            operation = jsonArray.getString(0);
        } catch (JSONException e) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_OPERATION.create(e, new Object[0]);
        }

        SearchTerm<?> retval;
        CompositeOperation compositeOperation = CompositeOperation.getCompositeOperation(operation);
        if (null == compositeOperation) {
            SingleOperation singleOperation = SingleOperation.getSingleOperation(operation);
            if (null == singleOperation) {
                throw SearchExceptionMessages.UNKNOWN_OPERATION.create(operation);
            }
            SingleSearchTerm singleSearchTerm = singleOperation.newInstance();
            parseSingleOperands(jsonArray, singleSearchTerm, singleOperation.getMaxOperands());
            retval = singleSearchTerm;
        } else {
            CompositeSearchTerm compositeSearchTerm = compositeOperation.newInstance();
            parseCompositeOperands(jsonArray, compositeSearchTerm, compositeOperation.getMaxTerms());
            retval = compositeSearchTerm;
        }
        return retval;
    }

    /**
     * Parses the composite operands from the specified json array and adds them
     * to the specified {@link CompositeSearchTerm}
     *
     * @param array The {@link JSONArray} the operands
     * @param compositeSearchTerm the term to add the operands to
     * @param maxTerms the maximum amount of allowed terms
     * @return The passed composite search term reference, with the added operands
     * @throws OXException if the array does not contain the appropriate amount of terms
     */
    protected CompositeSearchTerm parseCompositeOperands(JSONArray array, CompositeSearchTerm compositeSearchTerm, int maxTerms) throws OXException {
        int len = array.length();
        if (len < 2) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        } else if (len > maxTerms + 1) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        }
        for (int i = 1; i < len; i++) {
            SearchTerm<?> term = parseSearchTerm(array.optJSONArray(i));
            if (null != term) {
                compositeSearchTerm.addSearchTerm(term);
            }
        }
        return compositeSearchTerm;
    }
}
