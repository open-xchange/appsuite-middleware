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

package com.openexchange.find.basic.contacts;

import static com.openexchange.find.facet.Facets.newDefaultBuilder;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.SimpleTokenizer.tokenize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
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
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicContactsDriver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BasicContactsDriver extends AbstractContactFacetingModuleSearchDriver {

    static final ContactField[] ADDRESSBOOK_FIELDS = merge(AddressFacet.ADDRESS_FIELDS, EmailFacet.EMAIL_FIELDS, NameFacet.NAME_FIELDS, PhoneFacet.PHONE_FIELDS, new ContactField[] { ContactField.CATEGORIES, ContactField.COMPANY, ContactField.DEPARTMENT, ContactField.COMMERCIAL_REGISTER }
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
    static final ContactField[] SORT_FIELDS = new ContactField[] { ContactField.YOMI_LAST_NAME, ContactField.SUR_NAME, ContactField.YOMI_FIRST_NAME, ContactField.GIVEN_NAME, ContactField.DISPLAY_NAME, ContactField.YOMI_COMPANY, ContactField.COMPANY, ContactField.EMAIL1, ContactField.EMAIL2
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
            if (null == term) {
                /*
                 * no search results if any filter indicates a FALSE condition
                 */
                return SearchResult.EMPTY;
            }
            searchTerm.addSearchTerm(term);
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
         * extract requested contact fields & determine searched folders
         */
        int[] columnIDs = searchRequest.getColumns().getIntColumns();
        if (0 == columnIDs.length) {
            columnIDs = ColumnParser.parseColumns("list");
        }
        ContactField[] contactFields = ColumnParser.getFieldsToQuery(columnIDs, SORT_FIELDS);
        List<String> folderIds = Folders.getStringIDs(searchRequest, getModule(), session);
        /*
         * search
         */
        List<OXException> warnings = null;
        List<Contact> contacts;
        IDBasedContactsAccess access = Services.getIdBasedContactsAccessFactory().createAccess(session);
        try {
            access.set(ContactsParameters.PARAMETER_FIELDS, contactFields);
            access.set(ContactsParameters.PARAMETER_LEFT_HAND_LIMIT, I(searchRequest.getStart()));
            access.set(ContactsParameters.PARAMETER_RIGHT_HAND_LIMIT, I(searchRequest.getSize() + searchRequest.getStart())); //compensate for the diff in InternalAccess
            contacts = access.searchContacts(folderIds, searchTerm);
        } finally {
            access.finish();
            if (null != access.getWarnings() && 0 < access.getWarnings().size()) {
                warnings = new ArrayList<OXException>(access.getWarnings());
            }
        }
        /*
         * apply special sorting & convert resulting contacts
         */
        List<Document> contactDocuments = new ArrayList<>();
        if (null != contacts) {
            if (1 < contacts.size()) {
                SpecialAlphanumSortContactComparator comparator = new SpecialAlphanumSortContactComparator(session.getUser().getLocale());
                Collections.sort(contacts, comparator);
            }
            for (Contact contact : contacts) {
                contactDocuments.add(new ContactsDocument(contact));
            }
        }
        return new SearchResult(-1, searchRequest.getStart(), contactDocuments, searchRequest.getActiveFacets(), warnings);
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        /*
         * collect possible facets for current auto-complete iteration
         */
        List<Facet> facets = new LinkedList<>();
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

            facets.add(newSimpleBuilder(CommonFacetType.GLOBAL).withSimpleDisplayItem(prefix).withFilter(Filter.of(CommonFacetType.GLOBAL.getId(), prefixTokens)).build());
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
                    String objectId = contact.containsId() ? contact.getId() : String.valueOf(contact.getObjectID());
                    Filter filter = Filter.of(id, objectId);
                    String valueId = prepareFacetValueId(id, session.getContextId(), objectId);
                    builder.addValue(FacetValue.newBuilder(valueId).withDisplayItem(DisplayItems.convert(contact, session.getUser().getLocale(), Services.optionalService(I18nServiceRegistry.class))).withFilter(filter).build());
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
                searchTerm.addOperand(new ConstantOperand<>(queries.get(0)));
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

    private static ContactField[] merge(ContactField[]... fields) {
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
                    LOG.warn("\"{}\" is not a valid column or group and will be skipped!", field);
                }
            }
        } catch (OXException ex) {
            LOG.error(ex.getMessage());
            return ADDRESSBOOK_FIELDS;
        }
        return contacFields.toArray(new ContactField[contacFields.size()]);
    }

}
