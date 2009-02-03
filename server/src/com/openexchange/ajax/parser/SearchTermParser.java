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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import org.json.JSONObject;
import com.openexchange.ajax.fields.SearchTermFields;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchException;
import com.openexchange.search.SearchExceptionMessages;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnOperand;
import com.openexchange.search.internal.operands.ConstantOperand;

/**
 * {@link SearchTermParser}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchTermParser {

    /**
     * Initializes a new {@link SearchTermParser}.
     */
    private SearchTermParser() {
        super();
    }

    public static void parseSingleOperands(final SingleSearchTerm singleSearchTerm, final JSONObject jsonObject, final int maxTerms) throws SearchException {
        if (!jsonObject.hasAndNotNull(SearchTermFields.OPERANDS)) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_OPERANDS.create(new Object[0]);
        }
        final JSONArray array = jsonObject.optJSONArray(SearchTermFields.OPERANDS);
        final int len = array.length();
        if (len < 1) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        } else if (len > maxTerms) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        }
        for (int i = 0; i < len; i++) {
            final JSONObject operand = array.optJSONObject(i);
            if (null == operand) {
                singleSearchTerm.addOperand(parseConstantOperand(array.optString(i)));
            } else {
                singleSearchTerm.addOperand(parseOperand(operand));
            }
        }
    }

    private static ConstantOperand<?> parseConstantOperand(final String s) throws NumberFormatException {
        if (s == null) {
            return ConstantOperand.NULL;
        }
        if ("true".equalsIgnoreCase(s)) {
            return new ConstantOperand<Boolean>(Boolean.TRUE);
        } else if ("false".equalsIgnoreCase(s)) {
            return new ConstantOperand<Boolean>(Boolean.FALSE);
        } else if (isInteger(s)) {
            return new ConstantOperand<Integer>(Integer.valueOf(s));
        } else if (isLong(s)) {
            return new ConstantOperand<Long>(Long.valueOf(s));
        } else {
            return new ConstantOperand<String>(s);
        }
    }

    private static Operand<?> parseOperand(final JSONObject operand) throws SearchException {
        if (!operand.hasAndNotNull(SearchTermFields.TYPE)) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_FIELD.create(SearchTermFields.TYPE);
        }
        if (!operand.hasAndNotNull(SearchTermFields.VALUE)) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_FIELD.create(SearchTermFields.VALUE);
        }
        final String type = operand.optString(SearchTermFields.TYPE);
        final String value = operand.optString(SearchTermFields.VALUE);
        if (Operand.Type.COLUMN.isType(type)) {
            return new ColumnOperand(value);
        }
        return parseConstantOperand(value);
    }

    private static boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private static boolean isLong(final String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parses specified search term JSON object.
     * 
     * @param jsonObject The search term JSON object.
     * @return The parsed instance of search term.
     * @throws SearchException If parsing fails.
     */
    public static SearchTerm<?> parse(final JSONObject jsonObject) throws SearchException {
        if (null == jsonObject) {
            return null;
        }
        if (!jsonObject.hasAndNotNull(SearchTermFields.OPERATION)) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_OPERATION.create(new Object[0]);
        }
        final SearchTerm<?> retval;
        final String operation = jsonObject.optString(SearchTermFields.OPERATION);
        final CompositeOperation compositeOperation = CompositeOperation.getCompositeOperation(operation);
        if (null == compositeOperation) {
            final SingleOperation singleOperation = SingleOperation.getSingleOperation(operation);
            if (null == singleOperation) {
                throw SearchExceptionMessages.UNKNOWN_OPERATION.create(operation);
            }
            final SingleSearchTerm singleSearchTerm = singleOperation.newInstance();
            parseSingleOperands(singleSearchTerm, jsonObject, singleOperation.getMaxOperands());
            retval = singleSearchTerm;
        } else {
            final CompositeSearchTerm compositeSearchTerm = compositeOperation.newInstance();
            parseCompositeOperands(compositeSearchTerm, jsonObject, compositeOperation.getMaxTerms());
            retval = compositeSearchTerm;
        }
        return retval;
    }

    private static void parseCompositeOperands(final CompositeSearchTerm compositeSearchTerm, final JSONObject jsonObject, final int maxTerms) throws SearchException {
        if (!jsonObject.hasAndNotNull(SearchTermFields.OPERANDS)) {
            throw SearchExceptionMessages.PARSING_FAILED_MISSING_OPERANDS.create(new Object[0]);
        }
        final JSONArray array = jsonObject.optJSONArray(SearchTermFields.OPERANDS);
        final int len = array.length();
        if (len < 1) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        } else if (len > maxTerms) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create(new Object[0]);
        }
        for (int i = 0; i < len; i++) {
            final SearchTerm<?> term = parse(array.optJSONObject(i));
            if (null != term) {
                compositeSearchTerm.addSearchTerm(term);
            }
        }
    }

    /*-
     * 
     * For testing purpose
     * 
    public static void main(final String[] args) {
        try {
            final JSONObject equals = new JSONObject(
                "{\"operation\":\"equals\",\"operands\":[{\"type\":\"column\",\"value\":\"" + ContactFields.FIRST_NAME + "\"},\"Herbert\"]}");

            final SearchTerm<?> term = SearchTermParser.parse(equals);
            System.out.println(term);

            final JSONObject lt = new JSONObject(
                "{\"operation\":\"lt\",\"operands\":[{\"type\":\"column\",\"value\":\"" + ContactFields.COLORLABEL + "\"},5]}");
            final SearchTerm<?> term2 = SearchTermParser.parse(lt);
            System.out.println(term2);

            final JSONObject or = new JSONObject("{\"operation\":\"or\", \"operands\":[" + equals + "," + lt + "]}");
            final SearchTerm<?> term3 = SearchTermParser.parse(or);
            System.out.println(term3);

            final ContactObject co = new ContactObject();
            co.setGivenName("Herbert");
            co.setObjectID(112233);
            co.setLabel(3);

            final SearchService ss = new SearchServiceImpl();

            final long s = System.currentTimeMillis();
            final boolean matches = ss.matches(co, term3, ContactAttributeFetcher.getInstance());
            final long d = System.currentTimeMillis() - s;

            System.out.println("Matches: " + matches + " | Duration: " + d + "msec");
            
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }*/

}
