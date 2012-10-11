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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import com.openexchange.ajax.parser.ContactSearchtermSqlConverter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.CollationContactComparator;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.DefaultContactComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.search.SearchTerm;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactSearchMultiplexer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactSearchMultiplexer {

    private final ContactInterfaceDiscoveryService discoveryService;

    public ContactSearchMultiplexer(final ContactInterfaceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public SearchIterator<Contact> extendedSearch(final ServerSession session, final ContactSearchObject searchObj, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
        final int[] folders = searchObj.getFolders();
        final int contextId = session.getContextId();
        final List<SearchIterator<Contact>> searchIterators = new LinkedList<SearchIterator<Contact>>();
        if ((null != folders) && (folders.length > 0)) {
            final List<Integer> foldersForDefaultSearch = new ArrayList<Integer>(folders.length);
            for (final int folderId : folders) {
                if(discoveryService.hasSpecificContactInterface(folderId, contextId)) {
                    final ContactInterface contactInterface = discoveryService.newContactInterface(folderId, session);
                    searchObj.setFolders(folderId);
                    final SearchIterator<Contact> iterator = contactInterface.getContactsByExtendedSearch(searchObj, orderBy, order, collation, cols);
                    searchIterators.add(iterator);
                } else {
                    foldersForDefaultSearch.add(I(folderId));
                }
            }
            if(!foldersForDefaultSearch.isEmpty()) {
                searchObj.setFolders(foldersForDefaultSearch);
                final ContactInterface defaultContactInterface = discoveryService.newDefaultContactInterface(session);
                final SearchIterator<Contact> contactsByExtendedSearch = defaultContactInterface.getContactsByExtendedSearch(searchObj, orderBy, order, collation, cols);
                searchIterators.add(contactsByExtendedSearch);
            }

        } else {
            final List<ContactInterfaceProviderRegistration> registrations = discoveryService.getRegistrations(contextId);
            for (final ContactInterfaceProviderRegistration registration : registrations) {
                final ContactInterface contactInterface = registration.newContactInterface(session);
                searchObj.setFolders(registration.getFolderId());
                final SearchIterator<Contact> searchIterator = contactInterface.getContactsByExtendedSearch(searchObj, orderBy, order, collation, cols);
                searchIterators.add(searchIterator);
            }
            searchObj.clearFolders();
            final ContactInterface defaultContactInterface = discoveryService.newDefaultContactInterface(session);
            final SearchIterator<Contact> contactsByExtendedSearch = defaultContactInterface.getContactsByExtendedSearch(searchObj, orderBy, order, collation, cols);
            searchIterators.add(contactsByExtendedSearch);

        }
        if(searchIterators.size() == 1) {
            return searchIterators.get(0);
        }
        return new ContactMergerator(new DefaultContactComparator(orderBy, order, session.getUser().getLocale()), searchIterators);
    }

    public SearchIterator<Contact> extendedSearch(final ServerSession session, final SearchTerm<?> searchTerm, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
    	final ContactSearchtermSqlConverter conv = new ContactSearchtermSqlConverter();
    	conv.parse(searchTerm);
    	final List<String> folders = conv.getFolders();
        final int contextId = session.getContextId();
        final List<SearchIterator<Contact>> searchIterators = new LinkedList<SearchIterator<Contact>>();
        if(null != folders && folders.size() > 0) {
            final TIntList foldersForDefaultSearch = new TIntArrayList(folders.size());
            for (final String folderStr : folders) {
            	final int folderId = Integer.parseInt(folderStr);
                if(discoveryService.hasSpecificContactInterface(folderId, contextId)) {
                    final ContactInterface contactInterface = discoveryService.newContactInterface(folderId, session);
                    final SearchIterator<Contact> iterator = contactInterface.getContactsByExtendedSearch(searchTerm, orderBy, order, collation, cols);
                    searchIterators.add(iterator);
                } else {
                    foldersForDefaultSearch.add(folderId);
                }
            }
            if(!foldersForDefaultSearch.isEmpty()) {
                final ContactInterface defaultContactInterface = discoveryService.newDefaultContactInterface(session);
                final SearchIterator<Contact> contactsByExtendedSearch = defaultContactInterface.getContactsByExtendedSearch(searchTerm, orderBy, order, collation, cols);
                searchIterators.add(contactsByExtendedSearch);
            }
        } else {
            final List<ContactInterfaceProviderRegistration> registrations = discoveryService.getRegistrations(contextId);
            for (final ContactInterfaceProviderRegistration registration : registrations) {
                final ContactInterface contactInterface = registration.newContactInterface(session);
                final SearchIterator<Contact> searchIterator = contactInterface.getContactsByExtendedSearch(searchTerm, orderBy, order, collation, cols);
                searchIterators.add(searchIterator);
            }
            final ContactInterface defaultContactInterface = discoveryService.newDefaultContactInterface(session);
            final SearchIterator<Contact> contactsByExtendedSearch = defaultContactInterface.getContactsByExtendedSearch(searchTerm, orderBy, order, collation, cols);
            searchIterators.add(contactsByExtendedSearch);

        }
        if(searchIterators.size() == 1) {
            return searchIterators.get(0);
        }
        final Comparator<Contact> comparator = getContactComparator(orderBy, order, collation, session.getUser().getLocale());
        return new ContactMergerator(comparator, searchIterators);
    }

	private Comparator<Contact> getContactComparator(final int orderBy, final Order order, final String collation, final Locale locale) {
		if(collation == null) {
            return new DefaultContactComparator(orderBy, order, locale);
        }

		final SuperCollator myI18nMap = SuperCollator.get(collation);
		final ContactField myField = ContactField.getByValue(orderBy);

		return new CollationContactComparator(myField, order, myI18nMap.getJavaLocale());
	}
}
