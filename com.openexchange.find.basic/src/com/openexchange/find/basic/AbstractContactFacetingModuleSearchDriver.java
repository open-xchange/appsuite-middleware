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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.java.StringAllocator;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link AbstractContactFacetingModuleSearchDriver} - An abstract class for search drivers that support <i>contacts</i> aka <i>persons</i>
 * facet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractContactFacetingModuleSearchDriver implements ModuleSearchDriver {

    /**
     * Initializes a new {@link AbstractContactFacetingModuleSearchDriver}.
     */
    protected AbstractContactFacetingModuleSearchDriver() {
        super();
    }

    /**
     * The searchable contact fields.
     */
    protected static final ContactField[] CONTACT_FIELDS = new ContactField[] {
        ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.PRIVATE_FLAG, ContactField.DISPLAY_NAME, ContactField.GIVEN_NAME,
        ContactField.SUR_NAME, ContactField.TITLE, ContactField.POSITION, ContactField.INTERNAL_USERID, ContactField.EMAIL1,
        ContactField.EMAIL2, ContactField.EMAIL3, ContactField.COMPANY, ContactField.DISTRIBUTIONLIST,
        ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.IMAGE1_URL, ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2 };

    /**
     * Performs the contacts auto-complete search.
     *
     * @param session The session associated with this auto-complete request
     * @param autocompleteRequest The auto-complete request
     * @return The resulting contacts
     * @throws OXException If auto-complete search fails for any reason
     */
    protected List<Contact> autocompleteContacts(Session session, AutocompleteRequest autocompleteRequest) throws OXException {
        ContactService contactService = Services.getContactService();

        // Compose search object
        ContactSearchObject searchObject = new ContactSearchObject();
        {
            String prefix = new StringAllocator(autocompleteRequest.getPrefix()).append('*').toString();
            searchObject.setOrSearch(true);
            searchObject.setEmailAutoComplete(false);
            searchObject.setDisplayName(prefix);
            searchObject.setSurname(prefix);
            searchObject.setGivenName(prefix);
            searchObject.setEmail1(prefix);
            searchObject.setEmail2(prefix);
            searchObject.setEmail3(prefix);
        }

        // Sort options
        SortOptions sortOptions = new SortOptions();
        sortOptions.setRangeStart(0);
        sortOptions.setLimit(10);

        // Fire search
        final SearchIterator<Contact> it = contactService.searchContacts(session, searchObject, CONTACT_FIELDS, sortOptions);
        try {
            if (it == null || !it.hasNext()) {
                return Collections.emptyList();
            }
            List<Contact> contacts = new LinkedList<Contact>();
            while (it.hasNext()) {
                contacts.add(it.next());
            }
            return contacts;
        } finally {
            SearchIterators.close(it);
        }
    }

}
