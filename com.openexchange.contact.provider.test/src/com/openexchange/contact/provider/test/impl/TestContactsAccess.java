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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.contact.provider.test.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.provider.basic.BasicContactsAccess;
import com.openexchange.contact.provider.basic.BasicSearchAware;
import com.openexchange.contact.provider.basic.ContactsSettings;
import com.openexchange.contact.provider.test.impl.search.SearchPredicateParser;
import com.openexchange.contact.provider.test.storage.TestContactsStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.java.Strings;
import com.openexchange.search.SearchTerm;

/**
 * {@link TestContactsAccess} An "in memory" implementation of {@link ContactAccess} for testing purpose only
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class TestContactsAccess implements BasicContactsAccess, BasicSearchAware {

    private TestContactsStorage storage = null;
    private ContactsAccount account = null;
    private final SearchPredicateParser<Contact> predicateParser;

    //----------------------------------------------------------------------------------------------------------------------------------------------------

    // function to retrieve a value for a given contact field
    final BiFunction<ContactField, Contact, Object> fieldMapping = (contactField, contact) -> {
        if (contactField == null) {
            return null;
        } else if (contactField.equals(ContactField.FOLDER_ID)) {
            return contact.getFolderId();
        } else if (contactField.equals(ContactField.GIVEN_NAME)) {
            return contact.getGivenName();
        } else if (contactField.equals(ContactField.SUR_NAME)) {
            return contact.getSurName();
        } else if (contactField.equals(ContactField.EMAIL1)) {
            return contact.getEmail1();
        }
        return null;
    };

    // helper function to modify a search value before it is evaluated
    final BiFunction<ContactField, Object, Object> searchValueModifier = (contactField, searchValue) -> {
        //we will receive the "full" folder id  (eg. "con://21/0") but only the last part (folder id) will be relevant for the actual search
        if (contactField.equals(ContactField.FOLDER_ID)) {
            String folderId = (String) searchValue;
            if (Strings.isNotEmpty(folderId)) {
                String accountPrefix = "con://" + account.getAccountId() + "/";
                if (folderId.startsWith(accountPrefix)) {
                    return folderId.substring(accountPrefix.length());
                }
            }
        }
        return searchValue;
    };

    // a list of supported fields supported this test implementation
    //@formatter:off
    static List<ContactField> supportedSearchFields = Arrays.asList(new ContactField[] {
        ContactField.FOLDER_ID,
        ContactField.GIVEN_NAME,
        ContactField.SUR_NAME,
        ContactField.EMAIL1,
    });
    //@formatter:on

    //----------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link TestContactsAccess}.
     *
     * @param storage The "in memory" storage to get the contacts from
     * @param account The related {@link ContactsAccount}
     */
    public TestContactsAccess(TestContactsStorage storage, ContactsAccount account) {
        this.storage = Objects.requireNonNull(storage, "storage must not be null");
        this.account = Objects.requireNonNull(account, "account must not be null");
        this.predicateParser = new SearchPredicateParser<Contact>();
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void close() {
        //no-op
    }

    @Override
    public ContactsSettings getSettings() {
        ContactsSettings settings = new ContactsSettings();
        settings.setName(TestContactsProvider.PROVIDER_DISPLAY_NAME);
        return settings;
    }

    @Override
    public List<Contact> getContacts(List<String> contactIds) throws OXException {
        List<Contact> ret = new ArrayList<Contact>();
        for (String id : contactIds) {
            Contact contact = storage.get(id);
            if (contact != null) {
                ret.add(contact);
            }
        }
        return ret;
    }

    @Override
    public List<Contact> getContacts() throws OXException {
        return storage.getAll();
    }

    @Override
    public <O> List<Contact> searchContacts(SearchTerm<O> term) throws OXException {
        //Advanced search

        //Transform SearchTerm into a Predicate
        //@formatter:off
        @SuppressWarnings("unchecked")
        Predicate<Contact> searchPredicate = predicateParser.parse(
            (SearchTerm<Contact>) term,
            supportedSearchFields,
            fieldMapping,
            searchValueModifier);
        //@formatter:on

        //Do the search: Get only those contacts which matches the parsed predicate
        List<Contact> resultContacts = storage.get(searchPredicate);
        return resultContacts;
    }

    @Override
    public List<Contact> searchContacts(ContactsSearchObject contactSearch) throws OXException {
        //Simple search
        //@formatter:off
        return storage.get( c ->
            contactSearch.getGivenName() != null && c.getGivenName() != null && c.getGivenName().toLowerCase().contains(contactSearch.getGivenName().toLowerCase()) ||
            contactSearch.getSurname()  != null && c.getSurName() != null &&  c.getSurName().toLowerCase().contains(contactSearch.getSurname().toLowerCase()) ||
            contactSearch.getEmail1()  != null && c.getEmail1() != null && c.getEmail1().toLowerCase().contains(contactSearch.getEmail1().toLowerCase())
        );
        //@formatter:on
    }

    @Override
    public List<Contact> autocompleteContacts(String query) throws OXException {
        //Simple search
        ContactsSearchObject search = new ContactsSearchObject();
        search.setGivenName(query);
        search.setSurname(query);
        search.setEmail1(query);
        return searchContacts(search);
    }
}
