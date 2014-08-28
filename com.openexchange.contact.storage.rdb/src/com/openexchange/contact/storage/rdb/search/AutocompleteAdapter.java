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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.rdb.search;

import java.util.List;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.Search;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.java.SimpleTokenizer;
import com.openexchange.tools.StringCollection;

/**
 * {@link AutocompleteAdapter}
 *
 * Helps constructing the database statement for a auto-complete query.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AutocompleteAdapter extends DefaultSearchAdapter {

	private final StringBuilder stringBuilder;

	/**
	 * Initializes a new {@link AutocompleteAdapter}.
	 *
     * @param query The query, as supplied by the client
     * @param requireEmail <code>true</code> if the returned contacts should have at least one e-mail address, <code>false</code>,
     *                     otherwise
     * @param folderIDs The folder IDs, or <code>null</code> if there's no restriction on folders
     * @param contextID The context ID
	 * @param charset The used charset
	 * @throws OXException
	 */
	public AutocompleteAdapter(String query, boolean requireEmail, int[] folderIDs, int contextID, ContactField[] fields, String charset) throws OXException {
		super(charset);
		this.stringBuilder = new StringBuilder(256);
		appendAutocomplete(extractPatterns(query), requireEmail, folderIDs, contextID, fields);
	}

	@Override
	public String getClause() {
		return stringBuilder.toString().trim();
	}

	private void appendAutocomplete(List<String> patterns, boolean requireEmail, int[] folderIDs, int contextID, ContactField[] fields) throws OXException {
	    if (null == patterns || 0 == patterns.size()) {
	        stringBuilder.append(getSelectClause(fields)).append(" WHERE ").append(getContextIDClause(contextID)).append(" AND ")
	            .append(getFolderIDsClause(folderIDs));
	        if (requireEmail) {
	            stringBuilder.append(" AND (").append(getEMailAutoCompleteClause()).append(')');
	        }
	    } else if (1 == patterns.size()) {
	        appendAutocompletePattern(patterns.get(0), requireEmail, folderIDs, contextID, fields);
	    } else {
	        stringBuilder.append("SELECT ");
	        stringBuilder.append("o.").append(Mappers.CONTACT.get(fields[0]).getColumnLabel());
	        for (int i = 1; i < fields.length; i++) {
	            stringBuilder.append(",o.").append(Mappers.CONTACT.get(fields[i]).getColumnLabel());
	        }
	        stringBuilder.append(" FROM (");
	        appendAutocompletePattern("i0", patterns.get(0), requireEmail, folderIDs, contextID, fields);
	        for (int i = 1; i < patterns.size(); i++) {
	            stringBuilder.append(" UNION ALL (");
	            appendAutocompletePattern('i' + String.valueOf(i), patterns.get(i), requireEmail, folderIDs, contextID, fields);
                stringBuilder.append(')');
	        }
	        stringBuilder.append(") AS o GROUP BY ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel())
	            .append(" HAVING COUNT(*) >= ").append(patterns.size());
	    }
   }

    private void appendAutocompletePattern(String pattern, boolean requireEmail, int[] folderIDs, int contextID, ContactField[] fields) throws OXException {
        String contextIDClause = getContextIDClause(contextID);
        String folderIDsClause = getFolderIDsClause(folderIDs);
        String selectClause = getSelectClause(fields);
        boolean first = true;
        for (ContactField field : ALTERNATIVE_INDEXED_FIELDS) {
            if (first) {
                appendComparison(contextIDClause, folderIDsClause, selectClause, field, pattern, requireEmail);
                first = false;
            } else {
                stringBuilder.append(" UNION (");
                appendComparison(contextIDClause, folderIDsClause, selectClause, field, pattern, requireEmail);
                stringBuilder.append(')');
            }
        }
    }

    private void appendAutocompletePattern(String tableAlias, String pattern, boolean requireEmail, int[] folderIDs, int contextID, ContactField[] fields) throws OXException {
        stringBuilder.append("SELECT ");
        stringBuilder.append(tableAlias).append('.').append(Mappers.CONTACT.get(fields[0]).getColumnLabel());
        for (int i = 1; i < fields.length; i++) {
            stringBuilder.append(',').append(tableAlias).append('.').append(Mappers.CONTACT.get(fields[i]).getColumnLabel());
        }
        stringBuilder.append(" FROM (");
        appendAutocompletePattern(pattern, requireEmail, folderIDs, contextID, fields);
        stringBuilder.append(") AS ").append(tableAlias);
    }

    private void appendComparison(String contextIDClause, String folderIDsClause, String selectClause, ContactField field, String pattern, boolean needsEMail) throws OXException {
        stringBuilder.append('(').append(selectClause);
        if (IGNORE_INDEX_CID_FOR_UNIONS && ALTERNATIVE_INDEXED_FIELDS.contains(field)) {
            stringBuilder.append(" IGNORE INDEX (cid)");
        }
        stringBuilder.append(" WHERE ").append(contextIDClause).append(" AND ");
        appendComparison(field, pattern);
        stringBuilder.append(" AND ").append(folderIDsClause);
        if (needsEMail) {
            stringBuilder.append(" AND (").append(getEMailAutoCompleteClause()).append(')');
        }
        stringBuilder.append(')');
    }

    private void appendComparison(ContactField field, String pattern) throws OXException {
        DbMapping<? extends Object, Contact> dbMapping = Mappers.CONTACT.get(field);
        if (null != this.charset) {
            stringBuilder.append("CONVERT(").append(dbMapping.getColumnLabel()).append(" USING ").append(this.charset).append(')');
        } else {
            stringBuilder.append(dbMapping.getColumnLabel());
        }
        if (containsWildcards(pattern)) {
            // use "LIKE" search
            stringBuilder.append(" LIKE ?");
            parameters.add(pattern);
        } else {
            stringBuilder.append("=?");
            parameters.add(pattern);
        }
    }

    /**
     * Extracts the search patterns from the supplied query, appending wildcards as needed, as well as checking the individual pattern
     * length restrictions.
     *
     * @param query The query as supplied by the client
     * @return The patterns
     * @throws OXException
     */
    private static List<String> extractPatterns(String query) throws OXException {
        List<String> patterns = SimpleTokenizer.tokenize(query);
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = StringCollection.prepareForSearch(patterns.get(i), false, true, true);
            Search.checkPatternLength(pattern);
            patterns.set(i, pattern);
        }
        return patterns;
    }

}
