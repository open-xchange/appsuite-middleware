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

package com.openexchange.halo.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactMerger;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactImageSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.halo.HaloExceptionCodes;
import com.openexchange.halo.Picture;
import com.openexchange.java.Strings;
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
    private final List<HaloContactImageSource> imageSources;

    private final Lock imageSourcesLock = new ReentrantLock();

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactHaloImpl}.
     *
     * @param services The service look-up
     */
    public ContactHaloImpl(final ServiceLookup services) {
        super();
        contactDataSources = new ConcurrentHashMap<String, HaloContactDataSource>(8);
        imageSources = new ArrayList<HaloContactImageSource>();
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
        return dataSource.investigate(buildQuery(contact, session, true), req, session);
    }

    @Override
    public Picture getPicture(Contact contact, ServerSession session) throws OXException {
        HaloContactQuery contactQuery = buildQuery(contact, session, true);

        for (HaloContactImageSource source : imageSources) {
            if (!source.isAvailable(session)) {
                continue;
            }
            Picture picture = source.getPicture(contactQuery, session);
            if (picture != null){
                StringBuilder etagBuilder = new StringBuilder();
                etagBuilder.append(source.getClass().getName()).append("://").append(picture.getEtag());

                picture.setEtag(etagBuilder.toString());

                return picture;
            }
        }
        return null;
    }

    @Override
    public String getPictureETag(Contact contact, ServerSession session) throws OXException {
        HaloContactQuery contactQuery = buildQuery(contact, session, false);
        for (HaloContactImageSource source : imageSources) {
            if (!source.isAvailable(session)) {
                continue;
            }
            String eTag = source.getPictureETag(contactQuery, session);
            if (eTag != null) {
                StringBuilder etagBuilder = new StringBuilder();
                etagBuilder.append(source.getClass().getName()).append("://").append(eTag);
                return etagBuilder.toString();
            }
        }
        return null;
    }

    // Friendly for testing
    HaloContactQuery buildQuery(final Contact contact, final ServerSession session, final boolean withBytes) throws OXException {
        final UserService userService = services.getService(UserService.class);
        final ContactService contactService = services.getService(ContactService.class);
        final HaloContactQuery contactQuery = new HaloContactQuery();
        final ContactField[] fields = withBytes ? null : new ContactField[] { ContactField.OBJECT_ID, ContactField.LAST_MODIFIED, ContactField.FOLDER_ID };

        Contact resultContact = contact;

        // Look-up associated user...
        User user = null;

        // Prefer look-up by user identifier
        {
            final int userId = resultContact.getInternalUserId();
            if (userId > 0) {
                user = userService.getUser(userId, session.getContext());
            }
        }

        // Check by object/folder identifier
        if (null == user) {
            if (resultContact.getObjectID() > 0 && resultContact.getParentFolderID() > 0) {
                Contact loaded = contactService.getContact(session, Integer.toString(resultContact.getParentFolderID()), Integer.toString(resultContact.getObjectID()), fields);
                contactQuery.setContact(loaded);
                contactQuery.setMergedContacts(Arrays.asList(loaded));
                return contactQuery;
            }
        }

        // Try to find a user with a given eMail address
        if (user == null && resultContact.containsEmail1()) {
            try {
                user = userService.searchUser(resultContact.getEmail1(), session.getContext(), false);
            } catch (final OXException x) {
                // Don't care. This is all best effort anyway.
            }
        }

        if (user == null && resultContact.containsEmail2()) {
            try {
                user = userService.searchUser(resultContact.getEmail2(), session.getContext(), false);
            } catch (final OXException x) {
                // Don't care. This is all best effort anyway.
            }
        }

        if (user == null && resultContact.containsEmail3()) {
            try {
                user = userService.searchUser(resultContact.getEmail3(), session.getContext(), false);
            } catch (final OXException x) {
                // Don't care. This is all best effort anyway.
            }
        }

        contactQuery.setUser(user);
        final List<Contact> contactsToMerge = new LinkedList<Contact>();
        if (user != null) {
            // Load the associated contact
            resultContact = contactService.getUser(session, user.getId(), fields);
            contactsToMerge.add(resultContact);
        } else if (false == Strings.isEmpty(resultContact.getEmail1())){
            // Try to find a contact
            final ContactSearchObject contactSearch = new ContactSearchObject();
            final String email = resultContact.getEmail1();
            contactSearch.setEmail1(email);
            contactSearch.setEmail2(email);
            contactSearch.setEmail3(email);
            contactSearch.setOrSearch(true);
            contactSearch.setExactMatch(true);
            SearchIterator<Contact> iterator = null;
            try {
                iterator = contactService.searchContacts(session, contactSearch, fields);
                while (iterator.hasNext()) {
                    Contact c = iterator.next();
                    if (checkEmails(c, email)) {
                        contactsToMerge.add(c);
                    }
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
            resultContact = contactMerger.merge(resultContact, c);
        }
        contactQuery.setContact(resultContact);
        return contactQuery;
    }

    private boolean checkEmails(Contact c, String email1) {
        return (email1.equalsIgnoreCase(c.getEmail1()) || email1.equalsIgnoreCase(c.getEmail2()) || email1.equalsIgnoreCase(c.getEmail3()));
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

    public void addContactImageSource(final HaloContactImageSource is) {
        try {
            imageSourcesLock.lock();
            imageSources.add(is);
            Collections.sort(imageSources, new Comparator<HaloContactImageSource>() {

                @Override
                public int compare(HaloContactImageSource o1, HaloContactImageSource o2) {
                    return o2.getPriority() - o1.getPriority();
                }

            });
        } finally {
            imageSourcesLock.unlock();
        }
    }

    public void removeContactImageSource(final HaloContactImageSource is) {
        try {
            imageSourcesLock.lock();
            imageSources.remove(is);

        } finally {
            imageSourcesLock.unlock();
        }
    }

}
