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

package com.openexchange.find.basic.contacts;

import static com.openexchange.java.Strings.isEmpty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.ModuleConfig;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.CommonStrings;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.ContactTypeDisplayItem;
import com.openexchange.find.common.FolderTypeDisplayItem;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.contacts.ContactsDocument;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.MandatoryFilter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;

/**
 * TODO {@link MockContactsDriver}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class MockContactsDriver extends AbstractContactFacetingModuleSearchDriver {

    private static final Set<String> PERSONS_FILTER_FIELDS = Collections.<String> unmodifiableSet(new HashSet<String>(Arrays.asList(
        "from",
        "to",
        "cc")));

    /**
     * {@inheritDoc}
     */
    @Override
    public Module getModule() {
        return Module.CONTACTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidFor(ServerSession session) {
        return session.getUserConfiguration().hasContact();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleConfig getConfiguration(ServerSession session) throws OXException {

        final List<Facet> staticFacets = new LinkedList<Facet>();
        {
            final String id = "address";
            final FacetValue staticFacetValue = new FacetValue(id, new SimpleDisplayItem(id), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singleton(id),
                "override"));
            final Facet addressFacet = new Facet(ContactsFacetType.ADDRESS, Collections.singletonList(staticFacetValue));
            staticFacets.add(addressFacet);
        }

        {
            final String id = "address_book";
            final FacetValue staticFacetValue = new FacetValue(id, new SimpleDisplayItem(id), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singleton(id),
                "override"));
            final Facet addressBookFacet = new Facet(ContactsFacetType.ADDRESSBOOK, Collections.singletonList(staticFacetValue));
            staticFacets.add(addressBookFacet);
        }

        {
            final String id = "email";
            final FacetValue staticFacetValue = new FacetValue(id, new SimpleDisplayItem(id), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singleton(id),
                "override"));
            final Facet emailFacet = new Facet(ContactsFacetType.EMAIL, Collections.singletonList(staticFacetValue));
            staticFacets.add(emailFacet);
        }

        {
            final String id = "name";
            final FacetValue staticFacetValue = new FacetValue(id, new SimpleDisplayItem(id), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singleton(id),
                "override"));
            final Facet nameFacet = new Facet(ContactsFacetType.NAME, Collections.singletonList(staticFacetValue));
            staticFacets.add(nameFacet);
        }

        {
            final String id = "phone";
            final FacetValue staticFacetValue = new FacetValue(id, new SimpleDisplayItem(id), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singleton(id),
                "override"));
            final Facet phoneFacet = new Facet(ContactsFacetType.PHONE, Collections.singletonList(staticFacetValue));
            staticFacets.add(phoneFacet);
        }

        {
            final String id = "contact_type";
            final List<FacetValue> contactTypes = new ArrayList<FacetValue>(2);
            contactTypes.add(new FacetValue(ContactTypeDisplayItem.Type.CONTACT.getIdentifier(), new ContactTypeDisplayItem(
                CommonStrings.CONTACT_TYPE_CONTACT,
                ContactTypeDisplayItem.Type.CONTACT), FacetValue.UNKNOWN_COUNT, new Filter(
                    Collections.singleton(id),
                    ContactTypeDisplayItem.Type.CONTACT.getIdentifier())));
            contactTypes.add(new FacetValue(ContactTypeDisplayItem.Type.DISTRIBUTION_LIST.getIdentifier(), new ContactTypeDisplayItem(
                CommonStrings.CONTACT_TYPE_DISTRIBUTION_LIST,
                ContactTypeDisplayItem.Type.DISTRIBUTION_LIST), FacetValue.UNKNOWN_COUNT, new Filter(
                    Collections.singleton(id),
                    ContactTypeDisplayItem.Type.DISTRIBUTION_LIST.getIdentifier())));
            final Facet contactTypeFacet = new Facet(ContactsFacetType.TYPE, contactTypes);
            staticFacets.add(contactTypeFacet);
        }

        {
            final String id = "folder_type";
            final List<FacetValue> folderTypes = new ArrayList<FacetValue>(3);
            folderTypes.add(new FacetValue(FolderTypeDisplayItem.Type.PRIVATE.getIdentifier(), new FolderTypeDisplayItem(
                CommonStrings.FOLDER_TYPE_PRIVATE,
                FolderTypeDisplayItem.Type.PRIVATE), FacetValue.UNKNOWN_COUNT, new Filter(
                    Collections.singleton(id),
                    FolderTypeDisplayItem.Type.PRIVATE.getIdentifier())));
            folderTypes.add(new FacetValue(FolderTypeDisplayItem.Type.PUBLIC.getIdentifier(), new FolderTypeDisplayItem(
                CommonStrings.FOLDER_TYPE_PUBLIC,
                FolderTypeDisplayItem.Type.PUBLIC), FacetValue.UNKNOWN_COUNT, new Filter(
                    Collections.singleton(id),
                    FolderTypeDisplayItem.Type.PUBLIC.getIdentifier())));
            folderTypes.add(new FacetValue(FolderTypeDisplayItem.Type.SHARED.getIdentifier(), new FolderTypeDisplayItem(
                CommonStrings.FOLDER_TYPE_SHARED,
                FolderTypeDisplayItem.Type.SHARED), FacetValue.UNKNOWN_COUNT, new Filter(
                    Collections.singleton(id),
                    FolderTypeDisplayItem.Type.SHARED.getIdentifier())));

            final Facet folderTypeFacet = new Facet(ContactsFacetType.FOLDERS, folderTypes);
            staticFacets.add(folderTypeFacet);
        }

        // No mandatory filters to define for contacts
        final List<MandatoryFilter> mandatoryFilters = Collections.emptyList();

        return new ModuleConfig(getModule(), staticFacets, mandatoryFilters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        ContactService contactService = Services.getContactService();

        // Compose search object
        ContactSearchObject searchObject = new ContactSearchObject();

        // Sort options
        SortOptions sortOptions = new SortOptions();
        sortOptions.setRangeStart(0);
        sortOptions.setLimit(10);

        // Fire search
        final SearchIterator<Contact> it = contactService.searchContacts(session, searchObject, CONTACT_FIELDS, sortOptions);

        List<Document> documents = new ArrayList<Document>(it.size());
        try {
            while (it.hasNext()) {
                documents.add(new ContactsDocument(it.next()));
            }
        } finally {
            SearchIterators.close(it);
        }

        return new SearchResult(-1, searchRequest.getStart(), documents);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        final List<Facet> facets = new LinkedList<Facet>();

        // Add the facet for contacts that needs to be auto-completed
        {
            final List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
            final List<FacetValue> contactValues = new ArrayList<FacetValue>(contacts.size());
            for (final Contact contact : contacts) {
                // Get appropriate E-Mail address
                String sInfo = contact.getEmail1();
                if (isEmpty(sInfo)) {
                    sInfo = contact.getEmail2();
                    if (isEmpty(sInfo)) {
                        sInfo = contact.getEmail3();
                    }
                }
                if (sInfo != null) {
                    final Filter filter = new Filter(PERSONS_FILTER_FIELDS, sInfo);
                    contactValues.add(new FacetValue(prepareFacetValueId(
                        "contact",
                        session.getContextId(),
                        Integer.toString(contact.getObjectID())), new ContactDisplayItem(contact), FacetValue.UNKNOWN_COUNT, filter));
                }

                // Get display name
                sInfo = contact.getDisplayName();
                if (!isEmpty(sInfo)) {
                    final Filter filter = new Filter(PERSONS_FILTER_FIELDS, sInfo);
                    contactValues.add(new FacetValue(prepareFacetValueId(
                        "contact",
                        session.getContextId(),
                        Integer.toString(contact.getObjectID())), new ContactDisplayItem(contact), FacetValue.UNKNOWN_COUNT, filter));
                }
            }
            facets.add(new Facet(ContactsFacetType.CONTACTS, contactValues));
        }

        return new AutocompleteResult(facets);
    }
}
