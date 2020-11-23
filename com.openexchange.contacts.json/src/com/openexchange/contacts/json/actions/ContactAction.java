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

package com.openexchange.contacts.json.actions;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
@RestrictedAction(module = IDBasedContactAction.MODULE, type = RestrictedAction.Type.READ)
public abstract class ContactAction implements AJAXActionService {

    protected static final String MODULE = ContactActionFactory.MODULE;

    /** Named logger instance */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactAction.class);

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link IDBasedContactAction}.
     *
     * @param serviceLookup The service lookup to use
     */
    public ContactAction(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        return perform(new ContactRequest(requestData, session));
    }

    /**
     * Performs the request.
     *
     * @param request The request
     * @return The AJAX result
     * @throws OXException
     */
    protected abstract AJAXRequestResult perform(ContactRequest request) throws OXException;

    /**
     * Adds all contacts available from a search iterator into a collection.
     *
     * @param contacts The collection to add the contacts to
     * @param searchIterator The search iterator to get the contacts from
     * @param excludedUserID A user ID to exclude from the results, or a value <code>&lt; 0</code> to ignore
     * @return The latest last-modified timestamp of all added contacts
     * @throws OXException
     */
    protected static Date addContacts(Collection<Contact> contacts, SearchIterator<Contact> searchIterator, int excludedUserID) throws OXException {
        Date lastModified = new Date(0);
        if (null == searchIterator) {
            return lastModified;
        }
        try {
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                if (0 < excludedUserID && excludedUserID == contact.getInternalUserId()) {
                    continue; // skip
                }
                lastModified = getLatestModified(lastModified, contact);
                contacts.add(contact);
            }
        } finally {
            close(searchIterator);
        }
        return lastModified;
    }

    /**
     * Gets the latest modification date of the contact compared to another date.
     *
     * @param lastModified the date to compare
     * @param contact the contact
     * @return The contact's last modified if it's newer than the specified lastModified
     */
    protected static Date getLatestModified(final Date lastModified, final Contact contact) {
        final Date contactLastModified = contact.getLastModified();
        return null == contactLastModified || lastModified.after(contactLastModified) ? lastModified : contactLastModified;
    }

    /**
     * Returns the latest (newer) last modified timestamp from the specified contacts
     *
     * @param results The contacts
     * @return The newest last modified
     */
    protected static Date getLatestTimestamp(List<Contact> results) {
        if (null == results || results.isEmpty()) {
            return null;
        }
        long maximumTimestamp = 0L;
        for (Contact contact : results) {
            maximumTimestamp = Math.max(maximumTimestamp, contact.getLastModified().getTime());
        }
        return new Date(maximumTimestamp);
    }

    /**
     * Closes a search iterator silently
     *
     * @param searchIterator The search iterator to close
     * @throws OXException
     */
    protected static <T> void close(SearchIterator<T> searchIterator) {
        SearchIterators.close(searchIterator);
    }

    /**
     * Gets the contact service.
     *
     * @return the contact service
     * @throws OXException
     */
    ContactService getContactService() throws OXException {
        return requireService(ContactService.class);
    }

    /**
     * Gets the vCard service.
     *
     * @return The vCard service
     */
    VCardService getVCardService() throws OXException {
        return requireService(VCardService.class);
    }

    /**
     * Optionally gets the vCard storage service.
     *
     * @return The vCard storage service, or <code>null</code> if not available
     */
    VCardStorageService optVCardStorageService(int contextId) {
        VCardStorageFactory vCardStorageFactory = serviceLookup.getOptionalService(VCardStorageFactory.class);
        if (vCardStorageFactory != null) {
            return vCardStorageFactory.getVCardStorageService(serviceLookup.getService(ConfigViewFactory.class), contextId);
        }
        return null;
    }

    /**
     * Obtains a service from the local {@link ServiceLookup} instance and returns it. If the
     * service is not available, {@link ServiceExceptionCode#SERVICE_UNAVAILABLE} is thrown.
     *
     * @param <S> The service type
     * @param serviceClass The service class to obtain
     * @return The service
     * @throws OXException if the service is not available
     */
    <S extends Object> S requireService(Class<? extends S> clazz) throws OXException {
        return com.openexchange.osgi.Tools.requireService(clazz, serviceLookup);
    }
}
