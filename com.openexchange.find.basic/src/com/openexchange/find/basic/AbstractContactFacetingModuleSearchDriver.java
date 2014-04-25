package com.openexchange.find.basic;
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.resource.Resource;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIterators;
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
    protected static final ContactField[] CONTACT_FIELDS = new ContactField[] {
        ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.PRIVATE_FLAG, ContactField.DISPLAY_NAME, ContactField.GIVEN_NAME,
        ContactField.SUR_NAME, ContactField.TITLE, ContactField.POSITION, ContactField.INTERNAL_USERID, ContactField.EMAIL1,
        ContactField.EMAIL2, ContactField.EMAIL3, ContactField.COMPANY, ContactField.DISTRIBUTIONLIST,
        ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.IMAGE1_URL, ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2 };

    /**
     * The default sort order used to get pre-sorted results when retrieving contacts for auto-completion.
     */
    private static final SortOrder[] SORT_ORDER = new SortOrder[] {
        new SortOrder(ContactField.USE_COUNT, Order.DESCENDING), new SortOrder(ContactField.FOLDER_ID, Order.ASCENDING)
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
     * Performs the contacts auto-complete search.
     *
     * @param session The session associated with this auto-complete request
     * @param autocompleteRequest The auto-complete request
     * @return The resulting contacts
     * @throws OXException If auto-complete search fails for any reason
     */
    protected List<Contact> autocompleteContacts(ServerSession session, AutocompleteRequest autocompleteRequest) throws OXException {
        return searchContacts(
            session,
            autocompleteRequest.getPrefix(),
            true,
            null,
            autocompleteRequest.getLimit(),
            autocompleteRequest.getOptions().includeContextAdmin());
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
        return searchContacts(
            session,
            autocompleteRequest.getPrefix(),
            false,
            Collections.singletonList(String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID)),
            autocompleteRequest.getLimit(),
            autocompleteRequest.getOptions().includeContextAdmin());
    }

    /**
     * Performs the resources auto-complete search.
     *
     * @param session The session associated with this auto-complete request
     * @param autocompleteRequest The auto-complete request
     * @return The resulting resources
     * @throws OXException If auto-complete search fails for any reason
     */
    protected List<Resource> autocompleteResources(ServerSession session, AutocompleteRequest autocompleteRequest) throws OXException {
        Resource[] resources = Services.getResourceService().searchResources(autocompleteRequest.getPrefix(), session.getContext());
        if (null == resources) {
            return Collections.emptyList();
        }
        List<Resource> resourceList = Arrays.asList(resources);
        if (1 < resourceList.size()) {
            Collections.sort(resourceList, new Comparator<Resource>() {

                @Override
                public int compare(Resource o1, Resource o2) {
                    String name1 = null != o1 ? o1.getDisplayName() : null;
                    String name2 = null != o2 ? o2.getDisplayName() : null;
                    if (null == name1) {
                        return null == name2 ? 0 : -1;
                    } else if (null == name2) {
                        return 1;
                    } else {
                        return name1.compareTo(name2);
                    }
                }
            });
            if (autocompleteRequest.getLimit() > resourceList.size()) {
                resourceList.subList(0, autocompleteRequest.getLimit()).clear();
            }
        }
        return resourceList;
    }

    /**
     * Performs a contact search by prefix.
     *
     * @param session The server session
     * @param prefix The search prefix; no need to append a wild-card here
     * @param requireEmail <code>true</code> if the returned contacts should have at least one e-mail address, <code>false</code>,
     *                     otherwise
     * @param folderIDs A list of folder IDs to restrict the search for, or <code>null</code> to search in all visible folders
     * @param includeAdmin <code>true</code> to include the context administrator from search results, <code>false</code>, otherwise
     * @return A list of found contacts, sorted using the {@link UseCountComparator} comparator
     * @throws OXException If contact search fails
     */
    private List<Contact> searchContacts(ServerSession session, String prefix, boolean requireEmail, List<String> folderIDs, int limit, boolean includeAdmin) throws OXException {
        SortOptions sortOptions = new SortOptions(SORT_ORDER);
        sortOptions.setLimit(0 < limit ? limit : DEFAULT_LIMIT);
        int excludedAdminID = !includeAdmin ? session.getContext().getMailadmin() : -1;
        List<Contact> contacts = null;
        {
            SearchIterator<Contact> searchIterator = null;
            try {
                searchIterator = Services.getContactService().searchContacts(session, getSearchObject(prefix, requireEmail, folderIDs), CONTACT_FIELDS, sortOptions);
                if (0 < excludedAdminID) {
                    contacts = new ArrayList<Contact>();
                    while (searchIterator.hasNext()) {
                        Contact contact = searchIterator.next();
                        if (excludedAdminID != contact.getInternalUserId()) {
                            contacts.add(contact);
                        }
                    }
                } else {
                    contacts = SearchIteratorAdapter.toList(searchIterator);
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        if (null == contacts) {
            return Collections.emptyList();
        }
        if (1 < contacts.size()) {
            Collections.sort(contacts, new UseCountComparator(true, session.getUser().getLocale()));
            if (0 < sortOptions.getLimit() && contacts.size() > sortOptions.getLimit()) {
                contacts.subList(0, sortOptions.getLimit()).clear();
            }
        }
        return contacts;
    }

    /**
     * Constructs a search object using the supplied parameters.
     *
     * @param prefix The prefix for the search
     * @param requireEmail <code>true</code> if the returned contacts should have at least one e-mail address, <code>false</code>,
     *                     otherwise
     * @param folderIDs A list of folder IDs to restrict the search for, or <code>null</code> to search in all visible folders
     * @return The prepared search object
     * @throws OXException
     */
    private static ContactSearchObject getSearchObject(String prefix, boolean requireEmail, List<String> folderIDs) throws OXException {
        ConfigurationService config = Services.getConfigurationService();
        int minSearchCharacters = config.getIntProperty("com.openexchange.MinimumSearchCharacters", 0);
        ContactSearchObject searchObject = new ContactSearchObject();
        searchObject.setOrSearch(true);
        searchObject.setEmailAutoComplete(requireEmail);
        if (prefix.length() >= minSearchCharacters) {
            searchObject.setDisplayName(prefix);
            searchObject.setSurname(prefix);
            searchObject.setGivenName(prefix);
            searchObject.setEmail1(prefix);
            searchObject.setEmail2(prefix);
            searchObject.setEmail3(prefix);
        }

        if (null != folderIDs) {
            for (String folderID : folderIDs) {
                try {
                    searchObject.addFolder(Integer.valueOf(folderID));
                } catch (NumberFormatException e) {
                    //TODO: FindExceptionCode.INTERNAL_ERROR ?
                    throw OXException.general("Non-numerical folder IDs are not supported", e);
                }
            }
        }
        return searchObject;
    }

}
