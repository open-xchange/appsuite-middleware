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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.indexedSearch.json.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link MailQuery}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailQuery {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private static final Pattern SPLIT_CSV = Pattern.compile(" *, *");

    private static final String[] FIELDS_STANDARD = { "from", "to", "subject" };

    private static final String[] FIELDS_EXTENDED = { "from", "to", "subject", "cc", "content" };

    /**
     * Parses specified JSON representation of a query.
     *
     * @param jsonQuery The JSON query representation
     * @return The parsed query
     * @throws OXException If parsing fails
     */
    public static MailQuery queryFor(final JSONObject jsonQuery) throws OXException {
        try {
            final String sQuery = jsonQuery.optString("query");
            if (isEmpty(sQuery)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("query");
            }
            final String sFields = jsonQuery.optString("fields");
            if (isEmpty(sFields)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("fields");
            }
            List<SearchTerm<?>> terms;
            List<String> names = null;
            try {
                // Try to parse as JSON array
                terms = Collections.<SearchTerm<?>> singletonList(SearchTermParser.parse(new JSONArray(sQuery)));
            } catch (final JSONException e) {
                // Not a JSON array
                String[] fields;
                if ("standard".equalsIgnoreCase(sFields)) {
                    fields = FIELDS_STANDARD;
                } else if ("extended".equalsIgnoreCase(sFields)) {
                    fields = FIELDS_EXTENDED;
                } else {
                    fields = SPLIT_CSV.split(sFields, 0);
                }
                names = Arrays.asList(fields);
                terms = standardSearchTerm(sQuery, fields);
            }
            // Return query
            return new MailQuery(terms, names, optFullName(jsonQuery), optAccountId(jsonQuery));
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static String optFullName(final JSONObject jsonQuery) throws JSONException {
        if (jsonQuery.hasAndNotNull("folder")) {
            return jsonQuery.getString("folder");
        }
        if (jsonQuery.hasAndNotNull("fullName")) {
            return jsonQuery.getString("fullName");
        }
        return null;
    }

    private static int optAccountId(final JSONObject jsonQuery) throws JSONException {
        if (jsonQuery.hasAndNotNull("accountId")) {
            return jsonQuery.getInt("accountId");
        }
        return 0; // MailAccount.DEFAULT_ID
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static List<SearchTerm<?>> standardSearchTerm(final String pattern, final String... fields) throws JSONException, OXException {
        if (null == fields || 0 == fields.length) {
            return Collections.<SearchTerm<?>> emptyList();
        }
        final int length = fields.length;
        final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(length);
        for (int i = 0; i < length; i++) {
            final SingleSearchTerm searchTerm = SingleOperation.EQUALS.newInstance();
            final JSONArray ja = new JSONArray();
            ja.put(SingleOperation.EQUALS.toString());
            ja.put(pattern);
            ja.put(new JSONObject().put("field", fields[i].toLowerCase(DEFAULT_LOCALE)));
            SearchTermParser.parseSingleOperands(searchTerm, ja, 2);
            terms.add(searchTerm);
        }
        return terms;
    }

    /*-
     * --------------------- Member stuff ---------------------
     */

    private final String fullName;

    private final int accountId;

    private final List<SearchTerm<?>> terms;

    private final List<String> names;

    /**
     * Initializes a new {@link MailQuery}.
     */
    private MailQuery(final List<SearchTerm<?>> terms, final List<String> names, final String fullName, final int accountId) {
        super();
        this.terms = terms == null ? Collections.<SearchTerm<?>> emptyList() : Collections.unmodifiableList(terms);
        this.names = names == null ? Collections.<String> emptyList() : Collections.unmodifiableList(names);
        this.fullName = fullName;
        this.accountId = accountId;
    }

    /**
     * Gets the search terms
     *
     * @return The search terms
     */
    public List<SearchTerm<?>> getTerms() {
        return terms;
    }

    /**
     * Gets the account ID
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the full name
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the names
     *
     * @return The names
     */
    public List<String> getNames() {
        return names;
    }

}
