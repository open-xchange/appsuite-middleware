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

package com.openexchange.contact.storage.rdb.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.storage.rdb.internal.RdbServiceLookup;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.contact.storage.rdb.sql.Table;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.Search;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.java.SimpleTokenizer;
import com.openexchange.java.Strings;
import com.openexchange.tools.StringCollection;

/**
 * {@link AutocompleteAdapter}
 *
 * Helps constructing the database statement for a auto-complete query.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AutocompleteAdapter extends DefaultSearchAdapter {

    private static final int MAX_PATTERNS = 5;
	private final StringBuilder stringBuilder;
    private final AutocompleteParameters autoCompleteParameters;
    private static final String AUTOCOMPLETE_CONFIGURATION = "com.openexchange.contact.autocomplete.fields";
    private static final Logger LOG = LoggerFactory.getLogger(AutocompleteAdapter.class);

    /**
     * Initializes a new {@link AutocompleteAdapter}.
     *
     * @param query The query, as supplied by the client
     * @param parameters The {@link AutocompleteParameters}
     * @param folderIDs The folder identifiers, or <code>null</code> if there's no restriction on folders
     * @param contextID The context identifier
     * @param charset The used charset
     * @throws OXException
     */
    public AutocompleteAdapter(String query, AutocompleteParameters parameters, int[] folderIDs, int contextID, ContactField[] fields, String charset) throws OXException {
        this(query, parameters, folderIDs, contextID, fields, charset, true);
    }

    AutocompleteAdapter(String query, AutocompleteParameters parameters, int[] folderIDs, int contextID, ContactField[] fields, String charset, boolean checkPatternLength) throws OXException {
        super(charset);
        this.stringBuilder = new StringBuilder(2048);
        this.autoCompleteParameters = parameters;
        /*
         * extract patterns & remove too short patterns
         */
        List<String> patterns = SimpleTokenizer.tokenize(query);
        if (checkPatternLength) {
            for (Iterator<String> iterator = patterns.iterator(); iterator.hasNext();) {
                String pattern = iterator.next();
                try {
                    Search.checkPatternLength(pattern);
                } catch (OXException e) {
                    if (ContactExceptionCodes.TOO_FEW_SEARCH_CHARS.equals(e)) {
                        addIgnoredPatternWarning(pattern, parameters);
                        iterator.remove();
                    } else {
                        throw e;
                    }
                }
            }
        }
        /*
         * prepare & optimize patterns, restricting the number of used patterns
         */
        patterns = preparePatterns(patterns);
        if (MAX_PATTERNS < patterns.size()) {
            for (int i = 5; i < patterns.size(); i++) {
                addIgnoredPatternWarning(patterns.get(i), parameters);
            }
            patterns = patterns.subList(0, 5);
        }
        String preparedQuery = StringCollection.prepareForSearch(query, false, true, true);
        if (false == patterns.contains(preparedQuery)) {
            patterns.add(preparedQuery);
        }
        appendAutocomplete(patterns, parameters, folderIDs, contextID, fields);
    }

    private static void addIgnoredPatternWarning(String ignoredPattern, AutocompleteParameters parameters) {
        if (null != parameters) {
            parameters.addWarning(ContactExceptionCodes.IGNORED_PATTERN.create(ignoredPattern));
        }
    }

	@Override
	public StringBuilder getClause() {
		return Strings.trim(stringBuilder);
	}

	private void appendAutocomplete(List<String> patterns, AutocompleteParameters parameters, int[] folderIDs, int contextID, ContactField[] fields) throws OXException {
        boolean requireEmail = parameters.getBoolean(AutocompleteParameters.REQUIRE_EMAIL, true);
        boolean ignoreDistributionLists = parameters.getBoolean(AutocompleteParameters.IGNORE_DISTRIBUTION_LISTS, false);
        boolean ignoreNonWebmailUsers = false; // TODO: Maybe for future use
        int forUser = parameters.getInteger(AutocompleteParameters.USER_ID, -1);
		if (null == patterns || 0 == patterns.size()) {
            stringBuilder.append(getSelectClause(fields, forUser)).append(" WHERE ").append(getContextIDClause(contextID)).append(" AND ")
                .append(getFolderIDsClause(folderIDs));
	        if (requireEmail) {
	            stringBuilder.append(" AND (").append(getEMailAutoCompleteClause(ignoreDistributionLists)).append(')');
	        } else if (ignoreDistributionLists) {
	        	stringBuilder.append(" AND (").append(getIgnoreDistributionListsClause()).append(')');
	        }
	    } else if (1 == patterns.size()) {
            appendAutocompletePattern(patterns.get(0), requireEmail, ignoreDistributionLists, ignoreNonWebmailUsers, folderIDs, contextID, fields, forUser);
	    } else {
	        stringBuilder.append("SELECT ");
	        stringBuilder.append("o.").append(Mappers.CONTACT.get(fields[0]).getColumnLabel());
	        for (int i = 1; i < fields.length; i++) {
	            stringBuilder.append(",o.").append(Mappers.CONTACT.get(fields[i]).getColumnLabel());
	        }
            stringBuilder.append(",").append(Table.OBJECT_USE_COUNT).append(".value");
	        stringBuilder.append(" FROM (");
            appendAutocompletePattern("i0", patterns.get(0), requireEmail, ignoreDistributionLists, ignoreNonWebmailUsers, folderIDs, contextID, fields, forUser);
	        for (int i = 1; i < patterns.size(); i++) {
	            stringBuilder.append(" UNION ALL (");
                appendAutocompletePattern('i' + String.valueOf(i), patterns.get(i), requireEmail, ignoreDistributionLists, ignoreNonWebmailUsers, folderIDs, contextID, fields);
                stringBuilder.append(')');
	        }
            stringBuilder.append(") AS o");
            stringBuilder.append(" LEFT JOIN ").append(Table.OBJECT_USE_COUNT).append(" ON ").append("o.cid=").append(Table.OBJECT_USE_COUNT)
                .append(".cid AND ").append(autoCompleteParameters.getInteger(AutocompleteParameters.USER_ID, -1)).append("=").append(Table.OBJECT_USE_COUNT).append(".user AND ")
                .append("o.fid=").append(Table.OBJECT_USE_COUNT).append(".folder AND ")
                .append("o.intfield01=").append(Table.OBJECT_USE_COUNT).append(".object ");
            stringBuilder.append("GROUP BY ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel())
	            .append(" HAVING COUNT(*) >= ").append(patterns.size());
	    }
   }

    private void appendAutocompletePattern(String pattern, boolean requireEmail, boolean ignoreDistributionLists, boolean ignoreNonWebmailUsers, int[] folderIDs, int contextID, ContactField[] fields, int forUser) throws OXException {
        String contextIDClause = getContextIDClause(contextID);
        String folderIDsClause = getFolderIDsClause(folderIDs);
        String selectClause = getSelectClause(fields, forUser);

        boolean first = true;
        EnumSet<ContactField> enumFields = getConfiguredIndexFields();
        for (ContactField field : enumFields) {
            if (first) {
                appendComparison(contextIDClause, folderIDsClause, selectClause, field, pattern, requireEmail, ignoreDistributionLists);
                first = false;
            } else {
                stringBuilder.append(" UNION (");
                appendComparison(contextIDClause, folderIDsClause, selectClause, field, pattern, requireEmail, ignoreDistributionLists);
                stringBuilder.append(')');
            }
        }
        if (ignoreNonWebmailUsers) {
            stringBuilder.append(") AS U WHERE U.intfield01 NOT IN (SELECT intfield01 FROM prg_contacts as c JOIN user_configuration as u ON c.cid=u.cid and c.userid=u.user WHERE c.cid=").append(contextID).append(" AND (u.permissions & 1) <> 1)");
            stringBuilder.insert(0, '(');
            stringBuilder.insert(0, getSelectClause(fields, false, forUser));
        }
    }

    private void appendAutocompletePattern(String pattern, boolean requireEmail, boolean ignoreDistributionLists, boolean ignoreNonWebmailUsers, int[] folderIDs, int contextID, ContactField[] fields) throws OXException {
        String contextIDClause = getContextIDClause(contextID);
        String folderIDsClause = getFolderIDsClause(folderIDs);
        String selectClause = getSelectClause(fields, true);

        boolean first = true;
        EnumSet<ContactField> enumFields = getConfiguredIndexFields();
        for (ContactField field : enumFields) {
            if (first) {
                appendComparison(contextIDClause, folderIDsClause, selectClause, field, pattern, requireEmail, ignoreDistributionLists);
                first = false;
            } else {
                stringBuilder.append(" UNION (");
                appendComparison(contextIDClause, folderIDsClause, selectClause, field, pattern, requireEmail, ignoreDistributionLists);
                stringBuilder.append(')');
            }
        }
        if (ignoreNonWebmailUsers) {
            stringBuilder.append(") AS U WHERE U.intfield01 NOT IN (SELECT intfield01 FROM prg_contacts as c JOIN user_configuration as u ON c.cid=u.cid and c.userid=u.user WHERE c.cid=").append(contextID).append(" AND (u.permissions & 1) <> 1)");
            stringBuilder.insert(0, '(');
            stringBuilder.insert(0, getSelectClause(fields, false));
        }
    }

    private void appendAutocompletePattern(String tableAlias, String pattern, boolean requireEmail, boolean ignoreDistributionLists, boolean ignoreNonWebmailUsers, int[] folderIDs, int contextID, ContactField[] fields, int forUser) throws OXException {
        stringBuilder.append("SELECT ");
        stringBuilder.append(tableAlias).append('.').append(Mappers.CONTACT.get(fields[0]).getColumnLabel());
        for (int i = 1; i < fields.length; i++) {
            stringBuilder.append(',').append(tableAlias).append('.').append(Mappers.CONTACT.get(fields[i]).getColumnLabel());
        }
        stringBuilder.append(" FROM (");
        appendAutocompletePattern(pattern, requireEmail, ignoreDistributionLists, ignoreNonWebmailUsers, folderIDs, contextID, fields, forUser);
        stringBuilder.append(") AS ").append(tableAlias);
    }

    private void appendAutocompletePattern(String tableAlias, String pattern, boolean requireEmail, boolean ignoreDistributionLists, boolean ignoreNonWebmailUsers, int[] folderIDs, int contextID, ContactField[] fields) throws OXException {
        stringBuilder.append("SELECT ");
        stringBuilder.append(tableAlias).append('.').append(Mappers.CONTACT.get(fields[0]).getColumnLabel());
        for (int i = 1; i < fields.length; i++) {
            stringBuilder.append(',').append(tableAlias).append('.').append(Mappers.CONTACT.get(fields[i]).getColumnLabel());
        }
        stringBuilder.append(" FROM (");
        appendAutocompletePattern(pattern, requireEmail, ignoreDistributionLists, ignoreNonWebmailUsers, folderIDs, contextID, fields);
        stringBuilder.append(") AS ").append(tableAlias);
    }

    private void appendComparison(String contextIDClause, String folderIDsClause, String selectClause, ContactField field, String pattern, boolean needsEMail, boolean ignoreDistributionLists) throws OXException {
        stringBuilder.append('(').append(selectClause);
        //        if (IGNORE_INDEX_CID_FOR_UNIONS && ALTERNATIVE_INDEXED_FIELDS.contains(field)) {
        //            stringBuilder.append(" IGNORE INDEX (cid)");
        //        }
        stringBuilder.append(" WHERE ");
        stringBuilder.append(contextIDClause).append(" AND ");
        appendComparison(field, pattern);
        stringBuilder.append(" AND ").append(folderIDsClause);
        if (needsEMail) {
            stringBuilder.append(" AND (").append(getEMailAutoCompleteClause(ignoreDistributionLists)).append(')');
        } else if (ignoreDistributionLists) {
        	stringBuilder.append(" AND (").append(getIgnoreDistributionListsClause()).append(')');
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
     * Prepares search patterns from the tokenized query, appending wildcards as needed, performing ome optimizations regarding sole
     * wildcards or redundant patterns.
     *
     * @param tokens The tokenized query as supplied by the client
     * @param checkPatternLength <code>true</code> to check each pattern length against the configured restrictions, <code>false</code>, otherwise
     * @return The patterns
     * @throws OXException
     */
    static List<String> preparePatterns(List<String> tokens) throws OXException {
        List<String> resultingPatterns = new ArrayList<String>();
        for (String pattern : tokens) {
            pattern = StringCollection.prepareForSearch(pattern, false, true, true);
            if (Strings.isEmpty(pattern)) {
                /*
                 * ignore empty patterns
                 */
                continue;
            }
            /*
             * condense multiple not escaped wildcard characters
             * TODO: consider to also add this to Collection.prepareForSearch
             */
            pattern = pattern.replaceAll("(?<!\\\\)%+", "%");
            if ("%".equals(pattern)) {
                /*
                 * sole wildcard, match everything
                 */
                return Collections.singletonList(pattern);
            }
            if (resultingPatterns.contains(pattern)) {
                /*
                 * skip an equal pattern
                 */
                continue;
            }
            boolean addPattern = true;
            for (int i = 0; i < resultingPatterns.size(); i++) {
                /*
                 * prefer a more general pattern
                 */
                String patternPrefix = Strings.trimEnd(pattern, '%');
                String existingPatternPrefix = Strings.trimEnd(resultingPatterns.get(i), '%');
                if (patternPrefix.startsWith(existingPatternPrefix)) {
                    /*
                     * existing: ot% , new: otto% -> new can be ignored
                     */
                    addPattern = false;
                    break;
                }
                if (existingPatternPrefix.startsWith(patternPrefix)) {
                    /*
                     * existing: otto% , new: ot% -> existing can be replaced
                     */
                    resultingPatterns.set(i, pattern);
                    addPattern = false;
                    break;
                }
            }
            if (addPattern) {
                resultingPatterns.add(pattern);
            }
        }
        return resultingPatterns;
    }

    private static EnumSet<ContactField> getConfiguredIndexFields() {

        ArrayList<ContactField> contacFields = new ArrayList<ContactField>();
        try {
            ConfigurationService confServ = RdbServiceLookup.getService(ConfigurationService.class);
            List<String> fields = confServ.getProperty(AUTOCOMPLETE_CONFIGURATION, "", ",");
            if (fields == null || fields.isEmpty()) {
                return ALTERNATIVE_INDEXED_FIELDS;
            }
            for (String field : fields) {
                try {
                    contacFields.add(ContactField.valueOf(field));
                } catch (IllegalArgumentException ex) {
                    LOG.warn("\"" + field + "\" is not a valid column and will be skipped!");
                }
            }
        } catch (OXException ex) {
            LOG.error(ex.getMessage());
            return ALTERNATIVE_INDEXED_FIELDS;
        }
        return EnumSet.copyOf(contacFields);
    }
}
