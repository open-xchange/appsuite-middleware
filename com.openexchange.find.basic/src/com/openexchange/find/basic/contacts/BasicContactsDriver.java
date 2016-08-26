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

import static com.openexchange.find.facet.Facets.newDefaultBuilder;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.java.SimpleTokenizer.tokenize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.SortOptions;
import com.openexchange.contacts.json.mapping.ColumnParser;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Folders;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.contacts.ContactsDocument;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets.DefaultFacetBuilder;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.SpecialAlphanumSortContactComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link BasicContactsDriver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BasicContactsDriver extends AbstractContactFacetingModuleSearchDriver {

    static final ContactField[] ADDRESSBOOK_FIELDS = merge(
        AddressFacet.ADDRESS_FIELDS, EmailFacet.EMAIL_FIELDS, NameFacet.NAME_FIELDS, PhoneFacet.PHONE_FIELDS,
        new ContactField[] { ContactField.CATEGORIES, ContactField.COMPANY, ContactField.DEPARTMENT, ContactField.COMMERCIAL_REGISTER }
        //TOD=: more fields
    );

    private static final String ADDRESS_FIELDS_NAME = "ADDRESS_FIELDS";
    private static final String EMAIL_FIELDS_NAME = "EMAIL_FIELDS";
    private static final String NAME_FIELDS_NAME = "NAME_FIELDS";
    private static final String PHONE_FIELDS_NAME = "PHONE_FIELDS";
    private static final String USER_FIELDS_NAME = "USER_FIELDS";

    private static final String ADDRESSBOOK_FIELDS_CONFIG = "com.openexchange.contact.search.fields";

    private static final Logger LOG = LoggerFactory.getLogger(BasicContactsDriver.class);

    /**
     * Contact fields that are required to perform a {@link Contact#SPECIAL_SORTING} of search results.
     */
    static final ContactField[] SORT_FIELDS = new ContactField[] {
        ContactField.YOMI_LAST_NAME, ContactField.SUR_NAME, ContactField.YOMI_FIRST_NAME, ContactField.GIVEN_NAME,
        ContactField.DISPLAY_NAME, ContactField.YOMI_COMPANY, ContactField.COMPANY, ContactField.EMAIL1, ContactField.EMAIL2
    };

    /**
     * Initializes a new {@link BasicContactsDriver}.
     */
    public BasicContactsDriver() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.CONTACTS;
    }

    @Override
    public boolean isValidFor(ServerSession session) {
        return session.getUserConfiguration().hasContact();
    }

    @Override
    protected Set<FolderType> getSupportedFolderTypes(AutocompleteRequest autocompleteRequest, ServerSession session) {
        UserPermissionBits userPermissionBits = session.getUserPermissionBits();
        if (userPermissionBits.hasFullSharedFolderAccess()) {
            return ALL_FOLDER_TYPES;
        }

        Set<FolderType> types = EnumSet.noneOf(FolderType.class);
        types.add(FolderType.PRIVATE);
        types.add(FolderType.PUBLIC);
        return types;
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);
        /*
         * build filters
         */
        for (Filter filter : searchRequest.getFilters()) {
            SearchTerm<?> term = getSearchTerm(session, filter);
            if (null != term) {
                searchTerm.addSearchTerm(term);
            } else {
                /*
                 * no search results if any filter indicates a FALSE condition
                 */
                return SearchResult.EMPTY;
            }
        }
        /*
         * restrict to specific folders if set
         */
        SearchTerm<?> folderTypeTerm = getFolderTypeTerm(searchRequest, session);
        if (folderTypeTerm != null) {
            searchTerm.addSearchTerm(folderTypeTerm);
        }
        /*
         * combine with addressbook queries
         */
        SearchTerm<?> term = Utils.getSearchTerm(session, getConfiguredAddressbookFields(), searchRequest.getQueries());
        if (null != term) {
            searchTerm.addSearchTerm(term);
        }
        /*
         * check for valid search term
         */
        if (0 == searchTerm.getOperands().length) {
            return SearchResult.EMPTY;
        }
        /*
         * extract requested contact fields
         */
        ContactField[] contactFields;
        int[] columnIDs = searchRequest.getColumns().getIntColumns();
        if (0 == columnIDs.length) {
            columnIDs = ColumnParser.parseColumns("list");
        }
        contactFields = ColumnParser.getFieldsToQuery(columnIDs, SORT_FIELDS);
        /*
         * exclude context admin if requested
         */
        if (searchRequest.getOptions().includeContextAdmin()) {
            contactFields = ColumnParser.getFieldsToQuery(columnIDs, SORT_FIELDS);
        } else {
            ContactField[] mandatoryFields = new ContactField[SORT_FIELDS.length + 1];
            mandatoryFields[0] = ContactField.INTERNAL_USERID;
            System.arraycopy(SORT_FIELDS, 0, mandatoryFields, 1, SORT_FIELDS.length);
            contactFields = ColumnParser.getFieldsToQuery(columnIDs, mandatoryFields);
            CompositeSearchTerm excludeAdminTerm = new CompositeSearchTerm(CompositeOperation.OR);
            SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
            isNullTerm.addOperand(new ContactFieldOperand(ContactField.INTERNAL_USERID));
            excludeAdminTerm.addSearchTerm(isNullTerm);
            SingleSearchTerm notEqualsTerm = new SingleSearchTerm(SingleOperation.NOT_EQUALS);
            notEqualsTerm.addOperand(new ContactFieldOperand(ContactField.INTERNAL_USERID));
            notEqualsTerm.addOperand(new ConstantOperand<>(session.getContext().getMailadmin()));
            excludeAdminTerm.addSearchTerm(notEqualsTerm);
            searchTerm.addSearchTerm(excludeAdminTerm);
        }
        /*
         * search
         */
        List<Document> contactDocuments = new ArrayList<>();
        SortOptions sortOptions = new SortOptions(searchRequest.getStart(), searchRequest.getSize());
        List<Contact> contacts = null;
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator = Services.getContactService().searchContacts(session, searchTerm, contactFields, sortOptions);
            contacts = SearchIterators.asList(searchIterator);
        } finally {
            SearchIterators.close(searchIterator);
        }
        /*
         * apply special sorting & convert resulting contacts
         */
        if (null != contacts) {
            if (1 < contacts.size()) {
                SpecialAlphanumSortContactComparator comparator = new SpecialAlphanumSortContactComparator(session.getUser().getLocale());
                Collections.sort(contacts, comparator);
            }
            for (Contact contact : contacts) {
                contactDocuments.add(new ContactsDocument(contact));
            }
        }
        return new SearchResult(-1, searchRequest.getStart(), contactDocuments, searchRequest.getActiveFacets());
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        /*
         * collect possible facets for current auto-complete iteration
         */
        List<Facet> facets = new ArrayList<>();
        String prefix = autocompleteRequest.getPrefix();
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (Strings.isNotEmpty(prefix) && prefix.length() >= minimumSearchCharacters) {
            /*
             * add prefix-aware field facets
             */
            List<String> prefixTokens = tokenize(prefix, minimumSearchCharacters);
            if (prefixTokens.isEmpty()) {
                prefixTokens = Collections.singletonList(prefix);
            }

            facets.add(newSimpleBuilder(CommonFacetType.GLOBAL)
                .withSimpleDisplayItem(prefix)
                .withFilter(Filter.of(CommonFacetType.GLOBAL.getId(), prefixTokens))
                .build());
            facets.add(new NameFacet(prefix, prefixTokens));
            facets.add(new EmailFacet(prefix, prefixTokens));
            facets.add(new PhoneFacet(prefix, prefixTokens));
            facets.add(new DepartmentFacet(prefix, prefixTokens));
            facets.add(new AddressFacet(prefix, prefixTokens));

            AutocompleteParameters parameters = AutocompleteParameters.newInstance();
            parameters.put(AutocompleteParameters.REQUIRE_EMAIL, Boolean.FALSE);
            List<Contact> contacts = autocompleteContacts(session, autocompleteRequest, parameters);
            if (null != contacts && !contacts.isEmpty()) {
                DefaultFacetBuilder builder = newDefaultBuilder(ContactsFacetType.CONTACT);
                for (Contact contact : contacts) {
                    String id = ContactsFacetType.CONTACT.getId();
                    Filter filter = Filter.of(id, String.valueOf(contact.getObjectID()));
                    String valueId = prepareFacetValueId(id, session.getContextId(), Integer.toString(contact.getObjectID()));
                    builder.addValue(FacetValue.newBuilder(valueId)
                        .withDisplayItem(DisplayItems.convert(contact))
                        .withFilter(filter)
                        .build());
                }
                facets.add(builder.build());
            }
        }

        /*
         * add other facets
         */
        facets.add(ContactTypeFacet.getInstance());
        return new AutocompleteResult(facets);
    }

    private SearchTerm<?> getFolderTypeTerm(SearchRequest searchRequest, ServerSession session) throws OXException {
        List<String> folderIDs = Folders.getStringIDs(searchRequest, getModule(), session);
        if (folderIDs == null) {
            return null;
        }

        if (null != folderIDs && 0 < folderIDs.size()) {
            if (1 == folderIDs.size()) {
                String folderID = folderIDs.get(0);
                SingleSearchTerm searchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                searchTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
                searchTerm.addOperand(new ConstantOperand<>(folderID));
                return searchTerm;
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (String folderID : folderIDs) {
                    SingleSearchTerm searchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                    searchTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
                    searchTerm.addOperand(new ConstantOperand<>(folderID));
                    orTerm.addSearchTerm(searchTerm);
                }
                return orTerm;
            }
        }

        /*
         * no folders found, no results
         */
        return null;
    }

    /**
     * Creates a search term for the queries using a facet matching the supplied field.
     *
     * @param session The server session
     * @param field The filter field to select the matching facet
     * @param queries The queries
     * @return The search term, or <code>null</code> to indicate a <code>FALSE</code> condition with empty results.
     * @throws OXException
     */
    private SearchTerm<?> createSearchTerm(ServerSession session, String field, List<String> queries) throws OXException {
        ContactsFacetType type = ContactsFacetType.getById(field);
        if (null == type) {
            throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
        }
        switch (type) {
            case ADDRESS:
                return Utils.getSearchTerm(session, AddressFacet.ADDRESS_FIELDS, queries);
            case CONTACT:
                SingleSearchTerm searchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                searchTerm.addOperand(new ContactFieldOperand(ContactField.OBJECT_ID));
                searchTerm.addOperand(new ConstantOperand<>(Integer.valueOf(queries.get(0))));
                return searchTerm;
            case CONTACT_TYPE:
                return ContactTypeFacet.getInstance().getSearchTerm(session, queries);
            case EMAIL:
                return Utils.getSearchTerm(session, EmailFacet.EMAIL_FIELDS, queries);
            case NAME:
                return Utils.getSearchTerm(session, NameFacet.NAME_FIELDS, queries);
            case PHONE:
                return Utils.getSearchTerm(session, PhoneFacet.PHONE_FIELDS, queries);
            case DEPARTMENT:
                return Utils.getSearchTerm(session, DepartmentFacet.DEPARTMENT_FIELDS, queries);
            case USER_FIELDS:
                return Utils.getSearchTerm(session, UserFieldsFacet.USER_FIELDS, queries);
            default:
                throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
        }
    }

    /**
     * Gets the search term for a filter definition.
     *
     * @param session The server session
     * @param filter The filter
     * @return The search term, or <code>null</code> to indicate a <code>FALSE</code> condition with empty results.
     * @throws OXException
     */
    private SearchTerm<?> getSearchTerm(ServerSession session, Filter filter) throws OXException {
        List<String> fields = filter.getFields();
        List<String> queries = filter.getQueries();
        if (1 == fields.size() && 1 == queries.size()) {
            return createSearchTerm(session, fields.iterator().next(), queries);
        }
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (String field : fields) {
            SearchTerm<?> searchTerm = createSearchTerm(session, field, filter.getQueries());
            if (null != searchTerm) {
                compositeTerm.addSearchTerm(searchTerm);
            }
        }
        return 0 == compositeTerm.getOperands().length ? null : compositeTerm;
    }

    private static ContactField[] merge(ContactField[]...fields) {
        Set<ContactField> mergedFields = new HashSet<>();
        for (ContactField[] contactFields : fields) {
            mergedFields.addAll(Arrays.asList(contactFields));
        }
        return mergedFields.toArray(new ContactField[mergedFields.size()]);
    }

    private static ContactField[] getConfiguredAddressbookFields() {

        ArrayList<ContactField> contacFields = new ArrayList<>();
        try {
            ConfigurationService confServ = Services.getConfigurationService();
            List<String> fields = confServ.getProperty(ADDRESSBOOK_FIELDS_CONFIG, "", ",");
            if (fields == null || fields.isEmpty()) {
                return ADDRESSBOOK_FIELDS;
            }
            for (String field : fields) {
                switch (field) {

                    case ADDRESS_FIELDS_NAME:
                        contacFields.addAll(Arrays.asList(AddressFacet.ADDRESS_FIELDS));
                        continue;
                    case EMAIL_FIELDS_NAME:
                        contacFields.addAll(Arrays.asList(EmailFacet.EMAIL_FIELDS));
                        continue;
                    case NAME_FIELDS_NAME:
                        contacFields.addAll(Arrays.asList(NameFacet.NAME_FIELDS));
                        continue;
                    case PHONE_FIELDS_NAME:
                        contacFields.addAll(Arrays.asList(PhoneFacet.PHONE_FIELDS));
                        continue;
                    case USER_FIELDS_NAME:
                        contacFields.addAll(Arrays.asList(UserFieldsFacet.USER_FIELDS));
                        continue;
                }
                try {
                    contacFields.add(ContactField.valueOf(field));
                } catch (IllegalArgumentException ex) {
                    LOG.warn("\"" + field + "\" is not a valid column or group and will be skipped!");
                }
            }
        } catch (OXException ex) {
            LOG.error(ex.getMessage());
            return ADDRESSBOOK_FIELDS;
        }
        return contacFields.toArray(new ContactField[contacFields.size()]);
    }

}
