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

package com.openexchange.halo.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactMerger;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.halo.HaloExceptionCodes;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link ContactHaloImpl} - The <code>ContactHalo</code> implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactHaloImpl implements ContactHalo {

    private final Map<String, HaloContactDataSource> contactDataSources;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactHaloImpl}.
     *
     * @param services The service look-up
     */
    public ContactHaloImpl(final ServiceLookup services) {
        super();
        contactDataSources = new ConcurrentHashMap<String, HaloContactDataSource>(8);
        this.services = ExceptionOnAbsenceServiceLookup.valueOf(services);
    }

    @Override
    public AJAXRequestResult investigate(final String provider, final Contact contact, final AJAXRequestData req, final ServerSession session) throws OXException {
        final HaloContactDataSource dataSource = contactDataSources.get(provider);
        if (dataSource == null) {
            throw HaloExceptionCodes.UNKNOWN_PROVIDER.create(provider);
        }
        if (!dataSource.isAvailable(session)) {
            throw HaloExceptionCodes.UNAVAILABLE_PROVIDER.create(provider);
        }
        if (!(contact.getInternalUserId() > 0) && !contact.containsEmail1() & !contact.containsEmail2() & !contact.containsEmail3()) {
            throw HaloExceptionCodes.INVALID_CONTACT.create();
        }
        return dataSource.investigate(buildQuery(contact, session), req, session);
    }

    private HaloContactQuery buildQuery(Contact contact, final ServerSession session) throws OXException {
        final UserService userService = services.getService(UserService.class);
        final ContactService contactService = services.getService(ContactService.class);

        final HaloContactQuery contactQuery = new HaloContactQuery();

        // Try to find a user with a given eMail address

        User user = null;
        if (contact.getInternalUserId() > 0) {
            user = userService.getUser(contact.getInternalUserId(), session.getContext());
        }

        if (user == null) {
            try {
                user = userService.searchUser(contact.getEmail1(), session.getContext(), false);
            } catch (final OXException x) {
                // Don't care. This is all best effort anyway.
            }
        }

        if (user == null) {
            try {
                user = userService.searchUser(contact.getEmail2(), session.getContext(), false);
            } catch (final OXException x) {
                // Don't care. This is all best effort anyway.
            }
        }

        if (user == null) {
            try {
                user = userService.searchUser(contact.getEmail3(), session.getContext(), false);
            } catch (final OXException x) {
                // Don't care. This is all best effort anyway.
            }
        }

        contactQuery.setUser(user);
        final List<Contact> contactsToMerge = new ArrayList<Contact>();
        if (user != null) {
            // Load the associated contact
            contact = contactService.getUser(session, user.getId());
            contactsToMerge.add(contact);
        } else {
            // Try to find a contact
            final ContactSearchObject contactSearch = new ContactSearchObject();
            contactSearch.setEmail1(contact.getEmail1());
            contactSearch.setEmail2(contact.getEmail1());
            contactSearch.setEmail3(contact.getEmail1());
            contactSearch.setOrSearch(true);
            SearchIterator<Contact> iterator = null;
            try {
                iterator = contactService.searchContacts(session, contactSearch);
                while (iterator.hasNext()) {
                    contactsToMerge.add(iterator.next());
                }
            } finally {
                if (null != iterator) {
                    iterator.close();
                }
            }
        }
        contactQuery.setMergedContacts(contactsToMerge);

        final ContactMerger contactMerger = new ContactMerger(false);
        for (final Contact c : contactsToMerge) {
            contact = contactMerger.merge(contact, c);
        }
        contactQuery.setContact(contact);
        return contactQuery;
    }

    @Override
    public List<String> getProviders(final ServerSession session) throws OXException {
        final ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
        final ConfigView view = configViews.getView(session.getUserId(), session.getContextId());
        final List<String> providers = new ArrayList<String>();
        for (final Entry<String, HaloContactDataSource> entry : contactDataSources.entrySet()) {
            if (entry.getValue().isAvailable(session)) {
                final String provider = entry.getKey();
                final ComposedConfigProperty<Boolean> property = view.property(provider, boolean.class);
                if (!property.isDefined() || property.get().booleanValue()) {
                    providers.add(provider);
                }
            }
        }
        return providers;
    }

    public void addContactDataSource(final HaloContactDataSource ds) {
        contactDataSources.put(ds.getId(), ds);
    }

    public void removeContactDataSource(final HaloContactDataSource ds) {
        contactDataSources.remove(ds.getId());
    }

}
