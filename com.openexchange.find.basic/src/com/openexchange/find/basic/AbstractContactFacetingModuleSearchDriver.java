
package com.openexchange.find.basic;
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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractContactFacetingModuleSearchDriver} - An abstract class for search drivers that support <i>contacts</i> aka <i>persons</i>
 * facet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractContactFacetingModuleSearchDriver extends AbstractModuleSearchDriver {

    /**
     * The requested contact fields of the autocomplete search results.
     */
    protected static final ContactField[] CONTACT_FIELDS = { ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.PRIVATE_FLAG, ContactField.DISPLAY_NAME, ContactField.GIVEN_NAME, ContactField.SUR_NAME, ContactField.TITLE, ContactField.POSITION, ContactField.INTERNAL_USERID, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.COMPANY, ContactField.DISTRIBUTIONLIST, ContactField.NUMBER_OF_IMAGES, ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2, ContactField.DEPARTMENT, ContactField.IMAGE_LAST_MODIFIED
    };

    /**
     * The default sort order used to get pre-sorted results when retrieving contacts for auto-completion.
     */
    private static final SortOrder[] SORT_ORDER = new SortOrder[] { //new SortOrder(ContactField.VALUE, Order.DESCENDING),
        new SortOrder(ContactField.FOLDER_ID, Order.ASCENDING)
    };

    /**
     * The default maximum number of returned autocomplete search results.
     */
    private static final int DEFAULT_LIMIT = 10;

    /**
     * Initializes a new {@link AbstractContactFacetingModuleSearchDriver}.
     */
    protected AbstractContactFacetingModuleSearchDriver() {
        super();
    }

    /**
     * Performs the contacts auto-complete search. only contacts with at least one e-mail address are considered.
     *
     * @param session The session associated with this auto-complete request
     * @param autocompleteRequest The auto-complete request
     * @return The resulting contacts
     * @throws OXException If auto-complete search fails for any reason
     */
    protected List<Contact> autocompleteContacts(ServerSession session, AutocompleteRequest autocompleteRequest) throws OXException {
        return autocompleteContacts(session, autocompleteRequest, AutocompleteParameters.newInstance());
    }

    /**
     * Performs the contacts auto-complete search.
     *
     * @param session The session associated with this auto-complete request
     * @param autocompleteRequest The auto-complete request
     * @param requireEmail <code>true</code> if the returned contacts should have at least one e-mail address, <code>false</code>,
     *            otherwise
     * @return The resulting contacts
     * @throws OXException If auto-complete search fails for any reason
     */
    protected List<Contact> autocompleteContacts(ServerSession session, AutocompleteRequest autocompleteRequest, AutocompleteParameters parameters) throws OXException {
        return searchContacts(session, autocompleteRequest.getPrefix(), parameters, null, autocompleteRequest.getLimit());
    }

    /**
     * Performs the users auto-complete search.
     *
     * @param session The session associated with this auto-complete request
     * @param autocompleteRequest The auto-complete request
     * @return The resulting user contacts
     * @throws OXException If auto-complete search fails for any reason
     */
    protected List<Contact> autocompleteUsers(ServerSession session, AutocompleteRequest autocompleteRequest) throws OXException {
        AutocompleteParameters parameters = AutocompleteParameters.newInstance();
        parameters.put(AutocompleteParameters.REQUIRE_EMAIL, Boolean.FALSE);
        return searchContacts(session, autocompleteRequest.getPrefix(), parameters, Collections.singletonList(String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID)), autocompleteRequest.getLimit());
    }

    /**
     * Returns all mail addresses of a {@link Contact}.
     */
    protected static List<String> extractMailAddessesFrom(final Contact contact) {
        List<String> addrs = new ArrayList<>(3);
        String mailAddress = contact.getEmail1();
        if (Strings.isNotEmpty(mailAddress)) {
            addrs.add(mailAddress);
        }

        mailAddress = contact.getEmail2();
        if (Strings.isNotEmpty(mailAddress)) {
            addrs.add(mailAddress);
        }

        mailAddress = contact.getEmail3();
        if (Strings.isNotEmpty(mailAddress)) {
            addrs.add(mailAddress);
        }

        return addrs;
    }

    /**
     * Performs a contact search by prefix.
     *
     * @param session The server session
     * @param prefix The search prefix; no need to append a wild-card here
     * @param parameters The {@link AutocompleteParameters}
     * @param folderIDs A list of folder IDs to restrict the search for, or <code>null</code> to search in all visible folders
     * @return A list of found contacts, sorted using the {@link UseCountComparator} comparator
     * @throws OXException If contact search fails
     */
    private List<Contact> searchContacts(ServerSession session, String prefix, AutocompleteParameters parameters, List<String> folderIDs, int limit) throws OXException {
        if (false == session.getUserConfiguration().hasContact()) {
            return Collections.emptyList();
        }
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (Strings.isEmpty(prefix) || prefix.length() < minimumSearchCharacters) {
            return Collections.emptyList();
        }

        SortOptions sortOptions = new SortOptions(SORT_ORDER);
        sortOptions.setLimit(0 < limit ? limit : DEFAULT_LIMIT);

        List<Contact> contacts;
        IDBasedContactsAccess access = Services.getIdBasedContactsAccessFactory().createAccess(session);
        try {
            access.set(ContactsParameters.PARAMETER_FIELDS, CONTACT_FIELDS);
            access.set(ContactsParameters.PARAMETER_ORDER, sortOptions.getOrder()[0].getOrder());
            access.set(ContactsParameters.PARAMETER_ORDER_BY, sortOptions.getOrder()[0].getBy());
            access.set(ContactsParameters.PARAMETER_RIGHT_HAND_LIMIT, I(sortOptions.getLimit()));
            access.set(ContactsParameters.PARAMETER_REQUIRE_EMAIL, B(parameters.getBoolean(AutocompleteParameters.REQUIRE_EMAIL, true)));
            access.set(ContactsParameters.PARAMETER_IGNORE_DISTRIBUTION_LISTS, B(parameters.getBoolean(AutocompleteParameters.IGNORE_DISTRIBUTION_LISTS, false)));
            contacts = access.autocompleteContacts(folderIDs, prefix);
        } catch (OXException e) {
            if (ContactExceptionCodes.TOO_FEW_SEARCH_CHARS.equals(e)) {
                return Collections.emptyList();
            }
            throw e;
        } finally {
            access.finish();
        }
        if (null == contacts) {
            return Collections.emptyList();
        }
        if (1 < contacts.size()) {
            Collections.sort(contacts, new UseCountComparator(session.getUser().getLocale()));
            if (0 < sortOptions.getLimit() && contacts.size() > sortOptions.getLimit()) {
                contacts.subList(0, sortOptions.getLimit()).clear();
            }
        }
        return contacts;
    }
}
