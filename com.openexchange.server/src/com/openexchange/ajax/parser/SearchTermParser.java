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
    public static SearchTerm<?> parse(final JSONArray jsonArray) throws OXException {
        return INSTANCE.parseSearchTerm(jsonArray);
    }

    public static void parseSingleOperands(final SingleSearchTerm singleSearchTerm, final JSONArray array, final int maxTerms) throws OXException {
        INSTANCE.parseSingleOperands(array, singleSearchTerm, maxTerms);
    }

    /**
     * Initializes a new {@link SearchTermParser}.
     */
    protected SearchTermParser() {
        super();
    }

    public void parseSingleOperands(final JSONArray array, final SingleSearchTerm singleSearchTerm, final int maxTerms) throws OXException {
        final int len = array.length();
        if (len < 2) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        } else if (len > maxTerms + 1) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        }
        for (int i = 1; i < len; i++) {
            final JSONObject operand = array.optJSONObject(i);
            if (null == operand) {
                singleSearchTerm.addOperand(parseConstantOperand(array.optString(i)));
            } else {
                singleSearchTerm.addOperand(parseOperand(operand));
            }
        }
    }

    protected ConstantOperand<?> parseConstantOperand(final String s) throws NumberFormatException {
        if (s == null) {
            return ConstantOperand.NULL;
        }
/* TODO: Get type information from other operands.
 * Disabled guessing because e.g. "True" is a female first name.
        if ("true".equalsIgnoreCase(s)) {
            return new ConstantOperand<Boolean>(Boolean.TRUE);
        } else if ("false".equalsIgnoreCase(s)) {
            return new ConstantOperand<Boolean>(Boolean.FALSE);
        } else if (isInteger(s)) {
            return new ConstantOperand<Integer>(Integer.valueOf(s));
        } else if (isLong(s)) {
            return new ConstantOperand<Long>(Long.valueOf(s));
        } else {
*/
            return new ConstantOperand<String>(s);
/*
        }
*/
    }

    protected Operand<?> parseOperand(final JSONObject operand) throws OXException {
        if (!operand.hasAndNotNull(SearchTermFields.FIELD) && !operand.hasAndNotNull(SearchTermFields.ATTACHMENT)) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
        }

        if (operand.hasAndNotNull(SearchTermFields.FIELD)) {
            return new ColumnOperand(operand.optString(SearchTermFields.FIELD));
        } else if (operand.hasAndNotNull(SearchTermFields.HEADER)) {
            return new HeaderOperand(operand.optString(SearchTermFields.HEADER));
        } else {
            return new AttachmentOperand(AttachmentOperandType.valueOf(operand.optString(SearchTermFields.ATTACHMENT)));
        }
    }

    private boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private boolean isLong(final String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parses specified search term JSON array.
     *
     * @param jsonArray The search term JSON array.
     * @return The parsed instance of search term.
     * @throws OXException If parsing fails.
     */
    public SearchTerm<?> parseSearchTerm(final JSONArray jsonArray) throws OXException {
        if (null == jsonArray) {
            return null;
        }

        final String operation;
        try {
            operation = jsonArray.getString(0);
        } catch (final JSONException e) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_OPERATION.create(e, new Object[0]);
        }

        final SearchTerm<?> retval;
        final CompositeOperation compositeOperation = CompositeOperation.getCompositeOperation(operation);
        if (null == compositeOperation) {
            final SingleOperation singleOperation = SingleOperation.getSingleOperation(operation);
            if (null == singleOperation) {
                throw SearchExceptionMessages.UNKNOWN_OPERATION.create(operation);
            }
            final SingleSearchTerm singleSearchTerm = singleOperation.newInstance();
            parseSingleOperands(jsonArray, singleSearchTerm, singleOperation.getMaxOperands());
            retval = singleSearchTerm;
        } else {
            final CompositeSearchTerm compositeSearchTerm = compositeOperation.newInstance();
            parseCompositeOperands(jsonArray, compositeSearchTerm, compositeOperation.getMaxTerms());
            retval = compositeSearchTerm;
        }
        return retval;
    }

    protected void parseCompositeOperands(final JSONArray array, final CompositeSearchTerm compositeSearchTerm, final int maxTerms) throws OXException {
        final int len = array.length();
        if (len < 2) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        } else if (len > maxTerms + 1) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        }
        for (int i = 1; i < len; i++) {
            final SearchTerm<?> term = parseSearchTerm(array.optJSONArray(i));
            if (null != term) {
                compositeSearchTerm.addSearchTerm(term);
            }
        }
    }
}
