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

package com.openexchange.find.basic.contacts;

import java.util.List;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.session.ServerSession;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class Utils {

    public static SearchTerm<?> getSearchTerm(ServerSession session, ContactField[] fields, List<String> queries) throws OXException {
        if (queries.isEmpty()) {
            return null;
        }

        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (queries.size() == 1) {
            String query = queries.get(0);
            checkPatternLength(query, minimumSearchCharacters);
            return getTermForFields(fields, query, minimumSearchCharacters);
        } else {
            CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
            int operands = 0;
            for (String query : queries) {
                if (fulfillsLengthConstraint(query, minimumSearchCharacters)) {
                    andTerm.addSearchTerm(getTermForFields(fields, query, minimumSearchCharacters));
                    ++operands;
                }
            }

            if (operands > 0) {
                return andTerm;
            } else {
                // Fallback to avoid aborting the search as a whole
                String query = Strings.join(queries, " ");
                checkPatternLength(query, minimumSearchCharacters);
                return getTermForFields(fields, query, minimumSearchCharacters);
            }
        }
    }

    private static SearchTerm<?> getTermForFields(ContactField[] fields, String query, int minimumSearchCharacters) {
        String pattern = addWildcards(query, true, true);
        CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (ContactField field : fields) {
            orTerm.addSearchTerm(getFieldEqualsPatternTerm(field, pattern));
        }

        return orTerm;
    }

    private static SingleSearchTerm getFieldEqualsPatternTerm(ContactField field, String pattern) {
        SingleSearchTerm searchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
        searchTerm.addOperand(new ContactFieldOperand(field));
        searchTerm.addOperand(new ConstantOperand<String>(pattern));
        return searchTerm;
    }

    private static String addWildcards(String pattern, boolean prepend, boolean append) {
        if ((null == pattern || 0 == pattern.length()) && (append || prepend)) {
            return "*";
        }
        if (null != pattern) {
            if (prepend && '*' != pattern.charAt(0)) {
                pattern = "*" + pattern;
            }
            if (append && '*' != pattern.charAt(pattern.length() - 1)) {
                pattern = pattern + "*";
            }
        }
        return pattern;
    }

    private static boolean fulfillsLengthConstraint(String pattern, int minimumSearchCharacters) throws OXException {
        if (null != pattern && 0 < minimumSearchCharacters && pattern.length() < minimumSearchCharacters) {
            return false;
        }

        return true;
    }

    private static void checkPatternLength(String pattern, int minimumSearchCharacters) throws OXException {
        if (!fulfillsLengthConstraint(pattern, minimumSearchCharacters)) {
            throw FindExceptionCode.QUERY_TOO_SHORT.create(Integer.valueOf(minimumSearchCharacters));
        }
    }

}
