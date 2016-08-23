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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Reloadable;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.storage.rdb.internal.RdbServiceLookup;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.contact.storage.rdb.sql.Table;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.Search;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.java.Enums;
import com.openexchange.java.SimpleTokenizer;
import com.openexchange.java.Strings;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadPools.ExpectedExceptionFactory;
import com.openexchange.tools.update.Tools;

/**
 * {@link FulltextAutocompleteAdapter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FulltextAutocompleteAdapter extends DefaultSearchAdapter {

    private static final int MAX_PATTERNS = 5;

    // --------------------------------------------------------------------------------------------------------------------------------

	private final StringBuilder stringBuilder;

    /**
     * Initializes a new {@link FulltextAutocompleteAdapter}.
     *
     * @param query The query, as supplied by the client
     * @param parameters The {@link AutocompleteParameters}
     * @param folderIDs The folder identifiers, or <code>null</code> if there's no restriction on folders
     * @param contextID The context identifier
     * @param charset The used charset
     */
    public FulltextAutocompleteAdapter(String query, AutocompleteParameters parameters, int[] folderIDs, int contextID, ContactField[] fields, String charset) throws OXException {
        this(query, parameters, folderIDs, contextID, fields, charset, true);
    }

    FulltextAutocompleteAdapter(String query, AutocompleteParameters parameters, int[] folderIDs, int contextID, ContactField[] fields, String charset, boolean checkPatternLength) throws OXException {
        super(charset);
        this.stringBuilder = new StringBuilder(2048);
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
        int forUser = parameters.getInteger(AutocompleteParameters.USER_ID, -1);
        if (null == patterns || 0 == patterns.size()) {
            stringBuilder.append(getSelectClause(fields, forUser)).append(" WHERE ").append(getContextIDClause(contextID)).append(" AND ").append(getFolderIDsClause(folderIDs));
            if (requireEmail) {
                stringBuilder.append(" AND (").append(getEMailAutoCompleteClause(ignoreDistributionLists)).append(')');
            } else if (ignoreDistributionLists) {
                stringBuilder.append(" AND (").append(getIgnoreDistributionListsClause()).append(')');
            }
        } else {
            stringBuilder.append(getSelectClause(fields, forUser)).append(" WHERE ").append(getContextIDClause(contextID)).append(" AND ").append(getFolderIDsClause(folderIDs));
            if (requireEmail) {
                stringBuilder.append(" AND (").append(getEMailAutoCompleteClause(ignoreDistributionLists)).append(')');
            } else if (ignoreDistributionLists) {
                stringBuilder.append(" AND (").append(getIgnoreDistributionListsClause()).append(')');
            }
            stringBuilder.append(" AND MATCH (").append(Mappers.CONTACT.getColumns(fulltextIndexFields())).append(") AGAINST ('");
            for (String pattern : patterns) {
                stringBuilder.append('+').append(pattern).append(' ');
            }
            stringBuilder.append("' IN BOOLEAN MODE)");
        }
    }

    /**
     * Prepares search patterns from the tokenized query, appending wildcards as needed, performing some optimizations regarding sole
     * wildcards or redundant patterns.
     *
     * @param tokens The tokenized query as supplied by the client
     * @param checkPatternLength <code>true</code> to check each pattern length against the configured restrictions, <code>false</code>, otherwise
     * @return The patterns
     */
    static List<String> preparePatterns(List<String> tokens) throws OXException {
        List<String> resultingPatterns = new ArrayList<String>();
        for (String token : tokens) {
            if (Strings.isEmpty(token)) {
                /*
                 * ignore empty patterns
                 */
                continue;
            }
            /*
             * replace all non-word characters in token
             */
            token = token.replaceAll("[+\\-><()~*\"@]+", " ");
            for (String pattern : Strings.splitByWhitespaces(token)) {
                pattern = pattern + '*';
                if ("*".equals(pattern)) {
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
                    String patternPrefix = Strings.trimEnd(pattern, '*');
                    String existingPatternPrefix = Strings.trimEnd(resultingPatterns.get(i), '*');
                    if (patternPrefix.startsWith(existingPatternPrefix)) {
                        /*
                         * existing: ot* , new: otto* -> new can be ignored
                         */
                        addPattern = false;
                        break;
                    }
                    if (existingPatternPrefix.startsWith(patternPrefix)) {
                        /*
                         * existing: otto* , new: ot* -> existing can be replaced
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
        }
        return resultingPatterns;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /** The associated reloadable instance */
    public static final Reloadable RELOADABLE = new Reloadable() {

        @Override
        public void reloadConfiguration(ConfigurationService configService) {
            {
                Boolean previous = fulltextAutocomplete;
                if (null != previous && previous.booleanValue() != configService.getBoolProperty("com.openexchange.contact.fulltextAutocomplete", false)) {
                    fulltextAutocomplete = null;
                    FULLTEXT_INDEX_SCHEMAS.clear();
                }
            }

            {
                ContactField[] previous = fulltextIndexFields;
                if (null != previous) {
                    EnumSet<ContactField> prevSet = EnumSet.copyOf(Arrays.asList(previous));
                    String defaultValue = "DISPLAY_NAME, SUR_NAME, GIVEN_NAME, TITLE, SUFFIX, MIDDLE_NAME, COMPANY, EMAIL1, EMAIL2, EMAIL3";
                    EnumSet<ContactField> curSet = EnumSet.copyOf(Enums.parseCsv(ContactField.class, configService.getProperty("com.openexchange.contact.fulltextIndexFields", defaultValue)));
                    if (!prevSet.equals(curSet)) {
                        fulltextIndexFields = null;
                        FULLTEXT_INDEX_SCHEMAS.clear();
                    }
                }
            }
        }

        @Override
        public com.openexchange.config.Interests getInterests() {
            return DefaultInterests.builder().propertiesOfInterest("com.openexchange.contact.fulltextAutocomplete", "com.openexchange.contact.fulltextIndexFields").build();
        }
    };

    // ----------------------------------------------------------------------------------------------------------------------------------

    static volatile ContactField[] fulltextIndexFields;

    /**
     * Gets the currently configured full-text index fields.
     *
     * @return The full-text index fields
     * @throws OXException If returning full-text index fields fails
     */
    public static ContactField[] fulltextIndexFields() throws OXException {
        ContactField[] tmp = fulltextIndexFields;
        if (null == tmp) {
            synchronized (FulltextAutocompleteAdapter.class) {
                tmp = fulltextIndexFields;
                if (null == tmp) {
                    String defaultValue = "DISPLAY_NAME, SUR_NAME, GIVEN_NAME, TITLE, SUFFIX, MIDDLE_NAME, COMPANY, EMAIL1, EMAIL2, EMAIL3";
                    ConfigurationService service = RdbServiceLookup.get().getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        return new ContactField[] { ContactField.DISPLAY_NAME, ContactField.SUR_NAME, ContactField.GIVEN_NAME,
                            ContactField.TITLE, ContactField.SUFFIX, ContactField.MIDDLE_NAME, ContactField.COMPANY, ContactField.EMAIL1,
                            ContactField.EMAIL2, ContactField.EMAIL3
                        };
                    }

                    String value = service.getProperty("com.openexchange.contact.fulltextIndexFields", defaultValue);
                    List<ContactField> fields = Enums.parseCsv(ContactField.class, value);
                    if (null == fields || 0 == fields.size()) {
                        throw ContactExceptionCodes.UNEXPECTED_ERROR.create("Invalid configuration setting for \"com.openexchange.contact.fulltextIndexFields\": " + value);
                    }
                    tmp = fields.toArray(new ContactField[fields.size()]);
                    fulltextIndexFields = tmp;
                }
            }
        }
        return tmp;
    }

    static volatile Boolean fulltextAutocomplete;

    /**
     * Checks whether FULLTEXT index is enabled
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code> (default)
     */
    private static boolean fulltextAutocomplete() {
        Boolean tmp = fulltextAutocomplete;
        if (null == tmp) {
            synchronized (FulltextAutocompleteAdapter.class) {
                tmp = fulltextAutocomplete;
                if (null == tmp) {
                    boolean defaultValue = false;
                    ConfigurationService service = RdbServiceLookup.get().getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.contact.fulltextAutocomplete", defaultValue));
                    fulltextAutocomplete = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    private static final ExpectedExceptionFactory<SQLException> EXCEPTION_FACTORY = new ExpectedExceptionFactory<SQLException>() {

        @Override
        public SQLException newUnexpectedError(Throwable t) {
            return new SQLException("unchecked", t);
        }

        @Override
        public Class<SQLException> getType() {
            return SQLException.class;
        }
    };

    static final ConcurrentMap<String, Future<Boolean>> FULLTEXT_INDEX_SCHEMAS = new ConcurrentHashMap<String, Future<Boolean>>(32, 0.9F, 1);

    /**
     * Gets a value indicating whether the supplied database connection points to a database schema that contains a special
     * <code>FULLTEXT</code> index aiding the auto-complete operation or not.
     *
     * @param connection The connection to check
     * @param contextID The context identifier
     * @return <code>true</code> if the <code>prg_contacts</code> table has the <code>autocomplete</code> index, <code>false</code>, otherwise
     */
    public static boolean hasFulltextIndex(final Connection connection, int contextID) throws OXException {
        if (false == fulltextAutocomplete()) {
            // Not enabled
            return false;
        }

        // Determine schema name
        String schemaName = getSchemaName(connection, contextID);

        // Check for existence of FULLTEXT index
        boolean removeOnError = false;
        boolean error = true;
        try {
            Future<Boolean> f = FULLTEXT_INDEX_SCHEMAS.get(schemaName);
            if (null == f) {
                FutureTask<Boolean> ft = new FutureTask<Boolean>(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        // Check for "autocomplete" FULLTEXT index
                        ContactField[] fields = fulltextIndexFields();
                        String[] columns = new String[fields.length];
                        for (int i = fields.length; i-- > 0;) {
                            columns[i] = Mappers.CONTACT.get(fields[i]).getColumnLabel();
                        }
                        String indexName = Tools.existsIndex(connection, Table.CONTACTS.getName(), columns);
                        return Boolean.valueOf((null != indexName) && indexName.startsWith("autocomplete"));
                    }
                });
                f = FULLTEXT_INDEX_SCHEMAS.putIfAbsent(schemaName, ft);
                if (null == f) {
                    f = ft;
                    removeOnError = true;
                    ft.run();
                }
            }

            Boolean value = ThreadPools.getFrom(f, EXCEPTION_FACTORY);
            error = false;
            return value.booleanValue();
        } catch (SQLException e) {
            if ("unchecked".equals(e.getMessage())) {
                Throwable cause = e.getCause();
                if (null != cause) {
                    throw (cause instanceof OXException) ? (OXException) cause : ContactExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
                }
            }
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (error && removeOnError) {
                FULLTEXT_INDEX_SCHEMAS.remove(schemaName);
            }
        }
    }

    private static final Cache<Integer, String> CACHE_SCHEMA_NAMES = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    private static String getSchemaName(final Connection connection, final int contextID) throws OXException {
        try {
            return CACHE_SCHEMA_NAMES.get(Integer.valueOf(contextID), new Callable<String>() {

                @Override
                public String call() throws Exception {
                    String schemaName = connection.getCatalog();
                    if (null == schemaName) {
                        schemaName = RdbServiceLookup.getService(DatabaseService.class).getSchemaName(contextID);
                        if (null == schemaName) {
                            throw ContactExceptionCodes.SQL_PROBLEM.create("No schema name for connection");
                        }
                    }
                    return schemaName;
                }
            });
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            if (cause instanceof SQLException) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(cause, cause.getMessage());
            }
            throw ContactExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

}
