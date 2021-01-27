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
